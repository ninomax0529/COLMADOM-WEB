package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.LiquidacionDelivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LiquidacionDeliveryRepo extends JpaRepository<LiquidacionDelivery, Integer> {

    /**
     * Facturas de delivery del motorista que todavía no se han liquidado:
     * estado "delivery", no anuladas y que no figuran en ninguna liquidación.
     * Es el único lugar que depende de los campos delivery, estadoFactura y
     * anulada de FacturaDeVenta.
     */
    @Query("select f from FacturaDeVenta f "
            + "where f.delivery.codigo = :delivery "
            + "and f.estadoFactura.codigo = :estado "
            + "and (f.anulada is null or f.anulada = false) "
            + "and not exists (select 1 from DetalleLiquidacionDelivery d where d.factura = f) "
            + "order by f.codigo")
    List<FacturaDeVenta> findPendientes(@Param("delivery") Integer codigoDelivery,
            @Param("estado") Integer codigoEstadoDelivery);
}
