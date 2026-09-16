/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.TipoMovimietoRepo;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TipoMovimientoServiceImpl implements TipoMovimientoService {

    @Autowired
    TipoMovimietoRepo repo;

    @Override
    public List<TipoMovimiento> getLista() {

        return repo.findAll();
    }

    @Override
    public TipoMovimiento getTipoMovimientoa(int codigo) {
        return repo.findById(codigo).get();
    }

}
