package com.maxsoft.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Factura de delivery pendiente de liquidar, lista para mostrar en pantalla.
 */
public class PedidoDeliveryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String CONTADO = "CONTADO";
    public static final String FIADO = "FIADO";

    private final Integer codigoFactura;
    private final String numeroFactura;
    private final String cliente;
    private final String tipoPago;
    private final BigDecimal montoTotal;

    public PedidoDeliveryDTO(Integer codigoFactura, String numeroFactura, String cliente,
            String tipoPago, BigDecimal montoTotal) {
        this.codigoFactura = codigoFactura;
        this.numeroFactura = numeroFactura;
        this.cliente = cliente;
        this.tipoPago = tipoPago;
        this.montoTotal = montoTotal != null ? montoTotal : BigDecimal.ZERO;
    }

    public Integer getCodigoFactura() {
        return codigoFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public String getCliente() {
        return cliente;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public boolean esFiado() {
        return FIADO.equals(tipoPago);
    }
}
