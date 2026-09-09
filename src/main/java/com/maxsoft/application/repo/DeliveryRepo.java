/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Delivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author Maximiliano
 */
public interface DeliveryRepo extends JpaRepository<Delivery, Integer> {

    String strLista = "  select * from  delivery o  ";

    @Query(value = strLista, nativeQuery = true)
    public List<Delivery> getLista();
//    public List<Delivery> getLista(@Param("obj") int op);
}
