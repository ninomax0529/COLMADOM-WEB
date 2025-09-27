/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Maximiliano
 */
public interface SalidaInventarioRepo extends JpaRepository<SalidaInventario, Integer> {

       String strDet = "  select * from  detalle_salida_inventario o where salida_inventario=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleSalidaInventario> getDetalle(@Param("obj") int op);
    
    
      String strDetProHabilitado = """
                           
                           SELECT * FROM   salida_inventario o 
                             
                             where anulada=:estado
                           
                             """;
    @Query(value = strDetProHabilitado, nativeQuery = true)
    List<SalidaInventario> getLista(@Param("estado") boolean estado);
    
}
