package com.maxsoft.application.view.componente;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Estilo común de las pantallas de consulta (Facturas, Recibos, Órdenes,
 * Recepciones, Estado de Cuenta). Las clases CSS (fv-*) están en
 * themes/gestion-colmadom/views/consulta-view.css y aplican dentro de un
 * contenedor con la clase "fv-vista".
 */
public final class EstiloConsulta {

    private EstiloConsulta() {
    }

    private static final DecimalFormat FORMATO = new DecimalFormat("#,##0.00");
    /** Diferencias menores a un centavo se consideran cero. */
    public static final BigDecimal CENTAVO = new BigDecimal("0.005");

    /** Marca la vista y su TabSheet (si tiene) con el estilo de consulta. */
    public static void aplicarVista(Component vista, TabSheet tabs) {
        vista.addClassName("fv-vista");
        if (tabs != null) {
            tabs.addClassName("fv-tabs");
        }
    }

    /** Encabezado verde, filas alternas y compacto. */
    public static void aplicarGrid(Grid<?> grid) {
        grid.addClassName("fv-grid");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
    }

    /** Columna de monto: a la derecha y con números de ancho fijo. */
    public static <T> Grid.Column<T> monto(Grid.Column<T> col) {
        return col.setTextAlign(ColumnTextAlign.END).setPartNameGenerator(x -> "fv-monto");
    }

    public static Component etiquetaTab(VaadinIcon icono, String texto) {
        HorizontalLayout h = new HorizontalLayout(icono.create(), new Span(texto));
        h.setSpacing(true);
        h.setAlignItems(FlexComponent.Alignment.CENTER);
        return h;
    }

    /** Cambia el texto de una pestaña conservando el ícono. */
    public static void renombrarTab(TabSheet tabs, int indice, VaadinIcon icono, String texto) {
        tabs.getTabAt(indice).removeAll();
        tabs.getTabAt(indice).add(etiquetaTab(icono, texto));
    }

    public static Span ayuda(String texto) {
        Span s = new Span(texto);
        s.addClassName("fv-ayuda");
        return s;
    }

    public static Span titulo(String texto) {
        Span s = new Span(texto);
        s.addClassName("fv-titulo-texto");
        return s;
    }

    // ---------- Etiquetas ----------
    public static Span chip(String texto, String clase) {
        Span s = new Span(texto);
        s.addClassNames("fv-chip", clase);
        return s;
    }

    public static Span chipPagada() {
        return chip("PAGADA", "fv-chip-pagada");
    }

    public static Span chipPendiente() {
        return chip("PENDIENTE", "fv-chip-pendiente");
    }

    public static Span chipAnulada() {
        return chip("ANULADA", "fv-chip-anulada");
    }

    public static Span chipCredito() {
        return chip("CRÉDITO", "fv-chip-credito");
    }

    public static Span chipContado() {
        return chip("CONTADO", "fv-chip-contado");
    }

    /** Monto pendiente: rojo suave si debe algo, gris si está saldado; "—" si anulado. */
    public static Span pendiente(BigDecimal pendiente, boolean anulado) {
        if (anulado) {
            Span s = new Span("—");
            s.addClassName("fv-saldado");
            return s;
        }
        BigDecimal p = pendiente != null ? pendiente.max(BigDecimal.ZERO) : BigDecimal.ZERO;
        Span s = new Span(moneda(p));
        s.addClassName(p.compareTo(CENTAVO) > 0 ? "fv-pendiente" : "fv-saldado");
        return s;
    }

    public static boolean tieneSaldo(BigDecimal pendiente) {
        return pendiente != null && pendiente.compareTo(CENTAVO) > 0;
    }

    // ---------- Detalle ----------
    /** Cabecera del detalle: número grande, datos debajo y etiquetas a la derecha. */
    public static void cabecera(Div contenedor, String numero, String datos, Component... chips) {
        contenedor.removeAll();
        contenedor.addClassName("fv-detalle-cabecera");
        Span n = new Span(numero);
        n.addClassName("fv-detalle-numero");
        Span d = new Span(datos != null ? datos : "");
        d.addClassName("fv-detalle-datos");
        Div c = new Div(chips);
        c.addClassName("fv-chips");
        contenedor.add(new Div(n, d), c);
    }

    /** Mensaje del detalle vacío. */
    public static void cabeceraVacia(Div contenedor, String mensaje) {
        contenedor.removeAll();
        contenedor.addClassName("fv-detalle-cabecera");
        Span s = new Span(mensaje);
        s.addClassName("fv-vacio");
        contenedor.add(s);
    }

    /**
     * Tarjeta de resumen. Clases: fv-tarjeta-total, fv-tarjeta-pagado,
     * fv-tarjeta-pendiente (rojo suave), fv-tarjeta-saldada, fv-tarjeta-devuelto.
     */
    public static Div tarjeta(String etiqueta, BigDecimal valor, String clase, VaadinIcon icono) {
        Icon ic = icono.create();
        ic.addClassName("fv-tarjeta-icono");
        Span lbl = new Span(etiqueta);
        lbl.addClassName("fv-tarjeta-etiqueta");
        Span val = new Span(moneda(valor));
        val.addClassName("fv-tarjeta-valor");
        Div d = new Div(ic, lbl, val);
        d.addClassNames("fv-tarjeta", clase);
        return d;
    }

    /** Tarjeta de pendiente: rojo suave si hay saldo. */
    public static Div tarjetaPendiente(String etiqueta, BigDecimal pendiente) {
        return tarjeta(etiqueta, pendiente, tieneSaldo(pendiente) ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada",
                VaadinIcon.CLOCK);
    }

    /** Contenedor del detalle: cabecera + tarjetas + grilla, con scroll propio. */
    public static Div panelDetalle(Div cabecera, Div tarjetas, Component grilla) {
        cabecera.addClassName("fv-detalle-cabecera");
        tarjetas.addClassName("fv-tarjetas");
        Div p = new Div(cabecera, tarjetas, grilla);
        p.addClassName("fv-detalle");
        p.setSizeFull();
        return p;
    }

    public static String moneda(BigDecimal v) {
        BigDecimal m = v != null ? v : BigDecimal.ZERO;
        return (m.signum() < 0 ? "-RD$ " : "RD$ ") + FORMATO.format(m.abs());
    }
}
