/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.cxc;

import com.maxsoft.application.modelo.DetalleLiquidacionDelivery;
import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author maximilianoalmonte
 */
public interface ReciboDeIngresoService {

    ReciboDeIngreso guardar(ReciboDeIngreso obj);

    ReciboDeIngreso getReciboDeIngreso(int codigo);

    ReciboDeIngreso procesarReciboDeIngresoPos(FacturaDeVenta obj);

    ReciboDeIngreso procesarReciboDeIngreso(ReciboDeIngreso obj, List<DetalleReciboDeIngreso> listaDet);

    /**
     * Cobro de un cliente a sus facturas a crédito (CxC). Si es en efectivo,
     * exige caja abierta y registra la entrada del dinero en la gaveta.
     */
    ReciboDeIngreso registrarCobroCliente(ReciboDeIngreso recibo, List<DetalleReciboDeIngreso> detalles,
            boolean enEfectivo, String usuario);

    /**
     * Abono en efectivo a la libreta desde el POS: reparte el monto entre las
     * facturas a crédito pendientes del cliente, de la más vieja a la más nueva.
     */
    ReciboDeIngreso abonarLibreta(Integer cliente, BigDecimal monto, String usuario);

    /** Total pendiente de las facturas a crédito del cliente (lo máximo que puede abonar). */
    BigDecimal pendienteLibreta(Integer cliente);

    void procesarReciboDeIngresoLiquidacionDelivery(List<DetalleLiquidacionDelivery> listLiquidacion);

    List<ReciboDeIngreso> getLista();

    List<DetalleReciboDeIngreso> getDetalle(int obj);

    ReciboDeEgreso anularReciboDeIngreso(Integer id);

    Double getPagoFacturaVenta(int fact);

}
