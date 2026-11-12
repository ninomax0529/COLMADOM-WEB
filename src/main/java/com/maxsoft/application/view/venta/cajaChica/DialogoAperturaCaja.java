package com.maxsoft.application.view.venta.cajaChica;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

public class DialogoAperturaCaja extends Dialog {

    private final CajaService cajaService;
    private final Consumer<BigDecimal> onAperturaExitosa;

    public DialogoAperturaCaja(CajaService cajaService, Consumer<BigDecimal> onAperturaExitosa) {
        this.cajaService = cajaService;
        this.onAperturaExitosa = onAperturaExitosa;

        addClassName("dc-dialogo");
        setHeaderTitle("Apertura de Caja");
        setWidth("480px");
        setMaxWidth("96vw");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        com.vaadin.flow.component.html.Div banda = DialogoCajaEstilo.banda(VaadinIcon.KEY, "Fondo inicial",
                "Efectivo con el que arranca la gaveta hoy, "
                + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        BigDecimalField montoInicialField = new BigDecimalField("Monto base / fondo de caja");
        montoInicialField.addClassName("dc-campo-grande");
        montoInicialField.setWidthFull();
        montoInicialField.setValue(new BigDecimal("1500.00"));
        montoInicialField.setPlaceholder("0.00");
        montoInicialField.setPrefixComponent(new com.vaadin.flow.component.html.Span("RD$"));

        // Montos rápidos: un clic pone el fondo más usado
        com.vaadin.flow.component.html.Div rapidos = new com.vaadin.flow.component.html.Div();
        rapidos.addClassName("dc-rapidos");
        for (int valor : new int[]{500, 1000, 1500, 2000, 3000, 5000}) {
            Button b = new Button("RD$ " + String.format("%,d", valor), e -> {
                montoInicialField.setValue(new BigDecimal(valor + ".00"));
                montoInicialField.focus();
            });
            b.addClassName("dc-rapido");
            rapidos.add(b);
        }

        com.vaadin.flow.component.html.Div cuerpo = new com.vaadin.flow.component.html.Div(banda, montoInicialField, rapidos);
        cuerpo.addClassName("dc-cuerpo");
        add(cuerpo);

        Button cancelBtn = new Button("Cancelar", e -> {
            getUI().ifPresent(ui -> ui.navigate("modulo/moduloPrincipal"));
            close();
        });
        cancelBtn.addClassName("dc-btn-secundario");

        Button processBtn = new Button("Abrir Caja [Enter]", VaadinIcon.KEY.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        processBtn.addClassName("dc-btn-abrir");
        processBtn.addClickShortcut(Key.ENTER);

        processBtn.addClickListener(e -> {
            // Verificar si ya existe una caja abierta antes de intentar procesar una nueva
            Optional<CajaTurno> cajaActiva = this.cajaService.obtenerCajaAbierta();
            if (cajaActiva.isPresent()) {
                Notification.show("Ya existe un turno de caja abierto", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                close();
                ejecutarTransicionYCallback(cajaActiva.get().getMontoApertura());
                return;
            }

            if (confirmando) {
                return; // ya hay una confirmación abierta (p. ej. Enter repetido)
            }
            BigDecimal fondo = montoInicialField.getValue();
            if (fondo != null && fondo.compareTo(BigDecimal.ZERO) >= 0) {
                confirmarApertura(fondo);
            } else {
                Notification.show("Ingresa un monto válido para abrir caja", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });

        getFooter().add(cancelBtn, processBtn);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                // Validación al abrir el diálogo: si la caja ya está abierta, omite la apertura
                Optional<CajaTurno> cajaActiva = this.cajaService.obtenerCajaAbierta();
                if (cajaActiva.isPresent()) {
                    Notification.show("Caja detectada previamente abierta", 2500, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                    close();
                    ejecutarTransicionYCallback(cajaActiva.get().getMontoApertura());
                } else {
                    montoInicialField.focus();
                }
            }
        });
    }

    /** true mientras se muestra la pregunta "¿Está seguro?". */
    private boolean confirmando;

    /** Pregunta antes de abrir, mostrando el fondo con que arranca la gaveta. */
    private void confirmarApertura(BigDecimal fondo) {
        confirmando = true;

        com.vaadin.flow.component.html.Span lbl = new com.vaadin.flow.component.html.Span("Fondo inicial");
        com.vaadin.flow.component.html.Span val = new com.vaadin.flow.component.html.Span(
                "RD$ " + String.format("%,.2f", fondo));
        val.addClassName("dc-monto");
        com.vaadin.flow.component.html.Div linea = new com.vaadin.flow.component.html.Div(lbl, val);
        linea.addClassNames("dc-confirma-linea", "dc-dif-cuadre");
        com.vaadin.flow.component.html.Div resumen = new com.vaadin.flow.component.html.Div(linea);
        resumen.addClassName("dc-confirma-resumen");
        com.vaadin.flow.component.html.Span aviso = new com.vaadin.flow.component.html.Span(
                "Cuente el efectivo de la gaveta: este monto será la base para el cuadre al cerrar.");
        aviso.addClassName("dc-confirma-aviso");
        com.vaadin.flow.component.html.Div cuerpo = new com.vaadin.flow.component.html.Div(resumen, aviso);
        cuerpo.addClassName("dc-confirma");

        com.vaadin.flow.component.confirmdialog.ConfirmDialog cd = new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        cd.addClassName("dc-dialogo");
        cd.setHeader("¿Está seguro de abrir la caja?");
        cd.setText(cuerpo);
        cd.setCancelable(true);
        cd.setCancelText("No, volver");
        cd.setConfirmText("Sí, abrir la caja");
        cd.setConfirmButtonTheme("success primary");
        cd.addConfirmListener(ev -> {
            confirmando = false;
            abrir(fondo);
        });
        cd.addCancelListener(ev -> confirmando = false);
        cd.addRejectListener(ev -> confirmando = false);
        cd.open();
    }

    private void abrir(BigDecimal fondo) {
        // Mientras se confirmaba, otra pantalla pudo abrir la caja
        Optional<CajaTurno> cajaActiva = this.cajaService.obtenerCajaAbierta();
        if (cajaActiva.isPresent()) {
            Notification.show("Ya existe un turno de caja abierto", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            close();
            ejecutarTransicionYCallback(cajaActiva.get().getMontoApertura());
            return;
        }

        CajaTurno cajaTurno = new CajaTurno();
        cajaTurno.setFechaApertura(LocalDateTime.now());
        cajaTurno.setCajeroApertura("Admin");
        cajaTurno.setMontoApertura(fondo);
        cajaTurno.setEstado("Abierta");
        cajaTurno.setObservaciones("Apertura con monto mayor");

        this.cajaService.guardar(cajaTurno);

        Notification.show("Caja abierta correctamente con RD$ " + String.format("%,.2f", fondo),
                3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        close();
        ejecutarTransicionYCallback(fondo);
    }

    /**
     * Aplica la animación CSS sobre la pantalla del POS (Vista Padred)
     * y luego dispara la devolución de llamada principal.
     */
    private void ejecutarTransicionYCallback(BigDecimal fondo) {
        // Se llama justo después de close(): el diálogo ya se separó de la UI y
        // getUI() viene vacío, por lo que el POS nunca se enteraba de la apertura
        // (había que refrescar). Se usa la UI de la petición actual.
        UI ui = getUI().orElse(UI.getCurrent());

        // 1. Avisar al POS primero (muestra la pantalla de venta): nada de lo
        //    visual de abajo debe poder impedirlo.
        if (this.onAperturaExitosa != null) {
            this.onAperturaExitosa.accept(fondo);
        }
        if (ui == null) {
            return;
        }

        // 2. Animación de entrada (opcional)
        try {
            ui.getChildren().findFirst().ifPresent(componenteRaiz -> {
                if (componenteRaiz instanceof com.vaadin.flow.component.HasStyle target) {
                    target.getStyle().set("opacity", "0");
                    target.getStyle().set("transform", "scale(0.96)");
                    target.getStyle().set("transition", "opacity 0.8s cubic-bezier(0.25, 1, 0.5, 1), transform 0.8s cubic-bezier(0.25, 1, 0.5, 1)");
                    ui.getPage().executeJs("setTimeout(() => { $0.style.opacity = '1'; $0.style.transform = 'scale(1)'; }, 80);",
                            componenteRaiz.getElement());
                }
            });
        } catch (Exception ex) {
            // La animación es solo estética
        }
    }
}