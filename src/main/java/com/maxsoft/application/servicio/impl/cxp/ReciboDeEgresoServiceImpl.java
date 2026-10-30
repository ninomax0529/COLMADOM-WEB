/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxp;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DetalleReciboDeEgreso;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ReciboDeEgresoRepo;
import com.maxsoft.application.servicio.interfaces.cxp.FacturaDeCompraService;
import com.maxsoft.application.servicio.interfaces.cxp.ReciboDeEgresoService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReciboDeEgresoServiceImpl implements ReciboDeEgresoService {

    @Autowired
    ReciboDeEgresoRepo repo;
    @Autowired
    FacturaDeCompraService FacturaDeCompraService;
            
    @Autowired
    private CajaService cajaService;


    @Override
    public ReciboDeEgreso guardar(ReciboDeEgreso obj) {
        return repo.save(obj);
    }

    @Override
    public ReciboDeEgreso getReciboDeEgreso(int codigo) {
        return repo.getReferenceById(codigo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeEgreso procesarReciboDeEgreso(ReciboDeEgreso obj, List<DetalleReciboDeEgreso> listaDet) {

        BigDecimal totalPago, balanceAntrior, balanceActual;

        for (DetalleReciboDeEgreso det : listaDet) {

            FacturaDeCompra factCompra = this.FacturaDeCompraService.getFactura(det.getFactura().getCodigo());

            totalPago = BigDecimal.valueOf(this.repo.getPagoFacturaCompra(factCompra.getCodigo()));
            System.out.println("totalPago :" + totalPago + " factCompra.getTotal() " + factCompra.getTotal());
            balanceAntrior = factCompra.getTotal().subtract(totalPago);
            System.out.println("balanceAntrior :" + balanceAntrior);
            balanceActual = factCompra.getTotal().subtract(totalPago.add(det.getTotal()));
            System.out.println("balanceActual :" + balanceActual + " totalPago.add(det.getTotal())" + totalPago.add(det.getTotal()));

            det.setBalancePendienteAntes(balanceAntrior);
            det.setBalancePendienteDespues(balanceActual);

            factCompra.setTotalPagado(totalPago.add(det.getTotal()));
            factCompra.setTotalPendiente(balanceActual);
            factCompra.setFechaActualizacion(LocalDateTime.now());

            if (balanceActual.compareTo(BigDecimal.ZERO) == 0) {

                factCompra.setEstado(new EstadoFactura(2));
                det.setConcepto("Pago a factura total en este recibo ");

            } else {
                det.setConcepto("Abono a factura en este recibo ");
            }

            obj.setDetalleReciboDeEgresoCollection(listaDet);
            obj = this.repo.saveAndFlush(obj);

            this.FacturaDeCompraService.guardar(factCompra);

        }

        return obj;
    }

    @Override
    public List<ReciboDeEgreso> getLista() {
        return repo.findAll();
    }

    @Override
    public List<DetalleReciboDeEgreso> getDetalle(int obj) {
        return repo.getDetalle(obj);
    }

    @Override
    public ReciboDeEgreso anularReciboDeEgreso(Integer id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Double getPagoFacturaCompra(int fact) {
        return repo.getPagoFacturaCompra(fact);
    }

    @Override
    public ReciboDeEgreso procesarReciboDeEgreso(FacturaDeCompra factCompra) {

                // 1. Validación de Caja
        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja antes de registrar movimientos de POS"));
        
        ReciboDeEgreso recibo = new ReciboDeEgreso();
        recibo.setNombreProveedor(factCompra.getNombreProveedor());
        recibo.setProveedor(factCompra.getProveedor());
        recibo.setFecha(factCompra.getFecha());
        recibo.setCajaChica(turnoActual.getId().intValue());
       
        recibo.setConcepto(factCompra.getConcepto());
        recibo.setTotalPagado(factCompra.getTotal());
        recibo.setFechaCreacion(new Date());
        recibo.setUsuario(new Usuario(1));
        recibo.setNombreUsuario("admin");
        recibo.setAnulado(false);

        List<DetalleReciboDeEgreso> detalles = new ArrayList<>();

        DetalleReciboDeEgreso d = new DetalleReciboDeEgreso();

        d.setReciboEgreso(recibo);
        d.setFactura(factCompra);
        d.setBalancePendienteAntes(factCompra.getTotal());
        d.setTotal(factCompra.getTotal());
        d.setBalancePendienteDespues(BigDecimal.ZERO);      
        detalles.add(d);
        recibo.setDetalleReciboDeEgresoCollection(detalles);

        ReciboDeEgreso reciBd = this.repo.save(recibo);
        
//                6. Registrar Movimiento de Caja
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    factCompra.getCondicionPago()!= null ?  factCompra.getCondicionPago().getNombre(): "COMPRA",
                    reciBd.getTotalPagado(),
                    "Egreso por compra #" + factCompra.getCodigo() + " Recibo de Egreso #" + reciBd.getCodigo(),
                    reciBd.getNombreUsuario()
            );

        return reciBd;
    }

}
