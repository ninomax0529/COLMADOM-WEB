package com.maxsoft.application.dto;

import java.math.BigDecimal;

/**
 * Una fila del análisis de merma, agrupada por tipo o por artículo.
 *
 * @param codigo código del tipo de merma o del artículo
 * @param nombre descripción del tipo o del artículo
 * @param mermas cuántas mermas lo incluyen
 * @param cantidadBase cantidad perdida en unidad base (cero cuando se agrupa por tipo)
 * @param valor pérdida al costo, en RD$
 */
public record ResumenMermaDTO(Integer codigo, String nombre, long mermas, BigDecimal cantidadBase,
        BigDecimal valor) {
}
