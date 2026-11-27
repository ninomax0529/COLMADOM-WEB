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

    /** Nombres de unidad que se venden fraccionadas (con decimales). */
    private static final List<String> UNIDADES_FRACCIONABLES
            = List.of("LIBRA", "PESAD", "FRACC", "FRACION");

    /**
     * true si la presentación se puede vender con decimales (2.5): el artículo
     * "se vende en" LIBRA / pesado / fraccionado, es venta a granel, o la
     * presentación misma es de libra. UNIDAD, PAQUETE, CAJA... solo enteros.
     * Sin datos del artículo no se bloquea.
     */
    public static boolean permiteDecimales(ArticuloEmpaque empaque) {
        if (empaque == null || empaque.getArticulo() == null) {
            return true;
        }
        com.maxsoft.application.modelo.Articulo a = empaque.getArticulo();
        if (a.getVentaAgranel()) {
            return true;
        }
        if (a.getUnidadDeVenta() != null && esFraccionable(a.getUnidadDeVenta().getNombre())) {
            return true;
        }
        return empaque.getUnidadEmpaque() != null
                && esFraccionable(empaque.getUnidadEmpaque().getDescripcion());
    }

    /** true si la cantidad tiene parte decimal (2.5 sí; 2 y 2.00 no). */
    public static boolean tieneDecimales(BigDecimal cantidad) {
        return cantidad != null && cantidad.stripTrailingZeros().scale() > 0;
    }

    private static boolean esFraccionable(String nombreUnidad) {
        if (nombreUnidad == null) {
            return false;
        }
        String n = nombreUnidad.trim().toUpperCase();
        return UNIDADES_FRACCIONABLES.stream().anyMatch(n::contains);
    }

    /**
     * Abreviatura de la unidad para espacios cortos: UNIDAD → UND, PAQUETE →
     * PAQ, FARDO/FALDO → FDO, CAJITA → CJTA, LIBRA → LB... Otros nombres
     * de más de 5 letras se cortan a 4. null → UND.
     */
    public static String abreviaturaUnidad(String nombreUnidad) {
        String unidad = nombreUnidad != null && !nombreUnidad.isBlank()
                ? nombreUnidad.trim().toUpperCase()
                : "UNIDAD";
        return switch (unidad) {
            case "UNIDAD", "UNIDADES" -> "UND";
            case "PAQUETE", "PAQUETES" -> "PAQ";
            case "FARDO", "FALDO" -> "FDO";
            case "CAJITA" -> "CJTA";
            case "DOCENA" -> "DOC";
            case "LIBRA", "LIBRAS" -> "LB";
            case "GALON", "GALÓN" -> "GAL";
            default -> unidad.length() > 5 ? unidad.substring(0, 4) : unidad;
        };
    }

    /**
     * Presentación en corto para la tarjeta del POS y la columna "Unidad" del
     * ticket: la abreviatura y, si trae varias unidades, el factor ("PAQ×20").
     */
    public static String presentacionCorta(ArticuloEmpaque empaque) {
        if (empaque == null) {
            return abreviaturaUnidad(null);
        }
        String corto = abreviaturaUnidad(empaque.getUnidadEmpaque() != null
                ? empaque.getUnidadEmpaque().getDescripcion() : null);
        BigDecimal factor = empaque.getFactorConversion();
        if (factor != null && factor.compareTo(BigDecimal.ONE) != 0) {
            return corto + "×" + factor.stripTrailingZeros().toPlainString();
        }
        return corto;
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
     * Presentación cuyo código es EXACTAMENTE {@code texto} (lo que manda el
     * escáner): primero el código de barras (el del fabricante o el asignado),
     * luego el código interno del empaque, que es lo que imprimen las
     * etiquetas de las presentaciones sin código de barras. Sin coincidencia
     * parcial: "71" no encuentra "7100".
     */
    public static Optional<ArticuloEmpaque> buscarPorCodigo(String texto, List<ArticuloEmpaque> catalogo) {
        if (texto == null || texto.isBlank() || catalogo == null) {
            return Optional.empty();
        }
        String codigo = texto.trim();
        Optional<ArticuloEmpaque> porBarra = catalogo.stream()
                .filter(ae -> ae.getCodigoBarra() != null && ae.getCodigoBarra().trim().equalsIgnoreCase(codigo))
                .findFirst();
        if (porBarra.isPresent()) {
            return porBarra;
        }
        return catalogo.stream()
                .filter(ae -> ae.getCodigo() != null && String.valueOf(ae.getCodigo()).equals(codigo))
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
