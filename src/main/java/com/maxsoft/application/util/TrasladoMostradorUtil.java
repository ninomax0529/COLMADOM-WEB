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

    /**
     * Máximo que se pone por defecto en articulo_almacen cuando no se
     * configuró uno: desde aquí en adelante se trata como "sin máximo".
     */
    public static final BigDecimal SIN_MAXIMO = new BigDecimal("999999");

    /**
     * Cuánto pasar al mostrador (unidad base): hasta su máximo; si no tiene
     * máximo, hasta el doble del mínimo; si no tiene ninguno, todo lo que hay
     * en el almacén de origen. Nunca más de lo que hay en el origen.
     *
     * @param enOrigen existencia en el almacén del que se traslada
     */
    public static BigDecimal cantidadSugerida(BigDecimal mostrador, BigDecimal minimo, BigDecimal maximo,
            BigDecimal enOrigen) {
        if (enOrigen == null || enOrigen.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal enMostrador = mostrador != null ? mostrador : BigDecimal.ZERO;
        BigDecimal objetivo;
        if (maximo != null && maximo.signum() > 0 && maximo.compareTo(SIN_MAXIMO) < 0) {
            objetivo = maximo;
        } else if (minimo != null && minimo.signum() > 0) {
            objetivo = minimo.multiply(BigDecimal.valueOf(2));
        } else {
            return enOrigen;
        }
        BigDecimal falta = objetivo.subtract(enMostrador);
        if (falta.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return falta.min(enOrigen);
    }
}
