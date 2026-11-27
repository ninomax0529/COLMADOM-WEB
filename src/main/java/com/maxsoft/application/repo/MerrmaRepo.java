/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Merma;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MerrmaRepo extends JpaRepository<Merma, Integer> {
    
     String strDet = "  SELECT * FROM  detalle_merma  where merma=:m ";
    @Query(value = strDet, nativeQuery = true)
    public List<DetalleMerma> getDetalle(int m);

    /**
     * Mermas entre dos fechas (inclusive), opcionalmente de un tipo y/o
     * almacén (null = todos), con sus relaciones en la misma consulta. Las
     * más recientes primero.
     */
    @Query("SELECT m FROM Merma m"
            + " JOIN FETCH m.almacen JOIN FETCH m.tipoMerma"
            + " LEFT JOIN FETCH m.usuario LEFT JOIN FETCH m.salidaInventario"
            + " WHERE m.fecha BETWEEN :desde AND :hasta"
            + " AND (:tipo IS NULL OR m.tipoMerma.codigo = :tipo)"
            + " AND (:almacen IS NULL OR m.almacen.codigo = :almacen)"
            + " ORDER BY m.fecha DESC, m.codigo DESC")
    List<Merma> buscar(@org.springframework.data.repository.query.Param("desde") java.util.Date desde,
            @org.springframework.data.repository.query.Param("hasta") java.util.Date hasta,
            @org.springframework.data.repository.query.Param("tipo") Integer tipo,
            @org.springframework.data.repository.query.Param("almacen") Integer almacen);

    /**
     * Pérdida por tipo de merma (sin anuladas): [codigo tipo, descripción,
     * cantidad de mermas, valor]. La de más valor primero.
     */
    @Query(value = "SELECT t.codigo, t.descripcion, COUNT(DISTINCT m.codigo), COALESCE(SUM(d.total), 0)"
            + " FROM merma m JOIN tipo_merma t ON t.codigo = m.tipo_merma"
            + " JOIN detalle_merma d ON d.merma = m.codigo"
            + " WHERE m.anulada = 0 AND m.fecha BETWEEN :desde AND :hasta"
            + " AND (:almacen IS NULL OR m.almacen = :almacen)"
            + " GROUP BY t.codigo, t.descripcion ORDER BY 4 DESC", nativeQuery = true)
    List<Object[]> resumenPorTipo(@org.springframework.data.repository.query.Param("desde") java.util.Date desde,
            @org.springframework.data.repository.query.Param("hasta") java.util.Date hasta,
            @org.springframework.data.repository.query.Param("almacen") Integer almacen);

    /**
     * Pérdida por artículo (sin anuladas): [codigo artículo, descripción,
     * veces que se mermó, cantidad en unidad base, valor]. El de más valor
     * primero.
     */
    @Query(value = "SELECT d.articulo, MAX(d.descripcion_articulo), COUNT(DISTINCT m.codigo),"
            + " COALESCE(SUM(d.cantidad * d.factor_conversion), 0), COALESCE(SUM(d.total), 0)"
            + " FROM merma m JOIN detalle_merma d ON d.merma = m.codigo"
            + " WHERE m.anulada = 0 AND m.fecha BETWEEN :desde AND :hasta"
            + " AND (:almacen IS NULL OR m.almacen = :almacen)"
            + " GROUP BY d.articulo ORDER BY 5 DESC", nativeQuery = true)
    List<Object[]> resumenPorArticulo(@org.springframework.data.repository.query.Param("desde") java.util.Date desde,
            @org.springframework.data.repository.query.Param("hasta") java.util.Date hasta,
            @org.springframework.data.repository.query.Param("almacen") Integer almacen);

    /** Ventas netas del período: total de las facturas no anuladas menos lo devuelto. */
    @Query(value = "SELECT COALESCE(SUM(f.total - COALESCE(f.total_devuelto, 0)), 0) FROM factura_de_venta f"
            + " WHERE f.anulada = 0 AND f.fecha BETWEEN :desde AND :hasta", nativeQuery = true)
    java.math.BigDecimal ventasNetas(@org.springframework.data.repository.query.Param("desde") java.util.Date desde,
            @org.springframework.data.repository.query.Param("hasta") java.util.Date hasta);
}
