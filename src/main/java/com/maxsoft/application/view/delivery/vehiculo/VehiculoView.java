/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.delivery.vehiculo;

import com.maxsoft.application.view.delivery.gestiondelivey.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.Vehiculo;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.delivery.VehiculoService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.FiltroAvanzado;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.PageTitle;
//import org.vaadin.lineawesome.LineAwesomeIconUrl;

//@PermitAll
@PageTitle("Gestion Vehiculo")
@Route(value = "delivery/gestionDeVehiculo")

public class VehiculoView extends VerticalLayout {

    private final VehiculoService vehiculoService;
    private final Grid<Vehiculo> grid = new Grid<>(Vehiculo.class, false);

    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    @Autowired
    public VehiculoView(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
        setSizeFull();
        setSpacing(false);

        GridListDataView<Vehiculo> dataView = grid.setItems(vehiculoService.getLista());

// Crear el filtro avanzado
        FiltroAvanzado<Vehiculo> filtro = new FiltroAvanzado<>(dataView);

// Agregar filtros por nombre y cédula
        filtro.addFiltro("Nombre", Vehiculo::getNombre);
        filtro.addFiltro("Placa", Vehiculo::getPlaca);

//        add(new H3("Gestión de Clientes"));
        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            String key = NavigationContext.store(new Delivery());
            UI.getCurrent().navigate(RegistroVehiculoView.class, key);

        });

        configurarGrid();

        actualizarLista();

        add(botonera,
                new H2("Gestion Vehiculo"),
                filtro, grid);
    }

    private void configurarGrid() {

        grid.addColumn(Vehiculo::getCodigo).setHeader("Código").setAutoWidth(true);
        grid.addColumn(Vehiculo::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Vehiculo::getPlaca).setHeader("Placa").setAutoWidth(true);
        grid.addColumn(Vehiculo::getFechaCreacion).setHeader("Fecha Registro").setAutoWidth(true);
    
        grid.addColumn(o -> o.getHabilitado() ? "Sí" : "No").setHeader("Habilitado").setAutoWidth(true);

        grid.addComponentColumn(delivery -> {

            Button editar = new Button("Editar ", event -> {

                String key = NavigationContext.store(delivery);

                UI.getCurrent().navigate(RegistroVehiculoView.class, key);

            });

            return new HorizontalLayout(editar);
        }).setHeader("Acciones");

    }

    private void actualizarLista() {
        grid.setItems(vehiculoService.getLista());
    }

}
