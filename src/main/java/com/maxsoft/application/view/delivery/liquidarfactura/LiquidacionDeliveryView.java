package com.maxsoft.application.view.delivery.liquidarfactura;

import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.delivery.LiquidacionDeliveryService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@PageTitle("Liquidación de Delivery")
@Route(value = "delivery/liquidacionDelivery")
//@Menu(order = 7, icon = LineAwesomeIconUrl.MOTORCYCLE_SOLID)
public class LiquidacionDeliveryView extends VerticalLayout {

    private static final String USUARIO_ACTUAL = "ADMIN";

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    private final LiquidacionDeliveryService liquidacionService;
    private final DeliveryService deliveryService;

    private final ComboBox<Delivery> motoristaCombo = new ComboBox<>("Seleccionar Motorista");
    private final Grid<PedidoDeliveryDTO> gridPedidos = new Grid<>(PedidoDeliveryDTO.class, false);

    private List<PedidoDeliveryDTO> pedidosActuales = new ArrayList<>();

    private final Span lblTotalEfectivo = new Span("Efectivo a Entregar: RD$ 0.00");
    private final Span lblTotalFiado = new Span("Total Fiados en Ruta: RD$ 0.00");
    private final Span lblTotalGeneral = new Span("Monto Global Liquidado: RD$ 0.00");

    private Button liquidarTodoBtn;

    @Autowired
    public LiquidacionDeliveryView(LiquidacionDeliveryService liquidacionService,
            DeliveryService deliveryService) {

        this.liquidacionService = liquidacionService;
        this.deliveryService = deliveryService;

        setSizeFull();
        setSpacing(true);

        H2 title = new H2("Cuadre y Liquidación de Motoristas (Delivery)");

        configurarMotoristas();
        configurarGrid();

        lblTotalEfectivo.getStyle().set("font-size", "1.2rem").set("font-weight", "bold").set("color", "var(--lumo-success-text-color)");
        lblTotalFiado.getStyle().set("font-size", "1.2rem").set("font-weight", "bold").set("color", "var(--lumo-error-text-color)");
        lblTotalGeneral.getStyle().set("font-size", "1.3rem").set("font-weight", "bold");

        HorizontalLayout resumenLayout = new HorizontalLayout(lblTotalEfectivo, lblTotalFiado, lblTotalGeneral);
        resumenLayout.setWidthFull();
        resumenLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Botón general para liquidar todo el lote pendiente del motorista
        liquidarTodoBtn = new Button("Liquidar Todo el Lote", VaadinIcon.CHECK_CIRCLE.create());
        liquidarTodoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        liquidarTodoBtn.addClickListener(e -> abrirDialogoLiquidacion(
                "Cuadre Global - " + motoristaCombo.getValue().getNombre(), pedidosActuales));

        HorizontalLayout accionesLayout = new HorizontalLayout(resumenLayout, liquidarTodoBtn);
        accionesLayout.setWidthFull();
        accionesLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        accionesLayout.setAlignItems(Alignment.CENTER);

        add(title, motoristaCombo, gridPedidos, accionesLayout);

        actualizarTotales();
    }

    // ============================================================
    // CONFIGURACIÓN
    // ============================================================
    private void configurarMotoristas() {

        motoristaCombo.setItems(deliveryService.getLista());
        motoristaCombo.setItemLabelGenerator(Delivery::getNombre);
        motoristaCombo.setWidth("300px");
        motoristaCombo.setClearButtonVisible(true);
        motoristaCombo.setPlaceholder("Seleccione un motorista...");
        motoristaCombo.addValueChangeListener(e -> cargarPedidosPendientes());
    }

    private void configurarGrid() {

        gridPedidos.addColumn(PedidoDeliveryDTO::getNumeroFactura).setHeader("No. Factura").setAutoWidth(true)
                .setComparator(Comparator.comparing(PedidoDeliveryDTO::getCodigoFactura));
        gridPedidos.addColumn(PedidoDeliveryDTO::getCliente).setHeader("Cliente").setAutoWidth(true);
        gridPedidos.addColumn(PedidoDeliveryDTO::getTipoPago).setHeader("Tipo de Pago").setAutoWidth(true);
        gridPedidos.addColumn(item -> moneda(item.getMontoTotal())).setHeader("Total a Cobrar")
                .setComparator(Comparator.comparing(PedidoDeliveryDTO::getMontoTotal));

        // Botón individual de liquidar por cada pedido
        gridPedidos.addComponentColumn(pedido -> {
            Button btnLiquidarIndividual = new Button("Liquidar", VaadinIcon.CHECK.create());
            btnLiquidarIndividual.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            btnLiquidarIndividual.addClickListener(e -> abrirDialogoLiquidacion(
                    "Liquidar Pedido: " + pedido.getNumeroFactura(), List.of(pedido)));
            return btnLiquidarIndividual;
        }).setHeader("Acción").setAutoWidth(true);

        gridPedidos.setHeight("350px");
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
            Notification.show("Error consultando los pedidos pendientes: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        gridPedidos.setItems(pedidosActuales);
        actualizarTotales();

        if (motorista != null && pedidosActuales.isEmpty()) {
            Notification.show("El motorista no tiene pedidos pendientes de liquidar.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        }
    }

    private void actualizarTotales() {

        BigDecimal totalEfectivo = efectivoEsperado(pedidosActuales);
        BigDecimal totalFiado = totalFiado(pedidosActuales);

        lblTotalEfectivo.setText("Efectivo a Entregar: " + moneda(totalEfectivo));
        lblTotalFiado.setText("Total Fiados en Ruta: " + moneda(totalFiado));
        lblTotalGeneral.setText("Monto Global Liquidado: " + moneda(totalEfectivo.add(totalFiado)));

        liquidarTodoBtn.setEnabled(!pedidosActuales.isEmpty());
    }

    // ============================================================
    // LIQUIDACIÓN (un pedido o todo el lote)
    // ============================================================
    /**
     * Diálogo de cuadre. Sirve para un pedido (lista de uno) o para todo el
     * lote: muestra el efectivo esperado, pide el efectivo recibido y exige una
     * observación si hay sobrante o faltante.
     */
    private void abrirDialogoLiquidacion(String titulo, List<PedidoDeliveryDTO> pedidos) {

        Delivery motorista = motoristaCombo.getValue();

        if (motorista == null || pedidos == null || pedidos.isEmpty()) {
            Notification.show("No hay pedidos activos para liquidar", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        BigDecimal esperado = efectivoEsperado(pedidos);
        BigDecimal fiado = totalFiado(pedidos);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(titulo);
        dialog.setWidth("420px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        if (pedidos.size() == 1) {
            PedidoDeliveryDTO p = pedidos.get(0);
            layout.add(new Span("Cliente: " + p.getCliente()), new Span("Tipo de Pago: " + p.getTipoPago()));
        } else {
            layout.add(new Span("Pedidos a liquidar: " + pedidos.size()));
        }

        Span lblMonto = new Span("Monto Total: " + moneda(esperado.add(fiado)));
        lblMonto.getStyle().set("font-weight", "bold").set("font-size", "1.1rem");
        layout.add(lblMonto);

        if (fiado.signum() > 0) {
            Span lblFiado = new Span("De los cuales fiados (no se cobran): " + moneda(fiado));
            lblFiado.getStyle().set("color", "var(--lumo-error-text-color)");
            layout.add(lblFiado);
        }

        Span lblEsperado = new Span("Efectivo Esperado: " + moneda(esperado));
        lblEsperado.getStyle().set("font-weight", "bold");
        layout.add(lblEsperado);

        BigDecimalField efectivoField = new BigDecimalField("Efectivo Recibido en Caja (RD$)");
        efectivoField.setValue(esperado);
        efectivoField.setWidthFull();

        Span lblDiferencia = new Span();
        lblDiferencia.getStyle().set("font-weight", "bold");
        mostrarDiferencia(lblDiferencia, esperado, esperado);

        TextArea txtObservacion = new TextArea("Observación (obligatoria si hay sobrante o faltante)");
        txtObservacion.setWidthFull();
        txtObservacion.setMaxLength(500);

        // Si solo hay pedidos fiados no hay efectivo que cuadrar
        if (esperado.signum() > 0) {
            efectivoField.addValueChangeListener(e -> mostrarDiferencia(lblDiferencia, esperado, valor(e.getValue())));
            layout.add(efectivoField, lblDiferencia, txtObservacion);
        } else {
            efectivoField.setValue(BigDecimal.ZERO);
        }

        dialog.add(layout);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        Button btnConfirmar = new Button("Confirmar Liquidación", VaadinIcon.CHECK.create());
        btnConfirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnConfirmar.addClickShortcut(Key.ENTER);

        btnConfirmar.addClickListener(e -> {

            BigDecimal recibido = esperado.signum() > 0 ? valor(efectivoField.getValue()) : BigDecimal.ZERO;

            if (recibido.signum() < 0) {
                Notification.show("El efectivo recibido no puede ser negativo", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            if (recibido.compareTo(esperado) != 0 && txtObservacion.getValue().isBlank()) {
                Notification.show("Explique el motivo del " + (recibido.compareTo(esperado) > 0 ? "sobrante" : "faltante"),
                        3500, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                txtObservacion.focus();
                return;
            }

            liquidar(pedidos, recibido, txtObservacion.getValue(), dialog);
        });

        dialog.getFooter().add(btnCancelar, btnConfirmar);
        dialog.open();
    }

    /**
     * Registra la liquidación en el servicio y recarga los pendientes.
     */
    private void liquidar(List<PedidoDeliveryDTO> pedidos, BigDecimal recibido, String observacion, Dialog dialog) {

        try {
            List<Integer> codigos = pedidos.stream().map(PedidoDeliveryDTO::getCodigoFactura).toList();

            liquidacionService.liquidar(motoristaCombo.getValue(), codigos, recibido, observacion, USUARIO_ACTUAL);

            Notification.show(pedidos.size() == 1
                    ? "Pedido " + pedidos.get(0).getNumeroFactura() + " liquidado correctamente"
                    : "¡" + pedidos.size() + " pedidos liquidados con éxito!",
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            dialog.close();
            cargarPedidosPendientes();

        } catch (IllegalStateException | IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            // Si alguna factura ya no estaba pendiente, se refresca la lista
            if (ex instanceof IllegalStateException) {
                dialog.close();
                cargarPedidosPendientes();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al liquidar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void mostrarDiferencia(Span lbl, BigDecimal esperado, BigDecimal real) {

        BigDecimal diff = real.subtract(esperado);

        if (diff.signum() == 0) {
            lbl.setText("Cuadre Exacto: RD$ 0.00");
            lbl.getStyle().set("color", "var(--lumo-success-text-color)");
        } else if (diff.signum() > 0) {
            lbl.setText("Sobrante: " + moneda(diff));
            lbl.getStyle().set("color", "var(--lumo-primary-text-color)");
        } else {
            lbl.setText("Faltante: " + moneda(diff.abs()));
            lbl.getStyle().set("color", "var(--lumo-error-text-color)");
        }
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private BigDecimal efectivoEsperado(List<PedidoDeliveryDTO> pedidos) {

        return pedidos.stream()
                .filter(p -> !p.esFiado())
                .map(PedidoDeliveryDTO::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalFiado(List<PedidoDeliveryDTO> pedidos) {

        return pedidos.stream()
                .filter(PedidoDeliveryDTO::esFiado)
                .map(PedidoDeliveryDTO::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal valor(BigDecimal v) {

        return v != null ? v : BigDecimal.ZERO;
    }

    private String moneda(BigDecimal v) {

        return "RD$ " + MONEDA_FORMAT.format(valor(v));
    }
}