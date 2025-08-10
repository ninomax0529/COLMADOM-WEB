/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 *
 * @author maximilianoalmonte
 */

//@CssImport("./styles/views/cobro-view.css") // si vas a usar CSS externo
@Route("cobro-resumen")
public class CobroView extends Dialog {

    private final double total;
    private final TextField montoRecibido;
    private final H3 cambioLabel;

    public CobroView(double total) {
        this.total = total;

        setHeaderTitle("Resumen de Cobros");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.START);

        H3 totalLabel = new H3("Total: $" + String.format("%.2f", total));
        totalLabel.getStyle().set("font-size", "20px").set("font-weight", "bold");

        montoRecibido = new TextField("Monto recibido");
        montoRecibido.setPlaceholder("0.00");
        montoRecibido.setPrefixComponent(new Icon(VaadinIcon.MONEY));
        montoRecibido.setWidth("200px");
        montoRecibido.getStyle()
            .set("background-color", "#1565c0")
            .set("color", "white")
            .set("border-radius", "8px");

        cambioLabel = new H3("Cambio: $0.00");
        cambioLabel.getStyle().set("font-size", "16px").set("margin-top", "10px");

        Button cobrarBtn = new Button("Cobrar", VaadinIcon.CHECK.create());
        cobrarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cobrarBtn.addClickListener(e -> procesarCobro());

        layout.add(totalLabel, montoRecibido, cobrarBtn, cambioLabel);
        add(layout);

        Button cerrar = new Button("Cancelar", e -> close());
        getFooter().add(cerrar);
    }

    private void procesarCobro() {
        try {
            double recibido = Double.parseDouble(montoRecibido.getValue());
            if (recibido < total) {
                Notification.show("Monto insuficiente.", 3000, Notification.Position.MIDDLE);
                return;
            }

            double cambio = recibido - total;
            cambioLabel.setText("Cambio: $" + String.format("%.2f", cambio));

            Notification.show("¡Cobro realizado con éxito!", 3000, Notification.Position.TOP_CENTER);
            close();

        } catch (NumberFormatException e) {
            Notification.show("Ingrese un monto válido.", 3000, Notification.Position.MIDDLE);
        }
    }
}
