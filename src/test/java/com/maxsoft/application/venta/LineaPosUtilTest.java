package com.maxsoft.application.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.util.LineaPosUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Líneas del POS con multi-empaque (unidad / caja). Sin base de datos. */
class LineaPosUtilTest {

    private final Articulo cerveza = articulo(10, "100", "5");
    private final ArticuloEmpaque unidad = empaque(1, cerveza, "Unidad", "1", null);
    private final ArticuloEmpaque caja = empaque(2, cerveza, "Caja 24", "24", "2280");

    @Test
    void factorInvalidoOAusenteEsUno() {
        assertEquals(BigDecimal.ONE, LineaPosUtil.factorConversion(null));
        assertEquals(BigDecimal.ONE, LineaPosUtil.factorConversion(empaque(3, cerveza, "x", "0", null)));
        assertEquals(new BigDecimal("24"), LineaPosUtil.factorConversion(caja));
    }

    @Test
    void precioPropioDelEmpaqueOPrecioBasePorFactor() {
        assertEquals(new BigDecimal("2280"), LineaPosUtil.precioEmpaque(caja, caja), "la caja tiene precio propio");
        assertEquals(0, new BigDecimal("100").compareTo(LineaPosUtil.precioEmpaque(unidad, unidad)), "100 x 1");
        ArticuloEmpaque six = empaque(4, cerveza, "Six pack", "6", null);
        assertEquals(0, new BigDecimal("600").compareTo(LineaPosUtil.precioEmpaque(six, six)), "100 x 6");
    }

    @Test
    void nuevaLineaCalculaPrecioItbisYUnidadBase() {
        DetalleFacturaDeVenta d = LineaPosUtil.nuevaLinea(caja, null, new BigDecimal("2"), 1);
        assertEquals(new BigDecimal("2280"), d.getPrecioVenta());
        assertEquals(new BigDecimal("4560.00"), d.getTotal(), "2 cajas, ITBIS incluido");
        assertEquals(0, new BigDecimal("48").compareTo(d.getCantidadFisicaBase()), "2 x 24 unidades");
        assertEquals("Cerveza Caja 24", d.getDescripcionArticulo());
        assertEquals(LineaPosUtil.UNIDAD_POR_DEFECTO, d.getNombreUnidad());
        assertEquals(1, d.getNumeroDeLinea());
    }

    @Test
    void cantidadBaseSumaTodasLasPresentacionesDelArticulo() {
        List<DetalleFacturaDeVenta> lineas = new ArrayList<>();
        lineas.add(LineaPosUtil.nuevaLinea(unidad, null, new BigDecimal("5"), 1));
        lineas.add(LineaPosUtil.nuevaLinea(caja, null, new BigDecimal("1"), 2));
        assertEquals(0, new BigDecimal("29").compareTo(LineaPosUtil.cantidadBase(lineas, unidad)), "5 + 24");
        assertEquals(3, LineaPosUtil.siguienteNumeroLinea(lineas));
        assertTrue(LineaPosUtil.buscarLinea(lineas, caja).isPresent());
        assertTrue(LineaPosUtil.buscarLinea(lineas, empaque(9, cerveza, "x", "1", null)).isEmpty());
    }

    @Test
    void recalcularActualizaTotalYUnidadBase() {
        DetalleFacturaDeVenta d = LineaPosUtil.nuevaLinea(caja, null, BigDecimal.ONE, 1);
        d.setCantidad(new BigDecimal("3"));
        LineaPosUtil.recalcular(d);
        assertEquals(new BigDecimal("6840.00"), d.getTotal());
        assertEquals(0, new BigDecimal("72").compareTo(d.getCantidadFisicaBase()));
    }

    @Test
    void soloLibraPesadoOFraccionadoAceptanDecimales() {
        Articulo arroz = articulo(20, "35", "100");
        arroz.setUnidadDeVenta(new com.maxsoft.application.modelo.UnidadDeVenta(2, "LIBRA"));
        assertTrue(LineaPosUtil.permiteDecimales(empaque(6, arroz, "Libra", "1", null)));

        Articulo queso = articulo(21, "250", "10");
        queso.setUnidadDeVenta(new com.maxsoft.application.modelo.UnidadDeVenta(9, "Pesado"));
        assertTrue(LineaPosUtil.permiteDecimales(empaque(7, queso, "Pesado", "1", null)));

        Articulo aguacate = articulo(22, "50", "10");
        aguacate.setVentaAgranel(true);
        assertTrue(LineaPosUtil.permiteDecimales(empaque(8, aguacate, "Unidad", "1", null)), "a granel");

        cerveza.setUnidadDeVenta(new com.maxsoft.application.modelo.UnidadDeVenta(1, "UNIDAD"));
        org.junit.jupiter.api.Assertions.assertFalse(LineaPosUtil.permiteDecimales(unidad));
        org.junit.jupiter.api.Assertions.assertFalse(LineaPosUtil.permiteDecimales(caja));
    }

    @Test
    void abreviaturasDeUnidad() {
        assertEquals("UND", LineaPosUtil.abreviaturaUnidad("Unidad"));
        assertEquals("PAQ", LineaPosUtil.abreviaturaUnidad("PAQUETE"));
        assertEquals("FDO", LineaPosUtil.abreviaturaUnidad("FALDO"));
        assertEquals("LB", LineaPosUtil.abreviaturaUnidad("LIBRA"));
        assertEquals("CAJA", LineaPosUtil.abreviaturaUnidad("CAJA"));
        assertEquals("UND", LineaPosUtil.abreviaturaUnidad(null));
        assertEquals("BOTE", LineaPosUtil.abreviaturaUnidad("BOTELLA"));
    }

    @Test
    void detectaCantidadesConDecimales() {
        assertTrue(LineaPosUtil.tieneDecimales(new BigDecimal("2.5")));
        org.junit.jupiter.api.Assertions.assertFalse(LineaPosUtil.tieneDecimales(new BigDecimal("2.0000")));
        org.junit.jupiter.api.Assertions.assertFalse(LineaPosUtil.tieneDecimales(new BigDecimal("3")));
    }

    @Test
    void descripcionSinPresentacionNoDiceNull() {
        assertEquals("Cerveza", LineaPosUtil.descripcion(empaque(5, cerveza, null, "1", null)));
    }

    private static Articulo articulo(int codigo, String precio, String existencia) {
        Articulo a = new Articulo();
        a.setCodigo(codigo);
        a.setPrecioVenta(new BigDecimal(precio));
        a.setExistencia(new BigDecimal(existencia));
        return a;
    }

    private static ArticuloEmpaque empaque(int codigo, Articulo art, String descripcion, String factor, String precio) {
        ArticuloEmpaque e = new ArticuloEmpaque();
        e.setCodigo(codigo);
        e.setArticulo(art);
        e.setNombreArticulo("Cerveza");
        e.setDescripcionEmpaque(descripcion);
        e.setFactorConversion(new BigDecimal(factor));
        e.setPrecioVenta(precio != null ? new BigDecimal(precio) : null);
        return e;
    }
}
