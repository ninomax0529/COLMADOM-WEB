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
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Consulta Articulos")
@Route("consulta-articulos")
public class ConsultaArticuloDgView extends Dialog {
    

    private Grid<Articulo> grid;
    private TextField searchField;
    private Button closeButton;

    private Articulo articulo;

    @Autowired
    ArticuloService articuloService;
    private final List<Articulo> listaArticulos = new ArrayList<>();
    private final ListDataProvider<Articulo> dataProvider;

    List<Articulo> articulos;
      private Timer filtroTimer;  // Temporizador para manejar el debounce

    public ConsultaArticuloDgView(ArticuloService articuloServiceArg, DialogCallback dialogCallback) {

        this.articuloService = articuloServiceArg;
        setWidth("800px");
        setHeight("400px");
        setHeaderTitle("Consulta de Artículos");
        setModal(true); // Hace que el diálogo sea modal
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        dataProvider = new ListDataProvider<>(listaArticulos);
        searchField = new TextField("Buscar");
        searchField.setPlaceholder("Ingrese nombre o código...");

        searchField.addValueChangeListener(e -> actualizarLista(articuloServiceArg, e.getValue()));

        grid = new Grid<>(Articulo.class, false);
        grid.addColumn(Articulo::getCodigo).setHeader("Código");
        grid.addColumn(Articulo::getDescripcion).setHeader("Descripcion");
        grid.addColumn(Articulo::getPrecioCompra).setHeader("Precio");
        grid.setDataProvider(dataProvider);

        actualizarLista(articuloService, "");

        closeButton = new Button("Aceptar", e -> {

//            Set<Articulo> selectedItems = grid.getSelectedItems();
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

        // Campo de filtro
        TextField filtro = new TextField("Buscar artículo");
        
        filtro.setPlaceholder("Escriba para filtrar...");
        filtro.setClearButtonVisible(true);  // Muestra un botón para limpiar el filtro
        filtro.addValueChangeListener(event -> {
            
              // Detener el temporizador anterior, si existe
            if (filtroTimer != null) {
                filtroTimer.cancel();
            }

            
            String filtroTexto = event.getValue().toLowerCase();
            dataProvider.setFilter(Articulo::getDescripcion, nombre -> nombre.toLowerCase().contains(filtroTexto));
        });
        
        closeButton.addClickShortcut(Key.ENTER);
   
        // Configura un pequeño retraso para evitar filtros innecesarios
//        filtro.set(300);  // 300 ms de espera para mejorar rendimiento


        VerticalLayout layout = new VerticalLayout(filtro, closeButton, grid);
        add(layout);
    }

    private void actualizarLista(ArticuloService articuloService, String filtro) {

        listaArticulos.addAll(articuloService.getLista());
//        grid.setItems(articulos);
    }

    public interface DialogCallback {

        void onConfirm(Articulo input);
    }

    /**
     * @return the articulo
     */
    public Articulo getArticulo() {
        return articulo;
    }

    /**
     * @param articulo the articulo to set
     */
    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }
}