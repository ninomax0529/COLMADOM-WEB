package com.maxsoft.application.view.venta.delivery;

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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Liquidación de Delivery")
@Route(value = "liquidacionDelivery")
@Menu(order = 5, icon = LineAwesomeIconUrl.MOTORCYCLE_SOLID)
public class LiquidacionDeliveryView extends VerticalLayout {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    private final ComboBox<String> motoristaCombo = new ComboBox<>("Seleccionar Motorista");
    private final Grid<PedidoDeliveryDTO> gridPedidos = new Grid<>(PedidoDeliveryDTO.class, false);
    
    private List<PedidoDeliveryDTO> pedidosActuales = new ArrayList<>();

    private final Span lblTotalEfectivo = new Span("Efectivo a Entregar: RD$ 0.00");
    private final Span lblTotalFiado = new Span("Total Fiados en Ruta: RD$ 0.00");
    private final Span lblTotalGeneral = new Span("Monto Global Liquidado: RD$ 0.00");

    public LiquidacionDeliveryView() {
        setSizeFull();
        setSpacing(true);

        H2 title = new H2("Cuadre y Liquidación de Motoristas (Delivery)");

        motoristaCombo.setItems("José (Motor 1)", "Luis (Motor 2)", "Junior (Motor 3)");
        motoristaCombo.setWidth("300px");
        motoristaCombo.addValueChangeListener(e -> cargarPedidosPendientes(e.getValue()));

        gridPedidos.addColumn(PedidoDeliveryDTO::getNumeroFactura).setHeader("No. Factura").setAutoWidth(true);
        gridPedidos.addColumn(PedidoDeliveryDTO::getCliente).setHeader("Cliente").setAutoWidth(true);
        gridPedidos.addColumn(PedidoDeliveryDTO::getTipoPago).setHeader("Tipo de Pago").setAutoWidth(true);
        gridPedidos.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getMontoTotal())).setHeader("Total a Cobrar");
        gridPedidos.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getCostoEnvio())).setHeader("Envío");

        // NUEVA COLUMNA: Botón individual de liquidar por cada pedido
        gridPedidos.addComponentColumn(pedido -> {
            Button btnLiquidarIndividual = new Button("Liquidar", VaadinIcon.CHECK.create());
            btnLiquidarIndividual.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            btnLiquidarIndividual.addClickListener(e -> abrirDialogoCuadreIndividual(pedido));
            return btnLiquidarIndividual;
        }).setHeader("Acción").setAutoWidth(true);

        gridPedidos.setHeight("350px");

        lblTotalEfectivo.getStyle().set("font-size", "1.2rem").set("font-weight", "bold").set("color", "var(--lumo-success-text-color)");
        lblTotalFiado.getStyle().set("font-size", "1.2rem").set("font-weight", "bold").set("color", "var(--lumo-error-text-color)");
        lblTotalGeneral.getStyle().set("font-size", "1.3rem").set("font-weight", "bold");

        HorizontalLayout resumenLayout = new HorizontalLayout(lblTotalEfectivo, lblTotalFiado, lblTotalGeneral);
        resumenLayout.setWidthFull();
        resumenLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Botón general para liquidar todo el lote pendiente del motorista
        Button liquidarTodoBtn = new Button("Liquidar Todo el Lote", VaadinIcon.CHECK_CIRCLE.create());
        liquidarTodoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        liquidarTodoBtn.addClickListener(e -> abrirDialogoCuadreGlobal());

        HorizontalLayout accionesLayout = new HorizontalLayout(resumenLayout, liquidarTodoBtn);
        accionesLayout.setWidthFull();
        accionesLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        accionesLayout.setAlignItems(Alignment.CENTER);

        add(title, motoristaCombo, gridPedidos, accionesLayout);
    }

    private void cargarPedidosPendientes(String motorista) {
        if (motorista == null) {
            pedidosActuales = new ArrayList<>();
            gridPedidos.setItems(pedidosActuales);
            actualizarTotales(pedidosActuales);
            return;
        }

        pedidosActuales = new ArrayList<>();
        if (motorista.contains("José")) {
            pedidosActuales.add(new PedidoDeliveryDTO("FAC-001", "Juan Pérez", "CONTADO", 450.00, 50.00));
            pedidosActuales.add(new PedidoDeliveryDTO("FAC-002", "María Gómez", "FIADO", 1200.00, 50.00));
        } else {
            pedidosActuales.add(new PedidoDeliveryDTO("FAC-003", "Carlos Juan", "CONTADO", 850.00, 75.00));
        }

        gridPedidos.setItems(pedidosActuales);
        actualizarTotales(pedidosActuales);
    }

    private void actualizarTotales(List<PedidoDeliveryDTO> pedidos) {
        double totalEfectivo = pedidos.stream()
                .filter(p -> "CONTADO".equals(p.getTipoPago()))
                .mapToDouble(PedidoDeliveryDTO::getMontoTotal)
                .sum();

        double totalFiado = pedidos.stream()
                .filter(p -> "FIADO".equals(p.getTipoPago()))
                .mapToDouble(PedidoDeliveryDTO::getMontoTotal)
                .sum();

        double global = totalEfectivo + totalFiado;

        lblTotalEfectivo.setText("Efectivo a Entregar: RD$ " + MONEDA_FORMAT.format(totalEfectivo));
        lblTotalFiado.setText("Total Fiados en Ruta: RD$ " + MONEDA_FORMAT.format(totalFiado));
        lblTotalGeneral.setText("Monto Global Liquidado: RD$ " + MONEDA_FORMAT.format(global));
    }

    // DIÁLOGO PARA LIQUIDAR UN PEDIDO INDIVIDUALMENTE
    private void abrirDialogoCuadreIndividual(PedidoDeliveryDTO pedido) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Liquidar Pedido: " + pedido.getNumeroFactura());
        dialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Span lblCliente = new Span("Cliente: " + pedido.getCliente());
        Span lblTipo = new Span("Tipo de Pago: " + pedido.getTipoPago());
        Span lblMonto = new Span("Monto Total: RD$ " + MONEDA_FORMAT.format(pedido.getMontoTotal()));
        lblMonto.getStyle().set("font-weight", "bold").set("font-size", "1.1rem");

        BigDecimalField efectivoEntregadoField = new BigDecimalField("Efectivo Recibido en Caja (RD$)");
        
        // Si es de contado, sugerimos el monto exacto. Si es fiado, el efectivo esperado es 0.
        double efectivoEsperado = "CONTADO".equals(pedido.getTipoPago()) ? pedido.getMontoTotal() : 0.0;
        efectivoEntregadoField.setValue(BigDecimal.valueOf(efectivoEsperado));
        efectivoEntregadoField.setWidthFull();

        Span lblDiferencia = new Span("Cuadre / Devuelta: RD$ 0.00");
        lblDiferencia.getStyle().set("font-weight", "bold");

        efectivoEntregadoField.addValueChangeListener(e -> {
            BigDecimal real = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            double diff = real.doubleValue() - efectivoEsperado;

            if (diff == 0) {
                lblDiferencia.setText("Cuadre Exacto: RD$ 0.00");
                lblDiferencia.getStyle().set("color", "var(--lumo-success-text-color)");
            } else if (diff > 0) {
                lblDiferencia.setText("Sobrante: RD$ " + MONEDA_FORMAT.format(diff));
                lblDiferencia.getStyle().set("color", "var(--lumo-primary-text-color)");
            } else {
                lblDiferencia.setText("Faltante: RD$ " + MONEDA_FORMAT.format(Math.abs(diff)));
                lblDiferencia.getStyle().set("color", "var(--lumo-error-text-color)");
            }
        });

        layout.add(lblCliente, lblTipo, lblMonto);
        if ("CONTADO".equals(pedido.getTipoPago())) {
            layout.add(efectivoEntregadoField, lblDiferencia);
        }
        dialog.add(layout);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());
        Button btnConfirmar = new Button("Confirmar Liquidación", VaadinIcon.CHECK.create());
        btnConfirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnConfirmar.addClickShortcut(Key.ENTER);

        btnConfirmar.addClickListener(e -> {
            // Remover el pedido liquidado de la lista actual
            pedidosActuales.remove(pedido);
            gridPedidos.setItems(pedidosActuales);
            actualizarTotales(pedidosActuales);

            Notification.show("Pedido " + pedido.getNumeroFactura() + " liquidado correctamente", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
        });

        dialog.getFooter().add(btnCancelar, btnConfirmar);
        dialog.open();
    }

    // DIÁLOGO PARA LIQUIDAR TODO EL LOTE DE UN GOLPE
    private void abrirDialogoCuadreGlobal() {
        if (motoristaCombo.getValue() == null || pedidosActuales.isEmpty()) {
            Notification.show("No hay pedidos activos para liquidar", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        double efectivoEsperado = pedidosActuales.stream()
                .filter(p -> "CONTADO".equals(p.getTipoPago()))
                .mapToDouble(PedidoDeliveryDTO::getMontoTotal)
                .sum();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cuadre Global - " + motoristaCombo.getValue());
        dialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Span lblEsperado = new Span("Efectivo Total Esperado: RD$ " + MONEDA_FORMAT.format(efectivoEsperado));
        lblEsperado.getStyle().set("font-size", "1.1rem").set("font-weight", "bold");

        BigDecimalField efectivoEntregadoField = new BigDecimalField("Efectivo Real Entregado (RD$)");
        efectivoEntregadoField.setValue(BigDecimal.valueOf(efectivoEsperado));
        efectivoEntregadoField.setWidthFull();

        Span lblDiferencia = new Span("Diferencia / Cuadre: RD$ 0.00");
        lblDiferencia.getStyle().set("font-size", "1.2rem").set("font-weight", "bold");

        efectivoEntregadoField.addValueChangeListener(e -> {
            BigDecimal real = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            double diff = real.doubleValue() - efectivoEsperado;
            
            if (diff == 0) {
                lblDiferencia.setText("Cuadre Exacto: RD$ 0.00");
                lblDiferencia.getStyle().set("color", "var(--lumo-success-text-color)");
            } else if (diff > 0) {
                lblDiferencia.setText("Sobrante: RD$ " + MONEDA_FORMAT.format(diff));
                lblDiferencia.getStyle().set("color", "var(--lumo-primary-text-color)");
            } else {
                lblDiferencia.setText("Faltante: RD$ " + MONEDA_FORMAT.format(Math.abs(diff)));
                lblDiferencia.getStyle().set("color", "var(--lumo-error-text-color)");
            }
        });

        layout.add(lblEsperado, efectivoEntregadoField, lblDiferencia);
        dialog.add(layout);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());
        Button btnConfirmar = new Button("Liquidar Todo el Lote", VaadinIcon.CHECK.create());
        btnConfirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnConfirmar.addClickShortcut(Key.ENTER);

        btnConfirmar.addClickListener(e -> {
            pedidosActuales.clear();
            gridPedidos.setItems(pedidosActuales);
            actualizarTotales(pedidosActuales);
            motoristaCombo.clear();

            Notification.show("¡Lote completo liquidado con éxito!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
        });

        dialog.getFooter().add(btnCancelar, btnConfirmar);
        dialog.open();
    }

    public static class PedidoDeliveryDTO {
        private String numeroFactura;
        private String cliente;
        private String tipoPago;
        private double montoTotal;
        private double costoEnvio;

        public PedidoDeliveryDTO(String numeroFactura, String cliente, String tipoPago, double montoTotal, double costoEnvio) {
            this.numeroFactura = numeroFactura;
            this.cliente = cliente;
            this.tipoPago = tipoPago;
            this.montoTotal = montoTotal;
            this.costoEnvio = costoEnvio;
        }

        public String getNumeroFactura() { return numeroFactura; }
        public String getCliente() { return cliente; }
        public String getTipoPago() { return tipoPago; }
        public double getMontoTotal() { return montoTotal; }
        public double getCostoEnvio() { return costoEnvio; }
    }
}