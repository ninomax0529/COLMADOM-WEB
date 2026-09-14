/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.repo.EstadoFacturaRepo;
import com.maxsoft.application.servicio.interfaces.venta.EstadoFacturaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EstadoFacturaServiceImpl implements EstadoFacturaService {

    @Autowired
    EstadoFacturaRepo repo;
    
    @Override
    public List<EstadoFactura> getLista() {
        return repo.findAll();
    }

    @Override
    public EstadoFactura getEstadoFactura(int codigo) {
        return  repo.getEstadoFactura(codigo);
    }
    
}
