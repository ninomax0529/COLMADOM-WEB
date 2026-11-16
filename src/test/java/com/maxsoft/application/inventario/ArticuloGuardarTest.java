package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.dto.ArticuloRegistroDTO;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Guardar la ficha del artículo conserva los datos de inventario (se revierte al final). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class ArticuloGuardarTest {

    @Autowired
    EntityManager em;
    @Autowired
    ArticuloService articuloService;
    @Autowired
    ArticuloEmpaqueService empaqueService;
    @Autowired
    ArticuloAlmacenService almacenService;

    @Test
    void guardaDatosDeInventario() {
        // Igual que ArticuloView -> RegistrarArticuloViewV4
        Articulo art = articuloService.getLista().stream().filter(a -> a.getCodigo() == 37).findFirst().orElseThrow();
        em.detach(art);
        art.setPerecedero(true);
        art.setDiasVencimiento(20);
        art.setControlaLote(true);
        art.setMetodoDeCosteo(1);
        art.setFechaVencimiento(java.sql.Date.valueOf("2027-06-30"));

        articuloService.guardarArticuloCompleto(new ArticuloRegistroDTO(art,
                new ArrayList<>(almacenService.buscarPorArticulo(37).get()),
                new ArrayList<>(empaqueService.getPorArticulo(37))));
        em.flush();
        em.clear();

        Object[] r = (Object[]) em.createNativeQuery("select perecedero, dias_vencimiento, controla_lote, metodo_de_costeo,"
                + " fecha_vencimiento from articulo where codigo = 37").getSingleResult();
        System.out.println(">>> BD: perecedero=" + r[0] + " dias=" + r[1] + " lote=" + r[2] + " metodo=" + r[3]);
        assertTrue(Boolean.TRUE.equals(r[0]) || "1".equals(String.valueOf(r[0])));
        assertEquals(20, ((Number) r[1]).intValue());
        assertTrue(Boolean.TRUE.equals(r[2]) || "1".equals(String.valueOf(r[2])));
        assertEquals(1, ((Number) r[3]).intValue());
        assertEquals("2027-06-30", String.valueOf(r[4]));
    }
}
