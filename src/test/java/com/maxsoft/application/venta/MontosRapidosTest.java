package com.maxsoft.application.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.view.componente.pos.DialogoCobroEfectivo;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Botones de billetes rápidos del cobro en efectivo. Sin base de datos. */
class MontosRapidosTest {

    private static List<String> montos(String total) {
        return DialogoCobroEfectivo.montosRapidos(new BigDecimal(total)).stream()
                .map(m -> m.stripTrailingZeros().toPlainString())
                .toList();
    }

    @Test
    void totalComunRedondeaACadaBillete() {
        assertEquals(List.of("435", "450", "500", "1000", "2000"), montos("435"));
    }

    @Test
    void totalConCentavos() {
        assertEquals(List.of("87.5", "100", "200", "500", "1000", "2000"), montos("87.50"));
    }

    @Test
    void totalRedondoNoRepiteElExacto() {
        assertEquals(List.of("500", "1000", "2000"), montos("500"));
    }

    @Test
    void totalMayorQueTodosLosBilletes() {
        assertEquals(List.of("2340", "2350", "2400", "2500", "3000"), montos("2340"));
    }

    @Test
    void totalCeroNoMuestraBotones() {
        assertTrue(montos("0").isEmpty());
    }
}
