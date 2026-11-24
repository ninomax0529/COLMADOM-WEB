package com.maxsoft.application.view.compra.devolucion;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleDevolucionProveedor;
import com.maxsoft.application.modelo.DevolucionProveedor;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.servicio.interfaces.compra.DevolucionProveedorService;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.EmpaqueSeleccionDialogo;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.componente.inventario.LoteUi;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Registro de una devolución de mercancía a proveedor. Cada línea sale de un
 * lote; el proveedor la liquida con nota de crédito, cambio por mercancía o
 * reembolso en efectivo.
 */
@PermitAll
@PageTitle("Devolución a Proveedor | MaxSoft ERP")
@Route(value = "compra/registroDevolucionProveedor")
public class RegistroDevolucionProveedorView extends VerticalLayout implements BeforeEnterObserver {

    /** ?loteAlmacen=&lt;codigo&gt;: entra con ese lote cargado (desde Lotes y vencimientos). */
    public static final String PARAM_LOTE_ALMACEN = "loteAlmacen";

    private static final String USUARIO_ACTUAL = "ADMIN";
    private static final BigDecimal ITBIS_DEFECTO = new BigDecimal("18");
    private static final DecimalFormat CANTIDAD = new DecimalFormat("#,##0.##");

    private final DevolucionProveedorService devolucionService;
    private final LoteService loteService;
    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService empaqueService;

    private final ComboBox<Proveedor> cbProveedor = new ComboBox<>("Proveedor");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén");
    private final ComboBox<String> cbMotivo = new ComboBox<>("Motivo");
    private final RadioButtonGroup<String> rbTipo = new RadioButtonGroup<>("¿Cómo lo liquida el proveedor?");
    private final ComboBox<FacturaDeCompra> cbFactura = new ComboBox<>("Aplicar a factura de compra");
    private final TextField txtObservacion = new TextField("Observación");
    private final Span lblAyudaTipo = new Span();

    private final Grid<DetalleDevolucionProveedor> grid = new Grid<>(DetalleDevolucionProveedor.class, false);
    private final List<DetalleDevolucionProveedor> lineas = new ArrayList<>();
    private Grid.Column<DetalleDevolucionProveedor> colLoteNuevo;
    private Grid.Column<DetalleDevolucionProveedor> colVenceNuevo;

    private final Span lblSubTotal = new Span();
    private final Span lblItbis = new Span();
    private final Span lblTotal = new Span();
    private final Button btnGuardar = new Button("Guardar devolución", VaadinIcon.CHECK.create());

    public RegistroDevolucionProveedorView(DevolucionProveedorService devolucionService, LoteService loteService,
            ArticuloService articuloService, ArticuloEmpaqueService empaqueService, ProveedorService proveedorService,
            AlmacenService almacenService) {

        this.devolucionService = devolucionService;
        this.loteService = loteService;
        this.articuloService = articuloService;
        this.empaqueService = empaqueService;

        setSizeFull();
        setSpacing(false);
        addClassName("fv-vista");

        H2 titulo = new H2("Devolución a Proveedor");
        titulo.addClassName("fv-titulo");

        cbProveedor.setItems(proveedorService.getLista());
        cbProveedor.setItemLabelGenerator(Proveedor::getNombre);
        cbProveedor.setWidth("280px");
        cbProveedor.setRequired(true);
        cbProveedor.addValueChangeListener(e -> cargarFacturas());

        cbAlmacen.setItems(almacenService.getLista());
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setWidth("180px");
        cbAlmacen.setRequired(true);
        cbAlmacen.addValueChangeListener(e -> {
            if (e.isFromClient() && !lineas.isEmpty()) {
                lineas.forEach(l -> l.setLote(null));
                ClaseUtil.mostrarNotificacion("Cambió el almacén: vuelva a elegir el lote de cada línea",
                        NotificationVariant.LUMO_WARNING);
            }
            grid.getDataProvider().refreshAll();
        });

        cbMotivo.setItems(DevolucionProveedor.MOTIVO_VENCIDO, DevolucionProveedor.MOTIVO_DANADO,
                DevolucionProveedor.MOTIVO_OTRO);
        cbMotivo.setItemLabelGenerator(DevolucionProveedor::nombreMotivo);
        cbMotivo.setValue(DevolucionProveedor.MOTIVO_VENCIDO);
        cbMotivo.setWidth("140px");

        rbTipo.setItems(DevolucionProveedor.NOTA_CREDITO, DevolucionProveedor.CAMBIO, DevolucionProveedor.EFECTIVO);
        rbTipo.setItemLabelGenerator(DevolucionProveedor::nombreLiquidacion);
        rbTipo.setValue(DevolucionProveedor.NOTA_CREDITO);
        rbTipo.addValueChangeListener(e -> cambiarTipo());

        cbFactura.setWidth("340px");
        cbFactura.setClearButtonVisible(true);
        cbFactura.setPlaceholder("Ninguna: queda como crédito a favor");

        txtObservacion.setWidth("280px");
        txtObservacion.setMaxLength(250);
        txtObservacion.setPlaceholder("Conduce, nombre del vendedor...");

        lblAyudaTipo.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        HorizontalLayout fila1 = new HorizontalLayout(cbProveedor, cbAlmacen, cbMotivo, txtObservacion);
        fila1.setAlignItems(Alignment.BASELINE);
        fila1.getStyle().set("flex-wrap", "wrap");
        HorizontalLayout fila2 = new HorizontalLayout(rbTipo, cbFactura);
        fila2.setAlignItems(Alignment.BASELINE);
        fila2.getStyle().set("flex-wrap", "wrap");

        Button btnArticulo = new Button("Artículo (F2)", VaadinIcon.PLUS.create(), e -> agregarArticulo());
        btnArticulo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnArticulo.addClickShortcut(Key.F2);
        Button btnVencidos = new Button("Cargar vencidos", VaadinIcon.CALENDAR_CLOCK.create(), e -> cargarVencidos());
        btnVencidos.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnVencidos.setTooltipText("Agrega los lotes vencidos del almacén que entraron de este proveedor");
        HorizontalLayout barra = new HorizontalLayout(btnArticulo, btnVencidos, lblAyudaTipo);
        barra.setAlignItems(Alignment.CENTER);

        configurarGrid();

        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardar.addClickListener(e -> guardar());
        Button btnCancelar = new Button("Cancelar", e -> UI.getCurrent().navigate(ConsultaDevolucionProveedorView.class));

        Div totales = new Div(etiquetaTotal("Subtotal", lblSubTotal), etiquetaTotal("ITBIS", lblItbis),
                etiquetaTotal("Total", lblTotal));
        totales.getStyle().set("display", "flex").set("gap", "var(--lumo-space-l)").set("align-items", "baseline");
        lblTotal.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("font-weight", "700");
        HorizontalLayout pie = new HorizontalLayout(totales, btnCancelar, btnGuardar);
        pie.setWidthFull();
        pie.setAlignItems(Alignment.CENTER);
        pie.expand(totales);

        add(titulo, fila1, fila2, barra, grid, pie);
        setFlexGrow(1, grid);

        cambiarTipo();
        actualizarTotales();
    }

    private static Span etiquetaTotal(String texto, Span valor) {
        Span s = new Span(texto + ": ");
        s.getStyle().set("color", "var(--lumo-secondary-text-color)");
        Span c = new Span(s, valor);
        return c;
    }

    // ============================================================
    // TIPO DE LIQUIDACIÓN
    // ============================================================
    private void cambiarTipo() {
        String tipo = rbTipo.getValue();
        boolean nota = DevolucionProveedor.NOTA_CREDITO.equals(tipo);
        boolean cambio = DevolucionProveedor.CAMBIO.equals(tipo);
        cbFactura.setVisible(nota);
        if (!nota) {
            cbFactura.clear();
        }
        colLoteNuevo.setVisible(cambio);
        colVenceNuevo.setVisible(cambio);
        lblAyudaTipo.setText(switch (tipo) {
            case DevolucionProveedor.NOTA_CREDITO ->
                "Rebaja lo que se le debe al proveedor. Si no elige factura (o sobra), queda como crédito a favor.";
            case DevolucionProveedor.CAMBIO ->
                "El proveedor repone la misma cantidad: indique el lote y vencimiento de la mercancía nueva.";
            case DevolucionProveedor.EFECTIVO -> "El dinero entra a la caja abierta.";
            default -> "";
        });
    }

    private void cargarFacturas() {
        cbFactura.clear();
        Proveedor p = cbProveedor.getValue();
        if (p == null) {
            cbFactura.setItems(new ArrayList<>());
            return;
        }
        List<FacturaDeCompra> facturas = devolucionService.getFacturasPendientes(p.getCodigo());
        java.util.Map<Integer, BigDecimal> pendientes = new java.util.HashMap<>();
        facturas.forEach(f -> pendientes.put(f.getCodigo(), devolucionService.getPendienteFactura(f.getCodigo())));
        cbFactura.setItems(facturas);
        cbFactura.setItemLabelGenerator(f -> "#" + f.getCodigo()
                + (f.getNumeroFactura() != null ? " · " + f.getNumeroFactura() : "")
                + " · pendiente " + EstiloConsulta.moneda(pendientes.get(f.getCodigo())));
        cbFactura.setHelperText(facturas.isEmpty() ? "El proveedor no tiene facturas pendientes" : null);
    }

    // ============================================================
    // LÍNEAS
    // ============================================================
    private void configurarGrid() {
        EstiloConsulta.aplicarGrid(grid);
        grid.setWidthFull();
        grid.setItems(lineas);

        grid.addColumn(DetalleDevolucionProveedor::getDescripcionArticulo).setHeader("Artículo")
                .setAutoWidth(true).setFlexGrow(2);
        grid.addColumn(d -> d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion() + " ×"
                + CANTIDAD.format(d.getFactorConversion()) : "").setHeader("Empaque").setAutoWidth(true);
        grid.addComponentColumn(d -> {
            ComboBox<LoteAlmacen> cb = LoteUi.selectorSalida(loteService, d.getArticulo(), cbAlmacen.getValue(),
                    d.getLote(), lote -> {
                        d.setLote(lote);
                        sugerirCosto(d);
                        grid.getDataProvider().refreshItem(d);
                        actualizarTotales();
                    });
            if (d.getArticulo().isControlaLote() && cbAlmacen.getValue() != null) {
                cb.setPlaceholder("Elija el lote");
                cb.setClearButtonVisible(false);
            }
            return cb;
        }).setHeader("Lote que se devuelve").setAutoWidth(true);
        grid.addComponentColumn(d -> numero(d.getCantidad(), "90px", v -> d.setCantidad(v), d))
                .setHeader("Cantidad").setAutoWidth(true);
        grid.addComponentColumn(d -> numero(d.getCostoUnitario(), "110px", v -> d.setCostoUnitario(v), d))
                .setHeader("Costo unit.").setAutoWidth(true);
        grid.addComponentColumn(d -> numero(d.getPorcentajeItbis(), "70px", v -> d.setPorcentajeItbis(v), d))
                .setHeader("% ITBIS").setAutoWidth(true);
        EstiloConsulta.monto(grid.addColumn(d -> EstiloConsulta.moneda(d.getTotal()))).setHeader("Total")
                .setAutoWidth(true);
        colLoteNuevo = grid.addComponentColumn(d -> LoteUi.numeroLote(d.getArticulo(), d.getNumeroLoteNuevo(),
                d::setNumeroLoteNuevo)).setHeader("Lote nuevo").setAutoWidth(true);
        colVenceNuevo = grid.addComponentColumn(d -> LoteUi.vencimiento(d.getArticulo(), d.getFechaVencimientoNueva(),
                d::setFechaVencimientoNueva)).setHeader("Vence (nuevo)").setAutoWidth(true);
        grid.addComponentColumn(d -> {
            Button quitar = new Button(VaadinIcon.TRASH.create(), e -> {
                lineas.remove(d);
                grid.getDataProvider().refreshAll();
                actualizarTotales();
            });
            quitar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            return quitar;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);
    }

    private BigDecimalField numero(BigDecimal valor, String ancho,
            java.util.function.Consumer<BigDecimal> alCambiar, DetalleDevolucionProveedor d) {
        BigDecimalField f = new BigDecimalField();
        f.setWidth(ancho);
        f.setValue(valor);
        f.setValueChangeMode(ValueChangeMode.ON_BLUR);
        f.addValueChangeListener(e -> {
            BigDecimal v = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            if (v.signum() < 0) {
                v = BigDecimal.ZERO;
            }
            alCambiar.accept(v);
            DevolucionProveedorService.calcularLinea(d);
            grid.getDataProvider().refreshItem(d);
            actualizarTotales();
        });
        return f;
    }

    private void agregarArticulo() {
        if (cbAlmacen.getValue() == null) {
            ClaseUtil.mostrarNotificacion("Seleccione el almacén de donde sale la mercancía", NotificationVariant.LUMO_WARNING);
            cbAlmacen.focus();
            return;
        }
        new ArticuloDialogoFilteringView(articuloService, articulo -> {
            if (articulo != null) {
                // Solo presentaciones de compra; una sola → directo, sin diálogo
                EmpaqueSeleccionDialogo.seleccionarParaCompra(articulo, empaqueService, empaque -> {
                    if (empaque != null) {
                        agregarLinea(articulo, empaque, BigDecimal.ONE, null);
                    }
                });
            }
        }).open();
    }

    private DetalleDevolucionProveedor agregarLinea(Articulo articulo, ArticuloEmpaque empaque, BigDecimal cantidad,
            Lote lote) {
        DetalleDevolucionProveedor d = new DetalleDevolucionProveedor();
        d.setArticulo(articulo);
        d.setDescripcionArticulo(articulo.getDescripcion());
        d.setArticuloEmpaque(empaque);
        d.setFactorConversion(empaque.getFactorConversion());
        d.setCantidad(cantidad);
        d.setLote(lote);
        d.setPorcentajeItbis(articulo.getExentoItbis() ? BigDecimal.ZERO : ITBIS_DEFECTO);
        sugerirCosto(d);
        lineas.add(d);
        grid.getDataProvider().refreshAll();
        actualizarTotales();
        return d;
    }

    /** Costo sugerido: el del lote (por unidad base x factor) o el precio de compra del empaque. */
    private void sugerirCosto(DetalleDevolucionProveedor d) {
        BigDecimal costo = null;
        if (d.getLote() != null && d.getLote().getCostoUnitario() != null) {
            costo = d.getLote().getCostoUnitario().multiply(d.getFactorConversion()).setScale(2, RoundingMode.HALF_UP);
        } else if (d.getArticuloEmpaque().getPrecioCompra() != null) {
            costo = d.getArticuloEmpaque().getPrecioCompra();
        } else if (d.getArticulo().getPrecioCompra() != null) {
            costo = d.getArticulo().getPrecioCompra().multiply(d.getFactorConversion());
        }
        if (costo != null && (d.getCostoUnitario().signum() == 0 || d.getLote() != null)) {
            d.setCostoUnitario(costo);
        }
        DevolucionProveedorService.calcularLinea(d);
    }

    /** Agrega los lotes vencidos del almacén (del proveedor elegido, si tiene) que no estén ya en la lista. */
    private void cargarVencidos() {
        Almacen almacen = cbAlmacen.getValue();
        if (almacen == null) {
            ClaseUtil.mostrarNotificacion("Seleccione el almacén", NotificationVariant.LUMO_WARNING);
            cbAlmacen.focus();
            return;
        }
        Proveedor p = cbProveedor.getValue();
        LocalDate hoy = LocalDate.now();
        int agregados = 0;
        for (LoteAlmacen la : loteService.getLotesConExistencia()) {
            Lote lote = la.getLote();
            if (!Objects.equals(la.getAlmacen().getCodigo(), almacen.getCodigo()) || la.getExistencia().signum() <= 0
                    || !lote.isVencido(hoy)) {
                continue;
            }
            if (p != null && lote.getProveedor() != null && !Objects.equals(lote.getProveedor().getCodigo(), p.getCodigo())) {
                continue;
            }
            if (lineas.stream().anyMatch(d -> d.getLote() != null && Objects.equals(d.getLote().getCodigo(), lote.getCodigo()))) {
                continue;
            }
            if (agregarLineaDeLote(la) != null) {
                agregados++;
            }
        }
        if (agregados == 0) {
            ClaseUtil.mostrarNotificacion("No hay lotes vencidos " + (p != null ? "de " + p.getNombre() + " " : "")
                    + "con existencia en " + almacen.getNombre(), NotificationVariant.LUMO_CONTRAST);
            return;
        }
        cbMotivo.setValue(DevolucionProveedor.MOTIVO_VENCIDO);
        ClaseUtil.mostrarNotificacion(agregados + " lote(s) vencido(s) agregados", NotificationVariant.LUMO_SUCCESS);
    }

    /** Línea con toda la existencia del lote, en unidad base. */
    private DetalleDevolucionProveedor agregarLineaDeLote(LoteAlmacen la) {
        Articulo articulo = la.getLote().getArticulo();
        ArticuloEmpaque base = empaqueService.getEmpaqueBase(articulo.getCodigo()).orElse(null);
        if (base == null) {
            return null;
        }
        return agregarLinea(articulo, base, la.getExistencia(), la.getLote());
    }

    /** Desde "Lotes y vencimientos": ?loteAlmacen=codigo entra con ese lote, su almacén y su proveedor. */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<String> valor = event.getLocation().getQueryParameters().getParameters().get(PARAM_LOTE_ALMACEN);
        if (valor == null || valor.isEmpty() || !lineas.isEmpty()) {
            return;
        }
        try {
            Integer codigo = Integer.valueOf(valor.get(0));
            loteService.getLotesConExistencia().stream()
                    .filter(la -> Objects.equals(la.getCodigo(), codigo) && la.getExistencia().signum() > 0)
                    .findFirst()
                    .ifPresent(la -> {
                        cbAlmacen.getListDataView().getItems()
                                .filter(a -> Objects.equals(a.getCodigo(), la.getAlmacen().getCodigo()))
                                .findFirst().ifPresent(cbAlmacen::setValue);
                        if (la.getLote().getProveedor() != null) {
                            cbProveedor.getListDataView().getItems()
                                    .filter(p -> Objects.equals(p.getCodigo(), la.getLote().getProveedor().getCodigo()))
                                    .findFirst().ifPresent(cbProveedor::setValue);
                        }
                        cbMotivo.setValue(la.getLote().isVencido(LocalDate.now())
                                ? DevolucionProveedor.MOTIVO_VENCIDO : DevolucionProveedor.MOTIVO_DANADO);
                        agregarLineaDeLote(la);
                    });
        } catch (NumberFormatException ignorado) {
            // parámetro inválido: se abre vacío
        }
    }

    private void actualizarTotales() {
        BigDecimal sub = BigDecimal.ZERO;
        BigDecimal itbis = BigDecimal.ZERO;
        for (DetalleDevolucionProveedor d : lineas) {
            sub = sub.add(d.getSubTotal());
            itbis = itbis.add(d.getItbis());
        }
        lblSubTotal.setText(EstiloConsulta.moneda(sub));
        lblItbis.setText(EstiloConsulta.moneda(itbis));
        lblTotal.setText(EstiloConsulta.moneda(sub.add(itbis)));
    }

    // ============================================================
    // GUARDAR
    // ============================================================
    private void guardar() {
        if (cbProveedor.getValue() == null) {
            ClaseUtil.mostrarNotificacion("Seleccione el proveedor", NotificationVariant.LUMO_WARNING);
            cbProveedor.focus();
            return;
        }
        if (cbAlmacen.getValue() == null) {
            ClaseUtil.mostrarNotificacion("Seleccione el almacén", NotificationVariant.LUMO_WARNING);
            cbAlmacen.focus();
            return;
        }
        if (lineas.isEmpty()) {
            ClaseUtil.mostrarNotificacion("Agregue al menos un artículo", NotificationVariant.LUMO_WARNING);
            return;
        }
        for (DetalleDevolucionProveedor d : lineas) {
            if (d.getArticulo().isControlaLote() && d.getLote() == null) {
                ClaseUtil.mostrarNotificacion("Elija el lote que se devuelve de " + d.getDescripcionArticulo(),
                        NotificationVariant.LUMO_WARNING);
                return;
            }
            if (d.getCantidad().signum() <= 0) {
                ClaseUtil.mostrarNotificacion("La cantidad de " + d.getDescripcionArticulo() + " debe ser mayor que cero",
                        NotificationVariant.LUMO_WARNING);
                return;
            }
        }

        DevolucionProveedor dev = new DevolucionProveedor();
        dev.setProveedor(cbProveedor.getValue());
        dev.setAlmacen(cbAlmacen.getValue());
        dev.setTipoLiquidacion(rbTipo.getValue());
        dev.setMotivo(cbMotivo.getValue());
        dev.setObservacion(txtObservacion.getValue() == null || txtObservacion.getValue().isBlank()
                ? null : txtObservacion.getValue().trim());
        dev.setFacturaCompra(cbFactura.getValue());

        btnGuardar.setEnabled(false);
        try {
            DevolucionProveedor guardada = devolucionService.registrar(dev, new ArrayList<>(lineas), USUARIO_ACTUAL);
            String detalle = switch (guardada.getTipoLiquidacion()) {
                case DevolucionProveedor.NOTA_CREDITO -> guardada.getCreditoDisponible().signum() > 0
                        ? "Crédito a favor disponible: " + EstiloConsulta.moneda(guardada.getCreditoDisponible())
                        : "Aplicado a la factura: " + EstiloConsulta.moneda(guardada.getCreditoAplicado());
                case DevolucionProveedor.CAMBIO -> "La reposición entró al inventario.";
                default -> "Entraron " + EstiloConsulta.moneda(guardada.getTotal()) + " a la caja.";
            };
            ClaseUtil.mostrarNotificacion("Devolución " + guardada.getNumero() + " registrada. " + detalle,
                    NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(ConsultaDevolucionProveedorView.class);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ClaseUtil.mostrarNotificacion(ex.getMessage(), NotificationVariant.LUMO_ERROR);
            btnGuardar.setEnabled(true);
        } catch (RuntimeException ex) {
            ClaseUtil.mostrarNotificacion("No se pudo guardar la devolución: " + ex.getMessage(),
                    NotificationVariant.LUMO_ERROR);
            btnGuardar.setEnabled(true);
        }
    }
}
