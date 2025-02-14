/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

import com.example.application.modelo.CementoDejadoEnPiso;
import com.example.application.modelo.DetalleCementoDejadoEnPiso;
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
public interface InventarioDeProductoRepo extends JpaRepository<CementoDejadoEnPiso, Integer> {

    String str1 = "  select * from  detalle_cemento_dejado_en_piso o where cemento_dejado_en_piso=:obj ";

    @Query(value = str1, nativeQuery = true)
    public List<DetalleCementoDejadoEnPiso> getDetalle(@Param("obj") int cep);

    String strest = "  SELECT * from cemento_dejado_en_piso  "
            + " where  date(fecha)=:fecha ";

    @Query(value = strest, nativeQuery = true)
    public CementoDejadoEnPiso getCementoDejadoEnPiso(@Param("fecha") Date fecha);

    String qlInvAyer = """
                         SELECT cantidad_ayer from cemento_dejado_en_piso o
                          
                           INNER JOIN  detalle_cemento_dejado_en_piso d 
                          
                           on d.cemento_dejado_en_piso=o.codigo
                          
                          where
                          
                          o.fecha=:fecha
                          
                           and d.producto=:produ """;

    @Query(value = qlInvAyer, nativeQuery = true)
    public Integer getInvAyer(@Param("fecha") Date fecha, @Param("produ") Integer produc);
}
