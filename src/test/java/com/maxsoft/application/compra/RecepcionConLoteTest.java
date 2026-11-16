package com.maxsoft.application.compra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.CondicionPago;
import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.EstadoDocumento;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Recepción de un artículo con lote: el lote digitado o el automático queda guardado (se revierte al final). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class RecepcionConLoteTest {

    @Autowired
    EntityManager em;
    @Autowired
    ArticuloRepo articuloRepo;
    @Autowired
    RecepcionMercanciaService recepcionService;

    @Test
    void loteDigitadoYLoteAutomaticoQuedanGuardados() {
        LocalDate vence = LocalDate.now().plusDays(40);
        RecepcionMercancia rec = recepcion(
                linea("PRUEBA-REC-1", vence, 4),
                linea(null, null, 3));

        RecepcionMercancia guardada = recepcionService.procesarRecepcion(rec);
        em.flush();
        em.clear();

        List<Object[]> filas = em.createNativeQuery("select d.numero_lote, d.fecha_vencimiento, l.numero_lote,"
                + " l.fecha_vencimiento from detalle_recepcion_mercancia d join lote l on l.codigo = d.lote"
                + " where d.recepcion_mercancia = :r order by d.codigo").setParameter("r", guardada.getCodigo())
                .getResultList();
        for (Object[] f : filas) {
            System.out.println(">>> Recepción: línea lote=" + f[0] + " vence=" + f[1] + " | lote BD " + f[2] + " vence " + f[3]);
        }
        assertEquals(2, filas.size(), "las dos líneas tienen lote");
        assertEquals("PRUEBA-REC-1", filas.get(0)[0]);
        assertEquals(vence.toString(), String.valueOf(filas.get(0)[1]));
        assertNotNull(filas.get(1)[0], "el lote automático también queda escrito en la línea");
        assertTrue(String.valueOf(filas.get(1)[0]).startsWith("L"));

        Number entradas = (Number) em.createNativeQuery("select count(*) from detalle_entrada_inventario"
                + " where numero_lote = 'PRUEBA-REC-1' and lote is not null").getSingleResult();
        assertEquals(1, entradas.intValue(), "la entrada de inventario también guarda el lote");
    }

    /** Como la pantalla: entidades desconectadas y pago de contado. */
    @Test
    void recepcionDeContadoConEntidadesDesconectadas() {
        RecepcionMercancia rec = recepcion(linea("PRUEBA-REC-2", LocalDate.now().plusDays(30), 2), linea(null, null, 1));
        rec.setCondicionPago(em.find(CondicionPago.class, 1));
        em.clear();
        RecepcionMercancia guardada = recepcionService.procesarRecepcion(rec);
        em.flush();
        System.out.println(">>> Recepción de contado OK #" + guardada.getCodigo());
    }

    private RecepcionMercancia recepcion(DetalleRecepcionMercancia... lineas) {
        RecepcionMercancia rec = new RecepcionMercancia();
        rec.setFecha(new Date());
        rec.setTipoDocumento(em.find(TipoDocumento.class, 10));
        rec.setNombreDocumento("PRUEBA");
        EstadoDocumento estado = em.find(EstadoDocumento.class, 2);
        rec.setEstadoDocumento(estado);
        rec.setNombreEstado(estado.getNombre());
        rec.setAlmacen(em.find(Almacen.class, 1));
        Proveedor prov = em.find(Proveedor.class, 8);
        rec.setProveedor(prov);
        rec.setNombreProveedor(prov.getNombre());
        rec.setCondicionPago(em.find(CondicionPago.class, 2));
        rec.setNombreUsuario("PRUEBA");
        rec.setUsuario(new Usuario(1));
        rec.setComentario("prueba lote");
        rec.setNumeroDocumento("PRUEBA-LOTE");
        List<DetalleRecepcionMercancia> det = new ArrayList<>(List.of(lineas));
        BigDecimal sub = det.stream().map(DetalleRecepcionMercancia::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        rec.setSubtotal(sub);
        rec.setTotalItbis(BigDecimal.ZERO);
        rec.setTotal(sub);
        rec.setDetalleRecepcionMercanciaCollection(det);
        return rec;
    }

    /** LECHE RICA (2, controla lote), empaque unidad. */
    private DetalleRecepcionMercancia linea(String numeroLote, LocalDate vence, int cantidad) {
        ArticuloEmpaque emp = em.find(ArticuloEmpaque.class, 62);
        DetalleRecepcionMercancia d = new DetalleRecepcionMercancia();
        d.setArticulo(articuloRepo.findById(2).orElseThrow());
        d.setArticuloEmpaque(emp);
        d.setUnidad(emp.getUnidadEmpaque());
        d.setNombreUnidad(emp.getUnidadEmpaque().getDescripcion());
        d.setDescripcionArticulo("LECHE RICA");
        d.setAlmacen(em.find(Almacen.class, 1));
        d.setFactorConversion(BigDecimal.ONE);
        d.setCantidadRecibida(BigDecimal.valueOf(cantidad));
        d.setCantidadFisicaBase(BigDecimal.valueOf(cantidad));
        d.setPrecioUnitario(BigDecimal.TEN);
        d.setSubTotal(BigDecimal.TEN.multiply(BigDecimal.valueOf(cantidad)));
        d.setItbis(BigDecimal.ZERO);
        d.setTotal(d.getSubTotal());
        d.setNumeroLote(numeroLote);
        d.setFechaVencimientoLote(vence);
        return d;
    }
}
