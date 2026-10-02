/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.repo.ArticuloAlmacenRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticuloAlmacenServiceImpl implements ArticuloAlmacenService {

    @Autowired
    ArticuloAlmacenRepo repo;
    
    @Override
    public List<ArticuloAlmacen> getLista() {
       return repo.findAll();
    }

    @Override
    public ArticuloAlmacen guardar(ArticuloAlmacen alm) {
        return repo.save(alm);
    }

    @Override
    public Optional<ArticuloAlmacen> buscarPorArticuloYAlmacen(Integer idArticulo, Integer idAlmacen) {
        return repo.buscarPorArticuloYAlmacenOptimizado(idArticulo, idAlmacen );
    }


    @Override
    public Optional<List<ArticuloAlmacen>> buscarPorAlmacen(Integer idAlmacen) {
        return repo.buscarPorAlmacen(idAlmacen);
    }
    
}
