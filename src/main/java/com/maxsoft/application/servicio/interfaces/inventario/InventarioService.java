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
}
