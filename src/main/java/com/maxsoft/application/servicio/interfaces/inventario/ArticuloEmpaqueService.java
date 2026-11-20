/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.ArticuloEmpaque;
import java.util.List;
import java.util.Optional;

public interface ArticuloEmpaqueService {

    List<ArticuloEmpaque> getArticuloEmpaqueParaVenta();

    /**
     * Veces que se vendió cada presentación en los últimos {@code dias} días
     * (código de ArticuloEmpaque → líneas de factura no anuladas). Las que no
     * se vendieron no aparecen en el mapa.
     */
    java.util.Map<Integer, Long> vecesVendidoUltimosDias(int dias);

    List<ArticuloEmpaque> getPorArticulo(Integer codArticulo);

    Optional<ArticuloEmpaque> getPorCodigoBarra(String codigoBarra);

    Optional<ArticuloEmpaque> getEmpaqueBase(Integer codArticulo);

    ArticuloEmpaque guardar(ArticuloEmpaque empaque);

    void guardarLista(List<ArticuloEmpaque> empaques, String usuario);
    List<ArticuloEmpaque> buscarPorDescripcionEfectivo(String filtro);
    
    void actualizarCodigoBarra(Integer empaqueId, String nuevoCodigo);
    
    Optional<ArticuloEmpaque> buscarPorCodigoBarra(String codigoBarra);
    
    // AGREGA ESTA LÍNEA:
    List<ArticuloEmpaque> buscarPorDescripcionEfectivoPaginado(String filtro, int offset, int limit);
    
    

    void eliminar(Integer codigo);
}
