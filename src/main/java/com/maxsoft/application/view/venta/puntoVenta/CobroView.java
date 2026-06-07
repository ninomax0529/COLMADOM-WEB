/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

/**
 *
 * @author maximilianoalmonte
 */


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Route("cobro")
@Menu(order = 5, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class CobroView extends VerticalLayout {

    private NumberField totalPagar = new NumberField("Total a Pagar");
    private NumberField montoRecibido = new NumberField("Monto Recibido");
    private NumberField devuelta = new NumberField("Devuelta");

    public CobroView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        // TITULO
        H2 titulo = new H2("💳 Cobro de Venta");

        // TOTAL
        totalPagar.setValue(0.0);
        totalPagar.setReadOnly(true);
        totalPagar.setWidth("300px");

        montoRecibido.setWidth("300px");
        devuelta.setWidth("300px");
        devuelta.setReadOnly(true);

        montoRecibido.addValueChangeListener(e -> calcularDevuelta());

        // METODOS DE PAGO
        H3 metodoLabel = new H3("Método de Pago");

        Button btnEfectivo = new Button("💵 Efectivo");
        Button btnTarjeta = new Button("💳 Tarjeta");
        Button btnTransferencia = new Button("🏦 Transferencia");
        Button btnMixto = new Button("🔀 Mixto");

        btnEfectivo.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnTarjeta.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnTransferencia.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnMixto.addThemeVariants(ButtonVariant.LUMO_WARNING);

        btnEfectivo.setWidth("150px");
        btnTarjeta.setWidth("150px");
        btnTransferencia.setWidth("150px");
        btnMixto.setWidth("150px");

        HorizontalLayout metodosPago = new HorizontalLayout(
                btnEfectivo, btnTarjeta, btnTransferencia, btnMixto
        );

        metodosPago.setSpacing(true);
        metodosPago.setJustifyContentMode(JustifyContentMode.CENTER);

        // BOTON CONFIRMAR
        Button btnCobrar = new Button("✅ Confirmar Cobro");
        btnCobrar.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnCobrar.setWidth("300px");

        
        // CARD VISUAL
        Div card = new Div();
      card.getStyle()
                .set("background", "#f8fafc")
                .set("border-radius", "15px")
                .set("box-shadow", "0 10px 25px rgba(0,0,0,0.15)");

        VerticalLayout contenido = new VerticalLayout(
                titulo,
                totalPagar,
                montoRecibido,
                devuelta,
                metodoLabel,
                metodosPago,
                btnCobrar
        );
        

        contenido.setAlignItems(Alignment.CENTER);
        contenido.setSpacing(true);

        card.add(contenido);

        add(card);
    }

    private void calcularDevuelta() {
        if (montoRecibido.getValue() != null && totalPagar.getValue() != null) {
            double dev = montoRecibido.getValue() - totalPagar.getValue();
            devuelta.setValue(dev);
        }
    }
}
