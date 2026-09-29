/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.compra.recepcion;

import com.maxsoft.application.modelo.*;
import com.maxsoft.application.servicio.interfaces.EstadoDocumentoService;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Route(value = "compra/recepcion")
@PageTitle("Recepción de Mercancía")
public class RecepcionMercanciaView extends VerticalLayout {

    private final RecepcionMercanciaService recepcionService;
    private final OrdenDeCompraService ordenCompraService;
    private final ProveedorService proveedorService;
    private final ArticuloService articuloService;
    TipoDocumentoService tipoDocumentoService;
    EstadoDocumentoService essDocumentoService;

    // Estado interno
    private final List<DetalleRecepcionMercancia> detalles = new ArrayList<>();

    // Encabezado
    private final TextField txtNumero = new TextField("N° Recepción");
    private final ComboBox<TipoDocumento> cbTipoDocumento = new ComboBox<>("Tipo Origen");
    private final ComboBox<OrdenDeCompra> cbOrdenCompra = new ComboBox<>("Orden de Compra");
    private final TextField txtDocOrigen = new TextField("N° Doc. Referencia / NCF");
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("Suplidor / Proveedor");
    private final DatePicker dtFecha = new DatePicker("Fecha Recepción");
    private final TextArea txtComentario = new TextArea("Comentarios / Observaciones");

    // Formulario Agregar Detalle
    private final ComboBox<Articulo> cbArticulo = new ComboBox<>("Artículo");
    private final TextField txtDescripcion = new TextField("Descripción");
    private final NumberField numCantidad = new NumberField("Cant. A Recibir");
    private final NumberField numPrecio = new NumberField("P. Unitario");
    private final NumberField numItbisPorc = new NumberField("% ITBIS");
    private final Button btnAgregarDetalle = new Button("Agregar Detalle", VaadinIcon.PLUS.create());

    // Grid
    private final Grid<DetalleRecepcionMercancia> gridDetalles = new Grid<>(DetalleRecepcionMercancia.class, false);

    // Totales
    private final Span lblSubTotal = new Span("RD$ 0.00");
    private final Span lblItbis = new Span("RD$ 0.00");
    private final Span lblTotal = new Span("RD$ 0.00");

    // Botones
    private final Button btnGuardar = new Button("Procesar Entrada", VaadinIcon.CHECK.create());
    private final Button btnCancelar = new Button("Cancelar", VaadinIcon.CLOSE.create());

    public RecepcionMercanciaView(RecepcionMercanciaService recepcionService,
            OrdenDeCompraService ordenCompraService,
            ProveedorService proveedorService,
            ArticuloService articuloService,
            TipoDocumentoService tipoDocumentoService,
            EstadoDocumentoService essDocumentoService) {

        this.recepcionService = recepcionService;
        this.ordenCompraService = ordenCompraService;
        this.proveedorService = proveedorService;
        this.articuloService = articuloService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.essDocumentoService = essDocumentoService;

        setSpacing(true);
        setPadding(true);

        add(new H2("Recepción e Ingreso de Mercancía"));

        crearFormularioEncabezado();
        crearSeccionAgregarDetalle();
        configurarGridDetalles();
        crearSeccionTotalesYAcciones();

        cargarDatosIniciales();
    }

    private void crearFormularioEncabezado() {

        FormLayout form = new FormLayout();

        cbTipoDocumento.setItems(this.tipoDocumentoService.getLista());
//        cbTipoDocumento.setValue("ORDEN_COMPRA");
        cbTipoDocumento.addValueChangeListener(e -> {

            try {

                cambiarTipoOrigen(e.getValue().getNombre());

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        cbOrdenCompra.setItemLabelGenerator(o -> o.getNumero() + " - " + (o.getProveedor() != null ? o.getProveedor().getNombre() : ""));
        cbOrdenCompra.setPlaceholder("Seleccione Orden...");
        cbOrdenCompra.addValueChangeListener(e -> cargarDesdeOrdenCompra(e.getValue()));

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(s -> s.getNombre());
        cbSuplidor.setRequired(true);

        dtFecha.setValue(LocalDate.now());

        txtComentario.setMinHeight("70px");

        form.add(txtNumero, cbTipoDocumento, cbOrdenCompra, txtDocOrigen, cbSuplidor, dtFecha);
        form.add(txtComentario);
        form.setColspan(txtComentario, 3);

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        add(form);
    }

    private void cambiarTipoOrigen(String tipo) {
        if ("ORDEN_COMPRA".equals(tipo)) {
            cbOrdenCompra.setVisible(true);
            cbOrdenCompra.setItems(ordenCompraService.getLista().stream()
                    .filter(o -> !"COMPLETADA".equalsIgnoreCase(o.getEstado()) && (o.getAnulada() == null || !o.getAnulada()))
                    .toList());
        } else {
            cbOrdenCompra.setVisible(false);
            cbOrdenCompra.clear();
            cbSuplidor.setReadOnly(false);
        }
    }

    private void cargarDesdeOrdenCompra(OrdenDeCompra oc) {
        if (oc == null) {
            return;
        }

        cbSuplidor.setValue(oc.getProveedor());
        cbSuplidor.setReadOnly(true);
        txtDocOrigen.setValue(oc.getNumero());

        detalles.clear();
        if (oc.getDetalleOrdendeDeCompraCollection() != null) {
            for (DetalleOrdendeDeCompra doc : oc.getDetalleOrdendeDeCompraCollection()) {
                BigDecimal pendiente = doc.getPendiente() != null ? doc.getPendiente() : doc.getCantidad();
                if (pendiente.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                DetalleRecepcionMercancia det = new DetalleRecepcionMercancia();
                det.setArticulo(doc.getArticulo());
                det.setDescripcionArticulo(doc.getDescripcionArticulo());
                det.setCantidadRecibida(pendiente);
                det.setPrecioUnitario(doc.getPrecioUnitario());

                BigDecimal sub = pendiente.multiply(doc.getPrecioUnitario()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal itbis = doc.getItbis() != null && doc.getSubTotal() != null && doc.getSubTotal().compareTo(BigDecimal.ZERO) > 0
                        ? sub.multiply(doc.getItbis()).divide(doc.getSubTotal(), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                det.setSubTotal(sub);
                det.setItbis(itbis);
                det.setTotal(sub.add(itbis));

                detalles.add(det);
            }
        }
        actualizarGridYTotales();
    }

    private void crearSeccionAgregarDetalle() {
        HorizontalLayout layoutDetalle = new HorizontalLayout();
        layoutDetalle.setWidthFull();
        layoutDetalle.setAlignItems(FlexComponent.Alignment.BASELINE);

        cbArticulo.setItems(articuloService.getLista());
        cbArticulo.setItemLabelGenerator(a -> a.getCodigo() + " - " + a.getDescripcion());
        cbArticulo.setPlaceholder("Buscar artículo...");
        cbArticulo.setWidth("20%");
        cbArticulo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                txtDescripcion.setValue(e.getValue().getDescripcion());
                numPrecio.setValue(e.getValue().getPrecioCompra() != null ? e.getValue().getPrecioCompra().doubleValue() : 0.0);
                numCantidad.focus();
            }
        });

        txtDescripcion.setWidth("30%");

        numCantidad.setMin(0.01);
        numCantidad.setValue(1.0);
        numCantidad.setWidth("12%");
        numCantidad.addKeyPressListener(Key.ENTER, e -> {

            try {

                agregarDetalleAGrid();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        numPrecio.setMin(0);
        numPrecio.setWidth("13%");

        numItbisPorc.setValue(18.0);
        numItbisPorc.setWidth("10%");

        btnAgregarDetalle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarDetalle.addClickListener(e -> agregarDetalleAGrid());

        layoutDetalle.add(cbArticulo, txtDescripcion, numCantidad, numPrecio, numItbisPorc, btnAgregarDetalle);
        add(layoutDetalle);
    }

    private void agregarDetalleAGrid() {
        if ((cbArticulo.isEmpty() && txtDescripcion.isEmpty()) || numCantidad.getValue() == null || numPrecio.getValue() == null) {
            Notification.show("Seleccione un artículo, cantidad y precio válidos.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Articulo artSel = cbArticulo.getValue();
        String desc = txtDescripcion.getValue() != null ? txtDescripcion.getValue().trim() : "";

        boolean yaExiste = detalles.stream().anyMatch(d -> {
            if (artSel != null && d.getArticulo() != null) {
                return d.getArticulo().getCodigo().equals(artSel.getCodigo());
            }
            return d.getDescripcionArticulo().equalsIgnoreCase(desc);
        });

        if (yaExiste) {
            Notification.show("El artículo ya está agregado.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        BigDecimal cant = BigDecimal.valueOf(numCantidad.getValue());
        BigDecimal prec = BigDecimal.valueOf(numPrecio.getValue());
        BigDecimal porcItbis = BigDecimal.valueOf(numItbisPorc.getValue() != null ? numItbisPorc.getValue() : 0.0);

        BigDecimal sub = cant.multiply(prec).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = sub.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        DetalleRecepcionMercancia det = new DetalleRecepcionMercancia();
        
        det.setCodigo(artSel.getCodigo());
        det.setArticulo(artSel);
        det.setUnidad(artSel.getUnidadEntrada());
        det.setNombreUnidad(artSel.getUnidadEntrada().getAbreviatura());
        det.setDescripcionArticulo(desc);
        det.setCantidadRecibida(cant);
        det.setPrecioUnitario(prec);
        det.setSubTotal(sub);
        det.setItbis(itbis);
        det.setTotal(sub.add(itbis));

        detalles.add(det);
        actualizarGridYTotales();
        limpiarFormularioDetalle();
    }

    private void configurarGridDetalles() {
        gridDetalles.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        gridDetalles.addColumn(d -> d.getArticulo() != null ? d.getArticulo().getCodigo() : "-").setHeader("Código").setAutoWidth(true);
        gridDetalles.addColumn(DetalleRecepcionMercancia::getDescripcionArticulo).setHeader("Descripción").setFlexGrow(3);

        // Edición de Cantidad Recibida
        gridDetalles.addComponentColumn(d -> {
            NumberField fieldCant = new NumberField();
            fieldCant.setValue(d.getCantidadRecibida() != null ? d.getCantidadRecibida().doubleValue() : 1.0);
            fieldCant.setMin(0.01);
            fieldCant.setWidth("100px");
            fieldCant.addValueChangeListener(e -> {
                if (e.isFromClient() && e.getValue() != null && e.getValue() > 0) {
                    recalcularFila(d, BigDecimal.valueOf(e.getValue()), d.getPrecioUnitario());
                }
            });
            return fieldCant;
        }).setHeader("Cant. Recibida").setAutoWidth(true);

        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getPrecioUnitario())).setHeader("P. Unitario").setAutoWidth(true);
        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getItbis())).setHeader("ITBIS").setAutoWidth(true);
        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getSubTotal())).setHeader("SubTotal").setAutoWidth(true);
        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getTotal())).setHeader("Total").setAutoWidth(true);

        gridDetalles.addComponentColumn(d -> {
            Button btnEliminar = new Button(VaadinIcon.TRASH.create(), e -> {
                detalles.remove(d);
                actualizarGridYTotales();
            });
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return btnEliminar;
        }).setHeader("Acciones").setAutoWidth(true);

        gridDetalles.setItems(detalles);
        gridDetalles.setHeight("280px");

        add(gridDetalles);
    }

    private void recalcularFila(DetalleRecepcionMercancia det, BigDecimal cant, BigDecimal prec) {
        BigDecimal porcItbis = det.getSubTotal() != null && det.getSubTotal().compareTo(BigDecimal.ZERO) > 0
                ? det.getItbis().multiply(BigDecimal.valueOf(100)).divide(det.getSubTotal(), 2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(18.0);

        BigDecimal sub = cant.multiply(prec).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = sub.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        det.setCantidadRecibida(cant);
        det.setPrecioUnitario(prec);
        det.setSubTotal(sub);
        det.setItbis(itbis);
        det.setTotal(sub.add(itbis));

        actualizarGridYTotales();
    }

    private void actualizarGridYTotales() {
        gridDetalles.getDataProvider().refreshAll();

        BigDecimal subG = detalles.stream().map(DetalleRecepcionMercancia::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal itbisG = detalles.stream().map(DetalleRecepcionMercancia::getItbis).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalG = subG.add(itbisG);

        lblSubTotal.setText(String.format("RD$ %,.2f", subG));
        lblItbis.setText(String.format("RD$ %,.2f", itbisG));
        lblTotal.setText(String.format("RD$ %,.2f", totalG));
    }

    private void crearSeccionTotalesYAcciones() {
        HorizontalLayout layoutInferior = new HorizontalLayout();
        layoutInferior.setWidthFull();
        layoutInferior.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layoutInferior.setAlignItems(FlexComponent.Alignment.END);

        FormLayout layoutTotales = new FormLayout();
        layoutTotales.addFormItem(lblSubTotal, "SubTotal:");
        layoutTotales.addFormItem(lblItbis, "ITBIS:");
        layoutTotales.addFormItem(lblTotal, "Total Entrada:");

        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardar.addClickListener(e -> {

            try {

                procesarRecepcion();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        btnCancelar.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("ordenes-compra")));

        HorizontalLayout layoutBotones = new HorizontalLayout(btnGuardar, btnCancelar);

        layoutInferior.add(layoutBotones, layoutTotales);
        add(layoutInferior);
    }

    private void cargarDatosIniciales() {
        txtNumero.setValue("REC-" + System.currentTimeMillis() / 1000);
        txtNumero.setReadOnly(true);
        cambiarTipoOrigen("ORDEN_COMPRA");
    }

    private void procesarRecepcion() {
        if (cbSuplidor.isEmpty()) {
            Notification.show("Seleccione un Suplidor.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (detalles.isEmpty()) {
            Notification.show("Agregue al menos un artículo recibido.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {

            RecepcionMercancia rec = new RecepcionMercancia();
            rec.setNumeroDocumento(txtNumero.getValue());
            rec.setFecha(Date.from(dtFecha.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            rec.setTipoDocumento(cbTipoDocumento.getValue());
            rec.setNombreDocumento(cbTipoDocumento.getValue().getNombre());
            rec.setEstadoDocumento(this.essDocumentoService.getEstado(2));
            rec.setNombreEstado(this.essDocumentoService.getEstado(2).getNombre());

            rec.setProveedor(cbSuplidor.getValue());
            rec.setNombreUsuario("Admin");
            rec.setUsuario(new Usuario(1));
            rec.setNombreProveedor(cbSuplidor.getValue().getNombre());
            rec.setComentario(txtComentario.getValue());

            if (cbOrdenCompra.getValue() != null) {

                rec.setNumeroDocumento(cbOrdenCompra.getValue().getCodigo().toString());
                rec.setTipoDocumento(cbTipoDocumento.getValue());

            } else {

                rec.setNombreDocumento(txtDocOrigen.getValue());
            }

            BigDecimal subG = detalles.stream().map(DetalleRecepcionMercancia::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal itbisG = detalles.stream().map(DetalleRecepcionMercancia::getItbis).reduce(BigDecimal.ZERO, BigDecimal::add);

            rec.setSubtotal(subG);
            rec.setTotalItbis(itbisG);
            rec.setTotal(subG.add(itbisG));

            rec.setDetalleRecepcionMercanciaCollection(new ArrayList<>(detalles));

            recepcionService.procesarRecepcion(rec);

            Notification.show("Recepción procesada exitosamente. Se ha actualizado el inventario.", 3500, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            limpiarFormularioCompleto();

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al procesar la recepción: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarFormularioCompleto() {
        detalles.clear();
        actualizarGridYTotales();

        txtNumero.setValue("REC-" + System.currentTimeMillis() / 1000);
//        cbTipoDocumento.setValue("ORDEN_COMPRA");
        cbOrdenCompra.clear();
        txtDocOrigen.clear();
        cbSuplidor.clear();
        cbSuplidor.setReadOnly(false);
        dtFecha.setValue(LocalDate.now());
        txtComentario.clear();

        limpiarFormularioDetalle();
        cbTipoDocumento.focus();
    }

    private void limpiarFormularioDetalle() {
        cbArticulo.clear();
        txtDescripcion.clear();
        numCantidad.setValue(1.0);
        numPrecio.clear();
    }
}
