/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.MovimientoInventario;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Maximiliano
 */
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoInventarioRepo extends JpaRepository<MovimientoInventario, Integer> {
    
    @Query(" SELECT m FROM MovimientoInventario m WHERE m.articulo.codigo = :articulo ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventario> findByArticuloOrderByFechaMovimientoDesc(@Param("articulo") Integer articulo);

    List<MovimientoInventario> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(LocalDateTime inicio, LocalDateTime fin);

    /** Kardex de un lote: sus movimientos del más reciente al más antiguo. */
    @Query("SELECT m FROM MovimientoInventario m WHERE m.lote.codigo = :lote ORDER BY m.fechaMovimiento DESC, m.codigo DESC")
    List<MovimientoInventario> findPorLote(@Param("lote") Integer lote);

    /**
     * Movimientos de un documento ("SAL-154") de un tipo (1 entrada, 2
     * salida), con artículo, almacén y lote, en el orden en que se hicieron.
     */
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.articulo JOIN FETCH m.almacen LEFT JOIN FETCH m.lote"
            + " WHERE m.numeroDocumento = :documento AND m.tipoMovimiento.codigo = :tipo ORDER BY m.codigo")
    List<MovimientoInventario> findPorDocumento(@Param("documento") String documento, @Param("tipo") Integer tipo);
}
