package com.maxsoft.application.view.cxc.estadoCuenta;

import com.maxsoft.application.dto.ResumenCreditoDTO;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.CuentaPorCobrar;
import com.maxsoft.application.modelo.HistorialLimiteCredito;
import com.maxsoft.application.servicio.interfaces.cxc.CuentaPorCobrarService;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

/**
 * Estado de cuenta del cliente: facturas a crédito, pagos y balance, más el
 * histórico de cambios de su límite de crédito.
 */
@Route(value = "cxc/estadoCuenta")
@PageTitle("Estado de Cuenta")
public class EstadoDeCuentaView extends VerticalLayout {

    private final CuentaPorCobrarService cuentaPorCobrarService;
    private final ClienteService clienteService;

    private final ComboBox<Cliente> cbCliente = new ComboBox<>("Cliente");
    private final Div tarjetas = new Div();

    private final Grid<CuentaPorCobrar> gridMovimientos = new Grid<>(CuentaPorCobrar.class, false);
    private final Grid<HistorialLimiteCredito> gridHistorial = new Grid<>(HistorialLimiteCredito.class, false);

    private Grid.Column<CuentaPorCobrar> colFecha;
    private Grid.Column<CuentaPorCobrar> colDebito;
    private Grid.Column<CuentaPorCobrar> colCredito;

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

    public EstadoDeCuentaView(CuentaPorCobrarService cuentaPorCobrarService, ClienteService clienteService) {

        this.cuentaPorCobrarService = cuentaPorCobrarService;
        this.clienteService = clienteService;

        setSizeFull();
        setSpacing(false);

        cbCliente.setItems(clienteService.getLista().stream()
                .sorted(Comparator.comparing(c -> c.getNombre() != null ? c.getNombre() : ""))
                .toList());
        cbCliente.setItemLabelGenerator(c -> c.getCodigo() + " - " + c.getNombre());
        cbCliente.setPlaceholder("Buscar cliente...");
        cbCliente.setPrefixComponent(VaadinIcon.USER.create());
        cbCliente.setClearButtonVisible(true);
        cbCliente.setWidth("380px");
        cbCliente.addValueChangeListener(e -> cargar(e.getValue()));

        tarjetas.addClassName("fv-tarjetas");

        configurarGridMovimientos();
        configurarGridHistorial();

        TabSheet tabs = new TabSheet();
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.LIST, "Movimientos"), gridMovimientos);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.CHART_LINE, "Histórico de límite"), gridHistorial);
        tabs.setSizeFull();
        EstiloConsulta.aplicarVista(this, tabs);

        H2 titulo = new H2("Estado de cuenta del cliente");
        titulo.addClassName("fv-titulo");

        HorizontalLayout barra = new HorizontalLayout(cbCliente,
                EstiloConsulta.ayuda("Débito = lo que el cliente pasa a deber · Crédito = lo que paga o se le rebaja"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");

        add(titulo, barra, tarjetas, tabs);
        setFlexGrow(1, tabs);

        limpiarResumen();
    }

    /** Tarjeta con el estilo de las consultas pero con texto libre (p. ej. "Sin límite"). */
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

    private void configurarGridMovimientos() {

        EstiloConsulta.aplicarGrid(gridMovimientos);
        colFecha = gridMovimientos.addColumn(m -> m.getFecha() != null ? df.format(m.getFecha()) : "")
                .setHeader("Fecha").setAutoWidth(true);
        gridMovimientos.addComponentColumn(m -> chipTipo(m.getTipoMovimiento())).setHeader("Tipo").setAutoWidth(true);
        gridMovimientos.addColumn(CuentaPorCobrar::getNumeroDocumento).setHeader("Documento").setAutoWidth(true);
        gridMovimientos.addColumn(CuentaPorCobrar::getConcepto).setHeader("Concepto").setFlexGrow(1);
        colDebito = EstiloConsulta.monto(gridMovimientos.addColumn(m -> monto(m.getDebito())))
                .setHeader("Débito").setAutoWidth(true);
        colCredito = EstiloConsulta.monto(gridMovimientos.addColumn(m -> monto(m.getCredito())))
                .setHeader("Crédito").setAutoWidth(true);
        gridMovimientos.addComponentColumn(m -> EstiloConsulta.pendiente(m.getBalanceNuevo(), false))
                .setHeader("Balance")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END).setAutoWidth(true);
        gridMovimientos.addColumn(CuentaPorCobrar::getUsuario).setHeader("Usuario").setAutoWidth(true);
        gridMovimientos.setSizeFull();
    }

    private void configurarGridHistorial() {

        EstiloConsulta.aplicarGrid(gridHistorial);
        gridHistorial.addColumn(h -> h.getFecha() != null ? df.format(h.getFecha()) : "")
                .setHeader("Fecha").setAutoWidth(true);
        EstiloConsulta.monto(gridHistorial.addColumn(h -> limite(h.getLimiteAnterior())))
                .setHeader("Límite anterior").setAutoWidth(true);
        EstiloConsulta.monto(gridHistorial.addColumn(h -> limite(h.getLimiteNuevo())))
                .setHeader("Límite nuevo").setAutoWidth(true);
        gridHistorial.addColumn(HistorialLimiteCredito::getMotivo).setHeader("Motivo").setFlexGrow(1);
        gridHistorial.addColumn(HistorialLimiteCredito::getUsuario).setHeader("Usuario").setAutoWidth(true);
        gridHistorial.setSizeFull();
    }

    private void cargar(Cliente cliente) {

        if (cliente == null) {
            gridMovimientos.setItems(List.of());
            gridHistorial.setItems(List.of());
            limpiarResumen();
            return;
        }

        ResumenCreditoDTO r = cuentaPorCobrarService.obtenerResumen(cliente.getCodigo());
        BigDecimal balance = r.balance() != null ? r.balance() : BigDecimal.ZERO;

        tarjetas.removeAll();
        tarjetas.add(
                tarjeta("Límite de crédito", r.tieneLimite() ? EstiloConsulta.moneda(r.limite()) : "Sin límite",
                        "fv-tarjeta-total", VaadinIcon.CREDIT_CARD),
                EstiloConsulta.tarjetaPendiente("Balance (debe)", balance),
                tarjeta("Disponible", r.tieneLimite() ? EstiloConsulta.moneda(r.disponible()) : "—",
                        "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE));

        List<CuentaPorCobrar> movimientos = cuentaPorCobrarService.getEstadoDeCuenta(cliente.getCodigo());
        gridMovimientos.setItems(movimientos);
        gridHistorial.setItems(cuentaPorCobrarService.getHistorialLimite(cliente.getCodigo()));

        colFecha.setFooter("MOVIMIENTOS: " + movimientos.size());
        colDebito.setFooter(EstiloConsulta.moneda(movimientos.stream()
                .map(m -> m.getDebito() != null ? m.getDebito() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        colCredito.setFooter(EstiloConsulta.moneda(movimientos.stream()
                .map(m -> m.getCredito() != null ? m.getCredito() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private void limpiarResumen() {
        tarjetas.removeAll();
        tarjetas.add(
                tarjeta("Límite de crédito", "—", "fv-tarjeta-total", VaadinIcon.CREDIT_CARD),
                tarjeta("Balance (debe)", "—", "fv-tarjeta-total", VaadinIcon.CLOCK),
                tarjeta("Disponible", "—", "fv-tarjeta-total", VaadinIcon.CHECK_CIRCLE));
        colFecha.setFooter("");
        colDebito.setFooter("");
        colCredito.setFooter("");
    }

    private static Span chipTipo(String tipo) {
        if (tipo == null) {
            return new Span("");
        }
        return switch (tipo) {
            case CuentaPorCobrar.FACTURA -> EstiloConsulta.chip("FACTURA", "fv-chip-credito");
            case CuentaPorCobrar.RECIBO -> EstiloConsulta.chip("PAGO", "fv-chip-pagada");
            case CuentaPorCobrar.ANULACION_FACTURA -> EstiloConsulta.chip("ANULACIÓN FACT.", "fv-chip-anulada");
            case CuentaPorCobrar.ANULACION_RECIBO -> EstiloConsulta.chip("ANULACIÓN PAGO", "fv-chip-pendiente");
            case CuentaPorCobrar.REVERSO_CREDITO -> EstiloConsulta.chip("VUELVE A CONTADO", "fv-chip-anulada");
            case CuentaPorCobrar.DEVOLUCION -> EstiloConsulta.chip("DEVOLUCIÓN", "fv-chip-devuelto");
            case CuentaPorCobrar.AJUSTE_DEBITO -> EstiloConsulta.chip("AJUSTE (+)", "fv-chip-pendiente");
            case CuentaPorCobrar.AJUSTE_CREDITO -> EstiloConsulta.chip("AJUSTE (-)", "fv-chip-contado");
            default -> EstiloConsulta.chip(tipo, "fv-chip-delivery");
        };
    }

    private static String monto(BigDecimal v) {
        return v == null || v.signum() == 0 ? "" : EstiloConsulta.moneda(v);
    }

    private static String limite(BigDecimal v) {
        return v == null ? "Sin límite" : EstiloConsulta.moneda(v);
    }
}
