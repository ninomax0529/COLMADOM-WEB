/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import java.util.List;

public interface EntradaDeInventarioService {

    /**
     * Procesa de forma atómica (@Transactional) el guardado de la Entrada de
     * Inventario: 1.Guarda la cabecera y el detalle de la entrada.2.Actualiza
     * el stock del artículo y registra la traza en movimiento_inventario.
     *
     * @param obj
     * @param usuario
     * @return
     */
    EntradaInventario guardar(EntradaInventario obj, String usuario);

    List<EntradaInventario> getLista();

    List<DetalleEntradaInventario> getDetalle(int obj);

    EntradaInventario crearEntradaPorDevolucion(FacturaDeVenta factura, SolicitudDevolucionDto solicitud);
}
