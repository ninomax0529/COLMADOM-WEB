/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;


import com.maxsoft.application.modelo.OrdenDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenDeCompraRepo extends JpaRepository<OrdenDeCompra, Integer> {

    Optional<OrdenDeCompra> findByNumero(String numero);

    List<OrdenDeCompra> findByEstado(String estado);

    List<OrdenDeCompra> findByProveedor(Proveedor proveedor);

    // Consulta optimizada con JOIN FETCH para evitar LazyInitializationException
//    @Query("SELECT DISTINCT o FROM OrdenDeCompra o " +
//        //   "LEFT JOIN FETCH o.DetalleOrdendeDeCompra d " +
//           "LEFT JOIN FETCH d.articulo " +
//           "LEFT JOIN FETCH o.tipoCompra " +
//           "LEFT JOIN FETCH o.moneda " +
//           "WHERE o.codigo = :codigo")
//    Optional<OrdenDeCompra> buscarPorCodigo(@Param("codigo") Integer codigo);

    @Query("SELECT DISTINCT o FROM OrdenDeCompra o " +
          // "LEFT JOIN FETCH o.DetalleOrdendeDeCompra d " +
           "WHERE o.facturada = false AND o.anulada = false")
    List<OrdenDeCompra> pendientesDeFacturacion();

}