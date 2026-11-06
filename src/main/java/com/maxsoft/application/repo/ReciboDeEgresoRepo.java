/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleReciboDeEgreso;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReciboDeEgresoRepo extends JpaRepository<ReciboDeEgreso, Integer> {

    String strDet = "  select * from  detalle_recibo_de_egreso o where recibo_egreso=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleReciboDeEgreso> getDetalle(@Param("obj") int op);
    
    
    @Query(value = "SELECT PAGO_FACT_COMPRA(:fact)", nativeQuery = true)
    Double getPagoFacturaCompra(@Param("fact") int fact);

    /** Recibos de egreso vigentes (no anulados) que pagaron algo de esta factura de compra. */
    @Query("SELECT DISTINCT d.reciboEgreso FROM DetalleReciboDeEgreso d "
            + "WHERE d.factura.codigo = :factura AND (d.reciboEgreso.anulado = false OR d.reciboEgreso.anulado IS NULL)")
    List<ReciboDeEgreso> findVigentesPorFactura(@Param("factura") Integer factura);

    /** Cuántas líneas del recibo pagan facturas distintas a la indicada. */
    @Query("SELECT COUNT(d) FROM DetalleReciboDeEgreso d WHERE d.reciboEgreso.codigo = :recibo AND d.factura.codigo <> :factura")
    long contarOtrasFacturas(@Param("recibo") Integer recibo, @Param("factura") Integer factura);
}
