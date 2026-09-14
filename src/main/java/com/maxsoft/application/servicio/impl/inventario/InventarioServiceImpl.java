/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.evento.VentaRealizadaEvent;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.repo.ArticuloRepo; // Tu repositorio de artículos
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private ArticuloRepo articuloRepo;

    @Override
    @Transactional
    public void descontarStock(Integer idArticulo, Double cantidad) {
        Articulo articulo = articuloRepo.findById(idArticulo)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + idArticulo));

        double existenciaActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;

        // Descontar inventario
        articulo.setExistencia(existenciaActual - cantidad);
        articuloRepo.save(articulo);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarStockDisponible(Integer idArticulo, Double cantidad) {
        Articulo articulo = articuloRepo.findById(idArticulo)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + idArticulo));

        // 1. Si no es inventariable (ej. recargas), no se valida stock
        if (Boolean.TRUE.equals(articulo.getInventariable())) {

            System.out.println("articulo.getInventariable():" + articulo.getInventariable());
            return;
        }

        // 2. Si el producto permite ventas sin stock (inventario negativo), no se bloquea
        if (Boolean.TRUE.equals(articulo.isPermitirVentaSinExistencia())) {

            System.out.println("articulo.isPermitirVentaSinExistencia() :" + articulo.isPermitirVentaSinExistencia());
            throw new IllegalStateException("Stock insuficiente para: " + articulo.getDescripcion());

//            return;
        }

        double existencia = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;
        if (existencia < cantidad) {
            throw new IllegalStateException("Stock insuficiente para: " + articulo.getDescripcion()
                    + ". Disponible: " + existencia + ", Solicitado: " + cantidad);
        }
    }

    /**
     * Escucha automáticamente las ventas procesadas dentro de la misma
     * transacción.Si ocurre un error descontando el stock, la factura también
     * hace Rollback.
     *
     * @param event
     */
    @EventListener
    public void manejarVentaRealizada(VentaRealizadaEvent event) {
        for (VentaRealizadaEvent.ItemVentaDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {
                descontarStock(item.getIdArticulo(), item.getCantidad());
            }
        }
    }

    @Override
    @Transactional
    public void incrementarStock(Integer idArticulo, Double cantidad) {
        if (idArticulo == null || cantidad == null || cantidad <= 0) {
            return;
        }

        Articulo articulo = articuloRepo.findById(idArticulo)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + idArticulo));

        // Solo suma al stock si es un artículo inventariable
        if (Boolean.TRUE.equals(articulo.getInventariable())) {
            double existenciaActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;

            // Suma algebraica: Si el stock era -5 y entran 12 -> (-5 + 12 = 7)
            articulo.setExistencia(existenciaActual + cantidad);
            articuloRepo.save(articulo);
        }
    }
}
