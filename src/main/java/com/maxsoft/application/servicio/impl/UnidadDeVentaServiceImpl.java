/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl;

import com.maxsoft.application.modelo.UnidadDeVenta;
import com.maxsoft.application.repo.UnidadDeVentaRepo;
import com.maxsoft.application.servicio.interfaces.UnidadDeVentaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnidadDeVentaServiceImpl implements UnidadDeVentaService {

    @Autowired
     UnidadDeVentaRepo unidadVenta;
    
    @Override
    public UnidadDeVenta getUnidad(int codigo) {
       
        return unidadVenta.findById(codigo).get();
    }

    @Override
    public List<UnidadDeVenta> getLista() {
              
         List<UnidadDeVenta> lista=null;
         lista=unidadVenta.findAll();
//        
        return lista;
      
    }
    
}
