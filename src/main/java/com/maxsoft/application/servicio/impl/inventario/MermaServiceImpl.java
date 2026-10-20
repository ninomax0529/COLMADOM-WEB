/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Merma;
import com.maxsoft.application.repo.MerrmaRepo;
import com.maxsoft.application.servicio.interfaces.inventario.MermaService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MermaServiceImpl implements MermaService {

    @Autowired
    MerrmaRepo repo;
    @Autowired
    SalidaInventarioService salidaInventarioService;

    @Override
    public List<Merma> getLista() {
        return repo.findAll();
    }

    @Override
    public List<DetalleMerma> getDetalle(Integer merma) {
        return repo.getDetalle(merma);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Merma procesarMerma(Merma merma, List<DetalleMerma> detalles, String usuario) {

        detalles.forEach(d -> {
            d.setCodigo(null);
            d.setMerma(merma);
        });

        merma.setNombreUsuario(usuario);
        merma.setDetalleMermaCollection(detalles);

        Merma mermadb = repo.save(merma);
        
        this.salidaInventarioService.crearSalidaPorMerma(merma, detalles);

        return mermadb;
    }

}
