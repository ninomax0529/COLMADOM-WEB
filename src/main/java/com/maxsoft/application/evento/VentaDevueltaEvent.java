/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.evento;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public class VentaDevueltaEvent {
    private final Integer idFactura;
    private final String usuario;
    private final String motivo;
    private final boolean esDevolucionTotal;
    private final BigDecimal montoTotalDevuelto;
    private final List<ItemAnulacionDto> items;

    public VentaDevueltaEvent(Integer idFactura, String usuario, String motivo, 
                              boolean esDevolucionTotal, BigDecimal montoTotalDevuelto, 
                              List<ItemAnulacionDto> items) {
        this.idFactura = idFactura;
        this.usuario = usuario;
        this.motivo = motivo;
        this.esDevolucionTotal = esDevolucionTotal;
        this.montoTotalDevuelto = montoTotalDevuelto;
        this.items = items;
    }

    // Getters...
    public Integer getIdFactura() { return idFactura; }
    public String getUsuario() { return usuario; }
    public String getMotivo() { return motivo; }
    public boolean isEsDevolucionTotal() { return esDevolucionTotal; }
    public BigDecimal getMontoTotalDevuelto() { return montoTotalDevuelto; }
    public List<ItemAnulacionDto> getItems() { return items; }

    public static class ItemAnulacionDto {
        private final Integer codigoArticulo;
        private final Double cantidad;

        public ItemAnulacionDto(Integer codigoArticulo, Double cantidad) {
            this.codigoArticulo = codigoArticulo;
            this.cantidad = cantidad;
        }

        public Integer getCodigoArticulo() { return codigoArticulo; }
        public Double getCantidad() { return cantidad; }
    }
}