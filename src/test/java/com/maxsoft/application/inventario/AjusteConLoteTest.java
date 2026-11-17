package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.TipoAjuste;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Ajuste de un artículo con lote: la línea del ajuste guarda el lote y el kardex numera la salida con su propio código. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class AjusteConLoteTest {

    @Autowired
    EntityManager em;
    @Autowired
    ArticuloRepo articuloRepo;
    @Autowired
    AjusteInventarioService ajusteService;

    @Test
    void ajusteGuardaLoteYSalidaTieneNumeroPropio() {
        // Incremento de 5 en almacén 1 (sin lote digitado = lote automático del día)
        AjusteInventario sube = ajuste(1, 5);
        ajusteService.procesarAjusteTransaccional(sube, new ArrayList<>(sube.getDetalleAjusteInventarioCollection()), "PRUEBA");
        // Decremento de 2 (FEFO)
        AjusteInventario baja = ajuste(2, 2);
        ajusteService.procesarAjusteTransaccional(baja, new ArrayList<>(baja.getDetalleAjusteInventarioCollection()), "PRUEBA");
        em.flush();
        em.clear();

        for (AjusteInventario a : List.of(sube, baja)) {
            Object[] f = (Object[]) em.createNativeQuery("select lote, numero_lote, sub_total from detalle_ajuste_inventario"
                    + " where ajuste_inventario = :a").setParameter("a", a.getCodigo()).getSingleResult();
            System.out.println(">>> Ajuste #" + a.getCodigo() + " lote=" + f[0] + " num=" + f[1] + " subtotal=" + f[2]);
            assertNotNull(f[0], "la línea del ajuste guarda el lote");
            assertNotNull(f[1]);
        }

        String numSalida = (String) em.createNativeQuery("select m.numero_documento from movimiento_inventario m"
                + " where m.tipo_movimiento = 2 order by m.codigo desc limit 1").getSingleResult();
        Number codSalida = (Number) em.createNativeQuery("select max(codigo) from salida_inventario").getSingleResult();
        System.out.println(">>> Kardex salida: " + numSalida + " (salida #" + codSalida + ")");
        assertEquals("SAL-" + codSalida, numSalida);
    }

    /** MANZANA ROJA (38, controla lote), empaque 82 unidad, almacén 1. */
    private AjusteInventario ajuste(int tipo, int cantidad) {
        Almacen alm = em.find(Almacen.class, 1);
        AjusteInventario a = new AjusteInventario();
        a.setFecha(new Date());
        a.setTipoAjuste(em.find(TipoAjuste.class, tipo));
        a.setAlmacen(alm);
        a.setNombreAlmacen(alm.getNombre());
        a.setUsuario(em.find(Usuario.class, 1));
        a.setNombreUsuario("PRUEBA");
        a.setObservacion("prueba lote");
        a.setAnulado(false);

        ArticuloEmpaque emp = em.find(ArticuloEmpaque.class, 82);
        DetalleAjusteInventario d = new DetalleAjusteInventario();
        d.setArticulo(articuloRepo.findById(38).orElseThrow());
        d.setDescripcionArticulo("MANZANA ROJA");
        d.setArticuloEmpaque(emp);
        d.setUnidad(emp.getUnidadEmpaque());
        d.setNombreUnidad(emp.getUnidadEmpaque().getDescripcion());
        d.setFactorConversion(BigDecimal.ONE);
        d.setCantidad(BigDecimal.valueOf(cantidad));
        d.setExistencia(BigDecimal.ZERO);
        d.setNuevaExistencia(BigDecimal.ZERO);
        d.setAlmacen(alm);
        d.setNombreAlmacen(alm.getNombre());
        d.setPrecioCompra(BigDecimal.valueOf(50));
        d.setCostoUnitario(BigDecimal.valueOf(50));
        d.setSubTotal(BigDecimal.valueOf(50L * cantidad));
        d.setItbis(BigDecimal.ZERO);
        d.setTotal(d.getSubTotal());
        a.setDetalleAjusteInventarioCollection(new ArrayList<>(List.of(d)));
        return a;
    }
}
