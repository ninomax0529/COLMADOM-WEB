/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.articulo;

/**
 *
 * @author maximilianoalmonte
 */
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;


@Route(value = "inventario/popuArticulo1")
public class PopuView extends VerticalLayout {
    
    public PopuView() {
        // Crear el diálogo (ventana emergente)
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Formulario de Usuario");
        
        // Contenido del diálogo
        TextField nameField = new TextField("Nombre");
        TextField emailField = new TextField("Correo Electrónico");
        
        // Contenedor para los campos
        VerticalLayout dialogLayout = new VerticalLayout(nameField, emailField);
        dialog.add(dialogLayout);
        
        // Botón para cerrar el diálogo
        Button closeButton = new Button("Cerrar", event -> dialog.close());
        
        // Botón para enviar el formulario
        Button submitButton = new Button("Enviar", event -> {
            String name = nameField.getValue();
            String email = emailField.getValue();
            
            if (name.isEmpty() || email.isEmpty()) {
                Notification.show("Todos los campos son obligatorios", 3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Datos enviados: " + name + " - " + email, 3000, Notification.Position.MIDDLE);
                dialog.close();
            }
        });
        
        // Agregar botones al diálogo
        dialog.getFooter().add(closeButton, submitButton);
        
        // Botón para abrir el diálogo
        Button openButton = new Button("Mostrar ventana", event -> dialog.open());
        
        // Agregar el botón a la vista
        add(openButton);
    }
}
