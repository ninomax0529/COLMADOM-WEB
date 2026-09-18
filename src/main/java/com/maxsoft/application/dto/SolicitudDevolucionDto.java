/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.dto;

import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import java.util.List;

public class SolicitudDevolucionDto {

    private TipoDocumento tipoDocumento;
    private TipoMovimiento tipoMovimiento;
    private Integer idFactura;
    private String motivo;
    private String usuario;
    private List<ItemDevolucionDto> items;

    public static class ItemDevolucionDto {

        private Integer idDetalleFactura;
        private Integer idArticulo;
        private Double cantidadADevolver;

        public Integer getIdDetalleFactura() {
            return idDetalleFactura;
        }

        public void setIdDetalleFactura(Integer idDetalleFactura) {
            this.idDetalleFactura = idDetalleFactura;
        }

        public Integer getIdArticulo() {
            return idArticulo;
        }

        public void setIdArticulo(Integer idArticulo) {
            this.idArticulo = idArticulo;
        }

        public Double getCantidadADevolver() {
            return cantidadADevolver;
        }

        public void setCantidadADevolver(Double cantidadADevolver) {
            this.cantidadADevolver = cantidadADevolver;
        }
    }

    public Integer getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Integer idFactura) {
        this.idFactura = idFactura;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public List<ItemDevolucionDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDevolucionDto> items) {
        this.items = items;
    }

    /**
     * @return the tipoDocumento
     */
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    /**
     * @param tipoDocumento the tipoDocumento to set
     */
    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    /**
     * @return the tipoMovimiento
     */
    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    /**
     * @param tipoMovimiento the tipoMovimiento to set
     */
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

}
