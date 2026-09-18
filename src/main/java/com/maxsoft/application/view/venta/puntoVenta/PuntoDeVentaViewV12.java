package com.maxsoft.application.view.venta.puntoVenta;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.impl.venta.ImpresionDirectaService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.servicio.interfaces.venta.DeliveryService;
import com.maxsoft.application.servicio.interfaces.venta.EstadoFacturaService;
import com.maxsoft.application.servicio.interfaces.venta.FacturaDeVentaService;
import com.maxsoft.application.servicio.interfaces.reporte.ReporteService;
import com.maxsoft.application.servicio.interfaces.venta.TipoVentaService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.pos.DialogoAbonoLibreta;
import com.maxsoft.application.view.componente.pos.DialogoCobroEfectivo;
import com.maxsoft.application.view.componente.pos.DialogoConfirmarEliminarItem;
import com.maxsoft.application.view.componente.pos.DialogoEditarCantidad;
import com.maxsoft.application.view.componente.pos.DialogoMovimientoPos;
import com.maxsoft.application.view.componente.pos.DialogoRenombrarTicket;
import com.maxsoft.application.view.componente.pos.DialogoSeleccionCliente;
import com.maxsoft.application.view.componente.pos.DialogoVentaPorMonto;
import com.maxsoft.application.view.componente.pos.PanelCarritos;
import com.maxsoft.application.view.componente.pos.PanelProductos;
import com.maxsoft.application.view.componente.pos.PanelTicketContenido;
import com.maxsoft.application.view.venta.cajaChica.DialogoAperturaCaja;
import com.maxsoft.application.view.venta.cajaChica.DialogoCierreCaja;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@NpmPackage(value = "print-js", version = "1.6.0")
@JsModule("print-js/dist/print.js")
@CssImport("print-js/dist/print.css")
@PageTitle("Punto de Venta V12")
@Route(value = "puntoDeVentav12")
@Menu(order = 4, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class PuntoDeVentaViewV12 extends HorizontalLayout
        implements BeforeEnterObserver, BeforeLeaveObserver {

    // ============================================================
    // CONSTANTES
    // ============================================================
    private static final DecimalFormat MONEDA_FORMAT
            = new DecimalFormat("#,##0.00");

    private static final BigDecimal CIEN
            = BigDecimal.valueOf(100);

    private static final BigDecimal ITBIS_POR_DEFECTO
            = BigDecimal.valueOf(18);

    private static final double DESCUENTO_POR_DEFECTO = 0.00;

    private static final String ALMACEN_POR_DEFECTO = "General";

    private static final String UNIDAD_POR_DEFECTO = "Unidad";

    private static final String USUARIO_POR_DEFECTO = "Administrador";

    private static final int ESTADO_FACTURA_DELIVERY = 1;

    private static final int ESTADO_FACTURA_NORMAL = 2;

    private static final int TIPO_VENTA_LIBRETA = 2;

    private static final int ESCALA_CALCULO = 4;

    private static final int ESCALA_MONETARIA = 2;

    // ============================================================
    // SERVICIOS
    // ============================================================
    private final ReporteService reporteService;
    private final CajaService cajaService;
    private final ArticuloService articuloService;
    private final ClienteService clienteService;
    private final DeliveryService deliveryService;
    private final TipoVentaService tipoVentaService;
    private final EstadoFacturaService estadoFacturaService;
    private final FacturaDeVentaService factService;
    private final ImpresionDirectaService impresionDirectaService;

    // ============================================================
    // COMPONENTES / ESTADO DEL POS
    // ============================================================
    private final TabSheet ticketTabSheet = new TabSheet();

    private final List<TicketVenta> listaTicketsAbiertos
            = new ArrayList<>();

    private final ComboBox<Articulo> searchBox
            = new ComboBox<>("Buscar producto o escanear código");

    private final List<Cliente> libretaClientes
            = new ArrayList<>();

    private final List<ShortcutRegistration> atajosRegistrados
            = new ArrayList<>();

    private TicketVenta ticketActivo;

    private int contadorSecuencialTickets = 0;

    private int contadorLineas = 1;

    private boolean cajaAbierta = false;

    private DialogoSeleccionCliente dialogoActivo;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public PuntoDeVentaViewV12(
            ReporteService reporteService,
            CajaService cajaService,
            FacturaDeVentaService factService,
            ArticuloService articuloService,
            ClienteService clienteService,
            DeliveryService deliveryService,
            TipoVentaService tipoVentaService,
            EstadoFacturaService estadoFacturaService,
            ImpresionDirectaService impresionDirectaService
    ) {

        this.reporteService = reporteService;
        this.cajaService = cajaService;
        this.factService = factService;
        this.articuloService = articuloService;
        this.clienteService = clienteService;
        this.deliveryService = deliveryService;
        this.tipoVentaService = tipoVentaService;
        this.estadoFacturaService = estadoFacturaService;
        this.impresionDirectaService = impresionDirectaService;
     

        Shortcuts.addShortcutListener(
                this,
                () -> {
                    if (ticketActivo == null) {
                        return;
                    }

                    ticketActivo.getDetalleSeleccionado()
                            .ifPresent(detalle
                                    -> abrirDialogoEditarCantidad(
                                    ticketActivo,
                                    detalle
                            )
                            );
                },
                Key.NUMPAD_MULTIPLY
        );

        // Atajo global para abrir la ventana de anulación
        atajosRegistrados.add(Shortcuts.addShortcutListener(this, this::abrirDialogoAnulacion, Key.KEY_X, KeyModifier.ALT));

        configurarVista();
        configurarBuscador();
        configurarEventos();
        configurarAtajoCliente();

        crearNuevoTicket(null);
    }

    // ============================================================
    // CONFIGURACIÓN INICIAL
    // ============================================================
    private void configurarVista() {

        setSizeFull();
        setSpacing(true);

        VerticalLayout leftPanel = crearPanelProductos();
        leftPanel.setWidth("55%");
        leftPanel.setHeightFull();

        VerticalLayout rightPanel = crearPanelCarritos();
        rightPanel.setWidth("45%");
        rightPanel.setHeightFull();

        add(leftPanel, rightPanel);
    }

    private void configurarBuscador() {

        searchBox.setItemLabelGenerator(Articulo::getDescripcion);
    }

    private void configurarEventos() {

        addClickListener(event -> restaurarFocoArticulos());

        ticketTabSheet.addSelectedChangeListener(event -> {

            int index = ticketTabSheet.getSelectedIndex();

            if (index >= 0
                    && index < listaTicketsAbiertos.size()) {

                ticketActivo = listaTicketsAbiertos.get(index);

                enfocarBuscador();
            }
        });
    }

    private void configurarAtajoCliente() {

        Shortcuts.addShortcutListener(
                this,
                () -> {

                    if (dialogoActivo == null
                    || !dialogoActivo.isOpened()) {

                        abrirDialogoSeleccionCliente(ticketActivo);
                    }
                },
                Key.F3
        );
    }

    // ============================================================
    // CICLO DE VIDA VAADIN
    // ============================================================
    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        actualizarEstadoCaja();
    }

    private void actualizarEstadoCaja() {

        Optional<CajaTurno> turnoActivo
                = cajaService.obtenerCajaAbierta();

        cajaAbierta = turnoActivo.isPresent();

        setVisible(cajaAbierta);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {

        super.onAttach(attachEvent);

        configurarAtajosTeclado();

        if (!attachEvent.isInitialAttach()) {
            setVisible(true);
            enfocarBuscador();
            return;
        }

        Optional<CajaTurno> turnoActivo
                = cajaService.obtenerCajaAbierta();

        if (turnoActivo.isPresent()) {

            cajaAbierta = true;
            setVisible(true);
            enfocarBuscador();

            return;
        }

        abrirDialogoAperturaInicial();
    }

    private void abrirDialogoAperturaInicial() {

        DialogoAperturaCaja dialogo
                = new DialogoAperturaCaja(
                        cajaService,
                        fondo -> {

                            cajaAbierta = true;

                            getUI().ifPresent(ui
                                    -> ui.access(() -> {

                                setVisible(true);
                                enfocarBuscador();
                            })
                            );
                        }
                );

        dialogo.addOpenedChangeListener(event -> {

            if (!event.isOpened() && !cajaAbierta) {

                getUI().ifPresent(ui
                        -> ui.navigate("puntoDeVentav9")
                );
            }
        });

        dialogo.open();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {

        atajosRegistrados.forEach(
                ShortcutRegistration::remove
        );

        atajosRegistrados.clear();

        if (dialogoActivo != null
                && dialogoActivo.isOpened()) {

            dialogoActivo.close();
        }

        dialogoActivo = null;

        super.onDetach(detachEvent);
    }

    // ============================================================
    // ATAJOS DE TECLADO
    // ============================================================
    private void configurarAtajosTeclado() {

        Key[] digitos = {
            Key.DIGIT_1,
            Key.DIGIT_2,
            Key.DIGIT_3,
            Key.DIGIT_4,
            Key.DIGIT_5,
            Key.DIGIT_6,
            Key.DIGIT_7,
            Key.DIGIT_8,
            Key.DIGIT_9
        };

        Key[] numpadDigitos = {
            Key.NUMPAD_1,
            Key.NUMPAD_2,
            Key.NUMPAD_3,
            Key.NUMPAD_4,
            Key.NUMPAD_5,
            Key.NUMPAD_6,
            Key.NUMPAD_7,
            Key.NUMPAD_8,
            Key.NUMPAD_9
        };

        for (int i = 0; i < 9; i++) {

            final int index = i;

            atajosRegistrados.add(
                    Shortcuts.addShortcutListener(
                            this,
                            () -> seleccionarTicketPorPosicion(index),
                            digitos[i],
                            KeyModifier.ALT
                    )
            );

            atajosRegistrados.add(
                    Shortcuts.addShortcutListener(
                            this,
                            () -> seleccionarTicketPorPosicion(index),
                            numpadDigitos[i],
                            KeyModifier.ALT
                    )
            );
        }

        atajosRegistrados.add(
                Shortcuts.addShortcutListener(
                        this,
                        this::renombrarTicketActivoDesdeAtajo,
                        Key.KEY_R,
                        KeyModifier.ALT
                )
        );

        atajosRegistrados.add(
                Shortcuts.addShortcutListener(
                        this,
                        this::abrirDialogoAbonoLibreta,
                        Key.KEY_A,
                        KeyModifier.ALT
                )
        );
    }

    private void renombrarTicketActivoDesdeAtajo() {

        if (ticketActivo == null) {
            return;
        }

        abrirDialogoRenombrarTicket(
                ticketActivo,
                new Span(ticketActivo.getId())
        );
    }

    private void seleccionarTicketPorPosicion(int index) {

        if (index < 0
                || index >= listaTicketsAbiertos.size()) {

            return;
        }

        ticketTabSheet.setSelectedIndex(index);

        TicketVenta ticket
                = listaTicketsAbiertos.get(index);

        ClaseUtil.mostrarNotificacion(
                "Cambiado a " + ticket.getId(),
                NotificationVariant.LUMO_SUCCESS
        );

        enfocarBuscador();
    }

    // ============================================================
    // PANELES PRINCIPALES
    // ============================================================
    private VerticalLayout crearPanelProductos() {

        return new PanelProductos(
                articuloService,
                this::agregarAlTicketActivo,
                this::agregarAlTicketActivo,
                this::abrirDialogoVentaPorMonto,
                this::abrirDialogoAperturaCaja,
                this::abrirDialogoCierreCaja,
                this::abrirDialogoMovimientoPos,
                factService
        );
    }

    private VerticalLayout crearPanelCarritos() {
        return new PanelCarritos(
                ticketTabSheet,
                listaTicketsAbiertos,
                () -> crearNuevoTicket(null),
                // NUEVO: renombrar el ticket activo
                () -> {
                    if (ticketActivo != null) {
                        abrirDialogoRenombrarTicket(ticketActivo, null);
                    }
                },
                ticketSeleccionado -> {
                    this.ticketActivo = ticketSeleccionado;
                    enfocarBuscador();
                }
        );
    }

    // ============================================================
    // PRODUCTOS / ITEMS
    // ============================================================
    private void agregarAlTicketActivo(Articulo articulo) {

        agregarAlTicketActivo(articulo, 1.0);
    }

    private void agregarAlTicketActivo(
            Articulo articulo,
            Double cantidad) {

        if (articulo == null || ticketActivo == null) {
            return;
        }

        // Advertencia discreta si el producto se venderá sin stock en sistema
        if (Boolean.TRUE.equals(articulo.getInventariable())) {
            double stockActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;

            if (stockActual < cantidad) {
                Notification notif = Notification.show(
                        "⚠️ Notificación: " + articulo.getDescripcion() + " no tiene stock suficiente en sistema (" + stockActual + "). Se venderá en negativo.",
                        3000,
                        Notification.Position.TOP_CENTER
                );
                notif.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        }
        if (ticketActivo == null) {
            ClaseUtil.mostrarNotificacion(
                    "No existe un ticket activo.",
                    NotificationVariant.LUMO_ERROR
            );
            return;
        }

        if (cantidad == null
                || cantidad <= 0) {

            ClaseUtil.mostrarNotificacion(
                    "La cantidad debe ser mayor que cero.",
                    NotificationVariant.LUMO_WARNING
            );
            return;
        }

        Optional<DetalleFacturaDeVenta> existente
                = ticketActivo.getItems()
                        .stream()
                        .filter(item
                                -> item.getArticulo() != null
                        && item.getArticulo().equals(articulo))
                        .findFirst();

        if (existente.isPresent()) {

            actualizarCantidadItem(
                    ticketActivo,
                    existente.get(),
                    cantidad
            );

        } else {

            agregarNuevoItem(
                    ticketActivo,
                    articulo,
                    cantidad
            );
        }

        ticketActivo.updateUI();

        enfocarBuscador();
    }

    private void actualizarCantidadItem(
            TicketVenta ticket,
            DetalleFacturaDeVenta item,
            double cantidad) {

        double cantidadActual
                = valorSeguro(item.getCantidad());

        double nuevaCantidad
                = cantidadActual + cantidad;

        if (nuevaCantidad <= 0) {

            confirmarEliminarItem(
                    ticket,
                    item
            );

            return;
        }

        item.setCantidad(nuevaCantidad);

        recalcularTotalesItem(item);
    }

    private void agregarNuevoItem(
            TicketVenta ticket,
            Articulo articulo,
            double cantidad) {

        DetalleFacturaDeVenta nuevoDetalle
                = new DetalleFacturaDeVenta();

        nuevoDetalle.setCodigo(
                articulo.getCodigo()
        );

        nuevoDetalle.setArticulo(articulo);

        nuevoDetalle.setNumeroDeLinea(
                contadorLineas++
        );

        nuevoDetalle.setDescripcionArticulo(
                articulo.getDescripcion()
        );

        nuevoDetalle.setCantidad(cantidad);

        nuevoDetalle.setExistenciaActual(
                articulo.getExistencia()
        );

        nuevoDetalle.setPrecioVenta(
                articulo.getPrecioVenta()
        );

        nuevoDetalle.setPorcientoDescuento(
                DESCUENTO_POR_DEFECTO
        );

        nuevoDetalle.setPorcientoItbis(
                ITBIS_POR_DEFECTO.doubleValue()
        );

        nuevoDetalle.setNombreAlmacen(
                ALMACEN_POR_DEFECTO
        );

        nuevoDetalle.setNombreUnidad(
                UNIDAD_POR_DEFECTO
        );

        recalcularTotalesItem(nuevoDetalle);

        ticket.getItems().add(nuevoDetalle);
    }

    // ============================================================
    // CÁLCULOS
    // ============================================================
    /**
     * Recalcula los importes de una línea.
     *
     * <p>
     * Aunque las entidades actuales trabajan con Double, todas las operaciones
     * intermedias se realizan con BigDecimal.
     * </p>
     */
    private void recalcularTotalesItem(
            DetalleFacturaDeVenta item) {

        if (item == null) {
            return;
        }

        BigDecimal cantidad
                = decimalSeguro(item.getCantidad());

        BigDecimal precio
                = decimalSeguro(item.getPrecioVenta());

        BigDecimal porcDesc
                = decimalSeguro(item.getPorcientoDescuento());

        BigDecimal porcItbis
                = decimalSeguro(item.getPorcientoItbis());

        BigDecimal subtotal
                = cantidad.multiply(precio);

        BigDecimal totalDescuento
                = calcularPorcentaje(
                        subtotal,
                        porcDesc
                );

        BigDecimal subtotalConDescuento
                = subtotal.subtract(totalDescuento);

        BigDecimal totalItbis
                = calcularPorcentaje(
                        subtotalConDescuento,
                        porcItbis
                );

        BigDecimal total
                = subtotalConDescuento.add(totalItbis);

        item.setSubTotal(
                dinero(subtotal)
        );

        item.setTotalDescuento(
                dinero(totalDescuento)
        );

        item.setTotalItbis(
                dinero(totalItbis)
        );

        item.setTotal(
                dinero(total)
        );
    }

    private BigDecimal calcularPorcentaje(
            BigDecimal monto,
            BigDecimal porcentaje) {

        if (monto == null
                || porcentaje == null
                || monto.signum() == 0
                || porcentaje.signum() == 0) {

            return BigDecimal.ZERO;
        }

        return monto
                .multiply(porcentaje)
                .divide(
                        CIEN,
                        ESCALA_CALCULO,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal decimalSeguro(Double valor) {

        if (valor == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(valor);
    }

    private double valorSeguro(Double valor) {

        return valor == null ? 0.0 : valor;
    }

    private double dinero(BigDecimal valor) {

        return valor
                .setScale(
                        ESCALA_MONETARIA,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    // ============================================================
    // TICKETS
    // ============================================================
    private void crearNuevoTicket(
            String nombrePersonalizado) {

        contadorSecuencialTickets++;

        String nombreFinal
                = obtenerNombreTicket(
                        nombrePersonalizado
                );

        TicketVenta nuevoTicket
                = new TicketVenta(
                        contadorSecuencialTickets,
                        nombreFinal
                );

        listaTicketsAbiertos.add(nuevoTicket);

        configurarGridTicket(nuevoTicket);

        VerticalLayout contenidoTab
                = construirContenidoPanelTicket(nuevoTicket);

        ticketTabSheet.add(
                nombreFinal,
                contenidoTab
        );

        ticketTabSheet.setSelectedTab(
                ticketTabSheet.getTab(contenidoTab)
        );

        ticketActivo = nuevoTicket;

        actualizarTitulosPestanas();

        enfocarBuscador();
    }

    private String obtenerNombreTicket(
            String nombrePersonalizado) {

        if (nombrePersonalizado != null
                && !nombrePersonalizado.trim().isEmpty()) {

            return nombrePersonalizado.trim();
        }

        return "Venta " + contadorSecuencialTickets;
    }

    private void configurarGridTicket(
            TicketVenta ticket) {

        Grid<DetalleFacturaDeVenta> grid
                = ticket.getGrid();

        grid.setDataProvider(
                ticket.getDataProvider()
        );

        grid.setSelectionMode(
                Grid.SelectionMode.SINGLE
        );

        grid.addColumn(
                DetalleFacturaDeVenta::getArticulo
        ).setHeader("Producto")
                .setAutoWidth(true);

        grid.addColumn(
                DetalleFacturaDeVenta::getCantidad
        ).setHeader("Cant.");

        grid.addColumn(
                item -> moneda(item.getPrecioVenta())
        ).setHeader("Precio");

        grid.addColumn(
                item -> moneda(item.getSubTotal())
        ).setHeader("Total");

        grid.addComponentColumn(item -> {

            Button editQtyBtn
                    = new Button(
                            VaadinIcon.EDIT.create(),
                            event
                            -> abrirDialogoEditarCantidad(
                                    ticket,
                                    item
                            )
                    );

            editQtyBtn.addThemeVariants(
                    ButtonVariant.LUMO_TERTIARY,
                    ButtonVariant.LUMO_SMALL
            );

            Button removeBtn
                    = new Button(
                            VaadinIcon.TRASH.create(),
                            event
                            -> confirmarEliminarItem(
                                    ticket,
                                    item
                            )
                    );

            removeBtn.addThemeVariants(
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY,
                    ButtonVariant.LUMO_SMALL
            );

            return new HorizontalLayout(
                    editQtyBtn,
                    removeBtn
            );

        }).setHeader("");

        grid.addItemDoubleClickListener(
                event
                -> abrirDialogoEditarCantidad(
                        ticket,
                        event.getItem()
                )
        );

        registrarAtajosGrid(grid, ticket);

        grid.addCellFocusListener(
                event
                -> event.getItem()
                        .ifPresent(grid::select)
        );

        grid.setHeightFull();
    }

    private void registrarAtajosGrid(
            Grid<DetalleFacturaDeVenta> grid,
            TicketVenta ticket) {

        atajosRegistrados.add(
                Shortcuts.addShortcutListener(
                        grid,
                        () -> {

                            DetalleFacturaDeVenta seleccionado
                            = grid.asSingleSelect().getValue();

                            if (seleccionado != null) {

                                confirmarEliminarItem(
                                        ticket,
                                        seleccionado
                                );
                            }
                        },
                        Key.DELETE
                )
        );

        atajosRegistrados.add(
                Shortcuts.addShortcutListener(
                        grid,
                        () -> {

                            DetalleFacturaDeVenta seleccionado
                            = grid.asSingleSelect().getValue();

                            if (seleccionado != null) {

                                abrirDialogoEditarCantidad(
                                        ticket,
                                        seleccionado
                                );
                            }
                        },
                        Key.NUMPAD_MULTIPLY
                )
        );
    }

    private String moneda(Double valor) {

        return "RD$ "
                + MONEDA_FORMAT.format(
                        valorSeguro(valor)
                );
    }

    private void actualizarTitulosPestanas() {

        for (int i = 0;
                i < listaTicketsAbiertos.size();
                i++) {

            TicketVenta ticket
                    = listaTicketsAbiertos.get(i);

            ticketTabSheet
                    .getTabAt(i)
                    .setLabel(
                            "[Alt+" + (i + 1) + "] "
                            + ticket.getId()
                    );
        }
    }

    private VerticalLayout construirContenidoPanelTicket(
            TicketVenta ticket) {

        return new PanelTicketContenido(
                ticket,
                this::abrirDialogoRenombrarTicket,
                this::confirmarEliminarTicket,
                this::abrirDialogoCobroEfectivo,
                this::abrirDialogoSeleccionCliente
        );
    }

    // ============================================================
    // COBRO
    // ============================================================
    private void abrirDialogoCobroEfectivo(
            TicketVenta ticket) {

        if (ticket == null) {
            return;
        }

        if (ticket.getItems().isEmpty()) {

            ClaseUtil.mostrarNotificacion(
                    "La venta está vacía.",
                    NotificationVariant.LUMO_WARNING
            );

            enfocarBuscador();

            return;
        }

        DialogoCobroEfectivo dialogo
                = new DialogoCobroEfectivo(
                        ticket,
                        deliveryService,
                        estadoFacturaService,
                        clienteService,
                        tipoVentaService,
                        ticketAGuardar -> {

                            FacturaDeVenta factura
                            = guardar(ticketAGuardar);

                            if (factura != null) {

                                imprimir(
                                        factura.getCodigo()
                                );

                            }
                        },
                        this::cerrarTicketActual,
                        this::enfocarBuscador
                );

        dialogo.open();
    }

    // ============================================================
    // PERSISTENCIA
    // ============================================================
    private FacturaDeVenta guardar(
            
            TicketVenta ticketVenta) {

        if (ticketVenta == null) {
            return null;
        }

        if (ticketVenta.getItems().isEmpty()) {

            ClaseUtil.mostrarNotificacion(
                    "No se puede guardar una venta vacía.",
                    NotificationVariant.LUMO_WARNING
            );

            return null;
        }

        try {

            return factService.procesarVenta(
                    ticketVenta,
                    USUARIO_POR_DEFECTO
            );

        } catch (IllegalStateException
                | IllegalArgumentException ex) {

            Notification.show(
                    ex.getMessage(),
                    3500,
                    Notification.Position.MIDDLE
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_WARNING
                    );

            return null;

        } catch (Exception ex) {

            ex.printStackTrace();
            Notification.show(
                    "Error procesando la factura.",
                    4000,
                    Notification.Position.MIDDLE
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_ERROR
                    );

            return null;
        }
    }

    // ============================================================
    // IMPRESIÓN
    // ============================================================
    private void imprimir(int facturaCodigo) {

        try {

            StreamResource pdfResource
                    = reporteService
                            .generarReporteFacturaVenta(
                                    facturaCodigo
                            );

            pdfResource.setContentType(
                    "application/pdf"
            );

            StreamRegistration registration
                    = VaadinSession
                            .getCurrent()
                            .getResourceRegistry()
                            .registerResource(
                                    pdfResource
                            );

            String pdfUrl
                    = registration
                            .getResourceUri()
                            .toString();

            String script = String.format(
                    """
                    fetch('%s')
                        .then(response => response.blob())
                        .then(blob => {
                            const blobUrl = URL.createObjectURL(blob);

                            let iframe =
                                document.getElementById('kiosk-print-frame');

                            if (!iframe) {
                                iframe = document.createElement('iframe');
                                iframe.id = 'kiosk-print-frame';
                                iframe.style.position = 'fixed';
                                iframe.style.right = '0';
                                iframe.style.bottom = '0';
                                iframe.style.width = '0';
                                iframe.style.height = '0';
                                iframe.style.border = '0';

                                document.body.appendChild(iframe);
                            }

                            iframe.src = blobUrl;

                            iframe.onload = function() {
                                setTimeout(() => {
                                    iframe.contentWindow.focus();
                                    iframe.contentWindow.print();
                                    URL.revokeObjectURL(blobUrl);
                                }, 300);
                            };
                        })
                        .catch(err =>
                            console.error(
                                'Error al imprimir en modo kiosk:',
                                err
                            )
                        );
                    """,
                    pdfUrl
            );

            UI.getCurrent()
                    .getPage()
                    .executeJs(script);

            Notification.show(
                    "Enviando a la impresora de caja...",
                    2000,
                    Notification.Position.BOTTOM_END
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_SUCCESS
                    );

        } catch (Exception ex) {

            Notification.show(
                    "Error al generar el documento.",
                    4000,
                    Notification.Position.TOP_CENTER
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_ERROR
                    );
        }

        enfocarBuscador();
    }

    private void imprimirCaja(int facturaCodigo) {

        try {

            JasperPrint jasperPrint
                    = reporteService
                            .generarJasperPrintFacturaVenta(
                                    facturaCodigo
                            );

            impresionDirectaService
                    .imprimirJasperDirecto(
                            jasperPrint,
                            null
                    );

            Notification.show(
                    "Imprimiendo ticket de venta...",
                    2000,
                    Notification.Position.TOP_CENTER
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_SUCCESS
                    );

        } catch (Exception ex) {

            Notification.show(
                    "Error al enviar a la impresora.",
                    4000,
                    Notification.Position.TOP_CENTER
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_ERROR
                    );
        }

        enfocarBuscador();
    }

    // ============================================================
    // CAJA
    // ============================================================
    private void abrirDialogoAperturaCaja() {

        DialogoAperturaCaja dialogo
                = new DialogoAperturaCaja(
                        cajaService,
                        fondo -> {

                            cajaAbierta = true;

                            getUI().ifPresent(ui
                                    -> ui.access(
                                    this::mostrarPosConTransicion
                            )
                            );
                        }
                );

        dialogo.open();

        enfocarBuscador();
    }

    private void abrirDialogoCierreCaja() {

        DialogoCierreCaja dialogo
                = new DialogoCierreCaja(
                        cajaService,
                        this::enfocarBuscador
                );

        dialogo.open();
    }

    private void abrirDialogoMovimientoPos() {

        new DialogoMovimientoPos(
                cajaService,
                USUARIO_POR_DEFECTO
        ).open();
    }

    // ============================================================
    // TICKETS - ELIMINACIÓN
    // ============================================================
    private void cerrarTicketActual() {

        if (ticketActivo != null) {
            eliminarTicket(ticketActivo);
        }
    }

    private void eliminarTicket(
            TicketVenta ticket) {

        if (ticket == null) {
            return;
        }

        /*
         * Siempre debe existir al menos un ticket abierto.
         * Por eso, cuando solo existe uno, se limpia en lugar
         * de eliminarse.
         */
        if (listaTicketsAbiertos.size() <= 1) {

            ticket.getItems().clear();

            ticket.updateUI();

            ClaseUtil.mostrarNotificacion(
                    "Ticket limpiado.",
                    NotificationVariant.LUMO_SUCCESS
            );

            enfocarBuscador();

            return;
        }

        int index
                = listaTicketsAbiertos.indexOf(ticket);

        if (index < 0) {
            return;
        }

        listaTicketsAbiertos.remove(ticket);

        ticketTabSheet.remove(
                ticketTabSheet.getTabAt(index)
        );

        int nuevoIndex
                = Math.min(
                        Math.max(0, index - 1),
                        listaTicketsAbiertos.size() - 1
                );

        ticketActivo
                = listaTicketsAbiertos.get(nuevoIndex);

        ticketTabSheet.setSelectedIndex(
                nuevoIndex
        );

        actualizarTitulosPestanas();

        ClaseUtil.mostrarNotificacion(
                "Venta descartada.",
                NotificationVariant.LUMO_WARNING
        );

        enfocarBuscador();
    }

    private void confirmarEliminarTicket(
            TicketVenta ticket) {

        if (ticket == null) {
            return;
        }

        Dialog confirmDialog
                = new Dialog();

        confirmDialog.setHeaderTitle(
                "Descartar Venta"
        );

        confirmDialog.add(
                "¿Estás seguro de cancelar y eliminar "
                + ticket.getId()
                + "?"
        );

        Button cancelBtn
                = new Button(
                        "No, mantener",
                        event
                        -> confirmDialog.close()
                );

        Button yesBtn
                = new Button(
                        "Sí, eliminar [Enter]",
                        event -> {

                            eliminarTicket(ticket);

                            confirmDialog.close();
                        }
                );

        yesBtn.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR
        );

        yesBtn.addClickShortcut(
                Key.ENTER
        );

        confirmDialog
                .getFooter()
                .add(
                        cancelBtn,
                        yesBtn
                );

        confirmDialog.open();
    }

    // ============================================================
    // CANTIDADES
    // ============================================================
    private void abrirDialogoVentaPorMonto(
            String producto,
            Double precioUnitario) {

        new DialogoVentaPorMonto(
                producto,
                precioUnitario,
                (monto, cantidadCalculada) -> {

                    if (ticketActivo == null) {
                        return;
                    }

                    /*
                     * Se mantiene la misma integración existente.
                     *
                     * La implementación concreta de cómo se agrega
                     * la cantidad calculada continúa delegada al
                     * componente actual.
                     */
                    if (cantidadCalculada != null
                    && cantidadCalculada > 0) {

                        // Aquí se puede integrar directamente
                        // agregarAlTicketActivo() cuando el
                        // componente retorne el Articulo.
                    }
                }
        ).open();
    }

    private void abrirDialogoEditarCantidad(
            TicketVenta ticket,
            DetalleFacturaDeVenta item) {

        if (ticket == null || item == null) {
            return;
        }

        new DialogoEditarCantidad(
                ticket,
                item,
                nuevaCantidad -> {
                    enfocarBuscador();
                },
                () -> confirmarEliminarItem(ticket, item)
        ).open();

        // Refrescar el Grid y el Span del total dentro del objeto TicketVenta
        ticket.updateUI();

    }

    private void confirmarEliminarItem(
            TicketVenta ticket,
            DetalleFacturaDeVenta item) {

        if (ticket == null || item == null) {
            return;
        }

        new DialogoConfirmarEliminarItem(
                ticket,
                item,
                this::enfocarBuscador
        ).open();
    }

    // ============================================================
    // CLIENTES
    // ============================================================
    private void abrirDialogoSeleccionCliente(
            TicketVenta ticket) {

        if (ticket == null) {
            return;
        }

        if (ticket.getItems().isEmpty()) {

            ClaseUtil.mostrarNotificacion(
                    "La venta está vacía.",
                    NotificationVariant.LUMO_ERROR
            );

            enfocarBuscador();

            return;
        }

        if (dialogoActivo != null
                && dialogoActivo.isOpened()) {

            return;
        }

        libretaClientes.clear();

        libretaClientes.addAll(
                clienteService.getLista()
        );

        dialogoActivo
                = new DialogoSeleccionCliente(
                        ticket,
                        libretaClientes,
                        deliveryService.getLista(),
                        (
                                clienteSeleccionado,
                                esDelivery,
                                motorista,
                                direccion,
                                telefono) -> procesarClienteVenta(
                                ticket,
                                clienteSeleccionado,
                                esDelivery,
                                motorista,
                                direccion,
                                telefono
                        )
                );

        dialogoActivo.addOpenedChangeListener(
                event -> {

                    if (!event.isOpened()) {
                        dialogoActivo = null;
                        enfocarBuscador();
                    }
                }
        );

        dialogoActivo.open();
    }

    private void procesarClienteVenta(
            TicketVenta ticket,
            Cliente clienteSeleccionado,
            boolean esDelivery,
            Delivery motorista,
            String direccion,
            String telefono) {

        if (ticket == null
                || clienteSeleccionado == null) {

            return;
        }

        if (esDelivery) {

            ticket.setDelivery(motorista);

            ticket.setEstadoFactura(
                    estadoFacturaService
                            .getEstadoFactura(
                                    ESTADO_FACTURA_DELIVERY
                            )
            );

        } else {

            ticket.setEstadoFactura(
                    estadoFacturaService
                            .getEstadoFactura(
                                    ESTADO_FACTURA_NORMAL
                            )
            );
        }

        ticket.setCliente(
                clienteSeleccionado
        );

        ticket.setNombreCliente(
                clienteSeleccionado.getNombre()
        );

        /*
         * Se mantiene la lógica original para la dirección
         * del cliente. Si posteriormente queremos soportar
         * una dirección de delivery temporal diferente a la
         * registrada en el cliente, podemos usar el parámetro
         * direccion aquí.
         */
        ticket.setDireccion(
                clienteSeleccionado.getDireccion()
        );

        ticket.setTipoVenta(
                tipoVentaService.getTipoVenta(
                        TIPO_VENTA_LIBRETA
                )
        );

        FacturaDeVenta factura
                = guardar(ticket);

        cerrarDialogoCliente();

        cerrarTicketActual();

        enfocarBuscador();

        if (factura != null) {

            imprimir(
                    factura.getCodigo()
            );
        }
    }

    private void cerrarDialogoCliente() {

        if (dialogoActivo != null) {

            dialogoActivo.close();

            dialogoActivo = null;
        }
    }

    private void abrirDialogoAbonoLibreta() {

        new DialogoAbonoLibreta(
                libretaClientes,
                this::enfocarBuscador
        ).open();
    }

    // ============================================================
    // RENOMBRAR TICKET
    // ============================================================
    private void abrirDialogoRenombrarTicket(
            TicketVenta ticket,
            Span ticketNameSpan) {

        if (ticket == null) {
            return;
        }

        new DialogoRenombrarTicket(
                ticket,
                ticketNameSpan,
                nombreLimpio -> {

                    actualizarTitulosPestanas();

                    enfocarBuscador();
                }
        ).open();
    }

    // ============================================================
    // ANIMACIÓN
    // ============================================================
    private void mostrarPosConTransicion() {

        getUI().ifPresent(ui
                -> ui.access(() -> {

                    getElement()
                            .getStyle()
                            .set("opacity", "0");

                    getElement()
                            .getStyle()
                            .set(
                                    "transform",
                                    "scale(0.96)"
                            );

                    getElement()
                            .getStyle()
                            .set(
                                    "transition",
                                    "opacity 0.8s cubic-bezier(0.25, 1, 0.5, 1), "
                                    + "transform 0.8s cubic-bezier(0.25, 1, 0.5, 1)"
                            );

                    setVisible(true);

                    ui.getPage().executeJs(
                            """
                            setTimeout(() => {
                                $0.style.opacity = '1';
                                $0.style.transform = 'scale(1)';
                            }, 80);
                            """,
                            getElement()
                    );

                    enfocarBuscador();
                })
        );
    }

    // ============================================================
    // SALIDA DE LA VISTA
    // ============================================================
    @Override
    public void beforeLeave(
            BeforeLeaveEvent event) {

        boolean existenVentasPendientes
                = listaTicketsAbiertos
                        .stream()
                        .anyMatch(
                                ticket
                                -> !ticket.getItems().isEmpty()
                        );

        if (existenVentasPendientes) {

            /*
             * Se conserva el comportamiento actual.
             *
             * Aquí es un buen punto para implementar posteriormente
             * un BeforeLeaveDialog/ConfirmDialog que pregunte al
             * cajero si desea abandonar el POS con ventas pendientes.
             */
        }
    }

    private void enfocarBuscador() {
        if (!searchBox.isAttached() || !searchBox.isVisible() || !searchBox.isEnabled()) {
            return;
        }

        getUI().ifPresent(ui -> ui.access(() -> {
            searchBox.clear();
            searchBox.focus();
            // Solo un script ligero si el componente interno de Polymer/WebComponent necesita seleccionar el texto
            searchBox.getElement().executeJs("this.inputElement && this.inputElement.select()");
        }));
    }

    private void restaurarFocoArticulos() {

        enfocarBuscador();
    }

    private void ejecutarAnulacion(Integer codigoFactura, String motivo) {
        try {
            // Ejecución real contra la capa de negocio
            FacturaDeVenta facturaAnulada = this.factService.anularVenta(codigoFactura, motivo, USUARIO_POR_DEFECTO);

            if (facturaAnulada != null) {
                ClaseUtil.mostrarNotificacion(
                        "Factura #" + codigoFactura + " anulada correctamente.",
                        NotificationVariant.LUMO_SUCCESS
                );

                // Re-impresión del comprobante o ticket de anulación
//                imprimir(facturaAnulada.getCodigo());
            } else {
                ClaseUtil.mostrarNotificacion("No se encontró la factura N° " + codigoFactura, NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception ex) {
            System.out.println("Erro msg " + ex.getMessage());
            // En caso de que la factura ya esté anulada, cerrada o devuelva error de negocio
            Notification.show("Error al anular: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } finally {
            enfocarBuscador();
        }
    }

    public void abrirDialogoAnulacion() {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Anular Factura / Venta");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        IntegerField txtCodigoFactura = new IntegerField("Número de Factura");
        txtCodigoFactura.setPlaceholder("Ej. 1045");
        txtCodigoFactura.setWidthFull();
        txtCodigoFactura.setAutofocus(true);

        TextArea txtMotivo = new TextArea("Motivo de Anulación");
        txtMotivo.setPlaceholder("Ingrese la razón de la anulación...");
        txtMotivo.setWidthFull();

        layout.add(txtCodigoFactura, txtMotivo);

        Button btnAnular = new Button("Confirmar Anulación", VaadinIcon.CLOSE_CIRCLE.create(), e -> {
            Integer codigoFactura = txtCodigoFactura.getValue();
            String motivo = txtMotivo.getValue();

            if (codigoFactura == null) {
                ClaseUtil.mostrarNotificacion("Debe ingresar un número de factura válido.", NotificationVariant.LUMO_WARNING);
                return;
            }

            if (motivo == null || motivo.trim().isEmpty()) {
                ClaseUtil.mostrarNotificacion("Debe especificar el motivo de la anulación.", NotificationVariant.LUMO_WARNING);
                return;
            }

            ejecutarAnulacion(codigoFactura, motivo.trim());
            dialogo.close();
        });
        btnAnular.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button btnCancelar = new Button("Cancelar", e -> dialogo.close());
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialogo.getFooter().add(btnCancelar, btnAnular);
        dialogo.add(layout);
        dialogo.open();
    }
}
