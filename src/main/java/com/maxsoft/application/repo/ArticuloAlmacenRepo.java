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

    /** Existencia del artículo en un almacén (sin bloquear); vacío si no está registrado ahí. */
    @Query(value = "select existencia from articulo_almacen where articulo = :idArticulo and almacen = :idAlmacen"
            + " limit 1", nativeQuery = true)
    Optional<java.math.BigDecimal> existenciaEnAlmacen(@Param("idArticulo") Integer idArticulo,
            @Param("idAlmacen") Integer idAlmacen);

    /** [código de artículo, existencia] de todos los artículos registrados en un almacén. */
    @Query("SELECT aa.articulo.codigo, aa.existencia FROM ArticuloAlmacen aa WHERE aa.almacen.codigo = :idAlmacen")
    java.util.List<Object[]> existenciasDelAlmacen(@Param("idAlmacen") Integer idAlmacen);

    /**
     * Por artículo, para saber si hay que trasladar al mostrador: [0] código,
     * [1] existencia en el almacén de venta, [2] mínimo en el almacén de
     * venta, [3] existencia positiva en los demás almacenes.
     */
    @Query(value = """
            SELECT aa.articulo,
                   COALESCE(SUM(CASE WHEN aa.almacen = :venta THEN aa.existencia END), 0),
                   COALESCE(MAX(CASE WHEN aa.almacen = :venta THEN aa.minimo END), 0),
                   COALESCE(SUM(CASE WHEN aa.almacen <> :venta AND aa.existencia > 0 THEN aa.existencia END), 0)
            FROM articulo_almacen aa
            WHERE (:articulo IS NULL OR aa.articulo = :articulo)
            GROUP BY aa.articulo
            """, nativeQuery = true)
    java.util.List<Object[]> estadosMostrador(@Param("venta") int almacenVenta,
            @Param("articulo") Integer articulo);

    /**
     * Artículos de venta, inventariables y habilitados, registrados en el
     * almacén de venta (los que vende el POS), con existencia en algún otro
     * almacén; una fila por almacén (de más a menos existencia):
     * [0] código, [1] descripción, [2] existencia en el mostrador, [3] mínimo
     * del mostrador, [4] máximo del mostrador, [5] código del almacén, [6] su
     * nombre, [7] existencia en ese almacén.
     */
    @Query(value = """
            SELECT a.codigo, a.descripcion,
                   COALESCE(v.existencia, 0), COALESCE(v.minimo, 0), COALESCE(v.maximo, 0),
                   o.almacen, al.nombre, o.existencia
            FROM articulo a
            JOIN articulo_almacen o ON o.articulo = a.codigo AND o.almacen <> :venta AND o.existencia > 0
            JOIN almacen al ON al.codigo = o.almacen
            JOIN articulo_almacen v ON v.articulo = a.codigo AND v.almacen = :venta
            WHERE a.inventariable = 1 AND a.habilitado = 1 AND a.para_venta = 1
            ORDER BY a.descripcion, o.existencia DESC
            """, nativeQuery = true)
    java.util.List<Object[]> existenciasFueraDelMostrador(@Param("venta") int almacenVenta);

    /** [nombre del almacén, existencia] de los otros almacenes con existencia, de mayor a menor. */
    @Query(value = """
            SELECT al.nombre, aa.existencia
            FROM articulo_almacen aa
            JOIN almacen al ON al.codigo = aa.almacen
            WHERE aa.articulo = :articulo AND aa.almacen <> :venta AND aa.existencia > 0
            ORDER BY aa.existencia DESC
            """, nativeQuery = true)
    java.util.List<Object[]> existenciaEnOtrosAlmacenes(@Param("articulo") Integer articulo,
            @Param("venta") int almacenVenta);

    /** Existencia total del artículo: suma de todos sus almacenes (unidad base). */
    @Query("SELECT COALESCE(SUM(a.existencia), 0) FROM ArticuloAlmacen a WHERE a.articulo.codigo = :idArticulo")
    java.math.BigDecimal sumarExistencia(@Param("idArticulo") Integer idArticulo);

    // Opción A: Con consulta JPQL explícita (Recomendada para mayor claridad)
    @Query("SELECT a FROM ArticuloAlmacen a WHERE a.articulo.codigo = :idArticulo AND a.almacen.codigo = :idAlmacen")
    Optional<ArticuloAlmacen> buscarPorArticuloYAlmacen(@Param("idArticulo") Integer idArticulo,
            @Param("idAlmacen") Integer idAlmacen);

    /**
     * Igual que buscarPorArticuloYAlmacen pero bloquea la fila (SELECT ... FOR
     * UPDATE) hasta que termine la transacción: dos cajas que mueven el mismo
     * artículo a la vez esperan su turno en vez de pisarse la existencia.
     */
    @Query(value = "select existencia from articulo_almacen where articulo = :idArticulo and almacen = :idAlmacen"
            + " for update", nativeQuery = true)
    Optional<java.math.BigDecimal> bloquearExistencia(@Param("idArticulo") Integer idArticulo,
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
