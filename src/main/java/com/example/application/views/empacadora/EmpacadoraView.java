package com.example.application.views.empacadora;

import com.example.application.modelo.Almacen;
import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.DetalleOperacionEmpacadora;
import com.example.application.modelo.Equipo;
import com.example.application.modelo.OperacionEmpacadora;
import com.example.application.modelo.Producto;
import com.example.application.modelo.Turno;
import com.example.application.services.AlmacenService;
import com.example.application.services.DespachoService;
import com.example.application.services.OperacionEmpacadoraService;
import com.example.application.services.ProductoService;
import com.example.application.services.TurnoService;
import com.example.application.util.ClaseUtil;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.example.application.services.EquipoService;
import com.example.application.services.HanaService;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;

@PageTitle("Empacadora")
@Route("Empacadora")
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class EmpacadoraView extends Composite<VerticalLayout> {

    @Autowired
    TurnoService turnoService;
    @Autowired
    OperacionEmpacadoraService operacionEmpacadoraService;
    @Autowired
    ProductoService productoService;
    @Autowired
    EquipoService equipoService;
    @Autowired
    AlmacenService almacenService;
    @Autowired
    DespachoService despachoService;
    @Autowired
    HanaService hanaService;

    Grid<DetalleMovimientoProducto> movimientoProductoGrid;
    Grid<DetalleOperacionEmpacadora> basicGrid;
    ComboBox<Turno> comboBoxTurno;
    ComboBox<Equipo> comboBoxEmpacadora;
    ComboBox<Producto> comboBoxProducto;

    DatePicker datePicker = new DatePicker(LocalDate.now());

    List<DetalleOperacionEmpacadora> listaDet;
    List<DetalleMovimientoProducto> listaDetMovProdc;
    ListDataProvider<DetalleOperacionEmpacadora> dataProvider;
    ListDataProvider<DetalleMovimientoProducto> dataProviderMovProdc;
    Button editSecondRow;
    private Div footerDiv;
    private Div footePnhrDiv;
    private Div footerDivII;
    private Div footeDivIF;
    private Div footerDivDespacho;
    private Div footerDivProduc;
    private Div footerDivDespachoAcum;
    private Div footerDivProducAcum;

    OperacionEmpacadora op;
    Equipo paletizadora;
    Almacen almacen;
    Date ff, fi;
    Button btnBuscar;
    Double cantidadDespacho = 0.00, cantidadProducida = 0.00,
            cantidadDespachoAcum = 0.00,
            cantidadProducidaAcum = 0.00,
            inventarioFinal = 0.00;

    public EmpacadoraView(TurnoService turnoServiceArg, OperacionEmpacadoraService operacionEmpacadoraArg, EquipoService empacadoraServiceArg,
            ProductoService productoServiceArg, AlmacenService almacenServiceArg, DespachoService despachoServiceArg,
            HanaService hanaServiceArg
    ) {

        this.operacionEmpacadoraService = operacionEmpacadoraArg;
        this.equipoService = empacadoraServiceArg;
        this.productoService = productoServiceArg;
        this.turnoService = turnoServiceArg;
        this.almacenService = almacenServiceArg;
        this.despachoService = despachoServiceArg;
        this.hanaService = hanaServiceArg;

        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        comboBoxEmpacadora = new ComboBox();
        comboBoxProducto = new ComboBox();
        VerticalLayout layoutRow3 = new VerticalLayout();
        comboBoxTurno = new ComboBox();
        movimientoProductoGrid = new Grid<>(DetalleMovimientoProducto.class);
        datePicker.setLabel("Fecha");

        btnBuscar = new Button("Buscar",
                event -> {

                    if (comboBoxTurno.getValue() == null) {
                        Notification.show("Tiene que seleccionar un turno ", 3000, Position.MIDDLE);
                        return;
                    }
                    if (comboBoxEmpacadora.getValue() == null) {
                        Notification.show("Tiene que seleccionar una empacadora ", 3000, Position.MIDDLE);
                        return;
                    }

                    if (comboBoxProducto.getValue() == null) {
                        Notification.show("Tiene que seleccionar un producto ", 3000, Position.MIDDLE);
                        return;
                    }

                    buscar();

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
        layoutRow.setMargin(false);
        layoutRow2.setHeightFull();
        layoutRow.setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.addClassName(Padding.XSMALL);
        layoutRow2.setWidth("100%");
        layoutRow2.setHeight("70px");
        comboBoxEmpacadora.setLabel("Empacadora");
        comboBoxEmpacadora.setWidth("min-content");
        setComboBoxEmpacadora(comboBoxEmpacadora);
        comboBoxProducto.setLabel("Producto");
        comboBoxProducto.setWidth("min-content");
        comboBoxTurno.setLabel("Turno");
        comboBoxTurno.setWidth("min-content");
        setComboBoxProducto(comboBoxProducto);
        setComboBoxTurno(comboBoxTurno);

        layoutRow3.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow3);
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.getStyle().set("flex-grow", "1");
        layoutRow3.setHeight("min-content");
//        btnGuardar.setText("Guardar");
//        btnGuardar.setWidth("min-content");
//        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.setText("Buscar");
        btnBuscar.setWidth("min-content");
        basicGrid.setWidth("100%");
        basicGrid.addClassName("my-grid");
        basicGrid.getStyle().set("flex-grow", "0");

        movimientoProductoGrid.setWidth("100%");
//        movimientoProductoGrid.getStyle().set("flex-grow", "0");

        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.setAlignItems(FlexComponent.Alignment.BASELINE);
        layoutRow2.add(datePicker);
        layoutRow2.add(comboBoxTurno);
        layoutRow2.add(comboBoxEmpacadora);
        layoutRow2.add(comboBoxProducto);
        configurarGrid();
        configurarGridDeProdcto();
        layoutRow2.add(btnBuscar);
        layoutRow3.add(basicGrid);
        layoutRow3.add(movimientoProductoGrid);
        getContent().add(layoutRow3);
        this.hanaService = null;

    }

    private void guardar() {

        try {

        } catch (Exception e) {
            System.out.println("Error actualizando : " + e.getMessage());
        }

    }

    private void buscar() {

        try {

//            LocalDate localDate = LocalDate.of(
//                    datePicker.getValue().getYear(), datePicker.getValue().getMonthValue(),
//                    datePicker.getValue().getDayOfMonth()
//            );
//            loadHanaData("A05", "M00001");
//            Date fi = new Date();
//            Date ff = new Date();
            Date fecha;//= Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            fecha = ClaseUtil.asDate(datePicker.getValue());

            int turno = comboBoxTurno.getValue().getCodigo();

            op = this.operacionEmpacadoraService.getOperacionEmpacadora(turno, 1, fecha);

            int produ = comboBoxProducto.getValue().getCodigo();
            int empac = comboBoxEmpacadora.getValue().getCodigo();

            System.out.println("Op : " + op);

            if (op == null) {

                Notification.show("No hay un turno abierto en esta fecha !", 4000, Position.MIDDLE);

                return;
            }

            listaDet = this.operacionEmpacadoraService
                    .getDetalleOperacion(op, empac, produ);

            dataProvider = new ListDataProvider<>(listaDet);
            basicGrid.setDataProvider(dataProvider);
            basicGrid.getColumnByKey("editar");

            listaDetMovProdc = this.operacionEmpacadoraService
                    .getDetalleMovimientoProducto(op);

            dataProviderMovProdc = new ListDataProvider<>(listaDetMovProdc);
            movimientoProductoGrid.setDataProvider(dataProviderMovProdc);

            dataProvider.refreshAll();
            dataProviderMovProdc.refreshAll();

            actualizarMovimietoProducto();
            updateFooterDetalle();
            updateFooterMovimiento();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error buscando : " + e.getMessage());
        }

    }

//    record SampleItem(String value, String label, Boolean disabled) {
//
//    }
    private void setComboBoxProducto(ComboBox comboBox) {

        List<Producto> sampleItems = this.productoService.getLista();

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((Producto) item).getDescripcion());
    }

    private void setComboBoxTurno(ComboBox comboBox) {
        List<Turno> sampleItems = this.turnoService.getLista();

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((Turno) item).getDescripcion());
    }

    private void setComboBoxEmpacadora(ComboBox comboBox) {

        List<Equipo> sampleItems = this.equipoService.getLista(11);

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((Equipo) item).getDescripcion());
    }

    private void configurarGrid() {

        basicGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        Binder<DetalleOperacionEmpacadora> binder = new Binder<>(DetalleOperacionEmpacadora.class);
        Editor<DetalleOperacionEmpacadora> editor = basicGrid.getEditor();
        editor.setBinder(binder);
//        editor.setBuffered(true);

        basicGrid.setHeight("450px");

        basicGrid.addColumn(DetalleOperacionEmpacadora::getHora).setHeader("Horas")
                .setFooter("TOTAL : ")
                .setKey("hora");

        basicGrid.addColumn(DetalleOperacionEmpacadora::getMinutoTrabajado).setHeader("Tiempo(Hora)")
                .setFooter(createFooter(""))
                .setKey("tp");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getAcumulado).setHeader("PN/Acumulado")
                .setKey("acumulado");

        basicGrid.addColumn(DetalleOperacionEmpacadora::getEmpacadoPorHora).setHeader("PN/Horas")
                .setKey("pnh");

        basicGrid.addColumn(DetalleOperacionEmpacadora::getFundaRota).setHeader("Fundas Rotas")
                .setKey("fundarota");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getFundaFallida).setHeader("Fundas Fallidas")
                .setKey("fundafallida");

        basicGrid.addColumn(DetalleOperacionEmpacadora::getNombreProducto).setHeader("Producto");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getNombreEmpacadora).setHeader("Empacadora");

        footerDiv = new Div();
        footePnhrDiv = new Div();

        footerDiv.addClassName("custom-footer");
        footePnhrDiv.addClassName("custom-footer");
        basicGrid.getColumnByKey("tp").setFooter(footerDiv);
        basicGrid.getColumnByKey("pnh").setFooter(footePnhrDiv);
        updateFooterDetalle();

        basicGrid.addComponentColumn(det -> {

            ComboBox<Equipo> comboBox = new ComboBox<>();
            comboBox.setWidth("150px");

            comboBox.setItems(this.equipoService.getLista(14));
            comboBox.setItemLabelGenerator(item -> ((Equipo) item).getAbreviatura());

            if (!(det.getPaletizadora() == null)) {

                paletizadora = this.equipoService.getEquipo(det.getPaletizadora());
                comboBox.setValue(paletizadora); // Establecer valor inicial
            }
            // Escuchar cambios en el ComboBox
            comboBox.addValueChangeListener(event -> {

                paletizadora = event.getValue();
                det.setPaletizadora(event.getValue().getCodigo());
                det.setNombrePaletizadora(event.getValue().getAbreviatura());// Actualizar la paletizadora en el objeto

                editor.getItem().setPaletizadora(det.getPaletizadora());
                editor.getItem().setNombrePaletizadora(det.getNombrePaletizadora());
            });

            return comboBox;
        }).setHeader("Paletizadora");

        basicGrid.addComponentColumn(det -> {
            ComboBox<Almacen> comboBox = new ComboBox<>();
            comboBox.setWidth("150px");

            comboBox.setItems(this.almacenService.getLista(11));
            comboBox.setItemLabelGenerator(item -> ((Almacen) item).getAbreviatura());

            if (!(det.getSilo() == null)) {

                almacen = this.almacenService.getAlmacen(det.getSilo());
                comboBox.setValue(almacen); // Establecer valor inicial
            }
            // Escuchar cambios en el ComboBox
            comboBox.addValueChangeListener(event -> {

                almacen = event.getValue();
                det.setSilo(event.getValue().getCodigo());
                det.setNombreSilo(event.getValue().getAbreviatura());// Actualizar la paletizadora en el objeto

                editor.getItem().setSilo(det.getSilo());
                editor.getItem().setNombreSilo(det.getNombreSilo());
            });

            return comboBox;
        }).setHeader("Silos");

        TextField txtAcumulado = new TextField();

        TextField txtRotas = new TextField();
        TextField txtFallidas = new TextField();

        Grid.Column<DetalleOperacionEmpacadora> editColumn = basicGrid.addComponentColumn(det -> {

            Button editButton = new Button("Registrar");

//            
//            if (det.getEditable() == false) {
//
//                editButton.setEnabled(det.getEditable());
//                acumuladoTextField.setEnabled(det.getEditable());
//                System.out.println("Editable ");
            editButton.addClassName("sinprocesar");
//                editButton.setText("Editar");
//            } else {
//                
//                editButton.addClassName("procesada");
//                editButton.setText("Editada");
//            }

            editButton.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }
                basicGrid.getEditor().editItem(det);

            });

            System.out.println("det.getEditable()  : " + det.getEditable());

            txtAcumulado.setValue(det.getAcumulado().toString());
            txtAcumulado.focus();

            return editButton;
        }).setWidth("150px").setFlexGrow(1).setKey("editar");

        txtAcumulado.setWidthFull();

        basicGrid.getColumnByKey("acumulado").setEditorComponent(txtAcumulado);
        basicGrid.getColumnByKey("fundarota").setEditorComponent(txtRotas);
        basicGrid.getColumnByKey("fundafallida").setEditorComponent(txtFallidas);

        Button saveButton = new Button("Guardar", e -> {

            try {

                if (txtAcumulado.getValue().isEmpty()) {

                    Notification.show("La cantidad de funda acumulada esta vacia ", 3000, Notification.Position.MIDDLE);
                    return;
                }

                String valor = txtAcumulado.getValue().isEmpty() ? "0.0" : txtAcumulado.getValue();
                Double acumulado = Double.valueOf(valor);

                String valorRotas = txtRotas.getValue().isEmpty() ? "0" : txtRotas.getValue();
                Integer fundaRotas = Integer.valueOf(valorRotas);

                String valorFundaFalida = txtFallidas.getValue().isEmpty() ? "0" : txtFallidas.getValue();
                Integer fundaFalida = Integer.valueOf(valorFundaFalida);

                if (acumulado < 0) {

                    Notification.show("El acumulada no puede ser menor que cero ", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (almacen == null) {

                    Notification.show("Tiene que seleccionar un silo ", 3000, Notification.Position.MIDDLE);
                    return;
                }
                if (paletizadora == null) {

                    Notification.show("Tiene que seleccionar una paletizadora ", 3000, Notification.Position.MIDDLE);
                    return;
                }

                Optional<Integer> index = basicGrid.getListDataView().getItemIndex(editor.getItem());
                System.out.println("index " + index);

//                boolean confirmar = showConfirmDialog();
//                if (confirmar) {
                if (index.get() == 0) {

                    System.out.println("Primer registro ");
                    editor.getItem().setAcumulado(acumulado);
                    editor.getItem().setEmpacadoPorHora(acumulado);
                    editor.getItem().setMinutoTrabajado(ClaseUtil.formatoNumero(acumulado / 60));
                    editor.getItem().setEditable(true);
                    editor.getItem().setNombrePaletizadora(paletizadora.getAbreviatura());
                    editor.getItem().setPaletizadora(paletizadora.getCodigo());
                    editor.getItem().setNombreSilo(almacen.getAbreviatura());
                    editor.getItem().setSilo(almacen.getCodigo());
                    editor.getItem().setFundaRota(fundaRotas);
                    editor.getItem().setFundaFallida(fundaFalida);

                    op.setDetalleOperacionEmpacadoraCollection(listaDet);
                    op.setDetalleMovimientoProductoCollection(listaDetMovProdc);
                    this.operacionEmpacadoraService.guardar(op);

                    btnBuscar.click();
                    dataProvider.refreshAll();
                    dataProviderMovProdc.refreshAll();

                    txtAcumulado.clear();
                    updateFooterDetalle();
                    updateFooterMovimiento();
                    return;
                } else {
                    System.out.println("Mas registro ");
                }

                Optional<DetalleOperacionEmpacadora> detAnt = basicGrid.getListDataView().getPreviousItem(editor.getItem());
                System.out.println("detAnt : " + detAnt.get().getAcumulado());

                if (acumulado < detAnt.get().getAcumulado()) {

                    Notification.show("El acumulada actual no puede ser menor que el acumulado anterior ", 3000, Notification.Position.MIDDLE);
                    return;
                }

                System.out.println("acumulado actual : " + acumulado);
                editor.getItem().setAcumulado(acumulado);

                System.out.println(" detAnt.get().getAcumulado() anterior  : " + detAnt.get().getAcumulado());

                Double ph = acumulado - detAnt.get().getAcumulado();
                Double minutoTrabajado = ph / 60.00;
                System.out.println("Empacado por hora : " + ph);

                editor.getItem().setEmpacadoPorHora(ph);
                editor.getItem().setMinutoTrabajado(ClaseUtil.formatoNumero(minutoTrabajado));
                editor.getItem().setNombrePaletizadora(paletizadora.getAbreviatura());
                editor.getItem().setPaletizadora(paletizadora.getCodigo());
                editor.getItem().setNombreSilo(almacen.getAbreviatura());
                editor.getItem().setSilo(almacen.getCodigo());
                editor.getItem().setFundaRota(fundaRotas);
                editor.getItem().setFundaFallida(fundaFalida);
//                actualizarMovimietoProducto();

                listaDet.forEach(det -> {

                    det.setOperacionEmpacadora(op);

                });

                listaDetMovProdc
                        .forEach(det -> {

                            det.setOperacionEmpacadora(op);

                        });

                editor.getItem().setEditable(true);
                op.setDetalleOperacionEmpacadoraCollection(listaDet);
                op.setDetalleMovimientoProductoCollection(listaDetMovProdc);
                this.operacionEmpacadoraService.guardar(op);

                btnBuscar.click();
                dataProvider.refreshAll();

                txtAcumulado.clear();
                txtFallidas.clear();
                txtRotas.clear();
                updateFooterDetalle();
                updateFooterMovimiento();

            } catch (Exception ex) {
                ex.printStackTrace();

                Notification.show("Hubo un problema guardando los datos ", 3000, Notification.Position.MIDDLE);
                Logger.getLogger(EmpacadoraView.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        saveButton.addClickShortcut(Key.ENTER);

        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                e -> editor.cancel());

        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_WARNING);

        HorizontalLayout actions = new HorizontalLayout(saveButton,
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

    private void configurarGridDeProdcto() {

        movimientoProductoGrid.setHeight("220px");
        movimientoProductoGrid.setWidthFull();
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getNombreProducto).setHeader("Producto")
                .setFooter("TOTAL FUNDAS:");

        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getInventarioInicial)
                .setHeader("Inv.Inicial")
                .setKey("ii");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getInventarioFinalSap)
                .setHeader("Inv.Final")
                .setKey("inventarioFinal");

        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getDespacho)
                .setHeader("Despacho")
                .setKey("desp");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getPorduccionPorEmpacadora)
                .setHeader("Produccion")
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
        updateFooterMovimiento();

        footerDivDespacho.addClassName("custom-footer");
        footerDivProduc.addClassName("custom-footer");
        footerDivII.addClassName("custom-footer");
        footeDivIF.addClassName("custom-footer");
        footerDivDespachoAcum.addClassName("custom-footer");
        footerDivProducAcum.addClassName("custom-footer");

        movimientoProductoGrid.getColumnByKey("ii").setFooter(footerDivII);
        movimientoProductoGrid.getColumnByKey("inventarioFinal").setFooter(footeDivIF);
        movimientoProductoGrid.getColumnByKey("desp").setFooter(footerDivDespacho);
        movimientoProductoGrid.getColumnByKey("produc").setFooter(footerDivProduc);
        movimientoProductoGrid.getColumnByKey("despAcu").setFooter(footerDivDespachoAcum);
        movimientoProductoGrid.getColumnByKey("producAcu").setFooter(footerDivProducAcum);

        movimientoProductoGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    private void actualizarMovimietoProducto() {

        fi = ClaseUtil.asDate(datePicker.getValue());
        ff = fi;
        int turno = op.getTurno().getCodigo();

        listaDetMovProdc.forEach((DetalleMovimientoProducto rs) -> {

            cantidadDespacho = 0.00;
            cantidadDespachoAcum = 0.00;
            if (turno == 1) {

                if (rs.getProducto().getCodigo() == 4) {

                    cantidadDespacho = this.despachoService.getDespacho("13", rs.getProducto().getCodigoSap(), ff);
                    cantidadDespacho += this.despachoService.getDespachoPorTransferencia("13", rs.getProducto().getCodigoSap(), ff);

                } else {

                    cantidadDespacho = this.despachoService.getDespacho("13", rs.getProducto().getCodigoSap(), ff);
                }
//             

                cantidadDespachoAcum = cantidadDespacho;

                System.out.println("cantidadDespacho ti : " + cantidadDespacho);

            } else {

                if (rs.getProducto().getCodigo() == 4) {

                    cantidadDespacho = this.despachoService.getDespacho("23", rs.getProducto().getCodigoSap(), ff);
                    cantidadDespacho += this.despachoService.getDespachoPorTransferencia("23", rs.getProducto().getCodigoSap(), ff);
                    cantidadDespachoAcum = cantidadDespacho;

                    cantidadDespachoAcum += this.despachoService.getDespacho("13", rs.getProducto().getCodigoSap(), ff);
                    cantidadDespachoAcum += this.despachoService.getDespachoPorTransferencia("13", rs.getProducto().getCodigoSap(), ff);

                } else {

                    cantidadDespacho = this.despachoService.getDespacho("23", rs.getProducto().getCodigoSap(), ff);
                    cantidadDespachoAcum = this.despachoService.getDespacho("13", rs.getProducto().getCodigoSap(), ff);
                    cantidadDespachoAcum += this.despachoService.getDespacho("23", rs.getProducto().getCodigoSap(), ff);
                }

            }

            cantidadProducida = this.operacionEmpacadoraService.getEmpacado(turno, ff, rs.getProducto().getCodigo());
            cantidadProducidaAcum = this.operacionEmpacadoraService.getEmpacado(ff, rs.getProducto().getCodigo());

            inventarioFinal = rs.getInventarioInicial() + cantidadProducidaAcum - cantidadDespachoAcum;
            rs.setDespacho(cantidadDespacho.intValue());
            rs.setPorduccionPorEmpacadora(cantidadProducida.intValue());
            rs.setProduccionAcumulada(cantidadProducidaAcum.intValue());
            rs.setDspachoAcumulado(cantidadDespachoAcum.intValue());

            if (!(rs.getProducto().getCodigo() == 4)) {
                rs.setInventarioFinalSap(inventarioFinal.intValue());
            } else {
                rs.setInventarioFinalSap(0);
            }

        });

    }

    private Div createFooter(String text) {
        Div footer = new Div();
        footer.setText(text);
        footer.addClassName("custom-footer");
        return footer;
    }

    // Método para actualizar el total en el pie de página
    private void updateFooterDetalle() {

        Double totalHora = 0.00, totalPnh = 0.00;
        if (!(listaDet == null)) {

            for (DetalleOperacionEmpacadora det1 : listaDet) {
                if (!(det1.getMinutoTrabajado() == null)) {

                    totalHora += det1.getMinutoTrabajado();
                    totalPnh += det1.getEmpacadoPorHora();
                }
            }
            totalHora = ClaseUtil.formatoNumero(totalHora / 60);
//            Double total = listaDet.stream().mapToDouble(DetalleOperacionEmpacadora::getMinutoTrabajado).sum();
            System.out.println("total : " + totalHora);
            footerDiv.setText(totalHora.toString());
            footePnhrDiv.setText(totalPnh.toString());
        }

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
