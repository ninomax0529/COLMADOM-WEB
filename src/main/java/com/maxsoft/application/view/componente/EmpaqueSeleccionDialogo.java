/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;


import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;

public class EmpaqueSeleccionDialogo extends Dialog {

    private final Grid<ArticuloEmpaque> grid = new Grid<>(ArticuloEmpaque.class, false);

    /**
     * Elige la presentación del artículo: si tiene una sola, la usa directo
     * (sin abrir el diálogo); si tiene varias, abre el diálogo; si no tiene
     * ninguna, avisa.
     */
    public static void seleccionar(Articulo articulo,
            ArticuloEmpaqueService articuloEmpaqueService,
            Consumer<ArticuloEmpaque> onSeleccionarListener) {
        seleccionar(articulo, articuloEmpaqueService, ae -> true, "", onSeleccionarListener);
    }

    /**
     * Igual que {@link #seleccionar} pero solo con las presentaciones marcadas
     * "se compra en" (recepción de mercancía, compras).
     */
    public static void seleccionarParaCompra(Articulo articulo,
            ArticuloEmpaqueService articuloEmpaqueService,
            Consumer<ArticuloEmpaque> onSeleccionarListener) {
        seleccionar(articulo, articuloEmpaqueService,
                ae -> Boolean.TRUE.equals(ae.getSeCompraEn()), " de compra", onSeleccionarListener);
    }

    private static void seleccionar(Articulo articulo,
            ArticuloEmpaqueService articuloEmpaqueService,
            java.util.function.Predicate<ArticuloEmpaque> filtro,
            String tipo,
            Consumer<ArticuloEmpaque> onSeleccionarListener) {

        List<ArticuloEmpaque> todos = articuloEmpaqueService.getPorArticulo(articulo.getCodigo());
        List<ArticuloEmpaque> empaques = todos == null ? List.of() : todos.stream().filter(filtro).toList();

        if (empaques.isEmpty()) {
            com.vaadin.flow.component.notification.Notification.show(
                    "El artículo " + articulo.getDescripcion() + " no tiene presentaciones" + tipo
                    + " configuradas. Márquelas en la ficha del artículo.",
                    3000, com.vaadin.flow.component.notification.Notification.Position.MIDDLE)
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_WARNING);
            return;
        }

        if (empaques.size() == 1) {
            onSeleccionarListener.accept(empaques.get(0));
            return;
        }

        new EmpaqueSeleccionDialogo(articulo, empaques, onSeleccionarListener).open();
    }

    public EmpaqueSeleccionDialogo(Articulo articulo,
                                  ArticuloEmpaqueService articuloEmpaqueService,
                                  Consumer<ArticuloEmpaque> onSeleccionarListener) {
        this(articulo, articuloEmpaqueService.getPorArticulo(articulo.getCodigo()), onSeleccionarListener);
    }

    private EmpaqueSeleccionDialogo(Articulo articulo,
                                  List<ArticuloEmpaque> empaques,
                                  Consumer<ArticuloEmpaque> onSeleccionarListener) {

        setHeaderTitle("Seleccionar Empaque / Presentación");
        setWidth("550px");
        setHeight("400px");

        Span lblArticulo = new Span("Artículo: " + (articulo != null ? articulo.getDescripcion() : ""));
        lblArticulo.getStyle().set("font-weight", "bold");

        // Configurar Grid de empaques
        grid.addColumn(ae -> ae.getUnidadEmpaque()!= null ? ae.getUnidadEmpaque().getDescripcion(): "Sin descripción")
                .setHeader("Empaque")
                .setAutoWidth(true);

        // Unidades base que trae cada empaque (antes mostraba el objeto Unidad)
        grid.addColumn(ae -> ae.getFactorConversion() != null
                ? ae.getFactorConversion().stripTrailingZeros().toPlainString() : "1")
                .setHeader("Cant. x Empaque")
                .setAutoWidth(true);

        grid.addColumn(ArticuloEmpaque::getPrecioCompra)
                .setHeader("Precio Compra")
                .setAutoWidth(true);

        // Cargar datos
        grid.setItems(empaques);

        // Selección por doble clic
        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() != null) {
                onSeleccionarListener.accept(e.getItem());
                close();
            }
        });

        // Botones
        Button btnAceptar = new Button("Seleccionar", e -> {
            ArticuloEmpaque seleccionado = grid.asSingleSelect().getValue();
            if (seleccionado != null) {
                onSeleccionarListener.accept(seleccionado);
                close();
            }
        });
        btnAceptar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> close());

        HorizontalLayout footer = new HorizontalLayout(btnCancelar, btnAceptar);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();

        VerticalLayout layout = new VerticalLayout(lblArticulo, grid);
        layout.setSizeFull();

        add(layout);
        getFooter().add(footer);
    }
}