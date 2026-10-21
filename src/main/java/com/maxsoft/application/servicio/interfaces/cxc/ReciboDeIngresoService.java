/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.cxc;

import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import java.util.List;

/**
 *
 * @author maximilianoalmonte
 */
public interface ReciboDeIngresoService {

    ReciboDeIngreso guardar(ReciboDeIngreso obj);

    ReciboDeIngreso getReciboDeIngreso(int codigo);

    ReciboDeIngreso procesarReciboDeIngresoPos(FacturaDeVenta obj);
    
    List<ReciboDeIngreso> getLista();

    List<DetalleReciboDeIngreso> getDetalle(int obj);

    ReciboDeEgreso anularReciboDeIngreso(Integer id);

}
