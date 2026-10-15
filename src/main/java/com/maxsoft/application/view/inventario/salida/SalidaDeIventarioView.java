/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.salida;

/**
 *
 * @author maximilianoalmonte
 */

import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

@PermitAll
@PageTitle("Salida de Inventario")
@Route(value = "inventario/salida")

public class SalidaDeIventarioView extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat FORMATO_NUMERO
            = new DecimalFormat("#,##0.00");

    @Autowired
    DataSource dataSource;
    private final SalidaInventarioService salidaService;
    private final Grid<SalidaInventario> grid = new Grid<>(SalidaInventario.class, false);
    private Grid<DetalleSalidaInventario> gridDetalle = new Grid<>(DetalleSalidaInventario.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    // Filtros de consulta
    private final DatePicker dpDesde = new DatePicker("Desde");
    private final DatePicker dpHasta = new DatePicker("Hasta");
    private final TextField txtBuscar = new TextField();
    private Button btnBuscar;
    private Button btnLimpiar;

    private final TabSheet tabs = new TabSheet();

    List<DetalleSalidaInventario> listDet = new ArrayList<>();

    private TextField nombre = new TextField("Nombre");
    private NumberField precio = new NumberField("Precio");
    private NumberField stock = new NumberField("Stock");

    private Button guardar = new Button("Guardar");
    private Button cancelar = new Button("Cancelar");
    private Button nuevo = new Button("Nuevo");

    List<SalidaInventario> listaEntInv = new ArrayList<>();

    private GridListDataView<SalidaInventario> dataViewSalidas;

    // Columnas con pie (totales)
    private Grid.Column<SalidaInventario> colCodigo;
    private Grid.Column<DetalleSalidaInventario> colDescripcion;
    private Grid.Column<DetalleSalidaInventario> colSalidaBase;
    private Grid.Column<DetalleSalidaInventario> colTotal;

    @Autowired
    public SalidaDeIventarioView(SalidaInventarioService salidaServiceArg) {

        setSizeFull();
        setSpacing(false);

        this.salidaService = salidaServiceArg;
        tabs.setSizeFull();

        tabs.add("Salida de Inventario", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(RegistroSalidaDeIventarioView.class);

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
            List<SalidaInventario> resultado = this.salidaService.getLista(false)
                    .stream()
                    .filter(s -> dentroDeRango(aLocalDate(s.getFecha())))
                    .sorted(Comparator.comparing(
                            (SalidaInventario s) -> aLocalDate(s.getFecha()),
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
            mostrarNotificacion("Error consultando salidas: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filtro de texto en memoria sobre el resultado ya cargado.
     */
    private void aplicarFiltroTexto() {

        if (dataViewSalidas == null) {
            return;
        }

        String term = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toLowerCase();

        dataViewSalidas.setFilter(s -> {
            if (term.isEmpty()) {
                return true;
            }
            boolean matchCod = s.getCodigo() != null && s.getCodigo().toString().contains(term);
            boolean matchObs = s.getObservacion() != null && s.getObservacion().toLowerCase().contains(term);
            boolean matchUsr = s.getUsuario() != null && String.valueOf(s.getUsuario()).toLowerCase().contains(term);
            return matchCod || matchObs || matchUsr;
        });

        colCodigo.setFooter("TOTAL SALIDAS: " + dataViewSalidas.getItemCount());
    }

    private void cargarDetalle(SalidaInventario salida) {

        List<DetalleSalidaInventario> detalles = this.salidaService.getDetalle(salida.getCodigo());

        listDet = detalles != null ? detalles : new ArrayList<>();
        gridDetalle.setItems(listDet);

        tabs.getTabAt(1).setLabel("Detalle( Salida -> " + salida.getCodigo() + " )");

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
        colSalidaBase.setFooter("-" + numero(totalBase));
        colTotal.setFooter(numero(totalGeneral));
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {

        colCodigo = grid.addColumn(SalidaInventario::getCodigo).setHeader("Codigo")
                .setFooter("TOTAL SALIDAS:");

        grid.addColumn(s -> {
            LocalDate f = aLocalDate(s.getFecha());
            return f != null ? f.format(FORMATO_FECHA) : "";
        })
                .setHeader("Fecha")
                .setKey("fecha")
                .setComparator(Comparator.comparing(
                        (SalidaInventario s) -> s.getFecha(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(s -> s.getFechaRegistro() != null
                ? new Date(s.getFechaRegistro().getTime()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_HORA)
                : "")
                .setHeader("Fecha Registro")
                .setKey("fechaRegistro")
                .setComparator(Comparator.comparing(
                        (SalidaInventario s) -> s.getFechaRegistro(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(SalidaInventario::getUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        grid.addColumn(SalidaInventario::getObservacion)
                .setHeader("Observación")
                .setKey("observacion")
                .setFlexGrow(1);

        dataViewSalidas = grid.setItems(listaEntInv);
        grid.setSizeFull();

        grid.addComponentColumn(documento -> {

            HorizontalLayout acciones = new HorizontalLayout();

            Button verBtn = new Button(VaadinIcon.EYE.create(), event -> {

//                 Aquí puedes abrir un diálogo o navegar
                try (Connection conn = dataSource.getConnection()) {

//                    RptPlantilla rptP = new RptPlantilla();
//                    if (rptP != null) {
//
////                        StreamResource pdfResource = rptP.verProgramacion(documento.getCodigo(), conn);
//
//                        Anchor anchor = new Anchor(pdfResource, "");
//                        anchor.getElement().setAttribute("download", false);
//                        anchor.getElement().setAttribute("target", "_blank");
//
//                        anchor.getElement().callJsFunction("click"); // dispara la apertura automática
//
//                        add(anchor);
//                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Notification.show("Error al generar el reporte", 3000, Notification.Position.TOP_CENTER);
                }

            });

            Button btnAnular = new Button("Anular ", event -> {

//                ConfirmDialog dialog = new ConfirmDialog(
//                        "¿Seguro que quiere anular el regsitro -> "+documento.getCodigo(),
//                        () -> {
//
//                            documento.setAnulada(true);
//                            salidaService.guardar(documento);
//                            grid.setItems(salidaService.getLista(false));
//                            grid.getDataProvider().refreshAll();
//
//                            Notification.show("Registro anulado exitosamente", 3000, Notification.Position.TOP_CENTER);
//
//                        },
//                        () -> {
//
//                        }
//                );

//                dialog.open();

            });

            verBtn.getElement().setAttribute("title", "Ver");

            acciones.setSizeFull();
//            acciones.setSpacing("2px");

            acciones.add(btnAnular);

            return acciones;
        }).setHeader("Acciones").setAutoWidth(true);

    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();
//
//        gridDetalle.addColumn(
//                DetalleEntradaInventario::getArticulo).setHeader("Articulo")
//                .setFooter("TOTAL ARTICULOS:");

        colDescripcion = gridDetalle.addColumn(DetalleSalidaInventario::getDescripcionArticulo)
                .setHeader("Descripcion")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(DetalleSalidaInventario::getNombreAlmacen)
                .setHeader("Almacén")
                .setKey("almacen");

        gridDetalle.addColumn(DetalleSalidaInventario::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad");

        gridDetalle.addColumn(this::unidadDe)
                .setHeader("Unidad")
                .setKey("unidad");

        // MULTI-EMPAQUE: factor y equivalente en unidad base (calculado aquí)
        gridDetalle.addColumn(d -> numero(factorDe(d)))
                .setHeader("Factor")
                .setKey("factor")
                .setComparator(Comparator.comparing(this::factorDe));

        colSalidaBase = gridDetalle.addColumn(d -> "-" + numero(cantidadBase(d)))
                .setHeader("Salida (UMM)")
                .setKey("salidaBase")
                .setComparator(Comparator.comparing(this::cantidadBase));

        gridDetalle.addColumn(d -> numero(d.getExistenciaAnterior()))
                .setHeader("Existencia")
                .setKey("existencia");

        gridDetalle.addColumn(d -> numero(d.getExistencia()))
                .setHeader("Nueva Existencia")
                .setKey("nuevaexistencia");

        gridDetalle.addColumn(d -> numero(d.getPrecioCompra()))
                .setHeader("Costo Unit.")
                .setKey("costo");

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

    /**
     * Presentación vendida/retirada: descripción de la unidad del empaque; si
     * no hay, la unidad que tenga la línea.
     */
    private String unidadDe(DetalleSalidaInventario d) {

        if (d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null) {
            return d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion();
        }
        return d.getUnidad() != null ? String.valueOf(d.getUnidad()) : "";
    }

    private BigDecimal factorDe(DetalleSalidaInventario d) {

        BigDecimal f = d.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleSalidaInventario d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d));
    }

    /**
     * Total de la línea: el guardado si existe; si no, cantidad x costo.
     */
    private BigDecimal totalLinea(DetalleSalidaInventario d) {

        if (d.getTotal() != null && d.getTotal().signum() > 0) {
            return d.getTotal();
        }

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        BigDecimal costo = d.getPrecioCompra() != null ? d.getPrecioCompra() : BigDecimal.ZERO;
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