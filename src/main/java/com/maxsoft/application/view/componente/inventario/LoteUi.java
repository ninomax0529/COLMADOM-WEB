package com.maxsoft.application.view.componente.inventario;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Campos de lote para las grillas de documentos de inventario. Solo se
 * habilitan para artículos con control de lote.
 */
public final class LoteUi {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LoteUi() {
    }

    /**
     * Combo para elegir de qué lote sale la mercancía (merma, ajuste negativo,
     * traslado, salida). Vacío = automático por FEFO. Si el lote que tenía la
     * línea ya no está disponible en el almacén (p. ej. se cambió el almacén),
     * se limpia.
     */
    public static ComboBox<LoteAlmacen> selectorSalida(LoteService loteService, Articulo articulo, Almacen almacen,
            Lote actual, Consumer<Lote> alCambiar) {

        ComboBox<LoteAlmacen> cb = new ComboBox<>();
        cb.setWidth("230px");
        cb.setClearButtonVisible(true);
        boolean conLote = articulo != null && articulo.isControlaLote() && almacen != null;
        if (!conLote) {
            cb.setEnabled(false);
            cb.setPlaceholder(articulo != null && articulo.isControlaLote() ? "Elija almacén" : "No aplica");
            return cb;
        }
        LocalDate hoy = LocalDate.now();
        List<LoteAlmacen> lotes = loteService.getLotesDisponibles(articulo.getCodigo(), almacen.getCodigo());
        cb.setItems(lotes);
        cb.setItemLabelGenerator(la -> etiqueta(la, hoy));
        cb.setPlaceholder("Automático (FEFO)");

        if (actual != null) {
            LoteAlmacen sel = lotes.stream()
                    .filter(la -> Objects.equals(la.getLote().getCodigo(), actual.getCodigo()))
                    .findFirst().orElse(null);
            if (sel != null) {
                cb.setValue(sel);
            } else {
                alCambiar.accept(null);
            }
        }
        cb.addValueChangeListener(e -> alCambiar.accept(e.getValue() != null ? e.getValue().getLote() : null));
        return cb;
    }

    /** Número de lote para entradas (vacío = lote automático del día). */
    public static TextField numeroLote(Articulo articulo, String actual, Consumer<String> alCambiar) {
        TextField txt = new TextField();
        txt.setWidth("120px");
        txt.setMaxLength(50);
        boolean conLote = articulo != null && articulo.isControlaLote();
        txt.setEnabled(conLote);
        txt.setPlaceholder(conLote ? "Automático" : "No aplica");
        txt.setValue(actual != null ? actual : "");
        txt.addValueChangeListener(e -> alCambiar.accept(
                e.getValue() == null || e.getValue().isBlank() ? null : e.getValue().trim().toUpperCase()));
        return txt;
    }

    /** Vencimiento del lote para entradas (vacío = hoy + días de vida útil, o sin vencimiento). */
    public static DatePicker vencimiento(Articulo articulo, LocalDate actual, Consumer<LocalDate> alCambiar) {
        DatePicker dp = new DatePicker();
        dp.setWidth("150px");
        boolean conLote = articulo != null && articulo.isControlaLote();
        dp.setEnabled(conLote);
        dp.setMin(LocalDate.now());
        Integer dias = articulo != null ? articulo.getDiasVencimiento() : null;
        dp.setPlaceholder(!conLote ? "No aplica"
                : (dias != null && dias > 0 ? "Hoy + " + dias + " días" : "Sin vencimiento"));
        dp.setValue(actual);
        dp.addValueChangeListener(e -> alCambiar.accept(e.getValue()));
        return dp;
    }

    /** "L20261112 · vence 27/11/2026 · 12 disp." (o VENCIDO). */
    public static String etiqueta(LoteAlmacen la, LocalDate hoy) {
        Lote l = la.getLote();
        String vence = l.getFechaVencimiento() == null ? "sin venc."
                : (l.isVencido(hoy) ? "VENCIDO " : "vence ") + l.getFechaVencimiento().format(FECHA);
        return l.getNumeroLote() + " · " + vence + " · " + la.getExistencia().stripTrailingZeros().toPlainString() + " disp.";
    }
}
