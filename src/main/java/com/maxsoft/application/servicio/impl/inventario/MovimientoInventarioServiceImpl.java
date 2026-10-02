/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
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
    @Transactional
    public MovimientoInventario registrarMovimiento(
            Articulo articuloInput,
            Almacen alm,
            TipoMovimiento tipoMovimiento,
            TipoDocumento tipoDocumento,
            String numeroDoc,
            double cantidad,
            String usuario,
            String observacion) {

        // Validaciones básicas
        if (articuloInput == null || articuloInput.getCodigo() == null) {
            throw new IllegalArgumentException("El artículo proporcionado no es válido.");
        }

        if (cantidad <= 0) {
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

        double stockAnterior = artiAlm.getExistencia().doubleValue();
        double stockNuevo;

//        String tipoUpper = tipoMovimiento.getNombre().toUpperCase();
        Integer tipoUpper = tipoMovimiento.getCodigo();

        // 2. Determinar si suma o resta existencias
        switch (tipoUpper) {

            case 1 ->
                stockNuevo = stockAnterior + cantidad;
            case 2 -> {

                // Solo bloquea si NO permite ventas sin existencia y la cantidad requerida supera el stock
                boolean permiteSinStock = Boolean.TRUE.equals(articulo.getPermitirVentaSinExistencia());

                if (!permiteSinStock && stockAnterior < cantidad) {

//                      ClaseUtil.mostrarNotificacion("Existencia insuficiente para: " + articulo.getDescripcion()
//                    + ". Disponible: " + stockAnterior + ", Solicitado: " + cantidad, NotificationVariant.LUMO_PRIMARY);
//                    throw new IllegalStateException("Stock insuficiente para '" + articulo.getDescripcion()
//                            + "'. Existencia actual: " + stockAnterior + ", Cantidad requerida: " + cantidad);
                }

                if (stockAnterior < cantidad) {

//                    throw new IllegalStateException("Stock insuficiente para '" + articulo.getDescripcion()
//                            + "'. Existencia actual: " + stockAnterior + ", Cantidad requerida: " + cantidad);
                }
                stockNuevo = stockAnterior - cantidad;
            }

            default ->
                throw new IllegalArgumentException("Tipo de movimiento no válido: " + tipoMovimiento);
        }

        System.out.println("stockNuevo:" + stockNuevo);
        // 3. Impactar el stock en la entidad Articulo
        articulo.setExistencia(BigDecimal.valueOf(stockNuevo));
        articuloRepo.save(articulo);

        artiAlm.setExistencia(BigDecimal.valueOf(stockNuevo));
        this.articuloAlmacenService.guardar(artiAlm);

        // 4. Crear la auditoría en la tabla movimiento_inventario
        MovimientoInventario mov = new MovimientoInventario();

        mov.setArticulo(articulo);
        mov.setAlmacen(almacen);
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
