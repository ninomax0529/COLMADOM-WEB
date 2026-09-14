package com.maxsoft.application.view.inventario.salida;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.modelo.Unidad;
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
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@PermitAll
@PageTitle("Registro Salida de Inventario")
@Route(value = "inventario/registroSalida")
public class RegistroSalidaDeIventarioView extends VerticalLayout {

    private final Grid<DetalleSalidaInventario> grid = new Grid<>(DetalleSalidaInventario.class, false);
    private Editor<DetalleSalidaInventario> editor;

    private final TextField txtNumDoc = new TextField("Número Salida");
    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final ArticuloService articuloService;
    private final SalidaInventarioService salidaInvService;

    private final List<DetalleSalidaInventario> listDet = new ArrayList<>();

    @Autowired
    public RegistroSalidaDeIventarioView(SalidaInventarioService salidaInvService,
                                         ArticuloService articuloService) {

        this.salidaInvService = salidaInvService;
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
        botonera.getCancelar().addClickListener(e -> UI.getCurrent().navigate(SalidaDeIventarioView.class));
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
                    double nuevaCantSalida = d.getCantidad();
                    d.setCantidad(nuevaCantSalida);
                    d.setExistencia(d.getExistencia()- nuevaCantSalida); // Nueva existencia tras la salida
                }
            });
        } else {
            DetalleSalidaInventario det = new DetalleSalidaInventario();
            det.setCodigo(articulo.getCodigo());
            det.setArticulo(articulo);
            det.setDescripcionArticulo(articulo.getDescripcion());

            double stockActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;
            det.setExistenciaAnterior(stockActual);

            det.setCantidad(0.00); // Cantidad inicial a sacar
            det.setExistencia(stockActual); // Nueva existencia (Resta)
            det.setUnidad(new Unidad(1));

            listDet.add(det);
        }
        grid.getDataProvider().refreshAll();
    }

    private void configurarGridDetalle() {
        grid.setHeightFull();
        grid.setWidthFull();

        GridListDataView<DetalleSalidaInventario> dataView = grid.setItems(listDet);

        grid.addColumn(DetalleSalidaInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true);

        grid.addColumn(DetalleSalidaInventario::getExistenciaAnterior)
                .setHeader("Existencia Actual")
                .setAutoWidth(true);

        grid.addColumn(DetalleSalidaInventario::getCantidad)
                .setHeader("Cantidad Salida")
                .setKey("cantidad")
                .setAutoWidth(true);

        grid.addColumn(DetalleSalidaInventario::getExistencia)
                .setHeader("Nueva Existencia")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' de la salida?",
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
                DetalleSalidaInventario item = editor.getItem();
                item.setCantidad(cant);
                item.setExistencia(item.getExistencia()- cant); // Recálculo restando la salida
                grid.getDataProvider().refreshItem(item);

            } catch (NumberFormatException ex) {
                Notification.show("Ingrese una cantidad válida", 2000, Position.MIDDLE);
            }
        });
    }

    private void procesarGuardado() {
        if (listDet.isEmpty()) {
            Notification.show("La salida no tiene artículos registrados", 3000, Position.TOP_CENTER);
            return;
        }

        for (DetalleSalidaInventario det : listDet) {
            if (det.getCantidad() <= 0) {
                Notification.show("El artículo '" + det.getDescripcionArticulo() + "' tiene cantidad en cero", 4000, Position.TOP_CENTER);
                return;
            }
        }

        try {
            LocalDate localFecha = dpFecha.getValue();
            Date fecha = ClaseUtil.asDate(localFecha);

            SalidaInventario salidaInv = new SalidaInventario();
            salidaInv.setFecha(fecha);
            salidaInv.setFechaRegistro(new Date());

            listDet.forEach(e -> {
                e.setSalidaInventario(salidaInv);
                e.setCodigo(null);
            });

            salidaInv.setDetalleSalidaInventarioCollection(listDet);

            // Persistir la salida y actualizar/descontar el stock en la BD
            this.salidaInvService.guardar(salidaInv);

            Notification.show("Salida guardada exitosamente", 3000, Position.TOP_CENTER);
            listDet.clear();
            grid.getDataProvider().refreshAll();

        } catch (Exception e) {
            Notification.show("Error guardando la salida: " + e.getMessage(), 3000, Position.TOP_CENTER);
            e.printStackTrace();
        }
    }
}