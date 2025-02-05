/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

/**
 *
 * @author Maximiliano
 */
public interface InventarioService {

    public Double getInventarioCemento(String articulo);

    public Double getInventarioCemento(String almacen,String articulo);

    public Double getInventarioFundaVacia(String articulo);

}
