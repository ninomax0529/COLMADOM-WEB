package com.example.application.views.silo;

import com.example.application.modelo.Almacen;
import com.example.application.modelo.CementoEmpacadoPorSilo;
import com.example.application.modelo.DetalleCementoEmpacadoPorSilo;
import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.Usuariop;
import com.example.application.services.AlmacenService;
import com.example.application.services.CementoEmcadoPorSiloService;
import com.example.application.services.DespachoService;
import com.example.application.services.OperacionEmpacadoraService;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Movimiento Silos")
@Route("MovimientoSilos")
@Menu(order = 3, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class MovimientoSiloView extends Composite<VerticalLayout> {

    @Autowired
    CementoEmcadoPorSiloService cementoEmpacadoService;
    @Autowired
    AlmacenService almacenService;
    @Autowired
    OperacionEmpacadoraService operacionEmpacadoraService;

    Grid<DetalleCementoEmpacadoPorSilo> basicGrid;

    DatePicker datePicker = new DatePicker(LocalDate.now());

    List<DetalleCementoEmpacadoPorSilo> listaDet;
    List<DetalleMovimientoProducto> listaDetMovProdc;
    ListDataProvider<DetalleCementoEmpacadoPorSilo> dataProvider;
    ListDataProvider<DetalleMovimientoProducto> dataProviderMovProdc;
    CementoEmpacadoPorSilo cementoEmpacado;
    Double cantidadDespacho = 0.00, cantidadProducida = 0.00,
            cantidadDespachoAcum = 0.00, cantidadProducidaAcum = 0.00,
            inventarioFinal = 0.00;
    Date fecha;
    public Button saveButton;

    private Div footerDivNormales;
    private Div footeDivPaletizada;

    boolean editar = false;

    public MovimientoSiloView(CementoEmcadoPorSiloService cementoEmcadoPorSiloService,
            AlmacenService almacenServiceArg, OperacionEmpacadoraService opEmcadora) {

        this.cementoEmpacadoService = cementoEmcadoPorSiloService;
        this.almacenService = almacenServiceArg;
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

            if (cementoEmpacado == null) {

                Notification.show("No hay regsitro para guardar ", 3000, Notification.Position.MIDDLE);
                return;
            }

            Date fecha;//= Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            fecha = ClaseUtil.asDate(datePicker.getValue());
            cementoEmpacado.setFecha(fecha);
            cementoEmpacado.setDetalleCementoEmpacadoPorSiloCollection(dataProvider.getItems());

            cementoEmpacado = this.cementoEmpacadoService.guardar(cementoEmpacado);

            listaDet.clear();

            listaDet.addAll(this.cementoEmpacadoService.getDetalle(cementoEmpacado.getCodigo()));

            Notification.show("Silos actualizado exitosamente ", 3000, Notification.Position.MIDDLE);

        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error guardando ", 3000, Notification.Position.MIDDLE);
            System.out.println("Error actualizando : " + e.getMessage());
        }

    }

    private void nuevo() {

        try {

            if (editar == false) {

                cementoEmpacado = new CementoEmpacadoPorSilo();

                cementoEmpacado.setHoraMedicion(new Date());
                cementoEmpacado.setUsuario(new Usuariop(1));
                DetalleCementoEmpacadoPorSilo detCementoEmcado;

                List<Almacen> listaalm = this.almacenService.getLista(11);
                listaDet = new ArrayList<>();
                Double normal, paletizado, total;

                for (Almacen alm : listaalm) {

                    detCementoEmcado = new DetalleCementoEmpacadoPorSilo();
                    detCementoEmcado.setAlmacenSilo(alm);
                    detCementoEmcado.setCantidadFunda(0);
                    detCementoEmcado.setFundaEmpacada(0);

                    detCementoEmcado.setHoraPrimerTurno(0);
                    detCementoEmcado.setHoraSegundoTurno(0);
                    detCementoEmcado.setHoraTercerTurno(0);
                    detCementoEmcado.setInventarioFinal(0);
                    detCementoEmcado.setNombreSilo(alm.getAbreviatura());
                    detCementoEmcado.setEmpaqueSilo(cementoEmpacado);
                    paletizado = this.operacionEmpacadoraService.getEmpacadoPorSilo(fecha, alm.getCodigo());
                    detCementoEmcado.setFundaPaletizada(paletizado);

                    listaDet.add(detCementoEmcado);
                    listaDet.reversed();

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

            cementoEmpacado = this.cementoEmpacadoService.getCementoEmcadoPorSilo(fecha);

            System.out.println("Op : " + cementoEmpacado);

            if (cementoEmpacado == null) {

                Notification.show("No hay regsitro en esta fecha !", 4000, Position.MIDDLE);

                return;
            }

            listaDet = new ArrayList<>();
            listaDet.addAll(this.cementoEmpacadoService.getDetalle(cementoEmpacado.getCodigo()));
            
            for (DetalleCementoEmpacadoPorSilo detaEmcado : listaDet) {
                
                 detaEmcado.setFundaPaletizada( this.operacionEmpacadoraService.getEmpacadoPorSilo(fecha, detaEmcado.getAlmacenSilo().getCodigo()));
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

        Binder<DetalleCementoEmpacadoPorSilo> binder = new Binder<>(DetalleCementoEmpacadoPorSilo.class);
        Editor<DetalleCementoEmpacadoPorSilo> editor = basicGrid.getEditor();
        editor.setBinder(binder);

        basicGrid.setHeight("600px");

        basicGrid.addColumn(DetalleCementoEmpacadoPorSilo::getNombreSilo).setHeader("Silo").setFooter("Total :");
        basicGrid.addColumn(DetalleCementoEmpacadoPorSilo::getHoraPrimerTurno).setHeader("Inicio").setKey("inicio");
        basicGrid.addColumn(DetalleCementoEmpacadoPorSilo::getHoraSegundoTurno).setHeader("Segundo").setKey("segundo");
        basicGrid.addColumn(DetalleCementoEmpacadoPorSilo::getHoraTercerTurno).setHeader("Final").setKey("final");
        basicGrid.addColumn(DetalleCementoEmpacadoPorSilo::getCantidadFunda).setHeader("Total").setKey("total");
        basicGrid.addColumn(DetalleCementoEmpacadoPorSilo::getFundaEmpacada).setHeader("Nomal").setKey("normal");
        basicGrid.addColumn(DetalleCementoEmpacadoPorSilo::getFundaPaletizada).setHeader("Paletizado").setKey("paletizado");

        TextField txtPrimerTurno = new TextField();
        TextField txtSegundoTruno = new TextField();
        TextField txtTercerTurno = new TextField();
        TextField txtNormal = new TextField();
        TextField txtPaletizado = new TextField();

        basicGrid.getColumnByKey("inicio").setEditorComponent(txtPrimerTurno);
        basicGrid.getColumnByKey("segundo").setEditorComponent(txtSegundoTruno);
        basicGrid.getColumnByKey("final").setEditorComponent(txtTercerTurno);
        basicGrid.getColumnByKey("normal").setEditorComponent(txtNormal);
        basicGrid.getColumnByKey("paletizado").setEditorComponent(txtPaletizado);
        footerDivNormales = new Div();
        footeDivPaletizada = new Div();

        updateFooterMovimiento();

        footerDivNormales.addClassName("custom-footer");
        footeDivPaletizada.addClassName("custom-footer");

        basicGrid.getColumnByKey("normal").setFooter(footerDivNormales);
        basicGrid.getColumnByKey("paletizado").setFooter(footeDivPaletizada);

        Grid.Column<DetalleCementoEmpacadoPorSilo> editColumn = basicGrid.addComponentColumn(det -> {

            Button editButton = new Button("Editar");

            editButton.addClassName("sinprocesar");

            editButton.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }
                basicGrid.getEditor().editItem(det);

            });

            txtPrimerTurno.setValue(Double.toString(det.getHoraPrimerTurno()));
            txtSegundoTruno.setValue(Double.toString(det.getHoraPrimerTurno()));
            txtTercerTurno.setValue(Double.toString(det.getHoraPrimerTurno()));
            txtNormal.setValue(Double.toString(det.getFundaEmpacada()));
            txtPaletizado.setValue(Double.toString(det.getFundaPaletizada()));
            txtPrimerTurno.focus();

            return editButton;
        }).setWidth("150px").setFlexGrow(1).setKey("editar");

        Button saveButtonActualizar = new Button("Guardar", e -> {

            try {

                if (txtPrimerTurno.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtPrimerTurno.focus();
                    return;
                }
                if (txtSegundoTruno.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtSegundoTruno.focus();
                    return;
                }
                if (txtTercerTurno.getValue().isEmpty()) {

                    Notification.show("El campo esta vacio  ", 3000, Notification.Position.MIDDLE);
                    txtTercerTurno.focus();
                    return;
                }

                Double total = 0.00, paletizado = 0.00, normal = 0.00;

                String valorPrimerTurno = txtPrimerTurno.getValue().isEmpty() ? "0.0" : txtPrimerTurno.getValue();
                Double primerTurno = Double.valueOf(valorPrimerTurno);

                String valorSegundoTurno = txtSegundoTruno.getValue().isEmpty() ? "0.0" : txtSegundoTruno.getValue();
                Double segundoTurno = Double.valueOf(valorSegundoTurno);

                String valorTercerTurno = txtTercerTurno.getValue().isEmpty() ? "0.0" : txtTercerTurno.getValue();
                Double tercerTurno = Double.valueOf(valorTercerTurno);

                String normalStr = txtNormal.getValue().isEmpty() ? "0.0" : txtNormal.getValue();
                normal = Double.valueOf(normalStr);
                String paletizadoStr = txtPaletizado.getValue().isEmpty() ? "0.0" : txtPaletizado.getValue();
                paletizado = Double.valueOf(paletizadoStr);

                total = normal + paletizado;

                editor.getItem().setHoraPrimerTurno(primerTurno);
                editor.getItem().setHoraSegundoTurno(segundoTurno);
                editor.getItem().setHoraTercerTurno(tercerTurno);
                editor.getItem().setFundaEmpacada(normal);
                editor.getItem().setFundaPaletizada(paletizado);
                editor.getItem().setCantidadFunda(total);

                listaDet.forEach(det -> {

                    det.setEmpaqueSilo(cementoEmpacado);

                });

                cementoEmpacado.setDetalleCementoEmpacadoPorSiloCollection(listaDet);

                this.cementoEmpacadoService.guardar(cementoEmpacado);

                dataProvider.refreshAll();

                txtPrimerTurno.clear();
                txtSegundoTruno.clear();
                txtTercerTurno.clear();

            } catch (Exception ex) {
                ex.printStackTrace();

                Notification.show("Hubo un problema guardando los datos ", 3000, Notification.Position.MIDDLE);
                Logger.getLogger(MovimientoSiloView.class.getName()).log(Level.SEVERE, null, ex);
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

        basicGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        editor.addCancelListener(e -> {

        });

    }

    // Método para actualizar el total en el pie de página
    private void updateFooterMovimiento() {

        Double totalPaletizado = 0.00,
                totalNormal = 0.00;

        if (!(listaDet == null)) {

            for (DetalleCementoEmpacadoPorSilo det1 : listaDet) {

                totalNormal += det1.getFundaEmpacada();
                totalPaletizado += det1.getFundaPaletizada();

            }

            footeDivPaletizada.setText(totalPaletizado.toString());
            footerDivNormales.setText(totalNormal.toString());

        }

    }

}
