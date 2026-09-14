/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

import java.math.BigDecimal;
import java.util.Optional;

public class DialogoMovimientoPos extends Dialog {

    public DialogoMovimientoPos(CajaService cajaService, String usuarioLogueado) {
        // 1. Validar primero si hay una caja abierta antes de mostrar el diálogo
        Optional<CajaTurno> cajaAbiertaOpt = cajaService.obtenerCajaAbierta();

        if (cajaAbiertaOpt.isEmpty()) {
            Notification.show("Debe abrir una caja antes de registrar movimientos de POS", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        CajaTurno turnoActual = cajaAbiertaOpt.get();

        // 2. Construcción de la ventana de diálogo
        setHeaderTitle("Registrar Movimiento POS / Tarjeta");
        setWidth("450px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setWidthFull();

        // Componentes del formulario
        ComboBox<String> tipoPosCombo = new ComboBox<>("Tipo de Operación POS");
        tipoPosCombo.setItems(
                "Consumo con Tarjeta / Datáfono",
                "Avance de Efectivo con Tarjeta",
                "Anulación de Voucher"
        );
        tipoPosCombo.setValue("Consumo con Tarjeta / Datáfono");
        tipoPosCombo.setWidthFull();
        tipoPosCombo.setRequiredIndicatorVisible(true);

        BigDecimalField montoField = new BigDecimalField("Monto de la Transacción (RD$)");
        montoField.setValue(BigDecimal.ZERO);
        montoField.setWidthFull();
        montoField.setRequiredIndicatorVisible(true);

        TextField descripcionField = new TextField("Descripción / Referencia del Voucher");
        descripcionField.setPlaceholder("Ej: Voucher #1234, Tarjeta BHD León");
        descripcionField.setWidthFull();

        layout.add(tipoPosCombo, montoField, descripcionField);
        add(layout);

        // Botones de acción del pie de página
        Button cancelBtn = new Button("Cancelar", e -> close());

        Button saveBtn = new Button("Registrar [Enter]", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        saveBtn.addClickShortcut(Key.ENTER);

        saveBtn.addClickListener(e -> {
            BigDecimal monto = montoField.getValue();
            String tipoOperacion = tipoPosCombo.getValue();
            String descripcion = descripcionField.getValue();

            // Validaciones básicas de entrada
            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show("Ingrese un monto válido mayor a cero", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                montoField.focus();
                return;
            }

            if (descripcion == null || descripcion.trim().isEmpty()) {
                Notification.show("Debe ingresar una descripción o número de voucher", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                descripcionField.focus();
                return;
            }

            try {
                // Llamada al servicio para guardar en la base de datos
                cajaService.registrarMovimientoPos(
                        turnoActual.getId().intValue(),
                        tipoOperacion,
                        monto,
                        descripcion.trim(),
                        usuarioLogueado != null ? usuarioLogueado : "Administrador"
                );

                Notification.show("¡Movimiento de POS registrado correctamente!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                close();

            } catch (IllegalStateException | IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 3500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Error inesperado al registrar el movimiento: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        getFooter().add(cancelBtn, saveBtn);

        // Enfocar automáticamente el campo del monto al abrir la ventana
        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                montoField.focus();
            }
        });

        // Abrir el diálogo automáticamente una vez configurado
        open();
    }

    // Sobrecarga del constructor para cuando no se pasa el usuario explícitamente
    public DialogoMovimientoPos(CajaService cajaService) {
        this(cajaService, "Administrador");
    }
}