package com.maxsoft.application.view.delivery.liquidarfactura;

import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.dto.PendientesMotoristaDTO;
import com.maxsoft.application.dto.ResumenCreditoDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.LiquidacionDelivery;
import com.maxsoft.application.servicio.interfaces.cxc.CuentaPorCobrarService;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.delivery.LiquidacionDeliveryService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.util.ImpresionHtmlUtil;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.venta.cajaChica.DialogoCajaEstilo;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Cuadre y liquidación de motoristas: el efectivo que entregan entra a la caja
 * abierta y las facturas de contado pueden quedarse a crédito de un cliente.
 * Se puede liquidar un pedido, los seleccionados o todo el lote.
 */
@PageTitle("Liquidación de Delivery")
@Route(value = "delivery/liquidacionDelivery")
public class LiquidacionDeliveryView extends VerticalLayout {

    /** Si no hay caja abierta (no hay cajero), se registra con este usuario. */
    private static final String USUARIO_POR_DEFECTO = "ADMIN";
    /** Minutos en ruta a partir de los cuales el pedido se marca en rojo. */
    private static final long MINUTOS_ALERTA = 60;

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    private final LiquidacionDeliveryService liquidacionService;
    private final DeliveryService deliveryService;
    private final ClienteService clienteService;
    private final CuentaPorCobrarService cuentaPorCobrarService;
    private final CajaService cajaService;

    private final ComboBox<Delivery> motoristaCombo = new ComboBox<>("Motorista");
    private final Grid<PedidoDeliveryDTO> gridPedidos = new Grid<>(PedidoDeliveryDTO.class, false);
    private final Div avisoCaja = new Div();
    private final Div resumen = new Div();

    private final Button btnLiquidarSeleccion = new Button("Liquidar seleccionados", VaadinIcon.CHECK_SQUARE_O.create());
    private final Button btnLiquidarTodo = new Button("Liquidar todo el lote", VaadinIcon.CHECK_CIRCLE.create());

    private Grid.Column<PedidoDeliveryDTO> colFactura;
    private Grid.Column<PedidoDeliveryDTO> colMonto;

    private List<PedidoDeliveryDTO> pedidosActuales = new ArrayList<>();
    private Map<Integer, PendientesMotoristaDTO> pendientesPorMotorista = new HashMap<>();
    private CajaTurno cajaAbierta;
    /** Evita recargar pedidos mientras se refresca la lista de motoristas. */
    private boolean refrescandoMotoristas;

    @Autowired
    public LiquidacionDeliveryView(LiquidacionDeliveryService liquidacionService,
            DeliveryService deliveryService,
            ClienteService clienteService,
            CuentaPorCobrarService cuentaPorCobrarService,
            CajaService cajaService) {

        this.liquidacionService = liquidacionService;
        this.deliveryService = deliveryService;
        this.clienteService = clienteService;
        this.cuentaPorCobrarService = cuentaPorCobrarService;
        this.cajaService = cajaService;

        setSizeFull();
        setSpacing(false);
        EstiloConsulta.aplicarVista(this, null);

        H2 titulo = new H2("Cuadre y Liquidación de Motoristas");
        titulo.addClassName("fv-titulo");

        configurarAvisoCaja();
        configurarMotoristas();
        configurarGrid();
        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");

        Button btnRefrescar = new Button("Refrescar", VaadinIcon.REFRESH.create(), e -> refrescarTodo());

        HorizontalLayout barra = new HorizontalLayout(motoristaCombo, btnRefrescar,
                EstiloConsulta.ayuda("Marque las casillas para liquidar solo algunos pedidos"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");

        btnLiquidarSeleccion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnLiquidarSeleccion.addClickListener(e -> abrirDialogoLiquidacion(
                new ArrayList<>(gridPedidos.getSelectedItems())));
        btnLiquidarTodo.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnLiquidarTodo.addClickListener(e -> abrirDialogoLiquidacion(pedidosActuales));

        HorizontalLayout acciones = new HorizontalLayout(btnLiquidarSeleccion, btnLiquidarTodo);
        acciones.setWidthFull();
        acciones.setJustifyContentMode(JustifyContentMode.END);
        acciones.getStyle().set("padding-top", "var(--lumo-space-s)");

        add(titulo, avisoCaja, barra, resumen, gridPedidos, acciones);
        setFlexGrow(1, gridPedidos);

        verificarCaja();
        cargarMotoristas();
        actualizarTotales();
    }

    // ============================================================
    // CAJA
    // ============================================================
    private void configurarAvisoCaja() {

        Icon ic = VaadinIcon.WARNING.create();
        Span texto = new Span("No hay una caja abierta. Abra la caja en el punto de venta para poder liquidar: "
                + "el efectivo del motorista entra a la gaveta.");
        Button reintentar = new Button("Verificar de nuevo", VaadinIcon.REFRESH.create(), e -> {
            verificarCaja();
            if (cajaAbierta != null) {
                notificar("Caja abierta: ya puede liquidar.", NotificationVariant.LUMO_SUCCESS);
            }
        });
        reintentar.addThemeVariants(ButtonVariant.LUMO_SMALL);

        HorizontalLayout fila = new HorizontalLayout(ic, texto, reintentar);
        fila.setAlignItems(Alignment.CENTER);
        fila.setFlexGrow(1, texto);
        fila.setWidthFull();
        avisoCaja.add(fila);
        avisoCaja.setWidthFull();
        avisoCaja.getStyle()
                .set("box-sizing", "border-box")
                .set("padding", "8px 14px")
                .set("margin-bottom", "8px")
                .set("border-radius", "10px")
                .set("background", "#fdecea")
                .set("border", "1px solid #f3c2bd")
                .set("color", "#b3261e")
                .set("font-weight", "600");
    }

    /** Consulta la caja abierta (antes solo se sabía al confirmar la liquidación). */
    private void verificarCaja() {
        try {
            cajaAbierta = cajaService.obtenerCajaAbierta().orElse(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            cajaAbierta = null;
        }
        avisoCaja.setVisible(cajaAbierta == null);
        actualizarBotones();
        // Los botones "Liquidar" de cada fila dependen de la caja
        gridPedidos.getDataProvider().refreshAll();
    }

    /** Quién liquida: el cajero del turno abierto (antes siempre "ADMIN"). */
    private String usuarioActual() {
        if (cajaAbierta != null && cajaAbierta.getCajeroApertura() != null
                && !cajaAbierta.getCajeroApertura().isBlank()) {
            return cajaAbierta.getCajeroApertura();
        }
        return USUARIO_POR_DEFECTO;
    }

    // ============================================================
    // MOTORISTAS
    // ============================================================
    private void configurarMotoristas() {

        motoristaCombo.setItemLabelGenerator(this::etiquetaMotorista);
        motoristaCombo.setWidth("420px");
        motoristaCombo.setClearButtonVisible(true);
        motoristaCombo.setPlaceholder("Seleccione un motorista...");
        motoristaCombo.setPrefixComponent(VaadinIcon.TRUCK.create());
        motoristaCombo.addValueChangeListener(e -> {
            if (!refrescandoMotoristas) {
                cargarPedidosPendientes();
            }
        });
    }

    /** "JUAN — 4 pedidos · RD$ 3,250.00" para saber quién tiene pendientes. */
    private String etiquetaMotorista(Delivery d) {
        PendientesMotoristaDTO p = pendientesPorMotorista.getOrDefault(d.getCodigo(), PendientesMotoristaDTO.VACIO);
        if (p.pedidos() == 0) {
            return d.getNombre() + " — sin pendientes";
        }
        return d.getNombre() + " — " + p.pedidos() + (p.pedidos() == 1 ? " pedido" : " pedidos")
                + " · " + moneda(p.monto());
    }

    /** Recarga motoristas (con sus pendientes) conservando el seleccionado. */
    private void cargarMotoristas() {

        try {
            pendientesPorMotorista = liquidacionService.getPendientesPorMotorista();
        } catch (Exception ex) {
            ex.printStackTrace();
            pendientesPorMotorista = new HashMap<>();
        }

        Integer seleccionado = motoristaCombo.getValue() != null ? motoristaCombo.getValue().getCodigo() : null;

        // Primero los que tienen pedidos pendientes (más pedidos arriba), luego por nombre
        List<Delivery> motoristas = deliveryService.getLista().stream()
                .sorted(Comparator
                        .comparingLong((Delivery d) -> -pendientesPorMotorista
                        .getOrDefault(d.getCodigo(), PendientesMotoristaDTO.VACIO).pedidos())
                        .thenComparing(d -> d.getNombre() != null ? d.getNombre() : ""))
                .toList();

        refrescandoMotoristas = true;
        try {
            motoristaCombo.setItems(motoristas);
            if (seleccionado != null) {
                motoristas.stream()
                        .filter(d -> Objects.equals(d.getCodigo(), seleccionado))
                        .findFirst()
                        .ifPresent(motoristaCombo::setValue);
            }
        } finally {
            refrescandoMotoristas = false;
        }
    }

    private void refrescarTodo() {
        verificarCaja();
        cargarMotoristas();
        cargarPedidosPendientes();
    }

    // ============================================================
    // GRID
    // ============================================================
    private void configurarGrid() {

        EstiloConsulta.aplicarGrid(gridPedidos);
        gridPedidos.setSelectionMode(Grid.SelectionMode.MULTI);
        gridPedidos.setSizeFull();
        gridPedidos.setMinHeight("260px");

        colFactura = gridPedidos.addColumn(p -> "#" + p.getNumeroFactura())
                .setHeader("Factura").setAutoWidth(true)
                .setComparator(Comparator.comparing(PedidoDeliveryDTO::getCodigoFactura));

        gridPedidos.addColumn(p -> salida(p.getFechaSalida()))
                .setHeader("Salida").setAutoWidth(true)
                .setComparator(Comparator.comparing(PedidoDeliveryDTO::getFechaSalida,
                        Comparator.nullsLast(Comparator.naturalOrder())));

        gridPedidos.addComponentColumn(this::chipEnRuta)
                .setHeader("En ruta").setAutoWidth(true)
                .setComparator(Comparator.comparingLong(this::minutosEnRuta));

        // (Antes mostraba el objeto Cliente como texto)
        gridPedidos.addColumn(PedidoDeliveryDTO::getNombreCliente)
                .setHeader("Cliente").setAutoWidth(true).setFlexGrow(2).setSortable(true);

        gridPedidos.addComponentColumn(p -> p.esFiado()
                ? EstiloConsulta.chip("FIADO", "fv-chip-credito")
                : EstiloConsulta.chip("CONTADO", "fv-chip-contado"))
                .setHeader("Pago").setAutoWidth(true)
                .setComparator(Comparator.comparing(PedidoDeliveryDTO::getTipoPago));

        colMonto = EstiloConsulta.monto(gridPedidos.addColumn(p -> EstiloConsulta.moneda(p.getMontoTotal())))
                .setHeader("Total a cobrar").setAutoWidth(true)
                .setComparator(Comparator.comparing(PedidoDeliveryDTO::getMontoTotal));

        gridPedidos.addComponentColumn(pedido -> {
            Button b = new Button("Liquidar", VaadinIcon.CHECK.create(),
                    e -> abrirDialogoLiquidacion(List.of(pedido)));
            b.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            b.setEnabled(cajaAbierta != null);
            return b;
        }).setHeader("Acción").setAutoWidth(true);

        gridPedidos.addSelectionListener(e -> actualizarBotones());
    }

    private String salida(Date fecha) {
        if (fecha == null) {
            return "—";
        }
        LocalDate dia = new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return new SimpleDateFormat(dia.equals(LocalDate.now()) ? "hh:mm a" : "dd/MM hh:mm a").format(fecha);
    }

    private long minutosEnRuta(PedidoDeliveryDTO p) {
        if (p.getFechaSalida() == null) {
            return -1;
        }
        return Math.max(0, (System.currentTimeMillis() - p.getFechaSalida().getTime()) / 60000);
    }

    /** Tiempo desde que salió el pedido; en rojo suave si pasa de una hora. */
    private Span chipEnRuta(PedidoDeliveryDTO p) {
        long min = minutosEnRuta(p);
        if (min < 0) {
            return new Span("—");
        }
        String texto = min < 60 ? min + " min" : (min / 60) + " h " + String.format("%02d", min % 60) + " min";
        return EstiloConsulta.chip(texto, min >= MINUTOS_ALERTA ? "fv-chip-pendiente" : "fv-chip-contado");
    }

    // ============================================================
    // PEDIDOS PENDIENTES
    // ============================================================
    private void cargarPedidosPendientes() {

        Delivery motorista = motoristaCombo.getValue();

        try {
            pedidosActuales = motorista == null
                    ? new ArrayList<>()
                    : new ArrayList<>(liquidacionService.getPedidosPendientes(motorista.getCodigo()));
        } catch (Exception ex) {
            ex.printStackTrace();
            pedidosActuales = new ArrayList<>();
            notificar("Error consultando los pedidos pendientes: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }

        gridPedidos.setItems(pedidosActuales);
        gridPedidos.deselectAll();
        actualizarTotales();

        if (motorista != null && pedidosActuales.isEmpty()) {
            notificar("El motorista no tiene pedidos pendientes de liquidar.", NotificationVariant.LUMO_CONTRAST);
        }
    }

    private void actualizarTotales() {

        BigDecimal efectivo = sumar(pedidosActuales, false);
        BigDecimal fiado = sumar(pedidosActuales, true);

        resumen.removeAll();
        resumen.add(
                tarjeta("Pedidos en ruta", String.valueOf(pedidosActuales.size()), "fv-tarjeta-total", VaadinIcon.TRUCK),
                tarjeta("Efectivo a entregar", moneda(efectivo), "fv-tarjeta-pagado", VaadinIcon.MONEY),
                tarjeta("Fiado en ruta", moneda(fiado), "fv-tarjeta-devuelto", VaadinIcon.BOOK),
                // (Antes decía "Monto Global Liquidado" aunque aún no se había liquidado)
                tarjeta("Total en ruta", moneda(efectivo.add(fiado)),
                        pedidosActuales.isEmpty() ? "fv-tarjeta-saldada" : "fv-tarjeta-pendiente", VaadinIcon.WALLET));

        colFactura.setFooter("PEDIDOS: " + pedidosActuales.size());
        colMonto.setFooter(EstiloConsulta.moneda(efectivo.add(fiado)));
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean hayCaja = cajaAbierta != null;
        int seleccionados = gridPedidos.getSelectedItems().size();
        btnLiquidarSeleccion.setText(seleccionados > 0
                ? "Liquidar seleccionados (" + seleccionados + ")" : "Liquidar seleccionados");
        btnLiquidarSeleccion.setEnabled(hayCaja && seleccionados > 0);
        btnLiquidarTodo.setEnabled(hayCaja && !pedidosActuales.isEmpty());
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
    // LIQUIDACIÓN (un pedido, los seleccionados o todo el lote)
    // ============================================================
    /**
     * Diálogo de cuadre: muestra el efectivo esperado, pide el efectivo
     * recibido (vacío, hay que contarlo) y exige observación si hay sobrante o
     * faltante. Las facturas de contado se pueden dejar a crédito de un
     * cliente; su monto deja de contar como efectivo esperado.
     */
    private void abrirDialogoLiquidacion(List<PedidoDeliveryDTO> pedidos) {

        Delivery motorista = motoristaCombo.getValue();

        if (motorista == null || pedidos == null || pedidos.isEmpty()) {
            notificar("No hay pedidos para liquidar.", NotificationVariant.LUMO_WARNING);
            return;
        }
        verificarCaja();
        if (cajaAbierta == null) {
            notificar("Debe abrir una caja antes de liquidar el delivery.", NotificationVariant.LUMO_WARNING);
            return;
        }

        // codigo de factura -> cliente que se queda a crédito (solo facturas de contado)
        Map<Integer, Cliente> credito = new LinkedHashMap<>();
        Map<Integer, ComboBox<Cliente>> combosCredito = new HashMap<>();
        Map<Integer, ResumenCreditoDTO> cacheCredito = new HashMap<>();
        List<PedidoDeliveryDTO> contado = pedidos.stream().filter(p -> !p.esFiado()).toList();
        BigDecimal totalPedidos = pedidos.stream().map(PedidoDeliveryDTO::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Dialog dialog = new Dialog();
        dialog.addClassName("dc-dialogo");
        dialog.setHeaderTitle(pedidos.size() == 1
                ? "Liquidar pedido #" + pedidos.get(0).getNumeroFactura()
                : "Liquidar " + pedidos.size() + " pedidos");
        dialog.setWidth(contado.isEmpty() ? "480px" : "780px");
        dialog.setCloseOnOutsideClick(false);

        Div cuerpo = new Div();
        cuerpo.addClassName("dc-cuerpo");

        String subtitulo = pedidos.size() == 1
                ? pedidos.get(0).getNombreCliente() + " · " + pedidos.get(0).getTipoPago() + " · " + moneda(totalPedidos)
                : pedidos.size() + " pedidos · Total " + moneda(totalPedidos);
        cuerpo.add(DialogoCajaEstilo.banda(VaadinIcon.TRUCK, motorista.getNombre(), subtitulo));

        Span lblFiado = new Span();
        lblFiado.getStyle().set("color", "#a2531c").set("font-weight", "600");

        Span lblErrorCredito = new Span();
        lblErrorCredito.getStyle().set("color", "#b3261e").set("font-weight", "700");
        lblErrorCredito.setVisible(false);

        Div billete = new Div();

        BigDecimalField efectivoField = new BigDecimalField("Efectivo recibido del motorista");
        efectivoField.addClassName("dc-campo-grande");
        efectivoField.setPrefixComponent(new Span("RD$"));
        efectivoField.setPlaceholder("Cuente el efectivo y digítelo");
        efectivoField.setWidthFull();

        // Botones rápidos: monto exacto o sumar billetes
        BigDecimal[] esperadoActual = {BigDecimal.ZERO};
        Div rapidos = new Div();
        rapidos.addClassName("dc-rapidos");
        Button exacto = new Button("Exacto", e -> efectivoField.setValue(esperadoActual[0]));
        exacto.addClassName("dc-rapido");
        rapidos.add(exacto);
        for (int billeteValor : new int[]{100, 200, 500, 1000, 2000}) {
            Button b = new Button("+" + String.format("%,d", billeteValor), e -> efectivoField.setValue(
                    valor(efectivoField.getValue()).add(new BigDecimal(billeteValor))));
            b.addClassName("dc-rapido");
            rapidos.add(b);
        }
        Button borrar = new Button("Borrar", e -> efectivoField.clear());
        borrar.addClassName("dc-rapido");
        rapidos.add(borrar);

        Span lblDif = new Span();
        lblDif.addClassName("dc-dif-etiqueta");
        Span notaDif = new Span();
        notaDif.addClassName("dc-dif-nota");
        Span valDif = new Span();
        valDif.addClassName("dc-dif-valor");
        Div textosDif = new Div(lblDif, notaDif);
        Div panelDif = new Div(textosDif, valDif);
        panelDif.addClassName("dc-diferencia");

        TextArea txtObservacion = new TextArea("Observación (obligatoria si hay sobrante o faltante)");
        txtObservacion.setWidthFull();
        txtObservacion.setMaxLength(500);

        Runnable mostrarDiferencia = () -> {
            panelDif.removeClassNames("dc-dif-cuadre", "dc-dif-sobrante", "dc-dif-faltante");
            if (efectivoField.getValue() == null) {
                lblDif.setText("Pendiente de contar");
                notaDif.setText("Digite el efectivo que entrega el motorista");
                valDif.setText("");
                return;
            }
            BigDecimal diff = efectivoField.getValue().subtract(esperadoActual[0]);
            valDif.setText(moneda(diff.abs()));
            if (diff.signum() == 0) {
                lblDif.setText("Cuadre exacto");
                notaDif.setText("El efectivo coincide con lo esperado");
                panelDif.addClassName("dc-dif-cuadre");
            } else if (diff.signum() > 0) {
                lblDif.setText("Sobrante");
                notaDif.setText("Entregó más de lo esperado: explique en la observación");
                panelDif.addClassName("dc-dif-sobrante");
            } else {
                lblDif.setText("Faltante");
                notaDif.setText("Entregó menos de lo esperado: explique en la observación");
                panelDif.addClassName("dc-dif-faltante");
            }
        };

        // Recalcula esperado y crédito; NO toca lo que el cajero ya digitó
        Runnable recalcular = () -> {

            BigDecimal fiadoTotal = pedidos.stream()
                    .filter(p -> p.esFiado() || credito.containsKey(p.getCodigoFactura()))
                    .map(PedidoDeliveryDTO::getMontoTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal esperado = totalPedidos.subtract(fiadoTotal);
            esperadoActual[0] = esperado;

            lblFiado.setText("A crédito / fiado (no lo entrega el motorista): " + moneda(fiadoTotal));
            lblFiado.setVisible(fiadoTotal.signum() > 0);

            billete.removeAll();
            billete.add(DialogoCajaEstilo.billeteGrande("Efectivo esperado", MONEDA_FORMAT.format(esperado)));

            boolean hayEfectivo = esperado.signum() > 0;
            efectivoField.setVisible(hayEfectivo);
            rapidos.setVisible(hayEfectivo);
            panelDif.setVisible(hayEfectivo);
            txtObservacion.setVisible(hayEfectivo);

            validarCredito(credito, combosCredito, cacheCredito, pedidos, lblErrorCredito);
            mostrarDiferencia.run();
        };

        efectivoField.addValueChangeListener(e -> mostrarDiferencia.run());

        if (!contado.isEmpty()) {
            cuerpo.add(crearSeccionCredito(contado, credito, combosCredito, cacheCredito, recalcular), lblErrorCredito);
        }
        cuerpo.add(lblFiado, billete, efectivoField, rapidos, panelDif, txtObservacion);
        recalcular.run();

        dialog.add(cuerpo);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());
        btnCancelar.addClassName("dc-btn-secundario");

        Button btnConfirmar = new Button("Confirmar liquidación", VaadinIcon.CHECK.create());
        btnConfirmar.addClassName("dc-btn-cerrar");
        // Evita doble registro por doble clic; se reactiva si la validación falla
        btnConfirmar.setDisableOnClick(true);

        btnConfirmar.addClickListener(e -> {

            BigDecimal esperado = esperadoActual[0];

            if (esperado.signum() > 0 && efectivoField.getValue() == null) {
                notificar("Digite el efectivo que entrega el motorista.", NotificationVariant.LUMO_WARNING);
                efectivoField.focus();
                btnConfirmar.setEnabled(true);
                return;
            }
            BigDecimal recibido = esperado.signum() > 0 ? valor(efectivoField.getValue()) : BigDecimal.ZERO;

            if (recibido.signum() < 0) {
                notificar("El efectivo recibido no puede ser negativo.", NotificationVariant.LUMO_WARNING);
                btnConfirmar.setEnabled(true);
                return;
            }
            if (lblErrorCredito.isVisible()) {
                notificar(lblErrorCredito.getText(), NotificationVariant.LUMO_WARNING);
                btnConfirmar.setEnabled(true);
                return;
            }
            if (recibido.compareTo(esperado) != 0 && txtObservacion.getValue().isBlank()) {
                notificar("Explique el motivo del " + (recibido.compareTo(esperado) > 0 ? "sobrante" : "faltante") + ".",
                        NotificationVariant.LUMO_WARNING);
                txtObservacion.focus();
                btnConfirmar.setEnabled(true);
                return;
            }

            if (!liquidar(pedidos, credito, recibido, txtObservacion.getValue(), dialog)) {
                btnConfirmar.setEnabled(true);
            }
        });

        // Enter confirma solo desde el campo de efectivo (antes también al escribir la observación)
        efectivoField.addKeyDownListener(Key.ENTER, e -> btnConfirmar.click());

        dialog.getFooter().add(btnCancelar, btnConfirmar);
        dialog.open();
        if (efectivoField.isVisible()) {
            efectivoField.focus();
        }
    }

    /**
     * Facturas de contado con un selector de cliente por cada una. Elegir un
     * cliente deja esa factura a crédito; vaciarlo la devuelve a contado. El
     * selector muestra lo que debe el cliente y su crédito disponible.
     */
    private VerticalLayout crearSeccionCredito(List<PedidoDeliveryDTO> contado, Map<Integer, Cliente> credito,
            Map<Integer, ComboBox<Cliente>> combos, Map<Integer, ResumenCreditoDTO> cache, Runnable alCambiar) {

        List<Cliente> clientes = clienteService.getLista().stream()
                .sorted(Comparator.comparing(c -> c.getNombre() != null ? c.getNombre() : ""))
                .toList();

        Span titulo = new Span("¿Alguna factura de contado se queda a crédito? Seleccione el cliente:");
        titulo.getStyle().set("font-weight", "bold");

        Grid<PedidoDeliveryDTO> grid = new Grid<>(PedidoDeliveryDTO.class, false);
        EstiloConsulta.aplicarGrid(grid);
        grid.addColumn(p -> "#" + p.getNumeroFactura()).setHeader("Factura").setAutoWidth(true);
        grid.addColumn(PedidoDeliveryDTO::getNombreCliente).setHeader("Cliente de la factura").setAutoWidth(true);
        EstiloConsulta.monto(grid.addColumn(p -> EstiloConsulta.moneda(p.getMontoTotal())))
                .setHeader("Monto").setAutoWidth(true);

        grid.addComponentColumn(p -> {
            ComboBox<Cliente> cb = combos.computeIfAbsent(p.getCodigoFactura(), k -> {
                ComboBox<Cliente> nuevo = new ComboBox<>();
                // El cliente de la factura (si tiene) aparece primero como sugerencia
                List<Cliente> orden = new ArrayList<>(clientes);
                if (p.getCliente() != null) {
                    orden.removeIf(c -> Objects.equals(c.getCodigo(), p.getCliente().getCodigo()));
                    orden.add(0, p.getCliente());
                }
                nuevo.setItems(orden);
                nuevo.setItemLabelGenerator(Cliente::getNombre);
                nuevo.setPlaceholder("Contado (lo cobra el motorista)");
                nuevo.setClearButtonVisible(true);
                nuevo.setWidth("300px");
                nuevo.setValue(credito.get(p.getCodigoFactura()));
                nuevo.addValueChangeListener(e -> {
                    if (e.getValue() == null) {
                        credito.remove(p.getCodigoFactura());
                    } else {
                        credito.put(p.getCodigoFactura(), e.getValue());
                    }
                    alCambiar.run();
                });
                return nuevo;
            });
            return cb;
        }).setHeader("A crédito a nombre de").setAutoWidth(true).setFlexGrow(1);

        grid.setItems(contado);
        grid.setAllRowsVisible(contado.size() <= 5);
        if (contado.size() > 5) {
            grid.setHeight("250px");
        }

        VerticalLayout seccion = new VerticalLayout(titulo, grid);
        seccion.setPadding(false);
        seccion.setSpacing(false);
        return seccion;
    }

    /**
     * Muestra en cada selector lo que debe el cliente y su disponible, y marca
     * en rojo los que se pasarían del límite (antes solo se sabía al confirmar).
     */
    private void validarCredito(Map<Integer, Cliente> credito, Map<Integer, ComboBox<Cliente>> combos,
            Map<Integer, ResumenCreditoDTO> cache, List<PedidoDeliveryDTO> pedidos, Span lblError) {

        Map<Integer, BigDecimal> montoPorFactura = new HashMap<>();
        pedidos.forEach(p -> montoPorFactura.put(p.getCodigoFactura(), p.getMontoTotal()));

        // Lo que se le carga a cada cliente en esta liquidación
        Map<Integer, BigDecimal> cargoPorCliente = new HashMap<>();
        credito.forEach((factura, cliente) -> cargoPorCliente.merge(cliente.getCodigo(),
                montoPorFactura.getOrDefault(factura, BigDecimal.ZERO), BigDecimal::add));

        List<String> excedidos = new ArrayList<>();
        combos.forEach((factura, cb) -> {
            Cliente c = cb.getValue();
            if (c == null) {
                cb.setHelperText(null);
                cb.setInvalid(false);
                return;
            }
            ResumenCreditoDTO r = cache.computeIfAbsent(c.getCodigo(), cuentaPorCobrarService::obtenerResumen);
            BigDecimal cargo = cargoPorCliente.getOrDefault(c.getCodigo(), BigDecimal.ZERO);
            boolean excede = r.tieneLimite() && cargo.compareTo(valor(r.disponible())) > 0;
            cb.setHelperText("Debe " + moneda(r.balance())
                    + (r.tieneLimite() ? " · Disponible " + moneda(r.disponible()) : " · Sin límite"));
            cb.setInvalid(excede);
            cb.setErrorMessage(excede ? "Excede el crédito disponible (" + moneda(r.disponible()) + ")" : null);
            if (excede && !excedidos.contains(c.getNombre())) {
                excedidos.add(c.getNombre());
            }
        });

        lblError.setText(excedidos.isEmpty() ? ""
                : "Sin crédito suficiente: " + String.join(", ", excedidos) + ". Elija otro cliente o déjelo de contado.");
        lblError.setVisible(!excedidos.isEmpty());
    }

    /**
     * Registra la liquidación, recarga los pendientes y ofrece imprimir el
     * comprobante. Devuelve false si no se pudo registrar.
     */
    private boolean liquidar(List<PedidoDeliveryDTO> pedidos, Map<Integer, Cliente> credito,
            BigDecimal recibido, String observacion, Dialog dialog) {

        try {
            List<Integer> codigos = pedidos.stream().map(PedidoDeliveryDTO::getCodigoFactura).toList();

            LiquidacionDelivery liq = liquidacionService.liquidar(motoristaCombo.getValue(), codigos,
                    new LinkedHashMap<>(credito), recibido, observacion, usuarioActual());

            dialog.close();
            cargarMotoristas();
            cargarPedidosPendientes();
            mostrarComprobante(liq, pedidos.size(), credito.size());
            return true;

        } catch (IllegalStateException | IllegalArgumentException ex) {
            notificar(ex.getMessage(), NotificationVariant.LUMO_WARNING);
            // Si alguna factura ya no estaba pendiente, se refresca la lista
            if (ex instanceof IllegalStateException) {
                dialog.close();
                refrescarTodo();
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            notificar("Error al liquidar: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            return false;
        }
    }

    /** Confirmación con opción de imprimir el comprobante para que firme el motorista. */
    private void mostrarComprobante(LiquidacionDelivery liq, int pedidos, int aCredito) {

        Dialog d = new Dialog();
        d.addClassName("dc-dialogo");
        d.setHeaderTitle("Liquidación #" + liq.getCodigo() + " registrada");
        d.setWidth("440px");

        BigDecimal diferencia = valor(liq.getDiferencia());
        Div cuerpo = new Div(
                DialogoCajaEstilo.banda(VaadinIcon.CHECK_CIRCLE,
                        pedidos + (pedidos == 1 ? " pedido liquidado" : " pedidos liquidados"),
                        aCredito > 0 ? aCredito + " pasada(s) a cuentas por cobrar" : "Todo cuadrado en caja"),
                DialogoCajaEstilo.billeteGrande("Efectivo recibido", MONEDA_FORMAT.format(valor(liq.getEfectivoRecibido()))));
        if (diferencia.signum() != 0) {
            Span dif = new Span((diferencia.signum() > 0 ? "Sobrante: " : "Faltante: ") + moneda(diferencia.abs()));
            dif.getStyle().set("font-weight", "700").set("color", diferencia.signum() > 0 ? "#7a5d00" : "#b3261e");
            cuerpo.add(dif);
        }
        cuerpo.addClassName("dc-cuerpo");
        d.add(cuerpo);

        Button cerrar = new Button("Cerrar", e -> d.close());
        cerrar.addClassName("dc-btn-secundario");
        Button imprimir = new Button("Imprimir comprobante", VaadinIcon.PRINT.create(), e -> {
            try {
                ImpresionHtmlUtil.imprimir(UI.getCurrent(), liquidacionService.generarComprobanteHtml(liq.getCodigo()));
            } catch (Exception ex) {
                ex.printStackTrace();
                notificar("No se pudo imprimir: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
            d.close();
        });
        imprimir.addClassName("dc-btn-cerrar");
        imprimir.addClickShortcut(Key.ENTER);
        d.getFooter().add(cerrar, imprimir);
        d.open();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private BigDecimal sumar(List<PedidoDeliveryDTO> pedidos, boolean fiados) {
        return pedidos.stream()
                .filter(p -> p.esFiado() == fiados)
                .map(PedidoDeliveryDTO::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal valor(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private String moneda(BigDecimal v) {
        return "RD$ " + MONEDA_FORMAT.format(valor(v));
    }

    private void notificar(String mensaje, NotificationVariant variante) {
        Notification.show(mensaje, 4000, Notification.Position.MIDDLE).addThemeVariants(variante);
    }
}
