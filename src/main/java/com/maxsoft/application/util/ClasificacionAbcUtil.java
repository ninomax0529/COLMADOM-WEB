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

    /**
     * Días para el mínimo y el máximo sugeridos. Reposición: cada cuánto llega
     * el proveedor. Seguridad: colchón por si se vende más o el proveedor se
     * atrasa. Compra: para cuántos días se compra de una vez. Un artículo
     * nuevo (N) usa los días de la clase B.
     */
    public record PoliticaStock(int diasReposicion, int seguridadA, int seguridadB, int seguridadC,
            int compraA, int compraB, int compraC) {

        public static final PoliticaStock POR_DEFECTO = new PoliticaStock(7, 3, 2, 1, 7, 10, 15);

        int seguridad(String clase) {
            return switch (clase) {
                case CLASE_A -> seguridadA;
                case CLASE_C -> seguridadC;
                default -> seguridadB;
            };
        }

        int compra(String clase) {
            return switch (clase) {
                case CLASE_A -> compraA;
                case CLASE_C -> compraC;
                default -> compraB;
            };
        }
    }

    /**
     * Mínimo sugerido: demanda diaria x (reposición + seguridad de la clase),
     * redondeado hacia arriba a unidades enteras. Sin demanda: null (no se
     * sugiere nada para lo que no se vende).
     */
    public static BigDecimal minimoSugerido(BigDecimal demandaDiaria, String clase, PoliticaStock politica) {
        if (demandaDiaria == null || demandaDiaria.signum() <= 0) {
            return null;
        }
        PoliticaStock p = politica != null ? politica : PoliticaStock.POR_DEFECTO;
        int dias = p.diasReposicion() + p.seguridad(clase);
        return demandaDiaria.multiply(BigDecimal.valueOf(dias)).setScale(0, RoundingMode.CEILING);
    }

    /**
     * Máximo sugerido: el mínimo más lo que se vende en los días de compra de
     * la clase (redondeado hacia arriba). Sin demanda: null.
     */
    public static BigDecimal maximoSugerido(BigDecimal demandaDiaria, String clase, PoliticaStock politica) {
        BigDecimal minimo = minimoSugerido(demandaDiaria, clase, politica);
        if (minimo == null) {
            return null;
        }
        PoliticaStock p = politica != null ? politica : PoliticaStock.POR_DEFECTO;
        return minimo.add(demandaDiaria.multiply(BigDecimal.valueOf(p.compra(clase)))
                .setScale(0, RoundingMode.CEILING));
    }

    /** Con esta cobertura (días) o menos, el artículo está por agotarse. */
    public static final BigDecimal DIAS_COBERTURA_CRITICA = new BigDecimal("3");

    /**
     * Hay que reponer: la existencia está en el mínimo sugerido o por debajo,
     * o le alcanza para {@link #DIAS_COBERTURA_CRITICA} días o menos. Lo que no
     * se vende nunca está por reponer.
     */
    public static boolean porReponer(BigDecimal existencia, BigDecimal demandaDiaria, BigDecimal minimoSugerido) {
        if (demandaDiaria == null || demandaDiaria.signum() <= 0) {
            return false;
        }
        BigDecimal ex = existencia != null ? existencia : BigDecimal.ZERO;
        if (minimoSugerido != null && ex.compareTo(minimoSugerido) <= 0) {
            return true;
        }
        BigDecimal dias = diasCobertura(ex, demandaDiaria);
        return dias != null && dias.compareTo(DIAS_COBERTURA_CRITICA) <= 0;
    }

    /**
     * Cuánto pedir para llegar al máximo (unidades enteras, hacia arriba). Una
     * existencia negativa cuenta como 0. Sin máximo: 0.
     */
    public static BigDecimal cantidadAPedir(BigDecimal existencia, BigDecimal maximoSugerido) {
        if (maximoSugerido == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal ex = existencia != null && existencia.signum() > 0 ? existencia : BigDecimal.ZERO;
        return maximoSugerido.subtract(ex).max(BigDecimal.ZERO).setScale(0, RoundingMode.CEILING);
    }

    /**
     * Unidades que vende por día: unidades / días con datos (4 decimales).
     * Sin días o sin unidades: 0.
     */
    public static BigDecimal demandaDiaria(BigDecimal unidades, int diasConDatos) {
        if (diasConDatos <= 0 || unidades == null || unidades.signum() <= 0) {
            return BigDecimal.ZERO.setScale(4);
        }
        return unidades.divide(BigDecimal.valueOf(diasConDatos), 4, RoundingMode.HALF_UP);
    }

    /**
     * Días que le alcanza la existencia al ritmo de venta (1 decimal). Sin
     * demanda no se puede saber: null. Existencia en cero o negativa: 0.
     */
    public static BigDecimal diasCobertura(BigDecimal existencia, BigDecimal demandaDiaria) {
        if (demandaDiaria == null || demandaDiaria.signum() <= 0) {
            return null;
        }
        if (existencia == null || existencia.signum() <= 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return existencia.divide(demandaDiaria, 1, RoundingMode.HALF_UP);
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
