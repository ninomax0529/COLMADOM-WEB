package com.maxsoft.application.view.venta.cajaChica;

import com.maxsoft.application.dto.ResumenCajaDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;

/**
 * Cierre y arqueo del turno de caja. El efectivo esperado se calcula en
 * {@link CajaService#obtenerResumenCaja(Integer)} con los movimientos reales del
 * turno (ventas, anulaciones, devoluciones, delivery, pagos a proveedores...).
 */
public class DialogoCierreCaja extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    private final Runnable onCierreExitoso;

    public DialogoCierreCaja(CajaService cajaService, Runnable onCierreExitoso) {
        this(cajaService, "Administrador", onCierreExitoso);
    }

    public DialogoCierreCaja(CajaService cajaService, String usuario, Runnable onCierreExitoso) {

        this.onCierreExitoso = onCierreExitoso;

        setHeaderTitle("Cierre y Arqueo de Caja POS");
        setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Optional<CajaTurno> turnoOpt = cajaService.obtenerCajaAbierta();
        if (turnoOpt.isEmpty()) {
            layout.add(new Span("No hay un turno de caja abierto."));
            add(layout);
            getFooter().add(new Button("Cerrar", e -> close()));
            return;
        }
        CajaTurno turno = turnoOpt.get();
        Integer turnoId = turno.getId().intValue();

        ResumenCajaDTO resumen = cajaService.obtenerResumenCaja(turnoId);
        BigDecimal efectivoEsperado = resumen.getEfectivoEsperado();

        // Resumen informativo
        VerticalLayout resumenLayout = new VerticalLayout();
        resumenLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        resumenLayout.getStyle().set("padding", "0.75rem");
        resumenLayout.getStyle().set("border-radius", "6px");
        resumenLayout.setSpacing(false);

        H4 titleResumen = new H4("Resumen del Turno");
        titleResumen.getStyle().set("margin-top", "0").set("margin-bottom", "0.5rem");

        resumenLayout.add(
                titleResumen,
                crearFilaInfo("Fondo Inicial:", resumen.getFondoInicial()),
                crearFilaInfo("Ventas en Efectivo (netas):", resumen.getVentasEfectivo()),
                crearFilaInfo("Cobros de Delivery:", resumen.getCobrosDelivery()),
                crearFilaInfo("Abonos de Clientes (CxC):", resumen.getCobrosCxc()),
                crearFilaInfo("Pagos a Proveedores:", resumen.getPagosProveedores()),
                crearFilaInfo("Avances de Efectivo con Tarjeta:", resumen.getAvancesEfectivo()),
                crearFilaInfo("Otros Movimientos:", resumen.getOtrosMovimientos()),
                crearFilaInfo("Ventas Tarjeta/POS (Ref.):", resumen.getVentasTarjeta()),
                crearFilaInfoBold("Efectivo Esperado en Gaveta:", efectivoEsperado)
        );

        // Entrada del conteo real de efectivo
        BigDecimalField efectivoRealField = new BigDecimalField("Efectivo Real Contado en Caja (RD$)");
        efectivoRealField.setWidthFull();
        efectivoRealField.setPlaceholder("0.00");
        efectivoRealField.setValueChangeMode(ValueChangeMode.EAGER);

        Span diferenciaSpan = new Span("Diferencia: RD$ 0.00");
        diferenciaSpan.getStyle().set("font-weight", "bold").set("font-size", "1.1rem");

        efectivoRealField.addValueChangeListener(e -> {
            BigDecimal contado = e.getValue();
            if (contado != null) {
                BigDecimal diferencia = contado.subtract(efectivoEsperado);
                if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
                    diferenciaSpan.setText("Diferencia: RD$ 0.00 (Cuadre Perfecto)");
                    diferenciaSpan.getStyle().set("color", "var(--lumo-success-text-color)");
                } else if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
                    diferenciaSpan.setText("Diferencia: +RD$ " + MONEDA_FORMAT.format(diferencia) + " (Sobrante)");
                    diferenciaSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
                } else {
                    diferenciaSpan.setText("Diferencia: -RD$ " + MONEDA_FORMAT.format(diferencia.abs()) + " (Faltante)");
                    diferenciaSpan.getStyle().set("color", "var(--lumo-error-text-color)");
                }
            } else {
                diferenciaSpan.setText("Diferencia: RD$ 0.00");
                diferenciaSpan.getStyle().set("color", "var(--lumo-body-text-color)");
            }
        });

        layout.add(resumenLayout, efectivoRealField, diferenciaSpan);
        add(layout);

        Button cancelBtn = new Button("Cancelar", e -> close());

        Button processBtn = new Button("Cerrar Caja e Imprimir Reporte [Enter]", VaadinIcon.LOCK.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        processBtn.addClickShortcut(Key.ENTER);

        processBtn.addClickListener(e -> {
            BigDecimal contado = efectivoRealField.getValue();
            if (contado == null || contado.compareTo(BigDecimal.ZERO) < 0) {
                Notification.show("Ingresa el monto de efectivo contado", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            try {
                // El servicio recalcula el esperado con los movimientos al momento del cierre
                CajaTurno cerrada = cajaService.cerrarCaja(turnoId, usuario, contado);

                BigDecimal dif = cerrada.getDiferencia() != null ? cerrada.getDiferencia() : BigDecimal.ZERO;
                String detalleDif = dif.signum() == 0 ? "cuadre perfecto"
                        : (dif.signum() > 0 ? "sobrante RD$ " : "faltante RD$ ") + MONEDA_FORMAT.format(dif.abs());

                Notification.show("Caja cerrada exitosamente (" + detalleDif + "). Redirigiendo...",
                        3500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                close();
                if (this.onCierreExitoso != null) {
                    this.onCierreExitoso.run();
                }
                // Navegar automáticamente fuera de la vista del POS hacia la pantalla principal
                UI.getCurrent().navigate("modulo/moduloPrincipal");

            } catch (Exception ex) {
                Notification.show("Error al cerrar la caja: " + ex.getMessage(), 3500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        getFooter().add(cancelBtn, processBtn);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                efectivoRealField.focus();
            }
        });
    }

    private HorizontalLayout crearFilaInfo(String etiqueta, BigDecimal monto) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidthFull();
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span lbl = new Span(etiqueta);
        lbl.getStyle().set("font-size", "0.85rem").set("color", "var(--lumo-secondary-text-color)");

        Span val = new Span(formatear(monto));
        val.getStyle().set("font-size", "0.85rem");
        if (monto != null && monto.signum() < 0) {
            val.getStyle().set("color", "var(--lumo-error-text-color)");
        }

        hl.add(lbl, val);
        return hl;
    }

    private HorizontalLayout crearFilaInfoBold(String etiqueta, BigDecimal monto) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidthFull();
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        hl.getStyle().set("margin-top", "0.25rem");

        Span lbl = new Span(etiqueta);
        lbl.getStyle().set("font-weight", "bold").set("font-size", "0.95rem");

        Span val = new Span(formatear(monto));
        val.getStyle().set("font-weight", "bold").set("font-size", "0.95rem").set("color", "var(--lumo-primary-color)");

        hl.add(lbl, val);
        return hl;
    }

    private static String formatear(BigDecimal monto) {
        BigDecimal m = monto != null ? monto : BigDecimal.ZERO;
        return (m.signum() < 0 ? "-RD$ " : "RD$ ") + MONEDA_FORMAT.format(m.abs());
    }
}
