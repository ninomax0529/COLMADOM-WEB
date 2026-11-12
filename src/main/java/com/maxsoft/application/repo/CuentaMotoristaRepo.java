package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.CuentaMotorista;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuentaMotoristaRepo extends JpaRepository<CuentaMotorista, Integer> {

    /** Lo que debe el motorista (suma de sus movimientos). */
    @Query("select coalesce(sum(c.monto), 0) from CuentaMotorista c where c.delivery.codigo = :delivery")
    BigDecimal saldo(@Param("delivery") Integer codigoDelivery);

    /** Saldo de todos los motoristas que tienen movimientos: Object[] { codigo, saldo }. */
    @Query("select c.delivery.codigo, sum(c.monto) from CuentaMotorista c group by c.delivery.codigo")
    List<Object[]> saldos();

    /**
     * Resumen por motorista: Object[] { codigo, cargos (faltantes), descargos
     * (abonos y reversos, en negativo), último movimiento }.
     */
    @Query("select c.delivery.codigo, "
            + "sum(case when c.monto > 0 then c.monto else 0 end), "
            + "sum(case when c.monto < 0 then c.monto else 0 end), "
            + "max(c.fecha) "
            + "from CuentaMotorista c group by c.delivery.codigo")
    List<Object[]> resumenPorMotorista();

    @Query("select c from CuentaMotorista c where c.delivery.codigo = :delivery order by c.fecha desc, c.codigo desc")
    List<CuentaMotorista> findMovimientos(@Param("delivery") Integer codigoDelivery);

    @Query("select c from CuentaMotorista c where c.liquidacionDelivery.codigo = :liquidacion")
    List<CuentaMotorista> findByLiquidacion(@Param("liquidacion") Integer codigoLiquidacion);
}
