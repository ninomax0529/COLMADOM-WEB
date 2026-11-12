package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.CuentaPorCobrar;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuentaPorCobrarRepo extends JpaRepository<CuentaPorCobrar, Integer> {

    @Query("SELECT c FROM CuentaPorCobrar c WHERE c.cliente.codigo = :cliente ORDER BY c.fecha ASC, c.codigo ASC")
    List<CuentaPorCobrar> findEstadoDeCuenta(@Param("cliente") Integer cliente);

    @Query("SELECT COALESCE(SUM(c.debito - c.credito), 0) FROM CuentaPorCobrar c WHERE c.factura.codigo = :factura")
    BigDecimal saldoFactura(@Param("factura") Integer factura);

    @Query("SELECT COUNT(c) > 0 FROM CuentaPorCobrar c WHERE c.factura.codigo = :factura AND c.tipoMovimiento = :tipo")
    boolean existeMovimientoFactura(@Param("factura") Integer factura, @Param("tipo") String tipo);

    /** Lo que un recibo le acreditó a una factura en el estado de cuenta. */
    @Query("SELECT COALESCE(SUM(c.credito), 0) FROM CuentaPorCobrar c WHERE c.reciboIngreso.codigo = :recibo "
            + "AND c.factura.codigo = :factura AND c.tipoMovimiento = 'RECIBO'")
    BigDecimal creditoDeRecibo(@Param("recibo") Integer recibo, @Param("factura") Integer factura);
}
