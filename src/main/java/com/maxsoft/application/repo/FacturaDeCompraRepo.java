/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.CondicionPago;
import com.maxsoft.application.modelo.DetalleFacturaDeCompra;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.FacturaDeCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author maximilianoalmonte
 */
public interface FacturaDeCompraRepo extends JpaRepository<FacturaDeCompra, Integer>{
    
    
    
    String strDet = "  select * from  detalle_factura_de_compra o where factura_de_compra=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleFacturaDeCompra> getDetalle(@Param("obj") int op);
    
       
    String strFac = "  select * from  factura_de_compra o where o.codigo=:codigo ";

    @Query(value = strFac, nativeQuery = true)
    public FacturaDeCompra getFactura(@Param("codigo") int op);
    
    
    String strPend = """
                       SELECT * FROM  factura_de_compra 
                       where  anulada=false
                        and  proveedor=:codPro
                        and  total_pendiente>0
                     
                        """;
    @Query(value = strPend, nativeQuery = true)
    public List<FacturaDeCompra> getListaFactPendiente(@Param("codPro") int codPro);

    
    // Query JPQL que maneja filtros opcionales automáticamente
//    @Query("SELECT f FROM FacturaCompra f WHERE " +
//           "(:estado IS NULL OR f.estado = :estado) AND " +
//           "(:condicion IS NULL OR f.condicionPago = :condicion) " +
//           "ORDER BY f.fechaRecepcion DESC")
//    List<FacturaDeCompra> filtrar(@Param("estado") EstadoFactura estado, 
//                                @Param("condicion") CondicionPago condicion);

    
}
