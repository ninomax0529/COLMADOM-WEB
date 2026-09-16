/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.repo.AlmacenRepo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;

@Service
public class AlmacenServiceImpl implements AlmacenService {

    @Autowired
    AlmacenRepo repo;

    @Override
    public List<Almacen> getLista() {
        return repo.findAll();
    }

    @Override
    public Almacen getAlmacen(Integer codigo) {
        return repo.findById(codigo).get();
    }

}
