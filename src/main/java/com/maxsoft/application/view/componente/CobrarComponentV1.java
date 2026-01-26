/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;

import java.math.BigDecimal;

public class CobrarComponentV1 extends VerticalLayout {

    private final BigDecimalField montoRecibido = new BigDecimalField("Monto recibido");
    private final BigDecimalField montoACobrar = new BigDecimalField("Monto a cobrar");
    private final BigDecimalField montoADevolver = new BigDecimalField("Monto a devolver");

    public CobrarComponentV1() {
        configurarCampos();
        configurarEventos();

        add(
                new Text("Cobro"),
                montoRecibido,
                montoACobrar,
                montoADevolver
        );

        setPadding(false);
        setSpacing(true);
    }

    private void configurarCampos() {
//        montoRecibido.setPrefixComponent(new Text("$"));
//        montoACobrar.setPrefixComponent(new Text("$"));
//        montoADevolver.setPrefixComponent(new Text("$"));
//
        montoADevolver.setReadOnly(true);
         montoACobrar.setReadOnly(true);
//
//        // Opcional pero válido
        montoRecibido.setClearButtonVisible(true);
        montoACobrar.setClearButtonVisible(true);
    }

    private void configurarEventos() {
        montoRecibido.addValueChangeListener(e -> calcularDevolucion());
        montoACobrar.addValueChangeListener(e -> calcularDevolucion());
    }

    private void calcularDevolucion() {
        BigDecimal recibido = montoRecibido.getValue() != null
                ? montoRecibido.getValue()
                : BigDecimal.ZERO;

        BigDecimal cobrar = montoACobrar.getValue() != null
                ? montoACobrar.getValue()
                : BigDecimal.ZERO;

        BigDecimal devolver = recibido.subtract(cobrar);

        montoADevolver.setValue(
                devolver.signum() > 0 ? devolver : BigDecimal.ZERO
        );
    }

    // Getters útiles para la vista / servicio
    public BigDecimal getMontoRecibido() {
        return montoRecibido.getValue();
    }

    public BigDecimal getMontoACobrar() {
        return montoACobrar.getValue();
    }

    public BigDecimal getMontoADevolver() {
        return montoADevolver.getValue();
    }
}
