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
}
