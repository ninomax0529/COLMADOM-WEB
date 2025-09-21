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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
// ...

import java.util.*;

import java.util.function.Function;

public class FiltroDataView<T> extends Composite<HorizontalLayout> {

    private TextField txtBuscar = new TextField();
    private final GridListDataView<T> dataView;
    private final Map<TextField, Function<T, String>> camposFiltrables = new LinkedHashMap<>();

    public FiltroDataView(GridListDataView<T> dataView) {
        this.dataView = dataView;
        getContent().setSpacing(true);
        txtBuscar.focus();
        txtBuscar.setWidth("50%");
        txtBuscar.setPlaceholder("Filtrar");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
        txtBuscar.addValueChangeListener(e -> dataView.refreshAll());

    }

    public boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    /**
     * Agrega un campo de filtro basado en texto.
     *
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

    /**
     * @return the txtBuscar
     */
    public TextField getTxtBuscar() {
        return txtBuscar;
    }

    /**
     * @param txtBuscar the txtBuscar to set
     */
    public void setTxtBuscar(TextField txtBuscar) {
        this.txtBuscar = txtBuscar;
    }

}
