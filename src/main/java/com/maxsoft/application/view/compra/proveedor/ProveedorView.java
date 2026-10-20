/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.compra.proveedor;

import com.maxsoft.application.view.venta.cliente.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.FiltroAvanzado;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.PageTitle;
//import org.vaadin.lineawesome.LineAwesomeIconUrl;

//@PermitAll
@PageTitle("Proveedor")
@Route(value = "compra/proveedor")

public class ProveedorView extends VerticalLayout {

    private final ProveedorService proveedorService;
    private final Grid<Proveedor> grid = new Grid<>(Proveedor.class, false);

    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    @Autowired
    public ProveedorView(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
        setSizeFull();
        setSpacing(false);

        GridListDataView<Proveedor> dataView = grid.setItems(proveedorService.getLista());

// Crear el filtro avanzado
        FiltroAvanzado<Proveedor> filtro = new FiltroAvanzado<>(dataView);

// Agregar filtros por nombre y cédula
        filtro.addFiltro("Nombre", Proveedor::getNombre);
        filtro.addFiltro("Cédula", Proveedor::getRnc);

//        add(new H3("Gestión de Clientes"));
        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            String key = NavigationContext.store(new Proveedor());
            UI.getCurrent().navigate(RegistroProveedorView.class, key);

        });

        configurarGrid();

        actualizarLista();

        add(botonera,
                new H2("Proveedores"),
                filtro, grid);
    }

    private void configurarGrid() {

        grid.addColumn(Proveedor::getCodigo).setHeader("Código").setAutoWidth(true);
        grid.addColumn(Proveedor::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Proveedor::getRnc).setHeader("Cédula").setAutoWidth(true);
        grid.addColumn(Proveedor::getCelular).setHeader("Celular").setAutoWidth(true);
        grid.addColumn(Proveedor::getDireccion).setHeader("Dirección").setAutoWidth(true);
        grid.addColumn(Proveedor::getFechaCracion).setHeader("Fecha Creacion").setAutoWidth(true);

        grid.addColumn(proveedor -> proveedor.getHabilitado() ? "Sí" : "No").setHeader("Habilitado").setAutoWidth(true);

        grid.addComponentColumn(proveedor -> {

            Button editar = new Button("Editar ", event -> {

                String key = NavigationContext.store(proveedor);

                UI.getCurrent().navigate(RegistroProveedorView.class, key);

            });

            return new HorizontalLayout(editar);
        }).setHeader("Acciones");

    }

    private void actualizarLista() {
        grid.setItems(proveedorService.getLista());
    }

}
