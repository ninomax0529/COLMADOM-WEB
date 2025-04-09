/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.articulo;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.UnidadDeVenta;
import com.maxsoft.application.servicio.ArticuloDaoService;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.servicio.interfaces.UnidadDeVentaService;
import com.maxsoft.application.view.inventario.entrada.RegistroEntradaDeIventarioView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

@PageTitle("Consulata Artículo")
@Route(value = "inventario/consultaArticulo")

public class ConsultaArticuloView extends VerticalLayout {

    private final ArticuloService articuloService;
    private Grid<Articulo> grid = new Grid<>(Articulo.class);

    ArticuloDaoService articuloDaoService;

    private Articulo articuloActual;

    @Autowired
    public ConsultaArticuloView(ArticuloService articuloServiceArg, UnidadDeVentaService UnidadDeVentaServiceArg,
            ArticuloDaoService articuloDaoServiceArg) {

        this.articuloDaoService = articuloDaoServiceArg;
        this.articuloService = articuloServiceArg;
        setSizeFull();

        configurarGrid();

        add(grid);

    }

    private void configurarGrid() {

        grid.setColumns("codigo", "descripcion", "precioCompra",
                "precioVenta", "unidadDeVenta", "existencia");
        grid.setItems(articuloService.getLista());
        grid.setSizeFull();

        grid.addComponentColumn(articulo -> {
            Button editar = new Button("Editar", event -> agregar(articulo));

            return new HorizontalLayout(editar);
        }).setHeader("Agregar");

    }

    private void agregar(Articulo articulo) {

        this.articuloActual = articulo;
        this.articuloDaoService.setArticulo(articulo);

        UI.getCurrent().navigate(RegistroEntradaDeIventarioView.class);

    }

}
