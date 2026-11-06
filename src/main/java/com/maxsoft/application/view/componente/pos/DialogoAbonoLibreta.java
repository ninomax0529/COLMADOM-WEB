/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Abono en efectivo a la libreta de un cliente desde el POS. Genera un recibo
 * de ingreso aplicado a sus facturas a crédito (de la más vieja a la más
 * nueva), rebaja su estado de cuenta y registra la entrada en caja.
 */
public class DialogoAbonoLibreta extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    private final ReciboDeIngresoService reciboService;
    private final String usuario;
    private final Runnable onEnfocarBuscador;

    private final ComboBox<Cliente> clientCombo = new ComboBox<>("Seleccionar Cliente");
    private final BigDecimalField abonoField = new BigDecimalField("Monto del Abono (RD$)");
    private final Span pendienteLabel = new Span("Pendiente: RD$ 0.00");
    private final Span nuevoBalanceLabel = new Span("Nuevo balance: RD$ 0.00");
    private final Button processBtn = new Button("Registrar Abono [Enter]", VaadinIcon.CHECK.create());

    private BigDecimal pendienteActual = BigDecimal.ZERO;

    public DialogoAbonoLibreta(List<Cliente> libretaClientes, ReciboDeIngresoService reciboService,
            String usuario, Runnable onEnfocarBuscador) {

        this.reciboService = reciboService;
        this.usuario = usuario;
        this.onEnfocarBuscador = onEnfocarBuscador;

        setHeaderTitle("Abonar a Libreta de Fiados");
        setWidth("450px");

        clientCombo.setItems(libretaClientes);
        clientCombo.setItemLabelGenerator(c -> c.getNombre()
                + (c.getBalance() != null ? "  (debe RD$ " + MONEDA_FORMAT.format(c.getBalance()) + ")" : ""));
        clientCombo.setPlaceholder(libretaClientes.isEmpty() ? "Ningún cliente debe" : "Buscar cliente...");
        clientCombo.setWidthFull();

        abonoField.setWidthFull();
        abonoField.setEnabled(false);
        abonoField.setValueChangeMode(ValueChangeMode.EAGER);

        pendienteLabel.getStyle().set("font-weight", "bold");
        nuevoBalanceLabel.getStyle().set("font-weight", "bold").set("color", "var(--lumo-success-text-color)");

        clientCombo.addValueChangeListener(e -> seleccionarCliente(e.getValue()));
        abonoField.addValueChangeListener(e -> actualizarNuevoBalance());

        VerticalLayout layout = new VerticalLayout(clientCombo, pendienteLabel, abonoField, nuevoBalanceLabel);
        layout.setPadding(false);
        add(layout);

        Button cancelBtn = new Button("Cancelar", e -> cerrar());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        processBtn.addClickShortcut(Key.ENTER);
        processBtn.setDisableOnClick(true);
        processBtn.addClickListener(e -> registrar());

        getFooter().add(cancelBtn, processBtn);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                clientCombo.focus();
            }
        });
    }

    /** Compatibilidad con vistas anteriores del POS que no pasan el servicio. */
    public DialogoAbonoLibreta(List<Cliente> libretaClientes, Runnable onEnfocarBuscador) {
        this(libretaClientes, null, "Admin", onEnfocarBuscador);
    }

    public DialogoAbonoLibreta(List<Cliente> libretaClientes) {
        this(libretaClientes, null, "Admin", null);
    }

    private void seleccionarCliente(Cliente c) {

        pendienteActual = BigDecimal.ZERO;
        if (c != null && reciboService != null) {
            pendienteActual = reciboService.pendienteLibreta(c.getCodigo());
        }
        pendienteLabel.setText("Pendiente en facturas: RD$ " + MONEDA_FORMAT.format(pendienteActual));

        boolean puedeAbonar = pendienteActual.signum() > 0;
        abonoField.setEnabled(puedeAbonar);
        abonoField.clear();
        if (puedeAbonar) {
            abonoField.focus();
        } else if (c != null) {
            Notification.show("El cliente no tiene facturas a crédito pendientes.", 2500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        }
        actualizarNuevoBalance();
    }

    private void actualizarNuevoBalance() {

        BigDecimal abono = abonoField.getValue() != null ? abonoField.getValue() : BigDecimal.ZERO;
        BigDecimal restante = pendienteActual.subtract(abono);
        boolean excede = restante.signum() < 0;
        nuevoBalanceLabel.setText(excede
                ? "El abono supera lo pendiente"
                : "Nuevo balance: RD$ " + MONEDA_FORMAT.format(restante));
        nuevoBalanceLabel.getStyle().set("color", excede
                ? "var(--lumo-error-text-color)" : "var(--lumo-success-text-color)");
    }

    private void registrar() {

        try {
            Cliente c = clientCombo.getValue();
            BigDecimal abono = abonoField.getValue();

            if (reciboService == null) {
                aviso("El abono a libreta no está disponible en esta pantalla.");
                return;
            }
            if (c == null) {
                aviso("Seleccione el cliente.");
                return;
            }
            if (abono == null || abono.signum() <= 0) {
                aviso("Ingresa un monto de abono válido.");
                return;
            }
            if (abono.compareTo(pendienteActual) > 0) {
                aviso("El abono no puede ser mayor que lo pendiente (RD$ " + MONEDA_FORMAT.format(pendienteActual) + ").");
                return;
            }

            ReciboDeIngreso recibo = reciboService.abonarLibreta(c.getCodigo(), abono, usuario);

            Notification.show("Abono de RD$ " + MONEDA_FORMAT.format(abono) + " registrado a " + c.getNombre()
                    + " (Recibo #" + recibo.getCodigo() + ").", 3500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            cerrar();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            aviso(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("No se pudo registrar el abono.", 3500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } finally {
            processBtn.setEnabled(true);
        }
    }

    private void aviso(String mensaje) {
        Notification.show(mensaje, 3500, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    private void cerrar() {
        close();
        if (onEnfocarBuscador != null) {
            onEnfocarBuscador.run();
        }
    }
}
