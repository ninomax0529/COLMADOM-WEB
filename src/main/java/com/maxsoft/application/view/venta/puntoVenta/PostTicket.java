/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

import com.vaadin.flow.router.Menu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@PageTitle("POS - Colmado")
@Route("postTicket")
@Menu(order = 5, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)

public class PostTicket extends HorizontalLayout {

    public static class CartItem {
        private String producto;
        private int cantidad;
        private BigDecimal precioUnitario;

        public CartItem(String producto, int cantidad, BigDecimal precioUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public String getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public BigDecimal getTotal() { return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); }
    }

    public static class TicketVenta {
        private String id;
        private final List<CartItem> items = new ArrayList<>();
        private final ListDataProvider<CartItem> dataProvider = new ListDataProvider<>(items);
        private final Grid<CartItem> grid = new Grid<>(CartItem.class, false);
        private final Span totalSpan = new Span("RD$ 0.00");

        public TicketVenta(String id) {
            this.id = id;
            totalSpan.getStyle()
                    .set("font-size", "1.8rem")
                    .set("font-weight", "bold")
                    .set("color", "var(--lumo-primary-color)");
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public List<CartItem> getItems() { return items; }
        public ListDataProvider<CartItem> getDataProvider() { return dataProvider; }
        public Grid<CartItem> getGrid() { return grid; }
        public Span getTotalSpan() { return totalSpan; }

        public BigDecimal getTotalAmount() {
            return items.stream().map(CartItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public void updateUI() {
            dataProvider.refreshAll();
            totalSpan.setText("RD$ " + getTotalAmount().toString());
        }
    }

    private final List<TicketVenta> ticketsAbiertos = new ArrayList<>();
    private final TabSheet ticketTabSheet = new TabSheet();
    private TicketVenta ticketAktivo;

    private final List<String> clientsList = List.of(
            "Juan Pérez (Vecino)", 
            "Maria Gómez", 
            "Pedro El Mecánico", 
            "Rosa Almonte", 
            "Carlos 'El Flaco'"
    );

    public PostTicket() {
        setSizeFull();
        setSpacing(true);

        crearNuevoTicket("Venta Principal");

        VerticalLayout leftPanel = createProductPanel();
        leftPanel.setWidth("55%");
        leftPanel.setHeightFull();

        VerticalLayout rightPanel = createMultiCartPanel();
        rightPanel.setWidth("45%");
        rightPanel.setHeightFull();

        add(leftPanel, rightPanel);
    }

    private void crearNuevoTicket(String nombre) {
        TicketVenta nuevoTicket = new TicketVenta(nombre);
        ticketsAbiertos.add(nuevoTicket);
        
        Grid<CartItem> grid = nuevoTicket.getGrid();
        grid.setDataProvider(nuevoTicket.getDataProvider());
        grid.addColumn(CartItem::getProducto).setHeader("Producto").setAutoWidth(true);
        grid.addColumn(CartItem::getCantidad).setHeader("Cant.");
        grid.addColumn(item -> "RD$ " + item.getPrecioUnitario()).setHeader("Precio");
        grid.addColumn(item -> "RD$ " + item.getTotal()).setHeader("Total");
        
        grid.addComponentColumn(item -> {
            Button removeBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                nuevoTicket.getItems().remove(item);
                nuevoTicket.updateUI();
            });
            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return removeBtn;
        }).setHeader("");
        grid.setHeight("250px");

        VerticalLayout contenidoTab = construirContenidoPanelTicket(nuevoTicket);
        
        ticketTabSheet.add(nombre, contenidoTab);
        ticketTabSheet.setSelectedTab(ticketTabSheet.getTab(contenidoTab));
        ticketAktivo = nuevoTicket;
    }

    private VerticalLayout construirContenidoPanelTicket(TicketVenta ticket) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        TextField filterText = new TextField();
        filterText.setPlaceholder("Filtrar productos...");
        filterText.setPrefixComponent(VaadinIcon.SEARCH.create());
        filterText.setClearButtonVisible(true);
        filterText.setWidthFull();
        filterText.setValueChangeMode(ValueChangeMode.LAZY); 
        filterText.addValueChangeListener(e -> {
            String searchTerm = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            ticket.getDataProvider().setFilter(item -> 
                item.getProducto().toLowerCase().contains(searchTerm)
            );
        });

        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        Span totalLabel = new Span("TOTAL:");
        totalLabel.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "bold");
                
        totalLayout.add(totalLabel, ticket.getTotalSpan());

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setWidthFull();

        Button cancelTicketBtn = new Button("Cancelar", VaadinIcon.CLOSE.create());
        cancelTicketBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        cancelTicketBtn.addClickListener(e -> confirmarEliminarTicket(ticket));

        Button checkoutBtn = new Button("COBRAR", VaadinIcon.CASH.create());
        checkoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        checkoutBtn.addClickListener(e -> openCashPaymentDialog(ticket));

        Button creditBtn = new Button("FIAR", VaadinIcon.BOOK.create());
        creditBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        creditBtn.addClickListener(e -> openClientSelectionDialog(ticket));

        actionButtons.setFlexGrow(1, cancelTicketBtn, checkoutBtn, creditBtn);
        actionButtons.add(cancelTicketBtn, checkoutBtn, creditBtn);

        layout.add(filterText, ticket.getGrid(), totalLayout, actionButtons);
        return layout;
    }

    private VerticalLayout createProductPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        H2 title = new H2("Colmado - Punto de Venta");

        ComboBox<String> searchBox = new ComboBox<>("Buscar producto o escanear código");
        searchBox.setItems("Arroz (lb)", "Habichuelas (lb)", "Aceite 16oz", "Plátano Verde", "Salami (lb)", "Cerveza Fría");
        searchBox.setWidthFull();
        searchBox.setClearButtonVisible(true);
        searchBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                agregarAlTicketActivo(e.getValue(), 1, new BigDecimal("75.00"));
                searchBox.clear();
            }
        });

        H3 quickTitle = new H3("Ventas Rápidas (Catálogo)");

        Div gridContainer = new Div();
        gridContainer.getStyle().set("display", "grid");
        gridContainer.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(150px, 1fr))");
        gridContainer.getStyle().set("gap", "1rem");
        gridContainer.setWidthFull();

        gridContainer.add(
            createProductCard("Plátano Verde", new BigDecimal("25.00"), "https://images.unsplash.com/photo-1528825871115-3581a5387919?w=300&auto=format&fit=crop&q=60"),
            createProductCard("Arroz (1 lb)", new BigDecimal("40.00"), "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=300&auto=format&fit=crop&q=60"),
            createProductCard("Cerveza Grande", new BigDecimal("180.00"), "https://images.unsplash.com/photo-1608270154045-24d9d1341cbf?w=300&auto=format&fit=crop&q=60"),
            createProductCard("Salami (lb)", new BigDecimal("140.00"), "https://images.unsplash.com/photo-1544025162-d76694265947?w=300&auto=format&fit=crop&q=60"),
            createProductCard("Aceite 16oz", new BigDecimal("95.00"), "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=300&auto=format&fit=crop&q=60"),
            createProductCard("Habichuelas (lb)", new BigDecimal("60.00"), "https://images.unsplash.com/photo-1551462147-3f005d5a6057?w=300&auto=format&fit=crop&q=60")
        );

        layout.add(title, searchBox, quickTitle, gridContainer);
        return layout;
    }

    private VerticalLayout createProductCard(String productName, BigDecimal price, String imageUrl) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("background-color", "var(--lumo-base-color)");
        card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("padding", "0.75rem");
        card.setAlignItems(Alignment.CENTER);
        card.setSpacing(false);

        Image img = new Image(imageUrl, productName);
        img.setWidth("100px");
        img.setHeight("80px");
        img.getStyle().set("object-fit", "cover").set("border-radius", "6px").set("margin-bottom", "0.5rem");

        Span nameLabel = new Span(productName);
        nameLabel.getStyle().set("font-weight", "650").set("font-size", "0.9rem").set("text-align", "center");

        Span priceLabel = new Span("RD$ " + price);
        priceLabel.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.8rem").set("margin-bottom", "0.5rem");

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);
        controls.setPadding(false);

        Button minusBtn = new Button(VaadinIcon.MINUS.create(), e -> agregarAlTicketActivo(productName, -1, price));
        minusBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        Button plusBtn = new Button(VaadinIcon.PLUS.create(), e -> agregarAlTicketActivo(productName, 1, price));
        plusBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        controls.add(minusBtn, plusBtn);
        card.add(img, nameLabel, priceLabel, controls);
        return card;
    }

    private VerticalLayout createMultiCartPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        layout.getStyle().set("padding", "1rem");
        layout.getStyle().set("border-radius", "8px");

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        H3 cartTitle = new H3("Ventas Abiertas");

        Button addTicketBtn = new Button("Nueva Venta (+)", VaadinIcon.PLUS.create());
        addTicketBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addTicketBtn.addClickListener(e -> abrirDialogoNombreTicket());

        headerLayout.add(cartTitle, addTicketBtn);

        ticketTabSheet.setWidthFull();
        ticketTabSheet.addSelectedChangeListener(event -> {
            int index = ticketTabSheet.getSelectedIndex();
            if (index >= 0 && index < ticketsAbiertos.size()) {
                ticketAktivo = ticketsAbiertos.get(index);
            }
        });

        layout.add(headerLayout, ticketTabSheet);
        return layout;
    }

    private void abrirDialogoNombreTicket() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nueva Venta en Espera");
        dialog.setWidth("350px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        TextField nameField = new TextField("Nombre del Ticket (Ej. Cliente Juan, Mesa 3)");
        nameField.setValue("Venta " + (ticketsAbiertos.size() + 1));
        nameField.setWidthFull();
        nameField.setClearButtonVisible(true);
        nameField.focus();

        dialogLayout.add(nameField);
        dialog.add(dialogLayout);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());
        Button createBtn = new Button("Crear Ticket", VaadinIcon.CHECK.create());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        createBtn.addClickListener(e -> {
            String nombre = nameField.getValue();
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = "Venta " + (ticketsAbiertos.size() + 1);
            }
            crearNuevoTicket(nombre.trim());
            dialog.close();
        });

        dialog.getFooter().add(cancelBtn, createBtn);
        dialog.open();
        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) nameField.focus();
        });
    }

    private void agregarAlTicketActivo(String producto, int delta, BigDecimal precio) {
        if (ticketAktivo == null) return;

        for (CartItem item : ticketAktivo.getItems()) {
            if (item.getProducto().equals(producto)) {
                int nuevaCantidad = item.getCantidad() + delta;
                if (nuevaCantidad <= 0) {
                    ticketAktivo.getItems().remove(item);
                } else {
                    item.setCantidad(nuevaCantidad);
                }
                ticketAktivo.updateUI();
                return;
            }
        }

        if (delta > 0) {
            ticketAktivo.getItems().add(new CartItem(producto, delta, precio));
            ticketAktivo.updateUI();
        }
    }

    private void confirmarEliminarTicket(TicketVenta ticket) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Descartar Venta");
        confirmDialog.add("¿Estás seguro de cancelar y eliminar el " + ticket.getId() + "?");

        Button cancelBtn = new Button("No, mantener", e -> confirmDialog.close());
        Button yesBtn = new Button("Sí, eliminar", e -> {
            eliminarTicket(ticket);
            confirmDialog.close();
        });
        yesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        confirmDialog.getFooter().add(cancelBtn, yesBtn);
        confirmDialog.open();
    }

    private void eliminarTicket(TicketVenta ticket) {
        if (ticketsAbiertos.size() <= 1) {
            ticket.getItems().clear();
            ticket.updateUI();
            Notification.show("Ticket limpiado.", 3000, Notification.Position.MIDDLE);
            return;
        }

        int index = ticketsAbiertos.indexOf(ticket);
        if (index >= 0) {
            ticketsAbiertos.remove(ticket);
            ticketTabSheet.remove(ticketTabSheet.getTabAt(index));
            
            Notification.show("Venta descartada.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void cerrarTicketActual() {
        eliminarTicket(ticketAktivo);
    }

    private void openCashPaymentDialog(TicketVenta ticket) {
        if (ticket.getItems().isEmpty()) {
            Notification.show("Este ticket está vacío", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cobrar - " + ticket.getId());
        dialog.setWidth("380px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        BigDecimal total = ticket.getTotalAmount();
        Span totalToPayLabel = new Span("Total a Pagar: RD$ " + total);
        totalToPayLabel.getStyle().set("font-size", "1.2rem").set("font-weight", "bold");

        BigDecimalField cashGivenField = new BigDecimalField("Efectivo Recibido (RD$)");
        cashGivenField.setWidthFull();
        cashGivenField.setValueChangeMode(ValueChangeMode.EAGER);

        Span changeDueLabel = new Span("Devuelta: RD$ 0.00");
        changeDueLabel.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-success-text-color)");

        cashGivenField.addValueChangeListener(event -> {
            BigDecimal cashGiven = event.getValue();
            if (cashGiven != null && cashGiven.compareTo(total) >= 0) {
                BigDecimal change = cashGiven.subtract(total);
                changeDueLabel.setText("Devuelta: RD$ " + change);
            } else {
                changeDueLabel.setText("Devuelta: Insuficiente");
            }
        });

        dialogLayout.add(totalToPayLabel, cashGivenField, changeDueLabel);
        dialog.add(dialogLayout);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        Button completePaymentButton = new Button("Finalizar Venta", VaadinIcon.CHECK.create());
        completePaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        completePaymentButton.addClickListener(e -> {
            BigDecimal cashGiven = cashGivenField.getValue();
            if (cashGiven == null || cashGiven.compareTo(total) < 0) {
                Notification.show("El efectivo recibido es menor al total", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            Notification.show("¡Venta procesada con éxito en " + ticket.getId() + "!", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            cerrarTicketActual();
            dialog.close();
        });

        dialog.getFooter().add(cancelButton, completePaymentButton);
        dialog.open();
        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) cashGivenField.focus();
        });
    }

    private void openClientSelectionDialog(TicketVenta ticket) {
        if (ticket.getItems().isEmpty()) {
            Notification.show("Este ticket está vacío", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Fiar - " + ticket.getId());
        dialog.setWidth("400px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        TextField clientSearchField = new TextField("Buscar cliente...");
        clientSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        clientSearchField.setWidthFull();
        clientSearchField.setClearButtonVisible(true);

        Grid<String> clientGrid = new Grid<>(String.class);
        clientGrid.setColumns();
        clientGrid.addColumn(clientName -> clientName).setHeader("Cliente");
        clientGrid.setItems(clientsList);
        clientGrid.setHeight("200px");

        ListDataProvider<String> clientDataProvider = new ListDataProvider<>(clientsList);
        clientGrid.setDataProvider(clientDataProvider);
        
        clientSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        clientSearchField.addValueChangeListener(event -> {
            String filter = event.getValue() == null ? "" : event.getValue().trim().toLowerCase();
            clientDataProvider.setFilter(name -> name.toLowerCase().contains(filter));
        });

        dialogLayout.add(clientSearchField, clientGrid);
        dialog.add(dialogLayout);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        Button confirmCreditButton = new Button("Confirmar Crédito", VaadinIcon.CHECK.create());
        confirmCreditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        confirmCreditButton.addClickListener(e -> {
            String selectedClient = clientGrid.asSingleSelect().getValue();
            if (selectedClient == null) {
                Notification.show("Selecciona un cliente", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            Notification.show("¡Fiado registrado a la libreta de: " + selectedClient + "!", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            
            cerrarTicketActual();
            dialog.close();
        });

        dialog.getFooter().add(cancelButton, confirmCreditButton);
        dialog.open();
    }
}