/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.compra.orden;

import com.maxsoft.application.modelo.OrdenDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Route(value = "compra/consultaorden")
@PageTitle("Consulta de Órdenes de Compra")
public class ConsultaOrdenesDeCompraView extends VerticalLayout {

    private final OrdenDeCompraService ordenCompraService;
    private final ProveedorService proveedorService;

    // Componentes de Filtro
    private final TextField txtBuscar = new TextField("Buscar");
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("Suplidor / Proveedor");
    private final ComboBox<String> cbEstado = new ComboBox<>("Estado");
    private final DatePicker dtDesde = new DatePicker("Fecha Desde");
    private final DatePicker dtHasta = new DatePicker("Fecha Hasta");

    private final Button btnFiltrar = new Button("Buscar", VaadinIcon.SEARCH.create());
    private final Button btnLimpiar = new Button("Limpiar Filtros", VaadinIcon.REFRESH.create());
    private final Button btnNuevaOrden = new Button("Nueva Orden", VaadinIcon.PLUS.create());

    // Grid y Lista
    private final Grid<OrdenDeCompra> gridOrdenes = new Grid<>(OrdenDeCompra.class, false);
    private List<OrdenDeCompra> listaOrdenes = new ArrayList<>();

    // Resumen
    private final Span lblTotalRegistros = new Span("Registros: 0");
    private final Span lblMontoTotal = new Span("Monto Total: RD$ 0.00");

    public ConsultaOrdenesDeCompraView(OrdenDeCompraService ordenCompraService,
                                       ProveedorService proveedorService) {
        this.ordenCompraService = ordenCompraService;
        this.proveedorService = proveedorService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        crearEncabezado();
        crearFiltros();
        configurarGrid();
        crearResumenInferior();

        aplicarFiltros(); // Carga inicial
    }

    private void crearEncabezado() {
        HorizontalLayout layoutHeader = new HorizontalLayout();
        layoutHeader.setWidthFull();
        layoutHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layoutHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 titulo = new H2("Consulta de Órdenes de Compra");

        btnNuevaOrden.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnNuevaOrden.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("ordenes-compra/nueva")));

        layoutHeader.add(titulo, btnNuevaOrden);
        add(layoutHeader);
    }

    private void crearFiltros() {
        FormLayout layoutFiltros = new FormLayout();
        layoutFiltros.setWidthFull();

        txtBuscar.setPlaceholder("Número u observación...");
        txtBuscar.setClearButtonVisible(true);

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(s -> s.getRnc() != null ? s.getnombre() + " (" + s.getRnc() + ")" : s.getnombre());
        cbSuplidor.setPlaceholder("Todos los suplidores");
        cbSuplidor.setClearButtonVisible(true);

        cbEstado.setItems("TODOS", "PENDIENTE", "COMPLETADA", "ANULADA");
        cbEstado.setValue("TODOS");

        // Rango por defecto (Primer día del mes actual hasta hoy)
        dtDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dtHasta.setValue(LocalDate.now());

        layoutFiltros.add(txtBuscar, cbSuplidor, cbEstado, dtDesde, dtHasta);
        layoutFiltros.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3),
                new FormLayout.ResponsiveStep("900px", 5)
        );

        // Botones de acción del filtro
        btnFiltrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnFiltrar.addClickListener(e -> aplicarFiltros());

        btnLimpiar.addClickListener(e -> limpiarFiltros());

        HorizontalLayout layoutBotonesFiltro = new HorizontalLayout(btnFiltrar, btnLimpiar);
        layoutBotonesFiltro.setMargin(false);

        add(layoutFiltros, layoutBotonesFiltro);
    }

    private void configurarGrid() {
        gridOrdenes.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT, GridVariant.LUMO_COLUMN_BORDERS);
        gridOrdenes.setSizeFull();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        gridOrdenes.addColumn(OrdenDeCompra::getNumero).setHeader("Número").setAutoWidth(true).setSortable(true);
        gridOrdenes.addColumn(o -> o.getProveedor() != null ? o.getProveedor().getnombre() : "-").setHeader("Suplidor").setFlexGrow(2).setSortable(true);
        gridOrdenes.addColumn(o -> o.getFecha() != null ? df.format(o.getFecha()) : "-").setHeader("Fecha").setAutoWidth(true).setSortable(true);
        gridOrdenes.addColumn(o -> o.getFechaDeEntrega() != null ? df.format(o.getFechaDeEntrega()) : "-").setHeader("F. Entrega").setAutoWidth(true);
        
        // Columna Estado con Badge de Color
        gridOrdenes.addColumn(new ComponentRenderer<>(orden -> {
            Span badge = new Span(orden.getEstado() != null ? orden.getEstado() : "PENDIENTE");
            String estado = orden.getEstado() != null ? orden.getEstado().toUpperCase() : "";

            if (orden.getAnulada() != null && orden.getAnulada()) {
                badge.setText("ANULADA");
                badge.getElement().getThemeList().add("badge error");
            } else if ("COMPLETADA".equals(estado) || "SI".equalsIgnoreCase(orden.getCompletada())) {
                badge.setText("COMPLETADA");
                badge.getElement().getThemeList().add("badge success");
            } else {
                badge.setText("PENDIENTE");
                badge.getElement().getThemeList().add("badge warning");
            }
            return badge;
        })).setHeader("Estado").setAutoWidth(true);

        gridOrdenes.addColumn(o -> String.format("RD$ %,.2f", o.getSubtotal() != null ? o.getSubtotal() : BigDecimal.ZERO)).setHeader("SubTotal").setAutoWidth(true);
        gridOrdenes.addColumn(o -> String.format("RD$ %,.2f", o.getTotalItbis() != null ? o.getTotalItbis() : BigDecimal.ZERO)).setHeader("ITBIS").setAutoWidth(true);
        gridOrdenes.addColumn(o -> String.format("RD$ %,.2f", o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)).setHeader("Total").setAutoWidth(true);

        // Columna Botones de Acción
        gridOrdenes.addColumn(new ComponentRenderer<>(orden -> {
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
            btnAnular.setEnabled(orden.getAnulada() == null || !orden.getAnulada());

            acciones.add(btnEditar, btnAnular);
            return acciones;
        })).setHeader("Acciones").setAutoWidth(true);

        add(gridOrdenes);
        setFlexGrow(1, gridOrdenes);
    }

    private void crearResumenInferior() {
        HorizontalLayout layoutResumen = new HorizontalLayout();
        layoutResumen.setWidthFull();
        layoutResumen.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layoutResumen.setPadding(true);
        layoutResumen.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "8px");

        lblTotalRegistros.getStyle().set("font-weight", "bold");
        lblMontoTotal.getStyle().set("font-weight", "bold").set("color", "var(--lumo-primary-text-color)");

        layoutResumen.add(lblTotalRegistros, lblMontoTotal);
        add(layoutResumen);
    }

    private void aplicarFiltros() {
        // Cargar todos los datos desde el servicio
        List<OrdenDeCompra> ordenes = ordenCompraService.getLista();

        String textoBuscar = txtBuscar.getValue() != null ? txtBuscar.getValue().toLowerCase().trim() : "";
        Proveedor suplidorSel = cbSuplidor.getValue();
        String estadoSel = cbEstado.getValue();

        Date fechaDesde = dtDesde.getValue() != null ? Date.from(dtDesde.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
        Date fechaHasta = dtHasta.getValue() != null ? Date.from(dtHasta.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

        // Filtrado en memoria
        listaOrdenes = ordenes.stream().filter(orden -> {
            // Filtro Texto
            boolean coincideTexto = textoBuscar.isEmpty()
                    || (orden.getNumero() != null && orden.getNumero().toLowerCase().contains(textoBuscar))
                    || (orden.getComentario() != null && orden.getComentario().toLowerCase().contains(textoBuscar));

            // Filtro Suplidor
            boolean coincideSuplidor = suplidorSel == null || (orden.getProveedor() != null && orden.getProveedor()
                    .getCodigo().equals(suplidorSel.getCodigo()));

            // Filtro Estado
            boolean coincideEstado = true;
            if ("PENDIENTE".equals(estadoSel)) {
                coincideEstado = (orden.getAnulada() == null || !orden.getAnulada()) && !"SI".equalsIgnoreCase(orden.getCompletada());
            } else if ("COMPLETADA".equals(estadoSel)) {
                coincideEstado = "SI".equalsIgnoreCase(orden.getCompletada()) && (orden.getAnulada() == null || !orden.getAnulada());
            } else if ("ANULADA".equals(estadoSel)) {
                coincideEstado = orden.getAnulada() != null && orden.getAnulada();
            }

            // Filtro Rango Fechas
            boolean coincideFecha = true;
            if (orden.getFecha() != null) {
                if (fechaDesde != null && orden.getFecha().before(fechaDesde)) coincideFecha = false;
                if (fechaHasta != null && orden.getFecha().after(fechaHasta)) coincideFecha = false;
            }

            return coincideTexto && coincideSuplidor && coincideEstado && coincideFecha;
        }).toList();

        gridOrdenes.setItems(listaOrdenes);
        actualizarResumen();
    }

    private void actualizarResumen() {
        lblTotalRegistros.setText("Registros: " + listaOrdenes.size());

        BigDecimal totalSumatoria = listaOrdenes.stream()
                .filter(o -> o.getAnulada() == null || !o.getAnulada()) // Excluir anuladas del cálculo general
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblMontoTotal.setText(String.format("Monto Total Acumulado: RD$ %,.2f", totalSumatoria));
    }

    private void limpiarFiltros() {
        txtBuscar.clear();
        cbSuplidor.clear();
        cbEstado.setValue("TODOS");
        dtDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dtHasta.setValue(LocalDate.now());
        aplicarFiltros();
    }

    private void confirmarAnulacion(OrdenDeCompra orden) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Anular Orden de Compra");
        dialog.setText("¿Está seguro de que desea anular la orden de compra N° " + orden.getNumero() + "? Esta acción no se puede deshacer.");

        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");

        dialog.setConfirmText("Sí, Anular");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                orden.setAnulada(true);
                orden.setEstado("ANULADA");
                ordenCompraService.guardar(orden); // O método de actualización correspondiente

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
}