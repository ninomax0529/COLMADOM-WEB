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
     * @param cantidad Cantidad en la unidad del documento (cajas, fardos...),
     * solo informativa. El stock se mueve y se valida con
     * {@code cantidadEmpaque}, que debe venir en UNIDAD BASE
     * (cantidad del documento x factor de conversión) y ser mayor a 0.
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
     * Igual que {@link #registrarMovimiento}, con control de lotes. Si el
     * artículo controla lote:
     * <ul>
     * <li>Entrada: va al lote indicado, o al lote automático del día si es
     * null.</li>
     * <li>Salida: sale del lote indicado (error si no alcanza), o se reparte
     * por FEFO entre los lotes del almacén si es null.</li>
     * </ul>
     * Se graba una fila de kardex por cada lote tocado (con la existencia del
     * lote antes y después) y los montos se prorratean por cantidad. Si el
     * artículo no controla lote, el parámetro se ignora y queda una sola fila.
     *
     * @param lote lote del documento, o null
     * @return los movimientos grabados, en orden (al menos uno)
     */
    List<MovimientoInventario> registrarMovimientoConLote(
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
            String observacion,
            com.maxsoft.application.modelo.Lote lote
    );

    /**
     * Obtiene el historial completo de movimientos de un artículo ordenado del
     * más reciente al más antiguo.
     *
     * @param articuloCodigo
     * @return
     */
    List<MovimientoInventario> getMovimientosPorArticulo(Integer articuloCodigo);

    /** Kardex de un lote (del más reciente al más antiguo). */
    List<MovimientoInventario> getMovimientosPorLote(Integer codigoLote);

    /**
     * Obtiene los movimientos registrados dentro de un rango de fechas.
     *
     * @param inicio
     * @param fin
     * @return
     */
    List<MovimientoInventario> getMovimientosPorFechas(LocalDateTime inicio, LocalDateTime fin);
}
