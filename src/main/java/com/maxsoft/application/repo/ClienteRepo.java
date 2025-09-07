/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author maximilianoalmonte
 */
public interface ClienteRepo extends JpaRepository<Cliente, Integer>{
    
}
