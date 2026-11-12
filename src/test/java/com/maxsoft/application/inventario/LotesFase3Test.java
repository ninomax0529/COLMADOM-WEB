package com.maxsoft.application.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.DetalleFacturaVentaLote;
import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.modelo.Merma;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.modelo.TipoVenta;
import com.maxsoft.application.modelo.TrasladoInventario;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.DetalleFacturaVentaLoteRepo;
import com.maxsoft.application.repo.LoteAlmacenRepo;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteVencidoException;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.servicio.interfaces.venta.AutorizacionService;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fase 3a de lotes (servicios de cada módulo) contra la BD real. Cada prueba
 * se revierte al terminar.
 *
 * Artículo 2 (LECHE RICA, controla lote): UNIDAD factor 1, CAJA factor 10.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class LotesFase3Test {

    private static final int ART = 2;

    @Autowired
    EntityManager em;
    @Autowired
    LoteService loteService;
    @Autowired
    LoteAlmacenRepo loteAlmacenRepo;
    @Autowired
    DetalleFacturaVentaLoteRepo dfvlRepo;
    @Autowired
    MovimientoInventarioService movimientoService;
    @Autowired
    FacturaDeVentaService facturaService;
    @Autowired
    EntradaDeInventarioService entradaService;
    @Autowired
    SalidaInventarioService salidaService;
    @Autowired
    AutorizacionService autorizacionService;

    Almacen alm1;
    Almacen alm2;
    Articulo art;
    ArticuloEmpaque unidad;
    ArticuloEmpaque caja;
    Lote inicial;
    Lote vencido;

    @BeforeEach
    void preparar() {
        alm1 = em.find(Almacen.class, 1);
        alm2 = em.find(Almacen.class, 2);
        art = em.find(Articulo.class, ART);
        unidad = em.find(ArticuloEmpaque.class, 62);
        caja = em.find(ArticuloEmpaque.class, 63);
        assertTrue(art.isControlaLote());
        loteService.sincronizarLotes(art, "PRUEBA");
        inicial = em.createQuery("select l from Lote l where l.articulo.codigo = :a and l.numeroLote = 'INICIAL'", Lote.class)
                .setParameter("a", ART).getSingleResult();

        // Las pruebas suponen 30 vigentes (INICIAL) en el almacén 2; las ventas reales
        // lo van bajando, así que se completa aquí (todo se revierte al final).
        BigDecimal faltan = bd(30).subtract(ex(inicial, alm2));
        if (faltan.signum() > 0) {
            movimientoService.registrarMovimientoConLote(art, alm2, null, BigDecimal.ONE, faltan, null, null, null,
                    em.find(TipoMovimiento.class, 1), em.find(TipoDocumento.class, 1), "TEST-COMPLETA", faltan,
                    "PRUEBA", "completar 30 vigentes", inicial);
        }

        // Lote vencido con 5 unidades en el almacén de venta (2)
        vencido = loteService.obtenerOCrearLote(art, "PRUEBA-V", LocalDate.now().minusDays(3), null, null, "TEST", "PRUEBA");
        movimientoService.registrarMovimientoConLote(art, alm2, null, BigDecimal.ONE, bd(5), null, null, null,
                em.find(TipoMovimiento.class, 1), em.find(TipoDocumento.class, 1), "TEST-EV", bd(5), "PRUEBA",
                "lote vencido de prueba", vencido);
        System.out.println(">>> Inicio: INICIAL alm2=" + ex(inicial, alm2) + " PRUEBA-V alm2=" + ex(vencido, alm2));
    }

    /** Vende 3 CAJAS (30) + 2 UNIDADES (2): lo vigente es lo del INICIAL, lo demás sale del vencido. */
    private TicketVenta ticket32() {
        BigDecimal vigente = ex(inicial, alm2);
        assertEquals(0, bd(30).compareTo(vigente), "la prueba supone 30 vigentes en el almacén 2");
        TicketVenta t = new TicketVenta(1, "TEST");
        t.setCliente(em.find(Cliente.class, 13));
        t.setNombreCliente("PUBLICO GENERAL");
        t.setTipoVenta(em.find(TipoVenta.class, 1));
        t.setEstadoFactura(em.find(EstadoFactura.class, 1));
        t.getItems().add(linea(caja, 3, 1));
        t.getItems().add(linea(unidad, 2, 2));
        return t;
    }

    private DetalleFacturaDeVenta linea(ArticuloEmpaque emp, int cantidad, int numLinea) {
        DetalleFacturaDeVenta d = new DetalleFacturaDeVenta();
        BigDecimal cant = bd(cantidad);
        d.setArticulo(art);
        d.setArticuloEmpaque(emp);
        d.setCantidad(cant);
        d.setFactorConversion(emp.getFactorConversion());
        d.setCantidadFisicaBase(cant.multiply(emp.getFactorConversion()));
        d.setUnidad(emp.getUnidadEmpaque());
        d.setNombreUnidad(emp.getUnidadEmpaque().getDescripcion());
        d.setNumeroDeLinea(numLinea);
        d.setDescripcionArticulo(art.getDescripcion() + " " + emp.getNombreEmpaque());
        d.setPrecioVenta(emp.getPrecioVenta());
        d.setPorcientoDescuento(BigDecimal.ZERO);
        d.setPorcientoItbis(BigDecimal.ZERO);
        d.setNombreAlmacen("General");
        BigDecimal sub = emp.getPrecioVenta().multiply(cant);
        d.setSubTotal(sub);
        d.setTotalDescuento(BigDecimal.ZERO);
        d.setTotalItbis(BigDecimal.ZERO);
        d.setTotal(sub);
        return d;
    }

    @Test
    void ventaConVencidoRequiereAutorizacionYRegistraLotes() {
        // Sin autorización: bloqueada
        LoteVencidoException ex = assertThrows(LoteVencidoException.class,
                () -> facturaService.procesarVenta(ticket32(), "PRUEBA"));
        System.out.println(">>> Sin autorización: " + ex.getLineas());

        // Autorización de supervisor (admin / 123) y reintento
        String supervisor = autorizacionService.autorizarSupervisor("admin", "123");
        assertThrows(IllegalArgumentException.class, () -> autorizacionService.autorizarSupervisor("admin", "mala"));
        TicketVenta t = ticket32();
        t.setAutorizadoLoteVencidoPor(supervisor);
        FacturaDeVenta f = facturaService.procesarVenta(t, "PRUEBA");
        em.flush();

        List<DetalleFacturaVentaLote> filas = dfvlRepo.findPorFactura(f.getCodigo());
        for (DetalleFacturaVentaLote r : filas) {
            System.out.println("    factura " + f.getCodigo() + " línea " + r.getDetalleFacturaDeVenta().getNumeroDeLinea()
                    + " lote " + r.getLote().getNumeroLote() + " cant " + r.getCantidad()
                    + " vencido=" + r.isLoteVencido() + " autorizó=" + r.getAutorizadoPor());
        }
        assertEquals(2, filas.size());
        DetalleFacturaVentaLote deCaja = filas.stream().filter(r -> r.getLote().equals(inicial)).findFirst().orElseThrow();
        DetalleFacturaVentaLote deVencido = filas.stream().filter(r -> r.getLote().equals(vencido)).findFirst().orElseThrow();
        assertEquals(0, bd(30).compareTo(deCaja.getCantidad()));
        assertEquals(0, bd(2).compareTo(deVencido.getCantidad()));
        assertTrue(deVencido.isLoteVencido());
        assertEquals(supervisor, deVencido.getAutorizadoPor());
        assertEquals(0, ex(inicial, alm2).signum());
        assertEquals(0, bd(3).compareTo(ex(vencido, alm2)));
        assertCuadra();

        // Anulación: todo vuelve a sus lotes
        facturaService.anularVenta(f.getCodigo(), "prueba de lotes", "PRUEBA");
        em.flush();
        System.out.println(">>> Tras anular: INICIAL alm2=" + ex(inicial, alm2) + " PRUEBA-V alm2=" + ex(vencido, alm2));
        assertEquals(0, bd(30).compareTo(ex(inicial, alm2)));
        assertEquals(0, bd(5).compareTo(ex(vencido, alm2)));
        assertCuadra();
    }

    @Test
    void devolucionParcialVuelveAlLoteDeOrigen() {
        TicketVenta t = ticket32();
        t.setAutorizadoLoteVencidoPor("PRUEBA");
        FacturaDeVenta f = facturaService.procesarVenta(t, "PRUEBA");
        em.flush();
        List<DetalleFacturaDeVenta> lineas = em.createQuery(
                "select d from DetalleFacturaDeVenta d where d.factura.codigo = :f order by d.numeroDeLinea",
                DetalleFacturaDeVenta.class).setParameter("f", f.getCodigo()).getResultList();

        // Devuelve 1 CAJA de la línea 1 (10 unidades, salió del INICIAL) y 1 UNIDAD de la línea 2 (del vencido)
        SolicitudDevolucionDto s = new SolicitudDevolucionDto();
        s.setIdFactura(f.getCodigo());
        s.setMotivo("prueba de lotes");
        s.setUsuario("PRUEBA");
        SolicitudDevolucionDto.ItemDevolucionDto i1 = new SolicitudDevolucionDto.ItemDevolucionDto();
        i1.setIdDetalleFactura(lineas.get(0).getCodigo());
        i1.setIdArticulo(ART);
        i1.setCantidadADevolver(BigDecimal.ONE);
        SolicitudDevolucionDto.ItemDevolucionDto i2 = new SolicitudDevolucionDto.ItemDevolucionDto();
        i2.setIdDetalleFactura(lineas.get(1).getCodigo());
        i2.setIdArticulo(ART);
        i2.setCantidadADevolver(BigDecimal.ONE);
        s.setItems(List.of(i1, i2));
        facturaService.procesarDevolucion(s);
        em.flush();

        System.out.println(">>> Tras devolver 1 caja + 1 unidad: INICIAL alm2=" + ex(inicial, alm2)
                + " PRUEBA-V alm2=" + ex(vencido, alm2));
        assertEquals(0, bd(10).compareTo(ex(inicial, alm2)));
        assertEquals(0, bd(4).compareTo(ex(vencido, alm2)));
        assertCuadra();
    }

    @Test
    void ajusteMermaYTraslado() {
        Usuario usuario = em.find(Usuario.class, 1);

        // Ajuste positivo de 2 CAJAS con número y vencimiento digitados -> crea el lote
        AjusteInventario aj = new AjusteInventario();
        aj.setCodigo(999001);
        aj.setFecha(new Date());
        aj.setAlmacen(alm1);
        aj.setUsuario(usuario);
        aj.setObservacion("prueba lotes");
        DetalleAjusteInventario da = new DetalleAjusteInventario();
        da.setArticulo(art);
        da.setArticuloEmpaque(caja);
        da.setFactorConversion(caja.getFactorConversion());
        da.setCantidad(bd(2));
        da.setAlmacen(alm1);
        da.setItbis(BigDecimal.ZERO);
        da.setSubTotal(BigDecimal.ZERO);
        da.setTotal(BigDecimal.ZERO);
        da.setNumeroLote("aj-777");
        da.setFechaVencimientoLote(LocalDate.now().plusDays(40));
        entradaService.crearEntradaPorAjuste(aj, List.of(da));
        em.flush();
        Lote aj777 = em.createQuery("select l from Lote l where l.articulo.codigo = :a and l.numeroLote = 'AJ-777'", Lote.class)
                .setParameter("a", ART).getSingleResult();
        System.out.println(">>> Ajuste +2 cajas: lote AJ-777 vence " + aj777.getFechaVencimiento() + " alm1=" + ex(aj777, alm1));
        assertEquals(0, bd(20).compareTo(ex(aj777, alm1)));

        // Merma del lote vencido (alm2): sale de ese lote aunque haya vigentes
        Merma m = new Merma();
        m.setCodigo(999002);
        m.setFecha(new Date());
        m.setAlmacen(alm2);
        m.setUsuario(usuario);
        m.setObservacion("vencidos");
        DetalleMerma dm = new DetalleMerma();
        dm.setArticulo(art);
        dm.setArticuloEmpaque(unidad);
        dm.setFactorConversion(BigDecimal.ONE);
        dm.setCantidad(bd(5));
        dm.setLote(vencido);
        salidaService.crearSalidaPorMerma(m, List.of(dm));
        em.flush();
        System.out.println(">>> Merma 5 del vencido: PRUEBA-V alm2=" + ex(vencido, alm2) + " INICIAL alm2=" + ex(inicial, alm2));
        assertEquals(0, ex(vencido, alm2).signum());
        assertEquals(0, bd(30).compareTo(ex(inicial, alm2)));

        // Traslado de 2 CAJAS + algo de alm1 a alm2: FEFO en origen y mismos lotes en destino
        BigDecimal ajAntes2 = ex(aj777, alm2);
        TrasladoInventario tr = new TrasladoInventario();
        tr.setCodigo(999003);
        tr.setFechaEmision(new Date());
        tr.setAlmacenOrigen(alm1);
        tr.setAlmacenDestino(alm2);
        tr.setUsuarioEnvia(usuario);
        tr.setObservacion("prueba lotes");
        DetalleTrasladoInventario dt = new DetalleTrasladoInventario();
        dt.setArticulo(art);
        dt.setArticuloEmpaque(caja);
        dt.setFactorConversion(caja.getFactorConversion());
        dt.setCantidadEnviada(bd(3));
        dt.setCantidadRecibida(bd(3));
        dt.setCantidadFisicaBase(bd(30));
        dt.setUnidad(caja.getUnidadEmpaque());
        dt.setNombreUnidad(caja.getNombreEmpaque());
        dt.setItbis(BigDecimal.ZERO);
        dt.setSubTotal(BigDecimal.ZERO);
        dt.setTotal(BigDecimal.ZERO);
        List<DetalleTrasladoInventario> lista = List.of(dt);
        BigDecimal ini1 = ex(inicial, alm1);
        BigDecimal ini2 = ex(inicial, alm2);
        salidaService.crearSalidaPorTraslado(tr, lista);
        entradaService.crearEntradaPorTraslado(tr, lista);
        em.flush();
        for (MovimientoInventario mv : dt.getMovimientosSalida()) {
            System.out.println("    traslado salió lote " + mv.getLote().getNumeroLote() + " cant " + mv.getCantidad());
        }
        System.out.println(">>> Traslado 3 cajas alm1->alm2: AJ-777 alm1=" + ex(aj777, alm1) + " alm2=" + ex(aj777, alm2)
                + " | INICIAL alm1=" + ex(inicial, alm1) + " alm2=" + ex(inicial, alm2));
        // FEFO: AJ-777 (vence en 40 días) sale antes que INICIAL (sin vencimiento)
        assertEquals(0, ex(aj777, alm1).signum());
        assertEquals(0, ajAntes2.add(bd(20)).compareTo(ex(aj777, alm2)));
        assertEquals(0, ini1.subtract(bd(10)).compareTo(ex(inicial, alm1)));
        assertEquals(0, ini2.add(bd(10)).compareTo(ex(inicial, alm2)));
        assertCuadra();
        System.out.println(">>> TODO OK (se revierte al terminar)");
    }

    private void assertCuadra() {
        em.flush();
        List<Object[]> d = loteService.getDescuadres();
        assertTrue(d.stream().noneMatch(r -> ((Number) r[0]).intValue() == ART),
                "existencia del almacén distinta a la suma de lotes");
    }

    private BigDecimal ex(Lote lote, Almacen alm) {
        em.flush();
        return loteAlmacenRepo.findByLoteCodigoAndAlmacenCodigo(lote.getCodigo(), alm.getCodigo())
                .map(LoteAlmacen::getExistencia).orElse(BigDecimal.ZERO);
    }

    private static BigDecimal bd(int v) {
        return BigDecimal.valueOf(v);
    }

    static {
        assertNotNull(LotesFase3Test.class);
    }
}
