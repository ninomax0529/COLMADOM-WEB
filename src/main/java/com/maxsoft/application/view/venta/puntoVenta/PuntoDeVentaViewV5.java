///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.maxsoft.application.view.venta.puntoVenta;
//
///**
// *
// * @author maximilianoalmonte
// */
//import com.maxsoft.application.modelo.Articulo;
//import com.maxsoft.application.modelo.Cliente;
//import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
//import com.maxsoft.application.modelo.FacturaDeVenta;
//import com.maxsoft.application.reporte.venta.RptFaturaVenta;
//import com.maxsoft.application.servicio.interfaces.ArticuloService;
//import com.maxsoft.application.servicio.interfaces.CajaService;
//import com.maxsoft.application.servicio.interfaces.ClienteService;
//import com.maxsoft.application.servicio.interfaces.DeliveryService;
//import com.maxsoft.application.servicio.interfaces.EstadoFacturaService;
//import com.maxsoft.application.servicio.interfaces.FacturaDeVentaService;
//import com.maxsoft.application.servicio.interfaces.TipoVentaService;
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
//import com.vaadin.flow.component.Key;
//import com.vaadin.flow.component.KeyModifier;
//import com.vaadin.flow.component.Shortcuts;
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.html.Anchor;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.tabs.TabSheet;
//import com.vaadin.flow.router.BeforeLeaveEvent;
//import com.vaadin.flow.router.BeforeLeaveObserver;
//import com.vaadin.flow.router.Menu;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.server.StreamResource;
//import java.sql.Connection;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.List;
//import javax.sql.DataSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.vaadin.lineawesome.LineAwesomeIconUrl;
//
////@PageTitle("Punto de Venta V5")
////@Route(value = "puntoDeVentav5")
////@Menu(order = 4, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
//public class PuntoDeVentaViewV5 extends HorizontalLayout implements BeforeLeaveObserver {
//
//    @Autowired
//    DataSource dataSource;
//    @Autowired
//    private CajaService cajaService; // Inyectado por Spring en la vista
//
//    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");
//
//    Boolean cambiosSinGuardar = false;
//
//    // =========================================================================
//    // ESTADO Y COMPONENTES PRINCIPALES
//    // =========================================================================
//    private final TabSheet ticketTabSheet = new TabSheet();
//    private final List<TicketVenta> listaTicketsAbiertos = new ArrayList<>();
//    private TicketVenta ticketAktivo;
//    private int contadorSecuencialTickets = 0;
//    private final ComboBox<Articulo> searchBox = new ComboBox<>("Buscar producto o escanear código");
//
//    ArticuloService articuloServicel;
//    ClienteService clienteService;
//    DeliveryService deliveryService;
//    TipoVentaService tipoVentaService;
//    EstadoFacturaService estadoFacturaService;
//
//    private final List<Cliente> libretaClientes = new ArrayList<>();
//
//    private FacturaDeVenta factura;
//    FacturaDeVentaService factService;
//    int i = 0;
//
//    @Autowired
//    public PuntoDeVentaViewV5(FacturaDeVentaService factServiceArg,
//            ArticuloService articuloServiceArg, ClienteService clienteService,
//            DeliveryService deliveryService,
//            TipoVentaService tipoVentaService,
//            EstadoFacturaService estadoFacturaService,
//            CajaService cajaService
//    ) {
//
//        this.articuloServicel = articuloServiceArg;
//        this.clienteService = clienteService;
//        this.factService = factServiceArg;
//        this.deliveryService = deliveryService;
//        this.estadoFacturaService = estadoFacturaService;
//        this.tipoVentaService = tipoVentaService;
//        this.cajaService = cajaService;
//        setSizeFull();
//        setSpacing(true);
//
//        i = 1;
//
//        crearNuevoTicket(null);
//        searchBox.setItemLabelGenerator(it -> it.getDescripcion());
//        VerticalLayout leftPanel = crearPanelProductos();
//        leftPanel.setWidth("55%");
//        leftPanel.setHeightFull();
//
//        VerticalLayout rightPanel = crearPanelCarritos();
//        rightPanel.setWidth("45%");
//        rightPanel.setHeightFull();
//
//        configurarAtajosTecladoNumerico();
//        configurarAtajosRenombrar();
//        abrirDialogoAperturaCaja();
//
//        // Agrega este atajo en tu constructor para abrir abonos con Alt + A:
//        UI.getCurrent().addShortcutListener(() -> {
//
//            abrirDialogoAbonoLibreta();
//
//        },
//                Key.KEY_A, KeyModifier.ALT
//        );
//
//        add(leftPanel, rightPanel);
//
//    }
//
//    private void abrirDialogoAbonoLibreta() {
//        DialogoAbonoLibreta dialogoAbonoLibreta = new DialogoAbonoLibreta(this.libretaClientes, this::enfocarBuscador);
//    }
//
//    // =========================================================================
//    // ATAJOS DE TECLADO GLOBAL
//    // =========================================================================
//    private void configurarAtajosTecladoNumerico() {
//
//        Key[] digitos = new Key[]{
//            Key.DIGIT_1, Key.DIGIT_2, Key.DIGIT_3,
//            Key.DIGIT_4, Key.DIGIT_5, Key.DIGIT_6,
//            Key.DIGIT_7, Key.DIGIT_8, Key.DIGIT_9
//        };
//
//        Key[] numpadDigitos = new Key[]{
//            Key.NUMPAD_1, Key.NUMPAD_2, Key.NUMPAD_3,
//            Key.NUMPAD_4, Key.NUMPAD_5, Key.NUMPAD_6,
//            Key.NUMPAD_7, Key.NUMPAD_8, Key.NUMPAD_9
//        };
//
//        for (int j = 0; j < 9; j++) {
//            final int index = j;
//
//            UI.getCurrent().addShortcutListener(
//                    () -> seleccionarTicketPorPosicion(index),
//                    digitos[j], KeyModifier.ALT
//            );
//
//            UI.getCurrent().addShortcutListener(
//                    () -> seleccionarTicketPorPosicion(index),
//                    numpadDigitos[j], KeyModifier.ALT
//            );
//        }
//    }
//
//    private void seleccionarTicketPorPosicion(int index) {
//        if (index >= 0 && index < listaTicketsAbiertos.size()) {
//            ticketTabSheet.setSelectedIndex(index);
//            Notification.show("Cambiado a " + listaTicketsAbiertos.get(index).getId(), 1500, Notification.Position.TOP_CENTER);
//            enfocarBuscador();
//        }
//    }
//
//    private void configurarAtajosRenombrar() {
//        UI.getCurrent().addShortcutListener(
//                () -> {
//                    if (ticketAktivo != null) {
//                        abrirDialogoRenombrarTicket(ticketAktivo, new Span(ticketAktivo.getId()));
//                    }
//                },
//                Key.KEY_R, KeyModifier.ALT
//        );
//    }
//
//    // =========================================================================
//    // UI - PANELES
//    // =========================================================================
//    private VerticalLayout crearPanelProductos() {
//
//        return new PanelProductos(
//                this.articuloServicel,
//                this::agregarAlTicketActivo,
//                this::agregarAlTicketActivo,
//                this::abrirDialogoVentaPorMonto,
//                this::abrirDialogoAperturaCaja,
//                this::abrirDialogoCierreCaja,
//                this::abrirDialogoMovimientoPos
//        );
//    }
//
//    private VerticalLayout crearPanelCarritos() {
//
//        return new PanelCarritos(
//                ticketTabSheet,
//                listaTicketsAbiertos,
//                () -> crearNuevoTicket(null),
//                ticketSeleccionado -> {
//                    this.ticketAktivo = ticketSeleccionado;
//                    enfocarBuscador();
//                }
//        );
//    }
//    // =========================================================================
//    // DIÁLOGOS DE CONTROL DE CANTIDAD Y PESO (PASO 1)
//    // =========================================================================
//
//    private void abrirDialogoVentaPorMonto(String producto, Double precioUnitario) {
//
//        DialogoVentaPorMonto dialogo = new DialogoVentaPorMonto(
//                producto,
//                precioUnitario,
//                (monto, cantidadCalculada) -> {
//                    // Aquí invocas tu método o callback para agregar al ticket si lo deseas
//                    // p. ej. agregarAlTicketActivo(producto, cantidadCalculada, monto);
//                }
//        );
//        dialogo.open();
//    }
//
//    private void agregarAlTicketActivo(Articulo articulo) {
//
//        if (ticketAktivo == null) {
//            System.out.println("Tick es nulo");
//            return;
//        }
//
//        System.out.println("Tick no es  nulo");
//        DetalleFacturaDeVenta det1 = new DetalleFacturaDeVenta();
//
//        det1.setCodigo(articulo.getCodigo());//Colocarlo anull cuando le asignemo el encabezada
//
//        det1.setArticulo(articulo);
//        det1.setNumeroDeLinea(i++);
//
//        det1.setDescripcionArticulo(articulo.getDescripcion());
//
//        det1.setCantidad(1.00);
//        det1.setExistenciaActual(articulo.getExistencia());
//
//        det1.setPrecioVenta(articulo.getPrecioVenta());
//
//        det1.setSubTotal(subTotal(det1.getCantidad(), det1.getPrecioVenta()));
//        det1.setPorcientoDescuento(10.00);
//        det1.setPorcientoItbis(18.00);
//
//        det1.setTotalDescuento(totalDescuento(det1.getSubTotal(), det1.getPorcientoDescuento()));
//
//        det1.setTotalItbis(totalItbis(det1.getSubTotal(), det1.getTotalDescuento(), det1.getPorcientoItbis()));
//
//        det1.setTotal(total(det1.getSubTotal(), det1.getTotalDescuento(), det1.getTotalItbis()));
//
//        det1.setNombreAlmacen("General");
//        det1.setNombreUnidad("Unidad");
//
//        for (DetalleFacturaDeVenta item : ticketAktivo.getItems()) {
//
//            if (item.getArticulo().equals(articulo)) {
//
//                double nuevaCantidad = item.getCantidad() + 1;
//                if (nuevaCantidad <= 0) {
//                    confirmarEliminarItem(ticketAktivo, item);
//                } else {
//                    item.setCantidad(nuevaCantidad);
//                    item.setSubTotal(subTotal(nuevaCantidad, item.getPrecioVenta()));
//                    ticketAktivo.updateUI();
//                }
//                enfocarBuscador();
//                return;
//            }
//        }
//
//        if (1 > 0) {
//            ticketAktivo.getItems().add(det1);
//            ticketAktivo.updateUI();
//        }
//
//        enfocarBuscador();
//    }
//
//    private void agregarAlTicketActivo(Articulo articulo, Double cant) {
//
//        if (ticketAktivo == null) {
//            System.out.println("Tick es nulo");
//            return;
//        }
//
//        System.out.println("Tick no es  nulo");
//        DetalleFacturaDeVenta det1 = new DetalleFacturaDeVenta();
//
//        det1.setCodigo(articulo.getCodigo());//Colocarlo anull cuando le asignemo el encabezada
//
//        det1.setArticulo(articulo);
//        det1.setNumeroDeLinea(i++);
//
//        det1.setDescripcionArticulo(articulo.getDescripcion());
//
//        det1.setCantidad(1.00);
//        det1.setExistenciaActual(articulo.getExistencia());
//
//        det1.setPrecioVenta(articulo.getPrecioVenta());
//
//        det1.setSubTotal(subTotal(det1.getCantidad(), det1.getPrecioVenta()));
//        det1.setPorcientoDescuento(10.00);
//        det1.setPorcientoItbis(18.00);
//
//        det1.setTotalDescuento(totalDescuento(det1.getSubTotal(), det1.getPorcientoDescuento()));
//
//        det1.setTotalItbis(totalItbis(det1.getSubTotal(), det1.getTotalDescuento(), det1.getPorcientoItbis()));
//
//        det1.setTotal(total(det1.getSubTotal(), det1.getTotalDescuento(), det1.getTotalItbis()));
//
//        det1.setNombreAlmacen("General");
//        det1.setNombreUnidad("Unidad");
//
//        for (DetalleFacturaDeVenta item : ticketAktivo.getItems()) {
//
//            if (item.getArticulo().equals(articulo)) {
//
//                double nuevaCantidad = item.getCantidad() + cant;
//                if (nuevaCantidad <= 0) {
//                    confirmarEliminarItem(ticketAktivo, item);
//                } else {
//                    item.setCantidad(nuevaCantidad);
//                    item.setSubTotal(subTotal(nuevaCantidad, item.getPrecioVenta()));
//                    ticketAktivo.updateUI();
//                }
//                enfocarBuscador();
//                return;
//            }
//        }
//
//        if (1 > 0) {
//            ticketAktivo.getItems().add(det1);
//            ticketAktivo.updateUI();
//        }
//
//        enfocarBuscador();
//    }
//
//    private void enfocarBuscador() {
//        if (searchBox != null) {
//            searchBox.focus();
//        }
//    }
//
//    // =========================================================================
//    // GESTIÓN DE TICKETS
//    // =========================================================================
//    private void crearNuevoTicket(String nombrePersonalizado) {
//
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
//
//        gridDet.setSelectionMode(Grid.SelectionMode.SINGLE);
//
//        gridDet.addColumn(DetalleFacturaDeVenta::getArticulo).setHeader("Producto").setAutoWidth(true);
//        gridDet.addColumn(DetalleFacturaDeVenta::getCantidad).setHeader("Cant.");
////        
//        gridDet.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getPrecioVenta())).setHeader("Precio");
//        gridDet.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getSubTotal())).setHeader("Total");
//
//        // Columna de acciones rápidas (+ / - / Editar Cantidad / Eliminar)
//        gridDet.addComponentColumn(item -> {
//
//            HorizontalLayout actionsBnt = new HorizontalLayout();
//            actionsBnt.setSpacing(false);
//
//            Button editQtyBtn = new Button(VaadinIcon.EDIT.create(), e -> abrirDialogoEditarCantidad(nuevoTicket, item));
//            editQtyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
//            editQtyBtn.setTooltipText("Cambiar cantidad / libras [*]");
//
//            Button removeBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmarEliminarItem(nuevoTicket, item));
//            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
//
//            actionsBnt.add(editQtyBtn, removeBtn);
//            return actionsBnt;
//        }).setHeader("");
//
//        // Doble clic en la fila para editar la cantidad rápidamente
//        gridDet.addItemDoubleClickListener(e -> abrirDialogoEditarCantidad(nuevoTicket, e.getItem()));
//
//        // Tecla Supr / Delete para borrar la fila seleccionada
//        Shortcuts.addShortcutListener(gridDet, () -> {
//            DetalleFacturaDeVenta seleccionado = gridDet.asSingleSelect().getValue();
//
//            if (seleccionado != null) {
//
//                confirmarEliminarItem(nuevoTicket, seleccionado);
//            }
//
//        }, Key.DELETE);
//// ATAJO CON TECLA ASTERISCO (*): Editar cantidad con la tecla '*' o del teclado numérico
//        Shortcuts.addShortcutListener(gridDet, () -> {
//            DetalleFacturaDeVenta seleccionado = gridDet.asSingleSelect().getValue();
//            if (seleccionado != null) {
//                abrirDialogoEditarCantidad(nuevoTicket, seleccionado);
//            }
//        }, Key.NUMPAD_MULTIPLY);
//        // Selección automática al desplazarse con las flechas (Arriba / Abajo)
//        gridDet.addCellFocusListener(e -> {
//            e.getItem().ifPresent(item -> gridDet.select(item));
//        });
//
//        gridDet.setHeightFull();
//
//        VerticalLayout contenidoTab = construirContenidoPanelTicket(nuevoTicket);
//
//        ticketTabSheet.add(nombreFinal, contenidoTab);
//        ticketTabSheet.setSelectedTab(ticketTabSheet.getTab(contenidoTab));
//        ticketAktivo = nuevoTicket;
//
//        actualizarTitulosPestanas();
//        enfocarBuscador();
//    }
//
//    private void actualizarTitulosPestanas() {
//
//        for (int j = 0; j < listaTicketsAbiertos.size(); j++) {
//            TicketVenta ticket = listaTicketsAbiertos.get(j);
//            int atajoNum = j + 1;
//            String etiqueta = "[Alt+" + atajoNum + "] " + ticket.getId();
//            ticketTabSheet.getTabAt(j).setLabel(etiqueta);
//        }
//    }
//
//    private VerticalLayout construirContenidoPanelTicket(TicketVenta ticket) {
//
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
//            Notification.show("El ticket [" + ticket.getId() + "] está vacío", 3000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
//                    // Aquí guardas y obtienes la factura creada
//                    FacturaDeVenta f = guardar(ticketAGuardar);
//                    if (f != null) {
//                        imprimir(f.getCodigo());
//                    }
//                },
//                this::cerrarTicketActual,
//                this::enfocarBuscador
//        );
//
//        dialogo.open();
//    }
//
//    // Método auxiliar privado para mantener limpio el cálculo de la devuelta
//    private void abrirDialogoAperturaCaja() {
//
//        DialogoAperturaCaja dialogo = new DialogoAperturaCaja(cajaService, fondo -> {
//            // Lógica tras apertura exitosa
//        });
//        dialogo.open();
//
//    }
//
//    private void cerrarTicketActual() {
//        eliminarTicket(ticketAktivo);
//    }
//
//    private void eliminarTicket(TicketVenta ticket) {
//        if (listaTicketsAbiertos.size() <= 1) {
//            ticket.getItems().clear();
//            ticket.updateUI();
//            Notification.show("Ticket limpiado.", 3000, Notification.Position.MIDDLE);
//            enfocarBuscador();
//            return;
//        }
//
//        int index = listaTicketsAbiertos.indexOf(ticket);
//        if (index >= 0) {
//            listaTicketsAbiertos.remove(ticket);
//            ticketTabSheet.remove(ticketTabSheet.getTabAt(index));
//            actualizarTitulosPestanas();
//
//            Notification.show("Venta descartada.", 3000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
//        }
//        enfocarBuscador();
//    }
//
//    private void confirmarEliminarTicket(TicketVenta ticket) {
//        
//        Dialog confirmDialog = new Dialog();
//        confirmDialog.setHeaderTitle("Descartar Venta");
//        confirmDialog.add("¿Estás seguro de cancelar y eliminar " + ticket.getId() + "?");
//
//        Button cancelBtn = new Button("No, mantener", e -> confirmDialog.close());
//
//        Button yesBtn = new Button("Sí, eliminar [Enter]", e -> {
//            eliminarTicket(ticket);
//            confirmDialog.close();
//        });
//        yesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
//        yesBtn.addClickShortcut(Key.ENTER);
//
//        confirmDialog.getFooter().add(cancelBtn, yesBtn);
//        confirmDialog.open();
//    }
//
//    private void abrirDialogoSeleccionCliente(TicketVenta ticket) {
//
//        if (ticket.getItems().isEmpty()) {
//            Notification.show("El ticket [" + ticket.getId() + "] está vacío", 3000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            return;
//        }
//
//        libretaClientes.clear();
//        libretaClientes.addAll(this.clienteService.getLista());
//
//        DialogoSeleccionCliente dialogo = new DialogoSeleccionCliente(
//                ticket,
//                libretaClientes,
//                this.deliveryService.getLista(),
//                (clienteSeleccionado, esDelivery, motorista, direccion, telefono) -> {
//
//                    if (esDelivery) {
//                        ticket.setDelivery(motorista);
//                        ticket.setEstadoFactura(this.estadoFacturaService.getEstadoFactura(1)); // Abierta
//                    } else {
//                        ticket.setEstadoFactura(this.estadoFacturaService.getEstadoFactura(2)); // Cerrada
//                    }
//
//                    ticket.setCliente(clienteSeleccionado);
//                    ticket.setNombreCliente(ticket.getCliente().getNombre());
//                    ticket.setDireccion(ticket.getCliente().getDireccion());
//
//                    ticket.setTipoVenta(this.tipoVentaService.getTipoVenta(2)); // Credito
//
//                    factura = guardar(ticket);
//                    cerrarTicketActual();
//                    enfocarBuscador();
//
//                    imprimir(factura.getCodigo());
//                }
//        );
//
//        dialogo.open();
//    }
//
//    @Override
//    public void beforeLeave(BeforeLeaveEvent event) {
//        if (cambiosSinGuardar) {
//            event.postpone();
//        }
//    }
//
//    private FacturaDeVenta guardar(TicketVenta ticketVenta) {
//
//        try {
//
//            String usuarioActual = "Administrador"; // Obtener de la sesión
//            FacturaDeVenta facturaGuardada = factService.procesarVenta(ticketVenta, usuarioActual);
//
//            Notification.show("Factura guardada correctamente", 3000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//
//            return facturaGuardada;
//
//        } catch (IllegalStateException | IllegalArgumentException ex) {
//            // Errores esperados de validación de negocio
//            Notification.show(ex.getMessage(), 3500, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
//            return null;
//
//        } catch (Exception ex) {
//            // Errores no controlados o de base de datos
//            Notification.show("Error procesando la factura: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            return null;
//        }
//    }
//
//    private Double subTotal(Double cant, Double precio) {
//
//        Double sutTotal = cant * precio;
//
//        return ClaseUtil.FormatearDouble(sutTotal, 2);
//
//    }
//
//    private Double total(Double subTotal, Double totalDesc, Double totalItbis) {
//
//        Double total = (subTotal - totalDesc) + totalItbis;
//
//        System.out.println("Total " + subTotal + " " + totalDesc + " " + totalItbis);
//        return ClaseUtil.FormatearDouble(total, 2);
//    }
//
//    private Double totalItbis(Double subTotal, Double descuento, Double itbis) {
//
//        itbis = itbis / 100.00;//Converftir el itbis en tasa
//
//        Double totalItbis = (subTotal - descuento) * itbis;
//
//        return ClaseUtil.FormatearDouble(totalItbis, 2);
//    }
//
//    private Double totalDescuento(Double subTotal, Double desc) {
//
//        desc = desc / 100.00;//Converftir el itbis en tasa
//
//        Double totalDesc = subTotal * desc;
//
//        return ClaseUtil.FormatearDouble(totalDesc, 2);
//    }
//
//    private void imprimir(int factura) {
//
//        try (Connection conn = dataSource.getConnection()) {
//
//            RptFaturaVenta rptP = new RptFaturaVenta();
//
//            if (rptP != null) {
//
//                StreamResource pdfResource = rptP.rptFacturaVenta(factura, conn);
//
//                Anchor anchor = new Anchor(pdfResource, "");
//                anchor.getElement().setAttribute("download", false);
//                anchor.getElement().setAttribute("target", "_blank");
//
//                anchor.getElement().callJsFunction("click"); // dispara la apertura automática
//
//                add(anchor);
//
//                this.factura = null;
//            }
//
//        } catch (Exception ex) {
//            Notification.show("Error al generar el reporte", 3000, Notification.Position.TOP_CENTER);
//        }
//
//    }
//
//    private void abrirDialogoEditarCantidad(TicketVenta ticket, DetalleFacturaDeVenta item) {
//
//        DialogoEditarCantidad dialogo = new DialogoEditarCantidad(
//                ticket,
//                item,
//                nuevaCantidad -> {
//                    // Se ejecuta si la cantidad es mayor a 0
//                    enfocarBuscador();
//                },
//                () -> {
//                    // Se ejecuta si la cantidad ingresada es menor o igual a 0
//                    confirmarEliminarItem(ticket, item);
//                }
//        );
//        dialogo.open();
//    }
//
//    // =========================================================================
//    // OTROS DIÁLOGOS
//    // =========================================================================
//    private void confirmarEliminarItem(TicketVenta ticket, DetalleFacturaDeVenta item) {
//        DialogoConfirmarEliminarItem dialogoConfirmarEliminarItem = new DialogoConfirmarEliminarItem(ticket, item, this::enfocarBuscador);
//    }
//
//    private void abrirDialogoRenombrarTicket(TicketVenta ticket, Span ticketNameSpan) {
//        DialogoRenombrarTicket dialogo = new DialogoRenombrarTicket(
//                ticket,
//                ticketNameSpan,
//                nombreLimpio -> {
//                    actualizarTitulosPestanas();
//                    enfocarBuscador();
//                }
//        );
//        dialogo.open();
//    }
//
//    private void abrirDialogoMovimientoPos() {
//        new DialogoMovimientoPos(this.cajaService, "Administrador");
//    }
//
//    private void abrirDialogoCierreCaja() {
//
//        DialogoCierreCaja dialogo = new DialogoCierreCaja(() -> {
//            // Callback que se ejecuta cuando se cierra la caja con éxito
//            enfocarBuscador();
//        });
//        dialogo.open();
//    }
//}
