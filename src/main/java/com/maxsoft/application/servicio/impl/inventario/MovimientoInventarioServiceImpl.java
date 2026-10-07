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
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
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

                // Validar disponibilidad si NO permite ventas sin existencia
                if (!permiteSinStock && stockAnterior.compareTo(cantidad) < 0) {
                    throw new IllegalStateException("Stock insuficiente para '" + articulo.getDescripcion()
                            + "' en el almacén " + almacen.getNombre()
                            + ". Existencia actual: " + stockAnterior + ", Cantidad requerida: " + cantidad);
                }

                stockNuevo = stockAnterior.subtract(cantidad);
            }

            default ->
                throw new IllegalArgumentException("Tipo de movimiento no soportado (Código: " + codigoTipoMov + ")");
        }

        // 4. Actualizar existencia en el Almacén específico
        artiAlm.setExistencia(stockNuevo);
        this.articuloAlmacenService.guardar(artiAlm);

        // 5. Actualizar existencia general acumulada en el Artículo (Suma total de almacenes)
        BigDecimal existenciaTotalGlobal = (articulo.getExistencia() != null ? articulo.getExistencia() : BigDecimal.ZERO)
                .add(stockNuevo.subtract(stockAnterior));
        articulo.setExistencia(existenciaTotalGlobal);
        this.articuloRepo.save(articulo);

        // 6. Registrar Auditoría en MovimientoInventario / Kardex
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
        mov.setCantidad(cantidadEmpaque);
        mov.setExistenciaAnterior(stockAnterior);
        mov.setExistenciaNueva(stockNuevo);
        mov.setFechaMovimiento(new Date());
        mov.setUsuario(usuario != null && !usuario.isBlank() ? usuario : "SISTEMA");
        mov.setObservacion(observacion);

        return movimientoRepo.save(mov);
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
    public List<MovimientoInventario> getMovimientosPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepo.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(inicio, fin);
    }

}
