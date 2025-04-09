/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author maximilianoalmonte
 */
public interface EntradaDeInventarioRepo extends JpaRepository<EntradaInventario, Integer>{
    
    
    String strDet = "  select * from  detalle_entrada_inventario o where entrada_inventario=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleEntradaInventario> getDetalle(@Param("obj") int op);

    
}
