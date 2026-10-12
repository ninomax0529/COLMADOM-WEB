package com.maxsoft.application.view.inventario.traslado;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.TrasladoInventario;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.AlmacenRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.TrasladoInventarioService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@PermitAll
@PageTitle("Traslado de Inventario | MaxSoft ERP")
@Route(value = "inventario/registroTraslado")
public class TrasladoInventarioView extends VerticalLayout {

    // Servicios / Repositorios
    private final TrasladoInventarioService trasladoService;
    private final AlmacenRepo almacenRepo;
    private final ArticuloAlmacenService articuloAlmacenService;
    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService;

    // Componentes de la interfaz
    private ComboBox<Almacen> cbAlmacenOrigen;
    private ComboBox<Almacen> cbAlmacenDestino;
    private DatePicker dpFecha;
    private TextField txtObservacion;
    private TextField txtBuscar;
    private Button btnAgregarArticulo;
    private Button btnProcesar;
    private Button btnLimpiar;

    // Grid y Editor in-line
    private Grid<DetalleTrasladoInventario> gridDetalle;
    private Editor<DetalleTrasladoInventario> editor;
    private final List<DetalleTrasladoInventario> listaDetalles = new ArrayList<>();

    @Autowired
    public TrasladoInventarioView(TrasladoInventarioService trasladoService,
            AlmacenRepo almacenRepo,
            ArticuloAlmacenService articuloAlmacenService,
            ArticuloService articuloService,
            ArticuloEmpaqueService articuloEmpaqueService) {
        this.trasladoService = trasladoService;
        this.almacenRepo = almacenRepo;
        this.articuloAlmacenService = articuloAlmacenService;
        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Construir la interfaz
        add(new H2("Transferencia / Traslado de Inventario"));
        add(crearFormularioCabecera());
        add(crearControlesArticulo());
        add(crearGridDetalle());
        add(crearBotonesAccion());

        // Cargar datos iniciales
        cargarAlmacenes();
    }

    private FormLayout crearFormularioCabecera() {

        FormLayout formCabecera = new FormLayout();

        dpFecha = new DatePicker("Fecha");
        dpFecha.setValue(LocalDate.now());

        cbAlmacenOrigen = new ComboBox<>("Almacén Origen");
        cbAlmacenOrigen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenOrigen.setRequired(true);

        cbAlmacenDestino = new ComboBox<>("Almacén Destino");
        cbAlmacenDestino.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenDestino.setRequired(true);

        cbAlmacenOrigen.addValueChangeListener(e -> {
            validarAlmacenes();
            actualizarStockGridOrigen(e.getValue());
        });

        cbAlmacenDestino.addValueChangeListener(e -> validarAlmacenes());

        txtObservacion = new TextField("Observación / Motivo");
        txtObservacion.setPlaceholder("Ingrese detalles adicionales del traslado...");

        formCabecera.add(dpFecha, cbAlmacenOrigen, cbAlmacenDestino, txtObservacion);
        formCabecera.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 4)
        );

        return formCabecera;
    }

    private HorizontalLayout crearControlesArticulo() {

        HorizontalLayout hlArticulo = new HorizontalLayout();
        hlArticulo.setWidthFull();
        hlArticulo.setAlignItems(Alignment.BASELINE);

        txtBuscar = new TextField();
        txtBuscar.setWidth("50%");
        txtBuscar.setPlaceholder("Filtrar por código o descripción...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);

        btnAgregarArticulo = new Button("Artículo (F2)", VaadinIcon.PLUS.create(), e -> abrirDialogoSeleccionArticulo());
        btnAgregarArticulo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarArticulo.addClickShortcut(Key.F2);

        hlArticulo.add(txtBuscar, btnAgregarArticulo);
        return hlArticulo;
    }

    private BigDecimal obtenerExistenciaPorAlmacen(Articulo articulo, Almacen almacen) {
        if (articulo == null || almacen == null) {
            return BigDecimal.ZERO;
        }
        return articuloAlmacenService.buscarPorArticuloYAlmacen(articulo.getCodigo(), almacen.getCodigo())
                .map(a -> a.getExistencia() != null ? a.getExistencia() : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal obtenerFactorConversion(ArticuloEmpaque empaque) {
        if (empaque == null) {
            return BigDecimal.ONE;
        }
        BigDecimal factor = empaque.getFactorConversion();
        if (factor == null || factor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return factor;
    }

    private void actualizarStockGridOrigen(Almacen nuevoOrigen) {
        if (nuevoOrigen == null) {
            return;
        }

        for (DetalleTrasladoInventario det : listaDetalles) {
            BigDecimal stockBaseEnAlmacen = obtenerExistenciaPorAlmacen(det.getArticulo(), nuevoOrigen);
            det.setExistenciaAnterior(stockBaseEnAlmacen);
            recalcularExistenciaFila(det);
        }

        gridDetalle.getDataProvider().refreshAll();
    }

    private void recalcularExistenciaFila(DetalleTrasladoInventario det) {
        BigDecimal factor = obtenerFactorConversion(det.getArticuloEmpaque());
        BigDecimal cantidadEmpaque = det.getCantidadEnviada() != null ? det.getCantidadEnviada() : BigDecimal.ZERO;

        // Cantidad equivalente en la unidad base del sistema
        BigDecimal cantidadEnUnidadBase = cantidadEmpaque.multiply(factor);
        det.setCantidadFisicaBase(cantidadEnUnidadBase);

        BigDecimal existenciaAnteriorBase = det.getExistenciaAnterior() != null ? det.getExistenciaAnterior() : BigDecimal.ZERO;

        // Nueva existencia restante en almacén origen tras el despacho/traslado
        det.setCantidadFisicaBase(existenciaAnteriorBase.subtract(cantidadEnUnidadBase));
    }

    private void abrirDialogoSeleccionArticulo() {

        if (cbAlmacenOrigen.getValue() == null) {
            mostrarNotificacion("Debe seleccionar un Almacén de Origen primero", NotificationVariant.LUMO_WARNING);
            cbAlmacenOrigen.focus();
            return;
        }

        try {
            ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloService, articulo -> {
                if (articulo != null) {
                    procesarSeleccionArticulo(articulo);
                }
            });
            dialog.open();
        } catch (Exception e) {
            Notification.show("Error abriendo diálogo de artículos: " + e.getMessage(), 3000, Position.TOP_CENTER);
        }
    }

    private void procesarSeleccionArticulo(Articulo articulo) {
        List<ArticuloEmpaque> listaEmpaques = articuloEmpaqueService.getPorArticulo(articulo.getCodigo());

        if (listaEmpaques != null && listaEmpaques.size() > 1) {
            mostrarDialogoSeleccionEmpaque(articulo, listaEmpaques);
        } else {
            ArticuloEmpaque empaqueUnico = (listaEmpaques != null && !listaEmpaques.isEmpty()) ? listaEmpaques.get(0) : null;
            agregarOActualizarArticulo(articulo, empaqueUnico);
        }
    }

    private void mostrarDialogoSeleccionEmpaque(Articulo articulo, List<ArticuloEmpaque> empaques) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleccione Empaque / Presentación");

        ComboBox<ArticuloEmpaque> cbEmpaque = new ComboBox<>("Empaque Disponible");
        cbEmpaque.setItems(empaques);
        cbEmpaque.setItemLabelGenerator(ae -> ae.getUnidadEmpaque() != null
                ? ae.getUnidadEmpaque().getDescripcion() + " (Factor: " + obtenerFactorConversion(ae) + ")"
                : "Empaque " + ae.getCodigo());
        cbEmpaque.setWidthFull();
        cbEmpaque.setValue(empaques.get(0));

        Button btnAceptar = new Button("Agregar", e -> {
            ArticuloEmpaque empaqueSeleccionado = cbEmpaque.getValue();
            if (empaqueSeleccionado != null) {
                dialog.close();
                agregarOActualizarArticulo(articulo, empaqueSeleccionado);
            } else {
                Notification.show("Seleccione un empaque válido", 2500, Position.MIDDLE);
            }
        });
        btnAceptar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout hlAcciones = new HorizontalLayout(btnAceptar, btnCancelar);
        hlAcciones.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout layout = new VerticalLayout(
                new TextField("Artículo", articulo.getDescripcion(), ""),
                cbEmpaque,
                hlAcciones
        );
        layout.setPadding(true);
        dialog.add(layout);
        dialog.open();
    }

    private void agregarOActualizarArticulo(Articulo articulo, ArticuloEmpaque empaqueSeleccionado) {
        if (articulo == null) {
            return;
        }

        Almacen origen = cbAlmacenOrigen.getValue();
        BigDecimal stockDisponibleBase = obtenerExistenciaPorAlmacen(articulo, origen);

        if (Boolean.TRUE.equals(articulo.getInventariable())) {
            BigDecimal cantidadYaEnGridBase = calcularCantidadTotalBaseEnGrid(articulo, null);

            if (cantidadYaEnGridBase.compareTo(stockDisponibleBase) >= 0) {
                Notification.show(
                        String.format("Stock insuficiente en '%s' para '%s'. Disponible (UMM): %.2f | En lista (UMM): %.2f",
                                origen.getNombre(), articulo.getDescripcion(), stockDisponibleBase, cantidadYaEnGridBase),
                        5000, Position.MIDDLE
                );
                return;
            }
        }

        DetalleTrasladoInventario det = new DetalleTrasladoInventario();

        det.setCodigo(empaqueSeleccionado != null ? empaqueSeleccionado.getCodigo() : articulo.getCodigo());
        det.setArticulo(articulo);
        det.setDescripcionArticulo(articulo.getDescripcion());
        det.setArticuloEmpaque(empaqueSeleccionado);

        if (empaqueSeleccionado != null && empaqueSeleccionado.getUnidadEmpaque() != null) {
            det.setUnidad(empaqueSeleccionado.getUnidadEmpaque());
            det.setNombreUnidad(empaqueSeleccionado.getUnidadEmpaque().getDescripcion());
        } else if (articulo.getUnidadBase() != null) {
            det.setUnidad(articulo.getUnidadBase());
            det.setNombreUnidad(articulo.getUnidadBase().getDescripcion());
        } else {
            det.setNombreUnidad("Empaque");
        }

        BigDecimal factor = obtenerFactorConversion(empaqueSeleccionado);
        det.setFactorConversion(factor);

        BigDecimal precio = (empaqueSeleccionado != null && empaqueSeleccionado.getPrecioCompra() != null)
                ? empaqueSeleccionado.getPrecioCompra()
                : (articulo.getPrecioCompra() != null ? articulo.getPrecioCompra() : BigDecimal.ZERO);

        det.setPrecioCompra(precio);
        det.setCostoUnitario(precio);

        det.setExistenciaAnterior(stockDisponibleBase);

        // ✅ Asignar cantidad predeterminada en 1 para evitar enviar 0 al servicio
        BigDecimal cantidadInicial = BigDecimal.ONE;
        det.setCantidadEnviada(cantidadInicial);

        // ✅ Calcular cantidad física base en UMM
        BigDecimal cantidadEnBaseUMM = cantidadInicial.multiply(factor);
        det.setCantidadFisicaBase(cantidadEnBaseUMM);

        // ✅ Recalcular la proyección de nueva existencia (Anterior - Consumo UMM)
        recalcularExistenciaFila(det);

        listaDetalles.add(det);
        gridDetalle.getDataProvider().refreshAll();
    }

    private BigDecimal calcularCantidadTotalBaseEnGrid(Articulo articulo, DetalleTrasladoInventario itemExcluir) {
        return listaDetalles.stream()
                .filter(d -> !d.equals(itemExcluir) && d.getArticulo() != null && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo()))
                .map(d -> {
                    BigDecimal cant = d.getCantidadEnviada() != null ? d.getCantidadEnviada() : BigDecimal.ZERO;
                    BigDecimal factor = obtenerFactorConversion(d.getArticuloEmpaque());
                    return cant.multiply(factor);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Grid<DetalleTrasladoInventario> crearGridDetalle() {

        gridDetalle = new Grid<>(DetalleTrasladoInventario.class, false);
        gridDetalle.setWidthFull();
        gridDetalle.setHeight("350px");

        GridListDataView<DetalleTrasladoInventario> dataView = gridDetalle.setItems(listaDetalles);

        gridDetalle.addColumn(DetalleTrasladoInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true);

        gridDetalle.addColumn(item -> item.getArticuloEmpaque() != null && item.getArticuloEmpaque().getUnidadEmpaque() != null
                ? item.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : item.getNombreUnidad())
                .setHeader("Empaque / Presentación")
                .setKey("empaque")
                .setAutoWidth(true);

        gridDetalle.addColumn(DetalleTrasladoInventario::getCantidadEnviada)
                .setHeader("Cantidad Traslado")
                .setKey("cantidad")
                .setAutoWidth(true);

        gridDetalle.addColumn(DetalleTrasladoInventario::getExistenciaAnterior)
                .setHeader("Existencia Actual (UMM)")
                .setAutoWidth(true);

        gridDetalle.addColumn(DetalleTrasladoInventario::getCantidadFisicaBase)
                .setHeader("Nueva Existencia (UMM)")
                .setAutoWidth(true);
        gridDetalle.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' del traslado?",
                        () -> {
                            listaDetalles.remove(item);
                            gridDetalle.getDataProvider().refreshAll();
                        },
                        () -> {
                        }
                );
                dialog.open();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return deleteButton;
        })).setHeader("Acciones").setAutoWidth(true);

        txtBuscar.addValueChangeListener(e -> dataView.addFilter(det -> {
            String term = e.getValue().trim().toLowerCase();
            if (term.isEmpty()) {
                return true;
            }

            boolean matchDesc = det.getDescripcionArticulo() != null && det.getDescripcionArticulo().toLowerCase().contains(term);
            boolean matchCod = det.getCodigo() != null && det.getCodigo().toString().contains(term);
            return matchDesc || matchCod;
        }));

        configurarEditorInLine();

        return gridDetalle;
    }

    private void configurarEditorInLine() {

        editor = gridDetalle.getEditor();
        editor.setBuffered(false);

        BigDecimalField cantidadField = new BigDecimalField();
        cantidadField.setWidthFull();

        ComboBox<ArticuloEmpaque> cbEmpaqueEditor = new ComboBox<>();
        cbEmpaqueEditor.setItemLabelGenerator(ae -> ae.getUnidadEmpaque() != null ? ae.getUnidadEmpaque().getDescripcion() : "");
        cbEmpaqueEditor.setWidthFull();

        gridDetalle.getColumnByKey("cantidad").setEditorComponent(cantidadField);
        gridDetalle.getColumnByKey("empaque").setEditorComponent(cbEmpaqueEditor);

        gridDetalle.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel();
            }

            DetalleTrasladoInventario itemSeleccionado = event.getItem();

            if (itemSeleccionado != null && itemSeleccionado.getArticulo() != null) {

                List<ArticuloEmpaque> opcionesEmpaque = articuloEmpaqueService.getPorArticulo(itemSeleccionado.getArticulo().getCodigo());
                cbEmpaqueEditor.setItems(opcionesEmpaque != null ? opcionesEmpaque : new ArrayList<>());
                cbEmpaqueEditor.setValue(itemSeleccionado.getArticuloEmpaque());
            }

            editor.editItem(itemSeleccionado);
        });

        cbEmpaqueEditor.addValueChangeListener(e -> {

            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleTrasladoInventario itemActual = editor.getItem();
            ArticuloEmpaque nuevoEmpaque = e.getValue();
            if (nuevoEmpaque == null) {
                return;
            }

            if (validarYActualizarDetalle(itemActual, itemActual.getCantidadEnviada(), nuevoEmpaque)) {
                gridDetalle.getDataProvider().refreshItem(itemActual);
            } else {
                cbEmpaqueEditor.setValue(itemActual.getArticuloEmpaque());
            }
        });

        cantidadField.addValueChangeListener(e -> {

            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleTrasladoInventario itemActual = editor.getItem();
            BigDecimal cant = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;

            if (cant.compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show("La cantidad debe ser mayor a cero", 2500, Position.MIDDLE);
                return;
            }

            if (validarYActualizarDetalle(itemActual, cant, itemActual.getArticuloEmpaque())) {
                gridDetalle.getDataProvider().refreshItem(itemActual);
            } else {
                cantidadField.setValue(itemActual.getCantidadEnviada());
            }
        });
    }

    private boolean validarYActualizarDetalle(DetalleTrasladoInventario itemActual, BigDecimal nuevaCantidad, ArticuloEmpaque nuevoEmpaque) {
        if (nuevaCantidad == null || nuevaCantidad.compareTo(BigDecimal.ZERO) <= 0) {
            Notification.show("La cantidad del traslado debe ser mayor a cero.", 3000, Position.MIDDLE);
            return false;
        }

        Articulo articulo = itemActual.getArticulo();
        BigDecimal factor = obtenerFactorConversion(nuevoEmpaque);
        BigDecimal cantidadEnBase = nuevaCantidad.multiply(factor);

        if (articulo != null && Boolean.TRUE.equals(articulo.getInventariable())) {
            Almacen almacenOrigen = cbAlmacenOrigen.getValue();
            BigDecimal stockDisponibleBase = obtenerExistenciaPorAlmacen(articulo, almacenOrigen);

            BigDecimal cantidadOtrasFilasBase = calcularCantidadTotalBaseEnGrid(articulo, itemActual);

            if (cantidadOtrasFilasBase.add(cantidadEnBase).compareTo(stockDisponibleBase) > 0) {
                Notification.show(
                        String.format("Stock insuficiente en almacén origen '%s'. Disponible UMM: %.2f (En uso en otras filas UMM: %.2f)",
                                almacenOrigen.getNombre(), stockDisponibleBase, cantidadOtrasFilasBase),
                        4000, Position.MIDDLE
                );
                return false;
            }
        }

        itemActual.setCantidadEnviada(nuevaCantidad);
        itemActual.setArticuloEmpaque(nuevoEmpaque);
        itemActual.setFactorConversion(factor);

        if (nuevoEmpaque != null && nuevoEmpaque.getUnidadEmpaque() != null) {
            itemActual.setUnidad(nuevoEmpaque.getUnidadEmpaque());
            itemActual.setNombreUnidad(nuevoEmpaque.getUnidadEmpaque().getDescripcion());
        }

        itemActual.setCantidadFisicaBase(cantidadEnBase);
        recalcularExistenciaFila(itemActual);

        return true;
    }

    private HorizontalLayout crearBotonesAccion() {
        HorizontalLayout layoutAcciones = new HorizontalLayout();

        btnProcesar = new Button("Procesar Traslado", VaadinIcon.CHECK.create());
        btnProcesar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnProcesar.addClickListener(e -> {
            try {
                procesarTraslado();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnLimpiar = new Button("Limpiar Formulario", VaadinIcon.REFRESH.create());
        btnLimpiar.addClickListener(e -> limpiarFormulario());

        layoutAcciones.add(btnProcesar, btnLimpiar);
        return layoutAcciones;
    }

    private void cargarAlmacenes() {
        List<Almacen> almacenes = almacenRepo.findAll();
        cbAlmacenOrigen.setItems(almacenes);
        cbAlmacenDestino.setItems(almacenes);
    }

    private void validarAlmacenes() {
        if (cbAlmacenOrigen.getValue() != null && cbAlmacenDestino.getValue() != null) {
            if (cbAlmacenOrigen.getValue().equals(cbAlmacenDestino.getValue())) {
                mostrarNotificacion("El almacén de origen y destino no pueden ser el mismo.", NotificationVariant.LUMO_ERROR);
                cbAlmacenDestino.clear();
            }
        }
    }

    private void procesarTraslado() {

        if (cbAlmacenOrigen.getValue() == null || cbAlmacenDestino.getValue() == null) {
            mostrarNotificacion("Debe seleccionar origen y destino.", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (listaDetalles.isEmpty()) {
            mostrarNotificacion("Debe agregar al menos un ítem al traslado.", NotificationVariant.LUMO_ERROR);
            return;
        }

        for (DetalleTrasladoInventario det : listaDetalles) {
            if (det.getCantidadEnviada() == null || det.getCantidadEnviada().compareTo(BigDecimal.ZERO) <= 0) {
                mostrarNotificacion("El artículo '" + det.getDescripcionArticulo() + "' tiene una cantidad inválida.", NotificationVariant.LUMO_ERROR);
                return;
            }
            if (det.getArticuloEmpaque() == null) {
                mostrarNotificacion("El artículo '" + det.getDescripcionArticulo() + "' debe tener un empaque asignado.", NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        try {

            TrasladoInventario traslado = new TrasladoInventario();
            traslado.setFechaEmision(ClaseUtil.asDate(dpFecha.getValue()));
            traslado.setAlmacenOrigen(cbAlmacenOrigen.getValue());
            traslado.setAlmacenDestino(cbAlmacenDestino.getValue());
            traslado.setObservacion(txtObservacion.getValue());
            traslado.setNumeroDocumento("TR-" + System.currentTimeMillis());
            traslado.setUsuarioEnvia(new Usuario(1));

            listaDetalles.forEach(o -> {
                o.setTraslado(traslado);
                o.setCodigo(null);
            });

            traslado.setDetalleTrasladoInventarioCollection(listaDetalles);

            trasladoService.procesarTraslado(traslado);

            mostrarNotificacion("¡Traslado procesado exitosamente!", NotificationVariant.LUMO_SUCCESS);
            limpiarFormulario();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarNotificacion("Error al procesar traslado: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarFormulario() {
        cbAlmacenOrigen.clear();
        cbAlmacenDestino.clear();
        dpFecha.setValue(LocalDate.now());
        txtObservacion.clear();
        txtBuscar.clear();
        listaDetalles.clear();
        gridDetalle.getDataProvider().refreshAll();
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}
