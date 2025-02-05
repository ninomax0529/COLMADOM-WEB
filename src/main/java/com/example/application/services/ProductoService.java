/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.Producto;
import java.util.List;

/**
 *
 * @author Maximiliano
 */

public interface ProductoService {
 
    public List<Producto> getLista();
    
      public List<DetalleMovimientoProducto> getListaMov();
    
    
}
