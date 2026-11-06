package com.maxsoft.application.view.cxc.estadoCuenta;

import com.maxsoft.application.dto.ResumenCreditoDTO;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.CuentaPorCobrar;
import com.maxsoft.application.modelo.HistorialLimiteCredito;
import com.maxsoft.application.servicio.interfaces.cxc.CuentaPorCobrarService;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
    private final Span lblLimite = new Span();
    private final Span lblBalance = new Span();
    private final Span lblDisponible = new Span();

    private final Grid<CuentaPorCobrar> gridMovimientos = new Grid<>(CuentaPorCobrar.class, false);
    private final Grid<HistorialLimiteCredito> gridHistorial = new Grid<>(HistorialLimiteCredito.class, false);

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

    public EstadoDeCuentaView(CuentaPorCobrarService cuentaPorCobrarService, ClienteService clienteService) {

        this.cuentaPorCobrarService = cuentaPorCobrarService;
        this.clienteService = clienteService;

        setSizeFull();

        cbCliente.setItems(clienteService.getLista().stream()
                .sorted(Comparator.comparing(c -> c.getNombre() != null ? c.getNombre() : ""))
                .toList());
        cbCliente.setItemLabelGenerator(c -> c.getCodigo() + " - " + c.getNombre());
        cbCliente.setPlaceholder("Buscar cliente...");
        cbCliente.setWidth("380px");
        cbCliente.addValueChangeListener(e -> cargar(e.getValue()));

        HorizontalLayout resumen = new HorizontalLayout(
                tarjeta("Límite de crédito", lblLimite),
                tarjeta("Balance (debe)", lblBalance),
                tarjeta("Disponible", lblDisponible));
        resumen.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);

        configurarGridMovimientos();
        configurarGridHistorial();

        TabSheet tabs = new TabSheet();
        tabs.add("Movimientos", gridMovimientos);
        tabs.add("Histórico de límite", gridHistorial);
        tabs.setSizeFull();

        add(new H2("Estado de cuenta del cliente"), cbCliente, resumen, tabs);
        setFlexGrow(1, tabs);

        limpiarResumen();
    }

    private Div tarjeta(String titulo, Span valor) {
        Span t = new Span(titulo);
        t.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
        valor.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("font-weight", "600");
        Div d = new Div(t, new Div(valor));
        d.getStyle()
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("min-width", "180px");
        return d;
    }

    private void configurarGridMovimientos() {

        gridMovimientos.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        gridMovimientos.addColumn(m -> m.getFecha() != null ? df.format(m.getFecha()) : "")
                .setHeader("Fecha").setAutoWidth(true);
        gridMovimientos.addColumn(m -> tipoLegible(m.getTipoMovimiento())).setHeader("Tipo").setAutoWidth(true);
        gridMovimientos.addColumn(CuentaPorCobrar::getNumeroDocumento).setHeader("Documento").setAutoWidth(true);
        gridMovimientos.addColumn(CuentaPorCobrar::getConcepto).setHeader("Concepto").setFlexGrow(1);
        gridMovimientos.addColumn(m -> monto(m.getDebito())).setHeader("Débito")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END).setAutoWidth(true);
        gridMovimientos.addColumn(m -> monto(m.getCredito())).setHeader("Crédito")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END).setAutoWidth(true);
        gridMovimientos.addColumn(m -> fmt(m.getBalanceNuevo())).setHeader("Balance")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END).setAutoWidth(true);
        gridMovimientos.addColumn(CuentaPorCobrar::getUsuario).setHeader("Usuario").setAutoWidth(true);
        gridMovimientos.setSizeFull();
    }

    private void configurarGridHistorial() {

        gridHistorial.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        gridHistorial.addColumn(h -> h.getFecha() != null ? df.format(h.getFecha()) : "")
                .setHeader("Fecha").setAutoWidth(true);
        gridHistorial.addColumn(h -> limite(h.getLimiteAnterior())).setHeader("Límite anterior").setAutoWidth(true);
        gridHistorial.addColumn(h -> limite(h.getLimiteNuevo())).setHeader("Límite nuevo").setAutoWidth(true);
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
        lblLimite.setText(r.tieneLimite() ? "RD$ " + fmt(r.limite()) : "Sin límite");
        lblBalance.setText("RD$ " + fmt(r.balance()));
        lblDisponible.setText(r.tieneLimite() ? "RD$ " + fmt(r.disponible()) : "—");
        lblBalance.getStyle().set("color", r.balance().signum() > 0
                ? "var(--lumo-error-text-color)" : "var(--lumo-success-text-color)");

        gridMovimientos.setItems(cuentaPorCobrarService.getEstadoDeCuenta(cliente.getCodigo()));
        gridHistorial.setItems(cuentaPorCobrarService.getHistorialLimite(cliente.getCodigo()));
    }

    private void limpiarResumen() {
        lblLimite.setText("—");
        lblBalance.setText("—");
        lblDisponible.setText("—");
        lblBalance.getStyle().remove("color");
    }

    private static String tipoLegible(String tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case CuentaPorCobrar.FACTURA -> "Factura";
            case CuentaPorCobrar.RECIBO -> "Pago";
            case CuentaPorCobrar.ANULACION_FACTURA -> "Anulación";
            case CuentaPorCobrar.DEVOLUCION -> "Devolución";
            case CuentaPorCobrar.AJUSTE_DEBITO -> "Ajuste (+)";
            case CuentaPorCobrar.AJUSTE_CREDITO -> "Ajuste (-)";
            default -> tipo;
        };
    }

    private static String monto(BigDecimal v) {
        return v == null || v.signum() == 0 ? "" : fmt(v);
    }

    private static String limite(BigDecimal v) {
        return v == null ? "Sin límite" : fmt(v);
    }

    private static String fmt(BigDecimal v) {
        return String.format("%,.2f", v != null ? v : BigDecimal.ZERO);
    }
}
