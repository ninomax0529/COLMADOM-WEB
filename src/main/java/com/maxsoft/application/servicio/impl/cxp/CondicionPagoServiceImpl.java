
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxp;

import com.maxsoft.application.modelo.CondicionPago;
import com.maxsoft.application.repo.CondicionPagoRepo;
import com.maxsoft.application.servicio.interfaces.cxp.CondicionPagoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CondicionPagoServiceImpl implements CondicionPagoService {

    @Autowired
    CondicionPagoRepo repo;

    @Override
    public List<CondicionPago> getLista() {
        return repo.findAll();
    }

    @Override
    public CondicionPago getCondicionPago(int codigo) {

        return repo.getCondicionPago(codigo);
    }

}
