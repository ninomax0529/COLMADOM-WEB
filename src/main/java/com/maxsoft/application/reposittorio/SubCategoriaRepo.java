/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.reposittorio;

import com.maxsoft.application.modelo.SubCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Maximiliano
 */
public interface SubCategoriaRepo extends JpaRepository<SubCategoria, Integer>{
    
}
