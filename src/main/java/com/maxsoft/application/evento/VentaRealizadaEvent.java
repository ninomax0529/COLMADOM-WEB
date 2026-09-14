/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.evento;

import java.io.Serializable;
import java.util.List;

public class VentaRealizadaEvent {

    private final Integer idFactura;
    private final List<ItemVentaDto> items;

    public VentaRealizadaEvent(Integer idFactura, List<ItemVentaDto> items) {
        this.idFactura = idFactura;
        this.items = items;
    }

    public Integer getIdFactura() { return idFactura; }
    public List<ItemVentaDto> getItems() { return items; }

    public static class ItemVentaDto implements Serializable {
        private final Integer idArticulo;
        private final Double cantidad;

        public ItemVentaDto(Integer idArticulo, Double cantidad) {
            this.idArticulo = idArticulo;
            this.cantidad = cantidad;
        }

        public Integer getIdArticulo() { return idArticulo; }
        public Double getCantidad() { return cantidad; }
    }
}
