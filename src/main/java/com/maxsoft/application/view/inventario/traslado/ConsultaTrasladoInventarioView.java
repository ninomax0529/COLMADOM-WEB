//
//
//package com.maxsoft.application.view.inventario.traslado;
//
//import com.maxsoft.application.modelo.Almacen;
//import com.maxsoft.application.modelo.DetalleTrasladoInventario;
//import com.maxsoft.application.modelo.TrasladoInventario;
//import com.maxsoft.application.repo.AlmacenRepo;
//import com.maxsoft.application.servicio.interfaces.inventario.TrasladoInventarioService;
//import com.vaadin.flow.component.Key;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.datepicker.DatePicker;
//import com.vaadin.flow.component.formlayout.FormLayout;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.grid.GridVariant;
//import com.vaadin.flow.component.grid.dataview.GridListDataView;
//import com.vaadin.flow.component.html.H2;
//import com.vaadin.flow.component.html.H4;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.icon.Icon;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.data.value.ValueChangeMode;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import jakarta.annotation.security.PermitAll;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.math.BigDecimal;
//import java.text.DecimalFormat;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//@PermitAll
//@PageTitle("Consulta de Traslados | MaxSoft ERP")
//@Route(value = "inventario/consultaTraslado")
//public class ConsultaTrasladoInventarioView extends VerticalLayout {
//
//    private static final DateTimeFormatter FORMATO_FECHA
//            = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//    private static final DecimalFormat FORMATO_NUMERO
//            = new DecimalFormat("#,##0.00");
//
//    // Servicios / Repositorios
//    private final TrasladoInventarioService trasladoService;
//    private final AlmacenRepo almacenRepo;
//
//    // Filtros
//    private DatePicker dpDesde;
//    private DatePicker dpHasta;
//    private ComboBox<Almacen> cbAlmacenOrigen;
//    private ComboBox<Almacen> cbAlmacenDestino;
//    private TextField txtBuscar;
//    private Button btnBuscar;
//    private Button btnLimpiar;
//
//    // Resumen
//    private final Span lblResumen = new Span();
//    private final H4 lblTituloDetalle = new H4("Detalle del traslado");
//
//    // Grids
//    private Grid<TrasladoInventario> gridTraslados;
//    private Grid<DetalleTrasladoInventario> gridDetalle;
//    private GridListDataView<TrasladoInventario> dataViewTraslados;
//    private final List<TrasladoInventario> listaTraslados = new ArrayList<>();
//    private final List<DetalleTrasladoInventario> listaDetalle = new ArrayList<>();
//
//    @Autowired
//    public ConsultaTrasladoInventarioView(TrasladoInventarioService trasladoService,
//            AlmacenRepo almacenRepo) {
//
//        this.trasladoService = trasladoService;
//        this.almacenRepo = almacenRepo;
//
//        setSizeFull();
//        setPadding(true);
//        setSpacing(true);
//
//        add(new H2("Consulta de Traslados de Inventario"));
//        add(crearFiltros());
//        add(lblResumen);
//        add(crearGridTraslados());
//        add(lblTituloDetalle);
//        add(crearGridDetalle());
//
//        cargarAlmacenes();
//        buscar();
//    }
//
//    // ============================================================
//    // FILTROS
//    // ============================================================
//    private VerticalLayout crearFiltros() {
//
//        dpDesde = new DatePicker("Desde");
//        dpDesde.setValue(LocalDate.now().minusDays(30));
//
//        dpHasta = new DatePicker("Hasta");
//        dpHasta.setValue(LocalDate.now());
//
//        cbAlmacenOrigen = new ComboBox<>("Almacén Origen");
//        cbAlmacenOrigen.setItemLabelGenerator(Almacen::getNombre);
//        cbAlmacenOrigen.setClearButtonVisible(true);
//
//        cbAlmacenDestino = new ComboBox<>("Almacén Destino");
//        cbAlmacenDestino.setItemLabelGenerator(Almacen::getNombre);
//        cbAlmacenDestino.setClearButtonVisible(true);
//
//        txtBuscar = new TextField("N° documento / Observación");
//        txtBuscar.setPlaceholder("Ej. TR-17...");
//        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
//        txtBuscar.setClearButtonVisible(true);
//        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
//        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());
//
//        FormLayout form = new FormLayout();
//        form.add(dpDesde, dpHasta, cbAlmacenOrigen, cbAlmacenDestino, txtBuscar);
//        form.setResponsiveSteps(
//                new FormLayout.ResponsiveStep("0", 1),
//                new FormLayout.ResponsiveStep("600px", 5)
//        );
//
//        btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
//        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        btnBuscar.addClickShortcut(Key.ENTER);
//
//        btnLimpiar = new Button("Limpiar filtros", VaadinIcon.REFRESH.create(), e -> limpiarFiltros());
//
//        HorizontalLayout acciones = new HorizontalLayout(btnBuscar, btnLimpiar);
//
//        VerticalLayout contenedor = new VerticalLayout(form, acciones);
//        contenedor.setPadding(false);
//        contenedor.setSpacing(true);
//        return contenedor;
//    }
//
//    private void cargarAlmacenes() {
//        List<Almacen> almacenes = almacenRepo.findAll();
//        cbAlmacenOrigen.setItems(almacenes);
//        cbAlmacenDestino.setItems(almacenes);
//    }
//
//    private void limpiarFiltros() {
//        dpDesde.setValue(LocalDate.now().minusDays(30));
//        dpHasta.setValue(LocalDate.now());
//        cbAlmacenOrigen.clear();
//        cbAlmacenDestino.clear();
//        txtBuscar.clear();
//        buscar();
//    }
//
//    // ============================================================
//    // GRID PRINCIPAL (TRASLADOS)
//    // ============================================================
//    private Grid<TrasladoInventario> crearGridTraslados() {
//
//        gridTraslados = new Grid<>(TrasladoInventario.class, false);
//        gridTraslados.setWidthFull();
//        gridTraslados.setHeight("320px");
//        gridTraslados.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
//        gridTraslados.setSelectionMode(Grid.SelectionMode.SINGLE);
//
//        dataViewTraslados = gridTraslados.setItems(listaTraslados);
//
//        gridTraslados.addColumn(TrasladoInventario::getNumeroDocumento)
//                .setHeader("N° Documento")
//                .setAutoWidth(true);
//
//        gridTraslados.addColumn(t -> {
//            LocalDate f = aLocalDate(t.getFechaEmision());
//            return f != null ? f.format(FORMATO_FECHA) : "";
//        }).setHeader("Fecha").setAutoWidth(true);
//
//        gridTraslados.addColumn(t -> t.getAlmacenOrigen() != null ? t.getAlmacenOrigen().getNombre() : "")
//                .setHeader("Origen")
//                .setAutoWidth(true);
//
//        gridTraslados.addColumn(t -> t.getAlmacenDestino() != null ? t.getAlmacenDestino().getNombre() : "")
//                .setHeader("Destino")
//                .setAutoWidth(true);
//
//        gridTraslados.addColumn(this::contarItems)
//                .setHeader("Ítems")
//                .setAutoWidth(true);
//
//        gridTraslados.addColumn(TrasladoInventario::getObservacion)
//                .setHeader("Observación")
//                .setFlexGrow(1);
//
//        gridTraslados.addSelectionListener(e
//                -> cargarDetalle(e.getFirstSelectedItem().orElse(null)));
//
//        return gridTraslados;
//    }
//
//    // ============================================================
//    // GRID DETALLE
//    // ============================================================
//    private Grid<DetalleTrasladoInventario> crearGridDetalle() {
//
//        gridDetalle = new Grid<>(DetalleTrasladoInventario.class, false);
//        gridDetalle.setWidthFull();
//        gridDetalle.setHeight("250px");
//        gridDetalle.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
//        gridDetalle.setItems(listaDetalle);
//
//        gridDetalle.addColumn(DetalleTrasladoInventario::getDescripcionArticulo)
//                .setHeader("Artículo")
//                .setAutoWidth(true)
//                .setFlexGrow(1);
//
//        gridDetalle.addColumn(DetalleTrasladoInventario::getNombreUnidad)
//                .setHeader("Empaque / Presentación")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(d.getCantidadEnviada()))
//                .setHeader("Cantidad")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(factorDe(d)))
//                .setHeader("Factor")
//                .setAutoWidth(true);
//
//        // Equivalente en unidad base: cantidad x factor (calculado aquí)
//        gridDetalle.addColumn(d -> numero(cantidadBase(d)))
//                .setHeader("Equiv. (UMM)")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(d.getCostoUnitario()))
//                .setHeader("Costo Unit.")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(totalLinea(d)))
//                .setHeader("Total")
//                .setAutoWidth(true);
//
//        return gridDetalle;
//    }
//
//    // ============================================================
//    // CONSULTA
//    // ============================================================
//    private void buscar() {
//
//        if (dpDesde.getValue() != null && dpHasta.getValue() != null
//                && dpDesde.getValue().isAfter(dpHasta.getValue())) {
//            mostrarNotificacion("La fecha 'Desde' no puede ser mayor que 'Hasta'.", NotificationVariant.LUMO_WARNING);
//            return;
//        }
//
//        try {
//            Almacen origen = cbAlmacenOrigen.getValue();
//            Almacen destino = cbAlmacenDestino.getValue();
//
//            List<TrasladoInventario> resultado = trasladoService.getLista()
//                    .stream()
//                    .filter(t -> dentroDeRango(aLocalDate(t.getFechaEmision())))
//                    .filter(t -> origen == null || (t.getAlmacenOrigen() != null
//                    && Objects.equals(t.getAlmacenOrigen().getCodigo(), origen.getCodigo())))
//                    .filter(t -> destino == null || (t.getAlmacenDestino() != null
//                    && Objects.equals(t.getAlmacenDestino().getCodigo(), destino.getCodigo())))
//                    .sorted(Comparator.comparing(
//                            (TrasladoInventario t) -> aLocalDate(t.getFechaEmision()),
//                            Comparator.nullsLast(Comparator.reverseOrder())))
//                    .collect(Collectors.toList());
//
//            listaTraslados.clear();
//            listaTraslados.addAll(resultado);
//            gridTraslados.getDataProvider().refreshAll();
//
//            aplicarFiltroTexto();
//            cargarDetalle(null);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            mostrarNotificacion("Error consultando traslados: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
//        }
//    }
//
//    /**
//     * Filtro de texto en memoria sobre el resultado ya cargado.
//     */
//    private void aplicarFiltroTexto() {
//
//        if (dataViewTraslados == null) {
//            return;
//        }
//
//        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();
//
//        dataViewTraslados.setFilter(t -> {
//            if (term.isEmpty()) {
//                return true;
//            }
//            boolean matchDoc = t.getNumeroDocumento() != null
//                    && t.getNumeroDocumento().toLowerCase().contains(term);
//            boolean matchObs = t.getObservacion() != null
//                    && t.getObservacion().toLowerCase().contains(term);
//            return matchDoc || matchObs;
//        });
//
//        lblResumen.setText("Traslados encontrados: " + dataViewTraslados.getItemCount());
//    }
//
//    private void cargarDetalle(TrasladoInventario traslado) {
//
//        listaDetalle.clear();
//
//        if (traslado == null) {
//            lblTituloDetalle.setText("Detalle del traslado (seleccione un traslado)");
//            gridDetalle.getDataProvider().refreshAll();
//            return;
//        }
//
//        try {
//            Collection<DetalleTrasladoInventario> detalles = this.trasladoService.getDetalle(traslado.getCodigo());
//            if (detalles != null) {
//                listaDetalle.addAll(detalles);
//            }
//            lblTituloDetalle.setText("Detalle del traslado " + traslado.getNumeroDocumento());
//        } catch (RuntimeException ex) {
//            // Normalmente LazyInitializationException si el servicio no trae el detalle
//            ex.printStackTrace();
//            mostrarNotificacion("No se pudo cargar el detalle: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
//        }
//
//        gridDetalle.getDataProvider().refreshAll();
//    }
//
//    // ============================================================
//    // UTILIDADES
//    // ============================================================
//    private boolean dentroDeRango(LocalDate fecha) {
//        if (fecha == null) {
//            return dpDesde.getValue() == null && dpHasta.getValue() == null;
//        }
//        if (dpDesde.getValue() != null && fecha.isBefore(dpDesde.getValue())) {
//            return false;
//        }
//        return dpHasta.getValue() == null || !fecha.isAfter(dpHasta.getValue());
//    }
//
//    private LocalDate aLocalDate(Date fecha) {
//        if (fecha == null) {
//            return null;
//        }
//        // new Date(getTime()) evita problemas si llega un java.sql.Date
//        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//    }
//
//    private String contarItems(TrasladoInventario t) {
//        try {
//            Collection<DetalleTrasladoInventario> c = t.getDetalleTrasladoInventarioCollection();
//            return c != null ? String.valueOf(c.size()) : "0";
//        } catch (RuntimeException ex) {
//            return "-"; // detalle no inicializado (lazy)
//        }
//    }
//
//    private BigDecimal factorDe(DetalleTrasladoInventario d) {
//        BigDecimal f = d.getFactorConversion();
//        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
//    }
//
//    private BigDecimal cantidadBase(DetalleTrasladoInventario d) {
//        BigDecimal cant = d.getCantidadEnviada() != null ? d.getCantidadEnviada() : BigDecimal.ZERO;
//        return cant.multiply(factorDe(d));
//    }
//
//    private BigDecimal totalLinea(DetalleTrasladoInventario d) {
//        BigDecimal cant = d.getCantidadEnviada() != null ? d.getCantidadEnviada() : BigDecimal.ZERO;
//        BigDecimal costo = d.getCostoUnitario() != null ? d.getCostoUnitario() : BigDecimal.ZERO;
//        return cant.multiply(costo);
//    }
//
//    private String numero(BigDecimal valor) {
//        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
//    }
//
//    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
//        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
//        notif.addThemeVariants(variante);
//    }
//}

package com.maxsoft.application.view.inventario.traslado;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.TrasladoInventario;
import com.maxsoft.application.repo.AlmacenRepo;
import com.maxsoft.application.servicio.interfaces.inventario.TrasladoInventarioService;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@PermitAll
@PageTitle("Traslado de Inventario")
@Route(value = "inventario/consultaTraslado")
public class ConsultaTrasladoInventarioView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    // Servicios / Repositorios
    private final TrasladoInventarioService trasladoService;
    private final AlmacenRepo almacenRepo;

    private final Grid<TrasladoInventario> grid = new Grid<>(TrasladoInventario.class, false);
    private final Grid<DetalleTrasladoInventario> gridDetalle = new Grid<>(DetalleTrasladoInventario.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Filtros de consulta
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final ComboBox<Almacen> cbAlmacenOrigen = new ComboBox<>("Almacén Origen");
    private final ComboBox<Almacen> cbAlmacenDestino = new ComboBox<>("Almacén Destino");
    private final TextField txtBuscar = new TextField();
    private Button btnBuscar;
    private Button btnLimpiar;

    private final TabSheet tabs = new TabSheet();

    List<DetalleTrasladoInventario> listDet = new ArrayList<>();
    List<TrasladoInventario> listaTraslados = new ArrayList<>();

    private GridListDataView<TrasladoInventario> dataViewTraslados;

    // Columnas con pie (totales)
    private Grid.Column<TrasladoInventario> colDocumento;
    private Grid.Column<DetalleTrasladoInventario> colDescripcion;
    private Grid.Column<DetalleTrasladoInventario> colEquivalente;
    private Grid.Column<DetalleTrasladoInventario> colTotal;

    @Autowired
    public ConsultaTrasladoInventarioView(TrasladoInventarioService trasladoService,
            AlmacenRepo almacenRepo) {

        this.trasladoService = trasladoService;
        this.almacenRepo = almacenRepo;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();

        tabs.add("Traslado de Inventario", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(TrasladoInventarioView.class);

        });

        grid.addItemDoubleClickListener(c -> {

            if (c.getItem() == null) {
                return;
            }

            cargarDetalle(c.getItem());
            tabs.setSelectedIndex(1);

        });

        configurarFiltros();
        configurarGrid();
        configurarGridDetalle();

        add(botonera, crearBarraFiltros(), tabs);

        cargarAlmacenes();
        buscar();
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private void configurarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpDesde.setClearButtonVisible(true);

        dpHasta.setValue(LocalDate.now());
        dpHasta.setClearButtonVisible(true);

        cbAlmacenOrigen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenOrigen.setClearButtonVisible(true);

        cbAlmacenDestino.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenDestino.setClearButtonVisible(true);

        txtBuscar.setWidth("300px");
        txtBuscar.setPlaceholder("Filtrar por N° documento u observación...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());

        btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create(), e -> limpiarFiltros());
    }

    private HorizontalLayout crearBarraFiltros() {

        HorizontalLayout barra = new HorizontalLayout(
                dpDesde, dpHasta, cbAlmacenOrigen, cbAlmacenDestino, txtBuscar, btnBuscar, btnLimpiar);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        return barra;
    }

    private void cargarAlmacenes() {

        List<Almacen> almacenes = almacenRepo.findAll();
        cbAlmacenOrigen.setItems(almacenes);
        cbAlmacenDestino.setItems(almacenes);
    }

    private void limpiarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        cbAlmacenOrigen.clear();
        cbAlmacenDestino.clear();
        txtBuscar.clear();
        buscar();
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void buscar() {

        if (dpDesde.getValue() != null && dpHasta.getValue() != null
                && dpDesde.getValue().isAfter(dpHasta.getValue())) {
            mostrarNotificacion("La fecha 'Desde' no puede ser mayor que 'Hasta'.", NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            Almacen origen = cbAlmacenOrigen.getValue();
            Almacen destino = cbAlmacenDestino.getValue();

            List<TrasladoInventario> resultado = this.trasladoService.getLista()
                    .stream()
                    .filter(t -> dentroDeRango(aLocalDate(t.getFechaEmision())))
                    .filter(t -> origen == null || (t.getAlmacenOrigen() != null
                    && Objects.equals(t.getAlmacenOrigen().getCodigo(), origen.getCodigo())))
                    .filter(t -> destino == null || (t.getAlmacenDestino() != null
                    && Objects.equals(t.getAlmacenDestino().getCodigo(), destino.getCodigo())))
                    .sorted(Comparator.comparing(
                            (TrasladoInventario t) -> aLocalDate(t.getFechaEmision()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaTraslados.clear();
            listaTraslados.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando traslados: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewTraslados == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewTraslados.setFilter(t -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchDoc = t.getNumeroDocumento() != null
                    && t.getNumeroDocumento().toLowerCase().contains(term);
            boolean matchObs = t.getObservacion() != null
                    && t.getObservacion().toLowerCase().contains(term);
            return matchDoc || matchObs;
        });

        colDocumento.setFooter("TOTAL TRASLADOS: " + dataViewTraslados.getItemCount());
    }

    private void cargarDetalle(TrasladoInventario traslado) {

        Collection<DetalleTrasladoInventario> detalles = this.trasladoService.getDetalle(traslado.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).setLabel("Detalle( Traslado -> " + traslado.getNumeroDocumento() + " )");

        actualizarTotalesDetalle();
    }

    private void limpiarDetalle() {

        listDet = new ArrayList<>();
        gridDetalle.setItems(listDet);
        tabs.getTabAt(1).setLabel("Detalle");
        tabs.setSelectedIndex(0);

        actualizarTotalesDetalle();
    }

    private void actualizarTotalesDetalle() {

        BigDecimal totalBase = listDet.stream()
                .map(this::cantidadBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGeneral = listDet.stream()
                .map(this::totalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        colDescripcion.setFooter("TOTAL ARTÍCULOS: " + listDet.size());
        colEquivalente.setFooter(numero(totalBase));
        colTotal.setFooter(numero(totalGeneral));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        colDocumento = grid.addColumn(TrasladoInventario::getNumeroDocumento)
                .setHeader("N° Documento")
                .setFooter("TOTAL TRASLADOS:");

        grid.addColumn(t -> {
            LocalDate f = aLocalDate(t.getFechaEmision());
            return f != null ? f.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (TrasladoInventario t) -> t.getFechaEmision(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(t -> t.getAlmacenOrigen() != null ? t.getAlmacenOrigen().getNombre() : "")
                .setHeader("Origen")
                .setKey("origen");

        grid.addColumn(t -> t.getAlmacenDestino() != null ? t.getAlmacenDestino().getNombre() : "")
                .setHeader("Destino")
                .setKey("destino");

        grid.addColumn(TrasladoInventario::getObservacion)
                .setHeader("Observación")
                .setKey("observacion")
                .setFlexGrow(1);

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        dataViewTraslados = grid.setItems(listaTraslados);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

        colDescripcion = gridDetalle.addColumn(DetalleTrasladoInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(DetalleTrasladoInventario::getNombreUnidad)
                .setHeader("Empaque / Presentación")
                .setKey("unidad");

        gridDetalle.addColumn(d -> numero(d.getCantidadEnviada()))
                .setHeader("Cantidad")
                .setKey("cantidad")
                .setComparator(Comparator.comparing(
                        (DetalleTrasladoInventario d) -> d.getCantidadEnviada(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // MULTI-EMPAQUE: factor y equivalente en unidad base (calculado aquí)
        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setKey("factor")
                .setComparator(Comparator.comparing(this::factorDe));

        colEquivalente = gridDetalle.addColumn(d -> numero(cantidadBase(d)))
                .setHeader("Equiv. (UMM)")
                .setKey("equivalente")
                .setComparator(Comparator.comparing(this::cantidadBase));

        gridDetalle.addColumn(d -> numero(d.getExistenciaAnterior()))
                .setHeader("Existencia Origen (UMM)")
                .setKey("existencia");

        gridDetalle.addColumn(d -> numero(d.getCostoUnitario()))
                .setHeader("Costo Unit.")
                .setKey("costo");

        colTotal = gridDetalle.addColumn(d -> numero(totalLinea(d)))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(this::totalLinea));

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private boolean dentroDeRango(LocalDate fecha) {

        if (fecha == null) {
            return dpDesde.getValue() == null && dpHasta.getValue() == null;
        }
        if (dpDesde.getValue() != null && fecha.isBefore(dpDesde.getValue())) {
            return false;
        }
        return dpHasta.getValue() == null || !fecha.isAfter(dpHasta.getValue());
    }

    private LocalDate aLocalDate(Date fecha) {

        if (fecha == null) {
            return null;
        }
        // new Date(getTime()) evita problemas si llega un java.sql.Date
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private BigDecimal factorDe(DetalleTrasladoInventario d) {

        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleTrasladoInventario d) {

        BigDecimal cant = d.getCantidadEnviada() != null ? d.getCantidadEnviada() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    private BigDecimal totalLinea(DetalleTrasladoInventario d) {

        BigDecimal cant = d.getCantidadEnviada() != null ? d.getCantidadEnviada() : BigDecimal.ZERO;
        BigDecimal costo = d.getCostoUnitario() != null ? d.getCostoUnitario() : BigDecimal.ZERO;
        return cant.multiply(costo);
    }

    private String numero(BigDecimal valor) {

        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}