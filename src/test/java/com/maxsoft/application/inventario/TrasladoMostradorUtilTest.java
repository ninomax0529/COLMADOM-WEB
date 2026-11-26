package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.util.TrasladoMostradorUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TrasladoMostradorUtilTest {

    private static boolean toca(String mostrador, String minimo, String otros) {
        return TrasladoMostradorUtil.tocaTrasladar(
                mostrador == null ? null : new BigDecimal(mostrador),
                minimo == null ? null : new BigDecimal(minimo),
                otros == null ? null : new BigDecimal(otros));
    }

    @Test
    void agotadoEnMostradorConExistenciaAtras() {
        // LECHE: 0 en el mostrador, sin mínimo, 3 en el principal
        assertTrue(toca("0", "0", "3"));
        assertTrue(toca(null, null, "3"));
        // Vendida en negativo también toca
        assertTrue(toca("-2", "0", "3"));
    }

    @Test
    void llegoAlMinimoDelMostrador() {
        // FOSFORO: 2 en el mostrador, mínimo 20, 180 en el principal
        assertTrue(toca("2", "20", "180"));
        // Justo en el mínimo
        assertTrue(toca("20", "20", "180"));
        // Por encima del mínimo no
        assertFalse(toca("21", "20", "180"));
        // Sin mínimo y con existencia no
        assertFalse(toca("5", "0", "180"));
    }

    @Test
    void sinExistenciaEnOtroAlmacenNoAvisa() {
        assertFalse(toca("0", "10", "0"));
        assertFalse(toca("0", "10", null));
        assertFalse(toca("0", "10", "-1"));
    }

    private static String sugerido(String mostrador, String minimo, String maximo, String origen) {
        return TrasladoMostradorUtil.cantidadSugerida(
                mostrador == null ? null : new BigDecimal(mostrador),
                minimo == null ? null : new BigDecimal(minimo),
                maximo == null ? null : new BigDecimal(maximo),
                origen == null ? null : new BigDecimal(origen))
                .stripTrailingZeros().toPlainString();
    }

    @Test
    void sugeridoHastaElMaximoSinPasarDelOrigen() {
        // Máximo 50, hay 2: faltan 48 y en el principal hay 180
        assertEquals("48", sugerido("2", "20", "50", "180"));
        // Faltan 48 pero en el principal solo hay 10
        assertEquals("10", sugerido("2", "20", "50", "10"));
    }

    @Test
    void sinMaximoUsaElDobleDelMinimo() {
        // FOSFORO: mínimo 20, máximo 999999 (sin máximo): hasta 40, hay 2
        assertEquals("38", sugerido("2", "20", "999999", "180"));
        // CAFE: mínimo 10, hasta 20, hay 7: faltan 13 pero solo hay 7 atrás
        assertEquals("7", sugerido("7", "10", "999999", "7"));
    }

    @Test
    void sinMinimoNiMaximoPasaTodoLoDelOrigen() {
        // LECHE: sin mínimo ni máximo, 3 en el principal
        assertEquals("3", sugerido("0", "0", "999999", "3"));
        assertEquals("3", sugerido(null, null, null, "3"));
    }

    @Test
    void sinNadaQuePasar() {
        assertEquals("0", sugerido("0", "10", "50", "0"));
        // Ya está en el máximo
        assertEquals("0", sugerido("50", "10", "50", "180"));
    }
}
