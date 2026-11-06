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

    /**
     * Anula la recepción reversando sus efectos: pagos (y caja), factura de
     * compra, costo promedio, inventario y orden de compra.
     */
    RecepcionMercancia anularRecepcion(Integer codigo, String motivo, String usuario);

    /**
     * Revisa, con los costos ya actualizados, qué empaques a la venta de estos
     * artículos quedaron con un costo igual o mayor que su precio de venta.
     * Devuelve un texto por empaque afectado (lista vacía si todo está bien).
     */
    List<String> empaquesVendiendoConPerdida(java.util.Collection<Integer> articulos);

    List<RecepcionMercancia> getLista();

    RecepcionMercancia buscarPorId(Long id);

    List<DetalleRecepcionMercancia> getDetalle(int op);
}
