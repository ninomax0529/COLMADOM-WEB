/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl;

import com.maxsoft.application.modelo.CajaChica;
import com.maxsoft.application.repo.CajaChicaRepo;
import com.maxsoft.application.servicio.interfaces.CajaChicaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CajaChicaServiceImpl implements CajaChicaService {

    @Autowired
    CajaChicaRepo repo;

    @Override
    public CajaChica guardar(CajaChica obj) {

        return repo.save(obj);
    }

    @Override
    public List<CajaChica> getLista() {
        return repo.findAll();
    }

}
