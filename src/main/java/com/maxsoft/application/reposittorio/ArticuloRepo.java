/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.reposittorio;

import com.maxsoft.application.modelo.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Maximiliano
 */
@Repository
public interface ArticuloRepo extends JpaRepository<Articulo, Integer>{
    
}
