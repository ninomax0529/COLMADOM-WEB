/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;

/**
 *
 * @author maximilianoalmonte
 */
public class VistaCobrar extends Dialog {

    private H3 totalLabel = new H3();

    private NumberField recibido = new NumberField("Dinero recibido");

    private H3 cambio = new H3("0");

    private RadioButtonGroup<String> metodoPago = new RadioButtonGroup<>();

    private Button confirmar = new Button("Confirmar");
    private Button cancelar = new Button("Cancelar");

    private double total;

    private Runnable onConfirmar;

    public VistaCobrar(double total, Runnable onConfirmar) {

        this.total = total;
        this.onConfirmar = onConfirmar;

        setWidth("400px");

        totalLabel.setText("Total: $" + total);

        metodoPago.setLabel("Método de pago");

        metodoPago.setItems(
                "Efectivo",
                "Tarjeta",
                "Transferencia"
        );

        recibido.addValueChangeListener(e -> calcularCambio());

        confirmar.addClickListener(e -> confirmarPago());

        cancelar.addClickListener(e -> close());

        VerticalLayout layout = new VerticalLayout(

                new H3("Cobrar"),

                totalLabel,

                recibido,

                new HorizontalLayout(
                        new H3("Cambio: "),
                        cambio
                ),

                metodoPago,

                new HorizontalLayout(
                        confirmar,
                        cancelar
                )
        );

        add(layout);
    }

    private void calcularCambio() {

        if (recibido.getValue() != null) {

            double c = recibido.getValue() - total;

            cambio.setText(String.valueOf(c));
        }
    }

    private void confirmarPago() {

        if (metodoPago.getValue() == null) {

            Notification.show("Seleccione método de pago");

            return;
        }

        if (onConfirmar != null) {
            onConfirmar.run();
        }

        close();
    }
}