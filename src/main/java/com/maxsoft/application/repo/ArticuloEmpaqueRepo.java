/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ArticuloEmpaqueRepo extends JpaRepository<ArticuloEmpaque, Integer> {

    List<ArticuloEmpaque> findByArticuloCodigo(Integer codArticulo);

    Optional<ArticuloEmpaque> findByCodigoBarra(String codigoBarra);

    Optional<ArticuloEmpaque> findByArticuloCodigoAndEsEmpaqueBaseTrue(Integer codArticulo);

    List<ArticuloEmpaque> findByArticuloCodigoAndEstadoTrue(Integer codArticulo);

    String strDet = "  SELECT * FROM  articulo_empaque   where  se_vende_en=1 ";

    @Query(value = strDet, nativeQuery = true)
    public List<ArticuloEmpaque> getArticuloEmpaqueParaVenta();

}
