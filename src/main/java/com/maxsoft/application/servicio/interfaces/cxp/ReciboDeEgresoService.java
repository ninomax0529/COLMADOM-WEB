/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.cxp;

import com.maxsoft.application.modelo.DetalleFacturaDeCompra;
import com.maxsoft.application.modelo.DetalleReciboDeEgreso;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import java.util.List;

/**
 *
 * @author maximilianoalmonte
 */
public interface ReciboDeEgresoService {

    ReciboDeEgreso guardar(ReciboDeEgreso obj);

    ReciboDeEgreso getReciboDeEgreso(int codigo);

    ReciboDeEgreso procesarReciboDeEgreso(FacturaDeCompra obj, List<DetalleFacturaDeCompra> listaDet);

    List<ReciboDeEgreso> getLista();

    List<DetalleReciboDeEgreso> getDetalle(int obj);

    ReciboDeEgreso anularReciboDeEgreso(Integer id);
//    List<FacturaDeCompra> filtrarFacturas(EstadoFactura estado, CondicionPago condicion);

}
