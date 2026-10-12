package com.maxsoft.application.view.inventario.salida;

import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import java.util.stream.Collectors;

@PermitAll
@PageTitle("Consulta de Salidas de Inventario | MaxSoft ERP")
@Route(value = "inventario/consultaSalida")
public class ConsultaSalidaInventarioView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    // Servicios
    private final SalidaInventarioService salidaService;

    // Filtros
    private DatePicker dpDesde;
    private DatePicker dpHasta;
    private TextField txtBuscar;
    private Button btnBuscar;
    private Button btnLimpiar;

    // Resumen
    private final Span lblResumen = new Span();
    private final H4 lblTituloDetalle = new H4("Detalle de la salida");

    // Grids
    private Grid<SalidaInventario> gridSalidas;
    private Grid<DetalleSalidaInventario> gridDetalle;
    private GridListDataView<SalidaInventario> dataViewSalidas;
    private final List<SalidaInventario> listaSalidas = new ArrayList<>();
    private final List<DetalleSalidaInventario> listaDetalle = new ArrayList<>();

    @Autowired
    public ConsultaSalidaInventarioView(SalidaInventarioService salidaService) {

        this.salidaService = salidaService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Consulta de Salidas de Inventario"));
        add(crearFiltros());
        add(lblResumen);
        add(crearGridSalidas());
        add(lblTituloDetalle);
        add(crearGridDetalle());

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

        txtBuscar = new TextField("N° salida / Observación");
        txtBuscar.setPlaceholder("Filtrar resultados...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());

        FormLayout form = new FormLayout();
        form.add(dpDesde, dpHasta, txtBuscar);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3)
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

    private void limpiarFiltros() {
        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        txtBuscar.clear();
        buscar();
    }

    // ============================================================
    // GRID PRINCIPAL (SALIDAS)
    // ============================================================
    private Grid<SalidaInventario> crearGridSalidas() {

        gridSalidas = new Grid<>(SalidaInventario.class, false);
        gridSalidas.setWidthFull();
        gridSalidas.setHeight("320px");
        gridSalidas.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridSalidas.setSelectionMode(Grid.SelectionMode.SINGLE);

        dataViewSalidas = gridSalidas.setItems(listaSalidas);

        gridSalidas.addColumn(SalidaInventario::getCodigo)
                .setHeader("N° Salida")
                .setAutoWidth(true);

        gridSalidas.addColumn(s -> {
            LocalDate f = aLocalDate(s.getFecha());
            return f != null ? f.format(FORMATO_FECHA) : "";
        }).setHeader("Fecha").setAutoWidth(true);

        gridSalidas.addColumn(s -> s.getFechaRegistro() != null
                ? new Date(s.getFechaRegistro().getTime()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_HORA)
                : "")
                .setHeader("Registrado")
                .setAutoWidth(true);

        gridSalidas.addColumn(this::contarItems)
                .setHeader("Ítems")
                .setAutoWidth(true);

        gridSalidas.addColumn(this::totalSalida)
                .setHeader("Total")
                .setAutoWidth(true);

        gridSalidas.addColumn(SalidaInventario::getObservacion)
                .setHeader("Observación")
                .setFlexGrow(1);

        gridSalidas.addSelectionListener(e
                -> cargarDetalle(e.getFirstSelectedItem().orElse(null)));

        return gridSalidas;
    }

    // ============================================================
    // GRID DETALLE
    // ============================================================
    private Grid<DetalleSalidaInventario> crearGridDetalle() {

        gridDetalle = new Grid<>(DetalleSalidaInventario.class, false);
        gridDetalle.setWidthFull();
        gridDetalle.setHeight("250px");
        gridDetalle.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridDetalle.setItems(listaDetalle);

        gridDetalle.addColumn(DetalleSalidaInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true)
                .setFlexGrow(1);

        gridDetalle.addColumn(DetalleSalidaInventario::getNombreAlmacen)
                .setHeader("Almacén")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : "")
                .setHeader("Empaque / Presentación")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getCantidad()))
                .setHeader("Cantidad")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setAutoWidth(true);

        // Cantidad descontada en unidad base: cantidad x factor (calculado aquí)
        gridDetalle.addColumn(d -> "-" + numero(cantidadBase(d)))
                .setHeader("Salida (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getExistenciaAnterior()))
                .setHeader("Existencia Previa (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getExistencia()))
                .setHeader("Nueva Existencia (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getPrecioCompra()))
                .setHeader("Costo Unit.")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getSubTotal()))
                .setHeader("Subtotal")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getItbis()))
                .setHeader("ITBIS")
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
            List<SalidaInventario> resultado = salidaService.getLista()
                    .stream()
                    .filter(s -> dentroDeRango(aLocalDate(s.getFecha())))
                    .sorted(Comparator.comparing(
                            (SalidaInventario s) -> aLocalDate(s.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaSalidas.clear();
            listaSalidas.addAll(resultado);
            gridSalidas.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            gridSalidas.deselectAll();
            cargarDetalle(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando salidas: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void aplicarFiltroTexto() {

        if (dataViewSalidas == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewSalidas.setFilter(s -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchCod = s.getCodigo() != null && s.getCodigo().toString().contains(term);
            boolean matchObs = s.getObservacion() != null && s.getObservacion().toLowerCase().contains(term);
            return matchCod || matchObs;
        });

        lblResumen.setText("Salidas encontradas: " + dataViewSalidas.getItemCount());
    }

    private void cargarDetalle(SalidaInventario salida) {

        listaDetalle.clear();

        if (salida == null) {
            lblTituloDetalle.setText("Detalle de la salida (seleccione una salida)");
            gridDetalle.getDataProvider().refreshAll();
            return;
        }

        try {
            Collection<DetalleSalidaInventario> detalles =this.salidaService.getDetalle(salida.getCodigo());
            if (detalles != null) {
                listaDetalle.addAll(detalles);
            }
            lblTituloDetalle.setText("Detalle de la salida #" + salida.getCodigo());
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

    private String contarItems(SalidaInventario s) {
        try {
            Collection<DetalleSalidaInventario> c = s.getDetalleSalidaInventarioCollection();
            return c != null ? String.valueOf(c.size()) : "0";
        } catch (RuntimeException ex) {
            return "-"; // detalle no inicializado (lazy)
        }
    }

    private String totalSalida(SalidaInventario s) {
        try {
            Collection<DetalleSalidaInventario> c = s.getDetalleSalidaInventarioCollection();
            if (c == null) {
                return numero(BigDecimal.ZERO);
            }
            BigDecimal total = c.stream()
                    .map(d -> d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return numero(total);
        } catch (RuntimeException ex) {
            return "-"; // detalle no inicializado (lazy)
        }
    }

    private BigDecimal factorDe(DetalleSalidaInventario d) {
        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleSalidaInventario d) {
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