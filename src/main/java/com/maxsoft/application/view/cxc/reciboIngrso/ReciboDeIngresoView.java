package com.maxsoft.application.view.cxc.reciboIngrso;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Route(value = "cxc/reciboDeIngreso")
@PageTitle("Consulta Recibo de Ingreso")
public class ReciboDeIngresoView extends VerticalLayout {

    private static final String TODOS = "TODOS";
    private static final String VIGENTE = "VIGENTE";
    private static final String ANULADO = "ANULADO";

    /** Recibos creados solos por el sistema (no se anulan desde aquí). */
    private static final String CONCEPTO_RECIBO_POS = "Recibo automática por Factura de venta";
    private static final String CONCEPTO_RECIBO_LIQUIDACION = "Pago con liquidacion de factura";

    private final ReciboDeIngresoService reciboDeIngresoService;
    private final ClienteService clienteService;

    private final Grid<ReciboDeIngreso> grid = new Grid<>(ReciboDeIngreso.class, false);
    private final Grid<DetalleReciboDeIngreso> gridDetalle = new Grid<>(DetalleReciboDeIngreso.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Componentes de Filtro
    private final TextField txtBuscar = new TextField();
    private final ComboBox<Cliente> cbCliente = new ComboBox<>("Cliente");
    private final ComboBox<String> cbEstado = new ComboBox<>("Estado");
    private final DatePicker dtDesde = new DatePicker("Fecha Desde");
    private final DatePicker dtHasta = new DatePicker("Fecha Hasta");

    private final Button btnFiltrar = new Button("Buscar", VaadinIcon.SEARCH.create());
    private final Button btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create());

    private final TabSheet tabs = new TabSheet();
    private final Div cabeceraDetalle = new Div();
    private final Div tarjetasDetalle = new Div();

    List<ReciboDeIngreso> listaRecibos = new ArrayList<>();
    List<DetalleReciboDeIngreso> listDet = new ArrayList<>();

    private GridListDataView<ReciboDeIngreso> dataViewRecibos;

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    // Columnas con pie (totales)
    private Grid.Column<ReciboDeIngreso> colNumero;
    private Grid.Column<ReciboDeIngreso> colTotal;

    private Grid.Column<DetalleReciboDeIngreso> colDescripcion;
    private Grid.Column<DetalleReciboDeIngreso> colDetTotal;

    public ReciboDeIngresoView(ReciboDeIngresoService reciboDeIngresoService,
            ClienteService clienteService) {
        this.reciboDeIngresoService = reciboDeIngresoService;
        this.clienteService = clienteService;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();
        EstiloConsulta.aplicarVista(this, tabs);

        configurarFiltros();
        configurarGrid();
        configurarGridDetalle();

        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.LIST, "Recibos de Ingreso"), grid);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle"),
                EstiloConsulta.panelDetalle(cabeceraDetalle, tarjetasDetalle, gridDetalle));

        botonera.getNuevo().addClickListener(e -> UI.getCurrent().navigate(RegistroReciboDeIngresoView.class));

        grid.addItemDoubleClickListener(c -> {
            if (c.getItem() != null) {
                verDetalle(c.getItem());
            }
        });

        H2 titulo = new H2("Recibos de Ingreso");
        titulo.addClassName("fv-titulo");

        add(botonera, titulo, crearBarraFiltros(), tabs);
        setFlexGrow(1, tabs);

        aplicarFiltros(); // Carga inicial
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private void configurarFiltros() {

        txtBuscar.setWidth("260px");
        txtBuscar.setPlaceholder("Filtrar por número, cliente o concepto...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());

        cbCliente.setItems(clienteService.getLista());
        cbCliente.setItemLabelGenerator(s -> s.getCedula() != null
                ? s.getNombre() + " (" + s.getCedula() + ")" : s.getNombre());
        cbCliente.setPlaceholder("Todos los clientes");
        cbCliente.setClearButtonVisible(true);

        cbEstado.setItems(TODOS, VIGENTE, ANULADO);
        cbEstado.setValue(TODOS);
        cbEstado.setAllowCustomValue(false);

        // Rango por defecto (Primer día del mes actual hasta hoy)
        dtDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dtDesde.setClearButtonVisible(true);
        dtHasta.setValue(LocalDate.now());
        dtHasta.setClearButtonVisible(true);

        btnFiltrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnFiltrar.addClickShortcut(Key.ENTER);
        btnFiltrar.addClickListener(e -> aplicarFiltros());

        btnLimpiar.addClickListener(e -> limpiarFiltros());
    }

    private HorizontalLayout crearBarraFiltros() {

        HorizontalLayout barra = new HorizontalLayout(
                dtDesde, dtHasta, cbCliente, cbEstado, txtBuscar, btnFiltrar, btnLimpiar,
                EstiloConsulta.ayuda("Doble clic en un recibo para ver su detalle"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        // Permite que los filtros pasen a otra línea en pantallas angostas
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void limpiarFiltros() {

        txtBuscar.clear();
        cbCliente.clear();
        cbEstado.setValue(TODOS);
        dtDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dtHasta.setValue(LocalDate.now());
        aplicarFiltros();
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void aplicarFiltros() {

        if (dtDesde.getValue() != null && dtHasta.getValue() != null
                && dtDesde.getValue().isAfter(dtHasta.getValue())) {
            mostrarNotificacion("La fecha 'Desde' no puede ser mayor que 'Hasta'.", NotificationVariant.LUMO_WARNING);
            return;
        }

        try {

            Cliente clienteSel = cbCliente.getValue();
            String estadoSel = cbEstado.getValue() != null ? cbEstado.getValue() : TODOS;

            List<ReciboDeIngreso> resultado = reciboDeIngresoService.getLista().stream().filter(r -> {

                boolean coincideCliente = clienteSel == null
                        || (r.getCliente() != null
                        && r.getCliente().getCodigo().equals(clienteSel.getCodigo()));

                // (Antes el filtro de estado no hacía nada)
                boolean coincideEstado = switch (estadoSel) {
                    case VIGENTE -> !esAnulado(r);
                    case ANULADO -> esAnulado(r);
                    default -> true;
                };

                return coincideCliente && coincideEstado && dentroDeRango(aLocalDate(r.getFecha()));
            })
                    .sorted(Comparator.comparing(
                            (ReciboDeIngreso o) -> aLocalDate(o.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(ReciboDeIngreso::getCodigo,
                                    Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            listaRecibos.clear();
            listaRecibos.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando recibos: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewRecibos == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewRecibos.setFilter(o -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchNum = o.getCodigo() != null && o.getCodigo().toString().contains(term);
            boolean matchCom = o.getConcepto() != null && o.getConcepto().toLowerCase().contains(term);
            boolean matchCli = nombreCliente(o).toLowerCase().contains(term);
            return matchNum || matchCom || matchCli;
        });

        actualizarTotales();
    }

    /** Totales del pie sobre lo visible (los anulados no suman). */
    private void actualizarTotales() {

        List<ReciboDeIngreso> visibles = dataViewRecibos.getItems().toList();

        colNumero.setFooter("RECIBOS: " + visibles.size());
        colTotal.setFooter(EstiloConsulta.moneda(visibles.stream()
                .filter(o -> !esAnulado(o))
                .map(o -> valor(o.getTotalPagado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private void verDetalle(ReciboDeIngreso recibo) {
        cargarDetalle(recibo);
        tabs.setSelectedIndex(1);
    }

    private void cargarDetalle(ReciboDeIngreso recibo) {

        Collection<DetalleReciboDeIngreso> detalles = this.reciboDeIngresoService.getDetalle(recibo.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle · Recibo #" + recibo.getCodigo());

        EstiloConsulta.cabecera(cabeceraDetalle,
                "Recibo de ingreso #" + recibo.getCodigo(),
                nombreCliente(recibo)
                + (recibo.getFecha() != null ? "  ·  " + df.format(recibo.getFecha()) : "")
                + (recibo.getConcepto() != null && !recibo.getConcepto().isBlank() ? "  ·  " + recibo.getConcepto() : ""),
                chipEstado(recibo), chipOrigen(recibo));

        BigDecimal antes = listDet.stream().map(d -> valor(d.getBalancePendienteAntes())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal despues = listDet.stream().map(d -> valor(d.getBalancePendienteDespues())).reduce(BigDecimal.ZERO, BigDecimal::add);

        tarjetasDetalle.removeAll();
        tarjetasDetalle.add(
                EstiloConsulta.tarjeta("Total cobrado", recibo.getTotalPagado(), "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE),
                EstiloConsulta.tarjeta("Debía antes", antes, "fv-tarjeta-total", VaadinIcon.MONEY),
                EstiloConsulta.tarjetaPendiente("Quedó pendiente", despues));

        actualizarTotalesDetalle();
    }

    private void limpiarDetalle() {

        listDet = new ArrayList<>();
        gridDetalle.setItems(listDet);
        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle");
        tabs.setSelectedIndex(0);
        EstiloConsulta.cabeceraVacia(cabeceraDetalle,
                "Haga doble clic en un recibo de la pestaña \"Recibos de Ingreso\" para ver su detalle.");
        tarjetasDetalle.removeAll();

        actualizarTotalesDetalle();
    }

    private void actualizarTotalesDetalle() {

        colDescripcion.setFooter("FACTURAS: " + listDet.size());
        colDetTotal.setFooter(EstiloConsulta.moneda(listDet.stream()
                .map(d -> valor(d.getTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        EstiloConsulta.aplicarGrid(grid);
        grid.setPartNameGenerator(r -> esAnulado(r) ? "fv-fila-anulada" : null);

        colNumero = grid.addColumn(ReciboDeIngreso::getCodigo)
                .setHeader("Número")
                .setKey("numero")
                .setFooter("RECIBOS:");

        grid.addColumn(this::nombreCliente)
                .setHeader("Cliente")
                .setKey("cliente")
                .setFlexGrow(2);

        grid.addColumn(o -> o.getFecha() != null ? df.format(o.getFecha()) : "-")
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (ReciboDeIngreso o) -> o.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(o -> o.getConcepto() != null ? o.getConcepto() : "")
                .setHeader("Concepto")
                .setKey("concepto")
                .setFlexGrow(2);

        grid.addComponentColumn(this::chipOrigen)
                .setHeader("Origen")
                .setKey("origen");

        grid.addComponentColumn(this::chipEstado)
                .setHeader("Estado")
                .setKey("estado");

        colTotal = EstiloConsulta.monto(grid.addColumn(o -> EstiloConsulta.moneda(o.getTotalPagado())))
                .setHeader("Total cobrado")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (ReciboDeIngreso o) -> o.getTotalPagado(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // Acciones
        grid.addColumn(new ComponentRenderer<>(recibo -> {
            HorizontalLayout acciones = new HorizontalLayout();

            // (Antes navegaba a una ruta de órdenes de compra que no existe)
            Button btnVer = new Button(VaadinIcon.EYE.create(), e -> verDetalle(recibo));
            btnVer.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnVer.setTooltipText("Ver detalle del recibo");

            Button btnAnular = new Button(VaadinIcon.BAN.create(), e -> confirmarAnulacion(recibo));
            btnAnular.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            boolean anulable = !esAnulado(recibo) && !esAutomatico(recibo);
            btnAnular.setEnabled(anulable);
            btnAnular.setTooltipText(esAutomatico(recibo)
                    ? "Recibo automático: se revierte anulando o devolviendo la factura"
                    : "Anular recibo");

            acciones.add(btnVer, btnAnular);
            return acciones;
        })).setHeader("Acciones").setKey("acciones").setSortable(false);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        dataViewRecibos = grid.setItems(listaRecibos);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        EstiloConsulta.aplicarGrid(gridDetalle);
        gridDetalle.setWidthFull();
        gridDetalle.setMinHeight("300px");
        gridDetalle.setHeight("50vh");

        // (Antes mostraba el objeto factura completo como texto)
        colDescripcion = gridDetalle.addColumn(d -> {
            FacturaDeVenta f = d.getFactura();
            return f != null ? "Factura #" + f.getCodigo() : "";
        })
                .setHeader("Factura de venta")
                .setKey("factura")
                .setFooter("FACTURAS:");

        EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.getBalancePendienteAntes())))
                .setHeader("Debía antes")
                .setKey("bant")
                .setComparator(Comparator.comparing(
                        (DetalleReciboDeIngreso d) -> valor(d.getBalancePendienteAntes())));

        colDetTotal = EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.getTotal())))
                .setHeader("Cobrado")
                .setKey("total")
                .setComparator(Comparator.comparing((DetalleReciboDeIngreso d) -> valor(d.getTotal())));

        gridDetalle.addComponentColumn(d -> EstiloConsulta.pendiente(d.getBalancePendienteDespues(), false))
                .setHeader("Quedó pendiente")
                .setKey("bpdes")
                .setTextAlign(ColumnTextAlign.END)
                .setComparator(Comparator.comparing((DetalleReciboDeIngreso d) -> valor(d.getBalancePendienteDespues())));

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    // ============================================================
    // ANULACIÓN
    // ============================================================
    /**
     * Anula con el servicio completo: las facturas vuelven a deber lo que el
     * recibo les pagó, se reversa el estado de cuenta del cliente y, si el
     * cobro entró a la caja, el efectivo sale de la caja abierta. Antes solo
     * marcaba el recibo como anulado y las facturas quedaban como pagadas.
     */
    private void confirmarAnulacion(ReciboDeIngreso recibo) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Anular recibo de ingreso #" + recibo.getCodigo());
        dialog.setWidth("460px");

        Span texto = new Span("Las facturas volverán a deber lo que este recibo cobró (RD$ "
                + String.format("%,.2f", valor(recibo.getTotalPagado())) + ") y el balance de "
                + nombreCliente(recibo) + " aumentará"
                + (recibo.getCajaChica() != null ? "; ese efectivo saldrá de la caja abierta." : ".")
                + " Esta acción no se puede deshacer.");
        TextArea motivo = new TextArea("Motivo de la anulación");
        motivo.setWidthFull();
        motivo.setRequired(true);
        dialog.add(new VerticalLayout(texto, motivo));

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        Button anular = new Button("Sí, anular", VaadinIcon.BAN.create(), e -> {
            if (motivo.isEmpty() || motivo.getValue().isBlank()) {
                motivo.setInvalid(true);
                motivo.setErrorMessage("Indique el motivo");
                return;
            }
            try {
                reciboDeIngresoService.anularReciboDeIngreso(recibo.getCodigo(), motivo.getValue().trim(), "Admin");
                Notification.show("Recibo #" + recibo.getCodigo() + " anulado.", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                aplicarFiltros();
            } catch (Exception ex) {
                Notification.show("No se pudo anular el recibo: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        anular.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(cancelar, anular);
        dialog.open();
        motivo.focus();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private boolean esAnulado(ReciboDeIngreso r) {
        return Boolean.TRUE.equals(r.getAnulado());
    }

    /** Recibo generado por el POS o por una liquidación de delivery. */
    private boolean esAutomatico(ReciboDeIngreso r) {
        String c = r.getConcepto() != null ? r.getConcepto() : "";
        return c.startsWith(CONCEPTO_RECIBO_POS) || c.startsWith(CONCEPTO_RECIBO_LIQUIDACION);
    }

    private Span chipEstado(ReciboDeIngreso r) {
        return esAnulado(r) ? EstiloConsulta.chipAnulada() : EstiloConsulta.chip("VIGENTE", "fv-chip-pagada");
    }

    /** De dónde salió el recibo: venta POS, delivery, caja (efectivo) u otra forma. */
    private Span chipOrigen(ReciboDeIngreso r) {
        String c = r.getConcepto() != null ? r.getConcepto() : "";
        if (c.startsWith(CONCEPTO_RECIBO_POS)) {
            return EstiloConsulta.chip("VENTA POS", "fv-chip-contado");
        }
        if (c.startsWith(CONCEPTO_RECIBO_LIQUIDACION)) {
            return EstiloConsulta.chip("DELIVERY", "fv-chip-delivery");
        }
        return r.getCajaChica() != null
                ? EstiloConsulta.chip("EFECTIVO", "fv-chip-contado")
                : EstiloConsulta.chip("ABONO CxC", "fv-chip-credito");
    }

    private String nombreCliente(ReciboDeIngreso r) {
        return r.getCliente() != null && r.getCliente().getNombre() != null ? r.getCliente().getNombre() : "-";
    }

    private BigDecimal valor(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private boolean dentroDeRango(LocalDate fecha) {

        if (fecha == null) {
            return dtDesde.getValue() == null && dtHasta.getValue() == null;
        }
        if (dtDesde.getValue() != null && fecha.isBefore(dtDesde.getValue())) {
            return false;
        }
        return dtHasta.getValue() == null || !fecha.isAfter(dtHasta.getValue());
    }

    private LocalDate aLocalDate(Date fecha) {

        if (fecha == null) {
            return null;
        }
        // new Date(getTime()) evita problemas si llega un java.sql.Date
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}
