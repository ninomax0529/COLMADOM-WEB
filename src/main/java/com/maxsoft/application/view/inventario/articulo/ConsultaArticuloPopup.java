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
import com.maxsoft.application.servicio.ArticuloDaoService;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.view.inventario.entrada.RegistroEntradaDeIventarioView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("consulta-articulo")
public class ConsultaArticuloPopup extends VerticalLayout {

    private final ArticuloService articuloService;
    private final ArticuloDaoService articuloDaoService;
    private final Grid<Articulo> grid = new Grid<>(Articulo.class);
    private final Dialog dialog = new Dialog();

    @Autowired
    public ConsultaArticuloPopup(ArticuloService articuloServiceArg, ArticuloDaoService articuloDaoServiceArg) {
        this.articuloService = articuloServiceArg;
        this.articuloDaoService = articuloDaoServiceArg;
        setSizeFull();

        configurarGrid();
        configurarDialogo();

        Button openPopupButton = new Button("Mostrar Artículos", event -> dialog.open());
        add(openPopupButton);
    }

    private void configurarGrid() {
        grid.setColumns("codigo", "descripcion", "precioCompra", "precioVenta", "unidadDeVenta", "existencia");
        grid.setItems(articuloService.getLista());
        grid.setSizeFull();

        grid.addComponentColumn(articulo -> {
            Button editar = new Button("Editar", event -> agregar(articulo));
            return new HorizontalLayout(editar);
        }).setHeader("Acciones");
    }

    private void configurarDialogo() {
        dialog.setHeaderTitle("Consulta de Artículos");
        dialog.add(grid);
        Button closeButton = new Button("Cerrar", event -> dialog.close());
        dialog.getFooter().add(closeButton);
    }

    private void agregar(Articulo articulo) {
        articuloDaoService.setArticulo(articulo);
        UI.getCurrent().navigate(RegistroEntradaDeIventarioView.class);
    }
}
