package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class DialogoSeleccionCliente extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    // Interfaz funcional para callbacks necesarios desde la vista llamante
    @FunctionalInterface
    public interface AccionConfirmarFiado {
        void ejecutar(Cliente clienteSeleccionado, boolean esDelivery, Delivery motorista, String direccion, String telefono);
    }

    public DialogoSeleccionCliente(
            TicketVenta ticket,
            List<Cliente> listaClientes,
            List<Delivery> listaDeliveries,
            AccionConfirmarFiado onConfirmar
    ) {
        setHeaderTitle("Registrar Venta Fiada - " + ticket.getId());
        setWidth("450px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setWidthFull();

        Double subtotalProductos = ticket.getTotalAmount();

        ComboBox<Cliente> clientCombo = new ComboBox<>("Seleccionar Cliente (Fiado)");
        clientCombo.setItems(listaClientes);
        clientCombo.setItemLabelGenerator(c -> c.getNombre());
        clientCombo.setWidthFull();

        HorizontalLayout tipoEntregaLayout = new HorizontalLayout();
        tipoEntregaLayout.setWidthFull();
        tipoEntregaLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button btnLocal = new Button("1. Local [Alt+L]", VaadinIcon.SHOP.create());
        Button btnDelivery = new Button("2. Delivery [Alt+D]", VaadinIcon.MAGIC.create());

        btnLocal.setWidth("48%");
        btnDelivery.setWidth("48%");

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
        cbDelivery.setItems(listaDeliveries);
        cbDelivery.setWidthFull();

        BigDecimalField costoEnvioField = new BigDecimalField("Costo de Delivery (RD$)");
        costoEnvioField.setValue(BigDecimal.ZERO);
        costoEnvioField.setWidthFull();

        deliveryLayout.add(direccionField, cbDelivery);

        final boolean[] esDeliveryState = {false};

        btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnDelivery.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // Métodos de acción declarados explícitamente para mantener control del foco
        Runnable seleccionarLocal = () -> {
            esDeliveryState[0] = false;
            deliveryLayout.setVisible(false);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            clientCombo.focus();
        };

        Runnable seleccionarDelivery = () -> {
            esDeliveryState[0] = true;
            deliveryLayout.setVisible(true);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            direccionField.focus();
        };

        btnLocal.addClickListener(e -> seleccionarLocal.run());
        btnDelivery.addClickListener(e -> seleccionarDelivery.run());

        // --- REGISTRO CORRECTO DE ATAJOS GLOBALES (ALT+L Y ALT+D) ---
        Shortcuts.addShortcutListener(this, seleccionarLocal::run, Key.KEY_L, KeyModifier.ALT);
        Shortcuts.addShortcutListener(this, seleccionarDelivery::run, Key.KEY_D, KeyModifier.ALT);

        tipoEntregaLayout.add(btnLocal, btnDelivery);

        Span totalFiadoLabel = new Span("Total a Fiar: RD$ " + MONEDA_FORMAT.format(subtotalProductos));
        totalFiadoLabel.getStyle().set("font-size", "1.2rem").set("font-weight", "bold").set("color", "var(--lumo-error-text-color)");

        costoEnvioField.addValueChangeListener(e -> {
            BigDecimal envio = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            double totalFinal = subtotalProductos + envio.doubleValue();
            totalFiadoLabel.setText("Total a Fiar (Incl. Envío): RD$ " + MONEDA_FORMAT.format(totalFinal));
        });

        layout.add(clientCombo, tipoEntregaLayout, deliveryLayout, totalFiadoLabel);
        add(layout);

        Button cancelBtn = new Button("Cancelar", e -> close());
        Button processBtn = new Button("Confirmar Fiado [Enter]", VaadinIcon.CHECK.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        // Atajo global para Enter
        Shortcuts.addShortcutListener(this, () -> processBtn.click(), Key.ENTER);

        processBtn.addClickListener(e -> {

            Cliente clienteSeleccionado = clientCombo.getValue();
            boolean esDelivery = esDeliveryState[0];

            if (clienteSeleccionado == null) {
                Notification.show("Debe seleccionar un cliente de la libreta", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                clientCombo.focus();
                return;
            }

            if (esDelivery) {

                if (direccionField.getValue() == null || direccionField.getValue().trim().isEmpty()) {
                    Notification.show("Debe ingresar la dirección para el delivery", 2500, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                    direccionField.focus();
                    return;
                }
                if (cbDelivery.getValue() == null) {
                    Notification.show("Debe asignar un motorista para el pedido", 2500, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                    cbDelivery.focus();
                    return;
                }
            }

            close();

            if (onConfirmar != null) {
                onConfirmar.ejecutar(
                        clienteSeleccionado,
                        esDelivery,
                        cbDelivery.getValue(),
                        direccionField.getValue(),
                        telefonoField.getValue()
                );
            }
        });

        getFooter().add(cancelBtn, processBtn);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                clientCombo.focus();
            }
        });
    }
}