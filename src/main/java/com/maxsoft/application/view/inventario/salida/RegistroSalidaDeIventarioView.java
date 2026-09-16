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
import com.vaadin.flow.component.html.Span;
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

        if (articulo == null) {
            Notification.show("Seleccione un artículo y una cantidad válida", 3000, Position.TOP_CENTER);
            return;
        }

        if (Boolean.TRUE.equals(articulo.getInventariable())) {
            double stockActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;

            // Sumar cantidades del mismo artículo que YA están agregadas en la Grid
            double cantidadYaEnGrid = listDet.stream()
                    .filter(d -> d.getArticulo().getCodigo().equals(articulo.getCodigo()))
                    .mapToDouble(DetalleSalidaInventario::getCantidad)
                    .sum();

            double totalSolicitado = cantidadYaEnGrid;

            if (totalSolicitado > stockActual) {
                Notification.show(
                        String.format("Stock insuficiente para '%s'. Disponible: %.2f | En lista: %.2f | Intentas retirar: %.2f",
                                articulo.getDescripcion(), stockActual, cantidadYaEnGrid, totalSolicitado),
                        5000, Position.MIDDLE
                );
                return;
            }
        }

        boolean existe = listDet.stream()
                .anyMatch(d -> d.getArticulo() != null && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo()));

        if (existe) {

            listDet.forEach(d -> {
                if (Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())) {
                    double nuevaCantSalida = d.getCantidad();
                    d.setCantidad(nuevaCantSalida);
                    d.setExistencia(d.getExistencia() - nuevaCantSalida); // Nueva existencia tras la salida
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
            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            try {
                double cant = Double.parseDouble(e.getValue());

                if (cant <= 0) {
                    Notification.show("La cantidad debe ser mayor a cero", 2500, Position.MIDDLE);
                    return;
                }

                DetalleSalidaInventario itemActual = editor.getItem();
                Articulo articulo = itemActual.getArticulo();

                if (articulo != null && Boolean.TRUE.equals(articulo.getInventariable())) {

                    // 1. Stock real en el maestro del artículo
                    double stockDisponible = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;

                    // 2. Calcular la suma de cantidades agregadas en OTRAS filas para este mismo artículo
                    double cantidadEnOtrasFilas = listDet.stream()
                            .filter(d -> !d.equals(itemActual) && d.getArticulo() != null && d.getArticulo().getCodigo().equals(articulo.getCodigo()))
                            .mapToDouble(d -> d.getCantidad() != null ? d.getCantidad() : 0.0)
                            .sum();

                    // 3. Validar si la nueva cantidad excede el disponible
                    if ((cantidadEnOtrasFilas + cant) > stockDisponible) {
                        Notification.show(
                                String.format("Stock insuficiente para '%s'. Disponible: %.2f (Ya en uso en otras filas: %.2f)",
                                        articulo.getDescripcion(), stockDisponible, cantidadEnOtrasFilas),
                                4000, Position.MIDDLE
                        );

                        // Restaurar o limpiar la casilla si excede
                        cantidadField.setValue(itemActual.getCantidad() != null ? String.valueOf(itemActual.getCantidad()) : "");
                        return;
                    }
                }

                // 4. Si la validación pasa, actualizamos el modelo y recalculamos existencia en pantalla
                itemActual.setCantidad(cant);
                if (articulo != null && articulo.getExistencia() != null) {
                    itemActual.setExistencia(articulo.getExistencia() - cant);
                }

                grid.getDataProvider().refreshItem(itemActual);

            } catch (NumberFormatException ex) {
                Notification.show("Ingrese una cantidad válida", 2000, Position.MIDDLE);
            }
        });
    }

    private void procesarGuardado() {
        // 1. Validaciones previas de la interfaz
        if (listDet.isEmpty()) {
            Notification.show("Debe agregar al menos un artículo a la salida.", 3000, Position.TOP_CENTER);
            return;
        }

        for (DetalleSalidaInventario det : listDet) {

            if (det.getCantidad() == null || det.getCantidad() <= 0) {
                Notification.show("El artículo '" + det.getArticulo().getDescripcion() + "' tiene una cantidad inválida.", 4000, Position.TOP_CENTER);
                return;
            }
        }

        try {
            // 2. Construir el objeto cabecera de SalidaInventario
            SalidaInventario salida = new SalidaInventario();
            salida.setFecha(ClaseUtil.asDate(dpFecha.getValue()));
            salida.setFechaRegistro(new Date());
            salida.setObservacion("Amin");

            // Asignar explícitamente la relación bidireccional en memoria
            listDet.forEach(e -> {
                e.setSalidaInventario(salida);
                e.setCodigo(null);
            });

            salida.setDetalleSalidaInventarioCollection(listDet);

            // TODO: Reemplazar "ADMIN" por el usuario del contexto de Vaadin/Spring Security
            String usuarioActual = "ADMIN";

            // 3. Invocar al servicio transaccional (Guarda la Salida + Descuenta Stock + Traza Movimiento)
            SalidaInventario guardada = this.salidaInvService.guardar(salida, usuarioActual);

            Notification.show("Salida #" + guardada.getCodigo() + " procesada y descontada del inventario correctamente.", 3000, Position.TOP_CENTER);

            // 4. Limpiar la interfaz
            listDet.clear();

            grid.getDataProvider().refreshAll();

        } catch (IllegalStateException ex) {
            // Captura excepciones de stock insuficiente disparadas por MovimientoInventarioService
            Notification.show(ex.getMessage(), 5000, Position.MIDDLE);
        } catch (IllegalArgumentException ex) {
            // Validaciones de negocio (artículos nulos, cantidades en cero, etc.)
            Notification.show(ex.getMessage(), 4000, Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar la salida: " + e.getMessage(), 4000, Position.TOP_CENTER);
            e.printStackTrace();
        }
    }

//    private void procesarGuardado() {
//        if (listDet.isEmpty()) {
//            Notification.show("La salida no tiene artículos registrados", 3000, Position.TOP_CENTER);
//            return;
//        }
//
//        for (DetalleSalidaInventario det : listDet) {
//            if (det.getCantidad() <= 0) {
//                Notification.show("El artículo '" + det.getDescripcionArticulo() + "' tiene cantidad en cero", 4000, Position.TOP_CENTER);
//                return;
//            }
//        }
//
//        try {
//            LocalDate localFecha = dpFecha.getValue();
//            Date fecha = ClaseUtil.asDate(localFecha);
//
//            SalidaInventario salidaInv = new SalidaInventario();
//            salidaInv.setFecha(fecha);
//            salidaInv.setFechaRegistro(new Date());
//
//            listDet.forEach(e -> {
//                e.setSalidaInventario(salidaInv);
//                e.setCodigo(null);
//            });
//
//            salidaInv.setDetalleSalidaInventarioCollection(listDet);
//
//            // Persistir la salida y actualizar/descontar el stock en la BD
//            this.salidaInvService.guardar(salidaInv);
//
//            Notification.show("Salida guardada exitosamente", 3000, Position.TOP_CENTER);
//            listDet.clear();
//            grid.getDataProvider().refreshAll();
//
//        } catch (Exception e) {
//            Notification.show("Error guardando la salida: " + e.getMessage(), 3000, Position.TOP_CENTER);
//            e.printStackTrace();
//        }
//    }
}
