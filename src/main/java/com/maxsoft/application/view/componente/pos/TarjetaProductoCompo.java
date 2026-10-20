///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.maxsoft.application.view.componente.pos;
//
//import com.maxsoft.application.modelo.ArticuloEmpaque;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.html.Image;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import java.math.BigDecimal;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//
//public class TarjetaProductoCompo extends VerticalLayout {
//
//    private final ArticuloEmpaque articulo;
//
//    public TarjetaProductoCompo(
//            ArticuloEmpaque articulo,
//            String imageUrl,
//            BiConsumer<ArticuloEmpaque, BigDecimal> onCantidadChanged,
//            Consumer<ArticuloEmpaque> onVentaPorMontoRequested
//    ) {
//        this.articulo = articulo;
//
//        // Diseños y estilos de la tarjeta
//        getStyle().set("background-color", "var(--lumo-base-color)");
//        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
//        getStyle().set("border-radius", "8px");
//        getStyle().set("padding", "0.75rem");
//        setAlignItems(Alignment.CENTER);
//        setSpacing(false);
//
//        // Imagen del producto
//        Image img = new Image(imageUrl, articulo.getNombreArticulo());
//        img.setWidth("100px");
//        img.setHeight("80px");
//        img.getStyle()
//                .set("object-fit", "cover")
//                .set("border-radius", "6px")
//                .set("margin-bottom", "0.5rem");
//
//        // Título/Descripción
//        Span nameLabel = new Span(articulo.getNombreArticulo());
//        nameLabel.getStyle()
//                .set("font-weight", "650")
//                .set("font-size", "0.9rem")
//                .set("text-align", "center");
//
//        // Precio en RD$
//        Span priceLabel = new Span("RD$ " + articulo.getPrecioVenta());
//        priceLabel.getStyle()
//                .set("color", "var(--lumo-secondary-text-color)")
//                .set("font-size", "0.8rem")
//                .set("margin-bottom", "0.5rem");
//
//        // Botones de acción
//        HorizontalLayout controls = new HorizontalLayout();
//        controls.setSpacing(true);
//        controls.setPadding(false);
//
//        // Botón Restar (-)
//        Button minusBtn = new Button(VaadinIcon.MINUS.create(), e -> {
//            if (onCantidadChanged != null) {
//                onCantidadChanged.accept(articulo, BigDecimal.valueOf(-1.00));
//            }
//        });
//        minusBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
//
//        // Botón Venta por pesos ($)
//        Button pesoBtn = new Button("$", e -> {
//            if (onVentaPorMontoRequested != null) {
//                onVentaPorMontoRequested.accept(articulo);
//            }
//        });
//        pesoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
//        pesoBtn.setTooltipText("Vender por monto en dinero (RD$)");
//
//        // Botón Sumar (+)
//        Button plusBtn = new Button(VaadinIcon.PLUS.create(), e -> {
//            if (onCantidadChanged != null) {
//                onCantidadChanged.accept(articulo,BigDecimal.valueOf(1.00));
//            }
//        });
//        plusBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
//
//        controls.add(minusBtn, pesoBtn, plusBtn);
//        add(img, nameLabel, priceLabel, controls);
//    }
//
//    public ArticuloEmpaque getArticulo() {
//        return articulo;
//    }
//}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TarjetaProductoCompo extends VerticalLayout {

    /**
     * Mientras todas las tarjetas usen la misma foto genérica, la imagen solo
     * ocupa espacio y obliga a descargarla. Poner en true cuando los artículos
     * tengan su propia foto.
     */
    private static final boolean MOSTRAR_IMAGEN = false;

    private static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("#,##0.00");

    private final ArticuloEmpaque articulo;

    public TarjetaProductoCompo(
            ArticuloEmpaque articulo,
            String imageUrl,
            BiConsumer<ArticuloEmpaque, BigDecimal> onCantidadChanged,
            Consumer<ArticuloEmpaque> onVentaPorMontoRequested
    ) {
        this.articulo = articulo;

        // Diseños y estilos de la tarjeta
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("border-radius", "8px");
        getStyle().set("padding", "0.75rem");
        getStyle().set("box-sizing", "border-box");
        setAlignItems(Alignment.CENTER);
        setSpacing(false);

        // Todas las tarjetas de una fila tienen la misma altura
        setHeight("100%");

        // =========================================================
        // ZONA DE INFORMACIÓN (tocarla agrega 1 unidad)
        // =========================================================
        Div info = new Div();
        info.setWidthFull();
        info.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("cursor", "pointer");

        // Imagen del producto (opcional)
        if (MOSTRAR_IMAGEN && imageUrl != null && !imageUrl.isBlank()) {
            Image img = new Image(imageUrl, articulo.getNombreArticulo());
            img.setWidth("100px");
            img.setHeight("80px");
            img.getElement().setAttribute("loading", "lazy");
            img.getStyle()
                    .set("object-fit", "cover")
                    .set("border-radius", "6px")
                    .set("margin-bottom", "0.5rem");
            info.add(img);
        }

        // Título/Descripción: máximo 2 líneas y altura fija para que los
        // precios y botones queden alineados entre tarjetas
        String nombre = articulo.getNombreArticulo() != null ? articulo.getNombreArticulo() : "";
        Span nameLabel = new Span(nombre);
        nameLabel.getElement().setAttribute("title", nombre);
        nameLabel.getStyle()
                .set("font-weight", "650")
                .set("font-size", "0.9rem")
                .set("text-align", "center")
                .set("line-height", "1.2")
                .set("min-height", "2.4em")
                .set("display", "-webkit-box")
                .set("-webkit-line-clamp", "2")
                .set("-webkit-box-orient", "vertical")
                .set("overflow", "hidden");

        // Presentación: así se distinguen los empaques del mismo artículo
        Span presentacionLabel = new Span(textoPresentacion(articulo));
        presentacionLabel.getElement().getThemeList().add("badge contrast");
        presentacionLabel.getStyle()
                .set("font-size", "0.7rem")
                .set("margin-top", "0.25rem");

        // Precio en RD$
        BigDecimal precio = articulo.getPrecioVenta() != null ? articulo.getPrecioVenta() : BigDecimal.ZERO;
        Span priceLabel = new Span("RD$ " + FORMATO_PRECIO.format(precio));
        priceLabel.getStyle()
                .set("color", "var(--lumo-body-text-color)")
                .set("font-weight", "600")
                .set("font-size", "0.95rem")
                .set("margin-top", "0.25rem");

        info.add(nameLabel, presentacionLabel, priceLabel);

        info.addClickListener(e -> {
            if (onCantidadChanged != null) {
                onCantidadChanged.accept(articulo, BigDecimal.ONE);
            }
        });

        // =========================================================
        // BOTONES DE ACCIÓN (anclados al fondo de la tarjeta)
        // =========================================================
        HorizontalLayout controls = new HorizontalLayout();
        controls.setWidthFull();
        controls.setPadding(false);
        controls.setSpacing(true);
        controls.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        controls.getStyle().set("margin-top", "auto");

        // Botón Restar (-)
        Button minusBtn = new Button(VaadinIcon.MINUS.create(), e -> {
            if (onCantidadChanged != null) {
                onCantidadChanged.accept(articulo, BigDecimal.valueOf(-1.00));
            }
        });
        minusBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        minusBtn.setTooltipText("Quitar 1");
        minusBtn.getElement().setAttribute("aria-label", "Quitar una unidad");

        // Botón Venta por pesos ($)
        Button pesoBtn = new Button("$", e -> {
            if (onVentaPorMontoRequested != null) {
                onVentaPorMontoRequested.accept(articulo);
            }
        });
        pesoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        pesoBtn.setTooltipText("Vender por monto en dinero (RD$)");
        pesoBtn.getElement().setAttribute("aria-label", "Vender por monto");

        // Botón Sumar (+)
        Button plusBtn = new Button(VaadinIcon.PLUS.create(), e -> {
            if (onCantidadChanged != null) {
                onCantidadChanged.accept(articulo, BigDecimal.valueOf(1.00));
            }
        });
        plusBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        plusBtn.setTooltipText("Agregar 1");
        plusBtn.getElement().setAttribute("aria-label", "Agregar una unidad");

        controls.add(minusBtn, pesoBtn, plusBtn);

        // Espacio flexible entre la información y los botones
        info.getStyle().set("margin-bottom", "0.5rem");

        add(info, controls);
    }

    /**
     * Texto de la presentación: "Unidad", "Caja x12", etc.
     */
    private String textoPresentacion(ArticuloEmpaque ae) {

        String unidad = (ae.getUnidadEmpaque() != null && ae.getUnidadEmpaque().getDescripcion() != null)
                ? ae.getUnidadEmpaque().getDescripcion()
                : "Unidad";

        BigDecimal factor = ae.getFactorConversion();
        if (factor != null && factor.compareTo(BigDecimal.ONE) != 0) {
            return unidad + " x" + factor.stripTrailingZeros().toPlainString();
        }
        return unidad;
    }

    public ArticuloEmpaque getArticulo() {
        return articulo;
    }
}
