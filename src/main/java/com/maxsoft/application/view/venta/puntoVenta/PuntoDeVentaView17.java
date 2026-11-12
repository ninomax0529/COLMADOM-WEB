package com.maxsoft.application.view.venta.puntoVenta;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.impl.venta.ImpresionDirectaService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.venta.EstadoFacturaService;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
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
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
import java.util.Objects;
import java.util.Optional;

import net.sf.jasperreports.engine.JasperPrint;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@NpmPackage(value = "print-js", version = "1.6.0")
@JsModule("print-js/dist/print.js")
@CssImport("print-js/dist/print.css")
@PageTitle("Punto de Venta V17")
@Route(value = "puntoDeVentav17")
@Menu(order = 4, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class PuntoDeVentaView17 extends HorizontalLayout
        implements BeforeEnterObserver, BeforeLeaveObserver {

    // ============================================================
    // CONSTANTES
    // ============================================================
    private static final BigDecimal CIEN
            = BigDecimal.valueOf(100);

    private static final BigDecimal ITBIS_POR_DEFECTO
            = BigDecimal.valueOf(18);

    private static final BigDecimal DESCUENTO_POR_DEFECTO = BigDecimal.ZERO;

    private static final String ALMACEN_POR_DEFECTO = "General";

    private static final String UNIDAD_POR_DEFECTO = "Unidad";

    private static final String USUARIO_POR_DEFECTO = "Administrador";

    private static final int ESTADO_FACTURA_DELIVERY = 1;

    private static final int ESTADO_FACTURA_NORMAL = 2;

    private static final int TIPO_VENTA_LIBRETA = 2;

    private static final int ESCALA_CALCULO = 4;

    private static final int ESCALA_MONETARIA = 2;

    /**
     * Código del almacén contra el que el POS valida el stock al vender.
     */
    private static final int ALMACEN_STOCK_CODIGO = 2;

    /**
     * Ruta a la que se vuelve si el cajero cierra el diálogo de apertura sin
     * abrir caja. No puede ser la propia ruta del POS (se reabriría el diálogo
     * en bucle). Es la misma ruta que usan los diálogos de apertura y cierre
     * de caja.
     */
    private static final String RUTA_SIN_CAJA = "modulo/moduloPrincipal";

    /**
     * Selector de los diálogos abiertos en el navegador. Con uno abierto, el
     * foco y los atajos le pertenecen al diálogo.
     */
    private static final String SELECTOR_DIALOGO_ABIERTO
            = "vaadin-dialog-overlay[opened], vaadin-confirm-dialog-overlay[opened]";

    // ============================================================
    // SERVICIOS
    // ============================================================
    private final ReporteService reporteService;
    private final CajaService cajaService;
    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService; // MULTI-EMPAQUE
    private final ClienteService clienteService;
    private final com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService reciboDeIngresoService;
    private final DeliveryService deliveryService;
    private final TipoVentaService tipoVentaService;
    private final EstadoFacturaService estadoFacturaService;
    private final FacturaDeVentaService factService;
    private final ImpresionDirectaService impresionDirectaService;
    private final ArticuloAlmacenService articuloAlmacenService;
    /** Autorización de supervisor (p. ej. vender de un lote vencido). */
    @org.springframework.beans.factory.annotation.Autowired
    private com.maxsoft.application.servicio.interfaces.venta.AutorizacionService autorizacionService;

    // ============================================================
    // COMPONENTES / ESTADO DEL POS
    // ============================================================
    // DecimalFormat no es thread-safe: cada vista (sesión) usa su propia
    // instancia en lugar de una estática compartida por todos los cajeros.
    private final DecimalFormat monedaFormat
            = new DecimalFormat("#,##0.00");

    private final DecimalFormat cantidadFormat
            = new DecimalFormat("#,##0.####");

    private final TabSheet ticketTabSheet = new TabSheet();

    private final List<TicketVenta> listaTicketsAbiertos
            = new ArrayList<>();

    // Es el buscador del PanelProductos (se asigna en crearPanelProductos).
    // Antes era un combo propio que nunca se agregaba a la pantalla, por lo que
    // enfocarBuscador() no tenía ningún efecto.
    private ComboBox<ArticuloEmpaque> searchBox;

    // Panel izquierdo (catálogo y botones de caja)
    private PanelProductos panelProductos;

    private final List<Cliente> libretaClientes
            = new ArrayList<>();

    private TicketVenta ticketActivo;

    private int contadorSecuencialTickets = 0;

    private boolean cajaAbierta = false;

    private DialogoSeleccionCliente dialogoActivo;

    /**
     * PDF de la última factura impresa. Se libera al imprimir la siguiente
     * para no acumular recursos en la sesión durante todo el turno.
     */
    private StreamRegistration ultimoPdfRegistrado;

    /**
     * Se activa cuando el cajero confirma salir del POS con ventas
     * pendientes, para no volver a preguntar en la misma navegación.
     */
    private boolean salidaConfirmada = false;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public PuntoDeVentaView17(
            ReporteService reporteService,
            CajaService cajaService,
            FacturaDeVentaService factService,
            ArticuloService articuloService,
            ArticuloEmpaqueService articuloEmpaqueService, // MULTI-EMPAQUE
            ClienteService clienteService,
            DeliveryService deliveryService,
            TipoVentaService tipoVentaService,
            EstadoFacturaService estadoFacturaService,
            ImpresionDirectaService impresionDirectaService,
            ArticuloAlmacenService articuloAlmacenService,
            com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService reciboDeIngresoService
    ) {

        this.reciboDeIngresoService = reciboDeIngresoService;

        this.reporteService = reporteService;
        this.cajaService = cajaService;
        this.factService = factService;
        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;
        this.clienteService = clienteService;
        this.deliveryService = deliveryService;
        this.tipoVentaService = tipoVentaService;
        this.estadoFacturaService = estadoFacturaService;
        this.impresionDirectaService = impresionDirectaService;
        this.articuloAlmacenService = articuloAlmacenService;

        // "*" (teclado numérico) edita la cantidad de la línea seleccionada.
        // Es el ÚNICO registro de este atajo: antes también se registraba en
        // cada grid y, con el foco en el grid, se abrían dos diálogos.
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

        // Los atajos Alt+tecla (N, R, C, X, A, 1..9) se instalan en el navegador
        // desde onAttach(): ver instalarAtajosAlt().
        // F2 (cobrar) y F3 (fiar) son click-shortcuts de los botones de cada
        // ticket (PanelTicketContenido). Antes F3 también se registraba aquí y
        // con la venta vacía se mostraba el aviso dos veces.
        configurarVista();
        configurarEventos();

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

    /**
     * Botón de anulación. Abre el mismo diálogo que el atajo Alt+X y se coloca
     * en el encabezado del panel de ventas (junto a "Cambiar Nombre" y "Nueva
     * Venta").
     */
    private Button crearBotonAnular() {

        Button btnAnular
                = new Button(
                        "Anular factura (Alt+X)",
                        VaadinIcon.CLOSE_CIRCLE.create(),
                        event -> abrirDialogoAnulacion()
                );

        btnAnular.addThemeVariants(
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL
        );

        btnAnular.setTooltipText("Anular una factura emitida");

        // Fuera del orden de tabulación: evita activarlo por accidente con el
        // lector de códigos o con Enter mientras se vende.
        btnAnular.setTabIndex(-1);

        return btnAnular;
    }

    private void configurarEventos() {

        ticketTabSheet.addSelectedChangeListener(event -> {

            int index = ticketTabSheet.getSelectedIndex();

            if (index >= 0
                    && index < listaTicketsAbiertos.size()) {

                ticketActivo = listaTicketsAbiertos.get(index);

                enfocarBuscador();
            }
        });
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

        marcarCajaAbierta(turnoActivo.isPresent());

        setVisible(cajaAbierta);
    }

    /**
     * Único punto donde cambia el estado de la caja: actualiza el flag y los
     * botones del panel (Abrir Caja solo sin caja; Cerrar Caja solo con caja).
     */
    private void marcarCajaAbierta(boolean abierta) {

        cajaAbierta = abierta;

        if (panelProductos != null) {
            panelProductos.actualizarEstadoCaja(abierta);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {

        super.onAttach(attachEvent);

        instalarFocoPorClick();

        instalarAtajosAlt();

        salidaConfirmada = false;

        // Tanto en el primer attach como al volver a la vista se consulta la
        // caja: antes, al volver, el POS se mostraba aunque la caja se hubiera
        // cerrado mientras tanto.
        Optional<CajaTurno> turnoActivo
                = cajaService.obtenerCajaAbierta();

        if (turnoActivo.isPresent()) {

            marcarCajaAbierta(true);
            setVisible(true);
            enfocarBuscador();

            return;
        }

        marcarCajaAbierta(false);
        setVisible(false);

        abrirDialogoAperturaInicial();
    }

    private void abrirDialogoAperturaInicial() {

        DialogoAperturaCaja dialogo
                = new DialogoAperturaCaja(
                        cajaService,
                        fondo -> {

                            marcarCajaAbierta(true);

                            getUI().ifPresent(ui
                                    -> ui.access(() -> {

                                setVisible(true);
                                enfocarBuscador();
                            })
                            );
                        }
                );

        dialogo.addOpenedChangeListener(event -> {

            if (event.isOpened()) {
                return;
            }

            /*
             * El diálogo se cierra ANTES de ejecutar el callback de apertura
             * (que corre en un ui.access diferido), así que el flag cajaAbierta
             * todavía está en false en este punto. Se consulta la caja real
             * para no sacar al cajero del POS justo después de abrirla.
             */
            if (cajaService.obtenerCajaAbierta().isPresent()) {

                marcarCajaAbierta(true);

                // Respaldo: mostrar el POS aunque el callback del diálogo no
                // llegue (antes quedaba oculto y había que refrescar).
                setVisible(true);
                enfocarBuscador();

                return;
            }

            getUI().ifPresent(ui
                    -> ui.navigate(RUTA_SIN_CAJA)
            );
        });

        dialogo.open();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {

        // Los atajos de cada grid están ligados al ciclo de vida del grid
        // (Shortcuts.addShortcutListener(grid, ...)), así que no se eliminan
        // aquí. Antes se borraban al salir de la vista y, al volver, Supr ya
        // no quitaba líneas.
        liberarUltimoPdf();

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
    /**
     * Atajos Alt+tecla de la pantalla de venta, resueltos en el NAVEGADOR:
     * <ul>
     * <li>Alt+N: nueva venta</li>
     * <li>Alt+R: cambiar nombre de la venta activa</li>
     * <li>Alt+C: cancelar la venta activa</li>
     * <li>Alt+X: anular factura</li>
     * <li>Alt+A: abono de libreta</li>
     * <li>Alt+1 ... Alt+9: cambiar de venta</li>
     * </ul>
     *
     * Se detectan por la tecla FÍSICA (event.code) y en fase de captura, antes
     * que cualquier componente. Con el atajo de Vaadin, si el foco estaba en el
     * buscador, Alt+letra podía llegar como otro carácter (según el teclado o
     * el sistema) y entonces el atajo no se disparaba y la letra se escribía en
     * el buscador. Aquí se cancela el evento, así que la letra nunca se
     * escribe.
     */
    private void instalarAtajosAlt() {

        getElement().executeJs(
                """
                const vista = $0;
                if (vista.__atajosAltInstalados) { return; }
                vista.__atajosAltInstalados = true;

                const manejador = e => {
                    // Si la vista ya no está en pantalla, se limpia el atajo
                    if (!vista.isConnected) {
                        document.removeEventListener('keydown', manejador, true);
                        vista.__atajosAltInstalados = false;
                        return;
                    }

                    if (!e.altKey || e.metaKey || e.shiftKey) { return; }
                    // Ctrl+Alt es AltGr en Windows: se acepta solo si es AltGr
                    if (e.ctrlKey && !e.getModifierState('AltGraph')) { return; }

                    let accion = null;
                    switch (e.code) {
                        case 'KeyN': accion = () => vista.$server.atajoNuevaVenta(); break;
                        case 'KeyR': accion = () => vista.$server.atajoRenombrarTicket(); break;
                        case 'KeyC': accion = () => vista.$server.atajoCancelarVenta(); break;
                        case 'KeyX': accion = () => vista.$server.atajoAnularFactura(); break;
                        case 'KeyA': accion = () => vista.$server.atajoAbonoLibreta(); break;
                        default: {
                            const m = /^(Digit|Numpad)([1-9])$/.exec(e.code);
                            if (m) {
                                const posicion = parseInt(m[2], 10) - 1;
                                accion = () => vista.$server.atajoSeleccionarTicket(posicion);
                            }
                        }
                    }
                    if (!accion) { return; }

                    // Con un diálogo abierto no se disparan los atajos de la venta
                    if (document.querySelector($1)) { return; }

                    e.preventDefault();
                    e.stopPropagation();
                    accion();
                };

                document.addEventListener('keydown', manejador, true);
                """,
                getElement(),
                SELECTOR_DIALOGO_ABIERTO
        );
    }

    @ClientCallable
    public void atajoNuevaVenta() {

        if (!cajaAbierta) {
            return;
        }

        crearNuevoTicket(null);
    }

    @ClientCallable
    public void atajoRenombrarTicket() {

        renombrarTicketActivoDesdeAtajo();
    }

    @ClientCallable
    public void atajoCancelarVenta() {

        confirmarEliminarTicket(ticketActivo);
    }

    @ClientCallable
    public void atajoAnularFactura() {

        abrirDialogoAnulacion();
    }

    @ClientCallable
    public void atajoAbonoLibreta() {

        abrirDialogoAbonoLibreta();
    }

    @ClientCallable
    public void atajoSeleccionarTicket(int posicion) {

        seleccionarTicketPorPosicion(posicion);
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
                NotificationVariant.LUMO_SUCCESS, 2000
        );

        enfocarBuscador();
    }

    // ============================================================
    // PANELES PRINCIPALES
    // ============================================================
    private VerticalLayout crearPanelProductos() {

        PanelProductos panel = new PanelProductos(
                articuloEmpaqueService,
                this::agregarAlTicketActivo,
                this::agregarAlTicketActivo,
                this::abrirDialogoVentaPorMonto,
                this::abrirDialogoAperturaCaja,
                this::abrirDialogoCierreCaja,
                this::abrirDialogoMovimientoPos,
                factService
        );

        // El foco del POS (después de cada venta, cada diálogo, cada cambio de
        // pestaña) debe volver al buscador/lector de códigos del panel.
        this.searchBox = panel.getSearchBox();

        this.panelProductos = panel;

        panel.actualizarEstadoCaja(cajaAbierta);

        return panel;
    }

    private PanelCarritos crearPanelCarritos() {

        PanelCarritos panel = new PanelCarritos(
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

        panel.agregarAccion(crearBotonAnular());

        return panel;
    }

    // ============================================================
    // PRODUCTOS / ITEMS  (CON SOPORTE MULTI-EMPAQUE)
    // ============================================================
    private void agregarAlTicketActivo(ArticuloEmpaque articulo) {

        // Elegir un producto (buscador o toque en la tarjeta) agrega 1 unidad.
        // Antes se enviaba 0 y la validación siempre rechazaba la operación.
        agregarAlTicketActivo(articulo, BigDecimal.ONE);
    }

    /**
     * Cada tarjeta / resultado del buscador ya ES una presentación concreta
     * (ArticuloEmpaque), por lo que no se pregunta de nuevo por el empaque.
     * Cantidad positiva: agrega. Cantidad negativa: descuenta de la línea
     * existente de esa presentación.
     */
    private void agregarAlTicketActivo(
            ArticuloEmpaque articulo,
            BigDecimal cantidad) {

        if (articulo == null || ticketActivo == null) {
            return;
        }

        if (cantidad == null
                || cantidad.signum() == 0) {

            ClaseUtil.mostrarNotificacion(
                    "La cantidad debe ser mayor que cero.",
                    NotificationVariant.LUMO_WARNING
            );
            return;
        }

        // Se fija el ticket de destino al momento de la acción.
        final TicketVenta ticket = ticketActivo;

        if (cantidad.signum() < 0) {
            quitarDelTicket(ticket, articulo, cantidad);
            return;
        }

        procesarInsercionConEmpaque(
                ticket,
                articulo,
                articulo,
                cantidad
        );
    }

    /**
     * Botón "−" de la tarjeta: resta de la línea de esa presentación. Si la
     * cantidad queda en 0 se pide confirmar la eliminación de la línea.
     */
    private void quitarDelTicket(
            TicketVenta ticket,
            ArticuloEmpaque articulo,
            BigDecimal cantidad) {

        Optional<DetalleFacturaDeVenta> existente
                = buscarLinea(ticket, articulo);

        if (existente.isEmpty()) {

            ClaseUtil.mostrarNotificacion(
                    "Ese producto no está en la venta.",
                    NotificationVariant.LUMO_WARNING
            );

            enfocarBuscador();

            return;
        }

        actualizarCantidadItem(
                ticket,
                existente.get(),
                cantidad
        );

        ticket.updateUI();

        actualizarTitulosPestanas();

        enfocarBuscador();
    }

    private Optional<DetalleFacturaDeVenta> buscarLinea(
            TicketVenta ticket,
            ArticuloEmpaque articulo) {

        return ticket.getItems()
                .stream()
                .filter(item
                        -> item.getArticuloEmpaque() != null
                && Objects.equals(
                        item.getArticuloEmpaque().getCodigo(),
                        articulo.getCodigo()))
                .findFirst();
    }

    /**
     * Factor de conversión del empaque a unidad base. Sin empaque o con factor
     * inválido se asume 1.
     */
    private BigDecimal obtenerFactorConversion(
            ArticuloEmpaque empaque) {

        if (empaque == null) {
            return BigDecimal.ONE;
        }

        BigDecimal factor = empaque.getFactorConversion();

        if (factor == null || factor.signum() <= 0) {
            return BigDecimal.ONE;
        }

        return factor;
    }

    /**
     * Precio de venta de una presentación: el precio propio del empaque si lo
     * tiene; si no, precio base del artículo x factor.
     */
    private BigDecimal calcularPrecioEmpaque(
            ArticuloEmpaque articulo,
            ArticuloEmpaque empaque) {

        if (empaque != null && empaque.getPrecioVenta() != null) {
            return empaque.getPrecioVenta();
        }

        ArticuloEmpaque base = empaque != null ? empaque : articulo;

        Number precioBase = (base.getArticulo() != null
                && base.getArticulo().getPrecioVenta() != null)
                ? base.getArticulo().getPrecioVenta()
                : null;

        return aDecimal(precioBase)
                .multiply(obtenerFactorConversion(base));
    }

    /**
     * Cantidad total del ARTÍCULO dentro del ticket, expresada en unidad base.
     * Suma todas las presentaciones del mismo artículo (unidad, caja, etc.),
     * porque el stock es único por artículo.
     */
    private BigDecimal cantidadBaseEnTicket(
            TicketVenta ticket,
            ArticuloEmpaque articuloEmpaque) {

        Integer codigoArticulo = articuloEmpaque.getArticulo() != null
                ? articuloEmpaque.getArticulo().getCodigo()
                : null;

        return ticket.getItems()
                .stream()
                .filter(i -> i.getArticulo() != null
                && Objects.equals(i.getArticulo().getCodigo(), codigoArticulo))
                .map(i -> valorSeguro(i.getCantidad())
                .multiply(obtenerFactorConversion(i.getArticuloEmpaque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void procesarInsercionConEmpaque(
            TicketVenta ticket,
            ArticuloEmpaque articuloEmpaque,
            ArticuloEmpaque empaque,
            BigDecimal cantidad) {

        if (ticket == null) {
            ClaseUtil.mostrarNotificacion(
                    "No existe un ticket activo.",
                    NotificationVariant.LUMO_ERROR
            );
            return;
        }

        // Advertencia discreta si el producto se venderá sin stock en sistema.
        // Considera lo que ya hay en el ticket (todas las presentaciones del
        // artículo) y el factor del empaque.
        Articulo base = articuloEmpaque.getArticulo();

        if (base != null && Boolean.TRUE.equals(base.getInventariable())) {

            // Sin registro de existencia en el almacén se considera stock 0
            BigDecimal stock
                    = this.articuloAlmacenService
                            .buscarPorArticuloYAlmacen(
                                    base.getCodigo(),
                                    ALMACEN_STOCK_CODIGO
                            )
                            .map(a -> a.getExistencia())
                            .orElse(BigDecimal.ZERO);

            BigDecimal requerido
                    = cantidadBaseEnTicket(ticket, articuloEmpaque)
                            .add(cantidad.multiply(
                                    obtenerFactorConversion(articuloEmpaque)));

            if (stock.compareTo(requerido) < 0
                    && !Boolean.TRUE.equals(base.getPermitirVentaSinExistencia())) {

                // El artículo no permite venta sin existencia: se bloquea aquí
                // en lugar de fallar al cobrar (el kardex lo rechazaría).
                Notification bloqueo = Notification.show(
                        "⛔ " + articuloEmpaque.getNombreArticulo()
                        + ": stock insuficiente ("
                        + stock.stripTrailingZeros().toPlainString()
                        + " disponible, se requieren "
                        + requerido.stripTrailingZeros().toPlainString()
                        + " en unidad base). Este artículo no permite venta sin existencia.",
                        4000,
                        Notification.Position.TOP_CENTER
                );
                bloqueo.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (stock.compareTo(requerido) < 0) {

                Notification notif = Notification.show(
                        "⚠️ Notificación: " + articuloEmpaque.getNombreArticulo()
                        + " no tiene stock suficiente en sistema ("
                        + stock.stripTrailingZeros().toPlainString()
                        + " en unidad base). Se venderá en negativo.",
                        3000,
                        Notification.Position.TOP_CENTER
                );

                notif.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        }

        Optional<DetalleFacturaDeVenta> existente
                = buscarLinea(ticket, articuloEmpaque);

        if (existente.isPresent()) {

            actualizarCantidadItem(
                    ticket,
                    existente.get(),
                    cantidad
            );

        } else {

            agregarNuevoItemConEmpaque(
                    ticket,
                    articuloEmpaque,
                    empaque,
                    cantidad
            );
        }

        ticket.updateUI();

        actualizarTitulosPestanas();

        enfocarBuscador();
    }

    private void actualizarCantidadItem(
            TicketVenta ticket,
            DetalleFacturaDeVenta item,
            BigDecimal cantidad) {

        BigDecimal cantidadActual
                = valorSeguro(item.getCantidad());

        BigDecimal nuevaCantidad
                = cantidadActual.add(cantidad);

        if (nuevaCantidad.signum() <= 0) {

            confirmarEliminarItem(
                    ticket,
                    item
            );

            return;
        }

        item.setCantidad(nuevaCantidad);

        recalcularTotalesItem(item);
    }

    private void agregarNuevoItemConEmpaque(
            TicketVenta ticket,
            ArticuloEmpaque artculoEmpaque,
            ArticuloEmpaque empaque,
            BigDecimal cantidad) {

        DetalleFacturaDeVenta nuevoDetalle
                = new DetalleFacturaDeVenta();

        nuevoDetalle.setCodigo(
                artculoEmpaque.getCodigo()
        );

        nuevoDetalle.setArticulo(artculoEmpaque.getArticulo());

        nuevoDetalle.setCantidad(cantidad);

        // Presentación vendida
        nuevoDetalle.setArticuloEmpaque(artculoEmpaque);

        BigDecimal factor = obtenerFactorConversion(artculoEmpaque);

        nuevoDetalle.setFactorConversion(factor);

        nuevoDetalle.setCantidadFisicaBase(
                cantidad.multiply(factor)
        );

        if (artculoEmpaque.getUnidadEmpaque() != null) {

            nuevoDetalle.setUnidad(
                    artculoEmpaque.getUnidadEmpaque()
            );

            nuevoDetalle.setNombreUnidad(
                    artculoEmpaque.getUnidadEmpaque().getDescripcion()
            );

        } else {

            nuevoDetalle.setNombreUnidad(
                    UNIDAD_POR_DEFECTO
            );
        }

        nuevoDetalle.setNumeroDeLinea(
                siguienteNumeroLinea(ticket)
        );

        nuevoDetalle.setDescripcionArticulo(
                construirDescripcion(artculoEmpaque)
        );

        if (artculoEmpaque.getArticulo() != null) {
            nuevoDetalle.setExistenciaActual(
                    artculoEmpaque.getArticulo().getExistencia()
            );
        }

        nuevoDetalle.setPrecioVenta(
                calcularPrecioEmpaque(
                        artculoEmpaque,
                        empaque != null ? empaque : artculoEmpaque
                )
        );

        nuevoDetalle.setPorcientoDescuento(
                DESCUENTO_POR_DEFECTO
        );

        nuevoDetalle.setPorcientoItbis(
                ITBIS_POR_DEFECTO
        );

        nuevoDetalle.setNombreAlmacen(
                ALMACEN_POR_DEFECTO
        );

        recalcularTotalesItem(nuevoDetalle);

        ticket.getItems().add(nuevoDetalle);

        // La línea recién agregada queda seleccionada: "*" y Supr actúan
        // sobre ella sin tener que tocar el grid.
        ticket.getGrid().select(nuevoDetalle);
    }

    /**
     * Número de línea dentro del ticket. Antes era un contador global de la
     * vista, por lo que cada venta empezaba en el número donde quedó la
     * anterior.
     */
    private int siguienteNumeroLinea(TicketVenta ticket) {

        return ticket.getItems()
                .stream()
                .mapToInt(DetalleFacturaDeVenta::getNumeroDeLinea)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Nombre del artículo + presentación (ej. "Arroz Selecto Saco 25 lb").
     * Evita el texto "null" cuando el empaque no tiene descripción.
     */
    private String construirDescripcion(ArticuloEmpaque articuloEmpaque) {

        String nombre = articuloEmpaque.getNombreArticulo() != null
                ? articuloEmpaque.getNombreArticulo().trim()
                : "";

        String descripcionEmpaque = articuloEmpaque.getDescripcionEmpaque();

        if (descripcionEmpaque == null || descripcionEmpaque.isBlank()) {
            return nombre;
        }

        return (nombre + " " + descripcionEmpaque.trim()).trim();
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

        // MULTI-EMPAQUE: la cantidad en unidad base siempre sigue a la cantidad
        item.setCantidadFisicaBase(
                cantidad.multiply(
                        obtenerFactorConversion(item.getArticuloEmpaque())
                )
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

    private BigDecimal decimalSeguro(BigDecimal valor) {

        if (valor == null) {
            return BigDecimal.ZERO;
        }

        return valor;
    }

    private BigDecimal valorSeguro(BigDecimal valor) {

        return valor == null ? BigDecimal.ZERO : valor;
    }

    /**
     * Convierte cualquier Number (BigDecimal, Double, Integer...) a BigDecimal
     * de forma segura. Evita depender del tipo exacto que devuelvan las
     * entidades.
     */
    private BigDecimal aDecimal(Number valor) {

        if (valor == null) {
            return BigDecimal.ZERO;
        }

        if (valor instanceof BigDecimal) {
            return (BigDecimal) valor;
        }

        return BigDecimal.valueOf(valor.doubleValue());
    }

    private BigDecimal dinero(BigDecimal valor) {

        return valor
                .setScale(
                        ESCALA_MONETARIA,
                        RoundingMode.HALF_UP
                );
    }

    // ============================================================
    // TICKETS
    // ============================================================
    private void crearNuevoTicket(String nombrePersonalizado) {

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

        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT
        );

        grid.setEmptyStateComponent(
                crearEstadoVacioTicket()
        );

        grid.addColumn(
                DetalleFacturaDeVenta::getDescripcionArticulo
        ).setHeader("Artículo")
                .setFlexGrow(3)
                .setTooltipGenerator(
                        DetalleFacturaDeVenta::getDescripcionArticulo
                );

        // MULTI-EMPAQUE: muestra la presentación vendida en cada línea
        grid.addColumn(
                DetalleFacturaDeVenta::getNombreUnidad
        ).setHeader("Unidad")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Cantidad sin ceros sobrantes (1 en vez de 1.0000; 0.5 en venta
        // por monto o por libra)
        grid.addColumn(
                item -> cantidad(item.getCantidad())
        ).setHeader("Cant.")
                .setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(
                item -> moneda(item.getPrecioVenta())
        ).setHeader("Precio")
                .setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(
                item -> moneda(item.getSubTotal())
        ).setHeader("Total")
                .setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setPartNameGenerator(item -> "pos-total-linea");

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

            editQtyBtn.setTooltipText("Editar cantidad (*)");

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

            removeBtn.setTooltipText("Quitar de la venta (Supr)");

            HorizontalLayout acciones
                    = new HorizontalLayout(
                            editQtyBtn,
                            removeBtn
                    );

            acciones.setSpacing(false);

            return acciones;

        }).setHeader("")
                .setAutoWidth(true)
                .setFlexGrow(0);

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

    /**
     * Contenido del grid cuando la venta no tiene productos: carrito, mensaje
     * y atajos, centrados en el área del grid (antes era una línea de texto
     * arriba a la izquierda y el resto del panel quedaba vacío).
     */
    private VerticalLayout crearEstadoVacioTicket() {

        Icon icono = VaadinIcon.CART_O.create();

        icono.setSize("4rem");

        icono.getStyle()
                .set("color", "var(--lumo-contrast-30pct)");

        Span titulo = new Span(
                "Venta vacía"
        );

        titulo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)");

        Span ayuda = new Span(
                "Escanee o busque un producto (F4), o toque una tarjeta del catálogo."
        );

        ayuda.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("text-align", "center");

        VerticalLayout estadoVacio
                = new VerticalLayout(
                        icono,
                        titulo,
                        ayuda
                );

        estadoVacio.setSpacing(false);
        estadoVacio.setWidthFull();
        estadoVacio.setAlignItems(Alignment.CENTER);
        estadoVacio.setJustifyContentMode(JustifyContentMode.CENTER);

        // El área del grid ocupa aproximadamente la mitad de la pantalla
        estadoVacio.getStyle()
                .set("min-height", "45vh")
                .set("gap", "var(--lumo-space-s)");

        return estadoVacio;
    }

    /**
     * Supr quita la línea seleccionada. El atajo "*" NO se registra aquí: ya
     * existe a nivel de vista (constructor) y registrarlo dos veces abría dos
     * diálogos de cantidad.
     */
    private void registrarAtajosGrid(
            Grid<DetalleFacturaDeVenta> grid,
            TicketVenta ticket) {

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
        );
    }

    private String moneda(BigDecimal valor) {

        return "RD$ "
                + monedaFormat.format(
                        valorSeguro(valor)
                );
    }

    private String cantidad(BigDecimal valor) {

        return cantidadFormat.format(
                valorSeguro(valor)
        );
    }

    private void actualizarTitulosPestanas() {

        for (int i = 0;
                i < listaTicketsAbiertos.size();
                i++) {

            TicketVenta ticket
                    = listaTicketsAbiertos.get(i);

            int lineas = ticket.getItems().size();

            ticketTabSheet
                    .getTabAt(i)
                    .setLabel(
                            "[Alt+" + (i + 1) + "] "
                            + ticket.getId()
                            + (lineas > 0 ? " (" + lineas + ")" : "")
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

        /*
         * El diálogo llama a "guardar" y luego a "cerrar ticket" sin mirar el
         * resultado. Si el guardado fallaba, la venta se descartaba igual y el
         * cajero perdía todo lo escaneado. Ahora solo se cierra el ticket
         * cuando la factura realmente se guardó.
         */
        final boolean[] ventaGuardada = {false};

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

                                ventaGuardada[0] = true;

                                imprimir(
                                        factura.getCodigo()
                                );
                            }
                        },
                        () -> {

                            if (ventaGuardada[0]) {

                                cerrarTicketFacturado(ticket);
                            }
                        },
                        this::enfocarBuscador
                );

        dialogo.open();
    }

    // ============================================================
    // PERSISTENCIA
    // ============================================================
    private FacturaDeVenta guardar(TicketVenta ticketVenta) {

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

        } catch (com.maxsoft.application.servicio.interfaces.inventario.LoteVencidoException ex) {

            // Solo queda existencia en lotes vencidos: se pide un supervisor. La venta
            // no se guarda; con la autorización el cajero vuelve a cobrar.
            new com.maxsoft.application.view.componente.pos.DialogoAutorizacionSupervisor(
                    "Venta de lote vencido",
                    ex.getLineas(),
                    autorizacionService,
                    supervisor -> {
                        ticketVenta.setAutorizadoLoteVencidoPor(supervisor);
                        Notification.show(
                                "Autorizado por " + supervisor + ". Vuelva a cobrar la venta.",
                                4000,
                                Notification.Position.MIDDLE
                        ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        enfocarBuscador();
                    }
            ).open();

            return null;

        } catch (IllegalStateException
                | IllegalArgumentException ex) {

            Notification.show(
                    Objects.requireNonNullElse(
                            ex.getMessage(),
                            "No se pudo procesar la venta."
                    ),
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
                    "Error procesando la factura. La venta se mantiene abierta.",
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

            // El PDF de la factura anterior ya se imprimió: se libera antes de
            // registrar el nuevo (antes quedaban todos en memoria de la sesión).
            liberarUltimoPdf();

            StreamRegistration registration
                    = VaadinSession
                            .getCurrent()
                            .getResourceRegistry()
                            .registerResource(
                                    pdfResource
                            );

            ultimoPdfRegistrado = registration;

            String pdfUrl
                    = registration
                            .getResourceUri()
                            .toString();

            // La URL se pasa como parámetro ($0) en lugar de concatenarla en
            // el script con String.format.
            String script
                    = """
                    fetch($0)
                        .then(response => {
                            if (!response.ok) { throw new Error('HTTP ' + response.status); }
                            return response.blob();
                        })
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
                    """;

            UI.getCurrent()
                    .getPage()
                    .executeJs(script, pdfUrl);

            Notification.show(
                    "Enviando a la impresora de caja...",
                    2000,
                    Notification.Position.BOTTOM_END
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_SUCCESS
                    );

        } catch (Exception ex) {

            ex.printStackTrace();

            // La factura YA está guardada: se indica el número para poder
            // reimprimirla desde la consulta de facturas.
            Notification.show(
                    "La factura #" + facturaCodigo
                    + " se guardó, pero hubo un error al generar el documento.",
                    5000,
                    Notification.Position.TOP_CENTER
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_ERROR
                    );
        }

        enfocarBuscador();
    }

    private void liberarUltimoPdf() {

        if (ultimoPdfRegistrado != null) {

            ultimoPdfRegistrado.unregister();

            ultimoPdfRegistrado = null;
        }
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

                            marcarCajaAbierta(true);

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

    private long contarVentasPendientes() {

        return listaTicketsAbiertos
                .stream()
                .filter(
                        ticket
                        -> !ticket.getItems().isEmpty()
                )
                .count();
    }

    private void abrirDialogoCierreCaja() {

        // Al cerrar la caja se sale del POS y las ventas abiertas se pierden:
        // primero hay que cobrarlas o cancelarlas.
        if (contarVentasPendientes() > 0) {

            ClaseUtil.mostrarNotificacion(
                    "Cobre o cancele las ventas abiertas antes de cerrar la caja.",
                    NotificationVariant.LUMO_WARNING
            );

            enfocarBuscador();

            return;
        }

        DialogoCierreCaja dialogo
                = new DialogoCierreCaja(
                        cajaService,
                        USUARIO_POR_DEFECTO,
                        () -> {

                            // Caja cerrada: no se puede seguir vendiendo. El
                            // diálogo navega luego a la pantalla principal.
                            marcarCajaAbierta(false);
                        }
                );

        dialogo.addOpenedChangeListener(event -> {

            if (!event.isOpened() && cajaAbierta) {
                enfocarBuscador();
            }
        });

        dialogo.open();
    }

    private void abrirDialogoMovimientoPos() {

        DialogoMovimientoPos dialogo
                = new DialogoMovimientoPos(
                        cajaService,
                        USUARIO_POR_DEFECTO
                );

        dialogo.addOpenedChangeListener(event -> {

            if (!event.isOpened()) {
                enfocarBuscador();
            }
        });

        dialogo.open();
    }

    // ============================================================
    // TICKETS - ELIMINACIÓN
    // ============================================================
    /**
     * Descarta una venta (cancelada por el cajero).
     */
    private void eliminarTicket(
            TicketVenta ticket) {

        eliminarTicket(ticket, true);
    }

    /**
     * Cierra el ticket de una venta ya facturada. No muestra "Venta
     * descartada" porque la venta sí se completó.
     */
    private void cerrarTicketFacturado(
            TicketVenta ticket) {

        eliminarTicket(ticket, false);
    }

    private void eliminarTicket(
            TicketVenta ticket,
            boolean notificar) {

        if (ticket == null) {
            return;
        }

        /*
         * Siempre debe existir al menos un ticket abierto.
         * Por eso, cuando solo existe uno, se limpia en lugar
         * de eliminarse.
         */
        if (listaTicketsAbiertos.size() <= 1) {

            limpiarTicket(ticket);

            actualizarTitulosPestanas();

            if (notificar) {

                ClaseUtil.mostrarNotificacion(
                        "Ticket limpiado.",
                        NotificationVariant.LUMO_SUCCESS, 2000
                );
            }

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

        if (notificar) {

            ClaseUtil.mostrarNotificacion(
                    "Venta descartada.",
                    NotificationVariant.LUMO_WARNING, 2000
            );
        }

        enfocarBuscador();
    }

    /**
     * Deja el ticket como nuevo. Antes solo se vaciaban las líneas y el
     * cliente, el motorista y el tipo de venta de la venta anterior quedaban
     * pegados al siguiente cobro.
     */
    private void limpiarTicket(TicketVenta ticket) {

        ticket.getGrid().deselectAll();

        ticket.getItems().clear();

        ticket.setCliente(null);
        ticket.setNombreCliente(null);
        ticket.setDireccion(null);
        ticket.setDelivery(null);
        ticket.setTipoVenta(null);
        ticket.setEstadoFactura(null);

        ticket.updateUI();
    }

    private void confirmarEliminarTicket(
            TicketVenta ticket) {

        if (ticket == null) {
            return;
        }

        // Una venta vacía se descarta sin preguntar
        if (ticket.getItems().isEmpty()) {

            eliminarTicket(ticket);

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
    /**
     * Botón "$" de la tarjeta: el cajero escribe un monto en dinero y se agrega
     * la cantidad equivalente (monto / precio de la presentación).
     */
    private void abrirDialogoVentaPorMonto(
            ArticuloEmpaque articulo) {

        if (articulo == null) {
            return;
        }

        BigDecimal precioUnitario
                = calcularPrecioEmpaque(articulo, articulo);

        if (precioUnitario.signum() <= 0) {

            ClaseUtil.mostrarNotificacion(
                    "El producto no tiene precio de venta; no se puede vender por monto.",
                    NotificationVariant.LUMO_WARNING
            );

            enfocarBuscador();

            return;
        }

        new DialogoVentaPorMonto(
                articulo.getNombreArticulo(),
                precioUnitario,
                (monto, cantidadCalculada) -> {

                    if (ticketActivo == null
                    || monto == null) {
                        return;
                    }

                    // Se calcula con BigDecimal desde el monto (el diálogo
                    // divide con double y puede arrastrar decimales sueltos)
                    BigDecimal cantidad
                    = aDecimal(monto)
                            .divide(
                                    precioUnitario,
                                    ESCALA_CALCULO,
                                    RoundingMode.HALF_UP
                            );

                    if (cantidad.signum() > 0) {

                        agregarAlTicketActivo(
                                articulo,
                                cantidad
                        );
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

        DialogoEditarCantidad dialogo
                = new DialogoEditarCantidad(
                        ticket,
                        item,
                        nuevaCantidad -> enfocarBuscador(),
                        () -> confirmarEliminarItem(ticket, item)
                );

        /*
         * El diálogo solo cambia la cantidad (y, en la vista previa, el
         * subtotal). Al cerrarse, guardando o cancelando, se recalculan TODOS
         * los importes de la línea desde la cantidad final:
         * - al guardar, el ITBIS, el total y la cantidad base quedaban con los
         *   valores de la cantidad anterior y la factura se guardaba así;
         * - al cancelar, el subtotal de la vista previa quedaba en la línea.
         */
        dialogo.addOpenedChangeListener(event -> {

            if (event.isOpened()) {
                return;
            }

            recalcularTotalesItem(item);

            ticket.updateUI();

            actualizarTitulosPestanas();
        });

        dialogo.open();
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
                () -> {
                    actualizarTitulosPestanas();
                    enfocarBuscador();
                }
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
         * Dirección: la registrada en el cliente. Si es un delivery y el
         * cajero escribió una dirección de entrega en el diálogo, se usa esa.
         */
        String direccionFinal = clienteSeleccionado.getDireccion();

        if (esDelivery
                && direccion != null
                && !direccion.isBlank()) {

            direccionFinal = direccion.trim().toUpperCase();
        }

        ticket.setDireccion(
                direccionFinal
        );

        ticket.setTipoVenta(
                tipoVentaService.getTipoVenta(
                        TIPO_VENTA_LIBRETA
                )
        );

        FacturaDeVenta factura
                = guardar(ticket);

        cerrarDialogoCliente();

        if (factura == null) {

            // No se guardó: la venta se queda abierta para corregir y
            // reintentar (antes se descartaba igual y se perdía).
            enfocarBuscador();

            return;
        }

        cerrarTicketFacturado(ticket);

        imprimir(
                factura.getCodigo()
        );
    }

    private void cerrarDialogoCliente() {

        if (dialogoActivo != null) {

            dialogoActivo.close();

            dialogoActivo = null;
        }
    }

    private void abrirDialogoAbonoLibreta() {

        if (!cajaAbierta) {
            return;
        }

        // Solo los clientes que deben algo en su libreta
        libretaClientes.clear();

        libretaClientes.addAll(
                clienteService.getLista().stream()
                        .filter(c -> c.getBalance() != null && c.getBalance().signum() > 0)
                        .toList()
        );

        new DialogoAbonoLibreta(
                libretaClientes,
                reciboDeIngresoService,
                USUARIO_POR_DEFECTO,
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
    /**
     * Las ventas abiertas viven solo en esta vista: al salir se pierden. Si
     * hay ventas con productos se pide confirmación antes de salir (también
     * cuando el cierre de caja navega a la pantalla principal).
     */
    @Override
    public void beforeLeave(
            BeforeLeaveEvent event) {

        long ventasPendientes
                = contarVentasPendientes();

        // Sin caja abierta (p. ej. la salida que hace el cierre de caja) ya no
        // se puede seguir vendiendo, así que no tiene sentido retener al cajero.
        if (ventasPendientes == 0
                || salidaConfirmada
                || !cajaAbierta) {
            return;
        }

        BeforeLeaveEvent.ContinueNavigationAction continuar
                = event.postpone();

        Dialog confirmDialog
                = new Dialog();

        confirmDialog.setHeaderTitle(
                "Ventas sin cobrar"
        );

        confirmDialog.add(
                "Hay " + ventasPendientes
                + (ventasPendientes == 1
                        ? " venta con productos sin cobrar."
                        : " ventas con productos sin cobrar.")
                + " Si sale del punto de venta se perderán."
        );

        Button quedarseBtn
                = new Button(
                        "Seguir vendiendo [Enter]",
                        e -> confirmDialog.close()
                );

        quedarseBtn.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY
        );

        quedarseBtn.addClickShortcut(
                Key.ENTER
        );

        Button salirBtn
                = new Button(
                        "Salir y descartar",
                        e -> {

                            salidaConfirmada = true;

                            confirmDialog.close();

                            continuar.proceed();
                        }
                );

        salirBtn.addThemeVariants(
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY
        );

        confirmDialog
                .getFooter()
                .add(
                        salirBtn,
                        quedarseBtn
                );

        confirmDialog.addOpenedChangeListener(e -> {

            if (!e.isOpened() && !salidaConfirmada) {
                enfocarBuscador();
            }
        });

        confirmDialog.open();
    }

    /**
     * Devuelve el foco al buscador / lector de códigos SIN quitárselo a otro
     * componente que lo necesite:
     * <ul>
     * <li>si hay un diálogo abierto (cobro, anulación, cantidad...), no hace
     * nada: el foco es del diálogo;</li>
     * <li>si el buscador ya tiene el foco, tampoco lo vuelve a tomar (no pierde
     * lo que se esté escribiendo).</li>
     * </ul>
     */
    private void enfocarBuscador() {

        if (searchBox == null || !searchBox.isAttached() || !searchBox.isVisible() || !searchBox.isEnabled()) {
            return;
        }

        getUI().ifPresent(ui -> ui.access(() -> {

            // Con un diálogo modal abierto el foco le pertenece al diálogo
            if (ui.getInternals().hasModalComponent()) {
                return;
            }

            searchBox.clear();

            searchBox.getElement().executeJs(
                    """
                    if (document.querySelector($0)) { return; }
                    if (document.activeElement === this) { return; }
                    this.focus();
                    if (this.inputElement) { this.inputElement.select(); }
                    """,
                    SELECTOR_DIALOGO_ABIERTO
            );
        }));
    }

    /**
     * Un clic en una zona "vacía" de la pantalla devuelve el foco al buscador,
     * pero un clic sobre un control (botón, grid, campo, pestaña...) lo
     * respeta.
     *
     * Se resuelve en el navegador. Antes cualquier clic que subiera hasta la
     * vista (incluidos los "clics" sintéticos de los atajos F2, F3, Alt+C y
     * Alt+N, que son click-shortcuts de botones) mandaba el foco al buscador:
     * el diálogo recién abierto lo perdía y el grid dejaba de recibir Supr y *.
     */
    private void instalarFocoPorClick() {
        if (searchBox == null) {
            return;
        }

        getElement().executeJs(
                """
                const vista = $0;
                const buscador = $1;
                if (vista.__focoBuscadorInstalado) { return; }
                vista.__focoBuscadorInstalado = true;

                const interactivo = /^(VAADIN-(BUTTON|GRID|GRID-CELL-CONTENT|GRID-SORTER|TEXT-FIELD|TEXT-AREA|INTEGER-FIELD|NUMBER-FIELD|BIG-DECIMAL-FIELD|PASSWORD-FIELD|COMBO-BOX|SELECT|DATE-PICKER|CHECKBOX|RADIO-GROUP|TAB|TABS|ICON)|BUTTON|INPUT|TEXTAREA|SELECT|A)$/;

                vista.addEventListener('click', e => {
                    if (e.composedPath().some(n => n.tagName && interactivo.test(n.tagName))) { return; }
                    if (document.querySelector($2)) { return; }
                    buscador.focus();
                });
                """,
                getElement(),
                searchBox.getElement(),
                SELECTOR_DIALOGO_ABIERTO
        );
    }

    // ============================================================
    // ANULACIÓN
    // ============================================================
    /**
     * Anula la factura contra la capa de negocio.
     *
     * @return true si la factura quedó anulada; false si no existe o el
     * servicio la rechazó (ya anulada, cerrada, etc.).
     */
    private boolean ejecutarAnulacion(Integer codigoFactura, String motivo) {

        try {

            FacturaDeVenta facturaAnulada
                    = this.factService.anularVenta(
                            codigoFactura,
                            motivo,
                            USUARIO_POR_DEFECTO
                    );

            if (facturaAnulada == null) {

                ClaseUtil.mostrarNotificacion(
                        "No se encontró la factura N° " + codigoFactura,
                        NotificationVariant.LUMO_ERROR
                );

                return false;
            }

            ClaseUtil.mostrarNotificacion(
                    "Factura #" + codigoFactura + " anulada correctamente.",
                    NotificationVariant.LUMO_SUCCESS
            );

            return true;

        } catch (Exception ex) {

            ex.printStackTrace();

            // Factura ya anulada, cerrada o cualquier error de negocio
            Notification.show(
                    "Error al anular: "
                    + Objects.requireNonNullElse(
                            ex.getMessage(),
                            "error inesperado."
                    ),
                    4000,
                    Notification.Position.MIDDLE
            )
                    .addThemeVariants(
                            NotificationVariant.LUMO_ERROR
                    );

            return false;
        }
    }

    public void abrirDialogoAnulacion() {

        if (!cajaAbierta) {
            return;
        }

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Anular Factura / Venta");
        dialogo.setWidth("420px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Span aviso = new Span(
                "La anulación no se puede deshacer. Verifique el número de factura."
        );

        aviso.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-error-text-color)");

        IntegerField txtCodigoFactura = new IntegerField("Número de Factura");
        txtCodigoFactura.setPlaceholder("Ej. 1045");
        txtCodigoFactura.setMin(1);
        txtCodigoFactura.setWidthFull();
        txtCodigoFactura.setAutofocus(true);

        TextArea txtMotivo = new TextArea("Motivo de Anulación");
        txtMotivo.setPlaceholder("Ingrese la razón de la anulación...");
        txtMotivo.setMaxLength(250);
        txtMotivo.setWidthFull();

        layout.add(aviso, txtCodigoFactura, txtMotivo);

        Button btnAnular = new Button(
                "Confirmar Anulación",
                VaadinIcon.CLOSE_CIRCLE.create(),
                e -> {

                    Integer codigoFactura = txtCodigoFactura.getValue();
                    String motivo = txtMotivo.getValue();

                    if (codigoFactura == null || codigoFactura <= 0) {

                        ClaseUtil.mostrarNotificacion(
                                "Debe ingresar un número de factura válido.",
                                NotificationVariant.LUMO_WARNING
                        );

                        txtCodigoFactura.focus();

                        return;
                    }

                    if (motivo == null || motivo.trim().isEmpty()) {

                        ClaseUtil.mostrarNotificacion(
                                "Debe especificar el motivo de la anulación.",
                                NotificationVariant.LUMO_WARNING
                        );

                        txtMotivo.focus();

                        return;
                    }

                    // Solo se cierra si se anuló; si falló, el cajero puede
                    // corregir el número sin volver a escribir el motivo.
                    if (ejecutarAnulacion(codigoFactura, motivo.trim())) {
                        dialogo.close();
                    }
                });

        btnAnular.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        // Evita doble anulación por doble clic mientras procesa
        btnAnular.setDisableOnClick(true);

        btnAnular.addClickListener(e -> btnAnular.setEnabled(true));

        Button btnCancelar = new Button("Cancelar", e -> dialogo.close());
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialogo.addOpenedChangeListener(event -> {

            if (!event.isOpened()) {
                enfocarBuscador();
            }
        });

        dialogo.getFooter().add(btnCancelar, btnAnular);
        dialogo.add(layout);
        dialogo.open();
    }
}
