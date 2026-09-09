/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PanelTicketContenido extends VerticalLayout {

    public PanelTicketContenido(
            TicketVenta ticket,
            BiConsumer<TicketVenta, Span> onRenombrarTicket,
            Consumer<TicketVenta> onCancelarTicket,
            Consumer<TicketVenta> onCobrar,
            Consumer<TicketVenta> onFiar
    ) {
        setSpacing(true);
        setPadding(false);
        setHeightFull();

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        Span ticketNameSpan = new Span(ticket.getId());
        ticketNameSpan.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-header-text-color)");

        Button renameBtn = new Button("Cambiar Nombre [Alt+R]", VaadinIcon.EDIT.create());
        renameBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        renameBtn.addClickListener(e -> {
            if (onRenombrarTicket != null) {
                onRenombrarTicket.accept(ticket, ticketNameSpan);
            }
        });

        topBar.add(ticketNameSpan, renameBtn);

        TextField filterText = new TextField();
        filterText.setPlaceholder("Filtrar productos en este ticket...");
        filterText.setPrefixComponent(VaadinIcon.SEARCH.create());
        filterText.setClearButtonVisible(true);
        filterText.setWidthFull();
        filterText.setValueChangeMode(ValueChangeMode.LAZY);

        filterText.addValueChangeListener(e -> {
            String searchTerm = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            ticket.getDataProvider().setFilter(item
                    -> item.getArticulo().getDescripcion().toLowerCase().contains(searchTerm)
            );
        });

        VerticalLayout gridContainer = new VerticalLayout(ticket.getGrid());
        gridContainer.setPadding(false);
        gridContainer.setSpacing(false);
        gridContainer.setSizeFull();
        setFlexGrow(1, gridContainer);

        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span totalLabel = new Span("TOTAL:");
        totalLabel.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "bold");

        totalLayout.add(totalLabel, ticket.getTotalSpan());

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setWidthFull();

        Button cancelTicketBtn = new Button("Cancelar [Alt+C]", VaadinIcon.CLOSE.create());
        cancelTicketBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        cancelTicketBtn.addClickListener(e -> {
            if (onCancelarTicket != null) {
                onCancelarTicket.accept(ticket);
            }
        });
        cancelTicketBtn.addClickShortcut(Key.KEY_C, KeyModifier.ALT);

        Button checkoutBtn = new Button("COBRAR [F2]", VaadinIcon.CASH.create());
        checkoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        checkoutBtn.addClickListener(e -> {
            if (onCobrar != null) {
                onCobrar.accept(ticket);
            }
        });
        checkoutBtn.addClickShortcut(Key.F2);

        Button creditBtn = new Button("FIAR [F3]", VaadinIcon.BOOK.create());
        creditBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        creditBtn.addClickListener(e -> {
//          abrirDialogoCobroCliente(ticket);
            if (onFiar != null) {
                onFiar.accept(ticket);
            }
        });
        creditBtn.addClickShortcut(Key.F3);

        actionButtons.setFlexGrow(1, cancelTicketBtn, checkoutBtn, creditBtn);
        actionButtons.add(cancelTicketBtn, checkoutBtn, creditBtn);

        add(topBar, filterText, gridContainer, totalLayout, actionButtons);
    }
}
