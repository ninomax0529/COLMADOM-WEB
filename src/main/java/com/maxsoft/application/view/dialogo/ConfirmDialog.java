/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.dialogo;

/**
 *
 * @author maximilianoalmonte
 */
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class ConfirmDialog extends Dialog {

    private final Button confirmButton;
    private final Button cancelButton;

    public ConfirmDialog(String message, Runnable onConfirm, Runnable onCancel) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        H3 messageLabel = new H3(message);

        confirmButton = new Button("SI", event -> {
            onConfirm.run();
            close();
        });

        cancelButton = new Button("NO", event -> {
            if (onCancel != null) {
                onCancel.run();
            }
            close();
        });

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        VerticalLayout content = new VerticalLayout(messageLabel, buttons);
        add(content);
    }
}
