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

    public EmpaqueSeleccionDialogo(Articulo articulo, 
                                  ArticuloEmpaqueService articuloEmpaqueService, 
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

        grid.addColumn(ArticuloEmpaque::getUnidadEmpaque)
                .setHeader("Cant. x Empaque")
                .setAutoWidth(true);

        grid.addColumn(ArticuloEmpaque::getPrecioCompra)
                .setHeader("Precio Compra")
                .setAutoWidth(true);

        // Cargar datos
        List<ArticuloEmpaque> empaques = articuloEmpaqueService.getPorArticulo(articulo.getCodigo());
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