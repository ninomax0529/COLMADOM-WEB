/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Unidad;
import com.maxsoft.application.repo.UnidadRepo;
import com.maxsoft.application.servicio.interfaces.inventario.UnidadService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnidadServiceImpl implements UnidadService {

    @Autowired
    UnidadRepo repo;

    @Override
    public Unidad getUnidad(int codigo) {
      return repo.findById(codigo).get();
    }

    @Override
    public List<Unidad> getLista() {
        return repo.findAll();
    }

}
