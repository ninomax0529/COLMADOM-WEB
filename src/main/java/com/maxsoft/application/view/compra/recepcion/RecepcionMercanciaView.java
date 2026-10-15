package com.maxsoft.application.view.compra.recepcion;

import com.maxsoft.application.modelo.*;
import com.maxsoft.application.servicio.interfaces.EstadoDocumentoService;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.venta.UnidadDeVentaService;
import com.maxsoft.application.util.ClaseUtil;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.data.value.ValueChangeMode;
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

@Route(value = "compra/recepcion")
@PageTitle("Recepción de Mercancía")
public class RecepcionMercanciaView extends VerticalLayout {

    private static final Integer ORIGEN_ORDEN_COMPRA = 8;

    private final RecepcionMercanciaService recepcionService;
    private final OrdenDeCompraService ordenCompraService;
    private final ProveedorService proveedorService;
    private final ArticuloService articuloService;
    private final AlmacenService almacenService;
    private final ArticuloEmpaqueService articuloEmpaqueService;
    private final TipoDocumentoService tipoDocumentoService;
    private final EstadoDocumentoService essDocumentoService;
    private final UnidadDeVentaService unidadDeVentaService;

    // Estado interno
    private final List<DetalleRecepcionMercancia> detalles = new ArrayList<>();

    // IdentityHashMap: las entidades JPA suelen sobrescribir equals/hashCode por id (null aquí)
    // Cantidad pendiente de la orden por cada línea (solo líneas que vienen de una OC)
    private final Map<DetalleRecepcionMercancia, BigDecimal> pendientePorDetalle = new IdentityHashMap<>();
    // % de ITBIS de cada línea (evita recalcularlo desde montos redondeados)
    private final Map<DetalleRecepcionMercancia, BigDecimal> itbisPorcPorDetalle = new IdentityHashMap<>();

    // Encabezado
    private final TextField txtNumero = new TextField("N° Recepción");
    private final ComboBox<TipoDocumento> cbTipoDocumento = new ComboBox<>("Tipo Origen");
    private final ComboBox<OrdenDeCompra> cbOrdenCompra = new ComboBox<>("Orden de Compra");
    private final Button btnOrdenesPendientes = new Button("Ver pendientes", VaadinIcon.SEARCH.create());
    private final HorizontalLayout layoutOrden = new HorizontalLayout();
    private final TextField txtDocOrigen = new TextField("N° Doc. Referencia / NCF");
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("Suplidor / Proveedor");
    private final ComboBox<Almacen> cbAlmacenDestino = new ComboBox<>("Almacén Destino");
    private final DatePicker dtFecha = new DatePicker("Fecha Recepción");
    private final TextArea txtComentario = new TextArea("Comentarios / Observaciones");

    // Formulario Agregar Detalle
    private HorizontalLayout layoutDetalle;
    private final ComboBox<Articulo> cbArticulo = new ComboBox<>("Artículo");
    private final ComboBox<ArticuloEmpaque> cbEmpaque = new ComboBox<>("Presentación / Empaque");
    private final TextField txtDescripcion = new TextField("Descripción");
    private final NumberField numCantidad = new NumberField("Cant. A Recibir");
    private final NumberField numPrecio = new NumberField("P. Unitario Empaque");
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
            AlmacenService almacenService,
            ArticuloEmpaqueService articuloEmpaqueService,
            TipoDocumentoService tipoDocumentoService,
            EstadoDocumentoService essDocumentoService,
            UnidadDeVentaService unidadDeVentaService) {

        this.recepcionService = recepcionService;
        this.ordenCompraService = ordenCompraService;
        this.proveedorService = proveedorService;
        this.articuloService = articuloService;
        this.almacenService = almacenService;
        this.articuloEmpaqueService = articuloEmpaqueService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.essDocumentoService = essDocumentoService;
        this.unidadDeVentaService = unidadDeVentaService;

        setSpacing(true);
        setPadding(true);

        add(new H2("Recepción e Ingreso de Mercancía"));

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
        FormLayout form = new FormLayout();

        cbTipoDocumento.setItems(this.tipoDocumentoService.getLista());
        cbTipoDocumento.setItemLabelGenerator(TipoDocumento::getNombre);

        cbTipoDocumento.addValueChangeListener(e -> {

            cambiarTipoOrigen(e.getValue() != null ? e.getValue().getCodigo() : 0);
        });

        cbOrdenCompra.setItemLabelGenerator(o -> o.getNumero() + " - "
                + (o.getProveedor() != null ? o.getProveedor().getNombre() : ""));

        cbOrdenCompra.setPlaceholder("Seleccione Orden...");
        cbOrdenCompra.setClearButtonVisible(true);
        cbOrdenCompra.addValueChangeListener(e -> {

            cargarDesdeOrdenCompra(e.getValue());

        });
        cbOrdenCompra.setWidthFull();

        btnOrdenesPendientes.addClickListener(e -> abrirDialogoOrdenesPendientes());

        layoutOrden.setPadding(false);
        layoutOrden.setSpacing(true);
        layoutOrden.setWidthFull();
        layoutOrden.setAlignItems(FlexComponent.Alignment.END);
        layoutOrden.add(cbOrdenCompra, btnOrdenesPendientes);
        layoutOrden.setFlexGrow(1, cbOrdenCompra);

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(Proveedor::getNombre);
        cbSuplidor.setRequired(true);

        cbAlmacenDestino.setItems(almacenService.getLista());
        cbAlmacenDestino.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenDestino.setRequired(true);
        // Si el almacén cambia (antes o después de cargar la OC), se aplica a todas las líneas
        cbAlmacenDestino.addValueChangeListener(e -> {
            for (DetalleRecepcionMercancia d : detalles) {
                d.setAlmacen(e.getValue());
            }
            gridDetalles.getDataProvider().refreshAll();
        });

        dtFecha.setValue(LocalDate.now());
        txtComentario.setMinHeight("70px");

        form.add(txtNumero, cbTipoDocumento, layoutOrden, txtDocOrigen, cbSuplidor, cbAlmacenDestino, dtFecha);
        form.add(txtComentario);
        form.setColspan(txtComentario, 3);

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        add(form);
    }

    private void cambiarTipoOrigen(Integer tipo) {

        if (ORIGEN_ORDEN_COMPRA.equals(tipo)) {

            layoutOrden.setVisible(true);
            cargarOrdenesPendientes();
        } else {
            cbOrdenCompra.clear(); // dispara cargarDesdeOrdenCompra(null) que limpia las líneas de la OC
            layoutOrden.setVisible(false);
            cbSuplidor.setReadOnly(false);
            layoutDetalle.setVisible(true);
        }
    }

    /**
     * Órdenes (vía OrdenDeCompraService) que todavía tienen mercancía por
     * recibir.
     */
    private List<OrdenDeCompra> obtenerOrdenesPendientes() {

        return ordenCompraService.getListaOcPendiente().stream()
                .filter(o -> !"COMPLETADA".equalsIgnoreCase(o.getEstado())
                && (o.getAnulada() == null || !o.getAnulada())
                && tienePendiente(o))
                .toList();
    }

    private void cargarOrdenesPendientes() {

        cbOrdenCompra.setItems(obtenerOrdenesPendientes());
    }

    private BigDecimal pendienteDeLinea(DetalleOrdendeDeCompra d) {
        BigDecimal p = d.getPendiente() != null ? d.getPendiente() : d.getCantidad();
        return p != null ? p : BigDecimal.ZERO;
    }

    private boolean tienePendiente(OrdenDeCompra o) {
        if (o.getDetalleOrdendeDeCompraCollection() == null) {
            return true; // no se puede determinar: se deja visible
        }
        return o.getDetalleOrdendeDeCompraCollection().stream()
                .anyMatch(d -> pendienteDeLinea(d).compareTo(BigDecimal.ZERO) > 0);
    }

    private long contarLineasPendientes(OrdenDeCompra o) {
        if (o.getDetalleOrdendeDeCompraCollection() == null) {
            return 0;
        }
        return o.getDetalleOrdendeDeCompraCollection().stream()
                .filter(d -> pendienteDeLinea(d).compareTo(BigDecimal.ZERO) > 0)
                .count();
    }

    private BigDecimal sumarCantidadPendiente(OrdenDeCompra o) {
        if (o.getDetalleOrdendeDeCompraCollection() == null) {
            return BigDecimal.ZERO;
        }
        return o.getDetalleOrdendeDeCompraCollection().stream()
                .map(this::pendienteDeLinea)
                .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Diálogo con las órdenes de compra pendientes por recibir; al elegir una
     * se carga en la recepción.
     */
    private void abrirDialogoOrdenesPendientes() {
        List<OrdenDeCompra> pendientes = obtenerOrdenesPendientes();
        // El combo usa la misma lista para que la orden elegida en el diálogo exista en sus items
        cbOrdenCompra.setItems(pendientes);

        if (pendientes.isEmpty()) {
            Notification.show("No hay órdenes de compra pendientes por recibir.",
                    3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Órdenes de compra pendientes por recibir");
        dialog.setWidth("900px");

        TextField txtFiltro = new TextField();
        txtFiltro.setPlaceholder("Filtrar por número o proveedor...");
        txtFiltro.setPrefixComponent(VaadinIcon.SEARCH.create());
        txtFiltro.setClearButtonVisible(true);
        txtFiltro.setValueChangeMode(ValueChangeMode.LAZY);
        txtFiltro.setWidthFull();

        Grid<OrdenDeCompra> grid = new Grid<>(OrdenDeCompra.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.addColumn(OrdenDeCompra::getNumero).setHeader("N° Orden").setAutoWidth(true);
        grid.addColumn(o -> o.getProveedor() != null ? o.getProveedor().getNombre() : "-")
                .setHeader("Proveedor").setFlexGrow(2);
        grid.addColumn(OrdenDeCompra::getEstado).setHeader("Estado").setAutoWidth(true);
        grid.addColumn(this::contarLineasPendientes).setHeader("Líneas pend.").setAutoWidth(true);
        grid.addColumn(o -> sumarCantidadPendiente(o).stripTrailingZeros().toPlainString())
                .setHeader("Cant. pendiente").setAutoWidth(true);
        grid.addComponentColumn(o -> {
            Button btn = new Button("Recibir", VaadinIcon.DOWNLOAD.create(),
                    e -> seleccionarOrdenPendiente(o, dialog));
            btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            return btn;
        }).setHeader("Acción").setAutoWidth(true);

        grid.setItems(pendientes);
        grid.setHeight("380px");
        grid.addItemDoubleClickListener(e -> seleccionarOrdenPendiente(e.getItem(), dialog));

        txtFiltro.addValueChangeListener(e -> {
            String f = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            grid.getListDataView().setFilter(o
                    -> f.isEmpty()
                    || (o.getNumero() != null && o.getNumero().toLowerCase().contains(f))
                    || (o.getProveedor() != null && o.getProveedor().getNombre() != null
                    && o.getProveedor().getNombre().toLowerCase().contains(f)));
        });

        VerticalLayout contenido = new VerticalLayout(txtFiltro, grid);
        contenido.setPadding(false);
        contenido.setSpacing(true);
        dialog.add(contenido);

        Button btnCerrar = new Button("Cerrar", e -> dialog.close());
        dialog.getFooter().add(btnCerrar);

        dialog.open();
    }

    private void seleccionarOrdenPendiente(OrdenDeCompra oc, Dialog dialog) {
        dialog.close();
        cbOrdenCompra.setValue(oc); // dispara cargarDesdeOrdenCompra(oc)
    }

    // =====================================================================
    // RECIBIR DESDE ORDEN DE COMPRA
    // =====================================================================
    private void cargarDesdeOrdenCompra(OrdenDeCompra oc) {

        if (oc == null) {
            // Se quitó la orden: liberar proveedor y limpiar solo si las líneas venían de una OC
            if (!pendientePorDetalle.isEmpty()) {
                detalles.clear();
                pendientePorDetalle.clear();
                itbisPorcPorDetalle.clear();
                actualizarGridYTotales();
            }
            cbSuplidor.setReadOnly(false);
            layoutDetalle.setVisible(true);
            return;
        }

        // Encabezado en base a la orden
        cbSuplidor.setReadOnly(false);
        cbSuplidor.setValue(oc.getProveedor());
        cbSuplidor.setReadOnly(true);
        txtDocOrigen.setValue(oc.getNumero() != null ? oc.getNumero() : "");

        // Líneas en base al detalle pendiente de la orden
        detalles.clear();
        pendientePorDetalle.clear();
        itbisPorcPorDetalle.clear();

        int sinEmpaque = 0;

        if (oc.getDetalleOrdendeDeCompraCollection() != null) {

            for (DetalleOrdendeDeCompra doc : oc.getDetalleOrdendeDeCompraCollection()) {

                BigDecimal pendiente = doc.getPendiente() != null ? doc.getPendiente() : doc.getCantidad();
                if (pendiente == null || pendiente.compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // línea ya recibida completa
                }

                BigDecimal precio = doc.getPrecioUnitario() != null ? doc.getPrecioUnitario() : BigDecimal.ZERO;

                DetalleRecepcionMercancia det = new DetalleRecepcionMercancia();

                det.setCodigo(doc.getArticulo().getCodigo());
                det.setArticulo(doc.getArticulo());
                det.setAlmacen(cbAlmacenDestino.getValue()); // se actualiza si luego eligen otro almacén
                det.setDescripcionArticulo(doc.getDescripcionArticulo());
                det.setCantidadRecibida(pendiente);       // sugerido: todo lo pendiente
                det.setPrecioUnitario(precio);
                // det.setDetalleOrdenDeCompra(doc);      // <-- recomendado: vínculo con la línea de la OC

                // Empaque base por defecto (preferiblemente uno habilitado para compras)
                ArticuloEmpaque emp = doc.getArticuloEmpaque();
//                ArticuloEmpaque emp = buscarEmpaqueBase(doc.getArticulo());

                BigDecimal factor = BigDecimal.ONE;

                if (emp != null) {

                    det.setArticuloEmpaque(emp);

                    if (emp.getFactorConversion() != null) {
                        factor = emp.getFactorConversion();
                    }
                    if (emp.getUnidadEmpaque() != null) {

                        det.setUnidad(emp.getUnidadEmpaque());
                        det.setNombreUnidad(emp.getUnidadEmpaque().getDescripcion());
                    }
                } else {
                    sinEmpaque++;
                }
                det.setFactorConversion(factor);
                det.setCantidadFisicaBase(pendiente.multiply(factor));

                // ITBIS: se conserva el % que tenía la línea en la orden
                BigDecimal porc = BigDecimal.ZERO;
                if (doc.getItbis() != null && doc.getSubTotal() != null
                        && doc.getSubTotal().compareTo(BigDecimal.ZERO) > 0) {
                    porc = doc.getItbis().multiply(BigDecimal.valueOf(100))
                            .divide(doc.getSubTotal(), 4, RoundingMode.HALF_UP);
                }

                BigDecimal sub = pendiente.multiply(precio).setScale(2, RoundingMode.HALF_UP);
                BigDecimal itbis = sub.multiply(porc).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                det.setSubTotal(sub);
                det.setItbis(itbis);
                det.setTotal(sub.add(itbis));

                detalles.add(det);
                pendientePorDetalle.put(det, pendiente);
                itbisPorcPorDetalle.put(det, porc);
            }
        }

        // Con una OC seleccionada solo se recibe lo que la orden contiene
        layoutDetalle.setVisible(false);
        actualizarGridYTotales();

        if (detalles.isEmpty()) {
            Notification.show("La orden seleccionada no tiene artículos pendientes por recibir.",
                    3500, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
        } else if (sinEmpaque > 0) {
            Notification.show(sinEmpaque + " artículo(s) no tienen empaque configurado; se asumió factor 1.",
                    4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private ArticuloEmpaque buscarEmpaqueBase(Articulo articulo) {
        List<ArticuloEmpaque> empaques = articuloEmpaqueService.getPorArticulo(articulo.getCodigo());
        if (empaques == null || empaques.isEmpty()) {
            return null;
        }
        return empaques.stream()
                .filter(e -> Boolean.TRUE.equals(e.getEsEmpaqueBase()) && Boolean.TRUE.equals(e.getSeCompraEn()))
                .findFirst()
                .or(() -> empaques.stream().filter(e -> Boolean.TRUE.equals(e.getEsEmpaqueBase())).findFirst())
                .orElse(empaques.get(0));
    }

    // =====================================================================
    // AGREGAR DETALLE MANUAL (cuando el origen no es una orden de compra)
    // =====================================================================
    private void crearSeccionAgregarDetalle() {
        layoutDetalle = new HorizontalLayout();
        layoutDetalle.setWidthFull();
        layoutDetalle.setAlignItems(FlexComponent.Alignment.BASELINE);

        cbArticulo.setItems(articuloService.getLista());
        cbArticulo.setItemLabelGenerator(a -> a.getCodigo() + " - " + a.getDescripcion());
        cbArticulo.setPlaceholder("Buscar artículo...");
        cbArticulo.setWidth("20%");

        cbEmpaque.setItemLabelGenerator(e -> {
            String unidad = e.getUnidadEmpaque() != null ? e.getUnidadEmpaque().getDescripcion() : "Sin unidad";
            BigDecimal f = e.getFactorConversion() != null ? e.getFactorConversion() : BigDecimal.ONE;
            return unidad + " (x" + f.stripTrailingZeros().toPlainString() + ")";
        });
        cbEmpaque.setPlaceholder("Seleccionar Empaque...");
        cbEmpaque.setWidth("18%");

        cbArticulo.addValueChangeListener(e -> {
            Articulo artSel = e.getValue();
            cbEmpaque.clear();
            cbEmpaque.setItems(List.of());

            if (artSel == null) {
                return;
            }

            txtDescripcion.setValue(artSel.getDescripcion());

            List<ArticuloEmpaque> todos = articuloEmpaqueService.getPorArticulo(artSel.getCodigo());
            if (todos == null || todos.isEmpty()) {
                Notification.show("El artículo " + artSel.getCodigo() + " no tiene empaques configurados.",
                        3500, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            // Preferir los habilitados para compra; si ninguno lo está, mostrar todos
            List<ArticuloEmpaque> empaquesCompra = todos.stream()
                    .filter(emp -> Boolean.TRUE.equals(emp.getSeCompraEn()))
                    .toList();
            List<ArticuloEmpaque> disponibles = empaquesCompra.isEmpty() ? todos : empaquesCompra;

            cbEmpaque.setItems(disponibles);

            disponibles.stream()
                    .filter(emp -> Boolean.TRUE.equals(emp.getEsEmpaqueBase()))
                    .findFirst()
                    .or(() -> disponibles.stream().findFirst())
                    .ifPresent(cbEmpaque::setValue);

            numCantidad.focus();
        });

        cbEmpaque.addValueChangeListener(e -> {
            ArticuloEmpaque empSel = e.getValue();
            if (empSel != null && empSel.getPrecioCompra() != null) {
                numPrecio.setValue(empSel.getPrecioCompra().doubleValue());
            } else if (cbArticulo.getValue() != null && cbArticulo.getValue().getPrecioCompra() != null) {
                numPrecio.setValue(cbArticulo.getValue().getPrecioCompra().doubleValue());
            }
        });

        txtDescripcion.setWidth("22%");
        numCantidad.setMin(0.01);
        numCantidad.setValue(1.0);
        numCantidad.setWidth("10%");
        numCantidad.addKeyPressListener(Key.ENTER, e -> agregarDetalleAGrid());

        numPrecio.setMin(0);
        numPrecio.setWidth("12%");
        numItbisPorc.setValue(18.0);
        numItbisPorc.setWidth("8%");

        btnAgregarDetalle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarDetalle.addClickListener(e -> agregarDetalleAGrid());

        layoutDetalle.add(cbArticulo, cbEmpaque, txtDescripcion, numCantidad, numPrecio, numItbisPorc, btnAgregarDetalle);
        add(layoutDetalle);
    }

    private void agregarDetalleAGrid() {
        try {
            if (cbAlmacenDestino.isEmpty()) {
                Notification.show("Seleccione un almacén destino antes de agregar artículos.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                cbAlmacenDestino.focus();
                return;
            }

            if (cbArticulo.isEmpty() || cbEmpaque.isEmpty() || numCantidad.getValue() == null || numPrecio.getValue() == null) {
                Notification.show("Seleccione artículo, empaque, cantidad y precio válidos.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Articulo artSel = cbArticulo.getValue();
            ArticuloEmpaque empSel = cbEmpaque.getValue();
            Almacen almacenSel = cbAlmacenDestino.getValue();
            String desc = txtDescripcion.getValue() != null ? txtDescripcion.getValue().trim() : artSel.getDescripcion();

            boolean yaExiste = detalles.stream().anyMatch(d
                    -> d.getArticulo() != null && d.getArticulo().getCodigo().equals(artSel.getCodigo())
                    && d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getCodigo().equals(empSel.getCodigo())
            );

            if (yaExiste) {
                Notification.show("Esta presentación del artículo ya fue agregada.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            BigDecimal cantRecibida = BigDecimal.valueOf(numCantidad.getValue());
            BigDecimal factor = empSel.getFactorConversion() != null ? empSel.getFactorConversion() : BigDecimal.ONE;
            BigDecimal cantFisicaBase = cantRecibida.multiply(factor);

            BigDecimal precEmpaque = BigDecimal.valueOf(numPrecio.getValue());
            BigDecimal porcItbis = BigDecimal.valueOf(numItbisPorc.getValue() != null ? numItbisPorc.getValue() : 0.0);

            BigDecimal sub = cantRecibida.multiply(precEmpaque).setScale(2, RoundingMode.HALF_UP);
            BigDecimal itbis = sub.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            DetalleRecepcionMercancia det = new DetalleRecepcionMercancia();
            det.setCodigo(artSel.getCodigo());
            det.setArticulo(artSel);
            det.setArticuloEmpaque(empSel);
            det.setAlmacen(almacenSel);
            det.setFactorConversion(factor);
            det.setCantidadFisicaBase(cantFisicaBase);

            if (empSel.getUnidadEmpaque() != null) {
                det.setUnidad(empSel.getUnidadEmpaque());
                det.setNombreUnidad(empSel.getUnidadEmpaque().getDescripcion());
            }

            det.setDescripcionArticulo(desc);
            det.setCantidadRecibida(cantRecibida);
            det.setPrecioUnitario(precEmpaque);
            det.setSubTotal(sub);
            det.setItbis(itbis);
            det.setTotal(sub.add(itbis));

            detalles.add(det);
            itbisPorcPorDetalle.put(det, porcItbis);
            actualizarGridYTotales();
            limpiarFormularioDetalle();

        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error al agregar el detalle: " + e.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // =====================================================================
    // GRID
    // =====================================================================
    private void configurarGridDetalles() {
        gridDetalles.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        gridDetalles.addColumn(d -> d.getArticulo() != null ? d.getArticulo().getCodigo() : "-").setHeader("Código").setAutoWidth(true);
        gridDetalles.addColumn(DetalleRecepcionMercancia::getDescripcionArticulo).setHeader("Descripción").setFlexGrow(2);

        gridDetalles.addColumn(d -> d.getAlmacen() != null ? d.getAlmacen().getNombre() : "-").setHeader("Almacén").setAutoWidth(true);
        gridDetalles.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion() : "-").setHeader("Presentación").setAutoWidth(true);

        // Cantidad pendiente según la orden (solo visible si viene de una OC)
        gridDetalles.addColumn(d -> {
            BigDecimal p = pendientePorDetalle.get(d);
            return p != null ? p.stripTrailingZeros().toPlainString() : "-";
        }).setHeader("Pendiente OC").setAutoWidth(true);

        // Edición de Cantidad Recibida (no puede superar lo pendiente de la orden)
        gridDetalles.addComponentColumn(d -> {
            NumberField fieldCant = new NumberField();
            fieldCant.setValue(d.getCantidadRecibida() != null ? d.getCantidadRecibida().doubleValue() : 1.0);
            fieldCant.setMin(0.01);
            fieldCant.setWidth("90px");
            fieldCant.addValueChangeListener(e -> {
                if (!e.isFromClient() || e.getValue() == null || e.getValue() <= 0) {
                    return;
                }
                BigDecimal nueva = BigDecimal.valueOf(e.getValue());
                BigDecimal max = pendientePorDetalle.get(d);
                if (max != null && nueva.compareTo(max) > 0) {
                    Notification.show("No puede recibir más de lo pendiente en la orden (" + max.stripTrailingZeros().toPlainString() + ").",
                            3500, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    fieldCant.setValue(d.getCantidadRecibida().doubleValue());
                    return;
                }
                recalcularFila(d, nueva, d.getPrecioUnitario());
            });
            return fieldCant;
        }).setHeader("Cant. Recibida").setAutoWidth(true);

        gridDetalles.addColumn(d -> d.getCantidadFisicaBase() + " (Base)").setHeader("Stock Físico").setAutoWidth(true);

        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getPrecioUnitario())).setHeader("P. Unitario").setAutoWidth(true);
        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getItbis())).setHeader("ITBIS").setAutoWidth(true);
        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getSubTotal())).setHeader("SubTotal").setAutoWidth(true);
        gridDetalles.addColumn(d -> String.format("RD$ %,.2f", d.getTotal())).setHeader("Total").setAutoWidth(true);

        gridDetalles.addComponentColumn(d -> {
            Button btnEliminar = new Button(VaadinIcon.TRASH.create(), e -> {
                detalles.remove(d);
                pendientePorDetalle.remove(d);
                itbisPorcPorDetalle.remove(d);
                actualizarGridYTotales();
            });
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return btnEliminar;
        }).setHeader("Acciones").setAutoWidth(true);

        gridDetalles.setItems(detalles);
        gridDetalles.setHeight("280px");

        add(gridDetalles);
    }

    private void recalcularFila(DetalleRecepcionMercancia det, BigDecimal cantRecibida, BigDecimal prec) {
        BigDecimal factor = det.getFactorConversion() != null ? det.getFactorConversion() : BigDecimal.ONE;
        BigDecimal porcItbis = itbisPorcPorDetalle.getOrDefault(det, BigDecimal.ZERO);

        BigDecimal sub = cantRecibida.multiply(prec).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = sub.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        det.setCantidadRecibida(cantRecibida);
        det.setCantidadFisicaBase(cantRecibida.multiply(factor));
        det.setPrecioUnitario(prec);
        det.setSubTotal(sub);
        det.setItbis(itbis);
        det.setTotal(sub.add(itbis));

        // Solo se refresca la fila editada (refreshAll haría perder el foco al editar)
        gridDetalles.getDataProvider().refreshItem(det);
        actualizarTotales();
    }

    private void actualizarGridYTotales() {
        gridDetalles.getDataProvider().refreshAll();
        actualizarTotales();
    }

    private void actualizarTotales() {
        BigDecimal subG = detalles.stream().map(DetalleRecepcionMercancia::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal itbisG = detalles.stream().map(DetalleRecepcionMercancia::getItbis).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalG = subG.add(itbisG);

        lblSubTotal.setText(String.format("RD$ %,.2f", subG));
        lblItbis.setText(String.format("RD$ %,.2f", itbisG));
        lblTotal.setText(String.format("RD$ %,.2f", totalG));
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
        layoutTotales.addFormItem(lblTotal, "Total Entrada:");

        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardar.addClickListener(e ->{
        
             procesarRecepcion();
        });

        btnCancelar.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("ordenes-compra")));

        HorizontalLayout layoutBotones = new HorizontalLayout(btnGuardar, btnCancelar);

        layoutInferior.add(layoutBotones, layoutTotales);
        add(layoutInferior);
    }

    private void cargarDatosIniciales() {
        txtNumero.setValue("REC-" + System.currentTimeMillis() / 1000);
        txtNumero.setReadOnly(true);

        // Seleccionar "Orden de compra" como origen por defecto (dispara cambiarTipoOrigen)
        cbTipoDocumento.getListDataView().getItems()
                .filter(t -> t.getCodigo() == 8)
                .findFirst()
                .ifPresentOrElse(cbTipoDocumento::setValue, () -> {

                    cambiarTipoOrigen(8);

                });
    }

    // =====================================================================
    // PROCESAR
    // =====================================================================
    private void procesarRecepcion() {

        if (cbAlmacenDestino.isEmpty()) {
            Notification.show("Seleccione el Almacén Destino.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (cbSuplidor.isEmpty()) {
            Notification.show("Seleccione un Suplidor.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (txtComentario.getValue().isEmpty()) {

            Notification.show("Tiene que digitar un comentario ", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (detalles.isEmpty()) {
            Notification.show("Agregue al menos un artículo recibido.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        // Validación final contra lo pendiente de la orden
        for (DetalleRecepcionMercancia d : detalles) {

            BigDecimal max = pendientePorDetalle.get(d);

            if (max != null && d.getCantidadRecibida().compareTo(max) > 0) {

                Notification.show("La cantidad de '" + d.getDescripcionArticulo() + "' supera lo pendiente en la orden.",
                        3500, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        try {

            OrdenDeCompra oc = cbOrdenCompra.getValue();

            RecepcionMercancia rec = new RecepcionMercancia();

            rec.setNumeroDocumento(txtNumero.getValue());
            rec.setFecha(Date.from(dtFecha.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            rec.setTipoDocumento(cbTipoDocumento.getValue());
            rec.setNombreDocumento(cbTipoDocumento.getValue() != null ? cbTipoDocumento.getValue().getNombre() : "");
            rec.setEstadoDocumento(this.essDocumentoService.getEstado(2));
            rec.setNombreEstado(this.essDocumentoService.getEstado(2).getNombre());

            rec.setAlmacen(cbAlmacenDestino.getValue());
            rec.setProveedor(cbSuplidor.getValue());
            rec.setNombreUsuario("Admin");
            rec.setUsuario(new Usuario(1));
            rec.setNombreProveedor(cbSuplidor.getValue().getNombre());
            rec.setComentario(txtComentario.getValue());

            // Referencia al documento origen SIN pisar el número ni el tipo de la recepción.
            // AJUSTA estos setters a los campos reales de tu entidad RecepcionMercancia:
            if (oc != null) {

                rec.setOrdenCompra(oc);
                rec.setNumeroOc(oc.getCodigo().toString());

            } else {

                rec.setNumeroDocumento(txtDocOrigen.getValue());
            }

            BigDecimal subG = detalles.stream().map(DetalleRecepcionMercancia::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal itbisG = detalles.stream().map(DetalleRecepcionMercancia::getItbis).reduce(BigDecimal.ZERO, BigDecimal::add);

            rec.setSubtotal(subG);
            rec.setTotalItbis(itbisG);
            rec.setTotal(subG.add(itbisG));

            rec.setDetalleRecepcionMercanciaCollection(detalles);

            // Transaccional: inventario + actualización de pendiente/estado de la OC (ver servicio)
            recepcionService.procesarRecepcion(rec);

            ClaseUtil.mostrarNotificacion(" Recepción procesada exitosamente.", NotificationVariant.LUMO_PRIMARY);
      
            limpiarFormularioCompleto();

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al procesar la recepción: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarFormularioCompleto() {
        detalles.clear();
        pendientePorDetalle.clear();
        itbisPorcPorDetalle.clear();
        actualizarGridYTotales();

        txtNumero.setValue("REC-" + System.currentTimeMillis() / 1000);
        cbOrdenCompra.clear();
        if (layoutOrden.isVisible()) {
            cargarOrdenesPendientes(); // refrescar: la OC recibida puede haber quedado completa
        }
        cbAlmacenDestino.clear();
        txtDocOrigen.clear();
        cbSuplidor.clear();
        cbSuplidor.setReadOnly(false);
        dtFecha.setValue(LocalDate.now());
        txtComentario.clear();

        layoutDetalle.setVisible(!layoutOrden.isVisible());
        limpiarFormularioDetalle();
        cbTipoDocumento.focus();
    }

    private void limpiarFormularioDetalle() {
        cbArticulo.clear();
        cbEmpaque.clear();
        txtDescripcion.clear();
        numCantidad.setValue(1.0);
        numPrecio.clear();
    }
}
