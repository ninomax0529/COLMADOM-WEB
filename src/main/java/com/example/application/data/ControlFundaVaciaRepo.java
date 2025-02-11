/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

import com.example.application.modelo.ControlDeFundaVacia;
import com.example.application.modelo.DetalleCementoEmpacadoPorSilo;
import com.example.application.modelo.DetalleControlDeFundaVacia;
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
public interface ControlFundaVaciaRepo extends JpaRepository<ControlDeFundaVacia, Integer> {

    String str1 = "  select * from  detalle_control_de_funda_vacia o where control_funda_vacia=:control ";

    @Query(value = str1, nativeQuery = true)
    public List<DetalleControlDeFundaVacia> getDetalle(@Param("control") int control);
    
    
    
      String strest = "  SELECT * from control_de_funda_vacia  "
            + " where  date(fecha)=:fecha ";

    @Query(value = strest, nativeQuery = true)
    public ControlDeFundaVacia getControlDeFundaVacia(@Param("fecha") Date fecha);
}
