/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.compra.proveedor;

import com.maxsoft.application.view.venta.cliente.*;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import com.vaadin.flow.component.notification.Notification;

import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;

@PageTitle("Registro de Proveedor")
@Route(value = "venta/RegistroProveedor")
public class RegistroProveedorView extends VerticalLayout implements HasUrlParameter<String> {

    private final ProveedorService proveedorService;

    private final Binder<Proveedor> binder = new Binder<>(Proveedor.class);
    private final TextField codigo = new TextField("Codigo");
    private final TextField nombre = new TextField("Nombre y Apellidos");
    private final TextField cedula = new TextField("No.Cédula");
    private final TextField celular = new TextField("No.Celular");
    private final TextField direccion = new TextField("Dirección");
    private final TextField lugarDeTrabajo = new TextField("Lugar de Trabajo");
    private final TextField referencia = new TextField("Referencia");
    private final Checkbox habilitado = new Checkbox("Habilitado", true);

    ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Proveedor proveedorActual = new Proveedor();

    @Autowired
    public RegistroProveedorView(ProveedorService proveedorService) {

        this.proveedorService = proveedorService;

        add(botonera,
                new H2("Nuevo Proveedor")
        );

        configurarFormulario();

        botonera.getGuardar().addClickListener(e -> {
            // lógica de guardar
            guardar();
        });

        botonera.getCancelar().addClickListener(e -> {
            // lógica de cancelar
            UI.getCurrent().navigate(ProveedorView.class);
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

            binder.writeBean(proveedorActual);
            proveedorActual.setFechaCracion(new Date());
            proveedorActual.setFechaActualizacion(new Date());
            proveedorActual.setHabilitado(habilitado.getValue());
            proveedorActual.setContacto(referencia.getValue());
            proveedorService.guardar(proveedorActual);

            Notification.show("Proveedor guardado", 2000, Notification.Position.TOP_CENTER);
            proveedorActual = new Proveedor();
            binder.readBean(proveedorActual);
        } catch (ValidationException e) {
            Notification.show("Error al guardar el proveedor: " + e.getMessage(), 2000, Notification.Position.BOTTOM_START);
        }
    }

    private void editar(Proveedor proveedor) {
        
        this.proveedorActual = proveedor;
//        habilitado.setValue(proveedorActual.getHabilitado());
//        referencia.setValue(proveedorActual.getContacto());

        binder.setBean(proveedor);
    }

    @Override
    public void setParameter(BeforeEvent be, String parameter) {

        Proveedor entidad = NavigationContext.retrieve(parameter, Proveedor.class);
        if (entidad != null) {
            System.out.println("Art :" + entidad);
            editar(entidad);
        } else {
            System.out.println("Error Art :" + entidad);
            // manejar error si no se encuentra
        }
    }

}
