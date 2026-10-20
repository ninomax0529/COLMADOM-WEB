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
        factura.setCondicionPago(new CondicionPago(2));
        factura.setFecha(new Date());

        Proveedor proveedor = recepcionGuardada.getProveedor();
        int diasCredito = proveedor.getPlazo().getDias();
        Date fechaVenci = ClaseUtil.asDate(LocalDate.now().plusDays(diasCredito));
        // Asignación de estados según condición de pago
        if (2 == 2) {
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

        factura.setTipoCompra(new TipoCompra(1));
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

    @Override
    public FacturaDeCompra anularFactura(Integer idFactura, String motivoAnulacion) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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

        Optional<CajaTurno> cajaAbiertaOpt = cajaService.obtenerCajaAbierta();

        if (cajaAbiertaOpt.isEmpty()) {

            Notification.show("Debe abrir una caja antes de registrar movimientos de POS", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);

        }

        CajaTurno turnoActual = cajaAbiertaOpt.get();

        // Llamada al servicio para guardar en la base de datos
        cajaService.registrarMovimientoPos(
                turnoActual.getId().intValue(),
                "SALIDA DE DINERO",
                montoTotal,
                    "Pago a proveedor informal: " + proveedor.getNombre() + " - " + codigoInformal,
            
                factDb.getNombreUsaurio()
        );

        return factDb;

    }
}
