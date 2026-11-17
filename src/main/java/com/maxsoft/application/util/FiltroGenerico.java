/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.util;

/**
 *
 * @author maximilianoalmonte
 */
import java.util.List;
import java.util.Optional;

public class FiltroGenerico {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FiltroGenerico.class);


    // Verifica si un elemento existe
    public static <T> boolean existeElemento(List<T> lista, T elementoBuscado) {
        return lista.stream()
                .anyMatch(e -> e.equals(elementoBuscado));
    }

    // Devuelve todos los elementos que coinciden
    public static <T> List<T> filtrarElementos(List<T> lista, T elementoBuscado) {
        return lista.stream()
                .filter(e -> e.equals(elementoBuscado))
                .toList(); // Desde Java 16 se puede usar .toList()
    }

    // Devuelve solo el primer elemento encontrado (si existe)
    public static <T> Optional<T> primerElemento(List<T> lista, T elementoBuscado) {
        return lista.stream()
                .filter(e -> e.equals(elementoBuscado))
                .findFirst();
    }

    public static void main(String[] args) {
        List<Integer> numeros = List.of(10, 20, 30, 30, 40, 50);
        List<String> nombres = List.of("Ana", "Luis", "María", "Pedro", "Luis");

        // Validar existencia
        log.debug("¿Existe 30? " + existeElemento(numeros, 30)); // true
        log.debug("¿Existe Juan? " + existeElemento(nombres, "Juan")); // false

        // Filtrar coincidencias
        log.debug("Coincidencias de 30: " + filtrarElementos(numeros, 30)); // [30, 30]
        log.debug("Coincidencias de Luis: " + filtrarElementos(nombres, "Luis")); // [Luis, Luis]

        // Obtener el primer elemento encontrado
        Optional<Integer> primer30 = primerElemento(numeros, 30);
        Optional<String> primerLuis = primerElemento(nombres, "Luis");
        Optional<String> primerJuan = primerElemento(nombres, "Juan");

        log.debug("Primer 30 encontrado: " + primer30.orElse(null)); // 30
        log.debug("Primer Luis encontrado: " + primerLuis.orElse(null)); // Luis
        log.debug("Primer Juan encontrado: " + primerJuan.orElse(null)); // null
    }
}
