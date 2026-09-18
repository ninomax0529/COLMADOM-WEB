///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.maxsoft.application.view.venta.puntoVenta;
//
//import com.maxsoft.application.modelo.Articulo;
//import com.maxsoft.application.modelo.CajaTurno;
//import com.maxsoft.application.modelo.Cliente;
//import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
//import com.maxsoft.application.modelo.FacturaDeVenta;
//import com.maxsoft.application.servicio.impl.venta.ImpresionDirectaService;
//import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
//import com.maxsoft.application.servicio.interfaces.venta.CajaService;
//import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
//import com.maxsoft.application.servicio.interfaces.venta.DeliveryService;
//import com.maxsoft.application.servicio.interfaces.venta.EstadoFacturaService;
//import com.maxsoft.application.servicio.interfaces.venta.FacturaDeVentaService;
//import com.maxsoft.application.servicio.interfaces.reporte.ReporteService;
//import com.maxsoft.application.servicio.interfaces.venta.TipoVentaService;
//import com.maxsoft.application.util.ClaseUtil;
//import com.maxsoft.application.view.componente.pos.DialogoAbonoLibreta;
//import com.maxsoft.application.view.componente.pos.DialogoCobroEfectivo;
//import com.maxsoft.application.view.componente.pos.DialogoConfirmarEliminarItem;
//import com.maxsoft.application.view.componente.pos.DialogoEditarCantidad;
//import com.maxsoft.application.view.componente.pos.DialogoMovimientoPos;
//import com.maxsoft.application.view.componente.pos.DialogoRenombrarTicket;
//import com.maxsoft.application.view.componente.pos.DialogoSeleccionCliente;
//import com.maxsoft.application.view.componente.pos.DialogoVentaPorMonto;
//import com.maxsoft.application.view.componente.pos.PanelCarritos;
//import com.maxsoft.application.view.componente.pos.PanelProductos;
//import com.maxsoft.application.view.componente.pos.PanelTicketContenido;
//import com.maxsoft.application.view.venta.cajaChica.DialogoAperturaCaja;
//import com.maxsoft.application.view.venta.cajaChica.DialogoCierreCaja;
//import com.vaadin.flow.component.AttachEvent;
//import com.vaadin.flow.component.DetachEvent;
//import com.vaadin.flow.component.Key;
//import com.vaadin.flow.component.KeyModifier;
//import com.vaadin.flow.component.ShortcutRegistration;
//import com.vaadin.flow.component.Shortcuts;
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.dependency.CssImport;
//import com.vaadin.flow.component.dependency.JsModule;
//import com.vaadin.flow.component.dependency.NpmPackage;
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.tabs.TabSheet;
//import com.vaadin.flow.router.BeforeEnterEvent;
//import com.vaadin.flow.router.BeforeEnterObserver;
//import com.vaadin.flow.router.BeforeLeaveEvent;
//import com.vaadin.flow.router.BeforeLeaveObserver;
//import com.vaadin.flow.router.Menu;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.server.StreamRegistration;
//import com.vaadin.flow.server.StreamResource;
//import com.vaadin.flow.server.VaadinSession;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import net.sf.jasperreports.engine.JasperPrint;
//import org.vaadin.lineawesome.LineAwesomeIconUrl;
//
//@NpmPackage(value = "print-js", version = "1.6.0")
//@JsModule("print-js/dist/print.js")
//@CssImport("print-js/dist/print.css")
//@PageTitle("Punto de Venta V10")
//@Route(value = "puntoDeVentav10")
//@Menu(order = 4, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
//public class PuntoDeVentaViewV10 extends HorizontalLayout implements BeforeEnterObserver, BeforeLeaveObserver {
//
//    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");
//
//    // Servicios
//    private final ReporteService reporteService;
//    private final CajaService cajaService;
//    private final ArticuloService articuloService;
//    private final ClienteService clienteService;
//    private final DeliveryService deliveryService;
//    private final TipoVentaService tipoVentaService;
//    private final EstadoFacturaService estadoFacturaService;
//    private final FacturaDeVentaService factService;
//    // 1. Agregar el atributo
//    private final ImpresionDirectaService impresionDirectaService;
//
//    // Componentes de UI y Estado
//    private final TabSheet ticketTabSheet = new TabSheet();
//    private final List<TicketVenta> listaTicketsAbiertos = new ArrayList<>();
//    private final ComboBox<Articulo> searchBox = new ComboBox<>("Buscar producto o escanear código");
//    private final List<Cliente> libretaClientes = new ArrayList<>();
//    private final List<ShortcutRegistration> atajosRegistrados = new ArrayList<>();
//
//    private TicketVenta ticketActivo;
//    private int contadorSecuencialTickets = 0;
//    private int contadorLineas = 1;
//    private boolean cajaAbierta = false;
//    // Declarar una variable a nivel de clase en tu pantalla principal
//    private DialogoSeleccionCliente dialogoActivo;
//
//    public PuntoDeVentaViewV10(
//            ReporteService reporteService,
//            CajaService cajaService,
//            FacturaDeVentaService factService,
//            ArticuloService articuloService,
//            ClienteService clienteService,
//            DeliveryService deliveryService,
//            TipoVentaService tipoVentaService,
//            EstadoFacturaService estadoFacturaService,
//            ImpresionDirectaService impresionDirectaService
//    ) {
//        this.reporteService = reporteService;
//        this.cajaService = cajaService;
//        this.factService = factService;
//        this.articuloService = articuloService;
//        this.clienteService = clienteService;
//        this.deliveryService = deliveryService;
//        this.tipoVentaService = tipoVentaService;
//        this.estadoFacturaService = estadoFacturaService;
//        this.impresionDirectaService = impresionDirectaService;
//
//        enfocarBuscador();
//        // En el constructor o donde registras el atajo F3:
//        Shortcuts.addShortcutListener(this, () -> {
//            // Solo abre si no hay un diálogo abierto ya
//            if (dialogoActivo == null || !dialogoActivo.isOpened()) {
//                abrirDialogoSeleccionCliente(ticketActivo);
//            }
//        }, Key.F3);
//        setSizeFull();
//        setSpacing(true);
//
//        searchBox.setItemLabelGenerator(Articulo::getDescripcion);
//
//        VerticalLayout leftPanel = crearPanelProductos();
//        leftPanel.setWidth("55%");
//        leftPanel.setHeightFull();
//
//        VerticalLayout rightPanel = crearPanelCarritos();
//        rightPanel.setWidth("45%");
//        rightPanel.setHeightFull();
//
//        add(leftPanel, rightPanel);
//
//        // Suponiendo que tu vista principal es o tiene un layout contenedor (ej. VerticalLayout)
//        this.addClickListener(event -> {
//            // Si el usuario hace clic en el fondo de la pantalla, regresa el foco al buscador
//            restaurarFocoArticulos();
//        });
//
//        // Listener para sincronizar selección manual de pestañas
//        ticketTabSheet.addSelectedChangeListener(event -> {
//            int index = ticketTabSheet.getSelectedIndex();
//            if (index >= 0 && index < listaTicketsAbiertos.size()) {
//                ticketActivo = listaTicketsAbiertos.get(index);
//                enfocarBuscador();
//            }
//        });
//
//        crearNuevoTicket(null);
//    }
//
//    @Override
//    public void beforeEnter(BeforeEnterEvent event) {
//        // Verificar si existe un turno de caja activo en el backend
//        Optional<CajaTurno> turnoActivo = cajaService.obtenerCajaAbierta();
//        if (turnoActivo.isPresent()) {
//            this.cajaAbierta = true;
//            setVisible(true);
//        } else {
//            this.cajaAbierta = false;
//            setVisible(false);
//        }
//    }
//
//    @Override
//    protected void onAttach(AttachEvent attachEvent) {
//        super.onAttach(attachEvent);
//        configurarAtajosTeclado();
//
//        if (attachEvent.isInitialAttach()) {
//            if (!cajaAbierta) {
//                Optional<CajaTurno> turnoActivo = cajaService.obtenerCajaAbierta();
//                if (turnoActivo.isPresent()) {
//                    this.cajaAbierta = true;
//                    setVisible(true);
//                    enfocarBuscador();
//                    return;
//                }
//
//                DialogoAperturaCaja dialogo = new DialogoAperturaCaja(cajaService, fondo -> {
//                    this.cajaAbierta = true;
//                    // Mostrar la vista y enfocar el POS inmediatamente
//                    getUI().ifPresent(ui -> ui.access(() -> {
//                        setVisible(true);
//                        enfocarBuscador();
//                    }));
//                });
//
//                // Si se cierra el diálogo sin abrir la caja, regresar a la pantalla principal
//                dialogo.addOpenedChangeListener(e -> {
//                    if (!e.isOpened() && !cajaAbierta) {
//                        getUI().ifPresent(ui -> ui.navigate("puntoDeVentav9"));
//                    }
//                });
//
//                dialogo.open();
//            } else {
//                setVisible(true);
//                enfocarBuscador();
//            }
//        }
//    }
//
//    @Override
//    protected void onDetach(DetachEvent detachEvent) {
//        super.onDetach(detachEvent);
//        atajosRegistrados.forEach(ShortcutRegistration::remove);
//        atajosRegistrados.clear();
//    }
//
//    private void configurarAtajosTeclado() {
//        Key[] digitos = {Key.DIGIT_1, Key.DIGIT_2, Key.DIGIT_3, Key.DIGIT_4, Key.DIGIT_5, Key.DIGIT_6, Key.DIGIT_7, Key.DIGIT_8, Key.DIGIT_9};
//        Key[] numpadDigitos = {Key.NUMPAD_1, Key.NUMPAD_2, Key.NUMPAD_3, Key.NUMPAD_4, Key.NUMPAD_5, Key.NUMPAD_6, Key.NUMPAD_7, Key.NUMPAD_8, Key.NUMPAD_9};
//
//        for (int j = 0; j < 9; j++) {
//            final int index = j;
//            atajosRegistrados.add(Shortcuts.addShortcutListener(this, () -> seleccionarTicketPorPosicion(index), digitos[j], KeyModifier.ALT));
//            atajosRegistrados.add(Shortcuts.addShortcutListener(this, () -> seleccionarTicketPorPosicion(index), numpadDigitos[j], KeyModifier.ALT));
//        }
//
//        atajosRegistrados.add(Shortcuts.addShortcutListener(this, () -> {
//            if (ticketActivo != null) {
//                abrirDialogoRenombrarTicket(ticketActivo, new Span(ticketActivo.getId()));
//            }
//        }, Key.KEY_R, KeyModifier.ALT));
//
//        atajosRegistrados.add(Shortcuts.addShortcutListener(this, this::abrirDialogoAbonoLibreta, Key.KEY_A, KeyModifier.ALT));
//    }
//
//    private void seleccionarTicketPorPosicion(int index) {
//        if (index >= 0 && index < listaTicketsAbiertos.size()) {
//            ticketTabSheet.setSelectedIndex(index);
//            ClaseUtil.mostrarNotificacion("Cambiado a " + listaTicketsAbiertos.get(index).getId(), NotificationVariant.LUMO_SUCCESS);
////            Notification.show("Cambiado a " + listaTicketsAbiertos.get(index).getId(), 1500, Notification.Position.TOP_CENTER);
//            enfocarBuscador();
//        }
//    }
//
//    private VerticalLayout crearPanelProductos() {
//        return new PanelProductos(
//                this.articuloService,
//                this::agregarAlTicketActivo,
//                this::agregarAlTicketActivo,
//                this::abrirDialogoVentaPorMonto,
//                this::abrirDialogoAperturaCaja,
//                this::abrirDialogoCierreCaja,
//                this::abrirDialogoMovimientoPos
//        );
//    }
//
//private VerticalLayout crearPanelCarritos() {
//    return new PanelCarritos(
//            ticketTabSheet,
//            listaTicketsAbiertos,
//
//            // Nueva venta
//            () -> crearNuevoTicket(null),
//
//            // Cambiar nombre
//            () -> {
//                if (ticketActivo != null) {
//                    abrirDialogoRenombrarTicket(
//                            ticketActivo,
//                            new Span(ticketActivo.getId())
//                    );
//                }
//            },
//
//            // Selección de ticket
//            ticketSeleccionado -> {
//                this.ticketActivo = ticketSeleccionado;
//                enfocarBuscador();
//            }
//    );
//}
//
//    private void agregarAlTicketActivo(Articulo articulo) {
//        agregarAlTicketActivo(articulo, 1.0);
//    }
//
//    private void agregarAlTicketActivo(Articulo articulo, Double cantidad) {
//        if (ticketActivo == null || articulo == null) {
//            return;
//        }
//
//        Optional<DetalleFacturaDeVenta> existente = ticketActivo.getItems().stream()
//                .filter(item -> item.getArticulo().equals(articulo))
//                .findFirst();
//
//        if (existente.isPresent()) {
//            DetalleFacturaDeVenta item = existente.get();
//            double nuevaCantidad = item.getCantidad() + cantidad;
//            if (nuevaCantidad <= 0) {
//                confirmarEliminarItem(ticketActivo, item);
//            } else {
//                item.setCantidad(nuevaCantidad);
//                recalcularTotalesItem(item);
//                ticketActivo.updateUI();
//            }
//        } else {
//            DetalleFacturaDeVenta nuevoDetalle = new DetalleFacturaDeVenta();
//            nuevoDetalle.setCodigo(articulo.getCodigo());
//            nuevoDetalle.setArticulo(articulo);
//            nuevoDetalle.setNumeroDeLinea(contadorLineas++);
//            nuevoDetalle.setDescripcionArticulo(articulo.getDescripcion());
//            nuevoDetalle.setCantidad(cantidad);
//            nuevoDetalle.setExistenciaActual(articulo.getExistencia());
//            nuevoDetalle.setPrecioVenta(articulo.getPrecioVenta());
//            nuevoDetalle.setPorcientoDescuento(0.00);
//            nuevoDetalle.setPorcientoItbis(18.00);
//            nuevoDetalle.setNombreAlmacen("General");
//            nuevoDetalle.setNombreUnidad("Unidad");
//
//            recalcularTotalesItem(nuevoDetalle);
//
//            ticketActivo.getItems().add(nuevoDetalle);
//            ticketActivo.updateUI();
//        }
//
//        enfocarBuscador();
//    }
//
//    private void recalcularTotalesItem(DetalleFacturaDeVenta item) {
//        BigDecimal cantidad = BigDecimal.valueOf(item.getCantidad());
//        BigDecimal precio = BigDecimal.valueOf(item.getPrecioVenta());
//        BigDecimal porcDesc = BigDecimal.valueOf(item.getPorcientoDescuento());
//        BigDecimal porcItbis = BigDecimal.valueOf(item.getPorcientoItbis());
//
//        BigDecimal subTotal = cantidad.multiply(precio);
//        BigDecimal totalDesc = subTotal.multiply(porcDesc).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
//        BigDecimal subTotalConDesc = subTotal.subtract(totalDesc);
//        BigDecimal totalItbis = subTotalConDesc.multiply(porcItbis).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
//        BigDecimal total = subTotalConDesc.add(totalItbis);
//
//        item.setSubTotal(subTotal.setScale(2, RoundingMode.HALF_UP).doubleValue());
//        item.setTotalDescuento(totalDesc.setScale(2, RoundingMode.HALF_UP).doubleValue());
//        item.setTotalItbis(totalItbis.setScale(2, RoundingMode.HALF_UP).doubleValue());
//        item.setTotal(total.setScale(2, RoundingMode.HALF_UP).doubleValue());
//    }
//
//    private void crearNuevoTicket(String nombrePersonalizado) {
//        contadorSecuencialTickets++;
//        String nombreFinal = (nombrePersonalizado != null && !nombrePersonalizado.trim().isEmpty())
//                ? nombrePersonalizado.trim()
//                : "Venta " + contadorSecuencialTickets;
//
//        TicketVenta nuevoTicket = new TicketVenta(contadorSecuencialTickets, nombreFinal);
//        listaTicketsAbiertos.add(nuevoTicket);
//
//        Grid<DetalleFacturaDeVenta> gridDet = nuevoTicket.getGrid();
//        gridDet.setDataProvider(nuevoTicket.getDataProvider());
//        gridDet.setSelectionMode(Grid.SelectionMode.SINGLE);
//
//        gridDet.addColumn(DetalleFacturaDeVenta::getArticulo).setHeader("Producto").setAutoWidth(true);
//        gridDet.addColumn(DetalleFacturaDeVenta::getCantidad).setHeader("Cant.");
//        gridDet.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getPrecioVenta())).setHeader("Precio");
//        gridDet.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getSubTotal())).setHeader("Total");
//
//        gridDet.addComponentColumn(item -> {
//            Button editQtyBtn = new Button(VaadinIcon.EDIT.create(), e -> abrirDialogoEditarCantidad(nuevoTicket, item));
//            editQtyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
//
//            Button removeBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmarEliminarItem(nuevoTicket, item));
//            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
//
//            return new HorizontalLayout(editQtyBtn, removeBtn);
//        }).setHeader("");
//
//        gridDet.addItemDoubleClickListener(e -> abrirDialogoEditarCantidad(nuevoTicket, e.getItem()));
//
//        // Gestión de atajos limpia para el grid
//        atajosRegistrados.add(Shortcuts.addShortcutListener(gridDet, () -> {
//            DetalleFacturaDeVenta seleccionado = gridDet.asSingleSelect().getValue();
//            if (seleccionado != null) {
//                confirmarEliminarItem(nuevoTicket, seleccionado);
//            }
//        }, Key.DELETE));
//
//        atajosRegistrados.add(Shortcuts.addShortcutListener(gridDet, () -> {
//            DetalleFacturaDeVenta seleccionado = gridDet.asSingleSelect().getValue();
//            if (seleccionado != null) {
//                abrirDialogoEditarCantidad(nuevoTicket, seleccionado);
//            }
//        }, Key.NUMPAD_MULTIPLY));
//
//        gridDet.addCellFocusListener(e -> e.getItem().ifPresent(gridDet::select));
//        gridDet.setHeightFull();
//
//        VerticalLayout contenidoTab = construirContenidoPanelTicket(nuevoTicket);
//        ticketTabSheet.add(nombreFinal, contenidoTab);
//        ticketTabSheet.setSelectedTab(ticketTabSheet.getTab(contenidoTab));
//        ticketActivo = nuevoTicket;
//
//        actualizarTitulosPestanas();
//        enfocarBuscador();
//
//    }
//
//    private void actualizarTitulosPestanas() {
//        for (int j = 0; j < listaTicketsAbiertos.size(); j++) {
//            TicketVenta ticket = listaTicketsAbiertos.get(j);
//            ticketTabSheet.getTabAt(j).setLabel("[Alt+" + (j + 1) + "] " + ticket.getId());
//        }
//    }
//
//    private VerticalLayout construirContenidoPanelTicket(TicketVenta ticket) {
//        return new PanelTicketContenido(
//                ticket,
//                this::abrirDialogoRenombrarTicket,
//                this::confirmarEliminarTicket,
//                this::abrirDialogoCobroEfectivo,
//                this::abrirDialogoSeleccionCliente
//        );
//    }
//
//    private void abrirDialogoCobroEfectivo(TicketVenta ticket) {
//
//        if (ticket.getItems().isEmpty()) {
//
//            ClaseUtil.mostrarNotificacion("La : [" + ticket.getId() + "] está vacía", NotificationVariant.LUMO_SUCCESS);
//
//            return;
//        }
//
//        DialogoCobroEfectivo dialogo = new DialogoCobroEfectivo(
//                ticket,
//                this.deliveryService,
//                this.estadoFacturaService,
//                this.clienteService,
//                this.tipoVentaService,
//                ticketAGuardar -> {
//                    FacturaDeVenta f = guardar(ticketAGuardar);
//                    if (f != null) {
//
//                        imprimir(f.getCodigo());
//                    }
//                },
//                this::cerrarTicketActual,
//                this::enfocarBuscador
//        );
//        dialogo.open();
//    }
//
//    private FacturaDeVenta guardar(TicketVenta ticketVenta) {
//        try {
//            String usuarioActual = "Administrador";
//            FacturaDeVenta facturaGuardada = factService.procesarVenta(ticketVenta, usuarioActual);
//
////            ClaseUtil.mostrarNotificacion("Factura guardada correctamente", NotificationVariant.LUMO_SUCCESS);
////            Notification.show("Factura guardada correctamente", 3000, Notification.Position.TOP_CENTER)
////                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//            return facturaGuardada;
//        } catch (IllegalStateException | IllegalArgumentException ex) {
//            Notification.show(ex.getMessage(), 3500, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
//            return null;
//        } catch (Exception ex) {
//
//            ClaseUtil.mostrarNotificacion("Error procesando la factura: ", NotificationVariant.LUMO_ERROR);
////              
////            Notification.show("Error procesando la factura: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
////                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//
//            return null;
//        }
//    }
//
//    private void imprimir(int facturaCodigo) {
//        try {
//            // 1. Obtener el recurso del PDF usando tu método existente
//            StreamResource pdfResource = reporteService.generarReporteFacturaVenta(facturaCodigo);
//            pdfResource.setContentType("application/pdf");
//
//            // 2. Registrar el recurso dinámico en la sesión actual
//            StreamRegistration registration = VaadinSession.getCurrent()
//                    .getResourceRegistry()
//                    .registerResource(pdfResource);
//
//            String pdfUrl = registration.getResourceUri().toString();
//
//            // 3. Ejecutar el script JS que descarga el Blob e invoca iframe.print()
//            String script = String.format(
//                    "fetch('%s')"
//                    + ".then(response => response.blob())"
//                    + ".then(blob => {"
//                    + "   const blobUrl = URL.createObjectURL(blob);"
//                    + "   let iframe = document.getElementById('kiosk-print-frame');"
//                    + "   if (!iframe) {"
//                    + "       iframe = document.createElement('iframe');"
//                    + "       iframe.id = 'kiosk-print-frame';"
//                    + "       iframe.style.position = 'fixed';"
//                    + "       iframe.style.right = '0';"
//                    + "       iframe.style.bottom = '0';"
//                    + "       iframe.style.width = '0';"
//                    + "       iframe.style.height = '0';"
//                    + "       iframe.style.border = '0';"
//                    + "       document.body.appendChild(iframe);"
//                    + "   }"
//                    + "   iframe.src = blobUrl;"
//                    + "   iframe.onload = function() {"
//                    + "       setTimeout(() => {"
//                    + "           iframe.contentWindow.focus();"
//                    + "           iframe.contentWindow.print();"
//                    + "           URL.revokeObjectURL(blobUrl);"
//                    + "       }, 300);"
//                    + "   };"
//                    + "}).catch(err => console.error('Error al imprimir en modo kiosk:', err));",
//                    pdfUrl
//            );
//
//            UI.getCurrent().getPage().executeJs(script);
//
////                ClaseUtil.mostrarNotificacion("Enviando a la impresora de caja...",NotificationVariant.LUMO_SUCCESS);
//            Notification.show("Enviando a la impresora de caja...", 2000, Notification.Position.BOTTOM_END)
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//
//        } catch (Exception ex) {
//            Notification.show("Error al generar el documento: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        }
//
//        enfocarBuscador();
//    }
//
//    private void imprimirCaja(int facturaCodigo) {
//        try {
//
//            JasperPrint jasperPrint = reporteService.generarJasperPrintFacturaVenta(facturaCodigo);
//            impresionDirectaService.imprimirJasperDirecto(jasperPrint, null);
//
//            Notification.show("Imprimiendo ticket de venta...", 2000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//
//            enfocarBuscador();
//        } catch (Exception ex) {
//            Notification.show("Error al enviar a la impresora: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        }
//    }
//
//    private void enfocarBuscador() {
//        if (searchBox != null) {
//            searchBox.focus();
//        }
//    }
//
//    private void abrirDialogoAbonoLibreta() {
//        new DialogoAbonoLibreta(this.libretaClientes, this::enfocarBuscador).open();
//    }
//
//    private void abrirDialogoAperturaCaja() {
//        DialogoAperturaCaja dialogo = new DialogoAperturaCaja(cajaService, fondo -> {
//            this.cajaAbierta = true;
//            getUI().ifPresent(ui -> ui.access(() -> {
//                setVisible(true);
//
//                this.cajaAbierta = true;
//                mostrarPosConTransicion();
//            }));
//        });
//        dialogo.open();
//
//        enfocarBuscador();
//    }
//
//    private void cerrarTicketActual() {
//        eliminarTicket(ticketActivo);
//    }
//
//    private void eliminarTicket(TicketVenta ticket) {
//        if (ticket == null) {
//            return;
//        }
//
//        if (listaTicketsAbiertos.size() <= 1) {
//            ticket.getItems().clear();
//            ticket.updateUI();
//            ClaseUtil.mostrarNotificacion("Ticket limpiado.", NotificationVariant.LUMO_SUCCESS);
////            Notification.show("Ticket limpiado.", 3000, Notification.Position.MIDDLE);
//            enfocarBuscador();
//            return;
//        }
//
//        int index = listaTicketsAbiertos.indexOf(ticket);
//        if (index >= 0) {
//            listaTicketsAbiertos.remove(ticket);
//            ticketTabSheet.remove(ticketTabSheet.getTabAt(index));
//
//            // Reasignar el ticket activo al ticket adyacente o al primero disponible
//            int nuevoIndex = Math.max(0, index - 1);
//            ticketActivo = listaTicketsAbiertos.get(nuevoIndex);
//            ticketTabSheet.setSelectedIndex(nuevoIndex);
//
//            actualizarTitulosPestanas();
//            Notification.show("Venta descartada.", 3000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
//        }
//        enfocarBuscador();
//    }
//
//    private void confirmarEliminarTicket(TicketVenta ticket) {
//        Dialog confirmDialog = new Dialog();
//        confirmDialog.setHeaderTitle("Descartar Venta");
//        confirmDialog.add("¿Estás seguro de cancelar y eliminar " + ticket.getId() + "?");
//
//        Button cancelBtn = new Button("No, mantener", e -> confirmDialog.close());
//        Button yesBtn = new Button("Sí, eliminar [Enter]", e -> {
//            eliminarTicket(ticket);
//            confirmDialog.close();
//        });
//        yesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
//        yesBtn.addClickShortcut(Key.ENTER);
//
//        confirmDialog.getFooter().add(cancelBtn, yesBtn);
//        confirmDialog.open();
//        enfocarBuscador();
//    }
//
//    private void abrirDialogoVentaPorMonto(String producto, Double precioUnitario) {
//        new DialogoVentaPorMonto(producto, precioUnitario, (monto, cantidadCalculada) -> {
//            if (ticketActivo != null) {
//                // Lógica para procesar la venta agregando la cantidad calculada
//            }
//        }).open();
//    }
//
//    private void abrirDialogoEditarCantidad(TicketVenta ticket, DetalleFacturaDeVenta item) {
//        new DialogoEditarCantidad(
//                ticket,
//                item,
//                nuevaCantidad -> enfocarBuscador(),
//                () -> confirmarEliminarItem(ticket, item)
//        ).open();
//    }
//
//    private void confirmarEliminarItem(TicketVenta ticket, DetalleFacturaDeVenta item) {
//        new DialogoConfirmarEliminarItem(ticket, item, this::enfocarBuscador).open();
//    }
//
//    private void abrirDialogoRenombrarTicket(TicketVenta ticket, Span ticketNameSpan) {
//        new DialogoRenombrarTicket(ticket, ticketNameSpan, nombreLimpio -> {
//            actualizarTitulosPestanas();
//            enfocarBuscador();
//        }).open();
//    }
//
//    private void abrirDialogoMovimientoPos() {
//        new DialogoMovimientoPos(this.cajaService, "Administrador").open();
//    }
//
//    private void abrirDialogoCierreCaja() {
//        new DialogoCierreCaja(this.cajaService, this::enfocarBuscador).open();
//    }
//
//    private void abrirDialogoSeleccionCliente(TicketVenta ticket) {
//        if (ticket.getItems().isEmpty()) {
//
//            ClaseUtil.mostrarNotificacion("La : [" + ticket.getId() + "] está vacía", NotificationVariant.LUMO_ERROR);
////            Notification.show("El ticket [" + ticket.getId() + "] está vacío", 3000, Notification.Position.MIDDLE)
////                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//
//            return;
//        }
//
//        libretaClientes.clear();
//        libretaClientes.addAll(this.clienteService.getLista());
//
//        dialogoActivo = new DialogoSeleccionCliente(
//                ticket,
//                libretaClientes,
//                this.deliveryService.getLista(),
//                (clienteSeleccionado, esDelivery, motorista, direccion, telefono) -> {
//                    if (esDelivery) {
//                        ticket.setDelivery(motorista);
//                        ticket.setEstadoFactura(this.estadoFacturaService.getEstadoFactura(1));
//                    } else {
//                        ticket.setEstadoFactura(this.estadoFacturaService.getEstadoFactura(2));
//                    }
//
//                    ticket.setCliente(clienteSeleccionado);
//                    ticket.setNombreCliente(ticket.getCliente().getNombre());
//                    ticket.setDireccion(ticket.getCliente().getDireccion());
//                    ticket.setTipoVenta(this.tipoVentaService.getTipoVenta(2));
//
//                    FacturaDeVenta f = guardar(ticket);
//
//                    // IMPORTANTE: Primero cerramos el diálogo explícitamente si no se cierra solo
//                    if (dialogoActivo != null) {
//                        dialogoActivo.close();
//                    }
//                    cerrarTicketActual();
//                    enfocarBuscador();
//
//                    if (f != null) {
//                        imprimir(f.getCodigo());
//                    }
//
//                }
//        );
//
//        dialogoActivo.open();
//    }
//
//    private void mostrarPosConTransicion() {
//        getUI().ifPresent(ui -> ui.access(() -> {
//            // Opacidad inicial, escala reducida y configuración de duración (0.8 segundos)
//            getElement().getStyle().set("opacity", "0");
//            getElement().getStyle().set("transform", "scale(0.96)");
//            getElement().getStyle().set("transition", "opacity 0.8s cubic-bezier(0.25, 1, 0.5, 1), transform 0.8s cubic-bezier(0.25, 1, 0.5, 1)");
//
//            setVisible(true);
//
//            // Dispara la animación con un leve retraso para asegurar el renderizado
//            ui.getPage().executeJs("setTimeout(() => { $0.style.opacity = '1'; $0.style.transform = 'scale(1)'; }, 80);", getElement());
//
//            enfocarBuscador();
//        }));
//    }
//
//    @Override
//    public void beforeLeave(BeforeLeaveEvent event) {
//        if (listaTicketsAbiertos.stream().anyMatch(t -> !t.getItems().isEmpty())) {
//            // Lógica opcional para postergar la salida
//        }
//    }
//
//    private void restaurarFocoArticulos() {
//        if (UI.getCurrent() != null && searchBox != null) {
//            UI.getCurrent().access(() -> {
//                searchBox.focus();
//                // Esto limpia el buscador para que quede listo para el siguiente código de barra
//                searchBox.clear();
//            });
//        }
//    }
//
//}
