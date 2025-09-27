/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.salida;

/**
 *
 * @author maximilianoalmonte
 */

import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.servicio.interfaces.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.SalidaInventarioService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.ToolBarBotonera;
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
import jakarta.annotation.security.PermitAll;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PermitAll
@PageTitle("Registro Salida de Inventario")
@Route(value = "inventario/registroSalida")

public class RegistroSalidaDeIventarioView extends VerticalLayout {

    private SalidaInventario salidaInv;
    SalidaInventarioService salidaInvService;
    private Grid<DetalleSalidaInventario> grid = new Grid<>(DetalleSalidaInventario.class, false);
    Editor<DetalleSalidaInventario> editor = grid.getEditor();

    private TextField txtNumDoc = new TextField("Numero Salida");
    private DatePicker dpFecha = new DatePicker("Fecha");
    TextField txtCantidad = new TextField();
    TextField txtBuscar = new TextField();
    ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    FormLayout formLayout = new FormLayout();
    Button btnNuevo = null;
    ArticuloService articuloServicel;

    List<DetalleSalidaInventario> listDet = new ArrayList<>();

    HorizontalLayout hlArticulo = new HorizontalLayout();

    int i = 0;

    @Autowired
    public RegistroSalidaDeIventarioView(SalidaInventarioService salidaInvServiceArg,
            ArticuloService articuloServiceArg,
            EntradaDeInventarioService entradaInvServiceArg) {

        this.salidaInvService = salidaInvServiceArg;
        this.articuloServicel = articuloServiceArg;

        botonera.getGuardar().addClickListener(e -> {
            // lógica de guardar

            if (listDet.size() <= 0) {

                Notification.show("La salida no tiene detalle ",
                        3000, Notification.Position.TOP_CENTER);
                return;
            }

            for (DetalleSalidaInventario det2 : listDet) {

                if (det2.getCantidad() <= 0) {

                    Notification.show("El articulo  " + det2.getDescripcionArticulo()+ " tiene la cantidad en cero ",
                            4000, Notification.Position.TOP_CENTER);
                    return;
                }

            }

            guardar();

        });

        botonera.getCancelar().addClickListener(e -> {
            // lógica de cancelar
            UI.getCurrent().navigate(SalidaDeIventarioView.class);
        });

        btnNuevo = new Button("Ariculo(F2)",
                event -> {

                    try {

                        ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloServicel, articulo -> {

                            if (!(articulo == null)) {

                                i++;
                                DetalleSalidaInventario det1 = new DetalleSalidaInventario();

                                det1.setCodigo(articulo.getCodigo());//Colocarlo anull cuando le asignemo el encabezada
                                det1.setArticulo(articulo.getCodigo());
                                det1.setDescripcionArticulo(articulo.getDescripcion());

                                det1.setCantidad(articulo.getExistencia());
//                                det1.setCantidadRecibida(0.00);
//                                det1.setCantidadPendiente(0.00);
//                                det1.setExistenciaActual(0.00);
//                                det1.setNuevaExistencia(0.00);
//                                det1.setNombreAlmacen("General");
//                                det1.setNombreUnidad(articulo.getNombreEmbase());
//                                det1.setUnidad(articulo.getUnidada());
                                listDet.add(det1);

                                System.out.println("Articulo ingresó: " + articulo.getDescripcion());
//            
                                grid.getDataProvider().refreshAll(); // Refrescar el Grid sin perder la lista
                            }

                        });

                        dialog.open();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

        txtBuscar.setSizeFull();
        hlArticulo.setSizeFull();
        hlArticulo.add(txtBuscar, btnNuevo);
        grid.setItems(listDet);

        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

        configurarGridDetalle();
        editarDetalle();
        configurarFormulario();

        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha, botonera);
        hlDatos.setAlignItems(Alignment.BASELINE);

        add(hlDatos, hlArticulo, grid);

    }

    private void configurarFormulario() {

//        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha);
//        hlDatos.setAlignItems(Alignment.BASELINE);
        btnNuevo.addClickShortcut(Key.F2);
//        formLayout.add(hlDatos);

    }

    private void guardar() {

        LocalDate localFecha = dpFecha.getValue();
        Date fecha = ClaseUtil.asDate(localFecha);

        try {

            salidaInv = new SalidaInventario();
            salidaInv.setFecha(fecha);
            salidaInv.setFecha(new Date());
            salidaInv.setFechaRegistro(new Date());
//            salidaInv.setNombreUsuario("Administrador");
            salidaInv.setUsuario(new Usuario(i));
//            salidaInv.setNombreUsuario("admin");

//            entradaInventario.setTipoDocumento(1);
//            entradaInventario.setSecuenciaDocumento(new SecuenciaDocumento(i));
            listDet.stream().forEach(e -> {

//                e.setSalidaInventario(salidaInv);
                e.setCodigo(null);
            });

//            salidaInv.setDetalleSalidaInventarioCollection(listDet);
            this.salidaInvService.guardar(salidaInv);
            Notification.show("Salida guardada exitosamente", 3000, Notification.Position.TOP_CENTER);
            listDet.clear();
            grid.getDataProvider().refreshAll();

        } catch (Exception e) {
            Notification.show("Error guardando la salida ", 3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }

    }

    private void configurarGridDetalle() {

        grid.setHeight("620px");
        grid.setWidthFull();

        grid.setItems(listDet);

        GridListDataView<DetalleSalidaInventario> dataView = grid.setItems(listDet);

        grid.addColumn(DetalleSalidaInventario::getDescripcionArticulo).setHeader("Articulo")
                .setFooter("TOTAL ARTICULOS:");

        grid.addColumn(DetalleSalidaInventario::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad");

//        grid.addColumn(DetalleEntradaInventario::getCantidadRecibida)
//                .setHeader("Cantidad")
//                .setKey("cantidad");
//
//        grid.addColumn(DetalleEntradaInventario::getNuevaExistencia)
//                .setHeader("Existencia")
//                .setKey("existencia");
//
//        grid.addColumn(DetalleEntradaInventario::getExistenciaActual)
//                .setHeader("Nueva Existencia")
//                .setKey("nuevaexistencia");
        grid.addColumn(DetalleSalidaInventario::getUnidad)
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
//
//            boolean matchesDescripcion = matchesTerm(selectArticulo.get(),
//                    searchTerm);
            boolean matchesCodigo = matchesTerm(selectArticulo.getCodigo().toString(), searchTerm);
            grid.select(selectArticulo);

            return  matchesCodigo;
        });

        txtBuscar.setWidth("50%");
        txtBuscar.setPlaceholder("Filtrar");
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

            DetalleSalidaInventario item = e.getItem();
            cantidadField.setValue(String.valueOf(item.getCantidad()));

        });

        cantidadField.addValueChangeListener(e -> {

            Double cantidad;

            DetalleSalidaInventario item = editor.getItem();

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

                item.setCantidad(cantidad);

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
