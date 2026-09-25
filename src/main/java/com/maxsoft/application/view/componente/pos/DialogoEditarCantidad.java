/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DialogoEditarCantidad extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    @FunctionalInterface
    public interface AccionConfirmarEditarCantidad {
        void ejecutar(BigDecimal nuevaCantidad);
    }

    public DialogoEditarCantidad(
            TicketVenta ticket,
            DetalleFacturaDeVenta item,
            AccionConfirmarEditarCantidad onGuardar,
            Runnable onEliminar
    ) {
        setHeaderTitle("Editar Cantidad - " + item.getDescripcionArticulo());
        setWidth("350px");

        VerticalLayout layout = new VerticalLayout();

        BigDecimalField qtyField = new BigDecimalField("Nueva cantidad (Unidades / Libras)");
        qtyField.setValue(item.getCantidad());
        qtyField.getValue().add(BigDecimal.valueOf(0.25));
//        qtyField..setStep(0.25); // Permite incrementos de cuarto de libra (0.25)
        qtyField.setClearButtonVisible(true);
        qtyField.setWidthFull();

        Span subtotalSpan = new Span("Subtotal: RD$ " + MONEDA_FORMAT.format(item.getTotal()));
        subtotalSpan.getStyle().set("font-weight", "bold");

        qtyField.addValueChangeListener(e -> {
            BigDecimal val = e.getValue();
            
            if (val != null && val.doubleValue() > 0) {
                BigDecimal tempTotal = item.getPrecioVenta().multiply(val);
                item.setSubTotal(tempTotal);
                subtotalSpan.setText("Subtotal: RD$ " + MONEDA_FORMAT.format(tempTotal));
            } else {
                subtotalSpan.setText("Subtotal: RD$ 0.00");
            }
        });

        layout.add(qtyField, subtotalSpan);
        add(layout);

        Button cancelBtn = new Button("Cancelar", e -> close());
        Button saveBtn = new Button("Guardar [Enter]", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickShortcut(Key.ENTER);

        saveBtn.addClickListener(e -> {
            BigDecimal nuevaQty = qtyField.getValue();
            if (nuevaQty != null && nuevaQty.doubleValue() > 0) {
                item.setCantidad(nuevaQty);
                ticket.updateUI();
                close();
                if (onGuardar != null) {
                    onGuardar.ejecutar(nuevaQty);
                }
            } else if (nuevaQty != null && nuevaQty.doubleValue() <= 0) {
                close();
                if (onEliminar != null) {
                    onEliminar.run();
                }
            }
        });

        getFooter().add(cancelBtn, saveBtn);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                qtyField.focus();
                qtyField.getElement().executeJs("this.focus(); this.select();");
            }
        });
    }
}
