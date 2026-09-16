/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.dto;

import java.util.List;

/**
 *
 * @author Maximiliano
 */
public class SolicitudDevolucionDto {
    private Integer idFactura;
    private String motivo;
    private String usuario;
    private List<ItemDevolucionDto> items;

    public static class ItemDevolucionDto {
        private Integer idDetalleFactura; // O código del artículo
        private Integer idArticulo;
        private Double cantidadADevolver;

        // Getters y Setters
        public Integer getIdDetalleFactura() { return idDetalleFactura; }
        public void setIdDetalleFactura(Integer idDetalleFactura) { this.idDetalleFactura = idDetalleFactura; }
        public Integer getIdArticulo() { return idArticulo; }
        public void setIdArticulo(Integer idArticulo) { this.idArticulo = idArticulo; }
        public Double getCantidadADevolver() { return cantidadADevolver; }
        public void setCantidadADevolver(Double cantidadADevolver) { this.cantidadADevolver = cantidadADevolver; }
    }

    // Getters y Setters...
    public Integer getIdFactura() { return idFactura; }
    public void setIdFactura(Integer idFactura) { this.idFactura = idFactura; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public List<ItemDevolucionDto> getItems() { return items; }
    public void setItems(List<ItemDevolucionDto> items) { this.items = items; }
}