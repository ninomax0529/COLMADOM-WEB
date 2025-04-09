/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl;

import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.repo.FacturaDeventaRepo;
import com.maxsoft.application.servicio.interfaces.FacturaDeVentaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FacturaDeVentaServiceImpl implements FacturaDeVentaService {

    @Autowired
    FacturaDeventaRepo facttRepo;

    @Override
    public FacturaDeVenta guardar(FacturaDeVenta obj) {

        return facttRepo.save(obj);
    }

    @Override
    public List<FacturaDeVenta> getLista() {

        List<FacturaDeVenta> lista = null;
        lista = facttRepo.findAll();
//        
        return lista;

    }

    @Override
    public List<DetalleFacturaDeVenta> getDetalle(int obj) {

        List<DetalleFacturaDeVenta> lista = null;
        lista = facttRepo.getDetalle(obj);

        return lista;
    }

}
