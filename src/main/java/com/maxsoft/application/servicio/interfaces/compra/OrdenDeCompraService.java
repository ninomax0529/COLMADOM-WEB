/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.compra;

import com.maxsoft.application.modelo.OrdenDeCompra;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrdenDeCompraService {

    List<OrdenDeCompra> getLista();

    List<OrdenDeCompra> getListarPendientes();

    Optional<OrdenDeCompra> buscarPorCodigo(Integer codigo);

    Optional<OrdenDeCompra> buscarPorCodigoConDetalles(Integer codigo);

    OrdenDeCompra guardar(OrdenDeCompra orden);

    OrdenDeCompra autorizarOrden(Integer codigoOrden, Integer idAutorizador, String nombreAutorizador);

    OrdenDeCompra anularOrden(Integer codigoOrden, String motivo);

    OrdenDeCompra registrarRecepcionParcial(Integer codigoOrden, Integer codigoDetalle, BigDecimal cantidadRecibida);
}