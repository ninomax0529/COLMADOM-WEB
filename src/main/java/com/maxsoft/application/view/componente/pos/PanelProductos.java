/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.maxsoft.application.modelo.Categoria;
import com.maxsoft.application.util.LineaPosUtil;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PanelProductos extends VerticalLayout {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PanelProductos.class);


    /**
     * Cuántas tarjetas se dibujan por lote en el catálogo rápido. El resto se
     * encuentra con el buscador o con "Mostrar más".
     */
    private static final int TARJETAS_POR_LOTE = 60;

    /**
     * Días de ventas que se miran para ordenar el catálogo: lo que más se
     * vendió en este período sale primero (en el catálogo y en el buscador).
     */
    private static final int DIAS_MAS_VENDIDOS = 30;

    /**
     * Imagen por defecto de las tarjetas. Conviene reemplazarla por un recurso
     * local (ej. "images/producto.png") para no depender de internet.
     */
    private static final String IMAGEN_POR_DEFECTO
            = "https://images.unsplash.com/photo-1528825871115-3581a5387919?w=300&auto=format&fit=crop&q=60";

    // DecimalFormat no es thread-safe: instancia propia, no estática compartida
    private final DecimalFormat FORMATO_PRECIO = new DecimalFormat("#,##0.00");

    private ComboBox<ArticuloEmpaque> searchBox;
    FacturaDeVentaService facturaDeVentaService;

    // Catálogo (se carga una sola vez desde el servicio)
    private final List<ArticuloEmpaque> catalogo;
    // Tarjetas que se muestran según la categoría elegida (al inicio, todo):
    // una por artículo, con todas sus presentaciones
    private List<List<ArticuloEmpaque>> catalogoFiltrado;
    private final HorizontalLayout barraCategorias = new HorizontalLayout();
    private final Div gridContainer = new Div();
    private final Button btnMostrarMas = new Button();
    private int tarjetasCargadas = 0;

    // Botones de caja: se muestran u ocultan según el estado de la caja
    // (ver actualizarEstadoCaja)
    private Button btnAbrirCaja;
    private Button btnCerrarCaja;
    private Button btnMovimientoPos;

    // "Caja abierta desde 8:05 AM · Admin · 37 ventas · RD$ 12,450.00"
    // como etiquetas de colores con ícono (ver mostrarTurnoAbierto)
    private final Div barraEstadoTurno = new Div();

    // Avisa a la vista que cambiaron las ventas (p. ej. una devolución) para
    // refrescar el estado del turno
    private Runnable onVentasCambiadas;

    // Existencia por artículo (unidad base) que muestran las tarjetas. Se
    // recarga después de cada venta, anulación o devolución.
    private final InventarioService inventarioService;
    private Map<Integer, BigDecimal> existencias;
    /** Mostrador frente a su mínimo y a los otros almacenes (aviso "en almacén" de la tarjeta). */
    private Map<Integer, InventarioService.EstadoMostrador> estadosMostrador = Map.of();

    private final BiConsumer<ArticuloEmpaque, BigDecimal> onAgregarCantidadAlTicketActivo;
    private final Consumer<ArticuloEmpaque> onAbrirVentaPorMonto;
    private final Consumer<ArticuloEmpaque> onAgregarAlTicketActivo;
    /** Para decir por qué un código escaneado no está en el catálogo del mostrador. */
    private final ArticuloEmpaqueService articuloEmpaqueService;

    public PanelProductos(
            ArticuloEmpaqueService articuloServicel,
            Consumer<ArticuloEmpaque> onAgregarAlTicketActivo,
            BiConsumer<ArticuloEmpaque, BigDecimal> onAgregarCantidadAlTicketActivo,
            Consumer<ArticuloEmpaque> onAbrirVentaPorMonto,
            Runnable onAbrirAperturaCaja,
            Runnable onAbrirCierreCaja,
            Runnable onAbrirMovimientoPos,
            FacturaDeVentaService facturaDeVentaService,
            List<Categoria> categorias,
            InventarioService inventarioService
    ) {
        setSpacing(true);

        this.inventarioService = inventarioService;
        this.existencias = inventarioService.existenciasPorArticulo();
        this.estadosMostrador = leerEstadosMostrador();

        this.facturaDeVentaService = facturaDeVentaService;
        this.onAgregarCantidadAlTicketActivo = onAgregarCantidadAlTicketActivo;
        this.onAbrirVentaPorMonto = onAbrirVentaPorMonto;
        this.onAgregarAlTicketActivo = onAgregarAlTicketActivo;
        this.articuloEmpaqueService = articuloServicel;

        // Una sola consulta al servicio para el buscador y para el catálogo.
        // Solo los artículos registrados en el almacén de venta (mostrador):
        // los que están solo en el almacén principal no se venden en el POS.
        this.catalogo = ordenarPorMasVendidos(
                articuloServicel.getArticuloEmpaqueParaVenta().stream()
                        .filter(ae -> ae.getArticulo() != null
                        && existencias.containsKey(ae.getArticulo().getCodigo()))
                        .toList(),
                articuloServicel.vecesVendidoUltimosDias(DIAS_MAS_VENDIDOS));
        this.catalogoFiltrado = agruparPorArticulo(catalogo);

        // Título compacto: no compite con el buscador y el catálogo por espacio
        H2 title = new H2("Colmado - Punto de Venta");
        title.getStyle().set("margin", "0").set("font-size", "1.25rem");

        searchBox = crearBuscador();

        // Botones de Gestión de Caja
        btnCerrarCaja = new Button("Cerrar Caja", VaadinIcon.LOCK.create(), e -> {
            if (onAbrirCierreCaja != null) {
                onAbrirCierreCaja.run();
            }
        });
        btnCerrarCaja.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        btnAbrirCaja = new Button("Abrir Caja", VaadinIcon.UNLOCK.create(), e -> {
            if (onAbrirAperturaCaja != null) {
                onAbrirAperturaCaja.run();
            }
        });
        btnAbrirCaja.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);

        // Tarjeta/datáfono y entradas de efectivo (fondo extra)
        btnMovimientoPos = new Button("Movimiento de Caja", VaadinIcon.CREDIT_CARD.create(), e -> {
            try {
                if (onAbrirMovimientoPos != null) {
                    onAbrirMovimientoPos.run();
                }
            } catch (Exception ex) {
                log.error("Error inesperado", ex);
            }
        });

        btnMovimientoPos.addThemeVariants(ButtonVariant.LUMO_SMALL);

        // Mismo estilo que "Movimiento de Caja": antes era negro sólido y resaltaba
        // más que cualquier otra acción de la pantalla.
        Button btnDevolucion = new Button("Devolución", new Icon(VaadinIcon.ARROW_BACKWARD));
        btnDevolucion.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnDevolucion.setTooltipText("Devolver productos de una factura");

        // Evento de clic
        btnDevolucion.addClickListener(e -> abrirModalBuscarFactura());

        // Orden: acciones del día a día primero, cerrar caja al final
        HorizontalLayout cajaButtonsLayout = new HorizontalLayout(btnAbrirCaja, btnMovimientoPos, btnDevolucion, btnCerrarCaja);
        cajaButtonsLayout.setSpacing(true);
        cajaButtonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        // Los botones pasan a otra línea en pantallas angostas
        cajaButtonsLayout.getStyle().set("flex-wrap", "wrap");

        // Encabezado compacto: título y botones de caja en una sola fila
        // (antes eran dos filas más la etiqueta del buscador y el título
        // "Ventas Rápidas"; ese espacio ahora es del catálogo)
        // Debajo del título: estado de la caja y ventas del turno
        barraEstadoTurno.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "var(--lumo-space-xs)")
                .set("margin-top", "var(--lumo-space-xs)");
        Div bloqueTitulo = new Div(title, barraEstadoTurno);
        bloqueTitulo.getStyle().set("display", "flex").set("flex-direction", "column");

        HorizontalLayout encabezado = new HorizontalLayout(bloqueTitulo, cajaButtonsLayout);
        encabezado.setWidthFull();
        encabezado.setPadding(false);
        encabezado.setAlignItems(FlexComponent.Alignment.CENTER);
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.getStyle().set("flex-wrap", "wrap").set("row-gap", "var(--lumo-space-xs)");

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

        // Catálogo en cuadrícula
        gridContainer.getStyle().set("display", "grid");
        gridContainer.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(165px, 1fr))");
        gridContainer.getStyle().set("gap", "1rem");
        gridContainer.setWidthFull();

        btnMostrarMas.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnMostrarMas.addClickListener(e -> cargarMasTarjetas());

        configurarBarraCategorias(categorias);

        cargarMasTarjetas();

        // El catálogo tiene su propio scroll: título, botones y buscador quedan
        // siempre visibles mientras se recorre el catálogo.
        VerticalLayout contenidoCatalogo = new VerticalLayout(gridContainer, btnMostrarMas);
        contenidoCatalogo.setPadding(false);
        contenidoCatalogo.setSpacing(true);
        contenidoCatalogo.setAlignItems(FlexComponent.Alignment.CENTER);
        // Margen inferior: la última fila de tarjetas se ve completa al
        // llegar al final del scroll (antes quedaba cortada contra el borde).
        contenidoCatalogo.getStyle().set("padding-bottom", "var(--lumo-space-l)");

        Div scrollCatalogo = new Div(contenidoCatalogo);
        scrollCatalogo.setWidthFull();
        scrollCatalogo.getStyle().set("overflow-y", "auto").set("min-height", "0");

        add(encabezado, searchBox, barraCategorias, scrollCatalogo);
        expand(scrollCatalogo);
    }

    // ============================================================
    // BUSCADOR
    // ============================================================
    private ComboBox<ArticuloEmpaque> crearBuscador() {

        // Sin etiqueta: el texto de ayuda (placeholder) ya lo explica y la
        // etiqueta ocupaba una línea. Se conserva para lectores de pantalla.
        ComboBox<ArticuloEmpaque> combo = new ComboBox<>();
        combo.setAriaLabel("Buscar producto o escanear código");

        // Cada opción muestra producto, presentación, factor y precio: así se
        // distinguen las presentaciones del mismo artículo.
        combo.setItemLabelGenerator(this::etiquetaEmpaque);

        // Filtro en memoria: todas las palabras escritas deben aparecer en el
        // nombre, la presentación o el código.
        combo.setItems((empaque, texto) -> coincide(empaque, texto), catalogo);

        combo.setPlaceholder("Buscar producto o escanear código [F4]  ·  Cantidad: 50* y escanee");
        combo.setPrefixComponent(VaadinIcon.SEARCH.create());
        combo.setWidthFull();
        combo.setClearButtonVisible(true);
        combo.setAutofocus(true);
        combo.addFocusShortcut(Key.F4);

        // Escáner: manda el código y Enter. Si el combo no tiene un producto
        // resaltado, el texto llega como "valor propio" y se busca ese código
        // exacto (código de barras o código interno del empaque).
        combo.setAllowCustomValue(true);
        combo.addCustomValueSetListener(e -> agregarPorCodigo(e.getDetail()));

        return combo;
    }

    /**
     * Agrega al ticket la presentación con ese código exacto. Si no está en el
     * catálogo dice por qué (no existe, o no está en el mostrador). Un texto
     * que no parece código (un nombre a medio escribir) se deja como está
     * para que el cajero elija de la lista.
     */
    private void agregarPorCodigo(String texto) {

        String codigo = texto != null ? texto.trim() : "";
        if (codigo.isEmpty()) {
            return;
        }

        ArticuloEmpaque empaque = LineaPosUtil.buscarPorCodigo(codigo, catalogo).orElse(null);
        if (empaque != null) {
            if (onAgregarAlTicketActivo != null) {
                onAgregarAlTicketActivo.accept(empaque);
            }
            limpiarBuscador();
            return;
        }

        if (codigo.contains(" ") || codigo.chars().noneMatch(Character::isDigit)) {
            return; // es una búsqueda por nombre: que elija de la lista
        }

        ArticuloEmpaque fueraDelCatalogo = articuloEmpaqueService.getPorCodigoBarra(codigo).orElse(null);
        String mensaje = fueraDelCatalogo != null
                ? "\"" + etiquetaEmpaque(fueraDelCatalogo) + "\" no está a la venta en el mostrador "
                + "(inactivo o sin registrar en el almacén de venta)."
                : "No hay ningún producto con el código " + codigo + ".";
        Notification n = Notification.show(mensaje, 3500, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        limpiarBuscador();
    }

    /** Vacía el texto del buscador (y su filtro) y lo deja listo para el siguiente escaneo. */
    private void limpiarBuscador() {
        searchBox.clear();
        searchBox.getElement().executeJs(
                "this.filter = ''; if (this.inputElement) { this.inputElement.value = ''; }");
        searchBox.focus();
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
        if (ae.getCodigoBarra() != null && !ae.getCodigoBarra().isBlank()) {
            pajar.append(' ').append(ae.getCodigoBarra().trim().toLowerCase());
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
    // CATEGORÍAS
    // ============================================================
    /**
     * Botones "Todos" + una por categoría que tenga productos en el catálogo
     * (las vacías no se muestran) + "Sin categoría" si hay productos sin ella.
     * El botón elegido queda resaltado. Sin categorías asignadas no se muestra
     * la barra.
     */
    private void configurarBarraCategorias(List<Categoria> categorias) {

        barraCategorias.setSpacing(false);
        barraCategorias.setWidthFull();
        barraCategorias.getStyle().set("flex-wrap", "wrap").set("gap", "var(--lumo-space-xs)");

        Set<Integer> conProductos = new HashSet<>();
        boolean haySinCategoria = false;
        for (ArticuloEmpaque ae : catalogo) {
            Integer cat = categoriaDe(ae);
            if (cat == null) {
                haySinCategoria = true;
            } else {
                conProductos.add(cat);
            }
        }

        List<Categoria> visibles = categorias == null ? List.of()
                : categorias.stream().filter(c -> conProductos.contains(c.getCodigo())).toList();

        if (visibles.isEmpty()) {
            barraCategorias.setVisible(false);
            return;
        }

        List<Button> botones = new ArrayList<>();

        Button todos = crearBotonCategoria("Todos", ae -> true, botones);
        todos.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        barraCategorias.add(todos);

        for (Categoria c : visibles) {
            barraCategorias.add(crearBotonCategoria(c.getNombre(),
                    ae -> c.getCodigo().equals(categoriaDe(ae)), botones));
        }

        if (haySinCategoria) {
            barraCategorias.add(crearBotonCategoria("Sin categoría",
                    ae -> categoriaDe(ae) == null, botones));
        }
    }

    private Button crearBotonCategoria(String texto, Predicate<ArticuloEmpaque> filtro, List<Button> botones) {

        Button boton = new Button(texto);
        boton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        boton.getStyle().set("border-radius", "999px");
        botones.add(boton);

        boton.addClickListener(e -> {
            botones.forEach(b -> b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY));
            boton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            mostrarCatalogo(catalogo.stream().filter(filtro).toList());
            // El foco vuelve al buscador / lector de códigos
            searchBox.focus();
        });

        return boton;
    }

    /**
     * Más vendidos primero (veces vendido en los últimos días); a igual número
     * de ventas, por nombre. Lo que nunca se vendió queda al final en orden
     * alfabético.
     */
    private static List<ArticuloEmpaque> ordenarPorMasVendidos(List<ArticuloEmpaque> lista, Map<Integer, Long> veces) {
        List<ArticuloEmpaque> ordenada = new ArrayList<>(lista);
        ordenada.sort(Comparator
                .comparing((ArticuloEmpaque ae) -> veces.getOrDefault(ae.getCodigo(), 0L))
                .reversed()
                .thenComparing(ae -> ae.getNombreArticulo() == null ? "" : ae.getNombreArticulo(),
                        String.CASE_INSENSITIVE_ORDER));
        return ordenada;
    }

    /**
     * Junta las presentaciones de cada artículo en una sola tarjeta. Respeta el
     * orden de la lista: el artículo queda donde aparece su presentación más
     * vendida.
     */
    private List<List<ArticuloEmpaque>> agruparPorArticulo(List<ArticuloEmpaque> lista) {
        Map<Object, List<ArticuloEmpaque>> grupos = new LinkedHashMap<>();
        for (ArticuloEmpaque ae : lista) {
            Object clave = (ae.getArticulo() != null && ae.getArticulo().getCodigo() != null)
                    ? ae.getArticulo().getCodigo()
                    : "empaque-" + ae.getCodigo();
            grupos.computeIfAbsent(clave, k -> new ArrayList<>()).add(ae);
        }
        List<List<ArticuloEmpaque>> resultado = new ArrayList<>(grupos.values());
        // Los agotados al final: primero lo que se puede vender. El orden es
        // estable, así que dentro de cada bloque sigue el de más vendidos.
        resultado.sort(Comparator.comparing(grupo -> estaAgotado(grupo.get(0))));
        return resultado;
    }

    /**
     * Misma regla que la etiqueta "Agotado" de la tarjeta: artículo
     * inventariable sin existencia en el almacén de venta.
     */
    private boolean estaAgotado(ArticuloEmpaque ae) {
        if (ae.getArticulo() == null || !ae.getArticulo().getInventariable()) {
            return false;
        }
        BigDecimal existencia = existencias.get(ae.getArticulo().getCodigo());
        return existencia == null || existencia.signum() <= 0;
    }

    private static Integer categoriaDe(ArticuloEmpaque ae) {
        return ae.getArticulo() == null ? null : ae.getArticulo().getCategoria();
    }

    /** Reemplaza las tarjetas por las de la lista (se dibujan por lotes). */
    private void mostrarCatalogo(List<ArticuloEmpaque> lista) {
        catalogoFiltrado = agruparPorArticulo(lista);
        gridContainer.removeAll();
        tarjetasCargadas = 0;
        cargarMasTarjetas();
    }

    // ============================================================
    // CATÁLOGO RÁPIDO
    // ============================================================
    private void cargarMasTarjetas() {

        int hasta = Math.min(tarjetasCargadas + TARJETAS_POR_LOTE, catalogoFiltrado.size());

        for (int i = tarjetasCargadas; i < hasta; i++) {
            gridContainer.add(crearTarjeta(catalogoFiltrado.get(i)));
        }

        tarjetasCargadas = hasta;

        int restantes = catalogoFiltrado.size() - tarjetasCargadas;
        btnMostrarMas.setVisible(restantes > 0);
        btnMostrarMas.setText("Mostrar más (" + restantes + " restantes)");
    }

    private TarjetaProductoCompo crearTarjeta(List<ArticuloEmpaque> presentaciones) {

        return new TarjetaProductoCompo(
                presentaciones,
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
                },
                codigoArticulo -> existencias.get(codigoArticulo),
                codigoArticulo -> estadosMostrador.get(codigoArticulo)
        );
    }

    /** El aviso de traslado es informativo: si falla, la tarjeta sale como antes. */
    private Map<Integer, InventarioService.EstadoMostrador> leerEstadosMostrador() {
        try {
            return inventarioService.estadosMostrador();
        } catch (RuntimeException ex) {
            log.warn("No se pudo leer la existencia de los otros almacenes", ex);
            return Map.of();
        }
    }

    /**
     * Vuelve a leer la existencia de la BD y la actualiza en las tarjetas
     * visibles. Se llama al completar una venta, anulación o devolución.
     */
    public void refrescarExistencias() {
        try {
            existencias = inventarioService.existenciasPorArticulo();
            estadosMostrador = leerEstadosMostrador();
            gridContainer.getChildren()
                    .filter(TarjetaProductoCompo.class::isInstance)
                    .map(TarjetaProductoCompo.class::cast)
                    .forEach(TarjetaProductoCompo::actualizarExistencia);
        } catch (Exception ex) {
            // Mostrar existencia es informativo: nunca debe frenar la venta
            log.warn("No se pudo refrescar la existencia del catálogo", ex);
        }
    }

    // Método para permitir enfocar el buscador externamente si es necesario
    public ComboBox<ArticuloEmpaque> getSearchBox() {
        return searchBox;
    }

    /**
     * Ajusta los botones de caja al estado real del turno: con la caja abierta
     * se oculta "Abrir Caja"; sin caja se oculta "Cerrar Caja" y se desactivan
     * los movimientos. La vista del POS lo llama cada vez que cambia el estado.
     */
    /** Texto de la línea de estado del turno (lo arma la vista). */
    /**
     * Estado del turno como etiquetas de colores:
     * [● Caja abierta 4:43 p. m.] [👤 Admin] [🧾 12 ventas] [💵 RD$ 3,375.00] [📒 Fiado RD$ 1,370.00]
     */
    public void mostrarTurnoAbierto(String desde, String cajero, long ventas,
            BigDecimal total, BigDecimal fiado) {

        barraEstadoTurno.removeAll();
        barraEstadoTurno.add(chip(VaadinIcon.CIRCLE, "Caja abierta " + desde, "success"));
        if (cajero != null && !cajero.isBlank()) {
            barraEstadoTurno.add(chip(VaadinIcon.USER, cajero, "contrast"));
        }
        barraEstadoTurno.add(chip(VaadinIcon.RECORDS, ventas + (ventas == 1 ? " venta" : " ventas"), ""));

        com.vaadin.flow.component.html.Span chipTotal
                = chip(VaadinIcon.CASH, "RD$ " + FORMATO_PRECIO.format(total), "success primary");
        chipTotal.getStyle().set("font-size", "var(--lumo-font-size-s)");
        barraEstadoTurno.add(chipTotal);

        if (fiado != null && fiado.signum() > 0) {
            com.vaadin.flow.component.html.Span chipFiado
                    = chip(VaadinIcon.BOOK, "Fiado RD$ " + FORMATO_PRECIO.format(fiado), "");
            // Naranja: dinero que no entró a la gaveta
            chipFiado.getStyle()
                    .set("background", "rgba(234, 88, 12, 0.12)")
                    .set("color", "#c2410c");
            barraEstadoTurno.add(chipFiado);
        }
    }

    public void mostrarCajaCerrada() {
        barraEstadoTurno.removeAll();
        barraEstadoTurno.add(chip(VaadinIcon.LOCK, "Caja cerrada", "error primary"));
    }

    /** Etiqueta (badge de Lumo) con ícono. */
    private static com.vaadin.flow.component.html.Span chip(VaadinIcon icono, String texto, String variante) {
        Icon i = icono.create();
        i.getStyle().set("padding", "0 0.25em 0 0").set("width", "0.9em").set("height", "0.9em");
        com.vaadin.flow.component.html.Span s = new com.vaadin.flow.component.html.Span(i,
                new com.vaadin.flow.component.html.Span(texto));
        s.getElement().getThemeList().add(("badge " + variante).trim());
        s.getStyle().set("font-weight", "600");
        return s;
    }

    public void setOnVentasCambiadas(Runnable onVentasCambiadas) {
        this.onVentasCambiadas = onVentasCambiadas;
    }

    public void actualizarEstadoCaja(boolean cajaAbierta) {

        btnAbrirCaja.setVisible(!cajaAbierta);
        btnCerrarCaja.setVisible(cajaAbierta);
        btnMovimientoPos.setEnabled(cajaAbierta);
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

        Button btnBuscar = new Button("Buscar [Enter]", new Icon(VaadinIcon.SEARCH));
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
                        () -> {
                            Notification.show("Devolución completada con éxito", 3000, Notification.Position.TOP_CENTER);
                            refrescarExistencias();
                            if (onVentasCambiadas != null) {
                                onVentasCambiadas.run();
                            }
                        }
                );
                dialogoDev.open();

            } else {
                Notification.show("No se encontró ninguna factura con el código: " + codigo, 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        VerticalLayout content = new VerticalLayout(searchLayout);
        dialogBuscar.add(content);

        Button btnCerrar = new Button("Cancelar [Esc]", e -> dialogBuscar.close());
        dialogBuscar.getFooter().add(btnCerrar);

        dialogBuscar.open();
    }
}
