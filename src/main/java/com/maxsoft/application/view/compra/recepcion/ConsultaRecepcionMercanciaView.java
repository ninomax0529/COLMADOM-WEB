/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.compra.recepcion;


import com.maxsoft.application.modelo.EstadoDocumento;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;

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

@Route(value = "compra/consultaRecepcion")
@PageTitle("Consulta de Recepciones de Mercancía")
public class ConsultaRecepcionMercanciaView extends VerticalLayout {

    private final RecepcionMercanciaService recepcionService;
    private final ProveedorService proveedorService;

    // Componentes de Filtro
    private final TextField txtBuscar = new TextField("Buscar");
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("Suplidor / Proveedor");
    private final ComboBox<String> cbTipoOrigen = new ComboBox<>("Origen");
    private final ComboBox<EstadoDocumento> cbEstado = new ComboBox<>("Estado");
    private final DatePicker dtDesde = new DatePicker("Fecha Desde");
    private final DatePicker dtHasta = new DatePicker("Fecha Hasta");

    private final Button btnFiltrar = new Button("Buscar", VaadinIcon.SEARCH.create());
    private final Button btnLimpiar = new Button("Limpiar", VaadinIcon.REFRESH.create());
    private final Button btnNuevaRecepcion = new Button("Nueva Recepción", VaadinIcon.PLUS.create());

    // Grid y Lista
    private final Grid<RecepcionMercancia> gridRecepciones = new Grid<>(RecepcionMercancia.class, false);
    private List<RecepcionMercancia> listaRecepciones = new ArrayList<>();

    // Resumen
    private final Span lblTotalRegistros = new Span("Registros: 0");
    private final Span lblMontoTotal = new Span("Monto Total: RD$ 0.00");

    public ConsultaRecepcionMercanciaView(RecepcionMercanciaService recepcionService,
                                         ProveedorService proveedorService) {
        this.recepcionService = recepcionService;
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

        H2 titulo = new H2("Consulta de Recepciones de Mercancía");

        btnNuevaRecepcion.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnNuevaRecepcion.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("recepcion-mercancia/nueva")));

        layoutHeader.add(titulo, btnNuevaRecepcion);
        add(layoutHeader);
    }

    private void crearFiltros() {
        FormLayout layoutFiltros = new FormLayout();
        layoutFiltros.setWidthFull();

        txtBuscar.setPlaceholder("N° Recepción, N° Doc u observaciones...");
        txtBuscar.setClearButtonVisible(true);

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(s -> s.getRnc() != null ? s.getNombre()+ " (" + s.getRnc() + ")" : s.getNombre());
        cbSuplidor.setPlaceholder("Todos los suplidores");
        cbSuplidor.setClearButtonVisible(true);

        cbTipoOrigen.setItems("TODOS", "ORDEN_COMPRA", "FACTURA_DIRECTA", "OTRO");
        cbTipoOrigen.setValue("TODOS");

//        cbEstado.setItems(EstadoDocumento.values());
        cbEstado.setItemLabelGenerator(EstadoDocumento::getNombre);
        cbEstado.setClearButtonVisible(true);
        cbEstado.setPlaceholder("Todos");

        // Rango de fechas por defecto: Primer día del mes actual hasta la fecha actual
        dtDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dtHasta.setValue(LocalDate.now());

        layoutFiltros.add(txtBuscar, cbSuplidor, cbTipoOrigen, cbEstado, dtDesde, dtHasta);
        layoutFiltros.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3),
                new FormLayout.ResponsiveStep("900px", 6)
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
        
        gridRecepciones.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT, GridVariant.LUMO_COLUMN_BORDERS);
        gridRecepciones.setSizeFull();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        gridRecepciones.addColumn(RecepcionMercancia::getNumeroDocumento).setHeader("N° Recepción").setAutoWidth(true).setSortable(true);
        gridRecepciones.addColumn(RecepcionMercancia::getTipoDocumento).setHeader("Tipo Origen").setAutoWidth(true).setSortable(true);
//        gridRecepciones.addColumn(r -> r.getOrigenNumero() != null ? r.getOrigenNumero() : "-").setHeader("N° Doc. Ref.").setAutoWidth(true);
        gridRecepciones.addColumn(r -> r.getProveedor() != null ? r.getProveedor().getNombre(): "-").setHeader("Suplidor").setFlexGrow(2).setSortable(true);
        gridRecepciones.addColumn(r -> r.getFecha() != null ? df.format(r.getFecha()) : "-").setHeader("Fecha Recepción").setAutoWidth(true).setSortable(true);

        // Columna Estado usando la configuración del Enum EstadoDocumento
        gridRecepciones.addColumn(new ComponentRenderer<>(rec -> {
            EstadoDocumento est = rec.getEstadoDocumento()!= null ? rec.getEstadoDocumento() :rec.getEstadoDocumento();
            Span badge = new Span(est.getNombre());
            badge.getElement().getThemeList().add("badge " + est.getColorBadge());
            return badge;
        })).setHeader("Estado").setAutoWidth(true);

        gridRecepciones.addColumn(r -> String.format("RD$ %,.2f", r.getSubtotal() != null ? r.getSubtotal() : BigDecimal.ZERO)).setHeader("SubTotal").setAutoWidth(true);
        gridRecepciones.addColumn(r -> String.format("RD$ %,.2f", r.getTotalItbis() != null ? r.getTotalItbis() : BigDecimal.ZERO)).setHeader("ITBIS").setAutoWidth(true);
        gridRecepciones.addColumn(r -> String.format("RD$ %,.2f", r.getTotal() != null ? r.getTotal() : BigDecimal.ZERO)).setHeader("Total").setAutoWidth(true);

        // Columna de Acciones
        gridRecepciones.addColumn(new ComponentRenderer<>(rec -> {
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
        })).setHeader("Acciones").setAutoWidth(true);

        add(gridRecepciones);
        setFlexGrow(1, gridRecepciones);
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
        List<RecepcionMercancia> todas = recepcionService.getLista();

        String textoBuscar = txtBuscar.getValue() != null ? txtBuscar.getValue().toLowerCase().trim() : "";
        Proveedor suplidorSel = cbSuplidor.getValue();
        String tipoOrigenSel = cbTipoOrigen.getValue();
        EstadoDocumento estadoSel = cbEstado.getValue();

        Date fechaDesde = dtDesde.getValue() != null ? Date.from(dtDesde.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
        Date fechaHasta = dtHasta.getValue() != null ? Date.from(dtHasta.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

        // Filtrado dinámico en memoria
        listaRecepciones = todas.stream().filter(rec -> {
            // Filtro Texto
            boolean coincideTexto = textoBuscar.isEmpty()
                    || (rec.getNumeroDocumento()!= null && rec.getNumeroDocumento().toLowerCase().contains(textoBuscar))
                    || (rec.getTipoDocumento() != null && rec.getTipoDocumento().getNombre().toLowerCase().contains(textoBuscar))
                    || (rec.getComentario() != null && rec.getComentario().toLowerCase().contains(textoBuscar));

            // Filtro Suplidor
            boolean coincideSuplidor = suplidorSel == null 
                    || (rec.getProveedor() != null && rec.getProveedor().equals(suplidorSel));

            // Filtro Tipo Origen
            boolean coincideOrigen = "TODOS".equalsIgnoreCase(tipoOrigenSel) || tipoOrigenSel == null
                    || (rec.getTipoDocumento()!= null && rec.getTipoDocumento().getNombre().equalsIgnoreCase(tipoOrigenSel));

            // Filtro Estado
            boolean coincideEstado = estadoSel == null || rec.getEstadoDocumento()== estadoSel;

            // Filtro Fechas
            boolean coincideFecha = true;
            if (rec.getFecha() != null) {
                if (fechaDesde != null && rec.getFecha().before(fechaDesde)) coincideFecha = false;
                if (fechaHasta != null && rec.getFecha().after(fechaHasta)) coincideFecha = false;
            }

            return coincideTexto && coincideSuplidor && coincideOrigen && coincideEstado && coincideFecha;
        }).toList();

        gridRecepciones.setItems(listaRecepciones);
        actualizarResumen();
    }

    private void actualizarResumen() {
        lblTotalRegistros.setText("Registros: " + listaRecepciones.size());

        BigDecimal totalSumatoria = listaRecepciones.stream()
                .filter(r -> r.getEstadoDocumento()!=  r.getEstadoDocumento() ) // Excluir recepciones anuladas del total
                .map(r -> r.getTotal() != null ? r.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblMontoTotal.setText(String.format("Monto Total Acumulado: RD$ %,.2f", totalSumatoria));
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

    private void confirmarAnulacion(RecepcionMercancia rec) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Anular Recepción de Mercancía");
        dialog.setText("¿Está seguro de que desea anular la Recepción N° " + rec.getNumeroDocumento()+ "? Esta acción reversará las entradas de inventario registradas.");

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
}