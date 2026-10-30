/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.delivery;

import com.maxsoft.application.modelo.Vehiculo;
import com.maxsoft.application.repo.VehiculoRepo;
import com.maxsoft.application.servicio.interfaces.delivery.VehiculoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    @Autowired
    VehiculoRepo repo;

    @Override
    public Vehiculo guardar(Vehiculo obj) {
        return repo.save(obj);
    }

    @Override
    public List<Vehiculo> getLista() {
        return repo.getLista();
    }

}
