/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import java.util.function.BiConsumer;

public class DialogoVentaPorMonto extends Dialog {

    private static final DecimalFormat CANTIDAD_FORMAT = new DecimalFormat("#,##0.00");

    public DialogoVentaPorMonto(
            String producto,
            Double precioUnitario,
            BiConsumer<Double, Double> onAceptar // Recibe (monto, cantidadCalculada)
    ) {
        setHeaderTitle("Vender por Dinero (RD$)");
        setWidth("360px");

        VerticalLayout layout = new VerticalLayout();

        Span infoLabel = new Span(producto + " - Precio: RD$ " + precioUnitario + " / unidad o lb");
        infoLabel.getStyle().set("font-size", "0.85rem").set("color", "var(--lumo-secondary-text-color)");

        BigDecimalField montoField = new BigDecimalField("Monto deseado (RD$)");
        montoField.setPlaceholder("Ej: 50, 100, 150");
        montoField.setWidthFull();
        montoField.setValueChangeMode(ValueChangeMode.EAGER);

        Span resultadoSpan = new Span("Cantidad calculada: 0.00");
        resultadoSpan.getStyle().set("font-weight", "bold").set("color", "var(--lumo-primary-color)");

        montoField.addValueChangeListener(e -> {
            BigDecimal val = e.getValue();
            Double monto = (val != null) ? val.doubleValue() : null;

            if (monto != null && monto > 0 && precioUnitario > 0) {

                Double cantidadCalculada = monto / precioUnitario;
//              Double cantidadCalculada = montodivide(precioUnitario, 3, RoundingMode.HALF_UP);

                resultadoSpan.setText("Cantidad calculada: " + CANTIDAD_FORMAT.format(cantidadCalculada));
            } else {
                resultadoSpan.setText("Cantidad calculada: 0.00");
            }
        });

        layout.add(infoLabel, montoField, resultadoSpan);
        add(layout);

        Button cancelBtn = new Button("Cancelar", e -> close());
        Button acceptBtn = new Button("Agregar al Ticket [Enter]", VaadinIcon.CHECK.create());
        acceptBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        acceptBtn.addClickShortcut(Key.ENTER);

        acceptBtn.addClickListener(e -> {

            BigDecimal val = montoField.getValue();
            Double monto = (val != null) ? val.doubleValue() : null;

            if (monto != null && monto > 0) {
//
//              Double cantidadCalculada = monto/precioUnitario;
//              
//              agregarAlTicketActivo();
                close();
                if (onAceptar != null) {
                    Double cantidadCalculada = (precioUnitario > 0) ? (monto / precioUnitario) : 0.0;
                    onAceptar.accept(monto, cantidadCalculada);
                }
            } else {
                Notification.show("Ingresa un monto válido en RD$", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });

        getFooter().add(cancelBtn, acceptBtn);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                montoField.focus();
            }
        });
    }
}
