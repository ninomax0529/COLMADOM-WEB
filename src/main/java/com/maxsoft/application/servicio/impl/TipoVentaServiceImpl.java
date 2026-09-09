/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl;

import com.maxsoft.application.modelo.TipoVenta;
import com.maxsoft.application.repo.TipoVentaRepo;
import com.maxsoft.application.servicio.interfaces.TipoVentaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TipoVentaServiceImpl implements TipoVentaService {

    @Autowired
    TipoVentaRepo repo;

    @Override
    public List<TipoVenta> getLista() {
        return repo.findAll();
    }

    @Override
    public TipoVenta getTipoVenta(int codigo) {
       return repo.getTipoVenta(codigo);
    }

}
