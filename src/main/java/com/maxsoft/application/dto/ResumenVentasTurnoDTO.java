package com.maxsoft.application.dto;

import java.math.BigDecimal;

/**
 * Ventas de un turno de caja para la barra de estado del POS: cantidad de
 * facturas no anuladas, total vendido (menos lo devuelto) y cuánto de eso fue
 * fiado.
 */
public record ResumenVentasTurnoDTO(long facturas, BigDecimal total, BigDecimal fiado) {
}
