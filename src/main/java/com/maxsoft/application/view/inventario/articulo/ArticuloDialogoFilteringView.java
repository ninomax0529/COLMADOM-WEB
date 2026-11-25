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
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.view.componente.FiltroDataView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.router.Route;

import java.util.List;

//@Route("grid-column-filtering")
@Route(value = "consultaArticulos")
public final class ArticuloDialogoFilteringView extends Dialog {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArticuloDialogoFilteringView.class);


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

                log.debug("no es null");
                dialogCallback.onConfirm(getArticulo());

                close();

            } else {
                log.debug(" es  null ");

                close();
            }
        });

        grid.addColumn(Articulo::getCodigo).setHeader("Codigo");
        grid.addColumn(Articulo::getDescripcion).setHeader("Descripcion");

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

        add(filtroDataView.getTxtBuscar(), grid);
    }

    /**
     * Oculta los artículos deshabilitados (dados de baja). Para compras,
     * órdenes de compra y recepción; los ajustes, mermas y traslados siguen
     * viéndolos porque pueden tener existencia que corregir.
     */
    public ArticuloDialogoFilteringView soloHabilitados() {
        dataView.addFilter(Articulo::getHabilitado);
        return this;
    }

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
