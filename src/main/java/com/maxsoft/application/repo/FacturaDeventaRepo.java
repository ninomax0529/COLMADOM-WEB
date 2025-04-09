/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author maximilianoalmonte
 */
public interface FacturaDeventaRepo extends JpaRepository<FacturaDeVenta, Integer>{
    
    
    
    String strDet = "  select * from  detalle_factura_de_venta o where factura=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleFacturaDeVenta> getDetalle(@Param("obj") int op);
    
}
