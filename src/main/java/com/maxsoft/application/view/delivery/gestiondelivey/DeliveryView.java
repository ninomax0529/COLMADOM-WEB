/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.delivery.gestiondelivey;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.FiltroAvanzado;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.PageTitle;
//import org.vaadin.lineawesome.LineAwesomeIconUrl;

//@PermitAll
@PageTitle("Gestion Delivery")
@Route(value = "delivery/gestionDelivery")

public class DeliveryView extends VerticalLayout {

    private final DeliveryService deliveryservice;
    private final Grid<Delivery> grid = new Grid<>(Delivery.class, false);

    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    @Autowired
    public DeliveryView(DeliveryService deliveryservice) {
        this.deliveryservice = deliveryservice;
        setSizeFull();
        setSpacing(false);

        GridListDataView<Delivery> dataView = grid.setItems(deliveryservice.getLista());

// Crear el filtro avanzado
        FiltroAvanzado<Delivery> filtro = new FiltroAvanzado<>(dataView);

// Agregar filtros por nombre y cédula
        filtro.addFiltro("Nombre", Delivery::getNombre);
        filtro.addFiltro("Célular", Delivery::getCelular);

//        add(new H3("Gestión de Clientes"));
        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            String key = NavigationContext.store(new Delivery());
            UI.getCurrent().navigate(RegistroDeliveryView.class, key);

        });

        configurarGrid();

        actualizarLista();

        add(botonera,
                new H2("Gestion Delivery"),
                filtro, grid);
    }

    private void configurarGrid() {

        grid.addColumn(Delivery::getCodigo).setHeader("Código").setAutoWidth(true);
        grid.addColumn(Delivery::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Delivery::getCedula).setHeader("Cédula").setAutoWidth(true);
        grid.addColumn(Delivery::getCelular).setHeader("Celular").setAutoWidth(true);
        grid.addColumn(Delivery::getDireccion).setHeader("Dirección").setAutoWidth(true);
        grid.addColumn(Delivery::getVehiculo).setHeader("Vehiculo").setAutoWidth(true);

        grid.addColumn(cliente -> cliente.getHabilitado() ? "Sí" : "No").setHeader("Habilitado").setAutoWidth(true);

        grid.addComponentColumn(delivery -> {

            Button editar = new Button("Editar ", event -> {

                String key = NavigationContext.store(delivery);

                UI.getCurrent().navigate(RegistroDeliveryView.class, key);

            });

            return new HorizontalLayout(editar);
        }).setHeader("Acciones");

    }

    private void actualizarLista() {
        grid.setItems(deliveryservice.getLista());
    }

}
