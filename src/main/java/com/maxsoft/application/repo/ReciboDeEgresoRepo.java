/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleReciboDeEgreso;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReciboDeEgresoRepo extends JpaRepository<ReciboDeEgreso, Integer> {

    String strDet = "  select * from  detalle_recibo_de_egreso o where recibo_egreso=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleReciboDeEgreso> getDetalle(@Param("obj") int op);
}
