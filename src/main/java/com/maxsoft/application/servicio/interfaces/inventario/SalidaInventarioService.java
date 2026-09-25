/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.SalidaInventario;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface SalidaInventarioService {

    /**
     * Procesa de forma atómica (@Transactional) la Salida de Inventario: 1.
     * Valida el stock actual disponible de cada artículo. 2. Guarda el
     * documento de cabecera y detalle de la salida. 3. Descuenta el stock y
     * genera la traza imborrable en movimiento_inventario.
     *
     * @param obj Objeto con la cabecera y los detalles de la salida
     * @param usuario Nombre del usuario que realiza la operación
     * @return El objeto SalidaInventario persistido
     */
    SalidaInventario guardar(SalidaInventario obj, String usuario);

    List<SalidaInventario> getLista();

    List<DetalleSalidaInventario> getDetalle(int codigoSalida);

    List<SalidaInventario> getLista(boolean estado);

    SalidaInventario crearSalidaPorVenta(FacturaDeVenta factura);

    SalidaInventario crearSalidaPorAjuste(AjusteInventario ajuste, List<DetalleAjusteInventario> detalles);
}
