package com.maxsoft.application.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Costo con que una línea de entrada o salida valora el kardex, sin BD ni UI.
 *
 * <p>Los documentos de inventario guardan la cantidad en la PRESENTACIÓN
 * (caja, paquete...) y el kardex la valora como costo × cantidad, así que el
 * costo también tiene que ser el de la presentación. Si se usa el costo base,
 * una caja de 24 queda valorada como una sola unidad.
 */
public final class CostoInventarioUtil {

    private CostoInventarioUtil() {
    }

    /** Factor válido: el recibido si es mayor que cero, si no 1 (unidad base). */
    public static BigDecimal factor(BigDecimal factorConversion) {
        return factorConversion != null && factorConversion.signum() > 0 ? factorConversion : BigDecimal.ONE;
    }

    /**
     * Costo de la presentación: el de la línea si viene (> 0), que es el del
     * empaque elegido en la pantalla; si no, costo base × factor.
     */
    public static BigDecimal costoPresentacion(BigDecimal costoLinea, BigDecimal costoBase,
            BigDecimal factorConversion) {
        if (costoLinea != null && costoLinea.signum() > 0) {
            return costoLinea;
        }
        BigDecimal base = costoBase != null ? costoBase : BigDecimal.ZERO;
        return base.multiply(factor(factorConversion));
    }

    /**
     * Valor (2 decimales) de una línea repartida por lote, que trae su
     * cantidad exacta en unidad base: costo de la presentación ÷ factor ×
     * cantidad base. Así no se arrastra el redondeo de pasar la cantidad
     * base a presentación (1 unidad de una caja de 24 = 0.04 cajas).
     */
    public static BigDecimal valorPorCantidadBase(BigDecimal costoPresentacion, BigDecimal cantidadBase,
            BigDecimal factorConversion) {
        if (costoPresentacion == null || cantidadBase == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return costoPresentacion.multiply(cantidadBase).divide(factor(factorConversion), 2, RoundingMode.HALF_UP);
    }
}
