package com.maxsoft.application.evento;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Evento que se dispara al anular una venta para reingresar el stock al Kardex e Inventario.
 * 
 * @author Maximiliano
 */
public class VentaAnuladaEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Integer idFactura;
    private final String numeroFactura;
    private final String usuario;
    private final String motivo;
    private final List<ItemAnulacionDto> items;

    public VentaAnuladaEvent(Integer idFactura, String numeroFactura, String usuario, String motivo, List<ItemAnulacionDto> items) {
        this.idFactura = idFactura;
        this.numeroFactura = numeroFactura;
        this.usuario = usuario;
        this.motivo = motivo;
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

    public String getMotivo() { 
        return motivo; 
    }

    public List<ItemAnulacionDto> getItems() { 
        return items; 
    }

    /**
     * Contiene los detalles del ítem de la factura anulada que reingresará al inventario.
     */
    public static class ItemAnulacionDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Integer idArticulo;
        private final Integer idAlmacen;
        private final Integer idArticuloEmpaque;  // Puede ser null si fue vendido en Unidad Base
        private final BigDecimal factorConversion;
        private final BigDecimal cantidadEmpaque;  // Cantidad devuelta en empaque
        private final BigDecimal cantidadBase;     // Cantidad física base a reingresar (+ stock)
        private final BigDecimal subTotal;
        private final BigDecimal itbis;
        private final BigDecimal total;

        public ItemAnulacionDto(Integer idArticulo, 
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