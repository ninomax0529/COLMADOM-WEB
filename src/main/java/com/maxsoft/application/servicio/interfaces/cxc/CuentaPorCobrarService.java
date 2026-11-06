package com.maxsoft.application.servicio.interfaces.cxc;

import com.maxsoft.application.dto.ResumenCreditoDTO;
import com.maxsoft.application.modelo.CuentaPorCobrar;
import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.HistorialLimiteCredito;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import java.math.BigDecimal;
import java.util.List;

/**
 * Estado de cuenta del cliente (libreta / fiado) y control del límite de
 * crédito. Todo movimiento que cambie lo que debe un cliente pasa por aquí,
 * dentro de la transacción del documento que lo origina.
 */
public interface CuentaPorCobrarService {

    /** Tipo de venta "CREDITO" (libreta). */
    int TIPO_VENTA_CREDITO = 2;

    /**
     * Registra el débito de una factura a crédito. Valida antes que el
     * cliente esté habilitado y que la factura no exceda su límite; si no
     * cumple lanza IllegalStateException y la venta completa se revierte.
     */
    CuentaPorCobrar registrarFacturaCredito(FacturaDeVenta factura, String usuario);

    /** Registra el crédito de una línea de recibo aplicada a una factura a crédito. */
    CuentaPorCobrar registrarPago(ReciboDeIngreso recibo, DetalleReciboDeIngreso detalle, String usuario);

    /**
     * Registra el crédito por anular una factura a crédito, por su saldo según
     * el estado de cuenta. Devuelve null si ya estaba saldada.
     */
    CuentaPorCobrar registrarAnulacionFactura(FacturaDeVenta factura, String motivo, String usuario);

    /**
     * Registra el crédito por devolución de artículos de una factura a crédito
     * (rebaja la deuda del cliente). El monto no puede exceder el saldo de la
     * factura en el estado de cuenta.
     */
    CuentaPorCobrar registrarDevolucion(FacturaDeVenta factura, BigDecimal monto, String motivo, String usuario);

    /** Lo que queda pendiente de una factura según el estado de cuenta (débitos - créditos). */
    BigDecimal saldoFactura(Integer factura);

    /** Ajuste manual del balance (débito = aumenta la deuda, crédito = la disminuye). */
    CuentaPorCobrar registrarAjuste(Integer cliente, BigDecimal monto, boolean esDebito, String concepto, String usuario);

    /** true si la factura es a crédito y ya tiene su débito en el estado de cuenta. */
    boolean facturaEnEstadoDeCuenta(FacturaDeVenta factura);

    /** Lanza IllegalStateException si el cliente no puede recibir este crédito adicional. */
    void validarCredito(Integer cliente, BigDecimal montoNuevo);

    ResumenCreditoDTO obtenerResumen(Integer cliente);

    List<CuentaPorCobrar> getEstadoDeCuenta(Integer cliente);

    /** Guarda en el histórico un cambio de límite (no modifica el cliente). */
    void registrarCambioLimite(Integer cliente, BigDecimal limiteAnterior, BigDecimal limiteNuevo,
            String motivo, String usuario);

    List<HistorialLimiteCredito> getHistorialLimite(Integer cliente);
}
