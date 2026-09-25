/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.compra;

import com.maxsoft.application.modelo.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorService {

    List<Proveedor> getLista();

    List<Proveedor> buscarPorNombre(String nombre);

    Optional<Proveedor> buscarPorCodigo(Integer codigo);

    Optional<Proveedor> buscarPorRnc(String rnc);

    Proveedor guardar(Proveedor proveedor);

}