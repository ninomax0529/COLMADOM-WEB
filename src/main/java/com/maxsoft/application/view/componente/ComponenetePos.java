/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

/**
 *
 * @author maximilianoalmonte
 */
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class ComponenetePos extends VerticalLayout {

    private BigDecimal montoACobrar = BigDecimal.ZERO;

    private final Span totalLabel;
    private final Span totalValor;
    private final BigDecimalField montoRecibido;
    private final Span cambioValor;

    private Consumer<Boolean> estadoValidezListener;

    private final NumberFormat formato =
            NumberFormat.getCurrencyInstance(new Locale("es", "DO"));

    public ComponenetePos() {

        addClassName("pos-cobrar");
        setPadding(false);
        setSpacing(true);
        setAlignItems(Alignment.STRETCH);

        /* ========= TOTAL A COBRAR ========= */

        totalLabel = new Span("TOTAL A COBRAR");
        totalLabel.addClassName("pos-total-label");

        totalValor = new Span(formato.format(BigDecimal.ZERO));
        totalValor.addClassName("pos-total-valor");

        /* ========= MONTO RECIBIDO ========= */

        montoRecibido = new BigDecimalField("Monto recibido");
        montoRecibido.addClassName("alineado-derecha");
        montoRecibido.setClearButtonVisible(true);
        montoRecibido.setAutofocus(true);

        /* ========= CAMBIO ========= */

        Span cambioLabel = new Span("CAMBIO");
        cambioLabel.addClassName("pos-cambio-label");

        cambioValor = new Span(formato.format(BigDecimal.ZERO));
        cambioValor.addClassNames("pos-cambio");

        /* ========= ENSAMBLAR ========= */

        add(
                totalLabel,
                totalValor,
                montoRecibido,
                cambioLabel,
                cambioValor
        );

        configurarEventos();
    }

    /* ======================================
       EVENTOS
       ====================================== */

    private void configurarEventos() {

        montoRecibido.addValueChangeListener(e -> recalcular());

        // ENTER confirma visualmente (útil en POS)
        montoRecibido.addKeyPressListener(Key.ENTER, e -> {
            recalcular();
        });
    }

    private void recalcular() {

        BigDecimal recibido = montoRecibido.getValue();

        if (recibido == null || montoACobrar == null) {
            actualizarCambio(BigDecimal.ZERO);
            notificarEstado(false);
            return;
        }

        BigDecimal cambio = recibido.subtract(montoACobrar);
        actualizarCambio(cambio);

        boolean valido = cambio.compareTo(BigDecimal.ZERO) >= 0;
        notificarEstado(valido);
    }

    private void actualizarCambio(BigDecimal cambio) {

        cambioValor.setText(formato.format(cambio));

        cambioValor.removeClassNames("positivo", "negativo");

        if (cambio.signum() >= 0) {
            cambioValor.addClassName("positivo");
        } else {
            cambioValor.addClassName("negativo");
        }
    }

    private void notificarEstado(boolean valido) {
        if (estadoValidezListener != null) {
            estadoValidezListener.accept(valido);
        }
    }

    /* ======================================
       API PÚBLICA DEL COMPONENTE
       ====================================== */

    /** Establece el total a cobrar desde la vista
     * @param monto */
    public void setMontoACobrar(BigDecimal monto) {
        this.montoACobrar = monto != null ? monto : BigDecimal.ZERO;
        totalValor.setText(formato.format(this.montoACobrar));
        recalcular();
    }

    /** Devuelve el monto recibido
     * @return  */
    public BigDecimal getMontoRecibido() {
        return montoRecibido.getValue();
    }

    /** Devuelve el cambio calculado
     * @return  */
    public BigDecimal getCambio() {
        if (montoRecibido.getValue() == null) {
            return BigDecimal.ZERO;
        }
        return montoRecibido.getValue().subtract(montoACobrar);
    }

    /** Limpia el componente luego de cobrar */
    public void limpiar() {
        montoRecibido.clear();
        cambioValor.setText(formato.format(BigDecimal.ZERO));
        notificarEstado(false);
        montoRecibido.focus();
    }

    /** Listener para habilitar/deshabilitar botón COBRAR
     * @param listener */
    public void setEstadoValidezListener(Consumer<Boolean> listener) {
        this.estadoValidezListener = listener;
    }
}
