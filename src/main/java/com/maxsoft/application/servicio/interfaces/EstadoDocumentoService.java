/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces;

import com.maxsoft.application.modelo.EstadoDocumento;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface EstadoDocumentoService {
    
    List<EstadoDocumento> getLista();
    EstadoDocumento getEstado(int codigo);
}
