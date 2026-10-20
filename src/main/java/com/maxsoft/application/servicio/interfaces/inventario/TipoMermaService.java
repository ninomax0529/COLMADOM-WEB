/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.TipoMerma;
import java.util.List;

public interface TipoMermaService {

    /**
     * Todos los tipos (activos e inactivos): para consultas y filtros.
     * @return 
     */
    List<TipoMerma> getLista();

    /**
     * Solo los activos: para el formulario de registro.
     * @return 
     */
    List<TipoMerma> getListaActivos();

    TipoMerma guardar(TipoMerma tipoMerma);
}
