/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxp;

import com.maxsoft.application.modelo.DetalleFacturaDeCompra;
import com.maxsoft.application.modelo.DetalleReciboDeEgreso;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.repo.ReciboDeEgresoRepo;
import com.maxsoft.application.servicio.interfaces.cxp.ReciboDeEgresoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReciboDeEgresoServiceImpl implements ReciboDeEgresoService {

    @Autowired
    ReciboDeEgresoRepo repo;
    @Override
    public ReciboDeEgreso guardar(ReciboDeEgreso obj) {
     return repo.save(obj);
    }

    @Override
    public ReciboDeEgreso getReciboDeEgreso(int codigo) {
       return repo.getReferenceById(codigo);
    }

    @Override
    public ReciboDeEgreso procesarReciboDeEgreso(FacturaDeCompra obj, List<DetalleFacturaDeCompra> listaDet) {
        
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
    
}
