package com.maxsoft.application.util;

import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Lógica de las líneas de venta del POS (multi-empaque), sin dependencias de UI:
 * factor de conversión, precio por presentación, búsqueda de líneas y armado de
 * una línea nueva. Los importes los calcula {@link CalculoFacturaUtil}.
 */
public final class LineaPosUtil {

    public static final String UNIDAD_POR_DEFECTO = "Unidad";
    public static final String ALMACEN_POR_DEFECTO = "General";

    private LineaPosUtil() {
    }

    /** Factor de conversión del empaque a unidad base; 1 si no hay empaque o el factor es inválido. */
    public static BigDecimal factorConversion(ArticuloEmpaque empaque) {
        if (empaque == null) {
            return BigDecimal.ONE;
        }
        BigDecimal factor = empaque.getFactorConversion();
        return factor == null || factor.signum() <= 0 ? BigDecimal.ONE : factor;
    }

    /**
     * Precio de venta de una presentación: el precio propio del empaque si lo
     * tiene; si no, precio base del artículo × factor.
     */
    public static BigDecimal precioEmpaque(ArticuloEmpaque articulo, ArticuloEmpaque empaque) {
        if (empaque != null && empaque.getPrecioVenta() != null) {
            return empaque.getPrecioVenta();
        }
        ArticuloEmpaque base = empaque != null ? empaque : articulo;
        BigDecimal precioBase = base.getArticulo() != null && base.getArticulo().getPrecioVenta() != null
                ? base.getArticulo().getPrecioVenta()
                : BigDecimal.ZERO;
        return precioBase.multiply(factorConversion(base));
    }

    /** Línea del ticket que corresponde a esa presentación (mismo código de empaque). */
    public static Optional<DetalleFacturaDeVenta> buscarLinea(List<DetalleFacturaDeVenta> lineas,
            ArticuloEmpaque empaque) {
        return lineas.stream()
                .filter(l -> l.getArticuloEmpaque() != null
                && Objects.equals(l.getArticuloEmpaque().getCodigo(), empaque.getCodigo()))
                .findFirst();
    }

    /**
     * Cantidad total del ARTÍCULO en las líneas, en unidad base. Suma todas las
     * presentaciones del mismo artículo (unidad, caja, etc.) porque el stock es
     * único por artículo.
     */
    public static BigDecimal cantidadBase(List<DetalleFacturaDeVenta> lineas, ArticuloEmpaque empaque) {
        Integer codigoArticulo = empaque.getArticulo() != null ? empaque.getArticulo().getCodigo() : null;
        return lineas.stream()
                .filter(l -> l.getArticulo() != null && Objects.equals(l.getArticulo().getCodigo(), codigoArticulo))
                .map(l -> nz(l.getCantidad()).multiply(factorConversion(l.getArticuloEmpaque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Siguiente número de línea dentro del ticket (empieza en 1 en cada venta). */
    public static int siguienteNumeroLinea(List<DetalleFacturaDeVenta> lineas) {
        return lineas.stream().mapToInt(DetalleFacturaDeVenta::getNumeroDeLinea).max().orElse(0) + 1;
    }

    /** Nombre del artículo + presentación (ej. "Arroz Selecto Saco 25 lb"), sin "null". */
    public static String descripcion(ArticuloEmpaque empaque) {
        String nombre = empaque.getNombreArticulo() != null ? empaque.getNombreArticulo().trim() : "";
        String presentacion = empaque.getDescripcionEmpaque();
        if (presentacion == null || presentacion.isBlank()) {
            return nombre;
        }
        return (nombre + " " + presentacion.trim()).trim();
    }

    /**
     * Recalcula los importes de la línea (ITBIS incluido en el precio) y su
     * cantidad en unidad base.
     */
    public static void recalcular(DetalleFacturaDeVenta linea) {
        if (linea == null) {
            return;
        }
        CalculoFacturaUtil.calcularLineaItbisIncluido(linea);
        linea.setCantidadFisicaBase(nz(linea.getCantidad()).multiply(factorConversion(linea.getArticuloEmpaque())));
    }

    /**
     * Arma una línea nueva para la presentación indicada, con precio, ITBIS del
     * artículo y totales ya calculados. No la agrega al ticket.
     *
     * @param empaquePrecio presentación de la que se toma el precio (null = la misma)
     */
    public static DetalleFacturaDeVenta nuevaLinea(ArticuloEmpaque empaque, ArticuloEmpaque empaquePrecio,
            BigDecimal cantidad, int numeroLinea) {
        DetalleFacturaDeVenta d = new DetalleFacturaDeVenta();
        d.setCodigo(empaque.getCodigo());
        d.setArticulo(empaque.getArticulo());
        d.setCantidad(cantidad);
        d.setArticuloEmpaque(empaque);

        BigDecimal factor = factorConversion(empaque);
        d.setFactorConversion(factor);
        d.setCantidadFisicaBase(cantidad.multiply(factor));

        if (empaque.getUnidadEmpaque() != null) {
            d.setUnidad(empaque.getUnidadEmpaque());
            d.setNombreUnidad(empaque.getUnidadEmpaque().getDescripcion());
        } else {
            d.setNombreUnidad(UNIDAD_POR_DEFECTO);
        }

        d.setNumeroDeLinea(numeroLinea);
        d.setDescripcionArticulo(descripcion(empaque));
        if (empaque.getArticulo() != null) {
            d.setExistenciaActual(empaque.getArticulo().getExistencia());
        }
        d.setPrecioVenta(precioEmpaque(empaque, empaquePrecio != null ? empaquePrecio : empaque));
        d.setPorcientoDescuento(BigDecimal.ZERO);
        // Tasa del artículo: 0 si es exento de ITBIS; si no, la suya o el 18 %
        d.setPorcientoItbis(CalculoFacturaUtil.tasaItbis(empaque.getArticulo()));
        d.setNombreAlmacen(ALMACEN_POR_DEFECTO);

        recalcular(d);
        return d;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
