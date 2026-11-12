package com.maxsoft.application.view.cxp.factura;

import com.maxsoft.application.modelo.DetalleFacturaDeCompra;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.cxp.FacturaDeCompraService;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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
    private final Div cabeceraDetalle = new Div();
    private final Div tarjetasDetalle = new Div();

    List<FacturaDeCompra> listaFacturas = new ArrayList<>();
    List<DetalleFacturaDeCompra> listDet = new ArrayList<>();

    private GridListDataView<FacturaDeCompra> dataViewFacturas;

    // Columnas con pie (totales)
    private Grid.Column<FacturaDeCompra> colCodigo;
    private Grid.Column<FacturaDeCompra> colSubTotalFactura;
    private Grid.Column<FacturaDeCompra> colItbisFactura;
    private Grid.Column<FacturaDeCompra> colTotalFactura;
    private Grid.Column<FacturaDeCompra> colTotalPendienteFactura;

    private Grid.Column<FacturaDeCompra> colTotalPagadoFactura;

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
        EstiloConsulta.aplicarVista(this, tabs);

        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.LIST, "Facturas de Compra"), grid);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle"),
                EstiloConsulta.panelDetalle(cabeceraDetalle, tarjetasDetalle, gridDetalle));

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
        setFlexGrow(1, tabs);

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
                dpDesde, dpHasta, cbProveedor, txtBuscar, btnBuscar, btnLimpiar,
                EstiloConsulta.ayuda("Doble clic en una factura para ver su detalle"));
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
            boolean matchNum = f.getNumeroFactura() != null && f.getNumeroFactura().toLowerCase().contains(term);
            boolean matchNcf = f.getNcf() != null && f.getNcf().toLowerCase().contains(term);
            boolean matchUsr = f.getNombreUsaurio() != null && f.getNombreUsaurio().toLowerCase().contains(term);
            return matchCod || matchNum || matchNcf || matchUsr;
        });

        actualizarTotalesFacturas();
    }

    /**
     * Totales del pie sobre lo que se está viendo.
     */
    private void actualizarTotalesFacturas() {

        List<FacturaDeCompra> todas = dataViewFacturas.getItems().toList();
        // Las anuladas no suman en los totales
        List<FacturaDeCompra> visibles = todas.stream().filter(f -> !esAnulada(f)).toList();

        colCodigo.setFooter("FACTURAS: " + todas.size());
        colSubTotalFactura.setFooter(moneda(visibles.stream()
                .map(f -> valor(f.getSubTotal())).reduce(BigDecimal.ZERO, BigDecimal::add)));
        colItbisFactura.setFooter(moneda(visibles.stream()
                .map(f -> valor(f.getTotalItbis())).reduce(BigDecimal.ZERO, BigDecimal::add)));

        colTotalFactura.setFooter(moneda(visibles.stream()
                .map(f -> valor(f.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add)));

        // (Antes sumaba el total, no el pendiente)
        colTotalPendienteFactura.setFooter(moneda(visibles.stream()
                .map(this::pendienteDe).reduce(BigDecimal.ZERO, BigDecimal::add)));
                
        colTotalPagadoFactura.setFooter(moneda(visibles.stream()
                .map(f -> valor(f.getTotalPagado())).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private void cargarDetalle(FacturaDeCompra factura) {

        Collection<DetalleFacturaDeCompra> detalles = this.facturaService.getDetalle(factura.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle · Factura #" + factura.getCodigo());

        LocalDate fecha = aLocalDate(factura.getFecha());
        String suplidor = factura.getProveedor() != null ? factura.getProveedor().getNombre() : factura.getNombreProveedor();
        EstiloConsulta.cabecera(cabeceraDetalle,
                "Factura de compra #" + factura.getCodigo()
                + (factura.getNumeroFactura() != null ? "  (N° " + factura.getNumeroFactura() + ")" : ""),
                (suplidor != null ? suplidor : "")
                + (fecha != null ? "  ·  " + fecha.format(FORMATO_FECHA) : "")
                + (factura.getNcf() != null ? "  ·  NCF " + factura.getNcf() : ""),
                chipEstado(factura), chipCondicion(factura));

        tarjetasDetalle.removeAll();
        tarjetasDetalle.add(
                EstiloConsulta.tarjeta("Total", factura.getTotal(), "fv-tarjeta-total", VaadinIcon.MONEY),
                EstiloConsulta.tarjeta("Pagado", factura.getTotalPagado(), "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE),
                EstiloConsulta.tarjetaPendiente("Pendiente", esAnulada(factura) ? BigDecimal.ZERO : pendienteDe(factura)));

        actualizarTotalesDetalle();
    }

    private void limpiarDetalle() {

        listDet = new ArrayList<>();
        gridDetalle.setItems(listDet);
        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle");
        tabs.setSelectedIndex(0);
        EstiloConsulta.cabeceraVacia(cabeceraDetalle,
                "Haga doble clic en una factura de la pestaña \"Facturas de Compra\" para ver su detalle.");
        tarjetasDetalle.removeAll();

        actualizarTotalesDetalle();
    }

    // ---------- Estado ----------
    private boolean esAnulada(FacturaDeCompra f) {
        return Boolean.TRUE.equals(f.getAnulada());
    }

    private BigDecimal pendienteDe(FacturaDeCompra f) {
        return valor(f.getTotalPendiente()).max(BigDecimal.ZERO);
    }

    private Span chipEstado(FacturaDeCompra f) {
        if (esAnulada(f)) {
            return EstiloConsulta.chipAnulada();
        }
        return EstiloConsulta.tieneSaldo(pendienteDe(f)) ? EstiloConsulta.chipPendiente() : EstiloConsulta.chipPagada();
    }

    private Span chipCondicion(FacturaDeCompra f) {
        // Condición de pago 1 = contado; el resto es a crédito
        boolean contado = f.getCondicionPago() != null && Integer.valueOf(1).equals(f.getCondicionPago().getCodigo());
        return contado ? EstiloConsulta.chipContado() : EstiloConsulta.chipCredito();
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

        EstiloConsulta.aplicarGrid(grid);
        grid.setPartNameGenerator(f -> esAnulada(f) ? "fv-fila-anulada" : null);

        colCodigo = grid.addColumn(FacturaDeCompra::getCodigo)
                .setHeader("Código")
                .setFooter("FACTURAS:");

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

        grid.addColumn(f -> f.getFecha() != null
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

        grid.addComponentColumn(this::chipCondicion)
                .setHeader("Condición")
                .setKey("condicion");

        grid.addComponentColumn(this::chipEstado)
                .setHeader("Estado")
                .setKey("estado");

        colSubTotalFactura = EstiloConsulta.monto(grid.addColumn(f -> moneda(f.getSubTotal())))
                .setHeader("SubTotal")
                .setKey("subtotal")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getSubTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colItbisFactura = EstiloConsulta.monto(grid.addColumn(f -> moneda(f.getTotalItbis())))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getTotalItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotalFactura = EstiloConsulta.monto(grid.addColumn(f -> moneda(f.getTotal())))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // (Antes "Total Pagado" mostraba el total a pagar, no lo pagado)
        colTotalPagadoFactura = EstiloConsulta.monto(grid.addColumn(f -> moneda(f.getTotalPagado())))
                .setHeader("Pagado")
                .setKey("totalpagado")
                .setComparator(Comparator.comparing(
                        (FacturaDeCompra f) -> f.getTotalPagado(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotalPendienteFactura = grid.addComponentColumn(f -> EstiloConsulta.pendiente(pendienteDe(f), esAnulada(f)))
                .setHeader("Pendiente")
                .setKey("balpendiente")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END)
                .setComparator(Comparator.comparing(this::pendienteDe));

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        dataViewFacturas = grid.setItems(listaFacturas);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        EstiloConsulta.aplicarGrid(gridDetalle);
        gridDetalle.setWidthFull();
        gridDetalle.setMinHeight("360px");
        gridDetalle.setHeight("55vh");

        colDescripcion = gridDetalle.addColumn(DetalleFacturaDeCompra::getDescripcionArticulo)
                .setHeader("Descripción")
                .setKey("descripcion")
                .setFooter("ARTÍCULOS:");

        // (Antes fallaba con NullPointerException si la línea no tenía empaque)
        gridDetalle.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : "")
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
        // Montos del detalle alineados (precio, descuento, subtotal, ITBIS, total)
        for (String k : new String[]{"precio", "descuento", "subTotal", "itbis", "total"}) {
            EstiloConsulta.monto(gridDetalle.getColumnByKey(k));
        }
        for (String k : new String[]{"cantidad", "factor", "entradaBase"}) {
            gridDetalle.getColumnByKey(k).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        }
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
