/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

public interface InventarioService {
    void descontarStock(Integer idArticulo, Double cantidad, String referencia);
    void incrementarStock(Integer idArticulo, Double cantidad, String referencia);
    void validarStockDisponible(Integer idArticulo, Double cantidad);
}
//
//public interface InventarioService {
//
//    void descontarStock(Integer idArticulo, Double cantidad);
//    void incrementarStock(Integer idArticulo, Double cantidad); // <-- AGREGAR ESTE MÉTODO
//    void validarStockDisponible(Integer idArticulo, Double cantidad);
//}
//    

