/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl;

import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.repo.EntradaDeInventarioRepo;
import com.maxsoft.application.servicio.interfaces.EntradaDeInventarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntradaDeInventarioServiceImpl implements EntradaDeInventarioService {

    @Autowired
    EntradaDeInventarioRepo entradaRepo;

    @Override
    public EntradaInventario guardar(EntradaInventario obj) {

        obj= entradaRepo.save(obj);
        
        return obj;

    }

    @Override
    public List<EntradaInventario> getLista() {

        List<EntradaInventario> lista = null;
        lista = entradaRepo.findAll();
//        
        return lista;

    }

    @Override
    public List<DetalleEntradaInventario> getDetalle(int obj) {
                
        List<DetalleEntradaInventario> lista = null;
        lista = entradaRepo.getDetalle(obj);
        
        return lista;
    }

}
