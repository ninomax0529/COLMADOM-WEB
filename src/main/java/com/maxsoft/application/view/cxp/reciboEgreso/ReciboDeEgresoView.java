/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.cxp.reciboEgreso;

import com.maxsoft.application.modelo.DetalleReciboDeEgreso;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.cxp.ReciboDeEgresoService;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Route(value = "cxp/reciboDeEgreso")
@PageTitle("Consulta Recibo de Egreso")
public class ReciboDeEgresoView extends VerticalLayout {

    private static final String TODOS = "TODOS";
    private static final String VIGENTE = "VIGENTE";
    private static final String ANULADO = "ANULADO";

    private final ReciboDeEgresoService reciboDeEgresoService;
    private final ProveedorService proveedorService;

    private final Grid<ReciboDeEgreso> grid = new Grid<>(ReciboDeEgreso.class, false);
    private final Grid<DetalleReciboDeEgreso> gridDetalle = new Grid<>(DetalleReciboDeEgreso.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Componentes de Filtro
    private final TextField txtBuscar = new TextField();
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("Suplidor / Proveedor");
    private final ComboBox<String> cbEstado = new ComboBox<>("Estado");
    private final DatePicker dtDesde = new DatePicker("Fecha Desde");
    private final DatePicker dtHasta = new DatePicker("Fecha Hasta");

    private final Button btnFiltrar = new Button("Buscar", VaadinIcon.SEARCH.create());
    private final Button btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create());

    private final TabSheet tabs = new TabSheet();
    private final Div cabeceraDetalle = new Div();
    private final Div tarjetasDetalle = new Div();

    List<ReciboDeEgreso> listaRecibos = new ArrayList<>();
    List<DetalleReciboDeEgreso> listDet = new ArrayList<>();

    private GridListDataView<ReciboDeEgreso> dataViewRecibos;

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    // Columnas con pie (totales)
    private Grid.Column<ReciboDeEgreso> colNumero;
    private Grid.Column<ReciboDeEgreso> colTotal;

    private Grid.Column<DetalleReciboDeEgreso> colDescripcion;
    private Grid.Column<DetalleReciboDeEgreso> colDetTotal;

    public ReciboDeEgresoView(ReciboDeEgresoService reciboDeEgresoService,
            ProveedorService proveedorService) {
        this.reciboDeEgresoService = reciboDeEgresoService;
        this.proveedorService = proveedorService;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();
        EstiloConsulta.aplicarVista(this, tabs);

        configurarFiltros();
        configurarGrid();
        configurarGridDetalle();

        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.LIST, "Recibos de Egreso"), grid);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle"),
                EstiloConsulta.panelDetalle(cabeceraDetalle, tarjetasDetalle, gridDetalle));

        botonera.getNuevo().addClickListener(e -> UI.getCurrent().navigate(RegistroReciboDeEgresoView.class));

        grid.addItemDoubleClickListener(c -> {
            if (c.getItem() != null) {
                verDetalle(c.getItem());
            }
        });

        H2 titulo = new H2("Recibos de Egreso");
        titulo.addClassName("fv-titulo");

        add(botonera, titulo, crearBarraFiltros(), tabs);
        setFlexGrow(1, tabs);

        aplicarFiltros(); // Carga inicial
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private void configurarFiltros() {

        txtBuscar.setWidth("260px");
        txtBuscar.setPlaceholder("Filtrar por número, suplidor o concepto...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(s -> s.getRnc() != null ? s.getNombre() + " (" + s.getRnc() + ")" : s.getNombre());
        cbSuplidor.setPlaceholder("Todos los suplidores");
        cbSuplidor.setClearButtonVisible(true);

        cbEstado.setItems(TODOS, VIGENTE, ANULADO);
        cbEstado.setValue(TODOS);
        cbEstado.setAllowCustomValue(false);

        // Rango por defecto (Primer día del mes actual hasta hoy)
        dtDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dtDesde.setClearButtonVisible(true);
        dtHasta.setValue(LocalDate.now());
        dtHasta.setClearButtonVisible(true);

        btnFiltrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnFiltrar.addClickShortcut(Key.ENTER);
        btnFiltrar.addClickListener(e -> aplicarFiltros());

        btnLimpiar.addClickListener(e -> limpiarFiltros());
    }

    private HorizontalLayout crearBarraFiltros() {

        HorizontalLayout barra = new HorizontalLayout(
                dtDesde, dtHasta, cbSuplidor, cbEstado, txtBuscar, btnFiltrar, btnLimpiar,
                EstiloConsulta.ayuda("Doble clic en un recibo para ver su detalle"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        // Permite que los filtros pasen a otra línea en pantallas angostas
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void limpiarFiltros() {

        txtBuscar.clear();
        cbSuplidor.clear();
        cbEstado.setValue(TODOS);
        dtDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dtHasta.setValue(LocalDate.now());
        aplicarFiltros();
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void aplicarFiltros() {

        if (dtDesde.getValue() != null && dtHasta.getValue() != null
                && dtDesde.getValue().isAfter(dtHasta.getValue())) {
            mostrarNotificacion("La fecha 'Desde' no puede ser mayor que 'Hasta'.", NotificationVariant.LUMO_WARNING);
            return;
        }

        try {

            Proveedor suplidorSel = cbSuplidor.getValue();
            String estadoSel = cbEstado.getValue() != null ? cbEstado.getValue() : TODOS;

            List<ReciboDeEgreso> resultado = reciboDeEgresoService.getLista().stream().filter(r -> {

                boolean coincideSuplidor = suplidorSel == null
                        || (r.getProveedor() != null
                        && r.getProveedor().getCodigo().equals(suplidorSel.getCodigo()));

                // (Antes el filtro de estado no hacía nada)
                boolean coincideEstado = switch (estadoSel) {
                    case VIGENTE -> !esAnulado(r);
                    case ANULADO -> esAnulado(r);
                    default -> true;
                };

                return coincideSuplidor && coincideEstado && dentroDeRango(aLocalDate(r.getFecha()));
            })
                    .sorted(Comparator.comparing(
                            (ReciboDeEgreso o) -> aLocalDate(o.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            listaRecibos.clear();
            listaRecibos.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando recibos: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewRecibos == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewRecibos.setFilter(o -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchNum = o.getCodigo() != null && o.getCodigo().toString().contains(term);
            boolean matchCom = o.getConcepto() != null && o.getConcepto().toLowerCase().contains(term);
            boolean matchSup = nombreSuplidor(o).toLowerCase().contains(term);
            return matchNum || matchCom || matchSup;
        });

        actualizarTotales();
    }

    /** Totales del pie sobre lo visible (los anulados no suman). */
    private void actualizarTotales() {

        List<ReciboDeEgreso> visibles = dataViewRecibos.getItems().toList();

        colNumero.setFooter("RECIBOS: " + visibles.size());
        colTotal.setFooter(EstiloConsulta.moneda(visibles.stream()
                .filter(o -> !esAnulado(o))
                .map(o -> o.getTotalPagado() != null ? o.getTotalPagado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private void verDetalle(ReciboDeEgreso recibo) {
        cargarDetalle(recibo);
        tabs.setSelectedIndex(1);
    }

    private void cargarDetalle(ReciboDeEgreso recibo) {

        Collection<DetalleReciboDeEgreso> detalles = this.reciboDeEgresoService.getDetalle(recibo.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle · Recibo #" + recibo.getCodigo());

        EstiloConsulta.cabecera(cabeceraDetalle,
                "Recibo de egreso #" + recibo.getCodigo(),
                nombreSuplidor(recibo)
                + (recibo.getFecha() != null ? "  ·  " + df.format(recibo.getFecha()) : "")
                + (recibo.getConcepto() != null && !recibo.getConcepto().isBlank() ? "  ·  " + recibo.getConcepto() : ""),
                chipEstado(recibo), chipOrigen(recibo));

        BigDecimal antes = listDet.stream().map(d -> valor(d.getBalancePendienteAntes())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal despues = listDet.stream().map(d -> valor(d.getBalancePendienteDespues())).reduce(BigDecimal.ZERO, BigDecimal::add);

        tarjetasDetalle.removeAll();
        tarjetasDetalle.add(
                EstiloConsulta.tarjeta("Total pagado", recibo.getTotalPagado(), "fv-tarjeta-pagado", VaadinIcon.CHECK_CIRCLE),
                EstiloConsulta.tarjeta("Debía antes", antes, "fv-tarjeta-total", VaadinIcon.MONEY),
                EstiloConsulta.tarjetaPendiente("Queda pendiente", despues));

        actualizarTotalesDetalle();
    }

    private void limpiarDetalle() {

        listDet = new ArrayList<>();
        gridDetalle.setItems(listDet);
        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle");
        tabs.setSelectedIndex(0);
        EstiloConsulta.cabeceraVacia(cabeceraDetalle,
                "Haga doble clic en un recibo de la pestaña \"Recibos de Egreso\" para ver su detalle.");
        tarjetasDetalle.removeAll();

        actualizarTotalesDetalle();
    }

    private void actualizarTotalesDetalle() {

        colDescripcion.setFooter("FACTURAS: " + listDet.size());
        colDetTotal.setFooter(EstiloConsulta.moneda(listDet.stream()
                .map(d -> valor(d.getTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        EstiloConsulta.aplicarGrid(grid);
        grid.setPartNameGenerator(r -> esAnulado(r) ? "fv-fila-anulada" : null);

        colNumero = grid.addColumn(ReciboDeEgreso::getCodigo)
                .setHeader("Número")
                .setKey("numero")
                .setFooter("RECIBOS:");

        grid.addColumn(this::nombreSuplidor)
                .setHeader("Proveedor")
                .setKey("proveedor")
                .setFlexGrow(2);

        grid.addColumn(o -> o.getFecha() != null ? df.format(o.getFecha()) : "-")
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (ReciboDeEgreso o) -> o.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(o -> o.getConcepto() != null ? o.getConcepto() : "")
                .setHeader("Concepto")
                .setKey("concepto")
                .setFlexGrow(2);

        grid.addComponentColumn(this::chipOrigen)
                .setHeader("Pago")
                .setKey("origen");

        grid.addComponentColumn(this::chipEstado)
                .setHeader("Estado")
                .setKey("estado");

        colTotal = EstiloConsulta.monto(grid.addColumn(o -> EstiloConsulta.moneda(o.getTotalPagado())))
                .setHeader("Total pagado")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (ReciboDeEgreso o) -> o.getTotalPagado(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // Acciones
        grid.addColumn(new ComponentRenderer<>(recibo -> {
            HorizontalLayout acciones = new HorizontalLayout();

            // (Antes navegaba a una ruta inexistente)
            Button btnVer = new Button(VaadinIcon.EYE.create(), e -> verDetalle(recibo));
            btnVer.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnVer.setTooltipText("Ver detalle del recibo");

            Button btnAnular = new Button(VaadinIcon.BAN.create(), e -> confirmarAnulacion(recibo));
            btnAnular.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            btnAnular.setTooltipText("Anular recibo");
            btnAnular.setEnabled(!esAnulado(recibo));

            acciones.add(btnVer, btnAnular);
            return acciones;
        })).setHeader("Acciones").setKey("acciones").setSortable(false);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        dataViewRecibos = grid.setItems(listaRecibos);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        EstiloConsulta.aplicarGrid(gridDetalle);
        gridDetalle.setWidthFull();
        gridDetalle.setMinHeight("300px");
        gridDetalle.setHeight("50vh");

        // (Antes mostraba el objeto factura completo como texto)
        colDescripcion = gridDetalle.addColumn(d -> {
            FacturaDeCompra f = d.getFactura();
            if (f == null) {
                return "";
            }
            return "#" + f.getCodigo() + (f.getNumeroFactura() != null ? "  (N° " + f.getNumeroFactura() + ")" : "");
        })
                .setHeader("Factura de compra")
                .setKey("factura")
                .setFooter("FACTURAS:");

        EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.getBalancePendienteAntes())))
                .setHeader("Debía antes")
                .setKey("bant")
                .setComparator(Comparator.comparing(
                        (DetalleReciboDeEgreso d) -> valor(d.getBalancePendienteAntes())));

        colDetTotal = EstiloConsulta.monto(gridDetalle.addColumn(d -> EstiloConsulta.moneda(d.getTotal())))
                .setHeader("Pagado")
                .setKey("total")
                .setComparator(Comparator.comparing((DetalleReciboDeEgreso d) -> valor(d.getTotal())));

        gridDetalle.addComponentColumn(d -> EstiloConsulta.pendiente(d.getBalancePendienteDespues(), false))
                .setHeader("Queda pendiente")
                .setKey("bpdes")
                .setTextAlign(ColumnTextAlign.END)
                .setComparator(Comparator.comparing((DetalleReciboDeEgreso d) -> valor(d.getBalancePendienteDespues())));

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    // ============================================================
    // ANULACIÓN
    // ============================================================
    /**
     * Anula con el servicio completo: devuelve a cada factura lo que el recibo
     * le había pagado y, si salió de la caja, reingresa el efectivo. Antes
     * solo marcaba el recibo como anulado y las facturas quedaban como pagadas.
     */
    private void confirmarAnulacion(ReciboDeEgreso recibo) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Anular recibo de egreso #" + recibo.getCodigo());
        dialog.setWidth("460px");

        Span texto = new Span("Se devolverá a cada factura lo que este recibo le pagó (RD$ "
                + String.format("%,.2f", valor(recibo.getTotalPagado())) + ")"
                + (recibo.getCajaChica() != null ? " y el efectivo regresará a la caja abierta." : ".")
                + " Esta acción no se puede deshacer.");
        TextArea motivo = new TextArea("Motivo de la anulación");
        motivo.setWidthFull();
        motivo.setRequired(true);
        dialog.add(new VerticalLayout(texto, motivo));

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        Button anular = new Button("Sí, anular", VaadinIcon.BAN.create(), e -> {
            if (motivo.isEmpty() || motivo.getValue().isBlank()) {
                motivo.setInvalid(true);
                motivo.setErrorMessage("Indique el motivo");
                return;
            }
            try {
                reciboDeEgresoService.anularReciboDeEgreso(recibo.getCodigo(), motivo.getValue().trim(), "Admin");
                Notification.show("Recibo #" + recibo.getCodigo() + " anulado.", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                aplicarFiltros();
            } catch (Exception ex) {
                Notification.show("No se pudo anular el recibo: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        anular.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(cancelar, anular);
        dialog.open();
        motivo.focus();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private boolean esAnulado(ReciboDeEgreso r) {
        return Boolean.TRUE.equals(r.getAnulado());
    }

    private Span chipEstado(ReciboDeEgreso r) {
        return esAnulado(r) ? EstiloConsulta.chipAnulada() : EstiloConsulta.chip("VIGENTE", "fv-chip-pagada");
    }

    /** Efectivo (salió de la caja) u otra forma (cheque/transferencia). */
    private Span chipOrigen(ReciboDeEgreso r) {
        return r.getCajaChica() != null
                ? EstiloConsulta.chip("EFECTIVO", "fv-chip-contado")
                : EstiloConsulta.chip("CHEQUE / TRANSF.", "fv-chip-delivery");
    }

    private String nombreSuplidor(ReciboDeEgreso r) {
        if (r.getProveedor() != null) {
            return r.getProveedor().getNombre();
        }
        return r.getNombreProveedor() != null ? r.getNombreProveedor() : "-";
    }

    private BigDecimal valor(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private boolean dentroDeRango(LocalDate fecha) {

        if (fecha == null) {
            return dtDesde.getValue() == null && dtHasta.getValue() == null;
        }
        if (dtDesde.getValue() != null && fecha.isBefore(dtDesde.getValue())) {
            return false;
        }
        return dtHasta.getValue() == null || !fecha.isAfter(dtHasta.getValue());
    }

    private LocalDate aLocalDate(Date fecha) {

        if (fecha == null) {
            return null;
        }
        // new Date(getTime()) evita problemas si llega un java.sql.Date
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}
