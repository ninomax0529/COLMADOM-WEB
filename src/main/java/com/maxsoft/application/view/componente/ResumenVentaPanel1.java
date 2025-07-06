/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;

/**
 *
 * @author maximilianoalmonte
 */
public class ResumenVentaPanel1 extends VerticalLayout {

    private final NumberField subtotalField = new NumberField("Subtotal");
    private final NumberField descuentoField = new NumberField("Descuento");
    private final NumberField itbisField = new NumberField("ITBIS");
    private final NumberField totalField = new NumberField("Total");
    private final NumberField cantidadArticulosField = new NumberField("Artículos");

    public ResumenVentaPanel1() {
        setPadding(true);
        setSpacing(false);
        
        getStyle().set("border", "1px solid #ccc");
        getStyle().set("border-radius", "8px");
        getStyle().set("padding", "1rem");
        getStyle().set("background-color", "#f8f9fa");

        // Configurar campos
        configurarCampo(subtotalField, false);
        configurarCampo(descuentoField, false);
        configurarCampo(itbisField, false);
        configurarCampo(totalField, true); // Total resaltado
        configurarCampo(cantidadArticulosField, false);

        add(subtotalField, descuentoField, itbisField, totalField, cantidadArticulosField);
    }

    private void configurarCampo(NumberField campo, boolean esTotal) {
        campo.setReadOnly(true);
        campo.setWidth("100%");
        campo.setMin(0);
        campo.setValue(0.0);

        if (esTotal) {
            campo.getStyle().set("font-weight", "bold");
            campo.getStyle().set("font-size", "1.2rem");
            campo.getStyle().set("color", "#155724");
            campo.getStyle().set("background-color", "#d4edda");
        } else {
            campo.getStyle().set("background-color", "#e9ecef");
        }
    }

    // Métodos públicos para actualizar los valores desde la vista
    public void setSubtotal(double subtotal) {
        subtotalField.setValue(subtotal);
    }

    public void setDescuento(double descuento) {
        descuentoField.setValue(descuento);
    }

    public void setItbis(double itbis) {
        itbisField.setValue(itbis);
    }

    public void setTotal(double total) {
        totalField.setValue(total);
    }

    public void setCantidadArticulos(int cantidad) {
        cantidadArticulosField.setValue((double) cantidad);
    }
}
