package com.maxsoft.application.view.compra.orden;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleOrdendeDeCompra;
import com.maxsoft.application.modelo.OrdenDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Nueva Orden de Compra")
@Route(value = "compra/ordenCompra")
public class OrdenDeCompraView extends VerticalLayout {

    private final OrdenDeCompraService ordenCompraService;
    private final ProveedorService proveedorService;
    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService;

    // Modelos
    private final OrdenDeCompra ordenCompra = new OrdenDeCompra();
    private final List<DetalleOrdendeDeCompra> detalles = new ArrayList<>();
    private final Binder<OrdenDeCompra> binder = new Binder<>(OrdenDeCompra.class);

    // % de ITBIS por línea (IdentityHashMap: equals de la entidad puede depender del id, que aquí es null)
    private final Map<DetalleOrdendeDeCompra, BigDecimal> itbisPorcPorDetalle = new IdentityHashMap<>();

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
    private final ComboBox<ArticuloEmpaque> cbEmpaque = new ComboBox<>("Presentación / Empaque");
    private final TextField txtDescripcion = new TextField("Descripción");
    private final NumberField numCantidad = new NumberField("Cantidad");
    private final NumberField numPrecio = new NumberField("P. Unitario Empaque");
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
            ArticuloService articuloService,
            ArticuloEmpaqueService articuloEmpaqueService) {
        this.ordenCompraService = ordenCompraService;
        this.proveedorService = proveedorService;
        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;

        setSpacing(true);
        setPadding(true);

        add(new H2("Nueva Orden de Compra"));

        crearFormularioEncabezado();
        crearSeccionAgregarDetalle();
        configurarGridDetalles();
        crearSeccionTotalesYAcciones();

        cargarDatosIniciales();
    }

    // =====================================================================
    // ENCABEZADO
    // =====================================================================
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

    // =====================================================================
    // AGREGAR DETALLE (con multi-empaque)
    // =====================================================================
    private void crearSeccionAgregarDetalle() {
        HorizontalLayout layoutDetalle = new HorizontalLayout();
        layoutDetalle.setWidthFull();
        layoutDetalle.setAlignItems(FlexComponent.Alignment.BASELINE);

        cbArticulo.setItems(articuloService.getLista());
        cbArticulo.setItemLabelGenerator(a -> a.getCodigo() + " - " + a.getDescripcion());
        cbArticulo.setPlaceholder("Buscar artículo...");
        cbArticulo.setWidth("20%");
        cbArticulo.addValueChangeListener(e -> seleccionarArticulo(e.getValue()));

        cbEmpaque.setItemLabelGenerator(this::etiquetaEmpaque);
        cbEmpaque.setPlaceholder("Seleccionar Empaque...");
        cbEmpaque.setWidth("18%");
        cbEmpaque.addValueChangeListener(e -> sugerirPrecioDeEmpaque(e.getValue()));

        txtDescripcion.setPlaceholder("Descripción del artículo");
        txtDescripcion.setWidth("22%");

        numCantidad.setMin(0.01);
        numCantidad.setValue(1.0);
        numCantidad.setWidth("10%");
        numCantidad.addKeyPressListener(Key.ENTER, e ->{
            agregarDetalleAGrid();
        });

        numPrecio.setMin(0);
        numPrecio.setPlaceholder("0.00");
        numPrecio.setWidth("12%");

        numItbisPorc.setPlaceholder("18.0");
        numItbisPorc.setValue(18.0);
        numItbisPorc.setWidth("8%");

        btnAgregarDetalle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarDetalle.addClickListener(e ->{
         agregarDetalleAGrid();
        });

        layoutDetalle.add(cbArticulo, cbEmpaque, txtDescripcion, numCantidad, numPrecio, numItbisPorc, btnAgregarDetalle);
        add(layoutDetalle);
    }

    private String etiquetaEmpaque(ArticuloEmpaque e) {
        String unidad = e.getUnidadEmpaque() != null ? e.getUnidadEmpaque().getDescripcion() : "Sin unidad";
        BigDecimal f = e.getFactorConversion() != null ? e.getFactorConversion() : BigDecimal.ONE;
        return unidad + " (x" + f.stripTrailingZeros().toPlainString() + ")";
    }

    private void seleccionarArticulo(Articulo articulo) {
        cbEmpaque.clear();
        cbEmpaque.setItems(List.of());

        if (articulo == null) {
            return;
        }

        txtDescripcion.setValue(articulo.getDescripcion() != null ? articulo.getDescripcion() : "");

        List<ArticuloEmpaque> todos = articuloEmpaqueService.getPorArticulo(articulo.getCodigo());
        if (todos == null || todos.isEmpty()) {
            Notification.show("El artículo " + articulo.getCodigo() + " no tiene empaques configurados.",
                    3500, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        // Preferir los habilitados para compra; si ninguno lo está, mostrar todos
        List<ArticuloEmpaque> empaquesCompra = todos.stream()
                .filter(emp -> Boolean.TRUE.equals(emp.getSeCompraEn()))
                .toList();
        List<ArticuloEmpaque> disponibles = empaquesCompra.isEmpty() ? todos : empaquesCompra;

        cbEmpaque.setItems(disponibles);

        // Autoseleccionar el empaque base (o el primero); esto dispara la sugerencia de precio
        disponibles.stream()
                .filter(emp -> Boolean.TRUE.equals(emp.getEsEmpaqueBase()))
                .findFirst()
                .or(() -> disponibles.stream().findFirst())
                .ifPresent(cbEmpaque::setValue);

        numCantidad.focus();
    }

    /**
     * Sugiere el precio de compra del empaque; si no tiene, el del artículo.
     */
    private void sugerirPrecioDeEmpaque(ArticuloEmpaque emp) {
        if (emp != null && emp.getPrecioCompra() != null) {
            numPrecio.setValue(emp.getPrecioCompra().doubleValue());
        } else if (cbArticulo.getValue() != null && cbArticulo.getValue().getPrecioCompra() != null) {
            numPrecio.setValue(cbArticulo.getValue().getPrecioCompra().doubleValue());
        }
    }

    private void agregarDetalleAGrid() {
        
        try {
            if (cbArticulo.isEmpty() || cbEmpaque.isEmpty()
                    || numCantidad.getValue() == null || numCantidad.getValue() <= 0
                    || numPrecio.getValue() == null) {
                Notification.show("Seleccione artículo, empaque, cantidad y precio válidos.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Articulo articulo = cbArticulo.getValue();
            ArticuloEmpaque empaque = cbEmpaque.getValue();
            String descripcion = txtDescripcion.getValue() != null && !txtDescripcion.getValue().isBlank()
                    ? txtDescripcion.getValue().trim() : articulo.getDescripcion();

            // Duplicado = mismo artículo + mismo empaque (el mismo artículo en otra presentación sí se permite)
            boolean yaExiste = detalles.stream().anyMatch(d
                    -> d.getArticulo() != null && d.getArticulo().getCodigo().equals(articulo.getCodigo())
                    && d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getCodigo().equals(empaque.getCodigo()));

            if (yaExiste) {
                Notification.show("Esta presentación del artículo ya está agregada en la orden.", 3500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            BigDecimal cantidad = BigDecimal.valueOf(numCantidad.getValue());
            BigDecimal precioEmpaque = BigDecimal.valueOf(numPrecio.getValue());
            BigDecimal porcItbis = BigDecimal.valueOf(numItbisPorc.getValue() != null ? numItbisPorc.getValue() : 0.0);
            BigDecimal factor = empaque.getFactorConversion() != null ? empaque.getFactorConversion() : BigDecimal.ONE;

            BigDecimal subTotal = cantidad.multiply(precioEmpaque).setScale(2, RoundingMode.HALF_UP);
            BigDecimal itbis = subTotal.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            DetalleOrdendeDeCompra detalle = new DetalleOrdendeDeCompra();
            detalle.setCodigo(articulo.getCodigo());
            detalle.setArticulo(articulo);
            detalle.setDescripcionArticulo(descripcion);

            // Multi-empaque (estos campos deben existir en DetalleOrdendeDeCompra)
            detalle.setArticuloEmpaque(empaque);
            detalle.setFactorConversion(factor);
            detalle.setCantidadFisicaBase(cantidad.multiply(factor));


            detalle.setCantidad(cantidad);              // en unidades del empaque
            detalle.setCantidadRecibida(BigDecimal.ZERO);
            detalle.setPendiente(cantidad);             // pendiente también en unidades del empaque
            detalle.setPrecioUnitario(precioEmpaque);   // precio por empaque
            detalle.setSubTotal(subTotal);
            detalle.setItbis(itbis);
            detalle.setTotal(subTotal.add(itbis));

            detalles.add(detalle);
            itbisPorcPorDetalle.put(detalle, porcItbis);
            actualizarGridYTotales();
            limpiarFormularioDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al agregar el detalle: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // =====================================================================
    // GRID
    // =====================================================================
    private void configurarGridDetalles() {
        gridDetalles.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        gridDetalles.addColumn(d -> d.getArticulo() != null ? d.getArticulo().getCodigo() : "-").setHeader("Código").setAutoWidth(true);
        gridDetalles.addColumn(DetalleOrdendeDeCompra::getDescripcionArticulo).setHeader("Descripción").setFlexGrow(3);

        gridDetalles.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion() : "-").setHeader("Presentación").setAutoWidth(true);

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

        // Equivalencia en unidad base
        gridDetalles.addColumn(d -> d.getCantidadFisicaBase() != null
                ? d.getCantidadFisicaBase().stripTrailingZeros().toPlainString() + " (Base)" : "-")
                .setHeader("Equiv. Base").setAutoWidth(true);

        // Edición de Precio Unitario (por empaque)
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
        BigDecimal porcItbis = itbisPorcPorDetalle.getOrDefault(detalle, BigDecimal.ZERO);
        BigDecimal factor = detalle.getFactorConversion() != null ? detalle.getFactorConversion() : BigDecimal.ONE;

        BigDecimal subTotal = nuevaCantidad.multiply(nuevoPrecio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = subTotal.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        detalle.setCantidad(nuevaCantidad);
        detalle.setCantidadFisicaBase(nuevaCantidad.multiply(factor)); // mantener la conversión al cambiar la cantidad
        detalle.setPendiente(nuevaCantidad);
        detalle.setPrecioUnitario(nuevoPrecio);
        detalle.setSubTotal(subTotal);
        detalle.setItbis(itbis);
        detalle.setTotal(subTotal.add(itbis));

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
            itbisPorcPorDetalle.remove(detalle);
            actualizarGridYTotales();
            Notification.show("Artículo eliminado de la orden.", 2500, Notification.Position.BOTTOM_END);
        });

        dialog.open();
    }

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
        cbEmpaque.clear();
        cbEmpaque.setItems(List.of());
        txtDescripcion.clear();
        numCantidad.setValue(1.0);
        numPrecio.clear();
        cbArticulo.focus();
    }

    // =====================================================================
    // TOTALES Y ACCIONES
    // =====================================================================
    private void crearSeccionTotalesYAcciones() {
        HorizontalLayout layoutInferior = new HorizontalLayout();
        layoutInferior.setWidthFull();
        layoutInferior.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layoutInferior.setAlignItems(FlexComponent.Alignment.END);

        FormLayout layoutTotales = new FormLayout();
        layoutTotales.addFormItem(lblSubTotal, "SubTotal:");
        layoutTotales.addFormItem(lblItbis, "ITBIS:");
        layoutTotales.addFormItem(lblTotal, "Total General:");

        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardar.addClickListener(e -> {
            guardarOrden();
        });

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
            ordenCompra.setNombreProveedor(cbSuplidor.getValue().getNombre());
            ordenCompra.setRnc(cbSuplidor.getValue().getRnc());
            ordenCompra.setPlazo(cbSuplidor.getValue().getPlazo());
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
                o.setUnidad(o.getArticuloEmpaque().getUnidadEmpaque());
            });

            ordenCompra.setDetalleOrdendeDeCompraCollection(detalles);

            ordenCompraService.guardar(ordenCompra);

            ClaseUtil.mostrarNotificacion("Orden de Compra guardada exitosamente.", NotificationVariant.LUMO_PRIMARY);
            limpiarFormularioCompleto();

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al guardar la orden: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarFormularioCompleto() {
        detalles.clear();
        itbisPorcPorDetalle.clear();
        actualizarGridYTotales();

        txtNumero.setValue("OC-" + System.currentTimeMillis() / 1000);

        cbSuplidor.clear();
        txtTipoProveedor.clear();
        txtPlazoProveedor.clear();
        dtFecha.setValue(LocalDate.now());
        dtFechaEntrega.setValue(LocalDate.now().plusDays(7));
        txtComentario.clear();

        limpiarFormularioDetalle();

        cbSuplidor.focus();
    }
}
