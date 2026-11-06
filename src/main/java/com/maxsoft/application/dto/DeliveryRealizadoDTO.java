package com.maxsoft.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Factura de delivery con su estado, para la consulta de deliveries realizados.
 */
public class DeliveryRealizadoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PENDIENTE = "PENDIENTE";
    public static final String LIQUIDADO = "LIQUIDADO";
    public static final String ANULADO = "ANULADO";

    private final Integer codigoFactura;
    private final Date fecha;
    private final String cliente;
    private final Integer codigoMotorista;
    private final String motorista;
    private final String tipoPago;
    private final BigDecimal montoTotal;
    private final String estado;
    private final Integer codigoLiquidacion;
    private final Date fechaLiquidacion;
    // Salió de contado y al liquidar se pasó a cuentas por cobrar
    private final String clienteCredito;

    public DeliveryRealizadoDTO(Integer codigoFactura, Date fecha, String cliente,
            Integer codigoMotorista, String motorista, String tipoPago, BigDecimal montoTotal,
            String estado, Integer codigoLiquidacion, Date fechaLiquidacion, String clienteCredito) {
        this.codigoFactura = codigoFactura;
        this.fecha = fecha;
        this.cliente = cliente;
        this.codigoMotorista = codigoMotorista;
        this.motorista = motorista;
        this.tipoPago = tipoPago;
        this.montoTotal = montoTotal != null ? montoTotal : BigDecimal.ZERO;
        this.estado = estado;
        this.codigoLiquidacion = codigoLiquidacion;
        this.fechaLiquidacion = fechaLiquidacion;
        this.clienteCredito = clienteCredito;
    }

    public Integer getCodigoFactura() {
        return codigoFactura;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public Integer getCodigoMotorista() {
        return codigoMotorista;
    }

    public String getMotorista() {
        return motorista;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public String getEstado() {
        return estado;
    }

    public Integer getCodigoLiquidacion() {
        return codigoLiquidacion;
    }

    public Date getFechaLiquidacion() {
        return fechaLiquidacion;
    }

    public String getClienteCredito() {
        return clienteCredito;
    }

    public boolean isConvertidoACredito() {
        return clienteCredito != null;
    }
}
