package com.maxsoft.application.dto;

import com.maxsoft.application.modelo.Cliente;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Factura de delivery pendiente de liquidar, lista para mostrar en pantalla.
 */
public class PedidoDeliveryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String CONTADO = "CONTADO";
    public static final String FIADO = "FIADO";

    private final Cliente cliente;
    private final Integer codigoFactura;
    private final String numeroFactura;
    private final String nombreCliente;
    private final String tipoPago;
    private final BigDecimal montoTotal;
    /** Momento en que se facturó el pedido (salida a ruta); puede ser null. */
    private final Date fechaSalida;

    public PedidoDeliveryDTO(Integer codigoFactura, String numeroFactura, String nombreCliente,
            String tipoPago, BigDecimal montoTotal, Cliente cliente) {
        this(codigoFactura, numeroFactura, nombreCliente, tipoPago, montoTotal, cliente, null);
    }

    public PedidoDeliveryDTO(Integer codigoFactura, String numeroFactura, String nombreCliente,
            String tipoPago, BigDecimal montoTotal, Cliente cliente, Date fechaSalida) {
        this.codigoFactura = codigoFactura;
        this.numeroFactura = numeroFactura;
        this.nombreCliente = nombreCliente;
        this.cliente = cliente;
        this.tipoPago = tipoPago;
        this.montoTotal = montoTotal != null ? montoTotal : BigDecimal.ZERO;
        this.fechaSalida = fechaSalida;
    }

    public Integer getCodigoFactura() {
        return codigoFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public Cliente getCliente() {
        return cliente;
    }

    /** Nombre que figura en la factura (o del cliente si no tiene). */
    public String getNombreCliente() {
        if (nombreCliente != null && !nombreCliente.isBlank()) {
            return nombreCliente;
        }
        return cliente != null && cliente.getNombre() != null ? cliente.getNombre() : "";
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public Date getFechaSalida() {
        return fechaSalida;
    }

    public boolean esFiado() {
        return FIADO.equals(tipoPago);
    }
}
