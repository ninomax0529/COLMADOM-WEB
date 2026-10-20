

package com.maxsoft.application.view.cxp.factura;

import com.maxsoft.application.modelo.DetalleFacturaDeCompra;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.cxp.FacturaDeCompraService;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import jakarta.annotation.security.PermitAll;
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
import java.util.Objects;
import java.util.stream.Collectors;

@PermitAll
@PageTitle("Factura de Compra")
@Route(value = "cxp/facturaCompra")
public class FacturaDeCompraView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    private final FacturaDeCompraService facturaService;
    private final ProveedorService proveedorService;

    private final Grid<FacturaDeCompra> grid = new Grid<>(FacturaDeCompra.class, false);
    private final Grid<DetalleFacturaDeCompra> gridDetalle = new Grid<>(DetalleFacturaDeCompra.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Filtros de consulta
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final ComboBox<Proveedor> cbProveedor = new ComboBox<>("Suplidor / Proveedor");
    private final TextField txtBuscar = new TextField();
    private Button btnBuscar;
    private Button btnLimpiar;

    private final TabSheet tabs = new TabSheet();

    List<FacturaDeCompra> listaFacturas = new ArrayList<>();
    List<DetalleFacturaDeCompra> listDet = new ArrayList<>();

    private GridListDataView<FacturaDeCompra> dataViewFacturas;

    // Columnas con pie (totales)
    private Grid.Column<FacturaDeCompra> colCodigo;
    private Grid.Column<FacturaDeCompra> colSubTotalFactura;
    private Grid.Column<FacturaDeCompra> colItbisFactura;
    private Grid.Column<FacturaDeCompra> colTotalFactura;

    private Grid.Column<DetalleFacturaDeCompra> colDescripcion;
    private Grid.Column<DetalleFacturaDeCompra> colEntradaBase;
    private Grid.Column<DetalleFacturaDeCompra> colDescuento;
    private Grid.Column<DetalleFacturaDeCompra> colSubTotal;
    private Grid.Column<DetalleFacturaDeCompra> colItbis;
    private Grid.Column<DetalleFacturaDeCompra> colTotal;

    @Autowired
    public FacturaDeCompraView(FacturaDeCompraService facturaService,
            ProveedorService proveedorService) {

        this.facturaService = facturaService;
        this.proveedorService = proveedorService;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();

        tabs.add("Factura de Compra", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(RegistroFacturaDeCompraView.class);

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

        cargarProveedores();
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

        cbProveedor.setItemLabelGenerator(p -> p.getRnc() != null ? p.getNombre() + " (" + p.getRnc() + ")" : p.getNombre());
        cbProveedor.setClearButtonVisible(true);
        cbProveedor.setPlaceholder("Todos los suplidores");
        cbProveedor.setWidth("300px");

        txtBuscar.setWidth("300px");
        txtBuscar.setPlaceholder("Filtrar por código, N° factura, NCF o usuario...");
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

        HorizontalLayout barra = new HorizontalLayout(
                dpDesde, dpHasta, cbProveedor, txtBuscar, btnBuscar, btnLimpiar);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        // Permite que los filtros pasen a otra línea en pantallas angostas
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void cargarProveedores() {

        cbProveedor.setItems(proveedorService.getLista());
    }

    private void limpiarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        cbProveedor.clear();
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
            Proveedor proveedor = cbProveedor.getValue();

            List<FacturaDeCompra> resultado = this.facturaService.getLista()
                    .stream()
                    .filter(f -> dentroDeRango(aLocalDate(f.getFecha())))
                    .filter(f -> proveedor == null || (f.getProveedor() != null
                    && Objects.equals(f.getProveedor().getCodigo(), proveedor.getCodigo())))
                    .sorted(Comparator.comparing(
                            (FacturaDeCompra f) -> aLocalDate(f.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaFacturas.clear();
            listaFacturas.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando facturas de compra: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
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
            boolean matchNum = f.getNumeroFactura()!= null && f.getNumeroFactura().toLowerCase().contains(term);
            boolean matchNcf = f.getNcf() != null && f.getNcf().toLowerCase().contains(term);
            boolean matchUsr = f.getNombreUsaurio()!= null && f.getNombreUsaurio().toLowerCase().contains(term);
            return matchCod || matchNum || matchNcf || matchUsr;
        });

        actualizarTotalesFacturas();
    }

    /**
     * Totales del pie sobre lo que se está viendo.
     */
    private void actualizarTotalesFacturas() {

        List<FacturaDeCompra> visibles = dataViewFacturas.getItems().toList();

        colCodigo.setFooter("TOTAL FACTURAS: " + visibles.size());
        colSubTotalFactura.setFooter(moneda(visibles.stream()
                .map(f -> valor(f.getSubTotal())).reduce(BigDecimal.ZERO, BigDecimal::add)));
        colItbisFactura.setFooter(moneda(visibles.stream()
                .map(f -> valor(f.getTotalItbis())).reduce(BigDecimal.ZERO, BigDecimal::add)));
        colTotalFactura.setFooter(moneda(visibles.stream()
                .map(f -> valor(f.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private void cargarDetalle(FacturaDeCompra factura) {

        Collection<DetalleFacturaDeCompra> detalles = this.facturaService.getDetalle(factura.getCodigo());

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

        colDescripcion.setFooter("TOTAL ARTÍCULOS: " + listDet.size());
        colEntradaBase.setFooter("+" + numero(totalBase));
        colDescuento.setFooter(moneda(listDet.stream()
                .map(d -> valor(d.getTotalDescuento())).reduce(BigDecimal.ZERO, BigDecimal::add)));
        colSubTotal.setFooter(moneda(listDet.stream()
                .map(d -> valor(d.getSubTotal())).reduce(BigDecimal.ZERO, BigDecimal::add)));
        colItbis.setFooter(moneda(listDet.stream()
                .map(d -> valor(d.getTotalItbis())).reduce(BigDecimal.ZERO, BigDecimal::add)));
        colTotal.setFooter(moneda(listDet.stream()
                .map(d -> valor(d.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        colCodigo = grid.addColumn(FacturaDeCompra::getCodigo)
                .setHeader("Codigo")
                .setFooter("TOTAL FACTURAS:");

        grid.addColumn(FacturaDeCompra::getNumeroFactura)
                .setHeader("N° Factura")
                .setKey("numero");

        grid.addColumn(FacturaDeCompra::getNcf)
                .setHeader("NCF")
                .setKey("ncf");

        grid.addColumn(f -> f.getProveedor() != null ? f.getProveedor().getNombre() : "-")
                .setHeader("Suplidor")
                .setKey("suplidor")
                .setFlexGrow(2);

        grid.addColumn(f -> {
            LocalDate fecha = aLocalDate(f.getFecha());
            return fecha != null ? fecha.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(f -> f.getFecha()!= null
                ? new Date(f.getFecha().getTime()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_HORA)
                : "")
                .setHeader("Fecha Creacion")
                .setKey("fechaCreacion")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(FacturaDeCompra::getNombreUsaurio)
                .setHeader("Usuario")
                .setKey("usuario");

        colSubTotalFactura = grid.addColumn(f -> moneda(f.getSubTotal()))
                .setHeader("SubTotal")
                .setKey("subtotal")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getSubTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colItbisFactura = grid.addColumn(f -> moneda(f.getTotalItbis()))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getTotalItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotalFactura = grid.addColumn(f -> moneda(f.getTotal()))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        dataViewFacturas = grid.setItems(listaFacturas);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

        colDescripcion = gridDetalle.addColumn(DetalleFacturaDeCompra::getDescripcionArticulo)
                .setHeader("Descripcion")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : d.getArticuloEmpaque().getUnidadEmpaque())
                .setHeader("Empaque / Unidad")
                .setKey("unidad");

        gridDetalle.addColumn(d -> numero(d.getCantidad()))
                .setHeader("Cantidad")
                .setKey("cantidad")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeCompra d) -> d.getCantidad(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // MULTI-EMPAQUE: factor y entrada al inventario en unidad base (calculado aquí)
        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setKey("factor")
                .setComparator(Comparator.comparing(this::factorDe));

        colEntradaBase = gridDetalle.addColumn(d -> "+" + numero(cantidadBase(d)))
                .setHeader("Entrada (UMM)")
                .setKey("entradaBase")
                .setComparator(Comparator.comparing(this::cantidadBase));

        gridDetalle.addColumn(d -> moneda(d.getPrecioUnitario()))
                .setHeader("Precio Compra")
                .setKey("precio")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeCompra d) -> d.getPrecioUnitario(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        gridDetalle.addColumn(d -> numero(d.getPorcientoDescuento()) + " %")
                .setHeader("Desc. %")
                .setKey("porcDescuento");

        colDescuento = gridDetalle.addColumn(d -> moneda(d.getTotalDescuento()))
                .setHeader("Descuento")
                .setKey("descuento")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeCompra d) -> d.getTotalDescuento(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colSubTotal = gridDetalle.addColumn(d -> moneda(d.getSubTotal()))
                .setHeader("SubTotal")
                .setKey("subTotal")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeCompra d) -> d.getSubTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colItbis = gridDetalle.addColumn(d -> moneda(d.getTotalItbis()))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeCompra d) -> d.getTotalItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotal = gridDetalle.addColumn(d -> moneda(d.getTotal()))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (DetalleFacturaDeCompra d) -> d.getTotal(),
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

    private BigDecimal factorDe(DetalleFacturaDeCompra d) {

        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleFacturaDeCompra d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    private BigDecimal valor(BigDecimal v) {

        return v != null ? v : BigDecimal.ZERO;
    }

    private String numero(BigDecimal valor) {

        return FORMATO_NUMERO.format(valor(valor));
    }

    private String moneda(BigDecimal valor) {

        return "RD$ " + numero(valor);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}
