/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.venta;

import com.maxsoft.application.modelo.EstadoFactura;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface EstadoFacturaService {

    List<EstadoFactura> getLista();

    EstadoFactura getEstadoFactura(int codigo);
}
