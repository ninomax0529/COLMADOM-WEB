/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("POS - Colmado 1")
@Route("postv2")
@Menu(order = 6, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class PostViewV2 extends HorizontalLayout {

    private static final DecimalFormat CANTIDAD_FORMAT = new DecimalFormat("#,##0.##");
    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    private final List<ClienteFiado> libretaClientes = new ArrayList<>(List.of(
            new ClienteFiado("Juan Pérez", "Vecino Casa 4", new BigDecimal("1200.00"), new BigDecimal("3000.00")),
            new ClienteFiado("Maria Gómez", "La Rubia", new BigDecimal("450.00"), new BigDecimal("2000.00")),
            new ClienteFiado("Pedro Martínez", "El Mecánico", new BigDecimal("2800.00"), new BigDecimal("3000.00")),
            new ClienteFiado("Rosa Almonte", "", new BigDecimal("0.00"), new BigDecimal("1500.00")),
            new ClienteFiado("Carlos Peña", "El Flaco", new BigDecimal("3500.00"), new BigDecimal("3500.00")) // Al límite
    ));

    // =========================================================================
    // MODELOS INTERNOS (Con soporte para cantidades decimales)
    // =========================================================================
    public static class CartItem {

        private final String producto;
        private double cantidad; // Permite fracciones (ej. 0.5 lb, 0.25 lb)
        private final BigDecimal precioUnitario;

        public CartItem(String producto, double cantidad, BigDecimal precioUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public String getProducto() {
            return producto;
        }

        public double getCantidad() {
            return cantidad;
        }

        public void setCantidad(double cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public BigDecimal getTotal() {
            return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
        }

        public String getCantidadFormateada() {
            return CANTIDAD_FORMAT.format(cantidad);
        }
    }

    public static class TicketVenta {

        private String id;
        private final int numeroTicket;
        private final List<CartItem> items = new ArrayList<>();
        private final ListDataProvider<CartItem> dataProvider = new ListDataProvider<>(items);
        private final Grid<CartItem> grid = new Grid<>(CartItem.class, false);
        private final Span totalSpan = new Span("RD$ 0.00");

        public TicketVenta(int numeroTicket, String id) {
            this.numeroTicket = numeroTicket;
            this.id = id;
            totalSpan.getStyle()
                    .set("font-size", "1.8rem")
                    .set("font-weight", "bold")
                    .set("color", "var(--lumo-primary-color)");
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getNumeroTicket() {
            return numeroTicket;
        }

        public List<CartItem> getItems() {
            return items;
        }

        public ListDataProvider<CartItem> getDataProvider() {
            return dataProvider;
        }

        public Grid<CartItem> getGrid() {
            return grid;
        }

        public Span getTotalSpan() {
            return totalSpan;
        }

        public BigDecimal getTotalAmount() {
            return items.stream()
                    .map(CartItem::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        public void updateUI() {
            dataProvider.refreshAll();
            totalSpan.setText("RD$ " + MONEDA_FORMAT.format(getTotalAmount()));
        }
    }

    // =========================================================================
    // ESTADO Y COMPONENTES PRINCIPALES
    // =========================================================================
    private final List<TicketVenta> ticketsAbiertos = new ArrayList<>();
    private final TabSheet ticketTabSheet = new TabSheet();
    private TicketVenta ticketAktivo;
    private int contadorSecuencialTickets = 0;
    private ComboBox<String> searchBox;

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
    public PostViewV2() {
        setSizeFull();
        setSpacing(true);

        crearNuevoTicket(null);

        VerticalLayout leftPanel = crearPanelProductos();
        leftPanel.setWidth("55%");
        leftPanel.setHeightFull();

        VerticalLayout rightPanel = crearPanelCarritos();
        rightPanel.setWidth("45%");
        rightPanel.setHeightFull();

        add(leftPanel, rightPanel);

        configurarAtajosTecladoNumerico();
        configurarAtajosRenombrar();

        // Agrega este atajo en tu constructor para abrir abonos con Alt + A:
        UI.getCurrent().addShortcutListener(
                () -> abrirDialogoAbonoLibreta(),
                Key.KEY_A, KeyModifier.ALT
        );
    }

    private void abrirDialogoAbonoLibreta() {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Abonar a Libreta de Fiados");
        dialog.setWidth("450px");

        VerticalLayout layout = new VerticalLayout();

        ComboBox<ClienteFiado> clientCombo = new ComboBox<>("Seleccionar Cliente");
        clientCombo.setItems(libretaClientes);
        clientCombo.setItemLabelGenerator(c -> c.getDisplayName() + " - Debe: RD$ " + MONEDA_FORMAT.format(c.getBalancePendiente()));
        clientCombo.setWidthFull();

        BigDecimalField abonoField = new BigDecimalField("Monto del Abono (RD$)");
        abonoField.setWidthFull();
        abonoField.setEnabled(false);

        Span nuevoBalanceLabel = new Span("Nuevo balance: RD$ 0.00");
        nuevoBalanceLabel.getStyle().set("font-weight", "bold").set("color", "var(--lumo-success-text-color)");

        clientCombo.addValueChangeListener(e -> {
            ClienteFiado c = e.getValue();
            if (c != null && c.getBalancePendiente().compareTo(BigDecimal.ZERO) > 0) {
                abonoField.setEnabled(true);
                abonoField.setValue(BigDecimal.ZERO);
                abonoField.focus();
            } else {
                abonoField.setEnabled(false);
            }
        });

        abonoField.addValueChangeListener(e -> {
            ClienteFiado c = clientCombo.getValue();
            BigDecimal abono = e.getValue();
            if (c != null && abono != null) {
                BigDecimal restante = c.getBalancePendiente().subtract(abono);
                if (restante.compareTo(BigDecimal.ZERO) < 0) {
                    restante = BigDecimal.ZERO;
                }
                nuevoBalanceLabel.setText("Nuevo balance: RD$ " + MONEDA_FORMAT.format(restante));
            }
        });

        layout.add(clientCombo, abonoField, nuevoBalanceLabel);
        dialog.add(layout);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());
        Button processBtn = new Button("Registrar Abono [Enter]", VaadinIcon.CHECK.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        processBtn.addClickShortcut(Key.ENTER);

        processBtn.addClickListener(e -> {
            ClienteFiado c = clientCombo.getValue();
            BigDecimal abono = abonoField.getValue();
            if (c != null && abono != null && abono.compareTo(BigDecimal.ZERO) > 0) {
                c.setBalancePendiente(c.getBalancePendiente().subtract(abono));
                if (c.getBalancePendiente().compareTo(BigDecimal.ZERO) < 0) {
                    c.setBalancePendiente(BigDecimal.ZERO);
                }
                Notification.show("Abono de RD$ " + MONEDA_FORMAT.format(abono) + " registrado a " + c.getDisplayName(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                enfocarBuscador();
            } else {
                Notification.show("Ingresa un monto de abono válido", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });

        dialog.getFooter().add(cancelBtn, processBtn);
        dialog.open();
    }

    private void enfocarBuscador() {
        if (searchBox != null) {
            searchBox.focus();
        }
    }

    // =========================================================================
    // ATAJOS DE TECLADO GLOBAL
    // =========================================================================
    private void configurarAtajosTecladoNumerico() {
        Key[] digitos = new Key[]{
            Key.DIGIT_1, Key.DIGIT_2, Key.DIGIT_3,
            Key.DIGIT_4, Key.DIGIT_5, Key.DIGIT_6,
            Key.DIGIT_7, Key.DIGIT_8, Key.DIGIT_9
        };

        Key[] numpadDigitos = new Key[]{
            Key.NUMPAD_1, Key.NUMPAD_2, Key.NUMPAD_3,
            Key.NUMPAD_4, Key.NUMPAD_5, Key.NUMPAD_6,
            Key.NUMPAD_7, Key.NUMPAD_8, Key.NUMPAD_9
        };

        for (int i = 0; i < 9; i++) {
            final int index = i;

            UI.getCurrent().addShortcutListener(
                    () -> seleccionarTicketPorPosicion(index),
                    digitos[i], KeyModifier.ALT
            );

            UI.getCurrent().addShortcutListener(
                    () -> seleccionarTicketPorPosicion(index),
                    numpadDigitos[i], KeyModifier.ALT
            );
        }
    }

    private void configurarAtajosRenombrar() {
        UI.getCurrent().addShortcutListener(
                () -> {
                    if (ticketAktivo != null) {
                        abrirDialogoRenombrarTicket(ticketAktivo, new Span(ticketAktivo.getId()));
                    }
                },
                Key.KEY_R, KeyModifier.ALT
        );
    }

    private void seleccionarTicketPorPosicion(int index) {
        if (index >= 0 && index < ticketsAbiertos.size()) {
            ticketTabSheet.setSelectedIndex(index);
            Notification.show("Cambiado a " + ticketsAbiertos.get(index).getId(), 1500, Notification.Position.BOTTOM_END);
            enfocarBuscador();
        }
    }

    // =========================================================================
    // UI - PANELES
    // =========================================================================
    private VerticalLayout crearPanelProductos() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        H2 title = new H2("Colmado - Punto de Venta");

        searchBox = new ComboBox<>("Buscar producto o escanear código");
        searchBox.setItems("Arroz (lb)", "Habichuelas (lb)", "Aceite 16oz", "Plátano Verde", "Salami (lb)", "Queso Geo (lb)", "Cerveza Fría");
        searchBox.setWidthFull();
        searchBox.setClearButtonVisible(true);
        searchBox.focus();

        searchBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                BigDecimal precio = obtenerPrecioEjemplo(e.getValue());
                agregarAlTicketActivo(e.getValue(), 1.0, precio);
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
                crearTarjetaProducto("Queso Geo (lb)", new BigDecimal("220.00"), "https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?w=300&auto=format&fit=crop&q=60"),
                crearTarjetaProducto("Habichuelas (lb)", new BigDecimal("60.00"), "https://images.unsplash.com/photo-1551462147-3f005d5a6057?w=300&auto=format&fit=crop&q=60")
        );

        layout.add(title, searchBox, quickTitle, gridContainer);
        return layout;
    }

    private BigDecimal obtenerPrecioEjemplo(String producto) {
        if (producto.contains("Arroz")) {
            return new BigDecimal("40.00");
        }
        if (producto.contains("Habichuelas")) {
            return new BigDecimal("60.00");
        }
        if (producto.contains("Salami")) {
            return new BigDecimal("140.00");
        }
        if (producto.contains("Queso")) {
            return new BigDecimal("220.00");
        }
        if (producto.contains("Cerveza")) {
            return new BigDecimal("180.00");
        }
        if (producto.contains("Plátano")) {
            return new BigDecimal("25.00");
        }
        return new BigDecimal("75.00");
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

        Button minusBtn = new Button(VaadinIcon.MINUS.create(), e -> agregarAlTicketActivo(productName, -1.0, price));
        minusBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        // Botón especial para vender por pesos RD$ (Ej: RD$ 50 de queso)
        Button pesoBtn = new Button("$", e -> abrirDialogoVentaPorMonto(productName, price));
        pesoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        pesoBtn.setTooltipText("Vender por monto en dinero (RD$)");

        Button plusBtn = new Button(VaadinIcon.PLUS.create(), e -> agregarAlTicketActivo(productName, 1.0, price));
        plusBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        controls.add(minusBtn, pesoBtn, plusBtn);
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
        addTicketBtn.addClickListener(e -> crearNuevoTicket(null));
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
                enfocarBuscador();
            }
        });

        layout.add(headerLayout, ticketTabSheet);
        layout.setFlexGrow(1, ticketTabSheet);
        return layout;
    }

    // =========================================================================
    // GESTIÓN DE TICKETS
    // =========================================================================
    private void crearNuevoTicket(String nombrePersonalizado) {
        contadorSecuencialTickets++;
        String nombreFinal = (nombrePersonalizado != null && !nombrePersonalizado.trim().isEmpty())
                ? nombrePersonalizado.trim()
                : "Venta " + contadorSecuencialTickets;

        TicketVenta nuevoTicket = new TicketVenta(contadorSecuencialTickets, nombreFinal);
        ticketsAbiertos.add(nuevoTicket);

        Grid<CartItem> grid = nuevoTicket.getGrid();
        grid.setDataProvider(nuevoTicket.getDataProvider());

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addColumn(CartItem::getProducto).setHeader("Producto").setAutoWidth(true);
        grid.addColumn(CartItem::getCantidadFormateada).setHeader("Cant.");
        grid.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getPrecioUnitario())).setHeader("Precio");
        grid.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getTotal())).setHeader("Total");

        // Columna de acciones rápidas (+ / - / Editar Cantidad / Eliminar)
        grid.addComponentColumn(item -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(false);

            Button editQtyBtn = new Button(VaadinIcon.EDIT.create(), e -> abrirDialogoEditarCantidad(nuevoTicket, item));
            editQtyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editQtyBtn.setTooltipText("Cambiar cantidad / libras [*]");

            Button removeBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmarEliminarItem(nuevoTicket, item));
            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

            actions.add(editQtyBtn, removeBtn);
            return actions;
        }).setHeader("");

        // Doble clic en la fila para editar la cantidad rápidamente
        grid.addItemDoubleClickListener(e -> abrirDialogoEditarCantidad(nuevoTicket, e.getItem()));

        // Tecla Supr / Delete para borrar la fila seleccionada
        Shortcuts.addShortcutListener(grid, () -> {
            CartItem seleccionado = grid.asSingleSelect().getValue();
            if (seleccionado != null) {
                confirmarEliminarItem(nuevoTicket, seleccionado);
            }
        }, Key.DELETE);
// ATAJO CON TECLA ASTERISCO (*): Editar cantidad con la tecla '*' o del teclado numérico
        Shortcuts.addShortcutListener(grid, () -> {
            CartItem seleccionado = grid.asSingleSelect().getValue();
            if (seleccionado != null) {
                abrirDialogoEditarCantidad(nuevoTicket, seleccionado);
            }
        }, Key.NUMPAD_MULTIPLY);
        // Selección automática al desplazarse con las flechas (Arriba / Abajo)
        grid.addCellFocusListener(e -> {
            e.getItem().ifPresent(item -> grid.select(item));
        });

        grid.setHeightFull();

        VerticalLayout contenidoTab = construirContenidoPanelTicket(nuevoTicket);

        ticketTabSheet.add(nombreFinal, contenidoTab);
        ticketTabSheet.setSelectedTab(ticketTabSheet.getTab(contenidoTab));
        ticketAktivo = nuevoTicket;

        actualizarTitulosPestanas();
        enfocarBuscador();
    }

    private void actualizarTitulosPestanas() {
        for (int i = 0; i < ticketsAbiertos.size(); i++) {
            TicketVenta ticket = ticketsAbiertos.get(i);
            int atajoNum = i + 1;
            String etiqueta = "[Alt+" + atajoNum + "] " + ticket.getId();
            ticketTabSheet.getTabAt(i).setLabel(etiqueta);
        }
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

        Button renameBtn = new Button("Cambiar Nombre [Alt+R]", VaadinIcon.EDIT.create());
        renameBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        renameBtn.addClickListener(e -> abrirDialogoRenombrarTicket(ticket, ticketNameSpan));

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
                    -> item.getProducto().toLowerCase().contains(searchTerm)
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

    private void agregarAlTicketActivo(String producto, double deltaCantidad, BigDecimal precio) {
        if (ticketAktivo == null) {
            return;
        }

        for (CartItem item : ticketAktivo.getItems()) {
            if (item.getProducto().equals(producto)) {
                double nuevaCantidad = item.getCantidad() + deltaCantidad;
                if (nuevaCantidad <= 0) {
                    confirmarEliminarItem(ticketAktivo, item);
                } else {
                    item.setCantidad(nuevaCantidad);
                    ticketAktivo.updateUI();
                }
                enfocarBuscador();
                return;
            }
        }

        if (deltaCantidad > 0) {
            ticketAktivo.getItems().add(new CartItem(producto, deltaCantidad, precio));
            ticketAktivo.updateUI();
        }
        enfocarBuscador();
    }

    private void eliminarTicket(TicketVenta ticket) {
        if (ticketsAbiertos.size() <= 1) {
            ticket.getItems().clear();
            ticket.updateUI();
            Notification.show("Ticket limpiado.", 3000, Notification.Position.MIDDLE);
            enfocarBuscador();
            return;
        }

        int index = ticketsAbiertos.indexOf(ticket);
        if (index >= 0) {
            ticketsAbiertos.remove(ticket);
            ticketTabSheet.remove(ticketTabSheet.getTabAt(index));
            actualizarTitulosPestanas();

            Notification.show("Venta descartada.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
        enfocarBuscador();
    }

    private void cerrarTicketActual() {
        eliminarTicket(ticketAktivo);
    }

    // =========================================================================
    // DIÁLOGOS DE CONTROL DE CANTIDAD Y PESO (PASO 1)
    // =========================================================================
    private void abrirDialogoVentaPorMonto(String producto, BigDecimal precioUnitario) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Vender por Dinero (RD$)");
        dialog.setWidth("360px");

        VerticalLayout layout = new VerticalLayout();

        Span infoLabel = new Span(producto + " - Precio: RD$ " + precioUnitario + " / unidad o lb");
        infoLabel.getStyle().set("font-size", "0.85rem").set("color", "var(--lumo-secondary-text-color)");

        BigDecimalField montoField = new BigDecimalField("Monto deseado (RD$)");
        montoField.setPlaceholder("Ej: 50, 100, 150");
        montoField.setWidthFull();
        montoField.setValueChangeMode(ValueChangeMode.EAGER);

        Span resultadoSpan = new Span("Cantidad calculada: 0.00");
        resultadoSpan.getStyle().set("font-weight", "bold").set("color", "var(--lumo-primary-color)");

        montoField.addValueChangeListener(e -> {
            BigDecimal monto = e.getValue();
            if (monto != null && monto.compareTo(BigDecimal.ZERO) > 0 && precioUnitario.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal cantidadCalculada = monto.divide(precioUnitario, 3, RoundingMode.HALF_UP);
                resultadoSpan.setText("Cantidad calculada: " + CANTIDAD_FORMAT.format(cantidadCalculada));
            } else {
                resultadoSpan.setText("Cantidad calculada: 0.00");
            }
        });

        layout.add(infoLabel, montoField, resultadoSpan);
        dialog.add(layout);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());
        Button acceptBtn = new Button("Agregar al Ticket [Enter]", VaadinIcon.CHECK.create());
        acceptBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        acceptBtn.addClickShortcut(Key.ENTER);

        acceptBtn.addClickListener(e -> {
            BigDecimal monto = montoField.getValue();
            if (monto != null && monto.compareTo(BigDecimal.ZERO) > 0) {
                double cantidadCalculada = monto.divide(precioUnitario, 4, RoundingMode.HALF_UP).doubleValue();
                agregarAlTicketActivo(producto, cantidadCalculada, precioUnitario);
                dialog.close();
            } else {
                Notification.show("Ingresa un monto válido en RD$", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });

        dialog.getFooter().add(cancelBtn, acceptBtn);
        dialog.open();
        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                montoField.focus();
            }
        });
    }

    private void abrirDialogoEditarCantidad(TicketVenta ticket, CartItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar Cantidad - " + item.getProducto());
        dialog.setWidth("350px");

        VerticalLayout layout = new VerticalLayout();

        NumberField qtyField = new NumberField("Nueva cantidad (Unidades / Libras)");
        qtyField.setValue(item.getCantidad());
        qtyField.setStep(0.25); // Permite incrementos de cuarto de libra (0.25)
        qtyField.setClearButtonVisible(true);
        qtyField.setWidthFull();

        Span subtotalSpan = new Span("Subtotal: RD$ " + MONEDA_FORMAT.format(item.getTotal()));
        subtotalSpan.getStyle().set("font-weight", "bold");

        qtyField.addValueChangeListener(e -> {
            Double val = e.getValue();
            if (val != null && val > 0) {
                BigDecimal tempTotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(val));
                subtotalSpan.setText("Subtotal: RD$ " + MONEDA_FORMAT.format(tempTotal));
            } else {
                subtotalSpan.setText("Subtotal: RD$ 0.00");
            }
        });

        layout.add(qtyField, subtotalSpan);
        dialog.add(layout);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());
        Button saveBtn = new Button("Guardar [Enter]", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickShortcut(Key.ENTER);

        saveBtn.addClickListener(e -> {
            Double nuevaQty = qtyField.getValue();
            if (nuevaQty != null && nuevaQty > 0) {
                item.setCantidad(nuevaQty);
                ticket.updateUI();
                dialog.close();
                enfocarBuscador();
            } else if (nuevaQty != null && nuevaQty <= 0) {
                confirmarEliminarItem(ticket, item);
                dialog.close();
            }
        });

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                qtyField.focus();
                qtyField.getElement().executeJs("this.focus(); this.select();");
            }
        });
    }

    // =========================================================================
    // OTROS DIÁLOGOS
    // =========================================================================
    private void confirmarEliminarItem(TicketVenta ticket, CartItem item) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Eliminar Producto");
        confirmDialog.add("¿Deseas quitar \"" + item.getProducto() + "\" del ticket?");

        Button cancelBtn = new Button("Cancelar", e -> confirmDialog.close());

        Button removeBtn = new Button("Sí, quitar [Enter]", e -> {
            ticket.getItems().remove(item);
            ticket.updateUI();
            confirmDialog.close();
            enfocarBuscador();
        });
        removeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        removeBtn.addClickShortcut(Key.ENTER);

        confirmDialog.getFooter().add(cancelBtn, removeBtn);
        confirmDialog.open();
    }

    private void abrirDialogoRenombrarTicket(TicketVenta ticket, Span ticketNameSpan) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Renombrar Ticket");
        dialog.setWidth("350px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        TextField nameField = new TextField("Nuevo nombre del cliente / venta");
        nameField.setValue(ticket.getId());
        nameField.setWidthFull();
        nameField.setClearButtonVisible(true);

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
                actualizarTitulosPestanas();
                Notification.show("Venta renombrada a: " + nombreLimpio, 2000, Notification.Position.BOTTOM_END);
            }
            dialog.close();
            enfocarBuscador();
        });

        dialog.getFooter().add(cancelBtn, saveBtn);

        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                nameField.focus();
                nameField.getElement().executeJs("this.focus(); this.select();");
            }
        });

        dialog.open();
    }

    private void confirmarEliminarTicket(TicketVenta ticket) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Descartar Venta");
        confirmDialog.add("¿Estás seguro de cancelar y eliminar " + ticket.getId() + "?");

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
            Notification.show("El ticket [" + ticket.getId() + "] está vacío", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cobrar - " + ticket.getId());
        dialog.setWidth("380px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        BigDecimal total = ticket.getTotalAmount();
        Span totalToPayLabel = new Span("Total a Pagar: RD$ " + MONEDA_FORMAT.format(total));
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
                changeDueLabel.setText("Devuelta: RD$ " + MONEDA_FORMAT.format(change));
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

            Notification.show("¡Venta completada para: " + ticket.getId() + "! (RD$ " + MONEDA_FORMAT.format(total) + ")", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            cerrarTicketActual();
            dialog.close();
        });

        dialog.getFooter().add(cancelButton, completePaymentButton);
        dialog.open();
        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                cashGivenField.focus();
            }
        });
    }

    private void abrirDialogoSeleccionCliente(TicketVenta ticket) {

        if (ticket.getItems().isEmpty()) {
            Notification.show("El ticket [" + ticket.getId() + "] está vacío", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Fiar Ticket - " + ticket.getId());
        dialog.setWidth("480px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        BigDecimal totalVenta = ticket.getTotalAmount();
        Span totalLabel = new Span("Monto a fiar: RD$ " + MONEDA_FORMAT.format(totalVenta));
        totalLabel.getStyle().set("font-weight", "bold").set("font-size", "1.1rem");

        TextField clientSearchField = new TextField("Buscar cliente o apodo...");
        clientSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        clientSearchField.setWidthFull();
        clientSearchField.setClearButtonVisible(true);

        Grid<ClienteFiado> clientGrid = new Grid<>();
        clientGrid.setHeight("220px");

        clientGrid.addColumn(ClienteFiado::getDisplayName).setHeader("Cliente / Apodo");
        clientGrid.addColumn(c -> "RD$ " + MONEDA_FORMAT.format(c.getBalancePendiente())).setHeader("Debe");
        clientGrid.addColumn(c -> "RD$ " + MONEDA_FORMAT.format(c.getCreditoDisponible())).setHeader("Disponible");

        ListDataProvider<ClienteFiado> dataProvider = new ListDataProvider<>(libretaClientes);
        clientGrid.setDataProvider(dataProvider);

        // Búsqueda inteligente por nombre o apodo
        clientSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        clientSearchField.addValueChangeListener(event -> {
            String filter = event.getValue() == null ? "" : event.getValue().trim().toLowerCase();
            dataProvider.setFilter(cliente
                    -> cliente.getNombre().toLowerCase().contains(filter)
                    || cliente.getApodo().toLowerCase().contains(filter)
            );
        });

        // Panel de advertencia si sobrepasa el límite
        Span warningLabel = new Span();
        warningLabel.getStyle().set("color", "var(--lumo-error-text-color)").set("font-weight", "bold").set("font-size", "0.9rem");
        warningLabel.setVisible(false);

        Button confirmCreditButton = new Button("Confirmar Crédito [Enter]", VaadinIcon.CHECK.create());
        confirmCreditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        confirmCreditButton.addClickShortcut(Key.ENTER);
        confirmCreditButton.setEnabled(false);

        clientGrid.asSingleSelect().addValueChangeListener(e -> {
            ClienteFiado seleccionado = e.getValue();
            if (seleccionado != null) {
                BigDecimal disponible = seleccionado.getCreditoDisponible();
                if (totalVenta.compareTo(disponible) > 0) {
                    BigDecimal exceso = totalVenta.subtract(disponible);
                    warningLabel.setText("⚠️ Excede límite por RD$ " + MONEDA_FORMAT.format(exceso) + " (Disponible: RD$ " + MONEDA_FORMAT.format(disponible) + ")");
                    warningLabel.setVisible(true);
                    confirmCreditButton.setEnabled(false); // Bloquea si excede límite
                } else {
                    warningLabel.setVisible(false);
                    confirmCreditButton.setEnabled(true);
                }
            } else {
                confirmCreditButton.setEnabled(false);
                warningLabel.setVisible(false);
            }
        });

        confirmCreditButton.addClickListener(e -> {
            ClienteFiado cliente = clientGrid.asSingleSelect().getValue();
            if (cliente != null) {
                // Sumamos la venta al balance pendiente del cliente
                cliente.setBalancePendiente(cliente.getBalancePendiente().add(totalVenta));

                Notification.show("¡Fiado cargado a la libreta de " + cliente.getDisplayName() + "! Nuevo balance: RD$ " + MONEDA_FORMAT.format(cliente.getBalancePendiente()), 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
// Dentro de procesarPagoEfectivo, cuando el cobro sea exitoso:
                imprimirTicketTermico(ticket, "EFECTIVO", cliente.getNombre());
               
                cerrarTicketActual();
                dialog.close();
            }
        });

        dialogLayout.add(totalLabel, clientSearchField, clientGrid, warningLabel);
        dialog.add(dialogLayout);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancelButton, confirmCreditButton);
        dialog.open();

        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                clientSearchField.focus();
            }
        });
    }

    public static class ClienteFiado {

        private String nombre;
        private String apodo;
        private BigDecimal balancePendiente;
        private BigDecimal limiteCredito;

        public ClienteFiado(String nombre, String apodo, BigDecimal balancePendiente, BigDecimal limiteCredito) {
            this.nombre = nombre;
            this.apodo = apodo;
            this.balancePendiente = balancePendiente;
            this.limiteCredito = limiteCredito;
        }

        public String getNombre() {
            return nombre;
        }

        public String getApodo() {
            return apodo;
        }

        public BigDecimal getBalancePendiente() {
            return balancePendiente;
        }

        public void setBalancePendiente(BigDecimal balance) {
            this.balancePendiente = balance;
        }

        public BigDecimal getLimiteCredito() {
            return limiteCredito;
        }

        public BigDecimal getCreditoDisponible() {
            return limiteCredito.subtract(balancePendiente);
        }

        public String getDisplayName() {
            if (apodo != null && !apodo.isBlank()) {
                return nombre + " (" + apodo + ")";
            }
            return nombre;
        }
    }

    private String generarHtmlTicketTermico(TicketVenta ticket, String tipoPago, String clienteNombre) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
                .append("body { font-family: 'Courier New', monospace; font-size: 12px; width: 280px; margin: 0; padding: 5px; }")
                .append(".center { text-align: center; }")
                .append(".bold { font-weight: bold; }")
                .append(".right { text-align: right; }")
                .append(".line { border-bottom: 1px dashed #000; margin: 5px 0; }")
                .append("table { width: 100%; border-collapse: collapse; font-size: 11px; }")
                .append("td, th { text-align: left; padding: 2px 0; }")
                .append("</style></head><body>");

        // Encabezado del Colmado
        html.append("<div class='center bold' style='font-size: 14px;'>COLMADO LA BENDICIÓN</div>");
        html.append("<div class='center'>RNC: 101-00000-1</div>");
        html.append("<div class='center'>Tel: (809) 555-0199</div>");
        html.append("<div class='center'>Santiago, Rep. Dom.</div>");
        html.append("<div class='line'></div>");

        // Info del Ticket
        html.append("<div><b>Ticket #:</b> ").append(ticket.getId()).append("</div>");
        html.append("<div><b>Fecha:</b> ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"))).append("</div>");
        html.append("<div><b>Tipo:</b> ").append(tipoPago).append("</div>");

        if (clienteNombre != null && !clienteNombre.isBlank()) {
            html.append("<div><b>Cliente:</b> ").append(clienteNombre).append("</div>");
        }

        html.append("<div class='line'></div>");

        // Detalle de Productos
        html.append("<table>");
        html.append("<tr><th>Cant.</th><th>Producto</th><th class='right'>Total</th></tr>");

        for (CartItem item : ticket.getItems()) {
            html.append("<tr>")
                    .append("<td>").append(item.getCantidadFormateada()).append("</td>")
                    .append("<td>").append(item.getProducto()).append("</td>")
                    .append("<td class='right'>RD$ ").append(MONEDA_FORMAT.format(item.getTotal())).append("</td>")
                    .append("</tr>");
        }

        html.append("</table>");
        html.append("<div class='line'></div>");

        // Total General
        html.append("<div class='right bold' style='font-size: 13px;'>TOTAL: RD$ ")
                .append(MONEDA_FORMAT.format(ticket.getTotalAmount())).append("</div>");

        // Pie de página
        html.append("<div class='line'></div>");
        html.append("<div class='center'>¡Gracias por su compra!</div>");
        html.append("<div class='center'>Dios le bendiga</div>");
        html.append("</body></html>");

        return html.toString();
    }
    
    
    private void imprimirTicketTermico(TicketVenta ticket, String tipoPago, String clienteNombre) {
    String htmlContenido = generarHtmlTicketTermico(ticket, tipoPago, clienteNombre)
            .replace("'", "\\'")
            .replace("\n", "");

    String scriptJs = 
        "var frame = document.createElement('iframe');" +
        "frame.style.display = 'none';" +
        "document.body.appendChild(frame);" +
        "frame.contentWindow.document.open();" +
        "frame.contentWindow.document.write('" + htmlContenido + "');" +
        "frame.contentWindow.document.close();" +
        "setTimeout(function() {" +
        "  frame.contentWindow.focus();" +
        "  frame.contentWindow.print();" +
        "  document.body.removeChild(frame);" +
        "}, 300);";

    UI.getCurrent().getPage().executeJs(scriptJs);
}

//    private void imprimirTicketTermico(TicketVenta ticket, String tipoPago, String clienteNombre) {
//        String htmlContenido = generarHtmlTicketTermico(ticket, tipoPago, clienteNombre);
//
//        // Inyectamos un iframe invisible para enviar la orden de impresión directa a la POS Thermal Printer
//        String scriptJs
//                = "var iframe = document.createElement('iframe');"
//                + "iframe.style.position = 'absolute';"
//                + "iframe.style.width = '0px';"
//                + "iframe.style.height = '0px';"
//                + "iframe.style.border = 'none';"
//                + "document.body.appendChild(iframe);"
//                + "var doc = iframe.contentWindow.document;"
//                + "doc.open();"
////                + "doc.write(" + com.fasterxml.jackson.databind.ObjectMapperHolder.getMapper().valueToTree(htmlContenido).toString() + ");"
//                + "doc.close();"
//                + "setTimeout(function() {"
//                + "  iframe.contentWindow.focus();"
//                + "  iframe.contentWindow.print();"
//                + "  document.body.removeChild(iframe);"
//                + "}, 500);";
//
//        UI.getCurrent().getPage().executeJs(scriptJs);
//    }
}
