package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.ArticuloClasificacionAbc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Clasificación ABC automática de los artículos por valor (RD$ vendidos) y
 * rotación (facturas). Se recalcula sola cada noche a las 2:00 a.m. y al
 * arrancar la app si el último cálculo tiene más de 24 horas; también se
 * puede pedir con {@link #recalcular()}. El cálculo está en
 * ClasificacionAbcUtil.
 */
public interface ClasificacionAbcService {

    /** Cuántos artículos quedaron en cada clase. */
    record ResumenAbc(int articulos, int claseA, int claseB, int claseC, int nuevos,
            int sinMovimiento, LocalDate desde, LocalDate hasta, LocalDateTime fechaCalculo) {
    }

    /** Recalcula todos los artículos con las ventas del período y lo guarda. */
    ResumenAbc recalcular();

    /**
     * Recalcula solo si nunca se calculó o el último cálculo tiene más de 24
     * horas (p. ej. la PC estuvo apagada a las 2:00 a.m.).
     *
     * @return true si recalculó
     */
    boolean recalcularSiEstaVencida();

    /** Resultado guardado de todos los artículos (el último cálculo). */
    List<ArticuloClasificacionAbc> obtenerTodas();

    /** Resultado guardado de un artículo. */
    Optional<ArticuloClasificacionAbc> obtenerPorArticulo(Integer codigoArticulo);

    /**
     * Copia el mínimo y el máximo sugeridos a los del artículo
     * (articulo.minimo / maximo). Los que no tienen sugerencia (no venden)
     * no se tocan.
     *
     * @return cuántos artículos se actualizaron
     */
    int aplicarSugeridos(java.util.Collection<Integer> codigosArticulo);

    /** Artículo que hay que reponer, con la existencia actual y cuánto pedir. */
    record ArticuloPorReponer(Integer articulo, String descripcion, String clase, BigDecimal existencia,
            BigDecimal diasCobertura, BigDecimal minimoSugerido, BigDecimal maximoSugerido, BigDecimal pedir) {
    }

    /**
     * Artículos por agotarse según ClasificacionAbcUtil.porReponer, con la
     * existencia de este momento; los que menos días tienen, primero.
     *
     * @param soloClaseA true para el aviso: solo los A (los que no pueden faltar)
     */
    List<ArticuloPorReponer> porReponer(boolean soloClaseA);

    /** Fecha y hora del último cálculo; vacío si nunca se calculó. */
    Optional<LocalDateTime> ultimoCalculo();
}
