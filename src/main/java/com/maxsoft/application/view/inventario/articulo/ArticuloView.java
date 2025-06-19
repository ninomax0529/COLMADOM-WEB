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
import com.maxsoft.application.servicio.interfaces.UnidadDeVentaService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Artículos")
@Route(value = "inventario/articulo")

public class ArticuloView extends VerticalLayout {

     ArticuloService articuloService;

    Grid<Articulo> grid = new Grid<>(Articulo.class, false);
    List<Articulo> listaArt = new ArrayList<>();
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);
    TextField txtFiltroArt = new TextField();

    @Autowired
    public ArticuloView(ArticuloService articuloServiceArg) {


        this.articuloService = articuloServiceArg;
      
        setSizeFull();
        setSpacing(false);

        add(botonera, txtFiltroArt, grid); // parte superior

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            String key = NavigationContext.store(new Articulo());

            UI.getCurrent().navigate(RegistrarArticuloView.class, key);

        });

        // Grupo de botones de radio para seleccionar categoría
        configurarGrid();
        configurarFiltros();

    }

    private void configurarGrid() {

        grid.addColumn(Articulo::getCodigo).setHeader("Código").setAutoWidth(true);
        grid.addColumn(Articulo::getDescripcion).setHeader("Descripcion").setAutoWidth(true);
        grid.addColumn(Articulo::getPrecioCompra).setHeader("Precio.Compra").setAutoWidth(true);
        grid.addColumn(Articulo::getPrecioVenta).setHeader("Precio.Venta").setAutoWidth(true);
        grid.addColumn(Articulo::getUnidadDeVenta).setHeader("Unidad").setAutoWidth(true);

        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addComponentColumn(articulo -> {

            Button editar = new Button("Editar ", event -> {

                String key = NavigationContext.store(articulo);

                UI.getCurrent().navigate(RegistrarArticuloView.class, key);

            });

            return new HorizontalLayout(editar);
        }).setHeader("Acciones");

        listaArt = articuloService.getLista();

        grid.setItems(listaArt);

    }

    private void configurarFiltros() {

        GridListDataView<Articulo> dataView = grid.setItems(listaArt);

        txtFiltroArt.focus();
        txtFiltroArt.setWidth("100%");
        txtFiltroArt.setPlaceholder("Buscar");
        txtFiltroArt.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtFiltroArt.setValueChangeMode(ValueChangeMode.EAGER);
        txtFiltroArt.addValueChangeListener(e -> dataView.refreshAll());
//        txtFiltroArt.addValueChangeListener(e -> aplicarFiltros());

        dataView.addFilter(art -> {

            String searchTerm = txtFiltroArt.getValue().trim();

            if (searchTerm.isEmpty()) {
                return true;
            }

            boolean descrip = matchesTerm(art.getDescripcion(),
                    searchTerm);

            boolean codigo = matchesTerm(art.getCodigo().toString(), searchTerm);

            return descrip || codigo;
        });
    }

    private void aplicarFiltros() {

        String nombreFiltro = txtFiltroArt.getValue().trim().toLowerCase();

        List<Articulo> filtrados = listaArt.stream()
                .filter(art
                        -> (nombreFiltro.isEmpty() || art.getDescripcion().toLowerCase().contains(nombreFiltro))
                )
                .collect(Collectors.toList());

        grid.setItems(filtrados);
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
