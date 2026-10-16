/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.compra.recepcion;

import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.EstadoDocumento;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;
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
import com.vaadin.flow.component.html.Span;
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

@Route(value = "compra/consultaRecepcion")
@PageTitle("Consulta de Recepciones de Mercancía")
public class ConsultaRecepcionMercanciaView extends VerticalLayout {

    private final RecepcionMercanciaService recepcionService;
    private final ProveedorService proveedorService;

    private final Grid<RecepcionMercancia> grid = new Grid<>(RecepcionMercancia.class, false);
    private final Grid<DetalleRecepcionMercancia> gridDetalle = new Grid<>(DetalleRecepcionMercancia.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Componentes de Filtro
    private final TextField txtBuscar = new TextField();
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("Suplidor / Proveedor");
    private final ComboBox<String> cbTipoOrigen = new ComboBox<>("Origen");
    private final ComboBox<EstadoDocumento> cbEstado = new ComboBox<>("Estado");
    private final DatePicker dtDesde = new DatePicker("Fecha Desde");
    private final DatePicker dtHasta = new DatePicker("Fecha Hasta");

    private final Button btnFiltrar = new Button("Buscar", VaadinIcon.SEARCH.create());
    private final Button btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create());

    private final TabSheet tabs = new TabSheet();

    List<RecepcionMercancia> listaRecepciones = new ArrayList<>();
    List<DetalleRecepcionMercancia> listDet = new ArrayList<>();

    private GridListDataView<RecepcionMercancia> dataViewRecepciones;

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Columnas con pie (totales)
    private Grid.Column<RecepcionMercancia> colNumero;
    private Grid.Column<RecepcionMercancia> colSubtotal;
    private Grid.Column<RecepcionMercancia> colItbis;
    private Grid.Column<RecepcionMercancia> colTotal;

    private Grid.Column<DetalleRecepcionMercancia> colDescripcion;
    private Grid.Column<DetalleRecepcionMercancia> colDetSubtotal;
    private Grid.Column<DetalleRecepcionMercancia> colDetItbis;
    private Grid.Column<DetalleRecepcionMercancia> colDetTotal;

    public ConsultaRecepcionMercanciaView(RecepcionMercanciaService recepcionService,
            ProveedorService proveedorService) {
        this.recepcionService = recepcionService;
        this.proveedorService = proveedorService;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();

        tabs.add("Recepciones de Mercancía", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(RecepcionMercanciaView.class);

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

        add(botonera,
                new H2("Recepcion de Mercancia"),
                crearBarraFiltros(), tabs);

        aplicarFiltros(); // Carga inicial
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private void configurarFiltros() {

        txtBuscar.setWidth("260px");
        txtBuscar.setPlaceholder("Filtrar por N° recepción, origen u observación...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.addValueChangeListener(e -> aplicarFiltroTexto());

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(s -> s.getRnc() != null ? s.getNombre() + " (" + s.getRnc() + ")" : s.getNombre());
        cbSuplidor.setPlaceholder("Todos los suplidores");
        cbSuplidor.setClearButtonVisible(true);

        cbTipoOrigen.setItems("TODOS", "ORDEN_COMPRA", "FACTURA_DIRECTA", "OTRO");
        cbTipoOrigen.setValue("TODOS");
        cbTipoOrigen.setAllowCustomValue(false);

//        cbEstado.setItems(EstadoDocumento.values());
        cbEstado.setItemLabelGenerator(EstadoDocumento::getNombre);
        cbEstado.setClearButtonVisible(true);
        cbEstado.setPlaceholder("Todos");

        // Rango de fechas por defecto: Primer día del mes actual hasta la fecha actual
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
                dtDesde, dtHasta, cbSuplidor, cbTipoOrigen, cbEstado, txtBuscar, btnFiltrar, btnLimpiar);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        // Permite que los filtros pasen a otra línea en pantallas angostas
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void limpiarFiltros() {

        txtBuscar.clear();
        cbSuplidor.clear();
        cbTipoOrigen.setValue("TODOS");
        cbEstado.clear();
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
            String tipoOrigenSel = cbTipoOrigen.getValue();
            EstadoDocumento estadoSel = cbEstado.getValue();

            // Filtrado dinámico en memoria sobre el listado del servicio
            List<RecepcionMercancia> resultado = recepcionService.getLista().stream().filter(rec -> {

                // Filtro Suplidor
                boolean coincideSuplidor = suplidorSel == null
                        || (rec.getProveedor() != null
                        && rec.getProveedor().getCodigo().equals(suplidorSel.getCodigo()));

                // Filtro Tipo Origen
                boolean coincideOrigen = "TODOS".equalsIgnoreCase(tipoOrigenSel) || tipoOrigenSel == null
                        || (rec.getTipoDocumento() != null
                        && rec.getTipoDocumento().getNombre().equalsIgnoreCase(tipoOrigenSel));

                // Filtro Estado
                boolean coincideEstado = estadoSel == null || rec.getEstadoDocumento() == estadoSel;

                // Filtro Fechas
                boolean coincideFecha = dentroDeRango(aLocalDate(rec.getFecha()));

                return coincideSuplidor && coincideOrigen && coincideEstado && coincideFecha;
            })
                    .sorted(Comparator.comparing(
                            (RecepcionMercancia r) -> aLocalDate(r.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            listaRecepciones.clear();
            listaRecepciones.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando recepciones: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewRecepciones == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewRecepciones.setFilter(r -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchNum = r.getNumeroDocumento() != null && r.getNumeroDocumento().toLowerCase().contains(term);
            boolean matchTipo = r.getTipoDocumento() != null && r.getTipoDocumento().getNombre() != null
                    && r.getTipoDocumento().getNombre().toLowerCase().contains(term);
            boolean matchCom = r.getComentario() != null && r.getComentario().toLowerCase().contains(term);
            return matchNum || matchTipo || matchCom;
        });

        actualizarTotalesRecepciones();
    }

    /**
     * Totales del pie sobre lo que se está viendo (excluye anuladas de los
     * montos).
     */
    private void actualizarTotalesRecepciones() {

        List<RecepcionMercancia> visibles = dataViewRecepciones.getItems().toList();

        List<RecepcionMercancia> vigentes = visibles.stream()
                .filter(r -> !esAnulada(r))
                .toList();

        colNumero.setFooter("TOTAL RECEPCIONES: " + visibles.size());
        colSubtotal.setFooter(moneda(vigentes.stream()
                .map(r -> r.getSubtotal() != null ? r.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        colItbis.setFooter(moneda(vigentes.stream()
                .map(r -> r.getTotalItbis() != null ? r.getTotalItbis() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        colTotal.setFooter(moneda(vigentes.stream()
                .map(r -> r.getTotal() != null ? r.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private void cargarDetalle(RecepcionMercancia rec) {

        Collection<DetalleRecepcionMercancia> detalles = this.recepcionService.getDetalle(rec.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).setLabel("Detalle( Recepción -> " + rec.getNumeroDocumento() + " )");

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
                .map(d -> d.getSubTotal() != null ? d.getSubTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        colDetItbis.setFooter(moneda(listDet.stream()
                .map(d -> d.getItbis() != null ? d.getItbis() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        colDetTotal.setFooter(moneda(listDet.stream()
                .map(d -> d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT, GridVariant.LUMO_COLUMN_BORDERS);

        colNumero = grid.addColumn(RecepcionMercancia::getNumeroDocumento)
                .setHeader("N° Recepción")
                .setKey("numero")
                .setFooter("TOTAL RECEPCIONES:");

        grid.addColumn(r -> r.getTipoDocumento() != null ? r.getTipoDocumento().getNombre() : "-")
                .setHeader("Tipo Origen")
                .setKey("tipoOrigen");

//        grid.addColumn(r -> r.getOrigenNumero() != null ? r.getOrigenNumero() : "-").setHeader("N° Doc. Ref.");
        grid.addColumn(r -> r.getProveedor() != null ? r.getProveedor().getNombre() : "-")
                .setHeader("Suplidor")
                .setKey("suplidor")
                .setFlexGrow(2);

        grid.addColumn(r -> r.getFecha() != null ? df.format(r.getFecha()) : "-")
                .setHeader("Fecha Recepción")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (RecepcionMercancia r) -> r.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // Columna Estado usando la configuración del Enum EstadoDocumento
        grid.addColumn(new ComponentRenderer<>(rec -> {
            EstadoDocumento est = rec.getEstadoDocumento();
            if (est == null) {
                return new Span("-");
            }
            Span badge = new Span(est.getNombre());
            badge.getElement().getThemeList().add("badge " + est.getColorBadge());
            return badge;
        })).setHeader("Estado").setKey("estado");

        colSubtotal = grid.addColumn(r -> moneda(r.getSubtotal()))
                .setHeader("SubTotal")
                .setKey("subtotal")
                .setComparator(Comparator.comparing(
                        (RecepcionMercancia r) -> r.getSubtotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colItbis = grid.addColumn(r -> moneda(r.getTotalItbis()))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setComparator(Comparator.comparing(
                        (RecepcionMercancia r) -> r.getTotalItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotal = grid.addColumn(r -> moneda(r.getTotal()))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (RecepcionMercancia r) -> r.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // Columna de Acciones
        grid.addColumn(new ComponentRenderer<>(rec -> {
            HorizontalLayout acciones = new HorizontalLayout();

            // Ver Detalle
            Button btnVer = new Button(VaadinIcon.EYE.create(), e -> {
                getUI().ifPresent(ui -> ui.navigate("recepcion-mercancia/detalle/" + rec.getCodigo()));
            });
            btnVer.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnVer.setTooltipText("Ver Detalle / Documento");

            // Anular Recepción
            Button btnAnular = new Button(VaadinIcon.BAN.create(), e -> confirmarAnulacion(rec));
            btnAnular.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            btnAnular.setTooltipText("Anular Recepción");
//            btnAnular.setEnabled(rec.getEstadoDocumento()!= EstadoDocumento.ANULADA);

            acciones.add(btnVer, btnAnular);
            return acciones;
        })).setHeader("Acciones").setKey("acciones").setSortable(false);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        dataViewRecepciones = grid.setItems(listaRecepciones);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

        // ASUNCIÓN: DetalleRecepcionMercancia expone estos getters (mismo patrón
        // que los demás detalles). Ajustar nombres si difieren.
        colDescripcion = gridDetalle.addColumn(DetalleRecepcionMercancia::getDescripcionArticulo)
                .setHeader("Descripcion")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(DetalleRecepcionMercancia::getNombreUnidad)
                .setHeader("Unidad")
                .setKey("unidad");

        gridDetalle.addColumn(DetalleRecepcionMercancia::getCantidadRecibida)
                .setHeader("Cantidad Recibida")
                .setKey("cantidad");

        gridDetalle.addColumn(d -> moneda(d.getPrecioUnitario()))
                .setHeader("Precio Compra")
                .setKey("precio")
                .setComparator(Comparator.comparing(
                        (DetalleRecepcionMercancia d) -> d.getPrecioUnitario(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetSubtotal = gridDetalle.addColumn(d -> moneda(d.getSubTotal()))
                .setHeader("SubTotal")
                .setKey("subtotal")
                .setComparator(Comparator.comparing(
                        (DetalleRecepcionMercancia d) -> d.getSubTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetItbis = gridDetalle.addColumn(d -> moneda(d.getItbis()))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setComparator(Comparator.comparing(
                        (DetalleRecepcionMercancia d) -> d.getItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetTotal = gridDetalle.addColumn(d -> moneda(d.getTotal()))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (DetalleRecepcionMercancia d) -> d.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    // ============================================================
    // ANULACIÓN
    // ============================================================
    private void confirmarAnulacion(RecepcionMercancia rec) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Anular Recepción de Mercancía");
        dialog.setText("¿Está seguro de que desea anular la Recepción N° " + rec.getNumeroDocumento() + "? Esta acción reversará las entradas de inventario registradas.");

        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");

        dialog.setConfirmText("Sí, Anular");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {

            try {
//                rec.setEstado(EstadoDocumento.ANULADA);
                // Si tu servicio tiene lógica para revertir Kardex/Inventario al anular, la invocamos aquí
                recepcionService.procesarRecepcion(rec);

                Notification.show("Recepción de Mercancía anulada con éxito.", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                aplicarFiltros();
            } catch (Exception ex) {
                Notification.show("Error al anular la recepción: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    /**
     * Una recepción está anulada si su estado se llama ANULADA (por constante o
     * por nombre visible). Evita depender de que exista la constante.
     */
    private boolean esAnulada(RecepcionMercancia r) {

        EstadoDocumento e = r.getEstadoDocumento();
        if (e == null) {
            return false;
        }
        return "ANULADA".equalsIgnoreCase(e.getNombre())
                || (e.getNombre() != null && e.getNombre().toLowerCase().contains("anul"));
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

    private String moneda(BigDecimal valor) {

        return String.format("RD$ %,.2f", valor != null ? valor : BigDecimal.ZERO);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}
