/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.cliente;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import com.vaadin.flow.component.notification.Notification;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.servicio.interfaces.ClienteService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;


@PageTitle("Registro de Clientes")
@Route(value ="venta/RegistroCliente")
public class RegistroClienteView extends VerticalLayout implements HasUrlParameter<String> {

    private final ClienteService clienteService;

    private final Binder<Cliente> binder = new Binder<>(Cliente.class);
    private final TextField codigo = new TextField("Codigo");
    private final TextField nombre = new TextField("Nombre y Apellidos");
    private final TextField cedula = new TextField("No.Cédula");
    private final TextField celular = new TextField("No.Celular");
    private final TextField direccion = new TextField("Dirección");
    private final TextField lugarDeTrabajo = new TextField("Lugar de Trabajo");
    private final TextField referencia = new TextField("Referencia");
    private final Checkbox habilitado = new Checkbox("Habilitado", true);

    ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Cliente clienteActual = new Cliente();

    @Autowired
    public RegistroClienteView(ClienteService clienteService) {
        
        this.clienteService = clienteService;
             
        add(botonera);

        configurarFormulario();

        botonera.getGuardar().addClickListener(e -> {
            // lógica de guardar
            guardar();
        });

        botonera.getCancelar().addClickListener(e -> {
            // lógica de cancelar
            UI.getCurrent().navigate(ClienteView.class);
        });

    }

    private void configurarFormulario() {

        FormLayout form = new FormLayout();

        codigo.setEnabled(false);
        form.add(codigo, habilitado, nombre, cedula, celular, direccion, lugarDeTrabajo, referencia);

           habilitado.setValue(Boolean.TRUE);
        binder.bindInstanceFields(this);

        add(form);

    }

    private void guardar() {

        try {

            binder.writeBean(clienteActual);
            clienteActual.setFechaActualizacion(new Date());
            clienteActual.setFechaCreacion(new Date());
            clienteService.guardar(clienteActual);

            Notification.show("Cliente guardado", 2000, Notification.Position.TOP_CENTER);
            clienteActual = new Cliente();
            binder.readBean(clienteActual);
        } catch (ValidationException e) {
            Notification.show("Error al guardar el cliente: " + e.getMessage(), 2000, Notification.Position.BOTTOM_START);
        }
    }

    private void editar(Cliente clienteEdi) {
        this.clienteActual = clienteEdi;

        binder.setBean(clienteEdi);
    }

    @Override
    public void setParameter(BeforeEvent be, String parameter) {

        Cliente entidad = NavigationContext.retrieve(parameter, Cliente.class);
        if (entidad != null) {
            System.out.println("Art :" + entidad);
            editar(entidad);
        } else {
            System.out.println("Error Art :" + entidad);
            // manejar error si no se encuentra
        }
    }

}
