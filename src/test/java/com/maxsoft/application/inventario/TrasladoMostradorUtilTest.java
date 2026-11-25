package com.maxsoft.application.inventario;

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
}
