/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.ArticuloAlmacen;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Maximiliano
 */
public interface ArticuloAlmacenRepo extends JpaRepository<ArticuloAlmacen, Integer> {

    // Opción A: Con consulta JPQL explícita (Recomendada para mayor claridad)
    @Query("SELECT a FROM ArticuloAlmacen a WHERE a.articulo.codigo = :idArticulo AND a.almacen.codigo = :idAlmacen")
    Optional<ArticuloAlmacen> buscarPorArticuloYAlmacen(@Param("idArticulo") Integer idArticulo,
            @Param("idAlmacen") Integer idAlmacen);

    @Query("SELECT aa FROM ArticuloAlmacen aa "
            + "JOIN FETCH aa.articulo a "
            + "JOIN FETCH aa.almacen alm "
            + "WHERE a.codigo = :idArticulo AND alm.codigo = :idAlmacen")
    Optional<ArticuloAlmacen> buscarPorArticuloYAlmacenOptimizado(@Param("idArticulo") Integer idArticulo,
            @Param("idAlmacen") Integer idAlmacen);

    @Query("SELECT aa FROM ArticuloAlmacen aa "
            + "JOIN FETCH aa.articulo a "
            + "JOIN FETCH aa.almacen alm "
            + "WHERE  alm.codigo = :idAlmacen")
    Optional<List<ArticuloAlmacen>> buscarPorAlmacen(@Param("idAlmacen") Integer idAlmacen);

    @Query("SELECT aa FROM ArticuloAlmacen aa "
            + "JOIN FETCH aa.articulo a "
            + "JOIN FETCH aa.almacen alm "
            + "WHERE  a.codigo = :idArticulo")
    Optional<List<ArticuloAlmacen>> buscarPorArticulo(@Param("idArticulo") Integer idAlmacen);

    // Opción B: Mediante convención de nombres de Spring Data JPA (Derived Query Method)
    Optional<ArticuloAlmacen> findByArticuloCodigoAndAlmacenCodigo(Integer idArticulo, Integer idAlmacen);
}
