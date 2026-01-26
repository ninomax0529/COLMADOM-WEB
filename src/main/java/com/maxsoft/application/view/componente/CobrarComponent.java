/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class CobrarComponent extends VerticalLayout {

    private Consumer<Boolean> estadoValidezListener;
    private final BigDecimalField montoRecibido = new BigDecimalField("DINERO RECIBIDO");
    private final BigDecimalField montoACobrar = new BigDecimalField("TOTAL A COBRAR");
    private final BigDecimalField montoADevolver = new BigDecimalField("DEVUELTA");

    public CobrarComponent() {
        configurarCampos();
        configurarEventos();

        add(                      
                montoACobrar,
                montoRecibido,
                montoADevolver
        );

        setPadding(true);
        setSpacing(true);
        addClassNames("pos-header");
//        addClassName("pos-header");
    }

    public void limpiarMonto() {
        montoRecibido.clear();
        montoACobrar.clear();
        montoADevolver.clear();
    }

    private void configurarCampos() {

//        montoRecibido.setPrefixComponent(new Text("$"));
//        montoACobrar.setPrefixComponent(new Text("$"));
//        montoADevolver.setPrefixComponent(new Text("$"));
        montoADevolver.setReadOnly(true);
        montoACobrar.setReadOnly(true);
        montoRecibido.setReadOnly(true);

        montoRecibido.setClearButtonVisible(true);
        montoACobrar.setClearButtonVisible(true);
        montoRecibido.setValueChangeMode(ValueChangeMode.EAGER);
        montoACobrar.addClassName("monto-total-pos");
        montoADevolver.addClassName("monto-devolucion-pos");
        montoRecibido.addClassName("monto-recibido-pos");

    }

    private void configurarEventos() {
        montoRecibido.addValueChangeListener(e -> recalcular());
        montoACobrar.addValueChangeListener(e -> recalcular());
    }

    private void recalcular() {
        
        BigDecimal recibido = valorSeguro(montoRecibido.getValue());
        BigDecimal cobrar = valorSeguro(montoACobrar.getValue());

        if(cobrar.doubleValue()>0){
              montoRecibido.setReadOnly(false);

        }
        
        boolean valido = cobrar.signum() > 0 && recibido.compareTo(cobrar) >= 0;

        BigDecimal devolver = valido
                ? recibido.subtract(cobrar)
                : BigDecimal.ZERO;

        montoRecibido.setInvalid(!valido && cobrar.signum() > 0);
        montoRecibido.setErrorMessage(
                valido ? null : "El dinero recibido no cubre el total"
        );

        montoADevolver.setValue(devolver);

        if (estadoValidezListener != null) {
            estadoValidezListener.accept(valido);
        }
    }


    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    // 🔐 Para usar antes de guardar
    public boolean isCobroValido() {
        return !montoRecibido.isInvalid();
    }

    // Getters
    public BigDecimal getMontoRecibido() {
        return valorSeguro(montoRecibido.getValue());
    }

    public BigDecimal getMontoADevolver() {
        return valorSeguro(montoADevolver.getValue());
    }

    public void setEstadoValidezListener(Consumer<Boolean> listener) {
        this.estadoValidezListener = listener;
    }

    public BigDecimal getMontoACobrar() {
        return valorSeguro(montoACobrar.getValue());
    }

    public void setMontoACobrar(BigDecimal monto) {
        montoACobrar.setValue(monto != null ? monto : BigDecimal.ZERO);
        recalcular(); // clave: recalcular al setear desde fuera
    }
}
