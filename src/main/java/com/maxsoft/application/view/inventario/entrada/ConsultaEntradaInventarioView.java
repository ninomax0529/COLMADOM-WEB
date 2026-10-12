package com.maxsoft.application.view.inventario.entrada;

import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
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
@PageTitle("Consulta de Entradas de Inventario | MaxSoft ERP")
@Route(value = "inventario/consultaEntrada")
public class ConsultaEntradaInventarioView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    // Servicios
    private final EntradaDeInventarioService entradaService;

    // Filtros
    private DatePicker dpDesde;
    private DatePicker dpHasta;
    private TextField txtBuscar;
    private Button btnBuscar;
    private Button btnLimpiar;

    // Resumen
    private final Span lblResumen = new Span();
    private final H4 lblTituloDetalle = new H4("Detalle de la entrada");

    // Grids
    private Grid<EntradaInventario> gridEntradas;
    private Grid<DetalleEntradaInventario> gridDetalle;
    private GridListDataView<EntradaInventario> dataViewEntradas;
    private final List<EntradaInventario> listaEntradas = new ArrayList<>();
    private final List<DetalleEntradaInventario> listaDetalle = new ArrayList<>();

    @Autowired
    public ConsultaEntradaInventarioView(EntradaDeInventarioService entradaService) {

        this.entradaService = entradaService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Consulta de Entradas de Inventario"));
        add(crearFiltros());
        add(lblResumen);
        add(crearGridEntradas());
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

        txtBuscar = new TextField("N° entrada / Comentario / Usuario");
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
    // GRID PRINCIPAL (ENTRADAS)
    // ============================================================
    private Grid<EntradaInventario> crearGridEntradas() {

        gridEntradas = new Grid<>(EntradaInventario.class, false);
        gridEntradas.setWidthFull();
        gridEntradas.setHeight("320px");
        gridEntradas.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridEntradas.setSelectionMode(Grid.SelectionMode.SINGLE);

        dataViewEntradas = gridEntradas.setItems(listaEntradas);

        gridEntradas.addColumn(EntradaInventario::getCodigo)
                .setHeader("N° Entrada")
                .setAutoWidth(true);

        gridEntradas.addColumn(e -> {
            LocalDate f = aLocalDate(e.getFecha());
            return f != null ? f.format(FORMATO_FECHA) : "";
        }).setHeader("Fecha").setAutoWidth(true);

        gridEntradas.addColumn(e -> e.getFechaCreacion() != null
                ? new Date(e.getFechaCreacion().getTime()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_HORA)
                : "")
                .setHeader("Registrado")
                .setAutoWidth(true);

        gridEntradas.addColumn(EntradaInventario::getNombreUsuario)
                .setHeader("Usuario")
                .setAutoWidth(true);

        gridEntradas.addColumn(this::contarItems)
                .setHeader("Ítems")
                .setAutoWidth(true);

        gridEntradas.addColumn(this::totalEntrada)
                .setHeader("Total")
                .setAutoWidth(true);

        gridEntradas.addColumn(EntradaInventario::getComentario)
                .setHeader("Comentario")
                .setFlexGrow(1);

        gridEntradas.addSelectionListener(e
                -> cargarDetalle(e.getFirstSelectedItem().orElse(null)));

        return gridEntradas;
    }

    // ============================================================
    // GRID DETALLE
    // ============================================================
    private Grid<DetalleEntradaInventario> crearGridDetalle() {

        gridDetalle = new Grid<>(DetalleEntradaInventario.class, false);
        gridDetalle.setWidthFull();
        gridDetalle.setHeight("250px");
        gridDetalle.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridDetalle.setItems(listaDetalle);

        gridDetalle.addColumn(DetalleEntradaInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true)
                .setFlexGrow(1);

        gridDetalle.addColumn(DetalleEntradaInventario::getNombreAlmacen)
                .setHeader("Almacén Destino")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : d.getNombreUnidad())
                .setHeader("Empaque / Unidad")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getCantidadRecibida()))
                .setHeader("Cant. Recibida")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setAutoWidth(true);

        // Cantidad ingresada en unidad base: cantidad x factor (calculado aquí)
        gridDetalle.addColumn(d -> "+" + numero(cantidadBase(d)))
                .setHeader("Entrada (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getExistenciaActual()))
                .setHeader("Existencia Previa (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(d.getNuevaExistencia()))
                .setHeader("Nueva Existencia (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(d -> numero(costoDe(d)))
                .setHeader("Costo Unit.")
                .setAutoWidth(true);

        // Total calculado (cantidad x costo), no depende del campo "total" guardado
        gridDetalle.addColumn(d -> numero(totalLinea(d)))
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
            List<EntradaInventario> resultado = entradaService.getLista()
                    .stream()
                    .filter(e -> dentroDeRango(aLocalDate(e.getFecha())))
                    .sorted(Comparator.comparing(
                            (EntradaInventario e) -> aLocalDate(e.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaEntradas.clear();
            listaEntradas.addAll(resultado);
            gridEntradas.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            gridEntradas.deselectAll();
            cargarDetalle(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando entradas: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void aplicarFiltroTexto() {

        if (dataViewEntradas == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewEntradas.setFilter(e -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchCod = e.getCodigo() != null && e.getCodigo().toString().contains(term);
            boolean matchCom = e.getComentario() != null && e.getComentario().toLowerCase().contains(term);
            boolean matchUsr = e.getNombreUsuario() != null && e.getNombreUsuario().toLowerCase().contains(term);
            return matchCod || matchCom || matchUsr;
        });

        lblResumen.setText("Entradas encontradas: " + dataViewEntradas.getItemCount());
    }

    private void cargarDetalle(EntradaInventario entrada) {

        listaDetalle.clear();

        if (entrada == null) {
            lblTituloDetalle.setText("Detalle de la entrada (seleccione una entrada)");
            gridDetalle.getDataProvider().refreshAll();
            return;
        }

        try {
            Collection<DetalleEntradaInventario> detalles = this.entradaService.getDetalle(entrada.getCodigo());
            if (detalles != null) {
                listaDetalle.addAll(detalles);
            }
            lblTituloDetalle.setText("Detalle de la entrada #" + entrada.getCodigo());
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

    private String contarItems(EntradaInventario e) {
        try {
            Collection<DetalleEntradaInventario> c = e.getDetalleEntradaInventarioCollection();
            return c != null ? String.valueOf(c.size()) : "0";
        } catch (RuntimeException ex) {
            return "-"; // detalle no inicializado (lazy)
        }
    }

    private String totalEntrada(EntradaInventario e) {
        try {
            Collection<DetalleEntradaInventario> c = e.getDetalleEntradaInventarioCollection();
            if (c == null) {
                return numero(BigDecimal.ZERO);
            }
            BigDecimal total = c.stream()
                    .map(this::totalLinea)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return numero(total);
        } catch (RuntimeException ex) {
            return "-"; // detalle no inicializado (lazy)
        }
    }

    private BigDecimal factorDe(DetalleEntradaInventario d) {
        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleEntradaInventario d) {
        BigDecimal cant = d.getCantidadRecibida() != null ? d.getCantidadRecibida() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    private BigDecimal costoDe(DetalleEntradaInventario d) {
        if (d.getCostoUnitario() != null) {
            return d.getCostoUnitario();
        }
        return d.getPrecioCompra() != null ? d.getPrecioCompra() : BigDecimal.ZERO;
    }

    private BigDecimal totalLinea(DetalleEntradaInventario d) {
        BigDecimal cant = d.getCantidadRecibida() != null ? d.getCantidadRecibida() : BigDecimal.ZERO;
        return cant.multiply(costoDe(d));
    }

    private String numero(BigDecimal valor) {
        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}