package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.LoteAlmacen;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoteAlmacenRepo extends JpaRepository<LoteAlmacen, Integer> {

    Optional<LoteAlmacen> findByLoteCodigoAndAlmacenCodigo(Integer codigoLote, Integer codigoAlmacen);

    /** Igual que la anterior pero bloquea la fila hasta que termine la transacción. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select la from LoteAlmacen la where la.lote.codigo = :lote and la.almacen.codigo = :almacen")
    Optional<LoteAlmacen> findParaActualizar(@Param("lote") Integer codigoLote, @Param("almacen") Integer codigoAlmacen);

    /**
     * Orden FEFO (primero vence, primero sale) de los lotes con existencia del
     * artículo en el almacén. Bloquea las filas para que dos ventas
     * simultáneas no tomen el mismo stock. Los lotes sin vencimiento van al
     * final, y a igual vencimiento sale primero el que entró antes.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select la from LoteAlmacen la join la.lote l"
            + " where l.articulo.codigo = :articulo and la.almacen.codigo = :almacen and la.existencia > 0"
            + " order by case when l.fechaVencimiento is null then 1 else 0 end,"
            + " l.fechaVencimiento, l.fechaEntrada, l.codigo")
    List<LoteAlmacen> findFefoParaActualizar(@Param("articulo") Integer codigoArticulo,
            @Param("almacen") Integer codigoAlmacen);

    /** Igual que el FEFO pero sin bloquear (para mostrar y para avisar de vencidos). */
    @Query("select la from LoteAlmacen la join fetch la.lote l"
            + " where l.articulo.codigo = :articulo and la.almacen.codigo = :almacen and la.existencia > 0"
            + " order by case when l.fechaVencimiento is null then 1 else 0 end,"
            + " l.fechaVencimiento, l.fechaEntrada, l.codigo")
    List<LoteAlmacen> findDisponibles(@Param("articulo") Integer codigoArticulo,
            @Param("almacen") Integer codigoAlmacen);

    /** Existencias por lote del artículo (todos los almacenes), para la ficha y las consultas. */
    @Query("select la from LoteAlmacen la join la.lote l where l.articulo.codigo = :articulo"
            + " order by la.almacen.codigo, case when l.fechaVencimiento is null then 1 else 0 end,"
            + " l.fechaVencimiento, l.codigo")
    List<LoteAlmacen> findPorArticulo(@Param("articulo") Integer codigoArticulo);

    /** Suma de los lotes del artículo en el almacén (para el chequeo de integridad). */
    @Query("select coalesce(sum(la.existencia), 0) from LoteAlmacen la"
            + " where la.lote.articulo.codigo = :articulo and la.almacen.codigo = :almacen")
    BigDecimal sumarExistencia(@Param("articulo") Integer codigoArticulo, @Param("almacen") Integer codigoAlmacen);

    /** Todas las existencias de lotes con stock (consulta de lotes y vencimientos). */
    @Query("select la from LoteAlmacen la join fetch la.lote l join fetch la.almacen"
            + " where la.existencia <> 0"
            + " order by case when l.fechaVencimiento is null then 1 else 0 end, l.fechaVencimiento")
    List<LoteAlmacen> findConExistencia();

    /**
     * Artículo/almacén donde articulo_almacen.existencia no cuadra con la suma
     * de sus lotes (solo artículos que controlan lote). Debe venir vacía.
     * Columnas: articulo, almacen, existencia, suma_lotes.
     */
    @Query(value = "SELECT aa.articulo, aa.almacen, aa.existencia, COALESCE(SUM(la.existencia),0) AS suma_lotes"
            + " FROM articulo_almacen aa"
            + " JOIN articulo a ON a.codigo = aa.articulo AND a.controla_lote = 1"
            + " LEFT JOIN lote l ON l.articulo = aa.articulo"
            + " LEFT JOIN lote_almacen la ON la.lote = l.codigo AND la.almacen = aa.almacen"
            + " GROUP BY aa.articulo, aa.almacen, aa.existencia"
            + " HAVING aa.existencia <> COALESCE(SUM(la.existencia),0)", nativeQuery = true)
    List<Object[]> findDescuadres();
}
