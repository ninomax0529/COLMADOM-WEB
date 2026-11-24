package com.maxsoft.application.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.maxsoft.application.servicio.impl.inventario.ArticuloServiceImpl;
import org.junit.jupiter.api.Test;

/** Nombres de productos uniformes (mayúsculas y espacios). Sin base de datos. */
class NombreArticuloTest {

    @Test
    void mayusculasYSinEspaciosSobrantes() {
        assertEquals("LECHE RICA LISTA MIL", ArticuloServiceImpl.normalizarNombre("  leche rica  lista\tmil "));
        assertEquals("ARROZ LA GARZA DE 1LBS", ArticuloServiceImpl.normalizarNombre("ARROZ  LA GARZA DE  1LBS"));
    }

    @Test
    void conservaAcentosYEnie() {
        assertEquals("SOPITA DOÑA GALLINA", ArticuloServiceImpl.normalizarNombre("sopita doña gallina"));
        assertEquals("CAFÉ", ArticuloServiceImpl.normalizarNombre("café"));
    }

    @Test
    void nuloSigueNulo() {
        assertNull(ArticuloServiceImpl.normalizarNombre(null));
    }
}
