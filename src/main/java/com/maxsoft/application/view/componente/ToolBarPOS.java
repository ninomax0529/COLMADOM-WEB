/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

/**
 *
 * @author maximilianoalmonte
 */

import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;

import com.vaadin.flow.component.Key;

public class ToolBarPOS extends FlexLayout {

    public Button btnNuevo = new Button("Nuevo", VaadinIcon.FILE_ADD.create());
    public Button btnGuardar = new Button("Guardar", VaadinIcon.CHECK.create());
    public Button btnEliminar = new Button("Eliminar", VaadinIcon.TRASH.create());
    public Button btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create());
    public Button btnImprimir = new Button("Imprimir", VaadinIcon.PRINT.create());
    public Button btnSalir = new Button("Salir", VaadinIcon.EXIT.create());
    

    public ToolBarPOS() {
        
        setWidthFull();
       
        setFlexWrap(FlexWrap.WRAP);
        setJustifyContentMode(JustifyContentMode.START);
        getStyle().set("position", "sticky");
        getStyle().set("top", "0");
        getStyle().set("z-index", "100");
        getStyle().set("background", "#fff");
        getStyle().set("border-bottom", "1px solid #ccc");
        getStyle().set("padding", "0.5rem");

        // Estilos de botones
        btnNuevo.getStyle().set("background-color", "#f0f0f0");
        btnGuardar.getStyle().set("background-color", "#28a745").set("color", "white");
        btnEliminar.getStyle().set("background-color", "#dc3545").set("color", "white");
        btnBuscar.getStyle().set("background-color", "#17a2b8").set("color", "white");
        btnImprimir.getStyle().set("background-color", "#ffc107").set("color", "black");
        btnSalir.getStyle().set("background-color", "#6c757d").set("color", "white");

        add(btnNuevo, btnGuardar, btnEliminar, btnBuscar, btnImprimir, btnSalir);

        // Accesos rápidos con teclas de función
        Shortcuts.addShortcutListener(this, btnNuevo::click, Key.F1);
        Shortcuts.addShortcutListener(this, btnGuardar::click, Key.F2);
        Shortcuts.addShortcutListener(this, btnEliminar::click, Key.F3);
        Shortcuts.addShortcutListener(this, btnBuscar::click, Key.F4);
        Shortcuts.addShortcutListener(this, btnImprimir::click, Key.F5);
        Shortcuts.addShortcutListener(this, btnSalir::click, Key.F12);
    }
}
