/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

/**
 *
 * @author maximilianoalmonte
 */
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ToolBarBotonera extends HorizontalLayout {

    private final Button guardar;
    private final Button nuevo;
    private final Button cancelar;

    public ToolBarBotonera(boolean bn, boolean bg, boolean bc) {
        addClassName("buttons-toolbar");
        setWidthFull();
        setSpacing(true);
        setPadding(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        guardar = new Button("Guardar", new Icon(VaadinIcon.CHECK_CIRCLE_O));
        guardar.addClassName("guardar");
        guardar.setVisible(bg);

        nuevo = new Button("Nuevo", new Icon(VaadinIcon.PLUS));
        nuevo.addClassName("nuevo");
        nuevo.setVisible(bn);

        cancelar = new Button("Salir", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelar.addClassName("cancelar");
        cancelar.setVisible(bc);

        guardar.addClassName("guardar");
        nuevo.addClassName("nuevo");
        cancelar.addClassName("cancelar");

        add(guardar, nuevo, cancelar);
    }

    public Button getGuardar() {
        return guardar;
    }

    public Button getNuevo() {
        return nuevo;
    }

    public Button getCancelar() {
        return cancelar;
    }
}
