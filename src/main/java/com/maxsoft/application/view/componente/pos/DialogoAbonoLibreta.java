/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Cliente;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class DialogoAbonoLibreta extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    public DialogoAbonoLibreta(List<Cliente> libretaClientes, Runnable onEnfocarBuscador) {
        setHeaderTitle("Abonar a Libreta de Fiados");
        setWidth("450px");

        VerticalLayout layout = new VerticalLayout();

        ComboBox<Cliente> clientCombo = new ComboBox<>("Seleccionar Cliente");
        clientCombo.setItems(libretaClientes);
        clientCombo.setItemLabelGenerator(c -> c.getNombre());
        clientCombo.setWidthFull();

        BigDecimalField abonoField = new BigDecimalField("Monto del Abono (RD$)");
        abonoField.setWidthFull();
        abonoField.setEnabled(false);

        Span nuevoBalanceLabel = new Span("Nuevo balance: RD$ 0.00");
        nuevoBalanceLabel.getStyle().set("font-weight", "bold").set("color", "var(--lumo-success-text-color)");

        clientCombo.addValueChangeListener(e -> {

            Cliente c = e.getValue();

//          if (c != null && c.get().compareTo(BigDecimal.ZERO) > 0) {
//              abonoField.setEnabled(true);
//              abonoField.setValue(BigDecimal.ZERO);
//              abonoField.focus();
//          } else {
//              abonoField.setEnabled(false);
//          }
        });

        abonoField.addValueChangeListener(e -> {

            Cliente c = clientCombo.getValue();
            BigDecimal abono = e.getValue();
            if (c != null && abono != null) {

//              BigDecimal restante = c.getBalancePendiente().subtract(abono);
//              
//              if (restante.compareTo(BigDecimal.ZERO) < 0) {
//                  
//                  restante = BigDecimal.ZERO;
//              }
//              nuevoBalanceLabel.setText("Nuevo balance: RD$ " + MONEDA_FORMAT.format(restante));
            }
        });

        layout.add(clientCombo, abonoField, nuevoBalanceLabel);
        add(layout);

        Button cancelBtn = new Button("Cancelar", e -> close());
        Button processBtn = new Button("Registrar Abono [Enter]", VaadinIcon.CHECK.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        processBtn.addClickShortcut(Key.ENTER);

        processBtn.addClickListener(e -> {

            Cliente c = clientCombo.getValue();
            BigDecimal abono = abonoField.getValue();
            if (c != null && abono != null && abono.compareTo(BigDecimal.ZERO) > 0) {

//              c.setBalancePendiente(c.getBalancePendiente().subtract(abono));
//              
//              if (c.getBalancePendiente().compareTo(BigDecimal.ZERO) < 0) {
//                  c.setBalancePendiente(BigDecimal.ZERO);
//              }
//              
//              Notification.show("Abono de RD$ " + MONEDA_FORMAT.format(abono) + " registrado a "
////                      + c.getDisplayName(), 3000, Notification.Position.MIDDLE)
//                      .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                close();
                if (onEnfocarBuscador != null) {
                    onEnfocarBuscador.run();
                }
            } else {
                Notification.show("Ingresa un monto de abono válido", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });

        getFooter().add(cancelBtn, processBtn);
        open();
    }

    // Sobrecarga por si no se especifica el callback para enfocar el buscador
    public DialogoAbonoLibreta(List<Cliente> libretaClientes) {
        this(libretaClientes, null);
    }
}