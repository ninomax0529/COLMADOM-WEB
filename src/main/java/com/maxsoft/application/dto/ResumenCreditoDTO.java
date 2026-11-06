package com.maxsoft.application.dto;

import java.math.BigDecimal;

/**
 * Situación de crédito de un cliente.
 *
 * @param limite límite de crédito; null = sin límite definido
 * @param balance lo que debe hoy según su estado de cuenta
 * @param disponible cuánto más se le puede fiar; null = sin límite
 */
public record ResumenCreditoDTO(BigDecimal limite, BigDecimal balance, BigDecimal disponible) {

    public boolean tieneLimite() {
        return limite != null;
    }
}
