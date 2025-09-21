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
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.view.componente.FiltroDataView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.List;

//@Route("grid-column-filtering")
@Route(value = "consultaArticulos")
public final class ArticuloDialogoFilteringView extends Dialog {

    /**
     * @return the articulo
     */
    private final ArticuloService articuloService;

    private Articulo articulo;

    List<Articulo> listArt;
    Grid<Articulo> grid = new Grid<>(Articulo.class, false);
    GridListDataView<Articulo> dataView;
    FiltroDataView<Articulo> filtroDataView;

//    TextField txtBuscar = new TextField();
    public ArticuloDialogoFilteringView(ArticuloService articuloService, DialogCallback dialogCallback) {

        this.articuloService = articuloService;

//        txtBuscar.focus();
        setSizeFull();
        listArt = articuloService.getLista();

        setHeaderTitle("Consulta de Artículos");

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        setArticulo(new Articulo());
        dataView = grid.setItems(listArt);
        filtroDataView = new FiltroDataView<Articulo>(dataView);

        filtroDataView.getTxtBuscar().addKeyPressListener(Key.ENTER, e -> {

            Articulo selected = grid.asSingleSelect().getValue();

            if (selected != null) {

                setArticulo(grid.getSelectionModel().getFirstSelectedItem().get());

                System.out.println("no es null");
                dialogCallback.onConfirm(getArticulo());

                close();

            } else {
                System.out.println(" es  null ");

                close();
            }
        });

        grid.addColumn(Articulo::getCodigo).setHeader("Codigo");
        grid.addColumn(Articulo::getDescripcion).setHeader("Descripcion");

//        txtBuscar.setWidth("50%");
//        txtBuscar.setPlaceholder("Buscar");
//        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
//        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
//        txtBuscar.addValueChangeListener(e -> dataView.refreshAll());
        dataView.addFilter(selectArticulo -> {

            String searchTerm = filtroDataView.getTxtBuscar().getValue().trim();

            if (searchTerm.isEmpty()) {
                return true;
            }

            boolean matchesDescripcion = filtroDataView.matchesTerm(selectArticulo.getDescripcion(),
                    searchTerm);

            boolean matchesCodigo = filtroDataView.matchesTerm(selectArticulo.getCodigo().toString(), searchTerm);
            grid.select(selectArticulo);

            return matchesDescripcion || matchesCodigo;
        });

//        VerticalLayout layout = new VerticalLayout(grid);
//        layout.setSizeFull();
//        layout.setPadding(false);
        add(filtroDataView.getTxtBuscar(), grid);
    }

//    private boolean matchesTerm(String value, String searchTerm) {
//        return value.toLowerCase().contains(searchTerm.toLowerCase());
//    }
    public Articulo getArticulo() {
        return articulo;
    }

    /**
     * @param articulo the articulo to set
     */
    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public interface DialogCallback {

        void onConfirm(Articulo input);
    }

}
