/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.venta;

import com.maxsoft.application.modelo.Cliente;
import java.util.List;

/**
 *
 * @author maximilianoalmonte
 */
public interface ClienteService {

    Cliente guardar(Cliente obj);

    /**
     * Guarda el cliente y, si cambió su límite de crédito, deja el cambio en
     * el histórico con el usuario y el motivo.
     */
    Cliente guardar(Cliente obj, String usuario, String motivoCambioLimite);

    List<Cliente> getLista();

    /**
     * Cliente de las ventas de contado y del delivery sin cliente: PUBLICO
     * GENERAL, buscado por nombre (antes se tomaba el primero de la lista).
     *
     * @throws IllegalStateException si no existe
     */
    Cliente getClienteGenerico();
}
