/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.cajaChica;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.CajaChica;
import com.maxsoft.application.modelo.MovimientoCajaChica;
import com.maxsoft.application.servicio.interfaces.CajaChicaService;
import com.maxsoft.application.util.ClaseUtil;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;

@PageTitle("Gestión de Caja")
@Route(value = "venta/caja")
//@RolesAllowed("CAJERO")
public class CajaChicaView extends VerticalLayout {

    private final CajaChicaService cajaService;

    // Formulario de apertura
    private TextField puntoDeVenta = new TextField("Punto de Venta");
    private TextField usuario = new TextField("Usuario Apertura");
    private NumberField montoApertura = new NumberField("Monto Apertura");
    private Button abrirCajaBtn = new Button("Abrir Caja");

    // Movimientos
    private Grid<MovimientoCajaChica> grid = new Grid<>(MovimientoCajaChica.class);

    // Cierre de caja
    private Button cerrarCajaBtn = new Button("Cerrar Caja");
    private NumberField montoRealCierre = new NumberField("Monto Real Contado");

    // --- Formulario de movimientos ---
    private NumberField montoMovimiento = new NumberField("Monto");
    private TextField descripcionMovimiento = new TextField("Descripción");
    private TextField metodoPagoMovimiento = new TextField("Método de Pago");
    private Button registrarIngresoBtn = new Button("Registrar Ingreso");
    private Button registrarEgresoBtn = new Button("Registrar Egreso");

    private CajaChica cajaActual;

    public CajaChicaView(CajaChicaService cajaService) {
        this.cajaService = cajaService;
        setSizeFull();

        // --- Formulario de apertura ---
        FormLayout form = new FormLayout();
        puntoDeVenta.setPlaceholder("Ej: Caja 1 - Sucursal Centro");
        usuario.setPlaceholder("Usuario logueado");
        montoApertura.setPlaceholder("Monto inicial en caja");

        // --- Formulario de movimiento ---
        HorizontalLayout movimientoLayout = new HorizontalLayout(
                montoMovimiento,
                descripcionMovimiento,
                metodoPagoMovimiento,
                registrarIngresoBtn,
                registrarEgresoBtn
        );

        registrarIngresoBtn.addClickListener(e -> registrarMovimiento("INGRESO"));
        registrarEgresoBtn.addClickListener(e -> registrarMovimiento("EGRESO"));

        add(movimientoLayout);

        abrirCajaBtn.addClickListener(e -> abrirCaja());

        form.add(puntoDeVenta, usuario, montoApertura, abrirCajaBtn);

        // --- Grid de movimientos ---
        grid.setColumns("fecha", "tipo", "monto", "descripcion", "metodoPago");
        grid.setSizeFull();

        // --- Botón de cierre ---
        cerrarCajaBtn.addClickListener(e -> cerrarCaja());

        HorizontalLayout cierreLayout = new HorizontalLayout(montoRealCierre, cerrarCajaBtn);

        add(new Text("Gestión de Caja"), form, grid, cierreLayout);
    }

    private void abrirCaja() {
        if (cajaActual != null && cajaActual.getAbierta()) {
            Notification.show("Ya existe una caja abierta.");
            return;
        }

        cajaActual = new CajaChica();
        cajaActual.setPuntoDeVenta(puntoDeVenta.getValue());
        cajaActual.setUsuarioApertura(usuario.getValue());
        cajaActual.setFechaApertura(ClaseUtil.asDate(LocalDate.now()));
        cajaActual.setMontoApertura(montoApertura.getValue());
        cajaActual.setAbierta(true);

        cajaActual = cajaService.guardar(cajaActual);
        Notification.show("Caja abierta exitosamente.");
        refrescarMovimientos();
    }

    private void cerrarCaja() {
        if (cajaActual == null || !cajaActual.getAbierta()) {
            Notification.show("No hay caja abierta para cerrar.");
            return;
        }

        double totalMovimientos = cajaActual.getMovimientoCajaChicaCollection()
                .stream()
                .mapToDouble(m -> "INGRESO".equals(m.getTipo()) ? m.getMonto() : -m.getMonto())
                .sum();

        double esperado = cajaActual.getMontoApertura() + totalMovimientos;

        cajaActual.setFechaCierre(ClaseUtil.asDate(LocalDate.now()));
        cajaActual.setUsuarioCierre(usuario.getValue());
        cajaActual.setMontoCierreEsperado(esperado);
        cajaActual.setMontoCierreReal(montoRealCierre.getValue());
        cajaActual.setAbierta(false);

        cajaService.guardar(cajaActual);

        Notification.show("Caja cerrada. Esperado: " + esperado + " / Real: " + montoRealCierre.getValue());
        refrescarMovimientos();
    }

    private void refrescarMovimientos() {
        if (cajaActual != null) {
            grid.setItems(cajaActual.getMovimientoCajaChicaCollection());
        } else {
            grid.setItems();
        }
    }
    
    private void registrarMovimiento(String tipo) {
    if (cajaActual == null || !cajaActual.getAbierta()) {
        Notification.show("Debe haber una caja abierta para registrar movimientos.");
        return;
    }

    MovimientoCajaChica mov = new MovimientoCajaChica();
    mov.setFecha(ClaseUtil.asDate(LocalDate.now()));
    mov.setTipo(tipo);
    mov.setMonto(montoMovimiento.getValue());
    mov.setDescripcion(descripcionMovimiento.getValue());
    mov.setMetodoPago(metodoPagoMovimiento.getValue());
    mov.setCajaChica(cajaActual);

    // Guardar en BD
    cajaActual.getMovimientoCajaChicaCollection().add(mov);
    cajaService.guardar(cajaActual);

    Notification.show(tipo + " registrado correctamente.");
    refrescarMovimientos();

    // Limpiar campos
    montoMovimiento.clear();
    descripcionMovimiento.clear();
    metodoPagoMovimiento.clear();
}

}
