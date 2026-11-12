package com.maxsoft.application.view.venta.cajaChica;

import com.maxsoft.application.dto.ResumenCajaDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.MovimientosCaja;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.util.ImpresionHtmlUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Caja Diaria: consulta de los turnos de caja (el actual y los pasados), su
 * resumen de efectivo y todos sus movimientos. Solo consulta: los movimientos
 * no se editan ni se borran.
 */
@PageTitle("Caja Diaria")
@Route(value = "venta/cajaDiaria")
public class CajaDiariaView extends VerticalLayout {

    private static final DateTimeFormatter F_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.of("es", "DO"));
    private static final DateTimeFormatter F_HORA = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.of("es", "DO"));
    private static final String TODOS = "Todos los tipos";

    private final CajaService cajaService;

    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final Grid<CajaTurno> gridTurnos = new Grid<>(CajaTurno.class, false);
    private final Grid<FilaMovimiento> gridMovimientos = new Grid<>(FilaMovimiento.class, false);
    private final ComboBox<String> cbTipo = new ComboBox<>();
    private final TextField txtBuscar = new TextField();
    private final Button btnReimprimir = new Button("Reimprimir comprobante", VaadinIcon.PRINT.create());

    private final TabSheet tabs = new TabSheet();
    private Tab tabTurnos;
    private Tab tabDetalle;

    private final Div billetes = new Div();
    private final Div panelDetalle = new Div();
    private final H3 tituloDetalle = new H3();
    private final Span estadoDetalle = new Span();

    private List<FilaMovimiento> filas = new ArrayList<>();
    private CajaTurno turnoSeleccionado;

    public CajaDiariaView(CajaService cajaService) {
        this.cajaService = cajaService;

        addClassName("caja-diaria");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Igual que Facturas de Venta: lista en una pestaña, detalle en otra (doble clic)
        tabs.addClassName("cd-tabs");
        tabs.setSizeFull();
        tabTurnos = tabs.add(etiquetaTab(VaadinIcon.LIST, "Turnos de caja"), crearGridTurnos());
        tabDetalle = tabs.add(etiquetaTab(VaadinIcon.MONEY, "Detalle del turno"), crearDetalle());

        add(crearEncabezado(), crearFiltros(), tabs);
        setFlexGrow(1, tabs);

        dpDesde.setValue(LocalDate.now().minusDays(6));
        dpHasta.setValue(LocalDate.now());
        buscar();
        mostrarTurno(null);
    }

    private Component etiquetaTab(VaadinIcon icono, String texto) {
        HorizontalLayout h = new HorizontalLayout(icono.create(), new Span(texto));
        h.setSpacing(true);
        h.setAlignItems(FlexComponent.Alignment.CENTER);
        return h;
    }

    // ============================================================
    // ENCABEZADO Y FILTROS
    // ============================================================
    private Component crearEncabezado() {
        Div logo = new Div(new Span("RD$"));
        logo.addClassName("cd-logo");

        H2 titulo = new H2("Caja Diaria");
        titulo.addClassName("cd-titulo");
        Span sub = new Span("Turnos de caja, efectivo en gaveta y movimientos del día y de días anteriores");
        sub.addClassName("cd-subtitulo");

        Div textos = new Div(titulo, sub);
        HorizontalLayout h = new HorizontalLayout(logo, textos);
        h.setAlignItems(FlexComponent.Alignment.CENTER);
        h.addClassName("cd-encabezado");
        h.setWidthFull();
        return h;
    }

    private Component crearFiltros() {
        dpDesde.setWidth("160px");
        dpHasta.setWidth("160px");

        Button btnHoy = new Button("Hoy", VaadinIcon.CALENDAR.create(), e -> {
            dpDesde.setValue(LocalDate.now());
            dpHasta.setValue(LocalDate.now());
            buscar();
        });
        Button btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClassName("cd-btn-dinero");
        Button btnRefrescar = new Button("Refrescar", VaadinIcon.REFRESH.create(), e -> refrescar());

        Span ayuda = new Span("Doble clic en un turno para ver su detalle y movimientos");
        ayuda.addClassName("cd-ayuda");

        HorizontalLayout h = new HorizontalLayout(dpDesde, dpHasta, btnHoy, btnBuscar, btnRefrescar, ayuda);
        h.setAlignItems(FlexComponent.Alignment.BASELINE);
        h.addClassName("cd-filtros");
        h.setWidthFull();
        return h;
    }

    // ============================================================
    // TURNOS
    // ============================================================
    private Component crearGridTurnos() {
        gridTurnos.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        gridTurnos.addClassName("cd-grid");
        // Ocupa toda la pestaña
        gridTurnos.setSizeFull();
        gridTurnos.setMinHeight("360px");

        gridTurnos.addColumn(t -> "#" + t.getId()).setHeader("Turno").setWidth("80px").setFlexGrow(0);
        gridTurnos.addComponentColumn(t -> {
            Span s = new Span(esAbierta(t) ? "ABIERTA" : "CERRADA");
            s.addClassName(esAbierta(t) ? "cd-chip-abierta" : "cd-chip-cerrada");
            return s;
        }).setHeader("Estado").setWidth("110px").setFlexGrow(0);
        gridTurnos.addColumn(t -> t.getFechaApertura() != null ? t.getFechaApertura().format(F_FECHA_HORA) : "")
                .setHeader("Apertura").setAutoWidth(true);
        gridTurnos.addColumn(t -> t.getFechaCierre() != null ? t.getFechaCierre().format(F_FECHA_HORA) : "—")
                .setHeader("Cierre").setAutoWidth(true);
        gridTurnos.addColumn(t -> nvl(t.getCajeroApertura())
                + (t.getCajeroCierre() != null && !t.getCajeroCierre().equals(t.getCajeroApertura()) ? " / " + t.getCajeroCierre() : ""))
                .setHeader("Cajero").setAutoWidth(true);
        gridTurnos.addColumn(t -> rd(t.getMontoApertura())).setHeader("Fondo inicial")
                .setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        gridTurnos.addColumn(t -> esAbierta(t) ? rd(efectivoEsperado(t)) : rd(t.getVentasEfectivoEsperadas()))
                .setHeader("Efectivo esperado").setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        gridTurnos.addColumn(t -> esAbierta(t) ? "—" : rd(t.getMontoCierreReal()))
                .setHeader("Contado").setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        gridTurnos.addComponentColumn(t -> chipDiferencia(esAbierta(t) ? null : t.getDiferencia()))
                .setHeader("Diferencia").setTextAlign(ColumnTextAlign.END).setAutoWidth(true);

        gridTurnos.setPartNameGenerator(t -> esAbierta(t) ? "cd-fila-abierta" : null);
        gridTurnos.addItemDoubleClickListener(e -> {
            if (e.getItem() == null) {
                return;
            }
            mostrarTurno(e.getItem());
            tabs.setSelectedTab(tabDetalle);
        });
        return gridTurnos;
    }

    private void buscar() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        if (desde == null || hasta == null) {
            aviso("Indique el rango de fechas.", NotificationVariant.LUMO_WARNING);
            return;
        }
        List<CajaTurno> turnos = new ArrayList<>(cajaService.getTurnos(desde, hasta));

        // El turno abierto siempre va primero, aunque se haya abierto antes del rango
        cajaService.obtenerCajaAbierta().ifPresent(abierta -> {
            turnos.removeIf(t -> Objects.equals(t.getId(), abierta.getId()));
            turnos.add(0, abierta);
        });

        gridTurnos.setItems(turnos);
    }

    /**
     * Recarga la lista y el detalle abierto (el turno en curso cambia con cada
     * venta), sin cambiar de pestaña.
     */
    private void refrescar() {
        buscar();
        if (turnoSeleccionado != null) {
            Long id = turnoSeleccionado.getId();
            CajaTurno actual = gridTurnos.getListDataView().getItems()
                    .filter(t -> Objects.equals(t.getId(), id))
                    .findFirst().orElse(turnoSeleccionado);
            mostrarTurno(actual);
        }
        aviso("Caja actualizada.", NotificationVariant.LUMO_SUCCESS);
    }

    // ============================================================
    // DETALLE DEL TURNO
    // ============================================================
    private Component crearDetalle() {
        tituloDetalle.addClassName("cd-detalle-titulo");
        Button btnVolver = new Button("Volver a turnos", VaadinIcon.ARROW_LEFT.create(),
                e -> tabs.setSelectedTab(tabTurnos));
        btnVolver.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout cab = new HorizontalLayout(btnVolver, tituloDetalle, estadoDetalle);
        cab.setAlignItems(FlexComponent.Alignment.CENTER);
        cab.getStyle().set("flex-wrap", "wrap");

        billetes.addClassName("cd-billetes");

        cbTipo.setPlaceholder("Tipo de movimiento");
        cbTipo.setWidth("240px");
        cbTipo.addValueChangeListener(e -> filtrarMovimientos());
        txtBuscar.setPlaceholder("Buscar en descripción o usuario...");
        txtBuscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setWidth("280px");
        txtBuscar.addValueChangeListener(e -> filtrarMovimientos());

        btnReimprimir.setEnabled(false);
        btnReimprimir.addClassName("cd-btn-dinero");
        btnReimprimir.addClickListener(e -> reimprimir());

        HorizontalLayout barra = new HorizontalLayout(cbTipo, txtBuscar, btnReimprimir);
        barra.setAlignItems(FlexComponent.Alignment.BASELINE);
        barra.setWidthFull();
        barra.addClassName("cd-barra-mov");

        crearGridMovimientos();

        panelDetalle.add(cab, billetes, barra, gridMovimientos);
        panelDetalle.addClassName("cd-detalle");
        panelDetalle.setSizeFull();
        return panelDetalle;
    }

    private void crearGridMovimientos() {
        gridMovimientos.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        gridMovimientos.addClassName("cd-grid");
        gridMovimientos.setWidthFull();
        gridMovimientos.setMinHeight("380px");
        gridMovimientos.setHeight("60vh");

        gridMovimientos.addColumn(f -> f.mov.getFechaHora() != null ? f.mov.getFechaHora().format(F_HORA) : "")
                .setHeader("Hora").setWidth("110px").setFlexGrow(0);
        gridMovimientos.addComponentColumn(f -> {
            Span s = new Span(CajaService.nombreTipo(f.mov.getTipo()));
            s.addClassName("cd-tipo");
            s.addClassName(f.efecto.signum() > 0 ? "cd-tipo-entra"
                    : f.efecto.signum() < 0 ? "cd-tipo-sale" : "cd-tipo-neutro");
            return s;
        }).setHeader("Tipo").setAutoWidth(true);
        gridMovimientos.addColumn(f -> nvl(f.mov.getDescripcion())).setHeader("Descripción").setFlexGrow(1);
        gridMovimientos.addColumn(f -> nvl(f.mov.getUsuario())).setHeader("Usuario").setAutoWidth(true);
        gridMovimientos.addComponentColumn(f -> monto(f.efecto.signum() > 0 ? f.efecto : null, "cd-entra"))
                .setHeader("Entra").setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        gridMovimientos.addComponentColumn(f -> monto(f.efecto.signum() < 0 ? f.efecto.abs() : null, "cd-sale"))
                .setHeader("Sale").setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        gridMovimientos.addComponentColumn(f -> {
            if (f.efecto.signum() == 0) {
                // Tarjeta: no toca la gaveta, se muestra como referencia
                return monto(f.mov.getMonto() != null ? f.mov.getMonto().abs() : null, "cd-ref");
            }
            return new Span();
        }).setHeader("Tarjeta (ref.)").setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        gridMovimientos.addColumn(f -> rd(f.saldo)).setHeader("Efectivo en gaveta")
                .setTextAlign(ColumnTextAlign.END).setAutoWidth(true).setPartNameGenerator(f -> "cd-saldo");

        gridMovimientos.asSingleSelect().addValueChangeListener(e -> btnReimprimir.setEnabled(e.getValue() != null
                && ("POS_" + CajaService.TIPO_FONDO_EXTRA).equals(e.getValue().mov.getTipo())));
    }

    private void mostrarTurno(CajaTurno t) {
        turnoSeleccionado = t;
        billetes.removeAll();
        btnReimprimir.setEnabled(false);

        if (t == null) {
            tituloDetalle.setText("Haga doble clic en un turno de la pestaña \"Turnos de caja\" para ver su detalle");
            estadoDetalle.setText("");
            estadoDetalle.setClassName("");
            filas = new ArrayList<>();
            gridMovimientos.setItems(filas);
            cbTipo.setItems(List.of(TODOS));
            return;
        }

        boolean abierta = esAbierta(t);
        tabDetalle.removeAll();
        tabDetalle.add(etiquetaTab(VaadinIcon.MONEY, "Detalle del turno #" + t.getId()));
        tituloDetalle.setText("Turno #" + t.getId() + " · "
                + (t.getFechaApertura() != null ? t.getFechaApertura().format(F_FECHA_HORA) : ""));
        estadoDetalle.setText(abierta ? "ABIERTA · en curso" : "CERRADA");
        estadoDetalle.setClassName(abierta ? "cd-chip-abierta" : "cd-chip-cerrada");

        ResumenCajaDTO r = cajaService.obtenerResumenCaja(t.getId().intValue());

        // Billete principal: el efectivo que debe haber en la gaveta
        billetes.add(billete(abierta ? "Efectivo en gaveta ahora" : "Efectivo esperado al cierre",
                r.getEfectivoEsperado(), "cd-billete-principal", VaadinIcon.MONEY));
        if (!abierta) {
            billetes.add(billete("Efectivo contado", t.getMontoCierreReal(), "cd-billete-contado", VaadinIcon.WALLET));
            BigDecimal dif = t.getDiferencia() != null ? t.getDiferencia() : BigDecimal.ZERO;
            billetes.add(billete(dif.signum() > 0 ? "Sobrante" : dif.signum() < 0 ? "Faltante" : "Cuadre exacto",
                    dif, dif.signum() < 0 ? "cd-billete-faltante" : "cd-billete-sobrante", VaadinIcon.SCALE));
        }
        billetes.add(
                billete("Fondo inicial", r.getFondoInicial(), "cd-billete", VaadinIcon.PIGGY_BANK),
                billete("Fondo extra", r.getFondoExtra(), "cd-billete", VaadinIcon.PLUS_CIRCLE),
                billete("Ventas en efectivo (netas)", r.getVentasEfectivo(), "cd-billete", VaadinIcon.CART),
                billete("Cobros de delivery", r.getCobrosDelivery(), "cd-billete", VaadinIcon.TRUCK),
                billete("Abonos de clientes (CxC)", r.getCobrosCxc(), "cd-billete", VaadinIcon.USER_CHECK),
                billete("Pagos a proveedores", r.getPagosProveedores(), "cd-billete", VaadinIcon.PACKAGE),
                billete("Avances con tarjeta", r.getAvancesEfectivo(), "cd-billete", VaadinIcon.EXCHANGE),
                billete("Otros movimientos", r.getOtrosMovimientos(), "cd-billete", VaadinIcon.ELLIPSIS_DOTS_H),
                billete("Ventas con tarjeta (ref.)", r.getVentasTarjeta(), "cd-billete cd-billete-tarjeta", VaadinIcon.CREDIT_CARD));

        // Movimientos con el efectivo acumulado de la gaveta después de cada uno
        filas = new ArrayList<>();
        BigDecimal saldo = r.getFondoInicial() != null ? r.getFondoInicial() : BigDecimal.ZERO;
        for (MovimientosCaja m : cajaService.getMovimientos(t.getId())) {
            BigDecimal efecto = CajaService.efectoEnGaveta(m);
            saldo = saldo.add(efecto);
            filas.add(new FilaMovimiento(m, efecto, saldo));
        }

        List<String> tipos = new ArrayList<>();
        tipos.add(TODOS);
        filas.stream().map(f -> CajaService.nombreTipo(f.mov.getTipo())).distinct().sorted().forEach(tipos::add);
        cbTipo.setItems(tipos);
        cbTipo.setValue(TODOS);
        filtrarMovimientos();
    }

    private void filtrarMovimientos() {
        String tipo = cbTipo.getValue();
        String texto = txtBuscar.getValue() != null ? txtBuscar.getValue().trim().toLowerCase() : "";
        gridMovimientos.setItems(filas.stream()
                .filter(f -> tipo == null || TODOS.equals(tipo) || tipo.equals(CajaService.nombreTipo(f.mov.getTipo())))
                .filter(f -> texto.isEmpty()
                || nvl(f.mov.getDescripcion()).toLowerCase().contains(texto)
                || nvl(f.mov.getUsuario()).toLowerCase().contains(texto))
                .toList());
    }

    private void reimprimir() {
        FilaMovimiento f = gridMovimientos.asSingleSelect().getValue();
        if (f == null) {
            return;
        }
        try {
            ImpresionHtmlUtil.imprimir(getUI().orElse(null), cajaService.generarComprobanteFondoExtraHtml(f.mov.getId()));
        } catch (Exception ex) {
            aviso("No se pudo imprimir el comprobante: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    // ============================================================
    // COMPONENTES Y UTILIDADES
    // ============================================================
    private Div billete(String etiqueta, BigDecimal valor, String clases, VaadinIcon icono) {
        Icon ic = icono.create();
        ic.addClassName("cd-billete-icono");
        Span lbl = new Span(etiqueta);
        lbl.addClassName("cd-billete-etiqueta");
        Span val = new Span(rd(valor));
        val.addClassName("cd-billete-valor");
        if (valor != null && valor.signum() < 0) {
            val.addClassName("cd-negativo");
        }
        Div sello = new Div(new Span("RD$"));
        sello.addClassName("cd-billete-sello");

        Div b = new Div(sello, ic, lbl, val);
        b.addClassName("cd-billete");
        for (String c : clases.split(" ")) {
            b.addClassName(c);
        }
        return b;
    }

    private Span monto(BigDecimal v, String clase) {
        Span s = new Span(v != null ? rd(v) : "");
        s.addClassName(clase);
        return s;
    }

    private Span chipDiferencia(BigDecimal dif) {
        if (dif == null) {
            return new Span("—");
        }
        Span s = new Span((dif.signum() > 0 ? "+" : "") + rd(dif));
        s.addClassName(dif.signum() < 0 ? "cd-sale" : dif.signum() > 0 ? "cd-entra" : "cd-ref");
        return s;
    }

    private BigDecimal efectivoEsperado(CajaTurno t) {
        try {
            return cajaService.obtenerResumenCaja(t.getId().intValue()).getEfectivoEsperado();
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean esAbierta(CajaTurno t) {
        return t.getEstado() != null && "ABIERTA".equalsIgnoreCase(t.getEstado().trim());
    }

    private static String rd(BigDecimal v) {
        if (v == null) {
            return "RD$ 0.00";
        }
        return (v.signum() < 0 ? "-" : "") + "RD$ " + String.format(Locale.US, "%,.2f", v.abs());
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private void aviso(String msg, NotificationVariant v) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER).addThemeVariants(v);
    }

    /** Fila de la grilla: el movimiento, su efecto en la gaveta y el efectivo acumulado. */
    public static class FilaMovimiento {

        final MovimientosCaja mov;
        final BigDecimal efecto;
        final BigDecimal saldo;

        FilaMovimiento(MovimientosCaja mov, BigDecimal efecto, BigDecimal saldo) {
            this.mov = mov;
            this.efecto = efecto;
            this.saldo = saldo;
        }
    }
}
