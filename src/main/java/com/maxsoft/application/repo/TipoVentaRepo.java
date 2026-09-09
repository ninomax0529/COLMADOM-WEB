/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.TipoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Maximiliano
 */
public interface TipoVentaRepo extends JpaRepository<TipoVenta, Integer> {

    String str = "  select * from  tipo_venta o where codigo=:obj ";

    @Query(value = str, nativeQuery = true)
    public TipoVenta getTipoVenta(@Param("obj") int op);
}
