/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces;

import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author maximilianoalmonte
 */

public interface FacturaDeVentaService {
    
    
    FacturaDeVenta guardar(FacturaDeVenta obj);

    List<FacturaDeVenta> getLista();

    List<DetalleFacturaDeVenta> getDetalle(int obj);
}
