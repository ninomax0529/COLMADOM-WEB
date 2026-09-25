/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.ArticuloAlmacen;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Maximiliano
 */
public interface ArticuloAlmacenService {

    ArticuloAlmacen guardar(ArticuloAlmacen alm);

    List<ArticuloAlmacen> getLista();
 
    
    Optional<List<ArticuloAlmacen>> buscarPorAlmacen(Integer idAlmacen);

    Optional<ArticuloAlmacen> buscarPorArticuloYAlmacen(
            Integer idArticulo, Integer idAlmacen);

}
