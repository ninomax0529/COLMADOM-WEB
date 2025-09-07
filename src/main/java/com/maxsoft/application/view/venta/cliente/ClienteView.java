/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.cliente;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.servicio.interfaces.ClienteService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.FiltroAvanzado;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.router.PageTitle;
//import org.vaadin.lineawesome.LineAwesomeIconUrl;

//@PermitAll
@PageTitle("Clientes")
@Route( value = "venta/cliente")

public class ClienteView extends VerticalLayout {

    private final ClienteService clienteService;
    private final Grid<Cliente> grid = new Grid<>(Cliente.class, false);

    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    @Autowired
    public ClienteView(ClienteService clienteService) {
        this.clienteService = clienteService;
        setSizeFull();
        setSpacing(false);
      
        GridListDataView<Cliente> dataView = grid.setItems(clienteService.getLista());

// Crear el filtro avanzado
        FiltroAvanzado<Cliente> filtro = new FiltroAvanzado<>(dataView);

// Agregar filtros por nombre y cédula
        filtro.addFiltro("Nombre", Cliente::getNombre);
        filtro.addFiltro("Cédula", Cliente::getCedula);


//        add(new H3("Gestión de Clientes"));
        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            String key = NavigationContext.store(new Cliente());
            UI.getCurrent().navigate(RegistroClienteView.class, key);

        });

        configurarGrid();

        actualizarLista();

        add(botonera,filtro, grid);
    }

    private void configurarGrid() {

        grid.addColumn(Cliente::getCodigo).setHeader("Código").setAutoWidth(true);
        grid.addColumn(Cliente::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Cliente::getCedula).setHeader("Cédula").setAutoWidth(true);
        grid.addColumn(Cliente::getCelular).setHeader("Celular").setAutoWidth(true);
        grid.addColumn(Cliente::getDireccion).setHeader("Dirección").setAutoWidth(true);
        grid.addColumn(Cliente::getLugarDeTrabajo).setHeader("Lugar de Trabajo").setAutoWidth(true);

        grid.addColumn(cliente -> cliente.getHabilitado() ? "Sí" : "No").setHeader("Habilitado").setAutoWidth(true);

        grid.addComponentColumn(cliente -> {

            Button editar = new Button("Editar ", event -> {

                String key = NavigationContext.store(cliente);

                UI.getCurrent().navigate(RegistroClienteView.class, key);

            });

            return new HorizontalLayout(editar);
        }).setHeader("Acciones");

    }

    private void actualizarLista() {
        grid.setItems(clienteService.getLista());
    }

}
