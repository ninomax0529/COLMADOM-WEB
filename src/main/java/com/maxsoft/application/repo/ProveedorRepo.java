/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepo extends JpaRepository<Proveedor, Integer> {

    Optional<Proveedor> findByRnc(String rnc);

    List<Proveedor> findByNombre(String nombre);

    @Query("SELECT s FROM Proveedor s " +
           "LEFT JOIN FETCH s.plazo " +
           "LEFT JOIN FETCH s.tipoSuplidor " +
           "WHERE s.codigo = :codigo")
    Optional<Proveedor> findByIdConRelaciones(@Param("codigo") Integer codigo);
}
