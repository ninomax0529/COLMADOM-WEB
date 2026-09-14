package com.maxsoft.application.view.inventario.entrada;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@PageTitle("Registro Entrada de Inventario")
@Route(value = "inventario/registroEntrada")
public class RegistroEntradaDeIventarioView extends VerticalLayout {

    private final Grid<DetalleEntradaInventario> grid = new Grid<>(DetalleEntradaInventario.class, false);
    private Editor<DetalleEntradaInventario> editor;

    private final TextField txtNumDoc = new TextField("Número Entrada");
    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final ArticuloService articuloService;
    private final EntradaDeInventarioService entradaInvService;

    private final List<DetalleEntradaInventario> listDet = new ArrayList<>();

    @Autowired
    public RegistroEntradaDeIventarioView(EntradaDeInventarioService entradaInvService,
                                         ArticuloService articuloService) {

        this.entradaInvService = entradaInvService;
        this.articuloService = articuloService;

        setSizeFull();
        configurarBotonera();
        configurarControlesSuperiores();
        configurarGridDetalle();

        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha, botonera);
        hlDatos.setAlignItems(Alignment.BASELINE);

        HorizontalLayout hlArticulo = new HorizontalLayout(txtBuscar, btnNuevo);
        hlArticulo.setWidthFull();

        add(hlDatos, hlArticulo, grid);
    }

    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> procesarGuardado());
        botonera.getCancelar().addClickListener(e -> UI.getCurrent().navigate(EntradaDeIventarioView.class));
    }

    private void configurarControlesSuperiores() {
        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

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
                    double nuevaCant = d.getCantidadRecibida() + 1.0;
                    d.setCantidadRecibida(nuevaCant);
                    d.setNuevaExistencia(d.getExistenciaActual() + nuevaCant);
                }
            });
        } else {
            DetalleEntradaInventario det = new DetalleEntradaInventario();
            det.setCodigo(articulo.getCodigo());
            det.setArticulo(articulo);
            det.setDescripcionArticulo(articulo.getDescripcion());
            
            double stockActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;
            det.setExistenciaActual(stockActual);
            
            det.setCantidadPedida(0.00);
            det.setCantidadRecibida(0.00);
            det.setCantidadPendiente(0.00);
            det.setNuevaExistencia(stockActual);
            det.setNombreAlmacen("General");
            det.setNombreUnidad("Unidad");

            listDet.add(det);
        }
        grid.getDataProvider().refreshAll();
    }

    private void configurarGridDetalle() {
        grid.setHeightFull();
        grid.setWidthFull();

        GridListDataView<DetalleEntradaInventario> dataView = grid.setItems(listDet);

        grid.addColumn(DetalleEntradaInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true);

        grid.addColumn(DetalleEntradaInventario::getExistenciaActual)
                .setHeader("Existencia Actual")
                .setAutoWidth(true);

        grid.addColumn(DetalleEntradaInventario::getCantidadRecibida)
                .setHeader("Cantidad Entrada")
                .setKey("cantidad")
                .setAutoWidth(true);

        grid.addColumn(DetalleEntradaInventario::getNuevaExistencia)
                .setHeader("Nueva Existencia")
                .setAutoWidth(true);

        grid.addColumn(DetalleEntradaInventario::getNombreUnidad)
                .setHeader("Unidad")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' de la entrada?",
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
            boolean matchCod = det.getCodigo() != null && det.getCodigo().toString().contains(term);
            return matchDesc || matchCod;
        }));

        configurarEditorInLine();
    }

    private void configurarEditorInLine() {
        editor = grid.getEditor();
        editor.setBuffered(false);

        TextField cantidadField = new TextField();
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
                double cant = Double.parseDouble(e.getValue());
                if (cant <= 0) {
                    Notification.show("La cantidad debe ser mayor a cero", 2500, Position.MIDDLE);
                    return;
                }
                DetalleEntradaInventario item = editor.getItem();
                item.setCantidadRecibida(cant);
                item.setNuevaExistencia(item.getExistenciaActual() + cant);
                grid.getDataProvider().refreshItem(item);

            } catch (NumberFormatException ex) {
                Notification.show("Ingrese una cantidad válida", 2000, Position.MIDDLE);
            }
        });
    }

    private void procesarGuardado() {
        if (listDet.isEmpty()) {
            Notification.show("La entrada no tiene artículos registrados", 3000, Position.TOP_CENTER);
            return;
        }

        for (DetalleEntradaInventario det : listDet) {
            if (det.getCantidadRecibida() <= 0) {
                Notification.show("El artículo '" + det.getDescripcionArticulo() + "' tiene cantidad en cero", 4000, Position.TOP_CENTER);
                return;
            }
        }

        try {
            LocalDate localFecha = dpFecha.getValue();
            Date fecha = ClaseUtil.asDate(localFecha);

            EntradaInventario entradaInv = new EntradaInventario();
            entradaInv.setFecha(fecha);
            entradaInv.setFechaCreacion(new Date());
            entradaInv.setFechaActualizacion(new Date());
            entradaInv.setNombreUsuario("Administrador");

            listDet.forEach(e -> {
                e.setEntradaInventario(entradaInv);
                e.setCodigo(null);
            });

            entradaInv.setDetalleEntradaInventarioCollection(listDet);
            
            this.entradaInvService.guardar(entradaInv);

            Notification.show("Entrada guardada exitosamente", 3000, Position.TOP_CENTER);
            listDet.clear();
            grid.getDataProvider().refreshAll();

        } catch (Exception e) {
            Notification.show("Error guardando la entrada: " + e.getMessage(), 3000, Position.TOP_CENTER);
            e.printStackTrace();
        }
    }
}