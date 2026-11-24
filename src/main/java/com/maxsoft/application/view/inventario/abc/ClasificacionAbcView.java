package com.maxsoft.application.view.inventario.abc;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloClasificacionAbc;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.ClasificacionAbcService;
import com.maxsoft.application.servicio.interfaces.inventario.ClasificacionAbcService.ResumenAbc;
import com.maxsoft.application.util.ClasificacionAbcUtil;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Clasificación ABC de los artículos por valor (RD$) y rotación (facturas).
 * Solo consulta: el cálculo lo hace ClasificacionAbcService cada noche; el
 * botón "Recalcular ahora" lo adelanta.
 */
@PageTitle("Clasificación ABC | MaxSoft ERP")
@Route(value = "inventario/clasificacionAbc")
public class ClasificacionAbcView extends VerticalLayout {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private static final DecimalFormat CANTIDAD = new DecimalFormat("#,##0.##");
    private static final DecimalFormat PORCIENTO = new DecimalFormat("0.0'%'");

    /** Clase ABC: texto para el usuario y estilo del chip y la tarjeta. */
    private enum Clase {
        A(ClasificacionAbcUtil.CLASE_A, "A · Vital", "fv-chip-pagada", "fv-tarjeta-pagado", VaadinIcon.STAR,
                "Lo que más vende o más se pide: nunca debe faltar"),
        B(ClasificacionAbcUtil.CLASE_B, "B · Importante", "fv-chip-credito", "fv-tarjeta-devuelto", VaadinIcon.CHART,
                "Movimiento intermedio"),
        C(ClasificacionAbcUtil.CLASE_C, "C · Poco movimiento", "fv-chip-contado", "fv-tarjeta-saldada",
                VaadinIcon.ARROW_DOWN, "Vende poco: compre con cuidado"),
        N(ClasificacionAbcUtil.CLASE_NUEVO, "Nuevo", "fv-chip-delivery", "fv-tarjeta-total", VaadinIcon.PLUS_CIRCLE,
                "Creado hace poco: todavía no tiene historia suficiente");

        private final String codigo;
        private final String nombre;
        private final String chip;
        private final String tarjeta;
        private final VaadinIcon icono;
        private final String descripcion;

        Clase(String codigo, String nombre, String chip, String tarjeta, VaadinIcon icono, String descripcion) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.chip = chip;
            this.tarjeta = tarjeta;
            this.icono = icono;
            this.descripcion = descripcion;
        }

        static Clase de(String codigo) {
            for (Clase c : values()) {
                if (c.codigo.equals(codigo)) {
                    return c;
                }
            }
            return C;
        }
    }

    /**
     * Etiqueta de color de la clase ("A · Vital", "Nuevo"...) con su
     * explicación al pasar el mouse. La usan también la lista y el formulario
     * de artículos (la vista debe tener EstiloConsulta.aplicarVista).
     */
    public static Span chip(String codigoClase) {
        Clase c = Clase.de(codigoClase);
        Span chip = EstiloConsulta.chip(c.nombre, c.chip);
        chip.getElement().setAttribute("title", c.descripcion);
        return chip;
    }

    /** Nombre de la clase para filtros ("A · Vital"). */
    public static String nombreClase(String codigoClase) {
        return Clase.de(codigoClase).nombre;
    }

    /** Códigos de las clases en orden: A, B, C, N. */
    public static List<String> codigosClase() {
        return java.util.Arrays.stream(Clase.values()).map(c -> c.codigo).toList();
    }

    /** Una fila del grid: el resultado guardado más datos del artículo. */
    private record Fila(ArticuloClasificacionAbc abc, String articulo, BigDecimal existencia) {

        Clase clase() {
            return Clase.de(abc.getClase());
        }

        boolean sinMovimiento() {
            return Boolean.TRUE.equals(abc.getSinMovimiento());
        }
    }

    private final ClasificacionAbcService abcService;
    private final ArticuloService articuloService;

    private final ComboBox<Clase> cbClase = new ComboBox<>();
    private final Checkbox chkSinMovimiento = new Checkbox("Solo sin movimiento");
    private final TextField txtBuscar = new TextField();
    private final Span lblPeriodo = new Span();
    private final Div resumen = new Div();
    private final Grid<Fila> grid = new Grid<>();
    private final List<Fila> filas = new ArrayList<>();
    private GridListDataView<Fila> dataView;

    public ClasificacionAbcView(ClasificacionAbcService abcService, ArticuloService articuloService) {

        this.abcService = abcService;
        this.articuloService = articuloService;

        setSizeFull();
        setSpacing(false);
        EstiloConsulta.aplicarVista(this, null);

        configurarGrid();
        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");

        H2 titulo = new H2("Clasificación ABC");
        titulo.addClassName("fv-titulo");

        lblPeriodo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        add(titulo, lblPeriodo, crearFiltros(), resumen, grid);
        setFlexGrow(1, grid);

        cargar();
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private HorizontalLayout crearFiltros() {

        cbClase.setItems(Clase.values());
        cbClase.setItemLabelGenerator(c -> c.nombre);
        cbClase.setPlaceholder("Todas las clases");
        cbClase.setClearButtonVisible(true);
        cbClase.setWidth("220px");
        cbClase.addValueChangeListener(e -> aplicarFiltros());

        chkSinMovimiento.addValueChangeListener(e -> aplicarFiltros());

        txtBuscar.setWidth("240px");
        txtBuscar.setPlaceholder("Buscar artículo...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltros());

        Button btnRecalcular = new Button("Recalcular ahora", VaadinIcon.REFRESH.create(), e -> recalcular());
        btnRecalcular.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnRecalcular.setTooltipText("Se recalcula sola cada noche; use esto si quiere verla al momento");
        btnRecalcular.setDisableOnClick(true);
        btnRecalcular.addClickListener(e -> btnRecalcular.setEnabled(true));

        HorizontalLayout barra = new HorizontalLayout(cbClase, txtBuscar, chkSinMovimiento, btnRecalcular,
                EstiloConsulta.ayuda("A = 80 % de las ventas o de las facturas · B = hasta el 95 % · C = el resto"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void aplicarFiltros() {
        Clase clase = cbClase.getValue();
        boolean soloParados = Boolean.TRUE.equals(chkSinMovimiento.getValue());
        String texto = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toUpperCase();

        dataView.setFilter(f -> (clase == null || f.clase() == clase)
                && (!soloParados || f.sinMovimiento())
                && (texto.isEmpty() || f.articulo().toUpperCase().contains(texto)
                || String.valueOf(f.abc().getArticulo()).equals(texto)));
    }

    // ============================================================
    // GRID
    // ============================================================
    private void configurarGrid() {

        EstiloConsulta.aplicarGrid(grid);
        grid.setSizeFull();
        dataView = grid.setItems(filas);

        grid.addComponentColumn(f -> chip(f.abc().getClase())).setHeader("Clase").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(Fila::clase));

        grid.addColumn(Fila::articulo).setHeader("Artículo")
                .setAutoWidth(true).setFlexGrow(2).setSortable(true);

        grid.addColumn(f -> f.abc().getClaseValor() + " / " + f.abc().getClaseRotacion())
                .setHeader("Valor / Rotación").setAutoWidth(true).setFlexGrow(0)
                .setTooltipGenerator(f -> "Por valor (RD$): " + f.abc().getClaseValor()
                + " · Por rotación (facturas): " + f.abc().getClaseRotacion());

        EstiloConsulta.monto(grid.addColumn(f -> EstiloConsulta.moneda(f.abc().getVentas())))
                .setHeader("Ventas").setAutoWidth(true)
                .setComparator(Comparator.comparing(f -> f.abc().getVentas()));

        EstiloConsulta.monto(grid.addColumn(f -> PORCIENTO.format(f.abc().getPorcientoValor())))
                .setHeader("% ventas").setAutoWidth(true)
                .setComparator(Comparator.comparing(f -> f.abc().getPorcientoValor()));

        EstiloConsulta.monto(grid.addColumn(f -> String.valueOf(f.abc().getFacturas())))
                .setHeader("Facturas").setAutoWidth(true)
                .setComparator(Comparator.comparing(f -> f.abc().getFacturas()));

        EstiloConsulta.monto(grid.addColumn(f -> PORCIENTO.format(f.abc().getPorcientoRotacion())))
                .setHeader("% facturas").setAutoWidth(true)
                .setComparator(Comparator.comparing(f -> f.abc().getPorcientoRotacion()));

        EstiloConsulta.monto(grid.addColumn(f -> CANTIDAD.format(f.abc().getUnidades())))
                .setHeader("Unidades").setAutoWidth(true)
                .setComparator(Comparator.comparing(f -> f.abc().getUnidades()));

        EstiloConsulta.monto(grid.addColumn(f -> f.existencia() == null ? "—" : CANTIDAD.format(f.existencia())))
                .setHeader("Existencia").setAutoWidth(true)
                .setComparator(Comparator.comparing(Fila::existencia, Comparator.nullsFirst(Comparator.naturalOrder())));

        grid.addComponentColumn(f -> f.sinMovimiento()
                ? EstiloConsulta.chip("SIN MOVIMIENTO", "fv-chip-devuelto")
                : new Span())
                .setHeader("").setAutoWidth(true).setFlexGrow(0);
    }

    // ============================================================
    // DATOS
    // ============================================================
    private void cargar() {

        Map<Integer, Articulo> articulos = articuloService.getLista().stream()
                .collect(Collectors.toMap(Articulo::getCodigo, Function.identity(), (x, y) -> x));

        filas.clear();
        for (ArticuloClasificacionAbc abc : abcService.obtenerTodas()) {
            Articulo a = articulos.get(abc.getArticulo());
            filas.add(new Fila(abc,
                    a != null && a.getDescripcion() != null ? a.getDescripcion() : "Artículo " + abc.getArticulo(),
                    a != null ? a.getExistencia() : null));
        }
        // Lo más importante arriba: por clase y, dentro de cada una, por ventas
        filas.sort(Comparator.comparing(Fila::clase)
                .thenComparing(f -> f.abc().getVentas(), Comparator.reverseOrder()));
        dataView.refreshAll();
        aplicarFiltros();

        actualizarPeriodo();
        actualizarResumen();
    }

    private void actualizarPeriodo() {
        if (filas.isEmpty()) {
            lblPeriodo.setText("Todavía no hay cálculo. Use \"Recalcular ahora\".");
            return;
        }
        ArticuloClasificacionAbc primera = filas.get(0).abc();
        lblPeriodo.setText("Ventas del " + primera.getFechaDesde().format(FECHA)
                + " al " + primera.getFechaHasta().format(FECHA)
                + "  ·  Calculado: " + primera.getFechaCalculo().format(FECHA_HORA)
                + "  ·  Se actualiza sola cada noche a las 2:00 a.m.");
    }

    private void recalcular() {
        try {
            ResumenAbc r = abcService.recalcular();
            cargar();
            Notification n = Notification.show("Clasificación actualizada: " + r.articulos() + " artículos (A "
                    + r.claseA() + ", B " + r.claseB() + ", C " + r.claseC() + ", nuevos " + r.nuevos() + ")",
                    4000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (RuntimeException e) {
            Notification n = Notification.show("No se pudo recalcular: " + e.getMessage(),
                    5000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // ============================================================
    // TARJETAS
    // ============================================================
    private void actualizarResumen() {
        BigDecimal totalVentas = filas.stream().map(f -> f.abc().getVentas())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        resumen.removeAll();
        for (Clase c : Clase.values()) {
            resumen.add(tarjetaClase(c, totalVentas));
        }
        resumen.add(tarjetaSinMovimiento());
    }

    /** Artículos de la clase y su parte de las ventas; al hacer clic filtra por la clase. */
    private Div tarjetaClase(Clase clase, BigDecimal totalVentas) {
        List<Fila> de = filtrar(f -> f.clase() == clase);
        BigDecimal ventas = de.stream().map(f -> f.abc().getVentas()).reduce(BigDecimal.ZERO, BigDecimal::add);
        String parte = totalVentas.signum() == 0 ? "0 %"
                : ventas.multiply(BigDecimal.valueOf(100)).divide(totalVentas, 0, RoundingMode.HALF_UP) + " %";

        Div d = tarjeta(clase.icono, clase.nombre, de.size(), parte + " de las ventas", clase.tarjeta,
                clase.descripcion);
        d.addClickListener(e -> {
            chkSinMovimiento.setValue(false);
            cbClase.setValue(clase);
        });
        return d;
    }

    private Div tarjetaSinMovimiento() {
        List<Fila> parados = filtrar(Fila::sinMovimiento);
        Div d = tarjeta(VaadinIcon.PAUSE, "Sin movimiento", parados.size(), "sin ventas en el período",
                "fv-tarjeta-pendiente", "Mercancía parada: no se vendió en el período");
        d.addClickListener(e -> {
            cbClase.clear();
            chkSinMovimiento.setValue(true);
        });
        return d;
    }

    private List<Fila> filtrar(Predicate<Fila> condicion) {
        return filas.stream().filter(condicion).toList();
    }

    private static Div tarjeta(VaadinIcon icono, String etiqueta, int cantidad, String detalle,
            String clase, String ayuda) {
        Icon ic = icono.create();
        ic.addClassName("fv-tarjeta-icono");
        Span lbl = new Span(etiqueta);
        lbl.addClassName("fv-tarjeta-etiqueta");
        Span val = new Span(cantidad + (cantidad == 1 ? " artículo" : " artículos"));
        val.addClassName("fv-tarjeta-valor");
        Span det = new Span(detalle);
        det.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");
        Div d = new Div(ic, lbl, val, det);
        d.addClassNames("fv-tarjeta", clase);
        d.getStyle().set("cursor", "pointer");
        d.getElement().setAttribute("title", ayuda + ". Clic para ver solo estos.");
        return d;
    }
}
