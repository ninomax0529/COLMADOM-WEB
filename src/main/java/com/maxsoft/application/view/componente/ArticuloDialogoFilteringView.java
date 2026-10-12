/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente;


import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

public class ArticuloDialogoFilteringView extends Dialog {

    private final Grid<Articulo> grid = new Grid<>(Articulo.class, false);
    private final TextField txtFiltro = new TextField();
    private final Button btnSeleccionar = new Button("Seleccionar (Enter)");
    private final Button btnCancelar = new Button("Cancelar");

    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService;
    private final ArticuloAlmacenService existenciaService;
    private final Almacen almacenSeleccionado;
    private final Consumer<Articulo> onSeleccionConsumer;

    /**
     * Constructor adaptado para Multi-Empaque y Multi-Almacén
     */
    public ArticuloDialogoFilteringView(ArticuloService articuloService,
                                        ArticuloEmpaqueService articuloEmpaqueService,
                                        ArticuloAlmacenService existenciaService,
                                        Almacen almacenSeleccionado,
                                        Consumer<Articulo> onSeleccionConsumer) {

        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;
        this.existenciaService = existenciaService;
        this.almacenSeleccionado = almacenSeleccionado;
        this.onSeleccionConsumer = onSeleccionConsumer;

        setHeaderTitle("Búsqueda y Selección de Artículos (Multi-Empaque)");
        setWidth("900px");
        setHeight("600px");

        configurarControles();
        configurarGrid();
        cargarDatos();

        VerticalLayout layoutContenido = new VerticalLayout(txtFiltro, grid);
        layoutContenido.setPadding(false);
        layoutContenido.setSpacing(true);
        layoutContenido.setSizeFull();

        HorizontalLayout layoutBotones = new HorizontalLayout(btnSeleccionar, btnCancelar);
        layoutBotones.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layoutBotones.setWidthFull();

        add(layoutContenido);
        getFooter().add(layoutBotones);
    }

    private void configurarControles() {
        txtFiltro.setPlaceholder("Buscar por código, descripción o categoría...");
        txtFiltro.setWidthFull();
        txtFiltro.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        btnSeleccionar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSeleccionar.addClickShortcut(Key.ENTER);
        btnSeleccionar.addClickListener(e -> seleccionarItemActual());

        btnCancelar.addClickListener(e -> close());
    }

    private void configurarGrid() {
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();

        // Código
        grid.addColumn(Articulo::getCodigo)
                .setHeader("Código")
                .setWidth("100px")
                .setFlexGrow(0);

        // Descripción
        grid.addColumn(Articulo::getDescripcion)
                .setHeader("Descripción")
                .setAutoWidth(true);

        // Categoría
        grid.addColumn(art -> art.getCategoria() != null ? art.getCategoria(): "")
                .setHeader("Categoría")
                .setAutoWidth(true);

        // Empaque Predeterminado (Multi-Empaque)
        grid.addColumn(art -> {
            
            if (articuloEmpaqueService == null) return "";
            List<ArticuloEmpaque> empaques = articuloEmpaqueService.getPorArticulo(art.getCodigo());
            
            return empaques.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getUnidadEmpaque()) || Boolean.TRUE.equals(e.getUnidadEmpaque()))
                    .findFirst()
                    .map(e -> (e.getUnidadEmpaque()!= null ? e.getUnidadEmpaque().getDescripcion()
                            : "") + " (" + e.getUnidadEmpaque() + " U.)")
                    .orElse("Unidad Base");
        }).setHeader("Empaque Principal").setAutoWidth(true);

        // Stock Actual en Almacén Seleccionado
        grid.addColumn(art -> {
            if (existenciaService == null || almacenSeleccionado == null) return BigDecimal.ZERO;
            return existenciaService.buscarPorArticuloYAlmacen(art.getCodigo(), almacenSeleccionado.getCodigo())
                    .map(e -> e.getExistencia() != null ? e.getExistencia() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);
        }).setHeader("Stock (" + (almacenSeleccionado != null ? almacenSeleccionado.getNombre() : "Gral") + ")").setAutoWidth(true);

        // Doble clic para rápida selección
        grid.addItemDoubleClickListener(event -> {
            if (event.getItem() != null) {
                confirmarSeleccion(event.getItem());
            }
        });
    }

    private void cargarDatos() {
        
        List<Articulo> listaArticulos = articuloService.getLista();
        GridListDataView<Articulo> dataView = grid.setItems(listaArticulos);

        txtFiltro.addValueChangeListener(e -> dataView.addFilter(art -> {
            String term = e.getValue().trim().toLowerCase();
            if (term.isEmpty()) return true;

            boolean matchDesc = art.getDescripcion() != null && art.getDescripcion().toLowerCase().contains(term);
            boolean matchCod = art.getCodigo() != null && art.getCodigo().toString().contains(term);
            boolean matchCat = art.getCategoria() != null && art.getCategoria().toString().toLowerCase().contains(term);

            return matchDesc || matchCod || matchCat;
        }));
    }

    private void seleccionarItemActual() {
        grid.getSelectedItems().stream().findFirst().ifPresent(this::confirmarSeleccion);
    }

    private void confirmarSeleccion(Articulo articulo) {
        if (onSeleccionConsumer != null && articulo != null) {
            onSeleccionConsumer.accept(articulo);
        }
        close();
    }
}