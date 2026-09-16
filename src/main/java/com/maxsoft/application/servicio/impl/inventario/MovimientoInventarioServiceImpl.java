/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.MovimientoInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
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
    public MovimientoInventarioServiceImpl(MovimientoInventarioRepo movimientoRepo,
            ArticuloRepo articuloRepo) {
        this.movimientoRepo = movimientoRepo;
        this.articuloRepo = articuloRepo;
    }

    @Override
    @Transactional
    public MovimientoInventario registrarMovimiento(Articulo articuloInput,
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
        Articulo articulo = articuloRepo.findById(articuloInput.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado con ID: " + articuloInput.getCodigo()));

        double stockAnterior = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;
        double stockNuevo;

        String tipoUpper = tipoMovimiento.getNombre().toUpperCase();

        // 2. Determinar si suma o resta existencias
        switch (tipoUpper) {

            case "ENTRADA", "AJUSTE_INCREMENTO" ->
                stockNuevo = stockAnterior + cantidad;
            case "SALIDA", "AJUSTE_DECREMENTO" -> {

                // Solo bloquea si NO permite ventas sin existencia y la cantidad requerida supera el stock
                boolean permiteSinStock = Boolean.TRUE.equals(articulo.isPermitirVentaSinExistencia());

                if (!permiteSinStock && stockAnterior < cantidad) {
                    throw new IllegalStateException("Stock insuficiente para '" + articulo.getDescripcion()
                            + "'. Existencia actual: " + stockAnterior + ", Cantidad requerida: " + cantidad);
                }

                if (stockAnterior < cantidad) {

                    throw new IllegalStateException("Stock insuficiente para '" + articulo.getDescripcion()
                            + "'. Existencia actual: " + stockAnterior + ", Cantidad requerida: " + cantidad);
                }
                stockNuevo = stockAnterior - cantidad;
            }

            default ->
                throw new IllegalArgumentException("Tipo de movimiento no válido: " + tipoMovimiento);
        }

        System.out.println("stockNuevo:" + stockNuevo);
        // 3. Impactar el stock en la entidad Articulo
        articulo.setExistencia(stockNuevo);
        articuloRepo.save(articulo);

        // 4. Crear la auditoría en la tabla movimiento_inventario
        MovimientoInventario mov = new MovimientoInventario();

        mov.setArticulo(articulo);
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
