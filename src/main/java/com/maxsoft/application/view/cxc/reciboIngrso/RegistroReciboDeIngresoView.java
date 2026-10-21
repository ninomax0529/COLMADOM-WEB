package com.maxsoft.application.view.cxc.reciboIngrso;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@PermitAll
@PageTitle("Registro Recibo de Egreso")
@Route(value = "cxc/registroReciboIngreso")
public class RegistroReciboDeIngresoView extends VerticalLayout {

    private static final String USUARIO_ACTUAL = "ADMIN";

    private static final String FORMA_EFECTIVO = "EFECTIVO";

    private static final List<String> FORMAS_PAGO = List.of(
            FORMA_EFECTIVO, "TRANSFERENCIA", "CHEQUE", "TARJETA");

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat FORMATO_NUMERO = new DecimalFormat("#,##0.00");

    private final ReciboDeIngresoService reciboService;
    private final ClienteService clienteService;
    FacturaDeVentaService facturaDeVentaService;

    private final Grid<LineaPago> grid = new Grid<>(LineaPago.class, false);

    private final ComboBox<Cliente> cbProveedor = new ComboBox<>("Clienter");
    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final ComboBox<String> cbFormaPago = new ComboBox<>("Forma de Pago");
    private final TextField txtReferencia = new TextField("N° Cheque / Referencia");
    private final TextField txtObservacion = new TextField("Observación");
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);

    private Button btnPagarTodo;
    private Button btnQuitarSeleccion;
    private final Span lblResumen = new Span();

    private final List<LineaPago> lineas = new ArrayList<>();

    // Columnas con pie (totales)
    private Grid.Column<LineaPago> colNumero;
    private Grid.Column<LineaPago> colTotalFactura;
    private Grid.Column<LineaPago> colBalance;
    private Grid.Column<LineaPago> colMonto;
    private Grid.Column<LineaPago> colRestante;

    // Evita que los cambios hechos por código disparen de nuevo los listeners
    private boolean actualizando = false;

    @Autowired
    public RegistroReciboDeIngresoView(ReciboDeIngresoService reciboService,
           ClienteService clienteService,
              FacturaDeVentaService facturaDeVentaService
    ) {

        this.reciboService = reciboService;
        this.clienteService = clienteService;
        this.facturaDeVentaService=facturaDeVentaService;

        setSizeFull();
        configurarBotonera();
        configurarControlesSuperiores();
        configurarGrid();

        HorizontalLayout hlDatos = new HorizontalLayout(
                cbProveedor, dpFecha, cbFormaPago, txtReferencia, txtObservacion, botonera);
        hlDatos.setAlignItems(Alignment.BASELINE);
        hlDatos.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout hlAcciones = crearBarraAcciones();

        add(hlDatos, hlAcciones, grid);

        actualizarTotales();
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> procesarGuardado());
        botonera.getCancelar().addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
    }

    private void configurarControlesSuperiores() {

        cbProveedor.setItems(this.clienteService.getLista());
        cbProveedor.setItemLabelGenerator(p -> p.getCedula()!= null ? p.getNombre() + " (" + p.getCedula() + ")" : p.getNombre());
        cbProveedor.setAllowCustomValue(false);
        cbProveedor.setRequired(true);
        cbProveedor.setClearButtonVisible(true);
        cbProveedor.setWidth("320px");
        cbProveedor.setPlaceholder("Seleccione el cliente...");
        cbProveedor.addValueChangeListener(e -> cargarPendientes());

        dpFecha.setValue(LocalDate.now());

        cbFormaPago.setItems(FORMAS_PAGO);
        cbFormaPago.setAllowCustomValue(false);
        cbFormaPago.setRequired(true);
        cbFormaPago.setValue(FORMA_EFECTIVO);

        txtReferencia.setWidth("200px");
        txtReferencia.setPlaceholder("Cheque, transferencia...");

        txtObservacion.setWidth("240px");
    }

    private HorizontalLayout crearBarraAcciones() {

        btnPagarTodo = new Button("Pagar todas (total)", VaadinIcon.CHECK_SQUARE_O.create(), e -> pagarTodas());
        btnPagarTodo.addThemeVariants(ButtonVariant.LUMO_SMALL);

        btnQuitarSeleccion = new Button("Quitar selección", VaadinIcon.CLOSE_SMALL.create(), e -> quitarSeleccion());
        btnQuitarSeleccion.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        lblResumen.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "1.1rem")
                .set("color", "var(--lumo-primary-text-color)");

        HorizontalLayout barra = new HorizontalLayout(btnPagarTodo, btnQuitarSeleccion, lblResumen);
        barra.setWidthFull();
        barra.setAlignItems(FlexComponent.Alignment.CENTER);
        barra.expand(lblResumen);
        lblResumen.getStyle().set("text-align", "right");
        return barra;
    }

    // ============================================================
    // FACTURAS PENDIENTES
    // ============================================================
    /**
     * Trae las facturas pendientes de pago del suplidor, de la más antigua a la
     * más reciente.
     */
    private void cargarPendientes() {

        lineas.clear();

        Cliente cliente = cbProveedor.getValue();

        if (cliente != null) {

            try {

                List<FacturaDeVenta> pendientes = this.facturaDeVentaService.getListaFactPendiente(cliente.getCodigo());

                lineas.addAll(pendientes.stream()
                        .filter(f -> balanceDe(f).signum() > 0)
                        .sorted(Comparator.comparing(
                                (FacturaDeVenta f) -> aLocalDate(f.getFecha()),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(LineaPago::new)
                        .collect(Collectors.toList()));

                if (lineas.isEmpty()) {
                    Notification.show("El suplidor no tiene facturas pendientes de pago.", 3000, Position.MIDDLE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Error consultando facturas pendientes: " + ex.getMessage(), 4000, Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }

        grid.setItems(lineas);
        actualizarTotales();
    }

    /**
     * Balance pendiente de la factura. Es el único lugar que lee ese campo.
     */
    private BigDecimal balanceDe(FacturaDeVenta factura) {

        return valor(factura.getTotalPendiente());
    }

    private void pagarTodas() {

        if (lineas.isEmpty()) {
            return;
        }
        actualizando = true;
        try {
            for (LineaPago l : lineas) {
                l.aplicarMonto(l.balance);
            }
        } finally {
            actualizando = false;
        }
        actualizarTotales();
    }

    private void quitarSeleccion() {

        actualizando = true;
        try {
            for (LineaPago l : lineas) {
                l.aplicarMonto(BigDecimal.ZERO);
            }
        } finally {
            actualizando = false;
        }
        actualizarTotales();
    }

    // ============================================================
    // GRID
    // ============================================================
    private void configurarGrid() {

        grid.setHeightFull();
        grid.setWidthFull();
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.addColumn(new ComponentRenderer<>(l -> l.chkPagar))
                .setHeader("Pagar")
                .setAutoWidth(true)
                .setSortable(false);

        colNumero = grid.addColumn(l -> l.factura.getNumeroDocumento())
                .setHeader("N° Factura")
                .setKey("numero")
                .setAutoWidth(true)
                .setFooter("TOTAL: 0 facturas");

        grid.addColumn(l -> l.factura.getNcf())
                .setHeader("NCF")
                .setKey("ncf")
                .setAutoWidth(true);

        grid.addColumn(l -> {
            LocalDate f = aLocalDate(l.factura.getFecha());
            return f != null ? f.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setAutoWidth(true);

        colTotalFactura = grid.addColumn(l -> moneda(l.factura.getTotal()))
                .setHeader("Total Factura")
                .setKey("total")
                .setAutoWidth(true)
                .setFooter("RD$ 0.00");

        colBalance = grid.addColumn(l -> moneda(l.balance))
                .setHeader("Balance Pendiente")
                .setKey("balance")
                .setAutoWidth(true)
                .setFooter("RD$ 0.00");

        colMonto = grid.addColumn(new ComponentRenderer<>(l -> l.txtMonto))
                .setHeader("Monto a Pagar")
                .setKey("monto")
                .setAutoWidth(true)
                .setSortable(false)
                .setFooter("RD$ 0.00");

        colRestante = grid.addColumn(new ComponentRenderer<>(l -> l.lblRestante))
                .setHeader("Balance Restante")
                .setKey("restante")
                .setAutoWidth(true)
                .setSortable(false)
                .setFooter("RD$ 0.00");

        grid.setItems(lineas);
    }

    private void actualizarTotales() {

        BigDecimal totalFacturas = lineas.stream().map(l -> valor(l.factura.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBalance = lineas.stream().map(l -> l.balance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPagar = totalAPagar();
        long cantidadPagar = lineas.stream().filter(l -> l.monto.signum() > 0).count();

        colNumero.setFooter("TOTAL: " + lineas.size() + (lineas.size() == 1 ? " factura" : " facturas"));
        colTotalFactura.setFooter(moneda(totalFacturas));
        colBalance.setFooter(moneda(totalBalance));
        colMonto.setFooter(moneda(totalPagar));
        colRestante.setFooter(moneda(totalBalance.subtract(totalPagar)));

        lblResumen.setText("Total a pagar: " + moneda(totalPagar)
                + "  (" + cantidadPagar + (cantidadPagar == 1 ? " factura" : " facturas") + ")");
    }

    private BigDecimal totalAPagar() {

        return lineas.stream().map(l -> l.monto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ============================================================
    // LÍNEA DE PAGO (una factura pendiente)
    // ============================================================
    /**
     * Una factura pendiente con sus controles de pago. Los componentes se crean
     * una sola vez por línea, así escribir un monto no reconstruye la fila ni
     * hace perder el cursor.
     */
    private class LineaPago {

        final FacturaDeVenta factura;
        final BigDecimal balance;
        final Checkbox chkPagar = new Checkbox();
        final BigDecimalField txtMonto = new BigDecimalField();
        final Span lblRestante = new Span();
        BigDecimal monto = BigDecimal.ZERO;

        LineaPago(FacturaDeVenta factura) {

            this.factura = factura;
            this.balance = balanceDe(factura);

            txtMonto.setWidth("150px");
            txtMonto.setValue(BigDecimal.ZERO);
            txtMonto.setPlaceholder("0.00");

            // Marcar = pago total; desmarcar = sin pago
            chkPagar.addValueChangeListener(e -> {
                if (actualizando) {
                    return;
                }
                aplicarMonto(e.getValue() ? balance : BigDecimal.ZERO);
                actualizarTotales();
            });

            // Monto escrito = pago total o parcial
            txtMonto.addValueChangeListener(e -> {
                if (actualizando) {
                    return;
                }
                BigDecimal v = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;

                if (v.signum() < 0) {
                    Notification.show("El monto a pagar no puede ser negativo.", 2500, Position.MIDDLE);
                    v = BigDecimal.ZERO;
                } else if (v.compareTo(balance) > 0) {
                    Notification.show("El monto no puede superar el balance pendiente (" + moneda(balance) + ").",
                            3000, Position.MIDDLE);
                    v = balance;
                }

                aplicarMonto(v);
                actualizarTotales();
            });

            aplicarMonto(BigDecimal.ZERO);
        }

        /**
         * Fija el monto de la línea y sincroniza checkbox, campo y balance
         * restante sin disparar los listeners.
         */
        void aplicarMonto(BigDecimal nuevo) {

            boolean anterior = actualizando;
            actualizando = true;
            try {
                monto = nuevo;
                txtMonto.setValue(nuevo);
                chkPagar.setValue(nuevo.signum() > 0);
                lblRestante.setText(moneda(balance.subtract(nuevo)));
            } finally {
                actualizando = anterior;
            }
        }
    }

    // ============================================================
    // GUARDADO
    // ============================================================
    private void procesarGuardado() {

        if (cbProveedor.getValue() == null) {
            Notification.show("Debe seleccionar el suplidor", 3000, Position.TOP_CENTER);
            cbProveedor.focus();
            return;
        }

        if (dpFecha.getValue() == null) {
            Notification.show("Debe indicar la fecha del recibo", 3000, Position.TOP_CENTER);
            dpFecha.focus();
            return;
        }

        if (cbFormaPago.getValue() == null) {
            Notification.show("Debe seleccionar la forma de pago", 3000, Position.TOP_CENTER);
            cbFormaPago.focus();
            return;
        }

        if (!FORMA_EFECTIVO.equals(cbFormaPago.getValue())
                && (txtReferencia.getValue() == null || txtReferencia.getValue().isBlank())) {
            Notification.show("Indique el número de cheque o la referencia del pago", 3500, Position.TOP_CENTER);
            txtReferencia.focus();
            return;
        }

        List<LineaPago> aPagar = lineas.stream()
                .filter(l -> l.monto.signum() > 0)
                .collect(Collectors.toList());

        if (aPagar.isEmpty()) {
            Notification.show("Seleccione al menos una factura a pagar", 3000, Position.TOP_CENTER);
            return;
        }

        for (LineaPago l : aPagar) {
            if (l.monto.compareTo(l.balance) > 0) {
                Notification.show("El pago de la factura " + l.factura.getNumeroDocumento()
                        + " supera su balance pendiente.", 4000, Position.TOP_CENTER);
                return;
            }
        }

        try {
            
            ReciboDeIngreso recibo = new ReciboDeIngreso();
            recibo.setCliente(cbProveedor.getValue());
            recibo.setFecha(ClaseUtil.asDate(dpFecha.getValue()));
//            recibo.setFormaPago(cbFormaPago.getValue());
//            recibo.setReferencia(txtReferencia.getValue() != null ? txtReferencia.getValue().trim() : null);
            recibo.setConcepto(txtObservacion.getValue());
            recibo.setTotalPagado(totalAPagar());

            List<DetalleReciboDeIngreso> detalles = new ArrayList<>();

            for (LineaPago l : aPagar) {
                DetalleReciboDeIngreso d = new DetalleReciboDeIngreso();
                d.setCodigo(null);
                d.setReciboIngreso(recibo);
                d.setFactura(l.factura);
                d.setBalancePendienteAntes(l.balance);
                d.setTotal(l.monto);
                d.setBalancePendienteDespues(l.balance.subtract(l.monto));
                detalles.add(d);
            }

            recibo.setDetalleReciboDeIngresoCollection(detalles);

            ReciboDeIngreso guardado = this.reciboService.guardar(recibo);

            Notification.show("Recibo de egreso #" + guardado.getCodigo() + " registrado por "
                    + moneda(guardado.getTotalPagado()) + ".", 4000, Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            limpiarDespuesDeGuardar();

        } catch (IllegalStateException | IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 5000, Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error al guardar el recibo de egreso: " + e.getMessage(), 4000, Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Se mantiene el suplidor y se recargan sus facturas pendientes (los
     * balances ya cambiaron), por si se quiere seguir pagando.
     */
    private void limpiarDespuesDeGuardar() {

        txtReferencia.clear();
        txtObservacion.clear();
        dpFecha.setValue(LocalDate.now());
        cbFormaPago.setValue(FORMA_EFECTIVO);
        cargarPendientes();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private LocalDate aLocalDate(Date fecha) {

        if (fecha == null) {
            return null;
        }
        // new Date(getTime()) evita problemas si llega un java.sql.Date
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private BigDecimal valor(BigDecimal v) {

        return v != null ? v : BigDecimal.ZERO;
    }

    private String moneda(BigDecimal v) {

        return "RD$ " + FORMATO_NUMERO.format(valor(v));
    }
}
