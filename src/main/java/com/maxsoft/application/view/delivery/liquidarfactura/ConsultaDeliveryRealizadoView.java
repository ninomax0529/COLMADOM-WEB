package com.maxsoft.application.view.delivery.liquidarfactura;

import com.maxsoft.application.dto.DeliveryRealizadoDTO;
import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.delivery.LiquidacionDeliveryService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import java.util.Date;
import java.util.List;

/**
 * Consulta de deliveries realizados con su estado (PENDIENTE de liquidar,
 * LIQUIDADO o ANULADO). Doble clic sobre un delivery muestra el detalle de la
 * factura en la pestaña "Detalle".
 */
@PermitAll
@PageTitle("Deliveries Realizados | MaxSoft ERP")
@Route(value = "delivery/consultaDelivery")
public class ConsultaDeliveryRealizadoView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat FORMATO_NUMERO = new DecimalFormat("#,##0.00");

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
    private final TextField txtBuscar = new TextField("Factura / Cliente");

    // Resumen (tarjetas)
    private final Div resumen = new Div();
    private final Span kpiTotal = new Span("0");
    private final Span kpiPendientes = new Span("0");
    private final Span kpiLiquidados = new Span("0");
    private final Span kpiAnulados = new Span("0");
    private final Span kpiContado = new Span("RD$ 0.00");
    private final Span kpiFiado = new Span("RD$ 0.00");
    private final Span kpiPorLiquidar = new Span("RD$ 0.00");

    // Grids
    private final Grid<DeliveryRealizadoDTO> grid = new Grid<>(DeliveryRealizadoDTO.class, false);
    private final Grid<DetalleFacturaDeVenta> gridDetalle = new Grid<>(DetalleFacturaDeVenta.class, false);
    private final TabSheet tabs = new TabSheet();
    private GridListDataView<DeliveryRealizadoDTO> dataView;
    private final List<DeliveryRealizadoDTO> lista = new ArrayList<>();

    private Grid.Column<DeliveryRealizadoDTO> colCodigo;
    private Grid.Column<DeliveryRealizadoDTO> colMonto;
    private Grid.Column<DeliveryRealizadoDTO> colEstado;

    @Autowired
    public ConsultaDeliveryRealizadoView(LiquidacionDeliveryService liquidacionService,
            DeliveryService deliveryService,
            FacturaDeVentaService facturaService) {

        this.liquidacionService = liquidacionService;
        this.deliveryService = deliveryService;
        this.facturaService = facturaService;

        setSizeFull();
        setSpacing(false);

        configurarGrid();
        configurarGridDetalle();

        tabs.setSizeFull();
        tabs.add("Deliveries", grid);
        tabs.add("Detalle", gridDetalle);

        configurarResumen();

        add(new H2("Deliveries Realizados"), crearFiltros(), resumen, tabs);
        setFlexGrow(1, tabs);

        cargarMotoristas();
        buscar();
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private VerticalLayout crearFiltros() {

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

        txtBuscar.setPlaceholder("Filtrar resultados...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);

        // Estado, tipo de pago y texto filtran en memoria sobre lo ya cargado
        cbEstado.addValueChangeListener(e -> aplicarFiltros());
        cbTipoPago.addValueChangeListener(e -> aplicarFiltros());
        txtBuscar.addValueChangeListener(e -> aplicarFiltros());

        FormLayout form = new FormLayout(dpDesde, dpHasta, cbMotorista, cbEstado, cbTipoPago, txtBuscar);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 6)
        );

        Button btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        Button btnLimpiar = new Button("Limpiar filtros", VaadinIcon.REFRESH.create(), e -> limpiarFiltros());

        VerticalLayout contenedor = new VerticalLayout(form, new HorizontalLayout(btnBuscar, btnLimpiar));
        contenedor.setPadding(false);
        contenedor.setSpacing(true);
        return contenedor;
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

        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        dataView = grid.setItems(lista);

        colCodigo = grid.addColumn(DeliveryRealizadoDTO::getCodigoFactura)
                .setHeader("N° Factura").setAutoWidth(true).setSortable(true);

        grid.addColumn(d -> fecha(d.getFecha(), FORMATO_FECHA))
                .setHeader("Fecha").setAutoWidth(true)
                .setComparator((a, b) -> compararFechas(a.getFecha(), b.getFecha()));

        grid.addColumn(DeliveryRealizadoDTO::getCliente).setHeader("Cliente").setAutoWidth(true).setSortable(true);
        grid.addColumn(DeliveryRealizadoDTO::getMotorista).setHeader("Motorista").setAutoWidth(true).setSortable(true);

        grid.addColumn(new ComponentRenderer<>(d -> {
            Span s = new Span(d.getTipoPago());
            s.getElement().getThemeList().add("badge " + (PedidoDeliveryDTO.FIADO.equals(d.getTipoPago()) ? "error" : "contrast"));
            return s;
        })).setHeader("Tipo de pago").setAutoWidth(true)
                .setComparator((a, b) -> a.getTipoPago().compareTo(b.getTipoPago()));

        colMonto = grid.addColumn(d -> numero(d.getMontoTotal()))
                .setHeader("Monto").setAutoWidth(true)
                .setComparator((a, b) -> a.getMontoTotal().compareTo(b.getMontoTotal()));

        colEstado = grid.addColumn(new ComponentRenderer<>(d -> {
            Span s = new Span(d.getEstado());
            s.getElement().getThemeList().add("badge " + colorEstado(d.getEstado()));
            return s;
        })).setHeader("Estado").setAutoWidth(true)
                .setComparator((a, b) -> a.getEstado().compareTo(b.getEstado()));

        grid.addColumn(d -> d.getCodigoLiquidacion() != null ? "#" + d.getCodigoLiquidacion() : "")
                .setHeader("Liquidación").setAutoWidth(true);

        grid.addColumn(d -> fecha(d.getFechaLiquidacion(), FORMATO_FECHA_HORA))
                .setHeader("Fecha liquidación").setAutoWidth(true);

        // Facturas de contado que el cliente se quedó a crédito al liquidar
        grid.addColumn(d -> d.isConvertidoACredito() ? "A crédito: " + d.getClienteCredito() : "")
                .setHeader("Observación").setAutoWidth(true).setFlexGrow(1);

        grid.addItemDoubleClickListener(e -> mostrarDetalle(e.getItem()));
    }

    private String colorEstado(String estado) {
        if (DeliveryRealizadoDTO.LIQUIDADO.equals(estado)) {
            return "success";
        }
        if (DeliveryRealizadoDTO.ANULADO.equals(estado)) {
            return "error";
        }
        return "warning"; // PENDIENTE
    }

    // ============================================================
    // GRID DETALLE (líneas de la factura)
    // ============================================================
    private void configurarGridDetalle() {

        gridDetalle.setSizeFull();
        gridDetalle.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        gridDetalle.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo)
                .setHeader("Descripción").setFlexGrow(1)
                .setFooter("TOTAL:");
        gridDetalle.addColumn(DetalleFacturaDeVenta::getNombreUnidad).setHeader("Unidad");
        gridDetalle.addColumn(d -> numero(d.getCantidad())).setHeader("Cantidad");
        gridDetalle.addColumn(d -> numero(d.getPrecioVenta())).setHeader("Precio");
        gridDetalle.addColumn(d -> numero(d.getSubTotal())).setHeader("Sub Total");
        gridDetalle.addColumn(d -> numero(d.getTotalItbis())).setHeader("ITBIS");
        gridDetalle.addColumn(d -> numero(d.getTotal())).setHeader("Total");

        gridDetalle.getColumns().forEach(c -> {
            c.setAutoWidth(true);
            c.setSortable(false);
        });
    }

    private void mostrarDetalle(DeliveryRealizadoDTO d) {

        try {
            Collection<DetalleFacturaDeVenta> detalles = facturaService.getDetalle(d.getCodigoFactura());
            gridDetalle.setItems(detalles != null ? detalles : List.of());
            tabs.setSelectedIndex(1);
            tabs.getTabAt(1).setLabel("Detalle( Factura -> " + d.getCodigoFactura() + " )");
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            notificar("No se pudo cargar el detalle: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    // ============================================================
    // RESUMEN (tarjetas)
    // ============================================================
    private void configurarResumen() {

        resumen.setWidthFull();
        resumen.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(150px, 1fr))")
                .set("gap", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)");

        resumen.add(
                crearTarjeta("Deliveries", kpiTotal, "var(--lumo-primary-color)", VaadinIcon.TRUCK),
                crearTarjeta("Pendientes", kpiPendientes, "var(--lumo-warning-text-color)", VaadinIcon.CLOCK),
                crearTarjeta("Liquidados", kpiLiquidados, "var(--lumo-success-text-color)", VaadinIcon.CHECK_CIRCLE),
                crearTarjeta("Anulados", kpiAnulados, "var(--lumo-error-text-color)", VaadinIcon.BAN),
                crearTarjeta("Contado", kpiContado, "var(--lumo-success-text-color)", VaadinIcon.MONEY),
                crearTarjeta("Fiado", kpiFiado, "var(--lumo-error-text-color)", VaadinIcon.BOOK),
                crearTarjeta("Por liquidar", kpiPorLiquidar, "var(--lumo-warning-text-color)", VaadinIcon.WALLET)
        );
    }

    private Div crearTarjeta(String titulo, Span valor, String color, VaadinIcon icono) {

        Icon ic = icono.create();
        ic.setSize("var(--lumo-icon-size-s)");
        ic.getStyle().set("color", color);

        Span lbl = new Span(titulo);
        lbl.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout cabecera = new HorizontalLayout(ic, lbl);
        cabecera.setPadding(false);
        cabecera.setSpacing(true);
        cabecera.setAlignItems(Alignment.CENTER);

        valor.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "700")
                .set("color", color);

        Div card = new Div(cabecera, valor);
        card.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-xs)")
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-left", "4px solid " + color)
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");
        return card;
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
        long anulados = visibles.stream().filter(d -> DeliveryRealizadoDTO.ANULADO.equals(d.getEstado())).count();

        BigDecimal total = sumar(visibles, false, null);
        BigDecimal contado = sumar(visibles, false, PedidoDeliveryDTO.CONTADO);
        BigDecimal fiado = sumar(visibles, false, PedidoDeliveryDTO.FIADO);
        BigDecimal porLiquidar = visibles.stream()
                .filter(d -> DeliveryRealizadoDTO.PENDIENTE.equals(d.getEstado()))
                .map(DeliveryRealizadoDTO::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        kpiTotal.setText(String.valueOf(visibles.size()));
        kpiPendientes.setText(String.valueOf(pendientes));
        kpiLiquidados.setText(String.valueOf(liquidados));
        kpiAnulados.setText(String.valueOf(anulados));
        kpiContado.setText("RD$ " + numero(contado));
        kpiFiado.setText("RD$ " + numero(fiado));
        kpiPorLiquidar.setText("RD$ " + numero(porLiquidar));

        colCodigo.setFooter("TOTAL (" + visibles.size() + "):");
        colMonto.setFooter("RD$ " + numero(total));
        colEstado.setFooter("");
    }

    private BigDecimal sumar(List<DeliveryRealizadoDTO> items, boolean incluirAnulados, String tipoPago) {

        return items.stream()
                .filter(d -> incluirAnulados || !DeliveryRealizadoDTO.ANULADO.equals(d.getEstado()))
                .filter(d -> tipoPago == null || tipoPago.equals(d.getTipoPago()))
                .map(DeliveryRealizadoDTO::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
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

    private String numero(BigDecimal valor) {
        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void notificar(String mensaje, NotificationVariant variante) {
        Notification.show(mensaje, 4000, Notification.Position.TOP_END).addThemeVariants(variante);
    }
}
