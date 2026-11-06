package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.HistorialLimiteCredito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistorialLimiteCreditoRepo extends JpaRepository<HistorialLimiteCredito, Integer> {

    @Query("SELECT h FROM HistorialLimiteCredito h WHERE h.cliente.codigo = :cliente ORDER BY h.fecha DESC, h.codigo DESC")
    List<HistorialLimiteCredito> findByCliente(@Param("cliente") Integer cliente);
}
