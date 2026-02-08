/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

/**
 *
 * @author maximilianoalmonte
 */


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RelojDigitalComponent extends VerticalLayout {

    private final Span hora = new Span();
    private final Span fecha = new Span();
    private UI ui;

    public RelojDigitalComponent() {

        configurarEstilos();
        add(hora, fecha);

        setSpacing(false);
        setPadding(false);
        setAlignItems(Alignment.END);
    }

    private void configurarEstilos() {

        hora.getStyle()
                .set("font-size", "20px")
                 .set("padding", "0px 0px")
                .set("font-weight", "900")
                .set("color", "#22c55e")
                .set("letter-spacing", "2px");

        fecha.getStyle()
                .set("font-size", "14px")
                  .set("padding", "0px 0px")
                .set("color", "#94a3b8");
    }

    private void actualizarReloj() {

        LocalDateTime now = LocalDateTime.now();

        hora.setText(
                now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );

        fecha.setText(
                now.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
        );
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        ui = attachEvent.getUI();
        ui.setPollInterval(1000);
        ui.addPollListener(event -> actualizarReloj());
        actualizarReloj();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (ui != null) {
            ui.setPollInterval(-1); // desactiva polling
        }
    }
}
