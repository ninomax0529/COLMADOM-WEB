/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.ajuste;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.TipoAjuste;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
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
    private final ComboBox<TipoAjuste> cbTipoAjuste = new ComboBox<>("Tipo de Ajuste");
    private final TextField txtObservacion = new TextField("Observación");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final ArticuloService articuloService;
    private final AjusteInventarioService ajusteService;
    TipoAjusteService tipoAjusteService;
    AlmacenService almacenService;
    private final MovimientoInventarioService movimientoService; // <--- Nuevo Servicio
    TipoMovimientoService tipoMovimientoService;
    TipoDocumentoService tipoDocumentoService;

    private final List<DetalleAjusteInventario> listDet = new ArrayList<>();

    @Autowired
    public RegistroAjusteDeInventarioView(AjusteInventarioService ajusteService,
            ArticuloService articuloService,
            TipoAjusteService tipoAjusteService,
            AlmacenService almacenService,
            MovimientoInventarioService movimientoService,
            TipoMovimientoService tipoMovimientoService,
            TipoDocumentoService tipoDocumentoService) {

        this.ajusteService = ajusteService;
        this.articuloService = articuloService;
        this.tipoAjusteService = tipoAjusteService;
        this.almacenService = almacenService;
        this.movimientoService = movimientoService; // <--- Asignación
        this.tipoDocumentoService = tipoDocumentoService;
        this.tipoMovimientoService = tipoMovimientoService;

        setSizeFull();
        configurarBotonera();
        configurarControlesSuperiores();
        configurarGridDetalle();

        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha, cbTipoAjuste, txtObservacion, botonera);
        hlDatos.setAlignItems(Alignment.BASELINE);

        HorizontalLayout hlArticulo = new HorizontalLayout(txtBuscar, btnNuevo);
        hlArticulo.setWidthFull();

        add(hlDatos, hlArticulo, grid);
    }

    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> procesarGuardado());
        botonera.getCancelar().addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
    }

    private void configurarControlesSuperiores() {
        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

        cbTipoAjuste.setItems(this.tipoAjusteService.getLista());

        cbTipoAjuste.setItemLabelGenerator(o -> o.getDescripcion());
//        cbTipoAjuste.setValue("DECREMENTO");
        cbTipoAjuste.setAllowCustomValue(false);
        cbTipoAjuste.addValueChangeListener(e -> recalcularNuevasExistencias());

        txtObservacion.setWidth("300px");

        btnNuevo = new Button("Artículo (F2)", event -> abrirDialogoSeleccionArticulo());
        btnNuevo.addClickShortcut(Key.F2);

        txtBuscar.setWidth("50%");
        txtBuscar.setPlaceholder("Filtrar por código o descripción...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
    }

    private void abrirDialogoSeleccionArticulo() {
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

        boolean existe = listDet.stream()
                .anyMatch(d -> d.getArticulo() != null && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo()));

        if (existe) {

            listDet.forEach(d -> {
                if (Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())) {
                    double nuevaCant = d.getCantidad();
                    d.setCantidad(nuevaCant);
                    d.setNuevaExistencia(calcularNuevaExistencia(d.getExistencia(), nuevaCant));
                }
            });

        } else {

            DetalleAjusteInventario det = new DetalleAjusteInventario();
            det.setCodigo(articulo.getCodigo());
            det.setArticulo(articulo);
            det.setDecripcionArticulo(articulo.getDescripcion());

            double stockActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;
            det.setExistencia(stockActual);

            det.setCantidad(0.00);
            det.setNuevaExistencia(stockActual);
            det.setUnidad(articulo.getUnidadEntrada());
            det.setNombreUnidad(det.getUnidad().getDescripcion());
            det.setAlmacen(this.almacenService.getAlmacen(1));
            det.setNombreAlmacen(det.getAlmacen().getNombre());

            listDet.add(det);
        }

        grid.getDataProvider().refreshAll();
    }

    private double calcularNuevaExistencia(double existenciaActual, double cantidad) {
        // 1. Obtener el valor de forma segura
        TipoAjuste tipo = cbTipoAjuste.getValue();

        // 2. Si es nulo, retornar la existencia sin cambios o asumir un comportamiento por defecto
//        if (tipo == null || tipo.getDescripcion() == null) {
//
//            ClaseUtil.mostrarNotificacion("Tiene que seleccionar el tipo de ajuste", NotificationVariant.LUMO_WARNING);
//            return 0.00;
//        }
        // 3. Evaluar según el tipo seleccionado
        if ("INCREMENTO".equalsIgnoreCase(tipo.getDescripcion())) {
            return existenciaActual + cantidad;
        } else {
            return existenciaActual - cantidad;
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

        grid.addColumn(DetalleAjusteInventario::getDecripcionArticulo)
                .setHeader("Artículo")
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
                        "¿Seguro que desea remover '" + item.getDecripcionArticulo() + "' del ajuste?",
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

            boolean matchDesc = det.getDecripcionArticulo() != null && det.getDecripcionArticulo().toLowerCase().contains(term);
            boolean matchCod = det.getArticulo() != null && det.getArticulo().getCodigo().toString().contains(term);
            return matchDesc || matchCod;
        }));

        configurarEditorInLine();
    }

    private void configurarEditorInLine() {
        editor = grid.getEditor();
        editor.setBuffered(false);

        NumberField cantidadField = new NumberField();
        cantidadField.setWidthFull();

        grid.getColumnByKey("cantidad").setEditorComponent(cantidadField);

        grid.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel();
            }
            editor.editItem(event.getItem());
        });

        cantidadField.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            try {
                double cant = e.getValue();
                if (cant <= 0 && cbTipoAjuste.getValue() != null) {

                    Notification.show("La cantidad debe ser mayor a cero", 2500, Position.MIDDLE);
                    return;

                }

                DetalleAjusteInventario item = editor.getItem();

                // 2. Si es nulo, retornar la existencia sin cambios o asumir un comportamiento por defecto
                if (cbTipoAjuste.getValue() != null) {

                    item.setCantidad(cant);
                    item.setNuevaExistencia(calcularNuevaExistencia(item.getExistencia(), cant));
//                    grid.getDataProvider().refreshItem(item);
                } else {

                    ClaseUtil.mostrarNotificacion("Tiene que seleccionar el tipo de ajuste", NotificationVariant.LUMO_WARNING);

//                    cantidadField.setValue(0.00);
                    item.setCantidad(0.0);
                }

                grid.getDataProvider().refreshItem(item);
            } catch (NumberFormatException ex) {
                Notification.show("Ingrese una cantidad válida", 2000, Position.MIDDLE);
            }
        });
    }

    private void procesarGuardado() {
        // 1. Validaciones simples de la UI
        if (listDet.isEmpty()) {
            Notification.show("El ajuste no tiene artículos registrados", 3000, Position.TOP_CENTER);
            return;
        }

        String tipoSel = cbTipoAjuste.getValue().getDescripcion();
        if (tipoSel == null) {
            Notification.show("Debe seleccionar un Tipo de Ajuste", 3000, Position.TOP_CENTER);
            cbTipoAjuste.focus();
            return;
        }

        try {
            
            
            String usuarioActual = "ADMIN"; // Sustituir por usuario del contexto de seguridad
            // 2. Construir objeto cabecera
            AjusteInventario ajuste = new AjusteInventario();
            ajuste.setFecha(ClaseUtil.asDate(dpFecha.getValue()));
            ajuste.setTipoAjuste(cbTipoAjuste.getValue());
            ajuste.setObservacion(txtObservacion.getValue());
            ajuste.setUsuario(new Usuario(1));
            ajuste.setNombreUsuario(usuarioActual);
            ajuste.setAnulado(false);


            // 3. Ejecutar la transacción completa atómicamente
            AjusteInventario guardado = ajusteService.procesarAjusteTransaccional(ajuste, listDet, usuarioActual);

            Notification.show("Ajuste #" + guardado.getCodigo() + " guardado con éxito", 3000, Position.TOP_CENTER);

            // 4. Limpiar formulario
            listDet.clear();
            txtObservacion.clear();
            grid.getDataProvider().refreshAll();

        } catch (IllegalStateException | IllegalArgumentException ex) {
            // Muestra excepciones de stock insuficiente o validaciones de negocio
            Notification.show(ex.getMessage(), 4000, Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Error al guardar el ajuste: " + e.getMessage(), 4000, Position.TOP_CENTER);
            e.printStackTrace();
        }
    }
}
