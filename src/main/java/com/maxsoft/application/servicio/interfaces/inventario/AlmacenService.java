/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.Almacen;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface AlmacenService {

    List<Almacen> getLista();

    Almacen getAlmacen(Integer codigo);
}
