/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio;

import com.maxsoft.application.modelo.Articulo;
import org.springframework.stereotype.Service;

/**
 *
 * @author maximilianoalmonte
 */
@Service
public class ArticuloDaoService {

    
    private Articulo articulo;
    /**
     * @return the articulo
     */
    public Articulo getArticulo() {
        return articulo;
    }

    /**
     * @param articulo the articulo to set
     */
    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }
    
    
}
