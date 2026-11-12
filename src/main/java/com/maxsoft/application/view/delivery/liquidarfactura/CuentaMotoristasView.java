package com.maxsoft.application.view.delivery.liquidarfactura;

import com.maxsoft.application.dto.CuentaMotoristaResumenDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.CuentaMotorista;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.delivery.LiquidacionDeliveryService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.util.ImpresionHtmlUtil;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.venta.cajaChica.DialogoCajaEstilo;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Cuenta de los motoristas: cuánto debe cada uno por faltantes en sus
 * liquidaciones, su historial (faltantes, abonos y reversos) y el cobro de
 * la deuda en efectivo.
 */
@PermitAll
@PageTitle("Cuenta de Motoristas | MaxSoft ERP")
@Route(value = "delivery/cuentaMotoristas")
public class CuentaMotoristasView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    private final LiquidacionDeliveryService liquidacionService;
    private final DeliveryService deliveryService;
    private final CajaService cajaService;

    private final Checkbox chkSoloDeuda = new Checkbox("Solo los que deben", false);
    private final TextField txtBuscar = new TextField();
    private final Div resumen = new Div();
    private final TabSheet tabs = new TabSheet();
    private final Grid<CuentaMotoristaResumenDTO> grid = new Grid<>(CuentaMotoristaResumenDTO.class, false);
    private final Grid<CuentaMotorista> gridMovimientos = new Grid<>(CuentaMotorista.class, false);
    private final Div cabeceraDetalle = new Div();
    private final Div tarjetasDetalle = new Div();
    private final List<CuentaMotoristaResumenDTO> lista = new ArrayList<>();
    private GridListDataView<CuentaMotoristaResumenDTO> dataView;

    private Grid.Column<CuentaMotoristaResumenDTO> colNombre;
    private Grid.Column<CuentaMotoristaResumenDTO> colCargos;
    private Grid.Column<CuentaMotoristaResumenDTO> colDescargos;
    private Grid.Column<CuentaMotoristaResumenDTO> colSaldo;
    private Grid.Column<CuentaMotorista> colMovFecha;
    private Grid.Column<CuentaMotorista> colMovMonto;

    @Autowired
    public CuentaMotoristasView(LiquidacionDeliveryService liquidacionService,
            DeliveryService deliveryService, CajaService cajaService) {

        this.liquidacionService = liquidacionService;
        this.deliveryService = deliveryService;
        this.cajaService = cajaService;

        setSizeFull();
        setSpacing(false);
        tabs.setSizeFull();
        EstiloConsulta.aplicarVista(this, tabs);

        configurarGrid();
        configurarGridMovimientos();

        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.USERS, "Motoristas"), grid);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.BOOK_DOLLAR, "Movimientos"),
                EstiloConsulta.panelDetalle(cabeceraDetalle, tarjetasDetalle, gridMovimientos));

        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");

        H2 titulo = new H2("Cuenta de Motoristas");
        titulo.addClassName("fv-titulo");

        add(titulo, crearFiltros(), resumen, tabs);
        setFlexGrow(1, tabs);

        limpiarDetalle();
        cargar();
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private HorizontalLayout crearFiltros() {

        txtBuscar.setWidth("260px");
        txtBuscar.setPlaceholder("Buscar motorista o celular...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltros());
        chkSoloDeuda.addValueChangeListener(e -> aplicarFiltros());

        Button btnRefrescar = new Button("Refrescar", VaadinIcon.REFRESH.create(), e -> cargar());

        HorizontalLayout barra = new HorizontalLayout(txtBuscar, chkSoloDeuda, btnRefrescar,
                EstiloConsulta.ayuda("Doble clic en un motorista para ver sus faltantes y abonos"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    // ============================================================
    // GRID DE MOTORISTAS
    // ============================================================
    private void configurarGrid() {

        EstiloConsulta.aplicarGrid(grid);
        grid.setSizeFull();
        dataView = grid.setItems(lista);

        colNombre = grid.addColumn(CuentaMotoristaResumenDTO::nombre).setHeader("Motorista")
                .setAutoWidth(true).setFlexGrow(2).setSortable(true);
        grid.addColumn(r -> texto(r.celular())).setHeader("Celular").setAutoWidth(true);
        colCargos = EstiloConsulta.monto(grid.addColumn(r -> EstiloConsulta.moneda(r.cargos())))
                .setHeader("Faltantes cargados").setAutoWidth(true)
                .setComparator(Comparator.comparing(CuentaMotoristaResumenDTO::cargos));
        colDescargos = EstiloConsulta.monto(grid.addColumn(r -> EstiloConsulta.moneda(r.descargos())))
                .setHeader("Abonado / reversado").setAutoWidth(true)
                .setComparator(Comparator.comparing(CuentaMotoristaResumenDTO::descargos));
        colSaldo = grid.addComponentColumn(this::chipSaldo).setHeader("Saldo").setAutoWidth(true)
                .setComparator(Comparator.comparing(CuentaMotoristaResumenDTO::saldo));
        grid.addColumn(r -> fecha(r.ultimoMovimiento())).setHeader("Último movimiento").setAutoWidth(true)
                .setComparator(Comparator.comparing(CuentaMotoristaResumenDTO::ultimoMovimiento,
                        Comparator.nullsFirst(Comparator.naturalOrder())));

        grid.addComponentColumn(r -> {
            Button ver = new Button(VaadinIcon.EYE.create(), e -> mostrarMovimientos(r));
            ver.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            ver.setTooltipText("Ver movimientos");
            Button cobrar = new Button("Cobrar", VaadinIcon.MONEY_DEPOSIT.create(), e -> abrirDialogoAbono(r));
            cobrar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            cobrar.setEnabled(r.saldo().signum() > 0);
            cobrar.setTooltipText(r.saldo().signum() > 0 ? "El motorista paga en efectivo" : "No tiene deuda");
            HorizontalLayout acciones = new HorizontalLayout(ver, cobrar);
            acciones.setAlignItems(Alignment.CENTER);
            return acciones;
        }).setHeader("Acciones").setAutoWidth(true);

        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() != null) {
                mostrarMovimientos(e.getItem());
            }
        });
    }

    private Span chipSaldo(CuentaMotoristaResumenDTO r) {
        BigDecimal s = r.saldo();
        if (s.signum() > 0) {
            return EstiloConsulta.chip("DEBE " + EstiloConsulta.moneda(s), "fv-chip-anulada");
        }
        if (s.signum() < 0) {
            return EstiloConsulta.chip("A FAVOR " + EstiloConsulta.moneda(s.abs()), "fv-chip-credito");
        }
        return EstiloConsulta.chip("AL DÍA", "fv-chip-pagada");
    }

    // ============================================================
    // MOVIMIENTOS
    // ============================================================
    private void configurarGridMovimientos() {

        EstiloConsulta.aplicarGrid(gridMovimientos);
        gridMovimientos.setWidthFull();
        gridMovimientos.setMinHeight("300px");
        gridMovimientos.setHeight("50vh");

        colMovFecha = gridMovimientos.addColumn(m -> fecha(m.getFecha())).setHeader("Fecha");
        gridMovimientos.addComponentColumn(m -> switch (m.getTipo()) {
            case CuentaMotorista.FALTANTE -> EstiloConsulta.chip("FALTANTE", "fv-chip-anulada");
            case CuentaMotorista.ABONO -> EstiloConsulta.chip("ABONO", "fv-chip-pagada");
            case CuentaMotorista.ANULACION_FALTANTE -> EstiloConsulta.chip("REVERSO", "fv-chip-devuelto");
            default -> EstiloConsulta.chip(m.getTipo(), "fv-chip-contado");
        }).setHeader("Tipo");
        gridMovimientos.addColumn(m -> m.getLiquidacionDelivery() != null
                ? "#" + m.getLiquidacionDelivery().getCodigo() : "—").setHeader("Liquidación");
        gridMovimientos.addColumn(m -> texto(m.getDescripcion())).setHeader("Detalle").setFlexGrow(2);
        colMovMonto = EstiloConsulta.monto(gridMovimientos.addColumn(m -> (valor(m.getMonto()).signum() > 0 ? "+" : "")
                + EstiloConsulta.moneda(m.getMonto()))).setHeader("Monto");
        gridMovimientos.addColumn(m -> texto(m.getNombreUsuario())).setHeader("Usuario");

        gridMovimientos.addComponentColumn(m -> {
            if (!CuentaMotorista.ABONO.equals(m.getTipo())) {
                return new Span("");
            }
            Button imprimir = new Button(VaadinIcon.PRINT.create(), e -> imprimirAbono(m.getCodigo()));
            imprimir.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            imprimir.setTooltipText("Reimprimir recibo del abono");
            return imprimir;
        }).setHeader("Recibo");

        gridMovimientos.getColumns().forEach(c -> c.setAutoWidth(true));
    }

    private void mostrarMovimientos(CuentaMotoristaResumenDTO r) {

        try {
            List<CuentaMotorista> movimientos = liquidacionService.getCuentaMotorista(r.codigo());
            gridMovimientos.setItems(movimientos);
            colMovFecha.setFooter("MOVIMIENTOS: " + movimientos.size());
            colMovMonto.setFooter((r.saldo().signum() >= 0 ? "DEBE: " : "A FAVOR: ")
                    + EstiloConsulta.moneda(r.saldo().abs()));

            EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.BOOK_DOLLAR, "Movimientos · " + r.nombre());
            EstiloConsulta.cabecera(cabeceraDetalle, r.nombre(),
                    (r.celular() != null && !r.celular().isBlank() ? "Cel.: " + r.celular() + "  ·  " : "")
                    + (r.ultimoMovimiento() != null ? "Último movimiento: " + fecha(r.ultimoMovimiento())
                    : "Sin movimientos"),
                    chipSaldo(r));

            tarjetasDetalle.removeAll();
            tarjetasDetalle.add(
                    EstiloConsulta.tarjeta("Faltantes cargados", r.cargos(), "fv-tarjeta-pendiente", VaadinIcon.ARROW_DOWN),
                    EstiloConsulta.tarjeta("Abonado / reversado", r.descargos(), "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE),
                    EstiloConsulta.tarjetaPendiente(r.saldo().signum() >= 0 ? "Debe" : "A favor", r.saldo().abs()));
            if (r.saldo().signum() > 0) {
                Button cobrar = new Button("Cobrar deuda", VaadinIcon.MONEY_DEPOSIT.create(), e -> abrirDialogoAbono(r));
                cobrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
                tarjetasDetalle.add(cobrar);
            }
            tabs.setSelectedIndex(1);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            notificar("No se pudo cargar la cuenta: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarDetalle() {
        gridMovimientos.setItems(new ArrayList<>());
        colMovFecha.setFooter("MOVIMIENTOS: 0");
        colMovMonto.setFooter("");
        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.BOOK_DOLLAR, "Movimientos");
        tabs.setSelectedIndex(0);
        EstiloConsulta.cabeceraVacia(cabeceraDetalle,
                "Haga doble clic en un motorista para ver sus faltantes y abonos.");
        tarjetasDetalle.removeAll();
    }

    // ============================================================
    // COBRO DE LA DEUDA
    // ============================================================
    private void abrirDialogoAbono(CuentaMotoristaResumenDTO r) {

        CajaTurno caja = cajaService.obtenerCajaAbierta().orElse(null);
        if (caja == null) {
            notificar("Abra una caja: el abono del motorista entra a la gaveta.", NotificationVariant.LUMO_WARNING);
            return;
        }
        Delivery motorista = deliveryService.getLista().stream()
                .filter(d -> Objects.equals(d.getCodigo(), r.codigo()))
                .findFirst().orElse(null);
        if (motorista == null) {
            notificar("No se encontró el motorista.", NotificationVariant.LUMO_ERROR);
            return;
        }
        BigDecimal deuda = liquidacionService.getDeudaMotorista(r.codigo());
        if (deuda.signum() <= 0) {
            notificar(r.nombre() + " no tiene deuda pendiente.", NotificationVariant.LUMO_CONTRAST);
            cargar();
            return;
        }

        Dialog dialog = new Dialog();
        dialog.addClassName("dc-dialogo");
        dialog.setHeaderTitle("Cobrar deuda del motorista");
        dialog.setWidth("460px");
        dialog.setCloseOnOutsideClick(false);

        BigDecimalField monto = new BigDecimalField("Monto que paga");
        monto.addClassName("dc-campo-grande");
        monto.setPrefixComponent(new Span("RD$"));
        monto.setWidthFull();
        Div rapidos = new Div();
        rapidos.addClassName("dc-rapidos");
        Button todo = new Button("Toda la deuda", e -> monto.setValue(deuda));
        todo.addClassName("dc-rapido");
        rapidos.add(todo);
        TextField nota = new TextField("Nota (opcional)");
        nota.setWidthFull();
        nota.setMaxLength(200);

        Div cuerpo = new Div(DialogoCajaEstilo.banda(VaadinIcon.USER_CARD, r.nombre(),
                "Faltantes de liquidaciones anteriores"),
                DialogoCajaEstilo.billeteGrande("Deuda actual", MONEDA_FORMAT.format(deuda)),
                monto, rapidos, nota);
        cuerpo.addClassName("dc-cuerpo");
        dialog.add(cuerpo);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addClassName("dc-btn-secundario");
        Button confirmar = new Button("Registrar abono", VaadinIcon.CHECK.create());
        confirmar.addClassName("dc-btn-cerrar");
        confirmar.setDisableOnClick(true);
        confirmar.addClickListener(e -> {
            try {
                String usuario = caja.getCajeroApertura() != null && !caja.getCajeroApertura().isBlank()
                        ? caja.getCajeroApertura() : "ADMIN";
                CuentaMotorista mov = liquidacionService.registrarAbonoMotorista(motorista, monto.getValue(),
                        nota.getValue(), usuario);
                dialog.close();
                notificar("Abono registrado: " + EstiloConsulta.moneda(valor(mov.getMonto()).abs()),
                        NotificationVariant.LUMO_SUCCESS);
                imprimirAbono(mov.getCodigo());
                cargar();
            } catch (IllegalStateException | IllegalArgumentException ex) {
                notificar(ex.getMessage(), NotificationVariant.LUMO_WARNING);
                confirmar.setEnabled(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                notificar("Error al registrar el abono: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
                confirmar.setEnabled(true);
            }
        });
        monto.addKeyDownListener(Key.ENTER, e -> confirmar.click());
        dialog.getFooter().add(cancelar, confirmar);
        dialog.open();
        monto.focus();
    }

    private void imprimirAbono(Integer codigoMovimiento) {
        try {
            ImpresionHtmlUtil.imprimir(UI.getCurrent(), liquidacionService.generarComprobanteAbonoHtml(codigoMovimiento));
        } catch (Exception ex) {
            ex.printStackTrace();
            notificar("No se pudo imprimir: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    // ============================================================
    // CARGA Y RESUMEN
    // ============================================================
    private void cargar() {
        try {
            lista.clear();
            lista.addAll(liquidacionService.getResumenCuentasMotoristas());
            dataView.refreshAll();
            limpiarDetalle();
            aplicarFiltros();
        } catch (Exception ex) {
            ex.printStackTrace();
            notificar("Error consultando las cuentas: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void aplicarFiltros() {

        boolean soloDeuda = chkSoloDeuda.getValue();
        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();
        dataView.setFilter(r -> (!soloDeuda || r.saldo().signum() > 0)
                && (term.isEmpty()
                || texto(r.nombre()).toLowerCase().contains(term)
                || texto(r.celular()).contains(term)));
        actualizarResumen();
    }

    private void actualizarResumen() {

        List<CuentaMotoristaResumenDTO> visibles = dataView.getItems().toList();
        long conDeuda = visibles.stream().filter(r -> r.saldo().signum() > 0).count();
        BigDecimal deudaTotal = visibles.stream().map(CuentaMotoristaResumenDTO::saldo)
                .filter(s -> s.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cargos = visibles.stream().map(CuentaMotoristaResumenDTO::cargos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descargos = visibles.stream().map(CuentaMotoristaResumenDTO::descargos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        resumen.removeAll();
        resumen.add(
                tarjeta("Motoristas", String.valueOf(visibles.size()), "fv-tarjeta-total", VaadinIcon.USERS),
                tarjeta("Con deuda", String.valueOf(conDeuda),
                        conDeuda > 0 ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada", VaadinIcon.WARNING),
                tarjeta("Faltantes cargados", EstiloConsulta.moneda(cargos), "fv-tarjeta-devuelto", VaadinIcon.ARROW_DOWN),
                tarjeta("Abonado / reversado", EstiloConsulta.moneda(descargos), "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE),
                tarjeta("Deuda total", EstiloConsulta.moneda(deudaTotal),
                        deudaTotal.signum() > 0 ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada", VaadinIcon.WALLET));

        colNombre.setFooter("MOTORISTAS: " + visibles.size());
        colCargos.setFooter(EstiloConsulta.moneda(cargos));
        colDescargos.setFooter(EstiloConsulta.moneda(descargos));
        colSaldo.setFooter("DEBEN: " + EstiloConsulta.moneda(deudaTotal));
    }

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
    // UTILIDADES
    // ============================================================
    private String fecha(Date fecha) {
        if (fecha == null) {
            return "—";
        }
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).format(FORMATO_FECHA_HORA);
    }

    private static String texto(String s) {
        return s != null ? s : "";
    }

    private static BigDecimal valor(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private void notificar(String mensaje, NotificationVariant variante) {
        Notification.show(mensaje, 5000, Notification.Position.MIDDLE).addThemeVariants(variante);
    }
}
