package com.maxsoft.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Resumen de lo que un motorista tiene pendiente de liquidar.
 *
 * @param pedidos cantidad de facturas en ruta sin liquidar
 * @param monto suma neta (total menos devuelto) de esas facturas
 */
public record PendientesMotoristaDTO(long pedidos, BigDecimal monto) implements Serializable {

    public static final PendientesMotoristaDTO VACIO = new PendientesMotoristaDTO(0, BigDecimal.ZERO);
}
