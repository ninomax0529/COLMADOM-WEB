/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

import com.example.application.modelo.CementoEmpacadoPorSilo;
import com.example.application.modelo.DetalleCementoEmpacadoPorSilo;
import com.example.application.modelo.OperacionEmpacadora;
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
public interface CementoEmpacadoPorsiloRepo extends JpaRepository<CementoEmpacadoPorSilo, Integer>{
    
    
    String str1 = "  select * from  detalle_cemento_empacado_por_silo o where empaque_silo=:cep ";

    @Query(value = str1, nativeQuery = true)
    public List<DetalleCementoEmpacadoPorSilo> getDetalle(@Param("cep") int cep);
    
    
      String strest = "  SELECT * from cemento_empacado_por_silo  "
            + " where  date(fecha)=:fecha ";

    @Query(value = strest, nativeQuery = true)
    public CementoEmpacadoPorSilo getCementoEmcadoPorSilo(@Param("fecha") Date fecha);


}
