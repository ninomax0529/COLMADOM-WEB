package com.maxsoft.application.dto;

import java.math.BigDecimal;

/**
 * Resumen del turno de caja calculado a partir de los movimientos registrados.
 * Convención de montos: positivo = entra efectivo a la gaveta, negativo = sale.
 */
public class ResumenCajaDTO {

    private final BigDecimal fondoInicial;
    /** Efectivo agregado a la gaveta con el turno abierto (fondo extra). */
    private final BigDecimal fondoExtra;
    /** Ventas de contado en mostrador, netas de anulaciones y devoluciones. */
    private final BigDecimal ventasEfectivo;
    /** Efectivo entregado por los motoristas en las liquidaciones de delivery. */
    private final BigDecimal cobrosDelivery;
    /** Abonos y pagos en efectivo de clientes a su libreta (CxC). */
    private final BigDecimal cobrosCxc;
    /** Pagos en efectivo a proveedores (valor negativo). */
    private final BigDecimal pagosProveedores;
    /** Otros movimientos de efectivo (ingresos extra, retiros, gastos menores). */
    private final BigDecimal otrosMovimientos;
    /** Avances de efectivo con tarjeta: el efectivo sale de la gaveta (valor negativo). */
    private final BigDecimal avancesEfectivo;
    /** Total cobrado con tarjeta/datáfono (referencia, no afecta la gaveta). */
    private final BigDecimal ventasTarjeta;

    public ResumenCajaDTO(BigDecimal fondoInicial, BigDecimal fondoExtra, BigDecimal ventasEfectivo,
            BigDecimal cobrosDelivery, BigDecimal cobrosCxc, BigDecimal pagosProveedores,
            BigDecimal otrosMovimientos, BigDecimal avancesEfectivo, BigDecimal ventasTarjeta) {
        this.fondoInicial = fondoInicial;
        this.fondoExtra = fondoExtra;
        this.ventasEfectivo = ventasEfectivo;
        this.cobrosDelivery = cobrosDelivery;
        this.cobrosCxc = cobrosCxc;
        this.pagosProveedores = pagosProveedores;
        this.otrosMovimientos = otrosMovimientos;
        this.avancesEfectivo = avancesEfectivo;
        this.ventasTarjeta = ventasTarjeta;
    }

    public BigDecimal getFondoInicial() {
        return fondoInicial;
    }

    public BigDecimal getFondoExtra() {
        return fondoExtra;
    }

    public BigDecimal getVentasEfectivo() {
        return ventasEfectivo;
    }

    public BigDecimal getCobrosDelivery() {
        return cobrosDelivery;
    }

    public BigDecimal getCobrosCxc() {
        return cobrosCxc;
    }

    public BigDecimal getPagosProveedores() {
        return pagosProveedores;
    }

    public BigDecimal getOtrosMovimientos() {
        return otrosMovimientos;
    }

    public BigDecimal getAvancesEfectivo() {
        return avancesEfectivo;
    }

    public BigDecimal getVentasTarjeta() {
        return ventasTarjeta;
    }

    /** Efectivo que debería haber físicamente en la gaveta. */
    public BigDecimal getEfectivoEsperado() {
        return fondoInicial
                .add(fondoExtra)
                .add(ventasEfectivo)
                .add(cobrosDelivery)
                .add(cobrosCxc)
                .add(pagosProveedores)
                .add(otrosMovimientos)
                .add(avancesEfectivo);
    }
}
