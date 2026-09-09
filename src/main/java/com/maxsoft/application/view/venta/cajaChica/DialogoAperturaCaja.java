package com.maxsoft.application.view.venta.cajaChica;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.servicio.interfaces.CajaService;
import com.vaadin.flow.component.Key;
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

        setHeaderTitle("Apertura de Caja");
        setWidth("420px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        H4 sub = new H4("Configurar Fondo Inicial");
        sub.getStyle().set("margin-top", "0").set("margin-bottom", "0.2rem");

        Paragraph desc = new Paragraph("Ingresa el monto de efectivo base en la caja para iniciar la jornada.");
        desc.getStyle().set("font-size", "0.85rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "0");

        BigDecimalField montoInicialField = new BigDecimalField("Monto Base / Fondo de Caja (RD$)");
        montoInicialField.setWidthFull();
        montoInicialField.setValue(new BigDecimal("1500.00"));
        montoInicialField.setPlaceholder("0.00");

        layout.add(sub, desc, montoInicialField);
        add(layout);

        Button cancelBtn = new Button("Cancelar", e -> {
            getUI().ifPresent(ui -> ui.navigate("modulo/moduloPrincipal"));
            close();
        });

        Button processBtn = new Button("Abrir Caja [Enter]", VaadinIcon.KEY.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
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

            BigDecimal fondo = montoInicialField.getValue();
            if (fondo != null && fondo.compareTo(BigDecimal.ZERO) >= 0) {

                CajaTurno cajaTurno = new CajaTurno();
                cajaTurno.setFechaApertura(LocalDateTime.now());
                cajaTurno.setCajeroApertura("Admin");
                cajaTurno.setMontoApertura(fondo);
                cajaTurno.setEstado("Abierta");
                cajaTurno.setObservaciones("Apertura con monto mayor");

                this.cajaService.guardar(cajaTurno);

                Notification.show("Caja abierta correctamente con RD$ " + fondo, 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                close();
                ejecutarTransicionYCallback(fondo);
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

    /**
     * Aplica la animación CSS sobre la pantalla del POS (Vista Padred)
     * y luego dispara la devolución de llamada principal.
     */
    private void ejecutarTransicionYCallback(BigDecimal fondo) {
        getUI().ifPresent(ui -> ui.access(() -> {
            ui.getCurrent().getChildren().findFirst().ifPresent(componenteRaiz -> {
                com.vaadin.flow.component.HasStyle target = (com.vaadin.flow.component.HasStyle) componenteRaiz;
                target.getStyle().set("opacity", "0");
                target.getStyle().set("transform", "scale(0.96)");
                target.getStyle().set("transition", "opacity 0.8s cubic-bezier(0.25, 1, 0.5, 1), transform 0.8s cubic-bezier(0.25, 1, 0.5, 1)");

                ui.getPage().executeJs("setTimeout(() => { $0.style.opacity = '1'; $0.style.transform = 'scale(1)'; }, 80);", componenteRaiz.getElement());
            });

            if (this.onAperturaExitosa != null) {
                this.onAperturaExitosa.accept(fondo);
            }
        }));
    }
}