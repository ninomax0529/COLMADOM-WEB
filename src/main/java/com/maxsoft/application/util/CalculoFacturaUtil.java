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

    /**
     * Recalcula subtotal, descuento, ITBIS y total para una línea de detalle.Seguro ante valores nulos e imprecisiones decimales.
     * @param detalle
     */
    
    public static void calcularTotales(DetalleFacturaDeVenta detalle) {
        if (detalle == null) {
            return;
        }

        double cantidad = detalle.getCantidad() != null ? detalle.getCantidad().doubleValue() : 0.0;
        double precio = detalle.getPrecioVenta() != null ? detalle.getPrecioVenta().doubleValue() : 0.0;
        double pctDesc = detalle.getPorcientoDescuento() != null ? detalle.getPorcientoDescuento().doubleValue() : 0.0;
        double pctItbis = detalle.getPorcientoItbis() != null ? detalle.getPorcientoItbis().doubleValue() : 0.0;

        double subTotal = redondear(cantidad * precio);
        double totalDescuento = redondear(subTotal * (pctDesc / 100.0));
        double subTotalNeto = subTotal - totalDescuento;
        double totalItbis = redondear(subTotalNeto * (pctItbis / 100.0));
        double total = redondear(subTotalNeto + totalItbis);

        detalle.setSubTotal(BigDecimal.valueOf(subTotal));
        detalle.setTotalDescuento(BigDecimal.valueOf(totalDescuento));
        detalle.setItbis(BigDecimal.valueOf(totalItbis));
        detalle.setTotal(BigDecimal.valueOf(total));
    }

    /**
     * Redondeo financiero estandar a 2 decimales usando HALF_UP
     * @param valor
     * @return 
     */
    public static Double redondear(Double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}