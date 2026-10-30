/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.delivery.gestiondelivey;

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

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.Vehiculo;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.delivery.VehiculoService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;

@PageTitle("Registro de Delivery")
@Route(value = "delivery/registroDelivery")
public class RegistroDeliveryView extends VerticalLayout implements HasUrlParameter<String> {

    private final DeliveryService deliveryService;

    private final VehiculoService vehiculoService;

    private final Binder<Delivery> binder = new Binder<>(Delivery.class);
    private final TextField codigo = new TextField("Codigo");
    private final TextField nombre = new TextField("Nombre y Apellidos");
    private final TextField celular = new TextField("No.Celular");
    private final TextField cedula = new TextField("No.Cedula");
    private final TextField direccion = new TextField("Dirección");
    private final Checkbox habilitado = new Checkbox("Habilitado", true);
    private final ComboBox<Vehiculo> cbVehiculo = new ComboBox<>("Seleccionar Vehiculo");

    ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Delivery delivery = new Delivery();

    @Autowired
    public RegistroDeliveryView(DeliveryService deliveryService,
            VehiculoService vehiculoService
    ) {

        this.deliveryService = deliveryService;
        this.vehiculoService = vehiculoService;

        add(botonera,
                new H2("Nuevo Delivery")
        );

        configurarFormulario();

        botonera.getGuardar().addClickListener(e -> {
            // lógica de guardar

            if (nombre.getValue().isEmpty()) {
                ClaseUtil.mostrarNotificacion("Tiene que digitar el nombre", NotificationVariant.LUMO_WARNING);
                nombre.focus();
                return;
            }

            if (direccion.getValue().isEmpty()) {
                ClaseUtil.mostrarNotificacion("Tiene que digitar la direccion", NotificationVariant.LUMO_WARNING);
                direccion.focus();
                return;
            }

            if (celular.getValue().isEmpty()) {
                ClaseUtil.mostrarNotificacion("Tiene que digitar el numero de celular", NotificationVariant.LUMO_WARNING);
                celular.focus();
                return;
            }

            guardar();
        });

        botonera.getCancelar().addClickListener(e -> {
            // lógica de cancelar
            UI.getCurrent().navigate(DeliveryView.class);
        });

    }

    private void configurarFormulario() {

        FormLayout form = new FormLayout();

        cbVehiculo.setItemLabelGenerator(lb->lb.getNombre());
        cbVehiculo.setItems(this.vehiculoService.getLista());
        codigo.setEnabled(false);
        form.add(codigo, habilitado, nombre, cedula, celular, direccion, cbVehiculo);

        habilitado.setValue(Boolean.TRUE);
        binder.bindInstanceFields(this);

        add(form);

    }

    private void guardar() {

        try {

            binder.writeBean(delivery);
            delivery.setFecha(new Date());
            delivery.setVehiculo(cbVehiculo.getValue());
            deliveryService.guardar(delivery);

            ClaseUtil.mostrarNotificacion("Registro exitosamente", NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(DeliveryView.class);
            delivery = new Delivery();
            binder.readBean(delivery);
        } catch (ValidationException e) {
            ClaseUtil.mostrarNotificacion("Error al guardar el delivery: " + e.getMessage(), NotificationVariant.LUMO_ERROR);

        }
    }

    private void editar(Delivery delivery) {
        this.delivery = delivery;

        binder.setBean(delivery);
    }

    @Override
    public void setParameter(BeforeEvent be, String parameter) {

        Delivery entidad = NavigationContext.retrieve(parameter, Delivery.class);
        if (entidad != null) {
            System.out.println("Art :" + entidad);
            editar(entidad);
        } else {
            System.out.println("Error Art :" + entidad);
            // manejar error si no se encuentra
        }
    }

}
