/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.evento.AjusteInventarioCreadoEvent;
import com.maxsoft.application.evento.SalidaInventarioCreadaEvent;
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
import com.maxsoft.application.util.ClaseUtil;
import com.vaadin.flow.component.notification.NotificationVariant;
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
        if (Boolean.TRUE.equals(articulo.getPermitirVentaSinExistencia())) {
            return;
        }

        // 3. Validar existencia real
        double existencia = articulo.getExistencia() != null ? articulo.getExistencia().doubleValue() : 0.0;

        // Ejemplo de mejora en tu MovimientoInventarioServiceImpl:
        if (existencia < cantidad && !articulo.getPermitirVentaSinExistencia()) {

            ClaseUtil.mostrarNotificacion("Existencia insuficiente para: " + articulo.getDescripcion()
                    + ". Disponible: " + existencia + ", Solicitado: " + cantidad, NotificationVariant.LUMO_WARNING);

//           throw new IllegalStateException();
        }

//        if (existencia < cantidad) {
//
//            throw new IllegalStateException("Stock insuficiente para: " + articulo.getDescripcion()
//                    + ". Disponible: " + existencia + ", Solicitado: " + cantidad);
//        }
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

        TipoDocumento tipoFactura = tipoDocumentoRepo.getTipoDocumento(1);
//                .orElseThrow(() -> new IllegalStateException("No existe el TipoDocumento 'FACTURA' en la BD"));
        for (VentaDevueltaEvent.ItemAnulacionDto item : event.getItems()) {

            Articulo articulo = articuloRepo.findById(item.getCodigoArticulo())
                    .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getCodigoArticulo()));

            if (articulo != null) {
                String tipoDevolucion = event.isEsDevolucionTotal() ? "Devolución Total" : "Devolución Parcial";

                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        tipoEntrada,
                        tipoFactura,
                        String.valueOf(event.getIdFactura()),
                        item.getCantidad(),
                        event.getUsuario(),
                        tipoDevolucion + " Venta #" + event.getIdFactura() + ". Motivo: " + event.getMotivo()
                );
            }
        }
    }

// ... dentro de InventarioServiceImpl ...
    @EventListener
    @Transactional
    public void manejarSalidaInventarioCreada(SalidaInventarioCreadaEvent event) {

        // 1. Obtener los tipos de movimiento/documento correspondientes a "SALIDA" y "SALIDA DE INVENTARIO"
        TipoMovimiento tipoSalida = tipoMovimientoRepo.getTipoMovimientoa(2); // ID o código de SALIDA
        TipoDocumento tipoDocSalida = tipoDocumentoRepo.getTipoDocumento(2); // ID de Tipo Documento (Salida / POS)

        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        // 2. Recorrer los ítems que vienen en la Salida y registrar su movimiento en Kardex
        for (SalidaInventarioCreadaEvent.ItemSalidaDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {

                Articulo articulo = articuloRepo.findById(item.getIdArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getIdArticulo()));

                // Registrar movimiento en la tabla de inventario / kardex
                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        tipoSalida,
                        tipoDocSalida,
                        String.valueOf(event.getIdSalida()), // ID de la salida
                        item.getCantidad(),
                        event.getUsuario(),
                        "Salida de inventario #" + event.getIdSalida() + " por doc: " + event.getNumeroDocumento()
                );
            }
        }
    }

    @EventListener
    @Transactional
    public void manejarAjusteInventarioCreado(AjusteInventarioCreadoEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        TipoMovimiento tipoMovimiento = tipoMovimientoRepo.getTipoMovimientoa(event.getIdTipoMovimiento());
        TipoDocumento tipoDocAjuste = tipoDocumentoRepo.getTipoDocumento(3); // Tipo Documento 'Ajuste de Inventario'

        for (AjusteInventarioCreadoEvent.ItemAjusteDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {
                Articulo articulo = articuloRepo.findById(item.getIdArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getIdArticulo()));

                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        tipoMovimiento,
                        tipoDocAjuste,
                        "AJ-" + event.getIdAjuste(),
                        item.getCantidad(),
                        event.getUsuario(),
                        "Ajuste #" + event.getIdAjuste() + ". " + (event.getObservacion() != null ? event.getObservacion() : "")
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
