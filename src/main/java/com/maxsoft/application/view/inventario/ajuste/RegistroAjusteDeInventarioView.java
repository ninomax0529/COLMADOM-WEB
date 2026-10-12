package com.maxsoft.application.view.inventario.ajuste;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.TipoAjuste;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoAjusteService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
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
import java.util.List;
import java.util.Objects;

@PermitAll
@PageTitle("Registro Ajuste de Inventario Multi-Empaque")
@Route(value = "inventario/registroAjuste")
public class RegistroAjusteDeInventarioView extends VerticalLayout {

    private final Grid<DetalleAjusteInventario> grid = new Grid<>(DetalleAjusteInventario.class, false);
    private Editor<DetalleAjusteInventario> editor;

    private final TextField txtNumDoc = new TextField("Número Ajuste");
    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén");
    private final ComboBox<TipoAjuste> cbTipoAjuste = new ComboBox<>("Tipo de Ajuste");
    private final TextField txtObservacion = new TextField("Observación");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService;
    private final AjusteInventarioService ajusteService;
    private final TipoAjusteService tipoAjusteService;
    private final AlmacenService almacenService;
    private final ArticuloAlmacenService existenciaService;
    private final MovimientoInventarioService movimientoService;
    private final TipoMovimientoService tipoMovimientoService;
    private final TipoDocumentoService tipoDocumentoService;

    private final List<DetalleAjusteInventario> listDet = new ArrayList<>();

    @Autowired
    public RegistroAjusteDeInventarioView(AjusteInventarioService ajusteService,
            ArticuloService articuloService,
            ArticuloEmpaqueService articuloEmpaqueService,
            TipoAjusteService tipoAjusteService,
            AlmacenService almacenService,
            ArticuloAlmacenService existenciaService,
            MovimientoInventarioService movimientoService,
            TipoMovimientoService tipoMovimientoService,
            TipoDocumentoService tipoDocumentoService) {

        this.ajusteService = ajusteService;
        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;
        this.tipoAjusteService = tipoAjusteService;
        this.almacenService = almacenService;
        this.existenciaService = existenciaService;
        this.movimientoService = movimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.tipoMovimientoService = tipoMovimientoService;

        setSizeFull();
        configurarBotonera();
        configurarControlesSuperiores();
        configurarGridDetalle();

        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha, cbAlmacen, cbTipoAjuste, txtObservacion, botonera);
        hlDatos.setAlignItems(Alignment.BASELINE);

        HorizontalLayout hlArticulo = new HorizontalLayout(txtBuscar, btnNuevo);
        hlArticulo.setWidthFull();

        add(hlDatos, hlArticulo, grid);
    }

    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> {
            try {
                procesarGuardado();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        botonera.getCancelar().addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
    }

    private void configurarControlesSuperiores() {
        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

        cbAlmacen.setItems(this.almacenService.getLista());
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setAllowCustomValue(false);
        cbAlmacen.setRequired(true);
        cbAlmacen.addValueChangeListener(e -> cambiarAlmacenGeneral(e.getValue()));

        cbTipoAjuste.setItems(this.tipoAjusteService.getLista());
        cbTipoAjuste.setItemLabelGenerator(TipoAjuste::getDescripcion);
        cbTipoAjuste.setAllowCustomValue(false);
        cbTipoAjuste.setRequired(true);
        cbTipoAjuste.addValueChangeListener(e -> recalcularNuevasExistencias());

        txtObservacion.setWidth("300px");

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

        for (DetalleAjusteInventario det : listDet) {
            det.setAlmacen(nuevoAlmacen);
            det.setNombreAlmacen(nuevoAlmacen.getNombre());

            BigDecimal stockEnAlmacen = obtenerExistenciaPorAlmacen(det.getArticulo(), nuevoAlmacen);
            det.setExistencia(stockEnAlmacen);
            det.setNuevaExistencia(calcularNuevaExistencia(stockEnAlmacen, det.getCantidad(), det.getArticuloEmpaque()));
        }

        grid.getDataProvider().refreshAll();
    }

    private BigDecimal obtenerExistenciaPorAlmacen(Articulo articulo, Almacen almacen) {
        if (articulo == null || almacen == null) {
            return BigDecimal.ZERO;
        }
        return existenciaService.buscarPorArticuloYAlmacen(articulo.getCodigo(), almacen.getCodigo())
                .map(a -> a.getExistencia() != null ? a.getExistencia() : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }

    private void abrirDialogoSeleccionArticulo() {
        if (cbAlmacen.getValue() == null) {
            ClaseUtil.mostrarNotificacion("Debe seleccionar un Almacén antes de agregar artículos", NotificationVariant.LUMO_WARNING);
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

    // Evalúa la lista de empaques para determinar si despliega el diálogo de selección o no
    private void procesarSeleccionArticulo(Articulo articulo) {
        List<ArticuloEmpaque> listaEmpaques = articuloEmpaqueService.getPorArticulo(articulo.getCodigo());

        if (listaEmpaques != null && listaEmpaques.size() > 1) {
            mostrarDialogoSeleccionEmpaque(articulo, listaEmpaques);
        } else {
            ArticuloEmpaque empaqueUnico = (listaEmpaques != null && !listaEmpaques.isEmpty()) ? listaEmpaques.get(0) : null;
            agregarOActualizarArticulo(articulo, empaqueUnico);
        }
    }

    // Diálogo modal para elegir empaque (mismo estilo que en Salida)
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

        Almacen almacenSeleccionado = cbAlmacen.getValue();
        BigDecimal stockActual = obtenerExistenciaPorAlmacen(articulo, almacenSeleccionado);

        boolean existe = listDet.stream()
                .anyMatch(d -> d.getArticulo() != null
                && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())
                && Objects.equals(d.getArticuloEmpaque(), empaqueSeleccionado));

        if (existe) {
            // Incrementa en 1 la cantidad si vuelve a seleccionar el mismo artículo y empaque
            listDet.forEach(d -> {
                if (Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())
                        && Objects.equals(d.getArticuloEmpaque(), empaqueSeleccionado)) {

                    BigDecimal cantidadActual = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
                    BigDecimal nuevaCant = cantidadActual.add(BigDecimal.ONE);

                    recalcularTotalesYExistenciaFila(d, nuevaCant, empaqueSeleccionado);
                }
            });
        } else {
            
            DetalleAjusteInventario det = new DetalleAjusteInventario();
            det.setCodigo(articulo.getCodigo());
            det.setArticulo(articulo);
            det.setDescripcionArticulo(articulo.getDescripcion());
            det.setExistencia(stockActual);
            det.setAlmacen(almacenSeleccionado);
            det.setNombreAlmacen(almacenSeleccionado != null ? almacenSeleccionado.getNombre() : "");

            det.setArticuloEmpaque(empaqueSeleccionado);
            det.setFactorConversion(empaqueSeleccionado.getFactorConversion());
            det.setUnidad(articulo.getUnidadBase());
            if (empaqueSeleccionado != null && empaqueSeleccionado.getUnidadEmpaque() != null) {
                det.setNombreUnidad(empaqueSeleccionado.getUnidadEmpaque().getDescripcion());
            } else if (articulo.getUnidadBase() != null) {
                det.setNombreUnidad(articulo.getUnidadBase().getDescripcion());
            } else {
                det.setNombreUnidad("Unidad");
            }

            // Se inicializan la cantidad en 1 y se calculan subtotal, itbis, total y nuevaExistencia
            recalcularTotalesYExistenciaFila(det, BigDecimal.ONE, empaqueSeleccionado);

            listDet.add(det);
        }

        grid.getDataProvider().refreshAll();
    }

    private void recalcularTotalesYExistenciaFila(DetalleAjusteInventario item, BigDecimal cantidad, ArticuloEmpaque empaque) {
        BigDecimal cantEfectiva = cantidad != null ? cantidad : BigDecimal.ZERO;
        item.setCantidad(cantEfectiva);
        item.setArticuloEmpaque(empaque);

        // 1. Recalcula existencia base (UMM)
        item.setNuevaExistencia(calcularNuevaExistencia(item.getExistencia(), cantEfectiva, empaque));

        // 2. Obtiene precio unitario según empaque o artículo base
        BigDecimal precioUnitario = (empaque != null && empaque.getPrecioCompra() != null)
                ? empaque.getPrecioCompra()
                : (item.getArticulo() != null && item.getArticulo().getPrecioCompra() != null
                ? item.getArticulo().getPrecioCompra()
                : BigDecimal.ZERO);

        item.setPrecioCompra(precioUnitario);
        item.setCostoUnitario(precioUnitario);

        // 3. Montos financieros requeridos (evitan la falla del Validator @NotNull)
        BigDecimal subTotal = cantEfectiva.multiply(precioUnitario);

        double porcentajeItbis = (item.getArticulo() != null && item.getArticulo().getItbisGravado() != null)
                ? item.getArticulo().getItbisGravado()
                : 0.0;

        BigDecimal itbis = subTotal.multiply(BigDecimal.valueOf(porcentajeItbis / 100.0));

        item.setSubTotal(subTotal);
        item.setItbis(itbis);
        item.setTotal(subTotal.add(itbis));
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

    private BigDecimal calcularNuevaExistencia(BigDecimal existenciaActual, BigDecimal cantidad, ArticuloEmpaque empaque) {
        TipoAjuste tipo = cbTipoAjuste.getValue();
        if (tipo == null || existenciaActual == null || cantidad == null) {
            return existenciaActual != null ? existenciaActual : BigDecimal.ZERO;
        }

        BigDecimal factor = obtenerFactorConversion(empaque);
        BigDecimal cantidadConvertida = cantidad.multiply(factor);

        if ("INCREMENTO".equalsIgnoreCase(tipo.getDescripcion())) {
            return existenciaActual.add(cantidadConvertida);
        } else {
            return existenciaActual.subtract(cantidadConvertida);
        }
    }

    private void recalcularNuevasExistencias() {
        for (DetalleAjusteInventario det : listDet) {
            det.setNuevaExistencia(calcularNuevaExistencia(det.getExistencia(), det.getCantidad(), det.getArticuloEmpaque()));
        }
        grid.getDataProvider().refreshAll();
    }

    private void configurarGridDetalle() {
        grid.setHeightFull();
        grid.setWidthFull();

        GridListDataView<DetalleAjusteInventario> dataView = grid.setItems(listDet);

        grid.addColumn(DetalleAjusteInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true);

        grid.addColumn(DetalleAjusteInventario::getNombreAlmacen)
                .setHeader("Almacén")
                .setAutoWidth(true);

        grid.addColumn(item -> item.getArticuloEmpaque() != null && item.getArticuloEmpaque().getUnidadEmpaque() != null
                ? item.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : item.getNombreUnidad())
                .setHeader("Empaque / Presentación")
                .setKey("empaque")
                .setAutoWidth(true);

        grid.addColumn(DetalleAjusteInventario::getExistencia)
                .setHeader("Existencia Actual (UMM)")
                .setAutoWidth(true);

        grid.addColumn(DetalleAjusteInventario::getCantidad)
                .setHeader("Cantidad Ajuste")
                .setKey("cantidad")
                .setAutoWidth(true);

        grid.addColumn(DetalleAjusteInventario::getNuevaExistencia)
                .setHeader("Nueva Existencia (UMM)")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' del ajuste?",
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
            boolean matchCod = det.getArticulo() != null && det.getArticulo().getCodigo().toString().contains(term);
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

            DetalleAjusteInventario itemSeleccionado = event.getItem();

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

            DetalleAjusteInventario itemActual = editor.getItem();
            ArticuloEmpaque nuevoEmpaque = e.getValue();
            if (nuevoEmpaque == null) {
                return;
            }

            itemActual.setArticuloEmpaque(nuevoEmpaque);
            if (nuevoEmpaque.getUnidadEmpaque() != null) {
                itemActual.setNombreUnidad(nuevoEmpaque.getUnidadEmpaque().getDescripcion());
            }
            itemActual.setNuevaExistencia(calcularNuevaExistencia(itemActual.getExistencia(), itemActual.getCantidad(), nuevoEmpaque));

            grid.getDataProvider().refreshItem(itemActual);
        });

        cantidadField.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            try {
                BigDecimal cant = e.getValue();
                if (cant == null) {
                    cant = BigDecimal.ZERO;
                }

                if (cant.compareTo(BigDecimal.ZERO) <= 0 && cbTipoAjuste.getValue() != null) {
                    Notification.show("La cantidad debe ser mayor a cero", 2500, Position.MIDDLE);
                    return;
                }

                DetalleAjusteInventario item = editor.getItem();

                if (cbTipoAjuste.getValue() != null) {
                    item.setCantidad(cant);
                    item.setNuevaExistencia(calcularNuevaExistencia(item.getExistencia(), cant, item.getArticuloEmpaque()));
                } else {
                    ClaseUtil.mostrarNotificacion("Tiene que seleccionar el tipo de ajuste", NotificationVariant.LUMO_WARNING);
                    item.setCantidad(BigDecimal.ZERO);
                }

                grid.getDataProvider().refreshItem(item);
            } catch (Exception ex) {
                Notification.show("Ingrese una cantidad válida", 2000, Position.MIDDLE);
            }
        });
    }

    private void procesarGuardado() {
        if (cbAlmacen.getValue() == null) {
            Notification.show("Debe seleccionar un Almacén", 3000, Position.TOP_CENTER);
            cbAlmacen.focus();
            return;
        }

        if (cbTipoAjuste.getValue() == null) {
            Notification.show("Debe seleccionar un Tipo de Ajuste", 3000, Position.TOP_CENTER);
            cbTipoAjuste.focus();
            return;
        }

        if (listDet.isEmpty()) {
            Notification.show("El ajuste no tiene artículos registrados", 3000, Position.TOP_CENTER);
            return;
        }

        try {
            String usuarioActual = "ADMIN";

            AjusteInventario ajuste = new AjusteInventario();
            ajuste.setFecha(ClaseUtil.asDate(dpFecha.getValue()));
            ajuste.setAlmacen(cbAlmacen.getValue());
            ajuste.setNombreAlmacen(cbAlmacen.getValue().getNombre());
            ajuste.setTipoAjuste(cbTipoAjuste.getValue());
            ajuste.setObservacion(txtObservacion.getValue());
            ajuste.setUsuario(new Usuario(1));
            ajuste.setNombreUsuario(usuarioActual);
            ajuste.setAnulado(false);

            AjusteInventario guardado = ajusteService.procesarAjusteTransaccional(ajuste, listDet, usuarioActual);

            Notification.show("Ajuste #" + guardado.getCodigo() + " guardado con éxito", 3000, Position.TOP_CENTER);

            listDet.clear();
            txtObservacion.clear();
            grid.getDataProvider().refreshAll();

        } catch (IllegalStateException | IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 4000, Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Error al guardar el ajuste: " + e.getMessage(), 4000, Position.TOP_CENTER);
            e.printStackTrace();
        }
    }
}
