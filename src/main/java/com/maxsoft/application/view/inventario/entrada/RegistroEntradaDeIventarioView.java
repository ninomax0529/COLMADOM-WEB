/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.entrada;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.servicio.interfaces.EntradaDeInventarioService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PageTitle("Registro Entrada de Inventario")
@Route(value = "inventario/registroEntrada")

public class RegistroEntradaDeIventarioView extends VerticalLayout {

    private Grid<DetalleEntradaInventario> grid = new Grid<>(DetalleEntradaInventario.class, false);
    Editor<DetalleEntradaInventario> editor = grid.getEditor();

    private TextField txtNumDoc = new TextField("Numero Entrada");
    private DatePicker dpFecha = new DatePicker("Fecha");
    TextField txtCantidad = new TextField();
    TextField txtBuscar = new TextField();

    private Button btnGuardar = new Button("Guardar");
    private Button btnSalir = new Button("Salir");
    Button btnNuevo = null;
    ArticuloService articuloServicel;

    List<DetalleEntradaInventario> listDet = new ArrayList<>();

    HorizontalLayout botones = new HorizontalLayout();

    private EntradaInventario entradaInv;
    EntradaDeInventarioService entradaInvService;
    int i = 0;

    @Autowired
    public RegistroEntradaDeIventarioView(EntradaDeInventarioService entradaServiceArg,
            ArticuloService articuloServiceArg,
            EntradaDeInventarioService entradaInvServiceArg) {

        this.entradaInvService = entradaInvServiceArg;
        this.articuloServicel = articuloServiceArg;

        btnSalir = new Button("Salir", e -> UI.getCurrent().navigate(EntradaDeIventarioView.class));

        btnGuardar = new Button("Guardar",
                event -> {

                    try {

                        if (listDet.size() <= 0) {

                            Notification.show("La entrada no tiene detalle ",
                                    3000, Notification.Position.TOP_CENTER);
                            return;
                        }

                        for (DetalleEntradaInventario det2 : listDet) {

                            if (det2.getCantidadRecibida() <= 0) {

                                Notification.show("El articulo  " + det2.getDescripcionArticulo() + " tiene la cantidad en cero ",
                                        4000, Notification.Position.TOP_CENTER);
                                return;
                            }

                        }

                        guardar();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

        btnNuevo = new Button("Ariculo(F2)",
                event -> {

                    try {

                        ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloServicel, entrada -> {

                            if (!(entrada == null)) {

                                i++;
                                DetalleEntradaInventario det1 = new DetalleEntradaInventario();

                                det1.setCodigo(entrada.getCodigo());//Colocarlo anull cuando le asignemo el encabezada
                                det1.setArticulo(entrada);
                                det1.setDescripcionArticulo(entrada.getDescripcion());
                                det1.setCantidadPedida(0.00);
                                det1.setCantidadRecibida(0.00);
                                det1.setCantidadPendiente(0.00);
                                det1.setExistenciaActual(0.00);
                                det1.setNuevaExistencia(0.00);
                                det1.setNombreAlmacen("General");
                                det1.setNombreUnidad("Unidad");
                                listDet.add(det1);

                                System.out.println("Articulo ingresó: " + entrada.getDescripcion());
//            
                                grid.getDataProvider().refreshAll(); // Refrescar el Grid sin perder la lista
                            }

                        });

                        dialog.open();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

        grid.setItems(listDet);

        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

        configurarGridDetalle();
        editarDetalle();
        configurarFormulario();

        add(btnNuevo, txtBuscar, grid);

    }

    private void configurarFormulario() {

        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha, btnGuardar, btnSalir);
        hlDatos.setAlignItems(Alignment.BASELINE);
        btnNuevo.addClickShortcut(Key.F2);

        FormLayout formLayout = new FormLayout(hlDatos);

        add(formLayout, botones);

    }

    private void guardar() {

        LocalDate localFecha = dpFecha.getValue();
        Date fecha = ClaseUtil.asDate(localFecha);

        try {

            entradaInv = new EntradaInventario();
            entradaInv.setFecha(fecha);
            entradaInv.setFechaCreacion(new Date());
            entradaInv.setFechaActualizacion(new Date());
            entradaInv.setNombreUsuario("Administrador");

//            entradaInventario.setTipoDocumento(1);
//            entradaInventario.setSecuenciaDocumento(new SecuenciaDocumento(i));
            listDet.stream().forEach(e -> {

                e.setEntradaInventario(entradaInv);
                e.setCodigo(null);
            });

            entradaInv.setDetalleEntradaInventarioCollection(listDet);
            this.entradaInvService.guardar(entradaInv);
            Notification.show("Entrada guardada exitosamente", 3000, Notification.Position.TOP_CENTER);
            listDet.clear();
            grid.getDataProvider().refreshAll();

        } catch (Exception e) {
            Notification.show("Error guardando la entrada ", 3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }

    }

    private void configurarGridDetalle() {

        grid.setHeight("620px");
        grid.setWidthFull();

        grid.setItems(listDet);

        GridListDataView<DetalleEntradaInventario> dataView = grid.setItems(listDet);

        grid.addColumn(DetalleEntradaInventario::getDescripcionArticulo).setHeader("Articulo")
                .setFooter("TOTAL ARTICULOS:");

//        grid.addColumn(DetalleEntradaInventario::getDescripcionArticulo)
//                .setHeader("Descripcion")
//                .setKey("descripcion");
        grid.addColumn(DetalleEntradaInventario::getCantidadRecibida)
                .setHeader("Cantidad")
                .setKey("cantidad");

        grid.addColumn(DetalleEntradaInventario::getNuevaExistencia)
                .setHeader("Existencia")
                .setKey("existencia");

        grid.addColumn(DetalleEntradaInventario::getExistenciaActual)
                .setHeader("Nueva Existencia")
                .setKey("nuevaexistencia");

        grid.addColumn(DetalleEntradaInventario::getNombreUnidad)
                .setHeader("Unidad")
                .setKey("unidad");

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        grid.getColumnByKey("cantidad").setEditorComponent(txtCantidad);

        grid.addColumn(new ComponentRenderer<>(item -> {

            Button deleteButton = new Button("🗑️ (F3) ", click -> {

                if (!txtBuscar.isEmpty()) {

                    listDet.remove(item);
                    grid.getDataProvider().refreshAll();
                    txtBuscar.clear();
                }

            });

            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            deleteButton.addClickShortcut(Key.F3);
            return deleteButton;

        })).setHeader("Acciones").setAutoWidth(true);

        dataView.addFilter(selectArticulo -> {

            String searchTerm = txtBuscar.getValue().trim();

            if (searchTerm.isEmpty()) {
                return true;
            }

            boolean matchesDescripcion = matchesTerm(selectArticulo.getArticulo().getDescripcion(),
                    searchTerm);
            boolean matchesCodigo = matchesTerm(selectArticulo.getCodigo().toString(), searchTerm);
            grid.select(selectArticulo);

            return matchesDescripcion || matchesCodigo;
        });

        txtBuscar.setWidth("50%");
        txtBuscar.setPlaceholder("Buscar");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
        txtBuscar.addValueChangeListener(e -> dataView.refreshAll());

        editarDetalle();
    }

    private void editarDetalle() {

        editor = grid.getEditor();
        editor.setBuffered(true); // sin botón de guardar

        TextField cantidadField = new TextField();
        cantidadField.setWidthFull();

        grid.getColumnByKey("cantidad").setEditorComponent(cantidadField);

        grid.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel(); // cerrar el editor anterior
                return;
            }

            editor.editItem(event.getItem());

        });

        editor.addOpenListener(e -> {

            DetalleEntradaInventario item = e.getItem();
            cantidadField.setValue(String.valueOf(item.getCantidadRecibida()));

        });

        cantidadField.addValueChangeListener(e -> {

            Double cantidad;

            DetalleEntradaInventario item = editor.getItem();

            try {

                cantidad = Double.valueOf(e.getValue());
                ////
////                if (!e.getValue().matches("\\d*")) {
////                    cantidadField.setInvalid(true);
////                    cantidadField.setErrorMessage("Debe ser un número");
////                    return;
////                } 

                if (cantidad <= 0) {

                    Notification.show("La cantidad no puede ser menor que cero ", 3000, Notification.Position.MIDDLE);
                    cantidad = 0.00;
                    return;
                }

                item.setCantidadRecibida(cantidad);

            } catch (NumberFormatException ex) {
                Notification.show(" Ingrese un número válido ", 2000, Notification.Position.MIDDLE);

                cantidadField.clear();
            }
        });

    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

}
