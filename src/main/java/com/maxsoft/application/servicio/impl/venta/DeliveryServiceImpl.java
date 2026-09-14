/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.repo.DeliveryRepo;
import com.maxsoft.application.servicio.interfaces.venta.DeliveryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    DeliveryRepo repo;

    @Override
    public List<Delivery> getLista() {
        return repo.getLista();
    }

}
