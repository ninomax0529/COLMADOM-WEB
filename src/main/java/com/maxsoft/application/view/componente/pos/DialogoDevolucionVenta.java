/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.servicio.interfaces.venta.FacturaDeVentaService;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogoDevolucionVenta extends Dialog {

    private final FacturaDeVenta factura;
    private final FacturaDeVentaService facturaService;
//    private final ReporteService reporteService;
    private final String usuarioActual;
    private final Runnable onSuccessCallback;

    private Grid<ItemDevolucionModel> gridItems;
    private TextArea txtMotivo;
    private Span lblTotalReembolso;
    private Map<Integer, NumberField> cantidadFields = new HashMap<>();

    public DialogoDevolucionVenta(
            FacturaDeVenta factura, 
                                 FacturaDeVentaService facturaService, 
//                                 ReporteService reporteService,
                                 String usuarioActual, 
                                 Runnable onSuccessCallback
    ) {
        this.factura = factura;
        this.facturaService = facturaService;
//        this.reporteService = reporteService;
        this.usuarioActual = usuarioActual;
        this.onSuccessCallback = onSuccessCallback;

        setHeaderTitle("Procesar Devolución - Factura #" + factura.getCodigo());
        setWidth("750px");

        crearInterfaz();
    }

    private void crearInterfaz() {
        // 1. Configurar la Grilla de Ítems
        gridItems = new Grid<>();
        gridItems.setHeight("250px");

        gridItems.addColumn(ItemDevolucionModel::getDescripcion).setHeader("Producto").setAutoWidth(true);
        gridItems.addColumn(ItemDevolucionModel::getCantidadComprada).setHeader("Comprado");
        gridItems.addColumn(ItemDevolucionModel::getDisponibleDevolver).setHeader("Disponible");
        
        gridItems.addComponentColumn(item -> {
            NumberField numField = new NumberField();
            numField.setValue(0.0);
            numField.setMin(0);
            numField.setMax(item.getDisponibleDevolver());
            numField.setStepButtonsVisible(true);
            numField.setWidth("120px");
            
            numField.addValueChangeListener(e -> {
                item.setCantidadADevolver(e.getValue() != null ? e.getValue() : 0.0);
                calcularTotalReembolso();
            });
            
            cantidadFields.put(item.getIdDetalle(), numField);
            return numField;
        }).setHeader("A Devolver");

        // Cargar Ítems disponibles de la factura
        List<ItemDevolucionModel> itemsModel = new ArrayList<>();
        factura.getDetalleFacturaDeVentaCollection().forEach(det -> {
            
            BigDecimal devuelto = det.getCantidadDevuelta() != null ? det.getCantidadDevuelta() : BigDecimal.ZERO;
            BigDecimal disponible = det.getCantidad().subtract(devuelto);
            
            if (disponible.doubleValue() > 0) {
                
                BigDecimal precio = det.getPrecioVenta() != null ? det.getPrecioVenta() : det.getPrecioVenta();
                
                itemsModel.add(new ItemDevolucionModel(det.getCodigo(), det.getArticulo().getDescripcion(),
                        det.getCantidad().doubleValue(), disponible.doubleValue(), precio.doubleValue()));
            }
        });
        gridItems.setItems(itemsModel);

        // 2. Campo Motivo y Total
        txtMotivo = new TextArea("Motivo de la Devolución");
        txtMotivo.setWidthFull();
        txtMotivo.setRequired(true);

        lblTotalReembolso = new Span("Total a Reembolsar: $0.00");
        lblTotalReembolso.getStyle().set("font-weight", "bold").set("font-size", "16px").set("color", "var(--lumo-primary-color)");

        // 3. Botones de Acción
        Button btnProcesar = new Button("Procesar Devolución", e -> ejecutarDevolucion(itemsModel));
        btnProcesar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> close());

        VerticalLayout layout = new VerticalLayout(gridItems, txtMotivo, lblTotalReembolso);
        add(layout);
        getFooter().add(btnCancelar, btnProcesar);
    }

    private void calcularTotalReembolso() {
        double total = gridItems.getGenericDataView().getItems()
                .mapToDouble(item -> item.getCantidadADevolver() * item.getPrecioUnitario())
                .sum();
        lblTotalReembolso.setText(String.format("Total a Reembolsar: $%.2f", total));
    }

    private void ejecutarDevolucion(List<ItemDevolucionModel> items) {
        if (txtMotivo.isEmpty()) {
            Notification.show("Debe indicar el motivo de la devolución.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Armar el DTO de Solicitud
        SolicitudDevolucionDto solicitud = new SolicitudDevolucionDto();
        solicitud.setIdFactura(factura.getCodigo());
        solicitud.setMotivo(txtMotivo.getValue());
        solicitud.setUsuario(usuarioActual);

        List<SolicitudDevolucionDto.ItemDevolucionDto> itemsDev = new ArrayList<>();
        for (ItemDevolucionModel model : items) {
            if (model.getCantidadADevolver() > 0) {
                SolicitudDevolucionDto.ItemDevolucionDto itemDto = new SolicitudDevolucionDto.ItemDevolucionDto();
                itemDto.setIdDetalleFactura(model.getIdDetalle());
                itemDto.setCantidadADevolver(model.getCantidadADevolver());
                itemsDev.add(itemDto);
            }
        }

        if (itemsDev.isEmpty()) {
            Notification.show("Seleccione al menos una cantidad a devolver.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        solicitud.setItems(itemsDev);

        try {
            // 1. Lógica backend: Actualiza Factura, Caja, crea EntradaInventario y registra Kardex
            FacturaDeVenta facturaProcesada = facturaService.procesarDevolucion(solicitud);

            // 2. Generar e Imprimir Comprobante/Ticket con Jasper
            imprimirComprobanteJasper(facturaProcesada, solicitud);

            Notification.show("Devolución procesada con éxito.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            close();
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }

        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void imprimirComprobanteJasper(FacturaDeVenta facturaProcesada, SolicitudDevolucionDto solicitud) {
        try {
//            byte[] pdfBytes = reporteService.generarTicketDevolucionPdf(facturaProcesada, solicitud);
//            StreamResource resource = new StreamResource("Devolucion_" + facturaProcesada.getCodigo() + ".pdf",
//                    () -> new ByteArrayInputStream(pdfBytes));
//            
//            // Abre el PDF en una pestaña emergente para previsualizar/imprimir
//            UI.getCurrent().getPage().open(resource, "_blank");
        } catch (Exception e) {
            Notification.show("La devolución se registró, pero ocurrió un error al generar el ticket PDF.", 4000, Notification.Position.MIDDLE);
        }
    }

    // Modelo auxiliar interno para la grilla de la UI
    public static class ItemDevolucionModel {
        private final Integer idDetalle;
        private final String descripcion;
        private final Double cantidadComprada;
        private final Double disponibleDevolver;
        private final Double precioUnitario;
        private Double cantidadADevolver = 0.0;

        public ItemDevolucionModel(Integer idDetalle, String descripcion, Double cantidadComprada, Double disponibleDevolver, Double precioUnitario) {
            this.idDetalle = idDetalle;
            this.descripcion = descripcion;
            this.cantidadComprada = cantidadComprada;
            this.disponibleDevolver = disponibleDevolver;
            this.precioUnitario = precioUnitario;
        }

        public Integer getIdDetalle() { return idDetalle; }
        public String getDescripcion() { return descripcion; }
        public Double getCantidadComprada() { return cantidadComprada; }
        public Double getDisponibleDevolver() { return disponibleDevolver; }
        public Double getPrecioUnitario() { return precioUnitario; }
        public Double getCantidadADevolver() { return cantidadADevolver; }
        public void setCantidadADevolver(Double cantidadADevolver) { this.cantidadADevolver = cantidadADevolver; }
    }
}

//
//public class DialogoDevolucionVenta extends Dialog {
//
//    private final FacturaDeVenta factura;
//    private final FacturaDeVentaService facturaService;
//    private final String usuarioActual;
//    private final Runnable onSuccessCallback;
//
//    @Autowired
//    private TipoMovimientoService tipoMovimientoRepo;
//
//    @Autowired
//    private TipoDocumentoService tipoDocumentoRepo;
//
//    private final Map<Integer, Double> cantidadesADevolverMap = new HashMap<>();
//    private final TextArea txtMotivo = new TextArea("Motivo de la Devolución");
//    private final Span lblMontoTotalDevolucion = new Span("Total Reembolso: $0.00");
//
//    public DialogoDevolucionVenta(FacturaDeVenta factura, FacturaDeVentaService facturaService,
//            String usuarioActual, Runnable onSuccessCallback) {
//        this.factura = factura;
//        this.facturaService = facturaService;
//        this.usuarioActual = usuarioActual;
//        this.onSuccessCallback = onSuccessCallback;
//
//        setHeaderTitle("Devolución de Venta - Factura #" + factura.getCodigo());
//        setWidth("750px");
//
//        VerticalLayout layoutContenido = new VerticalLayout();
//        layoutContenido.setPadding(false);
//        layoutContenido.setSpacing(true);
//
//        // 1. Grilla de ítems vendibles con control numérico
//        Grid<DetalleFacturaDeVenta> grid = new Grid<>(DetalleFacturaDeVenta.class, false);
//        grid.setAllRowsVisible(true);
//
//        grid.addColumn(d -> d.getArticulo().getDescripcion()).setHeader("Artículo").setFlexGrow(2);
//        grid.addColumn(DetalleFacturaDeVenta::getCantidad).setHeader("Vendida");
//        grid.addColumn(DetalleFacturaDeVenta::getCantidadDevuelta).setHeader("Devuelta Previa");
////        grid.addColumn(DetalleFacturaDeVenta::getCantidadDisponibleParaDevolucion).setHeader("Disponible");
//
//        grid.addComponentColumn(detalle -> {
//
//            double disponible = detalle.getCantidad();
//            NumberField numDevolver = new NumberField();
//            numDevolver.setMin(0);
//            numDevolver.setMax(disponible);
//            numDevolver.setStep(1);
//            numDevolver.setValue(0.0);
////            numDevolver.setHasControls(true);
//            numDevolver.setWidth("120px");
//
//            if (disponible <= 0) {
//                numDevolver.setEnabled(false);
//            }
//
//            numDevolver.addValueChangeListener(e -> {
//                double valor = e.getValue() != null ? e.getValue() : 0.0;
//                cantidadesADevolverMap.put(detalle.getCodigo(), valor);
//                recalcularMontoReembolso();
//            });
//
//            return numDevolver;
//        }).setHeader("A Devolver").setFlexGrow(1);
//
//        grid.setItems(factura.getDetalleFacturaDeVentaCollection());
//
//        // 2. Campo de Motivo y resumen de total
//        txtMotivo.setWidthFull();
//        txtMotivo.setPlaceholder("Escriba la razón de la devolución...");
//
//        lblMontoTotalDevolucion.getStyle().set("font-weight", "bold").set("font-size", "1.1em");
//
//        layoutContenido.add(grid, txtMotivo, lblMontoTotalDevolucion);
//        add(layoutContenido);
//
//        // 3. Botones de Acción
//        Button btnCancelar = new Button("Cancelar", e -> close());
//        Button btnProcesar = new Button("Procesar Devolución", e -> {
//
//            try {
//
//                ejecutarDevolucion();
//            } catch (Exception ex) {
//                System.out.println("Eror devolucion " + ex.getMessage());
//                ex.printStackTrace();
//            }
//        });
//
//        btnProcesar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
//
//        HorizontalLayout footerLayout = new HorizontalLayout(btnCancelar, btnProcesar);
//        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
//        footerLayout.setWidthFull();
//        getFooter().add(footerLayout);
//    }
//
//    private void recalcularMontoReembolso() {
//        BigDecimal totalReembolso = BigDecimal.ZERO;
//
//        for (DetalleFacturaDeVenta detalle : factura.getDetalleFacturaDeVentaCollection()) {
//            Double cant = cantidadesADevolverMap.getOrDefault(detalle.getCodigo(), 0.0);
//            if (cant > 0) {
//                BigDecimal sub = BigDecimal.valueOf(detalle.getPrecioCompra()).multiply(BigDecimal.valueOf(cant));
//                totalReembolso = totalReembolso.add(sub);
//            }
//        }
//
//        lblMontoTotalDevolucion.setText(String.format("Total Reembolso: $%.2f", totalReembolso));
//    }
//
//    private void ejecutarDevolucion() {
//
//        if (txtMotivo.getValue() == null || txtMotivo.getValue().trim().isEmpty()) {
//
//            Notification.show("Debe especificar el motivo de la devolución.", 3000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            return;
//        }
//
////        TipoMovimiento tipoSalida = tipoMovimientoRepo.getTipoMovimientoa(1);//
////        //.orElseThrow(() -> new IllegalStateException("Tipo de movimiento 'SALIDA' no configurado."));
////
////        TipoDocumento tipoFactura = tipoDocumentoRepo.getTipoDocumento(1);//
//        
//        SolicitudDevolucionDto solicitud = new SolicitudDevolucionDto();
//
////        solicitud.setTipoDocumento(tipoFactura);
////        solicitud.setTipoMovimiento(tipoSalida);
//        solicitud.setIdFactura(factura.getCodigo());
//        solicitud.setMotivo(txtMotivo.getValue().trim());
//        solicitud.setUsuario(usuarioActual);
//        solicitud.setItems(new ArrayList<>());
//
//        cantidadesADevolverMap.forEach((idDetalle, cantidad) -> {
//            if (cantidad > 0) {
//                SolicitudDevolucionDto.ItemDevolucionDto item = new SolicitudDevolucionDto.ItemDevolucionDto();
//                item.setIdDetalleFactura(idDetalle);
//                item.setCantidadADevolver(cantidad);
//                solicitud.getItems().add(item);
//            }
//        });
//
//        if (solicitud.getItems().isEmpty()) {
//            Notification.show("Seleccione al menos un artículo para devolver.", 3000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
//            return;
//        }
//
//        try {
//
//            facturaService.procesarDevolucion(solicitud);
//            Notification.show("Devolución procesada correctamente.", 3000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//
//            close();
//            if (onSuccessCallback != null) {
//                onSuccessCallback.run();
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        }
//    }
//}
