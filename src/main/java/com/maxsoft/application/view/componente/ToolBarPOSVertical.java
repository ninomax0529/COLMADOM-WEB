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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.Key;

public class ToolBarPOSVertical extends VerticalLayout {

    public Button btnNuevo = new Button("Nuevo (F1)", VaadinIcon.FILE_ADD.create());
    public Button btnGuardar = new Button("Guardar (F2)", VaadinIcon.CHECK.create());
    public Button btnEliminar = new Button("Eliminar (F3)", VaadinIcon.TRASH.create());
    public Button btnBuscar = new Button("Buscar (F4)", VaadinIcon.SEARCH.create());
    public Button btnImprimir = new Button("Imprimir (F5)", VaadinIcon.PRINT.create());
    public Button btnSalir = new Button("Salir (F12)", VaadinIcon.EXIT.create());

    public ToolBarPOSVertical() {
        setWidth("180px");
        setPadding(true);
        setSpacing(false);
        setAlignItems(Alignment.STRETCH);
        getStyle().set("border-right", "1px solid #ccc");
        getStyle().set("padding", "1rem");
        getStyle().set("background-color", "#ffffff");

        // Estilos y tooltips
        styleButton(btnNuevo, "#f0f0f0", null, "Nuevo");
        styleButton(btnGuardar, "#28a745", "white", "Guardar");
        styleButton(btnEliminar, "#dc3545", "white", "Eliminar");
        styleButton(btnBuscar, "#17a2b8", "white", "Buscar");
        styleButton(btnImprimir, "#ffc107", "black", "Imprimir");
        styleButton(btnSalir, "#6c757d", "white", "Salir");

        add(btnNuevo, btnGuardar, btnEliminar, btnBuscar, btnImprimir, btnSalir);

        // Atajos de teclado (teclas de función)
        Shortcuts.addShortcutListener(this, btnNuevo::click, Key.F1);
        Shortcuts.addShortcutListener(this, btnGuardar::click, Key.F2);
        Shortcuts.addShortcutListener(this, btnEliminar::click, Key.F3);
        Shortcuts.addShortcutListener(this, btnBuscar::click, Key.F4);
        Shortcuts.addShortcutListener(this, btnImprimir::click, Key.F5);
        Shortcuts.addShortcutListener(this, btnSalir::click, Key.F12);
    }

    private void styleButton(Button button, String backgroundColor, String textColor, String tooltip) {
        button.setWidthFull();
        button.getStyle().set("background-color", backgroundColor);
        if (textColor != null) {
            button.getStyle().set("color", textColor);
        }
        button.getElement().setAttribute("title", tooltip);
    }
}
