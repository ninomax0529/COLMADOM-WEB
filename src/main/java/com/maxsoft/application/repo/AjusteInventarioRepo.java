/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AjusteInventarioRepo extends JpaRepository<AjusteInventario, Integer> {

    String strDet = "  select * from  detalle_ajuste_inventario o where ajuste_inventario=:obj ";

    @Query(value = strDet, nativeQuery = true)
    List<DetalleAjusteInventario> getDetalle(@Param("obj") int codigo);
}
