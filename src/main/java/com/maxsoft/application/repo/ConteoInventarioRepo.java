package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.ConteoInventario;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConteoInventarioRepo extends JpaRepository<ConteoInventario, Integer> {

    /**
     * Artículos que se pueden contar en el almacén (los de la clasificación
     * ABC), con su último conteo en ese almacén. Columnas: [0] código,
     * [1] descripción, [2] clase, [3] unidad base, [4] ubicación/pasillo,
     * [5] fecha del último conteo (null = nunca), [6] existencia en el almacén.
     */
    @Query(value = """
            SELECT c.articulo, a.descripcion, c.clase, u.descripcion, aa.ubicacion_pasillo,
                   (SELECT MAX(ci.fecha)
                    FROM detalle_conteo_inventario d
                    JOIN conteo_inventario ci ON ci.codigo = d.conteo
                    WHERE d.articulo = c.articulo AND ci.almacen = :almacen),
                   COALESCE(aa.existencia, 0)
            FROM articulo_clasificacion_abc c
            JOIN articulo a ON a.codigo = c.articulo
            LEFT JOIN unidad u ON u.codigo = a.unidad_base
            LEFT JOIN articulo_almacen aa ON aa.articulo = c.articulo AND aa.almacen = :almacen
            """, nativeQuery = true)
    List<Object[]> candidatos(@Param("almacen") Integer almacen);

    /** Artículos distintos contados ese día en el almacén. */
    @Query(value = """
            SELECT COUNT(DISTINCT d.articulo)
            FROM detalle_conteo_inventario d
            JOIN conteo_inventario ci ON ci.codigo = d.conteo
            WHERE ci.almacen = :almacen AND ci.fecha = :fecha
            """, nativeQuery = true)
    long contadosEnFecha(@Param("almacen") Integer almacen, @Param("fecha") LocalDate fecha);

    /**
     * Líneas contadas desde una fecha, las más recientes primero. Columnas:
     * [0] fecha, [1] código del conteo, [2] artículo, [3] clase,
     * [4] existencia del sistema, [5] contada, [6] diferencia,
     * [7] ajuste incremento, [8] ajuste disminución.
     */
    @Query(value = """
            SELECT ci.fecha, ci.codigo, d.descripcion_articulo, d.clase,
                   d.existencia_sistema, d.cantidad_contada, d.diferencia,
                   ci.ajuste_incremento, ci.ajuste_disminucion
            FROM detalle_conteo_inventario d
            JOIN conteo_inventario ci ON ci.codigo = d.conteo
            WHERE ci.almacen = :almacen AND ci.fecha >= :desde
            ORDER BY ci.fecha DESC, ci.codigo DESC, d.codigo
            """, nativeQuery = true)
    List<Object[]> historial(@Param("almacen") Integer almacen, @Param("desde") LocalDate desde);
}
