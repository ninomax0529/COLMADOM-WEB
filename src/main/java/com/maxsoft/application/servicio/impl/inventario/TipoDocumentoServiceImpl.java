/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.repo.TipoDocumentoRepo;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    @Autowired
    TipoDocumentoRepo repo;

    @Override
    public List<TipoDocumento> getLista() {
        return repo.findAll();
    }

    @Override
    public TipoDocumento getTipoDocumento(int codigo) {

        return repo.findById(codigo).get();
    }

}
