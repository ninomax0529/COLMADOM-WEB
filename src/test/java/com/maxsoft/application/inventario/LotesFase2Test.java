package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.LoteAlmacenRepo;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de la Fase 2 de lotes contra la BD real. Cada prueba corre en una
 * transacción que se revierte al final: no deja datos. No supone un estado
 * fijo: compara contra lo que haya en la BD al empezar.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class LotesFase2Test {

    @Autowired
    EntityManager em;
    @Autowired
    ArticuloRepo articuloRepo;
    @Autowired
    LoteService loteService;
    @Autowired
    LoteAlmacenRepo loteAlmacenRepo;
    @Autowired
    MovimientoInventarioService movimientoService;

    private Almacen alm1;
    private Almacen alm2;
    private TipoMovimiento entrada;
    private TipoMovimiento salida;
    private TipoDocumento docEntrada;
    private TipoDocumento docVenta;

    @BeforeEach
    void cargar() {
        alm1 = em.find(Almacen.class, 1);
        alm2 = em.find(Almacen.class, 2);
        entrada = em.find(TipoMovimiento.class, 1);
        salida = em.find(TipoMovimiento.class, 2);
        docEntrada = em.find(TipoDocumento.class, 1);
        docVenta = em.find(TipoDocumento.class, 5);
    }

    @Test
    void articuloSinControlDeLoteFuncionaComoAntes() {
        Articulo art = articuloRepo.findById(3).orElseThrow();
        assertTrue(!art.isControlaLote());
        BigDecimal antes = existencia(3, 1);
        List<MovimientoInventario> v = movimientoService.registrarMovimientoConLote(art, alm1, null, BigDecimal.ONE,
                bd(1), null, null, null, salida, docVenta, "TEST-SL", bd(1), "PRUEBA", "sin lote", null);
        System.out.println(">>> Sin lote: " + v.size() + " fila, lote=" + v.get(0).getLote()
                + " existencia " + antes + " -> " + existencia(3, 1));
        assertEquals(1, v.size());
        assertNull(v.get(0).getLote());
        assertEquals(0, antes.subtract(bd(1)).compareTo(existencia(3, 1)));
    }

    @Test
    void autoCuadreDeArticuloActivadoSinLotes() {
        // LECHE NIDO (5) quedó con controla_lote = 1 y sin lotes
        Articulo art = articuloRepo.findById(5).orElseThrow();
        BigDecimal ex1 = existencia(5, 1);
        BigDecimal sumaAntes = loteAlmacenRepo.sumarExistencia(5, 1);
        movimientoService.registrarMovimientoConLote(art, alm1, null, BigDecimal.ONE, bd(1), null, null, null,
                salida, docVenta, "TEST-AC", bd(1), "PRUEBA", "auto-cuadre", null);
        BigDecimal sumaDespues = loteAlmacenRepo.sumarExistencia(5, 1);
        System.out.println(">>> Auto-cuadre LECHE NIDO alm1: existencia " + ex1 + " suma lotes antes=" + sumaAntes
                + " después de vender 1: existencia=" + existencia(5, 1) + " suma lotes=" + sumaDespues);
        assertEquals(0, existencia(5, 1).compareTo(sumaDespues));
    }

    @Test
    void flujoCompletoDeLotes() {
        final int A = 4;
        Articulo art = articuloRepo.findById(A).orElseThrow();
        art.setControlaLote(true);
        articuloRepo.saveAndFlush(art);

        // 1. Sincronizar: los lotes deben sumar la existencia de cada almacén
        int ajustados = loteService.sincronizarLotes(art, "PRUEBA");
        System.out.println(">>> 1. " + art.getDescripcion() + " alm1=" + existencia(A, 1) + " alm2=" + existencia(A, 2)
                + " | ajustados al sincronizar=" + ajustados);
        assertCuadra(A);
        assertEquals(0, loteService.sincronizarLotes(art, "PRUEBA"), "segunda sincronización no ajusta nada");

        // 2. Entrada con lote indicado (vence en 5 días) y entrada sin lote (lote del día)
        Lote loteA = loteService.obtenerOCrearLote(art, "prueba-a", LocalDate.now().plusDays(5),
                null, null, "TEST", "PRUEBA");
        movimientoService.registrarMovimientoConLote(art, alm1, null, BigDecimal.ONE, bd(10), bd(100), bd(0), bd(100),
                entrada, docEntrada, "TEST-E1", bd(10), "PRUEBA", "entrada lote A", loteA);
        BigDecimal diaAntes = BigDecimal.ZERO;
        List<MovimientoInventario> e2 = movimientoService.registrarMovimientoConLote(art, alm1, null, BigDecimal.ONE,
                bd(8), bd(80), bd(0), bd(80), entrada, docEntrada, "TEST-E2", bd(8), "PRUEBA", "entrada sin lote", null);
        Lote loteDia = e2.get(0).getLote();
        diaAntes = e2.get(0).getExistenciaLoteAnterior();
        System.out.println(">>> 2. Lote " + loteA.getNumeroLote() + "=" + exLote(loteA, alm1)
                + " | lote del día " + loteDia.getNumeroLote() + "=" + exLote(loteDia, alm1));
        assertEquals("PRUEBA-A", loteA.getNumeroLote());
        assertEquals("L" + LocalDate.now().toString().replace("-", ""), loteDia.getNumeroLote());
        assertCuadra(A);

        // 3. Venta de lo que hay en A + 5: FEFO -> todo el lote A (vence primero) y 5 de los sin vencimiento
        BigDecimal enA = exLote(loteA, alm1);
        BigDecimal pedir = enA.add(bd(5));
        List<MovimientoInventario> v1 = movimientoService.registrarMovimientoConLote(art, alm1, null, BigDecimal.ONE,
                pedir, bd(150), bd(27), bd(177), salida, docVenta, "TEST-V1", pedir, "PRUEBA", "venta FEFO", null);
        imprimir("3. Venta " + pedir + " (FEFO)", v1);
        assertEquals(loteA, v1.get(0).getLote());
        assertEquals(0, enA.compareTo(v1.get(0).getCantidad()));
        BigDecimal sumaCant = v1.stream().map(MovimientoInventario::getCantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumaTotal = v1.stream().map(MovimientoInventario::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, pedir.compareTo(sumaCant));
        assertEquals(0, bd(177).compareTo(sumaTotal), "total prorrateado sin perder centavos");
        for (int i = 1; i < v1.size(); i++) {
            assertEquals(0, v1.get(i - 1).getExistenciaNueva().compareTo(v1.get(i).getExistenciaAnterior()),
                    "kardex encadenado");
        }
        assertCuadra(A);

        // 4. Lote vencido: aunque venza primero, FEFO lo deja de último si hay vigentes
        Lote vencido = loteService.obtenerOCrearLote(art, "PRUEBA-V", LocalDate.now().minusDays(3),
                null, null, "TEST", "PRUEBA");
        movimientoService.registrarMovimientoConLote(art, alm1, null, BigDecimal.ONE, bd(4), null, null, null,
                entrada, docEntrada, "TEST-E3", bd(4), "PRUEBA", "entrada lote vencido", vencido);
        List<MovimientoInventario> v2 = movimientoService.registrarMovimientoConLote(art, alm1, null, BigDecimal.ONE,
                bd(1), null, null, null, salida, docVenta, "TEST-V2", bd(1), "PRUEBA", "venta con vencido presente", null);
        imprimir("4. Venta 1 con lote vencido presente", v2);
        assertNotEquals(vencido, v2.get(0).getLote(), "no debe tomar el vencido si hay vigente");

        // 5. Salida de un lote indicado que no alcanza (el almacén sí tiene) -> error del lote
        BigDecimal enVencido = exLote(vencido, alm1);
        BigDecimal pedirLote = enVencido.add(BigDecimal.ONE);
        assertTrue(existencia(A, 1).compareTo(pedirLote) >= 0, "el almacén debe alcanzar para probar el lote");
        IllegalStateException err = assertThrows(IllegalStateException.class, () -> movimientoService
                .registrarMovimientoConLote(art, alm1, null, BigDecimal.ONE, pedirLote, null, null, null, salida,
                        docVenta, "TEST-V3", pedirLote, "PRUEBA", "lote indicado sin suficiente", vencido));
        System.out.println(">>> 5. Error esperado: " + err.getMessage());
        assertTrue(err.getMessage().startsWith("El lote PRUEBA-V"));

        // 6. Venta sin existencia en alm2: lo que falta va a SIN-LOTE (negativo)
        art.setPermitirVentaSinExistencia(true);
        articuloRepo.saveAndFlush(art);
        BigDecimal pedir2 = existencia(A, 2).add(bd(4));
        List<MovimientoInventario> v4 = movimientoService.registrarMovimientoConLote(art, alm2, null, BigDecimal.ONE,
                pedir2, null, null, null, salida, docVenta, "TEST-V4", pedir2, "PRUEBA", "venta sin existencia", null);
        imprimir("6. Venta " + pedir2 + " en alm2 (4 más de lo que hay)", v4);
        Lote sinLote = v4.get(v4.size() - 1).getLote();
        assertEquals(LoteService.NUMERO_SIN_LOTE, sinLote.getNumeroLote());
        assertEquals(0, bd(-4).compareTo(existencia(A, 2)));
        assertCuadra(A);

        // 7. Estado final de los lotes del artículo
        for (LoteAlmacen la : loteService.getExistenciasPorArticulo(A)) {
            System.out.println("    lote " + la.getLote().getNumeroLote() + " vence=" + la.getLote().getFechaVencimiento()
                    + " almacén=" + la.getAlmacen().getCodigo() + " existencia=" + la.getExistencia());
        }
        System.out.println(">>> TODO OK (se revierte al terminar)");
    }

    private void assertCuadra(int articulo) {
        em.flush();
        List<Object[]> descuadres = loteService.getDescuadres();
        assertTrue(descuadres.stream().noneMatch(r -> ((Number) r[0]).intValue() == articulo),
                "existencia del almacén distinta a la suma de lotes");
    }

    private void imprimir(String titulo, List<MovimientoInventario> movs) {
        System.out.println(">>> " + titulo + ": " + movs.size() + " fila(s) de kardex");
        for (MovimientoInventario m : movs) {
            System.out.println("    lote=" + (m.getLote() == null ? "-" : m.getLote().getNumeroLote())
                    + " cant=" + m.getCantidad() + " alm " + m.getExistenciaAnterior() + "->" + m.getExistenciaNueva()
                    + " lote " + m.getExistenciaLoteAnterior() + "->" + m.getExistenciaLoteNueva()
                    + " total=" + m.getTotal());
        }
    }

    private BigDecimal exLote(Lote lote, Almacen alm) {
        return loteAlmacenRepo.findByLoteCodigoAndAlmacenCodigo(lote.getCodigo(), alm.getCodigo())
                .map(LoteAlmacen::getExistencia).orElse(BigDecimal.ZERO);
    }

    private BigDecimal existencia(int articulo, int almacen) {
        em.flush();
        return (BigDecimal) em.createNativeQuery("select existencia from articulo_almacen where articulo = :a and almacen = :b")
                .setParameter("a", articulo).setParameter("b", almacen).getSingleResult();
    }

    private static BigDecimal bd(int v) {
        return BigDecimal.valueOf(v);
    }
}
