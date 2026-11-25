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

    /**
     * Existencia del artículo en el mostrador (almacén de venta) frente a su
     * mínimo ahí y a lo que hay en los demás almacenes, en unidad base.
     *
     * @param dondeHay texto con los otros almacenes ("ALMACEN PRINCIPAL: 3");
     * solo lo llena {@link #estadoMostrador(Integer)}, para los mensajes
     */
    record EstadoMostrador(BigDecimal existencia, BigDecimal minimo, BigDecimal enOtrosAlmacenes, String dondeHay) {

        /** Llegó al mínimo (o se agotó) y hay de dónde reponer. */
        public boolean tocaTrasladar() {
            return com.maxsoft.application.util.TrasladoMostradorUtil.tocaTrasladar(existencia, minimo, enOtrosAlmacenes);
        }
    }

    /** Estado del mostrador de un artículo, con el detalle de los otros almacenes. */
    EstadoMostrador estadoMostrador(Integer idArticulo);

    /** Estado del mostrador de todos los artículos (sin el detalle de almacenes). */
    java.util.Map<Integer, EstadoMostrador> estadosMostrador();
}
