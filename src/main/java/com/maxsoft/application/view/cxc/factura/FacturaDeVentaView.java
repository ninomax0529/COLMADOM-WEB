/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.cxc.factura;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Consulta de facturas de venta: lista en una pestaña y, con doble clic, su
 * detalle con tarjetas de resumen. Estilos en
 * themes/gestion-colmadom/views/factura-venta-view.css (clases fv-*).
 */
@PageTitle("Factura de Venta")
@Route(value = "cxc/FacturaVenta")
public class FacturaDeVentaView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    /** Tipo de venta a crédito (libreta). */
    private static final int TIPO_VENTA_CREDITO = 2;
    /** Diferencias menores a un centavo se consideran cero. */
    private static final BigDecimal CENTAVO = new BigDecimal("0.005");

    private final FacturaDeVentaService facturaService;
    private final Grid<FacturaDeVenta> grid = new Grid<>(FacturaDeVenta.class, false);
    private Grid<DetalleFacturaDeVenta> gridDetalle = new Grid<>(DetalleFacturaDeVenta.class, false);

    // Filtros de consulta
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final TextField txtBuscar = new TextField();
    private Button btnBuscar;
    private Button btnLimpiar;

    private final TabSheet tabs = new TabSheet();

    // Pestaña de detalle: encabezado + tarjetas de resumen + líneas
    private final Div panelDetalle = new Div();
    private final Div cabeceraDetalle = new Div();
    private final Div tarjetasDetalle = new Div();

    List<DetalleFacturaDeVenta> listDet = new ArrayList<>();

    List<FacturaDeVenta> listaFactura = new ArrayList<>();

    private GridListDataView<FacturaDeVenta> dataViewFacturas;

    // Columnas con pie (totales)
    private Grid.Column<FacturaDeVenta> colCodigo;
    private Grid.Column<FacturaDeVenta> colTotalFactura;
    private Grid.Column<FacturaDeVenta> colTotalPagadoFactura;
    private Grid.Column<FacturaDeVenta> colTotalPendienteFactura;
    private Grid.Column<DetalleFacturaDeVenta> colDescripcion;
    private Grid.Column<DetalleFacturaDeVenta> colEquivalente;
    private Grid.Column<DetalleFacturaDeVenta> colSubTotal;
    private Grid.Column<DetalleFacturaDeVenta> colItbis;
    private Grid.Column<DetalleFacturaDeVenta> colTotal;

    @Autowired
    public FacturaDeVentaView(FacturaDeVentaService entradaServiceArg) {

        addClassName("fv-vista");
        setSizeFull();
        setSpacing(false);

        this.facturaService = entradaServiceArg;
        tabs.setSizeFull();
        tabs.addClassName("fv-tabs");

        configurarFiltros();
        configurarGrid();
        configurarGridDetalle();

        cabeceraDetalle.addClassName("fv-detalle-cabecera");
        tarjetasDetalle.addClassName("fv-tarjetas");
        panelDetalle.add(cabeceraDetalle, tarjetasDetalle, gridDetalle);
        panelDetalle.addClassName("fv-detalle");
        panelDetalle.setSizeFull();

        tabs.add(etiquetaTab(VaadinIcon.LIST, "Facturas"), grid);
        tabs.add(etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle"), panelDetalle);

        grid.addItemDoubleClickListener(c -> {

            if (c.getItem() == null) {
                return;
            }

            cargarDetalle(c.getItem());
            tabs.setSelectedIndex(1);

        });

        H2 titulo = new H2("Facturas de Venta");
        titulo.addClassName("fv-titulo");

        add(
                titulo,
                crearBarraFiltros(),
                tabs
        );
        setFlexGrow(1, tabs);

        buscar();

    }

    private Component etiquetaTab(VaadinIcon icono, String texto) {
        HorizontalLayout h = new HorizontalLayout(icono.create(), new Span(texto));
        h.setSpacing(true);
        h.setAlignItems(Alignment.CENTER);
        return h;
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private void configurarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpDesde.setClearButtonVisible(true);

        dpHasta.setValue(LocalDate.now());
        dpHasta.setClearButtonVisible(true);

        txtBuscar.setWidth("320px");
        txtBuscar.setPlaceholder("Filtrar por código, cliente o usuario...");
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

        Span ayuda = new Span("Doble clic en una factura para ver su detalle");
        ayuda.addClassName("fv-ayuda");

        HorizontalLayout barra = new HorizontalLayout(dpDesde, dpHasta, txtBuscar, btnBuscar, btnLimpiar, ayuda);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        // Permite que los filtros pasen a otra línea en pantallas angostas
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void limpiarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
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
            List<FacturaDeVenta> resultado = this.facturaService.getLista()
                    .stream()
                    .filter(f -> dentroDeRango(aLocalDate(f.getFecha())))
                    .sorted(Comparator.comparing(
                            (FacturaDeVenta f) -> aLocalDate(f.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaFactura.clear();
            listaFactura.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando facturas: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewFacturas == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewFacturas.setFilter(f -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchCod = f.getCodigo() != null && f.getCodigo().toString().contains(term);
            boolean matchCli = f.getNombreCliente() != null && f.getNombreCliente().toLowerCase().contains(term);
            boolean matchUsr = f.getNombreUsuario() != null && f.getNombreUsuario().toLowerCase().contains(term);
            return matchCod || matchCli || matchUsr;
        });

        actualizarTotalesFacturas();
    }

    /**
     * Totales del pie sobre lo que se está viendo (sin contar las anuladas).
     */
    private void actualizarTotalesFacturas() {

        List<FacturaDeVenta> visibles = dataViewFacturas.getItems().toList();
        List<FacturaDeVenta> vigentes = visibles.stream().filter(f -> !esAnulada(f)).toList();

        colCodigo.setFooter("FACTURAS: " + visibles.size());
        colTotalFactura.setFooter(moneda(sumar(vigentes, f -> neto(f))));
        colTotalPagadoFactura.setFooter(moneda(sumar(vigentes, FacturaDeVenta::getTotalPagado)));
        colTotalPendienteFactura.setFooter(moneda(sumar(vigentes, this::pendiente)));
    }

    private BigDecimal sumar(List<FacturaDeVenta> lista, Function<FacturaDeVenta, BigDecimal> valor) {
        return lista.stream()
                .map(valor)
                .map(v -> v != null ? v : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void cargarDetalle(FacturaDeVenta factura) {

        Collection<DetalleFacturaDeVenta> detalles = this.facturaService.getDetalle(factura.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).removeAll();
        tabs.getTabAt(1).add(etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle · Factura #" + factura.getCodigo()));

        mostrarResumen(factura);
        actualizarTotalesDetalle();
    }

    private void limpiarDetalle() {

        listDet = new ArrayList<>();
        gridDetalle.setItems(listDet);
        tabs.getTabAt(1).removeAll();
        tabs.getTabAt(1).add(etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle"));
        tabs.setSelectedIndex(0);

        cabeceraDetalle.removeAll();
        Span vacio = new Span("Haga doble clic en una factura de la pestaña \"Facturas\" para ver su detalle.");
        vacio.addClassName("fv-vacio");
        cabeceraDetalle.add(vacio);
        tarjetasDetalle.removeAll();

        actualizarTotalesDetalle();
    }

    /** Encabezado (número, cliente, fecha, estado) y tarjetas Total/Pagado/Pendiente/Devuelto. */
    private void mostrarResumen(FacturaDeVenta f) {

        cabeceraDetalle.removeAll();
        Span numero = new Span("Factura #" + f.getCodigo());
        numero.addClassName("fv-detalle-numero");
        LocalDate fecha = aLocalDate(f.getFecha());
        Span datos = new Span((f.getNombreCliente() != null ? f.getNombreCliente() : "")
                + (fecha != null ? "  ·  " + fecha.format(FORMATO_FECHA) : "")
                + (f.getNombreUsuario() != null ? "  ·  " + f.getNombreUsuario() : ""));
        datos.addClassName("fv-detalle-datos");
        Div chips = new Div(chipEstado(f), chipTipo(f));
        chips.addClassName("fv-chips");
        if (f.getDelivery() != null) {
            chips.add(chip("DELIVERY", "fv-chip-delivery"));
        }
        cabeceraDetalle.add(new Div(numero, datos), chips);

        tarjetasDetalle.removeAll();
        tarjetasDetalle.add(
                tarjeta("Total", neto(f), "fv-tarjeta-total", VaadinIcon.MONEY),
                tarjeta("Pagado", f.getTotalPagado(), "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE),
                tarjeta("Pendiente", esAnulada(f) ? BigDecimal.ZERO : pendiente(f),
                        tienePendiente(f) ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada", VaadinIcon.CLOCK));
        if (f.getTotalDevuelto().signum() > 0) {
            tarjetasDetalle.add(tarjeta("Devuelto", f.getTotalDevuelto(), "fv-tarjeta-devuelto", VaadinIcon.ARROW_BACKWARD));
        }
    }

    private Div tarjeta(String etiqueta, BigDecimal valor, String clase, VaadinIcon icono) {
        Icon ic = icono.create();
        ic.addClassName("fv-tarjeta-icono");
        Span lbl = new Span(etiqueta);
        lbl.addClassName("fv-tarjeta-etiqueta");
        Span val = new Span(moneda(valor));
        val.addClassName("fv-tarjeta-valor");
        Div d = new Div(ic, lbl, val);
        d.addClassNames("fv-tarjeta", clase);
        return d;
    }

    private void actualizarTotalesDetalle() {

        BigDecimal totalBase = listDet.stream()
                .map(this::cantidadBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subTotal = listDet.stream()
                .map(d -> d.getSubTotal() != null ? d.getSubTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itbis = listDet.stream()
                .map(d -> d.getTotalItbis() != null ? d.getTotalItbis() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = listDet.stream()
                .map(d -> d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        colDescripcion.setFooter("ARTÍCULOS: " + listDet.size());
        colEquivalente.setFooter("-" + numero(totalBase));
        colSubTotal.setFooter(moneda(subTotal));
        colItbis.setFooter(moneda(itbis));
        colTotal.setFooter(moneda(total));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        grid.addClassName("fv-grid");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        colCodigo = grid.addColumn(FacturaDeVenta::getCodigo).setHeader("Código")
                .setFooter("FACTURAS:");

        grid.addColumn(f -> {
            LocalDate fecha = aLocalDate(f.getFecha());
            return fecha != null ? fecha.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (FacturaDeVenta f) -> f.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(f -> f.getFechaCreacion() != null
                ? new Date(f.getFechaCreacion().getTime()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_HORA)
                : "")
                .setHeader("Fecha Creación")
                .setKey("fechaCreacion")
                .setComparator(Comparator.comparing(
                        (FacturaDeVenta f) -> f.getFechaCreacion(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(FacturaDeVenta::getNombreCliente)
                .setHeader("Cliente")
                .setKey("cliente")
                .setFlexGrow(1);

        grid.addColumn(FacturaDeVenta::getNombreUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        grid.addComponentColumn(this::chipTipo)
                .setHeader("Tipo")
                .setKey("tipo")
                .setComparator(Comparator.comparing(f -> esCredito(f) ? 1 : 0));

        grid.addComponentColumn(this::chipEstado)
                .setHeader("Estado")
                .setKey("estado")
                .setComparator(Comparator.comparing(f -> textoEstado(f)));

        // Montos: ancho fijo y a la derecha para leer las cifras alineadas.
        // Total = lo facturado menos lo devuelto (lo que realmente se cobra).
        colTotalFactura = grid.addColumn(f -> moneda(neto(f)))
                .setHeader("Total")
                .setKey("total")
                .setTextAlign(ColumnTextAlign.END)
                .setPartNameGenerator(f -> "fv-monto")
                .setComparator(Comparator.comparing(this::neto));

        // (Antes "Total Pagado" mostraba el pendiente y viceversa)
        colTotalPagadoFactura = grid.addColumn(f -> moneda(f.getTotalPagado()))
                .setHeader("Pagado")
                .setKey("totalpag")
                .setTextAlign(ColumnTextAlign.END)
                .setPartNameGenerator(f -> "fv-monto")
                .setComparator(Comparator.comparing(
                        (FacturaDeVenta f) -> f.getTotalPagado(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotalPendienteFactura = grid.addComponentColumn(f -> {
            Span s = new Span(esAnulada(f) ? "—" : moneda(pendiente(f)));
            s.addClassName(tienePendiente(f) ? "fv-pendiente" : "fv-saldado");
            return s;
        })
                .setHeader("Pendiente")
                .setKey("totalpend")
                .setTextAlign(ColumnTextAlign.END)
                .setComparator(Comparator.comparing(this::pendiente));

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        // Las anuladas se ven tachadas y en gris
        grid.setPartNameGenerator(f -> esAnulada(f) ? "fv-fila-anulada" : null);

        dataViewFacturas = grid.setItems(listaFactura);
        grid.setSizeFull();

    }

    private void configurarGridDetalle() {

        gridDetalle.addClassName("fv-grid");
        gridDetalle.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        gridDetalle.setWidthFull();
        gridDetalle.setMinHeight("360px");
        gridDetalle.setHeight("55vh");

        colDescripcion = gridDetalle.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo)
                .setHeader("Descripción")
                .setKey("descripcion")
                .setFooter("ARTÍCULOS:");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad")
                .setTextAlign(ColumnTextAlign.END);

        gridDetalle.addComponentColumn(d -> {
            BigDecimal dev = d.getCantidadDevuelta();
            if (dev == null || dev.signum() <= 0) {
                return new Span();
            }
            Span s = new Span("Devuelto " + dev.stripTrailingZeros().toPlainString());
            s.addClassNames("fv-chip", "fv-chip-devuelto");
            return s;
        })
                .setHeader("Devolución")
                .setKey("devuelto");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getNombreUnidad)
                .setHeader("Unidad")
                .setKey("unidad");

        // MULTI-EMPAQUE: factor y descuento de inventario en unidad base (calculado aquí)
        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setKey("factor")
                .setTextAlign(ColumnTextAlign.END)
                .setComparator(Comparator.comparing(this::factorDe));

        colEquivalente = gridDetalle.addColumn(d -> "-" + numero(cantidadBase(d)))
                .setHeader("Salida (UMM)")
                .setKey("salidaBase")
                .setTextAlign(ColumnTextAlign.END)
                .setComparator(Comparator.comparing(this::cantidadBase));

        gridDetalle.addColumn(d -> moneda(d.getPrecioVenta()))
                .setHeader("Precio")
                .setKey("precio")
                .setTextAlign(ColumnTextAlign.END)
                .setPartNameGenerator(d -> "fv-monto")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeVenta d) -> d.getPrecioVenta(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colSubTotal = gridDetalle.addColumn(d -> moneda(d.getSubTotal()))
                .setHeader("Sub Total")
                .setKey("subTotal")
                .setTextAlign(ColumnTextAlign.END)
                .setPartNameGenerator(d -> "fv-monto")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeVenta d) -> d.getSubTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colItbis = gridDetalle.addColumn(d -> moneda(d.getTotalItbis()))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setTextAlign(ColumnTextAlign.END)
                .setPartNameGenerator(d -> "fv-monto")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeVenta d) -> d.getTotalItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotal = gridDetalle.addColumn(d -> moneda(d.getTotal()))
                .setHeader("Total")
                .setKey("total")
                .setTextAlign(ColumnTextAlign.END)
                .setPartNameGenerator(d -> "fv-monto")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeVenta d) -> d.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

    }

    // ============================================================
    // ESTADO Y TIPO
    // ============================================================
    private boolean esAnulada(FacturaDeVenta f) {
        return Boolean.TRUE.equals(f.getAnulada());
    }

    private boolean esCredito(FacturaDeVenta f) {
        return f.getTipoVenta() != null && f.getTipoVenta().getCodigo() != null
                && f.getTipoVenta().getCodigo() == TIPO_VENTA_CREDITO;
    }

    /** Total facturado menos lo devuelto. */
    private BigDecimal neto(FacturaDeVenta f) {
        BigDecimal total = f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO;
        return total.subtract(f.getTotalDevuelto()).max(BigDecimal.ZERO);
    }

    private BigDecimal pendiente(FacturaDeVenta f) {
        BigDecimal p = f.getTotalPendiente() != null ? f.getTotalPendiente() : BigDecimal.ZERO;
        return p.max(BigDecimal.ZERO);
    }

    private boolean tienePendiente(FacturaDeVenta f) {
        return !esAnulada(f) && pendiente(f).compareTo(CENTAVO) > 0;
    }

    private String textoEstado(FacturaDeVenta f) {
        if (esAnulada(f)) {
            return "ANULADA";
        }
        return tienePendiente(f) ? "PENDIENTE" : "PAGADA";
    }

    private Span chipEstado(FacturaDeVenta f) {
        String estado = textoEstado(f);
        return chip(estado, switch (estado) {
            case "ANULADA" -> "fv-chip-anulada";
            case "PENDIENTE" -> "fv-chip-pendiente";
            default -> "fv-chip-pagada";
        });
    }

    private Span chipTipo(FacturaDeVenta f) {
        return esCredito(f) ? chip("CRÉDITO", "fv-chip-credito") : chip("CONTADO", "fv-chip-contado");
    }

    private Span chip(String texto, String clase) {
        Span s = new Span(texto);
        s.addClassNames("fv-chip", clase);
        return s;
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

    private BigDecimal factorDe(DetalleFacturaDeVenta d) {

        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleFacturaDeVenta d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    private String numero(BigDecimal valor) {

        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private String moneda(BigDecimal valor) {

        return "RD$ " + numero(valor);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }

}
