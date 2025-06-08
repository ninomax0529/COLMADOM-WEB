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

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

@PageTitle("Registrar Artículos")
@Route(value = "inventario/registrarArticulo")

public class RegistrarArticuloView extends VerticalLayout implements HasUrlParameter<String> {

    private final ArticuloService articuloService;
    private final UnidadDeVentaService unidaService;
    private Binder<Articulo> binder = new Binder<>(Articulo.class);

    private TextField txtDescripcion = new TextField("Descripcion");
    private NumberField txtPrecioCompra = new NumberField("Precio de Compra Unitario");
    private NumberField txtPrecioVenta = new NumberField("Precio de Venta Unitario");
    private IntegerField txtCodigo = new IntegerField("Codigo");
    private RadioButtonGroup<UnidadDeVenta> rdbGrupo = new RadioButtonGroup<>();
    ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    FormLayout formLayout = new FormLayout();

    RadioButtonGroup<String> categoriaGroup = new RadioButtonGroup<>();
    ArticuloDaoService articuloDaoService;

    private Articulo articuloActual;

    @Autowired
    public RegistrarArticuloView(ArticuloService articuloServiceArg, UnidadDeVentaService UnidadDeVentaServiceArg,
            ArticuloDaoService articuloDaoServiceArg) {

        this.articuloDaoService = articuloDaoServiceArg;

        this.articuloService = articuloServiceArg;
        this.unidaService = UnidadDeVentaServiceArg;
        setSizeFull();
        setSpacing(false);

        add(botonera, formLayout); // parte superior

        botonera.getGuardar().addClickListener(e -> {
            // lógica de guardar
            guardarArticulo();
        });

        botonera.getCancelar().addClickListener(e -> {
            // lógica de cancelar
            UI.getCurrent().navigate(ArticuloView.class);
        });

        // Grupo de botones de radio para seleccionar categoría
        rdbGrupo.setLabel("Se Vende por :");
        rdbGrupo.setItems(unidaService.getLista());

        configurarFormulario();

    }

    private void configurarFormulario() {

        txtCodigo.setEnabled(false);
        txtCodigo.setWidth("100px");
        HorizontalLayout hbPrecio = new HorizontalLayout(txtPrecioCompra, txtPrecioVenta);
        HorizontalLayout hlArt = new HorizontalLayout(txtCodigo, txtDescripcion);
        hlArt.addAndExpand(txtDescripcion);

        formLayout = new FormLayout(hlArt, hbPrecio, rdbGrupo);

        binder.bind(txtCodigo, Articulo::getCodigo, Articulo::setCodigo);
        binder.bind(txtDescripcion, Articulo::getDescripcion, Articulo::setDescripcion);
        binder.bind(txtPrecioCompra, Articulo::getPrecioCompra, Articulo::setPrecioCompra);
        binder.bind(txtPrecioVenta, Articulo::getPrecioVenta, Articulo::setPrecioVenta);
        binder.bind(rdbGrupo, Articulo::getUnidadDeVenta, Articulo::setUnidadDeVenta);

//        botonera.getGuardar().addClickListener(event -> guardarArticulo());
//        botonera.getCancelar().addClickListener(event -> limpiarFormulario());
        add(formLayout);

    }

    private void editarArticulo(Articulo articulo) {
        this.articuloActual = articulo;
        this.articuloDaoService.setArticulo(articulo);
        binder.setBean(articulo);
    }

    private void guardarArticulo() {

        articuloActual.setExistencia(0.00);
        articuloService.guardar(articuloActual);
        Notification.show("Artículo guardado", 2000, Notification.Position.TOP_CENTER);
        articuloActual = new Articulo();
//        limpiarFormulario();

    }

    private void limpiarFormulario() {
        editarArticulo(new Articulo());
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {

        Articulo entidad = NavigationContext.retrieve(parameter, Articulo.class);
        if (entidad != null) {
            System.out.println("Art :" + entidad);
            editarArticulo(entidad);
        } else {
            System.out.println("Error Art :" + entidad);
            // manejar error si no se encuentra
        }
    }
}
