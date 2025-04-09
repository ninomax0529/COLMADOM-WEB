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

@PageTitle("Artículos")
@Route(value = "inventario/articulo")

public class ArticuloView extends VerticalLayout {

    private final ArticuloService articuloService;
    private final UnidadDeVentaService unidaService;
    private Grid<Articulo> grid = new Grid<>(Articulo.class);
    private Binder<Articulo> binder = new Binder<>(Articulo.class);

    private TextField txtDescripcion = new TextField("Descripcion");
    private NumberField txtPrecioCompra = new NumberField("Precio de Compra Unitario");
    private NumberField txtPrecioVenta = new NumberField("Precio de Venta Unitario");
    private RadioButtonGroup<UnidadDeVenta> rdbGrupo = new RadioButtonGroup<>();

    private Button guardar = new Button("Guardar");
    private Button cancelar = new Button("Cancelar");
    private Button nuevo = new Button("Nuevo");
    RadioButtonGroup<String> categoriaGroup = new RadioButtonGroup<>();
    ArticuloDaoService articuloDaoService;

    private Articulo articuloActual;

    @Autowired
    public ArticuloView(ArticuloService articuloServiceArg,UnidadDeVentaService UnidadDeVentaServiceArg
    , ArticuloDaoService articuloDaoServiceArg  ) {
        
        this.articuloDaoService=articuloDaoServiceArg;
        
        this.articuloService = articuloServiceArg;
        this.unidaService=UnidadDeVentaServiceArg;
        setSizeFull();

        // Grupo de botones de radio para seleccionar categoría
        rdbGrupo.setLabel("Se Vende por :");
        rdbGrupo.setItems(unidaService.getLista());

        configurarGrid();
        configurarFormulario();
      

        add( grid);
        actualizarLista();
    }

    private void configurarGrid() {

        grid.setColumns("codigo", "descripcion", "precioCompra"
                , "precioVenta","unidadDeVenta","existencia");
        grid.setSizeFull();

        grid.addComponentColumn(articulo -> {
            Button editar = new Button("Editar", event -> editarArticulo(articulo));

            return new HorizontalLayout(editar);
        }).setHeader("Acciones");

        nuevo.addClickListener(event -> nuevoArticulo());
    }

    private void configurarFormulario() {

        HorizontalLayout hbPrecio = new HorizontalLayout(txtPrecioCompra, txtPrecioVenta);

        FormLayout formLayout = new FormLayout(txtDescripcion, hbPrecio, rdbGrupo);
        HorizontalLayout botones = new HorizontalLayout(guardar, nuevo, cancelar);

        add(formLayout, botones);

        binder.bind(txtDescripcion, Articulo::getDescripcion, Articulo::setDescripcion);
        binder.bind(txtPrecioCompra, Articulo::getPrecioCompra, Articulo::setPrecioCompra);
        binder.bind(txtPrecioVenta, Articulo::getPrecioVenta, Articulo::setPrecioVenta);
        binder.bind(rdbGrupo, Articulo::getUnidadDeVenta, Articulo::setUnidadDeVenta);
        
        guardar.addClickListener(event -> guardarArticulo());
        cancelar.addClickListener(event -> limpiarFormulario());

        limpiarFormulario();
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

    private void guardarArticulo() {
        articuloActual.setExistencia(0.00);
        articuloService.guardar(articuloActual);
        Notification.show("Artículo guardado");
        limpiarFormulario();
        actualizarLista();
    }

    private void eliminarArticulo(Articulo articulo) {
        articuloService.eliminarArticulo(articulo.getCodigo());
        Notification.show("Artículo eliminado");
        actualizarLista();
    }
    private void actualizarLista() {
        grid.setItems(articuloService.getLista());
    }

    private void limpiarFormulario() {
        editarArticulo(new Articulo());
    }
}
