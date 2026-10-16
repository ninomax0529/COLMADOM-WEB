//package com.maxsoft.application.view.inventario.ajuste;
//
//import com.maxsoft.application.modelo.AjusteInventario;
//import com.maxsoft.application.modelo.Almacen;
//import com.maxsoft.application.modelo.DetalleAjusteInventario;
//import com.maxsoft.application.modelo.TipoAjuste;
//import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
//import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
//import com.maxsoft.application.servicio.interfaces.inventario.TipoAjusteService;
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
//@PageTitle("Consulta de Ajustes de Inventario | MaxSoft ERP")
//@Route(value = "inventario/consultaAjuste")
//public class ConsultaAjusteInventarioView extends VerticalLayout {
//
//    private static final DateTimeFormatter FORMATO_FECHA
//            = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//    private static final DecimalFormat FORMATO_NUMERO
//            = new DecimalFormat("#,##0.00");
//
//    private static final String ESTADO_TODOS = "Todos";
//    private static final String ESTADO_ACTIVOS = "Activos";
//    private static final String ESTADO_ANULADOS = "Anulados";
//
//    // Servicios
//    private final AjusteInventarioService ajusteService;
//    private final AlmacenService almacenService;
//    private final TipoAjusteService tipoAjusteService;
//
//    // Filtros
//    private DatePicker dpDesde;
//    private DatePicker dpHasta;
//    private ComboBox<Almacen> cbAlmacen;
//    private ComboBox<TipoAjuste> cbTipoAjuste;
//    private ComboBox<String> cbEstado;
//    private TextField txtBuscar;
//    private Button btnBuscar;
//    private Button btnLimpiar;
//
//    // Resumen
//    private final Span lblResumen = new Span();
//    private final H4 lblTituloDetalle = new H4("Detalle del ajuste");
//
//    // Grids
//    private Grid<AjusteInventario> gridAjustes;
//    private Grid<DetalleAjusteInventario> gridDetalle;
//    private GridListDataView<AjusteInventario> dataViewAjustes;
//    private final List<AjusteInventario> listaAjustes = new ArrayList<>();
//    private final List<DetalleAjusteInventario> listaDetalle = new ArrayList<>();
//
//    // Ajuste seleccionado (para mostrar el signo del movimiento en el detalle)
//    private AjusteInventario ajusteSeleccionado;
//
//    @Autowired
//    public ConsultaAjusteInventarioView(AjusteInventarioService ajusteService,
//            AlmacenService almacenService,
//            TipoAjusteService tipoAjusteService) {
//
//        this.ajusteService = ajusteService;
//        this.almacenService = almacenService;
//        this.tipoAjusteService = tipoAjusteService;
//
//        setSizeFull();
//        setPadding(true);
//        setSpacing(true);
//
//        add(new H2("Consulta de Ajustes de Inventario"));
//        add(crearFiltros());
//        add(lblResumen);
//        add(crearGridAjustes());
//        add(lblTituloDetalle);
//        add(crearGridDetalle());
//
//        cargarCombos();
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
//        cbAlmacen = new ComboBox<>("Almacén");
//        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
//        cbAlmacen.setClearButtonVisible(true);
//
//        cbTipoAjuste = new ComboBox<>("Tipo de Ajuste");
//        cbTipoAjuste.setItemLabelGenerator(TipoAjuste::getDescripcion);
//        cbTipoAjuste.setClearButtonVisible(true);
//
//        cbEstado = new ComboBox<>("Estado");
//        cbEstado.setItems(ESTADO_TODOS, ESTADO_ACTIVOS, ESTADO_ANULADOS);
//        cbEstado.setAllowCustomValue(false);
//        cbEstado.setValue(ESTADO_TODOS);
//
//        txtBuscar = new TextField("N° ajuste / Observación / Usuario");
//        txtBuscar.setPlaceholder("Filtrar resultados...");
//        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
//        txtBuscar.setClearButtonVisible(true);
//        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
//        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());
//
//        FormLayout form = new FormLayout();
//        form.add(dpDesde, dpHasta, cbAlmacen, cbTipoAjuste, cbEstado, txtBuscar);
//        form.setResponsiveSteps(
//                new FormLayout.ResponsiveStep("0", 1),
//                new FormLayout.ResponsiveStep("600px", 6)
//        );
//
//        btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
//        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        btnBuscar.addClickShortcut(Key.ENTER);
//
//        btnLimpiar = new Button("Limpiar filtros", VaadinIcon.REFRESH.create(), e -> limpiarFiltros());
//
//        VerticalLayout contenedor = new VerticalLayout(form, new HorizontalLayout(btnBuscar, btnLimpiar));
//        contenedor.setPadding(false);
//        contenedor.setSpacing(true);
//        return contenedor;
//    }
//
//    private void cargarCombos() {
//        cbAlmacen.setItems(almacenService.getLista());
//        cbTipoAjuste.setItems(tipoAjusteService.getLista());
//    }
//
//    private void limpiarFiltros() {
//        dpDesde.setValue(LocalDate.now().minusDays(30));
//        dpHasta.setValue(LocalDate.now());
//        cbAlmacen.clear();
//        cbTipoAjuste.clear();
//        cbEstado.setValue(ESTADO_TODOS);
//        txtBuscar.clear();
//        buscar();
//    }
//
//    // ============================================================
//    // GRID PRINCIPAL (AJUSTES)
//    // ============================================================
//    private Grid<AjusteInventario> crearGridAjustes() {
//
//        gridAjustes = new Grid<>(AjusteInventario.class, false);
//        gridAjustes.setWidthFull();
//        gridAjustes.setHeight("320px");
//        gridAjustes.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
//        gridAjustes.setSelectionMode(Grid.SelectionMode.SINGLE);
//
//        dataViewAjustes = gridAjustes.setItems(listaAjustes);
//
//        gridAjustes.addColumn(AjusteInventario::getCodigo)
//                .setHeader("N° Ajuste")
//                .setAutoWidth(true);
//
//        gridAjustes.addColumn(a -> {
//            LocalDate f = aLocalDate(a.getFecha());
//            return f != null ? f.format(FORMATO_FECHA) : "";
//        }).setHeader("Fecha").setAutoWidth(true);
//
//        gridAjustes.addColumn(AjusteInventario::getNombreAlmacen)
//                .setHeader("Almacén")
//                .setAutoWidth(true);
//
//        gridAjustes.addColumn(a -> a.getTipoAjuste() != null ? a.getTipoAjuste().getDescripcion() : "")
//                .setHeader("Tipo")
//                .setAutoWidth(true);
//
//        gridAjustes.addColumn(this::contarItems)
//                .setHeader("Ítems")
//                .setAutoWidth(true);
//
//        gridAjustes.addColumn(AjusteInventario::getNombreUsuario)
//                .setHeader("Usuario")
//                .setAutoWidth(true);
//
//        gridAjustes.addColumn(a -> estaAnulado(a) ? "ANULADO" : "Activo")
//                .setHeader("Estado")
//                .setAutoWidth(true);
//
//        gridAjustes.addColumn(AjusteInventario::getObservacion)
//                .setHeader("Observación")
//                .setFlexGrow(1);
//
//        gridAjustes.addSelectionListener(e
//                -> cargarDetalle(e.getFirstSelectedItem().orElse(null)));
//
//        return gridAjustes;
//    }
//
//    // ============================================================
//    // GRID DETALLE
//    // ============================================================
//    private Grid<DetalleAjusteInventario> crearGridDetalle() {
//
//        gridDetalle = new Grid<>(DetalleAjusteInventario.class, false);
//        gridDetalle.setWidthFull();
//        gridDetalle.setHeight("250px");
//        gridDetalle.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
//        gridDetalle.setItems(listaDetalle);
//
//        gridDetalle.addColumn(DetalleAjusteInventario::getDescripcionArticulo)
//                .setHeader("Artículo")
//                .setAutoWidth(true)
//                .setFlexGrow(1);
//
//        gridDetalle.addColumn(DetalleAjusteInventario::getNombreAlmacen)
//                .setHeader("Almacén")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(DetalleAjusteInventario::getNombreUnidad)
//                .setHeader("Empaque / Presentación")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(d.getExistencia()))
//                .setHeader("Existencia Previa (UMM)")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(d.getCantidad()))
//                .setHeader("Cantidad")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(factorDe(d)))
//                .setHeader("Factor")
//                .setAutoWidth(true);
//
//        // Movimiento en unidad base con signo según el tipo de ajuste
//        gridDetalle.addColumn(d -> (esIncremento(ajusteSeleccionado) ? "+" : "-") + numero(cantidadBase(d)))
//                .setHeader("Movimiento (UMM)")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(d.getNuevaExistencia()))
//                .setHeader("Nueva Existencia (UMM)")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(d.getCostoUnitario()))
//                .setHeader("Costo Unit.")
//                .setAutoWidth(true);
//
//        gridDetalle.addColumn(d -> numero(d.getTotal()))
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
//            Almacen almacen = cbAlmacen.getValue();
//            TipoAjuste tipo = cbTipoAjuste.getValue();
//            String estado = cbEstado.getValue() != null ? cbEstado.getValue() : ESTADO_TODOS;
//
//            List<AjusteInventario> resultado = ajusteService.getLista()
//                    .stream()
//                    .filter(a -> dentroDeRango(aLocalDate(a.getFecha())))
//                    .filter(a -> almacen == null || (a.getAlmacen() != null
//                    && Objects.equals(a.getAlmacen().getCodigo(), almacen.getCodigo())))
//                    .filter(a -> tipo == null || (a.getTipoAjuste() != null
//                    && Objects.equals(a.getTipoAjuste().getCodigo(), tipo.getCodigo())))
//                    .filter(a -> ESTADO_TODOS.equals(estado)
//                    || (ESTADO_ANULADOS.equals(estado) == estaAnulado(a)))
//                    .sorted(Comparator.comparing(
//                            (AjusteInventario a) -> aLocalDate(a.getFecha()),
//                            Comparator.nullsLast(Comparator.reverseOrder())))
//                    .collect(Collectors.toList());
//
//            listaAjustes.clear();
//            listaAjustes.addAll(resultado);
//            gridAjustes.getDataProvider().refreshAll();
//
//            aplicarFiltroTexto();
//            gridAjustes.deselectAll();
//            cargarDetalle(null);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            mostrarNotificacion("Error consultando ajustes: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
//        }
//    }
//
//    private void aplicarFiltroTexto() {
//
//        if (dataViewAjustes == null) {
//            return;
//        }
//
//        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();
//
//        dataViewAjustes.setFilter(a -> {
//            if (term.isEmpty()) {
//                return true;
//            }
//            boolean matchCod = a.getCodigo() != null && a.getCodigo().toString().contains(term);
//            boolean matchObs = a.getObservacion() != null && a.getObservacion().toLowerCase().contains(term);
//            boolean matchUsr = a.getNombreUsuario() != null && a.getNombreUsuario().toLowerCase().contains(term);
//            return matchCod || matchObs || matchUsr;
//        });
//
//        lblResumen.setText("Ajustes encontrados: " + dataViewAjustes.getItemCount());
//    }
//
//    private void cargarDetalle(AjusteInventario ajuste) {
//
//        ajusteSeleccionado = ajuste;
//        listaDetalle.clear();
//
//        if (ajuste == null) {
//            lblTituloDetalle.setText("Detalle del ajuste (seleccione un ajuste)");
//            gridDetalle.getDataProvider().refreshAll();
//            return;
//        }
//
//        try {
//            Collection<DetalleAjusteInventario> detalles = this.ajusteService.getDetalle(ajuste.getCodigo());
//            if (detalles != null) {
//                listaDetalle.addAll(detalles);
//            }
//            lblTituloDetalle.setText("Detalle del ajuste #" + ajuste.getCodigo()
//                    + (estaAnulado(ajuste) ? " (ANULADO)" : ""));
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
//    private boolean estaAnulado(AjusteInventario a) {
//        return Boolean.TRUE.equals(a.getAnulado());
//    }
//
//    private boolean esIncremento(AjusteInventario a) {
//        return a != null && a.getTipoAjuste() != null
//                && "INCREMENTO".equalsIgnoreCase(a.getTipoAjuste().getDescripcion());
//    }
//
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
//    private String contarItems(AjusteInventario a) {
//        try {
//            Collection<DetalleAjusteInventario> c = a.getDetalleAjusteInventarioCollection();
//            return c != null ? String.valueOf(c.size()) : "0";
//        } catch (RuntimeException ex) {
//            return "-"; // detalle no inicializado (lazy)
//        }
//    }
//
//    private BigDecimal factorDe(DetalleAjusteInventario d) {
//        BigDecimal f = d.getFactorConversion();
//        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
//    }
//
//    private BigDecimal cantidadBase(DetalleAjusteInventario d) {
//        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
//        return cant.multiply(factorDe(d));
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
package com.maxsoft.application.view.inventario.ajuste;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.TipoAjuste;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoAjusteService;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
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
@PageTitle("Ajuste de Inventario")
@Route(value = "inventario/consultaAjuste")
public class ConsultaAjusteInventarioView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    private static final String ESTADO_TODOS = "Todos";
    private static final String ESTADO_ACTIVOS = "Activos";
    private static final String ESTADO_ANULADOS = "Anulados";

    // Servicios
    private final AjusteInventarioService ajusteService;
    private final AlmacenService almacenService;
    private final TipoAjusteService tipoAjusteService;

    private final Grid<AjusteInventario> grid = new Grid<>(AjusteInventario.class, false);
    private final Grid<DetalleAjusteInventario> gridDetalle = new Grid<>(DetalleAjusteInventario.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Filtros de consulta
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén");
    private final ComboBox<TipoAjuste> cbTipoAjuste = new ComboBox<>("Tipo de Ajuste");
    private final ComboBox<String> cbEstado = new ComboBox<>("Estado");
    private final TextField txtBuscar = new TextField();
    private Button btnBuscar;
    private Button btnLimpiar;

    private final TabSheet tabs = new TabSheet();

    List<AjusteInventario> listaAjustes = new ArrayList<>();
    List<DetalleAjusteInventario> listDet = new ArrayList<>();

    private GridListDataView<AjusteInventario> dataViewAjustes;

    // Ajuste seleccionado (para mostrar el signo del movimiento en el detalle)
    private AjusteInventario ajusteSeleccionado;

    // Columnas con pie (totales)
    private Grid.Column<AjusteInventario> colCodigo;
    private Grid.Column<DetalleAjusteInventario> colDescripcion;
    private Grid.Column<DetalleAjusteInventario> colMovimiento;
    private Grid.Column<DetalleAjusteInventario> colTotal;

    @Autowired
    public ConsultaAjusteInventarioView(AjusteInventarioService ajusteService,
            AlmacenService almacenService,
            TipoAjusteService tipoAjusteService) {

        this.ajusteService = ajusteService;
        this.almacenService = almacenService;
        this.tipoAjusteService = tipoAjusteService;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();

        tabs.add("Ajuste de Inventario", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(RegistroAjusteDeInventarioView.class);

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

        add(
       botonera,
        new H2("Ajuste de Inventario")
       , crearBarraFiltros(),
          tabs);

        cargarCombos();
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

        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setClearButtonVisible(true);

        cbTipoAjuste.setItemLabelGenerator(TipoAjuste::getDescripcion);
        cbTipoAjuste.setClearButtonVisible(true);

        cbEstado.setItems(ESTADO_TODOS, ESTADO_ACTIVOS, ESTADO_ANULADOS);
        cbEstado.setAllowCustomValue(false);
        cbEstado.setValue(ESTADO_TODOS);

        txtBuscar.setWidth("260px");
        txtBuscar.setPlaceholder("Filtrar por código, observación o usuario...");
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
                dpDesde, dpHasta, cbAlmacen, cbTipoAjuste, cbEstado, txtBuscar, btnBuscar, btnLimpiar);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        // Permite que los filtros pasen a otra línea en pantallas angostas
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void cargarCombos() {

        cbAlmacen.setItems(almacenService.getLista());
        cbTipoAjuste.setItems(tipoAjusteService.getLista());
    }

    private void limpiarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        cbAlmacen.clear();
        cbTipoAjuste.clear();
        cbEstado.setValue(ESTADO_TODOS);
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
            Almacen almacen = cbAlmacen.getValue();
            TipoAjuste tipo = cbTipoAjuste.getValue();
            String estado = cbEstado.getValue() != null ? cbEstado.getValue() : ESTADO_TODOS;

            List<AjusteInventario> resultado = ajusteService.getLista()
                    .stream()
                    .filter(a -> dentroDeRango(aLocalDate(a.getFecha())))
                    .filter(a -> almacen == null || (a.getAlmacen() != null
                    && Objects.equals(a.getAlmacen().getCodigo(), almacen.getCodigo())))
                    .filter(a -> tipo == null || (a.getTipoAjuste() != null
                    && Objects.equals(a.getTipoAjuste().getCodigo(), tipo.getCodigo())))
                    .filter(a -> ESTADO_TODOS.equals(estado)
                    || (ESTADO_ANULADOS.equals(estado) == estaAnulado(a)))
                    .sorted(Comparator.comparing(
                            (AjusteInventario a) -> aLocalDate(a.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaAjustes.clear();
            listaAjustes.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando ajustes: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewAjustes == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewAjustes.setFilter(a -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchCod = a.getCodigo() != null && a.getCodigo().toString().contains(term);
            boolean matchObs = a.getObservacion() != null && a.getObservacion().toLowerCase().contains(term);
            boolean matchUsr = a.getNombreUsuario() != null && a.getNombreUsuario().toLowerCase().contains(term);
            return matchCod || matchObs || matchUsr;
        });

        colCodigo.setFooter("TOTAL AJUSTES: " + dataViewAjustes.getItemCount());
    }

    private void cargarDetalle(AjusteInventario ajuste) {

        Collection<DetalleAjusteInventario> detalles = this.ajusteService.getDetalle(ajuste.getCodigo());

        ajusteSeleccionado = ajuste;
        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).setLabel("Detalle( Ajuste -> " + ajuste.getCodigo()
                + (estaAnulado(ajuste) ? " - ANULADO" : "") + " )");

        actualizarTotalesDetalle();
    }

    private void limpiarDetalle() {

        ajusteSeleccionado = null;
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
        colMovimiento.setFooter(signo() + numero(totalBase));
        colTotal.setFooter(numero(totalGeneral));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        colCodigo = grid.addColumn(AjusteInventario::getCodigo)
                .setHeader("N° Ajuste")
                .setKey("codigo")
                .setFooter("TOTAL AJUSTES:");

        grid.addColumn(a -> {
            LocalDate f = aLocalDate(a.getFecha());
            return f != null ? f.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (AjusteInventario a) -> a.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(AjusteInventario::getNombreAlmacen)
                .setHeader("Almacén")
                .setKey("almacen");

        grid.addColumn(a -> a.getTipoAjuste() != null ? a.getTipoAjuste().getDescripcion() : "")
                .setHeader("Tipo")
                .setKey("tipo");

        grid.addColumn(AjusteInventario::getNombreUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        grid.addColumn(a -> estaAnulado(a) ? "ANULADO" : "Activo")
                .setHeader("Estado")
                .setKey("estado");

        grid.addColumn(AjusteInventario::getObservacion)
                .setHeader("Observación")
                .setKey("observacion")
                .setFlexGrow(1);

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        dataViewAjustes = grid.setItems(listaAjustes);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

        colDescripcion = gridDetalle.addColumn(DetalleAjusteInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(DetalleAjusteInventario::getNombreAlmacen)
                .setHeader("Almacén")
                .setKey("almacen");

        gridDetalle.addColumn(DetalleAjusteInventario::getNombreUnidad)
                .setHeader("Empaque / Presentación")
                .setKey("unidad");

        gridDetalle.addColumn(d -> numero(d.getExistencia()))
                .setHeader("Existencia Previa (UMM)")
                .setKey("existencia");

        gridDetalle.addColumn(d -> numero(d.getCantidad()))
                .setHeader("Cantidad")
                .setKey("cantidad")
                .setComparator(Comparator.comparing(
                        (DetalleAjusteInventario d) -> d.getCantidad(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // MULTI-EMPAQUE: factor y movimiento en unidad base (calculado aquí)
        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setKey("factor")
                .setComparator(Comparator.comparing(this::factorDe));

        // Movimiento en unidad base con signo según el tipo de ajuste
        colMovimiento = gridDetalle.addColumn(d -> signo() + numero(cantidadBase(d)))
                .setHeader("Movimiento (UMM)")
                .setKey("movimiento")
                .setComparator(Comparator.comparing(this::cantidadBase));

        gridDetalle.addColumn(d -> numero(d.getNuevaExistencia()))
                .setHeader("Nueva Existencia (UMM)")
                .setKey("nuevaexistencia");

        gridDetalle.addColumn(d -> numero(d.getCostoUnitario()))
                .setHeader("Costo Unit.")
                .setKey("costo");

        // Total calculado (cantidad x costo): no depende del campo "total" guardado
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
    private boolean estaAnulado(AjusteInventario a) {

        return Boolean.TRUE.equals(a.getAnulado());
    }

    private boolean esIncremento(AjusteInventario a) {

        return a != null && a.getTipoAjuste() != null
                && "INCREMENTO".equalsIgnoreCase(a.getTipoAjuste().getDescripcion());
    }

    /**
     * Signo del movimiento según el tipo del ajuste seleccionado.
     */
    private String signo() {

        return esIncremento(ajusteSeleccionado) ? "+" : "-";
    }

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

    private BigDecimal factorDe(DetalleAjusteInventario d) {

        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleAjusteInventario d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    private BigDecimal totalLinea(DetalleAjusteInventario d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
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
