/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.venta.FacturaDeVentaService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PanelProductos extends VerticalLayout {

    /**
     * Cuántas tarjetas se dibujan por lote en el catálogo rápido. El resto se
     * encuentra con el buscador o con "Mostrar más".
     */
    private static final int TARJETAS_POR_LOTE = 60;

    /**
     * Imagen por defecto de las tarjetas. Conviene reemplazarla por un recurso
     * local (ej. "images/producto.png") para no depender de internet.
     */
    private static final String IMAGEN_POR_DEFECTO
            = "https://images.unsplash.com/photo-1528825871115-3581a5387919?w=300&auto=format&fit=crop&q=60";

    private static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("#,##0.00");

    private ComboBox<ArticuloEmpaque> searchBox;
    FacturaDeVentaService facturaDeVentaService;

    // Catálogo (se carga una sola vez desde el servicio)
    private final List<ArticuloEmpaque> catalogo;
    private final Div gridContainer = new Div();
    private final Button btnMostrarMas = new Button();
    private int tarjetasCargadas = 0;

    private final BiConsumer<ArticuloEmpaque, BigDecimal> onAgregarCantidadAlTicketActivo;
    private final Consumer<ArticuloEmpaque> onAbrirVentaPorMonto;

    public PanelProductos(
            ArticuloEmpaqueService articuloServicel,
            Consumer<ArticuloEmpaque> onAgregarAlTicketActivo,
            BiConsumer<ArticuloEmpaque, BigDecimal> onAgregarCantidadAlTicketActivo,
            Consumer<ArticuloEmpaque> onAbrirVentaPorMonto,
            Runnable onAbrirAperturaCaja,
            Runnable onAbrirCierreCaja,
            Runnable onAbrirMovimientoPos,
            FacturaDeVentaService facturaDeVentaService
    ) {
        setSpacing(true);

        this.facturaDeVentaService = facturaDeVentaService;
        this.onAgregarCantidadAlTicketActivo = onAgregarCantidadAlTicketActivo;
        this.onAbrirVentaPorMonto = onAbrirVentaPorMonto;

        // Una sola consulta al servicio para el buscador y para el catálogo
        this.catalogo = articuloServicel.getArticuloEmpaqueParaVenta();

        // Título compacto: no compite con el buscador y el catálogo por espacio
        H2 title = new H2("Colmado - Punto de Venta");
        title.getStyle().set("margin", "0").set("font-size", "1.25rem");

        searchBox = crearBuscador();

        // Botones de Gestión de Caja
        Button btnCerrarCaja = new Button("Cerrar Caja", VaadinIcon.LOCK.create(), e -> {
            if (onAbrirCierreCaja != null) {
                onAbrirCierreCaja.run();
            }
        });
        btnCerrarCaja.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        Button btnAbrirCaja = new Button("Abrir Caja", VaadinIcon.UNLOCK.create(), e -> {
            if (onAbrirAperturaCaja != null) {
                onAbrirAperturaCaja.run();
            }
        });
        btnAbrirCaja.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);

        Button btnMovimientoPos = new Button("Movimiento POS", VaadinIcon.CREDIT_CARD.create(), e -> {
            try {
                if (onAbrirMovimientoPos != null) {
                    onAbrirMovimientoPos.run();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnMovimientoPos.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button btnDevolucion = new Button("Devolución", new Icon(VaadinIcon.ABACUS));
        btnDevolucion.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);

        // Evento de clic
        btnDevolucion.addClickListener(e -> abrirModalBuscarFactura());

        HorizontalLayout cajaButtonsLayout = new HorizontalLayout(btnAbrirCaja, btnMovimientoPos, btnCerrarCaja, btnDevolucion);
        cajaButtonsLayout.setSpacing(true);
        // Los botones pasan a otra línea en pantallas angostas
        cajaButtonsLayout.getStyle().set("flex-wrap", "wrap");

        searchBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                if (onAgregarAlTicketActivo != null) {
                    onAgregarAlTicketActivo.accept(e.getValue());
                }
                searchBox.clear();
                // Listo para el siguiente producto / lectura del escáner
                searchBox.focus();
            }
        });

        H3 quickTitle = new H3("Ventas Rápidas (Catálogo)");
        quickTitle.getStyle().set("margin", "0");

        // Catálogo en cuadrícula
        gridContainer.getStyle().set("display", "grid");
        gridContainer.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(150px, 1fr))");
        gridContainer.getStyle().set("gap", "1rem");
        gridContainer.setWidthFull();

        btnMostrarMas.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnMostrarMas.addClickListener(e -> cargarMasTarjetas());

        cargarMasTarjetas();

        // El catálogo tiene su propio scroll: título, botones y buscador quedan
        // siempre visibles mientras se recorre el catálogo.
        VerticalLayout contenidoCatalogo = new VerticalLayout(gridContainer, btnMostrarMas);
        contenidoCatalogo.setPadding(false);
        contenidoCatalogo.setSpacing(true);
        contenidoCatalogo.setAlignItems(FlexComponent.Alignment.CENTER);

        Div scrollCatalogo = new Div(contenidoCatalogo);
        scrollCatalogo.setWidthFull();
        scrollCatalogo.getStyle().set("overflow-y", "auto").set("min-height", "0");

        add(title, cajaButtonsLayout, searchBox, quickTitle, scrollCatalogo);
        expand(scrollCatalogo);
    }

    // ============================================================
    // BUSCADOR
    // ============================================================
    private ComboBox<ArticuloEmpaque> crearBuscador() {

        ComboBox<ArticuloEmpaque> combo = new ComboBox<>("Buscar producto o escanear código");

        // Cada opción muestra producto, presentación, factor y precio: así se
        // distinguen las presentaciones del mismo artículo.
        combo.setItemLabelGenerator(this::etiquetaEmpaque);

        // Filtro en memoria: todas las palabras escritas deben aparecer en el
        // nombre, la presentación o el código.
        combo.setItems((empaque, texto) -> coincide(empaque, texto), catalogo);

        combo.setPlaceholder("Escriba nombre o código, o escanee...");
        combo.setWidthFull();
        combo.setClearButtonVisible(true);
        combo.setAutofocus(true);
        combo.addFocusShortcut(Key.F4);

        return combo;
    }

    private String etiquetaEmpaque(ArticuloEmpaque ae) {

        StringBuilder sb = new StringBuilder();
        sb.append(ae.getNombreArticulo() != null ? ae.getNombreArticulo() : "Artículo");

        if (ae.getUnidadEmpaque() != null && ae.getUnidadEmpaque().getDescripcion() != null) {
            sb.append(" · ").append(ae.getUnidadEmpaque().getDescripcion());
        }

        BigDecimal factor = ae.getFactorConversion();
        if (factor != null && factor.compareTo(BigDecimal.ONE) != 0) {
            sb.append(" (x").append(factor.stripTrailingZeros().toPlainString()).append(")");
        }

        if (ae.getPrecioVenta() != null) {
            sb.append(" — RD$ ").append(FORMATO_PRECIO.format(ae.getPrecioVenta()));
        }

        return sb.toString();
    }

    private boolean coincide(ArticuloEmpaque ae, String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            return true;
        }

        StringBuilder pajar = new StringBuilder(etiquetaEmpaque(ae).toLowerCase());
        if (ae.getCodigo() != null) {
            pajar.append(' ').append(ae.getCodigo());
        }
        if (ae.getArticulo() != null && ae.getArticulo().getCodigo() != null) {
            pajar.append(' ').append(ae.getArticulo().getCodigo());
        }

        String contenido = pajar.toString();
        for (String palabra : texto.toLowerCase().trim().split("\\s+")) {
            if (!contenido.contains(palabra)) {
                return false;
            }
        }
        return true;
    }

    // ============================================================
    // CATÁLOGO RÁPIDO
    // ============================================================
    private void cargarMasTarjetas() {

        int hasta = Math.min(tarjetasCargadas + TARJETAS_POR_LOTE, catalogo.size());

        for (int i = tarjetasCargadas; i < hasta; i++) {
            gridContainer.add(crearTarjeta(catalogo.get(i)));
        }

        tarjetasCargadas = hasta;

        int restantes = catalogo.size() - tarjetasCargadas;
        btnMostrarMas.setVisible(restantes > 0);
        btnMostrarMas.setText("Mostrar más (" + restantes + " restantes)");
    }

    private TarjetaProductoCompo crearTarjeta(ArticuloEmpaque art) {

        return new TarjetaProductoCompo(
                art,
                IMAGEN_POR_DEFECTO,
                (articulo, cantidad) -> {
                    if (onAgregarCantidadAlTicketActivo != null) {
                        onAgregarCantidadAlTicketActivo.accept(articulo, cantidad);
                    }
                },
                (articulo) -> {
                    if (onAbrirVentaPorMonto != null) {
                        onAbrirVentaPorMonto.accept(articulo);
                    }
                }
        );
    }

    // Método para permitir enfocar el buscador externamente si es necesario
    public ComboBox<ArticuloEmpaque> getSearchBox() {
        return searchBox;
    }

    // ============================================================
    // DEVOLUCIÓN
    // ============================================================
    private void abrirModalBuscarFactura() {

        Dialog dialogBuscar = new Dialog();
        dialogBuscar.setHeaderTitle("Buscar Factura para Devolución");
        dialogBuscar.setWidth("600px");

        TextField txtCodigo = new TextField("Número de Factura");
        txtCodigo.setPlaceholder("Ej: 1045");
        txtCodigo.setWidthFull();
        txtCodigo.setAutofocus(true);

        Button btnBuscar = new Button("Buscar", new Icon(VaadinIcon.SEARCH));
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickShortcut(Key.ENTER);

        HorizontalLayout searchLayout = new HorizontalLayout(txtCodigo, btnBuscar);
        searchLayout.setAlignItems(FlexComponent.Alignment.END);
        searchLayout.setWidthFull();

        btnBuscar.addClickListener(e -> {
            String codigo = txtCodigo.getValue();
            if (codigo == null || codigo.trim().isEmpty()) {
                Notification.show("Ingrese un número de factura válido", 3000, Notification.Position.MIDDLE);
                return;
            }

            // El código de factura es numérico: evita la excepción con texto
            int codigoFactura;
            try {
                codigoFactura = Integer.parseInt(codigo.trim());
            } catch (NumberFormatException ex) {
                Notification.show("El número de factura debe ser numérico", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            // Buscar factura en la base de datos
            FacturaDeVenta factura = this.facturaDeVentaService.getFactura(codigoFactura);

            if (factura != null) {

                if (Boolean.TRUE.equals(factura.getAnulada())) {
                    Notification.show("La factura ya está totalmente anulada/devuelta.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                dialogBuscar.close();

                // Abrir el modal de selección de ítems (Modal 2)
                DialogoDevolucionVenta dialogoDev = new DialogoDevolucionVenta(
                        factura,
                        this.facturaDeVentaService,
                        "ADMIN", // Pasar usuario actual
                        () -> Notification.show("Devolución completada con éxito", 3000, Notification.Position.TOP_CENTER)
                );
                dialogoDev.open();

            } else {
                Notification.show("No se encontró ninguna factura con el código: " + codigo, 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        VerticalLayout content = new VerticalLayout(searchLayout);
        dialogBuscar.add(content);

        Button btnCerrar = new Button("Cancelar", e -> dialogBuscar.close());
        dialogBuscar.getFooter().add(btnCerrar);

        dialogBuscar.open();
    }
}
