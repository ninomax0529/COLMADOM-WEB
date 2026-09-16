/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.TipoAjuste;
import com.maxsoft.application.repo.TipoAjusteRepo;
import com.maxsoft.application.servicio.interfaces.inventario.TipoAjusteService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TipoAjusteServiceImpl implements TipoAjusteService {

    @Autowired
    TipoAjusteRepo repo;
    @Override
    public List<TipoAjuste> getLista() {
       return repo.findAll();
    }
    
}
