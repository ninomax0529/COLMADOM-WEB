package com.maxsoft.application.view.inventario.lote;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.inventario.merma.RegistroMermaView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Consulta de lotes con existencia y su vencimiento (semáforo), con el kardex
 * de cada lote, el paso de los vencidos a merma y la verificación del cuadre
 * entre la existencia del artículo y la suma de sus lotes.
 */
@PermitAll
@PageTitle("Lotes y Vencimientos | MaxSoft ERP")
@Route(value = "inventario/lotes")
public class LotesVencimientosView extends VerticalLayout implements BeforeEnterObserver {

    /** Días para considerar que un lote "vence pronto" (crítico) y "próximo a vencer". */
    public static final int DIAS_CRITICO = 7;
    public static final int DIAS_PROXIMO = 30;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private static final DecimalFormat CANTIDAD = new DecimalFormat("#,##0.##");

    /** Estado de un lote según su vencimiento. */
    public enum Estado {
        VENCIDO("VENCIDO", "fv-chip-anulada"),
        CRITICO("VENCE ≤ " + DIAS_CRITICO + " DÍAS", "fv-chip-devuelto"),
        PROXIMO("VENCE ≤ " + DIAS_PROXIMO + " DÍAS", "fv-chip-credito"),
        VIGENTE("VIGENTE", "fv-chip-pagada"),
        SIN_VENCIMIENTO("SIN VENCIMIENTO", "fv-chip-contado"),
        NEGATIVO("EN NEGATIVO", "fv-chip-anulada");

        private final String etiqueta;
        private final String clase;

        Estado(String etiqueta, String clase) {
            this.etiqueta = etiqueta;
            this.clase = clase;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        /** Clase CSS del chip (fv-chip-*). */
        public String getClase() {
            return clase;
        }
    }

    /** Opción del filtro de estado. */
    private enum FiltroEstado {
        TODOS("Todos"),
        POR_ATENDER("Vencidos y vencen ≤ " + DIAS_CRITICO + " días"),
        VENCIDO("Vencidos"),
        CRITICO("Vencen ≤ " + DIAS_CRITICO + " días"),
        PROXIMO("Vencen ≤ " + DIAS_PROXIMO + " días"),
        VIGENTE("Vigentes"),
        SIN_VENCIMIENTO("Sin vencimiento"),
        NEGATIVO("En negativo");

        private final String texto;

        FiltroEstado(String texto) {
            this.texto = texto;
        }
    }

    private final LoteService loteService;
    private final MovimientoInventarioService movimientoService;
    private final ArticuloService articuloService;

    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>();
    private final ComboBox<FiltroEstado> cbEstado = new ComboBox<>();
    private final TextField txtBuscar = new TextField();
    private final Div resumen = new Div();
    private final Grid<LoteAlmacen> grid = new Grid<>(LoteAlmacen.class, false);
    private final List<LoteAlmacen> lista = new ArrayList<>();
    private GridListDataView<LoteAlmacen> dataView;
    private LocalDate hoy = LocalDate.now();

    public LotesVencimientosView(LoteService loteService, MovimientoInventarioService movimientoService,
            ArticuloService articuloService, AlmacenService almacenService) {

        this.loteService = loteService;
        this.movimientoService = movimientoService;
        this.articuloService = articuloService;

        setSizeFull();
        setSpacing(false);
        EstiloConsulta.aplicarVista(this, null);

        cbAlmacen.setItems(almacenService.getLista());
        configurarGrid();

        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");

        H2 titulo = new H2("Lotes y Vencimientos");
        titulo.addClassName("fv-titulo");

        add(titulo, crearFiltros(), resumen, grid);
        setFlexGrow(1, grid);

        cargar();
    }

    /** ?estado=VENCIDO (o cualquier FiltroEstado) desde la alerta del módulo. */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<String> estado = event.getLocation().getQueryParameters().getParameters().get("estado");
        if (estado != null && !estado.isEmpty()) {
            try {
                cbEstado.setValue(FiltroEstado.valueOf(estado.get(0)));
            } catch (IllegalArgumentException ignorado) {
                // parámetro desconocido: se queda en Todos
            }
        }
    }

    // ============================================================
    // ESTADO DE UN LOTE
    // ============================================================
    public static Estado estado(LoteAlmacen la, LocalDate hoy) {
        if (la.getExistencia().signum() < 0) {
            return Estado.NEGATIVO;
        }
        LocalDate venc = la.getLote().getFechaVencimiento();
        if (venc == null) {
            return Estado.SIN_VENCIMIENTO;
        }
        long dias = ChronoUnit.DAYS.between(hoy, venc);
        if (dias < 0) {
            return Estado.VENCIDO;
        }
        if (dias <= DIAS_CRITICO) {
            return Estado.CRITICO;
        }
        if (dias <= DIAS_PROXIMO) {
            return Estado.PROXIMO;
        }
        return Estado.VIGENTE;
    }

    /** Valor al costo del lote (existencia base x costo unitario base del lote). */
    public static BigDecimal valor(LoteAlmacen la) {
        BigDecimal costo = la.getLote().getCostoUnitario();
        if (costo == null || la.getExistencia().signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return costo.multiply(la.getExistencia()).setScale(2, RoundingMode.HALF_UP);
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private HorizontalLayout crearFiltros() {

        cbAlmacen.setPlaceholder("Todos los almacenes");
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setClearButtonVisible(true);
        cbAlmacen.setWidth("200px");
        cbAlmacen.addValueChangeListener(e -> {
            aplicarFiltros();
            actualizarResumen();
        });

        cbEstado.setItems(FiltroEstado.values());
        cbEstado.setItemLabelGenerator(f -> f.texto);
        cbEstado.setValue(FiltroEstado.TODOS);
        cbEstado.setWidth("240px");
        cbEstado.addValueChangeListener(e -> aplicarFiltros());

        txtBuscar.setWidth("240px");
        txtBuscar.setPlaceholder("Buscar artículo o lote...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltros());

        Button btnRefrescar = new Button("Refrescar", VaadinIcon.REFRESH.create(), e -> cargar());

        Button btnMerma = new Button("Vencidos a merma", VaadinIcon.TRASH.create(), e -> mandarVencidosAMerma());
        btnMerma.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnMerma.setTooltipText("Abre el registro de merma con los lotes vencidos del almacén");

        Button btnCuadre = new Button("Verificar cuadre", VaadinIcon.SCALE.create(), e -> verificarCuadre());
        btnCuadre.setTooltipText("Compara la existencia de cada artículo con la suma de sus lotes");

        HorizontalLayout barra = new HorizontalLayout(cbAlmacen, cbEstado, txtBuscar, btnRefrescar, btnMerma,
                btnCuadre, EstiloConsulta.ayuda("Doble clic en un lote para ver su kardex"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void aplicarFiltros() {
        Almacen almacen = cbAlmacen.getValue();
        FiltroEstado filtro = cbEstado.getValue() != null ? cbEstado.getValue() : FiltroEstado.TODOS;
        String texto = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toUpperCase();

        dataView.setFilter(la -> {
            if (almacen != null && !Objects.equals(almacen.getCodigo(), la.getAlmacen().getCodigo())) {
                return false;
            }
            Estado e = estado(la, hoy);
            boolean pasaEstado = switch (filtro) {
                case TODOS -> true;
                case POR_ATENDER -> e == Estado.VENCIDO || e == Estado.CRITICO;
                case VENCIDO -> e == Estado.VENCIDO;
                case CRITICO -> e == Estado.CRITICO;
                case PROXIMO -> e == Estado.CRITICO || e == Estado.PROXIMO;
                case VIGENTE -> e == Estado.VIGENTE;
                case SIN_VENCIMIENTO -> e == Estado.SIN_VENCIMIENTO;
                case NEGATIVO -> e == Estado.NEGATIVO;
            };
            if (!pasaEstado) {
                return false;
            }
            if (texto.isEmpty()) {
                return true;
            }
            Articulo a = la.getLote().getArticulo();
            return contiene(a.getDescripcion(), texto) || contiene(a.getCodigoDeBarra(), texto)
                    || contiene(la.getLote().getNumeroLote(), texto)
                    || contiene(String.valueOf(a.getCodigo()), texto);
        });
    }

    private static boolean contiene(String valor, String texto) {
        return valor != null && valor.toUpperCase().contains(texto);
    }

    // ============================================================
    // GRID
    // ============================================================
    private void configurarGrid() {

        EstiloConsulta.aplicarGrid(grid);
        grid.setSizeFull();
        dataView = grid.setItems(lista);

        grid.addComponentColumn(la -> {
            Estado e = estado(la, hoy);
            return EstiloConsulta.chip(e.etiqueta, e.clase);
        }).setHeader("Estado").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(la -> estado(la, hoy)));
        grid.addColumn(la -> la.getLote().getArticulo().getDescripcion()).setHeader("Artículo")
                .setAutoWidth(true).setFlexGrow(2).setSortable(true);
        grid.addColumn(la -> la.getLote().getNumeroLote()).setHeader("Lote").setAutoWidth(true).setSortable(true);
        grid.addColumn(la -> fecha(la.getLote().getFechaVencimiento())).setHeader("Vence").setAutoWidth(true)
                .setComparator(Comparator.comparing(la -> la.getLote().getFechaVencimiento(),
                        Comparator.nullsLast(Comparator.naturalOrder())));
        grid.addColumn(la -> textoDias(la.getLote().getFechaVencimiento())).setHeader("Días").setAutoWidth(true)
                .setComparator(Comparator.comparing(la -> la.getLote().getFechaVencimiento(),
                        Comparator.nullsLast(Comparator.naturalOrder())));
        grid.addColumn(la -> la.getAlmacen().getNombre()).setHeader("Almacén").setAutoWidth(true).setSortable(true);
        EstiloConsulta.monto(grid.addColumn(la -> CANTIDAD.format(la.getExistencia()) + " "
                + texto(la.getLote().getArticulo().getNombreUnidadBase())))
                .setHeader("Existencia").setAutoWidth(true)
                .setComparator(Comparator.comparing(LoteAlmacen::getExistencia));
        EstiloConsulta.monto(grid.addColumn(la -> la.getLote().getCostoUnitario() == null ? "—"
                : EstiloConsulta.moneda(la.getLote().getCostoUnitario())))
                .setHeader("Costo unit.").setAutoWidth(true);
        EstiloConsulta.monto(grid.addColumn(la -> EstiloConsulta.moneda(valor(la))))
                .setHeader("Valor").setAutoWidth(true).setComparator(Comparator.comparing(LotesVencimientosView::valor));
        grid.addColumn(la -> la.getLote().getProveedor() != null ? texto(la.getLote().getProveedor().getNombre()) : "")
                .setHeader("Proveedor").setAutoWidth(true);
        grid.addColumn(la -> texto(la.getLote().getDocumentoOrigen())).setHeader("Documento").setAutoWidth(true);

        grid.addComponentColumn(la -> {
            Button ver = new Button(VaadinIcon.LINES_LIST.create(), e -> abrirKardex(la));
            ver.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            ver.setTooltipText("Kardex del lote");
            return ver;
        }).setHeader("Kardex").setAutoWidth(true).setFlexGrow(0);

        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() != null) {
                abrirKardex(e.getItem());
            }
        });
    }

    private void cargar() {
        hoy = LocalDate.now();
        lista.clear();
        lista.addAll(loteService.getLotesConExistencia());
        dataView.refreshAll();
        aplicarFiltros();
        actualizarResumen();
    }

    // ============================================================
    // TARJETAS
    // ============================================================
    private void actualizarResumen() {
        Almacen almacen = cbAlmacen.getValue();
        List<LoteAlmacen> base = lista.stream()
                .filter(la -> almacen == null || Objects.equals(almacen.getCodigo(), la.getAlmacen().getCodigo()))
                .toList();

        resumen.removeAll();
        resumen.add(
                tarjeta("Vencidos", base, Set.of(Estado.VENCIDO), "fv-tarjeta-pendiente",
                        VaadinIcon.EXCLAMATION_CIRCLE, FiltroEstado.VENCIDO),
                tarjeta("Vencen en " + DIAS_CRITICO + " días", base, Set.of(Estado.CRITICO), "fv-tarjeta-devuelto",
                        VaadinIcon.WARNING, FiltroEstado.CRITICO),
                tarjeta("Vencen en " + DIAS_PROXIMO + " días", base, Set.of(Estado.CRITICO, Estado.PROXIMO),
                        "fv-tarjeta-total", VaadinIcon.CALENDAR_CLOCK, FiltroEstado.PROXIMO),
                tarjeta("Vigentes", base, Set.of(Estado.VIGENTE, Estado.SIN_VENCIMIENTO), "fv-tarjeta-pagado",
                        VaadinIcon.CHECK_CIRCLE, FiltroEstado.VIGENTE));
    }

    /** Tarjeta con la cantidad de lotes y su valor al costo; al hacer clic filtra por ese estado. */
    private Div tarjeta(String etiqueta, List<LoteAlmacen> base, Set<Estado> estados, String clase,
            VaadinIcon icono, FiltroEstado filtro) {

        List<LoteAlmacen> del = base.stream().filter(la -> estados.contains(estado(la, hoy))).toList();
        BigDecimal valor = del.stream().map(LotesVencimientosView::valor).reduce(BigDecimal.ZERO, BigDecimal::add);

        Icon ic = icono.create();
        ic.addClassName("fv-tarjeta-icono");
        Span lbl = new Span(etiqueta);
        lbl.addClassName("fv-tarjeta-etiqueta");
        Span val = new Span(del.size() + (del.size() == 1 ? " lote" : " lotes"));
        val.addClassName("fv-tarjeta-valor");
        Span monto = new Span(EstiloConsulta.moneda(valor) + " al costo");
        monto.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");
        Div d = new Div(ic, lbl, val, monto);
        d.addClassNames("fv-tarjeta", clase);
        d.getStyle().set("cursor", "pointer");
        d.getElement().setAttribute("title", "Ver solo estos lotes");
        d.addClickListener(e -> cbEstado.setValue(filtro));
        return d;
    }

    // ============================================================
    // KARDEX DEL LOTE
    // ============================================================
    private void abrirKardex(LoteAlmacen la) {

        Lote lote = la.getLote();
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Kardex del lote " + lote.getNumeroLote() + " — " + lote.getArticulo().getDescripcion());
        dlg.setWidth("1000px");
        dlg.setMaxWidth("95vw");

        Span info = new Span("Vence: " + fecha(lote.getFechaVencimiento())
                + "   ·   Entró: " + (lote.getFechaEntrada() != null ? lote.getFechaEntrada().format(FECHA_HORA) : "—")
                + "   ·   Documento: " + texto(lote.getDocumentoOrigen())
                + "   ·   Cantidades en " + texto(lote.getArticulo().getNombreUnidadBase()));
        info.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");

        Grid<MovimientoInventario> g = new Grid<>(MovimientoInventario.class, false);
        EstiloConsulta.aplicarGrid(g);
        g.setHeight("420px");
        g.addColumn(m -> m.getFechaMovimiento() == null ? ""
                : m.getFechaMovimiento().toInstant().atZone(ZoneId.systemDefault()).format(FECHA_HORA))
                .setHeader("Fecha").setAutoWidth(true);
        g.addColumn(m -> m.getTipoMovimiento() != null ? m.getTipoMovimiento().getNombre() : "")
                .setHeader("Movimiento").setAutoWidth(true);
        g.addColumn(m -> (m.getTipoDocumento() != null ? m.getTipoDocumento().getNombre() + " " : "")
                + texto(m.getNumeroDocumento())).setHeader("Documento").setAutoWidth(true);
        g.addColumn(m -> m.getAlmacen() != null ? m.getAlmacen().getNombre() : "").setHeader("Almacén")
                .setAutoWidth(true);
        EstiloConsulta.monto(g.addColumn(m -> CANTIDAD.format(m.getExistenciaLoteAnterior() == null ? BigDecimal.ZERO
                : m.getExistenciaLoteAnterior()))).setHeader("Anterior").setAutoWidth(true);
        EstiloConsulta.monto(g.addComponentColumn(m -> {
            BigDecimal ant = m.getExistenciaLoteAnterior() == null ? BigDecimal.ZERO : m.getExistenciaLoteAnterior();
            BigDecimal nue = m.getExistenciaLoteNueva() == null ? BigDecimal.ZERO : m.getExistenciaLoteNueva();
            BigDecimal delta = nue.subtract(ant);
            Span s = new Span((delta.signum() > 0 ? "+" : "") + CANTIDAD.format(delta));
            s.getStyle().set("font-weight", "600").set("color", delta.signum() < 0
                    ? "var(--lumo-error-text-color)" : "var(--lumo-success-text-color)");
            return s;
        })).setHeader("Movido").setAutoWidth(true);
        EstiloConsulta.monto(g.addColumn(m -> CANTIDAD.format(m.getExistenciaLoteNueva() == null ? BigDecimal.ZERO
                : m.getExistenciaLoteNueva()))).setHeader("Nueva").setAutoWidth(true);
        g.addColumn(m -> texto(m.getUsuario())).setHeader("Usuario").setAutoWidth(true);
        g.addColumn(m -> texto(m.getObservacion())).setHeader("Observación").setAutoWidth(true).setFlexGrow(1);

        List<MovimientoInventario> movs = movimientoService.getMovimientosPorLote(lote.getCodigo());
        g.setItems(movs);

        VerticalLayout cuerpo = new VerticalLayout(info, g);
        cuerpo.setPadding(false);
        if (movs.isEmpty()) {
            cuerpo.add(EstiloConsulta.ayuda("Este lote no tiene movimientos registrados (p. ej. el lote INICIAL"
                    + " creado al activar el control de lotes)."));
        }
        dlg.add(cuerpo);
        Button cerrar = new Button("Cerrar", e -> dlg.close());
        dlg.getFooter().add(cerrar);
        dlg.open();
    }

    // ============================================================
    // VENCIDOS A MERMA
    // ============================================================
    private void mandarVencidosAMerma() {

        Almacen almacen = cbAlmacen.getValue();
        List<LoteAlmacen> vencidos = lista.stream().filter(la -> estado(la, hoy) == Estado.VENCIDO).toList();
        if (almacen == null) {
            Set<Integer> almacenes = vencidos.stream().map(la -> la.getAlmacen().getCodigo())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (almacenes.isEmpty()) {
                notificar("No hay lotes vencidos con existencia", NotificationVariant.LUMO_CONTRAST);
                return;
            }
            if (almacenes.size() > 1) {
                notificar("Hay vencidos en varios almacenes: seleccione el almacén", NotificationVariant.LUMO_WARNING);
                cbAlmacen.focus();
                return;
            }
            Integer unico = almacenes.iterator().next();
            enviarAMerma(unico);
            return;
        }
        boolean hay = vencidos.stream().anyMatch(la -> Objects.equals(la.getAlmacen().getCodigo(), almacen.getCodigo()));
        if (!hay) {
            notificar("No hay lotes vencidos con existencia en " + almacen.getNombre(), NotificationVariant.LUMO_CONTRAST);
            return;
        }
        enviarAMerma(almacen.getCodigo());
    }

    private void enviarAMerma(Integer codigoAlmacen) {
        UI.getCurrent().navigate(RegistroMermaView.class,
                QueryParameters.simple(Map.of(RegistroMermaView.PARAM_VENCIDOS, String.valueOf(codigoAlmacen))));
    }

    // ============================================================
    // CUADRE ARTÍCULO vs LOTES
    // ============================================================
    private void verificarCuadre() {

        List<Object[]> descuadres = loteService.getDescuadres();
        if (descuadres.isEmpty()) {
            notificar("Todo cuadra: la existencia de cada artículo es igual a la suma de sus lotes",
                    NotificationVariant.LUMO_SUCCESS);
            return;
        }

        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Descuadres entre artículo y lotes");
        dlg.setWidth("760px");
        dlg.setMaxWidth("95vw");

        record Fila(Integer articulo, String descripcion, String almacen, BigDecimal existencia, BigDecimal lotes) {
        }
        Map<Integer, String> nombresAlmacen = cbAlmacen.getListDataView().getItems()
                .collect(Collectors.toMap(Almacen::getCodigo, Almacen::getNombre, (a, b) -> a));
        List<Fila> filas = new ArrayList<>();
        for (Object[] r : descuadres) {
            Integer art = ((Number) r[0]).intValue();
            Integer alm = ((Number) r[1]).intValue();
            Articulo a = articuloService.buscarPorCodigo(art);
            filas.add(new Fila(art, a != null ? a.getDescripcion() : String.valueOf(art),
                    nombresAlmacen.getOrDefault(alm, String.valueOf(alm)),
                    new BigDecimal(r[2].toString()), new BigDecimal(r[3].toString())));
        }

        Grid<Fila> g = new Grid<>();
        EstiloConsulta.aplicarGrid(g);
        g.setHeight("300px");
        g.addColumn(Fila::descripcion).setHeader("Artículo").setAutoWidth(true).setFlexGrow(1);
        g.addColumn(Fila::almacen).setHeader("Almacén").setAutoWidth(true);
        EstiloConsulta.monto(g.addColumn(f -> CANTIDAD.format(f.existencia()))).setHeader("Existencia").setAutoWidth(true);
        EstiloConsulta.monto(g.addColumn(f -> CANTIDAD.format(f.lotes()))).setHeader("Suma lotes").setAutoWidth(true);
        EstiloConsulta.monto(g.addColumn(f -> CANTIDAD.format(f.existencia().subtract(f.lotes()))))
                .setHeader("Diferencia").setAutoWidth(true);
        g.setItems(filas);

        dlg.add(new VerticalLayout(EstiloConsulta.ayuda("Corregir pasa la diferencia al lote INICIAL de cada"
                + " artículo (la existencia del artículo no cambia). También se corrige sola en el próximo"
                + " movimiento del artículo."), g));

        Button corregir = new Button("Corregir", VaadinIcon.WRENCH.create(), e -> {
            int ajustados = 0;
            for (Integer art : filas.stream().map(Fila::articulo).collect(Collectors.toCollection(LinkedHashSet::new))) {
                Articulo a = articuloService.buscarPorCodigo(art);
                if (a != null) {
                    ajustados += loteService.sincronizarLotes(a, "ADMIN");
                }
            }
            dlg.close();
            notificar("Se cuadraron " + ajustados + " almacén(es)", NotificationVariant.LUMO_SUCCESS);
            cargar();
        });
        corregir.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dlg.getFooter().add(new Button("Cerrar", e -> dlg.close()), corregir);
        dlg.open();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private String textoDias(LocalDate venc) {
        if (venc == null) {
            return "—";
        }
        long dias = ChronoUnit.DAYS.between(hoy, venc);
        if (dias < 0) {
            return "hace " + (-dias) + (dias == -1 ? " día" : " días");
        }
        if (dias == 0) {
            return "hoy";
        }
        return dias + (dias == 1 ? " día" : " días");
    }

    private static String fecha(LocalDate f) {
        return f != null ? f.format(FECHA) : "—";
    }

    private static String texto(String s) {
        return s != null ? s : "";
    }

    private static void notificar(String mensaje, NotificationVariant variante) {
        Notification n = Notification.show(mensaje, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(variante);
    }
}
