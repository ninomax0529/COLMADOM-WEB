package com.maxsoft.application.view.venta.puntoVenta;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.inventario.CategoriaService;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
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
import com.maxsoft.application.util.LineaPosUtil;
import com.maxsoft.application.view.componente.pos.DialogoAbonoLibreta;
import com.maxsoft.application.view.componente.pos.DialogoAnulacionFactura;
import com.maxsoft.application.view.componente.pos.DialogoCobroEfectivo;
import com.maxsoft.application.view.componente.pos.DialogoConfirmarEliminarItem;
import com.maxsoft.application.view.componente.pos.DialogoEditarCantidad;
import com.maxsoft.application.view.componente.pos.DialogoMovimientoPos;
import com.maxsoft.application.view.componente.pos.DialogoRenombrarTicket;
import com.maxsoft.application.view.componente.pos.DialogoSeleccionCliente;
import com.maxsoft.application.view.componente.pos.DialogoVentaPorMonto;
import com.maxsoft.application.view.componente.pos.GridTicketPos;
import com.maxsoft.application.view.componente.pos.ImpresoraFacturaPos;
import com.maxsoft.application.view.componente.pos.PanelCarritos;
import com.maxsoft.application.view.componente.pos.PanelProductos;
import com.maxsoft.application.view.componente.pos.PanelTicketContenido;
import com.maxsoft.application.view.componente.pos.ScriptsNavegadorPos;
import com.maxsoft.application.view.venta.cajaChica.DialogoAperturaCaja;
import com.maxsoft.application.view.venta.cajaChica.DialogoCierreCaja;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

@NpmPackage(value = "print-js", version = "1.6.0")
@JsModule("print-js/dist/print.js")
@CssImport("print-js/dist/print.css")
@PageTitle("Punto de Venta V17")
@Route(value = "puntoDeVentav17")
@Menu(order = 4, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class PuntoDeVentaView17 extends HorizontalLayout
        implements BeforeEnterObserver, BeforeLeaveObserver {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PuntoDeVentaView17.class);


    // ============================================================
    // CONSTANTES
    // ============================================================
    private static final String USUARIO_POR_DEFECTO = "Administrador";

    private static final int ESTADO_FACTURA_DELIVERY = 1;

    private static final int ESTADO_FACTURA_NORMAL = 2;

    private static final int TIPO_VENTA_LIBRETA = 2;

    private static final int TIPO_VENTA_CONTADO = 1;

    private static final int ESCALA_CALCULO = 4;

    /**
     * Ruta a la que se vuelve si el cajero cierra el diálogo de apertura sin
     * abrir caja. No puede ser la propia ruta del POS (se reabriría el diálogo
     * en bucle). Es la misma ruta que usan los diálogos de apertura y cierre
     * de caja.
     */
    private static final String RUTA_SIN_CAJA = "modulo/moduloPrincipal";

    // ============================================================
    // SERVICIOS
    // ============================================================
    private final CajaService cajaService;
    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService; // MULTI-EMPAQUE
    private final ClienteService clienteService;
    private final com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService reciboDeIngresoService;
    private final DeliveryService deliveryService;
    private final TipoVentaService tipoVentaService;
    private final EstadoFacturaService estadoFacturaService;
    private final FacturaDeVentaService factService;
    /** Valida la existencia con la misma regla que se aplica al facturar. */
    private final InventarioService inventarioService;
    /** Categorías para los botones de filtro del catálogo. */
    private final CategoriaService categoriaService;
    /** Autorización de supervisor (p. ej. vender de un lote vencido). */
    @org.springframework.beans.factory.annotation.Autowired
    private com.maxsoft.application.servicio.interfaces.venta.AutorizacionService autorizacionService;

    // ============================================================
    // COMPONENTES / ESTADO DEL POS
    // ============================================================
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

    /** Impresión de facturas (libera el PDF anterior al imprimir el siguiente). */
    private final ImpresoraFacturaPos impresora;

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
            InventarioService inventarioService,
            CategoriaService categoriaService,
            com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService reciboDeIngresoService
    ) {

        this.reciboDeIngresoService = reciboDeIngresoService;

        this.impresora = new ImpresoraFacturaPos(reporteService);
        this.cajaService = cajaService;
        this.factService = factService;
        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;
        this.clienteService = clienteService;
        this.deliveryService = deliveryService;
        this.tipoVentaService = tipoVentaService;
        this.estadoFacturaService = estadoFacturaService;
        this.inventarioService = inventarioService;
        this.categoriaService = categoriaService;

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

        ScriptsNavegadorPos.instalarBloqueoRecarga(getElement());
        sincronizarBloqueoRecarga();

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
        impresora.liberar();

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
    /** Atajos Alt+tecla (N, R, C, X, A, 1..9); ver ScriptsNavegadorPos. */
    private void instalarAtajosAlt() {

        ScriptsNavegadorPos.instalarAtajosAlt(getElement());
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

    /** F5 / Ctrl+R con ventas sin cobrar: el navegador ya canceló la recarga. */
    @ClientCallable
    public void avisoRecargaBloqueada() {

        long pendientes = contarVentasPendientes();

        ClaseUtil.mostrarNotificacion(
                "No se puede refrescar: hay " + pendientes
                + (pendientes == 1 ? " venta" : " ventas")
                + " con productos sin cobrar. Cóbrelas o cancélelas primero.",
                NotificationVariant.LUMO_WARNING
        );

        enfocarBuscador();
    }

    /**
     * Avisa al navegador si hay ventas con productos, para bloquear F5 /
     * Ctrl+R y pedir confirmación al recargar o cerrar la pestaña.
     */
    private void sincronizarBloqueoRecarga() {

        ScriptsNavegadorPos.marcarVentaConProductos(
                getElement(),
                contarVentasPendientes() > 0
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
                factService,
                categoriaService.listarHabilitadas(),
                inventarioService
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
                = LineaPosUtil.buscarLinea(ticket.getItems(), articulo);

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

        // Lo que ya hay en el ticket (todas las presentaciones del artículo)
        // más lo que se agrega, en unidad base
        BigDecimal requerido
                = LineaPosUtil.cantidadBase(ticket.getItems(), articuloEmpaque)
                        .add(cantidad.multiply(
                                LineaPosUtil.factorConversion(articuloEmpaque)));

        if (!hayExistencia(articuloEmpaque, requerido)) {
            enfocarBuscador();
            return;
        }

        Optional<DetalleFacturaDeVenta> existente
                = LineaPosUtil.buscarLinea(ticket.getItems(), articuloEmpaque);

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

    /**
     * Valida la existencia ANTES de poner el producto en la venta, con la
     * misma regla que aplica el cobro (InventarioService), para que el aviso
     * salga al agregar y no al facturar.
     * <ul>
     * <li>No alcanza y el artículo no permite venta sin existencia: muestra el
     * error y devuelve false (no se agrega).</li>
     * <li>No alcanza pero sí lo permite: avisa que se venderá en negativo.</li>
     * </ul>
     *
     * @param requeridoBase total del artículo en la venta, en unidad base
     */
    private boolean hayExistencia(ArticuloEmpaque empaque, BigDecimal requeridoBase) {

        Articulo base = empaque == null ? null : empaque.getArticulo();

        if (base == null || base.getCodigo() == null) {
            return true;
        }

        try {
            inventarioService.validarStockDisponible(base.getCodigo(), requeridoBase);
        } catch (IllegalStateException e) {
            Notification bloqueo = Notification.show(
                    "⛔ " + e.getMessage() + " Este artículo no permite venta sin existencia.",
                    5000,
                    Notification.Position.TOP_CENTER
            );
            bloqueo.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (Boolean.TRUE.equals(base.getInventariable())) {

            BigDecimal stock = inventarioService.existenciaDisponible(base.getCodigo());

            if (stock.compareTo(requeridoBase) < 0) {

                Notification notif = Notification.show(
                        "⚠️ Notificación: " + empaque.getNombreArticulo()
                        + " no tiene stock suficiente en sistema ("
                        + stock.stripTrailingZeros().toPlainString()
                        + " en unidad base). Se venderá en negativo.",
                        3000,
                        Notification.Position.TOP_CENTER
                );

                notif.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        }

        return true;
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

        LineaPosUtil.recalcular(item);
    }

    private void agregarNuevoItemConEmpaque(
            TicketVenta ticket,
            ArticuloEmpaque articuloEmpaque,
            ArticuloEmpaque empaque,
            BigDecimal cantidad) {

        DetalleFacturaDeVenta nuevoDetalle = LineaPosUtil.nuevaLinea(
                articuloEmpaque,
                empaque,
                cantidad,
                LineaPosUtil.siguienteNumeroLinea(ticket.getItems())
        );

        ticket.getItems().add(nuevoDetalle);

        // La línea recién agregada queda seleccionada: "*" y Supr actúan
        // sobre ella sin tener que tocar el grid.
        ticket.getGrid().select(nuevoDetalle);
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

        GridTicketPos.configurar(nuevoTicket, this::abrirDialogoEditarCantidad, this::confirmarEliminarItem);

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

        // Se llama tras cada cambio de líneas o de ventas abiertas
        sincronizarBloqueoRecarga();
    }

    private VerticalLayout construirContenidoPanelTicket(
            TicketVenta ticket) {

        return new PanelTicketContenido(
                ticket,
                this::abrirDialogoRenombrarTicket,
                this::confirmarEliminarTicket,
                this::abrirDialogoCobroEfectivo,
                this::abrirDialogoSeleccionCliente,
                this::abrirDialogoDelivery
        );
    }

    // ============================================================
    // DELIVERY (F6)
    // ============================================================
    /**
     * Venta por delivery: factura de contado PENDIENTE con el motorista
     * asignado. El motorista cobra al entregar y el administrador la liquida
     * después (Delivery → Liquidación).
     */
    private void abrirDialogoDelivery(
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

        List<Delivery> motoristas = deliveryService.getLista();

        if (motoristas == null || motoristas.isEmpty()) {

            ClaseUtil.mostrarNotificacion(
                    "No hay motoristas registrados.",
                    NotificationVariant.LUMO_WARNING
            );

            enfocarBuscador();

            return;
        }

        List<Cliente> clientes = clienteService.getLista();

        com.maxsoft.application.view.componente.pos.DialogoDelivery dialogo
                = new com.maxsoft.application.view.componente.pos.DialogoDelivery(
                        ticket,
                        motoristas,
                        clientes,
                        (motorista, cliente, direccion) -> {

                            // Cliente de paso: el genérico (el mismo que usa COBRAR)
                            Cliente clienteFactura = cliente != null
                                    ? cliente
                                    : clientes.get(0);

                            ticket.setDelivery(motorista);
                            ticket.setEstadoFactura(
                                    estadoFacturaService.getEstadoFactura(
                                            ESTADO_FACTURA_DELIVERY
                                    )
                            );
                            ticket.setTipoVenta(
                                    tipoVentaService.getTipoVenta(
                                            TIPO_VENTA_CONTADO
                                    )
                            );
                            ticket.setCliente(clienteFactura);
                            ticket.setNombreCliente(
                                    cliente != null
                                            ? cliente.getNombre()
                                            : direccion
                            );
                            ticket.setDireccion(direccion);

                            FacturaDeVenta factura = guardar(ticket);

                            if (factura == null) {
                                // No se guardó: la venta queda abierta para corregir
                                enfocarBuscador();
                                return;
                            }

                            cerrarTicketFacturado(ticket);

                            ClaseUtil.mostrarNotificacion(
                                    "Delivery enviado con " + motorista.getNombre()
                                    + ". Queda pendiente de liquidar.",
                                    NotificationVariant.LUMO_SUCCESS, 3500
                            );

                            imprimir(factura.getCodigo());
                        }
                );

        dialogo.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                enfocarBuscador();
            }
        });

        dialogo.open();
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

            log.error("Error inesperado", ex);
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

        impresora.imprimir(facturaCodigo);

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
        refrescarExistenciasCatalogo();
    }

    /** La venta/anulación cambió el inventario: actualizar las tarjetas. */
    private void refrescarExistenciasCatalogo() {
        if (panelProductos != null) {
            panelProductos.refrescarExistencias();
        }
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

        // La venta quedó vacía (p. ej. después de cobrar)
        sincronizarBloqueoRecarga();
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
                = LineaPosUtil.precioEmpaque(articulo, articulo);

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

        // Para volver a ella si la nueva cantidad no tiene existencia
        final BigDecimal cantidadAnterior = valorSeguro(item.getCantidad());

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

            // Si subió la cantidad, se valida la existencia ahora y no al
            // cobrar; si no alcanza, la línea vuelve a la cantidad anterior.
            if (ticket.getItems().contains(item)
                    && item.getArticuloEmpaque() != null
                    && valorSeguro(item.getCantidad()).compareTo(cantidadAnterior) > 0
                    && !hayExistencia(item.getArticuloEmpaque(),
                            LineaPosUtil.cantidadBase(ticket.getItems(), item.getArticuloEmpaque()))) {

                item.setCantidad(cantidadAnterior);
            }

            LineaPosUtil.recalcular(item);

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

            ScriptsNavegadorPos.enfocarBuscador(searchBox.getElement());
        }));
    }

    /** Un clic en una zona vacía de la pantalla devuelve el foco al buscador. */
    private void instalarFocoPorClick() {
        if (searchBox == null) {
            return;
        }

        ScriptsNavegadorPos.instalarFocoPorClick(getElement(), searchBox.getElement());
    }

    // ============================================================
    // ANULACIÓN
    // ============================================================
    public void abrirDialogoAnulacion() {

        if (!cajaAbierta) {
            return;
        }

        new DialogoAnulacionFactura(factService, USUARIO_POR_DEFECTO, () -> {
            refrescarExistenciasCatalogo();
            enfocarBuscador();
        }).open();
    }
}
