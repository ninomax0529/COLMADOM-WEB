package com.maxsoft.application.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Clasificación ABC de los artículos por valor (RD$ vendidos) y por rotación
 * (en cuántas facturas aparece), con la regla de Pareto. Sin base de datos ni
 * UI: recibe las ventas ya sumadas por artículo y devuelve la clase de cada uno.
 *
 * <p>Por cada criterio se ordenan los artículos de mayor a menor y se acumula
 * el porcentaje. Es A si lo acumulado ANTES de él no llega al límite A (80 %):
 * así el artículo que cruza el 80 % también es A. B si no llega al límite B
 * (95 %). El resto, C. Un artículo sin ventas en ese criterio es C.</p>
 *
 * <p>Clase final: A si es A en valor o en rotación; C si es C en los dos; B en
 * los demás casos. Un artículo nuevo que quedaría en C se marca N (nuevo): no
 * se castiga por no tener historia todavía.</p>
 */
public final class ClasificacionAbcUtil {

    public static final String CLASE_A = "A";
    public static final String CLASE_B = "B";
    public static final String CLASE_C = "C";
    /** Artículo nuevo que todavía no alcanza A ni B. */
    public static final String CLASE_NUEVO = "N";

    public static final BigDecimal LIMITE_A_POR_DEFECTO = new BigDecimal("80");
    public static final BigDecimal LIMITE_B_POR_DEFECTO = new BigDecimal("95");

    private static final BigDecimal CIEN = new BigDecimal("100");

    private ClasificacionAbcUtil() {
    }

    /**
     * Ventas de un artículo en el período.
     *
     * @param ventas   RD$ netos (sin ITBIS, menos devoluciones); null = 0
     * @param facturas en cuántas facturas apareció
     * @param unidades unidades base vendidas (solo informativo); null = 0
     * @param nuevo    el artículo es reciente (ver abc_dias_nuevo)
     */
    public record VentaArticulo(Integer articulo, BigDecimal ventas, long facturas,
            BigDecimal unidades, boolean nuevo) {
    }

    /** Resultado de un artículo; los porcentajes son su parte del total (0-100). */
    public record ResultadoAbc(Integer articulo, String clase, String claseValor,
            String claseRotacion, boolean sinMovimiento, BigDecimal ventas,
            long facturas, BigDecimal unidades, BigDecimal porcientoValor,
            BigDecimal porcientoRotacion) {
    }

    /**
     * Clasifica todos los artículos. Límites inválidos (no cumplen
     * 0 &lt; A &lt; B &le; 100) se cambian por 80 y 95.
     *
     * @param limiteA % acumulado hasta donde llega la clase A (80)
     * @param limiteB % acumulado hasta donde llega la clase B (95)
     */
    public static List<ResultadoAbc> clasificar(List<VentaArticulo> ventas,
            BigDecimal limiteA, BigDecimal limiteB) {

        if (!limitesValidos(limiteA, limiteB)) {
            limiteA = LIMITE_A_POR_DEFECTO;
            limiteB = LIMITE_B_POR_DEFECTO;
        }

        Map<Integer, Tramo> porValor = clasificarCriterio(ventas,
                v -> positivo(v.ventas()), limiteA, limiteB);
        Map<Integer, Tramo> porRotacion = clasificarCriterio(ventas,
                v -> BigDecimal.valueOf(Math.max(0, v.facturas())), limiteA, limiteB);

        List<ResultadoAbc> resultado = new ArrayList<>(ventas.size());
        for (VentaArticulo v : ventas) {
            Tramo valor = porValor.get(v.articulo());
            Tramo rotacion = porRotacion.get(v.articulo());
            boolean sinMovimiento = v.facturas() <= 0 && positivo(v.ventas()).signum() == 0;

            String clase = combinar(valor.clase(), rotacion.clase());
            if (v.nuevo() && CLASE_C.equals(clase)) {
                clase = CLASE_NUEVO;
            }

            resultado.add(new ResultadoAbc(v.articulo(), clase, valor.clase(),
                    rotacion.clase(), sinMovimiento, nz(v.ventas()), Math.max(0, v.facturas()),
                    nz(v.unidades()), valor.porciento(), rotacion.porciento()));
        }
        return resultado;
    }

    /** A si alguno es A; C si los dos son C; B en los demás casos. */
    public static String combinar(String claseValor, String claseRotacion) {
        if (CLASE_A.equals(claseValor) || CLASE_A.equals(claseRotacion)) {
            return CLASE_A;
        }
        if (CLASE_C.equals(claseValor) && CLASE_C.equals(claseRotacion)) {
            return CLASE_C;
        }
        return CLASE_B;
    }

    static boolean limitesValidos(BigDecimal limiteA, BigDecimal limiteB) {
        return limiteA != null && limiteB != null
                && limiteA.signum() > 0
                && limiteA.compareTo(limiteB) < 0
                && limiteB.compareTo(CIEN) <= 0;
    }

    /** Clase y % de participación de un artículo en un criterio. */
    private record Tramo(String clase, BigDecimal porciento) {
    }

    private static Map<Integer, Tramo> clasificarCriterio(List<VentaArticulo> ventas,
            Function<VentaArticulo, BigDecimal> medida, BigDecimal limiteA, BigDecimal limiteB) {

        BigDecimal total = ventas.stream().map(medida).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mayor a menor; a igual medida, por código, para que el resultado no
        // cambie de un cálculo a otro
        List<VentaArticulo> ordenadas = new ArrayList<>(ventas);
        ordenadas.sort(Comparator.comparing(medida).reversed()
                .thenComparing(VentaArticulo::articulo, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<Integer, Tramo> tramos = new HashMap<>();
        BigDecimal acumuladoAntes = BigDecimal.ZERO;

        for (VentaArticulo v : ordenadas) {
            BigDecimal valor = medida.apply(v);

            if (total.signum() == 0 || valor.signum() == 0) {
                tramos.put(v.articulo(), new Tramo(CLASE_C, BigDecimal.ZERO.setScale(4)));
                continue;
            }

            BigDecimal porciento = valor.multiply(CIEN).divide(total, 4, RoundingMode.HALF_UP);
            String clase = acumuladoAntes.compareTo(limiteA) < 0 ? CLASE_A
                    : acumuladoAntes.compareTo(limiteB) < 0 ? CLASE_B
                    : CLASE_C;

            tramos.put(v.articulo(), new Tramo(clase, porciento));
            // Se acumula sin redondear para no correr el límite
            acumuladoAntes = acumuladoAntes.add(valor.multiply(CIEN).divide(total, 10, RoundingMode.HALF_UP));
        }
        return tramos;
    }

    /** Valor no negativo: devoluciones mayores que lo vendido cuentan como 0. */
    private static BigDecimal positivo(BigDecimal v) {
        return v == null || v.signum() < 0 ? BigDecimal.ZERO : v;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
