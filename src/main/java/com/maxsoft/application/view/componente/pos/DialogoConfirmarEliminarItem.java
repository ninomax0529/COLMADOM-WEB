/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;

public class DialogoConfirmarEliminarItem extends Dialog {

    public DialogoConfirmarEliminarItem(TicketVenta ticket, DetalleFacturaDeVenta item, Runnable onEnfocarBuscador) {
        setHeaderTitle("Eliminar Producto");
        add("¿Deseas quitar \"" + item.getDescripcionArticulo() + "\" del ticket?");

        Button cancelBtn = new Button("Cancelar", e -> close());

        Button removeBtn = new Button("Sí, quitar [Enter]", e -> {
            ticket.getItems().remove(item);
            ticket.updateUI();
            close();
            if (onEnfocarBuscador != null) {
                onEnfocarBuscador.run();
            }
        });
        removeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        removeBtn.addClickShortcut(Key.ENTER);

        getFooter().add(cancelBtn, removeBtn);
        open();
    }

    // Sobrecarga por si no se requiere ejecutar el enfocarBuscador
    public DialogoConfirmarEliminarItem(TicketVenta ticket, DetalleFacturaDeVenta item) {
        this(ticket, item, null);
    }
}
