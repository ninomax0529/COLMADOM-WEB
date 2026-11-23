package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.BiConsumer;

/**
 * Configura el grid de líneas de un ticket del POS: columnas (artículo, unidad,
 * cantidad, precio, total, acciones), estado vacío, doble clic y Supr.
 */
public final class GridTicketPos {

    private GridTicketPos() {
    }

    /**
     * @param alEditarCantidad botón editar y doble clic en una línea
     * @param alEliminar       botón quitar y tecla Supr sobre la línea seleccionada
     */
    public static void configurar(TicketVenta ticket,
            BiConsumer<TicketVenta, DetalleFacturaDeVenta> alEditarCantidad,
            BiConsumer<TicketVenta, DetalleFacturaDeVenta> alEliminar) {

        // DecimalFormat no es thread-safe: cada grid tiene los suyos
        DecimalFormat monedaFormat = new DecimalFormat("#,##0.00");
        DecimalFormat cantidadFormat = new DecimalFormat("#,##0.####");

        Grid<DetalleFacturaDeVenta> grid = ticket.getGrid();
        grid.setDataProvider(ticket.getDataProvider());
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setEmptyStateComponent(crearEstadoVacio());

        grid.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo)
                .setHeader("Artículo")
                .setFlexGrow(3)
                .setTooltipGenerator(DetalleFacturaDeVenta::getDescripcionArticulo);

        // MULTI-EMPAQUE: muestra la presentación vendida en cada línea
        grid.addColumn(DetalleFacturaDeVenta::getNombreUnidad)
                .setHeader("Unidad")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Cantidad sin ceros sobrantes (1 en vez de 1.0000; 0.5 en venta por monto o por libra)
        grid.addColumn(item -> cantidadFormat.format(nz(item.getCantidad())))
                .setHeader("Cant.")
                .setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(item -> "RD$ " + monedaFormat.format(nz(item.getPrecioVenta())))
                .setHeader("Precio")
                .setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(item -> "RD$ " + monedaFormat.format(nz(item.getTotal())))
                .setHeader("Total")
                .setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setPartNameGenerator(item -> "pos-total-linea");

        grid.addComponentColumn(item -> {
            Button editar = new Button(VaadinIcon.EDIT.create(), e -> alEditarCantidad.accept(ticket, item));
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editar.setTooltipText("Editar cantidad (*)");

            Button quitar = new Button(VaadinIcon.TRASH.create(), e -> alEliminar.accept(ticket, item));
            quitar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            quitar.setTooltipText("Quitar de la venta (Supr)");

            HorizontalLayout acciones = new HorizontalLayout(editar, quitar);
            acciones.setSpacing(false);
            return acciones;
        }).setHeader("")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addItemDoubleClickListener(e -> alEditarCantidad.accept(ticket, e.getItem()));

        // Supr quita la línea seleccionada. El atajo "*" NO se registra aquí: ya
        // existe a nivel de vista y registrarlo dos veces abría dos diálogos.
        Shortcuts.addShortcutListener(grid, () -> {
            DetalleFacturaDeVenta seleccionado = grid.asSingleSelect().getValue();
            if (seleccionado != null) {
                alEliminar.accept(ticket, seleccionado);
            }
        }, Key.DELETE);

        grid.addCellFocusListener(e -> e.getItem().ifPresent(grid::select));
        grid.setHeightFull();
    }

    /**
     * Contenido del grid cuando la venta no tiene productos: carrito, mensaje y
     * ayuda, centrados en el área del grid.
     */
    private static VerticalLayout crearEstadoVacio() {
        Icon icono = VaadinIcon.CART_O.create();
        icono.setSize("4rem");
        icono.getStyle().set("color", "var(--lumo-contrast-30pct)");

        Span titulo = new Span("Venta vacía");
        titulo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)");

        Span ayuda = new Span("Escanee o busque un producto [F4], o toque una tarjeta del catálogo.");
        ayuda.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("text-align", "center");

        VerticalLayout estadoVacio = new VerticalLayout(icono, titulo, ayuda);
        estadoVacio.setSpacing(false);
        estadoVacio.setWidthFull();
        estadoVacio.setAlignItems(FlexComponent.Alignment.CENTER);
        estadoVacio.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        // El área del grid ocupa aproximadamente la mitad de la pantalla
        estadoVacio.getStyle()
                .set("min-height", "45vh")
                .set("gap", "var(--lumo-space-s)");
        return estadoVacio;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
