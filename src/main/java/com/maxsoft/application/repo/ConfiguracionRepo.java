package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;

/** Configuración general: una sola fila (id = 1). */
public interface ConfiguracionRepo extends JpaRepository<Configuracion, Integer> {
}
