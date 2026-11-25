/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Tarjeta del catálogo rápido: una por artículo. Si el artículo se vende en
 * varias presentaciones (Unidad, Caja x12, Fardo x24...) la tarjeta muestra un
 * selector; el precio y los botones actúan sobre la presentación elegida.
 */
public class TarjetaProductoCompo extends VerticalLayout {

    /**
     * Mientras todas las tarjetas usen la misma foto genérica, la imagen solo
     * ocupa espacio y obliga a descargarla. Poner en true cuando los artículos
     * tengan su propia foto.
     */
    private static final boolean MOSTRAR_IMAGEN = false;

    // DecimalFormat no es thread-safe: instancia propia, no estática compartida
    private final DecimalFormat FORMATO_PRECIO = new DecimalFormat("#,##0.00");

    /** Presentaciones del artículo, de menor a mayor factor. */
    private final List<ArticuloEmpaque> presentaciones;
    private ArticuloEmpaque seleccionada;

    /** Por debajo de esto (si el artículo no tiene mínimo) se avisa "poco". */
    private static final BigDecimal POCO_POR_DEFECTO = new BigDecimal("5");
    private static final String COLOR_POCO = "#c2410c";

    private final Span priceLabel = new Span();
    private final Span existenciaLabel = new Span();
    private final List<Button> botonesPresentacion = new ArrayList<>();

    /** Existencia actual (unidad base) por código de artículo. */
    private final Function<Integer, BigDecimal> existenciaDeArticulo;
    /** Mostrador frente a su mínimo y a los otros almacenes, por código (puede ser null). */
    private final Function<Integer, InventarioService.EstadoMostrador> estadoDeArticulo;

    public TarjetaProductoCompo(
            List<ArticuloEmpaque> presentacionesArticulo,
            String imageUrl,
            BiConsumer<ArticuloEmpaque, BigDecimal> onCantidadChanged,
            Consumer<ArticuloEmpaque> onVentaPorMontoRequested,
            Function<Integer, BigDecimal> existenciaDeArticulo,
            Function<Integer, InventarioService.EstadoMostrador> estadoDeArticulo
    ) {
        this.existenciaDeArticulo = existenciaDeArticulo;
        this.estadoDeArticulo = estadoDeArticulo;
        this.presentaciones = new ArrayList<>(presentacionesArticulo);
        this.presentaciones.sort(Comparator.comparing(TarjetaProductoCompo::factorDe));
        // Por defecto la presentación más pequeña (normalmente la unidad)
        this.seleccionada = presentaciones.get(0);

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
        // ZONA DE INFORMACIÓN (tocarla agrega 1 de la presentación elegida)
        // =========================================================
        Div info = new Div();
        info.setWidthFull();
        info.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("cursor", "pointer");

        String nombre = seleccionada.getNombreArticulo() != null ? seleccionada.getNombreArticulo() : "";

        // Imagen del producto (opcional)
        if (MOSTRAR_IMAGEN && imageUrl != null && !imageUrl.isBlank()) {
            Image img = new Image(imageUrl, nombre);
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

        priceLabel.getStyle()
                .set("color", "var(--lumo-body-text-color)")
                .set("font-weight", "600")
                .set("font-size", "0.95rem")
                .set("margin-top", "0.25rem");

        info.add(nameLabel);

        // Presentación: una sola se muestra como etiqueta; varias, como
        // selector (los botones no agregan al ticket, solo cambian la elegida).
        if (presentaciones.size() == 1) {
            Span presentacionLabel = new Span(textoPresentacion(seleccionada));
            presentacionLabel.getElement().getThemeList().add("badge contrast");
            presentacionLabel.getStyle()
                    .set("font-size", "0.7rem")
                    .set("margin-top", "0.25rem");
            info.add(presentacionLabel);
        } else {
            info.add(crearSelectorPresentacion());
        }

        existenciaLabel.getStyle()
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("margin-top", "0.15rem");

        info.add(priceLabel, existenciaLabel);
        actualizarSeleccion();

        info.addClickListener(e -> {
            if (onCantidadChanged != null) {
                onCantidadChanged.accept(seleccionada, BigDecimal.ONE);
            }
        });

        // =========================================================
        // BOTONES DE ACCIÓN (anclados al fondo de la tarjeta)
        // =========================================================
        HorizontalLayout controls = new HorizontalLayout();
        controls.setWidthFull();
        controls.setPadding(false);
        controls.setSpacing(true);
        controls.getStyle().set("margin-top", "auto");

        // Nota: el botón "−" se quitó de la tarjeta. Estaba pegado al "+" y se
        // tocaba por error; para quitar o bajar cantidad se usa el ticket
        // (editar cantidad o eliminar la línea).

        // Venta por monto: "dame 50 pesos de queso"
        Button pesoBtn = new Button("RD$", e -> {
            if (onVentaPorMontoRequested != null) {
                onVentaPorMontoRequested.accept(seleccionada);
            }
        });
        pesoBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        pesoBtn.setTooltipText("Vender por monto: cuántos pesos quiere el cliente");
        pesoBtn.getElement().setAttribute("aria-label", "Vender por monto en pesos");
        pesoBtn.getStyle().set("font-weight", "700");

        // Botón Sumar (+)
        Button plusBtn = new Button(VaadinIcon.PLUS.create(), e -> {
            if (onCantidadChanged != null) {
                onCantidadChanged.accept(seleccionada, BigDecimal.valueOf(1.00));
            }
        });
        plusBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        plusBtn.setTooltipText("Agregar 1");
        plusBtn.getElement().setAttribute("aria-label", "Agregar una unidad");

        controls.add(pesoBtn, plusBtn);
        // Cada botón ocupa la mitad: blancos más grandes para el dedo
        controls.setFlexGrow(1, pesoBtn, plusBtn);
        pesoBtn.getStyle().set("min-width", "0");
        plusBtn.getStyle().set("min-width", "0");

        // Espacio flexible entre la información y los botones
        info.getStyle().set("margin-bottom", "0.5rem");

        add(info, controls);
    }

    /**
     * Botones pequeños "Unidad | Caja x12": el elegido queda resaltado. El
     * clic no llega a la zona de información (no agrega al ticket).
     */
    private Div crearSelectorPresentacion() {

        // Una sola fila compacta: [UND] [PAQ×5] [CAJA×10]. Antes cada botón
        // ocupaba su propia línea y la tarjeta quedaba el doble de alta.
        Div selector = new Div();
        selector.setWidthFull();
        selector.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("justify-content", "center")
                .set("gap", "2px")
                .set("margin-top", "0.25rem");

        for (ArticuloEmpaque ae : presentaciones) {
            Button b = new Button(textoCorto(ae));
            b.addThemeVariants(ButtonVariant.LUMO_SMALL);
            b.getStyle()
                    .set("border-radius", "6px")
                    .set("margin", "0")
                    .set("font-size", "0.68rem")
                    .set("font-weight", "700")
                    .set("height", "1.6rem")
                    .set("min-width", "0")
                    .set("padding", "0 0.4em")
                    .set("white-space", "nowrap");
            b.setTooltipText(textoPresentacion(ae) + " — RD$ " + FORMATO_PRECIO.format(precioDe(ae)));
            // Que el clic no burbujee a la zona de información
            b.getElement().addEventListener("click", ev -> {
            }).stopPropagation();
            b.addClickListener(e -> {
                seleccionada = ae;
                actualizarSeleccion();
            });
            botonesPresentacion.add(b);
            selector.add(b);
        }
        return selector;
    }

    /** Refresca el precio, el botón resaltado y el borde según la elegida. */
    private void actualizarSeleccion() {

        priceLabel.setText("RD$ " + FORMATO_PRECIO.format(precioDe(seleccionada)));

        for (int i = 0; i < botonesPresentacion.size(); i++) {
            Button b = botonesPresentacion.get(i);
            if (presentaciones.get(i) == seleccionada) {
                b.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
        }

        // Caja, fardo, paquete...: borde azul para no confundirla con la unidad
        if (esEmpaqueMultiple(seleccionada)) {
            getStyle().set("border", "1px solid var(--lumo-primary-color-50pct)");
            getStyle().set("border-top", "4px solid var(--lumo-primary-color)");
        } else {
            getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
            getStyle().remove("border-top");
        }

        actualizarExistencia();
    }

    /**
     * Muestra cuánto queda en la presentación elegida: verde si hay, naranja si
     * queda poco (mínimo del artículo, o 5 si no tiene) y rojo "Agotado" sin
     * existencia (la tarjeta se ve apagada, pero se puede tocar: la alerta de
     * existencia decide). Los artículos no inventariables no muestran nada.
     */
    public void actualizarExistencia() {

        Articulo articulo = seleccionada.getArticulo();
        if (articulo == null || !articulo.getInventariable()) {
            existenciaLabel.setVisible(false);
            getStyle().remove("opacity");
            return;
        }

        BigDecimal base = existenciaDeArticulo != null ? existenciaDeArticulo.apply(articulo.getCodigo()) : null;
        if (base == null) {
            base = articulo.getExistencia() != null ? articulo.getExistencia() : BigDecimal.ZERO;
        }

        BigDecimal factor = factorDe(seleccionada);
        BigDecimal enPresentacion = factor.signum() > 0
                ? base.divide(factor, 2, RoundingMode.DOWN)
                : base;
        // Cajas, fardos...: solo enteras; la unidad puede tener decimales (libras)
        if (esEmpaqueMultiple(seleccionada)) {
            enPresentacion = enPresentacion.setScale(0, RoundingMode.DOWN);
        }

        BigDecimal minimo = articulo.getMinimo() != null && articulo.getMinimo().signum() > 0
                ? articulo.getMinimo()
                : POCO_POR_DEFECTO;

        // Mostrador en su mínimo (o agotado) y hay en otro almacén: se dice
        // cuánto hay atrás, para que lo traigan con un traslado
        InventarioService.EstadoMostrador estado = estadoDeArticulo != null
                ? estadoDeArticulo.apply(articulo.getCodigo()) : null;
        String atras = "";
        if (estado != null && estado.tocaTrasladar()) {
            BigDecimal otros = enPresentacion(estado.enOtrosAlmacenes(), factor);
            atras = " · " + (otros.signum() > 0 ? otros.stripTrailingZeros().toPlainString() : "hay")
                    + " en almacén";
            existenciaLabel.getElement().setAttribute("title",
                    "Hay en otro almacén: haga un traslado al mostrador");
        } else {
            existenciaLabel.getElement().removeAttribute("title");
        }

        existenciaLabel.setVisible(true);
        if (enPresentacion.signum() <= 0) {
            existenciaLabel.setText("Agotado" + atras);
            existenciaLabel.getStyle().set("color", atras.isEmpty()
                    ? "var(--lumo-error-text-color)"
                    : COLOR_POCO);
            getStyle().set("opacity", "0.6");
        } else {
            String cantidad = enPresentacion.stripTrailingZeros().toPlainString();
            existenciaLabel.setText("Quedan " + cantidad + atras);
            existenciaLabel.getStyle().set("color", base.compareTo(minimo) <= 0 || !atras.isEmpty()
                    ? COLOR_POCO
                    : "var(--lumo-success-text-color)");
            getStyle().remove("opacity");
        }
    }

    /** Unidades base llevadas a la presentación elegida (enteras si es caja, fardo...). */
    private BigDecimal enPresentacion(BigDecimal base, BigDecimal factor) {
        BigDecimal valor = factor.signum() > 0 ? base.divide(factor, 2, RoundingMode.DOWN) : base;
        return esEmpaqueMultiple(seleccionada) ? valor.setScale(0, RoundingMode.DOWN) : valor;
    }

    private static BigDecimal factorDe(ArticuloEmpaque ae) {
        return ae.getFactorConversion() != null ? ae.getFactorConversion() : BigDecimal.ONE;
    }

    private static BigDecimal precioDe(ArticuloEmpaque ae) {
        return ae.getPrecioVenta() != null ? ae.getPrecioVenta() : BigDecimal.ZERO;
    }

    /**
     * true si la presentación agrupa varias unidades base (factor mayor que 1).
     */
    private static boolean esEmpaqueMultiple(ArticuloEmpaque ae) {
        return factorDe(ae).compareTo(BigDecimal.ONE) > 0;
    }

    /**
     * Texto de la presentación: "Unidad", "Caja x12", etc.
     */
    private static String textoPresentacion(ArticuloEmpaque ae) {

        String unidad = (ae.getUnidadEmpaque() != null && ae.getUnidadEmpaque().getDescripcion() != null)
                ? ae.getUnidadEmpaque().getDescripcion()
                : "Unidad";

        BigDecimal factor = ae.getFactorConversion();
        if (factor != null && factor.compareTo(BigDecimal.ONE) != 0) {
            return unidad + " x" + factor.stripTrailingZeros().toPlainString();
        }
        return unidad;
    }

    /**
     * Texto corto para el selector: "UND", "PAQ×5", "CAJA×10". El nombre
     * completo y el precio quedan en el tooltip.
     */
    private static String textoCorto(ArticuloEmpaque ae) {
        // La misma abreviatura que la columna "Unidad" del ticket
        return com.maxsoft.application.util.LineaPosUtil.presentacionCorta(ae);
    }

    /** Presentación elegida en este momento. */
    public ArticuloEmpaque getArticulo() {
        return seleccionada;
    }
}
