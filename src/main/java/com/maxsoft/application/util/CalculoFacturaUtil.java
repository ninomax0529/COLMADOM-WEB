/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.util;


import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CalculoFacturaUtil {

    private CalculoFacturaUtil() {
        // Constructor privado para evitar instancias (Clase de Utilidad)
    }

    /** Tasa de ITBIS cuando el artículo no tiene una propia. */
    public static final BigDecimal ITBIS_DEFECTO = BigDecimal.valueOf(18);

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    /**
     * Tasa de ITBIS (%) del artículo: 0 si está exento; si no, su
     * itbis_gravado o, si no tiene, el 18 %.
     */
    public static BigDecimal tasaItbis(com.maxsoft.application.modelo.Articulo articulo) {
        if (articulo == null) {
            return ITBIS_DEFECTO;
        }
        if (articulo.getExentoItbis()) {
            return BigDecimal.ZERO;
        }
        BigDecimal propia = articulo.getItbisGravado();
        return propia != null && propia.signum() > 0 ? propia : ITBIS_DEFECTO;
    }

    /**
     * Línea de venta con el ITBIS INCLUIDO en el precio (lo que se cobra es
     * cantidad × precio − descuento):
     * <ul>
     * <li>total = cantidad × precio − descuento (lo que paga el cliente)</li>
     * <li>totalItbis = total − total / (1 + tasa/100); 0 si está exento</li>
     * <li>subTotal = cantidad × precio (como siempre; lo imprime la factura)</li>
     * </ul>
     * La base sin ITBIS es total − totalItbis (la cabecera la guarda en su subTotal).
     * Usa la tasa que ya tenga la línea en porcientoItbis.
     */
    public static void calcularLineaItbisIncluido(DetalleFacturaDeVenta d) {
        if (d == null) {
            return;
        }
        BigDecimal cantidad = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        BigDecimal precio = d.getPrecioVenta() != null ? d.getPrecioVenta() : BigDecimal.ZERO;
        BigDecimal pctDesc = d.getPorcientoDescuento() != null ? d.getPorcientoDescuento() : BigDecimal.ZERO;
        BigDecimal tasa = d.getPorcientoItbis() != null ? d.getPorcientoItbis() : BigDecimal.ZERO;

        BigDecimal importe = cantidad.multiply(precio);
        BigDecimal descuento = importe.multiply(pctDesc).divide(CIEN, 2, RoundingMode.HALF_UP);
        BigDecimal total = importe.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = tasa.signum() > 0
                ? total.subtract(total.multiply(CIEN).divide(CIEN.add(tasa), 2, RoundingMode.HALF_UP))
                : BigDecimal.ZERO.setScale(2);

        d.setTotalDescuento(descuento);
        d.setTotalItbis(itbis);
        d.setSubTotal(importe.setScale(2, RoundingMode.HALF_UP));
        d.setTotal(total);
    }
}