package com.maxsoft.application.view.inventario.entrada;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@PermitAll
@PageTitle("Registro Entrada de Inventario (Multi-Empaque)")
@Route(value = "inventario/registroEntrada")
public class RegistroEntradaDeIventarioView extends VerticalLayout {

    private final Grid<DetalleEntradaInventario> grid = new Grid<>(DetalleEntradaInventario.class, false);
    private Editor<DetalleEntradaInventario> editor;

    private final TextField txtNumDoc = new TextField("Número Entrada");
    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén Destino");
    private final TextField txtObservacion = new TextField("Observación / Comentario");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final ArticuloService articuloService;
    private final EntradaDeInventarioService entradaInvService;
    private final AlmacenService almacenService;
    private final ArticuloAlmacenService existenciaService;
    private final ArticuloEmpaqueService articuloEmpaqueService;

    private final List<DetalleEntradaInventario> listDet = new ArrayList<>();

    @Autowired
    public RegistroEntradaDeIventarioView(EntradaDeInventarioService entradaInvService,
            ArticuloService articuloService,
            AlmacenService almacenService,
            ArticuloAlmacenService existenciaService,
            ArticuloEmpaqueService articuloEmpaqueService) {

        this.entradaInvService = entradaInvService;
        this.articuloService = articuloService;
        this.almacenService = almacenService;
        this.existenciaService = existenciaService;
        this.articuloEmpaqueService = articuloEmpaqueService;

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
        botonera.getCancelar().addClickListener(e -> UI.getCurrent().navigate(EntradaDeIventarioView.class));
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

        for (DetalleEntradaInventario det : listDet) {
            det.setAlmacen(nuevoAlmacen);
            det.setNombreAlmacen(nuevoAlmacen.getNombre());

            BigDecimal stockEnAlmacen = obtenerExistenciaPorAlmacen(det.getArticulo(), nuevoAlmacen);
            det.setExistenciaActual(stockEnAlmacen);

            recalcularNuevaExistencia(det);
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
            ClaseUtil.mostrarNotificacion("Debe seleccionar un Almacén Destino primero", NotificationVariant.LUMO_WARNING);
            cbAlmacen.focus();
            return;
        }

        try {
            ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloService, articulo -> {
                if (articulo != null) {
                    // Abrir diálogo de empaque igual que en la salida de inventario
                    abrirDialogoSeleccionEmpaque(articulo);
                }
            });
            dialog.open();
        } catch (Exception e) {
            Notification.show("Error abriendo diálogo de artículos: " + e.getMessage(), 3000, Position.TOP_CENTER);
        }
    }

    private void abrirDialogoSeleccionEmpaque(Articulo articulo) {
        EmpaqueSeleccionDialogo dialogoEmpaque = new EmpaqueSeleccionDialogo(
                articulo,
                articuloEmpaqueService,
                empaqueSeleccionado -> {
                    if (empaqueSeleccionado != null) {
                        agregarOActualizarArticulo(articulo, empaqueSeleccionado);
                    }
                }
        );
        dialogoEmpaque.open();
    }

    private void agregarOActualizarArticulo(Articulo articulo, ArticuloEmpaque empaqueSeleccionado) {
        if (articulo == null || empaqueSeleccionado == null) {
            Notification.show("Seleccione un artículo y empaque válidos", 3000, Position.TOP_CENTER);
            return;
        }

        Almacen almacenSeleccionado = cbAlmacen.getValue();
        BigDecimal stockActual = obtenerExistenciaPorAlmacen(articulo, almacenSeleccionado);

        boolean existe = listDet.stream()
                .anyMatch(d -> d.getArticulo() != null && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())
                && d.getArticuloEmpaque() != null && Objects.equals(d.getArticuloEmpaque().getCodigo(), empaqueSeleccionado.getCodigo()));

        if (existe) {
            listDet.forEach(d -> {
                if (Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())
                        && d.getArticuloEmpaque() != null && Objects.equals(d.getArticuloEmpaque().getCodigo(), empaqueSeleccionado.getCodigo())) {
                    recalcularNuevaExistencia(d);
                }
            });
        } else {

            DetalleEntradaInventario det = new DetalleEntradaInventario();
            
            det.setCodigo(empaqueSeleccionado.getCodigo());            
            det.setArticuloEmpaque(empaqueSeleccionado);
            det.setNombreUnidad(empaqueSeleccionado.getUnidadEmpaque() != null ? empaqueSeleccionado.getUnidadEmpaque().getDescripcion() : "Empaque");
            det.setUnidad(empaqueSeleccionado.getUnidadEmpaque());
            det.setFactorConversion(empaqueSeleccionado.getFactorConversion());
            
            det.setArticulo(articulo);
            det.setDescripcionArticulo(articulo.getDescripcion());
            det.setAlmacen(almacenSeleccionado);
            det.setNombreAlmacen(almacenSeleccionado.getNombre());


            det.setExistenciaActual(stockActual);
            det.setCantidadPedida(BigDecimal.ZERO);
            det.setCantidadRecibida(BigDecimal.ONE);
            det.setCantidadPendiente(BigDecimal.ZERO);

            BigDecimal precioEmpaque = empaqueSeleccionado.getPrecioCompra() != null ? empaqueSeleccionado.getPrecioCompra() : articulo.getPrecioCompra();
            
            det.setPrecioCompra(precioEmpaque);
            det.setCostoUnitario(precioEmpaque);
            det.setPrecioVenta(articulo.getPrecioVenta());
            det.setCantidadFisicaBase(det.getFactorConversion().multiply(det.getCantidadRecibida()));
            det.setItbis(BigDecimal.ZERO);
            det.setSubTotal(BigDecimal.ZERO);
            det.setTotal(BigDecimal.ZERO);

            recalcularNuevaExistencia(det);
            listDet.add(det);
        }
        grid.getDataProvider().refreshAll();
    }

    private void recalcularNuevaExistencia(DetalleEntradaInventario det) {

        BigDecimal cantRecibida = det.getCantidadRecibida() != null ? det.getCantidadRecibida() : BigDecimal.ZERO;
        BigDecimal factor = BigDecimal.ONE;

        if (det.getArticuloEmpaque() != null && det.getArticuloEmpaque().getUnidadEmpaque() != null) {
            factor = det.getArticuloEmpaque().getFactorConversion();
        }

        BigDecimal cantidadBaseEfectiva = cantRecibida.multiply(factor);
        BigDecimal stockActual = det.getExistenciaActual() != null ? det.getExistenciaActual() : BigDecimal.ZERO;

        det.setNuevaExistencia(stockActual.add(cantidadBaseEfectiva));
    }

    private void configurarGridDetalle() {
        grid.setHeightFull();
        grid.setWidthFull();

        GridListDataView<DetalleEntradaInventario> dataView = grid.setItems(listDet);

        grid.addColumn(DetalleEntradaInventario::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true);

        grid.addColumn(DetalleEntradaInventario::getNombreAlmacen)
                .setHeader("Almacén Destino")
                .setAutoWidth(true);

        grid.addColumn(DetalleEntradaInventario::getExistenciaActual)
                .setHeader("Existencia Actual")
                .setAutoWidth(true);

        grid.addColumn(item -> item.getArticuloEmpaque() != null && item.getArticuloEmpaque().getUnidadEmpaque() != null
                ? item.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : item.getNombreUnidad())
                .setHeader("Empaque / Unidad")
                .setKey("empaque")
                .setAutoWidth(true);

        grid.addColumn(DetalleEntradaInventario::getCantidadRecibida)
                .setHeader("Cantidad Entrada")
                .setKey("cantidad")
                .setAutoWidth(true);

        grid.addColumn(DetalleEntradaInventario::getNuevaExistencia)
                .setHeader("Nueva Existencia")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' de la entrada?",
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

        ComboBox<ArticuloEmpaque> cbEmpaque = new ComboBox<>();
        cbEmpaque.setWidthFull();
        cbEmpaque.setItemLabelGenerator(ae -> ae.getUnidadEmpaque() != null ? ae.getUnidadEmpaque().getDescripcion() : "");

        grid.getColumnByKey("cantidad").setEditorComponent(cantidadField);
        grid.getColumnByKey("empaque").setEditorComponent(cbEmpaque);

        grid.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel();
            }

            DetalleEntradaInventario selectedItem = event.getItem();
            if (selectedItem != null && selectedItem.getArticulo() != null) {
                List<ArticuloEmpaque> empaquesValidos = articuloEmpaqueService.getPorArticulo(selectedItem.getArticulo().getCodigo());
                cbEmpaque.setItems(empaquesValidos);
                if (selectedItem.getArticuloEmpaque() != null) {
                    cbEmpaque.setValue(selectedItem.getArticuloEmpaque());
                }
            }

            editor.editItem(selectedItem);
        });

        cbEmpaque.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleEntradaInventario itemActual = editor.getItem();
            ArticuloEmpaque nuevoEmpaque = e.getValue();

            if (nuevoEmpaque != null) {
                itemActual.setArticuloEmpaque(nuevoEmpaque);
                itemActual.setNombreUnidad(nuevoEmpaque.getUnidadEmpaque() != null ? nuevoEmpaque.getUnidadEmpaque().getDescripcion() : "Empaque");

                if (nuevoEmpaque.getPrecioCompra() != null && nuevoEmpaque.getPrecioCompra().compareTo(BigDecimal.ZERO) > 0) {
                    itemActual.setPrecioCompra(nuevoEmpaque.getPrecioCompra());
                    itemActual.setCostoUnitario(nuevoEmpaque.getPrecioCompra());
                }

                recalcularNuevaExistencia(itemActual);
                grid.getDataProvider().refreshItem(itemActual);
            }
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

                if (cant.compareTo(BigDecimal.ZERO) <= 0) {
                    Notification.show("La cantidad debe ser mayor a cero", 2500, Position.MIDDLE);
                    return;
                }

                DetalleEntradaInventario itemActual = editor.getItem();
                itemActual.setCantidadRecibida(cant);

                recalcularNuevaExistencia(itemActual);
                grid.getDataProvider().refreshItem(itemActual);

            } catch (Exception ex) {
                Notification.show("Ingrese una cantidad válida", 2000, Position.MIDDLE);
            }
        });
    }

    private void procesarGuardado() {
        if (cbAlmacen.getValue() == null) {
            Notification.show("Debe seleccionar un Almacén Destino", 3000, Position.TOP_CENTER);
            cbAlmacen.focus();
            return;
        }

        if (listDet.isEmpty()) {
            Notification.show("La entrada no tiene artículos registrados", 3000, Position.TOP_CENTER);
            return;
        }

        for (DetalleEntradaInventario det : listDet) {
            if (det.getCantidadRecibida() == null || det.getCantidadRecibida().compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show("El artículo '" + det.getDescripcionArticulo() + "' tiene cantidad en cero o inválida", 4000, Position.TOP_CENTER);
                return;
            }
        }

        try {
            EntradaInventario entradaInv = new EntradaInventario();
            entradaInv.setFecha(ClaseUtil.asDate(dpFecha.getValue()));
            entradaInv.setFechaCreacion(new Date());
            entradaInv.setFechaActualizacion(new Date());
            entradaInv.setNombreUsuario("Administrador");
            entradaInv.setComentario(txtObservacion.getValue().isEmpty() ? "Entrada por proveedor" : txtObservacion.getValue());

            listDet.forEach(e -> {
                e.setEntradaInventario(entradaInv);
                e.setAlmacen(cbAlmacen.getValue());
                e.setCodigo(null);
            });

            entradaInv.setDetalleEntradaInventarioCollection(listDet);

            String usuarioActual = "Admin";

            EntradaInventario guardada = this.entradaInvService.guardar(entradaInv, usuarioActual);

            Notification.show("Entrada guardada exitosamente", 3000, Position.TOP_CENTER);

            listDet.clear();
            txtObservacion.clear();
            grid.getDataProvider().refreshAll();

        } catch (Exception e) {
            Notification.show("Error guardando la entrada: " + e.getMessage(), 3000, Position.TOP_CENTER);
            e.printStackTrace();
        }
    }
}
