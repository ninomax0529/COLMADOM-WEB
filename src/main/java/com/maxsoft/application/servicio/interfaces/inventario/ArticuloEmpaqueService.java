/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;


import com.maxsoft.application.modelo.ArticuloEmpaque;
import java.util.List;
import java.util.Optional;

public interface ArticuloEmpaqueService {

    List<ArticuloEmpaque> getPorArticulo(Integer codArticulo);

    Optional<ArticuloEmpaque> getPorCodigoBarra(String codigoBarra);

    Optional<ArticuloEmpaque> getEmpaqueBase(Integer codArticulo);

    ArticuloEmpaque guardar(ArticuloEmpaque empaque);

    void guardarLista(List<ArticuloEmpaque> empaques, String usuario);

    void eliminar(Integer codigo);
}