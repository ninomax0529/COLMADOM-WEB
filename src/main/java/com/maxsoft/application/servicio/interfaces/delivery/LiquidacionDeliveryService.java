package com.maxsoft.application.servicio.interfaces.delivery;

import com.maxsoft.application.dto.DeliveryRealizadoDTO;
import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.LiquidacionDelivery;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface LiquidacionDeliveryService {

    /**
     * Facturas de delivery del motorista pendientes de liquidar.
     */
    List<PedidoDeliveryDTO> getPedidosPendientes(Integer codigoDelivery);

    /** Código de motorista -> pedidos y monto pendientes de liquidar (solo los que tienen). */
    Map<Integer, com.maxsoft.application.dto.PendientesMotoristaDTO> getPendientesPorMotorista();

    /** Ticket (HTML de 80 mm) del comprobante de una liquidación, listo para imprimir. */
    String generarComprobanteHtml(Integer codigoLiquidacion);

    /**
     * Deliveries realizados en un rango de fechas con su estado: PENDIENTE
     * (sin liquidar), LIQUIDADO o ANULADO.
     *
     * @param hastaExclusivo primer instante que ya NO entra en el rango
     * @param codigoDelivery motorista, o null para todos
     */
    List<DeliveryRealizadoDTO> getDeliveriesRealizados(Date desde, Date hastaExclusivo, Integer codigoDelivery);

    /**
     * Liquida las facturas indicadas del motorista: registra el cuadre
     * (efectivo esperado, recibido y diferencia) y deja las facturas fuera de
     * los pendientes.
     *
     * @param efectivoRecibido efectivo que entrega el motorista por las
     * facturas de contado
     * @throws IllegalArgumentException datos inválidos, o diferencia sin
     * observación
     * @throws IllegalStateException alguna factura ya fue liquidada o no está
     * pendiente para ese motorista
     */
    LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            BigDecimal efectivoRecibido, String observacion, String usuario);

    /**
     * Igual que {@link #liquidar(Delivery, List, BigDecimal, String, String)},
     * pero permite que facturas que salieron de contado se queden a crédito:
     * por cada una indicada en {@code clientesCredito} la factura pasa a
     * libreta (cuentas por cobrar) a nombre del cliente, y su monto deja de
     * contar como efectivo esperado.
     *
     * @param clientesCredito codigo de factura -> cliente que se queda con
     * ella a crédito (puede ser vacío o null)
     * @throws IllegalArgumentException la factura no pertenece a la
     * liquidación, ya era fiada, o el cliente es nulo
     */
    LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            Map<Integer, Cliente> clientesCredito, BigDecimal efectivoRecibido,
            String observacion, String usuario);

    /**
     * Liquidación completa: además del crédito, admite pago mixto (parte en
     * transferencia, que no entra a la gaveta) y cargar el faltante a la
     * cuenta del motorista. La diferencia es (efectivo + transferencia) -
     * efectivo esperado.
     *
     * @param cargarFaltante si hay faltante, queda como deuda del motorista
     */
    LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            Map<Integer, Cliente> clientesCredito, BigDecimal efectivoRecibido,
            BigDecimal transferenciaRecibida, String referenciaTransferencia,
            boolean cargarFaltante, String observacion, String usuario);

    // ============================================================
    // CONSULTA, REIMPRESIÓN Y ANULACIÓN
    // ============================================================
    /** Liquidaciones (vigentes y anuladas) en [desde, hastaExclusivo), de un motorista o de todos (null). */
    List<com.maxsoft.application.dto.LiquidacionResumenDTO> getLiquidaciones(Date desde, Date hastaExclusivo,
            Integer codigoDelivery);

    List<com.maxsoft.application.dto.DetalleLiquidacionDTO> getDetalleLiquidacion(Integer codigoLiquidacion);

    /**
     * Anula una liquidación: sus facturas vuelven a quedar pendientes del
     * motorista, se anulan los recibos que generó, las facturas que pasó a
     * crédito vuelven a contado (salen del estado de cuenta), se reversa el
     * faltante cargado y el efectivo recibido sale de la caja abierta.
     *
     * @throws IllegalStateException ya anulada, sin caja o sin efectivo
     * suficiente, o algún cliente ya abonó a una factura que se pasó a crédito
     */
    LiquidacionDelivery anularLiquidacion(Integer codigoLiquidacion, String motivo, String usuario);

    // ============================================================
    // CUENTA DEL MOTORISTA (faltantes)
    // ============================================================
    /** Lo que debe el motorista por faltantes (negativo = saldo a su favor). */
    BigDecimal getDeudaMotorista(Integer codigoDelivery);

    /** Código de motorista -> deuda, solo los que tienen movimientos. */
    Map<Integer, BigDecimal> getDeudasMotoristas();

    /** Todos los motoristas con lo cargado, lo abonado y su saldo (los que no tienen movimientos salen en cero). */
    List<com.maxsoft.application.dto.CuentaMotoristaResumenDTO> getResumenCuentasMotoristas();

    /** Movimientos de la cuenta del motorista, los más nuevos primero. */
    List<com.maxsoft.application.modelo.CuentaMotorista> getCuentaMotorista(Integer codigoDelivery);

    /**
     * El motorista paga en efectivo parte o todo lo que debe: entra a la caja
     * abierta y rebaja su deuda. No puede pagar más de lo que debe.
     */
    com.maxsoft.application.modelo.CuentaMotorista registrarAbonoMotorista(Delivery motorista, BigDecimal monto,
            String nota, String usuario);

    /** Ticket (HTML de 80 mm) del recibo de un abono del motorista. */
    String generarComprobanteAbonoHtml(Integer codigoMovimiento);
}
