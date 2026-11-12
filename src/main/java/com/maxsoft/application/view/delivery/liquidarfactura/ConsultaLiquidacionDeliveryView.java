package com.maxsoft.application.view.delivery.liquidarfactura;

import com.maxsoft.application.dto.DetalleLiquidacionDTO;
import com.maxsoft.application.dto.LiquidacionResumenDTO;
import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.CajaTurno;
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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Consulta de liquidaciones de delivery: ver su detalle, reimprimir el
 * comprobante y anularlas (las facturas vuelven a quedar pendientes del
 * motorista y el efectivo sale de la caja).
 */
@PermitAll
@PageTitle("Liquidaciones de Delivery | MaxSoft ERP")
@Route(value = "delivery/consultaLiquidaciones")
public class ConsultaLiquidacionDeliveryView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private static final String TODAS = "Todas";
    private static final String VIGENTES = "Vigentes";
    private static final String ANULADAS = "Anuladas";
    private static final String CON_DIFERENCIA = "Con sobrante/faltante";

    private final LiquidacionDeliveryService liquidacionService;
    private final DeliveryService deliveryService;
    private final CajaService cajaService;

    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final ComboBox<Delivery> cbMotorista = new ComboBox<>("Motorista");
    private final ComboBox<String> cbEstado = new ComboBox<>("Estado");
    private final TextField txtBuscar = new TextField();

    private final Div resumen = new Div();
    private final Grid<LiquidacionResumenDTO> grid = new Grid<>(LiquidacionResumenDTO.class, false);
    private final Grid<DetalleLiquidacionDTO> gridDetalle = new Grid<>(DetalleLiquidacionDTO.class, false);
    private final TabSheet tabs = new TabSheet();
    private final Div cabeceraDetalle = new Div();
    private final Div tarjetasDetalle = new Div();
    private final List<LiquidacionResumenDTO> lista = new ArrayList<>();
    private GridListDataView<LiquidacionResumenDTO> dataView;

    private Grid.Column<LiquidacionResumenDTO> colCodigo;
    private Grid.Column<LiquidacionResumenDTO> colTotal;
    private Grid.Column<LiquidacionResumenDTO> colEfectivo;
    private Grid.Column<DetalleLiquidacionDTO> colDetFactura;
    private Grid.Column<DetalleLiquidacionDTO> colDetMonto;

    @Autowired
    public ConsultaLiquidacionDeliveryView(LiquidacionDeliveryService liquidacionService,
            DeliveryService deliveryService, CajaService cajaService) {

        this.liquidacionService = liquidacionService;
        this.deliveryService = deliveryService;
        this.cajaService = cajaService;

        setSizeFull();
        setSpacing(false);
        tabs.setSizeFull();
        EstiloConsulta.aplicarVista(this, tabs);

        configurarGrid();
        configurarGridDetalle();

        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.WALLET, "Liquidaciones"), grid);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle"),
                EstiloConsulta.panelDetalle(cabeceraDetalle, tarjetasDetalle, gridDetalle));

        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");

        H2 titulo = new H2("Liquidaciones de Delivery");
        titulo.addClassName("fv-titulo");

        add(titulo, crearFiltros(), resumen, tabs);
        setFlexGrow(1, tabs);

        cbMotorista.setItems(deliveryService.getLista());
        limpiarDetalle();
        buscar();
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private HorizontalLayout crearFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(7));
        dpHasta.setValue(LocalDate.now());

        cbMotorista.setItemLabelGenerator(Delivery::getNombre);
        cbMotorista.setClearButtonVisible(true);
        cbMotorista.setPlaceholder("Todos");

        cbEstado.setItems(TODAS, VIGENTES, ANULADAS, CON_DIFERENCIA);
        cbEstado.setAllowCustomValue(false);
        cbEstado.setValue(TODAS);
        cbEstado.addValueChangeListener(e -> aplicarFiltros());

        txtBuscar.setWidth("240px");
        txtBuscar.setPlaceholder("No., motorista o cajero...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltros());

        Button btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        HorizontalLayout barra = new HorizontalLayout(dpDesde, dpHasta, cbMotorista, cbEstado, txtBuscar, btnBuscar,
                EstiloConsulta.ayuda("Doble clic en una liquidación para ver sus facturas"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    // ============================================================
    // GRID PRINCIPAL
    // ============================================================
    private void configurarGrid() {

        EstiloConsulta.aplicarGrid(grid);
        grid.setSizeFull();
        grid.setPartNameGenerator(l -> l.anulada() ? "fv-fila-anulada" : null);
        dataView = grid.setItems(lista);

        colCodigo = grid.addColumn(l -> "#" + l.codigo()).setHeader("No.").setAutoWidth(true)
                .setComparator(Comparator.comparing(LiquidacionResumenDTO::codigo));
        grid.addColumn(l -> fecha(l.fecha())).setHeader("Fecha").setAutoWidth(true)
                .setComparator(Comparator.comparing(LiquidacionResumenDTO::fecha));
        grid.addColumn(LiquidacionResumenDTO::motorista).setHeader("Motorista").setAutoWidth(true).setSortable(true);
        grid.addColumn(LiquidacionResumenDTO::usuario).setHeader("Cajero").setAutoWidth(true).setSortable(true);
        grid.addColumn(l -> l.pedidos() + (l.aCredito() > 0 ? " (" + l.aCredito() + " a crédito)" : ""))
                .setHeader("Pedidos").setAutoWidth(true)
                .setComparator(Comparator.comparingInt(LiquidacionResumenDTO::pedidos));
        colTotal = EstiloConsulta.monto(grid.addColumn(l -> EstiloConsulta.moneda(l.totalLiquidado())))
                .setHeader("Total").setAutoWidth(true)
                .setComparator(Comparator.comparing(LiquidacionResumenDTO::totalLiquidado));
        EstiloConsulta.monto(grid.addColumn(l -> EstiloConsulta.moneda(l.efectivoEsperado())))
                .setHeader("A cobrar").setAutoWidth(true);
        colEfectivo = EstiloConsulta.monto(grid.addColumn(l -> EstiloConsulta.moneda(l.efectivoRecibido())))
                .setHeader("Efectivo").setAutoWidth(true);
        EstiloConsulta.monto(grid.addColumn(l -> l.transferenciaRecibida().signum() > 0
                ? EstiloConsulta.moneda(l.transferenciaRecibida()) : "—"))
                .setHeader("Transferencia").setAutoWidth(true);
        grid.addComponentColumn(this::chipDiferencia).setHeader("Diferencia").setAutoWidth(true)
                .setComparator(Comparator.comparing(LiquidacionResumenDTO::diferencia));
        grid.addComponentColumn(l -> l.anulada()
                ? EstiloConsulta.chip("ANULADA", "fv-chip-anulada")
                : EstiloConsulta.chip("VIGENTE", "fv-chip-pagada"))
                .setHeader("Estado").setAutoWidth(true)
                .setComparator(Comparator.comparing(LiquidacionResumenDTO::anulada));

        grid.addComponentColumn(l -> {
            Button ver = new Button(VaadinIcon.EYE.create(), e -> mostrarDetalle(l));
            ver.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            ver.setTooltipText("Ver las facturas");
            Button imprimir = new Button(VaadinIcon.PRINT.create(), e -> imprimir(l));
            imprimir.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            imprimir.setTooltipText("Reimprimir comprobante");
            Button anular = new Button(VaadinIcon.BAN.create(), e -> confirmarAnulacion(l));
            anular.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            anular.setTooltipText(l.anulada() ? "Ya está anulada" : "Anular liquidación");
            anular.setEnabled(!l.anulada());
            HorizontalLayout acciones = new HorizontalLayout(ver, imprimir, anular);
            acciones.setSpacing(false);
            return acciones;
        }).setHeader("Acciones").setAutoWidth(true);

        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() != null) {
                mostrarDetalle(e.getItem());
            }
        });
    }

    private Span chipDiferencia(LiquidacionResumenDTO l) {
        BigDecimal d = l.diferencia();
        if (d.signum() == 0) {
            return EstiloConsulta.chip("CUADRE", "fv-chip-pagada");
        }
        if (d.signum() > 0) {
            return EstiloConsulta.chip("SOBRANTE " + EstiloConsulta.moneda(d), "fv-chip-pendiente");
        }
        return EstiloConsulta.chip("FALTANTE " + EstiloConsulta.moneda(d.abs())
                + (l.faltanteCargado().signum() > 0 ? " · AL MOTORISTA" : ""), "fv-chip-anulada");
    }

    // ============================================================
    // DETALLE
    // ============================================================
    private void configurarGridDetalle() {

        EstiloConsulta.aplicarGrid(gridDetalle);
        gridDetalle.setWidthFull();
        gridDetalle.setMinHeight("300px");
        gridDetalle.setHeight("50vh");

        colDetFactura = gridDetalle.addColumn(d -> "#" + d.codigoFactura()).setHeader("Factura");
        gridDetalle.addColumn(d -> fecha(d.fechaFactura())).setHeader("Fecha factura");
        gridDetalle.addColumn(DetalleLiquidacionDTO::cliente).setHeader("Cliente").setFlexGrow(2);
        gridDetalle.addComponentColumn(d -> d.clienteCredito() != null
                ? EstiloConsulta.chip("A CRÉDITO: " + d.clienteCredito(), "fv-chip-credito")
                : PedidoDeliveryDTO.FIADO.equals(d.tipoPago())
                ? EstiloConsulta.chip("FIADO", "fv-chip-credito")
                : EstiloConsulta.chip("CONTADO", "fv-chip-contado")).setHeader("Pago");
        colDetMonto = EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.monto())))
                .setHeader("Monto");
        gridDetalle.getColumns().forEach(c -> c.setAutoWidth(true));
    }

    private void mostrarDetalle(LiquidacionResumenDTO l) {

        try {
            List<DetalleLiquidacionDTO> detalles = liquidacionService.getDetalleLiquidacion(l.codigo());
            gridDetalle.setItems(detalles);
            colDetFactura.setFooter("FACTURAS: " + detalles.size());
            colDetMonto.setFooter(EstiloConsulta.moneda(detalles.stream().map(DetalleLiquidacionDTO::monto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)));

            EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle · Liquidación #" + l.codigo());
            String sub = l.motorista() + "  ·  " + fecha(l.fecha()) + "  ·  Cajero: " + texto(l.usuario());
            if (l.anulada()) {
                sub += "  ·  Anulada " + fecha(l.fechaAnulacion()) + " por " + texto(l.usuarioAnulacion())
                        + ": " + texto(l.motivoAnulacion());
            } else if (l.observacion() != null && !l.observacion().isBlank()) {
                sub += "  ·  " + l.observacion();
            }
            EstiloConsulta.cabecera(cabeceraDetalle, "Liquidación #" + l.codigo(), sub,
                    l.anulada() ? EstiloConsulta.chip("ANULADA", "fv-chip-anulada")
                    : EstiloConsulta.chip("VIGENTE", "fv-chip-pagada"),
                    chipDiferencia(l));

            tarjetasDetalle.removeAll();
            tarjetasDetalle.add(
                    EstiloConsulta.tarjeta("Total liquidado", l.totalLiquidado(), "fv-tarjeta-total", VaadinIcon.WALLET),
                    EstiloConsulta.tarjeta("Fiado / crédito", l.totalFiado(), "fv-tarjeta-devuelto", VaadinIcon.BOOK),
                    EstiloConsulta.tarjeta("A cobrar", l.efectivoEsperado(), "fv-tarjeta-pendiente", VaadinIcon.MONEY),
                    EstiloConsulta.tarjeta("Efectivo", l.efectivoRecibido(), "fv-tarjeta-pagado", VaadinIcon.MONEY_DEPOSIT));
            if (l.transferenciaRecibida().signum() > 0) {
                tarjetasDetalle.add(EstiloConsulta.tarjeta("Transferencia"
                        + (l.referenciaTransferencia() != null ? " (" + l.referenciaTransferencia() + ")" : ""),
                        l.transferenciaRecibida(), "fv-tarjeta-pagado", VaadinIcon.EXCHANGE));
            }
            tabs.setSelectedIndex(1);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            notificar("No se pudo cargar el detalle: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarDetalle() {
        gridDetalle.setItems(new ArrayList<>());
        colDetFactura.setFooter("FACTURAS: 0");
        colDetMonto.setFooter("");
        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle");
        tabs.setSelectedIndex(0);
        EstiloConsulta.cabeceraVacia(cabeceraDetalle,
                "Haga doble clic en una liquidación para ver sus facturas.");
        tarjetasDetalle.removeAll();
    }

    // ============================================================
    // REIMPRIMIR / ANULAR
    // ============================================================
    private void imprimir(LiquidacionResumenDTO l) {
        try {
            ImpresionHtmlUtil.imprimir(UI.getCurrent(), liquidacionService.generarComprobanteHtml(l.codigo()));
        } catch (Exception ex) {
            ex.printStackTrace();
            notificar("No se pudo imprimir: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmarAnulacion(LiquidacionResumenDTO l) {

        CajaTurno caja = cajaService.obtenerCajaAbierta().orElse(null);
        if (caja == null && l.efectivoRecibido().signum() > 0) {
            notificar("Abra una caja: el efectivo de la liquidación sale de la gaveta.", NotificationVariant.LUMO_WARNING);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.addClassName("dc-dialogo");
        dialog.setHeaderTitle("Anular liquidación #" + l.codigo());
        dialog.setWidth("500px");
        dialog.setCloseOnOutsideClick(false);

        Div consecuencias = new Div();
        consecuencias.getStyle().set("font-size", "var(--lumo-font-size-s)").set("line-height", "1.5");
        consecuencias.add(new Div(new Span("• Las " + l.pedidos() + " facturas vuelven a quedar pendientes de "
                + l.motorista() + ".")));
        if (l.efectivoRecibido().signum() > 0) {
            consecuencias.add(new Div(new Span("• Salen " + EstiloConsulta.moneda(l.efectivoRecibido())
                    + " de la caja abierta.")));
        }
        if (l.aCredito() > 0) {
            consecuencias.add(new Div(new Span("• " + l.aCredito()
                    + " factura(s) pasada(s) a crédito vuelven a contado y salen del estado de cuenta del cliente.")));
        }
        if (l.faltanteCargado().signum() > 0) {
            consecuencias.add(new Div(new Span("• Se le rebajan " + EstiloConsulta.moneda(l.faltanteCargado())
                    + " de faltante al motorista.")));
        }
        if (l.transferenciaRecibida().signum() > 0) {
            consecuencias.add(new Div(new Span("• La transferencia (" + EstiloConsulta.moneda(l.transferenciaRecibida())
                    + ") queda registrada solo en esta liquidación anulada; se vuelve a declarar al liquidar de nuevo.")));
        }

        TextArea motivo = new TextArea("Motivo de la anulación");
        motivo.setWidthFull();
        motivo.setMaxLength(300);
        motivo.setRequired(true);

        Div cuerpo = new Div(DialogoCajaEstilo.banda(VaadinIcon.BAN, l.motorista(),
                fecha(l.fecha()) + " · " + EstiloConsulta.moneda(l.totalLiquidado())), consecuencias, motivo);
        cuerpo.addClassName("dc-cuerpo");
        dialog.add(cuerpo);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addClassName("dc-btn-secundario");
        Button confirmar = new Button("Anular liquidación", VaadinIcon.BAN.create());
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmar.setDisableOnClick(true);
        confirmar.addClickListener(e -> {
            if (motivo.getValue().isBlank()) {
                notificar("Indique el motivo de la anulación.", NotificationVariant.LUMO_WARNING);
                motivo.focus();
                confirmar.setEnabled(true);
                return;
            }
            try {
                liquidacionService.anularLiquidacion(l.codigo(), motivo.getValue(), usuarioActual());
                dialog.close();
                notificar("Liquidación #" + l.codigo() + " anulada. Las facturas volvieron a pendientes.",
                        NotificationVariant.LUMO_SUCCESS);
                buscar();
            } catch (IllegalStateException | IllegalArgumentException ex) {
                notificar(ex.getMessage(), NotificationVariant.LUMO_WARNING);
                confirmar.setEnabled(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                notificar("Error al anular: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
                confirmar.setEnabled(true);
            }
        });
        dialog.getFooter().add(cancelar, confirmar);
        dialog.open();
        motivo.focus();
    }

    private String usuarioActual() {
        return cajaService.obtenerCajaAbierta()
                .map(CajaTurno::getCajeroApertura)
                .filter(c -> !c.isBlank())
                .orElse("ADMIN");
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
            Delivery motorista = cbMotorista.getValue();
            List<LiquidacionResumenDTO> resultado = liquidacionService.getLiquidaciones(
                    aDate(dpDesde.getValue()), aDate(dpHasta.getValue().plusDays(1)),
                    motorista != null ? motorista.getCodigo() : null);
            lista.clear();
            lista.addAll(resultado);
            dataView.refreshAll();
            limpiarDetalle();
            aplicarFiltros();
        } catch (Exception ex) {
            ex.printStackTrace();
            notificar("Error consultando liquidaciones: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void aplicarFiltros() {

        String estado = cbEstado.getValue() != null ? cbEstado.getValue() : TODAS;
        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataView.setFilter(l -> switch (estado) {
            case VIGENTES -> !l.anulada();
            case ANULADAS -> l.anulada();
            case CON_DIFERENCIA -> !l.anulada() && l.diferencia().signum() != 0;
            default -> true;
        } && (term.isEmpty()
                || String.valueOf(l.codigo()).contains(term)
                || texto(l.motorista()).toLowerCase().contains(term)
                || texto(l.usuario()).toLowerCase().contains(term)));

        actualizarResumen();
    }

    /** Los montos suman solo las liquidaciones vigentes. */
    private void actualizarResumen() {

        List<LiquidacionResumenDTO> visibles = dataView.getItems().toList();
        List<LiquidacionResumenDTO> vigentes = visibles.stream().filter(l -> !l.anulada()).toList();

        BigDecimal total = sumar(vigentes, LiquidacionResumenDTO::totalLiquidado);
        BigDecimal efectivo = sumar(vigentes, LiquidacionResumenDTO::efectivoRecibido);
        BigDecimal transferencia = sumar(vigentes, LiquidacionResumenDTO::transferenciaRecibida);
        BigDecimal faltantes = vigentes.stream().map(LiquidacionResumenDTO::diferencia)
                .filter(d -> d.signum() < 0).map(BigDecimal::abs).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sobrantes = vigentes.stream().map(LiquidacionResumenDTO::diferencia)
                .filter(d -> d.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);

        resumen.removeAll();
        resumen.add(
                tarjeta("Liquidaciones", vigentes.size() + (visibles.size() > vigentes.size()
                        ? " (+" + (visibles.size() - vigentes.size()) + " anul.)" : ""), "fv-tarjeta-total", VaadinIcon.WALLET),
                tarjeta("Total liquidado", EstiloConsulta.moneda(total), "fv-tarjeta-total", VaadinIcon.TRUCK),
                tarjeta("Efectivo", EstiloConsulta.moneda(efectivo), "fv-tarjeta-pagado", VaadinIcon.MONEY),
                tarjeta("Transferencias", EstiloConsulta.moneda(transferencia), "fv-tarjeta-pagado", VaadinIcon.EXCHANGE),
                tarjeta("Faltantes", EstiloConsulta.moneda(faltantes),
                        faltantes.signum() > 0 ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada", VaadinIcon.ARROW_DOWN),
                tarjeta("Sobrantes", EstiloConsulta.moneda(sobrantes), "fv-tarjeta-devuelto", VaadinIcon.ARROW_UP));

        colCodigo.setFooter("TOTAL: " + visibles.size());
        colTotal.setFooter(EstiloConsulta.moneda(total));
        colEfectivo.setFooter(EstiloConsulta.moneda(efectivo));
    }

    private BigDecimal sumar(List<LiquidacionResumenDTO> items,
            java.util.function.Function<LiquidacionResumenDTO, BigDecimal> campo) {
        return items.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
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
    private Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String fecha(Date fecha) {
        if (fecha == null) {
            return "";
        }
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).format(FORMATO_FECHA_HORA);
    }

    private static String texto(String s) {
        return s != null ? s : "";
    }

    private void notificar(String mensaje, NotificationVariant variante) {
        Notification.show(mensaje, 5000, Notification.Position.MIDDLE).addThemeVariants(variante);
    }
}
