package com.maxsoft.application.compra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.AplicacionCreditoProveedor;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.CondicionPago;
import com.maxsoft.application.modelo.DetalleDevolucionProveedor;
import com.maxsoft.application.modelo.DevolucionProveedor;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.TipoCompra;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.LoteAlmacenRepo;
import com.maxsoft.application.servicio.interfaces.compra.DevolucionProveedorService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
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
 * Devolución a proveedor por lote contra la BD real. Cada prueba se revierte.
 * Artículo 2 (controla lote), empaque 62 = UNIDAD, almacén 2, proveedor 3.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class DevolucionProveedorTest {

    @Autowired
    EntityManager em;
    @Autowired
    DevolucionProveedorService devolucionService;
    @Autowired
    LoteService loteService;
    @Autowired
    LoteAlmacenRepo loteAlmacenRepo;
    @Autowired
    MovimientoInventarioService movimientoService;
    @Autowired
    ArticuloAlmacenService articuloAlmacenService;
    @Autowired
    CajaService cajaService;

    Almacen alm2;
    Articulo art;
    ArticuloEmpaque unidad;
    Proveedor proveedor;
    Lote vencido;

    @BeforeEach
    void preparar() {
        alm2 = em.find(Almacen.class, 2);
        art = em.find(Articulo.class, 2);
        unidad = em.find(ArticuloEmpaque.class, 62);
        proveedor = em.find(Proveedor.class, 3);
        loteService.sincronizarLotes(art, "PRUEBA");

        // Lote vencido con 10 unidades en el almacén 2
        vencido = loteService.obtenerOCrearLote(art, "PRUEBA-DV", LocalDate.now().minusDays(5),
                new BigDecimal("100"), proveedor, "TEST", "PRUEBA");
        movimientoService.registrarMovimientoConLote(art, alm2, null, BigDecimal.ONE, bd(10), null, null, null,
                em.find(TipoMovimiento.class, 1), em.find(TipoDocumento.class, 1), "TEST-DV", bd(10), "PRUEBA",
                "lote vencido de prueba", vencido);
        em.flush();
    }

    @Test
    void notaDeCreditoSeAplicaALaFacturaYSeAnula() {
        FacturaDeCompra factura = facturaPendiente(new BigDecimal("500.00"));
        BigDecimal existenciaAntes = existenciaArticulo();

        DevolucionProveedor dev = cabecera(DevolucionProveedor.NOTA_CREDITO);
        dev.setFacturaCompra(factura);
        dev = devolucionService.registrar(dev, List.of(linea(3, "100", "18")), "PRUEBA");
        em.flush();

        // 3 x 100 + 18% = 354; se aplica todo a la factura (pendiente 500)
        assertEquals(0, new BigDecimal("354.00").compareTo(dev.getTotal()));
        assertEquals(0, new BigDecimal("354.00").compareTo(dev.getCreditoAplicado()));
        assertEquals(0, dev.getCreditoDisponible().signum());
        assertEquals(0, new BigDecimal("146.00").compareTo(devolucionService.getPendienteFactura(factura.getCodigo())));
        assertEquals(0, bd(7).compareTo(ex(vencido)));
        assertEquals(0, existenciaAntes.subtract(bd(3)).compareTo(existenciaArticulo()));
        List<AplicacionCreditoProveedor> aps = devolucionService.getAplicaciones(dev.getCodigo());
        assertEquals(1, aps.size());
        ReciboDeEgreso recibo = aps.get(0).getReciboEgreso();
        assertTrue(recibo.getConcepto().startsWith("NOTA DE CRÉDITO"));
        assertEquals(null, recibo.getCajaChica());

        // Anular: la mercancía vuelve al lote y la factura vuelve a deber 500
        devolucionService.anular(dev.getCodigo(), "prueba", "PRUEBA");
        em.flush();
        em.clear();
        assertEquals(0, bd(10).compareTo(ex(vencido)));
        assertEquals(0, new BigDecimal("500.00").compareTo(devolucionService.getPendienteFactura(factura.getCodigo())));
        assertTrue(em.find(ReciboDeEgreso.class, recibo.getCodigo()).getAnulado());
        assertTrue(devolucionService.getDevolucion(dev.getCodigo()).isAnulada());
    }

    @Test
    void notaDeCreditoConSobranteSeAplicaDespues() {
        FacturaDeCompra f1 = facturaPendiente(new BigDecimal("200.00"));
        FacturaDeCompra f2 = facturaPendiente(new BigDecimal("1000.00"));

        DevolucionProveedor dev = cabecera(DevolucionProveedor.NOTA_CREDITO);
        dev.setFacturaCompra(f1);
        dev = devolucionService.registrar(dev, List.of(linea(5, "100", "0")), "PRUEBA");
        em.flush();

        // 500 de crédito: 200 a la factura 1, 300 quedan disponibles
        assertEquals(0, f1Pendiente(f1).signum());
        assertEquals(0, new BigDecimal("300.00").compareTo(dev.getCreditoDisponible()));
        Integer id = dev.getCodigo();
        assertTrue(devolucionService.getConCreditoDisponible(proveedor.getCodigo()).stream()
                .anyMatch(d -> d.getCodigo().equals(id)));

        // No se puede aplicar más de lo disponible
        assertThrows(IllegalArgumentException.class,
                () -> devolucionService.aplicarCredito(id, f2.getCodigo(), new BigDecimal("300.01"), "PRUEBA"));

        devolucionService.aplicarCredito(id, f2.getCodigo(), new BigDecimal("300"), "PRUEBA");
        em.flush();
        assertEquals(0, new BigDecimal("700.00").compareTo(devolucionService.getPendienteFactura(f2.getCodigo())));
        assertEquals(0, devolucionService.getDevolucion(id).getCreditoDisponible().signum());
    }

    @Test
    void cambioReponeEnLoteNuevoYSeAnula() {
        BigDecimal existenciaAntes = existenciaArticulo();

        DevolucionProveedor dev = cabecera(DevolucionProveedor.CAMBIO);
        DetalleDevolucionProveedor l = linea(4, "100", "18");
        l.setNumeroLoteNuevo("prueba-nv");
        l.setFechaVencimientoNueva(LocalDate.now().plusDays(60));
        dev = devolucionService.registrar(dev, List.of(l), "PRUEBA");
        em.flush();

        Lote nuevo = devolucionService.getDetalle(dev.getCodigo()).get(0).getLoteNuevo();
        assertEquals("PRUEBA-NV", nuevo.getNumeroLote());
        assertEquals(proveedor.getCodigo(), nuevo.getProveedor().getCodigo());
        assertEquals(0, bd(6).compareTo(ex(vencido)));
        assertEquals(0, bd(4).compareTo(ex(nuevo)));
        assertEquals(0, existenciaAntes.compareTo(existenciaArticulo()), "un cambio no cambia la existencia");
        assertEquals(0, dev.getCreditoDisponible().signum());

        devolucionService.anular(dev.getCodigo(), "prueba", "PRUEBA");
        em.flush();
        assertEquals(0, bd(10).compareTo(ex(vencido)));
        assertEquals(0, ex(nuevo).signum());
        assertEquals(0, existenciaAntes.compareTo(existenciaArticulo()));
    }

    @Test
    void reembolsoEnEfectivoEntraYSaleDeLaCaja() {
        if (cajaService.obtenerCajaAbierta().isEmpty()) {
            assertThrows(IllegalStateException.class, () -> devolucionService.registrar(
                    cabecera(DevolucionProveedor.EFECTIVO), List.of(linea(2, "50", "0")), "PRUEBA"));
            return;
        }
        DevolucionProveedor dev = devolucionService.registrar(cabecera(DevolucionProveedor.EFECTIVO),
                List.of(linea(2, "50", "0")), "PRUEBA");
        em.flush();
        assertEquals(0, new BigDecimal("100.00").compareTo(dev.getTotal()));
        assertEquals(0, new BigDecimal("100.00").compareTo(sumaCaja(dev)));

        devolucionService.anular(dev.getCodigo(), "prueba", "PRUEBA");
        em.flush();
        assertEquals(0, sumaCaja(dev).signum());
        assertEquals(0, bd(10).compareTo(ex(vencido)));
    }

    @Test
    void validaciones() {
        // más de lo que hay en el lote
        assertThrows(IllegalStateException.class, () -> devolucionService.registrar(
                cabecera(DevolucionProveedor.NOTA_CREDITO), List.of(linea(11, "100", "0")), "PRUEBA"));
        // artículo con lote sin indicar el lote
        DetalleDevolucionProveedor sinLote = linea(1, "100", "0");
        sinLote.setLote(null);
        assertThrows(IllegalArgumentException.class, () -> devolucionService.registrar(
                cabecera(DevolucionProveedor.NOTA_CREDITO), List.of(sinLote), "PRUEBA"));
        // factura de otro proveedor
        FacturaDeCompra ajena = facturaPendiente(new BigDecimal("100"));
        ajena.setProveedor(em.find(Proveedor.class, 4));
        em.flush();
        DevolucionProveedor dev = cabecera(DevolucionProveedor.NOTA_CREDITO);
        dev.setFacturaCompra(ajena);
        assertThrows(IllegalArgumentException.class,
                () -> devolucionService.registrar(dev, List.of(linea(1, "100", "0")), "PRUEBA"));
    }

    // ============================================================
    // AYUDAS
    // ============================================================
    private DevolucionProveedor cabecera(String tipo) {
        DevolucionProveedor d = new DevolucionProveedor();
        d.setProveedor(proveedor);
        d.setAlmacen(alm2);
        d.setTipoLiquidacion(tipo);
        d.setMotivo(DevolucionProveedor.MOTIVO_VENCIDO);
        d.setObservacion("prueba");
        return d;
    }

    private DetalleDevolucionProveedor linea(int cantidad, String costo, String itbis) {
        DetalleDevolucionProveedor d = new DetalleDevolucionProveedor();
        d.setArticulo(art);
        d.setArticuloEmpaque(unidad);
        d.setCantidad(bd(cantidad));
        d.setCostoUnitario(new BigDecimal(costo));
        d.setPorcentajeItbis(new BigDecimal(itbis));
        d.setLote(vencido);
        return d;
    }

    private FacturaDeCompra facturaPendiente(BigDecimal total) {
        FacturaDeCompra f = new FacturaDeCompra();
        f.setProveedor(proveedor);
        f.setNombreProveedor(proveedor.getNombre());
        f.setTipoCompra(em.find(TipoCompra.class, 2));
        f.setCondicionPago(em.find(CondicionPago.class, 1));
        f.setEstado(new EstadoFactura(1));
        f.setUsuario(new Usuario(1));
        f.setFecha(new Date());
        f.setSubTotal(total);
        f.setTotalItbis(BigDecimal.ZERO);
        f.setTotalIsr(BigDecimal.ZERO);
        f.setTotal(total);
        f.setTotalAPagar(total);
        f.setTotalPagado(BigDecimal.ZERO);
        f.setTotalPendiente(total);
        f.setAnulada(false);
        em.persist(f);
        em.flush();
        return f;
    }

    private BigDecimal f1Pendiente(FacturaDeCompra f) {
        return devolucionService.getPendienteFactura(f.getCodigo());
    }

    private BigDecimal sumaCaja(DevolucionProveedor dev) {
        em.flush();
        return em.createQuery("select coalesce(sum(m.monto), 0) from MovimientosCaja m"
                + " where m.descripcion like :d", BigDecimal.class)
                .setParameter("d", "%" + dev.getNumero() + "%").getSingleResult();
    }

    private BigDecimal ex(Lote lote) {
        em.flush();
        return loteAlmacenRepo.findByLoteCodigoAndAlmacenCodigo(lote.getCodigo(), alm2.getCodigo())
                .map(LoteAlmacen::getExistencia).orElse(BigDecimal.ZERO);
    }

    private BigDecimal existenciaArticulo() {
        em.flush();
        return articuloAlmacenService.buscarPorArticuloYAlmacen(art.getCodigo(), alm2.getCodigo())
                .map(ArticuloAlmacen::getExistencia).orElse(BigDecimal.ZERO);
    }

    private static BigDecimal bd(int v) {
        return BigDecimal.valueOf(v);
    }
}
