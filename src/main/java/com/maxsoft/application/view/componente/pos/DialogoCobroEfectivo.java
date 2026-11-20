package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.venta.EstadoFacturaService;
import com.maxsoft.application.servicio.interfaces.venta.TipoVentaService;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DialogoCobroEfectivo extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");
    // Para los botones de billetes: "1,000" en vez de "1,000.00"
    private static final DecimalFormat MONEDA_ENTERA = new DecimalFormat("#,##0");

    @FunctionalInterface
    public interface AccionGuardarVenta {
        void ejecutar(TicketVenta ticket);
    }

    public DialogoCobroEfectivo(
            TicketVenta ticket,
            DeliveryService deliveryService,
            EstadoFacturaService estadoFacturaService,
            ClienteService clienteService,
            TipoVentaService tipoVentaService,
            AccionGuardarVenta onGuardar,
            Runnable onCerrarTicketActual,
            Runnable onEnfocarBuscador
    ) {
        setHeaderTitle("Cobrar y Facturar - " + ticket.getId());
        setWidth("470px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        Double subtotalProductos = ticket.getTotalAmount().doubleValue();

        // Contenedor para selección rápida de tipo de entrega
        HorizontalLayout tipoEntregaLayout = new HorizontalLayout();
        tipoEntregaLayout.setWidthFull();
        tipoEntregaLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button btnLocal = new Button("1. Local [Alt+L]", VaadinIcon.SHOP.create());
        Button btnDelivery = new Button("2. Delivery [Alt+D]", VaadinIcon.MOON.create());

        btnLocal.setWidth("48%");
        btnDelivery.setWidth("48%");

        // Contenedor para campos dinámicos de Delivery
        VerticalLayout deliveryLayout = new VerticalLayout();
        deliveryLayout.setPadding(false);
        deliveryLayout.setSpacing(true);
        deliveryLayout.setVisible(false);

        TextField direccionField = new TextField("Dirección y Sector de Entrega");
        direccionField.setPlaceholder("Ej: Ensanche Altagracia, C/ Principal #5");
        direccionField.setWidthFull();

        TextField telefonoField = new TextField("Teléfono de Contacto");
        telefonoField.setPlaceholder("809-000-0000");
        telefonoField.setWidthFull();

        ComboBox<Delivery> cbDelivery = new ComboBox<>("Asignar Motorista (Delivery)");
        cbDelivery.setItems(deliveryService.getLista());
        cbDelivery.setWidthFull();

        BigDecimalField costoEnvioField = new BigDecimalField("Costo de Delivery (RD$)");
        costoEnvioField.setValue(BigDecimal.ZERO);
        costoEnvioField.setWidthFull();

        deliveryLayout.add(direccionField, cbDelivery);

        // Campos de pago
        NumberField cashGivenField = new NumberField("Efectivo Recibido (RD$)");
        cashGivenField.setWidthFull();
        cashGivenField.setValueChangeMode(ValueChangeMode.EAGER);

        final boolean[] esDeliveryState = {false};

        btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnDelivery.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // Métodos de selección que mantienen el foco estratégico
        Runnable seleccionarLocal = () -> {
            esDeliveryState[0] = false;
            deliveryLayout.setVisible(false);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            cashGivenField.focus(); // Retorna el foco al campo de monto a cobrar
        };

        Runnable seleccionarDelivery = () -> {
            esDeliveryState[0] = true;
            deliveryLayout.setVisible(true);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            direccionField.focus(); // Pasa el foco directamente a la dirección
        };

        btnLocal.addClickListener(e -> seleccionarLocal.run());
        btnDelivery.addClickListener(e -> seleccionarDelivery.run());

        // Atajos globales a nivel de Diálogo para trabajar sin importar dónde esté el foco
        Shortcuts.addShortcutListener(this, seleccionarLocal::run, Key.KEY_L, KeyModifier.ALT);
        Shortcuts.addShortcutListener(this, seleccionarDelivery::run, Key.KEY_D, KeyModifier.ALT);

        tipoEntregaLayout.add(btnLocal, btnDelivery);

        Span totalToPayLabel = new Span("Total a Pagar: RD$ " + MONEDA_FORMAT.format(subtotalProductos));
        totalToPayLabel.getStyle().set("font-size", "1.2rem").set("font-weight", "bold");

        Span changeDueLabel = new Span("Devuelta: RD$ 0.00");
        changeDueLabel.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-success-text-color)");

        // Lógica de cálculo de total con envío
        costoEnvioField.addValueChangeListener(e -> {
            BigDecimal envio = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            double totalFinal = subtotalProductos + envio.doubleValue();
            totalToPayLabel.setText("Total a Pagar (Incl. Envío): RD$ " + MONEDA_FORMAT.format(totalFinal));

            Double cashGiven = cashGivenField.getValue();
            if (cashGiven != null && cashGiven >= totalFinal) {
                Double change = cashGiven - totalFinal;
                changeDueLabel.setText("Devuelta: RD$ " + MONEDA_FORMAT.format(change));
            } else {
                changeDueLabel.setText("Devuelta: Insuficiente");
            }
        });

        // Lógica de cálculo de devuelta
        cashGivenField.addValueChangeListener(event -> {
            Double cashGiven = event.getValue();
            BigDecimal envio = costoEnvioField.getValue() != null ? costoEnvioField.getValue() : BigDecimal.ZERO;
            double totalFinal = subtotalProductos + envio.doubleValue();

            if (cashGiven != null && cashGiven >= totalFinal) {
                Double change = cashGiven - totalFinal;
                changeDueLabel.setText("Devuelta: RD$ " + MONEDA_FORMAT.format(change));
            } else {
                changeDueLabel.setText("Devuelta: Insuficiente");
            }
        });

        // Billetes rápidos: un toque pone con cuánto paga el cliente
        Div billetesRapidos = new Div();
        billetesRapidos.setWidthFull();
        billetesRapidos.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "var(--lumo-space-xs)");

        java.util.List<BigDecimal> montos = montosRapidos(ticket.getTotalAmount());
        for (int i = 0; i < montos.size(); i++) {
            BigDecimal monto = montos.get(i);
            // El primero siempre es el total exacto
            boolean exacto = i == 0;
            Button b = new Button(exacto ? "Exacto" : MONEDA_ENTERA.format(monto));
            b.addThemeVariants(exacto ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_CONTRAST);
            b.getStyle().set("flex", "1 1 0").set("min-width", "4.5em").set("margin", "0").set("font-weight", "600");
            b.setTooltipText("El cliente paga con RD$ " + MONEDA_FORMAT.format(monto));
            b.addClickListener(e -> {
                cashGivenField.setValue(monto.doubleValue());
                // El foco vuelve al campo: Enter completa la venta
                cashGivenField.focus();
            });
            billetesRapidos.add(b);
        }

        dialogLayout.add(tipoEntregaLayout, deliveryLayout, totalToPayLabel, cashGivenField, billetesRapidos, changeDueLabel);
        add(dialogLayout);

        Button cancelButton = new Button("Cancelar", e -> close());
        Button confirmButton = new Button("Completar Venta [Enter]", VaadinIcon.CHECK.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        // Atajo global para procesar venta con Enter
        Shortcuts.addShortcutListener(this, () -> confirmButton.click(), Key.ENTER);

        confirmButton.addClickListener(e -> {

            if (esDeliveryState[0]) {

                if (direccionField.getValue() == null || direccionField.getValue().trim().isEmpty()) {
                    Notification.show("Debe ingresar la dirección para el delivery", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                    direccionField.focus();
                    return;
                }

                if (cbDelivery.getValue() == null) {
                    Notification.show("Debe asignar un motorista para el pedido", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                    cbDelivery.focus();
                    return;
                }

                ticket.setDelivery(cbDelivery.getValue());
                ticket.setEstadoFactura(estadoFacturaService.getEstadoFactura(1)); // Abierta

                ticket.setCliente(clienteService.getLista().get(0));
                ticket.setNombreCliente(direccionField.getValue().toUpperCase());
                ticket.setDireccion(direccionField.getValue().toUpperCase());

            } else {

                ticket.setEstadoFactura(estadoFacturaService.getEstadoFactura(2)); // Abierta
                ticket.setCliente(clienteService.getLista().get(0));

                ticket.setNombreCliente(ticket.getCliente().getNombre());
                ticket.setDireccion(ticket.getCliente().getDireccion());
            }

            ticket.setTipoVenta(tipoVentaService.getTipoVenta(1)); // Crédito

            if (onGuardar != null) {
                onGuardar.ejecutar(ticket);
            }
            if (onCerrarTicketActual != null) {
                onCerrarTicketActual.run();
            }
            close();

            if (onEnfocarBuscador != null) {
                onEnfocarBuscador.run();
            }
        });

        getFooter().add(cancelButton, confirmButton);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                cashGivenField.focus();
            }
        });
    }

    /** Múltiplos a los que se redondea el total (ej. 435 → 450, 500, 1000). */
    private static final int[] REDONDEOS = {50, 100, 500, 1000};
    /** Billetes dominicanos sueltos con que se suele pagar. */
    private static final int[] BILLETES = {200, 500, 1000, 2000};

    /**
     * "Exacto" + los montos con que el cliente probablemente pague: el total
     * redondeado hacia arriba (a 50, 100, 500 y 1000) y cada billete que
     * alcance. Ej.: total 435 → 435, 450, 500, 1000, 2000. Máximo 6 botones.
     */
    public static java.util.List<BigDecimal> montosRapidos(BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return java.util.List.of();
        }
        java.util.TreeSet<BigDecimal> montos = new java.util.TreeSet<>();
        BigDecimal exacto = total.setScale(2, java.math.RoundingMode.HALF_UP);
        montos.add(exacto);
        for (int r : REDONDEOS) {
            BigDecimal m = BigDecimal.valueOf(r);
            BigDecimal redondeado = exacto.divide(m, 0, java.math.RoundingMode.CEILING).multiply(m);
            if (redondeado.compareTo(exacto) > 0) {
                montos.add(redondeado.setScale(2));
            }
        }
        for (int billete : BILLETES) {
            BigDecimal b = BigDecimal.valueOf(billete).setScale(2);
            if (b.compareTo(exacto) > 0) {
                montos.add(b);
            }
        }
        return montos.stream().limit(6).toList();
    }
}