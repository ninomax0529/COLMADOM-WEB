/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.MovimientoInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.modelo.Lote;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepo movimientoRepo;
    private final ArticuloRepo articuloRepo;

    @Autowired
    private com.maxsoft.application.repo.ArticuloAlmacenRepo articuloAlmacenRepo;
    @Autowired
    private LoteService loteService;
    AlmacenService almacenService;
    ArticuloAlmacenService articuloAlmacenService;

    @Autowired
    public MovimientoInventarioServiceImpl(MovimientoInventarioRepo movimientoRepo,
            ArticuloRepo articuloRepo,
            AlmacenService almacenService,
            ArticuloAlmacenService articuloAlmacenService
    ) {
        this.movimientoRepo = movimientoRepo;
        this.articuloRepo = articuloRepo;
        this.almacenService = almacenService;
        this.articuloAlmacenService = articuloAlmacenService;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MovimientoInventario registrarMovimiento(
            Articulo articuloInput,
            Almacen alm,
            ArticuloEmpaque articuloEmpaque,
            BigDecimal factorConversion,
            BigDecimal cantidadEmpaque,
            BigDecimal subTotal,
            BigDecimal itbis,
            BigDecimal total,
            TipoMovimiento tipoMovimiento,
            TipoDocumento tipoDocumento,
            String numeroDoc,
            BigDecimal cantidad,
            String usuario,
            String observacion
    ) {
        List<MovimientoInventario> movs = registrarMovimientoConLote(articuloInput, alm, articuloEmpaque,
                factorConversion, cantidadEmpaque, subTotal, itbis, total, tipoMovimiento, tipoDocumento,
                numeroDoc, cantidad, usuario, observacion, null);
        return movs.get(movs.size() - 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MovimientoInventario> registrarMovimientoConLote(
            Articulo articuloInput,
            Almacen alm,
            ArticuloEmpaque articuloEmpaque,
            BigDecimal factorConversion,
            BigDecimal cantidadEmpaque,
            BigDecimal subTotal,
            BigDecimal itbis,
            BigDecimal total,
            TipoMovimiento tipoMovimiento,
            TipoDocumento tipoDocumento,
            String numeroDoc,
            BigDecimal cantidad,
            String usuario,
            String observacion,
            Lote lote
    ) {

        // 1. Validaciones de entrada
        if (articuloInput == null || articuloInput.getCodigo() == null) {
            throw new IllegalArgumentException("El artículo proporcionado no es válido.");
        }

        if (alm == null || alm.getCodigo() == null) {
            throw new IllegalArgumentException("El almacén proporcionado no es válido.");
        }

        if (cantidadEmpaque == null || cantidadEmpaque.compareTo(BigDecimal.ZERO) <= 0) {
            
            throw new IllegalArgumentException("La cantidad del movimiento debe ser estrictamente mayor a cero.");
        }

        if (tipoMovimiento == null || tipoMovimiento.getCodigo() == null) {
            throw new IllegalArgumentException("El tipo de movimiento no es válido.");
        }

        // 2. Cargar entidades persistentes
        Articulo articulo = this.articuloRepo.findById(articuloInput.getCodigo())
                .orElseThrow(() -> new EntityNotFoundException("Artículo no encontrado con ID: " + articuloInput.getCodigo()));

        Almacen almacen = this.almacenService.getAlmacen(alm.getCodigo());

        // Buscar o instanciar el registro ArticuloAlmacen
        ArticuloAlmacen artiAlm = this.articuloAlmacenService
                .buscarPorArticuloYAlmacen(articulo.getCodigo(), almacen.getCodigo())
                .orElseThrow(() -> new EntityNotFoundException("El producto con ID " + articulo.getCodigo()
                + " no está asignado al almacén ID: " + almacen.getCodigo()));

        BigDecimal stockAnterior = artiAlm.getExistencia() != null ? artiAlm.getExistencia() : BigDecimal.ZERO;
        BigDecimal stockNuevo;

        Integer codigoTipoMov = tipoMovimiento.getCodigo();

        // 3. Procesar Entrada (1) o Salida (2)
        switch (codigoTipoMov) {
            case 1 -> // ENTRADA
                stockNuevo = stockAnterior.add(cantidadEmpaque);

            case 2 -> { // SALIDA
                boolean permiteSinStock = Boolean.TRUE.equals(articulo.getPermitirVentaSinExistencia());

                // Validar disponibilidad si NO permite ventas sin existencia.
                // El stock se guarda en unidad base: se compara contra la
                // misma cantidad que se descuenta (cantidadEmpaque = unidades
                // base), no contra la cantidad del documento (cajas, fardos...).
                if (!permiteSinStock && stockAnterior.compareTo(cantidadEmpaque) < 0) {

                    throw new IllegalStateException("Stock insuficiente para '" + articulo.getDescripcion()
                            + "' en el almacén " + almacen.getNombre()
                            + ". Existencia actual: " + stockAnterior.stripTrailingZeros().toPlainString()
                            + " (unidad base), cantidad requerida: "
                            + cantidadEmpaque.stripTrailingZeros().toPlainString() + " (unidad base)."
                            + " Si este artículo puede venderse en negativo, active"
                            + " 'Permitir venta sin existencia' en su ficha.");
                }

                stockNuevo = stockAnterior.subtract(cantidadEmpaque);
            }

            default ->
                throw new IllegalArgumentException("Tipo de movimiento no soportado (Código: " + codigoTipoMov + ")");
        }

        // 4. Tramos por lote. Sin control de lote: un solo tramo sin lote (como siempre).
        //    Entrada: todo al lote indicado o al lote automático del día.
        //    Salida: lote indicado, o reparto FEFO entre los lotes del almacén.
        List<LoteService.Tramo> tramos = new ArrayList<>();
        if (articulo.isControlaLote()) {
            // Auto-cuadre: los lotes deben sumar la existencia del almacén ANTES de moverlos
            this.loteService.cuadrarAlmacen(articulo, almacen, stockAnterior, usuario);
        }
        if (!articulo.isControlaLote()) {
            tramos.add(new LoteService.Tramo(null, cantidadEmpaque, false));
        } else if (codigoTipoMov == 1) {
            Lote loteEntrada = lote != null && lote.getCodigo() != null
                    ? lote
                    : this.loteService.obtenerOCrearLote(articulo, null, null, null, null, numeroDoc, usuario);
            tramos.add(new LoteService.Tramo(loteEntrada, cantidadEmpaque, false));
        } else {
            tramos.addAll(this.loteService.repartirSalida(articulo, almacen, cantidadEmpaque, lote));
        }

        // 5. Actualizar existencia en el Almacén específico
        artiAlm.setExistencia(stockNuevo);
        this.articuloAlmacenService.guardar(artiAlm);

        // 6. Existencia general del Artículo = suma real de sus almacenes. Se recalcula
        // (no se acumula la diferencia) para que un descuadre previo no se arrastre.
        BigDecimal existenciaTotalGlobal = this.articuloAlmacenRepo.sumarExistencia(articulo.getCodigo());
        if (existenciaTotalGlobal == null) {
            existenciaTotalGlobal = BigDecimal.ZERO;
        }

        articulo.setExistencia(existenciaTotalGlobal);
        this.articuloRepo.save(articulo);

        // 7. Kardex: una fila por tramo. La existencia del almacén va avanzando tramo a
        //    tramo y los montos se prorratean por cantidad (el último tramo lleva el resto).
        String usuarioMov = usuario != null && !usuario.isBlank() ? usuario : "SISTEMA";
        int signo = codigoTipoMov == 1 ? 1 : -1;
        BigDecimal existenciaCorriente = stockAnterior;
        BigDecimal restoSubTotal = subTotal;
        BigDecimal restoItbis = itbis;
        BigDecimal restoTotal = total;
        List<MovimientoInventario> movimientos = new ArrayList<>();

        for (int i = 0; i < tramos.size(); i++) {
            LoteService.Tramo tramo = tramos.get(i);
            boolean ultimo = i == tramos.size() - 1;
            BigDecimal cant = tramo.cantidad();

            BigDecimal tSubTotal = ultimo ? restoSubTotal : prorratear(subTotal, cant, cantidadEmpaque);
            BigDecimal tItbis = ultimo ? restoItbis : prorratear(itbis, cant, cantidadEmpaque);
            BigDecimal tTotal = ultimo ? restoTotal : prorratear(total, cant, cantidadEmpaque);
            restoSubTotal = restar(restoSubTotal, tSubTotal);
            restoItbis = restar(restoItbis, tItbis);
            restoTotal = restar(restoTotal, tTotal);

            BigDecimal existenciaNueva = existenciaCorriente.add(cant.multiply(BigDecimal.valueOf(signo)));

            MovimientoInventario mov = new MovimientoInventario();
            mov.setArticulo(articulo);
            mov.setAlmacen(almacen);
            mov.setArticuloEmpaque(articuloEmpaque);
            mov.setFactorConversion(factorConversion);
            mov.setCantidadEmpaque(cant);
            mov.setSubTotal(tSubTotal);
            mov.setItbis(tItbis);
            mov.setTotal(tTotal);
            mov.setTipoMovimiento(tipoMovimiento);
            mov.setTipoDocumento(tipoDocumento);
            mov.setNumeroDocumento(numeroDoc);
            mov.setCantidad(cant);
            mov.setExistenciaAnterior(existenciaCorriente);
            mov.setExistenciaNueva(existenciaNueva);
            mov.setFechaMovimiento(new Date());
            mov.setUsuario(usuarioMov);
            mov.setObservacion(observacion);

            if (tramo.lote() != null) {
                BigDecimal[] exLote = this.loteService.moverExistencia(tramo.lote(), almacen,
                        cant.multiply(BigDecimal.valueOf(signo)));
                mov.setLote(tramo.lote());
                mov.setExistenciaLoteAnterior(exLote[0]);
                mov.setExistenciaLoteNueva(exLote[1]);
            }

            movimientos.add(movimientoRepo.save(mov));
            existenciaCorriente = existenciaNueva;
        }

        return movimientos;
    }

    /** Parte proporcional de un monto (null si el monto es null). */
    private static BigDecimal prorratear(BigDecimal monto, BigDecimal parte, BigDecimal todo) {
        if (monto == null || todo == null || todo.signum() == 0) {
            return monto;
        }
        return monto.multiply(parte).divide(todo, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal restar(BigDecimal a, BigDecimal b) {
        return a == null || b == null ? a : a.subtract(b);
    }

    public MovimientoInventario registrarMovimiento1(
            Articulo articuloInput,
            Almacen alm,
            ArticuloEmpaque articuloEmpaque,
            BigDecimal factorConversion,
            BigDecimal cantidadEmpaque,
            BigDecimal subTotal,
            BigDecimal itbis,
            BigDecimal total,
            TipoMovimiento tipoMovimiento,
            TipoDocumento tipoDocumento,
            String numeroDoc,
            BigDecimal cantidad,
            String usuario,
            String observacion
    ) {

        // Validaciones básicas
        if (articuloInput == null || articuloInput.getCodigo() == null) {
            throw new IllegalArgumentException("El artículo proporcionado no es válido.");
        }

        if (cantidad.doubleValue() <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser estrictamente mayor a cero.");
        }

        // 1. Obtener la entidad actualizada directamente de la Base de Datos
        Articulo articulo = this.articuloRepo.findById(articuloInput.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado con ID: " + articuloInput.getCodigo()));
        // 1. Obtener la entidad actualizada directamente de la Base de Datos
        Almacen almacen = this.almacenService.getAlmacen(alm.getCodigo());

        ArticuloAlmacen artiAlm = this.articuloAlmacenService.buscarPorArticuloYAlmacen(articulo.getCodigo(), almacen.getCodigo())
                .orElseThrow(() -> new EntityNotFoundException("El producto con ID " + articulo.getCodigo() + " no existe."));

        System.out.println("artiAlm " + artiAlm.getNombreAlmacen()
                + " " + artiAlm.getDescripcionArticulo() + " existencia actual " + artiAlm.getExistencia());

        BigDecimal stockAnterior = artiAlm.getExistencia();
        BigDecimal stockNuevo;

//        String tipoUpper = tipoMovimiento.getNombre().toUpperCase();
        Integer tipoUpper = tipoMovimiento.getCodigo();

        // 2. Determinar si suma o resta existencias
        switch (tipoUpper) {

            case 1 ->
                stockNuevo = stockAnterior.add(cantidad);
            case 2 -> {

                // Solo bloquea si NO permite ventas sin existencia y la cantidad requerida supera el stock
                boolean permiteSinStock = Boolean.TRUE.equals(articulo.getPermitirVentaSinExistencia());

                if (!permiteSinStock && stockAnterior.doubleValue() < cantidad.doubleValue()) {

//                      ClaseUtil.mostrarNotificacion("Existencia insuficiente para: " + articulo.getDescripcion()
//                    + ". Disponible: " + stockAnterior + ", Solicitado: " + cantidad, NotificationVariant.LUMO_PRIMARY);
//                    throw new IllegalStateException("Stock insuficiente para '" + articulo.getDescripcion()
//                            + "'. Existencia actual: " + stockAnterior + ", Cantidad requerida: " + cantidad);
                }

                if (stockAnterior.doubleValue() < cantidad.doubleValue()) {

//                    throw new IllegalStateException("Stock insuficiente para '" + articulo.getDescripcion()
//                            + "'. Existencia actual: " + stockAnterior + ", Cantidad requerida: " + cantidad);
                }
                stockNuevo = stockAnterior.subtract(cantidad);
            }

            default ->
                throw new IllegalArgumentException("Tipo de movimiento no válido: " + tipoMovimiento);
        }

        System.out.println("stockNuevo:" + stockNuevo);
        // 3. Impactar el stock en la entidad Articulo
        articulo.setExistencia(stockNuevo);
        articuloRepo.save(articulo);

        artiAlm.setExistencia(stockNuevo);
        this.articuloAlmacenService.guardar(artiAlm);

        // 4. Crear la auditoría en la tabla movimiento_inventario
        MovimientoInventario mov = new MovimientoInventario();

        mov.setArticulo(articulo);
        mov.setAlmacen(almacen);
        mov.setArticuloEmpaque(articuloEmpaque);
        mov.setFactorConversion(factorConversion);
        mov.setCantidadEmpaque(cantidadEmpaque);
        mov.setSubTotal(subTotal);
        mov.setItbis(itbis);
        mov.setTotal(total);
        mov.setTipoMovimiento(tipoMovimiento);
        mov.setTipoDocumento(tipoDocumento);
        mov.setNumeroDocumento(numeroDoc);
        mov.setCantidad(cantidad);
        mov.setExistenciaAnterior(stockAnterior);
        mov.setExistenciaNueva(stockNuevo);
        mov.setFechaMovimiento(new Date());
        mov.setUsuario(usuario != null ? usuario : "SISTEMA");
        mov.setObservacion(observacion);

        return movimientoRepo.save(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> getMovimientosPorArticulo(Integer articuloCodigo) {
        return movimientoRepo.findByArticuloOrderByFechaMovimientoDesc(articuloCodigo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> getMovimientosPorLote(Integer codigoLote) {
        return movimientoRepo.findPorLote(codigoLote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> getMovimientosPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepo.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(inicio, fin);
    }

}
