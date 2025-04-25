/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta;

/**
 *
 * @author maximilianoalmonte
 */
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Menu;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Route("dashBorard")
@Menu(order = 6, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)

public class DashBoarrdView extends VerticalLayout {

    private final H1 clockLabel = new H1("Hora actual: --:--:--");
    private final H1 statusLabel = new H1("Estado: Detenido");

    private final Span cajasLocal = new Span("Cemento Local: 0");
    private final Span cajasExport = new Span("Cemento Exportación: 0");
    private final Span cajasEstructural = new Span("Cemento Estructural: 0");

    private final Span tiempoOperativo = new Span("Tiempo operativo: 00:00:00");
    private final Span horaInicioLabel = new Span("Hora de inicio: --:--:--");
    private final Span horaDetencionLabel = new Span("Hora de detención: --:--:--");

    private final Button iniciarButton = new Button("Iniciar");
    private final Button detenerButton = new Button("Detener");

    private UI ui;
    private boolean enMarcha = false;

    private int contadorLocal = 0;
    private int contadorExport = 0;
    private int contadorEstructural = 0;

    private LocalDateTime inicioOperacion;
    private LocalDateTime horaDetencion;

    private LocalDateTime lastRefreshTime;
    private final Span refreshWarning = new Span();  // Mensaje de advertencia

    private final DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @PostConstruct
    public void init() {
        ui = UI.getCurrent();
lastRefreshTime = LocalDateTime.now(); // al iniciar la vista
verificarTiempoDeRefresco();           // chequeo inicial

        HorizontalLayout botones = new HorizontalLayout(iniciarButton, detenerButton);
        iniciarButton.addClickListener(e -> iniciarProceso());
        detenerButton.addClickListener(e -> detenerProceso());

        VerticalLayout productos = new VerticalLayout(
                new H1("📦 Producción por tipo de cemento"),
                cajasLocal,
                cajasExport,
                cajasEstructural
        );

        add(
                new H1("🏭 Cuadro de mando - Empaquetadora"),
                clockLabel,
                statusLabel,
                productos,
                tiempoOperativo,
                horaInicioLabel,
                horaDetencionLabel,
                botones
        );

        iniciarReloj();
    }

    private void iniciarProceso() {
        if (!enMarcha) {
            enMarcha = true;
            statusLabel.setText("Estado: 🟢 En marcha");
            inicioOperacion = LocalDateTime.now();
            horaInicioLabel.setText("Hora de inicio: " + inicioOperacion.format(horaFormatter));
            iniciarSimulacionProductos();
        }
    }

    private void detenerProceso() {
        if (enMarcha) {
            enMarcha = false;
            statusLabel.setText("Estado: 🔴 Detenido");
            horaDetencion = LocalDateTime.now();
            horaDetencionLabel.setText("Hora de detención: " + horaDetencion.format(horaFormatter));
        }
    }

    private void iniciarReloj() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    String hora = LocalTime.now().format(horaFormatter);

                    ui.access(() -> {
                        clockLabel.setText("Hora actual: " + hora);
                        if (enMarcha && inicioOperacion != null) {
                            Duration dur = Duration.between(inicioOperacion, LocalDateTime.now());
                            tiempoOperativo.setText("Tiempo operativo: " + formatoDuracion(dur));
                        }
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    private void iniciarSimulacionProductos() {
        new Thread(() -> {
            Random rand = new Random();
            while (enMarcha) {
                try {
                    Thread.sleep(1000); // cada 3 segundos se produce algo
                    int tipo = rand.nextInt(3);

                    ui.access(() -> {
                        switch (tipo) {
                            case 0 -> {
                                contadorLocal++;
                                cajasLocal.setText("Cemento Local: " + contadorLocal);
                            }
                            case 1 -> {
                                contadorExport++;
                                cajasExport.setText("Cemento Exportación: " + contadorExport);
                            }
                            case 2 -> {
                                contadorEstructural++;
                                cajasEstructural.setText("Cemento Estructural: " + contadorEstructural);
                            }
                        }
                    });

                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    private String formatoDuracion(Duration duracion) {
        long horas = duracion.toHours();
        long minutos = duracion.toMinutesPart();
        long segundos = duracion.toSecondsPart();
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }
    
    private void verificarTiempoDeRefresco() {
    LocalDateTime now = LocalDateTime.now();
    if (lastRefreshTime != null) {
        Duration dur = Duration.between(lastRefreshTime, now);
        long segundos = dur.getSeconds();

        if (segundos < 10) {
            refreshWarning.setText("⚠️ La vista se actualizó antes de 10 segundos. Causa: refresco manual o forzado.");
        } else {
            refreshWarning.setText(""); // limpia el mensaje si está todo bien
        }
    }
    lastRefreshTime = now;
}

}
