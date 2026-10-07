/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioService {

    /**
     * Registra de forma atómica un movimiento en el historial y actualiza el
     * stock real del artículo.
     *
     * @param articulo Objeto artículo afectado
     * @param almacen
     * @param articuloEmpaque
     * @param factorConversion
     * @param cantidadEmpaque
     * @param subTotal
     * @param itbis
     * @param total
     * @param tipoMovimiento "ENTRADA", "SALIDA", "AJUSTE_INCREMENTO" o
     * "AJUSTE_DECREMENTO"
     * @param tipoDocumento "COMPRA", "VENTA", "AJUSTE_INVENTARIO",
     * "SALIDA_INVENTARIO", etc.
     * @param numeroDoc Correlativo o número de documento
     * @param cantidad Cantidad a mover (debe ser mayor a 0)
     * @param usuario Usuario que ejecuta la transacción
     * @param observacion Detalle o razón del movimiento
     * @return El registro de MovimientoInventario guardado
     */
    MovimientoInventario registrarMovimiento(
            Articulo articulo,
            Almacen almacen,
            ArticuloEmpaque articuloEmpaque,
            BigDecimal factorConversion,
            BigDecimal cantidadEmpaque,
            BigDecimal subTotal,
            BigDecimal itbis,
            BigDecimal total,
            TipoMovimiento tipoMovimiento,
            TipoDocumento tipoDocumento,
            String numeroDoc,
            BigDecimal cantidad,
            String usuario,
            String observacion
    );

    /**
     * Obtiene el historial completo de movimientos de un artículo ordenado del
     * más reciente al más antiguo.
     *
     * @param articuloCodigo
     * @return
     */
    List<MovimientoInventario> getMovimientosPorArticulo(Integer articuloCodigo);

    /**
     * Obtiene los movimientos registrados dentro de un rango de fechas.
     *
     * @param inicio
     * @param fin
     * @return
     */
    List<MovimientoInventario> getMovimientosPorFechas(LocalDateTime inicio, LocalDateTime fin);
}
