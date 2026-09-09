/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;

public class DialogoRenombrarTicket extends Dialog {

    public DialogoRenombrarTicket(
            TicketVenta ticket,
            Span ticketNameSpan,
            Consumer<String> onRenombradoExitoso
    ) {
        setHeaderTitle("Renombrar Ticket");
        setWidth("350px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        TextField nameField = new TextField("Nuevo nombre del cliente / venta");
        nameField.setValue(ticket.getId());
        nameField.setWidthFull();
        nameField.setClearButtonVisible(true);

        dialogLayout.add(nameField);
        add(dialogLayout);

        Button cancelBtn = new Button("Cancelar", e -> close());

        Button saveBtn = new Button("Guardar [Enter]", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickShortcut(Key.ENTER);

        saveBtn.addClickListener(e -> {
            String nuevoNombre = nameField.getValue();
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                String nombreLimpio = nuevoNombre.trim();
                ticket.setId(nombreLimpio);
                ticketNameSpan.setText(nombreLimpio);

                Notification.show("Venta renombrada a: " + nombreLimpio, 2000, Notification.Position.BOTTOM_END);

                close();

                if (onRenombradoExitoso != null) {
                    onRenombradoExitoso.accept(nombreLimpio);
                }
            } else {
                close();
            }
        });

        getFooter().add(cancelBtn, saveBtn);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                nameField.focus();
                nameField.getElement().executeJs("this.focus(); this.select();");
            }
        });
    }
}
