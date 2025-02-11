package com.example.application.views.inventario;

import com.example.application.modelo.Almacen;
import com.example.application.modelo.CementoEmpacadoPorSilo;
import com.example.application.modelo.ControlDeFundaVacia;
import com.example.application.modelo.DetalleCementoEmpacadoPorSilo;
import com.example.application.modelo.DetalleControlDeFundaVacia;
import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.Producto;
import com.example.application.modelo.Supervisor;
import com.example.application.modelo.Usuariop;
import com.example.application.services.AlmacenService;
import com.example.application.services.CementoEmcadoPorSiloService;
import com.example.application.services.ControlFundaVaciaService;
import com.example.application.services.OperacionEmpacadoraService;
import com.example.application.services.ProductoService;
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

@PageTitle("Control Fundas Vacias")
@Route("ControlFundaVacia")
@Menu(order = 4, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class ControlFundaVaciaView extends Composite<VerticalLayout> {

    @Autowired
    ControlFundaVaciaService controlFundaService;
    @Autowired
    AlmacenService almacenService;
    @Autowired
    OperacionEmpacadoraService operacionEmpacadoraService;
    @Autowired
    ProductoService productoService;

    Grid<DetalleControlDeFundaVacia> basicGrid;

    DatePicker datePicker = new DatePicker(LocalDate.now());

    List<DetalleControlDeFundaVacia> listaDet;
    ListDataProvider<DetalleControlDeFundaVacia> dataProvider;

    ControlDeFundaVacia controlFundaVacia;
    Double cantidadDespacho = 0.00, cantidadProducida = 0.00,
            cantidadDespachoAcum = 0.00, cantidadProducidaAcum = 0.00,
            inventarioFinal = 0.00;
    Date fecha;
    public Button saveButton;

    private Div footerDivRecibida;
    private Div footeDivempacada;
    private Div footeDivInvFisico;
    private Div footeDivInvRotas;
    private Div footeDivInvInicial;
    private Div footeDivInvFinal;
    private Div footeDivDiferencia;

    boolean editar = false;

    public ControlFundaVaciaView(ControlFundaVaciaService controlFundaVaciaServiceArg,
            ProductoService productoServiceArg, OperacionEmpacadoraService opEmcadora) {

        this.controlFundaService = controlFundaVaciaServiceArg;
        this.productoService = productoServiceArg;
        this.operacionEmpacadoraService = opEmcadora;

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

            if (controlFundaVacia == null) {

                Notification.show("No hay regsitro para guardar ", 3000, Notification.Position.MIDDLE);
                return;
            }

            fecha = ClaseUtil.asDate(datePicker.getValue());
            controlFundaVacia.setFecha(fecha);
            controlFundaVacia.setDetalleControlDeFundaVaciaCollection(dataProvider.getItems());

            controlFundaVacia = this.controlFundaService.guardar(controlFundaVacia);

            listaDet.clear();

            listaDet.addAll(this.controlFundaService.getDetalle(controlFundaVacia.getCodigo()));

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

                controlFundaVacia = new ControlDeFundaVacia();

                controlFundaVacia.setFechaRegistro(new Date());
                controlFundaVacia.setUsuario(new Usuariop(1));
                controlFundaVacia.setEmpacada(0);
                controlFundaVacia.setInventarioFinal(0);
                controlFundaVacia.setInventarioInicial(0);
                controlFundaVacia.setFundaRota(0);
                controlFundaVacia.setInventarioFisico(0);
                controlFundaVacia.setNombreSupervisor("maximo");
                controlFundaVacia.setSupervisor(1);

                DetalleControlDeFundaVacia detCementoEmcado;

                List<Producto> listaProd = this.productoService.getLista();
                listaDet = new ArrayList<>();

                for (Producto p : listaProd) {

                    if (p.getCodigo() != 4) {

                        detCementoEmcado = new DetalleControlDeFundaVacia();
                        detCementoEmcado.setArticulo(p.getCodigo());
                        detCementoEmcado.setNombreArticulo(p.getDescripcion());
                        detCementoEmcado.setEmpacada(0);
                        detCementoEmcado.setInventarioFinal(0);
                        detCementoEmcado.setInventarioInicial(0);
                        detCementoEmcado.setFundaRota(0);
                        detCementoEmcado.setInventarioFisico(0);
                        detCementoEmcado.setPorcientoFundaRota(0);
                        detCementoEmcado.setDiferencia(0);
                        detCementoEmcado.setDiferenciaAcumulada(0);
                        detCementoEmcado.setControlFundaVacia(controlFundaVacia);

                        listaDet.add(detCementoEmcado);
                    }

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

            controlFundaVacia = this.controlFundaService.getControlDeFundaVacia(fecha);

            System.out.println("Op : " + controlFundaVacia);

            if (controlFundaVacia == null) {

                Notification.show("No hay regsitro en esta fecha !", 4000, Position.MIDDLE);

                return;
            }

            listaDet = new ArrayList<>();
            listaDet.addAll(this.controlFundaService.getDetalle(controlFundaVacia.getCodigo()));

            for (DetalleControlDeFundaVacia detaEmcado : listaDet) {

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

        Binder<DetalleControlDeFundaVacia> binder = new Binder<>(DetalleControlDeFundaVacia.class);
        Editor<DetalleControlDeFundaVacia> editor = basicGrid.getEditor();
        editor.setBinder(binder);

        basicGrid.setHeight("600px");

        basicGrid.addColumn(DetalleControlDeFundaVacia::getNombreArticulo).setHeader("Fundas").setFooter("Total :");
        basicGrid.addColumn(DetalleControlDeFundaVacia::getRecibida).setHeader("Recibida").setKey("recibida");
        basicGrid.addColumn(DetalleControlDeFundaVacia::getEmpacada).setHeader("Empacada").setKey("empacada");
        basicGrid.addColumn(DetalleControlDeFundaVacia::getInventarioFisico).setHeader("Inventario Fisico").setKey("invFisico");
        basicGrid.addColumn(DetalleControlDeFundaVacia::getFundaRota).setHeader("Fundaas Rotas").setKey("rotas");
        basicGrid.addColumn(DetalleControlDeFundaVacia::getPorcientoFundaRota).setHeader("% Fundaas Rotas").setKey("porcientoRotas");
        basicGrid.addColumn(DetalleControlDeFundaVacia::getInventarioInicial).setHeader("Inventario Inicial").setKey("invInicial");
        basicGrid.addColumn(DetalleControlDeFundaVacia::getInventarioFinal).setHeader("Inventario Final").setKey("invFinal");
        basicGrid.addColumn(DetalleControlDeFundaVacia::getDiferencia).setHeader("Diferencia").setKey("diferencia");

        TextField txtRecibida = new TextField();
        TextField txtEmpacada = new TextField();
        TextField txtInvFisico = new TextField();
        TextField txtFundaRotas = new TextField();
        TextField txtInvIncial = new TextField();

        basicGrid.getColumnByKey("recibida").setEditorComponent(txtRecibida);
        basicGrid.getColumnByKey("empacada").setEditorComponent(txtEmpacada);
        basicGrid.getColumnByKey("rotas").setEditorComponent(txtFundaRotas);
        basicGrid.getColumnByKey("invFisico").setEditorComponent(txtInvFisico);
        basicGrid.getColumnByKey("invInicial").setEditorComponent(txtInvIncial);

        footerDivRecibida = new Div();
        footeDivempacada = new Div();
        footeDivInvFisico = new Div();
        footeDivInvRotas = new Div();
        footeDivInvInicial = new Div();
        footeDivInvFinal = new Div();
        footeDivDiferencia = new Div();

        footerDivRecibida.addClassName("custom-footer");
        footeDivInvFisico.addClassName("custom-footer");
        footeDivInvRotas.addClassName("custom-footer");
        footeDivempacada.addClassName("custom-footer");
        footeDivDiferencia.addClassName("custom-footer");
        footeDivInvFinal.addClassName("custom-footer");
        footeDivInvInicial.addClassName("custom-footer");

        basicGrid.getColumnByKey("recibida").setFooter(footerDivRecibida);
        basicGrid.getColumnByKey("empacada").setFooter(footeDivempacada);
        basicGrid.getColumnByKey("invFisico").setFooter(footeDivInvFisico);
        basicGrid.getColumnByKey("rotas").setFooter(footeDivInvRotas);

        basicGrid.getColumnByKey("diferencia").setFooter(footeDivDiferencia);
        basicGrid.getColumnByKey("invFinal").setFooter(footeDivInvFinal);
        basicGrid.getColumnByKey("invInicial").setFooter(footeDivInvInicial);

        updateFooterMovimiento();

        Grid.Column<DetalleControlDeFundaVacia> editColumn = basicGrid.addComponentColumn(det -> {

            Button editButton = new Button("Editar");

//            editButton.addClassName("sinprocesar");
            editButton.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }
                basicGrid.getEditor().editItem(det);

            });

            txtRecibida.setValue(Integer.toString(det.getRecibida()));
            txtEmpacada.setValue(Integer.toString(det.getEmpacada()));
            txtFundaRotas.setValue(Integer.toString(det.getFundaRota()));
            txtInvFisico.setValue(Integer.toString(det.getInventarioFisico()));
            txtInvIncial.setValue(Integer.toString(det.getInventarioInicial()));

            txtRecibida.focus();

            return editButton;
        }).setWidth("150px").setFlexGrow(1).setKey("editar");

        Button saveButtonActualizar = new Button("Guardar", e -> {

            try {

                if (txtRecibida.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtRecibida.focus();
                    return;
                }
                if (txtEmpacada.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtEmpacada.focus();
                    return;
                }
                if (txtFundaRotas.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtFundaRotas.focus();
                    return;
                }

                if (txtInvIncial.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtInvIncial.focus();
                    return;
                }

                if (txtInvFisico.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtInvFisico.focus();
                    return;
                }

                Integer invFinal = 0, diferencia = 0, invInicial = 0;
                String valorRecibida = txtRecibida.getValue().isEmpty() ? "0.0" : txtRecibida.getValue();
                Integer recibida = Integer.valueOf(valorRecibida);

                String valorEmpacada = txtEmpacada.getValue().isEmpty() ? "0.0" : txtEmpacada.getValue();
                Integer empacada = Integer.valueOf(valorEmpacada);

                String valorInvFisico = txtInvFisico.getValue().isEmpty() ? "0.0" : txtInvFisico.getValue();
                Integer invFisico = Integer.valueOf(valorInvFisico);

                String valorfundaRota = txtFundaRotas.getValue().isEmpty() ? "0.0" : txtFundaRotas.getValue();
                Integer fundaRota = Integer.valueOf(valorfundaRota);

                String valorInvInicial = txtInvIncial.getValue().isEmpty() ? "0.0" : txtInvIncial.getValue();
                Integer inventarioInicial = Integer.valueOf(valorInvInicial);

                invFinal = invInicial + recibida - empacada - fundaRota;
                diferencia = invFisico - invFinal;

                editor.getItem().setRecibida(recibida);
                editor.getItem().setEmpacada(empacada);
                editor.getItem().setInventarioFisico(invFisico);
                editor.getItem().setFundaRota(fundaRota);
                editor.getItem().setInventarioFinal(invFinal);
                editor.getItem().setDiferencia(diferencia);
                editor.getItem().setInventarioInicial(inventarioInicial);

                updateFooterMovimiento();

                listaDet.forEach(det -> {

                    det.setControlFundaVacia(controlFundaVacia);

                });

                controlFundaVacia.setDetalleControlDeFundaVaciaCollection(listaDet);

                this.controlFundaService.guardar(controlFundaVacia);

                dataProvider.refreshAll();

                txtRecibida.clear();
                txtEmpacada.clear();
                txtInvIncial.clear();
                txtInvFisico.clear();

            } catch (Exception ex) {
                ex.printStackTrace();

                Notification.show("Hubo un problema guardando los datos ", 3000, Notification.Position.MIDDLE);
                Logger.getLogger(ControlFundaVaciaView.class.getName()).log(Level.SEVERE, null, ex);
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

        Integer totalEmpacada = 0,
                totalRecibida = 0,
                invFisico = 0,
                totalRota = 0,
                invFinal = 0,
                invInicial = 0,
                totalDiferencia = 0;

        if (!(listaDet == null)) {

            for (DetalleControlDeFundaVacia det1 : listaDet) {

                totalEmpacada += det1.getEmpacada();
                totalRecibida += det1.getRecibida();
                invFisico += det1.getInventarioFisico();
                totalRota += det1.getFundaRota();
                invFinal += det1.getInventarioFinal();
                invInicial += det1.getInventarioInicial();
                totalDiferencia += det1.getDiferencia();

            }

            footeDivempacada.setText(totalEmpacada.toString());
            footerDivRecibida.setText(totalRecibida.toString());
            footeDivInvFisico.setText(invFisico.toString());
            footeDivInvRotas.setText(totalRota.toString());
            footeDivInvFinal.setText(invFinal.toString());
            footeDivInvInicial.setText(invInicial.toString());
            footeDivDiferencia.setText(totalDiferencia.toString());

            controlFundaVacia.setInventarioFisico(invFisico);
            controlFundaVacia.setEmpacada(totalEmpacada);
            controlFundaVacia.setRecibida(totalRecibida);
            controlFundaVacia.setInventarioFinal(invFinal);
            controlFundaVacia.setInventarioInicial(invInicial);
          
            

        }

    }

}
