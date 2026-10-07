///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */


package com.maxsoft.application.view.inventario.ajuste;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.TipoAjuste;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService; // Service para consultar stock por almacén
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
@PageTitle("Registro Ajuste de Inventario")
@Route(value = "inventario/registroAjuste")
public class RegistroAjusteDeInventarioView extends VerticalLayout {

    private final Grid<DetalleAjusteInventario> grid = new Grid<>(DetalleAjusteInventario.class, false);
    private Editor<DetalleAjusteInventario> editor;

    private final TextField txtNumDoc = new TextField("Número Ajuste");
    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén"); // <-- NUEVO: Multi-Almacén
    private final ComboBox<TipoAjuste> cbTipoAjuste = new ComboBox<>("Tipo de Ajuste");
    private final TextField txtObservacion = new TextField("Observación");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final ArticuloService articuloService;
    private final AjusteInventarioService ajusteService;
    private final TipoAjusteService tipoAjusteService;
    private final AlmacenService almacenService;
    private final ArticuloAlmacenService existenciaService; // <-- Servicio para consultar stock por almacén
    private final MovimientoInventarioService movimientoService;
    private final TipoMovimientoService tipoMovimientoService;
    private final TipoDocumentoService tipoDocumentoService;

    private final List<DetalleAjusteInventario> listDet = new ArrayList<>();

    @Autowired
    public RegistroAjusteDeInventarioView(AjusteInventarioService ajusteService,
                                           ArticuloService articuloService,
                                           TipoAjusteService tipoAjusteService,
                                           AlmacenService almacenService,
                                           ArticuloAlmacenService existenciaService,
                                           MovimientoInventarioService movimientoService,
                                           TipoMovimientoService tipoMovimientoService,
                                           TipoDocumentoService tipoDocumentoService) {

        this.ajusteService = ajusteService;
        this.articuloService = articuloService;
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

        // Layout de Cabecera con selector de Almacén
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

        // Configuración ComboBox Almacén
        cbAlmacen.setItems(this.almacenService.getLista());
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setAllowCustomValue(false);
        cbAlmacen.setRequired(true);
        cbAlmacen.addValueChangeListener(e -> cambiarAlmacenGeneral(e.getValue()));

        // Configuración ComboBox Tipo Ajuste
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
        if (nuevoAlmacen == null) return;

        // Actualizar almacén y stock actual de cada fila agregada
        for (DetalleAjusteInventario det : listDet) {
            det.setAlmacen(nuevoAlmacen);
            det.setNombreAlmacen(nuevoAlmacen.getNombre());

            // Consultar la existencia del artículo en el nuevo almacén seleccionado
            BigDecimal stockEnAlmacen = obtenerExistenciaPorAlmacen(det.getArticulo(), nuevoAlmacen);
            det.setExistencia(stockEnAlmacen);
            det.setNuevaExistencia(calcularNuevaExistencia(stockEnAlmacen, det.getCantidad()));
        }

        grid.getDataProvider().refreshAll();
    }

    private BigDecimal obtenerExistenciaPorAlmacen(Articulo articulo, Almacen almacen) {
        if (articulo == null || almacen == null) return BigDecimal.ZERO;
        // Lógica para traer la existencia de la tabla intermedia (ej. existencia_articulo / stock_almacen)
        BigDecimal stock = existenciaService.buscarPorArticuloYAlmacen(articulo.getCodigo(), almacen.getCodigo()).get().getExistencia();
        return stock != null ? stock : BigDecimal.ZERO;
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
                    agregarOActualizarArticulo(articulo);
                }
            });
            dialog.open();
        } catch (Exception e) {
            Notification.show("Error abriendo diálogo de artículos: " + e.getMessage(), 3000, Position.TOP_CENTER);
        }
    }

    private void agregarOActualizarArticulo(Articulo articulo) {
        
        Almacen almacenSeleccionado = cbAlmacen.getValue();

        boolean existe = listDet.stream()
                .anyMatch(d -> d.getArticulo() != null && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo()));

        if (existe) {
            listDet.forEach(d -> {
                if (Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())) {
                    BigDecimal nuevaCant = d.getCantidad();
                    d.setCantidad(nuevaCant);
                    d.setNuevaExistencia(calcularNuevaExistencia(d.getExistencia(), nuevaCant));
                }
            });
        } else {
            DetalleAjusteInventario det = new DetalleAjusteInventario();
            det.setCodigo(articulo.getCodigo());
            det.setArticulo(articulo);
            det.setDescripcionArticulo(articulo.getDescripcion());

            // Obtener stock específico para el almacén activo
            BigDecimal stockActual = obtenerExistenciaPorAlmacen(articulo, almacenSeleccionado);
            det.setExistencia(stockActual);

            det.setCantidad(BigDecimal.ZERO);
            det.setNuevaExistencia(stockActual);
            det.setUnidad(articulo.getUnidadBase());
            if (articulo.getUnidadBase()!= null) {
             det.setNombreUnidad(articulo.getUnidadBase().getDescripcion());
            }
            det.setAlmacen(almacenSeleccionado);
            det.setNombreAlmacen(almacenSeleccionado.getNombre());

            listDet.add(det);
        }

        grid.getDataProvider().refreshAll();
    }

    private BigDecimal calcularNuevaExistencia(BigDecimal existenciaActual, BigDecimal cantidad) {
        TipoAjuste tipo = cbTipoAjuste.getValue();
        if (tipo == null || existenciaActual == null || cantidad == null) {
            return existenciaActual != null ? existenciaActual : BigDecimal.ZERO;
        }

        if ("INCREMENTO".equalsIgnoreCase(tipo.getDescripcion())) {
            return existenciaActual.add(cantidad);
        } else {
            return existenciaActual.subtract(cantidad);
        }
    }

    private void recalcularNuevasExistencias() {
        for (DetalleAjusteInventario det : listDet) {
            det.setNuevaExistencia(calcularNuevaExistencia(det.getExistencia(), det.getCantidad()));
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

        grid.addColumn(DetalleAjusteInventario::getExistencia)
                .setHeader("Existencia Actual")
                .setAutoWidth(true);

        grid.addColumn(DetalleAjusteInventario::getCantidad)
                .setHeader("Cantidad Ajuste")
                .setKey("cantidad")
                .setAutoWidth(true);

        grid.addColumn(DetalleAjusteInventario::getNuevaExistencia)
                .setHeader("Nueva Existencia")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' del ajuste?",
                        () -> {
                            listDet.remove(item);
                            grid.getDataProvider().refreshAll();
                        },
                        () -> {}
                );
                dialog.open();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return deleteButton;
        })).setHeader("Acciones").setAutoWidth(true);

        txtBuscar.addValueChangeListener(e -> dataView.addFilter(det -> {
            String term = e.getValue().trim().toLowerCase();
            if (term.isEmpty()) return true;

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

        grid.getColumnByKey("cantidad").setEditorComponent(cantidadField);

        grid.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel();
            }
            editor.editItem(event.getItem());
        });

        cantidadField.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null) return;

            try {
                BigDecimal cant = e.getValue();
                if (cant == null) cant = BigDecimal.ZERO;

                if (cant.compareTo(BigDecimal.ZERO) <= 0 && cbTipoAjuste.getValue() != null) {
                    Notification.show("La cantidad debe ser mayor a cero", 2500, Position.MIDDLE);
                    return;
                }

                DetalleAjusteInventario item = editor.getItem();

                if (cbTipoAjuste.getValue() != null) {
                    item.setCantidad(cant);
                    item.setNuevaExistencia(calcularNuevaExistencia(item.getExistencia(), cant));
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
            String usuarioActual = "ADMIN"; // Ajustar al SecurityContext actual

            AjusteInventario ajuste = new AjusteInventario();
            ajuste.setFecha(ClaseUtil.asDate(dpFecha.getValue()));
            ajuste.setAlmacen(cbAlmacen.getValue()); // <-- Asignación del Almacén en la Cabecera
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
