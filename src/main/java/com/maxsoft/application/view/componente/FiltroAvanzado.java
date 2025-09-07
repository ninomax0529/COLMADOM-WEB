/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;

/**
 *
 * @author maximilianoalmonte
 */

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
// ...


import java.util.*;
import java.util.function.Function;

public class FiltroAvanzado<T> extends Composite<HorizontalLayout> {

    private final GridListDataView<T> dataView;
    private final Map<TextField, Function<T, String>> camposFiltrables = new LinkedHashMap<>();

    public FiltroAvanzado(GridListDataView<T> dataView) {
        this.dataView = dataView;
        getContent().setSpacing(true);
    }

    /**
     * Agrega un campo de filtro basado en texto.
     * @param label Etiqueta visible del campo
     * @param extractor Función que extrae la propiedad de la entidad
     */
    public void addFiltro(String label, Function<T, String> extractor) {
        TextField filtro = new TextField(label);
        filtro.setClearButtonVisible(true);
        filtro.setPlaceholder("Buscar...");
        filtro.setValueChangeMode(ValueChangeMode.EAGER);
        

        filtro.addValueChangeListener(e -> aplicarFiltros());

        camposFiltrables.put(filtro, extractor);
        getContent().add(filtro);
    }



private void aplicarFiltros() {
    List<SerializablePredicate<T>> predicados = new ArrayList<>();

    camposFiltrables.forEach((campo, extractor) -> {
        String valor = campo.getValue().trim().toLowerCase();
        if (!valor.isEmpty()) {
            predicados.add((SerializablePredicate<T>) item -> {
                String propiedad = Optional.ofNullable(extractor.apply(item)).orElse("");
                return propiedad.toLowerCase().contains(valor);
            });
        }
    });

    SerializablePredicate<T> combinado = predicados.stream()
            .reduce(x -> true, SerializablePredicate::and);

    dataView.setFilter(combinado);
}

}
