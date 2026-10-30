
package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.EmpaqueSeleccionDialogo;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
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
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@PermitAll
@PageTitle("Impresión de Códigos de Barra")
@Route(value = "inventario/codigoBarra")
public class ImpresionCodigoBarraView extends VerticalLayout {

    /**
     * Más etiquetas que esto en una sola impresión piden confirmación.
     */
    private static final int ETIQUETAS_PARA_CONFIRMAR = 500;

    private static final int MAXIMO_POR_LINEA = 1000;

    private static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("#,##0.00");

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

    // ============================================================
    // CONTROLES
    // ============================================================
    private void configurarControles() {

        cbTamano.setItems(TAMANOS);
        cbTamano.setAllowCustomValue(false);
        cbTamano.setValue(TAMANOS.get(2));

        txtPorFila.setMin(1);
        txtPorFila.setMax(6);
        txtPorFila.setValue(3);
        txtPorFila.setStepButtonsVisible(true);
        txtPorFila.setWidth("150px");

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
     * Valor que se codifica en la etiqueta para una presentación.
     *
     * Hoy se usa el código interno del empaque (el buscador del POS lo
     * reconoce). Si los empaques guardan un código de barras propio (por
     * ejemplo el EAN-13 del fabricante), este es el único lugar que hay que
     * cambiar.
     */
    private String codigoBarraDe(ArticuloEmpaque empaque) {

        return empaque.getCodigo() != null ? String.valueOf(empaque.getCodigo()) : "";
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

//        grid.addColumn(l -> l.codigo.isBlank() ? "(sin código)" : l.codigo + "  [" + BarcodeSvg.tipoPara(l.codigo) + "]")
//                .setHeader("Código de barras")
//                .setAutoWidth(true);

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

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Vista previa de etiquetas");
        dialog.setWidth("900px");
        dialog.setHeight("620px");

        IFrame marco = new IFrame();
        marco.setSizeFull();
        marco.getElement().setProperty("srcdoc", construirHtml(true));
        marco.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");

        VerticalLayout contenido = new VerticalLayout(marco);
        contenido.setSizeFull();
        contenido.setPadding(false);

        dialog.add(contenido);
        dialog.getFooter().add(
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
     * Imprime desde un marco oculto con el mismo HTML de la vista previa, sin
     * abrir otra pestaña.
     */
    private void enviarAImpresora() {

        String html = construirHtml(false);

        UI.getCurrent().getPage().executeJs(
                """
                const html = $0;
                const anterior = document.getElementById('etiquetas-print-frame');
                if (anterior) { anterior.remove(); }

                const marco = document.createElement('iframe');
                marco.id = 'etiquetas-print-frame';
                marco.style.cssText = 'position:fixed;right:0;bottom:0;width:0;height:0;border:0;';
                document.body.appendChild(marco);

                marco.onload = function () {
                    setTimeout(function () {
                        marco.contentWindow.focus();
                        marco.contentWindow.print();
                    }, 300);
                };
                marco.srcdoc = html;
                """,
                html
        );
    }

    private boolean hayEtiquetas() {

        if (lineas.isEmpty()) {
            ClaseUtil.mostrarNotificacion("Agregue al menos un artículo a la lista.", NotificationVariant.LUMO_WARNING);
            return false;
        }

        // Los códigos que no se pueden codificar se avisan antes de imprimir
        List<String> invalidos = new ArrayList<>();
        
        for (LineaEtiqueta l : lineas) {
            try {
//                BarcodeSvg.generar(l.codigo);
                
            } catch (IllegalArgumentException ex) {
                invalidos.add(l.empaque.getNombreArticulo());
            }
        }

        if (!invalidos.isEmpty()) {
            ClaseUtil.mostrarNotificacion(
                    "Sin código de barras válido: " + String.join(", ", invalidos),
                    NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    // ============================================================
    // HTML DE LAS ETIQUETAS
    // ============================================================
    /**
     * Arma el documento HTML con todas las etiquetas. En la vista previa las
     * etiquetas llevan un borde punteado para ver sus límites.
     */
    private String construirHtml(boolean vistaPrevia) {

        TamanoEtiqueta t = cbTamano.getValue() != null ? cbTamano.getValue() : TAMANOS.get(2);
        boolean rollo = chkRollo.getValue();
        int columnas = rollo ? 1 : Math.max(1, txtPorFila.getValue() != null ? txtPorFila.getValue() : 1);

        // Tamaños de texto y alto de barras según el alto de la etiqueta
        double fuenteNombre = clamp(t.alto() * 0.30, 7, 13);
        double fuenteTexto = clamp(t.alto() * 0.26, 6, 11);
        double altoBarras = t.alto() * (chkNombre.getValue() ? 0.36 : 0.50);

        StringBuilder css = new StringBuilder();
        css.append("*{box-sizing:border-box;}")
                .append("body{margin:0;font-family:Arial,Helvetica,sans-serif;color:#000;")
                .append(vistaPrevia ? "padding:6mm;background:#fff;" : "")
                .append("}");

        if (rollo) {
            css.append("@page{size:").append(t.ancho()).append("mm ").append(t.alto()).append("mm;margin:0;}");
        } else {
            css.append("@page{margin:8mm;}");
        }

        css.append(".hoja{display:grid;grid-template-columns:repeat(").append(columnas).append(",")
                .append(t.ancho()).append("mm);gap:").append(rollo ? "0" : "2mm").append(";}")
                .append(".etiqueta{width:").append(t.ancho()).append("mm;height:").append(t.alto()).append("mm;")
                .append("padding:1mm 1.5mm;overflow:hidden;display:flex;flex-direction:column;")
                .append("align-items:center;justify-content:center;text-align:center;")
                .append("break-inside:avoid;page-break-inside:avoid;")
                .append(rollo ? "page-break-after:always;break-after:page;" : "")
                .append(vistaPrevia ? "border:1px dashed #999;" : "")
                .append("}")
                .append(".nombre{width:100%;font-size:").append(fmt(fuenteNombre)).append("px;font-weight:bold;")
                .append("line-height:1.1;max-height:2.2em;overflow:hidden;}")
                .append(".pres{font-size:").append(fmt(fuenteTexto)).append("px;line-height:1.1;}")
                .append(".barras{width:100%;margin-top:0.5mm;}")
                .append(".barras svg{display:block;width:100%;height:").append(fmt(altoBarras)).append("mm;fill:#000;}")
                .append(".texto{font-size:").append(fmt(fuenteTexto)).append("px;letter-spacing:1px;line-height:1.1;}")
                .append(".precio{font-size:").append(fmt(fuenteNombre + 2)).append("px;font-weight:bold;line-height:1.1;}")
                .append(".error{font-size:").append(fmt(fuenteTexto)).append("px;color:#c00;}");

        StringBuilder cuerpo = new StringBuilder("<div class=\"hoja\">");

        for (LineaEtiqueta l : lineas) {

            String etiqueta = construirEtiqueta(l);

            for (int i = 0; i < l.cantidad(); i++) {
                cuerpo.append(etiqueta);
            }
        }

        cuerpo.append("</div>");

        return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><style>" + css + "</style></head><body>"
                + cuerpo + "</body></html>";
    }

    private String construirEtiqueta(LineaEtiqueta l) {

        StringBuilder sb = new StringBuilder("<div class=\"etiqueta\">");

        if (chkNombre.getValue() && l.empaque.getNombreArticulo() != null) {
            sb.append("<div class=\"nombre\">").append(esc(l.empaque.getNombreArticulo())).append("</div>");
        }

        if (chkPresentacion.getValue()) {
            sb.append("<div class=\"pres\">").append(esc(textoPresentacion(l.empaque))).append("</div>");
        }

        sb.append("<div class=\"barras\">");
        
        try {
            
//            sb.append(BarcodeSvg.generar(l.codigo));
            
        } catch (IllegalArgumentException ex) {
            sb.append("<div class=\"error\">Código no válido</div>");
        }
        sb.append("</div>");

        if (chkCodigo.getValue()) {
            sb.append("<div class=\"texto\">").append(esc(l.codigo)).append("</div>");
        }

        if (chkPrecio.getValue() && l.empaque.getPrecioVenta() != null) {
            sb.append("<div class=\"precio\">RD$ ").append(FORMATO_PRECIO.format(l.empaque.getPrecioVenta())).append("</div>");
        }

        sb.append("</div>");

        return sb.toString();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    /**
     * Texto de la presentación: "Unidad", "Caja x12", etc.
     */
    private String textoPresentacion(ArticuloEmpaque ae) {

        String unidad = (ae.getUnidadEmpaque() != null && ae.getUnidadEmpaque().getDescripcion() != null)
                ? ae.getUnidadEmpaque().getDescripcion()
                : "Unidad";

        BigDecimal factor = ae.getFactorConversion();
        if (factor != null && factor.compareTo(BigDecimal.ONE) != 0) {
            return unidad + " x" + factor.stripTrailingZeros().toPlainString();
        }
        return unidad;
    }

    private static double clamp(double v, double min, double max) {

        return Math.max(min, Math.min(max, v));
    }

    private static String fmt(double v) {

        return String.format(java.util.Locale.US, "%.1f", v);
    }

    /**
     * Escapa el texto que se inserta en el HTML de las etiquetas.
     */
    private static String esc(String s) {

        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}