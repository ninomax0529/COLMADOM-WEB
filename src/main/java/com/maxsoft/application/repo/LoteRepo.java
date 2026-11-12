package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Lote;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoteRepo extends JpaRepository<Lote, Integer> {

    Optional<Lote> findByArticuloCodigoAndNumeroLote(Integer codigoArticulo, String numeroLote);

    /** Lotes del artículo, los que vencen primero arriba (sin vencimiento al final). */
    @Query("select l from Lote l where l.articulo.codigo = :articulo"
            + " order by case when l.fechaVencimiento is null then 1 else 0 end,"
            + " l.fechaVencimiento, l.fechaEntrada, l.codigo")
    List<Lote> findPorArticulo(@Param("articulo") Integer codigoArticulo);

    /** Cuántos lotes del artículo empiezan con el prefijo (para generar el siguiente número automático). */
    @Query("select count(l) from Lote l where l.articulo.codigo = :articulo and l.numeroLote like concat(:prefijo, '%')")
    long contarConPrefijo(@Param("articulo") Integer codigoArticulo, @Param("prefijo") String prefijo);

    /** Lotes con existencia que vencen hasta la fecha indicada (incluye los ya vencidos). */
    @Query("select distinct l from LoteAlmacen la join la.lote l"
            + " where la.existencia > 0 and l.fechaVencimiento is not null and l.fechaVencimiento <= :hasta"
            + " order by l.fechaVencimiento")
    List<Lote> findConExistenciaQueVencenHasta(@Param("hasta") LocalDate hasta);
}
