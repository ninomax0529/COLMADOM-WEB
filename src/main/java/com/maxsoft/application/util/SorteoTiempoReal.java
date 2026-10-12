/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.util;

/**
 *
 * @author Maximiliano
 */
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

public class SorteoTiempoReal {

    private int premiosRestantes;
    private final LocalDateTime horaInicio;
    private final LocalDateTime horaFin;
    private final long segundosTotales;
    private final Random random;

    // Constructor: defines cuántos premios tienes y cuántas horas dura el evento
    public SorteoTiempoReal(int totalPremios, int duracionHoras) {
        this.premiosRestantes = totalPremios;
        this.horaInicio = LocalDateTime.now();
        this.horaFin = this.horaInicio.plusHours(duracionHoras);
        this.segundosTotales = Duration.between(horaInicio, horaFin).getSeconds();
        this.random = new Random();
    }

    // Método síncrono para evitar problemas si llegan muchas personas al mismo milisegundo
    public synchronized String concursar(String nombreParticipante) {
        // 1. Si ya no quedan premios, pierde de inmediato
        if (premiosRestantes <= 0) {
            return "❌ Lo sentimos " + nombreParticipante + ", los premios se han agotado.";
        }

        LocalDateTime ahora = LocalDateTime.now();
        // 2. Si el tiempo del evento ya expiró
        if (ahora.isAfter(horaFin)) {
            return "❌ Lo sentimos " + nombreParticipante + ", el evento ya terminó.";
        }

        // 3. Calcular qué porcentaje del tiempo del evento ha transcurrido
        long segundosTranscurridos = Duration.between(horaInicio, ahora).getSeconds();
        double porcentajeTiempoTranscurrido = (double) segundosTranscurridos / segundosTotales;

        // 4. Calcular cuántos premios DEBERÍAN quedar teóricamente en este instante
        // Ejemplo: Si ha pasado el 20% del tiempo, teóricamente debería quedar el 80% de los premios
        double premiosTeoricosRestantes = premiosRestantes * (1.0 - porcentajeTiempoTranscurrido);

        // 5. Ajustar la probabilidad dinámicamente
        double probabilidadGanar;
        if (premiosRestantes > premiosTeoricosRestantes) {
            // Vamos lento entregando premios, subimos la probabilidad al 15%
            probabilidadGanar = 0.15;
        } else {
            // Vamos muy rápido entregando premios, bajamos la probabilidad al 2% para proteger el stock
            probabilidadGanar = 0.02;
        }

        // 6. Tirada de suerte en tiempo real (Número entre 0.0 y 1.0)
        if (random.nextDouble() < probabilidadGanar) {
            premiosRestantes--; // Restamos el premio de la memoria fija
            return "🎉 ¡FELICIDADES " + nombreParticipante + "! ¡Ganaste un premio en este instante! (Premios restantes: " + premiosRestantes + ")";
        }

        return "❌ Lo sentimos " + nombreParticipante + ", no has ganado esta vez.";
    }

    // --- Simulación de uso ---
    public static void main(String[] args) throws InterruptedException {
        // Configuramos un sorteo con 5 premios que durará 1 hora
        SorteoTiempoReal sorteo = new SorteoTiempoReal(5, 1);
        
        System.out.println("🚀 Sistema de Sorteo Iniciado...");
        String[] participantes = {"Carlos", "Ana", "Luis", "Marta", "Jorge", "Sofia", "Pedro", "Elena"};

        // Simulamos que van llegando uno a uno con pequeñas pausas de tiempo
        for (String persona : participantes) {
            String resultado = sorteo.concursar(persona);
            System.out.println(resultado);
            
            // Pausa simulada de 1 segundo entre la llegada de cada participante
            Thread.sleep(1000); 
        }
    }
}
