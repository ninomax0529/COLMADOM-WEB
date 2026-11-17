package com.maxsoft.application.view.compra.recepcion;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.CondicionPago;
import com.maxsoft.application.modelo.DetalleOrdendeDeCompra;
import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.OrdenDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.servicio.interfaces.EstadoDocumentoService;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;
import com.maxsoft.application.servicio.interfaces.cxp.CondicionPagoService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Recepción de mercancía: desde una orden de compra (solo lo pendiente) o
 * manual (artículos con F2). Genera la entrada de inventario, el lote de cada
 * artículo que controla lote y la factura de compra.
 */
@PermitAll
@PageTitle("Recepción de Mercancía | MaxSoft ERP")
@Route(value = "compra/recepcion")
public class RecepcionMercanciaView extends VerticalLayout {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RecepcionMercanciaView.class);


    private static final Integer ORIGEN_ORDEN_COMPRA = 8;
    private static final BigDecimal ITBIS_DEFECTO = new BigDecimal("18");
    private static final DecimalFormat CANTIDAD = new DecimalFormat("#,##0.##");

    private final RecepcionMercanciaService recepcionService;
    private final OrdenDeCompraService ordenCompraService;
    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService;
    private final EstadoDocumentoService estadoDocumentoService;

    // Estado interno
    private final List<DetalleRecepcionMercancia> detalles = new ArrayList<>();
    // IdentityHashMap: las entidades JPA suelen sobrescribir equals/hashCode por id (null aquí)
    // Cantidad pendiente de la orden por cada línea (solo líneas que vienen de una OC)
    private final Map<DetalleRecepcionMercancia, BigDecimal> pendientePorDetalle = new IdentityHashMap<>();
    // % de ITBIS de cada línea (evita recalcularlo desde montos redondeados)
    private final Map<DetalleRecepcionMercancia, BigDecimal> itbisPorcPorDetalle = new IdentityHashMap<>();

    // Encabezado
    private final ComboBox<TipoDocumento> cbTipoDocumento = new ComboBox<>("Origen");
    private final ComboBox<OrdenDeCompra> cbOrdenCompra = new ComboBox<>("Orden de compra");
    private final Button btnOrdenesPendientes = new Button("Ver pendientes", VaadinIcon.SEARCH.create());
    private final HorizontalLayout layoutOrden = new HorizontalLayout();
    private final TextField txtDocOrigen = new TextField("N° conduce / NCF");
    private final ComboBox<Proveedor> cbSuplidor = new ComboBox<>("Proveedor");
    private final ComboBox<Almacen> cbAlmacenDestino = new ComboBox<>("Almacén destino");
    private final ComboBox<CondicionPago> cbCondicionPago = new ComboBox<>("Condición de pago");
    private final DatePicker dtFecha = new DatePicker("Fecha");
    private final TextField txtComentario = new TextField("Comentario");

    // Barra de artículos (solo recepción manual)
    private final HorizontalLayout barraArticulos = new HorizontalLayout();
    private final Span lblAyuda = new Span();

    // Grid
    private final Grid<DetalleRecepcionMercancia> grid = new Grid<>(DetalleRecepcionMercancia.class, false);
    private Grid.Column<DetalleRecepcionMercancia> colPendienteOc;

    // Totales y acciones
    private final Span lblSubTotal = new Span();
    private final Span lblItbis = new Span();
    private final Span lblTotal = new Span();
    private final Button btnGuardar = new Button("Procesar recepción", VaadinIcon.CHECK.create());

    public RecepcionMercanciaView(RecepcionMercanciaService recepcionService,
            OrdenDeCompraService ordenCompraService,
            ProveedorService proveedorService,
            ArticuloService articuloService,
            AlmacenService almacenService,
            ArticuloEmpaqueService articuloEmpaqueService,
            TipoDocumentoService tipoDocumentoService,
            EstadoDocumentoService estadoDocumentoService,
            CondicionPagoService condicionPagoService) {

        this.recepcionService = recepcionService;
        this.ordenCompraService = ordenCompraService;
        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;
        this.estadoDocumentoService = estadoDocumentoService;

        setSizeFull();
        setSpacing(false);
        addClassName("fv-vista");

        H2 titulo = new H2("Recepción de Mercancía");
        titulo.addClassName("fv-titulo");

        configurarEncabezado(proveedorService, almacenService, tipoDocumentoService, condicionPagoService);

        HorizontalLayout fila1 = new HorizontalLayout(cbTipoDocumento, layoutOrden, cbSuplidor, txtDocOrigen);
        fila1.setAlignItems(Alignment.BASELINE);
        fila1.getStyle().set("flex-wrap", "wrap");
        HorizontalLayout fila2 = new HorizontalLayout(cbAlmacenDestino, cbCondicionPago, dtFecha, txtComentario);
        fila2.setAlignItems(Alignment.BASELINE);
        fila2.getStyle().set("flex-wrap", "wrap");

        Button btnArticulo = new Button("Artículo (F2)", VaadinIcon.PLUS.create(), e -> agregarArticulo());
        btnArticulo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnArticulo.addClickShortcut(Key.F2);
        lblAyuda.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
        barraArticulos.add(btnArticulo, lblAyuda);
        barraArticulos.setAlignItems(Alignment.CENTER);

        configurarGrid();

        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardar.addClickListener(e -> procesarRecepcion());
        Button btnCancelar = new Button("Cancelar", e -> UI.getCurrent().navigate(ConsultaRecepcionMercanciaView.class));

        Div totales = new Div(etiquetaTotal("Subtotal", lblSubTotal), etiquetaTotal("ITBIS", lblItbis),
                etiquetaTotal("Total", lblTotal));
        totales.getStyle().set("display", "flex").set("gap", "var(--lumo-space-l)").set("align-items", "baseline");
        lblTotal.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("font-weight", "700");
        HorizontalLayout pie = new HorizontalLayout(totales, btnCancelar, btnGuardar);
        pie.setWidthFull();
        pie.setAlignItems(Alignment.CENTER);
        pie.expand(totales);

        add(titulo, fila1, fila2, barraArticulos, grid, pie);
        setFlexGrow(1, grid);

        actualizarTotales();
        cargarDatosIniciales();
    }

    private static Span etiquetaTotal(String texto, Span valor) {
        Span s = new Span(texto + ": ");
        s.getStyle().set("color", "var(--lumo-secondary-text-color)");
        return new Span(s, valor);
    }

    // =====================================================================
    // ENCABEZADO
    // =====================================================================
    private void configurarEncabezado(ProveedorService proveedorService, AlmacenService almacenService,
            TipoDocumentoService tipoDocumentoService, CondicionPagoService condicionPagoService) {

        cbTipoDocumento.setItems(tipoDocumentoService.getLista());
        cbTipoDocumento.setItemLabelGenerator(TipoDocumento::getNombre);
        cbTipoDocumento.setWidth("200px");
        cbTipoDocumento.addValueChangeListener(e
                -> cambiarTipoOrigen(e.getValue() != null ? e.getValue().getCodigo() : 0));

        cbOrdenCompra.setItemLabelGenerator(o -> o.getNumero() + " - "
                + (o.getProveedor() != null ? o.getProveedor().getNombre() : ""));
        cbOrdenCompra.setPlaceholder("Seleccione la orden...");
        cbOrdenCompra.setClearButtonVisible(true);
        cbOrdenCompra.setWidth("280px");
        cbOrdenCompra.addValueChangeListener(e -> cargarDesdeOrdenCompra(e.getValue()));

        btnOrdenesPendientes.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnOrdenesPendientes.addClickListener(e -> abrirDialogoOrdenesPendientes());

        layoutOrden.setPadding(false);
        layoutOrden.setSpacing(true);
        layoutOrden.setAlignItems(Alignment.BASELINE);
        layoutOrden.add(cbOrdenCompra, btnOrdenesPendientes);

        cbSuplidor.setItems(proveedorService.getLista());
        cbSuplidor.setItemLabelGenerator(Proveedor::getNombre);
        cbSuplidor.setWidth("280px");
        cbSuplidor.setRequired(true);

        txtDocOrigen.setWidth("180px");
        txtDocOrigen.setMaxLength(50);

        cbAlmacenDestino.setItems(almacenService.getLista());
        cbAlmacenDestino.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenDestino.setWidth("180px");
        cbAlmacenDestino.setRequired(true);
        // Si el almacén cambia (antes o después de cargar la OC), se aplica a todas las líneas
        cbAlmacenDestino.addValueChangeListener(e -> {
            for (DetalleRecepcionMercancia d : detalles) {
                d.setAlmacen(e.getValue());
            }
            grid.getDataProvider().refreshAll();
        });

        // Condición de pago (contado, crédito 30 días, etc.)
        cbCondicionPago.setItems(condicionPagoService.getLista());
        cbCondicionPago.setItemLabelGenerator(CondicionPago::getNombre);
        cbCondicionPago.setAllowCustomValue(false);
        cbCondicionPago.setRequired(true);
        cbCondicionPago.setWidth("200px");
        cbCondicionPago.setPlaceholder("Seleccione...");

        dtFecha.setValue(LocalDate.now());
        dtFecha.setWidth("150px");

        txtComentario.setWidth("300px");
        txtComentario.setMaxLength(250);
        txtComentario.setRequired(true);
        txtComentario.setPlaceholder("Quién entregó, observaciones...");
    }

    private void cambiarTipoOrigen(Integer tipo) {
        if (ORIGEN_ORDEN_COMPRA.equals(tipo)) {
            layoutOrden.setVisible(true);
            cargarOrdenesPendientes();
        } else {
            cbOrdenCompra.clear(); // dispara cargarDesdeOrdenCompra(null) que limpia las líneas de la OC
            layoutOrden.setVisible(false);
            cbSuplidor.setReadOnly(false);
        }
        actualizarModo();
    }

    /** Con una OC seleccionada solo se recibe lo que la orden contiene: no se agregan artículos. */
    private void actualizarModo() {
        boolean conOrden = cbOrdenCompra.getValue() != null;
        barraArticulos.setVisible(!conOrden);
        colPendienteOc.setVisible(conOrden);
        if (conOrden) {
            lblAyuda.setText("");
        } else if (layoutOrden.isVisible()) {
            lblAyuda.setText("Elija la orden de compra a recibir, o cambie el origen para una recepción manual.");
        } else {
            lblAyuda.setText("Agregue los artículos recibidos. Lote vacío = lote automático del día.");
        }
    }

    // =====================================================================
    // ÓRDENES DE COMPRA PENDIENTES
    // =====================================================================
    /** Órdenes (vía OrdenDeCompraService) que todavía tienen mercancía por recibir. */
    private List<OrdenDeCompra> obtenerOrdenesPendientes() {
        return ordenCompraService.getListaOcPendiente().stream()
                .filter(o -> !"COMPLETADA".equalsIgnoreCase(o.getEstado())
                && (o.getAnulada() == null || !o.getAnulada())
                && tienePendiente(o))
                .toList();
    }

    private void cargarOrdenesPendientes() {
        cbOrdenCompra.setItems(obtenerOrdenesPendientes());
    }

    private BigDecimal pendienteDeLinea(DetalleOrdendeDeCompra d) {
        BigDecimal p = d.getPendiente() != null ? d.getPendiente() : d.getCantidad();
        return p != null ? p : BigDecimal.ZERO;
    }

    private boolean tienePendiente(OrdenDeCompra o) {
        if (o.getDetalleOrdendeDeCompraCollection() == null) {
            return true; // no se puede determinar: se deja visible
        }
        return o.getDetalleOrdendeDeCompraCollection().stream()
                .anyMatch(d -> pendienteDeLinea(d).compareTo(BigDecimal.ZERO) > 0);
    }

    private long contarLineasPendientes(OrdenDeCompra o) {
        if (o.getDetalleOrdendeDeCompraCollection() == null) {
            return 0;
        }
        return o.getDetalleOrdendeDeCompraCollection().stream()
                .filter(d -> pendienteDeLinea(d).compareTo(BigDecimal.ZERO) > 0)
                .count();
    }

    private BigDecimal sumarCantidadPendiente(OrdenDeCompra o) {
        if (o.getDetalleOrdendeDeCompraCollection() == null) {
            return BigDecimal.ZERO;
        }
        return o.getDetalleOrdendeDeCompraCollection().stream()
                .map(this::pendienteDeLinea)
                .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Diálogo con las órdenes pendientes por recibir; al elegir una se carga en la recepción. */
    private void abrirDialogoOrdenesPendientes() {
        List<OrdenDeCompra> pendientes = obtenerOrdenesPendientes();
        // El combo usa la misma lista para que la orden elegida en el diálogo exista en sus items
        cbOrdenCompra.setItems(pendientes);

        if (pendientes.isEmpty()) {
            ClaseUtil.mostrarNotificacion("No hay órdenes de compra pendientes por recibir.",
                    NotificationVariant.LUMO_CONTRAST);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Órdenes de compra pendientes por recibir");
        dialog.setWidth("900px");

        TextField txtFiltro = new TextField();
        txtFiltro.setPlaceholder("Filtrar por número o proveedor...");
        txtFiltro.setPrefixComponent(VaadinIcon.SEARCH.create());
        txtFiltro.setClearButtonVisible(true);
        txtFiltro.setValueChangeMode(ValueChangeMode.LAZY);
        txtFiltro.setWidthFull();

        Grid<OrdenDeCompra> gridOc = new Grid<>(OrdenDeCompra.class, false);
        EstiloConsulta.aplicarGrid(gridOc);
        gridOc.addColumn(OrdenDeCompra::getNumero).setHeader("N° Orden").setAutoWidth(true);
        gridOc.addColumn(o -> o.getProveedor() != null ? o.getProveedor().getNombre() : "-")
                .setHeader("Proveedor").setFlexGrow(2);
        gridOc.addColumn(OrdenDeCompra::getEstado).setHeader("Estado").setAutoWidth(true);
        gridOc.addColumn(this::contarLineasPendientes).setHeader("Líneas pend.").setAutoWidth(true);
        gridOc.addColumn(o -> CANTIDAD.format(sumarCantidadPendiente(o)))
                .setHeader("Cant. pendiente").setAutoWidth(true);
        gridOc.addComponentColumn(o -> {
            Button btn = new Button("Recibir", VaadinIcon.DOWNLOAD.create(),
                    e -> seleccionarOrdenPendiente(o, dialog));
            btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            return btn;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);

        gridOc.setItems(pendientes);
        gridOc.setHeight("380px");
        gridOc.addItemDoubleClickListener(e -> seleccionarOrdenPendiente(e.getItem(), dialog));

        txtFiltro.addValueChangeListener(e -> {
            String f = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            gridOc.getListDataView().setFilter(o
                    -> f.isEmpty()
                    || (o.getNumero() != null && o.getNumero().toLowerCase().contains(f))
                    || (o.getProveedor() != null && o.getProveedor().getNombre() != null
                    && o.getProveedor().getNombre().toLowerCase().contains(f)));
        });

        VerticalLayout contenido = new VerticalLayout(txtFiltro, gridOc);
        contenido.setPadding(false);
        contenido.setSpacing(true);
        dialog.add(contenido);
        dialog.getFooter().add(new Button("Cerrar", e -> dialog.close()));
        dialog.open();
    }

    private void seleccionarOrdenPendiente(OrdenDeCompra oc, Dialog dialog) {
        dialog.close();
        cbOrdenCompra.setValue(oc); // dispara cargarDesdeOrdenCompra(oc)
    }

    // =====================================================================
    // RECIBIR DESDE ORDEN DE COMPRA
    // =====================================================================
    private void cargarDesdeOrdenCompra(OrdenDeCompra oc) {

        if (oc == null) {
            // Se quitó la orden: liberar proveedor y limpiar solo si las líneas venían de una OC
            if (!pendientePorDetalle.isEmpty()) {
                detalles.clear();
                pendientePorDetalle.clear();
                itbisPorcPorDetalle.clear();
                actualizarGridYTotales();
            }
            cbSuplidor.setReadOnly(false);
            actualizarModo();
            return;
        }

        // Encabezado en base a la orden
        cbSuplidor.setReadOnly(false);
        cbSuplidor.setValue(oc.getProveedor());
        cbSuplidor.setReadOnly(true);
        txtDocOrigen.setValue(oc.getNumero() != null ? oc.getNumero() : "");

        // Líneas en base al detalle pendiente de la orden
        detalles.clear();
        pendientePorDetalle.clear();
        itbisPorcPorDetalle.clear();

        int sinEmpaque = 0;

        if (oc.getDetalleOrdendeDeCompraCollection() != null) {
            for (DetalleOrdendeDeCompra doc : oc.getDetalleOrdendeDeCompraCollection()) {

                BigDecimal pendiente = doc.getPendiente() != null ? doc.getPendiente() : doc.getCantidad();
                if (pendiente == null || pendiente.compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // línea ya recibida completa
                }

                BigDecimal precio = doc.getPrecioUnitario() != null ? doc.getPrecioUnitario() : BigDecimal.ZERO;

                DetalleRecepcionMercancia det = new DetalleRecepcionMercancia();
                // codigo queda null (id de la fila en BD); el grid identifica las filas por instancia
                det.setArticulo(doc.getArticulo());
                det.setAlmacen(cbAlmacenDestino.getValue()); // se actualiza si luego eligen otro almacén
                det.setDescripcionArticulo(doc.getDescripcionArticulo());
                det.setCantidadRecibida(pendiente);       // sugerido: todo lo pendiente
                det.setPrecioUnitario(precio);

                ArticuloEmpaque emp = doc.getArticuloEmpaque();
                BigDecimal factor = BigDecimal.ONE;
                if (emp != null) {
                    det.setArticuloEmpaque(emp);
                    if (emp.getFactorConversion() != null) {
                        factor = emp.getFactorConversion();
                    }
                    if (emp.getUnidadEmpaque() != null) {
                        det.setUnidad(emp.getUnidadEmpaque());
                        det.setNombreUnidad(emp.getUnidadEmpaque().getDescripcion());
                    }
                } else {
                    sinEmpaque++;
                }
                det.setFactorConversion(factor);

                // ITBIS: se conserva el % que tenía la línea en la orden
                BigDecimal porc = BigDecimal.ZERO;
                if (doc.getItbis() != null && doc.getSubTotal() != null
                        && doc.getSubTotal().compareTo(BigDecimal.ZERO) > 0) {
                    porc = doc.getItbis().multiply(BigDecimal.valueOf(100))
                            .divide(doc.getSubTotal(), 4, RoundingMode.HALF_UP);
                }

                detalles.add(det);
                pendientePorDetalle.put(det, pendiente);
                itbisPorcPorDetalle.put(det, porc);
                calcularLinea(det);
            }
        }

        actualizarModo();
        actualizarGridYTotales();

        if (detalles.isEmpty()) {
            ClaseUtil.mostrarNotificacion("La orden seleccionada no tiene artículos pendientes por recibir.",
                    NotificationVariant.LUMO_WARNING);
        } else if (sinEmpaque > 0) {
            ClaseUtil.mostrarNotificacion(sinEmpaque + " artículo(s) no tienen empaque configurado; se asumió factor 1.",
                    NotificationVariant.LUMO_WARNING);
        }
    }

    // =====================================================================
    // AGREGAR ARTÍCULO (recepción manual)
    // =====================================================================
    private void agregarArticulo() {
        if (!barraArticulos.isVisible()) {
            return; // con orden de compra no se agregan artículos (F2 sigue activo)
        }
        if (cbAlmacenDestino.getValue() == null) {
            ClaseUtil.mostrarNotificacion("Seleccione el almacén destino antes de agregar artículos",
                    NotificationVariant.LUMO_WARNING);
            cbAlmacenDestino.focus();
            return;
        }
        new ArticuloDialogoFilteringView(articuloService, articulo -> {
            if (articulo != null) {
                new EmpaqueSeleccionDialogo(articulo, articuloEmpaqueService, empaque -> {
                    if (empaque != null) {
                        agregarLinea(articulo, empaque);
                    }
                }).open();
            }
        }).open();
    }

    private void agregarLinea(Articulo articulo, ArticuloEmpaque empaque) {
        boolean yaExiste = detalles.stream().anyMatch(d -> d.getArticulo() != null
                && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())
                && d.getArticuloEmpaque() != null
                && Objects.equals(d.getArticuloEmpaque().getCodigo(), empaque.getCodigo()));
        if (yaExiste) {
            ClaseUtil.mostrarNotificacion("Esta presentación del artículo ya fue agregada; cambie la cantidad en la lista.",
                    NotificationVariant.LUMO_WARNING);
            return;
        }

        BigDecimal factor = empaque.getFactorConversion() != null ? empaque.getFactorConversion() : BigDecimal.ONE;
        BigDecimal precio = empaque.getPrecioCompra() != null ? empaque.getPrecioCompra()
                : articulo.getPrecioCompra() != null ? articulo.getPrecioCompra().multiply(factor) : BigDecimal.ZERO;

        DetalleRecepcionMercancia det = new DetalleRecepcionMercancia();
        // codigo queda null (id de la fila en BD); el grid identifica las filas por instancia
        det.setArticulo(articulo);
        det.setArticuloEmpaque(empaque);
        det.setAlmacen(cbAlmacenDestino.getValue());
        det.setFactorConversion(factor);
        if (empaque.getUnidadEmpaque() != null) {
            det.setUnidad(empaque.getUnidadEmpaque());
            det.setNombreUnidad(empaque.getUnidadEmpaque().getDescripcion());
        }
        det.setDescripcionArticulo(articulo.getDescripcion());
        det.setCantidadRecibida(BigDecimal.ONE);
        det.setPrecioUnitario(precio);

        detalles.add(det);
        itbisPorcPorDetalle.put(det, articulo.getExentoItbis() ? BigDecimal.ZERO : ITBIS_DEFECTO);
        calcularLinea(det);
        actualizarGridYTotales();
    }

    // =====================================================================
    // GRID
    // =====================================================================
    private void configurarGrid() {
        EstiloConsulta.aplicarGrid(grid);
        grid.setWidthFull();
        // Las líneas nuevas tienen codigo null y el equals de la entidad las daría todas por
        // iguales (dos empaques del mismo artículo se pisaban): se identifican por instancia.
        java.util.Map<DetalleRecepcionMercancia, Integer> ids = new IdentityHashMap<>();
        grid.setItems(new com.vaadin.flow.data.provider.ListDataProvider<>(detalles) {
            @Override
            public Object getId(DetalleRecepcionMercancia item) {
                return ids.computeIfAbsent(item, k -> ids.size() + 1);
            }
        });

        grid.addColumn(d -> d.getArticulo() != null ? d.getArticulo().getCodigo() : "-")
                .setHeader("Código").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(DetalleRecepcionMercancia::getDescripcionArticulo).setHeader("Artículo")
                .setAutoWidth(true).setFlexGrow(2);
        grid.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion() + " ×" + CANTIDAD.format(factor(d))
                : "-").setHeader("Empaque").setAutoWidth(true);

        // Cantidad pendiente según la orden (solo visible si viene de una OC)
        colPendienteOc = grid.addColumn(d -> {
            BigDecimal p = pendientePorDetalle.get(d);
            return p != null ? CANTIDAD.format(p) : "-";
        }).setHeader("Pendiente OC").setAutoWidth(true);

        // Cantidad recibida: no puede superar lo pendiente de la orden
        grid.addComponentColumn(d -> numero(d.getCantidadRecibida(), "90px", v -> {
            BigDecimal max = pendientePorDetalle.get(d);
            if (v.signum() <= 0) {
                ClaseUtil.mostrarNotificacion("La cantidad debe ser mayor que cero", NotificationVariant.LUMO_WARNING);
                return;
            }
            if (max != null && v.compareTo(max) > 0) {
                ClaseUtil.mostrarNotificacion("No puede recibir más de lo pendiente en la orden ("
                        + CANTIDAD.format(max) + ")", NotificationVariant.LUMO_ERROR);
                return;
            }
            d.setCantidadRecibida(v);
        }, d)).setHeader("Cantidad").setAutoWidth(true);

        grid.addColumn(d -> d.getCantidadFisicaBase() != null ? CANTIDAD.format(d.getCantidadFisicaBase()) : "0")
                .setHeader("Unid. base").setAutoWidth(true);

        // Control de lotes: número (vacío = automático) y vencimiento, solo artículos con lote
        grid.addComponentColumn(d -> LoteUi.numeroLote(d.getArticulo(), d.getNumeroLote(), d::setNumeroLote))
                .setHeader("Lote").setAutoWidth(true);
        grid.addComponentColumn(d -> LoteUi.vencimiento(d.getArticulo(), d.getFechaVencimientoLote(),
                d::setFechaVencimientoLote)).setHeader("Vence").setAutoWidth(true);

        grid.addComponentColumn(d -> numero(d.getPrecioUnitario(), "110px", d::setPrecioUnitario, d))
                .setHeader("Costo unit.").setAutoWidth(true);
        grid.addComponentColumn(d -> numero(itbisPorcPorDetalle.getOrDefault(d, BigDecimal.ZERO), "70px",
                v -> itbisPorcPorDetalle.put(d, v), d)).setHeader("% ITBIS").setAutoWidth(true);
        EstiloConsulta.monto(grid.addColumn(d -> EstiloConsulta.moneda(d.getItbis()))).setHeader("ITBIS")
                .setAutoWidth(true);
        EstiloConsulta.monto(grid.addColumn(d -> EstiloConsulta.moneda(d.getTotal()))).setHeader("Total")
                .setAutoWidth(true);

        grid.addComponentColumn(d -> {
            Button quitar = new Button(VaadinIcon.TRASH.create(), e -> {
                detalles.removeIf(x -> x == d);
                pendientePorDetalle.remove(d);
                itbisPorcPorDetalle.remove(d);
                actualizarGridYTotales();
            });
            quitar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            return quitar;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);
    }

    /** Campo numérico de la grilla: aplica el valor al salir del campo y recalcula la línea. */
    private BigDecimalField numero(BigDecimal valor, String ancho, Consumer<BigDecimal> alCambiar,
            DetalleRecepcionMercancia d) {
        BigDecimalField f = new BigDecimalField();
        f.setWidth(ancho);
        f.setValue(valor);
        f.setValueChangeMode(ValueChangeMode.ON_BLUR);
        f.addValueChangeListener(e -> {
            if (!e.isFromClient()) {
                return;
            }
            BigDecimal v = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            if (v.signum() < 0) {
                v = BigDecimal.ZERO;
            }
            alCambiar.accept(v);
            calcularLinea(d);
            // Solo se refresca la fila editada (también devuelve el valor anterior si fue rechazado)
            grid.getDataProvider().refreshItem(d);
            actualizarTotales();
        });
        return f;
    }

    private static BigDecimal factor(DetalleRecepcionMercancia d) {
        return d.getFactorConversion() != null && d.getFactorConversion().signum() > 0
                ? d.getFactorConversion() : BigDecimal.ONE;
    }

    /** Unidades base, subtotal, ITBIS y total de la línea. */
    private void calcularLinea(DetalleRecepcionMercancia d) {
        BigDecimal cant = d.getCantidadRecibida() != null ? d.getCantidadRecibida() : BigDecimal.ZERO;
        BigDecimal precio = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : BigDecimal.ZERO;
        BigDecimal porcItbis = itbisPorcPorDetalle.getOrDefault(d, BigDecimal.ZERO);

        BigDecimal sub = cant.multiply(precio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = sub.multiply(porcItbis).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        d.setCantidadFisicaBase(cant.multiply(factor(d)));
        d.setSubTotal(sub);
        d.setItbis(itbis);
        d.setTotal(sub.add(itbis));
    }

    private void actualizarGridYTotales() {
        grid.getDataProvider().refreshAll();
        actualizarTotales();
    }

    private void actualizarTotales() {
        BigDecimal sub = BigDecimal.ZERO;
        BigDecimal itbis = BigDecimal.ZERO;
        for (DetalleRecepcionMercancia d : detalles) {
            sub = sub.add(d.getSubTotal() != null ? d.getSubTotal() : BigDecimal.ZERO);
            itbis = itbis.add(d.getItbis() != null ? d.getItbis() : BigDecimal.ZERO);
        }
        lblSubTotal.setText(EstiloConsulta.moneda(sub));
        lblItbis.setText(EstiloConsulta.moneda(itbis));
        lblTotal.setText(EstiloConsulta.moneda(sub.add(itbis)));
    }

    private void cargarDatosIniciales() {
        // "Orden de compra" como origen por defecto (dispara cambiarTipoOrigen)
        cbTipoDocumento.getListDataView().getItems()
                .filter(t -> ORIGEN_ORDEN_COMPRA.equals(t.getCodigo()))
                .findFirst()
                .ifPresentOrElse(cbTipoDocumento::setValue, () -> cambiarTipoOrigen(ORIGEN_ORDEN_COMPRA));
    }

    // =====================================================================
    // PROCESAR
    // =====================================================================
    private void procesarRecepcion() {

        if (cbSuplidor.isEmpty()) {
            ClaseUtil.mostrarNotificacion("Seleccione el proveedor", NotificationVariant.LUMO_WARNING);
            cbSuplidor.focus();
            return;
        }
        if (cbAlmacenDestino.isEmpty()) {
            ClaseUtil.mostrarNotificacion("Seleccione el almacén destino", NotificationVariant.LUMO_WARNING);
            cbAlmacenDestino.focus();
            return;
        }
        if (cbCondicionPago.isEmpty()) {
            ClaseUtil.mostrarNotificacion("Seleccione la condición de pago", NotificationVariant.LUMO_WARNING);
            cbCondicionPago.focus();
            return;
        }
        if (txtComentario.getValue() == null || txtComentario.getValue().isBlank()) {
            ClaseUtil.mostrarNotificacion("Digite un comentario", NotificationVariant.LUMO_WARNING);
            txtComentario.focus();
            return;
        }
        if (detalles.isEmpty()) {
            ClaseUtil.mostrarNotificacion("Agregue al menos un artículo recibido", NotificationVariant.LUMO_WARNING);
            return;
        }

        for (DetalleRecepcionMercancia d : detalles) {
            if (d.getCantidadRecibida() == null || d.getCantidadRecibida().signum() <= 0) {
                ClaseUtil.mostrarNotificacion("La cantidad de '" + d.getDescripcionArticulo()
                        + "' debe ser mayor que cero", NotificationVariant.LUMO_WARNING);
                return;
            }
            // Validación final contra lo pendiente de la orden
            BigDecimal max = pendientePorDetalle.get(d);
            if (max != null && d.getCantidadRecibida().compareTo(max) > 0) {
                ClaseUtil.mostrarNotificacion("La cantidad de '" + d.getDescripcionArticulo()
                        + "' supera lo pendiente en la orden", NotificationVariant.LUMO_ERROR);
                return;
            }
            // Lotes: un perecedero sin días de vida útil necesita la fecha de vencimiento del lote
            if (d.getArticulo() != null && d.getArticulo().isControlaLote()
                    && Boolean.TRUE.equals(d.getArticulo().getPerecedero())
                    && d.getFechaVencimientoLote() == null
                    && (d.getArticulo().getDiasVencimiento() == null || d.getArticulo().getDiasVencimiento() <= 0)) {
                ClaseUtil.mostrarNotificacion("Indique la fecha de vencimiento del lote de '"
                        + d.getDescripcionArticulo() + "'", NotificationVariant.LUMO_WARNING);
                return;
            }
            if (d.getFechaVencimientoLote() != null && d.getFechaVencimientoLote().isBefore(LocalDate.now())) {
                ClaseUtil.mostrarNotificacion("El lote de '" + d.getDescripcionArticulo()
                        + "' ya está vencido; no se puede recibir", NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        btnGuardar.setEnabled(false);
        // Lo que digitó el usuario en cada línea: si el proceso falla se restaura (el servicio
        // escribe en estas mismas instancias el lote creado, que la transacción revierte).
        Map<DetalleRecepcionMercancia, Object[]> loteDigitado = new IdentityHashMap<>();
        detalles.forEach(d -> loteDigitado.put(d, new Object[]{d.getNumeroLote(), d.getFechaVencimientoLote()}));
        boolean procesada = false;
        try {
            OrdenDeCompra oc = cbOrdenCompra.getValue();

            RecepcionMercancia rec = new RecepcionMercancia();
            rec.setFecha(Date.from(dtFecha.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            rec.setTipoDocumento(cbTipoDocumento.getValue());
            rec.setNombreDocumento(cbTipoDocumento.getValue() != null ? cbTipoDocumento.getValue().getNombre() : "");
            rec.setEstadoDocumento(estadoDocumentoService.getEstado(2));
            rec.setNombreEstado(estadoDocumentoService.getEstado(2).getNombre());
            rec.setAlmacen(cbAlmacenDestino.getValue());
            rec.setProveedor(cbSuplidor.getValue());
            rec.setCondicionPago(cbCondicionPago.getValue());
            rec.setNombreUsuario("Admin");
            rec.setUsuario(new Usuario(1));
            rec.setNombreProveedor(cbSuplidor.getValue().getNombre());
            rec.setComentario(txtComentario.getValue().trim());

            if (oc != null) {
                rec.setNumeroDocumento("REC-" + System.currentTimeMillis() / 1000);
                rec.setOrdenCompra(oc);
                rec.setNumeroOc(oc.getCodigo().toString());
            } else {
                rec.setNumeroDocumento(txtDocOrigen.getValue());
            }

            BigDecimal sub = BigDecimal.ZERO;
            BigDecimal itbis = BigDecimal.ZERO;
            for (DetalleRecepcionMercancia d : detalles) {
                sub = sub.add(d.getSubTotal());
                itbis = itbis.add(d.getItbis());
            }
            rec.setSubtotal(sub);
            rec.setTotalItbis(itbis);
            rec.setTotal(sub.add(itbis));
            rec.setDetalleRecepcionMercanciaCollection(detalles);

            List<Integer> articulosRecibidos = detalles.stream()
                    .filter(d -> d.getArticulo() != null)
                    .map(d -> d.getArticulo().getCodigo())
                    .toList();

            // Transaccional: inventario + lotes + factura de compra + pendiente/estado de la OC
            recepcionService.procesarRecepcion(rec);
            procesada = true;

            ClaseUtil.mostrarNotificacion("Recepción procesada exitosamente", NotificationVariant.LUMO_SUCCESS);
            limpiarFormularioCompleto();

            // Con el costo promedio ya actualizado, avisar si algo quedó vendiéndose con pérdida
            mostrarAvisoPerdida(recepcionService.empaquesVendiendoConPerdida(articulosRecibidos));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            ClaseUtil.mostrarNotificacion(ex.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (RuntimeException ex) {
            log.error("Error inesperado", ex);
            ClaseUtil.mostrarNotificacion("No se pudo procesar la recepción: " + ex.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        } finally {
            if (!procesada) {
                // Reintento limpio: sin id ni lote de la transacción revertida
                for (DetalleRecepcionMercancia d : detalles) {
                    Object[] v = loteDigitado.get(d);
                    d.setCodigo(null);
                    d.setRecepcionMercancia(null);
                    d.setLote(null);
                    if (v != null) {
                        d.setNumeroLote((String) v[0]);
                        d.setFechaVencimientoLote((LocalDate) v[1]);
                    }
                }
                grid.getDataProvider().refreshAll();
            }
            btnGuardar.setEnabled(true);
        }
    }

    private void mostrarAvisoPerdida(List<String> alertas) {
        if (alertas == null || alertas.isEmpty()) {
            return;
        }
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("⚠ Precios por debajo del costo");
        dlg.setWidth("560px");

        Paragraph intro = new Paragraph(
                "Con el nuevo costo de esta compra, estos productos se están vendiendo al costo o con pérdida. "
                + "Revise su precio de venta en el mantenimiento de artículos:");
        UnorderedList lista = new UnorderedList();
        alertas.forEach(a -> lista.add(new ListItem(a)));
        lista.getStyle().set("color", "var(--lumo-error-text-color)");
        dlg.add(intro, lista);

        Button btnOk = new Button("Entendido", ev -> dlg.close());
        btnOk.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dlg.getFooter().add(btnOk);
        dlg.open();
    }

    private void limpiarFormularioCompleto() {
        detalles.clear();
        pendientePorDetalle.clear();
        itbisPorcPorDetalle.clear();
        actualizarGridYTotales();

        cbOrdenCompra.clear();
        if (layoutOrden.isVisible()) {
            cargarOrdenesPendientes(); // refrescar: la OC recibida puede haber quedado completa
        }
        cbAlmacenDestino.clear();
        cbCondicionPago.clear();
        txtDocOrigen.clear();
        cbSuplidor.clear();
        cbSuplidor.setReadOnly(false);
        dtFecha.setValue(LocalDate.now());
        txtComentario.clear();

        actualizarModo();
        cbTipoDocumento.focus();
    }
}
