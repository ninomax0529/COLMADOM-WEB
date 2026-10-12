package com.maxsoft.application.view.inventario.salida;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
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
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PermitAll
@PageTitle("Registro Salida de Inventario")
@Route(value = "inventario/registroSalida")
public class RegistroSalidaDeIventarioView extends VerticalLayout {

    private final Grid<DetalleSalidaInventario> grid = new Grid<>(DetalleSalidaInventario.class, false);
    private Editor<DetalleSalidaInventario> editor;

    private final TextField txtNumDoc = new TextField("Número Salida");
    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén Origen");
    private final TextField txtObservacion = new TextField("Observación");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final ArticuloService articuloService;
    private final SalidaInventarioService salidaInvService;
    private final AlmacenService almacenService;
    private final ArticuloAlmacenService existenciaService;

    private final List<DetalleSalidaInventario> listDet = new ArrayList<>();

    @Autowired
    public RegistroSalidaDeIventarioView(SalidaInventarioService salidaInvService,
            ArticuloService articuloService,
            AlmacenService almacenService,
            ArticuloAlmacenService existenciaService) {

        this.salidaInvService = salidaInvService;
        this.articuloService = articuloService;
        this.almacenService = almacenService;
        this.existenciaService = existenciaService;

        setSizeFull();
        configurarBotonera();
        configurarControlesSuperiores();
        configurarGridDetalle();

        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha, cbAlmacen, txtObservacion, botonera);
        hlDatos.setAlignItems(Alignment.BASELINE);

        HorizontalLayout hlArticulo = new HorizontalLayout(txtBuscar, btnNuevo);
        hlArticulo.setWidthFull();

        add(hlDatos, hlArticulo, grid);
    }

    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> procesarGuardado());
        botonera.getCancelar().addClickListener(e -> UI.getCurrent().navigate(SalidaDeIventarioView.class));
    }

    private void configurarControlesSuperiores() {
        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

        cbAlmacen.setItems(this.almacenService.getLista());
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setAllowCustomValue(false);
        cbAlmacen.setRequired(true);
        cbAlmacen.addValueChangeListener(e -> cambiarAlmacenGeneral(e.getValue()));

        txtObservacion.setWidth("250px");

        btnNuevo = new Button("Artículo (F2)", event -> abrirDialogoSeleccionArticulo());
        btnNuevo.addClickShortcut(Key.F2);

        txtBuscar.setWidth("50%");
        txtBuscar.setPlaceholder("Filtrar por código o descripción...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
    }

    private void cambiarAlmacenGeneral(Almacen nuevoAlmacen) {
        if (nuevoAlmacen == null) {
            return;
        }

        for (DetalleSalidaInventario det : listDet) {
            det.setAlmacen(nuevoAlmacen);
            det.setNombreAlmacen(nuevoAlmacen.getNombre());

            BigDecimal stockBaseEnAlmacen = obtenerExistenciaPorAlmacen(det.getArticulo(), nuevoAlmacen);
            det.setExistenciaAnterior(stockBaseEnAlmacen);

            recalcularExistenciaFila(det);
        }

        grid.getDataProvider().refreshAll();
    }

    private BigDecimal obtenerExistenciaPorAlmacen(Articulo articulo, Almacen almacen) {

        if (articulo == null || almacen == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal stock = existenciaService.buscarPorArticuloYAlmacen(articulo.getCodigo(), almacen.getCodigo())
                .map(e -> e.getExistencia())
                .orElse(BigDecimal.ZERO);
        return stock != null ? stock : BigDecimal.ZERO;
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

    private void recalcularExistenciaFila(DetalleSalidaInventario det) {

        BigDecimal factor = obtenerFactorConversion(det.getArticuloEmpaque());
        BigDecimal cantidadEmpaque = det.getCantidad() != null ? det.getCantidad() : BigDecimal.ZERO;
        BigDecimal cantidadEnUnidadBase = cantidadEmpaque.multiply(factor);

        BigDecimal existenciaAnteriorBase = det.getExistenciaAnterior() != null ? det.getExistenciaAnterior() : BigDecimal.ZERO;
        det.setExistencia(existenciaAnteriorBase.subtract(cantidadEnUnidadBase));
    }

    private void abrirDialogoSeleccionArticulo() {
        if (cbAlmacen.getValue() == null) {
            ClaseUtil.mostrarNotificacion("Debe seleccionar un Almacén de Origen primero", NotificationVariant.LUMO_WARNING);
            cbAlmacen.focus();
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

    // Evalúa si el artículo tiene múltiples empaques para desplegar la ventana de elección
    private void procesarSeleccionArticulo(Articulo articulo) {

        List<ArticuloEmpaque> listaEmpaques
                = articulo.getArticuloEmpaqueCollection().stream().toList();

        if (listaEmpaques != null && listaEmpaques.size() > 1) {

            // Despliega modal con ComboBox para elegir empaque
            mostrarDialogoSeleccionEmpaque(articulo, listaEmpaques);
        } else {
            // Si solo tiene 1 empaque o ninguno, toma el por defecto
            ArticuloEmpaque empaqueUnico = (listaEmpaques != null && !listaEmpaques.isEmpty()) ? listaEmpaques.get(0) : null;
            agregarOActualizarArticulo(articulo, empaqueUnico);
        }
    }

    // Modal/Diálogo flotante con el ComboBox para elegir el empaque deseado
    private void mostrarDialogoSeleccionEmpaque(Articulo articulo, List<ArticuloEmpaque> empaques) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleccione Empaque / Presentación");

        ComboBox<ArticuloEmpaque> cbEmpaque = new ComboBox<>("Empaque Disponible");
        cbEmpaque.setItems(empaques);
        cbEmpaque.setItemLabelGenerator(ae -> ae.getUnidadEmpaque() != null
                ? ae.getUnidadEmpaque().getDescripcion() + " (Factor: " + ae.getFactorConversion() + ")"
                : "Empaque " + ae.getCodigo());
        cbEmpaque.setWidthFull();
        cbEmpaque.setValue(empaques.get(0)); // Primer empaque por defecto

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

        Almacen almacenSeleccionado = cbAlmacen.getValue();
        BigDecimal stockDisponibleBase = obtenerExistenciaPorAlmacen(articulo, almacenSeleccionado);

        if (Boolean.TRUE.equals(articulo.getInventariable())) {
            BigDecimal cantidadYaEnGridBase = calcularCantidadTotalBaseEnGrid(articulo, null);

            if (cantidadYaEnGridBase.compareTo(stockDisponibleBase) >= 0) {
                Notification.show(
                        String.format("Stock insuficiente en '%s' para '%s'. Disponible (UMM): %.2f | En lista (UMM): %.2f",
                                almacenSeleccionado.getNombre(), articulo.getDescripcion(), stockDisponibleBase, cantidadYaEnGridBase),
                        5000, Position.MIDDLE
                );
                return;
            }
        }

        DetalleSalidaInventario det = new DetalleSalidaInventario();

        det.setCodigo(empaqueSeleccionado.getCodigo());
        det.setArticuloEmpaque(empaqueSeleccionado);
        det.setUnidad(empaqueSeleccionado.getUnidadEmpaque());
        det.setFactorConversion(obtenerFactorConversion(empaqueSeleccionado));

        det.setArticulo(articulo);
        det.setDescripcionArticulo(articulo.getDescripcion());
        det.setAlmacen(almacenSeleccionado);
        det.setNombreAlmacen(almacenSeleccionado.getNombre());
        det.setPrecioCompra(empaqueSeleccionado.getPrecioCompra() != null ? empaqueSeleccionado.getPrecioCompra() : BigDecimal.ZERO);

        // 1. Asignar la existencia previa real del almacén (en UMM)
        det.setExistenciaAnterior(stockDisponibleBase);
        det.setCantidad(BigDecimal.ZERO);
        det.setCantidadFisicaBase(BigDecimal.ZERO);

        // 2. Recalcular la nueva existencia disponible (ExistenciaAnterior - (Cantidad * Factor))
        recalcularExistenciaFila(det);

        // 3. Totales económicos iniciales
        det.setSubTotal(BigDecimal.ZERO);
        det.setItbis(BigDecimal.ZERO);
        det.setTotal(BigDecimal.ZERO);

        listDet.add(det);
        grid.getDataProvider().refreshAll();
    }

    private BigDecimal calcularCantidadTotalBaseEnGrid(Articulo articulo, DetalleSalidaInventario itemExcluir) {
        return listDet.stream()
                .filter(d -> !d.equals(itemExcluir) && d.getArticulo() != null && d.getArticulo().getCodigo().equals(articulo.getCodigo()))
                .map(d -> {
                    BigDecimal cant = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
                    BigDecimal factor = obtenerFactorConversion(d.getArticuloEmpaque());
                    return cant.multiply(factor);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void configurarGridDetalle() {

        grid.setHeightFull();
        grid.setWidthFull();

        GridListDataView<DetalleSalidaInventario> dataView = grid.setItems(listDet);

        grid.addColumn(DetalleSalidaInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true);

        grid.addColumn(DetalleSalidaInventario::getNombreAlmacen)
                .setHeader("Almacén")
                .setAutoWidth(true);

        grid.addColumn(item -> item.getArticuloEmpaque() != null && item.getArticuloEmpaque().getUnidadEmpaque() != null
                ? item.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : "")
                .setHeader("Empaque / Presentación")
                .setKey("empaque")
                .setAutoWidth(true);

        grid.addColumn(DetalleSalidaInventario::getCantidad)
                .setHeader("Cantidad Salida")
                .setKey("cantidad")
                .setAutoWidth(true);

        grid.addColumn(DetalleSalidaInventario::getExistenciaAnterior)
                .setHeader("Existencia Actual (UMM)")
                .setAutoWidth(true);

        grid.addColumn(DetalleSalidaInventario::getExistencia)
                .setHeader("Nueva Existencia (UMM)")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(item -> {

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' de la salida?",
                        () -> {
                            listDet.remove(item);
                            grid.getDataProvider().refreshAll();
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

            DetalleSalidaInventario itemSeleccionado = event.getItem();

            if (itemSeleccionado != null && itemSeleccionado.getArticulo() != null) {

                List<ArticuloEmpaque> opcionesEmpaque = itemSeleccionado.getArticulo()
                        .getArticuloEmpaqueCollection().stream().toList();

                cbEmpaqueEditor.setItems(opcionesEmpaque != null ? opcionesEmpaque : new ArrayList<>());
                cbEmpaqueEditor.setValue(itemSeleccionado.getArticuloEmpaque());
            }

            editor.editItem(itemSeleccionado);
        });

        cbEmpaqueEditor.addValueChangeListener(e -> {

            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleSalidaInventario itemActual = editor.getItem();
            ArticuloEmpaque nuevoEmpaque = e.getValue();
            if (nuevoEmpaque == null) {
                return;
            }

            if (validarYActualizarDetalle(itemActual, itemActual.getCantidad(), nuevoEmpaque)) {
                grid.getDataProvider().refreshItem(itemActual);
            } else {
                cbEmpaqueEditor.setValue(itemActual.getArticuloEmpaque());
            }
        });

        cantidadField.addValueChangeListener(e -> {

            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleSalidaInventario itemActual = editor.getItem();
            BigDecimal cant = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;

            if (cant.compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show("La cantidad debe ser mayor a cero", 2500, Position.MIDDLE);
                return;
            }

            if (validarYActualizarDetalle(itemActual, cant, itemActual.getArticuloEmpaque())) {
                grid.getDataProvider().refreshItem(itemActual);
            } else {
                cantidadField.setValue(itemActual.getCantidad());
            }
        });
    }

    private boolean validarYActualizarDetalle(DetalleSalidaInventario itemActual, BigDecimal nuevaCantidad, ArticuloEmpaque nuevoEmpaque) {
        Articulo articulo = itemActual.getArticulo();
        BigDecimal factor = obtenerFactorConversion(nuevoEmpaque);
        BigDecimal cantidadEnBase = (nuevaCantidad != null ? nuevaCantidad : BigDecimal.ZERO).multiply(factor);

        if (articulo != null && Boolean.TRUE.equals(articulo.getInventariable())) {
            Almacen almacenSel = cbAlmacen.getValue();
            BigDecimal stockDisponibleBase = obtenerExistenciaPorAlmacen(articulo, almacenSel);

            BigDecimal cantidadOtrasFilasBase = calcularCantidadTotalBaseEnGrid(articulo, itemActual);

            if (cantidadOtrasFilasBase.add(cantidadEnBase).compareTo(stockDisponibleBase) > 0) {
                Notification.show(
                        String.format("Stock insuficiente en almacén '%s'. Disponible UMM: %.2f (En uso en otras filas UMM: %.2f)",
                                almacenSel.getNombre(), stockDisponibleBase, cantidadOtrasFilasBase),
                        4000, Position.MIDDLE
                );
                return false;
            }
        }

        itemActual.setCantidad(nuevaCantidad);
        itemActual.setArticuloEmpaque(nuevoEmpaque);
        itemActual.setFactorConversion(factor);

        // Recalcular existencia proyectada
        recalcularExistenciaFila(itemActual);

        // Actualizar cantidad física en UMM
        itemActual.setCantidadFisicaBase(cantidadEnBase);

        // Cálculos de montos
        BigDecimal precio = itemActual.getPrecioCompra() != null ? itemActual.getPrecioCompra() : BigDecimal.ZERO;
        BigDecimal subTotal = (nuevaCantidad != null ? nuevaCantidad : BigDecimal.ZERO).multiply(precio);

        double porcentajeItbis = itemActual.getArticulo() != null && itemActual.getArticulo().getItbisGravado() != null
                ? itemActual.getArticulo().getItbisGravado()
                : 0.0;

        BigDecimal itbis = subTotal.multiply(BigDecimal.valueOf(porcentajeItbis / 100.0));

        itemActual.setSubTotal(subTotal);
        itemActual.setItbis(itbis);
        itemActual.setTotal(subTotal.add(itbis));

        return true;
    }

    private void procesarGuardado() {
        if (cbAlmacen.getValue() == null) {
            Notification.show("Debe seleccionar un Almacén de Origen", 3000, Position.TOP_CENTER);
            cbAlmacen.focus();
            return;
        }

        if (listDet.isEmpty()) {
            Notification.show("Debe agregar al menos un artículo a la salida.", 3000, Position.TOP_CENTER);
            return;
        }

        for (DetalleSalidaInventario det : listDet) {
            if (det.getCantidad() == null || det.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show("El artículo '" + det.getDescripcionArticulo() + "' tiene una cantidad inválida.", 4000, Position.TOP_CENTER);
                return;
            }
            if (det.getArticuloEmpaque() == null) {
                Notification.show("El artículo '" + det.getDescripcionArticulo() + "' debe tener un empaque asignado.", 4000, Position.TOP_CENTER);
                return;
            }
        }

        try {
            SalidaInventario salida = new SalidaInventario();
            salida.setFecha(ClaseUtil.asDate(dpFecha.getValue()));
            salida.setFechaRegistro(new Date());
            salida.setObservacion(txtObservacion.getValue());

            listDet.forEach(e -> {
                e.setSalidaInventario(salida);
                e.setAlmacen(cbAlmacen.getValue());
                e.setCodigo(null);
            });

            salida.setDetalleSalidaInventarioCollection(listDet);

            String usuarioActual = "ADMIN";

            SalidaInventario guardada = this.salidaInvService.guardar(salida, usuarioActual);

            Notification.show("Salida #" + guardada.getCodigo() + " procesada y descontada del inventario correctamente.", 3000, Position.TOP_CENTER);

            listDet.clear();
            txtObservacion.clear();
            grid.getDataProvider().refreshAll();

        } catch (IllegalStateException ex) {
            Notification.show(ex.getMessage(), 5000, Position.MIDDLE);
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 4000, Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar la salida: " + e.getMessage(), 4000, Position.TOP_CENTER);
            e.printStackTrace();
        }
    }
}


