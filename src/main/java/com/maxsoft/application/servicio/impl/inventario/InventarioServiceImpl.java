/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.evento.VentaAnuladaEvent;
import com.maxsoft.application.evento.VentaDevueltaEvent;
import com.maxsoft.application.evento.VentaRealizadaEvent;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.ArticuloRepo;

import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private ArticuloRepo articuloRepo;

    @Autowired
    private MovimientoInventarioService movimientoInventarioService;

    @Autowired
    private TipoMovimientoService tipoMovimientoRepo;

    @Autowired
    private TipoDocumentoService tipoDocumentoRepo;

    @EventListener
    public void manejarVentaRealizada(VentaRealizadaEvent event) {
        // Cargar los tipos correspondientes para la auditoría (o usarlos desde enums/constantes)
        TipoMovimiento tipoSalida = tipoMovimientoRepo.getTipoMovimientoa(2);//
        //.orElseThrow(() -> new IllegalStateException("Tipo de movimiento 'SALIDA' no configurado."));

        TipoDocumento tipoFactura = tipoDocumentoRepo.getTipoDocumento(2);//
        //   .orElseThrow(() -> new IllegalStateException("Tipo de documento 'FACTURA' no configurado."));

        for (VentaRealizadaEvent.ItemVentaDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {
                Articulo articulo = articuloRepo.findById(item.getIdArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getIdArticulo()));

                // Reutilizas directamente tu servicio de movimientos
                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        tipoSalida,
                        tipoFactura,
                        event.getIdFactura().toString(),
                        item.getCantidad(),
                        "SISTEMA_POS",
                        "Venta POS automatizada"
                );
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validarStockDisponible(Integer idArticulo, Double cantidad) {
        Articulo articulo = articuloRepo.findById(idArticulo)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + idArticulo));

        // 1. Si NO es inventariable, omitir validación
        if (Boolean.FALSE.equals(articulo.getInventariable())) {
            return;
        }

        // 2. Si PERMITE ventas sin existencia (stock negativo), omitir validación
        if (Boolean.TRUE.equals(articulo.isPermitirVentaSinExistencia())) {
            return;
        }

        // 3. Validar existencia real
        double existencia = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;
        if (existencia < cantidad) {
            throw new IllegalStateException("Stock insuficiente para: " + articulo.getDescripcion()
                    + ". Disponible: " + existencia + ", Solicitado: " + cantidad);
        }
    }

    @EventListener
    @Transactional
    public void manejarVentaAnulada(VentaAnuladaEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        // Requiere que el TipoMovimiento 'ENTRADA' o 'ENTRADA_ANULACION' exista en BD
        TipoMovimiento tipoEntrada = tipoMovimientoRepo.getTipoMovimientoa(1);
//                .orElseThrow(() -> new IllegalStateException("No existe el TipoMovimiento 'ENTRADA' en la BD"));

        TipoDocumento tipoFactura = tipoDocumentoRepo.getTipoDocumento(1);
//                .orElseThrow(() -> new IllegalStateException("No existe el TipoDocumento 'FACTURA' en la BD"));

        for (VentaAnuladaEvent.ItemAnulacionDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {

                Articulo articulo = articuloRepo.findById(item.getIdArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getIdArticulo()));

                // Se usa el servicio existente: incrementa stock y crea la auditoría
                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        tipoEntrada,
                        tipoFactura,
                        String.valueOf(event.getIdFactura()),
                        item.getCantidad(),
                        event.getUsuario(),
                        "Anulación Venta #" + event.getIdFactura() + ". Motivo: " + event.getMotivo()
                );
            }
        }
    }
    
    @EventListener
    @Transactional
    public void onVentaDevuelta(VentaDevueltaEvent event) {
        
     
        // Requiere que el TipoMovimiento 'ENTRADA' o 'ENTRADA_ANULACION' exista en BD
        TipoMovimiento tipoEntrada = tipoMovimientoRepo.getTipoMovimientoa(1);
//                .orElseThrow(() -> new IllegalStateException("No existe el TipoMovimiento 'ENTRADA' en la BD"));

//        TipoDocumento tipoFactura = tipoDocumentoRepo.getTipoDocumento(1);
//                .orElseThrow(() -> new IllegalStateException("No existe el TipoDocumento 'FACTURA' en la BD"));

        for (VentaDevueltaEvent.ItemAnulacionDto item : event.getItems()) {
            
                 Articulo articulo = articuloRepo.findById(item.getCodigoArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getCodigoArticulo()));


            if (articulo != null) {
                String tipoDevolucion = event.isEsDevolucionTotal() ? "Devolución Total" : "Devolución Parcial";

                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        tipoEntrada,
                        null,
                        String.valueOf(event.getIdFactura()),
                        item.getCantidad(),
                        event.getUsuario(),
                        tipoDevolucion + " Venta #" + event.getIdFactura() + ". Motivo: " + event.getMotivo()
                );
            }
        }
    }

    @Override
    public void descontarStock(Integer idArticulo, Double cantidad, String referencia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void incrementarStock(Integer idArticulo, Double cantidad, String referencia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
