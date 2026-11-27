package com.maxsoft.application.view.inventario.merma;

import com.maxsoft.application.dto.ResumenMermaDTO;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.servicio.interfaces.inventario.MermaService;
import com.maxsoft.application.util.AnalisisMermaUtil;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.inventario.lote.LotesVencimientosView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Análisis de la merma del período: cuánto se perdió (RD$) por tipo y por
 * artículo, qué porcentaje de las ventas representa y cuánta mercancía está
 * en riesgo de convertirse en merma (lotes vencidos o que vencen pronto).
 * Las mermas anuladas no cuentan.
 */
@PermitAll
@PageTitle("Análisis de Merma | MaxSoft ERP")
@Route(value = "inventario/analisisMerma")
public class AnalisisMermaView extends VerticalLayout {

    /** Hasta este % acumulado un artículo está entre los que concentran la pérdida (Pareto). */
    private static final BigDecimal PARETO = new BigDecimal("80");

    private static final DecimalFormat CANTIDAD = new DecimalFormat("#,##0.##");
    private static final DecimalFormat PORCENTAJE = new DecimalFormat("#,##0.00 '%'");

    /** Fila del grid de artículos con su porcentaje y el acumulado. */
    private record FilaArticulo(ResumenMermaDTO resumen, BigDecimal porcentaje, BigDecimal acumulado) {
    }

    private final MermaService mermaService;
    private final LoteService loteService;

    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén");
    private final Div resumen = new Div();
    private final Grid<ResumenMermaDTO> gridTipo = new Grid<>(ResumenMermaDTO.class, false);
    private final Grid<FilaArticulo> gridArticulo = new Grid<>(FilaArticulo.class, false);

    private Grid.Column<ResumenMermaDTO> colTipoNombre;
    private Grid.Column<ResumenMermaDTO> colTipoValor;
    private Grid.Column<FilaArticulo> colArtNombre;
    private Grid.Column<FilaArticulo> colArtValor;
    private BigDecimal totalPerdida = BigDecimal.ZERO;

    public AnalisisMermaView(MermaService mermaService, LoteService loteService, AlmacenService almacenService) {

        this.mermaService = mermaService;
        this.loteService = loteService;

        setSizeFull();
        setSpacing(false);
        EstiloConsulta.aplicarVista(this, null);

        H2 titulo = new H2("Análisis de Merma");
        titulo.addClassName("fv-titulo");

        cbAlmacen.setItems(almacenService.getLista());
        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");
        configurarGridTipo();
        configurarGridArticulo();

        VerticalLayout panelTipo = panel("Por tipo de merma", gridTipo);
        panelTipo.setWidth("38%");
        VerticalLayout panelArticulo = panel("Por artículo (los que concentran el "
                + PARETO.toPlainString() + " % de la pérdida están marcados)", gridArticulo);
        HorizontalLayout grids = new HorizontalLayout(panelTipo, panelArticulo);
        grids.setSizeFull();
        grids.setFlexGrow(1, panelArticulo);

        add(titulo, crearFiltros(), resumen, grids);
        setFlexGrow(1, grids);

        buscar();
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private HorizontalLayout crearFiltros() {

        dpDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dpHasta.setValue(LocalDate.now());

        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setClearButtonVisible(true);
        cbAlmacen.setPlaceholder("Todos");

        Button btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        Button btnMes = new Button("Este mes", e -> periodo(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        Button btnAnterior = new Button("Mes anterior", e -> {
            LocalDate inicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            periodo(inicio, inicio.plusMonths(1).minusDays(1));
        });

        HorizontalLayout barra = new HorizontalLayout(dpDesde, dpHasta, cbAlmacen, btnBuscar, btnMes, btnAnterior,
                EstiloConsulta.ayuda("Valores al costo; las mermas anuladas no cuentan"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void periodo(LocalDate desde, LocalDate hasta) {
        dpDesde.setValue(desde);
        dpHasta.setValue(hasta);
        buscar();
    }

    private Integer almacen() {
        return cbAlmacen.getValue() != null ? cbAlmacen.getValue().getCodigo() : null;
    }

    // ============================================================
    // CARGA
    // ============================================================
    private void buscar() {

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        List<ResumenMermaDTO> tipos = mermaService.resumenPorTipo(desde, hasta, almacen());
        List<ResumenMermaDTO> articulos = mermaService.resumenPorArticulo(desde, hasta, almacen());

        totalPerdida = tipos.stream().map(ResumenMermaDTO::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        long mermas = tipos.stream().mapToLong(ResumenMermaDTO::mermas).sum();

        gridTipo.setItems(tipos);
        colTipoNombre.setFooter(tipos.size() + (tipos.size() == 1 ? " tipo" : " tipos"));
        colTipoValor.setFooter(EstiloConsulta.moneda(totalPerdida));

        List<BigDecimal> acumulados = AnalisisMermaUtil.acumulado(
                articulos.stream().map(ResumenMermaDTO::valor).toList());
        List<FilaArticulo> filas = new ArrayList<>(articulos.size());
        for (int i = 0; i < articulos.size(); i++) {
            ResumenMermaDTO r = articulos.get(i);
            filas.add(new FilaArticulo(r, AnalisisMermaUtil.porcentaje(r.valor(), totalPerdida), acumulados.get(i)));
        }
        gridArticulo.setItems(filas);
        colArtNombre.setFooter(filas.size() + (filas.size() == 1 ? " artículo" : " artículos"));
        colArtValor.setFooter(EstiloConsulta.moneda(totalPerdida));

        actualizarResumen(tipos, mermas, desde, hasta);
    }

    private void actualizarResumen(List<ResumenMermaDTO> tipos, long mermas, LocalDate desde, LocalDate hasta) {

        resumen.removeAll();

        resumen.add(tarjeta(VaadinIcon.TRASH, "Pérdida por merma", EstiloConsulta.moneda(totalPerdida),
                mermas + (mermas == 1 ? " merma" : " mermas") + " en el período", "fv-tarjeta-pendiente", null));

        // % sobre ventas: las ventas no se separan por almacén, así que con un
        // almacén elegido se compara su merma contra todas las ventas
        BigDecimal ventas = mermaService.ventasNetas(desde, hasta);
        String detalleVentas = ventas.signum() > 0
                ? "de " + EstiloConsulta.moneda(ventas) + " vendidos" + (almacen() != null ? " (todas las ventas)" : "")
                : "sin ventas en el período";
        resumen.add(tarjeta(VaadinIcon.CHART_LINE, "Merma sobre ventas",
                PORCENTAJE.format(AnalisisMermaUtil.porcentaje(totalPerdida, ventas)), detalleVentas,
                "fv-tarjeta-devuelto", null));

        if (!tipos.isEmpty()) {
            ResumenMermaDTO mayor = tipos.get(0);
            resumen.add(tarjeta(VaadinIcon.EXCLAMATION_CIRCLE, "Mayor causa", mayor.nombre(),
                    EstiloConsulta.moneda(mayor.valor()) + " · "
                    + PORCENTAJE.format(AnalisisMermaUtil.porcentaje(mayor.valor(), totalPerdida)) + " de la pérdida",
                    "fv-tarjeta-total", null));
        }

        resumen.add(tarjetaEnRiesgo());
    }

    /** Lotes vencidos o que vencen en los próximos días: la merma que viene si no se venden. */
    private Div tarjetaEnRiesgo() {

        LocalDate hoy = LocalDate.now();
        Integer almacen = almacen();
        int vencidos = 0;
        int criticos = 0;
        BigDecimal valor = BigDecimal.ZERO;
        for (LoteAlmacen la : loteService.getLotesConExistencia()) {
            if (almacen != null && (la.getAlmacen() == null || !almacen.equals(la.getAlmacen().getCodigo()))) {
                continue;
            }
            LotesVencimientosView.Estado e = LotesVencimientosView.estado(la, hoy);
            if (e == LotesVencimientosView.Estado.VENCIDO) {
                vencidos++;
            } else if (e == LotesVencimientosView.Estado.CRITICO) {
                criticos++;
            } else {
                continue;
            }
            valor = valor.add(LotesVencimientosView.valor(la));
        }

        String detalle = vencidos + (vencidos == 1 ? " lote vencido" : " lotes vencidos") + " · "
                + criticos + " vence" + (criticos == 1 ? "" : "n") + " en ≤ "
                + LotesVencimientosView.DIAS_CRITICO + " días";
        Div d = tarjeta(VaadinIcon.CALENDAR_CLOCK, "En riesgo de merma", EstiloConsulta.moneda(valor), detalle,
                vencidos + criticos > 0 ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada",
                "Clic para ver esos lotes y venderlos o pasarlos a merma");
        d.addClickListener(e -> UI.getCurrent().navigate(LotesVencimientosView.class,
                QueryParameters.simple(Map.of("estado", "POR_ATENDER"))));
        return d;
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGridTipo() {

        EstiloConsulta.aplicarGrid(gridTipo);
        gridTipo.setSizeFull();
        colTipoNombre = gridTipo.addColumn(ResumenMermaDTO::nombre).setHeader("Tipo").setFlexGrow(1);
        EstiloConsulta.monto(gridTipo.addColumn(ResumenMermaDTO::mermas).setHeader("Mermas").setAutoWidth(true));
        colTipoValor = EstiloConsulta.monto(gridTipo.addColumn(r -> EstiloConsulta.moneda(r.valor()))
                .setHeader("Pérdida").setAutoWidth(true));
        EstiloConsulta.monto(gridTipo.addColumn(r -> PORCENTAJE.format(
                AnalisisMermaUtil.porcentaje(r.valor(), totalPerdida))).setHeader("% del total").setAutoWidth(true));
    }

    private void configurarGridArticulo() {

        EstiloConsulta.aplicarGrid(gridArticulo);
        gridArticulo.setSizeFull();
        colArtNombre = gridArticulo.addColumn(f -> f.resumen().nombre()).setHeader("Artículo").setFlexGrow(1);
        EstiloConsulta.monto(gridArticulo.addColumn(f -> f.resumen().mermas()).setHeader("Veces").setAutoWidth(true));
        EstiloConsulta.monto(gridArticulo.addColumn(f -> CANTIDAD.format(f.resumen().cantidadBase()))
                .setHeader("Unid. base").setAutoWidth(true));
        colArtValor = EstiloConsulta.monto(gridArticulo.addColumn(f -> EstiloConsulta.moneda(f.resumen().valor()))
                .setHeader("Pérdida").setAutoWidth(true));
        EstiloConsulta.monto(gridArticulo.addColumn(f -> PORCENTAJE.format(f.porcentaje()))
                .setHeader("% del total").setAutoWidth(true));
        gridArticulo.addComponentColumn(this::acumulado).setHeader("Acumulado").setAutoWidth(true)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
    }

    /** Los artículos que entran en el 80 % de la pérdida se marcan: son los primeros a vigilar. */
    private Span acumulado(FilaArticulo f) {
        String texto = PORCENTAJE.format(f.acumulado());
        return entraEnPareto(f) ? EstiloConsulta.chip(texto, "fv-chip-devuelto") : new Span(texto);
    }

    /** Entra si el acumulado anterior a él aún no llegaba al 80 % (así el primero siempre entra). */
    private static boolean entraEnPareto(FilaArticulo f) {
        return f.acumulado().subtract(f.porcentaje()).compareTo(PARETO) < 0;
    }

    // ============================================================
    // AUXILIARES
    // ============================================================
    private static VerticalLayout panel(String titulo, Grid<?> grid) {
        H4 h = new H4(titulo);
        h.getStyle().set("margin", "0");
        VerticalLayout v = new VerticalLayout(h, grid);
        v.setPadding(false);
        v.setSpacing(false);
        v.setHeightFull();
        v.setFlexGrow(1, grid);
        return v;
    }

    private static Div tarjeta(VaadinIcon icono, String etiqueta, String valor, String detalle, String clase,
            String ayuda) {
        Icon ic = icono.create();
        ic.addClassName("fv-tarjeta-icono");
        Span lbl = new Span(etiqueta);
        lbl.addClassName("fv-tarjeta-etiqueta");
        Span val = new Span(valor);
        val.addClassName("fv-tarjeta-valor");
        Span det = new Span(detalle);
        det.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");
        Div d = new Div(ic, lbl, val, det);
        d.addClassNames("fv-tarjeta", clase);
        if (ayuda != null) {
            d.getStyle().set("cursor", "pointer");
            d.getElement().setAttribute("title", ayuda);
        }
        return d;
    }
}
