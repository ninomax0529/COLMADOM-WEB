/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces;

import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface EntradaDeInventarioService {

    EntradaInventario guardar(EntradaInventario obj);

    List<EntradaInventario> getLista();

    List<DetalleEntradaInventario> getDetalle(int obj);
}
