package com.maxsoft.application.util;

import java.math.BigDecimal;

/**
 * Cuándo hay que pasar mercancía de otro almacén (principal) al mostrador
 * (almacén de venta, del que vende el POS). Sin BD ni UI, con pruebas.
 */
public final class TrasladoMostradorUtil {

    private TrasladoMostradorUtil() {
    }

    /**
     * Toca trasladar si el mostrador llegó a su mínimo (o se agotó, si no
     * tiene mínimo) y en otro almacén hay existencia para reponerlo.
     *
     * @param mostrador existencia en el almacén de venta (unidad base)
     * @param minimo mínimo del artículo en el almacén de venta; null o 0 =
     * avisar solo al agotarse
     * @param enOtrosAlmacenes existencia positiva en los demás almacenes
     */
    public static boolean tocaTrasladar(BigDecimal mostrador, BigDecimal minimo, BigDecimal enOtrosAlmacenes) {
        if (enOtrosAlmacenes == null || enOtrosAlmacenes.signum() <= 0) {
            return false;
        }
        BigDecimal enMostrador = mostrador != null ? mostrador : BigDecimal.ZERO;
        BigDecimal limite = minimo != null && minimo.signum() > 0 ? minimo : BigDecimal.ZERO;
        return enMostrador.compareTo(limite) <= 0;
    }
}
