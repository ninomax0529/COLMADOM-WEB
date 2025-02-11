/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

import com.example.application.modelo.DetalleProyeccionDeVenta;
import com.example.application.modelo.ProyecconDeVenta;
import java.util.Date;
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
public interface ProyeccionDeVentaRepo extends JpaRepository<ProyecconDeVenta, Integer> {

    String str1 = " select * from  detalle_proyeccion_de_venta o where proyeccion_venta=:pv ";

    @Query(value = str1, nativeQuery = true)
    public List<DetalleProyeccionDeVenta> getDetalle(@Param("pv") int control);

    String strest = "  SELECT * from proyeccon_de_venta  "
            + " where  date(fecha)=:fecha ";

    @Query(value = strest, nativeQuery = true)
    public ProyecconDeVenta getProyecconDeVenta(@Param("fecha") Date fecha);
}
