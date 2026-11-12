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
        addClassName("dc-dialogo");
        setHeaderTitle("Movimiento de Caja");
        setWidth("500px");
        setMaxWidth("96vw");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setWidthFull();

        // Componentes del formulario
        ComboBox<String> tipoPosCombo = new ComboBox<>("Tipo de Operación");
        tipoPosCombo.setItems(
                CajaService.TARJETA_CONSUMO,
                CajaService.TARJETA_AVANCE,
                CajaService.TARJETA_ANULACION,
                CajaService.OPCION_FONDO_EXTRA
        );
        tipoPosCombo.setValue(CajaService.TARJETA_CONSUMO);
        tipoPosCombo.setAllowCustomValue(false);
        tipoPosCombo.setWidthFull();
        tipoPosCombo.setRequiredIndicatorVisible(true);

        BigDecimalField montoField = new BigDecimalField("Monto de la Transacción");
        montoField.setValue(BigDecimal.ZERO);
        montoField.setWidthFull();
        montoField.setRequiredIndicatorVisible(true);
        montoField.addClassName("dc-campo-grande");
        montoField.setPrefixComponent(new com.vaadin.flow.component.html.Span("RD$"));
        tipoPosCombo.addClassName("dc-combo");

        TextField descripcionField = new TextField("Descripción / Referencia del Voucher");
        descripcionField.setPlaceholder("Ej: Voucher #1234, Tarjeta BHD León");
        descripcionField.setWidthFull();

        // Banda superior: explica en grande qué hace la operación elegida
        com.vaadin.flow.component.html.Div bandaCont = new com.vaadin.flow.component.html.Div();
        Runnable actualizarBanda = () -> {
            String op = tipoPosCombo.getValue();
            bandaCont.removeAll();
            if (CajaService.OPCION_FONDO_EXTRA.equals(op)) {
                bandaCont.add(com.maxsoft.application.view.venta.cajaChica.DialogoCajaEstilo.banda(
                        VaadinIcon.PLUS_CIRCLE, "Entrada de efectivo", "El dinero ENTRA a la gaveta y se imprime un comprobante"));
            } else if (CajaService.TARJETA_AVANCE.equals(op)) {
                bandaCont.add(com.maxsoft.application.view.venta.cajaChica.DialogoCajaEstilo.banda(
                        VaadinIcon.EXCHANGE, "Avance de efectivo", "Se cobra a la tarjeta y el efectivo SALE de la gaveta"));
            } else if (CajaService.TARJETA_ANULACION.equals(op)) {
                bandaCont.add(com.maxsoft.application.view.venta.cajaChica.DialogoCajaEstilo.banda(
                        VaadinIcon.CLOSE_CIRCLE, "Anulación de voucher", "Resta del total cobrado con tarjeta; no toca la gaveta"));
            } else {
                bandaCont.add(com.maxsoft.application.view.venta.cajaChica.DialogoCajaEstilo.banda(
                        VaadinIcon.CREDIT_CARD, "Consumo con tarjeta", "Venta cobrada en el datáfono; no toca la gaveta"));
            }
        };
        actualizarBanda.run();

        // Para el fondo extra la descripción es el motivo / quién entrega el efectivo
        tipoPosCombo.addValueChangeListener(ev -> {
            boolean fondo = CajaService.OPCION_FONDO_EXTRA.equals(ev.getValue());
            montoField.setLabel(fondo ? "Efectivo que entra a la gaveta" : "Monto de la Transacción");
            descripcionField.setLabel(fondo ? "Motivo / Quién entrega el efectivo" : "Descripción / Referencia del Voucher");
            descripcionField.setPlaceholder(fondo ? "Ej: El dueño trae cambio para devoluciones" : "Ej: Voucher #1234, Tarjeta BHD León");
            actualizarBanda.run();
        });

        layout.addClassName("dc-cuerpo");
        layout.add(bandaCont, tipoPosCombo, montoField, descripcionField);
        add(layout);

        // Botones de acción del pie de página
        Button cancelBtn = new Button("Cancelar", e -> close());

        cancelBtn.addClassName("dc-btn-secundario");
        Button saveBtn = new Button("Registrar [Enter]", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClassName("dc-btn-abrir");
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
                if (CajaService.OPCION_FONDO_EXTRA.equals(tipoOperacion)) {
                    var mov = cajaService.registrarFondoExtra(monto, descripcion.trim(),
                            usuarioLogueado != null ? usuarioLogueado : "Administrador");
                    Notification.show("Fondo extra #" + mov.getId() + " registrado: entraron RD$ "
                            + String.format("%,.2f", monto) + " a la caja.", 4000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    imprimirComprobante(cajaService, mov.getId());
                    close();
                    return;
                }

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

    /**
     * Imprime el comprobante del fondo extra en la impresora del navegador
     * (iframe oculto, igual que el ticket de venta). Si falla, el movimiento ya
     * quedó guardado: solo se avisa.
     */
    private void imprimirComprobante(CajaService cajaService, Long movimientoId) {
        try {
            String html = cajaService.generarComprobanteFondoExtraHtml(movimientoId);
            com.maxsoft.application.util.ImpresionHtmlUtil.imprimir(
                    getUI().orElse(com.vaadin.flow.component.UI.getCurrent()), html);
        } catch (Exception ex) {
            Notification.show("El fondo extra #" + movimientoId + " se guardó, pero no se pudo imprimir el comprobante.",
                    4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // Sobrecarga del constructor para cuando no se pasa el usuario explícitamente
    public DialogoMovimientoPos(CajaService cajaService) {
        this(cajaService, "Administrador");
    }
}