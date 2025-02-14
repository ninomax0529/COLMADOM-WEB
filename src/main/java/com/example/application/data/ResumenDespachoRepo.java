/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

import com.example.application.modelo.DetalleResumenDespacho;
import com.example.application.modelo.ResumenDespacho;
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
public interface ResumenDespachoRepo extends JpaRepository<ResumenDespacho, Integer>{
    
        
    String str1 = "  select * from  detalle_resumen_despacho o where resumen_despacho=:obj ";

    @Query(value = str1, nativeQuery = true)
    public List<DetalleResumenDespacho> getDetalle(@Param("obj") int cep);

    String strest = "  SELECT * from resumen_despacho  "
            + " where  date(fecha)=:fecha ";

    @Query(value = strest, nativeQuery = true)
    public ResumenDespacho getResumenDespacho(@Param("fecha") Date fecha);

    String sqlTotalDesp = """
                         SELECT cantidad_ayer from cemento_dejado_en_piso o
                          
                           INNER JOIN  detalle_cemento_dejado_en_piso d 
                          
                           on d.cemento_dejado_en_piso=o.codigo
                          
                          where
                          
                          o.fecha=:fecha
                          
                           and d.producto=:produ """;

    @Query(value = sqlTotalDesp, nativeQuery = true)
    public Integer getTotalDespacho(@Param("fecha") Date fecha, @Param("produ") Integer produc);
    
}
