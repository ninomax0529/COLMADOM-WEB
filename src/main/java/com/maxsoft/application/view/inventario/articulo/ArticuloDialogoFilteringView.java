/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.articulo;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.function.Consumer;

//@Route("grid-column-filtering")
@Route(value = "consultaArticulos")
public final class ArticuloDialogoFilteringView extends Dialog {

    /**
     * @return the articulo
     */
    private final ArticuloService articuloService;

    private Articulo articulo;

    TextField searchField = new TextField();
    private Button closeButton;

    public ArticuloDialogoFilteringView(ArticuloService articuloService, DialogCallback dialogCallback) {

        this.articuloService = articuloService;
        // tag::snippet1[]

         setSizeFull();
//        setWidth("600px");
//        setHeight("300");
        setHeaderTitle("Consulta de Artículos");
        Grid<Articulo> grid = new Grid<>(Articulo.class, false);

        setArticulo(new Articulo());

//        closeButton = new Button("", e -> {
//
//            Articulo selected = grid.asSingleSelect().getValue();
//
//            if (selected != null) {
//
//                setArticulo(grid.getSelectionModel().getFirstSelectedItem().get());
//
//                System.out.println("no es null");
//                dialogCallback.onConfirm(getArticulo());
//
//                close();
//
//            } else {
//                System.out.println(" es  null ");
//
//                close();
//            }
//
//        });
        searchField.addKeyPressListener(Key.ENTER, e -> {

            Articulo selected = grid.asSingleSelect().getValue();

            if (selected != null) {

                setArticulo(grid.getSelectionModel().getFirstSelectedItem().get());

                System.out.println("no es null");
                dialogCallback.onConfirm(getArticulo());

                close();

            } else {
                System.out.println(" es  null ");

                close();
            }
        });

        grid.addColumn(Articulo::getCodigo).setHeader("Codigo");
        grid.addColumn(Articulo::getDescripcion).setHeader("Descripcion");

        List<Articulo> listArt = articuloService.getLista();
        GridListDataView<Articulo> dataView = grid.setItems(listArt);

        searchField.setWidth("50%");
        searchField.setPlaceholder("Buscar");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(person -> {
            
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty()) {
                return true;
            }

            boolean matchesFullName = matchesTerm(person.getDescripcion(),
                    searchTerm);
            boolean matchesEmail = matchesTerm(person.getCodigo().toString(), searchTerm);

            return matchesFullName || matchesEmail;
        });

//        VerticalLayout layout = new VerticalLayout(grid);
//        layout.setSizeFull();
//        layout.setPadding(false);

        add(searchField,grid);
    }

    private static Component createFilterHeader(String labelText,
            Consumer<String> filterChangeConsumer) {

        NativeLabel label = new NativeLabel(labelText);

        label.getStyle().set("padding-top", "var(--lumo-space-m)")
                .set("font-size", "var(--lumo-font-size-xs)");

        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");

        textField.addValueChangeListener(
                e -> filterChangeConsumer.accept(e.getValue()));
        VerticalLayout layout = new VerticalLayout(label, textField);
        layout.getThemeList().clear();

        layout.getThemeList().add("spacing-xs");

        return layout;
    }

    private static class PersonFilter {

        private final GridListDataView<Articulo> dataView;

        private String fullName;
        private String email;
        private String profession;

        public PersonFilter(GridListDataView<Articulo> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
            this.dataView.refreshAll();
        }

        public void setEmail(String email) {
            this.email = email;
            this.dataView.refreshAll();
        }

        public void setProfession(String profession) {
            this.profession = profession;
            this.dataView.refreshAll();
        }

        public boolean test(Articulo person) {

            boolean matchesFullName = matches(person.getDescripcion(), fullName);

            boolean matchesEmail = matches(person.getCodigo().toString(), email);
            boolean matchesProfession = matches(person.getDescripcion(),
                    profession);

            return matchesFullName && matchesEmail && matchesProfession;
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }
    }

    private static Renderer<Articulo> createPersonRenderer() {
        return LitRenderer.<Articulo>of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                + "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
                + "  <span> ${item.fullName} </span>"
                + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", Articulo::getCodigo)
                .withProperty("fullName", Articulo::getDescripcion);
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    public Articulo getArticulo() {
        return articulo;
    }

    /**
     * @param articulo the articulo to set
     */
    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public interface DialogCallback {

        void onConfirm(Articulo input);
    }

}
