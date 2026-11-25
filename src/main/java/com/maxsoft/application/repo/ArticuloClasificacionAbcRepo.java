package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.ArticuloClasificacionAbc;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticuloClasificacionAbcRepo extends JpaRepository<ArticuloClasificacionAbc, Integer> {

    /**
     * Ventas del período por artículo de venta: los que tienen una
     * presentación "se vende en" (igual que el POS) o vendieron algo en el
     * período. No se usan articulo.para_venta ni habilitado: hoy no se
     * guardan desde el formulario (están en 0). Columnas: [0] código, [1] fecha_creacion, [2] ventas netas sin
     * ITBIS, [3] facturas, [4] unidades base. Sin facturas anuladas y restando
     * lo devuelto de cada línea (proporcional a cantidad_devuelta).
     */
    @Query(value = """
            SELECT a.codigo,
                   a.fecha_creacion,
                   COALESCE(v.ventas, 0),
                   COALESCE(v.facturas, 0),
                   COALESCE(v.unidades, 0)
            FROM articulo a
            LEFT JOIN (
                SELECT d.articulo,
                       SUM((d.total - d.total_itbis)
                           * (d.cantidad - COALESCE(d.cantidad_devuelta, 0)) / NULLIF(d.cantidad, 0)) AS ventas,
                       COUNT(DISTINCT CASE WHEN d.cantidad > COALESCE(d.cantidad_devuelta, 0)
                                           THEN d.factura END) AS facturas,
                       SUM(COALESCE(d.cantidad_fisica_base, d.cantidad)
                           * (d.cantidad - COALESCE(d.cantidad_devuelta, 0)) / NULLIF(d.cantidad, 0)) AS unidades
                FROM detalle_factura_de_venta d
                JOIN factura_de_venta f ON f.codigo = d.factura
                WHERE (f.anulada IS NULL OR f.anulada = 0)
                  AND f.fecha BETWEEN :desde AND :hasta
                GROUP BY d.articulo
            ) v ON v.articulo = a.codigo
            WHERE v.articulo IS NOT NULL
               OR EXISTS (SELECT 1 FROM articulo_empaque e
                          WHERE e.articulo = a.codigo AND e.se_vende_en = 1)
            """, nativeQuery = true)
    List<Object[]> ventasPorArticulo(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    /**
     * Copia el mínimo y el máximo sugeridos a articulo.minimo / maximo de los
     * artículos indicados (solo los que tienen sugerencia).
     *
     * @return artículos actualizados
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query(value = """
            UPDATE articulo a
            JOIN articulo_clasificacion_abc c ON c.articulo = a.codigo
            SET a.minimo = c.minimo_sugerido,
                a.maximo = c.maximo_sugerido
            WHERE c.articulo IN (:articulos)
              AND c.minimo_sugerido IS NOT NULL
            """, nativeQuery = true)
    int aplicarSugeridos(@Param("articulos") java.util.Collection<Integer> articulos);

    /**
     * Artículos que venden, con su existencia ACTUAL (no la de la noche).
     * Columnas: [0] código, [1] descripción, [2] clase, [3] existencia,
     * [4] demanda diaria, [5] mínimo sugerido, [6] máximo sugerido.
     */
    @Query(value = """
            SELECT c.articulo, a.descripcion, c.clase, COALESCE(a.existencia, 0),
                   c.demanda_diaria, c.minimo_sugerido, c.maximo_sugerido
            FROM articulo_clasificacion_abc c
            JOIN articulo a ON a.codigo = c.articulo
            WHERE c.demanda_diaria > 0
            """, nativeQuery = true)
    List<Object[]> articulosQueVenden();

    /** Filas sin días con datos: calculadas antes de guardar la demanda diaria. */
    boolean existsByDiasConDatos(Integer diasConDatos);

    /** Filas que venden pero sin sugerencia: calculadas antes de guardar mínimo/máximo. */
    boolean existsByDemandaDiariaGreaterThanAndMinimoSugeridoIsNull(java.math.BigDecimal demanda);

    /**
     * Fecha de la primera factura no anulada: antes de ella el sistema no
     * facturaba, así que esos días no cuentan para la demanda diaria.
     */
    @Query(value = "SELECT MIN(fecha) FROM factura_de_venta WHERE anulada IS NULL OR anulada = 0",
            nativeQuery = true)
    Object primeraFechaDeVenta();
}
