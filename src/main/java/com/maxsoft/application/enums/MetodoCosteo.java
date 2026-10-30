/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.enums;


public enum MetodoCosteo {
    /**
     * Primero en Entrar, Primero en Salir (FIFO).
     * Ideal para reflejar valores de mercado actuales en el balance.
     */
    PEPS("Primero en Entrar, Primero en Salir", "FIFO"),

    /**
     * Promedio Ponderado.
     * Ideal para suavizar fluctuaciones drásticas de precios y simplificar la gestión.
     */
    PROMEDIO_PONDERADO("Promedio Ponderado", "Weighted Average"),

    /**
     * Último en Entrar, Primero en Salir (LIFO).
     * Utilizado para optimización fiscal en entornos inflacionarios (Restringido por NIIF).
     */
    UEPS("Último en Entrar, Primero en Salir", "LIFO");

    private final String descripcionEspanol;
    private final String siglasIngles;

    MetodoCosteo(String descripcionEspanol, String siglasIngles) {
        this.descripcionEspanol = descripcionEspanol;
        this.siglasIngles = siglasIngles;
    }

    public String getDescripcionEspanol() {
        return descripcionEspanol;
    }

    public String getSiglasIngles() {
        return siglasIngles;
    }
}
