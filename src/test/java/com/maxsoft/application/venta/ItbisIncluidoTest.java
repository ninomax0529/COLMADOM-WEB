package com.maxsoft.application.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.util.CalculoFacturaUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** ITBIS incluido en el precio y artículos exentos (sin base de datos). */
class ItbisIncluidoTest {

    @Test
    void tasaSegunFicha() {
        Articulo gravado = new Articulo();
        assertEquals(0, BigDecimal.valueOf(18).compareTo(CalculoFacturaUtil.tasaItbis(gravado)), "sin tasa = 18 %");
        gravado.setItbisGravado(BigDecimal.valueOf(16));
        assertEquals(0, BigDecimal.valueOf(16).compareTo(CalculoFacturaUtil.tasaItbis(gravado)));
        Articulo exento = new Articulo();
        exento.setExentoItbis(true);
        exento.setItbisGravado(BigDecimal.valueOf(18));
        assertEquals(0, BigDecimal.ZERO.compareTo(CalculoFacturaUtil.tasaItbis(exento)), "exento = 0");
    }

    @Test
    void lineaGravadaDesglosaElItbisDentroDelPrecio() {
        DetalleFacturaDeVenta d = linea(1, "70", "18");
        CalculoFacturaUtil.calcularLineaItbisIncluido(d);
        System.out.println(">>> Gravado: sub=" + d.getSubTotal() + " itbis=" + d.getTotalItbis() + " total=" + d.getTotal());
        assertEquals(new BigDecimal("70.00"), d.getTotal(), "se cobra el precio");
        assertEquals(new BigDecimal("70.00"), d.getSubTotal());
        assertEquals(new BigDecimal("10.68"), d.getTotalItbis(), "70 - 70/1.18");
    }

    @Test
    void lineaExentaNoLlevaItbis() {
        DetalleFacturaDeVenta d = linea(3, "35", "0");
        CalculoFacturaUtil.calcularLineaItbisIncluido(d);
        System.out.println(">>> Exento: sub=" + d.getSubTotal() + " itbis=" + d.getTotalItbis() + " total=" + d.getTotal());
        assertEquals(new BigDecimal("105.00"), d.getTotal());
        assertEquals(0, BigDecimal.ZERO.compareTo(d.getTotalItbis()));
    }

    private static DetalleFacturaDeVenta linea(int cantidad, String precio, String tasa) {
        DetalleFacturaDeVenta d = new DetalleFacturaDeVenta();
        d.setCantidad(BigDecimal.valueOf(cantidad));
        d.setPrecioVenta(new BigDecimal(precio));
        d.setPorcientoDescuento(BigDecimal.ZERO);
        d.setPorcientoItbis(new BigDecimal(tasa));
        return d;
    }
}
