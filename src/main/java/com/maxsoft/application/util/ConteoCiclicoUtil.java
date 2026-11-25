package com.maxsoft.application.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Conteo cíclico: qué artículos toca contar hoy según su clase ABC. Sin BD ni
 * UI (con pruebas en ConteoCiclicoUtilTest).
 *
 * <p>Un artículo toca si nunca se contó en ese almacén o si pasaron los días
 * de su clase desde el último conteo. Primero van los que nunca se contaron,
 * después los más atrasados (días / frecuencia) y, a igualdad, A antes que B,
 * N y C.
 */
public final class ConteoCiclicoUtil {

    private ConteoCiclicoUtil() {
    }

    /** Cada cuántos días se cuenta cada clase. Un artículo nuevo (N) usa los días de B. */
    public record Frecuencia(int diasA, int diasB, int diasC) {

        public static final Frecuencia POR_DEFECTO = new Frecuencia(7, 30, 90);

        public int dias(String clase) {
            if (ClasificacionAbcUtil.CLASE_A.equals(clase)) {
                return diasA;
            }
            if (ClasificacionAbcUtil.CLASE_C.equals(clase)) {
                return diasC;
            }
            return diasB;
        }
    }

    /** Artículo que se puede contar: su clase y cuándo se contó por última vez (null = nunca). */
    public record Candidato(Integer articulo, String clase, LocalDate ultimoConteo) {
    }

    /** Días desde el último conteo; null si nunca se contó. */
    public static Long diasDesdeConteo(LocalDate ultimoConteo, LocalDate hoy) {
        return ultimoConteo == null ? null : ChronoUnit.DAYS.between(ultimoConteo, hoy);
    }

    /** Nunca se contó o ya pasaron los días de su clase. */
    public static boolean tocaContar(Candidato c, LocalDate hoy, Frecuencia frecuencia) {
        Long dias = diasDesdeConteo(c.ultimoConteo(), hoy);
        return dias == null || dias >= frecuencia(frecuencia).dias(c.clase());
    }

    /**
     * Los que tocan, en orden de prioridad, hasta {@code cantidad}.
     */
    public static <T extends Candidato> List<T> paraContar(List<T> candidatos, LocalDate hoy,
            Frecuencia frecuencia, int cantidad) {
        if (candidatos == null || cantidad <= 0) {
            return List.of();
        }
        Frecuencia f = frecuencia(frecuencia);
        return candidatos.stream()
                .filter(c -> tocaContar(c, hoy, f))
                .sorted(orden(hoy, f))
                .limit(cantidad)
                .toList();
    }

    /** Cuántos tocan en total (para el aviso "N atrasados"). */
    public static long cuantosTocan(List<? extends Candidato> candidatos, LocalDate hoy, Frecuencia frecuencia) {
        if (candidatos == null) {
            return 0;
        }
        Frecuencia f = frecuencia(frecuencia);
        return candidatos.stream().filter(c -> tocaContar(c, hoy, f)).count();
    }

    /** Porciento de artículos contados sin diferencia (1 decimal); null si no se contó nada. */
    public static BigDecimal exactitud(long contados, long sinDiferencia) {
        if (contados <= 0) {
            return null;
        }
        return BigDecimal.valueOf(sinDiferencia * 100L)
                .divide(BigDecimal.valueOf(contados), 1, java.math.RoundingMode.HALF_UP);
    }

    private static <T extends Candidato> Comparator<T> orden(LocalDate hoy, Frecuencia f) {
        return (x, y) -> {
            boolean nuncaX = x.ultimoConteo() == null;
            boolean nuncaY = y.ultimoConteo() == null;
            if (nuncaX != nuncaY) {
                return nuncaX ? -1 : 1;
            }
            if (!nuncaX) {
                // Más atrasado primero: diasX/frecX contra diasY/frecY sin dividir
                long atrasoX = diasDesdeConteo(x.ultimoConteo(), hoy) * f.dias(y.clase());
                long atrasoY = diasDesdeConteo(y.ultimoConteo(), hoy) * f.dias(x.clase());
                if (atrasoX != atrasoY) {
                    return atrasoX > atrasoY ? -1 : 1;
                }
            }
            int porClase = Integer.compare(prioridad(x.clase()), prioridad(y.clase()));
            if (porClase != 0) {
                return porClase;
            }
            return Comparator.<Integer>nullsLast(Comparator.naturalOrder()).compare(x.articulo(), y.articulo());
        };
    }

    /** A, B, N, C. */
    static int prioridad(String clase) {
        if (ClasificacionAbcUtil.CLASE_A.equals(clase)) {
            return 0;
        }
        if (ClasificacionAbcUtil.CLASE_B.equals(clase)) {
            return 1;
        }
        if (ClasificacionAbcUtil.CLASE_NUEVO.equals(clase)) {
            return 2;
        }
        return 3;
    }

    private static Frecuencia frecuencia(Frecuencia f) {
        return f != null ? f : Frecuencia.POR_DEFECTO;
    }
}
