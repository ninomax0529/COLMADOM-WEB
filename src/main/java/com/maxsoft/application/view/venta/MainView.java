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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Route("actualizarPush")
//@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET_XHR) // Puedes usar solo @Push si quieres default
@Menu(order = 5, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class MainView extends VerticalLayout {

    private H1 counterLabel;
    private H1 clockLabel;
    private int counter = 0;
    private UI ui;

    public MainView() {
        counterLabel = new H1("Contador: 0");
        clockLabel=new H1("Hora actual: --:--:--");
        add(clockLabel, new Div(new Button("Este botón no hace nada")));

        // Guardamos la UI actual para usarla en un hilo
        ui = UI.getCurrent();
    }

//    @PostConstruct
    private void iniciarContador() {
        // Ejecutamos un hilo que actualiza el contador cada segundo
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // Espera de 1 segundo

                    int currentCount = ++counter;
                    // Actualizamos la UI de forma segura
                    ui.access(() -> {
                        counterLabel.setText("Contador: " + currentCount);
                    });

                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    @PostConstruct
    private void iniciarReloj() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // 1 segundo
                    String hora = LocalTime.now().format(formatter);

                    ui.access(() -> {
                        clockLabel.setText("Hora actual: " + hora);
                    });

                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

}

