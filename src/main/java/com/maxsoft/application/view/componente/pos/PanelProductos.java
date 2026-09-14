/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PanelProductos extends VerticalLayout {

    private ComboBox<Articulo> searchBox;

    public PanelProductos(
            
            ArticuloService articuloServicel,
            Consumer<Articulo> onAgregarAlTicketActivo,
            BiConsumer<Articulo, Double> onAgregarCantidadAlTicketActivo,
            BiConsumer<String, Double> onAbrirVentaPorMonto,
            Runnable onAbrirAperturaCaja,
            Runnable onAbrirCierreCaja,
            Runnable onAbrirMovimientoPos
    ) {
        setSpacing(true);

        H2 title = new H2("Colmado - Punto de Venta");

        searchBox = new ComboBox<>("Buscar producto o escanear código");
        searchBox.setItems(articuloServicel.getLista());
        searchBox.setWidthFull();
        searchBox.setClearButtonVisible(true);
        searchBox.focus();

        // Botones de Gestión de Caja
        Button btnCerrarCaja = new Button("Cerrar Caja", VaadinIcon.LOCK.create(), e -> {
            if (onAbrirCierreCaja != null) {
                onAbrirCierreCaja.run();
            }
        });
        btnCerrarCaja.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        Button btnAbrirCaja = new Button("Abrir Caja", VaadinIcon.UNLOCK.create(), e -> {
            if (onAbrirAperturaCaja != null) {
                onAbrirAperturaCaja.run();
            }
        });
        btnAbrirCaja.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);

        Button btnMovimientoPos = new Button("Movimiento POS", VaadinIcon.CREDIT_CARD.create(), e -> {
            try {
                if (onAbrirMovimientoPos != null) {
                    onAbrirMovimientoPos.run();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        btnMovimientoPos.addThemeVariants(ButtonVariant.LUMO_SMALL);

        HorizontalLayout cajaButtonsLayout = new HorizontalLayout(btnAbrirCaja, btnMovimientoPos, btnCerrarCaja);
        cajaButtonsLayout.setSpacing(true);

        searchBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                if (onAgregarAlTicketActivo != null) {
                    onAgregarAlTicketActivo.accept(e.getValue());
                }
                searchBox.clear();
            }
        });

        H3 quickTitle = new H3("Ventas Rápidas (Catálogo)");

        Div gridContainer = new Div();
        gridContainer.getStyle().set("display", "grid");
        gridContainer.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(150px, 1fr))");
        gridContainer.getStyle().set("gap", "1rem");
        gridContainer.setWidthFull();

        for (Articulo art : articuloServicel.getLista()) {

            TarjetaProductoCompo card = new TarjetaProductoCompo(
                    art,
                    "https://images.unsplash.com/photo-1528825871115-3581a5387919?w=300&auto=format&fit=crop&q=60",
                    (articulo, cantidad) -> {
                        if (onAgregarCantidadAlTicketActivo != null) {
                            onAgregarCantidadAlTicketActivo.accept(articulo, cantidad);
                        }
                    },
                    (articulo) -> {
                        if (onAbrirVentaPorMonto != null) {
                            onAbrirVentaPorMonto.accept(articulo.getDescripcion(), articulo.getPrecioVenta());
                        }
                    }
            );

            gridContainer.add(card);
        }

//      for (Articulo art : articuloServicel.getLista()) {
//
//          gridContainer.add(crearTarjetaProducto(art,
//                  "https://images.unsplash.com/photo-1528825871115-3581a5387919?w=300&auto=format&fit=crop&q=60"));
//      }

        add(title, cajaButtonsLayout, searchBox, quickTitle, gridContainer);
    }

    // Método para permitir enfocar el buscador externamente si es necesario
    public ComboBox<Articulo> getSearchBox() {
        return searchBox;
    }
}
