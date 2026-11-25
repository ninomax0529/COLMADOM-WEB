package com.maxsoft.application.view.inventario.conteo;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ConteoCiclicoService;
import com.maxsoft.application.servicio.interfaces.inventario.ConteoCiclicoService.ArticuloAContar;
import com.maxsoft.application.servicio.interfaces.inventario.ConteoCiclicoService.LineaConteo;
import com.maxsoft.application.servicio.interfaces.inventario.ConteoCiclicoService.LineaHistorial;
import com.maxsoft.application.servicio.interfaces.inventario.ConteoCiclicoService.ResultadoConteo;
import com.maxsoft.application.servicio.interfaces.inventario.ConteoCiclicoService.ResumenConteo;
import com.maxsoft.application.util.ConteoCiclicoUtil;
import com.maxsoft.application.view.componente.EstiloConsulta;
import com.maxsoft.application.view.inventario.abc.ClasificacionAbcView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Conteo cíclico: cada día se cuentan unos pocos artículos del almacén (los
 * A cada 7 días, los B cada 30 y los C cada 90). Se cuenta "a ciegas": la
 * pantalla no muestra la existencia del sistema hasta confirmar, para que el
 * conteo no se deje llevar por ella. Las diferencias se ajustan solas.
 */
@PageTitle("Conteo cíclico | MaxSoft ERP")
@Route(value = "inventario/conteoCiclico")
public class ConteoCiclicoView extends VerticalLayout {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConteoCiclicoView.class);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat CANTIDAD = new DecimalFormat("#,##0.##");
    private static final DecimalFormat DIFERENCIA = new DecimalFormat("+#,##0.##;-#,##0.##");
    private static final int DIAS_HISTORIAL = 30;

    private final ConteoCiclicoService conteoService;

    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén");
    private final Div resumen = new Div();
    private final Grid<ArticuloAContar> grid = new Grid<>();
    private final Grid<LineaHistorial> gridHistorial = new Grid<>();
    private final Span lblVacio = new Span();
    /** Lo que se va digitando, por artículo (se conserva al recargar la lista). */
    private final Map<Integer, BigDecimal> contado = new HashMap<>();
    private final List<ArticuloAContar> articulos = new ArrayList<>();

    public ConteoCiclicoView(ConteoCiclicoService conteoService, AlmacenService almacenService) {
        this.conteoService = conteoService;

        setSizeFull();
        setSpacing(false);

        H2 titulo = new H2("Conteo cíclico");
        titulo.addClassName("fv-titulo");

        resumen.addClassNames("fv-tarjetas", "fv-tarjetas-fila");
        configurarGrid();
        configurarHistorial();

        lblVacio.getStyle().set("color", "var(--lumo-secondary-text-color)").set("padding", "var(--lumo-space-m)");
        VerticalLayout contar = new VerticalLayout(lblVacio, grid);
        contar.setPadding(false);
        contar.setSizeFull();
        contar.setFlexGrow(1, grid);

        TabSheet tabs = new TabSheet();
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.CLIPBOARD_CHECK, "Contar hoy"), contar);
        tabs.add(EstiloConsulta.etiquetaTab(VaadinIcon.TIME_BACKWARD, "Historial (" + DIAS_HISTORIAL + " días)"),
                gridHistorial);
        tabs.setSizeFull();
        EstiloConsulta.aplicarVista(this, tabs);

        cbAlmacen.setItems(almacenService.getLista());
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setWidth("260px");
        cbAlmacen.addValueChangeListener(e -> {
            contado.clear();
            cargar(false);
        });

        add(titulo, crearBarra(), resumen, tabs);
        setFlexGrow(1, tabs);

        almacenService.getLista().stream().findFirst().ifPresent(cbAlmacen::setValue);
    }

    private HorizontalLayout crearBarra() {
        Button btnGuardar = new Button("Guardar conteo", VaadinIcon.CHECK.create(), e -> confirmarGuardar());
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnMas = new Button("Contar más", VaadinIcon.PLUS.create(), e -> cargar(true));
        btnMas.setTooltipText("Trae los siguientes que tocan aunque ya se contó la cantidad del día");

        HorizontalLayout barra = new HorizontalLayout(cbAlmacen, btnGuardar, btnMas,
                EstiloConsulta.ayuda("Cuente lo que hay en el almacén (en la unidad base) y escríbalo."
                        + " Los que deje en blanco quedan para después."));
        barra.setAlignItems(Alignment.BASELINE);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    // ============================================================
    // GRIDS
    // ============================================================
    private void configurarGrid() {
        EstiloConsulta.aplicarGrid(grid);
        grid.setSizeFull();

        grid.addComponentColumn(a -> ClasificacionAbcView.chip(a.clase()))
                .setHeader("Clase").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(ArticuloAContar::descripcion).setHeader("Artículo").setFlexGrow(1);
        grid.addColumn(a -> a.ubicacion() != null ? a.ubicacion() : "")
                .setHeader("Ubicación").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(a -> a.unidad() != null ? a.unidad() : "")
                .setHeader("Unidad").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(a -> ultimoConteo(a.ultimoConteo()))
                .setHeader("Último conteo").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::campoContado)
                .setHeader("Contado").setWidth("150px").setFlexGrow(0);
    }

    private BigDecimalField campoContado(ArticuloAContar a) {
        BigDecimalField campo = new BigDecimalField();
        campo.setWidthFull();
        campo.setPlaceholder("Cantidad");
        campo.setValue(contado.get(a.articulo()));
        campo.addValueChangeListener(e -> {
            if (e.getValue() == null) {
                contado.remove(a.articulo());
            } else {
                contado.put(a.articulo(), e.getValue());
            }
        });
        return campo;
    }

    private void configurarHistorial() {
        EstiloConsulta.aplicarGrid(gridHistorial);
        gridHistorial.setSizeFull();
        gridHistorial.addColumn(h -> h.fecha() != null ? h.fecha().format(FECHA) : "")
                .setHeader("Fecha").setAutoWidth(true).setFlexGrow(0);
        gridHistorial.addComponentColumn(h -> h.clase() != null ? ClasificacionAbcView.chip(h.clase()) : new Span())
                .setHeader("Clase").setAutoWidth(true).setFlexGrow(0);
        gridHistorial.addColumn(LineaHistorial::articulo).setHeader("Artículo").setFlexGrow(1);
        EstiloConsulta.monto(gridHistorial.addColumn(h -> CANTIDAD.format(h.sistema())))
                .setHeader("Sistema").setAutoWidth(true).setFlexGrow(0);
        EstiloConsulta.monto(gridHistorial.addColumn(h -> CANTIDAD.format(h.contada())))
                .setHeader("Contado").setAutoWidth(true).setFlexGrow(0);
        gridHistorial.addComponentColumn(h -> diferencia(h.diferencia()))
                .setHeader("Diferencia").setAutoWidth(true).setFlexGrow(0);
        gridHistorial.addColumn(this::ajustes).setHeader("Ajuste").setAutoWidth(true).setFlexGrow(0);
    }

    /** "Nunca", "Hoy", "Hace N días (dd/MM/yyyy)". */
    private static String ultimoConteo(LocalDate fecha) {
        Long dias = ConteoCiclicoUtil.diasDesdeConteo(fecha, LocalDate.now());
        if (dias == null) {
            return "Nunca";
        }
        if (dias == 0) {
            return "Hoy";
        }
        return (dias == 1 ? "Ayer" : "Hace " + dias + " días") + " (" + fecha.format(FECHA) + ")";
    }

    /** Verde si cuadra, naranja si sobró, rojo si faltó. */
    private static Span diferencia(BigDecimal d) {
        if (d == null || d.signum() == 0) {
            return EstiloConsulta.chip("Cuadra", "fv-chip-pagada");
        }
        return EstiloConsulta.chip((d.signum() > 0 ? "Sobran " : "Faltan ") + CANTIDAD.format(d.abs()),
                d.signum() > 0 ? "fv-chip-devuelto" : "fv-chip-anulada");
    }

    private String ajustes(LineaHistorial h) {
        if (h.diferencia() == null || h.diferencia().signum() == 0) {
            return "";
        }
        Integer ajuste = h.diferencia().signum() > 0 ? h.ajusteIncremento() : h.ajusteDisminucion();
        return ajuste != null ? "#" + ajuste : "";
    }

    // ============================================================
    // DATOS
    // ============================================================
    private void cargar(boolean contarMas) {
        Almacen almacen = cbAlmacen.getValue();
        Integer codigo = almacen != null ? almacen.getCodigo() : null;

        List<ArticuloAContar> nuevos = conteoService.paraHoy(codigo, contarMas);
        if (contarMas) {
            // Se agregan debajo de los que ya están (sin repetir)
            nuevos.stream()
                    .filter(n -> articulos.stream().noneMatch(a -> a.articulo().equals(n.articulo())))
                    .forEach(articulos::add);
            if (nuevos.isEmpty()) {
                Notification.show("No hay más artículos que toque contar en este almacén.", 3000,
                        Notification.Position.TOP_CENTER);
            }
        } else {
            articulos.clear();
            articulos.addAll(nuevos);
        }
        grid.setItems(articulos);

        ResumenConteo r = conteoService.resumen(codigo);
        lblVacio.setText(r.contadosHoy() >= r.porDia()
                ? "Ya contó los " + r.porDia() + " artículos de hoy en este almacén. Use \"Contar más\" si quiere seguir."
                : "No hay artículos que toque contar hoy en este almacén.");
        lblVacio.setVisible(articulos.isEmpty());
        grid.setVisible(!articulos.isEmpty());

        actualizarResumen(r);
        gridHistorial.setItems(conteoService.historial(codigo, DIAS_HISTORIAL));
    }

    private void actualizarResumen(ResumenConteo r) {
        resumen.removeAll();
        resumen.add(tarjeta(VaadinIcon.CLIPBOARD_CHECK, "Contados hoy", r.contadosHoy() + " de " + r.porDia(),
                r.contadosHoy() >= r.porDia() ? "listo por hoy" : "faltan " + (r.porDia() - r.contadosHoy()),
                r.contadosHoy() >= r.porDia() ? "fv-tarjeta-pagado" : "fv-tarjeta-total"));
        resumen.add(tarjeta(VaadinIcon.CLOCK, "Por contar", r.tocan() + (r.tocan() == 1 ? " artículo" : " artículos"),
                "nunca contados o ya les toca", r.tocan() > r.porDia() ? "fv-tarjeta-pendiente" : "fv-tarjeta-saldada"));
        resumen.add(tarjeta(VaadinIcon.CHECK_CIRCLE, "Exactitud " + DIAS_HISTORIAL + " días",
                r.exactitud30() == null ? "—" : CANTIDAD.format(r.exactitud30()) + " %",
                r.contados30() + " contados sin diferencia o con ella", "fv-tarjeta-devuelto"));
    }

    private static Div tarjeta(VaadinIcon icono, String etiqueta, String valor, String detalle, String clase) {
        Icon ic = icono.create();
        ic.addClassName("fv-tarjeta-icono");
        Span lbl = new Span(etiqueta);
        lbl.addClassName("fv-tarjeta-etiqueta");
        Span val = new Span(valor);
        val.addClassName("fv-tarjeta-valor");
        Span det = new Span(detalle);
        det.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");
        Div d = new Div(ic, lbl, val, det);
        d.addClassNames("fv-tarjeta", clase);
        return d;
    }

    // ============================================================
    // GUARDAR
    // ============================================================
    /** Muestra las diferencias (con la existencia de cuando se abrió la lista) y pide confirmación. */
    private void confirmarGuardar() {
        Almacen almacen = cbAlmacen.getValue();
        if (almacen == null) {
            Notification.show("Seleccione el almacén.", 3000, Notification.Position.TOP_CENTER);
            return;
        }
        List<ArticuloAContar> contados = articulos.stream().filter(a -> contado.containsKey(a.articulo())).toList();
        if (contados.isEmpty()) {
            Notification.show("Escriba lo que contó de al menos un artículo.", 3000, Notification.Position.TOP_CENTER);
            return;
        }
        if (contados.stream().anyMatch(a -> contado.get(a.articulo()).signum() < 0)) {
            Notification.show("Lo contado no puede ser negativo.", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        List<String> diferencias = contados.stream()
                .filter(a -> contado.get(a.articulo()).compareTo(a.existencia()) != 0)
                .map(a -> {
                    BigDecimal d = contado.get(a.articulo()).subtract(a.existencia());
                    return a.descripcion() + ": sistema " + CANTIDAD.format(a.existencia()) + ", contado "
                            + CANTIDAD.format(contado.get(a.articulo())) + " (" + DIFERENCIA.format(d) + ")";
                })
                .toList();
        long enBlanco = articulos.size() - contados.size();

        VerticalLayout cuerpo = new VerticalLayout();
        cuerpo.setPadding(false);
        cuerpo.setSpacing(false);
        cuerpo.add(new Span(contados.size() + (contados.size() == 1 ? " artículo contado" : " artículos contados")
                + (enBlanco > 0 ? "; " + enBlanco + " en blanco quedan para después." : ".")));
        if (diferencias.isEmpty()) {
            cuerpo.add(new Span("Todo cuadra con el sistema: no se hará ningún ajuste."));
        } else {
            cuerpo.add(new Span("Se ajustará el inventario de " + diferencias.size()
                    + (diferencias.size() == 1 ? " artículo:" : " artículos:")));
            diferencias.forEach(t -> cuerpo.add(new Span("• " + t)));
        }

        ConfirmDialog dlg = new ConfirmDialog();
        dlg.setHeader("Guardar conteo de " + almacen.getNombre());
        dlg.setText(cuerpo);
        dlg.setCancelable(true);
        dlg.setCancelText("Revisar");
        dlg.setConfirmText(diferencias.isEmpty() ? "Guardar" : "Guardar y ajustar");
        dlg.addConfirmListener(e -> guardar(almacen, contados));
        dlg.open();
    }

    private void guardar(Almacen almacen, List<ArticuloAContar> contados) {
        List<LineaConteo> lineas = contados.stream()
                .map(a -> new LineaConteo(a.articulo(), contado.get(a.articulo())))
                .toList();
        try {
            ResultadoConteo r = conteoService.guardar(almacen.getCodigo(), lineas, "ADMIN");
            String ajustes = java.util.stream.Stream.of(
                    r.ajusteIncremento() != null ? "incremento #" + r.ajusteIncremento() : null,
                    r.ajusteDisminucion() != null ? "disminución #" + r.ajusteDisminucion() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.joining(" y "));
            Notification ok = Notification.show("Conteo #" + r.conteo() + " guardado: " + r.articulos()
                    + (r.articulos() == 1 ? " artículo" : " artículos")
                    + (r.conDiferencia() == 0 ? ", todo cuadra." : ", " + r.conDiferencia()
                    + " con diferencia (ajuste " + ajustes + ")."), 6000, Notification.Position.TOP_CENTER);
            ok.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            contados.forEach(a -> contado.remove(a.articulo()));
            cargar(false);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Notification err = Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            err.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (RuntimeException ex) {
            log.error("Error al guardar el conteo", ex);
            Notification err = Notification.show("No se pudo guardar el conteo: " + ex.getMessage(), 6000,
                    Notification.Position.MIDDLE);
            err.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
