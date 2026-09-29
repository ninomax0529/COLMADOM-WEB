/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;


import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.RecepcionMercancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RecepcionMercanciaRepo extends JpaRepository<RecepcionMercancia, Integer> {

    
    String strDet = "  select * from  detalle_recepcion_mercancia o where recepcion_mercancia=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleRecepcionMercancia> getDetalle(@Param("obj") int op);
}