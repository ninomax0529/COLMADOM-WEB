package com.maxsoft.application.view.inventario;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.reposittorio.EntradaInvRepo;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.util.ClaseUtil;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Inventario de Productos")
@Route("InventarioDeProducto")
@Menu(order = 6, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class ArticuloView extends Composite<VerticalLayout> {

    @Autowired
    ArticuloService inventarioService;

    Grid<DetalleEntradaInventario> basicGrid;

    DatePicker datePicker = new DatePicker(LocalDate.now());

    List<DetalleEntradaInventario> listaArticulo;
    ListDataProvider<DetalleEntradaInventario> dataProvider;
    Button saveButtonActualizar;

    Integer invFinal = 0, diferencia = 0, invInicial = 0;
    Date fecha;
    public Button saveButton;

    private Div footerDivRecibida;
    private Div footeDivempacada;
    private Div footeDivInvFisico;
    private Div footeDivInvRotas;

    boolean editar = false;
    @Autowired
    EntradaInvRepo entradaInvRepo;

    int i = 0;

    public ArticuloView(ArticuloService inventarioServiceArg, EntradaInvRepo entradaInvRepoArg) {

        this.inventarioService = inventarioServiceArg;
        this.entradaInvRepo = entradaInvRepoArg;
        basicGrid = new Grid<>(DetalleEntradaInventario.class, false);

        listaArticulo = new ArrayList<>();
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        VerticalLayout layoutRow3 = new VerticalLayout();
        datePicker.setLabel("Fecha");

        basicGrid.setHeight("600px");

        basicGrid.addColumn(DetalleEntradaInventario::getArticulo).setHeader("Item");
        basicGrid.addColumn(DetalleEntradaInventario::getDescripcionArticulo).setHeader("Descripcion");
//        basicGrid.addColumn(DetalleEntradaInventario::getPrecioCompra).setHeader("Precio");
        basicGrid.setItems(listaArticulo);

        Button btnNuevo = new Button("Nuevo",
                event -> {

                    try {

                        ConsultaArticulosDialog dialog = new ConsultaArticulosDialog(inventarioService, entrada -> {

                            if (!(entrada == null)) {

                                i++;
                                DetalleEntradaInventario det = new DetalleEntradaInventario();

                                det.setCodigo(entrada.getCodigo());
                                det.setArticulo(entrada.getCodigo());
                                det.setDescripcionArticulo(entrada.getDescripcion());
                                listaArticulo.add(det);

//                                basicGrid.setItems(listaArticulo);
                                System.out.println("Articulo ingresó: " + entrada.getDescripcion());
//                                basicGrid.getListDataView().refreshAll();
                                basicGrid.getDataProvider().refreshAll(); // Refrescar el Grid sin perder la lista
                            }

                        });

                        dialog.open();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

        Button btnBuscar = new Button("Buscar",
                event -> {

                    buscar();

                });

        Button btnGuardar = new Button("Guardar",
                event -> {

                    guardar();

                });

        layoutRow3.setSizeFull();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRow2.setHeightFull();
        layoutRow.setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.addClassName(Padding.XSMALL);
        layoutRow2.setWidth("100%");
        layoutRow2.setHeight("70px");

        layoutRow3.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow3);
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.getStyle().set("flex-grow", "1");
        layoutRow3.setHeight("min-content");
        btnGuardar.setText("Guardar");
        btnGuardar.setWidth("min-content");
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.setText("Buscar");
        btnBuscar.setWidth("min-content");
        btnNuevo.setText("Nuevo");
        btnNuevo.setWidth("min-content");
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        basicGrid.setWidth("100%");
        basicGrid.addClassName("my-grid");
        basicGrid.getStyle().set("flex-grow", "0");

        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.setAlignItems(FlexComponent.Alignment.BASELINE);
        layoutRow2.add(datePicker);

//        configurarGrid();
        layoutRow2.add(btnNuevo);
        layoutRow2.add(btnBuscar);
        layoutRow2.add(btnGuardar);

        layoutRow3.add(basicGrid);
//        layoutRow3.add(movimientoProductoGrid);
        getContent().add(layoutRow3);

    }

    private void guardar() {

        try {

            fecha = ClaseUtil.asDate(datePicker.getValue());

            EntradaInventario entr = new EntradaInventario();

            listaArticulo.stream().forEach(e -> {

                e.setCodigo(null);
                e.setEntrada(entr);

            });

            entr.setDetalleEntradaInventarioCollection(listaArticulo);
            entr.setFecha(fecha);

            this.entradaInvRepo.save(entr);

            listaArticulo.clear();

            Notification.show("Registro actualizado exitosamente ", 3000, Notification.Position.MIDDLE);

        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error guardando ", 3000, Notification.Position.MIDDLE);
            System.out.println("Error actualizando : " + e.getMessage());
        }

    }

    private void nuevo() {

        try {

            if (editar == false) {

                dataProvider = new ListDataProvider<>(listaArticulo);

                basicGrid.setDataProvider(dataProvider);
                guardar();
            }

//                     updateFooterMovimiento();
        } catch (Exception e) {
            System.out.println("Error buscando : " + e.getMessage());
        }

    }

    private void buscar() {

        try {

            dataProvider = new ListDataProvider<>(listaArticulo);
            basicGrid.setDataProvider(dataProvider);
            editar = true;

            updateFooterMovimiento();

            dataProvider.refreshAll();

        } catch (Exception e) {
            System.out.println("Error buscando : " + e.getMessage());
        }

    }

    private void configurarGrid() {

        Binder<DetalleEntradaInventario> binder = new Binder<>(DetalleEntradaInventario.class);
        Editor<DetalleEntradaInventario> editor = basicGrid.getEditor();
        editor.setBinder(binder);

        basicGrid.setHeight("600px");

//
//        basicGrid.addColumn(DetalleCementoDejadoEnPiso::getNombreProducto).setHeader("Producto").setFooter("Total :");
//        basicGrid.addColumn(DetalleCementoDejadoEnPiso::getCantidadAyer).setHeader("Ayer").setKey("ayer");
//        basicGrid.addColumn(DetalleCementoDejadoEnPiso::getEntrada).setHeader("Entrada").setKey("entrada");
//        basicGrid.addColumn(DetalleCementoDejadoEnPiso::getSalida).setHeader("Salida").setKey("salida");
//        basicGrid.addColumn(DetalleCementoDejadoEnPiso::getCantidadHoy).setHeader("Hoy").setKey("hoy");
        TextField txtHoy = new TextField();
        TextField txtAyer = new TextField();
        TextField txtSalida = new TextField();
        TextField txtEntrada = new TextField();

        basicGrid.getColumnByKey("ayer").setEditorComponent(txtAyer);
        basicGrid.getColumnByKey("entrada").setEditorComponent(txtEntrada);
        basicGrid.getColumnByKey("salida").setEditorComponent(txtSalida);
        basicGrid.getColumnByKey("hoy").setEditorComponent(txtHoy);

        footerDivRecibida = new Div();
        footeDivempacada = new Div();
        footeDivInvFisico = new Div();
        footeDivInvRotas = new Div();

        footerDivRecibida.addClassName("custom-footer");
        footeDivInvFisico.addClassName("custom-footer");
        footeDivInvRotas.addClassName("custom-footer");
        footeDivempacada.addClassName("custom-footer");

        basicGrid.getColumnByKey("ayer").setFooter(footerDivRecibida);
        basicGrid.getColumnByKey("entrada").setFooter(footeDivempacada);
        basicGrid.getColumnByKey("salida").setFooter(footeDivInvFisico);
        basicGrid.getColumnByKey("hoy").setFooter(footeDivInvRotas);

        updateFooterMovimiento();

        Grid.Column<DetalleEntradaInventario> editColumn = basicGrid.addComponentColumn(det -> {

            Button editButton = new Button("Editar");

//            editButton.addClassName("sinprocesar");
            editButton.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }
                basicGrid.getEditor().editItem(det);

            });
//
//            txtHoy.setValue(Integer.toString(det.getCantidadHoy()));
//            txtAyer.setValue(Integer.toString(det.getCantidadAyer()));
//            txtEntrada.setValue(Integer.toString(det.getEntrada()));
//            txtSalida.setValue(Integer.toString(det.getSalida()));

            txtHoy.focus();

            return editButton;
        }).setWidth("150px").setFlexGrow(1).setKey("editar");

        saveButtonActualizar = new Button("Guardar", e -> {

            try {

                if (txtHoy.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtHoy.focus();
                    return;
                }
                if (txtAyer.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtAyer.focus();
                    return;
                }
                if (txtEntrada.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtEntrada.focus();
                    return;
                }

                if (txtSalida.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtSalida.focus();
                    return;
                }

                String valorAyer = txtAyer.getValue().isEmpty() ? "0" : txtAyer.getValue();
                Integer ayer = Integer.valueOf(valorAyer);

                String valorHoy = txtHoy.getValue().isEmpty() ? "0" : txtHoy.getValue();
                Integer hoy = Integer.valueOf(valorHoy);

                String valorEntrada = txtEntrada.getValue().isEmpty() ? "0" : txtEntrada.getValue();
                Integer entrada = Integer.valueOf(valorEntrada);

                String valorSalida = txtSalida.getValue().isEmpty() ? "0" : txtSalida.getValue();
                Integer salida = Integer.valueOf(valorSalida);

                hoy = ayer + entrada - salida;
//
//                editor.getItem().setCantidadAyer(ayer);
//                editor.getItem().setCantidadHoy(hoy);
//                editor.getItem().setEntrada(entrada);
//                editor.getItem().setSalida(salida);

                updateFooterMovimiento();
//
//                listaArticulo.forEach(det -> {
//
//                    det.setCementoDejadoEnPiso(cementoDejadoEnPiso);
//
//                });
//
//                cementoDejadoEnPiso.setDetalleCementoDejadoEnPisoCollection(listaArticulo);
//
//                this.inventarioService.guardar(cementoDejadoEnPiso);

                dataProvider.refreshAll();

                txtHoy.clear();
                txtAyer.clear();
                txtSalida.clear();
                txtEntrada.clear();

            } catch (Exception ex) {
                ex.printStackTrace();

                Notification.show("Hubo un problema guardando los datos ", 3000, Notification.Position.MIDDLE);
                Logger.getLogger(ArticuloView.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        saveButtonActualizar.addClickShortcut(Key.ENTER);

        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                e -> editor.cancel());

        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_WARNING);

        HorizontalLayout actions = new HorizontalLayout(saveButtonActualizar,
                cancelButton);

        actions.setPadding(true);

        editColumn.setEditorComponent(actions);

//        basicGrid.getColumns().forEach(col -> {
//            col.setAutoWidth(true);
//            col.setSortable(true);
//        });
        editor.addCancelListener(e -> {

        });

    }

    // Método para actualizar el total en el pie de página
    private void updateFooterMovimiento() {

        Integer totalAyer = 0,
                totalHoy = 0,
                totalEntrada = 0,
                totalSalida = 0;

        if (!(listaArticulo == null)) {

//            for (DetalleCementoDejadoEnPiso det1 : listaArticulo) {
//
//                totalAyer += det1.getCantidadAyer();
//                totalHoy += det1.getCantidadHoy();
//                totalEntrada += det1.getEntrada();
//                totalSalida += det1.getSalida();
//
//            }
            footeDivempacada.setText(totalAyer.toString());
            footerDivRecibida.setText(totalHoy.toString());
            footeDivInvFisico.setText(totalEntrada.toString());
            footeDivInvRotas.setText(totalSalida.toString());

        }

    }

}
