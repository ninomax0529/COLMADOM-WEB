/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.evento;

import java.math.BigDecimal;
import java.util.List;

public class VentaDevueltaEvent {
    private final Integer idFactura;
    private Integer idEntradaInventario; // Nuevo campo
    private final String usuario;
    private final String motivo;
    private final boolean esDevolucionTotal;
    private final List<ItemAnulacionDto> items;

    // CONSTRUCTOR ORIGINAL (Para compatibilidad existente)
    public VentaDevueltaEvent(Integer idFactura, String usuario, String motivo, boolean esDevolucionTotal, List<ItemAnulacionDto> items) {
        this.idFactura = idFactura;
        this.usuario = usuario;
        this.motivo = motivo;
        this.esDevolucionTotal = esDevolucionTotal;
        this.items = items;
    }

    // NUEVO CONSTRUCTOR (Con idEntradaInventario)
    public VentaDevueltaEvent(Integer idFactura, Integer idEntradaInventario, String usuario, String motivo, boolean esDevolucionTotal, List<ItemAnulacionDto> items) {
        this.idFactura = idFactura;
        this.idEntradaInventario = idEntradaInventario;
        this.usuario = usuario;
        this.motivo = motivo;
        this.esDevolucionTotal = esDevolucionTotal;
        this.items = items;
    }

    public Integer getIdFactura() { return idFactura; }
    public Integer getIdEntradaInventario() { return idEntradaInventario; }
    public String getUsuario() { return usuario; }
    public String getMotivo() { return motivo; }
    public boolean isEsDevolucionTotal() { return esDevolucionTotal; }
    public List<ItemAnulacionDto> getItems() { return items; }

    public static class ItemAnulacionDto {
        private final Integer idArticulo;
        private final BigDecimal cantidad;

        public ItemAnulacionDto(Integer idArticulo, BigDecimal cantidad) {
            this.idArticulo = idArticulo;
            this.cantidad = cantidad;
        }

        public Integer getIdArticulo() { return idArticulo; }
        public Integer getCodigoArticulo() { return idArticulo; }
        public BigDecimal getCantidad() { return cantidad; }
    }
}