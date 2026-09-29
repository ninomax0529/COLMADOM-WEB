/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.modelo.TrasladoInventario;
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

    EntradaInventario crearEntradaPorAnulacionVenta(FacturaDeVenta factura, List<DetalleFacturaDeVenta> listaDetFact);

    EntradaInventario crearEntradaPorAjuste(AjusteInventario ajuste, List<DetalleAjusteInventario> detalleAju);

    EntradaInventario crearEntradaPorRecepcion(RecepcionMercancia ajuste, List<DetalleRecepcionMercancia> detalles);

    EntradaInventario crearEntradaPorTraslado(TrasladoInventario ajuste, List<DetalleTrasladoInventario> detalles);

}
