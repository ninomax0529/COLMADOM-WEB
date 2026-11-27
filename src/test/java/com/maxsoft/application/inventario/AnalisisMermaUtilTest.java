package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.maxsoft.application.util.AnalisisMermaUtil;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalisisMermaUtilTest {

    @Test
    void porcentajeConDosDecimales() {
        assertEquals(new BigDecimal("3.81"), AnalisisMermaUtil.porcentaje(new BigDecimal("250"), new BigDecimal("6555")));
        assertEquals(new BigDecimal("50.00"), AnalisisMermaUtil.porcentaje(new BigDecimal("5"), new BigDecimal("10")));
    }

    @Test
    void porcentajeSinBaseEsCero() {
        assertEquals(new BigDecimal("0.00"), AnalisisMermaUtil.porcentaje(new BigDecimal("250"), BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), AnalisisMermaUtil.porcentaje(new BigDecimal("250"), null));
        assertEquals(new BigDecimal("0.00"), AnalisisMermaUtil.porcentaje(null, new BigDecimal("10")));
    }

    @Test
    void acumuladoTerminaEnCien() {
        List<BigDecimal> acum = AnalisisMermaUtil.acumulado(
                List.of(new BigDecimal("60"), new BigDecimal("30"), new BigDecimal("10")));
        assertEquals(List.of(new BigDecimal("60.00"), new BigDecimal("90.00"), new BigDecimal("100.00")), acum);
    }

    @Test
    void acumuladoToleraNulosYListaVacia() {
        assertEquals(List.of(), AnalisisMermaUtil.acumulado(List.of()));
        assertEquals(List.of(new BigDecimal("100.00"), new BigDecimal("100.00")),
                AnalisisMermaUtil.acumulado(Arrays.asList(new BigDecimal("5"), null)));
    }
}
