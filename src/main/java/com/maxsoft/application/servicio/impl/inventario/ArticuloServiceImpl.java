/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticuloServiceImpl implements ArticuloService {

    @Autowired 
    ArticuloRepo repo;
    
    @Override
    public Articulo guardar(Articulo art) {
       
        return repo.save(art);
    }

    @Override
    public List<Articulo> getLista() {
       
         List<Articulo> lista=null;
         lista=repo.findAll();
//        
        return lista;
        
    }

    @Override
    public void eliminarArticulo(int codigo) {
        
         repo.deleteById(codigo);
    }

    @Override
    public Articulo buscarPorCodigo(int codigo) {
       return repo.findById(codigo).get();
    }

  
    
}
