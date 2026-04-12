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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class CobroDialog extends Dialog {

    private NumberField total = new NumberField();
    private NumberField recibido = new NumberField();
    private NumberField devuelta = new NumberField();

    private String metodo = "EFECTIVO";
    private Button selectedMetodo;

    public CobroDialog(double totalVenta) {

        setWidth("400px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        // ===== TOTAL =====
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

        // ===== RECIBIDO =====
        recibido.setWidthFull();
        recibido.setPlaceholder("Monto recibido");

        recibido.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "bold");
        
        recibido.setValueChangeMode(ValueChangeMode.EAGER);

        recibido.addValueChangeListener(e -> calcular());

        // ===== DEVUELTA =====
        devuelta.setReadOnly(true);
        devuelta.setWidthFull();

        // ===== METODOS =====
        Button btnEfectivo = crearMetodo("💵 EFECTIVO", "#22c55e", "EFECTIVO");
        Button btnTarjeta = crearMetodo("💳 TARJETA", "#3b82f6", "TARJETA");
        Button btnTransferencia = crearMetodo("🏦 TRANSFERENCIA", "#6366f1", "TRANSFERENCIA");

        VerticalLayout metodos = new VerticalLayout(
                btnEfectivo, btnTarjeta, btnTransferencia
        );
        metodos.setWidthFull();
        metodos.setPadding(false);
        metodos.setSpacing(true);

        // ===== MONTO RAPIDO =====
        HorizontalLayout montos = new HorizontalLayout(
                crearMonto(100),
                crearMonto(500),
                crearMonto(1000),
                crearMonto(2000)
        );

        // ===== BOTON COBRAR =====
        Button cobrar = new Button("💰 COBRAR (ENTER)");
        cobrar.setWidthFull();
        cobrar.getStyle()
                .set("font-size", "18px")
                .set("height", "50px");

        cobrar.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        cobrar.addClickListener(e -> cobrar());

        // ===== BOTON CANCELAR =====
        Button cancelar = new Button("❌ Cancelar", e -> close());
        cancelar.setWidthFull();

        // ===== LAYOUT =====
        VerticalLayout layout = new VerticalLayout(
                new H3("Cobro"),
                total,
                new H3("Recibido"),
                recibido,
                new H3("Devuelta"),
                devuelta,
                new H3("Método de pago"),
                metodos,
                new H3("Monto rápido"),
                montos,
                cobrar,
                cancelar
        );

        layout.setPadding(true);
        layout.setSpacing(true);

        add(layout);

        // ===== COMPORTAMIENTO INICIAL =====
        btnEfectivo.click(); // selecciona efectivo por defecto
        recibido.focus();

//        // ===== ATAJOS =====
//        addKeyPressListener(Key.ENTER, e -> cobrar());
//        addKeyPressListener(Key.ESCAPE, e -> close());
    }

    // ===============================
    // METODOS
    // ===============================

    private Button crearMetodo(String text, String color, String tipo) {
        Button btn = new Button(text);
        btn.setWidthFull();

        btn.getStyle()
                .set("background", "#e5e7eb")
                .set("font-weight", "bold")
                .set("border-radius", "10px");

        btn.addClickListener(e -> {
            metodo = tipo;

            // reset anterior
            if (selectedMetodo != null) {
                selectedMetodo.getStyle().set("background", "#e5e7eb");
                selectedMetodo.getStyle().set("color", "black");
            }

            // activo
            btn.getStyle()
                    .set("background", color)
                    .set("color", "white");

            selectedMetodo = btn;

            // comportamiento efectivo
            if (tipo.equals("EFECTIVO")) {
                recibido.setValue(total.getValue());
                calcular();
            }
        });

        return btn;
    }

    private Button crearMonto(double monto) {
        Button btn = new Button("$" + (int) monto);

        btn.getStyle()
                .set("background", "#e2e8f0")
                .set("font-weight", "bold");

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

        // sonido (opcional)
        getElement().executeJs("new Audio('sounds/beep.mp3').play()");

        System.out.println("Cobrado método: " + metodo);

        close();
    }
}
