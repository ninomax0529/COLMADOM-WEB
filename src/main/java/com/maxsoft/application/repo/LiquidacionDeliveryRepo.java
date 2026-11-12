package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleLiquidacionDelivery;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.LiquidacionDelivery;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LiquidacionDeliveryRepo extends JpaRepository<LiquidacionDelivery, Integer> {

    /**
     * Facturas de delivery del motorista que todavía no se han liquidado:
     * estado "delivery", no anuladas y que no figuran en ninguna liquidación
     * vigente (las líneas de liquidaciones anuladas no cuentan).
     * Es el único lugar que depende de los campos delivery, estadoFactura y
     * anulada de FacturaDeVenta.
     */
    @Query("select f from FacturaDeVenta f "
            + "where f.delivery.codigo = :delivery "
            + "and f.estadoFactura.codigo = :estado "
            + "and (f.anulada is null or f.anulada = false) "
            + "and not exists (select 1 from DetalleLiquidacionDelivery d where d.factura = f and d.anulada = false) "
            + "order by f.codigo")
    List<FacturaDeVenta> findPendientes(@Param("delivery") Integer codigoDelivery,
            @Param("estado") Integer codigoEstadoDelivery);

    /** Igual que {@link #findPendientes} pero de todos los motoristas. */
    @Query("select f from FacturaDeVenta f "
            + "where f.delivery is not null "
            + "and f.estadoFactura.codigo = :estado "
            + "and (f.anulada is null or f.anulada = false) "
            + "and not exists (select 1 from DetalleLiquidacionDelivery d where d.factura = f and d.anulada = false)")
    List<FacturaDeVenta> findPendientesTodos(@Param("estado") Integer codigoEstadoDelivery);

    /**
     * Facturas con motorista en un rango de fechas ([desde, hasta) ), cada una
     * con su línea de liquidación vigente (null si todavía no se liquidó o si
     * su liquidación se anuló). Cada fila es un Object[] { FacturaDeVenta,
     * DetalleLiquidacionDelivery }.
     */
    @Query("select f, d from FacturaDeVenta f "
            + "left join DetalleLiquidacionDelivery d on d.factura = f and d.anulada = false "
            + "where f.delivery is not null "
            + "and f.fecha >= :desde and f.fecha < :hasta "
            + "order by f.fecha desc, f.codigo desc")
    List<Object[]> findDeliveriesConLiquidacion(@Param("desde") Date desde,
            @Param("hasta") Date hastaExclusivo);

    /** Liquidaciones (vigentes y anuladas) en un rango de fechas [desde, hasta), las más nuevas primero. */
    @Query("select l from LiquidacionDelivery l join fetch l.delivery "
            + "where l.fecha >= :desde and l.fecha < :hasta "
            + "order by l.fecha desc, l.codigo desc")
    List<LiquidacionDelivery> findEntreFechas(@Param("desde") Date desde, @Param("hasta") Date hastaExclusivo);

    /** Líneas de una liquidación con su factura. */
    @Query("select d from DetalleLiquidacionDelivery d join fetch d.factura f "
            + "where d.liquidacionDelivery.codigo = :liquidacion order by f.codigo")
    List<DetalleLiquidacionDelivery> findDetalles(@Param("liquidacion") Integer codigoLiquidacion);
}
