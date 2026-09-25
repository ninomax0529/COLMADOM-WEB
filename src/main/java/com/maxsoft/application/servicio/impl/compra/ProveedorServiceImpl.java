/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.compra;

import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.repo.ProveedorRepo;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    @Autowired
    ProveedorRepo repo;

    @Override
    public List<Proveedor> getLista() {
        return repo.findAll();
    }

    @Override
    public List<Proveedor> buscarPorNombre(String nombre) {
       return repo.findByNombre(nombre);
    }

    @Override
    public Optional<Proveedor> buscarPorCodigo(Integer codigo) {
      return repo.findById(codigo);
    }

    @Override
    public Optional<Proveedor> buscarPorRnc(String rnc) {
       return repo.findByRnc(rnc);
    }

    @Override
    public Proveedor guardar(Proveedor proveedor) {
       return repo.save(proveedor);
    }

}
