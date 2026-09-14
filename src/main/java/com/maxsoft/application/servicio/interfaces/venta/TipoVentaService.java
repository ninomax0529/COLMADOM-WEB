/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.venta;

import com.maxsoft.application.modelo.TipoVenta;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface TipoVentaService {

    List<TipoVenta> getLista();

    TipoVenta getTipoVenta(int codigo);
}
