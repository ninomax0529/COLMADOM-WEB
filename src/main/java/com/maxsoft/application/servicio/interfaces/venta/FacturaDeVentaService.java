/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.venta;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import java.util.List;

/**
 *
 * @author maximilianoalmonte
 */
public interface FacturaDeVentaService {

    FacturaDeVenta guardar(FacturaDeVenta obj);
    
   FacturaDeVenta getFactura(int codigo);

    FacturaDeVenta procesarVenta(TicketVenta obj, String usaurio);

    List<FacturaDeVenta> getLista();

    List<DetalleFacturaDeVenta> getDetalle(int obj);

    FacturaDeVenta anularVenta(Integer idFactura, String motivoAnulacion, String nombreUsuario);
    FacturaDeVenta procesarDevolucion(SolicitudDevolucionDto solicitud);
    
    
    
}
