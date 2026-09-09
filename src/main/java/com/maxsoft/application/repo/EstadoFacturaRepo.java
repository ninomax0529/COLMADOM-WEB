/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Maximiliano
 */
public interface EstadoFacturaRepo extends JpaRepository<EstadoFactura, Integer> {

    String str = "  select * from  estado_factura o where codigo=:obj ";

    @Query(value = str, nativeQuery = true)
    public EstadoFactura getEstadoFactura(@Param("obj") int op);
}
