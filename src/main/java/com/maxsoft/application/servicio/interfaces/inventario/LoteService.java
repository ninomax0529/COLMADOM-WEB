package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.modelo.Proveedor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Control de lotes. Todas las cantidades van en UNIDAD BASE. Solo aplica a
 * artículos con {@code controlaLote = true}; para los demás el inventario
 * funciona como siempre (sin lote).
 *
 * Regla: para un artículo con lote, articulo_almacen.existencia = suma de sus
 * lotes en ese almacén. Las existencias de los lotes solo las cambia
 * {@link MovimientoInventarioService} (y {@link #sincronizarLotes}).
 */
public interface LoteService {

    /** Número del lote donde va lo vendido sin existencia (queda en negativo). */
    String NUMERO_SIN_LOTE = "SIN-LOTE";

    /**
     * Parte de una salida que se toma de un lote.
     *
     * @param vencido el lote estaba vencido al momento de la salida
     */
    record Tramo(Lote lote, BigDecimal cantidad, boolean vencido) {
    }

    /**
     * Devuelve el lote con ese número para el artículo, o lo crea. Si el
     * número viene vacío se usa el lote automático del día (L + AAAAMMDD).
     * Si no se indica vencimiento y el artículo tiene días de vida útil, se
     * calcula hoy + días.
     *
     * @throws IllegalArgumentException el lote ya existe con otro vencimiento
     */
    Lote obtenerOCrearLote(Articulo articulo, String numeroLote, LocalDate fechaVencimiento,
            BigDecimal costoUnitario, Proveedor proveedor, String documentoOrigen, String usuario);

    /**
     * Reparte una salida entre los lotes del almacén.
     * <ul>
     * <li>Con lote indicado: sale todo de ese lote; si no alcanza, error.</li>
     * <li>Sin lote: FEFO (primero vence, primero sale), primero los lotes
     * vigentes y después los vencidos. Si los lotes no alcanzan (venta sin
     * existencia), el resto va al lote SIN-LOTE, que queda en negativo.</li>
     * </ul>
     * Bloquea las filas de lote_almacen hasta que termine la transacción. No
     * mueve existencias: solo dice de dónde sale.
     *
     * @throws IllegalStateException el lote indicado no tiene suficiente
     */
    List<Tramo> repartirSalida(Articulo articulo, Almacen almacen, BigDecimal cantidad, Lote loteIndicado);

    /**
     * Suma (delta positivo) o resta (negativo) existencia al lote en el
     * almacén, creando la fila si no existe.
     *
     * @return {existencia anterior, existencia nueva} del lote en el almacén
     */
    BigDecimal[] moverExistencia(Lote lote, Almacen almacen, BigDecimal delta);

    /**
     * Cuadra los lotes del artículo con su existencia por almacén: la
     * diferencia va al lote INICIAL. Se usa al activar el control de lotes
     * (todo lo que había queda en INICIAL) y para reparar descuadres.
     *
     * @return cantidad de almacenes que se ajustaron
     */
    int sincronizarLotes(Articulo articulo, String usuario);

    /**
     * Igual que {@link #sincronizarLotes} pero para un solo almacén: si la
     * suma de los lotes no es igual a la existencia indicada, la diferencia va
     * al lote INICIAL. Lo usa cada movimiento antes de tocar los lotes, para
     * que un descuadre (p. ej. control de lotes activado por fuera de la
     * ficha) se corrija solo.
     *
     * @return true si hubo que ajustar
     */
    boolean cuadrarAlmacen(Articulo articulo, Almacen almacen, BigDecimal existenciaAlmacen, String usuario);

    // ============================================================
    // VENTAS: vencidos, consumo y reposición
    // ============================================================
    /**
     * Si vender {@code cantidadBase} del artículo en el almacén obliga a tomar
     * de lotes vencidos (porque lo vigente no alcanza), devuelve la
     * descripción para el cajero; si no, null. No bloquea filas.
     */
    String describirVentaDeVencidos(Articulo articulo, Almacen almacen, BigDecimal cantidadBase);

    /**
     * Guarda de qué lotes salió una línea de venta, a partir de las filas de
     * kardex que generó su salida.
     */
    void registrarConsumoVenta(com.maxsoft.application.modelo.DetalleFacturaDeVenta detalle,
            List<com.maxsoft.application.modelo.MovimientoInventario> movimientos, String autorizadoPor);

    /**
     * Reparte una devolución o anulación de una línea de venta entre los lotes
     * de los que salió (primero el que vence más tarde) y marca lo devuelto.
     * Si la línea no tiene lotes registrados (venta vieja o artículo sin
     * lote), o lo registrado no alcanza, el resto sale como un tramo con lote
     * null.
     */
    List<Tramo> repartirReposicionVenta(com.maxsoft.application.modelo.DetalleFacturaDeVenta detalle,
            BigDecimal cantidadBase);

    /** Lotes con existencia del artículo en el almacén, en orden FEFO (para elegir lote en pantalla). */
    List<LoteAlmacen> getLotesDisponibles(Integer codigoArticulo, Integer codigoAlmacen);

    /** Lotes del artículo con su existencia en cada almacén (los que vencen primero arriba). */
    List<LoteAlmacen> getExistenciasPorArticulo(Integer codigoArticulo);

    /** Todos los lotes con existencia distinta de cero (consulta de vencimientos). */
    List<LoteAlmacen> getLotesConExistencia();

    /** Artículo/almacén con existencia distinta a la suma de sus lotes: {articulo, almacen, existencia, suma_lotes}. */
    List<Object[]> getDescuadres();
}
