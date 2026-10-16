/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.ArticuloEmpaque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ArticuloEmpaqueRepo extends JpaRepository<ArticuloEmpaque, Integer> {

    List<ArticuloEmpaque> findByArticuloCodigo(Integer codArticulo);

    Optional<ArticuloEmpaque> findByCodigoBarra(String codigoBarra);

    Optional<ArticuloEmpaque> findByArticuloCodigoAndEsEmpaqueBaseTrue(Integer codArticulo);

    List<ArticuloEmpaque> findByArticuloCodigoAndEstadoTrue(Integer codArticulo);

    String strDet = "  SELECT * FROM  articulo_empaque   where  se_vende_en=1 ";

    @Query(value = strDet, nativeQuery = true)
    public List<ArticuloEmpaque> getArticuloEmpaqueParaVenta();
    // Busca los empaques por descripción que estén activos y habilitados
  @Query("SELECT ae FROM ArticuloEmpaque ae WHERE UPPER(ae.descripcionEmpaque) LIKE UPPER(CONCAT('%', :filtro, '%')) AND ae.estado=true ")
    List<ArticuloEmpaque> buscarPorDescripcionEfectivo(@Param("filtro") String filtro);
    
    @Modifying
    @Transactional // Permite que la operación tenga permisos de escritura directa en la tabla
    @Query("UPDATE ArticuloEmpaque ae SET ae.codigoBarra = :nuevoCodigo WHERE ae.codigo = :empaqueId")
    int modificarCodigoBarraDirecto(@Param("empaqueId") Integer empaqueId, @Param("nuevoCodigo") String nuevoCodigo);
    
    // Busca un empaque específico por su código de barras único
    @Query("SELECT ae FROM ArticuloEmpaque ae WHERE ae.codigoBarra = :codigoBarra AND ae.estado=true ")
    Optional<ArticuloEmpaque> obtenerPorCodigoBarraDirecto(@Param("codigoBarra") String codigoBarra);
    
//    // CORRECCIÓN DEFINITIVA DE TU PRIMER ERROR:
    @Query("SELECT ae FROM ArticuloEmpaque ae WHERE UPPER(ae.descripcionEmpaque) LIKE UPPER(CONCAT('%', :filtro, '%')) AND ae.estado =true ")
    Page<ArticuloEmpaque> buscarPorFiltro(@Param("filtro") String filtro, Pageable pageable);

}
