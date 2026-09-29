/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.compra;

import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.RecepcionMercancia;
import java.util.List;

public interface RecepcionMercanciaService {
    RecepcionMercancia procesarRecepcion(RecepcionMercancia recepcion);
    List<RecepcionMercancia> getLista();
    RecepcionMercancia buscarPorId(Long id);
     List<DetalleRecepcionMercancia> getDetalle(int op);
}