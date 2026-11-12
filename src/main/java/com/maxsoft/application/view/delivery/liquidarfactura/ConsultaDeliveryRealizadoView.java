package com.maxsoft.application.view.delivery.liquidarfactura;

import com.maxsoft.application.dto.DeliveryRealizadoDTO;
import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.delivery.LiquidacionDeliveryService;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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

/**
 * Consulta de deliveries realizados con su estado (PENDIENTE de liquidar,
 * LIQUIDADO o ANULADO). Doble clic (o el ojo) sobre un delivery muestra la
 * factura en la pestaña "Detalle".
 */
@PermitAll
@PageTitle("Deliveries Realizados | MaxSoft ERP")
@Route(value = "delivery/consultaDelivery")
public class ConsultaDeliveryRealizadoView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private static final DecimalFormat FORMATO_NUMERO = new DecimalFormat("#,##0.##");

    private static final String TODOS = "Todos";

    private final LiquidacionDeliveryService liquidacionService;
    private final DeliveryService deliveryService;
    private final FacturaDeVentaService facturaService;

    // Filtros
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final ComboBox<Delivery> cbMotorista = new ComboBox<>("Motorista");
    private final ComboBox<String> cbEstado = new ComboBox<>("Estado");
    private final ComboBox<String> cbTipoPago = new ComboBox<>("Tipo de pago");
    private final TextField txtBuscar = new TextField();

    // Resumen (tarjetas)
    private final Div resumen = new Div();

    // Grids
    private final Grid<DeliveryRealizadoDTO> grid = new Grid<>(DeliveryRealizadoDTO.class, false);
    private final Grid<DetalleFacturaDeVenta> gridDetalle = new Grid<>(DetalleFacturaDeVenta.class, false);
    private final TabSheet tabs = new TabSheet();
    private final Div cabeceraDetalle = new Div();
    private final Div tarjetasDetalle = new Div();
    private GridListDataView<DeliveryRealizadoDTO> dataView;
    private final List<DeliveryRealizadoDTO> lista = new ArrayList<>();
    private List<DetalleFacturaDeVenta> listDet = new ArrayList<>();

    private Grid.Column<DeliveryRealizadoDTO> colCodigo;
    private Grid.Column<DeliveryRealizadoDTO> colMonto;
    private Grid.Column<DetalleFacturaDeVenta> colDetDescripcion;
    private Grid.Column<DetalleFacturaDeVenta> colDetItbis;
    private Grid.Column<DetalleFacturaDeVenta> colDetTotal;

    @Autowired
    public ConsultaDeliveryRealizadoView(LiquidacionDeliveryService liquidacionService,
            DeliveryService deliveryService,
            FacturaDeVentaService facturaService) {

        this.liquidacionService = liquidacionService;
        this.deliveryService = deliveryService;
        this.facturaService = facturaService;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();
        EstiloConsulta.aplicarVista(this, tabs);

        configurarGrid();
        configurarGridDetalle();

        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.TRUCK, "Deliveries"), grid);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle"),
                EstiloConsulta.panelDetalle(cabeceraDetalle, tarjetasDetalle, gridDetalle));

        // Una sola fila compacta: deja el alto de la pantalla a la tabla
        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");

        H2 titulo = new H2("Deliveries Realizados");
        titulo.addClassName("fv-titulo");

        add(titulo, crearFiltros(), resumen, tabs);
        setFlexGrow(1, tabs);

        cargarMotoristas();
        limpiarDetalle();
        buscar();
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private HorizontalLayout crearFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());

        cbMotorista.setItemLabelGenerator(Delivery::getNombre);
        cbMotorista.setClearButtonVisible(true);
        cbMotorista.setPlaceholder("Todos");

        cbEstado.setItems(TODOS, DeliveryRealizadoDTO.PENDIENTE, DeliveryRealizadoDTO.LIQUIDADO,
                DeliveryRealizadoDTO.ANULADO);
        cbEstado.setAllowCustomValue(false);
        cbEstado.setValue(TODOS);

        cbTipoPago.setItems(TODOS, PedidoDeliveryDTO.CONTADO, PedidoDeliveryDTO.FIADO);
        cbTipoPago.setAllowCustomValue(false);
        cbTipoPago.setValue(TODOS);

        txtBuscar.setWidth("260px");
        txtBuscar.setPlaceholder("Filtrar por factura, cliente o motorista...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);

        // Estado, tipo de pago y texto filtran en memoria sobre lo ya cargado
        cbEstado.addValueChangeListener(e -> aplicarFiltros());
        cbTipoPago.addValueChangeListener(e -> aplicarFiltros());
        txtBuscar.addValueChangeListener(e -> aplicarFiltros());

        Button btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        Button btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create(), e -> limpiarFiltros());

        HorizontalLayout barra = new HorizontalLayout(dpDesde, dpHasta, cbMotorista, cbEstado, cbTipoPago,
                txtBuscar, btnBuscar, btnLimpiar,
                EstiloConsulta.ayuda("Doble clic en un delivery para ver su factura"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void cargarMotoristas() {
        cbMotorista.setItems(deliveryService.getLista());
    }

    private void limpiarFiltros() {
        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        cbMotorista.clear();
        cbEstado.setValue(TODOS);
        cbTipoPago.setValue(TODOS);
        txtBuscar.clear();
        buscar();
    }

    // ============================================================
    // GRID PRINCIPAL
    // ============================================================
    private void configurarGrid() {

        EstiloConsulta.aplicarGrid(grid);
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setPartNameGenerator(d -> esAnulado(d) ? "fv-fila-anulada" : null);
        dataView = grid.setItems(lista);

        colCodigo = grid.addColumn(d -> "#" + d.getCodigoFactura())
                .setHeader("Factura").setAutoWidth(true)
                .setComparator(Comparator.comparing(DeliveryRealizadoDTO::getCodigoFactura));

        grid.addColumn(d -> fecha(d.getFecha(), FORMATO_FECHA))
                .setHeader("Fecha").setAutoWidth(true)
                .setComparator((a, b) -> compararFechas(a.getFecha(), b.getFecha()));

        grid.addColumn(DeliveryRealizadoDTO::getCliente).setHeader("Cliente").setAutoWidth(true)
                .setFlexGrow(2).setSortable(true);
        grid.addColumn(DeliveryRealizadoDTO::getMotorista).setHeader("Motorista").setAutoWidth(true).setSortable(true);

        grid.addComponentColumn(this::chipTipoPago)
                .setHeader("Pago").setAutoWidth(true)
                .setComparator(Comparator.comparing(d -> texto(d.getTipoPago())));

        grid.addComponentColumn(this::chipEstado)
                .setHeader("Estado").setAutoWidth(true)
                .setComparator(Comparator.comparing(d -> texto(d.getEstado())));

        colMonto = EstiloConsulta.monto(grid.addColumn(d -> EstiloConsulta.moneda(d.getMontoTotal())))
                .setHeader("Monto").setAutoWidth(true)
                .setComparator(Comparator.comparing(d -> valor(d.getMontoTotal())));

        grid.addColumn(d -> d.getCodigoLiquidacion() != null ? "#" + d.getCodigoLiquidacion() : "—")
                .setHeader("Liquidación").setAutoWidth(true);

        grid.addColumn(d -> fecha(d.getFechaLiquidacion(), FORMATO_FECHA_HORA))
                .setHeader("Fecha liquidación").setAutoWidth(true)
                .setComparator((a, b) -> compararFechas(a.getFechaLiquidacion(), b.getFechaLiquidacion()));

        // Facturas de contado que el cliente se quedó a crédito al liquidar
        grid.addComponentColumn(d -> d.isConvertidoACredito()
                ? EstiloConsulta.chip("A CRÉDITO: " + texto(d.getClienteCredito()), "fv-chip-credito")
                : new Span(""))
                .setHeader("Observación").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(d -> {
            Button btnVer = new Button(VaadinIcon.EYE.create(), e -> mostrarDetalle(d));
            btnVer.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnVer.setTooltipText("Ver la factura del delivery");
            return btnVer;
        })).setHeader("Ver").setAutoWidth(true).setSortable(false);

        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() != null) {
                mostrarDetalle(e.getItem());
            }
        });
    }

    // ============================================================
    // DETALLE (factura del delivery)
    // ============================================================
    private void configurarGridDetalle() {

        EstiloConsulta.aplicarGrid(gridDetalle);
        gridDetalle.setWidthFull();
        gridDetalle.setMinHeight("300px");
        gridDetalle.setHeight("50vh");

        colDetDescripcion = gridDetalle.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo)
                .setHeader("Descripción").setFlexGrow(2)
                .setFooter("ARTÍCULOS:");
        gridDetalle.addColumn(DetalleFacturaDeVenta::getNombreUnidad).setHeader("Unidad");
        EstiloConsulta.monto(gridDetalle.addColumn(d -> numero(d.getCantidad()))).setHeader("Cantidad");
        EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.getPrecioVenta()))).setHeader("Precio");
        EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.getSubTotal()))).setHeader("Sub Total");
        colDetItbis = EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.getTotalItbis())))
                .setHeader("ITBIS");
        colDetTotal = EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.getTotal())))
                .setHeader("Total");

        gridDetalle.getColumns().forEach(c -> {
            c.setAutoWidth(true);
            c.setSortable(false);
        });
    }

    private void mostrarDetalle(DeliveryRealizadoDTO d) {

        try {
            Collection<DetalleFacturaDeVenta> detalles = facturaService.getDetalle(d.getCodigoFactura());
            listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
            gridDetalle.setItems(listDet);

            EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle · Factura #" + d.getCodigoFactura());

            EstiloConsulta.cabecera(cabeceraDetalle,
                    "Factura #" + d.getCodigoFactura(),
                    texto(d.getCliente())
                    + (d.getFecha() != null ? "  ·  " + fecha(d.getFecha(), FORMATO_FECHA) : "")
                    + (d.getMotorista() != null ? "  ·  Motorista: " + d.getMotorista() : "")
                    + (d.getCodigoLiquidacion() != null
                    ? "  ·  Liquidación #" + d.getCodigoLiquidacion()
                    + (d.getFechaLiquidacion() != null ? " (" + fecha(d.getFechaLiquidacion(), FORMATO_FECHA_HORA) + ")" : "")
                    : ""),
                    chipEstado(d), chipTipoPago(d));

            tarjetasDetalle.removeAll();
            tarjetasDetalle.add(EstiloConsulta.tarjeta("Monto", d.getMontoTotal(), "fv-tarjeta-total", VaadinIcon.MONEY));

            // Pagado / pendiente / devuelto reales de la factura
            FacturaDeVenta f = facturaService.getFactura(d.getCodigoFactura());
            if (f != null) {
                boolean anulada = Boolean.TRUE.equals(f.getAnulada());
                tarjetasDetalle.add(
                        EstiloConsulta.tarjeta("Pagado", f.getTotalPagado(), "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE),
                        EstiloConsulta.tarjetaPendiente("Pendiente", anulada ? BigDecimal.ZERO : f.getTotalPendiente()));
                if (valor(f.getTotalDevuelto()).signum() > 0) {
                    tarjetasDetalle.add(EstiloConsulta.tarjeta("Devuelto", f.getTotalDevuelto(),
                            "fv-tarjeta-devuelto", VaadinIcon.ARROW_BACKWARD));
                }
            }

            actualizarTotalesDetalle();
            tabs.setSelectedIndex(1);

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            notificar("No se pudo cargar el detalle: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarDetalle() {

        listDet = new ArrayList<>();
        gridDetalle.setItems(listDet);
        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle");
        tabs.setSelectedIndex(0);
        EstiloConsulta.cabeceraVacia(cabeceraDetalle,
                "Haga doble clic en un delivery de la pestaña \"Deliveries\" para ver su factura.");
        tarjetasDetalle.removeAll();

        actualizarTotalesDetalle();
    }

    private void actualizarTotalesDetalle() {

        colDetDescripcion.setFooter("ARTÍCULOS: " + listDet.size());
        colDetItbis.setFooter(EstiloConsulta.moneda(listDet.stream()
                .map(x -> valor(x.getTotalItbis())).reduce(BigDecimal.ZERO, BigDecimal::add)));
        colDetTotal.setFooter(EstiloConsulta.moneda(listDet.stream()
                .map(x -> valor(x.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    // ============================================================
    // RESUMEN (tarjetas)
    // ============================================================
    /** Tarjeta con el estilo de las consultas y texto libre (cantidades o montos). */
    private Div tarjeta(String etiqueta, String valor, String clase, VaadinIcon icono) {
        Icon ic = icono.create();
        ic.addClassName("fv-tarjeta-icono");
        Span lbl = new Span(etiqueta);
        lbl.addClassName("fv-tarjeta-etiqueta");
        Span val = new Span(valor);
        val.addClassName("fv-tarjeta-valor");
        Div d = new Div(ic, lbl, val);
        d.addClassNames("fv-tarjeta", clase);
        return d;
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void buscar() {

        if (dpDesde.getValue() == null || dpHasta.getValue() == null) {
            notificar("Indique el rango de fechas.", NotificationVariant.LUMO_WARNING);
            return;
        }
        if (dpDesde.getValue().isAfter(dpHasta.getValue())) {
            notificar("La fecha 'Desde' no puede ser mayor que 'Hasta'.", NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            Date desde = aDate(dpDesde.getValue());
            Date hastaExclusivo = aDate(dpHasta.getValue().plusDays(1));
            Delivery motorista = cbMotorista.getValue();

            List<DeliveryRealizadoDTO> resultado = liquidacionService.getDeliveriesRealizados(
                    desde, hastaExclusivo, motorista != null ? motorista.getCodigo() : null);

            lista.clear();
            lista.addAll(resultado);
            dataView.refreshAll();

            grid.deselectAll();
            limpiarDetalle();
            aplicarFiltros();

        } catch (Exception ex) {
            ex.printStackTrace();
            notificar("Error consultando deliveries: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void aplicarFiltros() {

        String estado = cbEstado.getValue() != null ? cbEstado.getValue() : TODOS;
        String tipo = cbTipoPago.getValue() != null ? cbTipoPago.getValue() : TODOS;
        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataView.setFilter(d
                -> (TODOS.equals(estado) || estado.equals(d.getEstado()))
                && (TODOS.equals(tipo) || tipo.equals(d.getTipoPago()))
                && (term.isEmpty()
                || String.valueOf(d.getCodigoFactura()).contains(term)
                || (d.getCliente() != null && d.getCliente().toLowerCase().contains(term))
                || (d.getMotorista() != null && d.getMotorista().toLowerCase().contains(term))));

        actualizarResumenYFooters();
    }

    /**
     * Los totales respetan lo que se ve: las facturas anuladas se cuentan pero
     * no suman en los montos.
     */
    private void actualizarResumenYFooters() {

        List<DeliveryRealizadoDTO> visibles = dataView.getItems().toList();

        long pendientes = visibles.stream().filter(d -> DeliveryRealizadoDTO.PENDIENTE.equals(d.getEstado())).count();
        long liquidados = visibles.stream().filter(d -> DeliveryRealizadoDTO.LIQUIDADO.equals(d.getEstado())).count();
        long anulados = visibles.stream().filter(this::esAnulado).count();

        BigDecimal total = sumar(visibles, null);
        BigDecimal contado = sumar(visibles, PedidoDeliveryDTO.CONTADO);
        BigDecimal fiado = sumar(visibles, PedidoDeliveryDTO.FIADO);
        BigDecimal porLiquidar = visibles.stream()
                .filter(d -> DeliveryRealizadoDTO.PENDIENTE.equals(d.getEstado()))
                .map(d -> valor(d.getMontoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        resumen.removeAll();
        resumen.add(
                tarjeta("Deliveries", String.valueOf(visibles.size()), "fv-tarjeta-total", VaadinIcon.TRUCK),
                tarjeta("Pendientes", String.valueOf(pendientes),
                        pendientes > 0 ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada", VaadinIcon.CLOCK),
                tarjeta("Liquidados", String.valueOf(liquidados), "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE),
                tarjeta("Anulados", String.valueOf(anulados), "fv-tarjeta-saldada", VaadinIcon.BAN),
                tarjeta("Contado", EstiloConsulta.moneda(contado), "fv-tarjeta-pagado", VaadinIcon.MONEY),
                tarjeta("Fiado", EstiloConsulta.moneda(fiado), "fv-tarjeta-devuelto", VaadinIcon.BOOK),
                EstiloConsulta.tarjetaPendiente("Por liquidar", porLiquidar));

        colCodigo.setFooter("DELIVERIES: " + visibles.size());
        colMonto.setFooter(EstiloConsulta.moneda(total));
    }

    private BigDecimal sumar(List<DeliveryRealizadoDTO> items, String tipoPago) {

        return items.stream()
                .filter(d -> !esAnulado(d))
                .filter(d -> tipoPago == null || tipoPago.equals(d.getTipoPago()))
                .map(d -> valor(d.getMontoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private boolean esAnulado(DeliveryRealizadoDTO d) {
        return DeliveryRealizadoDTO.ANULADO.equals(d.getEstado());
    }

    private Span chipEstado(DeliveryRealizadoDTO d) {
        if (esAnulado(d)) {
            return EstiloConsulta.chip("ANULADO", "fv-chip-anulada");
        }
        return DeliveryRealizadoDTO.LIQUIDADO.equals(d.getEstado())
                ? EstiloConsulta.chip("LIQUIDADO", "fv-chip-pagada")
                : EstiloConsulta.chip("POR LIQUIDAR", "fv-chip-pendiente");
    }

    private Span chipTipoPago(DeliveryRealizadoDTO d) {
        return PedidoDeliveryDTO.FIADO.equals(d.getTipoPago())
                ? EstiloConsulta.chip("FIADO", "fv-chip-credito")
                : EstiloConsulta.chip(texto(d.getTipoPago()).toUpperCase(), "fv-chip-contado");
    }

    private Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String fecha(Date fecha, DateTimeFormatter formato) {
        if (fecha == null) {
            return "";
        }
        // new Date(getTime()) evita problemas si llega un java.sql.Date
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).format(formato);
    }

    private int compararFechas(Date a, Date b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }

    private static String texto(String s) {
        return s != null ? s : "";
    }

    private static BigDecimal valor(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private String numero(BigDecimal valor) {
        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void notificar(String mensaje, NotificationVariant variante) {
        Notification.show(mensaje, 4000, Notification.Position.TOP_END).addThemeVariants(variante);
    }
}
