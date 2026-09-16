/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.TipoDocumento;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface TipoDocumentoService {

    TipoDocumento getTipoDocumento(int codigo);

    List<TipoDocumento> getLista();
}
