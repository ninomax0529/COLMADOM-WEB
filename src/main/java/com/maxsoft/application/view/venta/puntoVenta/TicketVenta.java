/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.TipoVenta;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TicketVenta {

    private static final DecimalFormat CANTIDAD_FORMAT = new DecimalFormat("#,##0.##");
    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");
    private String id;
    private final int numeroTicket;
    private Cliente cliente;
    private Delivery delivery;
    private TipoVenta tipoVenta;
    private EstadoFactura estadoFactura;
    private final List<DetalleFacturaDeVenta> items = new ArrayList<>();
    private final ListDataProvider<DetalleFacturaDeVenta> dataProvider = new ListDataProvider<>(items);
    private final Grid<DetalleFacturaDeVenta> grid = new Grid<>(DetalleFacturaDeVenta.class, false);
    private final Span totalSpan = new Span("RD$ 0.00");
    private String nombreCliente;
    private String direccion;

    public TicketVenta(int numeroTicket, String id) {
        this.numeroTicket = numeroTicket;
        this.id = id;
        totalSpan.getStyle()
                .set("font-size", "1.8rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the nombreCliente
     */
    public String getNombreCliente() {
        return nombreCliente;
    }

    /**
     * @param nombreCliente the nombreCliente to set
     */
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    /**
     * @return the direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getNumeroTicket() {
        return numeroTicket;
    }

    public List<DetalleFacturaDeVenta> getItems() {
        return items;
    }

    public ListDataProvider<DetalleFacturaDeVenta> getDataProvider() {
        return dataProvider;
    }

    public Grid<DetalleFacturaDeVenta> getGrid() {
        return grid;
    }

    /**
     * @return the estadoFactura
     */
    public EstadoFactura getEstadoFactura() {
        return estadoFactura;
    }

    /**
     * @param estadoFactura the estadoFactura to set
     */
    public void setEstadoFactura(EstadoFactura estadoFactura) {
        this.estadoFactura = estadoFactura;
    }

    /**
     * @return the cliente
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * @param cliente the cliente to set
     */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * @return the delivery
     */
    public Delivery getDelivery() {
        return delivery;
    }

    /**
     * @param delivery the delivery to set
     */
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    /**
     * @return the tipoVenta
     */
    public TipoVenta getTipoVenta() {
        return tipoVenta;
    }

    /**
     * @param tipoVenta the tipoVenta to set
     */
    public void setTipoVenta(TipoVenta tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    public Span getTotalSpan() {
        return totalSpan;
    }

    public Double getTotalAmount() {

        Double subTotal = 0.00;
        for (DetalleFacturaDeVenta det : items) {

            subTotal += det.getCantidad() * det.getPrecioVenta();
        }
        return subTotal;
    }

    public void updateUI() {
        dataProvider.refreshAll();
        totalSpan.setText("RD$ " + MONEDA_FORMAT.format(getTotalAmount()));
    }
}
