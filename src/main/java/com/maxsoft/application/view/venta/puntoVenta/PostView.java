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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("POS - Colmado")
@Route("pos")
@Menu(order = 3, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class PostView extends HorizontalLayout {

    // Modelo sencillo de línea de venta
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

    private final List<CartItem> cart = new ArrayList<>();
    private final Grid<CartItem> grid = new Grid<>(CartItem.class, false);
    private final Span totalSpan = new Span("RD$ 0.00");
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public PostView() {
        setSizeFull();
        setSpacing(true);

        // Panel Izquierdo: Selección de Productos y Accesos Rápidos
        VerticalLayout leftPanel = createProductPanel();
        leftPanel.setWidth("50%");
        leftPanel.setHeightFull();

        // Panel Derecho: Carrito de Compras y Cobro
        VerticalLayout rightPanel = createCartPanel();
        rightPanel.setWidth("50%");
        rightPanel.setHeightFull();

        add(leftPanel, rightPanel);
    }

    private VerticalLayout createProductPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        H2 title = new H2("Colmado - Punto de Venta");

        // Buscador rápido
        ComboBox<String> searchBox = new ComboBox<>("Buscar producto o escanear código");
        searchBox.setItems("Arroz (lb)", "Habichuelas (lb)", "Aceite 16oz", "Plátano Verde", "Salami (lb)", "Cerveza Fría");
        searchBox.setWidthFull();
        searchBox.setClearButtonVisible(true);
        searchBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                // Precio simulado según producto
                addToCart(e.getValue(), 1, new BigDecimal("75.00"));
                searchBox.clear();
            }
        });

        // Botones de acceso rápido para productos comunes
        H3 quickTitle = new H3("Ventas Rápidas");
        HorizontalLayout quickButtons = new HorizontalLayout();
        quickButtons.setWidthFull();

        Button btnPlatano = new Button("Plátano Verde", e -> addToCart("Plátano Verde", 1, new BigDecimal("25.00")));
        Button btnArroz = new Button("Arroz (1 lb)", e -> addToCart("Arroz (lb)", 1, new BigDecimal("40.00")));
        Button btnFria = new Button("Cerveza Grande", e -> addToCart("Cerveza Fría", 1, new BigDecimal("180.00")));

        quickButtons.add(btnPlatano, btnArroz, btnFria);

        layout.add(title, searchBox, quickTitle, quickButtons);
        return layout;
    }

    private VerticalLayout createCartPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        layout.getStyle().set("padding", "1rem");
        layout.getStyle().set("border-radius", "8px");

        H3 cartTitle = new H3("Detalle de la Compra");

        // Configuración del Grid del Carrito
        grid.addColumn(CartItem::getProducto).setHeader("Producto").setAutoWidth(true);
        grid.addColumn(CartItem::getCantidad).setHeader("Cant.");
        grid.addColumn(item -> "RD$ " + item.getPrecioUnitario()).setHeader("Precio");
        grid.addColumn(item -> "RD$ " + item.getTotal()).setHeader("Total");
        
        grid.addComponentColumn(item -> {
            Button removeBtn = new Button(VaadinIcon.TRASH.create(), e -> removeFromCart(item));
            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return removeBtn;
        }).setHeader("");

        grid.setItems(cart);
        grid.setHeight("300px");

        // Panel del Total
        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span totalLabel = new Span("TOTAL:");
        totalLabel.getStyle().set("font-size", "1.5rem").set("font-weight", "bold");
        
        totalSpan.getStyle().set("font-size", "1.8rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        totalLayout.add(totalLabel, totalSpan);

        // Botón de Cobrar
        Button checkoutBtn = new Button("COBRAR", VaadinIcon.CASH.create());
        checkoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        checkoutBtn.setWidthFull();
        checkoutBtn.getStyle().set("height", "50px").set("font-size", "1.2rem");
        checkoutBtn.addClickListener(e -> processPayment());

        layout.add(cartTitle, grid, totalLayout, checkoutBtn);
        return layout;
    }

    private void addToCart(String producto, int cantidad, BigDecimal precio) {
        // Verificar si el producto ya existe en la lista
        for (CartItem item : cart) {
            if (item.getProducto().equals(producto)) {
                item.setCantidad(item.getCantidad() + cantidad);
                updateCartUI();
                return;
            }
        }

        cart.add(new CartItem(producto, cantidad, precio));
        updateCartUI();
    }

    private void removeFromCart(CartItem item) {
        cart.remove(item);
        updateCartUI();
    }

    private void updateCartUI() {
        grid.getDataProvider().refreshAll();
        
        // Calcular Total
        totalAmount = cart.stream()
                .map(CartItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalSpan.setText("RD$ " + totalAmount.toString());
    }

    private void processPayment() {
        if (cart.isEmpty()) {
            return;
        }
        // Lógica para registrar venta o imprimir recibo
        cart.clear();
        updateCartUI();
    }
}
