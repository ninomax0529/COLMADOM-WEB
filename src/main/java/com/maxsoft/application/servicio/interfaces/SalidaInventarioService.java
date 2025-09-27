/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces;

import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface SalidaInventarioService {

    SalidaInventario guardar(SalidaInventario obj);

    List<SalidaInventario> getLista();

    List<DetalleSalidaInventario> getDetalle(int obj);

    List<SalidaInventario> getLista(boolean estado);
}
