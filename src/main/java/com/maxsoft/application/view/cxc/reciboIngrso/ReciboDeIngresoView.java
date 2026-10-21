/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.cxc.reciboIngrso;

import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import com.maxsoft.application.view.componente.ToolBarBotonera;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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

@Route(value = "cxc/reciboDeIngreso")
@PageTitle("Consulta Recibo de Ingreso")
public class ReciboDeIngresoView extends VerticalLayout {

    private final ReciboDeIngresoService reciboDeIngresoService;
    private final ProveedorService proveedorService;

    private final Grid<ReciboDeIngreso> grid = new Grid<>(ReciboDeIngreso.class, false);
    private final Grid<DetalleReciboDeIngreso> gridDetalle = new Grid<>(DetalleReciboDeIngreso.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Componentes de Filtro
    private final TextField txtBuscar = new TextField();
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("CLiente");
    private final ComboBox<String> cbEstado = new ComboBox<>("Estado");
    private final DatePicker dtDesde = new DatePicker("Fecha Desde");
    private final DatePicker dtHasta = new DatePicker("Fecha Hasta");

    private final Button btnFiltrar = new Button("Buscar", VaadinIcon.SEARCH.create());
    private final Button btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create());

    private final TabSheet tabs = new TabSheet();

    List<ReciboDeIngreso> listaOrdenes = new ArrayList<>();
    List<DetalleReciboDeIngreso> listDet = new ArrayList<>();

    private GridListDataView<ReciboDeIngreso> dataViewOrdenes;

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    // Columnas con pie (totales)
    private Grid.Column<ReciboDeIngreso> colNumero;
    private Grid.Column<ReciboDeIngreso> colSubtotal;
    private Grid.Column<ReciboDeIngreso> colItbis;
    private Grid.Column<ReciboDeIngreso> colTotal;

    private Grid.Column<DetalleReciboDeIngreso> colDescripcion;
    private Grid.Column<DetalleReciboDeIngreso> colDetSubtotal;
    private Grid.Column<DetalleReciboDeIngreso> colDetItbis;
    private Grid.Column<DetalleReciboDeIngreso> colDetTotal;

    public ReciboDeIngresoView(ReciboDeIngresoService reciboDeIngresoService,
            ProveedorService proveedorService) {
        this.reciboDeIngresoService = reciboDeIngresoService;
        this.proveedorService = proveedorService;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();

        tabs.add("Recibo de Ingreso", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(RegistroReciboDeIngresoView.class);

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

        add(
                botonera,
                new H2("Recibo de Ingreso"),
                crearBarraFiltros(),
                tabs);

        aplicarFiltros(); // Carga inicial
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private void configurarFiltros() {

        txtBuscar.setWidth("260px");
        txtBuscar.setPlaceholder("Filtrar por número u observación...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(s -> s.getRnc() != null ? s.getNombre() + " (" + s.getRnc() + ")" : s.getNombre());
        cbSuplidor.setPlaceholder("Todos los suplidores");
        cbSuplidor.setClearButtonVisible(true);

        cbEstado.setItems("TODOS", "PENDIENTE", "COMPLETADA", "ANULADA");
        cbEstado.setValue("TODOS");
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
                dtDesde, dtHasta, cbSuplidor, cbEstado, txtBuscar, btnFiltrar, btnLimpiar);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        // Permite que los filtros pasen a otra línea en pantallas angostas
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void limpiarFiltros() {

        txtBuscar.clear();
        cbSuplidor.clear();
        cbEstado.setValue("TODOS");
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
            String estadoSel = cbEstado.getValue();

            // Filtrado en memoria sobre el listado del servicio
            List<ReciboDeIngreso> resultado = reciboDeIngresoService.getLista().stream().filter(reciboIngreso -> {

                // Filtro Suplidor
                boolean coincideSuplidor = suplidorSel == null
                        || (reciboIngreso.getCliente()!= null
                        && reciboIngreso.getCliente().getCodigo().equals(suplidorSel.getCodigo()));

                // Filtro Estado
                boolean anulada = reciboIngreso.getAnulado() != null && reciboIngreso.getAnulado();
//                boolean completada = "SI".equalsIgnoreCase(orden.getCompletada());

                boolean coincideEstado = true;

//                if ("PENDIENTE".equals(estadoSel)) {
//                    coincideEstado = !anulada && !completada;
//                } else if ("COMPLETADA".equals(estadoSel)) {
//                    coincideEstado = completada && !anulada;
//                } else if ("ANULADA".equals(estadoSel)) {
//                    coincideEstado = anulada;
//                }
                // Filtro Rango Fechas
                boolean coincideFecha = dentroDeRango(aLocalDate(reciboIngreso.getFecha()));

                return coincideSuplidor && coincideEstado && coincideFecha;
            }).sorted(Comparator.comparing(
                            (ReciboDeIngreso o) -> aLocalDate(o.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            listaOrdenes.clear();
            listaOrdenes.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando órdenes: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewOrdenes == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewOrdenes.setFilter(o -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchNum = o.getCodigo()!= null && o.getCodigo().toString().toLowerCase().contains(term);
            boolean matchCom = o.getConcepto()!= null && o.getConcepto().toLowerCase().contains(term);
            return matchNum || matchCom;
        });

        actualizarTotalesOrdenes();
    }

    /**
     * Totales del pie sobre lo que se está viendo (excluye anuladas de los
     * montos, igual que el resumen original).
     */
    private void actualizarTotalesOrdenes() {

        List<ReciboDeIngreso> visibles = dataViewOrdenes.getItems().toList();

        List<ReciboDeIngreso> vigentes = visibles.stream()
                .filter(o -> o.getAnulado() == null || !o.getAnulado())
                .toList();

        colNumero.setFooter("TOTAL ÓRDENES: " + visibles.size());
        
        colSubtotal.setFooter(moneda(vigentes.stream()
                .map(o -> o.getTotalPagado()!= null ? o.getTotalPagado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        colItbis.setFooter(moneda(vigentes.stream()
                .map(o -> o.getBalancePendienteAntes()!= null ? o.getBalancePendienteAntes() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        
        colTotal.setFooter(moneda(vigentes.stream()
                .map(o -> o.getBalancePendienteDespues()!= null ? o.getBalancePendienteDespues() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private void cargarDetalle(ReciboDeIngreso recibo) {

        Collection<DetalleReciboDeIngreso> detalles = this.reciboDeIngresoService.getDetalle(recibo.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).setLabel("Detalle( Orden -> " + recibo.getCodigo() + " )");

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

        colDescripcion.setFooter("TOTAL ARTÍCULOS: " + listDet.size());
        
        colDetSubtotal.setFooter(moneda(listDet.stream()
                .map(d -> d.getTotal()!= null ? d.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        
        colDetItbis.setFooter(moneda(listDet.stream()
                .map(d -> d.getBalancePendienteAntes()!= null ? d.getBalancePendienteAntes() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        
        colDetTotal.setFooter(moneda(listDet.stream()
                
                .map(d -> d.getBalancePendienteDespues()!= null ? d.getBalancePendienteDespues() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT, GridVariant.LUMO_COLUMN_BORDERS);

        colNumero = grid.addColumn(ReciboDeIngreso::getCodigo)
                .setHeader("Número")
                .setKey("numero")
                .setFooter("TOTAL RECIBO:");

        grid.addColumn(o -> o.getCliente()!= null ? o.getCliente().getNombre() : "-")
                .setHeader("Cliente")
                .setKey("cliente")
                .setFlexGrow(2);

        grid.addColumn(o -> o.getFecha() != null ? df.format(o.getFecha()) : "-")
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (ReciboDeIngreso o) -> o.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // Columna Estado con Badge de Color
//        grid.addColumn(new ComponentRenderer<>(orden -> {
//            
//            Span badge = new Span(orden.getEstado() != null ? orden.getEstado() : "PENDIENTE");
//            String estado = orden.getEstado() != null ? orden.getEstado().toUpperCase() : "";
//
//            if (orden.getAnulada() != null && orden.getAnulada()) {
//                badge.setText("ANULADA");
//                badge.getElement().getThemeList().add("badge error");
//            } else if ("COMPLETADA".equals(estado) || "SI".equalsIgnoreCase(orden.getCompletada())) {
//                badge.setText("COMPLETADA");
//                badge.getElement().getThemeList().add("badge success");
//            } else {
//                badge.setText("PENDIENTE");
//                badge.getElement().getThemeList().add("badge warning");
//            }
//            return badge;
//        })).setHeader("Estado").setKey("estado");

        colSubtotal = grid.addColumn(o -> moneda(o.getTotalPagado()))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (ReciboDeIngreso o) -> o.getTotalPagado(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colItbis = grid.addColumn(o -> moneda(o.getBalancePendienteAntes()))
                .setHeader("BalancePendienteAntes")
                .setKey("bant")
                .setComparator(Comparator.comparing(
                        (ReciboDeIngreso o) -> o.getBalancePendienteAntes(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotal = grid.addColumn(o -> moneda(o.getBalancePendienteDespues()))
                .setHeader("BalancePendienteDespues")
                .setKey("bdespues")
                .setComparator(Comparator.comparing(
                        (ReciboDeIngreso o) -> o.getBalancePendienteDespues(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // Columna Botones de Acción
        grid.addColumn(new ComponentRenderer<>(orden -> {
            HorizontalLayout acciones = new HorizontalLayout();

            // Ver / Editar
            Button btnEditar = new Button(VaadinIcon.EYE.create(), e -> {
                getUI().ifPresent(ui -> ui.navigate("ordenes-compra/detalle/" + orden.getCodigo()));
            });
            btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnEditar.setTooltipText("Ver detalle u Orden");

            // Anular
            Button btnAnular = new Button(VaadinIcon.BAN.create(), e -> confirmarAnulacion(orden));
            btnAnular.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            btnAnular.setTooltipText("Anular Orden");
            btnAnular.setEnabled(orden.getAnulado() == null || !orden.getAnulado());

            acciones.add(btnEditar, btnAnular);
            return acciones;
        })).setHeader("Acciones").setKey("acciones").setSortable(false);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        dataViewOrdenes = grid.setItems(listaOrdenes);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

        // ASUNCIÓN: DetalleOrdenDeCompra expone estos getters (mismo patrón que
        // los demás detalles de inventario). Ajustar nombres si difieren.
        colDescripcion = gridDetalle.addColumn(DetalleReciboDeIngreso::getFactura)
                .setHeader("Factura")
                .setKey("factura")
                .setFooter("TOTAL:");
//
//        gridDetalle.addColumn(DetalleReciboDeEgreso::getTotal)
//                .setHeader("Unidad")
//                .setKey("unidad");
//
//        gridDetalle.addColumn(DetalleReciboDeEgreso::getBalancePendienteAntes)
//                .setHeader("BalancePendienteAntes")
//                .setKey("bant");
//
//        gridDetalle.addColumn(d -> moneda(d.getBalancePendienteDespues()))
//                .setHeader("BalancePendienteDespues")
//                .setKey("bdesp")
//                .setComparator(Comparator.comparing(
//                        (DetalleReciboDeEgreso d) -> d.getBalancePendienteDespues(),
//                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetSubtotal = gridDetalle.addColumn(d -> moneda(d.getTotal()))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (DetalleReciboDeIngreso d) -> d.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetItbis = gridDetalle.addColumn(d -> moneda(d.getBalancePendienteAntes()))
                .setHeader("BalancePendienteAntes")
                .setKey("apant")
                .setComparator(Comparator.comparing(
                        (DetalleReciboDeIngreso d) -> d.getBalancePendienteDespues(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetTotal = gridDetalle.addColumn(d -> moneda(d.getBalancePendienteDespues()))
                .setHeader("BalancePendienteDespues")
                .setKey("bpdes")
                .setComparator(Comparator.comparing(
                        (DetalleReciboDeIngreso d) -> d.getBalancePendienteDespues(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    // ============================================================
    // ANULACIÓN
    // ============================================================
    private void confirmarAnulacion(ReciboDeIngreso recibo) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Anular Orden de Compra");
        dialog.setText("¿Está seguro de que desea anular la orden de compra N° " + recibo.getNumero() + "? Esta acción no se puede deshacer.");

        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");

        dialog.setConfirmText("Sí, Anular");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                recibo.setAnulado(true);
//                recibo.setAnulado("ANULADA");
                reciboDeIngresoService.guardar(recibo); // O método de actualización correspondiente

                Notification.show("Orden de Compra anulada con éxito.", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                aplicarFiltros();
            } catch (Exception ex) {
                Notification.show("Error al anular la orden: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
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

    private String moneda(BigDecimal valor) {

        return String.format("RD$ %,.2f", valor != null ? valor : BigDecimal.ZERO);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}
