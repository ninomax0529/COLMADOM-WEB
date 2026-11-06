/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.cliente;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;

@PageTitle("Registro de Clientes")
@Route(value = "venta/RegistroCliente")
public class RegistroClienteView extends VerticalLayout implements HasUrlParameter<String> {

    private static final String USUARIO_POR_DEFECTO = "Admin";

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

    // Crédito (libreta)
    private final BigDecimalField limiteCredito = new BigDecimalField("Límite de crédito (RD$)");
    private final TextField motivoCambioLimite = new TextField("Motivo del cambio de límite");
    private final TextField balanceActual = new TextField("Balance actual (debe)");

    ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Cliente clienteActual = new Cliente();

    @Autowired
    public RegistroClienteView(ClienteService clienteService) {

        this.clienteService = clienteService;

        add(botonera,
                new H2("Nuevo Cliente")
        );

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

        limiteCredito.setPlaceholder("Vacío = sin límite");
        limiteCredito.setHelperText("Vacío: sin límite. 0: no se le fía.");
        motivoCambioLimite.setPlaceholder("Ej.: buen pagador, atraso en pagos...");
        motivoCambioLimite.setMaxLength(255);
        balanceActual.setReadOnly(true);
        balanceActual.setValue("0.00");

        form.add(codigo, habilitado, nombre, cedula, celular, direccion, lugarDeTrabajo, referencia,
                limiteCredito, balanceActual, motivoCambioLimite);

        habilitado.setValue(Boolean.TRUE);
        binder.forField(codigo)
                .withNullRepresentation("")
                .bindReadOnly(c -> c.getCodigo() != null ? String.valueOf(c.getCodigo()) : "");
        binder.forField(limiteCredito)
                .withValidator(v -> v == null || v.signum() >= 0, "El límite no puede ser negativo")
                .bind(Cliente::getLimiteCredito, Cliente::setLimiteCredito);
        binder.bindInstanceFields(this);

        add(form);

    }

    private void guardar() {

        try {

            binder.writeBean(clienteActual);
            clienteActual.setFechaActualizacion(new Date());
            if (clienteActual.getFechaCreacion() == null) {
                clienteActual.setFechaCreacion(new Date());
                clienteActual.setCreadoPor(USUARIO_POR_DEFECTO);
            }
            clienteActual.setActualizadoPor(USUARIO_POR_DEFECTO);
            clienteService.guardar(clienteActual, USUARIO_POR_DEFECTO, motivoCambioLimite.getValue());

            Notification.show("Cliente guardado", 2000, Notification.Position.TOP_CENTER);
            clienteActual = new Cliente();
            binder.readBean(clienteActual);
            motivoCambioLimite.clear();
            balanceActual.setValue("0.00");
        } catch (ValidationException e) {
            Notification.show("Error al guardar el cliente: revise los campos marcados.", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Notification.show(e.getMessage(), 3500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void editar(Cliente clienteEdi) {
        this.clienteActual = clienteEdi;

        binder.setBean(clienteEdi);
        BigDecimal balance = clienteEdi.getBalance() != null ? clienteEdi.getBalance() : BigDecimal.ZERO;
        balanceActual.setValue(String.format("%,.2f", balance));
    }

    @Override
    public void setParameter(BeforeEvent be, String parameter) {

        Cliente entidad = NavigationContext.retrieve(parameter, Cliente.class);
        if (entidad != null) {
            editar(entidad);
        }
    }

}
