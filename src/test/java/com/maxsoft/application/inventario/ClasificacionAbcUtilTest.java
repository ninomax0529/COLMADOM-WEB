package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.util.ClasificacionAbcUtil;
import com.maxsoft.application.util.ClasificacionAbcUtil.ResultadoAbc;
import com.maxsoft.application.util.ClasificacionAbcUtil.VentaArticulo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Clasificación ABC (Pareto por valor y por rotación). Sin base de datos. */
class ClasificacionAbcUtilTest {

    private static final BigDecimal A = new BigDecimal("80");
    private static final BigDecimal B = new BigDecimal("95");

    @Test
    void paretoPorValor() {
        // Total 1000: 500 + 300 = 80 % (A), 100 + 50 hasta 95 % (B), el resto C
        Map<Integer, ResultadoAbc> r = clasificar(
                venta(1, "500", 10), venta(2, "300", 10), venta(3, "100", 10),
                venta(4, "50", 10), venta(5, "30", 10), venta(6, "20", 10));

        assertEquals("A", r.get(1).claseValor());
        assertEquals("A", r.get(2).claseValor());
        assertEquals("B", r.get(3).claseValor());
        assertEquals("B", r.get(4).claseValor());
        assertEquals("C", r.get(5).claseValor());
        assertEquals("C", r.get(6).claseValor());
        assertEquals(0, new BigDecimal("50").compareTo(r.get(1).porcientoValor()));
    }

    @Test
    void elQueCruzaElLimiteEntraEnLaClase() {
        // 700 (70 %) no llega al 80: el siguiente (200) lo cruza y también es A
        Map<Integer, ResultadoAbc> r = clasificar(
                venta(1, "700", 1), venta(2, "200", 1), venta(3, "100", 1));

        assertEquals("A", r.get(1).claseValor());
        assertEquals("A", r.get(2).claseValor());
        assertEquals("B", r.get(3).claseValor());
    }

    @Test
    void rotacionCuentaFacturasNoUnidades() {
        // Fósforo: 50 unidades en 1 factura. Pan: 1 unidad en 90 facturas.
        Map<Integer, ResultadoAbc> r = clasificar(
                new VentaArticulo(1, new BigDecimal("100"), 1, new BigDecimal("50"), false),
                new VentaArticulo(2, new BigDecimal("100"), 90, new BigDecimal("90"), false),
                new VentaArticulo(3, new BigDecimal("100"), 9, new BigDecimal("9"), false));

        assertEquals("A", r.get(2).claseRotacion());
        assertEquals("C", r.get(1).claseRotacion());
    }

    @Test
    void claseFinalCombinaLasDos() {
        assertEquals("A", ClasificacionAbcUtil.combinar("A", "C"), "deja dinero aunque se venda poco");
        assertEquals("A", ClasificacionAbcUtil.combinar("C", "A"), "se vende mucho aunque deje poco");
        assertEquals("B", ClasificacionAbcUtil.combinar("B", "C"));
        assertEquals("B", ClasificacionAbcUtil.combinar("B", "B"));
        assertEquals("C", ClasificacionAbcUtil.combinar("C", "C"));
    }

    @Test
    void sinVentasEsCSinMovimiento() {
        Map<Integer, ResultadoAbc> r = clasificar(venta(1, "1000", 50), venta(2, "0", 0));

        assertEquals("C", r.get(2).clase());
        assertTrue(r.get(2).sinMovimiento());
        assertFalse(r.get(1).sinMovimiento());
        assertEquals(0, BigDecimal.ZERO.compareTo(r.get(2).porcientoValor()));
    }

    @Test
    void nuevoNoQuedaEnCPeroSiPuedeSerA() {
        Map<Integer, ResultadoAbc> r = clasificar(
                venta(1, "900", 90),
                new VentaArticulo(2, BigDecimal.ZERO, 0, BigDecimal.ZERO, true),
                new VentaArticulo(3, new BigDecimal("2000"), 200, BigDecimal.ONE, true));

        assertEquals("N", r.get(2).clase(), "nuevo y sin ventas: N, no C");
        assertEquals("A", r.get(3).clase(), "nuevo que se vende mucho: A");
    }

    @Test
    void todoEnCeroNoDivideEntreCero() {
        Map<Integer, ResultadoAbc> r = clasificar(venta(1, "0", 0), venta(2, null, 0));

        assertEquals("C", r.get(1).clase());
        assertEquals("C", r.get(2).clase());
        assertTrue(r.get(2).sinMovimiento());
    }

    @Test
    void devolucionesDeMasCuentanComoCero() {
        Map<Integer, ResultadoAbc> r = clasificar(venta(1, "500", 5), venta(2, "-40", 1));

        assertEquals("C", r.get(2).claseValor());
        assertEquals("A", r.get(1).claseValor());
    }

    @Test
    void empatesSiempreIgualPorCodigo() {
        // Cinco iguales de 20 %: se ordenan por código (1..5). Antes del 4 van
        // 60 % -> A; el 5 empieza en 80 % -> B. No depende del orden de llegada.
        Map<Integer, ResultadoAbc> r = clasificar(venta(5, "20", 1), venta(1, "20", 1),
                venta(4, "20", 1), venta(2, "20", 1), venta(3, "20", 1));

        assertEquals("A", r.get(4).claseValor());
        assertEquals("B", r.get(5).claseValor());
    }

    @Test
    void limitesInvalidosUsanLosDeSiempre() {
        List<ResultadoAbc> r = ClasificacionAbcUtil.clasificar(
                List.of(venta(1, "500", 1), venta(2, "300", 1), venta(3, "200", 1)),
                new BigDecimal("95"), new BigDecimal("80"));

        // Mismo orden de entrada: artículos 1, 2, 3
        assertEquals("A", r.get(1).claseValor(), "con 80/95: 500 + 300 es A");
        assertEquals("B", r.get(2).claseValor(), "200 empieza en 80 %");
    }

    @Test
    void demandaDiariaPorDiasConDatos() {
        // 45 tomates en 8 días con datos
        assertEquals(new BigDecimal("5.6250"), ClasificacionAbcUtil.demandaDiaria(new BigDecimal("45"), 8));
        assertEquals(0, BigDecimal.ZERO.compareTo(ClasificacionAbcUtil.demandaDiaria(new BigDecimal("45"), 0)));
        assertEquals(0, BigDecimal.ZERO.compareTo(ClasificacionAbcUtil.demandaDiaria(null, 8)));
    }

    @Test
    void diasDeCobertura() {
        // 44 yucas, vende 6 al día: le alcanza para 7.3 días
        assertEquals(new BigDecimal("7.3"),
                ClasificacionAbcUtil.diasCobertura(new BigDecimal("44"), new BigDecimal("6")));
        assertEquals(new BigDecimal("0.0"),
                ClasificacionAbcUtil.diasCobertura(new BigDecimal("-2"), new BigDecimal("6")), "agotado");
        assertEquals(null, ClasificacionAbcUtil.diasCobertura(new BigDecimal("44"), BigDecimal.ZERO),
                "sin ventas no se puede saber");
    }

    @Test
    void minimoYMaximoSugeridosSegunLaClase() {
        ClasificacionAbcUtil.PoliticaStock p = ClasificacionAbcUtil.PoliticaStock.POR_DEFECTO;
        BigDecimal seisPorDia = new BigDecimal("6");

        // A: mínimo 6 x (7 + 3) = 60; máximo 60 + 6 x 7 = 102
        assertEquals(0, new BigDecimal("60").compareTo(ClasificacionAbcUtil.minimoSugerido(seisPorDia, "A", p)));
        assertEquals(0, new BigDecimal("102").compareTo(ClasificacionAbcUtil.maximoSugerido(seisPorDia, "A", p)));
        // C: mínimo 6 x (7 + 1) = 48; máximo 48 + 6 x 15 = 138
        assertEquals(0, new BigDecimal("48").compareTo(ClasificacionAbcUtil.minimoSugerido(seisPorDia, "C", p)));
        assertEquals(0, new BigDecimal("138").compareTo(ClasificacionAbcUtil.maximoSugerido(seisPorDia, "C", p)));
        // Nuevo usa los días de B: 6 x (7 + 2) = 54
        assertEquals(0, new BigDecimal("54").compareTo(ClasificacionAbcUtil.minimoSugerido(seisPorDia, "N", p)));
    }

    @Test
    void sugeridosRedondeanHaciaArribaYSinVentasNoSugiere() {
        ClasificacionAbcUtil.PoliticaStock p = ClasificacionAbcUtil.PoliticaStock.POR_DEFECTO;
        // 0.375 por día x 10 = 3.75 -> 4 unidades (no quedarse corto)
        assertEquals(0, new BigDecimal("4").compareTo(
                ClasificacionAbcUtil.minimoSugerido(new BigDecimal("0.375"), "A", p)));
        assertEquals(null, ClasificacionAbcUtil.minimoSugerido(BigDecimal.ZERO, "A", p));
        assertEquals(null, ClasificacionAbcUtil.maximoSugerido(null, "C", p));
    }

    @Test
    void porReponerPorMinimoOPorDias() {
        BigDecimal seis = new BigDecimal("6");
        // Mínimo 57: con 57 o menos hay que reponer
        assertTrue(ClasificacionAbcUtil.porReponer(new BigDecimal("57"), seis, new BigDecimal("57")));
        assertFalse(ClasificacionAbcUtil.porReponer(new BigDecimal("58"), seis, new BigDecimal("57")));
        // Sin mínimo: por días (18 / 6 = 3 días -> sí; 19 -> 3.2 días -> no)
        assertTrue(ClasificacionAbcUtil.porReponer(new BigDecimal("18"), seis, null));
        assertFalse(ClasificacionAbcUtil.porReponer(new BigDecimal("19"), seis, null));
        // Agotado o en negativo
        assertTrue(ClasificacionAbcUtil.porReponer(new BigDecimal("-3"), seis, null));
        // No vende: nunca
        assertFalse(ClasificacionAbcUtil.porReponer(BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("5")));
    }

    @Test
    void cantidadAPedirHastaElMaximo() {
        assertEquals(0, new BigDecimal("97").compareTo(
                ClasificacionAbcUtil.cantidadAPedir(BigDecimal.ZERO, new BigDecimal("97"))));
        assertEquals(0, new BigDecimal("16").compareTo(
                ClasificacionAbcUtil.cantidadAPedir(new BigDecimal("38"), new BigDecimal("54"))));
        assertEquals(0, new BigDecimal("97").compareTo(
                ClasificacionAbcUtil.cantidadAPedir(new BigDecimal("-4"), new BigDecimal("97"))), "negativo = 0");
        assertEquals(0, BigDecimal.ZERO.compareTo(
                ClasificacionAbcUtil.cantidadAPedir(new BigDecimal("200"), new BigDecimal("97"))), "sobra");
        assertEquals(0, BigDecimal.ZERO.compareTo(ClasificacionAbcUtil.cantidadAPedir(BigDecimal.TEN, null)));
    }

    private static VentaArticulo venta(int articulo, String ventas, long facturas) {
        return new VentaArticulo(articulo, ventas == null ? null : new BigDecimal(ventas),
                facturas, BigDecimal.ZERO, false);
    }

    private static Map<Integer, ResultadoAbc> clasificar(VentaArticulo... ventas) {
        return ClasificacionAbcUtil.clasificar(List.of(ventas), A, B).stream()
                .collect(Collectors.toMap(ResultadoAbc::articulo, Function.identity()));
    }
}
