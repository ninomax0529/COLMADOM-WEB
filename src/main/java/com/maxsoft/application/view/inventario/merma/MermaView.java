package com.maxsoft.application.view.inventario.merma;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Merma;
import com.maxsoft.application.modelo.TipoMerma;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.MermaService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMermaService;
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
@PageTitle("Merma de Inventario")
@Route(value = "inventario/merma")
public class MermaView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    private final MermaService mermaService;
    private final AlmacenService almacenService;
    private final TipoMermaService tipoMermaService;

    private final Grid<Merma> grid = new Grid<>(Merma.class, false);
    private final Grid<DetalleMerma> gridDetalle = new Grid<>(DetalleMerma.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Filtros de consulta
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final ComboBox<TipoMerma> cbTipoMerma = new ComboBox<>("Tipo de Merma");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén");
    private final TextField txtBuscar = new TextField();
    private Button btnBuscar;
    private Button btnLimpiar;

    private final TabSheet tabs = new TabSheet();

    List<Merma> listaMermas = new ArrayList<>();
    List<DetalleMerma> listDet = new ArrayList<>();

    private GridListDataView<Merma> dataViewMermas;

    // Columnas con pie (totales)
    private Grid.Column<Merma> colCodigo;
    private Grid.Column<DetalleMerma> colDescripcion;
    private Grid.Column<DetalleMerma> colSalidaBase;
    private Grid.Column<DetalleMerma> colTotal;

    @Autowired
    public MermaView(MermaService mermaService,
            AlmacenService almacenService,
            TipoMermaService tipoMermaService) {

        this.mermaService = mermaService;
        this.almacenService = almacenService;
        this.tipoMermaService = tipoMermaService;

        setSizeFull();
        setSpacing(false);

        tabs.setSizeFull();

        tabs.add("Merma de Inventario", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(RegistroMermaView.class);

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

        cargarAlmacenes();
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

        cbTipoMerma.setItemLabelGenerator(TipoMerma::getDescripcion);
        cbTipoMerma.setAllowCustomValue(false);
        cbTipoMerma.setClearButtonVisible(true);
        cbTipoMerma.setPlaceholder("Todos");

        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setClearButtonVisible(true);
        cbAlmacen.setPlaceholder("Todos");

        txtBuscar.setWidth("280px");
        txtBuscar.setPlaceholder("Filtrar por código, observación o usuario...");
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
                dpDesde, dpHasta, cbTipoMerma, cbAlmacen, txtBuscar, btnBuscar, btnLimpiar);
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void cargarAlmacenes() {

        cbAlmacen.setItems(almacenService.getLista());
        // Para filtrar se muestran todos los tipos, incluso los inactivos
        cbTipoMerma.setItems(tipoMermaService.getLista());
    }

    private void limpiarFiltros() {

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        cbTipoMerma.clear();
        cbAlmacen.clear();
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
            TipoMerma tipo = cbTipoMerma.getValue();
            Almacen almacen = cbAlmacen.getValue();

            List<Merma> resultado = this.mermaService.getLista()
                    .stream()
                    .filter(m -> dentroDeRango(aLocalDate(m.getFecha())))
                    .filter(m -> tipo == null || (m.getTipoMerma() != null
                    && Objects.equals(m.getTipoMerma().getCodigo(), tipo.getCodigo())))
                    .filter(m -> almacen == null || (m.getAlmacen() != null
                    && Objects.equals(m.getAlmacen().getCodigo(), almacen.getCodigo())))
                    .sorted(Comparator.comparing(
                            (Merma m) -> aLocalDate(m.getFecha()),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            listaMermas.clear();
            listaMermas.addAll(resultado);
            grid.getDataProvider().refreshAll();

            aplicarFiltroTexto();
            grid.deselectAll();
            limpiarDetalle();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error consultando mermas: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewMermas == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewMermas.setFilter(m -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchCod = m.getCodigo() != null && m.getCodigo().toString().contains(term);
            boolean matchObs = m.getObservacion() != null && m.getObservacion().toLowerCase().contains(term);
            boolean matchUsr = m.getNombreUsuario() != null && m.getNombreUsuario().toLowerCase().contains(term);
            return matchCod || matchObs || matchUsr;
        });

        colCodigo.setFooter("TOTAL MERMAS: " + dataViewMermas.getItemCount());
    }

    private void cargarDetalle(Merma merma) {

        Collection<DetalleMerma> detalles = this.mermaService.getDetalle(merma.getCodigo());

        listDet = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).setLabel("Detalle( Merma -> " + merma.getCodigo()
                + (merma.getTipoMerma() != null ? " - " + merma.getTipoMerma().getDescripcion() : "")
                + " )");

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

        BigDecimal totalPerdida = listDet.stream()
                .map(this::totalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        colDescripcion.setFooter("TOTAL ARTÍCULOS: " + listDet.size());
        colSalidaBase.setFooter("-" + numero(totalBase));
        colTotal.setFooter(numero(totalPerdida));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        colCodigo = grid.addColumn(Merma::getCodigo)
                .setHeader("N° Merma")
                .setKey("codigo")
                .setFooter("TOTAL MERMAS:");

        grid.addColumn(m -> {
            LocalDate f = aLocalDate(m.getFecha());
            return f != null ? f.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (Merma m) -> m.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(m -> m.getFechaRegistro() != null
                ? new Date(m.getFechaRegistro().getTime()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_HORA)
                : "")
                .setHeader("Registrada")
                .setKey("fechaRegistro")
                .setComparator(Comparator.comparing(
                        (Merma m) -> m.getFechaRegistro(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(m -> m.getAlmacen() != null ? m.getAlmacen().getNombre() : "")
                .setHeader("Almacén")
                .setKey("almacen");

        grid.addColumn(m -> m.getTipoMerma() != null ? m.getTipoMerma().getDescripcion() : "")
                .setHeader("Tipo de Merma")
                .setKey("tipo");

        grid.addColumn(Merma::getNombreUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        grid.addColumn(m -> m.getSalidaInventario() != null && m.getSalidaInventario().getCodigo() != null
                ? m.getSalidaInventario().getCodigo().toString()
                : "-")
                .setHeader("Salida N°")
                .setKey("salida");

        grid.addColumn(Merma::getObservacion)
                .setHeader("Observación")
                .setKey("observacion")
                .setFlexGrow(1);

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        dataViewMermas = grid.setItems(listaMermas);
        grid.setSizeFull();
    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

        colDescripcion = gridDetalle.addColumn(DetalleMerma::getDescripcionArticulo)
                .setHeader("Artículo")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : d.getNombreUnidad())
                .setHeader("Empaque / Presentación")
                .setKey("unidad");

        gridDetalle.addColumn(d -> numero(d.getCantidad()))
                .setHeader("Cantidad")
                .setKey("cantidad")
                .setComparator(Comparator.comparing(
                        (DetalleMerma d) -> d.getCantidad(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        // MULTI-EMPAQUE: factor y descuento en unidad base (calculado aquí)
        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setKey("factor")
                .setComparator(Comparator.comparing(this::factorDe));

        colSalidaBase = gridDetalle.addColumn(d -> "-" + numero(cantidadBase(d)))
                .setHeader("Salida (UMM)")
                .setKey("salidaBase")
                .setComparator(Comparator.comparing(this::cantidadBase));

        gridDetalle.addColumn(d -> numero(d.getCostoUnitario()))
                .setHeader("Costo Unit.")
                .setKey("costo");

        colTotal = gridDetalle.addColumn(d -> numero(totalLinea(d)))
                .setHeader("Valor Pérdida")
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

    private BigDecimal factorDe(DetalleMerma d) {

        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleMerma d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    /**
     * Valor de la pérdida: cantidad x costo unitario (no depende del campo
     * "total" guardado).
     */
    private BigDecimal totalLinea(DetalleMerma d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        BigDecimal costo = d.getCostoUnitario() != null ? d.getCostoUnitario() : BigDecimal.ZERO;
        return cant.multiply(costo);
    }

    private String numero(BigDecimal valor) {

        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}
