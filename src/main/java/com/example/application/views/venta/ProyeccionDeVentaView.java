package com.example.application.views.venta;

import com.example.application.modelo.ControlDeFundaVacia;
import com.example.application.modelo.DetalleControlDeFundaVacia;
import com.example.application.modelo.DetalleProyeccionDeVenta;
import com.example.application.modelo.Producto;
import com.example.application.modelo.ProyecconDeVenta;
import com.example.application.modelo.Usuariop;
import com.example.application.services.AlmacenService;
import com.example.application.services.ControlFundaVaciaService;
import com.example.application.services.OperacionEmpacadoraService;
import com.example.application.services.ProductoService;
import com.example.application.services.ProyeccionDeVentaService;
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

@PageTitle("Proyeccion de Ventas")
@Route("ProyeccionDeVenta")
@Menu(order = 5, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class ProyeccionDeVentaView extends Composite<VerticalLayout> {

    @Autowired
    ProyeccionDeVentaService proyeccionVentaService;
    @Autowired
    AlmacenService almacenService;   
    @Autowired
    ProductoService productoService;

    Grid<DetalleProyeccionDeVenta> basicGrid;

    DatePicker datePicker = new DatePicker(LocalDate.now());

    List<DetalleProyeccionDeVenta> listaDet;
    ListDataProvider<DetalleProyeccionDeVenta> dataProvider;

    ProyecconDeVenta proyeccionVemta;
  
    Date fecha;
    public Button saveButton;

    private Div footeDivCantidad;


    boolean editar = false;

    public ProyeccionDeVentaView(ProyeccionDeVentaService proyeccionVentaArg,
            ProductoService productoServiceArg) {

        this.proyeccionVentaService = proyeccionVentaArg;
        this.productoService = productoServiceArg;
      

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

            if (proyeccionVemta == null) {

                Notification.show("No hay regsitro para guardar ", 3000, Notification.Position.MIDDLE);
                return;
            }

            fecha = ClaseUtil.asDate(datePicker.getValue());
            proyeccionVemta.setFecha(fecha);
            proyeccionVemta.setDetalleProyeccionDeVentaCollection(dataProvider.getItems());

            proyeccionVemta = this.proyeccionVentaService.guardar(proyeccionVemta);

            listaDet.clear();

            listaDet.addAll(this.proyeccionVentaService.getDetalle(proyeccionVemta.getCodigo()));

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

                proyeccionVemta = new ProyecconDeVenta();

                proyeccionVemta.setFechaRegistro(new Date());
                proyeccionVemta.setUsaurio(new Usuariop(1));
                proyeccionVemta.setTotal(0);
                proyeccionVemta.setNombreUsuario("maximo");
             

                DetalleProyeccionDeVenta detalleProyeccion;

                List<Producto> listaProd = this.productoService.getLista();
                listaDet = new ArrayList<>();

                for (Producto p : listaProd) {

                        detalleProyeccion = new DetalleProyeccionDeVenta();
                        detalleProyeccion.setProducto(p.getCodigo());
                        detalleProyeccion.setNombreProducto(p.getDescripcion());
                        detalleProyeccion.setCantidad(0);
                        detalleProyeccion.setProyeccionVenta(proyeccionVemta);
                     
                        listaDet.add(detalleProyeccion);
                

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

            proyeccionVemta = this.proyeccionVentaService.getProyecconDeVenta(fecha);

            System.out.println("Op : " + proyeccionVemta);

            if (proyeccionVemta == null) {

                Notification.show("No hay regsitro en esta fecha !", 4000, Position.MIDDLE);

                return;
            }

            listaDet = new ArrayList<>();
            listaDet.addAll(this.proyeccionVentaService.getDetalle(proyeccionVemta.getCodigo()));

            for (DetalleProyeccionDeVenta detaEmcado : listaDet) {

//                detaEmcado.setFundaPaletizada(this.operacionEmpacadoraService.getEmpacadoPorSilo(fecha, detaEmcado.getAlmacenSilo().getCodigo()));
            }

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

        Binder<DetalleProyeccionDeVenta> binder = new Binder<>(DetalleProyeccionDeVenta.class);
        Editor<DetalleProyeccionDeVenta> editor = basicGrid.getEditor();
        editor.setBinder(binder);

        basicGrid.setHeight("600px");

        basicGrid.addColumn(DetalleProyeccionDeVenta::getNombreProducto).setHeader("Fundas").setFooter("Total :");
        basicGrid.addColumn(DetalleProyeccionDeVenta::getCantidad).setHeader("cantidad").setKey("cantidad");
    

        TextField txtCantidad = new TextField();       

        basicGrid.getColumnByKey("cantidad").setEditorComponent(txtCantidad);
      

        footeDivCantidad = new Div();
      

        footeDivCantidad.addClassName("custom-footer");
      

        basicGrid.getColumnByKey("cantidad").setFooter(footeDivCantidad);
      

        updateFooterMovimiento();

        Grid.Column<DetalleProyeccionDeVenta> editColumn = basicGrid.addComponentColumn(det -> {

            Button editButton = new Button("Editar");

//            editButton.addClassName("sinprocesar");
            editButton.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }
                basicGrid.getEditor().editItem(det);

            });

            txtCantidad.setValue(Integer.toString(det.getCantidad()));
          
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
                if (txtCantidad.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtCantidad.focus();
                    return;
                }
              

                String valorCantidad = txtCantidad.getValue().isEmpty() ? "0.0" : txtCantidad.getValue();
                Integer cantidad = Integer.valueOf(valorCantidad);


                editor.getItem().setCantidad(cantidad);
               
                updateFooterMovimiento();

                listaDet.forEach(det -> {

                    det.setProyeccionVenta(proyeccionVemta);

                });

                proyeccionVemta.setDetalleProyeccionDeVentaCollection(listaDet);

                this.proyeccionVentaService.guardar(proyeccionVemta);

                dataProvider.refreshAll();

                txtCantidad.clear();
           

            } catch (Exception ex) {
                ex.printStackTrace();

                Notification.show("Hubo un problema guardando los datos ", 3000, Notification.Position.MIDDLE);
                Logger.getLogger(ProyeccionDeVentaView.class.getName()).log(Level.SEVERE, null, ex);
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

        Integer total = 0;

        if (!(listaDet == null)) {

            for (DetalleProyeccionDeVenta det1 : listaDet) {

                total += det1.getCantidad();
                         
            }

            footeDivCantidad.setText(total.toString());
            
            proyeccionVemta.setTotal(total);
                   
            

        }

    }

}
