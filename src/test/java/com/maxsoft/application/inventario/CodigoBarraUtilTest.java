package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.util.CodigoBarraUtil;
import com.maxsoft.application.util.CodigoBarraUtil.Tipo;
import org.junit.jupiter.api.Test;

class CodigoBarraUtilTest {

    @Test
    void reconoceEanValidos() {
        assertEquals(Tipo.EAN13, CodigoBarraUtil.tipo("7501031311309"));
        assertEquals(Tipo.EAN13, CodigoBarraUtil.tipo(" 4006381333931 "));
        assertEquals(Tipo.EAN8, CodigoBarraUtil.tipo("96385074"));
        assertEquals(Tipo.UPCA, CodigoBarraUtil.tipo("036000291452"));
    }

    @Test
    void eanConDigitoMaloVaEnCode128() {
        assertEquals(Tipo.CODE128, CodigoBarraUtil.tipo("7501031311308"));
        assertEquals(Tipo.CODE128, CodigoBarraUtil.tipo("200000000001"));
    }

    @Test
    void codigosInternosVanEnCode128() {
        assertEquals(Tipo.CODE128, CodigoBarraUtil.tipo("7100"));
        assertEquals(Tipo.CODE128, CodigoBarraUtil.tipo("53"));
        assertEquals(Tipo.CODE128, CodigoBarraUtil.tipo("ABC-12"));
    }

    @Test
    void invalidos() {
        assertEquals(Tipo.INVALIDO, CodigoBarraUtil.tipo(null));
        assertEquals(Tipo.INVALIDO, CodigoBarraUtil.tipo("   "));
        assertEquals(Tipo.INVALIDO, CodigoBarraUtil.tipo("AÑO"));
        assertEquals(Tipo.INVALIDO, CodigoBarraUtil.tipo("1".repeat(CodigoBarraUtil.MAXIMO_CODE128 + 1)));
    }

    @Test
    void digitoVerificador() {
        assertEquals(9, CodigoBarraUtil.digitoVerificador("750103131130"));
        assertEquals(1, CodigoBarraUtil.digitoVerificador("400638133393"));
        assertTrue(CodigoBarraUtil.digitoValido("4006381333931"));
        assertFalse(CodigoBarraUtil.digitoValido("4006381333932"));
    }
}
