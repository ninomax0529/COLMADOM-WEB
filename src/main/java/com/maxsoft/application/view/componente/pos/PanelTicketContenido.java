/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PanelTicketContenido extends VerticalLayout {

    /**
     * El filtro del ticket solo se muestra cuando la venta ya tiene varias
     * líneas; con pocas líneas solo quita espacio al grid.
     */
    private static final int LINEAS_PARA_MOSTRAR_FILTRO = 6;

    private static final DecimalFormat FORMATO_MONEDA = new DecimalFormat("#,##0.00");

    private final TicketVenta ticket;
    private final TextField filterText = new TextField();
    private final Span lblResumen = new Span();

    private int lineasPrevias = 0;

    public PanelTicketContenido(
            TicketVenta ticket,
            BiConsumer<TicketVenta, Span> onRenombrarTicket,
            Consumer<TicketVenta> onCancelarTicket,
            Consumer<TicketVenta> onCobrar,
            Consumer<TicketVenta> onFiar
    ) {
        this.ticket = ticket;

        setSpacing(true);
        setPadding(false);
        setHeightFull();

        // Nota: la barra superior con el nombre del ticket y el botón "Cambiar
        // Nombre" se eliminó. El nombre ya aparece en la pestaña y el botón
        // está en PanelCarritos (y Alt+R), así que solo dejaba una fila vacía.
        // El parámetro onRenombrarTicket se conserva para no cambiar el
        // constructor que usa la vista.
        // =========================================================
        // FILTRO (solo visible con varias líneas)
        // =========================================================
        filterText.setPlaceholder("Filtrar productos en este ticket...");
        filterText.setPrefixComponent(VaadinIcon.SEARCH.create());
        filterText.setClearButtonVisible(true);
        filterText.setWidthFull();
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.setVisible(false);

        filterText.addValueChangeListener(e -> {
            String searchTerm = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            ticket.getDataProvider().setFilter(item -> coincide(item, searchTerm));
        });

        // =========================================================
        // GRID
        // =========================================================
        VerticalLayout gridContainer = new VerticalLayout(ticket.getGrid());
        gridContainer.setPadding(false);
        gridContainer.setSpacing(false);
        gridContainer.setSizeFull();
        // El grid hace scroll dentro del panel en vez de empujar los botones
        gridContainer.getStyle().set("min-height", "0");
        setFlexGrow(1, gridContainer);

        // =========================================================
        // RESUMEN Y TOTAL
        // =========================================================
        lblResumen.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        totalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span totalLabel = new Span("TOTAL:");
        totalLabel.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "bold");

        totalLayout.add(totalLabel, ticket.getTotalSpan());

        // =========================================================
        // ACCIONES
        // =========================================================
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setWidthFull();

        Button cancelTicketBtn = new Button("Cancelar [Alt+C]", VaadinIcon.CLOSE.create());
        cancelTicketBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        cancelTicketBtn.addClickListener(e -> {
            if (onCancelarTicket != null) {
                onCancelarTicket.accept(ticket);
            }
        });
        // Alt+C se atiende desde la vista del POS (en el navegador, por tecla física).

        // Botones principales más grandes (uso táctil / mostrador)
        Button checkoutBtn = new Button("COBRAR [F2]", VaadinIcon.CASH.create());
        checkoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        checkoutBtn.addClickListener(e -> {
            if (onCobrar != null) {
                onCobrar.accept(ticket);
            }
        });
        checkoutBtn.addClickShortcut(Key.F2);

        Button creditBtn = new Button("FIAR [F3]", VaadinIcon.BOOK.create());
        creditBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        creditBtn.addClickListener(e -> {
//          abrirDialogoCobroCliente(ticket);
            if (onFiar != null) {
                onFiar.accept(ticket);
            }
        });
        creditBtn.addClickShortcut(Key.F3);

        // COBRAR, la acción más frecuente, es el botón más ancho
        actionButtons.setFlexGrow(1, cancelTicketBtn, creditBtn);
        actionButtons.setFlexGrow(2, checkoutBtn);
        actionButtons.add(cancelTicketBtn, checkoutBtn, creditBtn);

        add(filterText, gridContainer, lblResumen, totalLayout, actionButtons);

        // El resumen se recalcula cada vez que el ticket refresca sus datos
        // (agregar, editar o eliminar líneas).
        ticket.getDataProvider().addDataProviderListener(e -> refrescarResumen());

        refrescarResumen();
    }

    /**
     * Recalcula el resumen (líneas, subtotal, descuento, ITBIS), muestra u
     * oculta el filtro y baja el grid hasta la última línea agregada. Es
     * público por si la vista necesita forzar la actualización.
     */
    public void refrescarResumen() {

        List<DetalleFacturaDeVenta> items = ticket.getItems();

        int lineas = items.size();

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal descuento = BigDecimal.ZERO;
        BigDecimal itbis = BigDecimal.ZERO;

        for (DetalleFacturaDeVenta d : items) {
            subTotal = subTotal.add(valor(d.getSubTotal()));
            descuento = descuento.add(valor(d.getTotalDescuento()));
            itbis = itbis.add(valor(d.getTotalItbis()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(lineas).append(lineas == 1 ? " línea" : " líneas");
        sb.append("  ·  Subtotal RD$ ").append(FORMATO_MONEDA.format(subTotal));
        if (descuento.signum() > 0) {
            sb.append("  ·  Desc. RD$ ").append(FORMATO_MONEDA.format(descuento));
        }
        sb.append("  ·  ITBIS RD$ ").append(FORMATO_MONEDA.format(itbis));
        lblResumen.setText(sb.toString());

        // Filtro: solo con varias líneas, o mientras tenga texto escrito
        boolean mostrarFiltro = lineas > LINEAS_PARA_MOSTRAR_FILTRO
                || !filterText.isEmpty();
        filterText.setVisible(mostrarFiltro);

        // Al agregar una línea, el grid baja hasta ella
        if (lineas > lineasPrevias) {
            ticket.getGrid().scrollToEnd();
        }
        lineasPrevias = lineas;
    }

    private boolean coincide(DetalleFacturaDeVenta item, String term) {

        if (term.isEmpty()) {
            return true;
        }

        String descripcion = item.getDescripcionArticulo() != null
                ? item.getDescripcionArticulo()
                : (item.getArticulo() != null && item.getArticulo().getDescripcion() != null
                ? item.getArticulo().getDescripcion() : "");

        String unidad = item.getNombreUnidad() != null ? item.getNombreUnidad() : "";

        return descripcion.toLowerCase().contains(term)
                || unidad.toLowerCase().contains(term);
    }

    private BigDecimal valor(BigDecimal v) {

        return v == null ? BigDecimal.ZERO : v;
    }
}
