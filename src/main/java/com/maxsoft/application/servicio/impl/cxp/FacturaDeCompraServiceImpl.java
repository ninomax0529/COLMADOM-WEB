/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxp;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.CondicionPago;
import com.maxsoft.application.modelo.DetalleFacturaDeCompra;
import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.modelo.TipoCompra;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.FacturaDeCompraRepo;
import com.maxsoft.application.servicio.interfaces.cxp.FacturaDeCompraService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.util.ClaseUtil;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaDeCompraServiceImpl implements FacturaDeCompraService {

    @Autowired
    FacturaDeCompraRepo repo;

    @Autowired
    private CajaService cajaService;

    @Autowired
    private com.maxsoft.application.repo.ReciboDeEgresoRepo reciboDeEgresoRepo;

    @Override
    public FacturaDeCompra guardar(FacturaDeCompra obj) {
        return repo.save(obj);
    }

    @Override
    public FacturaDeCompra getFactura(int codigo) {
        return repo.getFactura(codigo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FacturaDeCompra procesarFactura(RecepcionMercancia recepcionGuardada, List<DetalleRecepcionMercancia> listaDet) {

        // 2. Crear la factura de compra vinculándola a la recepción
        FacturaDeCompra factura = new FacturaDeCompra();
        factura.setRecepcionMercancia(recepcionGuardada); // <--- Aquí aseguraramos la trazabilidad
        factura.setProveedor(recepcionGuardada.getProveedor());
        factura.setNumeroFactura(recepcionGuardada.getNumeroDocumento());
        factura.setNcf(recepcionGuardada.getNumeroDocumento());
        factura.setCondicionPago(recepcionGuardada.getCondicionPago());
        factura.setFecha(new Date());

        Proveedor proveedor = recepcionGuardada.getProveedor();
        int diasCredito = proveedor.getPlazo().getDias();
        Date fechaVenci = ClaseUtil.asDate(LocalDate.now().plusDays(diasCredito));
        // Asignación de estados según condición de pago
        if (recepcionGuardada.getCondicionPago().getCodigo() == 2) {//Credito
            factura.setFechaVencimiento(fechaVenci);
            factura.setEstado(new EstadoFactura(2));
        } else {
            factura.setFechaVencimiento(new Date());
            factura.setEstado(new EstadoFactura(1));
        }

        List<DetalleFacturaDeCompra> detallesFact = new ArrayList<>();

        for (DetalleRecepcionMercancia item : listaDet) {

            DetalleFacturaDeCompra detFact = new DetalleFacturaDeCompra();

            detFact.setFacturaDeCompra(factura);
            detFact.setSubTotal(item.getSubTotal());
            detFact.setTotalItbis(item.getItbis());
            detFact.setTotalIsr(item.getItbis());
            detFact.setTotal(item.getTotal());
            detFact.setTotalAPagar(item.getTotal());
            detFact.setTotalDescuento(BigDecimal.TEN);
            detFact.setOtrosImpuesto(BigDecimal.ZERO);
            detFact.setCantidadFisicaBase(item.getCantidadFisicaBase());
            detFact.setFactorConversion(item.getFactorConversion());
            detFact.setArticuloEmpaque(item.getArticuloEmpaque());
            detFact.setArticulo(item.getArticulo());
            detFact.setCantidad(item.getCantidadRecibida());
            detFact.setDescripcionArticulo(item.getDescripcionArticulo());
            detFact.setPorcientoDescuento(BigDecimal.ZERO);
            detFact.setPorcientoIsr(BigDecimal.ZERO);
            detFact.setPorcientoItbis(BigDecimal.valueOf(18));
            detFact.setPrecioUnitario(item.getPrecioUnitario());
            detFact.setUnidad(item.getUnidad().getCodigo());

            detallesFact.add(detFact);
        }

        
        factura.setTipoCompra(new TipoCompra(proveedor.getTipoSuplidor().getCodigo()));
        factura.setNombreProveedor(proveedor.getNombre());
        factura.setAnulada(false);
        factura.setConcepto("Factura por recepcion de inv no." + recepcionGuardada.getCodigo());
        factura.setNombreEstado("PENDIENTE");
        factura.setUsuario(new Usuario(1));
        factura.setNombreUsaurio("Admin");

        factura.setSubTotal(recepcionGuardada.getSubtotal());
        factura.setTotalItbis(recepcionGuardada.getTotalItbis());
        factura.setTotalIsr(BigDecimal.ZERO);
        factura.setTotal(recepcionGuardada.getTotal());
        factura.setTotalPagado(BigDecimal.ZERO);
        factura.setTotalPendiente(recepcionGuardada.getTotal());

        factura.setDetalleFacturaDeCompraCollection(detallesFact);

        FacturaDeCompra factDb = repo.save(factura);

        return factDb;
    }

    @Override
    public List<FacturaDeCompra> getLista() {
        return repo.findAll();
    }

    @Override
    public List<DetalleFacturaDeCompra> getDetalle(int obj) {
        return repo.getDetalle(obj);
    }

    /**
     * Anula la factura de compra. Debe estar sin pagos vigentes: los recibos de
     * egreso se anulan antes (así se reversa también la caja).
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public FacturaDeCompra anularFactura(Integer idFactura, String motivoAnulacion) {

        FacturaDeCompra factura = repo.findById(idFactura)
                .orElseThrow(() -> new IllegalArgumentException("La factura de compra #" + idFactura + " no existe."));
        if (Boolean.TRUE.equals(factura.getAnulada())) {
            throw new IllegalStateException("La factura de compra #" + idFactura + " ya está anulada.");
        }
        Double pagado = reciboDeEgresoRepo.getPagoFacturaCompra(idFactura);
        if (pagado != null && pagado > 0) {
            throw new IllegalStateException("La factura de compra #" + idFactura + " tiene pagos vigentes por RD$ "
                    + String.format("%,.2f", pagado) + ". Anule primero sus recibos de egreso.");
        }

        factura.setAnulada(true);
        factura.setFechaAnulada(new Date());
        factura.setFechaActualizacion(java.time.LocalDateTime.now());
        factura.setNombreEstado("ANULADA");
        factura.setTotalPagado(BigDecimal.ZERO);
        factura.setTotalPendiente(BigDecimal.ZERO);
        String nota = "ANULADA" + (motivoAnulacion != null && !motivoAnulacion.isBlank() ? ": " + motivoAnulacion.trim() : "");
        factura.setConcepto(factura.getConcepto() != null ? factura.getConcepto() + " | " + nota : nota);
        return repo.save(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaDeCompra> getFacturasDeRecepcion(Integer recepcion) {
        return repo.findByRecepcionMercanciaCodigo(recepcion);
    }

    @Override
    public FacturaDeCompra procesarFacturaInformal(RecepcionMercancia recepcionGuardada, List<DetalleRecepcionMercancia> listaDet) {

        // 2. Crear la factura de compra vinculándola a la recepción
        FacturaDeCompra factura = new FacturaDeCompra();
        factura.setRecepcionMercancia(recepcionGuardada); // <--- Aquí aseguraramos la trazabilidad
        factura.setProveedor(recepcionGuardada.getProveedor());

        // Generar un correlativo interno automático para proveedores informales
        String codigoInformal = "REC-INF-" + System.currentTimeMillis();

        factura.setNumeroFactura(codigoInformal);
        factura.setNcf(recepcionGuardada.getNumeroDocumento());
        factura.setCondicionPago(new CondicionPago(1));
        factura.setFecha(new Date());

        Proveedor proveedor = recepcionGuardada.getProveedor();
//        int diasCredito = proveedor.getPlazo().getDias();
//        Date fechaVenci = ClaseUtil.asDate(LocalDate.now().plusDays(diasCredito));
        // Asignación de estados según condición de pago

        factura.setFechaVencimiento(new Date());
        factura.setEstado(new EstadoFactura(1));

        List<DetalleFacturaDeCompra> detallesFact = new ArrayList<>();

        BigDecimal montoTotal = BigDecimal.ZERO;

        for (DetalleRecepcionMercancia item : listaDet) {

            DetalleFacturaDeCompra detFact = new DetalleFacturaDeCompra();

            detFact.setFacturaDeCompra(factura);
            detFact.setSubTotal(item.getSubTotal());
            detFact.setTotalItbis(item.getItbis());
            detFact.setTotalIsr(item.getItbis());
            detFact.setTotal(item.getTotal());
            detFact.setTotalAPagar(item.getTotal());
            detFact.setTotalDescuento(BigDecimal.TEN);
            detFact.setOtrosImpuesto(BigDecimal.ZERO);
            detFact.setCantidadFisicaBase(item.getCantidadFisicaBase());
            detFact.setFactorConversion(item.getFactorConversion());
            detFact.setArticuloEmpaque(item.getArticuloEmpaque());
            detFact.setArticulo(item.getArticulo());
            detFact.setCantidad(item.getCantidadRecibida());
            detFact.setDescripcionArticulo(item.getDescripcionArticulo());
            detFact.setPorcientoDescuento(BigDecimal.ZERO);
            detFact.setPorcientoIsr(BigDecimal.ZERO);
            detFact.setPorcientoItbis(BigDecimal.valueOf(18));
            detFact.setPrecioUnitario(item.getPrecioUnitario());
            detFact.setUnidad(item.getUnidad().getCodigo());

            montoTotal = montoTotal.add(item.getTotal());

            detallesFact.add(detFact);
        }

        factura.setTipoCompra(new TipoCompra(2));
        factura.setNombreProveedor(proveedor.getNombre());
        factura.setAnulada(false);
        factura.setConcepto("Factura por recepcion de inv no." + recepcionGuardada.getCodigo());
        factura.setNombreEstado("PENDIENTE");
        factura.setUsuario(new Usuario(1));
        factura.setNombreUsaurio("Admin");

        factura.setSubTotal(recepcionGuardada.getSubtotal());
        factura.setTotalItbis(recepcionGuardada.getTotalItbis());
        factura.setTotalIsr(BigDecimal.ZERO);
        factura.setTotal(recepcionGuardada.getTotal());
        factura.setTotalPagado(montoTotal);
        factura.setTotalPendiente(BigDecimal.ZERO);

        factura.setDetalleFacturaDeCompraCollection(detallesFact);

        FacturaDeCompra factDb = repo.save(factura);

        // La salida de efectivo la registra el Recibo de Egreso que genera
        // RecepcionMercanciaServiceImpl (condición de pago contado). Registrarla
        // también aquí la contaba dos veces en la caja.
        return factDb;

    }

    @Override
    public List<FacturaDeCompra> getListaFactPendiente(int codPro) {
        return repo.getListaFactPendiente(codPro);
    }
}
