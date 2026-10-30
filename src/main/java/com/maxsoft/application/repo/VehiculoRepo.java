/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Vehiculo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author Maximiliano
 */
public interface VehiculoRepo extends JpaRepository<Vehiculo, Integer> {

    String strLista = "  select * from  vehiculo o  ";

    @Query(value = strLista, nativeQuery = true)
    public List<Vehiculo> getLista();
}
