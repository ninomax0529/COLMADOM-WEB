/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario;

/**
 *
 * @author Maximiliano
 */
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class ModalDialog extends Dialog {
    private final TextField inputField;
    private String userInput;

    public ModalDialog(DialogCallback callback) {
        setModal(true); // Hace que el diálogo sea modal
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        inputField = new TextField("Ingrese su nombre");
        Button confirmButton = new Button("Aceptar", event -> {
            userInput = inputField.getValue();
            
            callback.onConfirm(userInput);
            close();
        });

        Button cancelButton = new Button("Cancelar", event -> close());

        VerticalLayout layout = new VerticalLayout(inputField, confirmButton, cancelButton);
        add(layout);
    }

    public interface DialogCallback {
        void onConfirm(String input);
    }
}
