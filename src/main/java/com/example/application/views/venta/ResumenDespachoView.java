package com.example.application.views.venta;

import com.example.application.modelo.DetalleProyeccionDeVenta;
import com.example.application.modelo.DetalleResumenDespacho;
import com.example.application.modelo.Producto;
import com.example.application.modelo.ProyecconDeVenta;
import com.example.application.modelo.ResumenDespacho;
import com.example.application.modelo.Usuariop;
import com.example.application.services.AlmacenService;
import com.example.application.services.DespachoService;
import com.example.application.services.OperacionEmpacadoraService;
import com.example.application.services.ProductoService;
import com.example.application.services.ProyeccionDeVentaService;
import com.example.application.services.ResumenDespachoService;
import com.example.application.util.ClaseUtil;
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
import com.vaadin.flow.component.notification.Notification.Position;
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

@PageTitle("Resumen de Despacho")
@Route("ResumenDespacho")
@Menu(order = 7, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class ResumenDespachoView extends Composite<VerticalLayout> {

    @Autowired
    ResumenDespachoService resumenService;
    @Autowired
    ProductoService productoService;
    @Autowired
    DespachoService despachoService;
    @Autowired
    OperacionEmpacadoraService operacionEmpacadoraService;

    Grid<DetalleResumenDespacho> basicGrid;

    DatePicker datePicker = new DatePicker(LocalDate.now());

    List<DetalleResumenDespacho> listaDet;
    ListDataProvider<DetalleResumenDespacho> dataProvider;

    ResumenDespacho reumemenDesp;

    Date fecha, ff, fi;
    public Button saveButton;

    private Div footeDivEnvasado;
    private Div footeDivDespachado;
    private Div footeDivDespachoAcumulado;

    boolean editar = false;

    public ResumenDespachoView(ResumenDespachoService resumenServiceArg,
            ProductoService productoServiceArg, DespachoService despachoServiceArg,
            OperacionEmpacadoraService operacionEmpacadoraArg) {

        this.resumenService = resumenServiceArg;
        this.productoService = productoServiceArg;
        this.despachoService = despachoServiceArg;

        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        VerticalLayout layoutRow3 = new VerticalLayout();
        datePicker.setLabel("Fecha");

        Button btnNuevo = new Button("Nuevo",
                event -> {

                    try {

                        buscar();
                        nuevo();
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

        basicGrid = new Grid();

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

        configurarGrid();

        layoutRow2.add(btnNuevo);
        layoutRow2.add(btnBuscar);
        layoutRow2.add(btnGuardar);

        layoutRow3.add(basicGrid);
//        layoutRow3.add(movimientoProductoGrid);
        getContent().add(layoutRow3);

    }

    private void guardar() {

        try {

            if (reumemenDesp == null) {

                Notification.show("No hay regsitro para guardar ", 3000, Notification.Position.MIDDLE);
                return;
            }

            fecha = ClaseUtil.asDate(datePicker.getValue());
            reumemenDesp.setFecha(fecha);
            reumemenDesp.setDetalleResumenDespachoCollection(dataProvider.getItems());

            reumemenDesp = this.resumenService.guardar(reumemenDesp);

            listaDet.clear();

            listaDet.addAll(this.resumenService.getDetalle(reumemenDesp.getCodigo()));

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

                reumemenDesp = new ResumenDespacho();

                reumemenDesp.setFechaRegistro(new Date());
                reumemenDesp.setUsuario(new Usuariop(1));
                reumemenDesp.setFecha(fecha);

                DetalleResumenDespacho detalle;

                List<Producto> listaProd = this.productoService.getLista();
                listaDet = new ArrayList<>();

                for (Producto p : listaProd) {

                    detalle = new DetalleResumenDespacho();
                    detalle.setResumenDespacho(reumemenDesp);
                    detalle.setProducto(p);
                    detalle.setNombreProducto(p.getDescripcion());
                    detalle.setDespachado(0);
                    detalle.setEnvasado(0);
                    detalle.setDespachoAcumulado(0);
                    detalle.setEnvasdoAcumulada(0);

                    listaDet.add(detalle);

                }

                dataProvider = new ListDataProvider<>(listaDet);

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

            fecha = ClaseUtil.asDate(datePicker.getValue());

            reumemenDesp = this.resumenService.getResumenDespacho(fecha);

            System.out.println("Op : " + reumemenDesp);

            if (reumemenDesp == null) {

                Notification.show("No hay regsitro en esta fecha !", 4000, Position.MIDDLE);

                return;
            }

            listaDet = new ArrayList<>();
            listaDet.addAll(this.resumenService.getDetalle(reumemenDesp.getCodigo()));

            int cantidadDespacho, cantidadDespachoAcum, diferencia;

            for (DetalleResumenDespacho detaEmcado : listaDet) {

                if (detaEmcado.getProducto().getCodigo() == 4) {

                    cantidadDespacho = this.despachoService.getDespacho("23", detaEmcado.getProducto().getCodigoSap(), fecha).intValue();
                    cantidadDespacho += this.despachoService.getDespachoPorTransferencia("23", detaEmcado.getProducto().getCodigoSap(), fecha).intValue();
                    cantidadDespachoAcum = cantidadDespacho;

                    cantidadDespachoAcum += this.despachoService.getDespacho("13", detaEmcado.getProducto().getCodigoSap(), fecha).intValue();
                    cantidadDespachoAcum += this.despachoService.getDespachoPorTransferencia("13", detaEmcado.getProducto().getCodigoSap(), fecha).intValue();

                } else {

                    cantidadDespacho = this.despachoService.getDespacho("23", detaEmcado.getProducto().getCodigoSap(), fecha).intValue();
                    cantidadDespachoAcum = this.despachoService.getDespacho("13", detaEmcado.getProducto().getCodigoSap(), fecha).intValue();
                    cantidadDespachoAcum += this.despachoService.getDespacho("23", detaEmcado.getProducto().getCodigoSap(), fecha).intValue();
                }

                detaEmcado.setDespachado(cantidadDespachoAcum);

            }
            actualizarMovimietoProducto();

            dataProvider = new ListDataProvider<>(listaDet);
            basicGrid.setDataProvider(dataProvider);
            editar = true;

            updateFooterMovimiento();

            dataProvider.refreshAll();

        } catch (Exception e) {
            System.out.println("Error buscando : " + e.getMessage());
        }

    }

    private void configurarGrid() {

        Binder<DetalleResumenDespacho> binder = new Binder<>(DetalleResumenDespacho.class);
        Editor<DetalleResumenDespacho> editor = basicGrid.getEditor();
        editor.setBinder(binder);

        basicGrid.setHeight("600px");

        basicGrid.addColumn(DetalleResumenDespacho::getNombreProducto).setHeader("Producto").setFooter("Total :");
        basicGrid.addColumn(DetalleResumenDespacho::getDespachado).setHeader("Despacho").setKey("despacho");
        basicGrid.addColumn(DetalleResumenDespacho::getEnvasado).setHeader("Envasado").setKey("envasado");
        basicGrid.addColumn(DetalleResumenDespacho::getDespachoAcumulado).setHeader("Despacho Acumulado").setKey("despachoacumulado");

        TextField txtCantidad = new TextField();

        basicGrid.getColumnByKey("despacho").setEditorComponent(txtCantidad);

        footeDivEnvasado = new Div();
        footeDivDespachado = new Div();
        footeDivDespachoAcumulado = new Div();

        footeDivEnvasado.addClassName("custom-footer");
        footeDivDespachado.addClassName("custom-footer");
        footeDivDespachoAcumulado.addClassName("custom-footer");

        basicGrid.getColumnByKey("despacho").setFooter(footeDivEnvasado);
        basicGrid.getColumnByKey("envasado").setFooter(footeDivDespachado);
        basicGrid.getColumnByKey("despachoacumulado").setFooter(footeDivDespachoAcumulado);

        updateFooterMovimiento();

        Grid.Column<DetalleResumenDespacho> editColumn = basicGrid.addComponentColumn(det -> {

            Button editButton = new Button("Editar");

//            editButton.addClassName("sinprocesar");
            editButton.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }
                basicGrid.getEditor().editItem(det);

            });

            txtCantidad.focus();

            return editButton;
        }).setWidth("150px").setFlexGrow(1).setKey("editar");

        Button saveButtonActualizar = new Button("Guardar", e -> {

            try {

                if (txtCantidad.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtCantidad.focus();
                    return;
                }

//                String valorCantidad = txtCantidad.getValue().isEmpty() ? "0.0" : txtCantidad.getValue();
//                Integer cantidad = Integer.valueOf(valorCantidad);
//
//                editor.getItem().setDespachado(cantidad);
//                editor.getItem().setEnvasado(diferencia);
                updateFooterMovimiento();

                listaDet.forEach(det -> {

                    det.setResumenDespacho(reumemenDesp);

                });

                reumemenDesp.setDetalleResumenDespachoCollection(listaDet);

                this.resumenService.guardar(reumemenDesp);

                dataProvider.refreshAll();

                txtCantidad.clear();

            } catch (Exception ex) {
                ex.printStackTrace();

                Notification.show("Hubo un problema guardando los datos ", 3000, Notification.Position.MIDDLE);
                Logger.getLogger(ResumenDespachoView.class.getName()).log(Level.SEVERE, null, ex);
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

        Integer totalDespachado = 0, totalEnvasado = 0, despAcumulado = 0;

        if (!(listaDet == null)) {

            for (DetalleResumenDespacho det1 : listaDet) {

                totalDespachado += det1.getDespachado();
                totalEnvasado += det1.getEnvasado();
                despAcumulado += det1.getDespachoAcumulado();

            }

            footeDivEnvasado.setText(totalDespachado.toString());
            footeDivDespachado.setText(totalEnvasado.toString());
            footeDivDespachoAcumulado.setText(despAcumulado.toString());

        }

    }

    private void actualizarMovimietoProducto() {

        fi = ClaseUtil.asDate(datePicker.getValue());
        ff = fi;

        int cantidadDespacho = 0, cantidadDespachoAcum = 0, cantidadProducidaAcum = 0;

        for (DetalleResumenDespacho rs : listaDet) {

            if (rs.getProducto().getCodigo() == 4) {

                cantidadDespacho = this.despachoService.getDespacho("23", rs.getProducto().getCodigoSap(), ff).intValue();
                cantidadDespacho += this.despachoService.getDespachoPorTransferencia("23", rs.getProducto().getCodigoSap(), ff);
                cantidadDespachoAcum = cantidadDespacho;

                cantidadDespachoAcum += this.despachoService.getDespacho("13", rs.getProducto().getCodigoSap(), ff);
                cantidadDespachoAcum += this.despachoService.getDespachoPorTransferencia("13", rs.getProducto().getCodigoSap(), ff);

            } else {

                cantidadDespacho = this.despachoService.getDespacho("23", rs.getProducto().getCodigoSap(), ff).intValue();
                cantidadDespachoAcum = this.despachoService.getDespacho("13", rs.getProducto().getCodigoSap(), ff).intValue();
                cantidadDespachoAcum += this.despachoService.getDespacho("23", rs.getProducto().getCodigoSap(), ff);
            }

            cantidadProducidaAcum = this.operacionEmpacadoraService.getEmpacado(ff, rs.getProducto().getCodigo()).intValue();

            rs.setDespachado(cantidadDespachoAcum);
            rs.setEnvasado(cantidadProducidaAcum);

        }

    }
}
