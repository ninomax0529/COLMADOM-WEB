package com.maxsoft.application.servicio.interfaces.inventario;

import java.math.BigDecimal;

public interface InventarioService {

    /**
     * Lanza IllegalStateException si el artículo no tiene existencia suficiente
     * (respeta "no inventariable" y "permitir venta sin existencia").
     *
     * @param cantidad cantidad requerida en UNIDAD BASE
     */
    void validarStockDisponible(Integer idArticulo, BigDecimal cantidad);

    /**
     * Existencia actual del artículo en unidad base (0 si no tiene). Es la
     * misma que usa {@link #validarStockDisponible} al facturar.
     */
    BigDecimal existenciaDisponible(Integer idArticulo);

    /** Existencia (unidad base) de todos los artículos: código → existencia. */
    java.util.Map<Integer, BigDecimal> existenciasPorArticulo();
}
