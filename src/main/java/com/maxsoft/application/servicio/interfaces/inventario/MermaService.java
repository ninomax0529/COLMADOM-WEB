/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Merma;
import java.util.List;

/**
 * Gestión de mermas (pérdidas de inventario: vencimiento, daño, robo, consumo
 * interno...). Cada merma registrada genera una SALIDA DE INVENTARIO que
 * descuenta las existencias.
 */
public interface MermaService {

    List<Merma> getLista();

    List<DetalleMerma> getDetalle(Integer codigoMerma);

    /**
     * Mermas entre dos fechas (inclusive; null = sin límite), de un tipo y/o
     * almacén (null = todos). Filtra en la BD, las más recientes primero.
     */
    List<Merma> buscar(java.time.LocalDate desde, java.time.LocalDate hasta, Integer tipoMerma, Integer almacen);

    /**
     * Registra la merma y genera la salida de inventario correspondiente, todo
     * en una sola transacción: si la salida falla (por ejemplo, stock
     * insuficiente) la merma tampoco se guarda.
     *
     * @param merma
     * @param detalles
     * @param usuario
     * @return 
     * @throws IllegalArgumentException datos incompletos o inválidos
     * @throws IllegalStateException la salida de inventario no se pudo procesar
     */
    Merma procesarMerma(Merma merma, List<DetalleMerma> detalles, String usuario);

    /**
     * Anula la merma: la marca como anulada (con motivo, fecha y usuario) y
     * genera una entrada de inventario que devuelve exactamente lo que sacó
     * su salida (mismo almacén, lote y costo), en una sola transacción.
     *
     * @throws IllegalArgumentException falta el motivo o la merma no existe
     * @throws IllegalStateException ya está anulada o no tiene salida enlazada
     */
    Merma anular(Integer codigoMerma, String motivo, String usuario);

    /** Comprobante de la merma en PDF (para imprimir y firmar). */
    byte[] comprobantePdf(Integer codigoMerma);

    /**
     * Pérdida del período por tipo de merma, sin las anuladas (almacén null =
     * todos). La de más valor primero.
     */
    List<com.maxsoft.application.dto.ResumenMermaDTO> resumenPorTipo(java.time.LocalDate desde,
            java.time.LocalDate hasta, Integer almacen);

    /**
     * Pérdida del período por artículo, sin las anuladas (almacén null =
     * todos). El de más valor primero.
     */
    List<com.maxsoft.application.dto.ResumenMermaDTO> resumenPorArticulo(java.time.LocalDate desde,
            java.time.LocalDate hasta, Integer almacen);

    /** Ventas netas del período (facturas no anuladas menos devoluciones), base del % de merma. */
    java.math.BigDecimal ventasNetas(java.time.LocalDate desde, java.time.LocalDate hasta);
}
