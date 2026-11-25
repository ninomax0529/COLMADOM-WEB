package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.util.ConteoCiclicoUtil;
import com.maxsoft.application.util.ConteoCiclicoUtil.Candidato;
import com.maxsoft.application.util.ConteoCiclicoUtil.Frecuencia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConteoCiclicoUtilTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 25);
    private static final Frecuencia F = Frecuencia.POR_DEFECTO;

    private static Candidato c(int articulo, String clase, Integer haceDias) {
        return new Candidato(articulo, clase, haceDias == null ? null : HOY.minusDays(haceDias));
    }

    @Test
    void tocaSegunLosDiasDeSuClase() {
        assertTrue(ConteoCiclicoUtil.tocaContar(c(1, "A", null), HOY, F), "nunca contado");
        assertTrue(ConteoCiclicoUtil.tocaContar(c(1, "A", 7), HOY, F));
        assertFalse(ConteoCiclicoUtil.tocaContar(c(1, "A", 6), HOY, F));
        assertFalse(ConteoCiclicoUtil.tocaContar(c(1, "B", 29), HOY, F));
        assertTrue(ConteoCiclicoUtil.tocaContar(c(1, "B", 30), HOY, F));
        assertTrue(ConteoCiclicoUtil.tocaContar(c(1, "N", 30), HOY, F), "nuevo usa los días de B");
        assertFalse(ConteoCiclicoUtil.tocaContar(c(1, "C", 89), HOY, F));
        assertTrue(ConteoCiclicoUtil.tocaContar(c(1, "C", 90), HOY, F));
        assertFalse(ConteoCiclicoUtil.tocaContar(c(1, "A", 0), HOY, F), "contado hoy");
    }

    @Test
    void nuncaContadosPrimeroYDespuesLosMasAtrasados() {
        List<Candidato> lista = List.of(
                c(1, "C", 180), // atraso 2.0
                c(2, "A", 14), // atraso 2.0, A gana el empate
                c(3, "B", null),
                c(4, "A", null),
                c(5, "A", 3), // no toca
                c(6, "B", 45)); // atraso 1.5
        List<Integer> orden = ConteoCiclicoUtil.paraContar(lista, HOY, F, 10).stream()
                .map(Candidato::articulo).toList();
        assertEquals(List.of(4, 3, 2, 1, 6), orden);
    }

    @Test
    void limitaLaCantidadDelDia() {
        List<Candidato> lista = List.of(c(1, "A", null), c(2, "A", null), c(3, "A", null));
        assertEquals(2, ConteoCiclicoUtil.paraContar(lista, HOY, F, 2).size());
        assertEquals(0, ConteoCiclicoUtil.paraContar(lista, HOY, F, 0).size());
        assertEquals(3, ConteoCiclicoUtil.cuantosTocan(lista, HOY, F));
    }

    @Test
    void exactitudDeLosConteos() {
        assertEquals(new BigDecimal("80.0"), ConteoCiclicoUtil.exactitud(10, 8));
        assertEquals(new BigDecimal("66.7"), ConteoCiclicoUtil.exactitud(3, 2));
        assertNull(ConteoCiclicoUtil.exactitud(0, 0));
    }
}
