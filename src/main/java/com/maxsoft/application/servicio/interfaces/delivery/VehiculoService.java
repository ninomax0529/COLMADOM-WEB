/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.delivery;

import com.maxsoft.application.modelo.Vehiculo;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface VehiculoService {

    Vehiculo guardar(Vehiculo obj);

    List<Vehiculo> getLista();
}
