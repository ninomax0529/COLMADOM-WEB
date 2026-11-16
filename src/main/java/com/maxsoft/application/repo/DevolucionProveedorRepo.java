package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DevolucionProveedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DevolucionProveedorRepo extends JpaRepository<DevolucionProveedor, Integer> {

    @Query("select d from DevolucionProveedor d join fetch d.proveedor join fetch d.almacen order by d.codigo desc")
    List<DevolucionProveedor> findTodas();

    /** Notas de crédito vigentes del proveedor que todavía tienen saldo por aplicar. */
    @Query("select d from DevolucionProveedor d where d.proveedor.codigo = :proveedor and d.anulada = false"
            + " and d.tipoLiquidacion = 'NOTA_CREDITO' and d.creditoDisponible > 0 order by d.codigo")
    List<DevolucionProveedor> findConCreditoDisponible(@Param("proveedor") Integer proveedor);
}