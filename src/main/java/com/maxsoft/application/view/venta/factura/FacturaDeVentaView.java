/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.factura;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Factura de Venta")
@Route(value = "venta/FacturaVenta")

public class FacturaDeVentaView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    private final FacturaDeVentaService facturaService;
    private final Grid<FacturaDeVenta> grid = new Grid<>(FacturaDeVenta.class, false);
    private Grid<DetalleFacturaDeVenta> gridDetalle = new Grid<>(DetalleFacturaDeVenta.class, false);

//    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);
    // Filtros de consulta
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final TextField txtBuscar = new TextField();
    private Button btnBuscar;
    private Button btnLimpiar;

    private final TabSheet tabs = new TabSheet();

    List<DetalleFacturaDeVenta> listDet = new ArrayList<>();

    List<FacturaDeVenta> listaFactura = new ArrayList<>();

    private GridListDataView<FacturaDeVenta> dataViewFacturas;

    // Columnas con pie (totales)
    private Grid.Column<FacturaDeVenta> colCodigo;
    private Grid.Column<FacturaDeVenta> colTotalFactura;
    private Grid.Column<DetalleFacturaDeVenta> colDescripcion;
    private Grid.Column<DetalleFacturaDeVenta> colEquivalente;
    private Grid.Column<DetalleFacturaDeVenta> colSubTotal;
    private Grid.Column<DetalleFacturaDeVenta> colItbis;
    private Grid.Column<DetalleFacturaDeVenta> colTotal;

    @Autowired
    public FacturaDeVentaView(FacturaDeVentaService entradaServiceArg) {

        setSizeFull();
        setSpacing(false);

        this.facturaService = entradaServiceArg;
        tabs.setSizeFull();

        tabs.add("Factura", grid);
        tabs.add("Detalle", gridDetalle);

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

        add(
                new H2("Facturas de Venta"),
                crearBarraFiltros(),
                tabs
        );

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
        txtBuscar.setPlaceholder("Filtrar por código, cliente o usuario...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());

        btnBuscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> buscar());
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create(), e -> limpiarFiltros());
    }

    private HorizontalLayout crearBarraFiltros() {

        HorizontalLayout barra = new HorizontalLayout(dpDesde, dpHasta, txtBuscar, btnBuscar, btnLimpiar);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        // Permite que los filtros pasen a otra línea en pantallas angostas
        barra.getStyle().set("flex-wrap", "wrap");
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
            List<FacturaDeVenta> resultado = this.facturaService.getLista()
                    .stream()
                    .filter(f -> dentroDeRango(aLocalDate(f.getFecha())))
                    .sorted(Comparator.comparing(
                            (FacturaDeVenta f) -> aLocalDate(f.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaFactura.clear();
            listaFactura.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando facturas: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewFacturas == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewFacturas.setFilter(f -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchCod = f.getCodigo() != null && f.getCodigo().toString().contains(term);
            boolean matchCli = f.getNombreCliente() != null && f.getNombreCliente().toLowerCase().contains(term);
            boolean matchUsr = f.getNombreUsuario() != null && f.getNombreUsuario().toLowerCase().contains(term);
            return matchCod || matchCli || matchUsr;
        });

        actualizarTotalesFacturas();
    }

    /**
     * Totales del pie sobre lo que se está viendo.
     */
    private void actualizarTotalesFacturas() {

        List<FacturaDeVenta> visibles = dataViewFacturas.getItems().toList();

        BigDecimal total = visibles.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        colCodigo.setFooter("TOTAL FACTURAS: " + visibles.size());
        colTotalFactura.setFooter(moneda(total));
    }

    private void cargarDetalle(FacturaDeVenta factura) {

        Collection<DetalleFacturaDeVenta> detalles = this.facturaService.getDetalle(factura.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).setLabel("Detalle( Factura -> " + factura.getCodigo() + " )");

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

        BigDecimal subTotal = listDet.stream()
                .map(d -> d.getSubTotal() != null ? d.getSubTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itbis = listDet.stream()
                .map(d -> d.getTotalItbis()!= null ? d.getTotalItbis() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = listDet.stream()
                .map(d -> d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        colDescripcion.setFooter("TOTAL ARTÍCULOS: " + listDet.size());
        colEquivalente.setFooter("-" + numero(totalBase));
        colSubTotal.setFooter(moneda(subTotal));
        colItbis.setFooter(moneda(itbis));
        colTotal.setFooter(moneda(total));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        colCodigo = grid.addColumn(FacturaDeVenta::getCodigo).setHeader("Codigo")
                .setFooter("TOTAL FACTURAS:");

        grid.addColumn(f -> {
            LocalDate fecha = aLocalDate(f.getFecha());
            return fecha != null ? fecha.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (FacturaDeVenta f) -> f.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(f -> f.getFechaCreacion() != null
                ? new Date(f.getFechaCreacion().getTime()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_HORA)
                : "")
                .setHeader("Fecha Creacion")
                .setKey("fechaCreacion")
                .setComparator(Comparator.comparing(
                        (FacturaDeVenta f) -> f.getFechaCreacion(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(FacturaDeVenta::getNombreCliente)
                .setHeader("Cliente")
                .setKey("cliente")
                .setFlexGrow(1);

        grid.addColumn(FacturaDeVenta::getNombreUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        colTotalFactura = grid.addColumn(f -> moneda(f.getTotal()))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (FacturaDeVenta f) -> f.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        dataViewFacturas = grid.setItems(listaFactura);
        grid.setSizeFull();

    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

//        gridDetalle.addColumn(DetalleFacturaDeVenta::getArticulo).setHeader("Codigo")
//                .setFooter("TOTAL:");
        colDescripcion = gridDetalle.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo)
                .setHeader("Descripcion")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getNombreUnidad)
                .setHeader("Unidad")
                .setKey("unidad");

        // MULTI-EMPAQUE: factor y descuento de inventario en unidad base (calculado aquí)
        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setKey("factor")
                .setComparator(Comparator.comparing(this::factorDe));

        colEquivalente = gridDetalle.addColumn(d -> "-" + numero(cantidadBase(d)))
                .setHeader("Salida (UMM)")
                .setKey("salidaBase")
                .setComparator(Comparator.comparing(this::cantidadBase));

        gridDetalle.addColumn(d -> moneda(d.getPrecioVenta()))
                .setHeader("Precio")
                .setKey("precio")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeVenta d) -> d.getPrecioVenta(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colSubTotal = gridDetalle.addColumn(d -> moneda(d.getSubTotal()))
                .setHeader("Sub Total")
                .setKey("subTotal")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeVenta d) -> d.getSubTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colItbis = gridDetalle.addColumn(d -> moneda(d.getTotalItbis()))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeVenta d) -> d.getTotalItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotal = gridDetalle.addColumn(d -> moneda(d.getTotal()))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeVenta d) -> d.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

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

    private BigDecimal factorDe(DetalleFacturaDeVenta d) {

        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleFacturaDeVenta d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    private String numero(BigDecimal valor) {

        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private String moneda(BigDecimal valor) {

        return "RD$ " + numero(valor);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }

}