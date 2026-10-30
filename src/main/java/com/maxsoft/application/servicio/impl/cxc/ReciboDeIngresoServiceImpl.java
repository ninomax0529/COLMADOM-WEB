/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxc;

import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ReciboDeIngresoRepo;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReciboDeIngresoServiceImpl implements ReciboDeIngresoService {

    @Autowired
    ReciboDeIngresoRepo repo;

    @Autowired
    @Lazy
    FacturaDeVentaService facturaDeVentaService;

    @Override
    public ReciboDeIngreso guardar(ReciboDeIngreso obj) {
        return repo.save(obj);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso getReciboDeIngreso(int codigo) {
        return repo.getReferenceById(codigo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso procesarReciboDeIngresoPos(FacturaDeVenta factura) {

        ReciboDeIngreso recibo = new ReciboDeIngreso();
        Date fechaActual = new Date();

        recibo.setTotalPagado(factura.getTotal());
        recibo.setBalancePendienteAntes(factura.getTotal());
        recibo.setBalancePendienteDespues(BigDecimal.ZERO);

        recibo.setFecha(factura.getFecha() != null ? factura.getFecha() : fechaActual);
        recibo.setFechaCreacion(fechaActual);
        recibo.setFecha(factura.getFecha() != null ? factura.getFecha() : fechaActual);
        recibo.setCliente(factura.getCliente());
        recibo.setNombreCliente(factura.getCliente().getNombre());

        recibo.setUsuario(new Usuario(1));
        recibo.setNombreUsuario(factura.getNombreUsuario() != null ? factura.getNombreUsuario() : "Admin");
        recibo.setAnulado(false);
        recibo.setConcepto("Recibo automática por Factura de venta #" + factura.getCodigo());

        List<DetalleReciboDeIngreso> detalles = new ArrayList<>();

        DetalleReciboDeIngreso detSalida = new DetalleReciboDeIngreso();

        detSalida.setReciboIngreso(recibo);
        detSalida.setFactura(factura);
        detSalida.setConcepto("PAgo Factura de venta #" + factura.getCodigo());

        detSalida.setTotal(factura.getTotal());
        detSalida.setBalancePendienteAntes(factura.getTotal());
        detSalida.setBalancePendienteDespues(BigDecimal.ZERO);

        detalles.add(detSalida);

        recibo.setDetalleReciboDeIngresoCollection(detalles);

        ReciboDeIngreso reciGurad = this.repo.saveAndFlush(recibo);

        return reciGurad;
    }

    @Override
    public List<ReciboDeIngreso> getLista() {
        return repo.findAll();
    }

    @Override
    public List<DetalleReciboDeIngreso> getDetalle(int obj) {
        return repo.getDetalle(obj);
    }

    @Override
    public ReciboDeEgreso anularReciboDeIngreso(Integer id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Double getPagoFacturaVenta(int fact) {
        return repo.getPagoFacturaVenta(fact);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso procesarReciboDeIngreso(ReciboDeIngreso obj, List<DetalleReciboDeIngreso> listaDet) {

        BigDecimal totalPago, balanceAntrior, balanceActual;

        for (DetalleReciboDeIngreso det : listaDet) {

            FacturaDeVenta factura = this.facturaDeVentaService.getFactura(det.getFactura().getCodigo());

            totalPago = BigDecimal.valueOf(this.repo.getPagoFacturaVenta(factura.getCodigo()));
            System.out.println("totalPago :" + totalPago + " factCompra.getTotal() " + factura.getTotal());
            balanceAntrior = factura.getTotal().subtract(totalPago);
            System.out.println("balanceAntrior :" + balanceAntrior);
            balanceActual = factura.getTotal().subtract(totalPago.add(det.getTotal()));
            System.out.println("balanceActual :" + balanceActual + " totalPago.add(det.getTotal())" + totalPago.add(det.getTotal()));

            det.setBalancePendienteAntes(balanceAntrior);
            det.setBalancePendienteDespues(balanceActual);

            factura.setTotalPagado(totalPago.add(det.getTotal()));
            factura.setTotalPendiente(balanceActual);
            factura.setFechaActualizacion(new Date());

            if (balanceActual.compareTo(BigDecimal.ZERO) == 0) {

                factura.setEstadoFactura(new EstadoFactura(2));
                det.setConcepto("Pago a factura total en este recibo ");

            } else {
                det.setConcepto("Abono a factura en este recibo ");
            }

            obj.setDetalleReciboDeIngresoCollection(listaDet);
            obj = this.repo.saveAndFlush(obj);

            this.facturaDeVentaService.guardar(factura);

        }

        return obj;
    }

}
