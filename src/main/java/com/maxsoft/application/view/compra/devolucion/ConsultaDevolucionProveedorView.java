package com.maxsoft.application.view.compra.devolucion;

import com.maxsoft.application.modelo.AplicacionCreditoProveedor;
import com.maxsoft.application.modelo.DetalleDevolucionProveedor;
import com.maxsoft.application.modelo.DevolucionProveedor;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.servicio.interfaces.compra.DevolucionProveedorService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consulta de devoluciones a proveedor: totales, detalle por lote,
 * aplicación del crédito a facturas pendientes y anulación.
 */
@PermitAll
@PageTitle("Devoluciones a Proveedor | MaxSoft ERP")
@Route(value = "compra/devolucionProveedor")
public class ConsultaDevolucionProveedorView extends VerticalLayout {

    private static final String USUARIO_ACTUAL = "ADMIN";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat CANTIDAD = new DecimalFormat("#,##0.##");

    private final DevolucionProveedorService devolucionService;

    private final ComboBox<String> cbTipo = new ComboBox<>();
    private final TextField txtBuscar = new TextField();
    private final Div resumen = new Div();
    private final Grid<DevolucionProveedor> grid = new Grid<>(DevolucionProveedor.class, false);
    private final List<DevolucionProveedor> lista = new ArrayList<>();
    private GridListDataView<DevolucionProveedor> dataView;

    public ConsultaDevolucionProveedorView(DevolucionProveedorService devolucionService) {
        this.devolucionService = devolucionService;

        setSizeFull();
        setSpacing(false);
        EstiloConsulta.aplicarVista(this, null);

        H2 titulo = new H2("Devoluciones a Proveedor");
        titulo.addClassName("fv-titulo");
        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");

        configurarGrid();
        add(titulo, crearFiltros(), resumen, grid);
        setFlexGrow(1, grid);
        cargar();
    }

    private HorizontalLayout crearFiltros() {
        cbTipo.setItems(DevolucionProveedor.NOTA_CREDITO, DevolucionProveedor.CAMBIO, DevolucionProveedor.EFECTIVO);
        cbTipo.setItemLabelGenerator(DevolucionProveedor::nombreLiquidacion);
        cbTipo.setPlaceholder("Todas las liquidaciones");
        cbTipo.setClearButtonVisible(true);
        cbTipo.setWidth("220px");
        cbTipo.addValueChangeListener(e -> aplicarFiltros());

        txtBuscar.setPlaceholder("Buscar proveedor o número...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.LAZY);
        txtBuscar.setWidth("240px");
        txtBuscar.addValueChangeListener(e -> aplicarFiltros());

        Button btnNueva = new Button("Nueva devolución", VaadinIcon.PLUS.create(),
                e -> UI.getCurrent().navigate(RegistroDevolucionProveedorView.class));
        btnNueva.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button btnRefrescar = new Button("Refrescar", VaadinIcon.REFRESH.create(), e -> cargar());

        HorizontalLayout barra = new HorizontalLayout(btnNueva, cbTipo, txtBuscar, btnRefrescar,
                EstiloConsulta.ayuda("Doble clic para ver el detalle"));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private void configurarGrid() {
        EstiloConsulta.aplicarGrid(grid);
        grid.setSizeFull();
        dataView = grid.setItems(lista);

        grid.addColumn(DevolucionProveedor::getNumero).setHeader("Número").setAutoWidth(true)
                .setComparator(Comparator.comparing(DevolucionProveedor::getCodigo));
        grid.addColumn(d -> d.getFecha() != null ? d.getFecha().format(FECHA) : "").setHeader("Fecha")
                .setAutoWidth(true).setComparator(Comparator.comparing(DevolucionProveedor::getFecha));
        grid.addColumn(d -> d.getProveedor().getNombre()).setHeader("Proveedor").setAutoWidth(true).setFlexGrow(2)
                .setSortable(true);
        grid.addColumn(d -> d.getAlmacen().getNombre()).setHeader("Almacén").setAutoWidth(true);
        grid.addComponentColumn(this::chipTipo).setHeader("Liquidación").setAutoWidth(true);
        grid.addColumn(d -> DevolucionProveedor.nombreMotivo(d.getMotivo())).setHeader("Motivo").setAutoWidth(true);
        EstiloConsulta.monto(grid.addColumn(d -> EstiloConsulta.moneda(d.getTotal()))).setHeader("Total")
                .setAutoWidth(true).setComparator(Comparator.comparing(DevolucionProveedor::getTotal));
        EstiloConsulta.monto(grid.addColumn(d -> DevolucionProveedor.NOTA_CREDITO.equals(d.getTipoLiquidacion())
                && !d.isAnulada() ? EstiloConsulta.moneda(d.getCreditoDisponible()) : "—"))
                .setHeader("Crédito disponible").setAutoWidth(true);
        grid.addComponentColumn(d -> d.isAnulada() ? EstiloConsulta.chipAnulada()
                : EstiloConsulta.chip("VIGENTE", "fv-chip-pagada")).setHeader("Estado").setAutoWidth(true);

        grid.addComponentColumn(d -> {
            Button ver = new Button(VaadinIcon.EYE.create(), e -> verDetalle(d));
            ver.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            ver.setTooltipText("Ver detalle");
            Button aplicar = new Button("Aplicar", VaadinIcon.FILE_TEXT_O.create(), e -> dialogoAplicar(d));
            aplicar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            aplicar.setTooltipText("Aplicar el crédito a una factura de compra pendiente");
            aplicar.setVisible(!d.isAnulada() && DevolucionProveedor.NOTA_CREDITO.equals(d.getTipoLiquidacion())
                    && d.getCreditoDisponible().signum() > 0);
            Button anular = new Button(VaadinIcon.BAN.create(), e -> dialogoAnular(d));
            anular.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            anular.setTooltipText("Anular");
            anular.setVisible(!d.isAnulada());
            HorizontalLayout h = new HorizontalLayout(ver, aplicar, anular);
            h.setAlignItems(Alignment.CENTER);
            return h;
        }).setHeader("Acciones").setAutoWidth(true);

        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() != null) {
                verDetalle(e.getItem());
            }
        });
    }

    private Span chipTipo(DevolucionProveedor d) {
        String texto = DevolucionProveedor.nombreLiquidacion(d.getTipoLiquidacion()).toUpperCase();
        return switch (d.getTipoLiquidacion()) {
            case DevolucionProveedor.NOTA_CREDITO -> EstiloConsulta.chip(texto, "fv-chip-credito");
            case DevolucionProveedor.CAMBIO -> EstiloConsulta.chip(texto, "fv-chip-devuelto");
            default -> EstiloConsulta.chip(texto, "fv-chip-contado");
        };
    }

    private void cargar() {
        lista.clear();
        lista.addAll(devolucionService.getLista());
        dataView.refreshAll();
        aplicarFiltros();
        actualizarResumen();
    }

    private void aplicarFiltros() {
        String tipo = cbTipo.getValue();
        String texto = txtBuscar.getValue() == null ? "" : txtBuscar.getValue().trim().toUpperCase();
        dataView.setFilter(d -> (tipo == null || tipo.equals(d.getTipoLiquidacion()))
                && (texto.isEmpty() || d.getNumero().toUpperCase().contains(texto)
                || (d.getProveedor().getNombre() != null && d.getProveedor().getNombre().toUpperCase().contains(texto))));
    }

    private void actualizarResumen() {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal disponible = BigDecimal.ZERO;
        BigDecimal efectivo = BigDecimal.ZERO;
        BigDecimal cambios = BigDecimal.ZERO;
        for (DevolucionProveedor d : lista) {
            if (d.isAnulada()) {
                continue;
            }
            total = total.add(d.getTotal());
            switch (d.getTipoLiquidacion()) {
                case DevolucionProveedor.NOTA_CREDITO -> disponible = disponible.add(d.getCreditoDisponible());
                case DevolucionProveedor.EFECTIVO -> efectivo = efectivo.add(d.getTotal());
                case DevolucionProveedor.CAMBIO -> cambios = cambios.add(d.getTotal());
                default -> {
                }
            }
        }
        resumen.removeAll();
        resumen.add(
                EstiloConsulta.tarjeta("Total devuelto", total, "fv-tarjeta-total", VaadinIcon.TRUCK),
                EstiloConsulta.tarjeta("Crédito a favor por aplicar", disponible,
                        disponible.signum() > 0 ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada", VaadinIcon.FILE_TEXT_O),
                EstiloConsulta.tarjeta("Reembolsado en efectivo", efectivo, "fv-tarjeta-pagado", VaadinIcon.MONEY),
                EstiloConsulta.tarjeta("Cambiado por mercancía", cambios, "fv-tarjeta-devuelto", VaadinIcon.EXCHANGE));
    }

    // ============================================================
    // DETALLE
    // ============================================================
    private void verDetalle(DevolucionProveedor d) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Devolución " + d.getNumero() + " — " + d.getProveedor().getNombre());
        dlg.setWidth("1000px");
        dlg.setMaxWidth("95vw");

        Span info = new Span(DevolucionProveedor.nombreLiquidacion(d.getTipoLiquidacion())
                + " · Motivo: " + DevolucionProveedor.nombreMotivo(d.getMotivo())
                + " · Almacén: " + d.getAlmacen().getNombre()
                + " · Usuario: " + (d.getUsuario() != null ? d.getUsuario() : "")
                + (d.getObservacion() != null ? " · " + d.getObservacion() : "")
                + (d.isAnulada() ? " · ANULADA" + (d.getMotivoAnulacion() != null ? ": " + d.getMotivoAnulacion() : "") : ""));
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");

        boolean cambio = DevolucionProveedor.CAMBIO.equals(d.getTipoLiquidacion());
        Grid<DetalleDevolucionProveedor> g = new Grid<>(DetalleDevolucionProveedor.class, false);
        EstiloConsulta.aplicarGrid(g);
        g.setAllRowsVisible(true);
        g.addColumn(DetalleDevolucionProveedor::getDescripcionArticulo).setHeader("Artículo").setAutoWidth(true).setFlexGrow(2);
        g.addColumn(l -> l.getLote() != null ? l.getLote().getNumeroLote()
                + (l.getLote().getFechaVencimiento() != null ? " (vence " + l.getLote().getFechaVencimiento().format(FECHA_CORTA) + ")" : "")
                : "—").setHeader("Lote devuelto").setAutoWidth(true);
        g.addColumn(l -> CANTIDAD.format(l.getCantidad()) + (l.getArticuloEmpaque().getUnidadEmpaque() != null
                ? " " + l.getArticuloEmpaque().getUnidadEmpaque().getDescripcion() : "")).setHeader("Cantidad").setAutoWidth(true);
        EstiloConsulta.monto(g.addColumn(l -> EstiloConsulta.moneda(l.getCostoUnitario()))).setHeader("Costo").setAutoWidth(true);
        EstiloConsulta.monto(g.addColumn(l -> EstiloConsulta.moneda(l.getItbis()))).setHeader("ITBIS").setAutoWidth(true);
        EstiloConsulta.monto(g.addColumn(l -> EstiloConsulta.moneda(l.getTotal()))).setHeader("Total").setAutoWidth(true);
        if (cambio) {
            g.addColumn(l -> l.getLoteNuevo() != null ? l.getLoteNuevo().getNumeroLote()
                    + (l.getLoteNuevo().getFechaVencimiento() != null ? " (vence " + l.getLoteNuevo().getFechaVencimiento().format(FECHA_CORTA) + ")" : "")
                    : "—").setHeader("Repuesto en lote").setAutoWidth(true);
        }
        g.setItems(devolucionService.getDetalle(d.getCodigo()));

        VerticalLayout cuerpo = new VerticalLayout(info, g);
        cuerpo.setPadding(false);

        if (DevolucionProveedor.NOTA_CREDITO.equals(d.getTipoLiquidacion())) {
            List<AplicacionCreditoProveedor> aps = devolucionService.getAplicaciones(d.getCodigo());
            cuerpo.add(new H4("Aplicaciones del crédito"));
            if (aps.isEmpty()) {
                cuerpo.add(EstiloConsulta.ayuda("Todavía no se ha aplicado a ninguna factura."));
            } else {
                Grid<AplicacionCreditoProveedor> ga = new Grid<>(AplicacionCreditoProveedor.class, false);
                EstiloConsulta.aplicarGrid(ga);
                ga.setAllRowsVisible(true);
                ga.addColumn(a -> a.getFecha().format(FECHA)).setHeader("Fecha").setAutoWidth(true);
                ga.addColumn(a -> "#" + a.getFacturaCompra().getCodigo()
                        + (a.getFacturaCompra().getNumeroFactura() != null ? " · " + a.getFacturaCompra().getNumeroFactura() : ""))
                        .setHeader("Factura de compra").setAutoWidth(true);
                ga.addColumn(a -> "#" + a.getReciboEgreso().getCodigo()).setHeader("Recibo de egreso").setAutoWidth(true);
                EstiloConsulta.monto(ga.addColumn(a -> EstiloConsulta.moneda(a.getMonto()))).setHeader("Monto").setAutoWidth(true);
                ga.addComponentColumn(a -> a.isAnulada() ? EstiloConsulta.chipAnulada()
                        : EstiloConsulta.chip("APLICADA", "fv-chip-pagada")).setHeader("Estado").setAutoWidth(true);
                ga.setItems(aps);
                cuerpo.add(ga);
            }
            cuerpo.add(new Span("Total " + EstiloConsulta.moneda(d.getTotal()) + " · Aplicado "
                    + EstiloConsulta.moneda(d.getCreditoAplicado()) + " · Disponible "
                    + EstiloConsulta.moneda(d.getCreditoDisponible())));
        }
        dlg.add(cuerpo);
        dlg.getFooter().add(new Button("Cerrar", e -> dlg.close()));
        dlg.open();
    }

    // ============================================================
    // APLICAR CRÉDITO
    // ============================================================
    private void dialogoAplicar(DevolucionProveedor d) {
        List<FacturaDeCompra> facturas = devolucionService.getFacturasPendientes(d.getProveedor().getCodigo());
        if (facturas.isEmpty()) {
            ClaseUtil.mostrarNotificacion(d.getProveedor().getNombre() + " no tiene facturas de compra pendientes",
                    NotificationVariant.LUMO_CONTRAST);
            return;
        }
        Map<Integer, BigDecimal> pendientes = new HashMap<>();
        facturas.forEach(f -> pendientes.put(f.getCodigo(), devolucionService.getPendienteFactura(f.getCodigo())));

        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Aplicar crédito de " + d.getNumero());

        ComboBox<FacturaDeCompra> cbFactura = new ComboBox<>("Factura de compra");
        cbFactura.setWidth("360px");
        cbFactura.setItems(facturas);
        cbFactura.setItemLabelGenerator(f -> "#" + f.getCodigo()
                + (f.getNumeroFactura() != null ? " · " + f.getNumeroFactura() : "")
                + " · pendiente " + EstiloConsulta.moneda(pendientes.get(f.getCodigo())));
        BigDecimalField monto = new BigDecimalField("Monto a aplicar");
        monto.setWidth("200px");
        cbFactura.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                monto.setValue(pendientes.get(e.getValue().getCodigo()).min(d.getCreditoDisponible()));
            }
        });
        cbFactura.setValue(facturas.get(0));

        Span disp = new Span("Disponible: " + EstiloConsulta.moneda(d.getCreditoDisponible()));
        dlg.add(new VerticalLayout(disp, cbFactura, monto));

        Button aplicar = new Button("Aplicar", e -> {
            if (cbFactura.getValue() == null || monto.getValue() == null) {
                return;
            }
            try {
                devolucionService.aplicarCredito(d.getCodigo(), cbFactura.getValue().getCodigo(), monto.getValue(),
                        USUARIO_ACTUAL);
                dlg.close();
                ClaseUtil.mostrarNotificacion("Crédito aplicado a la factura #" + cbFactura.getValue().getCodigo(),
                        NotificationVariant.LUMO_SUCCESS);
                cargar();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ClaseUtil.mostrarNotificacion(ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        aplicar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dlg.getFooter().add(new Button("Cancelar", e -> dlg.close()), aplicar);
        dlg.open();
    }

    // ============================================================
    // ANULAR
    // ============================================================
    private void dialogoAnular(DevolucionProveedor d) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Anular devolución " + d.getNumero());
        String efecto = switch (d.getTipoLiquidacion()) {
            case DevolucionProveedor.NOTA_CREDITO ->
                "La mercancía vuelve a sus lotes y se anulan las aplicaciones del crédito (las facturas vuelven a deber).";
            case DevolucionProveedor.CAMBIO ->
                "La mercancía devuelta vuelve a sus lotes y se retira la reposición (si ya se vendió, no se puede).";
            default -> "La mercancía vuelve a sus lotes y salen " + EstiloConsulta.moneda(d.getTotal())
                    + " de la caja abierta.";
        };
        TextField motivo = new TextField("Motivo");
        motivo.setWidth("100%");
        motivo.setRequired(true);
        dlg.add(new VerticalLayout(new Span(efecto), motivo));
        Button anular = new Button("Anular", e -> {
            if (motivo.getValue() == null || motivo.getValue().isBlank()) {
                motivo.setInvalid(true);
                motivo.setErrorMessage("Indique el motivo");
                return;
            }
            try {
                devolucionService.anular(d.getCodigo(), motivo.getValue(), USUARIO_ACTUAL);
                dlg.close();
                ClaseUtil.mostrarNotificacion("Devolución " + d.getNumero() + " anulada", NotificationVariant.LUMO_SUCCESS);
                cargar();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ClaseUtil.mostrarNotificacion(ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        anular.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dlg.getFooter().add(new Button("Cancelar", e -> dlg.close()), anular);
        dlg.open();
    }
}
