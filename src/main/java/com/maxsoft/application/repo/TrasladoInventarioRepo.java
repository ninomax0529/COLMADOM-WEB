/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.TrasladoInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Maximiliano
 */
public interface TrasladoInventarioRepo extends JpaRepository<TrasladoInventario, Integer>{
    
     String strDet = "  select * from  detalle_traslado_inventario o where traslado=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleTrasladoInventario> getDetalle(@Param("obj") int op);
}
