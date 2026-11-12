package com.maxsoft.application.view.venta.cajaChica;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Piezas visuales compartidas por los diálogos de caja del POS (apertura,
 * cierre y movimientos). Estilos en themes/gestion-colmadom/views/dialogo-caja.css.
 */
public final class DialogoCajaEstilo {

    private DialogoCajaEstilo() {
    }

    /** Banda verde superior con ícono dorado, título y subtítulo. */
    public static Div banda(VaadinIcon icono, String titulo, String subtitulo) {
        Icon ic = icono.create();
        ic.addClassName("dc-banda-icono");
        Div moneda = new Div(ic);
        moneda.addClassName("dc-banda-moneda");

        Span t = new Span(titulo);
        t.addClassName("dc-banda-titulo");
        Span s = new Span(subtitulo != null ? subtitulo : "");
        s.addClassName("dc-banda-subtitulo");
        Div textos = new Div(t, s);
        textos.addClassName("dc-banda-textos");

        Div banda = new Div(moneda, textos);
        banda.addClassName("dc-banda");
        return banda;
    }

    /** Billete grande con un monto destacado. */
    public static Div billeteGrande(String etiqueta, String monto) {
        Span lbl = new Span(etiqueta);
        lbl.addClassName("dc-billete-etiqueta");
        Span val = new Span(monto);
        val.addClassName("dc-billete-valor");
        Div sello = new Div(new Span("RD$"));
        sello.addClassName("dc-billete-sello");
        Div b = new Div(sello, lbl, val);
        b.addClassName("dc-billete");
        return b;
    }
}
