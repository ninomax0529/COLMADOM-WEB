/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.dialogo;

/**
 *
 * @author Maximiliano
 */
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class DialogoVentaFiada extends Dialog {

    private ComboBox<String> comboCliente;
    private Button btnLocal;
    private Button btnDelivery;
    private Button btnConfirmar;
    private Button btnCancelar;

    // Variable para guardar la selección actual (Local o Delivery)
    private String tipoEntrega = "Local";

    public DialogoVentaFiada(String idVenta, double montoTotal) {
        // Configuración básica del diálogo (Ventana emergente)
        setHeaderTitle("Registrar Venta Fiada - " + idVenta);
        setWidth("450px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        // 1. Selector de Cliente (Escribir y buscar rápido)
        comboCliente = new ComboBox<>("Seleccionar Cliente (Fiado)");
        comboCliente.setPlaceholder("Escriba el nombre del cliente...");
        comboCliente.setItems("Juan Pérez", "María Rodríguez", "Pedro Martínez", "Casa de Doña Altagracia");
        comboCliente.setWidthFull();
        comboCliente.setClearButtonVisible(true);

        // 2. Botones de Tipo de Entrega (Estilo Colmado)
        btnLocal = new Button("1. Local", VaadinIcon.SHOP.create());
        btnLocal.setWidthFull();
        btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Seleccionado por defecto

        btnDelivery = new Button("2. Delivery", VaadinIcon.TRUCK.create());
        btnDelivery.setWidthFull();
        btnDelivery.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE); // Deseleccionado

        // Lógica visual para alternar entre Local y Delivery
        btnLocal.addClickListener(e -> seleccionarEntrega("Local"));
        btnDelivery.addClickListener(e -> seleccionarEntrega("Delivery"));

        HorizontalLayout layoutEntrega = new HorizontalLayout(btnLocal, btnDelivery);
        layoutEntrega.setWidthFull();
        layoutEntrega.setSpacing(true);

        // 3. Visualización del Total Grande y Llamativo
        Span txtEtiqueta = new Span("Total a Fiar: ");
        txtEtiqueta.getStyle().set("font-weight", "bold").set("font-size", "1.2rem");

        Span txtMonto = new Span(String.format("RD$ %.2f", montoTotal));
        txtMonto.getStyle()
                .set("color", "#d32f2f") // Rojo llamativo
                .set("font-weight", "bolder")
                .set("font-size", "1.6rem");

        HorizontalLayout layoutTotal = new HorizontalLayout(txtEtiqueta, txtMonto);
        layoutTotal.setAlignItems(HorizontalLayout.Alignment.BASELINE);
        layoutTotal.getStyle().set("margin-top", "15px").set("margin-bottom", "15px");

        // Cuerpo principal de la ventana
        VerticalLayout cuerpo = new VerticalLayout(comboCliente, layoutEntrega, layoutTotal);
        cuerpo.setPadding(false);
        add(cuerpo);

        // 4. Botones de Acción Inferiores (Footer)
        btnCancelar = new Button("Cancelar", e -> close());
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        btnConfirmar = new Button("Confirmar Fiado", VaadinIcon.CHECK.create());
        btnConfirmar.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        btnConfirmar.addClickListener(e -> ejecutarConfirmacion());

        getFooter().add(btnCancelar, btnConfirmar);

        // 5. CONFIGURACIÓN DE ATAJOS DE TECLADO (Para velocidad de colmado)
        // Alt + L -> Selecciona Local
// Esto hace lo mismo: si presionas Alt+L hace clic en el botón Local automáticamente
        btnLocal.addClickShortcut(Key.KEY_L, KeyModifier.ALT);
        btnDelivery.addClickShortcut(Key.KEY_D, KeyModifier.ALT);

        // Enter -> Ejecuta la acción de confirmar
        btnConfirmar.addClickShortcut(Key.ENTER);
    }

    private void seleccionarEntrega(String tipo) {
        this.tipoEntrega = tipo;
        if ("Local".equals(tipo)) {
            btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
    }

    private void ejecutarConfirmacion() {
        if (comboCliente.getValue() == null) {
            comboCliente.setInvalid(true);
            comboCliente.setErrorMessage("¡Debes elegir un cliente para fiar!");
            return;
        }

        // Aquí pones tu lógica para guardar en la base de datos
        System.out.println("Venta guardada para: " + comboCliente.getValue() + " Tipo: " + tipoEntrega);

        close(); // Cierra la ventana automáticamente al terminar
    }
}
