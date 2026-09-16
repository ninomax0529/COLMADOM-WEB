/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.evento;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public class VentaAnuladaEvent {

    private final Integer idFactura;
    private final String usuario;
    private final String motivo;
    private final List<ItemAnulacionDto> items;

    public VentaAnuladaEvent(Integer idFactura, String usuario, String motivo, List<ItemAnulacionDto> items) {
        this.idFactura = idFactura;
        this.usuario = usuario;
        this.motivo = motivo;
        this.items = items;
    }

    public Integer getIdFactura() { return idFactura; }
    public String getUsuario() { return usuario; }
    public String getMotivo() { return motivo; }
    public List<ItemAnulacionDto> getItems() { return items; }

    public static class ItemAnulacionDto implements Serializable {
        private final Integer idArticulo;
        private final Double cantidad;

        public ItemAnulacionDto(Integer idArticulo, Double cantidad) {
            this.idArticulo = idArticulo;
            this.cantidad = cantidad;
        }

        public Integer getIdArticulo() { return idArticulo; }
        public Double getCantidad() { return cantidad; }
    }
}