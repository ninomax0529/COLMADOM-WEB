/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxc;

import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ReciboDeIngresoRepo;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReciboDeIngresoServiceImpl implements ReciboDeIngresoService {

    @Autowired
    ReciboDeIngresoRepo repo;

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

}
