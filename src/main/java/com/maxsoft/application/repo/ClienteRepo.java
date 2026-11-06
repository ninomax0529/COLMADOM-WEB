/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Cliente;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author maximilianoalmonte
 */
public interface ClienteRepo extends JpaRepository<Cliente, Integer>{

    /**
     * Lee el balance bloqueando la fila del cliente hasta el fin de la
     * transacción, para que dos movimientos simultáneos no se pisen.
     */
    @Query(value = "SELECT balance FROM cliente WHERE codigo = :cliente FOR UPDATE", nativeQuery = true)
    BigDecimal bloquearYLeerBalance(@Param("cliente") Integer cliente);

    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE cliente SET balance = :balance WHERE codigo = :cliente", nativeQuery = true)
    int actualizarBalance(@Param("cliente") Integer cliente, @Param("balance") BigDecimal balance);

    @Query(value = "SELECT limite_credito FROM cliente WHERE codigo = :cliente", nativeQuery = true)
    BigDecimal leerLimiteCredito(@Param("cliente") Integer cliente);
}
