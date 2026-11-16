package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleDevolucionProveedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetalleDevolucionProveedorRepo extends JpaRepository<DetalleDevolucionProveedor, Integer> {

    @Query("select d from DetalleDevolucionProveedor d where d.devolucion.codigo = :devolucion order by d.codigo")
    List<DetalleDevolucionProveedor> findPorDevolucion(@Param("devolucion") Integer devolucion);
}