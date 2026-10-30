/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.delivery;

import com.maxsoft.application.modelo.Delivery;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface DeliveryService {

    Delivery guardar(Delivery obj);

    List<Delivery> getLista();
}
