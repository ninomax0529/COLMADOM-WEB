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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class DialogoSeleccionCliente extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    /** Ya se confirmó: un segundo Enter o clic no debe crear otra factura. */
    private boolean procesando = false;

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
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setWidthFull();

        BigDecimal subtotalProductos = ticket.getTotalAmount() != null ? ticket.getTotalAmount() : BigDecimal.ZERO;

        ComboBox<Cliente> clientCombo = new ComboBox<>("Seleccionar Cliente (Fiado)");
        // Se busca por nombre, apodo, cédula o celular; la lista muestra lo que debe
        clientCombo.setItems(this::coincideCliente, listaClientes);
        clientCombo.setItemLabelGenerator(DialogoSeleccionCliente::etiquetaCliente);
        clientCombo.setWidthFull();

        HorizontalLayout tipoEntregaLayout = new HorizontalLayout();
        tipoEntregaLayout.setWidthFull();
        tipoEntregaLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button btnLocal = new Button("1. Local [Alt+L]", VaadinIcon.SHOP.create());
        Button btnDelivery = new Button("2. Delivery [Alt+D]", VaadinIcon.TRUCK.create());

        btnLocal.setWidth("48%");
        btnDelivery.setWidth("48%");

        VerticalLayout deliveryLayout = new VerticalLayout();
        deliveryLayout.setPadding(false);
        deliveryLayout.setSpacing(true);
        deliveryLayout.setVisible(false);

        TextField direccionField = new TextField("Dirección de entrega");
        direccionField.setPlaceholder("Vacía: la dirección del cliente");
        direccionField.setWidthFull();
        direccionField.setMaxLength(150);
        // Valor al servidor mientras se escribe (Enter llegaba antes que el texto)
        direccionField.setValueChangeMode(ValueChangeMode.EAGER);
        // Ticket renombrado (ej. "29-5"): ese nombre es la dirección
        if (ticket.getNombrePersonalizado() != null) {
            direccionField.setValue(ticket.getNombrePersonalizado());
        }

        TextField telefonoField = new TextField("Teléfono de Contacto");
        telefonoField.setPlaceholder("809-000-0000");
        telefonoField.setWidthFull();

        ComboBox<Delivery> cbDelivery = new ComboBox<>("Asignar Motorista (Delivery)");
        cbDelivery.setItems(listaDeliveries);
        cbDelivery.setItemLabelGenerator(d -> d.getNombre() != null ? d.getNombre() : "");
        cbDelivery.setWidthFull();

        BigDecimalField costoEnvioField = new BigDecimalField("Costo de Delivery (RD$)");
        costoEnvioField.setValue(BigDecimal.ZERO);
        costoEnvioField.setWidthFull();

        // Se agregan TODOS los campos correspondientes al layout de delivery
        deliveryLayout.add(direccionField, telefonoField, cbDelivery, costoEnvioField);

        final boolean[] esDeliveryState = {false};

        btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnDelivery.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Runnable seleccionarLocal = () -> {
            esDeliveryState[0] = false;
            deliveryLayout.setVisible(false);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
           
        };

        Runnable seleccionarDelivery = () -> {
            esDeliveryState[0] = true;
            deliveryLayout.setVisible(true);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
           
        };

        btnLocal.addClickListener(e -> seleccionarLocal.run());
        btnDelivery.addClickListener(e -> seleccionarDelivery.run());

        // Atajos Alt+L y Alt+D
        Shortcuts.addShortcutListener(this, seleccionarLocal::run, Key.KEY_L, KeyModifier.ALT);
        Shortcuts.addShortcutListener(this, seleccionarDelivery::run, Key.KEY_D, KeyModifier.ALT);

        tipoEntregaLayout.add(btnLocal, btnDelivery);

        Span totalFiadoLabel = new Span("Total a Fiar: RD$ " + MONEDA_FORMAT.format(subtotalProductos));
        totalFiadoLabel.getStyle()
                .set("font-size", "1.2rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-error-text-color)");

        Button cancelBtn = new Button("Cancelar [Esc]", e -> close());
        Button processBtn = new Button("Confirmar Fiado [Enter]", VaadinIcon.CHECK.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        // Ficha de crédito del cliente elegido: debe, límite y cómo queda
        Div fichaCredito = new Div();
        fichaCredito.setWidthFull();
        fichaCredito.setVisible(false);
        fichaCredito.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "8px")
                .set("padding", "0.5rem 0.75rem")
                .set("box-sizing", "border-box")
                .set("font-size", "0.9rem");

        Runnable actualizarFicha = () -> {
            BigDecimal envio = costoEnvioField.getValue() != null ? costoEnvioField.getValue() : BigDecimal.ZERO;
            BigDecimal totalFinal = subtotalProductos.add(envio);
            totalFiadoLabel.setText((envio.signum() > 0 ? "Total a Fiar (Incl. Envío): RD$ " : "Total a Fiar: RD$ ")
                    + MONEDA_FORMAT.format(totalFinal));

            boolean puedeFiar = mostrarFichaCredito(fichaCredito, clientCombo.getValue(), totalFinal);
            processBtn.setEnabled(puedeFiar);
        };

        clientCombo.addValueChangeListener(e -> actualizarFicha.run());
        costoEnvioField.addValueChangeListener(e -> actualizarFicha.run());

        layout.add(clientCombo, fichaCredito, tipoEntregaLayout, deliveryLayout, totalFiadoLabel);
        add(layout);

        // Se usa addClickShortcut en lugar de listener manual de Enter para evitar capturar el Enter dentro de los ComboBoxes
        processBtn.addClickShortcut(Key.ENTER);

        processBtn.addClickListener(e -> {
            if (procesando) {
                return;
            }
            Cliente clienteSeleccionado = clientCombo.getValue();
            boolean esDelivery = esDeliveryState[0];

            if (clienteSeleccionado == null) {
                Notification.show("Debe seleccionar un cliente de la libreta", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                clientCombo.focus();
                return;
            }

            if (esDelivery) {
                // Dirección: la escrita, el nombre del ticket o la del cliente.
                // Si no hay ninguna, se pide.
                boolean clienteSinDireccion = clienteSeleccionado.getDireccion() == null
                        || clienteSeleccionado.getDireccion().isBlank();
                if (ticket.direccionEntrega(direccionField.getValue()) == null && clienteSinDireccion) {
                    Notification.show("Escriba la dirección de entrega (o póngale nombre al ticket)",
                            2500, Notification.Position.MIDDLE)
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

            // Validado: desde aquí solo se factura una vez
            procesando = true;
            processBtn.setEnabled(false);

            // 1. Cerrar el diálogo inmediatamente
            close();

            // 2. Notificar al callback de la vista principal
            if (onConfirmar != null) {
                onConfirmar.ejecutar(
                        clienteSeleccionado,
                        esDelivery,
                        cbDelivery.getValue(),
                        // La escrita o el nombre del ticket; null → la del cliente
                        ticket.direccionEntrega(direccionField.getValue()),
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

    /**
     * Llena la ficha con lo que debe el cliente, su límite, lo disponible y
     * cómo queda con esta venta. Devuelve false si no se le puede fiar
     * (deshabilitado o se pasa del límite): la misma regla que valida el
     * servicio al guardar, pero visible antes de confirmar.
     */
    private boolean mostrarFichaCredito(Div ficha, Cliente cliente, BigDecimal totalVenta) {

        ficha.removeAll();
        if (cliente == null) {
            ficha.setVisible(false);
            return true;
        }
        ficha.setVisible(true);

        BigDecimal debe = cliente.getBalance() != null ? cliente.getBalance() : BigDecimal.ZERO;
        BigDecimal limite = cliente.getLimiteCredito();
        BigDecimal quedara = debe.add(totalVenta);

        ficha.add(filaFicha("Debe ahora", "RD$ " + MONEDA_FORMAT.format(debe),
                debe.signum() > 0 ? "var(--lumo-error-text-color)" : null));
        ficha.add(filaFicha("Límite de crédito",
                limite != null ? "RD$ " + MONEDA_FORMAT.format(limite) : "Sin límite", null));
        if (limite != null) {
            BigDecimal disponible = limite.subtract(debe).max(BigDecimal.ZERO);
            ficha.add(filaFicha("Disponible", "RD$ " + MONEDA_FORMAT.format(disponible), null));
        }
        ficha.add(filaFicha("Quedará debiendo", "RD$ " + MONEDA_FORMAT.format(quedara), null));

        String problema = null;
        if (Boolean.FALSE.equals(cliente.getHabilitado())) {
            problema = "Cliente deshabilitado: no se le puede fiar.";
        } else if (limite != null && quedara.compareTo(limite) > 0) {
            problema = limite.signum() == 0
                    ? "Este cliente no tiene crédito (límite RD$ 0.00)."
                    : "Se pasa del límite por RD$ " + MONEDA_FORMAT.format(quedara.subtract(limite)) + ".";
        }

        if (problema != null) {
            Span aviso = new Span("⛔ " + problema);
            aviso.getStyle()
                    .set("display", "block")
                    .set("margin-top", "0.35rem")
                    .set("font-weight", "700")
                    .set("color", "var(--lumo-error-text-color)");
            ficha.add(aviso);
            ficha.getStyle().set("background", "var(--lumo-error-color-10pct)");
            return false;
        }

        ficha.getStyle().set("background", "var(--lumo-contrast-5pct)");
        return true;
    }

    private static HorizontalLayout filaFicha(String etiqueta, String valor, String color) {
        Span lbl = new Span(etiqueta);
        lbl.getStyle().set("color", "var(--lumo-secondary-text-color)");
        Span val = new Span(valor);
        val.getStyle().set("font-weight", "600");
        if (color != null) {
            val.getStyle().set("color", color);
        }
        HorizontalLayout fila = new HorizontalLayout(lbl, val);
        fila.setWidthFull();
        fila.setPadding(false);
        fila.setSpacing(false);
        fila.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return fila;
    }

    /** "JUAN PÉREZ (Juancito) — debe RD$ 1,250.00" */
    private static String etiquetaCliente(Cliente c) {
        StringBuilder sb = new StringBuilder(c.getNombre() != null ? c.getNombre() : "");
        if (c.getApodo() != null && !c.getApodo().isBlank()) {
            sb.append(" (").append(c.getApodo().trim()).append(")");
        }
        if (c.getBalance() != null && c.getBalance().signum() > 0) {
            sb.append(" — debe RD$ ").append(MONEDA_FORMAT.format(c.getBalance()));
        }
        return sb.toString();
    }

    /** Todas las palabras escritas deben aparecer en nombre, apodo, cédula o celular. */
    private boolean coincideCliente(Cliente c, String texto) {
        if (texto == null || texto.isBlank()) {
            return true;
        }
        String pajar = String.join(" ",
                c.getNombre() != null ? c.getNombre() : "",
                c.getApodo() != null ? c.getApodo() : "",
                c.getCedula() != null ? c.getCedula() : "",
                c.getCelular() != null ? c.getCelular() : "").toLowerCase();
        for (String palabra : texto.toLowerCase().trim().split("\\s+")) {
            if (!pajar.contains(palabra)) {
                return false;
            }
        }
        return true;
    }
}

