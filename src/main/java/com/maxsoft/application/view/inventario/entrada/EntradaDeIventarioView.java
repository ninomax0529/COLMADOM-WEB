
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.entrada;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Entrada de Inventario")
@Route(value = "inventario/entrada")

public class EntradaDeIventarioView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    private final EntradaDeInventarioService entradaService;
    private final Grid<EntradaInventario> grid = new Grid<>(EntradaInventario.class, false);
    private Grid<DetalleEntradaInventario> gridDetalle = new Grid<>(DetalleEntradaInventario.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Filtros de consulta
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final TextField txtBuscar = new TextField();
    private Button btnBuscar;
    private Button btnLimpiar;

    private final TabSheet tabs = new TabSheet();

    List<DetalleEntradaInventario> listDet = new ArrayList<>();
    List<EntradaInventario> listaEntInv = new ArrayList<>();

    private GridListDataView<EntradaInventario> dataViewEntradas;

    // Columnas con pie (totales)
    private Grid.Column<EntradaInventario> colCodigo;
    private Grid.Column<DetalleEntradaInventario> colDescripcion;
    private Grid.Column<DetalleEntradaInventario> colEntradaBase;
    private Grid.Column<DetalleEntradaInventario> colTotal;

    @Autowired
    public EntradaDeIventarioView(EntradaDeInventarioService entradaServiceArg) {

        setSizeFull();
        setSpacing(false);

        this.entradaService = entradaServiceArg;
        tabs.setSizeFull();

        tabs.add("Entrada", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(RegistroEntradaDeIventarioView.class);

        });

        grid.addItemDoubleClickListener(c -> {

            if (c.getItem() == null) {
                return;
            }

            cargarDetalle(c.getItem());
            tabs.setSelectedIndex(1);

        });

        configurarFiltros();
        configurarGrid();
        configurarGridDetalle();

        add(botonera, crearBarraFiltros(), tabs);

        buscar();

    }

    // ============================================================
    // FILTROS
    // ============================================================
    private void configurarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpDesde.setClearButtonVisible(true);

        dpHasta.setValue(LocalDate.now());
        dpHasta.setClearButtonVisible(true);

        txtBuscar.setWidth("320px");
        txtBuscar.setPlaceholder("Filtrar por código, comentario o usuario...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> {
        
                aplicarFiltroTexto();
        } );

        btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create(), e -> limpiarFiltros());
    }

    private HorizontalLayout crearBarraFiltros() {

        HorizontalLayout barra = new HorizontalLayout(dpDesde, dpHasta, txtBuscar, btnBuscar, btnLimpiar);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        return barra;
    }

    private void limpiarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        txtBuscar.clear();
        buscar();
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void buscar() {

        if (dpDesde.getValue() != null && dpHasta.getValue() != null
                && dpDesde.getValue().isAfter(dpHasta.getValue())) {
            mostrarNotificacion("La fecha 'Desde' no puede ser mayor que 'Hasta'.", NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            List<EntradaInventario> resultado = this.entradaService.getLista()
                    .stream()
                    .filter(e -> dentroDeRango(aLocalDate(e.getFecha())))
                    .sorted(Comparator.comparing(
                            (EntradaInventario e) -> aLocalDate(e.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaEntInv.clear();
            listaEntInv.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando entradas: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewEntradas == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewEntradas.setFilter(e -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchCod = e.getCodigo() != null && e.getCodigo().toString().contains(term);
            boolean matchCom = e.getComentario() != null && e.getComentario().toLowerCase().contains(term);
            boolean matchUsr = e.getNombreUsuario() != null && e.getNombreUsuario().toLowerCase().contains(term);
            return matchCod || matchCom || matchUsr;
        });

        colCodigo.setFooter("TOTAL ENTRADAS: " + dataViewEntradas.getItemCount());
    }

    private void cargarDetalle(EntradaInventario entrada) {

        List<DetalleEntradaInventario> detalles = this.entradaService.getDetalle(entrada.getCodigo());

        listDet = detalles != null ? detalles : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).setLabel("Detalle( Entrada -> " + entrada.getCodigo() + " )");

        actualizarTotalesDetalle();
    }

    private void limpiarDetalle() {

        listDet = new ArrayList<>();
        gridDetalle.setItems(listDet);
        tabs.getTabAt(1).setLabel("Detalle");
        tabs.setSelectedIndex(0);

        actualizarTotalesDetalle();
    }

    private void actualizarTotalesDetalle() {

        BigDecimal totalBase = listDet.stream()
                .map(this::cantidadBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGeneral = listDet.stream()
                .map(this::totalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        colDescripcion.setFooter("TOTAL ARTÍCULOS: " + listDet.size());
        colEntradaBase.setFooter(numero(totalBase));
        colTotal.setFooter(numero(totalGeneral));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        colCodigo = grid.addColumn(EntradaInventario::getCodigo).setHeader("Codigo")
                .setFooter("TOTAL ENTRADAS:");

        grid.addColumn(e -> {
            LocalDate f = aLocalDate(e.getFecha());
            return f != null ? f.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (EntradaInventario e) -> e.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(e -> e.getFechaCreacion() != null
                ? new Date(e.getFechaCreacion().getTime()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_HORA)
                : "")
                .setHeader("Fecha Creacion")
                .setKey("fechaCreacion")
                .setComparator(Comparator.comparing(
                        (EntradaInventario e) -> e.getFechaCreacion(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(EntradaInventario::getNombreUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        grid.addColumn(EntradaInventario::getComentario)
                .setHeader("Comentario")
                .setKey("comentario")
                .setFlexGrow(1);

        dataViewEntradas = grid.setItems(listaEntInv);
        grid.setSizeFull();

    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();
//
//        gridDetalle.addColumn(
//                DetalleEntradaInventario::getArticulo).setHeader("Articulo")
//                .setFooter("TOTAL ARTICULOS:");

        colDescripcion = gridDetalle.addColumn(DetalleEntradaInventario::getDescripcionArticulo)
                .setHeader("Descripcion")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(DetalleEntradaInventario::getCantidadRecibida)
                .setHeader("Cantidad")
                .setKey("cantidad");

        gridDetalle.addColumn(DetalleEntradaInventario::getExistenciaActual)
                .setHeader("Existencia")
                .setKey("existencia");

        gridDetalle.addColumn(DetalleEntradaInventario::getNuevaExistencia)
                .setHeader("Nueva Existencia")
                .setKey("nuevaexistencia");

        gridDetalle.addColumn(DetalleEntradaInventario::getNombreUnidad)
                .setHeader("Unidad")
                .setKey("unidad");

        // MULTI-EMPAQUE: factor y equivalente en unidad base (calculado aquí)
        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setKey("factor")
                .setComparator(Comparator.comparing(this::factorDe));

        colEntradaBase = gridDetalle.addColumn(d -> "+" + numero(cantidadBase(d)))
                .setHeader("Entrada (UMM)")
                .setKey("entradaBase")
                .setComparator(Comparator.comparing(this::cantidadBase));

        gridDetalle.addColumn(d -> numero(costoDe(d)))
                .setHeader("Costo Unit.")
                .setKey("costo")
                .setComparator(Comparator.comparing(this::costoDe));

        // Total calculado (cantidad x costo): no depende del campo "total" guardado
        colTotal = gridDetalle.addColumn(d -> numero(totalLinea(d)))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(this::totalLinea));

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private boolean dentroDeRango(LocalDate fecha) {

        if (fecha == null) {
            return dpDesde.getValue() == null && dpHasta.getValue() == null;
        }
        if (dpDesde.getValue() != null && fecha.isBefore(dpDesde.getValue())) {
            return false;
        }
        return dpHasta.getValue() == null || !fecha.isAfter(dpHasta.getValue());
    }

    private LocalDate aLocalDate(Date fecha) {

        if (fecha == null) {
            return null;
        }
        // new Date(getTime()) evita problemas si llega un java.sql.Date
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private BigDecimal factorDe(DetalleEntradaInventario d) {

        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleEntradaInventario d) {

        BigDecimal cant = d.getCantidadRecibida() != null ? d.getCantidadRecibida() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    private BigDecimal costoDe(DetalleEntradaInventario d) {

        if (d.getCostoUnitario() != null) {
            return d.getCostoUnitario();
        }
        return d.getPrecioCompra() != null ? d.getPrecioCompra() : BigDecimal.ZERO;
    }

    private BigDecimal totalLinea(DetalleEntradaInventario d) {

        BigDecimal cant = d.getCantidadRecibida() != null ? d.getCantidadRecibida() : BigDecimal.ZERO;
        return cant.multiply(costoDe(d));
    }

    private String numero(BigDecimal valor) {

        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }

}