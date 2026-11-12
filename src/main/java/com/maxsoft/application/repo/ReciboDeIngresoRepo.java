/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReciboDeIngresoRepo extends JpaRepository<ReciboDeIngreso, Integer> {

    String strDet = "  select * from  detalle_recibo_de_ingreso o where recibo_ingreso=:obj ";

    @Query(value = strDet, nativeQuery = true)
    public List<DetalleReciboDeIngreso> getDetalle(@Param("obj") int op);

    @Query(value = "SELECT PAGO_FACT_VENTA(:fact)", nativeQuery = true)
    Double getPagoFacturaVenta(@Param("fact") int fact);

    /** Recibos vigentes que pagaron la factura y cuyo concepto empieza por el indicado. */
    @Query("select distinct d.reciboIngreso from DetalleReciboDeIngreso d "
            + "where d.factura.codigo = :factura "
            + "and (d.reciboIngreso.anulado is null or d.reciboIngreso.anulado = false) "
            + "and d.reciboIngreso.concepto like concat(:prefijo, '%')")
    List<ReciboDeIngreso> findVigentesPorFacturaYConcepto(@Param("factura") Integer factura,
            @Param("prefijo") String prefijoConcepto);

    /** Recibos vigentes que pagaron la factura (cualquier origen). */
    @Query("select distinct d.reciboIngreso from DetalleReciboDeIngreso d "
            + "where d.factura.codigo = :factura "
            + "and (d.reciboIngreso.anulado is null or d.reciboIngreso.anulado = false)")
    List<ReciboDeIngreso> findVigentesPorFactura(@Param("factura") Integer factura);
}
