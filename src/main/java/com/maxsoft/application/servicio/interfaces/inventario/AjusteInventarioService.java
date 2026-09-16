/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;


import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import java.util.List;

public interface AjusteInventarioService {

    AjusteInventario guardar(AjusteInventario obj);

    List<AjusteInventario> getLista();

    List<DetalleAjusteInventario> getDetalle(int codigoAjuste);
    
    /**
     * Procesa un Ajuste de Inventario completo de forma atómica (@Transactional):
     * 1.Guarda la cabecera y el detalle del ajuste.2.Actualiza el stock de cada artículo.3. Registra la traza en la tabla movimiento_inventario.
     * @param ajuste
     * @param detalles
     * @param usuario
     * @return 
     */
    AjusteInventario procesarAjusteTransaccional(AjusteInventario ajuste, 
                                                 List<DetalleAjusteInventario> detalles, 
                                                 String usuario);
}