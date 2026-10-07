/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.Unidad;
import java.util.List;

/**
 *
 * @author maximilianoalmonte
 */
public interface UnidadService {
    
       Unidad getUnidad(int codigo);
       List<Unidad> getLista();
}
