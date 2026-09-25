/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Articulo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TarjetaProductoCompo extends VerticalLayout {

    private final Articulo articulo;

    public TarjetaProductoCompo(
            Articulo articulo,
            String imageUrl,
            BiConsumer<Articulo, BigDecimal> onCantidadChanged,
            Consumer<Articulo> onVentaPorMontoRequested
    ) {
        this.articulo = articulo;

        // Diseños y estilos de la tarjeta
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("border-radius", "8px");
        getStyle().set("padding", "0.75rem");
        setAlignItems(Alignment.CENTER);
        setSpacing(false);

        // Imagen del producto
        Image img = new Image(imageUrl, articulo.getDescripcion());
        img.setWidth("100px");
        img.setHeight("80px");
        img.getStyle()
                .set("object-fit", "cover")
                .set("border-radius", "6px")
                .set("margin-bottom", "0.5rem");

        // Título/Descripción
        Span nameLabel = new Span(articulo.getDescripcion());
        nameLabel.getStyle()
                .set("font-weight", "650")
                .set("font-size", "0.9rem")
                .set("text-align", "center");

        // Precio en RD$
        Span priceLabel = new Span("RD$ " + articulo.getPrecioVenta());
        priceLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.8rem")
                .set("margin-bottom", "0.5rem");

        // Botones de acción
        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);
        controls.setPadding(false);

        // Botón Restar (-)
        Button minusBtn = new Button(VaadinIcon.MINUS.create(), e -> {
            if (onCantidadChanged != null) {
                onCantidadChanged.accept(articulo, BigDecimal.valueOf(-1.00));
            }
        });
        minusBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        // Botón Venta por pesos ($)
        Button pesoBtn = new Button("$", e -> {
            if (onVentaPorMontoRequested != null) {
                onVentaPorMontoRequested.accept(articulo);
            }
        });
        pesoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        pesoBtn.setTooltipText("Vender por monto en dinero (RD$)");

        // Botón Sumar (+)
        Button plusBtn = new Button(VaadinIcon.PLUS.create(), e -> {
            if (onCantidadChanged != null) {
                onCantidadChanged.accept(articulo,BigDecimal.valueOf(1.00));
            }
        });
        plusBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        controls.add(minusBtn, pesoBtn, plusBtn);
        add(img, nameLabel, priceLabel, controls);
    }

    public Articulo getArticulo() {
        return articulo;
    }
}