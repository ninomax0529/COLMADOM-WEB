
package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.reporte.inventario.EtiquetasPdf;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.util.CodigoBarraUtil;
import com.maxsoft.application.view.componente.EmpaqueSeleccionDialogo;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Impresión de etiquetas de código de barras de los artículos. Las etiquetas
 * se generan en PDF al tamaño exacto (rollo o carta) con el código de barras
 * de la presentación; si no tiene uno, con su código interno.
 */
@PermitAll
@PageTitle("Impresión de Códigos de Barra")
@Route(value = "inventario/codigoBarra")
public class ImpresionCodigoBarraView extends VerticalLayout {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImpresionCodigoBarraView.class);

    /**
     * Más etiquetas que esto en una sola impresión piden confirmación.
     */
    private static final int ETIQUETAS_PARA_CONFIRMAR = 500;

    private static final int MAXIMO_POR_LINEA = 1000;

    private static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("#,##0.00");

    /** Imprime el PDF desde un iframe oculto (como la factura del POS). */
    private static final String SCRIPT_IMPRIMIR = """
            fetch($0)
                .then(r => { if (!r.ok) { throw new Error('HTTP ' + r.status); } return r.blob(); })
                .then(blob => {
                    const url = URL.createObjectURL(blob);
                    let marco = document.getElementById('etiquetas-print-frame');
                    if (!marco) {
                        marco = document.createElement('iframe');
                        marco.id = 'etiquetas-print-frame';
                        marco.style.cssText = 'position:fixed;right:0;bottom:0;width:0;height:0;border:0;';
                        document.body.appendChild(marco);
                    }
                    marco.onload = function () {
                        setTimeout(function () {
                            marco.contentWindow.focus();
                            marco.contentWindow.print();
                            URL.revokeObjectURL(url);
                        }, 300);
                    };
                    marco.src = url;
                })
                .catch(err => console.error('Error al imprimir las etiquetas:', err));
            """;

    /**
     * Tamaños de etiqueta disponibles (milímetros).
     */
    private record TamanoEtiqueta(int ancho, int alto) {

        @Override
        public String toString() {
            return ancho + " x " + alto + " mm";
        }
    }

    private static final List<TamanoEtiqueta> TAMANOS = List.of(
            new TamanoEtiqueta(30, 20),
            new TamanoEtiqueta(40, 25),
            new TamanoEtiqueta(50, 30),
            new TamanoEtiqueta(60, 40),
            new TamanoEtiqueta(100, 50)
    );

    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService;

    private final Grid<LineaEtiqueta> grid = new Grid<>(LineaEtiqueta.class, false);
    private GridListDataView<LineaEtiqueta> dataView;

    // Configuración de la etiqueta
    private final ComboBox<TamanoEtiqueta> cbTamano = new ComboBox<>("Tamaño de etiqueta");
    private final IntegerField txtPorFila = new IntegerField("Etiquetas por fila");
    private final Checkbox chkRollo = new Checkbox("Impresora de rollo (1 etiqueta por página)");
    private final Checkbox chkNombre = new Checkbox("Nombre", true);
    private final Checkbox chkPresentacion = new Checkbox("Presentación", true);
    private final Checkbox chkCodigo = new Checkbox("Número del código", true);
    private final Checkbox chkPrecio = new Checkbox("Precio", false);

    private final TextField txtBuscar = new TextField();
    private Button btnArticulo;
    private Button btnVistaPrevia;
    private Button btnImprimir;
    private Button btnLimpiar;

    private final List<LineaEtiqueta> lineas = new ArrayList<>();

    private Grid.Column<LineaEtiqueta> colArticulo;
    private Grid.Column<LineaEtiqueta> colCantidad;

    /** PDF mandado a imprimir; se libera al imprimir otro o al salir. */
    private StreamRegistration pdfImpresion;

    @Autowired
    public ImpresionCodigoBarraView(ArticuloService articuloService,
            ArticuloEmpaqueService articuloEmpaqueService) {

        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;

        setSizeFull();

        configurarControles();
        configurarGrid();

        HorizontalLayout hlConfig = new HorizontalLayout(cbTamano, txtPorFila, chkRollo);
        hlConfig.setAlignItems(Alignment.BASELINE);
        hlConfig.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout hlContenido = new HorizontalLayout(chkNombre, chkPresentacion, chkCodigo, chkPrecio);
        hlContenido.setAlignItems(Alignment.CENTER);
        hlContenido.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout hlAcciones = new HorizontalLayout(btnArticulo, txtBuscar, btnVistaPrevia, btnImprimir, btnLimpiar);
        hlAcciones.setWidthFull();
        hlAcciones.setAlignItems(Alignment.BASELINE);
        hlAcciones.getStyle().set("flex-wrap", "wrap");

        add(new H2("Impresión de Códigos de Barra"), hlConfig, hlContenido, hlAcciones, grid);

        actualizarTotales();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        liberarPdf();
        super.onDetach(detachEvent);
    }

    // ============================================================
    // CONTROLES
    // ============================================================
    private void configurarControles() {

        cbTamano.setItems(TAMANOS);
        cbTamano.setAllowCustomValue(false);
        cbTamano.setValue(TAMANOS.get(2));
        // En hoja carta no caben más etiquetas por fila que las que da el ancho
        cbTamano.addValueChangeListener(e -> ajustarPorFila());

        txtPorFila.setMin(1);
        txtPorFila.setStepButtonsVisible(true);
        txtPorFila.setWidth("150px");
        ajustarPorFila();
        txtPorFila.setValue(3);

        chkRollo.addValueChangeListener(e -> txtPorFila.setEnabled(!e.getValue()));
        chkRollo.setValue(true);

        btnArticulo = new Button("Artículo (F2)", VaadinIcon.PLUS.create(), e -> abrirDialogoSeleccionArticulo());
        btnArticulo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnArticulo.addClickShortcut(Key.F2);

        txtBuscar.setWidth("320px");
        txtBuscar.setPlaceholder("Filtrar la lista por código o descripción...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setClearButtonVisible(true);
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);

        btnVistaPrevia = new Button("Vista previa", VaadinIcon.EYE.create(), e -> mostrarVistaPrevia());

        btnImprimir = new Button("Imprimir etiquetas", VaadinIcon.PRINT.create(), e -> imprimir());
        btnImprimir.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

        btnLimpiar = new Button("Vaciar lista", VaadinIcon.TRASH.create(), e -> vaciarLista());
        btnLimpiar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    }

    private void ajustarPorFila() {

        TamanoEtiqueta t = cbTamano.getValue() != null ? cbTamano.getValue() : TAMANOS.get(2);
        int maximo = EtiquetasPdf.maximoPorFila(t.ancho());
        txtPorFila.setMax(maximo);
        txtPorFila.setHelperText("En carta caben hasta " + maximo);
        if (txtPorFila.getValue() != null && txtPorFila.getValue() > maximo) {
            txtPorFila.setValue(maximo);
        }
    }

    // ============================================================
    // ARTÍCULOS
    // ============================================================
    private void abrirDialogoSeleccionArticulo() {

        try {
            ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloService, articulo -> {
                if (articulo != null) {
                    new EmpaqueSeleccionDialogo(
                            articulo,
                            articuloEmpaqueService,
                            empaque -> {
                                if (empaque != null) {
                                    agregarOSumar(articulo, empaque);
                                }
                            }
                    ).open();
                }
            });
            dialog.open();
        } catch (Exception e) {
            Notification.show("Error abriendo diálogo de artículos: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void agregarOSumar(Articulo articulo, ArticuloEmpaque empaque) {

        LineaEtiqueta existente = lineas.stream()
                .filter(l -> Objects.equals(l.empaque.getCodigo(), empaque.getCodigo()))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            existente.txtCantidad.setValue(existente.cantidad() + 1);
            return;
        }

        lineas.add(new LineaEtiqueta(articulo, empaque));
        grid.getDataProvider().refreshAll();
        actualizarTotales();
    }

    /**
     * Valor que se codifica en la etiqueta: el código de barras de la
     * presentación (el del fabricante o el asignado); si no tiene, su código
     * interno.
     */
    private static String codigoBarraDe(ArticuloEmpaque empaque) {

        if (empaque.getCodigoBarra() != null && !empaque.getCodigoBarra().isBlank()) {
            return empaque.getCodigoBarra().trim();
        }
        return empaque.getCodigo() != null ? String.valueOf(empaque.getCodigo()) : "";
    }

    private static boolean usaCodigoInterno(ArticuloEmpaque empaque) {
        return empaque.getCodigoBarra() == null || empaque.getCodigoBarra().isBlank();
    }

    // ============================================================
    // GRID
    // ============================================================
    private void configurarGrid() {

        grid.setHeightFull();
        grid.setWidthFull();

        dataView = grid.setItems(lineas);

        colArticulo = grid.addColumn(l -> l.empaque.getNombreArticulo())
                .setHeader("Artículo")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setFooter("TOTAL: 0 artículos");

        grid.addColumn(l -> textoPresentacion(l.empaque))
                .setHeader("Presentación")
                .setAutoWidth(true);

        grid.addComponentColumn(this::celdaCodigo)
                .setHeader("Código de barras")
                .setAutoWidth(true);

        grid.addColumn(l -> l.empaque.getPrecioVenta() != null
                ? "RD$ " + FORMATO_PRECIO.format(l.empaque.getPrecioVenta()) : "")
                .setHeader("Precio")
                .setAutoWidth(true);

        colCantidad = grid.addColumn(new ComponentRenderer<>(l -> l.txtCantidad))
                .setHeader("Etiquetas a imprimir")
                .setAutoWidth(true)
                .setSortable(false)
                .setFooter("0 etiquetas");

        grid.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea quitar '" + item.empaque.getNombreArticulo() + "' de la lista?",
                        () -> {
                            lineas.remove(item);
                            grid.getDataProvider().refreshAll();
                            actualizarTotales();
                        },
                        () -> {
                        }
                );
                dialog.open();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return deleteButton;
        })).setHeader("Acciones").setAutoWidth(true);

        txtBuscar.addValueChangeListener(e -> dataView.setFilter(l -> {
            String term = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            if (term.isEmpty()) {
                return true;
            }
            String nombre = l.empaque.getNombreArticulo() != null ? l.empaque.getNombreArticulo().toLowerCase() : "";
            return nombre.contains(term) || l.codigo.toLowerCase().contains(term);
        }));
    }

    /** El código y su tipo; el interno y el no válido se marcan. */
    private HorizontalLayout celdaCodigo(LineaEtiqueta l) {

        CodigoBarraUtil.Tipo tipo = CodigoBarraUtil.tipo(l.codigo);
        Span chip;
        if (tipo == CodigoBarraUtil.Tipo.INVALIDO) {
            chip = EstiloConsulta.chip("NO VÁLIDO", "fv-chip-anulada");
        } else if (usaCodigoInterno(l.empaque)) {
            chip = EstiloConsulta.chip("INTERNO", "fv-chip-credito");
            chip.getElement().setAttribute("title",
                    "La presentación no tiene código de barras: se imprime su código interno");
        } else {
            chip = EstiloConsulta.chip(tipo.getNombre(), "fv-chip-pagada");
        }
        HorizontalLayout h = new HorizontalLayout(new Span(l.codigo), chip);
        h.setSpacing(true);
        h.setAlignItems(Alignment.CENTER);
        return h;
    }

    private void actualizarTotales() {

        int totalEtiquetas = lineas.stream().mapToInt(LineaEtiqueta::cantidad).sum();

        colArticulo.setFooter("TOTAL: " + lineas.size() + (lineas.size() == 1 ? " artículo" : " artículos"));
        colCantidad.setFooter(totalEtiquetas + (totalEtiquetas == 1 ? " etiqueta" : " etiquetas"));
    }

    private void vaciarLista() {

        if (lineas.isEmpty()) {
            return;
        }

        new ConfirmDialog(
                "¿Seguro que desea vaciar la lista de etiquetas?",
                () -> {
                    lineas.clear();
                    grid.getDataProvider().refreshAll();
                    actualizarTotales();
                },
                () -> {
                }
        ).open();
    }

    /**
     * Una presentación con su cantidad de etiquetas. El campo de cantidad se
     * crea una sola vez por línea para no perder el cursor al escribir.
     */
    private class LineaEtiqueta {

        final Articulo articulo;
        final ArticuloEmpaque empaque;
        final String codigo;
        final IntegerField txtCantidad = new IntegerField();

        LineaEtiqueta(Articulo articulo, ArticuloEmpaque empaque) {

            this.articulo = articulo;
            this.empaque = empaque;
            this.codigo = codigoBarraDe(empaque);

            txtCantidad.setMin(1);
            txtCantidad.setMax(MAXIMO_POR_LINEA);
            txtCantidad.setStepButtonsVisible(true);
            txtCantidad.setValue(1);
            txtCantidad.setWidth("130px");

            txtCantidad.addValueChangeListener(e -> {
                Integer v = e.getValue();
                if (v == null || v < 1) {
                    txtCantidad.setValue(1);
                    return;
                }
                actualizarTotales();
            });
        }

        int cantidad() {
            Integer v = txtCantidad.getValue();
            return v != null && v > 0 ? v : 1;
        }
    }

    // ============================================================
    // VISTA PREVIA E IMPRESIÓN
    // ============================================================
    private void mostrarVistaPrevia() {

        if (!hayEtiquetas()) {
            return;
        }

        byte[] pdf = generarPdf(true);
        if (pdf == null) {
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Vista previa de etiquetas");
        dialog.setWidth("90vw");
        dialog.setHeight("90vh");

        IFrame marco = new IFrame();
        marco.setSizeFull();
        marco.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        StreamResource recurso = new StreamResource("etiquetas-vista-previa.pdf", () -> new ByteArrayInputStream(pdf));
        recurso.setContentType("application/pdf");
        marco.getElement().setAttribute("src", recurso);

        VerticalLayout contenido = new VerticalLayout(marco);
        contenido.setSizeFull();
        contenido.setPadding(false);

        dialog.add(contenido);
        dialog.getFooter().add(
                new Span("La línea punteada marca el borde de cada etiqueta; no se imprime."),
                new Button("Cerrar", e -> dialog.close()),
                crearBotonImprimirDesde(dialog)
        );
        dialog.open();
    }

    private Button crearBotonImprimirDesde(Dialog dialog) {

        Button b = new Button("Imprimir", VaadinIcon.PRINT.create(), e -> {
            dialog.close();
            imprimir();
        });
        b.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        return b;
    }

    private void imprimir() {

        if (!hayEtiquetas()) {
            return;
        }

        int total = lineas.stream().mapToInt(LineaEtiqueta::cantidad).sum();

        if (total > ETIQUETAS_PARA_CONFIRMAR) {
            new ConfirmDialog(
                    "Va a imprimir " + total + " etiquetas. ¿Desea continuar?",
                    this::enviarAImpresora,
                    () -> {
                    }
            ).open();
            return;
        }

        enviarAImpresora();
    }

    /**
     * Manda el PDF a la impresora desde un marco oculto, sin abrir otra
     * pestaña. Al ser PDF, el tamaño de la etiqueta es exacto.
     */
    private void enviarAImpresora() {

        byte[] pdf = generarPdf(false);
        if (pdf == null) {
            return;
        }

        StreamResource recurso = new StreamResource("etiquetas.pdf", () -> new ByteArrayInputStream(pdf));
        recurso.setContentType("application/pdf");
        liberarPdf();
        pdfImpresion = VaadinSession.getCurrent().getResourceRegistry().registerResource(recurso);

        UI.getCurrent().getPage().executeJs(SCRIPT_IMPRIMIR, pdfImpresion.getResourceUri().toString());
        Notification.show("Enviando las etiquetas a la impresora...", 2000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void liberarPdf() {
        if (pdfImpresion != null) {
            pdfImpresion.unregister();
            pdfImpresion = null;
        }
    }

    /** PDF con todas las etiquetas de la lista; null si falló (ya se avisó). */
    private byte[] generarPdf(boolean vistaPrevia) {

        TamanoEtiqueta t = cbTamano.getValue() != null ? cbTamano.getValue() : TAMANOS.get(2);
        EtiquetasPdf.Formato formato = new EtiquetasPdf.Formato(t.ancho(), t.alto(),
                txtPorFila.getValue() != null ? txtPorFila.getValue() : 1, chkRollo.getValue(),
                chkNombre.getValue(), chkPresentacion.getValue(), chkCodigo.getValue(), chkPrecio.getValue());

        List<EtiquetasPdf.Etiqueta> etiquetas = lineas.stream()
                .map(l -> new EtiquetasPdf.Etiqueta(l.empaque.getNombreArticulo(), textoPresentacion(l.empaque),
                        l.codigo, l.empaque.getPrecioVenta(), l.cantidad()))
                .toList();
        try {
            return EtiquetasPdf.generar(etiquetas, formato, vistaPrevia);
        } catch (RuntimeException ex) {
            log.error("Error al generar las etiquetas", ex);
            ClaseUtil.mostrarNotificacion("No se pudieron generar las etiquetas: " + ex.getMessage(),
                    NotificationVariant.LUMO_ERROR);
            return null;
        }
    }

    private boolean hayEtiquetas() {

        if (lineas.isEmpty()) {
            ClaseUtil.mostrarNotificacion("Agregue al menos un artículo a la lista.", NotificationVariant.LUMO_WARNING);
            return false;
        }

        // Los códigos que no se pueden codificar se avisan antes de imprimir
        List<String> invalidos = lineas.stream()
                .filter(l -> CodigoBarraUtil.tipo(l.codigo) == CodigoBarraUtil.Tipo.INVALIDO)
                .map(l -> l.empaque.getNombreArticulo() + " (" + l.codigo + ")")
                .toList();

        if (!invalidos.isEmpty()) {
            ClaseUtil.mostrarNotificacion(
                    "Sin código de barras válido: " + String.join(", ", invalidos),
                    NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    /**
     * Texto de la presentación: "Unidad", "Caja x12", etc.
     */
    private static String textoPresentacion(ArticuloEmpaque ae) {

        String unidad = (ae.getUnidadEmpaque() != null && ae.getUnidadEmpaque().getDescripcion() != null)
                ? ae.getUnidadEmpaque().getDescripcion()
                : "Unidad";

        BigDecimal factor = ae.getFactorConversion();
        if (factor != null && factor.compareTo(BigDecimal.ONE) != 0) {
            return unidad + " x" + factor.stripTrailingZeros().toPlainString();
        }
        return unidad;
    }
}
