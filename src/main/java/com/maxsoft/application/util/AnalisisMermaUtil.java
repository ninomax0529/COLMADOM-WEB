package com.maxsoft.application.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Cálculos del análisis de merma, sin BD ni UI: el porcentaje de una parte
 * sobre el total y el porcentaje acumulado (Pareto) de una lista ya ordenada
 * de mayor a menor.
 */
public final class AnalisisMermaUtil {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private AnalisisMermaUtil() {
    }

    /**
     * Porcentaje de {@code parte} sobre {@code total}, con 2 decimales. Si el
     * total es cero, nulo o negativo devuelve cero (no hay base para comparar).
     */
    public static BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (parte == null || total == null || total.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return parte.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    /**
     * Porcentaje acumulado de cada valor sobre la suma de todos, en el orden
     * recibido (ordénelos de mayor a menor para leer el Pareto: los primeros
     * que llegan al 80 % son los que más pierden). El último queda en 100.
     */
    public static List<BigDecimal> acumulado(List<BigDecimal> valores) {
        BigDecimal total = valores.stream().map(v -> v != null ? v : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<BigDecimal> resultado = new ArrayList<>(valores.size());
        BigDecimal suma = BigDecimal.ZERO;
        for (BigDecimal v : valores) {
            suma = suma.add(v != null ? v : BigDecimal.ZERO);
            resultado.add(porcentaje(suma, total));
        }
        return resultado;
    }
}
