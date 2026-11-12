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
}
