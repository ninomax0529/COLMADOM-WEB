/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.dto;

import java.util.List;

public class AjusteInventarioCreadoEvent {

    private final Integer idAjuste;
    private final Integer idTipoMovimiento; // 1 = Entrada, 2 = Salida
    private final String usuario;
    private final String observacion;
    private final List<ItemAjusteDto> items;

    public AjusteInventarioCreadoEvent(Integer idAjuste, Integer idTipoMovimiento, String usuario, String observacion, List<ItemAjusteDto> items) {
        this.idAjuste = idAjuste;
        this.idTipoMovimiento = idTipoMovimiento;
        this.usuario = usuario;
        this.observacion = observacion;
        this.items = items;
    }

    public Integer getIdAjuste() { return idAjuste; }
    public Integer getIdTipoMovimiento() { return idTipoMovimiento; }
    public String getUsuario() { return usuario; }
    public String getObservacion() { return observacion; }
    public List<ItemAjusteDto> getItems() { return items; }

    public static class ItemAjusteDto {
        private final Integer idArticulo;
        private final Double cantidad;
        private final Double costo;

        public ItemAjusteDto(Integer idArticulo, Double cantidad, Double costo) {
            this.idArticulo = idArticulo;
            this.cantidad = cantidad;
            this.costo = costo;
        }

        public Integer getIdArticulo() { return idArticulo; }
        public Double getCantidad() { return cantidad; }
        public Double getCosto() { return costo; }
    }
}
