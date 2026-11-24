package com.maxsoft.application.servicio.interfaces.inventario;

import java.math.BigDecimal;

public interface InventarioService {

    /**
     * Almacén del que vende el POS ("DISPONIBLE PARA LA VENTA" / mostrador).
     * La venta descuenta de aquí y el POS solo muestra y valida su existencia,
     * no la del almacén principal.
     */
    int ALMACEN_VENTA = 2;

    /**
     * Lanza IllegalStateException si el artículo no tiene existencia suficiente
     * (respeta "no inventariable" y "permitir venta sin existencia").
     *
     * @param cantidad cantidad requerida en UNIDAD BASE
     */
    void validarStockDisponible(Integer idArticulo, BigDecimal cantidad);

    /**
     * Existencia del artículo en el almacén de venta, en unidad base (0 si no
     * tiene). Es la misma que usa {@link #validarStockDisponible} al facturar.
     */
    BigDecimal existenciaDisponible(Integer idArticulo);

    /**
     * Existencia (unidad base) en el almacén de venta: código → existencia.
     * Solo trae los artículos registrados en ese almacén: son los que se
     * pueden vender en el POS.
     */
    java.util.Map<Integer, BigDecimal> existenciasPorArticulo();
}
