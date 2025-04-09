/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces;

import com.maxsoft.application.modelo.UnidadDeVenta;
import java.util.List;

/**
 *
 * @author maximilianoalmonte
 */
public interface UnidadDeVentaService {
    
       UnidadDeVenta getUnidad(int codigo);
       List<UnidadDeVenta> getLista();
}
