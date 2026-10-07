package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.evento.AjusteInventarioCreadoEvent;
import com.maxsoft.application.evento.SalidaInventarioCreadaEvent;
import com.maxsoft.application.evento.VentaAnuladaEvent;
import com.maxsoft.application.evento.VentaDevueltaEvent;
import com.maxsoft.application.evento.VentaRealizadaEvent;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
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

import java.math.BigDecimal;

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

    // ==========================================
    // 1. MANEJO DE VENTA REALIZADA (SALIDA = 2)
    // ==========================================
    @EventListener
    @Transactional
    public void manejarVentaRealizada(VentaRealizadaEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        TipoMovimiento tipoSalida = tipoMovimientoRepo.getTipoMovimientoa(2); // 2 = SALIDA
        TipoDocumento tipoFactura = tipoDocumentoRepo.getTipoDocumento(2);   // 2 = FACTURA

        for (VentaRealizadaEvent.ItemVentaDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {
                Articulo articulo = articuloRepo.findById(item.getIdArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getIdArticulo()));

                Almacen almacen = new Almacen(item.getIdAlmacen());
                ArticuloEmpaque empaque = item.getIdArticuloEmpaque() != null ? new ArticuloEmpaque(item.getIdArticuloEmpaque()) : null;

                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        almacen,
                        empaque,
                        item.getFactorConversion(),
                        item.getCantidadEmpaque(),
                        item.getSubTotal(),
                        item.getItbis(),
                        item.getTotal(),
                        tipoSalida,
                        tipoFactura,
                        event.getNumeroFactura() != null ? event.getNumeroFactura() : String.valueOf(event.getIdFactura()),
                        item.getCantidadBase(),
                        event.getUsuario() != null ? event.getUsuario() : "SISTEMA_POS",
                        event.getObservacion() != null ? event.getObservacion() : "Venta POS automatizada"
                );
            }
        }
    }

    // ==========================================
    // 2. MANEJO DE VENTA ANULADA (ENTRADA = 1)
    // ==========================================
    @EventListener
    @Transactional
    public void manejarVentaAnulada(VentaAnuladaEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        TipoMovimiento tipoEntrada = tipoMovimientoRepo.getTipoMovimientoa(1); // 1 = ENTRADA
        TipoDocumento tipoAnulacion = tipoDocumentoRepo.getTipoDocumento(1);  // 1 = ANULACIÓN/FACTURA

        String numDoc = event.getNumeroFactura() != null ? event.getNumeroFactura() : String.valueOf(event.getIdFactura());
        String obs = "Anulación Venta #" + numDoc + (event.getMotivo() != null ? ". Motivo: " + event.getMotivo() : "");

        for (VentaAnuladaEvent.ItemAnulacionDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {
                Articulo articulo = articuloRepo.findById(item.getIdArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getIdArticulo()));

                Almacen almacen = new Almacen(item.getIdAlmacen());
                ArticuloEmpaque empaque = item.getIdArticuloEmpaque() != null ? new ArticuloEmpaque(item.getIdArticuloEmpaque()) : null;

                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        almacen,
                        empaque,
                        item.getFactorConversion(),
                        item.getCantidadEmpaque(),
                        item.getSubTotal(),
                        item.getItbis(),
                        item.getTotal(),
                        tipoEntrada,
                        tipoAnulacion,
                        numDoc,
                        item.getCantidadBase(),
                        event.getUsuario() != null ? event.getUsuario() : "SISTEMA",
                        obs
                );
            }
        }
    }

    // ==========================================
    // 3. MANEJO DE VENTA DEVUELTA (ENTRADA = 1)
    // ==========================================
    @EventListener
    @Transactional
    public void onVentaDevuelta(VentaDevueltaEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        TipoMovimiento tipoEntrada = tipoMovimientoRepo.getTipoMovimientoa(1);
        TipoDocumento tipoFactura = tipoDocumentoRepo.getTipoDocumento(1);

        for (VentaDevueltaEvent.ItemAnulacionDto item : event.getItems()) {
            if (item.getCodigoArticulo() != null) {
                Articulo articulo = articuloRepo.findById(item.getCodigoArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getCodigoArticulo()));

                String tipoDevolucion = event.isEsDevolucionTotal() ? "Devolución Total" : "Devolución Parcial";
                Almacen almacen = new Almacen(2); // Ajustar si el event trae el idAlmacen

                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        almacen,
                        null,                       // ArticuloEmpaque
                        BigDecimal.ONE,             // factorConversion
                        item.getCantidad(),         // cantidadEmpaque
                        BigDecimal.ZERO,            // subTotal
                        BigDecimal.ZERO,            // itbis
                        BigDecimal.ZERO,            // total
                        tipoEntrada,
                        tipoFactura,
                        String.valueOf(event.getIdFactura()),
                        item.getCantidad(),         // cantidadBase
                        event.getUsuario(),
                        tipoDevolucion + " Venta #" + event.getIdFactura() + ". Motivo: " + event.getMotivo()
                );
            }
        }
    }

    // ==========================================
    // 4. MANEJO DE SALIDA DE INVENTARIO (SALIDA = 2)
    // ==========================================
    @EventListener
    @Transactional
    public void manejarSalidaInventarioCreada(SalidaInventarioCreadaEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        TipoMovimiento tipoSalida = tipoMovimientoRepo.getTipoMovimientoa(2);
        TipoDocumento tipoDocSalida = tipoDocumentoRepo.getTipoDocumento(2);

        for (SalidaInventarioCreadaEvent.ItemSalidaDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {
                Articulo articulo = articuloRepo.findById(item.getIdArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getIdArticulo()));

                Almacen almacen = new Almacen(2); // Reemplazar con item.getIdAlmacen() si está disponible

                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        almacen,
                        null,                       // ArticuloEmpaque
                        BigDecimal.ONE,             // factorConversion
                        item.getCantidad(),         // cantidadEmpaque
                        BigDecimal.ZERO,            // subTotal
                        BigDecimal.ZERO,            // itbis
                        BigDecimal.ZERO,            // total
                        tipoSalida,
                        tipoDocSalida,
                        String.valueOf(event.getIdSalida()),
                        item.getCantidad(),         // cantidadBase
                        event.getUsuario(),
                        "Salida de inventario #" + event.getIdSalida() + " por doc: " + event.getNumeroDocumento()
                );
            }
        }
    }

    // ==========================================
    // 5. MANEJO DE AJUSTE DE INVENTARIO
    // ==========================================
    @EventListener
    @Transactional
    public void manejarAjusteInventarioCreado(AjusteInventarioCreadoEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        TipoMovimiento tipoMovimiento = tipoMovimientoRepo.getTipoMovimientoa(event.getIdTipoMovimiento());
        TipoDocumento tipoDocAjuste = tipoDocumentoRepo.getTipoDocumento(3); // 3 = AJUSTE

        String numDoc = event.getNumeroDocumento() != null ? event.getNumeroDocumento() : "AJ-" + event.getIdAjuste();

        for (AjusteInventarioCreadoEvent.ItemAjusteDto item : event.getItems()) {
            if (item.getIdArticulo() != null) {
                Articulo articulo = articuloRepo.findById(item.getIdArticulo())
                        .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + item.getIdArticulo()));

                Almacen almacen = new Almacen(item.getIdAlmacen());
                ArticuloEmpaque empaque = item.getIdArticuloEmpaque() != null ? new ArticuloEmpaque(item.getIdArticuloEmpaque()) : null;

                movimientoInventarioService.registrarMovimiento(
                        articulo,
                        almacen,
                        empaque,
                        item.getFactorConversion(),
                        item.getCantidadEmpaque(),
                        item.getSubTotal(),
                        item.getItbis(),
                        item.getTotal(),
                        tipoMovimiento,
                        tipoDocAjuste,
                        numDoc,
                        item.getCantidadBase(),
                        event.getUsuario(),
                        "Ajuste #" + event.getIdAjuste() + ". " + (event.getObservacion() != null ? event.getObservacion() : "")
                );
            }
        }
    }

    // ==========================================
    // VALIDACIONES Y MÉTODOS DE SERVICIO
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public void validarStockDisponible(Integer idArticulo, Double cantidad) {
        Articulo articulo = articuloRepo.findById(idArticulo)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + idArticulo));

        if (Boolean.FALSE.equals(articulo.getInventariable())) {
            return;
        }

        if (Boolean.TRUE.equals(articulo.getPermitirVentaSinExistencia())) {
            return;
        }

        BigDecimal existencia = articulo.getExistencia() != null ? articulo.getExistencia() : BigDecimal.ZERO;
        BigDecimal req = BigDecimal.valueOf(cantidad);

        if (existencia.compareTo(req) < 0) {
            ClaseUtil.mostrarNotificacion("Existencia insuficiente para: " + articulo.getDescripcion()
                    + ". Disponible: " + existencia + ", Solicitado: " + cantidad, NotificationVariant.LUMO_WARNING);
        }
    }

    @Override
    public void descontarStock(Integer idArticulo, Double cantidad, String referencia) {
        throw new UnsupportedOperationException("Método no implementado. Utilice el sistema de eventos/Kardex.");
    }

    @Override
    public void incrementarStock(Integer idArticulo, Double cantidad, String referencia) {
        throw new UnsupportedOperationException("Método no implementado. Utilice el sistema de eventos/Kardex.");
    }
}