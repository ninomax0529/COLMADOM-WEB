/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.cxp;

import com.maxsoft.application.modelo.DetalleFacturaDeCompra;
import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.RecepcionMercancia;
import java.util.List;

/**
 *
 * @author maximilianoalmonte
 */
public interface FacturaDeCompraService {

    FacturaDeCompra guardar(FacturaDeCompra obj);

    FacturaDeCompra getFactura(int codigo);

    FacturaDeCompra procesarFactura(RecepcionMercancia obj, List<DetalleRecepcionMercancia> listaDet);

    FacturaDeCompra procesarFacturaInformal(RecepcionMercancia obj, List<DetalleRecepcionMercancia> listaDet);

    List<FacturaDeCompra> getLista();

    List<DetalleFacturaDeCompra> getDetalle(int obj);

    FacturaDeCompra anularFactura(Integer idFactura, String motivoAnulacion);

    List<FacturaDeCompra> getListaFactPendiente(int codPro);

}
