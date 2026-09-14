/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Articulo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Maximiliano
 */
@Repository
public interface ArticuloRepo extends JpaRepository<Articulo, Integer>{
    
    // Retorna todos los productos que están en negativo para auditar compras no entradas
    @Query("SELECT a FROM Articulo a WHERE a.inventariable = true AND a.existencia < 0")
    List<Articulo> obtenerProductosEnNegativo();
}
