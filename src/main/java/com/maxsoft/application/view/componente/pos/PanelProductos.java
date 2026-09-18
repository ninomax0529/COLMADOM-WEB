/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.venta.FacturaDeVentaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PanelProductos extends VerticalLayout {

    private ComboBox<Articulo> searchBox;
    FacturaDeVentaService facturaDeVentaService;

    public PanelProductos(
            ArticuloService articuloServicel,
            Consumer<Articulo> onAgregarAlTicketActivo,
            BiConsumer<Articulo, Double> onAgregarCantidadAlTicketActivo,
            BiConsumer<String, Double> onAbrirVentaPorMonto,
            Runnable onAbrirAperturaCaja,
            Runnable onAbrirCierreCaja,
            Runnable onAbrirMovimientoPos,
            FacturaDeVentaService facturaDeVentaService
    ) {
        setSpacing(true);

        H2 title = new H2("Colmado - Punto de Venta");

        searchBox = new ComboBox<>("Buscar producto o escanear código");
        searchBox.setItems(articuloServicel.getLista());
        searchBox.setWidthFull();
        searchBox.setClearButtonVisible(true);
        searchBox.focus();
        this.facturaDeVentaService = facturaDeVentaService;

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

        Button btnDevolucion = new Button("Devolución", new Icon(VaadinIcon.ABACUS));
        btnDevolucion.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);

// Evento de clic
        btnDevolucion.addClickListener(e -> abrirModalBuscarFactura());

        HorizontalLayout cajaButtonsLayout = new HorizontalLayout(btnAbrirCaja, btnMovimientoPos, btnCerrarCaja, btnDevolucion);
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

    private void abrirModalBuscarFactura() {
        
        Dialog dialogBuscar = new Dialog();
        dialogBuscar.setHeaderTitle("Buscar Factura para Devolución");
        dialogBuscar.setWidth("600px");

        TextField txtCodigo = new TextField("Código o Número de Factura");
        txtCodigo.setPlaceholder("Ej: F-0001");
        txtCodigo.setWidthFull();

        Button btnBuscar = new Button("Buscar", new Icon(VaadinIcon.SEARCH));
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout searchLayout = new HorizontalLayout(txtCodigo, btnBuscar);
        searchLayout.setAlignItems(FlexComponent.Alignment.END);
        searchLayout.setWidthFull();

        btnBuscar.addClickListener(e -> {
            String codigo = txtCodigo.getValue();
            if (codigo == null || codigo.trim().isEmpty()) {
                Notification.show("Ingrese un código de factura válido", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Buscar factura en la base de datos
            FacturaDeVenta factura = this.facturaDeVentaService.getFactura(Integer.parseInt(codigo.trim()));

            System.out.println("facturaOpt:"+factura.getAnulada());
            if (factura != null) {
           
                if (Boolean.TRUE.equals(factura.getAnulada())) {
                    Notification.show("La factura ya está totalmente anulada/devuelta.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                dialogBuscar.close();

                // Abrir el modal de selección de ítems (Modal 2)
                DialogoDevolucionVenta dialogoDev = new DialogoDevolucionVenta(
                        factura,
                        this.facturaDeVentaService,
                        "ADMIN", // Pasar usuario actual
                        () -> Notification.show("Devolución completada con éxito", 3000, Notification.Position.TOP_CENTER)
                );
                dialogoDev.open();

            } else {
                Notification.show("No se encontró ninguna factura con el código: " + codigo, 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        VerticalLayout content = new VerticalLayout(searchLayout);
        dialogBuscar.add(content);

        Button btnCerrar = new Button("Cancelar", e -> dialogBuscar.close());
        dialogBuscar.getFooter().add(btnCerrar);

        dialogBuscar.open();
    }
}
