/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.compra;

import com.maxsoft.application.modelo.OrdenDeCompra;
import com.maxsoft.application.repo.OrdenDeCompraRepo;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrdenDeCompraServiceImpl implements OrdenDeCompraService {

    @Autowired
    OrdenDeCompraRepo repo;
    
    @Override
    public List<OrdenDeCompra> getLista() {
       return repo.findAll();
    }

    @Override
    public List<OrdenDeCompra> getListarPendientes() {
       return  repo.pendientesDeFacturacion();
    }

    @Override
    public Optional<OrdenDeCompra> buscarPorCodigo(Integer codigo) {
       return repo.findById(codigo);
    }

    @Override
    public Optional<OrdenDeCompra> buscarPorCodigoConDetalles(Integer codigo) {
      throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public OrdenDeCompra guardar(OrdenDeCompra orden) {
       return repo.save(orden);
    }

    @Override
    public OrdenDeCompra autorizarOrden(Integer codigoOrden, Integer idAutorizador, String nombreAutorizador) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public OrdenDeCompra anularOrden(Integer codigoOrden, String motivo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public OrdenDeCompra registrarRecepcionParcial(Integer codigoOrden, Integer codigoDetalle, BigDecimal cantidadRecibida) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
