/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.cxp;

import com.maxsoft.application.modelo.CondicionPago;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface CondicionPagoService {

    List<CondicionPago> getLista();

    CondicionPago getCondicionPago(int codigo);
}
