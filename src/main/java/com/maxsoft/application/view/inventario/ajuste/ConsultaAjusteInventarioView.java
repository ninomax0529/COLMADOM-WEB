package com.maxsoft.application.view.inventario.ajuste;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.TipoAjuste;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoAjusteService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
@PageTitle("Consulta de Ajustes de Inventario | MaxSoft ERP")
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

    // Filtros
    private DatePicker dpDesde;
    private DatePicker dpHasta;
    private ComboBox<Almacen> cbAlmacen;
    private ComboBox<TipoAjuste> cbTipoAjuste;
    private ComboBox<String> cbEstado;
    private TextField txtBuscar;
    private Button btnBuscar;
    private Button btnLimpiar;

    // Resumen
    private final Span lblResumen = new Span();
    private final H4 lblTituloDetalle = new H4("Detalle del ajuste");

    // Grids
    private Grid<AjusteInventario> gridAjustes;
    private Grid<DetalleAjusteInventario> gridDetalle;
    private GridListDataView<AjusteInventario> dataViewAjustes;
    private final List<AjusteInventario> listaAjustes = new ArrayList<>();
    private final List<DetalleAjusteInventario> listaDetalle = new ArrayList<>();

    // Ajuste seleccionado (para mostrar el signo del movimiento en el detalle)
    private AjusteInventario ajusteSeleccionado;

    @Autowired
    public ConsultaAjusteInventarioView(AjusteInventarioService ajusteService,
            AlmacenService almacenService,
            TipoAjusteService tipoAjusteService) {

        this.ajusteService = ajusteService;
        this.almacenService = almacenService;
        this.tipoAjusteService = tipoAjusteService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Consulta de Ajustes de Inventario"));
        add(crearFiltros());
        add(lblResumen);
        add(crearGridAjustes());
        add(lblTituloDetalle);
        add(crearGridDetalle());

        cargarCombos();
        buscar();
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private VerticalLayout crearFiltros() {

        dpDesde = new DatePicker("Desde");
        dpDesde.setValue(LocalDate.now().minusDays(30));

        dpHasta = new DatePicker("Hasta");
        dpHasta.setValue(LocalDate.now());

        cbAlmacen = new ComboBox<>("Almacén");
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setClearButtonVisible(true);

        cbTipoAjuste = new ComboBox<>("Tipo de Ajuste");
        cbTipoAjuste.setItemLabelGenerator(TipoAjuste::getDescripcion);
        cbTipoAjuste.setClearButtonVisible(true);

        cbEstado = new ComboBox<>("Estado");
        cbEstado.setItems(ESTADO_TODOS, ESTADO_ACTIVOS, ESTADO_ANULADOS);
        cbEstado.setAllowCustomValue(false);
        cbEstado.setValue(ESTADO_TODOS);

        txtBuscar = new TextField("N° ajuste / Observación / Usuario");
        txtBuscar.setPlaceholder("Filtrar resultados...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());

        FormLayout form = new FormLayout();
        form.add(dpDesde, dpHasta, cbAlmacen, cbTipoAjuste, cbEstado, txtBuscar);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 6)
        );

        btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        btnLimpiar = new Button("Limpiar filtros", VaadinIcon.REFRESH.create(), e -> limpiarFiltros());

        VerticalLayout contenedor = new VerticalLayout(form, new HorizontalLayout(btnBuscar, btnLimpiar));
        contenedor.setPadding(false);
        contenedor.setSpacing(true);
        return contenedor;
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
    // GRID PRINCIPAL (AJUSTES)
    // ============================================================
    private Grid<AjusteInventario> crearGridAjustes() {

        gridAjustes = new Grid<>(AjusteInventario.class, false);
        gridAjustes.setWidthFull();
        gridAjustes.setHeight("320px");
        gridAjustes.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridAjustes.setSelectionMode(Grid.SelectionMode.SINGLE);

        dataViewAjustes = gridAjustes.setItems(listaAjustes);

        gridAjustes.addColumn(AjusteInventario::getCodigo)
                .setHeader("N° Ajuste")
                .setAutoWidth(true);

        gridAjustes.addColumn(a -> {
            LocalDate f = aLocalDate(a.getFecha());
            return f != null ? f.format(FORMATO_FECHA) : "";
        }).setHeader("Fecha").setAutoWidth(true);

        gridAjustes.addColumn(AjusteInventario::getNombreAlmacen)
                .setHeader("Almacén")
                .setAutoWidth(true);

        gridAjustes.addColumn(a -> a.getTipoAjuste() != null ? a.getTipoAjuste().getDescripcion() : "")
                .setHeader("Tipo")
                .setAutoWidth(true);

        gridAjustes.addColumn(this::contarItems)
                .setHeader("Ítems")
                .setAutoWidth(true);

        gridAjustes.addColumn(AjusteInventario::getNombreUsuario)
                .setHeader("Usuario")
                .setAutoWidth(true);

        gridAjustes.addColumn(a -> estaAnulado(a) ? "ANULADO" : "Activo")
                .setHeader("Estado")
                .setAutoWidth(true);

        gridAjustes.addColumn(AjusteInventario::getObservacion)
                .setHeader("Observación")
                .setFlexGrow(1);

        gridAjustes.addSelectionListener(e
                -> cargarDetalle(e.getFirstSelectedItem().orElse(null)));

        return gridAjustes;
    }

    // ============================================================
    // GRID DETALLE
    // ============================================================
    private Grid<DetalleAjusteInventario> crearGridDetalle() {

        gridDetalle = new Grid<>(DetalleAjusteInventario.class, false);
        gridDetalle.setWidthFull();
        gridDetalle.setHeight("250px");
        gridDetalle.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridDetalle.setItems(listaDetalle);

        gridDetalle.addColumn(DetalleAjusteInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true)
                .setFlexGrow(1);

        gridDetalle.addColumn(DetalleAjusteInventario::getNombreAlmacen)
                .setHeader("Almacén")
                .setAutoWidth(true);

        gridDetalle.addColumn(DetalleAjusteInventario::getNombreUnidad)
                .setHeader("Empaque / Presentación")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getExistencia()))
                .setHeader("Existencia Previa (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getCantidad()))
                .setHeader("Cantidad")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setAutoWidth(true);

        // Movimiento en unidad base con signo según el tipo de ajuste
        gridDetalle.addColumn(d -> (esIncremento(ajusteSeleccionado) ? "+" : "-") + numero(cantidadBase(d)))
                .setHeader("Movimiento (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getNuevaExistencia()))
                .setHeader("Nueva Existencia (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getCostoUnitario()))
                .setHeader("Costo Unit.")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getTotal()))
                .setHeader("Total")
                .setAutoWidth(true);

        return gridDetalle;
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
            gridAjustes.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            gridAjustes.deselectAll();
            cargarDetalle(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando ajustes: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

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

        lblResumen.setText("Ajustes encontrados: " + dataViewAjustes.getItemCount());
    }

    private void cargarDetalle(AjusteInventario ajuste) {

        ajusteSeleccionado = ajuste;
        listaDetalle.clear();

        if (ajuste == null) {
            lblTituloDetalle.setText("Detalle del ajuste (seleccione un ajuste)");
            gridDetalle.getDataProvider().refreshAll();
            return;
        }

        try {
            Collection<DetalleAjusteInventario> detalles = this.ajusteService.getDetalle(ajuste.getCodigo());
            if (detalles != null) {
                listaDetalle.addAll(detalles);
            }
            lblTituloDetalle.setText("Detalle del ajuste #" + ajuste.getCodigo()
                    + (estaAnulado(ajuste) ? " (ANULADO)" : ""));
        } catch (RuntimeException ex) {
            // Normalmente LazyInitializationException si el servicio no trae el detalle
            ex.printStackTrace();
            mostrarNotificacion("No se pudo cargar el detalle: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }

        gridDetalle.getDataProvider().refreshAll();
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

    private String contarItems(AjusteInventario a) {
        try {
            Collection<DetalleAjusteInventario> c = a.getDetalleAjusteInventarioCollection();
            return c != null ? String.valueOf(c.size()) : "0";
        } catch (RuntimeException ex) {
            return "-"; // detalle no inicializado (lazy)
        }
    }

    private BigDecimal factorDe(DetalleAjusteInventario d) {
        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleAjusteInventario d) {
        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    private String numero(BigDecimal valor) {
        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}
