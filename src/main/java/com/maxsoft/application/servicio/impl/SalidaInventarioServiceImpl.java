/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl;


import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.repo.SalidaInventarioRepo;
import com.maxsoft.application.servicio.interfaces.SalidaInventarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalidaInventarioServiceImpl implements SalidaInventarioService {

    @Autowired
    SalidaInventarioRepo repo;

    @Override
    public SalidaInventario guardar(SalidaInventario obj) {
        return repo.save(obj);
    }

    @Override
    public List<SalidaInventario> getLista() {
       
        return repo.findAll();
    }

    @Override
    public List<DetalleSalidaInventario> getDetalle(int obj) {
        return repo.getDetalle(obj);
    }

    @Override
    public List<SalidaInventario> getLista(boolean estado) {
       return repo.getLista(estado);
    }

}
