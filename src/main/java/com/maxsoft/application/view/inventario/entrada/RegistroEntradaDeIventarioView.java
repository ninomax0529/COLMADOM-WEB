/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.entrada;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.servicio.interfaces.EntradaDeInventarioService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.maxsoft.application.view.inventario.articulo.ConsultaArticuloDgView;
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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PageTitle("Registro Entrada de Inventario")
@Route(value = "inventario/registroEntrada")

public class RegistroEntradaDeIventarioView extends VerticalLayout {

    private Grid<DetalleEntradaInventario> grid = new Grid<>(DetalleEntradaInventario.class, false);
    private ListDataProvider<DetalleEntradaInventario> dataProvider = null;
    private ListDataProvider<Articulo> dataProviderArt;
    private Binder<DetalleEntradaInventario> binder = new Binder<>(DetalleEntradaInventario.class);
    Editor<DetalleEntradaInventario> editor = grid.getEditor();
    private final Dialog consultaArticuloDialog = new Dialog();
    private Grid<Articulo> gridArt = new Grid<>(Articulo.class, false);
    private Articulo articulo;

    DetalleEntradaInventario det;

    private TextField txtNumDoc = new TextField("Numero Entrada");
    private DatePicker dpFecha = new DatePicker("Fecha");
    TextField txtCantidad = new TextField();

    private Button btnGuardar = new Button("Guardar");
    private Button btnSalir = new Button("Salir");

    ArticuloService articuloServicel;

    List<DetalleEntradaInventario> listDet = new ArrayList<>();

    HorizontalLayout botones = new HorizontalLayout();
    TextField filterField = new TextField("Buscar artículo");

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

        Button btnNuevo = new Button("Agragar Ariculo",
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

        add(btnNuevo, grid);

    }

    private void configurarFormulario() {

        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha, btnGuardar, btnSalir);
        hlDatos.setAlignItems(Alignment.BASELINE);

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

        } catch (Exception e) {
            Notification.show("Error guardando la entrada ", 3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }

    }

    private void configurarGridDetalle() {

        grid.setHeight("620px");
        grid.setWidthFull();

        grid.setItems(listDet);

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

            Button deleteButton = new Button("🗑️ Eliminar", click -> {
                listDet.remove(item);
                grid.getDataProvider().refreshAll();
                Notification.show("Articulo eliminado");
            });

            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return deleteButton;
        })).setHeader("Acciones").setAutoWidth(true);

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

}
