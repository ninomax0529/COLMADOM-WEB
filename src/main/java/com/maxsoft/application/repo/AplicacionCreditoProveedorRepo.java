package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.AplicacionCreditoProveedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AplicacionCreditoProveedorRepo extends JpaRepository<AplicacionCreditoProveedor, Integer> {

    @Query("select a from AplicacionCreditoProveedor a where a.devolucion.codigo = :devolucion order by a.codigo")
    List<AplicacionCreditoProveedor> findPorDevolucion(@Param("devolucion") Integer devolucion);
}