/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.TipoMovimiento;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface TipoMovimientoService {

    TipoMovimiento getTipoMovimientoa(int codigo);

    List<TipoMovimiento> getLista();
}
