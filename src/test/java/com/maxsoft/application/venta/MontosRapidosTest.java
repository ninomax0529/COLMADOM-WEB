package com.maxsoft.application.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void efectivoMenorQueElTotalEsInsuficiente() {
        assertTrue(DialogoCobroEfectivo.efectivoInsuficiente(200.0, new java.math.BigDecimal("435")));
        assertTrue(DialogoCobroEfectivo.efectivoInsuficiente(87.49, new java.math.BigDecimal("87.50")));
    }

    @Test
    void efectivoExactoMayorOVacioEsValido() {
        assertFalse(DialogoCobroEfectivo.efectivoInsuficiente(null, new java.math.BigDecimal("435")));
        assertFalse(DialogoCobroEfectivo.efectivoInsuficiente(435.0, new java.math.BigDecimal("435.00")));
        assertFalse(DialogoCobroEfectivo.efectivoInsuficiente(500.0, new java.math.BigDecimal("435")));
        // El double 0.1+0.2 no debe rechazar un pago exacto de 0.30
        assertFalse(DialogoCobroEfectivo.efectivoInsuficiente(0.1 + 0.2, new java.math.BigDecimal("0.30")));
    }
}
