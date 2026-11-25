package com.maxsoft.application.servicio.interfaces.inventario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Conteo cíclico por almacén: cada día propone unos pocos artículos según su
 * clase ABC (ConteoCiclicoUtil) y, al guardar lo contado, registra las
 * diferencias como ajustes de inventario (INCREMENTO / DISMINUCION).
 */
public interface ConteoCiclicoService {

    /** Artículo propuesto para contar. Cantidades en la unidad base. */
    record ArticuloAContar(Integer articulo, String descripcion, String clase, String unidad, String ubicacion,
            LocalDate ultimoConteo, BigDecimal existencia) {
    }

    /**
     * Cómo va el almacén: cuántos tocan (atrasados incluidos), cuántos se
     * contaron hoy de los que tocan por día, y la exactitud de los últimos 30
     * días (% sin diferencia; null si no se contó nada).
     */
    record ResumenConteo(long tocan, long contadosHoy, int porDia, long contados30, BigDecimal exactitud30) {
    }

    /** Lo que se contó de un artículo (unidad base). */
    record LineaConteo(Integer articulo, BigDecimal contada) {
    }

    /** Resultado de guardar: el conteo y los ajustes generados (null si no hubo). */
    record ResultadoConteo(Integer conteo, int articulos, int conDiferencia, Integer ajusteIncremento,
            Integer ajusteDisminucion) {
    }

    /** Una línea del historial. */
    record LineaHistorial(LocalDate fecha, Integer conteo, String articulo, String clase, BigDecimal sistema,
            BigDecimal contada, BigDecimal diferencia, Integer ajusteIncremento, Integer ajusteDisminucion) {
    }

    /**
     * Artículos a contar hoy en el almacén.
     *
     * @param contarMas false: solo lo que falta de la cuota del día; true: los
     *                  siguientes que tocan aunque ya se cumplió la cuota
     */
    List<ArticuloAContar> paraHoy(Integer almacen, boolean contarMas);

    ResumenConteo resumen(Integer almacen);

    /**
     * Guarda el conteo y, si hay diferencias, los ajustes de inventario. Todo
     * en una transacción: si un ajuste falla no se guarda nada. La existencia
     * del sistema se toma al guardar.
     */
    ResultadoConteo guardar(Integer almacen, List<LineaConteo> lineas, String usuario);

    /** Lo contado en el almacén en los últimos días, lo más reciente primero. */
    List<LineaHistorial> historial(Integer almacen, int dias);
}
