package com.maxsoft.application.venta;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.util.CalculoFacturaUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Cálculo de líneas de venta (ITBIS incluido, descuentos, redondeo). Sin base de datos. */
class CalculoFacturaUtilTest {

    @Test
    void descuentoSeRestaAntesDeDesglosarElItbis() {
        DetalleFacturaDeVenta d = linea("2", "50", "10", "18");
        CalculoFacturaUtil.calcularLineaItbisIncluido(d);
        assertEquals(new BigDecimal("100.00"), d.getSubTotal(), "2 x 50");
        assertEquals(new BigDecimal("10.00"), d.getTotalDescuento(), "10 % de 100");
        assertEquals(new BigDecimal("90.00"), d.getTotal(), "lo que paga el cliente");
        assertEquals(new BigDecimal("13.73"), d.getTotalItbis(), "90 - 90/1.18");
    }

    @Test
    void cantidadFraccionariaPorLibra() {
        DetalleFacturaDeVenta d = linea("1.5", "45.50", "0", "18");
        CalculoFacturaUtil.calcularLineaItbisIncluido(d);
        assertEquals(new BigDecimal("68.25"), d.getTotal());
        assertEquals(new BigDecimal("10.41"), d.getTotalItbis(), "68.25 - 68.25/1.18");
    }

    @Test
    void redondeaHalfUpADosDecimales() {
        DetalleFacturaDeVenta d = linea("3", "33.335", "0", "0");
        CalculoFacturaUtil.calcularLineaItbisIncluido(d);
        assertEquals(new BigDecimal("100.01"), d.getTotal(), "100.005 -> 100.01");
        assertEquals(new BigDecimal("100.01"), d.getSubTotal());
    }

    @Test
    void descuentoConCentavosSeRedondea() {
        DetalleFacturaDeVenta d = linea("7", "13.99", "5", "18");
        CalculoFacturaUtil.calcularLineaItbisIncluido(d);
        assertEquals(new BigDecimal("4.90"), d.getTotalDescuento(), "5 % de 97.93 = 4.8965");
        assertEquals(new BigDecimal("93.03"), d.getTotal(), "97.93 - 4.90");
        assertEquals(new BigDecimal("14.19"), d.getTotalItbis(), "93.03 - 93.03/1.18");
    }

    @Test
    void lineaSinDatosQuedaEnCero() {
        DetalleFacturaDeVenta d = new DetalleFacturaDeVenta();
        CalculoFacturaUtil.calcularLineaItbisIncluido(d);
        assertEquals(0, BigDecimal.ZERO.compareTo(d.getTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(d.getTotalItbis()));
        assertEquals(0, BigDecimal.ZERO.compareTo(d.getTotalDescuento()));
        assertDoesNotThrow(() -> CalculoFacturaUtil.calcularLineaItbisIncluido(null));
    }

    @Test
    void tasaPorDefectoCuandoNoHayArticuloOTasaCero() {
        assertEquals(0, CalculoFacturaUtil.ITBIS_DEFECTO.compareTo(CalculoFacturaUtil.tasaItbis(null)));
        Articulo a = new Articulo();
        a.setItbisGravado(BigDecimal.ZERO);
        assertEquals(0, CalculoFacturaUtil.ITBIS_DEFECTO.compareTo(CalculoFacturaUtil.tasaItbis(a)),
                "tasa 0 en un artículo gravado = 18 %");
    }

    private static DetalleFacturaDeVenta linea(String cantidad, String precio, String pctDesc, String tasa) {
        DetalleFacturaDeVenta d = new DetalleFacturaDeVenta();
        d.setCantidad(new BigDecimal(cantidad));
        d.setPrecioVenta(new BigDecimal(precio));
        d.setPorcientoDescuento(new BigDecimal(pctDesc));
        d.setPorcientoItbis(new BigDecimal(tasa));
        return d;
    }
}
