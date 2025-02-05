package com.example.application.views.turno;

import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.DetalleOperacionEmpacadora;
import com.example.application.modelo.EstadoTurno;
import com.example.application.modelo.OperacionEmpacadora;
import com.example.application.modelo.Turno;
import com.example.application.services.DespachoService;
import com.example.application.services.EstdoTurnoService;
import com.example.application.services.OperacionEmpacadoraService;
import com.example.application.services.TurnoService;
import com.example.application.util.ClaseUtil;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
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
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Cerrar Turno")
@Route("CerrarTurno")
@Menu(order = 2, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class CerrarTurnosView extends Composite<VerticalLayout> {

    @Autowired
    TurnoService turnoService;
    @Autowired
    EstdoTurnoService estadoTurnoService;
    @Autowired
    OperacionEmpacadoraService operacionEmpacadoraService;
    @Autowired
    DespachoService despachoService;

    Grid<DetalleMovimientoProducto> movimientoProductoGrid;
    Grid<DetalleOperacionEmpacadora> basicGrid;
    ComboBox<Turno> comboBoxTurno;
    ComboBox<EstadoTurno> comboBoxEstadoTurno;

    DatePicker datePicker = new DatePicker(LocalDate.now());

    List<DetalleOperacionEmpacadora> listaDet;
    List<DetalleMovimientoProducto> listaDetMovProdc;
    ListDataProvider<DetalleOperacionEmpacadora> dataProvider;
    ListDataProvider<DetalleMovimientoProducto> dataProviderMovProdc;
    OperacionEmpacadora op;
    Double cantidadDespacho = 0.00, cantidadProducida = 0.00,
            cantidadDespachoAcum = 0.00, cantidadProducidaAcum = 0.00;
    Date ff, fi;
    public Button saveButton;

    private Div footerDiv;
    private Div footePnhrDiv;
    private Div footerDivII;
    private Div footeDivIF;
    private Div footerDivDespacho;
    private Div footerDivProduc;
    private Div footerDivDespachoAcum;
    private Div footerDivProducAcum;
    private Div footerDivTotal;

    public CerrarTurnosView(TurnoService turnoServiceArg, OperacionEmpacadoraService operacionEmpacadoraArg,
            EstdoTurnoService estadoTurnoServiceArg) {

        this.operacionEmpacadoraService = operacionEmpacadoraArg;
        this.turnoService = turnoServiceArg;
        this.estadoTurnoService = estadoTurnoServiceArg;

        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        VerticalLayout layoutRow3 = new VerticalLayout();
        comboBoxTurno = new ComboBox();
        comboBoxEstadoTurno = new ComboBox();
        movimientoProductoGrid = new Grid<>(DetalleMovimientoProducto.class);
        datePicker.setLabel("Fecha");

        Button btnBuscar = new Button("Buscar",
                event -> {

                    if (comboBoxTurno.getValue() == null) {
                        Notification.show("Tiene que seleccionar un turno ", 3000, Position.MIDDLE);
                        return;
                    }

                    buscar();

                });

        Button btnGuardar = new Button("Guardar",
                event -> {

                    guardar();

                });

        movimientoProductoGrid = new Grid();
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
        comboBoxTurno.setLabel("Turno");
        comboBoxTurno.setWidth("min-content");
        setComboBoxTurno(comboBoxTurno);
        setComboBoxEstadoTurno(comboBoxEstadoTurno);
        comboBoxEstadoTurno.setLabel("Estado");
        comboBoxEstadoTurno.setWidth("min-content");

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
        basicGrid.setWidth("100%");
        basicGrid.addClassName("my-grid");
        basicGrid.getStyle().set("flex-grow", "0");
        movimientoProductoGrid.setWidth("100%");
        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.setAlignItems(FlexComponent.Alignment.BASELINE);
        layoutRow2.add(datePicker);
        layoutRow2.add(comboBoxTurno);
        layoutRow2.add(comboBoxEstadoTurno);
        configurarGrid();
        configurarGridDeProdcto();
        layoutRow2.add(btnBuscar);
        layoutRow2.add(btnGuardar);
        layoutRow3.add(basicGrid);
        layoutRow3.add(movimientoProductoGrid);
        getContent().add(layoutRow3);

    }

    private void guardar() {

        try {

            if (op == null) {

                Notification.show("No hay u turno para cerrar ", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (comboBoxEstadoTurno.getValue() == null) {

                Notification.show("Tiene que seleccionar el estado ", 3000, Notification.Position.MIDDLE);
                return;
            }

            op.setEstado(comboBoxEstadoTurno.getValue());
            op.setFechaFinal(new Date());
            op.setDetalleMovimientoProductoCollection(listaDetMovProdc);

            this.operacionEmpacadoraService.guardar(op);

            Notification.show("Turno actualizado exitosamente ", 3000, Notification.Position.MIDDLE);

        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error guardando ", 3000, Notification.Position.MIDDLE);
            System.out.println("Error actualizando : " + e.getMessage());
        }

    }

    private void buscar() {

        try {

//            LocalDate localDate = LocalDate.of(
//                    datePicker.getValue().getYear(), datePicker.getValue().getMonthValue(),
//                    datePicker.getValue().getDayOfMonth()
//            );
            Date fecha;//= Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            fecha = ClaseUtil.asDate(datePicker.getValue());

            int turno = comboBoxTurno.getValue().getCodigo();

            op = this.operacionEmpacadoraService.getOperacionEmpacadora(turno, fecha);

            System.out.println("Op : " + op);

            if (op == null) {

                Notification.show("No hay un turno abierto en esta fecha !", 4000, Position.MIDDLE);

                return;
            }

            comboBoxEstadoTurno.setValue(op.getEstado());

            listaDet = this.operacionEmpacadoraService
                    .getDetalleOperacion(op);

            dataProvider = new ListDataProvider<>(listaDet);
            basicGrid.setDataProvider(dataProvider);
            basicGrid.getColumnByKey("editar");

            listaDetMovProdc = this.operacionEmpacadoraService
                    .getDetalleMovimientoProducto(op);

            dataProviderMovProdc = new ListDataProvider<>(listaDetMovProdc);
            movimientoProductoGrid.setDataProvider(dataProviderMovProdc);
            actualizarMovimietoProducto();
            updateFooterMovimiento();

            dataProvider.refreshAll();
            dataProviderMovProdc.refreshAll();

        } catch (Exception e) {
            System.out.println("Error buscando : " + e.getMessage());
        }

    }

    private void setComboBoxTurno(ComboBox comboBox) {
        List<Turno> sampleItems = this.turnoService.getLista();

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((Turno) item).getDescripcion());
    }

    private void setComboBoxEstadoTurno(ComboBox comboBox) {
        List<EstadoTurno> sampleItems = this.estadoTurnoService.getLista();

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((EstadoTurno) item).getNombre());
    }

    private void configurarGrid() {

        Binder<DetalleOperacionEmpacadora> binder = new Binder<>(DetalleOperacionEmpacadora.class);
        Editor<DetalleOperacionEmpacadora> editor = basicGrid.getEditor();
        editor.setBinder(binder);
//        editor.setBuffered(true);

        basicGrid.setHeight("450px");

        basicGrid.addColumn(DetalleOperacionEmpacadora::getHora).setHeader("Horas").setKey("hora");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getMinutoTrabajado).setHeader("Tiempo Trabajado");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getAcumulado).setHeader("PN/Acumulado").setKey("acumulado");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getEmpacadoPorHora).setHeader("PN/Horas");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getNombreProducto).setHeader("Producto");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getNombreEmpacadora).setHeader("Empacadora");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getNombrePaletizadora).setHeader("Paletizadora");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getNombreSilo).setHeader("Silo");
      
        TextField acumuladoTextField = new TextField();

        acumuladoTextField.setWidthFull();

        basicGrid.getColumnByKey("acumulado").setEditorComponent(acumuladoTextField);

        basicGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    private void configurarGridDeProdcto() {

        Binder<DetalleMovimientoProducto> binder = new Binder<>(DetalleMovimientoProducto.class);
        Editor<DetalleMovimientoProducto> editor = movimientoProductoGrid.getEditor();
        editor.setBinder(binder);

        movimientoProductoGrid.setHeight("220px");
        movimientoProductoGrid.setWidthFull();
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getNombreProducto).setHeader("Producto")
                .setFooter("TOTAL FUNDAS :")
                             .setKey("total");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getInventarioInicial).setHeader("Inv.Inicial")
                .setKey("ii");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getInventarioFinal).setHeader("Inv.Final")
                .setKey("inventarioFinal");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getDespacho).setHeader("Despacho")
                .setKey("desp");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getPorduccionPorEmpacadora).setHeader("Produccion")
                .setKey("produc");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getDspachoAcumulado)
                .setHeader("Despacho Acumulado")
                .setKey("despAcu");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getProduccionAcumulada)
                .setHeader("Produccion Acumulada")
                .setKey("producAcu");

        footerDivII = new Div();
        footeDivIF = new Div();
        footerDivDespacho = new Div();
        footerDivProduc = new Div();
        footerDivDespachoAcum = new Div();
        footerDivProducAcum = new Div();
        footerDivTotal = new Div();

        footerDivDespacho.addClassName("custom-footer");
        footerDivProduc.addClassName("custom-footer");
        footerDivII.addClassName("custom-footer");
        footeDivIF.addClassName("custom-footer");
        footerDivDespachoAcum.addClassName("custom-footer");
        footerDivProducAcum.addClassName("custom-footer");
        footerDivTotal.addClassName("procesada");

//         movimientoProductoGrid.getColumnByKey("total").setFooter(footerDivTotal);
        movimientoProductoGrid.getColumnByKey("ii").setFooter(footerDivII);
        movimientoProductoGrid.getColumnByKey("inventarioFinal").setFooter(footeDivIF);
        movimientoProductoGrid.getColumnByKey("desp").setFooter(footerDivDespacho);
        movimientoProductoGrid.getColumnByKey("produc").setFooter(footerDivProduc);
        movimientoProductoGrid.getColumnByKey("despAcu").setFooter(footerDivDespachoAcum);
        movimientoProductoGrid.getColumnByKey("producAcu").setFooter(footerDivProducAcum);
        movimientoProductoGrid.addClassName("my-custom-grid");

        updateFooterMovimiento();

        TextField txtInventarioFinal = new TextField();

        txtInventarioFinal.setWidthFull();

        movimientoProductoGrid.getColumnByKey("inventarioFinal").setEditorComponent(txtInventarioFinal);

        Grid.Column<DetalleMovimientoProducto> editColumn = movimientoProductoGrid.addComponentColumn(det -> {

            Button editButton = new Button("Registrar");

            editButton.addClassName("sinprocesar");

            editButton.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }

                movimientoProductoGrid.getEditor().editItem(det);

            });

            System.out.println("det.getEditable()  : " + det.getInventarioFinal());

            txtInventarioFinal.setValue(Integer.toString(det.getInventarioFinal()));
            txtInventarioFinal.focus();

            return editButton;
        }).setWidth("150px").setFlexGrow(1).setKey("editar");

        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                e -> editor.cancel());

        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_WARNING);

        saveButton = new Button("Guradar", e -> {

            try {

                if (txtInventarioFinal.getValue().isEmpty()) {

                    Notification.show("La cantidad de funda  esta vacia ", 3000, Notification.Position.MIDDLE);
                    return;
                }

                String valor = txtInventarioFinal.getValue().isEmpty() ? "0.0" : txtInventarioFinal.getValue();
                Integer inventarioFinal = Integer.valueOf(valor);

                if (inventarioFinal < 0) {

                    Notification.show("El inventario no puede ser menor que cero ", 3000, Notification.Position.MIDDLE);
                    return;
                }

                System.out.println("inventarioFinal actual : " + inventarioFinal);
                editor.getItem().setInventarioFinal(inventarioFinal);
                actualizarMovimietoProducto();
                updateFooterMovimiento();

                listaDetMovProdc
                        .forEach(det -> {

                            det.setOperacionEmpacadora(op);

                        });

                op.setDetalleMovimientoProductoCollection(listaDetMovProdc);

                this.operacionEmpacadoraService.guardar(op);

                dataProviderMovProdc.refreshAll();

                txtInventarioFinal.clear();

            } catch (Exception ex) {

                Notification.show("Hubo un problema guardando los datos ", 3000, Notification.Position.MIDDLE);
                Logger.getLogger(CerrarTurnosView.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        HorizontalLayout actions = new HorizontalLayout(saveButton,
                cancelButton);

        actions.setPadding(true);

        editColumn.setEditorComponent(actions);

        movimientoProductoGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    private void actualizarMovimietoProducto() {

        fi = ClaseUtil.asDate(datePicker.getValue());
        ff = fi;
        int turno = op.getTurno().getCodigo();

        listaDetMovProdc.forEach(rs -> {

            if (turno == 1) {

                cantidadDespacho = this.despachoService.getDespacho("13", rs.getProducto().getCodigoSap(), ff);
                cantidadDespachoAcum = cantidadDespacho;
            } else {

                cantidadDespacho = this.despachoService.getDespacho("13", rs.getProducto().getCodigoSap(), ff);
                cantidadDespachoAcum = this.despachoService.getDespacho("23", rs.getProducto().getCodigoSap(), ff);
                cantidadDespacho = cantidadDespachoAcum - cantidadDespacho;
            }

            System.out.println("cantidadDespacho : " + cantidadDespacho);

//            cantidadDespachoAcum = this.despachoService.getDespacho(rs.getProducto().getCodigoSap(), fi, ff);
            cantidadProducida = this.operacionEmpacadoraService.getEmpacado(turno, ff, rs.getProducto().getCodigo());
            cantidadProducidaAcum = this.operacionEmpacadoraService.getEmpacado(ff, rs.getProducto().getCodigo());

            rs.setDespacho(cantidadDespacho.intValue());
            rs.setPorduccionPorEmpacadora(cantidadProducida.intValue());
            rs.setProduccionAcumulada(cantidadProducidaAcum.intValue());
            rs.setDspachoAcumulado(cantidadDespachoAcum.intValue());
            rs.setOperacionEmpacadora(op);

        });

    }

    // Método para actualizar el total en el pie de página
    private void updateFooterMovimiento() {

        Double totalII = 0.00,
                totalIf = 0.00,
                totalDespacho = 0.00,
                totalProduccion = 0.00,
                totalDespachoAcum = 0.00,
                totalProduccionAcum = 0.00;

        if (!(listaDetMovProdc == null)) {

            for (DetalleMovimientoProducto det1 : listaDetMovProdc) {

                totalII += det1.getInventarioInicial();
                totalIf += det1.getInventarioFinal();
                totalDespacho += det1.getDespacho();
                totalProduccion += det1.getPorduccionPorEmpacadora();
                totalDespachoAcum += det1.getDspachoAcumulado();
                totalProduccionAcum += det1.getProduccionAcumulada();

            }
//            totalII = ClaseUtil.formatoNumero(totalII / 60);
//            Double total = listaDet.stream().mapToDouble(DetalleOperacionEmpacadora::getMinutoTrabajado).sum();
            System.out.println("total : " + totalII);
            footeDivIF.setText(totalIf.toString());
            footerDivII.setText(totalII.toString());
            footerDivDespacho.setText(totalDespacho.toString());
            footerDivProduc.setText(totalProduccion.toString());
            footerDivDespachoAcum.setText(totalDespachoAcum.toString());
            footerDivProducAcum.setText(totalProduccionAcum.toString());
        }

    }

}
