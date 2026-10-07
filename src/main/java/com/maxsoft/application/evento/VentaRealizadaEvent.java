
package com.maxsoft.application.evento;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Evento que se dispara al realizar una venta para descontar inventario y registrar el Kardex.
 */
public class VentaRealizadaEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Integer idFactura;
    private final String numeroFactura;
    private final String usuario;
    private final String observacion;
    private final List<ItemVentaDto> items;

    public VentaRealizadaEvent(Integer idFactura, String numeroFactura, String usuario, String observacion, List<ItemVentaDto> items) {
        this.idFactura = idFactura;
        this.numeroFactura = numeroFactura;
        this.usuario = usuario;
        this.observacion = observacion;
        this.items = items;
    }

    public Integer getIdFactura() {
        return idFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getObservacion() {
        return observacion;
    }

    public List<ItemVentaDto> getItems() {
        return items;
    }

    /**
     * Contiene el detalle de cada ítem vendido con sus valores para Kardex e Inventario.
     */
    public static class ItemVentaDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Integer idArticulo;
        private final Integer idAlmacen;
        private final Integer idArticuloEmpaque;  // Puede ser null si se vende en Unidad Base
        private final BigDecimal factorConversion; // Ej. 1.00 si es base, o 12.00 si es Caja x12
        private final BigDecimal cantidadEmpaque;  // Ej. 2 (Cajas)
        private final BigDecimal cantidadBase;     // Ej. 24 (Unidades físicas a descontar)
        private final BigDecimal subTotal;
        private final BigDecimal itbis;
        private final BigDecimal total;

        public ItemVentaDto(Integer idArticulo, 
                            Integer idAlmacen, 
                            Integer idArticuloEmpaque, 
                            BigDecimal factorConversion, 
                            BigDecimal cantidadEmpaque, 
                            BigDecimal cantidadBase, 
                            BigDecimal subTotal, 
                            BigDecimal itbis, 
                            BigDecimal total) {
            this.idArticulo = idArticulo;
            this.idAlmacen = idAlmacen;
            this.idArticuloEmpaque = idArticuloEmpaque;
            this.factorConversion = factorConversion != null ? factorConversion : BigDecimal.ONE;
            this.cantidadEmpaque = cantidadEmpaque != null ? cantidadEmpaque : cantidadBase;
            this.cantidadBase = cantidadBase;
            this.subTotal = subTotal != null ? subTotal : BigDecimal.ZERO;
            this.itbis = itbis != null ? itbis : BigDecimal.ZERO;
            this.total = total != null ? total : BigDecimal.ZERO;
        }

        public Integer getIdArticulo() {
            return idArticulo;
        }

        public Integer getIdAlmacen() {
            return idAlmacen;
        }

        public Integer getIdArticuloEmpaque() {
            return idArticuloEmpaque;
        }

        public BigDecimal getFactorConversion() {
            return factorConversion;
        }

        public BigDecimal getCantidadEmpaque() {
            return cantidadEmpaque;
        }

        public BigDecimal getCantidadBase() {
            return cantidadBase;
        }

        public BigDecimal getSubTotal() {
            return subTotal;
        }

        public BigDecimal getItbis() {
            return itbis;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }
}


//////*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.maxsoft.application.evento;
//
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.util.List;
//
//public class VentaRealizadaEvent {
//
//    private final Integer idFactura;
//    private final List<ItemVentaDto> items;
//
//    public VentaRealizadaEvent(Integer idFactura, List<ItemVentaDto> items) {
//        this.idFactura = idFactura;
//        this.items = items;
//    }
//
//    public Integer getIdFactura() {
//        return idFactura;
//    }
//
//    public List<ItemVentaDto> getItems() {
//        return items;
//    }
//
//    public static class ItemVentaDto implements Serializable {
//
//        private final Integer idArticulo;
//        private final Integer idAlmacen;
//        private final BigDecimal cantidad;
//        
//
//        public ItemVentaDto(Integer idArticulo, BigDecimal cantidad, Integer idAlmacen) {
//            this.idArticulo = idArticulo;
//            this.cantidad = cantidad;
//            this.idAlmacen = idAlmacen;
//        }
//
//        public Integer getIdArticulo() {
//            return idArticulo;
//        }
//
//        public BigDecimal getCantidad() {
//            return cantidad;
//        }
//
//        /**
//         * @return the idAlmacen
//         */
//        public Integer getIdAlmacen() {
//            return idAlmacen;
//        }
//
//    }
//}
