/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.delivery.vehiculo;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.Vehiculo;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.delivery.VehiculoService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;

@PageTitle("Registro de Vehiculo")
@Route(value = "delivery/registroDeVehiculo")
public class RegistroVehiculoView extends VerticalLayout implements HasUrlParameter<String> {

    private final VehiculoService vehiculoService;

    private final Binder<Vehiculo> binder = new Binder<>(Vehiculo.class);
    private final TextField codigo = new TextField("Codigo");
    private final TextField nombre = new TextField("Nombre");
    private final TextField placa = new TextField("No.Placa");
    private final Checkbox habilitado = new Checkbox("Habilitado", true);

    ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Vehiculo delivery = new Vehiculo();

    @Autowired
    public RegistroVehiculoView(VehiculoService vehiculoService) {

        this.vehiculoService = vehiculoService;

        add(botonera,
                new H2("Nuevo Vehiculo")
        );

        configurarFormulario();

        botonera.getGuardar().addClickListener(e -> {
            // lógica de guardar

            if (nombre.getValue().isEmpty()) {
                ClaseUtil.mostrarNotificacion("Tiene que digitar el nombre", NotificationVariant.LUMO_WARNING);
                nombre.focus();
                return;
            }

            if (placa.getValue().isEmpty()) {
                ClaseUtil.mostrarNotificacion("Tiene que digitar la placa", NotificationVariant.LUMO_WARNING);
                placa.focus();
                return;
            }

            guardar();
        });

        botonera.getCancelar().addClickListener(e -> {
            // lógica de cancelar
            UI.getCurrent().navigate(VehiculoView.class);
        });

    }

    private void configurarFormulario() {

        FormLayout form = new FormLayout();

        codigo.setEnabled(false);
        form.add(codigo, habilitado, nombre, placa);

        habilitado.setValue(Boolean.TRUE);
        binder.bindInstanceFields(this);

        add(form);

    }

    private void guardar() {

        try {

            binder.writeBean(delivery);
            delivery.setFechaCreacion(new Date());
//            delivery.setFechaCreacion(new Date());
            this.vehiculoService.guardar(delivery);

            ClaseUtil.mostrarNotificacion("Registro exitosamente", NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(VehiculoView.class);
            delivery = new Vehiculo();
            binder.readBean(delivery);
        } catch (ValidationException e) {
            ClaseUtil.mostrarNotificacion("Error al guardar el delivery: " + e.getMessage(), NotificationVariant.LUMO_ERROR);

        }
    }

    private void editar(Vehiculo vehiculo) {
        this.delivery = vehiculo;

        binder.setBean(vehiculo);
    }

    @Override
    public void setParameter(BeforeEvent be, String parameter) {

        Vehiculo entidad = NavigationContext.retrieve(parameter, Vehiculo.class);
        if (entidad != null) {
            System.out.println("Art :" + entidad);
            editar(entidad);
        } else {
            System.out.println("Error Art :" + entidad);
            // manejar error si no se encuentra
        }
    }

}
