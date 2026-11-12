package com.maxsoft.application.view.venta.cajaChica;

import com.maxsoft.application.dto.ResumenCajaDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Cierre y arqueo del turno de caja. El efectivo esperado se calcula en
 * {@link CajaService#obtenerResumenCaja(Integer)} con los movimientos reales del
 * turno (ventas, anulaciones, devoluciones, delivery, pagos a proveedores...).
 * Estilo visual en themes/gestion-colmadom/views/dialogo-caja.css (clases dc-*).
 */
public class DialogoCierreCaja extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    private final Runnable onCierreExitoso;

    public DialogoCierreCaja(CajaService cajaService, Runnable onCierreExitoso) {
        this(cajaService, "Administrador", onCierreExitoso);
    }

    public DialogoCierreCaja(CajaService cajaService, String usuario, Runnable onCierreExitoso) {

        this.onCierreExitoso = onCierreExitoso;

        addClassName("dc-dialogo");
        setHeaderTitle("Cierre y Arqueo de Caja");
        setWidth("680px");
        setMaxWidth("96vw");

        Optional<CajaTurno> turnoOpt = cajaService.obtenerCajaAbierta();
        if (turnoOpt.isEmpty()) {
            Div vacio = new Div(new Span("No hay un turno de caja abierto."));
            vacio.addClassName("dc-vacio");
            add(vacio);
            getFooter().add(new Button("Cerrar", e -> close()));
            return;
        }
        CajaTurno turno = turnoOpt.get();
        Integer turnoId = turno.getId().intValue();

        ResumenCajaDTO r = cajaService.obtenerResumenCaja(turnoId);
        BigDecimal efectivoEsperado = r.getEfectivoEsperado();

        // ---------- Banda superior: datos del turno ----------
        Div banda = DialogoCajaEstilo.banda(VaadinIcon.LOCK, "Turno #" + turno.getId(),
                "Abierto: " + (turno.getFechaApertura() != null ? turno.getFechaApertura().format(F_FECHA) : "")
                + (turno.getCajeroApertura() != null ? "  ·  Cajero: " + turno.getCajeroApertura() : ""));

        // ---------- Billete principal: efectivo esperado ----------
        Div esperado = DialogoCajaEstilo.billeteGrande("Efectivo esperado en gaveta", formatear(efectivoEsperado));

        // ---------- Entradas y salidas ----------
        Div entradas = columna("Entradas de efectivo", "dc-col-entradas", VaadinIcon.ARROW_CIRCLE_DOWN,
                fila(VaadinIcon.PIGGY_BANK, "Fondo inicial", r.getFondoInicial()),
                fila(VaadinIcon.PLUS_CIRCLE, "Fondo extra", r.getFondoExtra()),
                fila(VaadinIcon.CART, "Ventas en efectivo (netas)", r.getVentasEfectivo()),
                fila(VaadinIcon.TRUCK, "Cobros de delivery", r.getCobrosDelivery()),
                fila(VaadinIcon.USER_CHECK, "Abonos de clientes (CxC)", r.getCobrosCxc()));
        Div salidas = columna("Salidas de efectivo", "dc-col-salidas", VaadinIcon.ARROW_CIRCLE_UP,
                fila(VaadinIcon.PACKAGE, "Pagos a proveedores", r.getPagosProveedores()),
                fila(VaadinIcon.EXCHANGE, "Avances con tarjeta", r.getAvancesEfectivo()),
                fila(VaadinIcon.ELLIPSIS_DOTS_H, "Otros movimientos", r.getOtrosMovimientos()));
        Div tarjeta = new Div(icono(VaadinIcon.CREDIT_CARD), new Span("Ventas con tarjeta (referencia, no está en la gaveta)"),
                monto(r.getVentasTarjeta(), "dc-monto dc-monto-ref"));
        tarjeta.addClassName("dc-tarjeta");

        Div columnas = new Div(entradas, salidas);
        columnas.addClassName("dc-columnas");

        // ---------- Conteo ----------
        BigDecimalField efectivoRealField = new BigDecimalField("Efectivo contado en la gaveta");
        efectivoRealField.addClassName("dc-campo-grande");
        efectivoRealField.setWidthFull();
        efectivoRealField.setPlaceholder("0.00");
        efectivoRealField.setPrefixComponent(new Span("RD$"));
        efectivoRealField.setValueChangeMode(ValueChangeMode.EAGER);

        Icon icDif = VaadinIcon.SCALE.create();
        Span lblDif = new Span("Diferencia");
        Span valDif = new Span("—");
        Span notaDif = new Span("Escriba lo que contó en la gaveta");
        lblDif.addClassName("dc-dif-etiqueta");
        valDif.addClassName("dc-dif-valor");
        notaDif.addClassName("dc-dif-nota");
        Div panelDif = new Div(icDif, new Div(lblDif, notaDif), valDif);
        panelDif.addClassName("dc-diferencia");

        efectivoRealField.addValueChangeListener(e -> {
            panelDif.removeClassNames("dc-dif-cuadre", "dc-dif-sobrante", "dc-dif-faltante");
            BigDecimal contado = e.getValue();
            if (contado == null) {
                valDif.setText("—");
                notaDif.setText("Escriba lo que contó en la gaveta");
                return;
            }
            BigDecimal diferencia = contado.subtract(efectivoEsperado);
            if (diferencia.signum() == 0) {
                valDif.setText("RD$ 0.00");
                notaDif.setText("¡Cuadre perfecto!");
                panelDif.addClassName("dc-dif-cuadre");
            } else if (diferencia.signum() > 0) {
                valDif.setText("+RD$ " + MONEDA_FORMAT.format(diferencia));
                notaDif.setText("Sobrante: hay más efectivo del esperado");
                panelDif.addClassName("dc-dif-sobrante");
            } else {
                valDif.setText("-RD$ " + MONEDA_FORMAT.format(diferencia.abs()));
                notaDif.setText("Faltante: hay menos efectivo del esperado");
                panelDif.addClassName("dc-dif-faltante");
            }
        });

        Div conteo = new Div(efectivoRealField, panelDif);
        conteo.addClassName("dc-conteo");

        Div cuerpo = new Div(banda, esperado, columnas, tarjeta, conteo);
        cuerpo.addClassName("dc-cuerpo");
        add(cuerpo);

        // ---------- Botones ----------
        Button cancelBtn = new Button("Cancelar", e -> close());
        cancelBtn.addClassName("dc-btn-secundario");

        Button processBtn = new Button("Cerrar Caja [Enter]", VaadinIcon.LOCK.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        processBtn.addClassName("dc-btn-cerrar");
        processBtn.addClickShortcut(Key.ENTER);

        processBtn.addClickListener(e -> {
            if (confirmando) {
                return; // ya hay una confirmación abierta (p. ej. Enter repetido)
            }
            BigDecimal contado = efectivoRealField.getValue();
            if (contado == null || contado.compareTo(BigDecimal.ZERO) < 0) {
                Notification.show("Ingresa el monto de efectivo contado", 2500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                efectivoRealField.focus();
                return;
            }
            confirmarCierre(turno, efectivoEsperado, contado, () -> ejecutarCierre(cajaService, turnoId, usuario, contado));
        });

        getFooter().add(cancelBtn, processBtn);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                efectivoRealField.focus();
            }
        });
    }

    /** true mientras se muestra la pregunta "¿Está seguro?". */
    private boolean confirmando;

    /** Pregunta antes de cerrar, mostrando esperado, contado y diferencia. */
    private void confirmarCierre(CajaTurno turno, BigDecimal esperado, BigDecimal contado, Runnable alConfirmar) {
        confirmando = true;
        BigDecimal dif = contado.subtract(esperado);
        String textoDif = dif.signum() == 0 ? "Cuadre perfecto"
                : (dif.signum() > 0 ? "Sobrante de +RD$ " : "Faltante de -RD$ ") + MONEDA_FORMAT.format(dif.abs());

        Div resumen = new Div(
                lineaConfirmacion("Efectivo esperado", formatear(esperado), null),
                lineaConfirmacion("Efectivo contado", formatear(contado), null),
                lineaConfirmacion("Diferencia", textoDif,
                        dif.signum() < 0 ? "dc-dif-faltante" : dif.signum() > 0 ? "dc-dif-sobrante" : "dc-dif-cuadre"));
        resumen.addClassName("dc-confirma-resumen");
        Span aviso = new Span("Al cerrar, el turno ya no acepta ventas ni movimientos y se sale del punto de venta.");
        aviso.addClassName("dc-confirma-aviso");
        Div cuerpo = new Div(resumen, aviso);
        cuerpo.addClassName("dc-confirma");

        com.vaadin.flow.component.confirmdialog.ConfirmDialog cd = new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        cd.addClassName("dc-dialogo");
        cd.setHeader("¿Está seguro de cerrar la caja del turno #" + turno.getId() + "?");
        cd.setText(cuerpo);
        cd.setCancelable(true);
        cd.setCancelText("No, volver");
        cd.setConfirmText("Sí, cerrar la caja");
        cd.setConfirmButtonTheme("error primary");
        cd.addConfirmListener(ev -> {
            confirmando = false;
            alConfirmar.run();
        });
        cd.addCancelListener(ev -> confirmando = false);
        cd.addRejectListener(ev -> confirmando = false);
        cd.open();
    }

    private Div lineaConfirmacion(String etiqueta, String valor, String clase) {
        Span l = new Span(etiqueta);
        Span v = new Span(valor);
        v.addClassName("dc-monto");
        Div d = new Div(l, v);
        d.addClassName("dc-confirma-linea");
        if (clase != null) {
            d.addClassName(clase);
        }
        return d;
    }

    private void ejecutarCierre(CajaService cajaService, Integer turnoId, String usuario, BigDecimal contado) {
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
    }

    // ============================================================
    // Componentes
    // ============================================================
    private Div columna(String titulo, String clase, VaadinIcon ic, Component... filas) {
        Div cab = new Div(icono(ic), new Span(titulo));
        cab.addClassName("dc-col-titulo");
        Div col = new Div(cab);
        col.add(filas);
        col.addClassNames("dc-col", clase);
        return col;
    }

    private Div fila(VaadinIcon ic, String etiqueta, BigDecimal valor) {
        Span lbl = new Span(etiqueta);
        lbl.addClassName("dc-fila-etiqueta");
        Div f = new Div(icono(ic), lbl, monto(valor, "dc-monto"));
        f.addClassName("dc-fila");
        if (valor == null || valor.signum() == 0) {
            f.addClassName("dc-fila-cero");
        }
        return f;
    }

    private Span monto(BigDecimal valor, String clases) {
        Span s = new Span(formatear(valor));
        for (String c : clases.split(" ")) {
            s.addClassName(c);
        }
        if (valor != null && valor.signum() < 0) {
            s.addClassName("dc-negativo");
        }
        return s;
    }

    private Icon icono(VaadinIcon ic) {
        Icon i = ic.create();
        i.addClassName("dc-icono");
        return i;
    }

    private static String formatear(BigDecimal monto) {
        BigDecimal m = monto != null ? monto : BigDecimal.ZERO;
        return (m.signum() < 0 ? "-RD$ " : "RD$ ") + MONEDA_FORMAT.format(m.abs());
    }
}
