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
import com.maxsoft.application.modelo.ArticuloClasificacionAbc;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.ClasificacionAbcService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.maxsoft.application.view.inventario.abc.ClasificacionAbcView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Artículos")
@Route(value = "inventario/articulo")

public class ArticuloView extends VerticalLayout {

    /** Orden de la columna ABC: A, B, C, N y al final los sin clasificar. */
    private static final List<String> ORDEN_CLASES = ClasificacionAbcView.codigosClase();

    ArticuloService articuloService;

    Grid<Articulo> grid = new Grid<>(Articulo.class, false);
    List<Articulo> listaArt = new ArrayList<>();
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);
    TextField txtFiltroArt = new TextField();
    ComboBox<String> cbClaseAbc = new ComboBox<>();

    // Clasificación ABC por código de artículo (la calcula ClasificacionAbcService)
    private Map<Integer, ArticuloClasificacionAbc> clasificacion = Map.of();
    private GridListDataView<Articulo> dataView;

    @Autowired
    public ArticuloView(ArticuloService articuloServiceArg, ClasificacionAbcService clasificacionAbcService) {

        this.articuloService = articuloServiceArg;
        this.clasificacion = clasificacionAbcService.obtenerTodas().stream()
                .collect(Collectors.toMap(ArticuloClasificacionAbc::getArticulo, Function.identity()));

        setSizeFull();
        setSpacing(false);
        // Colores de las etiquetas ABC (fv-chip-*)
        EstiloConsulta.aplicarVista(this, null);

        configurarGrid();
        configurarFiltros();

        HorizontalLayout filtros = new HorizontalLayout(txtFiltroArt, cbClaseAbc);
        filtros.setWidthFull();
        filtros.setAlignItems(Alignment.BASELINE);
        filtros.expand(txtFiltroArt);

        add(botonera,
                new H2("Articulos"),
                filtros, grid); // parte superior

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            String key = NavigationContext.store(new Articulo());

            UI.getCurrent().navigate(RegistrarArticuloViewV4.class, key);

        });

    }

    private void configurarGrid() {

        grid.addColumn(Articulo::getCodigo).setHeader("Código").setAutoWidth(true);
        grid.addColumn(Articulo::getDescripcion).setHeader("Descripcion").setAutoWidth(true);

        // Clasificación ABC automática; el detalle (por qué) al pasar el mouse
        grid.addComponentColumn(this::chipAbc)
                .setHeader("ABC").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(this::ordenAbc));

        grid.addColumn(Articulo::getPrecioCompra).setHeader("Precio.Compra").setAutoWidth(true);
        grid.addColumn(Articulo::getPrecioVenta).setHeader("Precio.Venta").setAutoWidth(true);
        grid.addColumn(Articulo::getExistencia).setHeader("Existencia").setAutoWidth(true);
        grid.addColumn(Articulo::getUnidadDeVenta).setHeader("Unidad").setAutoWidth(true);

        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addComponentColumn(articulo -> {

            Button editar = new Button("Editar ", event -> {

                String key = NavigationContext.store(articulo);

                UI.getCurrent().navigate(RegistrarArticuloViewV4.class, key);

            });

            return new HorizontalLayout(editar);
        }).setHeader("Acciones");

        listaArt = articuloService.getLista();

        dataView = grid.setItems(listaArt);

    }

    private Span chipAbc(Articulo articulo) {
        ArticuloClasificacionAbc abc = clasificacion.get(articulo.getCodigo());
        if (abc == null) {
            Span sin = new Span("—");
            sin.getElement().setAttribute("title", "Sin clasificar: no se vende en el POS");
            return sin;
        }
        Span chip = ClasificacionAbcView.chip(abc.getClase());
        chip.getElement().setAttribute("title", ClasificacionAbcView.nombreClase(abc.getClase())
                + " · Valor " + abc.getClaseValor() + " / Rotación " + abc.getClaseRotacion()
                + " · " + EstiloConsulta.moneda(abc.getVentas()) + " en " + abc.getFacturas() + " facturas"
                + (Boolean.TRUE.equals(abc.getSinMovimiento()) ? " · SIN MOVIMIENTO" : ""));
        return chip;
    }

    private int ordenAbc(Articulo articulo) {
        ArticuloClasificacionAbc abc = clasificacion.get(articulo.getCodigo());
        int i = abc == null ? -1 : ORDEN_CLASES.indexOf(abc.getClase());
        return i < 0 ? ORDEN_CLASES.size() : i;
    }

    private void configurarFiltros() {

        txtFiltroArt.focus();
        txtFiltroArt.setPlaceholder("Buscar");
        txtFiltroArt.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtFiltroArt.setClearButtonVisible(true);
        txtFiltroArt.setValueChangeMode(ValueChangeMode.LAZY);
        txtFiltroArt.addValueChangeListener(e -> dataView.refreshAll());

        cbClaseAbc.setItems(ORDEN_CLASES);
        cbClaseAbc.setItemLabelGenerator(ClasificacionAbcView::nombreClase);
        cbClaseAbc.setPlaceholder("Clase ABC: todas");
        cbClaseAbc.setClearButtonVisible(true);
        cbClaseAbc.setWidth("220px");
        cbClaseAbc.addValueChangeListener(e -> dataView.refreshAll());

        // Un solo filtro: texto (nombre o código) y clase ABC
        dataView.setFilter(art -> {

            String clase = cbClaseAbc.getValue();
            if (clase != null) {
                ArticuloClasificacionAbc abc = clasificacion.get(art.getCodigo());
                if (abc == null || !clase.equals(abc.getClase())) {
                    return false;
                }
            }

            String searchTerm = txtFiltroArt.getValue().trim();

            if (searchTerm.isEmpty()) {
                return true;
            }

            return matchesTerm(art.getDescripcion(), searchTerm)
                    || matchesTerm(String.valueOf(art.getCodigo()), searchTerm);
        });
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value != null && value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
