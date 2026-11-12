package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Empresa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepo extends JpaRepository<Empresa, Integer> {

    /** Datos del negocio (la tabla tiene un solo registro). */
    Optional<Empresa> findFirstByOrderByCodigoAsc();
}
