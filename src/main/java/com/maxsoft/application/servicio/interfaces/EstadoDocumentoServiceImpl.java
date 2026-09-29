/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces;

import com.maxsoft.application.modelo.EstadoDocumento;
import com.maxsoft.application.repo.EstadoDocumentoRepo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EstadoDocumentoServiceImpl implements EstadoDocumentoService {

    @Autowired
    EstadoDocumentoRepo repo;

    @Override
    public List<EstadoDocumento> getLista() {
        return repo.findAll();
    }

    @Override
    public EstadoDocumento getEstado(int codigo) {
      return repo.findById(codigo).get();
    }

}
