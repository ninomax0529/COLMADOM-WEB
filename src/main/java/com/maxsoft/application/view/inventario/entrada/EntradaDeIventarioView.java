/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.entrada;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.servicio.interfaces.EntradaDeInventarioService;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.tabs.TabSheet;
import java.util.List;

@PageTitle("Entrada de Inventario")
@Route(value = "inventario/entrada")

public class EntradaDeIventarioView extends VerticalLayout {

    private final EntradaDeInventarioService entradaService;
    private final Grid<EntradaInventario> grid = new Grid<>(EntradaInventario.class, false);
    private Grid<DetalleEntradaInventario> gridDetalle = new Grid<>(DetalleEntradaInventario.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    List<DetalleEntradaInventario> listDet;

    private TextField nombre = new TextField("Nombre");
    private NumberField precio = new NumberField("Precio");
    private NumberField stock = new NumberField("Stock");

    private Button guardar = new Button("Guardar");
    private Button cancelar = new Button("Cancelar");
    private Button nuevo = new Button("Nuevo");

    private Articulo articuloActual;
    List<EntradaInventario> listaEntInv;

    @Autowired
    public EntradaDeIventarioView(EntradaDeInventarioService entradaServiceArg) {

        setSizeFull();
        setSpacing(false);

        this.entradaService = entradaServiceArg;
        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();

        tabs.add("Entrada", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

          UI.getCurrent().navigate(RegistroEntradaDeIventarioView.class);

        });


        grid.addItemDoubleClickListener(c -> {

            listDet = this.entradaService.getDetalle(c.getItem().getCodigo());
            gridDetalle.setItems(listDet);
            tabs.setSelectedIndex(1);
            tabs.getTabAt(1).setLabel("Detalle( Entrada -> " + c.getItem().getCodigo() + " )");

        });

        configurarGrid();
        configurarGridDetalle();

        add(botonera, tabs);

    }

    private void configurarGrid() {

        listaEntInv = this.entradaService.getLista();

        grid.addColumn(EntradaInventario::getCodigo).setHeader("Codigo")
                .setFooter("TOTAL ENTRADA:");

        grid.addColumn(EntradaInventario::getFecha)
                .setHeader("Fecha")
                .setKey("fecha");

        grid.addColumn(EntradaInventario::getFechaCreacion)
                .setHeader("Fecha Creacion")
                .setKey("fechaCreacion");

        grid.addColumn(EntradaInventario::getNombreUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        grid.setItems(listaEntInv);
        grid.setSizeFull();

    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

        gridDetalle.addColumn(DetalleEntradaInventario::getDescripcionArticulo).setHeader("Articulo")
                .setFooter("TOTAL ARTICULOS:");

        gridDetalle.addColumn(DetalleEntradaInventario::getDescripcionArticulo)
                .setHeader("Descripcion")
                .setKey("descripcion");

        gridDetalle.addColumn(DetalleEntradaInventario::getCantidadRecibida)
                .setHeader("Cantidad")
                .setKey("cantidad");

        gridDetalle.addColumn(DetalleEntradaInventario::getNuevaExistencia)
                .setHeader("Existencia")
                .setKey("existencia");

        gridDetalle.addColumn(DetalleEntradaInventario::getExistenciaActual)
                .setHeader("Nueva Existencia")
                .setKey("nuevaexistencia");

        gridDetalle.addColumn(DetalleEntradaInventario::getNombreUnidad)
                .setHeader("Unidad")
                .setKey("unidad");

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

    }

}
