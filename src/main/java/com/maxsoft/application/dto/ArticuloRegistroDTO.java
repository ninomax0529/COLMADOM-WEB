/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.dto;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.ArticuloEmpaque;

import java.io.Serializable;
import java.util.List;

public class ArticuloRegistroDTO implements Serializable {

    private Articulo articulo;
    private List<ArticuloAlmacen> almacenes; // 🔥 Ahora soporta múltiples almacenes
    private List<ArticuloEmpaque> empaques;

    public ArticuloRegistroDTO() {
    }

    public ArticuloRegistroDTO(Articulo articulo, List<ArticuloAlmacen> almacenes, List<ArticuloEmpaque> empaques) {
        this.articulo = articulo;
        this.almacenes = almacenes;
        this.empaques = empaques;
    }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public List<ArticuloAlmacen> getAlmacenes() { return almacenes; }
    public void setAlmacenes(List<ArticuloAlmacen> almacenes) { this.almacenes = almacenes; }

    public List<ArticuloEmpaque> getEmpaques() { return empaques; }
    public void setEmpaques(List<ArticuloEmpaque> empaques) { this.empaques = empaques; }
}