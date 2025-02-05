/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

import com.example.application.modelo.Equipo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Maximiliano
 */
@Repository
public interface EquipoRepo  extends JpaRepository<Equipo, Integer>{
    
      String str1 = "  select * from  equipo e where tipo_equipo=:tipo ";

    @Query(value = str1, nativeQuery = true)
    public List<Equipo> getLista(@Param("tipo") int tipo);

}
