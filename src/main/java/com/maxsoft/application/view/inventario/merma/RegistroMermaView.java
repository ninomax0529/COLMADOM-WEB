package com.maxsoft.application.view.inventario.merma;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Merma;
import com.maxsoft.application.modelo.TipoMerma;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.MermaService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMermaService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.EmpaqueSeleccionDialogo;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@PermitAll
@PageTitle("Registro de Merma")
@Route(value = "inventario/registroMerma")
public class RegistroMermaView extends VerticalLayout {

    private static final String USUARIO_ACTUAL = "ADMIN";

    private static final DecimalFormat FORMATO_NUMERO = new DecimalFormat("#,##0.00");

    private final Grid<DetalleMerma> grid = new Grid<>(DetalleMerma.class, false);
    private Editor<DetalleMerma> editor;

    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén");
    private final ComboBox<TipoMerma> cbTipoMerma = new ComboBox<>("Tipo de Merma");
    private final TextField txtObservacion = new TextField("Observación");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final MermaService mermaService;
    private final TipoMermaService tipoMermaService;
    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService;
    private final AlmacenService almacenService;
    private final ArticuloAlmacenService existenciaService;

    private final List<DetalleMerma> listDet = new ArrayList<>();

    // Existencia del almacén (en unidad base) al momento de agregar cada línea.
    // Solo se usa para mostrar "existencia actual / nueva existencia".
    private final Map<DetalleMerma, BigDecimal> existenciaPrevia = new IdentityHashMap<>();

    private Grid.Column<DetalleMerma> colTotal;

    @Autowired
    public RegistroMermaView(MermaService mermaService,
            ArticuloService articuloService,
            ArticuloEmpaqueService articuloEmpaqueService,
            AlmacenService almacenService,
            ArticuloAlmacenService existenciaService,
            TipoMermaService tipoMermaService) {

        this.mermaService = mermaService;
        this.tipoMermaService = tipoMermaService;
        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;
        this.almacenService = almacenService;
        this.existenciaService = existenciaService;

        setSizeFull();
        configurarBotonera();
        configurarControlesSuperiores();
        configurarGridDetalle();

        HorizontalLayout hlDatos = new HorizontalLayout(dpFecha, cbAlmacen, cbTipoMerma, txtObservacion, botonera);
        hlDatos.setAlignItems(Alignment.BASELINE);
        hlDatos.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout hlArticulo = new HorizontalLayout(txtBuscar, btnNuevo);
        hlArticulo.setWidthFull();

        add(hlDatos, hlArticulo, grid);
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> procesarGuardado());
        botonera.getCancelar().addClickListener(e -> UI.getCurrent().navigate(MermaView.class));
    }

    private void configurarControlesSuperiores() {

        dpFecha.setValue(LocalDate.now());

        cbAlmacen.setItems(this.almacenService.getLista());
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setAllowCustomValue(false);
        cbAlmacen.setRequired(true);
        cbAlmacen.addValueChangeListener(e -> cambiarAlmacen(e.getValue()));

        cbTipoMerma.setItems(this.tipoMermaService.getListaActivos());
        cbTipoMerma.setItemLabelGenerator(TipoMerma::getDescripcion);
        cbTipoMerma.setAllowCustomValue(false);
        cbTipoMerma.setRequired(true);
        cbTipoMerma.setPlaceholder("Seleccione el tipo...");

        txtObservacion.setWidth("260px");
        txtObservacion.setPlaceholder("Motivo o detalle de la merma");

        btnNuevo = new Button("Artículo (F2)", VaadinIcon.PLUS.create(), event -> abrirDialogoSeleccionArticulo());
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickShortcut(Key.F2);

        txtBuscar.setWidth("50%");
        txtBuscar.setPlaceholder("Filtrar por código o descripción...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
    }

    private void cambiarAlmacen(Almacen nuevoAlmacen) {

        if (nuevoAlmacen == null) {
            return;
        }

        for (DetalleMerma det : listDet) {
            existenciaPrevia.put(det, obtenerExistencia(det.getArticulo(), nuevoAlmacen));
        }

        grid.getDataProvider().refreshAll();
    }

    // ============================================================
    // EXISTENCIAS
    // ============================================================
    private BigDecimal obtenerExistencia(Articulo articulo, Almacen almacen) {

        if (articulo == null || almacen == null) {
            return BigDecimal.ZERO;
        }

        return existenciaService.buscarPorArticuloYAlmacen(articulo.getCodigo(), almacen.getCodigo())
                .map(a -> a.getExistencia() != null ? a.getExistencia() : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal factorDe(ArticuloEmpaque empaque) {

        if (empaque == null) {
            return BigDecimal.ONE;
        }
        BigDecimal f = empaque.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    private BigDecimal cantidadBase(DetalleMerma d) {

        BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
        return cant.multiply(factorDe(d.getArticuloEmpaque()));
    }

    /**
     * Cantidad en unidad base del mismo artículo en las OTRAS líneas.
     */
    private BigDecimal baseEnOtrasLineas(Articulo articulo, DetalleMerma excluir) {

        return listDet.stream()
                .filter(d -> d != excluir && d.getArticulo() != null
                && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo()))
                .map(this::cantidadBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nuevaExistencia(DetalleMerma d) {

        BigDecimal previa = existenciaPrevia.getOrDefault(d, BigDecimal.ZERO);
        return previa.subtract(cantidadBase(d));
    }

    // ============================================================
    // ARTÍCULOS
    // ============================================================
    private void abrirDialogoSeleccionArticulo() {

        if (cbAlmacen.getValue() == null) {
            ClaseUtil.mostrarNotificacion("Debe seleccionar un Almacén antes de agregar artículos", NotificationVariant.LUMO_WARNING);
            cbAlmacen.focus();
            return;
        }

        try {
            ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloService, articulo -> {
                if (articulo != null) {
                    new EmpaqueSeleccionDialogo(
                            articulo,
                            articuloEmpaqueService,
                            empaque -> {
                                if (empaque != null) {
                                    agregarOActualizar(articulo, empaque);
                                }
                            }
                    ).open();
                }
            });
            dialog.open();
        } catch (Exception e) {
            Notification.show("Error abriendo diálogo de artículos: " + e.getMessage(), 3000, Position.TOP_CENTER);
        }
    }

    private void agregarOActualizar(Articulo articulo, ArticuloEmpaque empaque) {

        if (articulo == null || empaque == null) {
            Notification.show("Seleccione un artículo y empaque válidos", 3000, Position.TOP_CENTER);
            return;
        }

        DetalleMerma existente = listDet.stream()
                .filter(d -> d.getArticulo() != null
                && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())
                && d.getArticuloEmpaque() != null
                && Objects.equals(d.getArticuloEmpaque().getCodigo(), empaque.getCodigo()))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            BigDecimal actual = existente.getCantidad() != null ? existente.getCantidad() : BigDecimal.ZERO;
            if (validarYActualizar(existente, actual.add(BigDecimal.ONE), empaque)) {
                grid.getDataProvider().refreshItem(existente);
                actualizarTotales();
            }
            return;
        }

        DetalleMerma det = new DetalleMerma();
        
        det.setCodigo(empaque.getCodigo());
        det.setArticulo(articulo);
        det.setDescripcionArticulo(articulo.getDescripcion());
        det.setArticuloEmpaque(empaque);

        existenciaPrevia.put(det, obtenerExistencia(articulo, cbAlmacen.getValue()));

        if (!validarYActualizar(det, BigDecimal.ONE, empaque)) {
            existenciaPrevia.remove(det);
            return;
        }

        listDet.add(det);
        grid.getDataProvider().refreshAll();
        actualizarTotales();
    }

    /**
     * Valida el stock y aplica cantidad, empaque, factor, costo y total a la
     * línea. Es el único lugar donde se recalcula una fila.
     */
    private boolean validarYActualizar(DetalleMerma det, BigDecimal cantidad, ArticuloEmpaque empaque) {

        if (cantidad == null || cantidad.signum() <= 0) {
            Notification.show("La cantidad de la merma debe ser mayor a cero.", 3000, Position.MIDDLE);
            return false;
        }

        Articulo articulo = det.getArticulo();
        BigDecimal factor = factorDe(empaque);
        BigDecimal base = cantidad.multiply(factor);

        if (articulo != null && Boolean.TRUE.equals(articulo.getInventariable())) {
            BigDecimal stock = obtenerExistencia(articulo, cbAlmacen.getValue());
            BigDecimal otras = baseEnOtrasLineas(articulo, det);

            if (otras.add(base).compareTo(stock) > 0) {
                Notification.show(
                        String.format("Stock insuficiente en '%s'. Disponible (UMM): %s | En otras líneas (UMM): %s",
                                cbAlmacen.getValue() != null ? cbAlmacen.getValue().getNombre() : "",
                                FORMATO_NUMERO.format(stock), FORMATO_NUMERO.format(otras)),
                        4500, Position.MIDDLE);
                return false;
            }
        }

        det.setCantidad(cantidad);
        det.setArticuloEmpaque(empaque);
        det.setFactorConversion(factor);

        if (empaque != null && empaque.getUnidadEmpaque() != null) {
            det.setNombreUnidad(empaque.getUnidadEmpaque().getDescripcion());
        }

        BigDecimal costo = costoDe(articulo, empaque, factor);
        det.setCostoUnitario(costo);
        det.setTotal(cantidad.multiply(costo));

        return true;
    }

    /**
     * Costo de la presentación: el costo propio del empaque; si no lo tiene,
     * costo base del artículo x factor.
     */
    private BigDecimal costoDe(Articulo articulo, ArticuloEmpaque empaque, BigDecimal factor) {

        if (empaque != null && empaque.getPrecioCompra() != null) {
            return empaque.getPrecioCompra();
        }

        BigDecimal base = (articulo != null && articulo.getPrecioCompra() != null)
                ? articulo.getPrecioCompra() : BigDecimal.ZERO;

        return base.multiply(factor);
    }

    // ============================================================
    // GRID
    // ============================================================
    private void configurarGridDetalle() {

        grid.setHeightFull();
        grid.setWidthFull();

        GridListDataView<DetalleMerma> dataView = grid.setItems(listDet);

        grid.addColumn(DetalleMerma::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true);

        grid.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : d.getNombreUnidad())
                .setHeader("Empaque / Presentación")
                .setKey("empaque")
                .setAutoWidth(true);

        grid.addColumn(d -> numero(d.getCantidad()))
                .setHeader("Cantidad Merma")
                .setKey("cantidad")
                .setAutoWidth(true);

        grid.addColumn(d -> numero(existenciaPrevia.getOrDefault(d, BigDecimal.ZERO)))
                .setHeader("Existencia Actual (UMM)")
                .setAutoWidth(true);

        grid.addColumn(d -> numero(nuevaExistencia(d)))
                .setHeader("Nueva Existencia (UMM)")
                .setAutoWidth(true);

        grid.addColumn(d -> numero(d.getCostoUnitario()))
                .setHeader("Costo Unit.")
                .setAutoWidth(true);

        colTotal = grid.addColumn(d -> numero(d.getTotal()))
                .setHeader("Valor Pérdida")
                .setAutoWidth(true)
                .setFooter("TOTAL: 0.00");

        grid.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' de la merma?",
                        () -> {
                            listDet.remove(item);
                            existenciaPrevia.remove(item);
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

        txtBuscar.addValueChangeListener(e -> dataView.setFilter(det -> {
            String term = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            if (term.isEmpty()) {
                return true;
            }
            boolean matchDesc = det.getDescripcionArticulo() != null
                    && det.getDescripcionArticulo().toLowerCase().contains(term);
            boolean matchCod = det.getArticulo() != null && det.getArticulo().getCodigo() != null
                    && det.getArticulo().getCodigo().toString().contains(term);
            return matchDesc || matchCod;
        }));

        configurarEditorInLine();
    }

    private void configurarEditorInLine() {

        editor = grid.getEditor();
        editor.setBuffered(false);

        BigDecimalField cantidadField = new BigDecimalField();
        cantidadField.setWidthFull();

        ComboBox<ArticuloEmpaque> cbEmpaqueEditor = new ComboBox<>();
        cbEmpaqueEditor.setItemLabelGenerator(ae -> ae.getUnidadEmpaque() != null ? ae.getUnidadEmpaque().getDescripcion() : "");
        cbEmpaqueEditor.setWidthFull();

        grid.getColumnByKey("cantidad").setEditorComponent(cantidadField);
        grid.getColumnByKey("empaque").setEditorComponent(cbEmpaqueEditor);

        grid.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel();
            }

            DetalleMerma seleccionado = event.getItem();

            if (seleccionado != null && seleccionado.getArticulo() != null) {
                List<ArticuloEmpaque> opciones = articuloEmpaqueService.getPorArticulo(seleccionado.getArticulo().getCodigo());
                cbEmpaqueEditor.setItems(opciones != null ? opciones : new ArrayList<>());
                cbEmpaqueEditor.setValue(seleccionado.getArticuloEmpaque());
                cantidadField.setValue(seleccionado.getCantidad());
            }

            editor.editItem(seleccionado);
        });

        cbEmpaqueEditor.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null || e.getValue() == null) {
                return;
            }

            DetalleMerma actual = editor.getItem();

            if (validarYActualizar(actual, actual.getCantidad(), e.getValue())) {
                grid.getDataProvider().refreshItem(actual);
                actualizarTotales();
            } else {
                cbEmpaqueEditor.setValue(actual.getArticuloEmpaque());
            }
        });

        cantidadField.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleMerma actual = editor.getItem();
            BigDecimal cant = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;

            if (validarYActualizar(actual, cant, actual.getArticuloEmpaque())) {
                grid.getDataProvider().refreshItem(actual);
                actualizarTotales();
            } else {
                cantidadField.setValue(actual.getCantidad());
            }
        });
    }

    private void actualizarTotales() {

        BigDecimal total = listDet.stream()
                .map(d -> d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        colTotal.setFooter("TOTAL: " + FORMATO_NUMERO.format(total));
    }

    // ============================================================
    // GUARDADO
    // ============================================================
    private void procesarGuardado() {

        if (cbAlmacen.getValue() == null) {
            Notification.show("Debe seleccionar un Almacén", 3000, Position.TOP_CENTER);
            cbAlmacen.focus();
            return;
        }

        if (cbTipoMerma.getValue() == null) {
            Notification.show("Debe seleccionar el Tipo de Merma", 3000, Position.TOP_CENTER);
            cbTipoMerma.focus();
            return;
        }

        if (dpFecha.getValue() == null) {
            Notification.show("Debe indicar la fecha", 3000, Position.TOP_CENTER);
            dpFecha.focus();
            return;
        }

        if (listDet.isEmpty()) {
            Notification.show("La merma no tiene artículos registrados", 3000, Position.TOP_CENTER);
            return;
        }

        try {
            
            Merma merma = new Merma();
            
            merma.setFechaRegistro(new Date());
            merma.setFecha(ClaseUtil.asDate(dpFecha.getValue()));
            merma.setAlmacen(cbAlmacen.getValue());
            merma.setTipoMerma(cbTipoMerma.getValue());
            merma.setObservacion(txtObservacion.getValue());
            merma.setUsuario(new Usuario(1));

            Merma guardada = mermaService.procesarMerma(merma, new ArrayList<>(listDet), USUARIO_ACTUAL);

            String salida = guardada.getSalidaInventario() != null
                    ? " Salida de inventario #" + guardada.getSalidaInventario().getCodigo() + " generada."
                    : "";

            Notification.show("Merma #" + guardada.getCodigo() + " (" + guardada.getTipoMerma().getDescripcion() + ") registrada." + salida,
                    4000, Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            limpiarFormulario();

        } catch (IllegalStateException | IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 5000, Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error al guardar la merma: " + e.getMessage(), 4000, Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarFormulario() {

        listDet.clear();
        existenciaPrevia.clear();
        txtObservacion.clear();
        cbTipoMerma.clear();
        dpFecha.setValue(LocalDate.now());
        grid.getDataProvider().refreshAll();
        actualizarTotales();
    }

    private String numero(BigDecimal valor) {

        return FORMATO_NUMERO.format(valor == null ? BigDecimal.ZERO : valor);
    }
}
