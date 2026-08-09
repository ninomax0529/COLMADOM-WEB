/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("POS - Colmado V2")
@Route("postvRefactori")
@Menu(order = 5, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)

public class PostViewRefactori extends HorizontalLayout {

    // =========================================================================
    // MODELOS INTERNOS
    // =========================================================================

    public static class CartItem {
        private final String producto;
        private int cantidad;
        private final BigDecimal precioUnitario;

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

    // =========================================================================
    // ESTADO Y CONFIGURACIÓN
    // =========================================================================

    private final List<TicketVenta> ticketsAbiertos = new ArrayList<>();
    private final TabSheet ticketTabSheet = new TabSheet();
    private TicketVenta ticketAktivo;

    private static final List<String> CLIENTS_LIST = List.of(
            "Juan Pérez (Vecino)", 
            "Maria Gómez", 
            "Pedro El Mecánico", 
            "Rosa Almonte", 
            "Carlos 'El Flaco'"
    );

    // =========================================================================
    // CONSTRUCTOR PRINCIPAL
    // =========================================================================

//    public PostViewRefactori() {
//        setSizeFull();
//        setSpacing(true);
//
//        crearNuevoTicket("Venta Principal");
//
//        VerticalLayout leftPanel = crearPanelProductos();
//        leftPanel.setWidth("55%");
//        leftPanel.setHeightFull();
//
//        VerticalLayout rightPanel = crearPanelCarritos();
//        rightPanel.setWidth("45%");
//        rightPanel.setHeightFull();
//
//        add(leftPanel, rightPanel);
//    }

    // =========================================================================
// CONSTRUCTOR PRINCIPAL
// =========================================================================

public PostViewRefactori() {
    setSizeFull();
    setSpacing(true);

    crearNuevoTicket("Venta Principal");

    VerticalLayout leftPanel = crearPanelProductos();
    leftPanel.setWidth("55%");
    leftPanel.setHeightFull();

    VerticalLayout rightPanel = crearPanelCarritos();
    rightPanel.setWidth("45%");
    rightPanel.setHeightFull();

    add(leftPanel, rightPanel);

    // Configuración de atajos de teclado para cambiar de pestaña
    configurarAtajosPestanas();
}

private void configurarAtajosPestanas() {
    // Ctrl + Alt + Derecha -> Siguiente Ticket
    UI.getCurrent().addShortcutListener(
        () -> cambiarTabRelativo(1),
        Key.ARROW_RIGHT, KeyModifier.CONTROL, KeyModifier.ALT
    );

    // Ctrl + Alt + Izquierda -> Ticket Anterior
    UI.getCurrent().addShortcutListener(
        () -> cambiarTabRelativo(-1),
        Key.ARROW_LEFT, KeyModifier.CONTROL, KeyModifier.ALT
    );
}

private void cambiarTabRelativo(int direccion) {
    int totalTabs = ticketsAbiertos.size();
    if (totalTabs <= 1) return; // No hay a dónde cambiar si hay 0 o 1 ticket

    int currentIndex = ticketTabSheet.getSelectedIndex();
    int nextIndex = currentIndex + direccion;

    // Comportamiento circular (si llega al final vuelve al inicio y viceversa)
    if (nextIndex >= totalTabs) {
        nextIndex = 0;
    } else if (nextIndex < 0) {
        nextIndex = totalTabs - 1;
    }

    ticketTabSheet.setSelectedIndex(nextIndex);
}
    
    
    // =========================================================================
    // CONSTRUCCIÓN DE PANELES (UI)
    // =========================================================================

    private VerticalLayout crearPanelProductos() {
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
            crearTarjetaProducto("Plátano Verde", new BigDecimal("25.00"), "https://images.unsplash.com/photo-1528825871115-3581a5387919?w=300&auto=format&fit=crop&q=60"),
            crearTarjetaProducto("Arroz (1 lb)", new BigDecimal("40.00"), "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=300&auto=format&fit=crop&q=60"),
            crearTarjetaProducto("Cerveza Grande", new BigDecimal("180.00"), "https://images.unsplash.com/photo-1608270154045-24d9d1341cbf?w=300&auto=format&fit=crop&q=60"),
            crearTarjetaProducto("Salami (lb)", new BigDecimal("140.00"), "https://images.unsplash.com/photo-1544025162-d76694265947?w=300&auto=format&fit=crop&q=60"),
            crearTarjetaProducto("Aceite 16oz", new BigDecimal("95.00"), "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=300&auto=format&fit=crop&q=60"),
            crearTarjetaProducto("Habichuelas (lb)", new BigDecimal("60.00"), "https://images.unsplash.com/photo-1551462147-3f005d5a6057?w=300&auto=format&fit=crop&q=60")
        );

        layout.add(title, searchBox, quickTitle, gridContainer);
        return layout;
    }

    private VerticalLayout crearTarjetaProducto(String productName, BigDecimal price, String imageUrl) {
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

    private VerticalLayout crearPanelCarritos() {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        layout.getStyle().set("padding", "1rem");
        layout.getStyle().set("border-radius", "8px");
        layout.setHeightFull();

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        H3 cartTitle = new H3("Ventas Abiertas");

        Button addTicketBtn = new Button("Nueva Venta (+) [Alt+N]", VaadinIcon.PLUS.create());
        addTicketBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addTicketBtn.addClickListener(e -> abrirDialogoNombreTicket());
        addTicketBtn.addClickShortcut(Key.KEY_N, KeyModifier.ALT);

        headerLayout.add(cartTitle, addTicketBtn);

        ticketTabSheet.setWidthFull();
        ticketTabSheet.setHeightFull();
        ticketTabSheet.getStyle().set("display", "flex");
        ticketTabSheet.getStyle().set("flex-direction", "column");

        ticketTabSheet.addSelectedChangeListener(event -> {
            int index = ticketTabSheet.getSelectedIndex();
            if (index >= 0 && index < ticketsAbiertos.size()) {
                ticketAktivo = ticketsAbiertos.get(index);
            }
        });

        layout.add(headerLayout, ticketTabSheet);
        layout.setFlexGrow(1, ticketTabSheet);
        return layout;
    }

    // =========================================================================
    // GESTIÓN DE TICKETS Y CONTENIDO DE PESTAÑAS
    // =========================================================================

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
            Button removeBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmarEliminarItem(nuevoTicket, item));
            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return removeBtn;
        }).setHeader("");
        
        grid.setHeightFull();

        VerticalLayout contenidoTab = construirContenidoPanelTicket(nuevoTicket);
        
        ticketTabSheet.add(nombre, contenidoTab);
        ticketTabSheet.setSelectedTab(ticketTabSheet.getTab(contenidoTab));
        ticketAktivo = nuevoTicket;
    }

    private VerticalLayout construirContenidoPanelTicket(TicketVenta ticket) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setHeightFull();

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.setAlignItems(Alignment.CENTER);

        Span ticketNameSpan = new Span(ticket.getId());
        ticketNameSpan.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-header-text-color)");

        Button renameBtn = new Button("Cambiar Nombre", VaadinIcon.EDIT.create());
        renameBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        renameBtn.addClickListener(e -> abrirDialogoRenombrarTicket(ticket, ticketNameSpan));

        topBar.add(ticketNameSpan, renameBtn);

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

        VerticalLayout gridContainer = new VerticalLayout(ticket.getGrid());
        gridContainer.setPadding(false);
        gridContainer.setSpacing(false);
        gridContainer.setSizeFull();
        layout.setFlexGrow(1, gridContainer);

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

        Button cancelTicketBtn = new Button("Cancelar [Alt+C]", VaadinIcon.CLOSE.create());
        cancelTicketBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        cancelTicketBtn.addClickListener(e -> confirmarEliminarTicket(ticket));
        cancelTicketBtn.addClickShortcut(Key.KEY_C, KeyModifier.ALT);

        Button checkoutBtn = new Button("COBRAR [F2]", VaadinIcon.CASH.create());
        checkoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        checkoutBtn.addClickListener(e -> abrirDialogoCobroEfectivo(ticket));
        checkoutBtn.addClickShortcut(Key.F2);

        Button creditBtn = new Button("FIAR [F3]", VaadinIcon.BOOK.create());
        creditBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        creditBtn.addClickListener(e -> abrirDialogoSeleccionCliente(ticket));
        creditBtn.addClickShortcut(Key.F3);

        actionButtons.setFlexGrow(1, cancelTicketBtn, checkoutBtn, creditBtn);
        actionButtons.add(cancelTicketBtn, checkoutBtn, creditBtn);

        layout.add(topBar, filterText, gridContainer, totalLayout, actionButtons);
        return layout;
    }

    private void agregarAlTicketActivo(String producto, int delta, BigDecimal precio) {
        if (ticketAktivo == null) return;

        for (CartItem item : ticketAktivo.getItems()) {
            if (item.getProducto().equals(producto)) {
                int nuevaCantidad = item.getCantidad() + delta;
                if (nuevaCantidad <= 0) {
                    confirmarEliminarItem(ticketAktivo, item);
                } else {
                    item.setCantidad(nuevaCantidad);
                    ticketAktivo.updateUI();
                }
                return;
            }
        }

        if (delta > 0) {
            ticketAktivo.getItems().add(new CartItem(producto, delta, precio));
            ticketAktivo.updateUI();
        }
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

    // =========================================================================
    // DIÁLOGOS (MODALES)
    // =========================================================================

    private void confirmarEliminarItem(TicketVenta ticket, CartItem item) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Eliminar Producto");
        confirmDialog.add("¿Deseas quitar \"" + item.getProducto() + "\" del ticket?");

        Button cancelBtn = new Button("Cancelar", e -> confirmDialog.close());
        
        Button removeBtn = new Button("Sí, quitar", e -> {
            ticket.getItems().remove(item);
            ticket.updateUI();
            confirmDialog.close();
        });
        removeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        removeBtn.addClickShortcut(Key.ENTER);

        confirmDialog.getFooter().add(cancelBtn, removeBtn);
        confirmDialog.open();
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
        
        Button createBtn = new Button("Crear Ticket [Enter]", VaadinIcon.CHECK.create());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        createBtn.addClickShortcut(Key.ENTER);

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

    private void abrirDialogoRenombrarTicket(TicketVenta ticket, Span ticketNameSpan) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Renombrar Ticket");
        dialog.setWidth("350px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        TextField nameField = new TextField("Nuevo nombre");
        nameField.setValue(ticket.getId());
        nameField.setWidthFull();
        nameField.setClearButtonVisible(true);
        nameField.focus();

        dialogLayout.add(nameField);
        dialog.add(dialogLayout);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());
        
        Button saveBtn = new Button("Guardar [Enter]", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickShortcut(Key.ENTER);

        saveBtn.addClickListener(e -> {
            String nuevoNombre = nameField.getValue();
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                String nombreLimpio = nuevoNombre.trim();
                
                ticket.setId(nombreLimpio);
                ticketNameSpan.setText(nombreLimpio);

                int index = ticketsAbiertos.indexOf(ticket);
                if (index >= 0) {
                    ticketTabSheet.getTabAt(index).setLabel(nombreLimpio);
                }

                Notification.show("Ticket renombrado con éxito", 3000, Notification.Position.MIDDLE);
            }
            dialog.close();
        });

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) nameField.focus();
        });
    }

    private void confirmarEliminarTicket(TicketVenta ticket) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Descartar Venta");
        confirmDialog.add("¿Estás seguro de cancelar y eliminar el " + ticket.getId() + "?");

        Button cancelBtn = new Button("No, mantener", e -> confirmDialog.close());
        
        Button yesBtn = new Button("Sí, eliminar [Enter]", e -> {
            eliminarTicket(ticket);
            confirmDialog.close();
        });
        yesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        yesBtn.addClickShortcut(Key.ENTER);

        confirmDialog.getFooter().add(cancelBtn, yesBtn);
        confirmDialog.open();
    }

    private void abrirDialogoCobroEfectivo(TicketVenta ticket) {
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
        
        Button completePaymentButton = new Button("Finalizar Venta [Enter]", VaadinIcon.CHECK.create());
        completePaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        completePaymentButton.addClickShortcut(Key.ENTER);

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

    private void abrirDialogoSeleccionCliente(TicketVenta ticket) {
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
        clientGrid.setItems(CLIENTS_LIST);
        clientGrid.setHeight("200px");

        ListDataProvider<String> clientDataProvider = new ListDataProvider<>(CLIENTS_LIST);
        clientGrid.setDataProvider(clientDataProvider);
        
        clientSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        clientSearchField.addValueChangeListener(event -> {
            String filter = event.getValue() == null ? "" : event.getValue().trim().toLowerCase();
            clientDataProvider.setFilter(name -> name.toLowerCase().contains(filter));
        });

        dialogLayout.add(clientSearchField, clientGrid);
        dialog.add(dialogLayout);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        
        Button confirmCreditButton = new Button("Confirmar Crédito [Enter]", VaadinIcon.CHECK.create());
        confirmCreditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        confirmCreditButton.addClickShortcut(Key.ENTER);

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