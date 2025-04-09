/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces;

import com.maxsoft.application.modelo.Articulo;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface ArticuloService {

    Articulo guardar(Articulo art);

    void eliminarArticulo(int codigo);

    List<Articulo> getLista();


}
