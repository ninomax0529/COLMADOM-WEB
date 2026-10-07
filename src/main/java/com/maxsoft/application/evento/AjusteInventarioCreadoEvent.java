package com.maxsoft.application.evento;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Evento que se dispara al registrar un Ajuste de Inventario (Positivo o Negativo)
 * para actualizar el Kardex y las existencias por Almacén.
 * 
 * @author Maximiliano
 */
public class AjusteInventarioCreadoEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Integer idAjuste;
    private final String numeroDocumento;     // N° de Documento/Ajuste para auditoría en Kardex
    private final Integer idTipoMovimiento; // 1 = Entrada (Ajuste Positivo), 2 = Salida (Ajuste Negativo)
    private final String usuario;
    private final String observacion;
    private final List<ItemAjusteDto> items;

    public AjusteInventarioCreadoEvent(Integer idAjuste, 
                                      String numeroDocumento, 
                                      Integer idTipoMovimiento, 
                                      String usuario, 
                                      String observacion, 
                                      List<ItemAjusteDto> items) {
        this.idAjuste = idAjuste;
        this.numeroDocumento = numeroDocumento;
        this.idTipoMovimiento = idTipoMovimiento;
        this.usuario = usuario;
        this.observacion = observacion;
        this.items = items;
    }

    public Integer getIdAjuste() { 
        return idAjuste; 
    }

    public String getNumeroDocumento() { 
        return numeroDocumento; 
    }

    public Integer getIdTipoMovimiento() { 
        return idTipoMovimiento; 
    }

    public String getUsuario() { 
        return usuario; 
    }

    public String getObservacion() { 
        return observacion; 
    }

    public List<ItemAjusteDto> getItems() { 
        return items; 
    }

    /**
     * Contiene el detalle de los artículos ajustados.
     */
    public static class ItemAjusteDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Integer idArticulo;
        private final Integer idAlmacen;
        private final Integer idArticuloEmpaque;  // Opcional: si el ajuste se especificó en un empaque particular
        private final BigDecimal factorConversion;
        private final BigDecimal cantidadEmpaque;  // Cantidad ajustada según el empaque
        private final BigDecimal cantidadBase;     // Cantidad en unidad base a sumar/restar
        private final BigDecimal subTotal;         // Reemplaza a Double costo (monto base del ajuste)
        private final BigDecimal itbis;
        private final BigDecimal total;

        public ItemAjusteDto(Integer idArticulo, 
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