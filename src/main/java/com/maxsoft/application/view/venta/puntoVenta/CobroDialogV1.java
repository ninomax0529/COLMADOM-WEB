/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

/**
 *
 * @author maximilianoalmonte
 */

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class CobroDialogV1 extends Dialog {

    private NumberField total = new NumberField();
    private NumberField recibido = new NumberField();
    private NumberField devuelta = new NumberField();

    private String metodo = "EFECTIVO";
    private Button selectedMetodo;

    public CobroDialogV1(double totalVenta) {

        setWidth("650px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        // ===== IZQUIERDA =====
        total.setValue(totalVenta);
        total.setReadOnly(true);
        total.setWidthFull();

        total.getStyle()
                .set("font-size", "36px")
                .set("font-weight", "900")
                .set("text-align", "center")
                .set("color", "#16a34a")
                .set("background", "#ecfdf5")
                .set("padding", "10px")
                .set("border-radius", "10px");

        recibido.setWidthFull();
        recibido.setPlaceholder("Monto recibido");
        recibido.setValueChangeMode(ValueChangeMode.EAGER);

        recibido.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "bold");

        recibido.addValueChangeListener(e -> calcular());

        devuelta.setReadOnly(true);
        devuelta.setWidthFull();

        VerticalLayout izquierda = new VerticalLayout(
                new H1("TOTAL"),
                total,
                new H1("RECIBIDO"),
                recibido,
                new H1("DEVUELTA"),
                devuelta
        );
        izquierda.setSizeFull();

        // ===== DERECHA =====
        Button btnEfectivo = crearMetodo("💵 EFECTIVO", "#22c55e", "EFECTIVO");
        Button btnTarjeta = crearMetodo("💳 TARJETA", "#3b82f6", "TARJETA");
        Button btnTransferencia = crearMetodo("🏦 TRANSFER", "#6366f1", "TRANSFERENCIA");

        VerticalLayout metodos = new VerticalLayout(
                btnEfectivo, btnTarjeta, btnTransferencia
        );
        metodos.setWidthFull();

        // MONTO RAPIDO
        HorizontalLayout fila1 = new HorizontalLayout(
                crearMonto(100),
                crearMonto(500)
        );

        HorizontalLayout fila2 = new HorizontalLayout(
                crearMonto(1000),
                crearMonto(2000)
        );

        // BOTONES
        Button cobrar = new Button("💰 COBRAR (ENTER)");
        cobrar.setWidthFull();
        cobrar.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        cobrar.getStyle().set("height", "50px");

        cobrar.addClickListener(e -> cobrar());

        Button cancelar = new Button("❌ Cancelar", e -> close());
        cancelar.setWidthFull();

        VerticalLayout derecha = new VerticalLayout(
//                new H1("Método de pago"),
//                metodos,
//                new H1("Monto rápido"),            
                cobrar,
                cancelar
        );

        derecha.setWidth("250px");

        // ===== CONTENEDOR PRINCIPAL =====
        HorizontalLayout root = new HorizontalLayout(izquierda);
        root.setSizeFull();
        root.expand(izquierda);

        add(root,cobrar,
                cancelar);

        // ===== INICIAL =====
        btnEfectivo.click();
        recibido.focus();

//        // ===== ATAJOS =====
//        addKeyPressListener(Key.ENTER, e -> cobrar());
//        addKeyPressListener(Key.ESCAPE, e -> close());
    }

    private Button crearMetodo(String text, String color, String tipo) {
        Button btn = new Button(text);
        btn.setWidthFull();

        btn.getStyle()
                .set("background", "#e5e7eb")
                .set("font-weight", "bold");

        btn.addClickListener(e -> {
            metodo = tipo;

            if (selectedMetodo != null) {
                selectedMetodo.getStyle().set("background", "#e5e7eb");
                selectedMetodo.getStyle().set("color", "black");
            }

            btn.getStyle()
                    .set("background", color)
                    .set("color", "white");

            selectedMetodo = btn;

            if (tipo.equals("EFECTIVO")) {
                recibido.setValue(total.getValue());
                calcular();
            }
        });

        return btn;
    }

    private Button crearMonto(double monto) {
        Button btn = new Button("$" + (int) monto);

        btn.setWidth("80px");

        btn.addClickListener(e -> {
            double actual = recibido.getValue() != null ? recibido.getValue() : 0;
            recibido.setValue(actual + monto);
            calcular();
        });

        return btn;
    }

    private void calcular() {
        double t = total.getValue() != null ? total.getValue() : 0;
        double r = recibido.getValue() != null ? recibido.getValue() : 0;

        double d = r - t;
        devuelta.setValue(d < 0 ? 0 : d);

        if (r < t) {
            devuelta.getStyle().set("color", "red");
        } else {
            devuelta.getStyle().set("color", "#16a34a");
        }
    }

    private void cobrar() {
        double t = total.getValue();
        double r = recibido.getValue() != null ? recibido.getValue() : 0;

        if (r < t) {
            recibido.getStyle().set("border", "2px solid red");
            return;
        }

        getElement().executeJs("new Audio('sounds/beep.mp3').play()");

        close();
    }
}