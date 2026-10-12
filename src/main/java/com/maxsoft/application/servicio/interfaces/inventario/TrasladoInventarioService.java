/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.TrasladoInventario;
import java.util.List;

public interface TrasladoInventarioService {

    TrasladoInventario procesarTraslado(TrasladoInventario traslado);

    List<TrasladoInventario> getLista();

    List<DetalleTrasladoInventario> getDetalle(int obj);
}
