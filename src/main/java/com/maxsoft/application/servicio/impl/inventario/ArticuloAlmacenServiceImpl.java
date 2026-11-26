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

    @Override
    public Optional<List<ArticuloAlmacen>> buscarPorArticulo(Integer idArticulo) {
        return repo.buscarPorArticulo(idArticulo);
    }

    @Override
    public java.util.Map<Integer, java.math.BigDecimal> existenciasDelAlmacen(Integer idAlmacen) {
        java.util.Map<Integer, java.math.BigDecimal> mapa = new java.util.HashMap<>();
        for (Object[] fila : repo.existenciasDelAlmacen(idAlmacen)) {
            java.math.BigDecimal existencia = fila[1] instanceof java.math.BigDecimal b ? b
                    : fila[1] != null ? new java.math.BigDecimal(fila[1].toString()) : java.math.BigDecimal.ZERO;
            mapa.put(((Number) fila[0]).intValue(), existencia);
        }
        return mapa;
    }
    
}
