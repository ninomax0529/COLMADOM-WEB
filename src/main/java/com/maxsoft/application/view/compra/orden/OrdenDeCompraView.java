package com.maxsoft.application.view.compra.orden;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleOrdendeDeCompra;
import com.maxsoft.application.modelo.OrdenDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.util.ClaseUtil;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PageTitle("Nueva Orden de Compra")
@Route(value = "compra/ordenCompra")

public class OrdenDeCompraView extends VerticalLayout {

    private final OrdenDeCompraService ordenCompraService;
    private final ProveedorService proveedorService;
    private final ArticuloService articuloService;

    // Modelos
    private final OrdenDeCompra ordenCompra = new OrdenDeCompra();
    private final List<DetalleOrdendeDeCompra> detalles = new ArrayList<>();
    private final Binder<OrdenDeCompra> binder = new Binder<>(OrdenDeCompra.class);

    // Encabezado
    private final TextField txtNumero = new TextField("Número Orden");
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("Suplidor / Proveedor");
    private final TextField txtTipoProveedor = new TextField("Tipo Proveedor");
    private final TextField txtPlazoProveedor = new TextField("Plazo de Pago");
    private final DatePicker dtFecha = new DatePicker("Fecha");
    private final DatePicker dtFechaEntrega = new DatePicker("Fecha Compromiso Entrega");
    private final TextArea txtComentario = new TextArea("Comentarios / Observaciones");

    // Formulario de Detalle
    private final ComboBox<Articulo> cbArticulo = new ComboBox<>("Artículo");
    private final TextField txtDescripcion = new TextField("Descripción");
    private final NumberField numCantidad = new NumberField("Cantidad");
    private final NumberField numPrecio = new NumberField("Precio Unitario");
    private final NumberField numItbisPorc = new NumberField("% ITBIS");
    private final Button btnAgregarDetalle = new Button("Agregar Detalle", VaadinIcon.PLUS.create());

    // Grid
    private final Grid<DetalleOrdendeDeCompra> gridDetalles = new Grid<>(DetalleOrdendeDeCompra.class, false);

    // Totales
    private final Span lblSubTotal = new Span("RD$ 0.00");
    private final Span lblItbis = new Span("RD$ 0.00");
    private final Span lblTotal = new Span("RD$ 0.00");

    // Botones
    private final Button btnGuardar = new Button("Guardar Orden", VaadinIcon.CHECK.create());
    private final Button btnCancelar = new Button("Cancelar", VaadinIcon.CLOSE.create());

    public OrdenDeCompraView(OrdenDeCompraService ordenCompraService,
            ProveedorService proveedorService,
            ArticuloService articuloService) {
        this.ordenCompraService = ordenCompraService;
        this.proveedorService = proveedorService;
        this.articuloService = articuloService;

        setSpacing(true);
        setPadding(true);

        add(new H2("Nueva Orden de Compra"));

        crearFormularioEncabezado();
        crearSeccionAgregarDetalle();
        configurarGridDetalles();
        crearSeccionTotalesYAcciones();

        cargarDatosIniciales();
    }

    private void crearFormularioEncabezado() {
        FormLayout formLayout = new FormLayout();

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(s -> s.getRnc() != null ? s.getNombre() + " (" + s.getRnc() + ")" : s.getNombre());
        cbSuplidor.setRequired(true);
        cbSuplidor.addValueChangeListener(e -> actualizarDatosProveedor(e.getValue()));

        txtTipoProveedor.setReadOnly(true);
        txtPlazoProveedor.setReadOnly(true);

        dtFecha.setValue(LocalDate.now());
        dtFechaEntrega.setValue(LocalDate.now().plusDays(7));

        txtComentario.setMinHeight("75px");

        formLayout.add(txtNumero, cbSuplidor, txtTipoProveedor, txtPlazoProveedor, dtFecha, dtFechaEntrega);
        formLayout.add(txtComentario);

        formLayout.setColspan(txtComentario, 3);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        add(formLayout);
    }

    private void actualizarDatosProveedor(Proveedor proveedor) {
        if (proveedor != null) {
            txtTipoProveedor.setValue(proveedor.getTipoSuplidor() != null ? proveedor.getTipoSuplidor().getNombre() : "N/A");
            txtPlazoProveedor.setValue(proveedor.getPlazo() != null ? proveedor.getPlazo().getDescripcion() : "Contado");

            if (proveedor.getPlazo() != null) {
                dtFechaEntrega.setValue(LocalDate.now().plusDays(proveedor.getPlazo().getDias()));
            }
        } else {
            txtTipoProveedor.clear();
            txtPlazoProveedor.clear();
        }
    }

    private void crearSeccionAgregarDetalle() {
        HorizontalLayout layoutDetalle = new HorizontalLayout();
        layoutDetalle.setWidthFull();
        layoutDetalle.setAlignItems(FlexComponent.Alignment.BASELINE);

        cbArticulo.setItems(articuloService.getLista());
        cbArticulo.setItemLabelGenerator(a -> a.getCodigo() + " - " + a.getDescripcion());
        cbArticulo.setPlaceholder("Buscar artículo...");
        cbArticulo.setWidth("20%");
        cbArticulo.addValueChangeListener(e -> seleccionarArticulo(e.getValue()));

        txtDescripcion.setPlaceholder("Descripción del artículo");
        txtDescripcion.setWidth("30%");

        numCantidad.setMin(1);
        numCantidad.setValue(1.0);
        numCantidad.setWidth("12%");

        // Atajo enfocado únicamente en la caja de texto "Cantidad"
        numCantidad.addKeyPressListener(Key.ENTER, e -> {

            try {

                agregarDetalleAGrid();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        numPrecio.setMin(0);
        numPrecio.setPlaceholder("0.00");
        numPrecio.setWidth("13%");

        numItbisPorc.setPlaceholder("18.0");
        numItbisPorc.setValue(18.0);
        numItbisPorc.setWidth("10%");

        btnAgregarDetalle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarDetalle.addClickListener(e -> agregarDetalleAGrid());

        layoutDetalle.add(cbArticulo, txtDescripcion, numCantidad, numPrecio, numItbisPorc, btnAgregarDetalle);
        add(layoutDetalle);
    }

    private void seleccionarArticulo(Articulo articulo) {
        if (articulo != null) {
            txtDescripcion.setValue(articulo.getDescripcion() != null ? articulo.getDescripcion() : "");
            numPrecio.setValue(articulo.getPrecioCompra() != null ? articulo.getPrecioCompra().doubleValue() : 0.0);
            numItbisPorc.setValue(articulo.getPrecioCompra() != null ? articulo.getPrecioCompra().doubleValue() : 18.0);
            numCantidad.focus();
        }
    }

    private void configurarGridDetalles() {
        gridDetalles.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        gridDetalles.addColumn(d -> d.getArticulo() != null ? d.getArticulo().getCodigo() : "-").setHeader("Código").setAutoWidth(true);
        gridDetalles.addColumn(DetalleOrdendeDeCompra::getDescripcionArticulo).setHeader("Descripción").setFlexGrow(3);

        // Edición de Cantidad
        gridDetalles.addComponentColumn(detalle -> {
            NumberField fieldCantidad = new NumberField();
            fieldCantidad.setValue(detalle.getCantidad() != null ? detalle.getCantidad().doubleValue() : 1.0);
            fieldCantidad.setMin(0.01);
            fieldCantidad.setWidth("100px");

            fieldCantidad.addValueChangeListener(e -> {
                if (e.isFromClient() && e.getValue() != null && e.getValue() > 0) {
                    recalcularFila(detalle, BigDecimal.valueOf(e.getValue()), detalle.getPrecioUnitario());
                }
            });
            return fieldCantidad;
        }).setHeader("Cantidad").setAutoWidth(true);

        // Edición de Precio Unitario
        gridDetalles.addComponentColumn(detalle -> {
            NumberField fieldPrecio = new NumberField();
            fieldPrecio.setValue(detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario().doubleValue() : 0.0);
            fieldPrecio.setMin(0.0);
            fieldPrecio.setWidth("120px");

            fieldPrecio.addValueChangeListener(e -> {
                if (e.isFromClient() && e.getValue() != null && e.getValue() >= 0) {
                    recalcularFila(detalle, detalle.getCantidad(), BigDecimal.valueOf(e.getValue()));
                }
            });
            return fieldPrecio;
        }).setHeader("P. Unitario").setAutoWidth(true);

        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getItbis())).setHeader("ITBIS").setAutoWidth(true);
        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getSubTotal())).setHeader("SubTotal").setAutoWidth(true);
        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getTotal())).setHeader("Total").setAutoWidth(true);

        // Eliminación de Fila
        gridDetalles.addComponentColumn(detalle -> {
            Button btnEliminar = new Button(VaadinIcon.TRASH.create(), e -> confirmarEliminarDetalle(detalle));
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return btnEliminar;
        }).setHeader("Acciones").setAutoWidth(true);

        gridDetalles.setItems(detalles);
        gridDetalles.setHeight("280px");

        add(gridDetalles);
    }

    private void recalcularFila(DetalleOrdendeDeCompra detalle, BigDecimal nuevaCantidad, BigDecimal nuevoPrecio) {
        BigDecimal porcItbis = BigDecimal.ZERO;

        if (detalle.getSubTotal() != null && detalle.getSubTotal().compareTo(BigDecimal.ZERO) > 0) {
            porcItbis = detalle.getItbis().multiply(BigDecimal.valueOf(100)).divide(detalle.getSubTotal(), 2, RoundingMode.HALF_UP);
        } else if (numItbisPorc.getValue() != null) {
            porcItbis = BigDecimal.valueOf(numItbisPorc.getValue());
        }

        BigDecimal subTotal = nuevaCantidad.multiply(nuevoPrecio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = subTotal.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subTotal.add(itbis);

        detalle.setCantidad(nuevaCantidad);
        detalle.setPendiente(nuevaCantidad);
        detalle.setPrecioUnitario(nuevoPrecio);
        detalle.setSubTotal(subTotal);
        detalle.setItbis(itbis);
        detalle.setTotal(total);

        actualizarTotalesGenerales();
        gridDetalles.getDataProvider().refreshItem(detalle);
    }

    private void confirmarEliminarDetalle(DetalleOrdendeDeCompra detalle) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar eliminación");
        dialog.setText("¿Está seguro de que desea eliminar el artículo '" + detalle.getDescripcionArticulo() + "' de la orden?");

        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");

        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            detalles.remove(detalle);
            actualizarGridYTotales();
            Notification.show("Artículo eliminado de la orden.", 2500, Notification.Position.BOTTOM_END);
        });

        dialog.open();
    }

    private void agregarDetalleAGrid() {
        // 1. Validar campos obligatorios
        if ((cbArticulo.isEmpty() && txtDescripcion.isEmpty()) || numCantidad.getValue() == null || numPrecio.getValue() == null) {
            Notification.show("Seleccione un artículo o ingrese una descripción, cantidad y precio válidos.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Articulo articuloSeleccionado = cbArticulo.getValue();
        String descripcionIngresada = txtDescripcion.getValue() != null ? txtDescripcion.getValue().trim() : "";

        // 2. Validar si el artículo ya existe en la lista
        boolean yaExiste = detalles.stream().anyMatch(d -> {
            // Caso A: Coincide por objeto Articulo (si proviene del ComboBox)
            if (articuloSeleccionado != null && d.getArticulo() != null) {
                return d.getArticulo().getCodigo().equals(articuloSeleccionado.getCodigo());
            }
            // Caso B: Coincide por la Descripción del texto
            return d.getDescripcionArticulo() != null
                    && d.getDescripcionArticulo().equalsIgnoreCase(descripcionIngresada);
        });

        if (yaExiste) {
            Notification.show("El artículo '" + (articuloSeleccionado != null ? articuloSeleccionado.getDescripcion() : descripcionIngresada) + "' ya está agregado en la orden.", 3500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        // 3. Crear y calcular el nuevo detalle
        BigDecimal cantidad = BigDecimal.valueOf(numCantidad.getValue());
        BigDecimal precioUnitario = BigDecimal.valueOf(numPrecio.getValue());
        BigDecimal porcItbis = BigDecimal.valueOf(numItbisPorc.getValue() != null ? numItbisPorc.getValue() : 0.0);

        BigDecimal subTotal = cantidad.multiply(precioUnitario).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = subTotal.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subTotal.add(itbis);

        DetalleOrdendeDeCompra detalle = new DetalleOrdendeDeCompra();
        detalle.setCodigo(articuloSeleccionado.getCodigo());
        detalle.setArticulo(articuloSeleccionado);
        detalle.setDescripcionArticulo(descripcionIngresada);
        detalle.setCantidad(cantidad);
        detalle.setCantidadRecibida(BigDecimal.ZERO);
        detalle.setPendiente(cantidad);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubTotal(subTotal);
        detalle.setItbis(itbis);
        detalle.setTotal(total);

        detalles.add(detalle);
        actualizarGridYTotales();
        limpiarFormularioDetalle();
    }

//    private void agregarDetalleAGrid() {
//        
//        if ((cbArticulo.isEmpty() && txtDescripcion.isEmpty()) || numCantidad.getValue() == null || numPrecio.getValue() == null) {
//            Notification.show("Seleccione un artículo o ingrese una descripción, cantidad y precio válidos.", 3000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            return;
//        }
//
//        BigDecimal cantidad = BigDecimal.valueOf(numCantidad.getValue());
//        BigDecimal precioUnitario = BigDecimal.valueOf(numPrecio.getValue());
//        BigDecimal porcItbis = BigDecimal.valueOf(numItbisPorc.getValue() != null ? numItbisPorc.getValue() : 0.0);
//
//        BigDecimal subTotal = cantidad.multiply(precioUnitario).setScale(2, RoundingMode.HALF_UP);
//        BigDecimal itbis = subTotal.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//        BigDecimal total = subTotal.add(itbis);
//
//        DetalleOrdendeDeCompra detalle = new DetalleOrdendeDeCompra();
//        detalle.setCodigo(cbArticulo.getValue().getCodigo());
//        detalle.setArticulo(cbArticulo.getValue());
//        detalle.setDescripcionArticulo(txtDescripcion.getValue());
//        detalle.setCantidad(cantidad);
//        detalle.setCantidadRecibida(BigDecimal.ZERO);
//        detalle.setPendiente(cantidad);
//        detalle.setPrecioUnitario(precioUnitario);
//        detalle.setSubTotal(subTotal);
//        detalle.setItbis(itbis);
//        detalle.setTotal(total);
//
//        detalles.add(detalle);
//        actualizarGridYTotales();
//        limpiarFormularioDetalle();
//    }
    private void actualizarGridYTotales() {
        gridDetalles.getDataProvider().refreshAll();
        actualizarTotalesGenerales();
    }

    private void actualizarTotalesGenerales() {
        BigDecimal subTotalGen = detalles.stream().map(DetalleOrdendeDeCompra::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal itbisGen = detalles.stream().map(DetalleOrdendeDeCompra::getItbis).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGen = subTotalGen.add(itbisGen);

        lblSubTotal.setText(String.format("RD$ %,.2f", subTotalGen));
        lblItbis.setText(String.format("RD$ %,.2f", itbisGen));
        lblTotal.setText(String.format("RD$ %,.2f", totalGen));
    }

    private void limpiarFormularioDetalle() {
        cbArticulo.clear();
        txtDescripcion.clear();
        numCantidad.setValue(1.0);
        numPrecio.clear();
        cbArticulo.focus();
    }

    private void crearSeccionTotalesYAcciones() {
        HorizontalLayout layoutInferior = new HorizontalLayout();
        layoutInferior.setWidthFull();
        layoutInferior.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layoutInferior.setAlignItems(FlexComponent.Alignment.END);

        FormLayout layoutTotales = new FormLayout();
        layoutTotales.addFormItem(lblSubTotal, "SubTotal:");
        layoutTotales.addFormItem(lblItbis, "ITBIS (18%):");
        layoutTotales.addFormItem(lblTotal, "Total General:");

        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardar.addClickListener(e -> guardarOrden());

        btnCancelar.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("ordenes-compra")));

        HorizontalLayout layoutBotones = new HorizontalLayout(btnGuardar, btnCancelar);

        layoutInferior.add(layoutBotones, layoutTotales);
        add(layoutInferior);
    }

    private void cargarDatosIniciales() {
        txtNumero.setValue("OC-" + System.currentTimeMillis() / 1000);
        txtNumero.setReadOnly(true);
    }

    private void guardarOrden() {

        if (cbSuplidor.isEmpty()) {
            Notification.show("Debe seleccionar un Suplidor.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (detalles.isEmpty()) {
            Notification.show("Debe agregar al menos un artículo a la orden.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            ordenCompra.setNumero(txtNumero.getValue());
            ordenCompra.setProveedor(cbSuplidor.getValue());
            ordenCompra.setFecha(Date.from(dtFecha.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            ordenCompra.setFechaDeEntrega(Date.from(dtFechaEntrega.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            ordenCompra.setComentario(txtComentario.getValue());
            ordenCompra.setEstado("PENDIENTE");
            ordenCompra.setCompletada("NO");
            ordenCompra.setFacturada(false);
            ordenCompra.setAnulada(false);

            BigDecimal totalG = detalles.stream().map(DetalleOrdendeDeCompra::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal itbisG = detalles.stream().map(DetalleOrdendeDeCompra::getItbis).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal subG = detalles.stream().map(DetalleOrdendeDeCompra::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

            ordenCompra.setTotal(totalG);
            ordenCompra.setTotalItbis(itbisG);
            ordenCompra.setSubtotal(subG);

            detalles.forEach(o -> {

                o.setCodigo(null);
                o.setOrdenDeCompra(ordenCompra);

            });

            ordenCompra.setDetalleOrdendeDeCompraCollection(detalles);

            ordenCompraService.guardar(ordenCompra);

            ClaseUtil.mostrarNotificacion("Orden de Compra guardada exitosamente.", NotificationVariant.LUMO_PRIMARY);
//            Notification.show(, 3000, Notification.Position.BOTTOM_END)
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            limpiarFormularioCompleto();

//            getUI().ifPresent(ui -> ui.navigate());
        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al guardar la orden: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarFormularioCompleto() {
        // 1. Limpiar lista y actualizar Grid y Totales
        detalles.clear();
        actualizarGridYTotales();

        // 2. Generar un nuevo número de orden
        txtNumero.setValue("OC-" + System.currentTimeMillis() / 1000);

        // 3. Resetear datos del Encabezado
        cbSuplidor.clear();
        txtTipoProveedor.clear();
        txtPlazoProveedor.clear();
        dtFecha.setValue(LocalDate.now());
        dtFechaEntrega.setValue(LocalDate.now().plusDays(7));
        txtComentario.clear();

        // 4. Resetear la sección de Detalle
        limpiarFormularioDetalle();

        // 5. Enfocar el primer campo editable
        cbSuplidor.focus();
    }
}
