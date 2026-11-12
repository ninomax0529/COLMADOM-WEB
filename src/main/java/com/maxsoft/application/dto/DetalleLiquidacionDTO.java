package com.maxsoft.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Una factura dentro de una liquidación de delivery.
 *
 * @param tipoPago CONTADO o FIADO (como quedó al liquidar)
 * @param clienteCredito cliente a quien se pasó a crédito al liquidar, o null
 */
public record DetalleLiquidacionDTO(
        Integer codigoFactura,
        Date fechaFactura,
        String cliente,
        String tipoPago,
        BigDecimal monto,
        String clienteCredito) implements Serializable {
}
