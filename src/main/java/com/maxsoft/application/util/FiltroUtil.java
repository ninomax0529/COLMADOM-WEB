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
import java.util.stream.Collectors;

public class FiltroUtil {

    /**
     * Verifica si un elemento existe en la lista
     */
    public static <T> boolean existeElemento(List<T> lista, T elementoBuscado) {
        return lista.stream()
                .anyMatch(e -> e.equals(elementoBuscado));
    }

    /**
     * Devuelve todos los elementos que coinciden con el buscado
     */
    public static <T> List<T> filtrarElementos(List<T> lista, T elementoBuscado) {
        return lista.stream()
                .filter(e -> e.equals(elementoBuscado))
                .collect(Collectors.toList());
    }

    /**
     * Devuelve el primer elemento que coincida, en un Optional
     */
    public static <T> Optional<T> primerElemento(List<T> lista, T elementoBuscado) {
        return lista.stream()
                .filter(e -> e.equals(elementoBuscado))
                .findFirst();
    }

    // Ejemplo de uso
    public static void main(String[] args) {
        List<Integer> numeros = List.of(10, 20, 30, 30, 40, 50);
        List<String> nombres = List.of("Ana", "Luis", "María", "Pedro", "Luis");

        // 1. Verificar existencia
        System.out.println("¿Existe 30? " + existeElemento(numeros, 30));   // true
        System.out.println("¿Existe Juan? " + existeElemento(nombres, "Juan")); // false

        // 2. Obtener todos los elementos coincidentes
        System.out.println("Coincidencias de 30: " + filtrarElementos(numeros, 30)); // [30, 30]
        System.out.println("Coincidencias de Luis: " + filtrarElementos(nombres, "Luis")); // [Luis, Luis]

        // 3. Obtener solo el primer elemento coincidente
        System.out.println("Primer 30 encontrado: " + primerElemento(numeros, 30).orElse(null)); // 30
        System.out.println("Primer Luis encontrado: " + primerElemento(nombres, "Luis").orElse(null)); // Luis
        System.out.println("Primer Juan encontrado: " + primerElemento(nombres, "Juan").orElse(null)); // null
    }
}
