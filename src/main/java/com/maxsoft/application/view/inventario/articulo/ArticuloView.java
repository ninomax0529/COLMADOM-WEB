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
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.Key;
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
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Artículos")
@Route(value = "inventario/articulo")

public class ArticuloView extends VerticalLayout {

    private final ArticuloService articuloService;
    private final UnidadDeVentaService unidaService;
    private Grid<Articulo> grid = new Grid<>(Articulo.class);
    List<Articulo> listaArt = new ArrayList<>();
    private Binder<Articulo> binder = new Binder<>(Articulo.class);

    private TextField txtDescripcion = new TextField("Descripcion");
    private NumberField txtPrecioCompra = new NumberField("Precio de Compra Unitario");
    private NumberField txtPrecioVenta = new NumberField("Precio de Venta Unitario");
    private RadioButtonGroup<UnidadDeVenta> rdbGrupo = new RadioButtonGroup<>();
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);
    TextField txtBuscar = new TextField();

    RadioButtonGroup<String> categoriaGroup = new RadioButtonGroup<>();
    ArticuloDaoService articuloDaoService;

    private Articulo articuloActual;

    @Autowired
    public ArticuloView(ArticuloService articuloServiceArg, UnidadDeVentaService UnidadDeVentaServiceArg,
            ArticuloDaoService articuloDaoServiceArg) {

        this.articuloDaoService = articuloDaoServiceArg;

        this.articuloService = articuloServiceArg;
        this.unidaService = UnidadDeVentaServiceArg;
        setSizeFull();
        setSpacing(false);

        txtBuscar.focus();

        txtBuscar.addKeyPressListener(Key.ENTER, e -> {

            Articulo selected = grid.asSingleSelect().getValue();

            if (selected != null) {

            } else {
                System.out.println(" es  null ");

            }

        });

        add(botonera, txtBuscar, grid); // parte superior

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            String key = NavigationContext.store(new Articulo());

            UI.getCurrent().navigate(RegistrarArticuloView.class, key);

        });

        // Grupo de botones de radio para seleccionar categoría
        configurarGrid();
        GridListDataView<Articulo> dataView = grid.setItems();

        txtBuscar.setWidth("100%");
        txtBuscar.setPlaceholder("Buscar");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
        txtBuscar.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(selectArticulo -> {

            String searchTerm = txtBuscar.getValue().trim();

            if (searchTerm.isEmpty()) {
                System.out.println("Ejemplo");
                return true;
            }

            boolean matchesDescripcion = matchesTerm(selectArticulo.getDescripcion(),
                    searchTerm);
            boolean matchesCodigo = matchesTerm(selectArticulo.getCodigo().toString(), searchTerm);
            grid.select(selectArticulo);

            return matchesDescripcion || matchesCodigo;
        });

//        configurarFormulario();
//        actualizarLista();
    }

    private void configurarGrid() {

        grid.setColumns("codigo", "descripcion", "precioCompra",
                "precioVenta", "unidadDeVenta", "existencia");
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addComponentColumn(articulo -> {

            Button editar = new Button("Editar", event -> {

                String key = NavigationContext.store(articulo);

                UI.getCurrent().navigate(RegistrarArticuloView.class, key);

            });

            return new HorizontalLayout(editar);
        }).setHeader("Acciones");

    }

    private void editarArticulo(Articulo articulo) {
        this.articuloActual = articulo;
        this.articuloDaoService.setArticulo(articulo);
        binder.setBean(articulo);
    }

    private void nuevoArticulo() {
        System.out.println(" categoriaGroup.getValue() " + categoriaGroup.getValue());
        editarArticulo(new Articulo());
    }

    private void actualizarLista() {

        listaArt.clear();

        listaArt = articuloService.getLista();
//        GridListDataView<Articulo> dataView = grid.setItems(listArt);

        listaArt.addAll(listaArt);

        grid.setItems(listaArt);
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

}
