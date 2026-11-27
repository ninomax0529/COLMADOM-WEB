package com.maxsoft.application.util;

/**
 * Qué simbología usar para imprimir un código de barras, sin BD ni UI.
 *
 * <p>Los códigos del fabricante (EAN-13, EAN-8, UPC-A) se imprimen en su
 * formato solo si su dígito verificador cuadra; cualquier otro texto ASCII
 * (códigos internos, o un EAN mal digitado) va en Code128, que el escáner
 * lee igual.
 */
public final class CodigoBarraUtil {

    /** Simbología con que se imprime el código. */
    public enum Tipo {
        EAN13("EAN-13"), EAN8("EAN-8"), UPCA("UPC-A"), CODE128("Code128"), INVALIDO("No válido");

        private final String nombre;

        Tipo(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    /** Code128 sin abusar: más largo que esto la etiqueta pequeña no se lee. */
    public static final int MAXIMO_CODE128 = 40;

    private CodigoBarraUtil() {
    }

    /** Tipo con que se imprime {@code codigo} (sin espacios alrededor). */
    public static Tipo tipo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Tipo.INVALIDO;
        }
        String c = codigo.trim();
        if (c.chars().allMatch(Character::isDigit)) {
            if (c.length() == 13 && digitoValido(c)) {
                return Tipo.EAN13;
            }
            if (c.length() == 8 && digitoValido(c)) {
                return Tipo.EAN8;
            }
            if (c.length() == 12 && digitoValido(c)) {
                return Tipo.UPCA;
            }
        }
        if (c.length() > MAXIMO_CODE128 || !c.chars().allMatch(ch -> ch >= 32 && ch < 127)) {
            return Tipo.INVALIDO;
        }
        return Tipo.CODE128;
    }

    /**
     * Dígito verificador GS1 (EAN-8, UPC-A, EAN-13) de los dígitos dados SIN
     * el verificador: desde la derecha se multiplica por 3, 1, 3, 1...
     */
    public static int digitoVerificador(String digitos) {
        int suma = 0;
        boolean tres = true;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int d = digitos.charAt(i) - '0';
            suma += tres ? d * 3 : d;
            tres = !tres;
        }
        return (10 - suma % 10) % 10;
    }

    /** true si el último dígito es el verificador correcto de los anteriores. */
    public static boolean digitoValido(String codigo) {
        int ultimo = codigo.charAt(codigo.length() - 1) - '0';
        return digitoVerificador(codigo.substring(0, codigo.length() - 1)) == ultimo;
    }
}
