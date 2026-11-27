package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.maxsoft.application.util.CostoInventarioUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CostoInventarioUtilTest {

    @Test
    void usaElCostoDeLaLineaSiViene() {
        assertEquals(new BigDecimal("240"),
                CostoInventarioUtil.costoPresentacion(new BigDecimal("240"), new BigDecimal("10"), new BigDecimal("24")));
    }

    @Test
    void sinCostoDeLineaUsaBasePorFactor() {
        assertEquals(new BigDecimal("240"),
                CostoInventarioUtil.costoPresentacion(BigDecimal.ZERO, new BigDecimal("10"), new BigDecimal("24")));
        assertEquals(new BigDecimal("240"),
                CostoInventarioUtil.costoPresentacion(null, new BigDecimal("10"), new BigDecimal("24")));
    }

    @Test
    void factorInvalidoEsUno() {
        assertEquals(new BigDecimal("10"), CostoInventarioUtil.costoPresentacion(null, new BigDecimal("10"), null));
        assertEquals(new BigDecimal("10"),
                CostoInventarioUtil.costoPresentacion(null, new BigDecimal("10"), BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, CostoInventarioUtil.costoPresentacion(null, null, new BigDecimal("24")));
    }

    @Test
    void valorPorCantidadBaseNoArrastraRedondeo() {
        // 1 unidad de una caja de 24 a RD$240: RD$10 (con 0.04 cajas daría RD$9.60)
        assertEquals(new BigDecimal("10.00"),
                CostoInventarioUtil.valorPorCantidadBase(new BigDecimal("240"), BigDecimal.ONE, new BigDecimal("24")));
        assertEquals(new BigDecimal("50.00"),
                CostoInventarioUtil.valorPorCantidadBase(new BigDecimal("10"), new BigDecimal("5"), BigDecimal.ONE));
        assertEquals(new BigDecimal("0.00"),
                CostoInventarioUtil.valorPorCantidadBase(null, new BigDecimal("5"), BigDecimal.ONE));
    }
}
