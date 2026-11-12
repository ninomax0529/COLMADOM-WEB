package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleFacturaVentaLote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetalleFacturaVentaLoteRepo extends JpaRepository<DetalleFacturaVentaLote, Integer> {

    /** Lotes consumidos por una línea de venta; el que vence más tarde primero (orden para reponer). */
    @Query("select d from DetalleFacturaVentaLote d join d.lote l where d.detalleFacturaDeVenta.codigo = :detalle"
            + " order by case when l.fechaVencimiento is null then 0 else 1 end,"
            + " l.fechaVencimiento desc, d.codigo desc")
    List<DetalleFacturaVentaLote> findPorDetalle(@Param("detalle") Integer codigoDetalle);

    /** Lotes consumidos por toda la factura. */
    @Query("select d from DetalleFacturaVentaLote d where d.detalleFacturaDeVenta.factura.codigo = :factura")
    List<DetalleFacturaVentaLote> findPorFactura(@Param("factura") Integer codigoFactura);
}
