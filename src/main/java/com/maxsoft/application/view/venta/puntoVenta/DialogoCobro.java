/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

/**
 *
 * @author maximilianoalmonte
 */

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;

public class DialogoCobro extends Dialog {

    private NumberField totalPagar = new NumberField("Total a Pagar");
    private NumberField montoRecibido = new NumberField("Monto Recibido");
    private NumberField devuelta = new NumberField("Devuelta");

    public DialogoCobro(double total) {

        setWidth("420px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        // CONTENEDOR PRINCIPAL
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);

        card.getStyle()
                .set("background", "#f8fafc")
                .set("border-radius", "15px")
                .set("box-shadow", "0 10px 25px rgba(0,0,0,0.15)");

        // TITULO
        H3 titulo = new H3("💳 Cobrar Venta");

        // TOTAL
        totalPagar.setValue(total);
        totalPagar.setReadOnly(true);
        totalPagar.setWidthFull();

        totalPagar.getStyle()
                .set("font-size", "26px")
                .set("font-weight", "bold")
                .set("color", "#16a34a");

        // RECIBIDO
        montoRecibido.setWidthFull();

        // DEVUELTA
        devuelta.setWidthFull();
        devuelta.setReadOnly(true);

        devuelta.getStyle()
                .set("font-weight", "bold")
                .set("color", "#0ea5e9");

        montoRecibido.addValueChangeListener(e -> calcularDevuelta());

        // METODOS DE PAGO
        Button btnEfectivo = crearBoton("💵 Efectivo", "#22c55e");
        Button btnTarjeta = crearBoton("💳 Tarjeta", "#3b82f6");
        Button btnTransferencia = crearBoton("🏦 Transferencia", "#6366f1");

        HorizontalLayout metodos = new HorizontalLayout(btnEfectivo, btnTarjeta, btnTransferencia);
        metodos.setWidthFull();
        metodos.setSpacing(true);

        // BOTONES RAPIDOS DE EFECTIVO
        HorizontalLayout montosRapidos = new HorizontalLayout();
        montosRapidos.setWidthFull();

        montosRapidos.add(
                crearMontoRapido(100),
                crearMontoRapido(500),
                crearMontoRapido(1000),
                crearMontoRapido(2000)
        );

        // BOTONES ACCION
        Button btnCobrar = new Button("Cobrar (Enter)");
        btnCobrar.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnCobrar.setWidthFull();

        btnCobrar.addClickShortcut(Key.ENTER);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnCancelar.setWidthFull();

        btnCobrar.addClickListener(e -> cobrar());
        btnCancelar.addClickListener(e -> close());

        // ARMAR
        card.add(
                titulo,
                totalPagar,
                montoRecibido,
                devuelta,
                new H3("Método de Pago"),
                metodos,
                new H3("Monto rápido"),
                montosRapidos,
                btnCobrar,
                btnCancelar
        );

        add(card);

        montoRecibido.focus();
    }

    private Button crearBoton(String texto, String color) {
        Button btn = new Button(texto);
        btn.setWidthFull();

        btn.getStyle()
                .set("background", color)
                .set("color", "white")
                .set("font-weight", "bold");

        return btn;
    }

    private Button crearMontoRapido(double monto) {
        Button btn = new Button(String.valueOf((int) monto));
        btn.setWidthFull();

        btn.addClickListener(e -> {
            montoRecibido.setValue(monto);
            calcularDevuelta();
        });

        return btn;
    }

    private void calcularDevuelta() {
        double total = totalPagar.getValue() != null ? totalPagar.getValue() : 0;
        double recibido = montoRecibido.getValue() != null ? montoRecibido.getValue() : 0;

        double dev = recibido - total;
        devuelta.setValue(dev < 0 ? 0 : dev);
    }

    private void cobrar() {
        double total = totalPagar.getValue();
        double recibido = montoRecibido.getValue() != null ? montoRecibido.getValue() : 0;

        if (recibido < total) {
            montoRecibido.getStyle().set("border", "2px solid red");
            return;
        }

        System.out.println("Cobro realizado");
        close();
    }
}
