/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.cxp;

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

    ReciboDeEgreso procesarReciboDeEgreso(ReciboDeEgreso obj, List<DetalleReciboDeEgreso> listaDet);

    ReciboDeEgreso procesarReciboDeEgreso(FacturaDeCompra obj);

    /** Pago manual a proveedor; si es en efectivo registra la salida en la caja abierta. */
    ReciboDeEgreso registrarPagoProveedor(ReciboDeEgreso recibo, List<DetalleReciboDeEgreso> detalles,
            boolean enEfectivo, String usuario);

    List<ReciboDeEgreso> getLista();

    List<DetalleReciboDeEgreso> getDetalle(int obj);

    ReciboDeEgreso anularReciboDeEgreso(Integer id);

    /** Anula el recibo, devuelve el saldo a sus facturas y, si salió de caja, reingresa el efectivo. */
    ReciboDeEgreso anularReciboDeEgreso(Integer id, String motivo, String usuario);

    List<ReciboDeEgreso> getRecibosVigentesDeFactura(Integer factura);

    /** true si el recibo también paga facturas distintas a la indicada. */
    boolean pagaOtrasFacturas(Integer recibo, Integer factura);

    Double getPagoFacturaCompra(int fact);

}
