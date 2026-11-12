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
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.html.Div;

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
    private final ComboBox<String> cbEstado = new ComboBox<>("Estado");
    private static final String TODOS = "TODOS";
    private static final String VIGENTE = "VIGENTE";
    private static final String ANULADA = "ANULADA";
    private final DatePicker dtDesde = new DatePicker("Fecha Desde");
    private final DatePicker dtHasta = new DatePicker("Fecha Hasta");

    private final Button btnFiltrar = new Button("Buscar", VaadinIcon.SEARCH.create());
    private final Button btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create());

    private final TabSheet tabs = new TabSheet();
    private final Div cabeceraDetalle = new Div();
    private final Div tarjetasDetalle = new Div();

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
        EstiloConsulta.aplicarVista(this, tabs);

        configurarFiltros();
        configurarGrid();
        configurarGridDetalle();

        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.LIST, "Recepciones de Mercancía"), grid);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.FILE_TEXT_O, "Detalle"),
                EstiloConsulta.panelDetalle(cabeceraDetalle, tarjetasDetalle, gridDetalle));

        botonera.getNuevo().addClickListener(e -> UI.getCurrent().navigate(RecepcionMercanciaView.class));

        grid.addItemDoubleClickListener(c -> {
            if (c.getItem() != null) {
                verDetalle(c.getItem());
            }
        });

        H2 titulo = new H2("Recepciones de Mercancía");
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

        // (Antes el combo de estado estaba vacío y el filtro no hacía nada)
        cbEstado.setItems(TODOS, VIGENTE, ANULADA);
        cbEstado.setValue(TODOS);
        cbEstado.setAllowCustomValue(false);

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
                dtDesde, dtHasta, cbSuplidor, cbTipoOrigen, cbEstado, txtBuscar, btnFiltrar, btnLimpiar,
                EstiloConsulta.ayuda("Doble clic en una recepción para ver su detalle"));
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
            String tipoOrigenSel = cbTipoOrigen.getValue();
            String estadoSel = cbEstado.getValue() != null ? cbEstado.getValue() : TODOS;

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
                boolean coincideEstado = switch (estadoSel) {
                    case VIGENTE -> !esAnulada(rec);
                    case ANULADA -> esAnulada(rec);
                    default -> true;
                };

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
            boolean matchSup = r.getProveedor() != null && r.getProveedor().getNombre() != null
                    && r.getProveedor().getNombre().toLowerCase().contains(term);
            return matchNum || matchTipo || matchCom || matchSup;
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

        colNumero.setFooter("RECEPCIONES: " + visibles.size());
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

    private void verDetalle(RecepcionMercancia rec) {
        cargarDetalle(rec);
        tabs.setSelectedIndex(1);
    }

    private void cargarDetalle(RecepcionMercancia rec) {

        Collection<DetalleRecepcionMercancia> detalles = this.recepcionService.getDetalle(rec.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        String numero = rec.getNumeroDocumento() != null ? rec.getNumeroDocumento() : String.valueOf(rec.getCodigo());
        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle · Recepción " + numero);

        EstiloConsulta.cabecera(cabeceraDetalle,
                "Recepción " + numero,
                (rec.getProveedor() != null ? rec.getProveedor().getNombre() : "-")
                + (rec.getFecha() != null ? "  ·  " + df.format(rec.getFecha()) : "")
                + (rec.getTipoDocumento() != null ? "  ·  " + rec.getTipoDocumento().getNombre() : "")
                + (rec.getComentario() != null && !rec.getComentario().isBlank() ? "  ·  " + rec.getComentario() : ""),
                chipEstado(rec));

        tarjetasDetalle.removeAll();
        tarjetasDetalle.add(
                EstiloConsulta.tarjeta("SubTotal", rec.getSubtotal(), "fv-tarjeta-total", VaadinIcon.CART),
                EstiloConsulta.tarjeta("ITBIS", rec.getTotalItbis(), "fv-tarjeta-total", VaadinIcon.INVOICE),
                EstiloConsulta.tarjeta("Total", rec.getTotal(), "fv-tarjeta-pagado", VaadinIcon.MONEY));

        actualizarTotalesDetalle();
    }

    private void limpiarDetalle() {

        listDet = new ArrayList<>();
        gridDetalle.setItems(listDet);
        EstiloConsulta.renombrarTab(tabs, 1, VaadinIcon.FILE_TEXT_O, "Detalle");
        tabs.setSelectedIndex(0);
        EstiloConsulta.cabeceraVacia(cabeceraDetalle,
                "Haga doble clic en una recepción de la pestaña \"Recepciones de Mercancía\" para ver su detalle.");
        tarjetasDetalle.removeAll();

        actualizarTotalesDetalle();
    }

    private void actualizarTotalesDetalle() {

        colDescripcion.setFooter("ARTÍCULOS: " + listDet.size());
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

        EstiloConsulta.aplicarGrid(grid);
        grid.setPartNameGenerator(r -> esAnulada(r) ? "fv-fila-anulada" : null);

        colNumero = grid.addColumn(RecepcionMercancia::getNumeroDocumento)
                .setHeader("N° Recepción")
                .setKey("numero")
                .setFooter("RECEPCIONES:");

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

        grid.addComponentColumn(this::chipEstado).setHeader("Estado").setKey("estado");

        colSubtotal = EstiloConsulta.monto(grid.addColumn(r -> moneda(r.getSubtotal())))
                .setHeader("SubTotal")
                .setKey("subtotal")
                .setComparator(Comparator.comparing(
                        (RecepcionMercancia r) -> r.getSubtotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colItbis = EstiloConsulta.monto(grid.addColumn(r -> moneda(r.getTotalItbis())))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setComparator(Comparator.comparing(
                        (RecepcionMercancia r) -> r.getTotalItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colTotal = EstiloConsulta.monto(grid.addColumn(r -> moneda(r.getTotal())))
                .setHeader("Total")
                .setKey("total")
                .setComparator(Comparator.comparing(
                        (RecepcionMercancia r) -> r.getTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // Columna de Acciones
        grid.addColumn(new ComponentRenderer<>(rec -> {
            HorizontalLayout acciones = new HorizontalLayout();

            // (Antes navegaba a una ruta inexistente)
            Button btnVer = new Button(VaadinIcon.EYE.create(), e -> verDetalle(rec));
            btnVer.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btnVer.setTooltipText("Ver detalle de la recepción");

            Button btnAnular = new Button(VaadinIcon.BAN.create(), e -> confirmarAnulacion(rec));
            btnAnular.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            btnAnular.setTooltipText("Anular recepción");
            btnAnular.setEnabled(!esAnulada(rec));

            acciones.add(btnVer, btnAnular);
            return acciones;
        })).setHeader("Acciones").setKey("acciones").setSortable(false);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        dataViewRecepciones = grid.setItems(listaRecepciones);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        EstiloConsulta.aplicarGrid(gridDetalle);
        gridDetalle.setWidthFull();
        gridDetalle.setMinHeight("300px");
        gridDetalle.setHeight("50vh");

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

        EstiloConsulta.monto(gridDetalle.addColumn(d -> moneda(d.getPrecioUnitario())))
                .setHeader("Precio Compra")
                .setKey("precio")
                .setComparator(Comparator.comparing(
                        (DetalleRecepcionMercancia d) -> d.getPrecioUnitario(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetSubtotal = EstiloConsulta.monto(gridDetalle.addColumn(d -> moneda(d.getSubTotal())))
                .setHeader("SubTotal")
                .setKey("subtotal")
                .setComparator(Comparator.comparing(
                        (DetalleRecepcionMercancia d) -> d.getSubTotal(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetItbis = EstiloConsulta.monto(gridDetalle.addColumn(d -> moneda(d.getItbis())))
                .setHeader("ITBIS")
                .setKey("itbis")
                .setComparator(Comparator.comparing(
                        (DetalleRecepcionMercancia d) -> d.getItbis(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        colDetTotal = EstiloConsulta.monto(gridDetalle.addColumn(d -> moneda(d.getTotal())))
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

        if (esAnulada(rec)) {
            Notification.show("La recepción ya está anulada.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            return;
        }

        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Anular Recepción N° " + rec.getNumeroDocumento());
        dialog.setWidth("520px");

        com.vaadin.flow.component.html.Paragraph texto = new com.vaadin.flow.component.html.Paragraph(
                "Se sacará del inventario la mercancía recibida, se anulará su factura de compra "
                + "(y su pago; si fue de contado el efectivo vuelve a la caja), se revertirá el costo "
                + "y se devolverá lo recibido a la orden de compra. Si ya se vendió parte de la "
                + "mercancía, la anulación no se podrá hacer.");

        TextField txtMotivo = new TextField("Motivo de la anulación");
        txtMotivo.setWidthFull();
        txtMotivo.setRequired(true);
        txtMotivo.setMaxLength(200);

        dialog.add(texto, txtMotivo);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());
        Button btnAnular = new Button("Sí, Anular");
        btnAnular.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        btnAnular.setDisableOnClick(true);
        btnAnular.addClickListener(e -> {

            if (txtMotivo.getValue() == null || txtMotivo.getValue().isBlank()) {
                txtMotivo.setInvalid(true);
                txtMotivo.setErrorMessage("Indique el motivo");
                btnAnular.setEnabled(true);
                return;
            }

            try {
                recepcionService.anularRecepcion(rec.getCodigo(), txtMotivo.getValue().trim(), "Admin");

                Notification.show("Recepción de Mercancía anulada con éxito.", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                aplicarFiltros();

            } catch (IllegalArgumentException | IllegalStateException ex) {
                Notification.show(ex.getMessage(), 6000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                btnAnular.setEnabled(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Error al anular la recepción: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                btnAnular.setEnabled(true);
            }
        });

        dialog.getFooter().add(btnCancelar, btnAnular);
        dialog.open();
        txtMotivo.focus();
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

    private Span chipEstado(RecepcionMercancia r) {
        if (esAnulada(r)) {
            return EstiloConsulta.chipAnulada();
        }
        EstadoDocumento e = r.getEstadoDocumento();
        return EstiloConsulta.chip(e != null && e.getNombre() != null ? e.getNombre().toUpperCase() : "RECIBIDA",
                "fv-chip-pagada");
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
