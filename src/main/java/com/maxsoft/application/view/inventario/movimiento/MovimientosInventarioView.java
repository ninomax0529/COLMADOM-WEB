/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.movimiento;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.util.ClaseUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "consulta-movimientos-inventario")
@PageTitle("Consulta de Movimientos de Inventario | MaxSoft")
public class MovimientosInventarioView extends VerticalLayout {

    private final MovimientoInventarioService movimientoService;
    private final ArticuloService articuloService;

    // Componentes de Filtro
    private DatePicker dpFechaInicio;
    private DatePicker dpFechaFin;
    private ComboBox<Articulo> cbArticulo;
    private ComboBox<String> cbTipoMovimiento;
    private Button btnBuscar;
    private Button btnLimpiar;

    // Grid de datos
    private Grid<MovimientoInventario> grid;

    @Autowired
    public MovimientosInventarioView(MovimientoInventarioService movimientoService,
            ArticuloService articuloService) {
        this.movimientoService = movimientoService;
        this.articuloService = articuloService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        crearFiltros();
        crearGrid();

        add(crearLayoutFiltros(), grid);

        // Cargar movimientos del mes actual al iniciar
        filtrarMovimientos();
    }

    private void crearFiltros() {

        dpFechaInicio = new DatePicker("Fecha Inicio");
        dpFechaInicio.setValue(LocalDate.now().withDayOfMonth(1)); // Primer día del mes actual

        dpFechaFin = new DatePicker("Fecha Fin");
        dpFechaFin.setValue(LocalDate.now());

        cbArticulo = new ComboBox<>("Artículo");
        cbArticulo.setItemLabelGenerator(a -> a.getCodigo() + " - " + a.getDescripcion());
        cbArticulo.setItems(articuloService.getLista());
        cbArticulo.setClearButtonVisible(true);
        cbArticulo.setWidth("300px");

        cbTipoMovimiento = new ComboBox<>("Tipo Movimiento");
        cbTipoMovimiento.setItems("TODOS", "ENTRADA", "SALIDA", "AJUSTE_INCREMENTO", "AJUSTE_DECREMENTO");
        cbTipoMovimiento.setValue("TODOS");
        cbTipoMovimiento.setClearButtonVisible(false);

        btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickListener(e -> filtrarMovimientos());

        btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create());
        btnLimpiar.addClickListener(e -> limpiarFiltros());
    }

    private HorizontalLayout crearLayoutFiltros() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.END);
        layout.setSpacing(true);

        layout.add(dpFechaInicio, dpFechaFin, cbArticulo, cbTipoMovimiento, btnBuscar, btnLimpiar);
        return layout;
    }

    private void crearGrid() {
        grid = new Grid<>(MovimientoInventario.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        grid.addColumn(m -> m.getFechaMovimiento() != null ? m.getFechaMovimiento() : "")
                .setHeader("Fecha y Hora")
                .setAutoWidth(true);

        grid.addColumn(m -> m.getArticulo() != null ? m.getArticulo().getCodigo() : "")
                .setHeader("Código")
                .setWidth("90px")
                .setFlexGrow(0);

        grid.addColumn(m -> m.getArticulo() != null ? m.getArticulo().getDescripcion() : "")
                .setHeader("Artículo")
                .setAutoWidth(true)
                .setFlexGrow(1);
          grid.addColumn(m -> m.getAlmacen()!= null ? m.getAlmacen().getNombre(): "")
                .setHeader("Almacen")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Columna de Tipo con Badges de colores
        grid.addComponentColumn(this::crearBadgeTipoMovimiento)
                .setHeader("Tipo Movimiento")
                .setAutoWidth(true);

        grid.addColumn(MovimientoInventario::getTipoDocumento)
                .setHeader("Documento")
                .setAutoWidth(true);

        grid.addColumn(MovimientoInventario::getNumeroDocumento)
                .setHeader("N° Doc.")
                .setAutoWidth(true);

        grid.addColumn(m -> String.format("%.2f", m.getExistenciaAnterior()))
                .setHeader("Exist. Anterior")
                .setAutoWidth(true);

        grid.addColumn(m -> String.format("%.2f", m.getCantidad()))
                .setHeader("Cantidad")
                .setAutoWidth(true);

        grid.addColumn(m -> String.format("%.2f", m.getExistenciaNueva()))
                .setHeader("Exist. Nueva")
                .setAutoWidth(true);

        grid.addColumn(MovimientoInventario::getUsuario)
                .setHeader("Usuario")
                .setAutoWidth(true);

        grid.addColumn(MovimientoInventario::getObservacion)
                .setHeader("Observación")
                .setAutoWidth(true);
    }

    private Span crearBadgeTipoMovimiento(MovimientoInventario mov) {
        Span badge = new Span(mov.getTipoMovimiento().getNombre());
        badge.getElement().getThemeList().add("badge");

        if ("ENTRADA".equals(mov.getTipoMovimiento()) || "AJUSTE_INCREMENTO".equals(mov.getTipoMovimiento())) {
            badge.getElement().getThemeList().add("success");
        } else if ("SALIDA".equals(mov.getTipoMovimiento()) || "AJUSTE_DECREMENTO".equals(mov.getTipoMovimiento())) {
            badge.getElement().getThemeList().add("error");
        } else {
            badge.getElement().getThemeList().add("contrast");
        }

        return badge;
    }

    private void filtrarMovimientos() {
        LocalDate fInicio = dpFechaInicio.getValue() != null ? dpFechaInicio.getValue() : LocalDate.now().minusMonths(1);
        LocalDate fFin = dpFechaFin.getValue() != null ? dpFechaFin.getValue() : LocalDate.now();

        LocalDateTime inicioDt = fInicio.atStartOfDay();
        LocalDateTime finDt = fFin.atTime(LocalTime.MAX);

        Date fechaIni = ClaseUtil.asDate(fInicio);
        Date fechaFin = ClaseUtil.asDate(fFin);

        List<MovimientoInventario> lista;

//         1. Filtrar por fechas desde la base de datos
        if (cbArticulo.getValue() != null) {

            lista = movimientoService.getMovimientosPorArticulo(cbArticulo.getValue().getCodigo()).stream()
                    .filter(m -> {
                        return !m.getFechaMovimiento().before(fechaIni) && !m.getFechaMovimiento().after(fechaFin);
                    })
                    .collect(Collectors.toList());
        } else {
            lista = movimientoService.getMovimientosPorFechas(inicioDt, finDt);
        }

        // 2. Filtrar por Tipo de Movimiento en memoria
        String tipoSel = cbTipoMovimiento.getValue();
        if (tipoSel != null && !"TODOS".equals(tipoSel)) {
            lista = lista.stream()
                    .filter(m -> tipoSel.equalsIgnoreCase(m.getTipoMovimiento().getNombre()))
                    .collect(Collectors.toList());
        }

        grid.setItems(lista);
    }

    private void limpiarFiltros() {
        dpFechaInicio.setValue(LocalDate.now().withDayOfMonth(1));
        dpFechaFin.setValue(LocalDate.now());
        cbArticulo.clear();
        cbTipoMovimiento.setValue("TODOS");
        filtrarMovimientos();
    }
}
