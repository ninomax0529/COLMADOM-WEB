package com.example.application.views.turno;

import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.DetalleOperacionEmpacadora;
import com.example.application.modelo.Equipo;
import com.example.application.modelo.EstadoTurno;
import com.example.application.modelo.OperacionEmpacadora;
import com.example.application.modelo.Operador;
import com.example.application.modelo.Producto;
import com.example.application.modelo.Supervisor;
import com.example.application.modelo.Turno;
import com.example.application.modelo.Usuariop;
import com.example.application.services.OperacionEmpacadoraService;
import com.example.application.services.OperadorService;
import com.example.application.services.ProductoService;
import com.example.application.services.SupervisorService;
import com.example.application.services.TurnoService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.example.application.services.EquipoService;
import com.example.application.util.ClaseUtil;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import java.util.logging.Level;
import java.util.logging.Logger;

@PageTitle("Abrir Turno")
@Route("AbrirTurno")
@Menu(order = 0, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class AbrirTurnoView extends Composite<VerticalLayout> {

    @Autowired
    TurnoService turnoService;
    @Autowired
    SupervisorService supervisorService;
    @Autowired
    OperadorService operadorService;
    @Autowired
    ProductoService productoService;
    @Autowired
    EquipoService empacadoraService;
    @Autowired
    OperacionEmpacadoraService operacionEmpacadoraService;

    Grid<DetalleOperacionEmpacadora> basicGrid;
    ProgressBar progressBar = new ProgressBar();
    MultiSelectComboBox multiSelectComboBoxProducto;
    MultiSelectComboBox multiSelectComboBoxEmp;
    OperacionEmpacadora opem;
    boolean editar = false;

    ComboBox<Turno> comboBoxTurno;
    ComboBox<Operador> comboBoxControl;
    ComboBox<Supervisor> comboBoxSupervisor;
    ComboBox<Operador> comboBoxOperador;

    List<DetalleOperacionEmpacadora> listaData;
    List<DetalleOperacionEmpacadora> lista;
    ListDataProvider<DetalleOperacionEmpacadora> dataProvider;
    ListDataProvider<DetalleMovimientoProducto> dataProviderMovProdc;
    List<DetalleMovimientoProducto> listaDetMovProdc;
    Grid<DetalleMovimientoProducto> movimientoProductoGrid;

    DatePicker datePicker = new DatePicker();

    public AbrirTurnoView(TurnoService turnoServiceArg, SupervisorService supervisorServiceArg,
            OperadorService operadorServiceArg, ProductoService productoServiceArg,
            EquipoService empacadoraServiceArg, OperacionEmpacadoraService opracionEmpacadoraArg) {

        this.turnoService = turnoServiceArg;
        this.supervisorService = supervisorServiceArg;
        this.operadorService = operadorServiceArg;
        this.productoService = productoServiceArg;
        this.empacadoraService = empacadoraServiceArg;
        this.operacionEmpacadoraService = opracionEmpacadoraArg;

        basicGrid = new Grid<>(DetalleOperacionEmpacadora.class, false);
        movimientoProductoGrid = new Grid<>(DetalleMovimientoProducto.class, false);

        HorizontalLayout layoutRow = new HorizontalLayout();
        comboBoxTurno = new ComboBox();
        comboBoxControl = new ComboBox();
        comboBoxSupervisor = new ComboBox();
        comboBoxOperador = new ComboBox();

        datePicker = new DatePicker();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        multiSelectComboBoxProducto = new MultiSelectComboBox();
        multiSelectComboBoxEmp = new MultiSelectComboBox();
        HorizontalLayout layoutRow3 = new HorizontalLayout();
        getContent().setSpacing(false);

        Button btnGuardar = new Button("Guardar",
                event -> {

                    try {

                        if (datePicker.getValue() == null) {

                            Notification.show("Tiene que seleccionar una fecha ", 3000, Notification.Position.MIDDLE);
                            return;
                        }

                        if (comboBoxTurno.getValue() == null) {
                            Notification.show("Tiene que seleccionar un turno ", 3000, Notification.Position.MIDDLE);
                            return;
                        }

                        if (comboBoxControl.getValue() == null) {

                            Notification.show("Tiene que seleccionar un Control ", 3000, Notification.Position.MIDDLE);
                            return;
                        }

                        if (comboBoxSupervisor.getValue() == null) {

                            Notification.show("Tiene que seleccionar un Supervisor ", 3000, Notification.Position.MIDDLE);
                            return;
                        }

                        if (comboBoxOperador.getValue() == null) {

                            Notification.show("Tiene que seleccionar un Operador ", 3000, Notification.Position.MIDDLE);
                            return;
                        }

                        if (lista == null) {

                            Notification.show("No hay empacadora agregada para este turnbo ", 3000, Notification.Position.MIDDLE);
                            return;
                        }

                        Date fecha;//= Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                        fecha = ClaseUtil.asDate(datePicker.getValue());

                        int turno = comboBoxTurno.getValue().getCodigo();
                        System.out.println("turno : " + turno);
                        System.out.println("fecha : " + fecha);

                        opem = this.operacionEmpacadoraService.getOperacionEmpacadora(turno, fecha);

                        System.out.println("Op : " + opem);

                        if (!(opem == null)) {

                            Notification.show("Hay un turno en esta fecha !", 3000, Notification.Position.TOP_CENTER);

                            return;
                        }

//                        progressBar.setIndeterminate(true);
                        guardar();

//                        progressBar.setIndeterminate(false);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        progressBar.setIndeterminate(false);
                        System.out.println("Error ; " + e.getMessage());

                    }

                });

        Button btnNuevo = new Button("Nuevo",
                event -> {

                    if (comboBoxTurno.getValue() == null) {

                        Notification.show("Tiene que seleccionar un turno ", 2000, Notification.Position.MIDDLE);
                        return;
                    }

                    if (multiSelectComboBoxEmp.isEmpty()) {
                        Notification.show("No hay empacadora seleccionada ", 2000, Notification.Position.MIDDLE);
                        return;
                    }

                    if (multiSelectComboBoxProducto.isEmpty()) {
                        Notification.show("No hay productos seleccionado ", 2000, Notification.Position.MIDDLE);
                        return;
                    }

                    try {

                        abrirTurno();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

        Button btnBuscar = new Button("Buscar",
                event -> {

                    if (comboBoxTurno.getValue() == null) {
                        Notification.show("Tiene que seleccionar un turno ", 3000, Notification.Position.MIDDLE);
                        return;
                    }

                    buscar();

                });

        getContent().setWidthFull();
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("1300px");
        layoutRow.setHeight("min-content");
        comboBoxTurno.setLabel("Turno");
        comboBoxTurno.setWidth("min-content");
        comboBoxControl.setLabel("Contriol");
        comboBoxControl.setWidth("min-content");
        setComboBoxControl(comboBoxControl);
        setComboBoxTurno(comboBoxTurno);
        comboBoxSupervisor.setLabel("Supervisor");
        comboBoxSupervisor.setWidth("min-content");
        setComboBoxSupervisor(comboBoxSupervisor);
        comboBoxOperador.setLabel("Operador");
        comboBoxOperador.setWidth("min-content");
        setComboBoxOperdaor(comboBoxOperador);
        datePicker.setLabel("Fecha");
        datePicker.setWidth("min-content");
        layoutRow2.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.setHeight("min-content");
        multiSelectComboBoxProducto.setLabel("Producto");
        multiSelectComboBoxProducto.setWidth("min-content");
        setMultiSelectComboBoxProducto(multiSelectComboBoxProducto);
        multiSelectComboBoxEmp.setLabel("Empacadora");
        multiSelectComboBoxEmp.setWidth("min-content");
        setMultiSelectComboBoxEmpacadora(multiSelectComboBoxEmp);
        layoutRow3.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow3);
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.setWidth("100%");
        layoutRow3.setHeight("min-content");
        btnGuardar.setText("Guardar");
        btnGuardar.setWidth("min-content");
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_ICON);
        btnNuevo.setText("Nuevo");
        btnNuevo.setWidth("min-content");
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnBuscar.setText("Buscar");
        btnBuscar.setWidth("min-content");
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_WARNING);
        getContent().setPadding(true);
        getContent().setMargin(true);
        getContent().add(layoutRow);
        layoutRow.setAlignItems(FlexComponent.Alignment.BASELINE);
        layoutRow3.setAlignItems(FlexComponent.Alignment.BASELINE);

        layoutRow.add(datePicker);
        layoutRow.add(comboBoxTurno);
        layoutRow.add(comboBoxControl);
        layoutRow.add(comboBoxSupervisor);
        layoutRow.add(comboBoxOperador);
        layoutRow3.add(multiSelectComboBoxEmp);
        layoutRow3.add(multiSelectComboBoxProducto);

        getContent().add(layoutRow2);
        getContent().add(layoutRow3);
        layoutRow3.add(btnNuevo);
        layoutRow3.add(btnBuscar);
        layoutRow3.add(btnGuardar);
        getContent().add(basicGrid);
        getContent().add(movimientoProductoGrid);

        configurarGrid();
        configurarGridDeProdcto();

    }
//
//    record SampleItem(String value, String label, Boolean disabled) {
//
//    }

    private void setComboBoxTurno(ComboBox comboBox) {
        List<Turno> sampleItems = this.turnoService.getLista();

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((Turno) item).getDescripcion());
    }

    private void setComboBoxSupervisor(ComboBox comboBox) {

        List<Supervisor> sampleItems = supervisorService.getLista();

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((Supervisor) item).getNombre());
    }

    private void setComboBoxOperdaor(ComboBox comboBox) {

        List<Operador> sampleItems = operadorService.getLista();

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((Operador) item).getNombre());
    }

    private void setComboBoxControl(ComboBox comboBox) {

        List<Operador> sampleItems = operadorService.getLista();

        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((Operador) item).getNombre());
    }

    private void setMultiSelectComboBoxProducto(MultiSelectComboBox multiSelectComboBox) {
        List<Producto> sampleItems = this.productoService.getLista();

        multiSelectComboBox.setItems(sampleItems);
        multiSelectComboBox.setItemLabelGenerator(item -> ((Producto) item).getDescripcion());
    }

    private void setMultiSelectComboBoxEmpacadora(MultiSelectComboBox multiSelectComboBox) {
        List<Equipo> sampleItems = this.empacadoraService.getLista();

        multiSelectComboBox.setItems(sampleItems);
        multiSelectComboBox.setItemLabelGenerator(item -> ((Equipo) item).getDescripcion());
    }

    private List<DetalleMovimientoProducto> getListMovimientoProductp() {

        List<DetalleMovimientoProducto> listaMov = new ArrayList<>();

        DetalleMovimientoProducto detaMov;

        for (Producto p : this.productoService.getLista()) {

            detaMov = new DetalleMovimientoProducto();

            detaMov.setProducto(p);
            detaMov.setNombreProducto(p.getDescripcion());
            detaMov.setDespacho(0);
            detaMov.setInventarioFinal(0);
            detaMov.setInventarioFinalSap(0);
            detaMov.setInventarioInicial(0);
            detaMov.setInventarioInicialSap(0);
            detaMov.setProducto(p);
            listaMov.add(detaMov);

        }

        return listaMov;

    }

    private List<DetalleOperacionEmpacadora> abrirTurno() {

        editar = false;
        lista = new ArrayList<>();

        List<Producto> listaPro = new ArrayList<>();
        List<Equipo> listaEmp = new ArrayList<>();

        DetalleOperacionEmpacadora det;

        this.multiSelectComboBoxProducto.getSelectedItems().forEach(it -> {

            listaPro.add((Producto) it);
        });

        this.multiSelectComboBoxEmp.getSelectedItems().forEach(it -> {

            listaEmp.add((Equipo) it);
        });

        Turno turno = comboBoxTurno.getValue();
        int canHora = 0;
        if (turno.getCodigo() == 1) {
            canHora = 8;
        } else if (turno.getCodigo() == 2) {
            canHora = 16;
        }

        turno.getHoraInicio().getHours();

        try {

            for (Equipo e : listaEmp) {

                for (Producto p : listaPro) {

                    int hora = turno.getHoraInicio().getHours();
                    for (int tn = 1; tn <= canHora; tn++) {

                        det = new DetalleOperacionEmpacadora();
                        det.setProducto(p);
                        det.setEmpacadora(e);
                        det.setAcumulado(0.0);
                        det.setEmpacadoPorHora(0.0);
                        det.setHora(Integer.toString(tn));
                        det.setNombreEmpacadora(e.getAbreviatura());
                        det.setNombreProducto(p.getDescripcion());
                        det.setOperacionEmpacadora(opem);

                        if (hora == 23) {
                            hora = 0;
                        } else {
                            hora = hora + 1;
                        }

                        System.out.println("Hora : " + hora);
                        det.setHoraInicio(hora);
                        det.setHora(Integer.toString(hora) + ":" + "00");
                        det.setHoraFinal(turno.getHoraFinal().getHours());
                        lista.add(det);

                    }

                }

            }

            dataProvider = new ListDataProvider<>(lista);

            this.basicGrid.setItems(dataProvider);
            dataProvider.refreshAll();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error : " + e.getMessage());
        }

        System.out.println("Detalle : " + lista.size());

//        progressBar.setIndeterminate(false);
        return lista;
    }

    private void buscar() {

        try {

            Date fecha;//= Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            fecha = ClaseUtil.asDate(datePicker.getValue());

            int turno = comboBoxTurno.getValue().getCodigo();
            System.out.println("turno : " + turno);
            System.out.println("fecha : " + fecha);

            opem = this.operacionEmpacadoraService.getOperacionEmpacadora(turno, fecha);

            System.out.println("Op : " + opem);

            if (opem == null) {

                Notification.show("No hay un turno abierto en esta fecha !", 4000, Notification.Position.MIDDLE);

                return;
            }

            comboBoxControl.setValue(this.operadorService.getOperador(opem.getControl()));
            comboBoxOperador.setValue(this.operadorService.getOperador(opem.getOperador()));
            comboBoxSupervisor.setValue(this.supervisorService.getSupervisor(opem.getSupervisor()));

            lista = this.operacionEmpacadoraService
                    .getDetalleOperacion(opem);

            dataProvider = new ListDataProvider<>(lista);
            basicGrid.setDataProvider(dataProvider);
            basicGrid.getColumnByKey("editar");

            listaDetMovProdc = this.operacionEmpacadoraService
                    .getDetalleMovimientoProducto(opem);

            dataProviderMovProdc = new ListDataProvider<>(listaDetMovProdc);
            movimientoProductoGrid.setDataProvider(dataProviderMovProdc);

            dataProvider.refreshAll();
            dataProviderMovProdc.refreshAll();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error buscando : " + e.getMessage());
        }

    }

    private void guardar() {

        if (opem == null) {
            System.out.println("opem : " + opem);
            opem = new OperacionEmpacadora();
            editar = false;
        } else {
            editar = true;
        }

        try {

            basicGrid.getListDataView().getItems().forEach(detM -> {

                detM.setOperacionEmpacadora(opem);

            });

            movimientoProductoGrid.getListDataView().getItems().forEach(detM -> {

                System.out.println("DetMov : " + detM.getNombreProducto());

                detM.setOperacionEmpacadora(opem);

            });

            LocalDate localDate = LocalDate.of(
                    datePicker.getValue().getYear(), datePicker.getValue().getMonthValue(),
                    datePicker.getValue().getDayOfMonth()
            );

            // Convertir LocalDate a Date
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            opem.setAnulado(false);
            opem.setCerradoPor("amin");
            opem.setControl(comboBoxControl.getValue().getCodigo());
            opem.setOperador(comboBoxOperador.getValue().getCodigo());
            opem.setSupervisor(comboBoxSupervisor.getValue().getCodigo());
            opem.setTurno(comboBoxTurno.getValue());
            opem.setNombreControl(comboBoxControl.getValue().getNombre());
            opem.setNombreEstado("abierto");
            opem.setNombreOperador(comboBoxOperador.getValue().getNombre());
            opem.setNombreSupervisor(comboBoxSupervisor.getValue().getNombre());
            opem.setNombreTurno(comboBoxTurno.getValue().getDescripcion());
            opem.setNombreUsuario("admin");
            opem.setEstado(new EstadoTurno(1));
            opem.setFechaCreacion(new Date());
            opem.setFechaInicio(date);
            opem.setFechaFinal(date);
            opem.setDetalleOperacionEmpacadoraCollection(lista);

            if (editar == false) {

                System.out.println("Editando ...");
                listaDetMovProdc = crearMovimietoProducto(opem);
                dataProviderMovProdc = new ListDataProvider<>(listaDetMovProdc);
                movimientoProductoGrid.setDataProvider(dataProviderMovProdc);
                opem.setDetalleMovimientoProductoCollection(listaDetMovProdc);
            }

            opem.setHoraInicio(new Date());
            opem.setHoraFinal(new Date());
            opem.setUsuario(new Usuariop(1));

            opem = this.operacionEmpacadoraService.guardar(opem);

            listaDetMovProdc = this.operacionEmpacadoraService.getDetalleMovimientoProducto(opem);

            dataProviderMovProdc.refreshAll();
            dataProvider.refreshAll();

            Notification.show("Turno guardado exitosamenete ", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Hubo un problema guardando los datos ", 3000, Notification.Position.MIDDLE);
            System.out.println("Error guardando : " + e.getMessage());
        }
    }

    private void configurarGrid() {

        basicGrid.setHeight("400px");
        basicGrid.setWidthFull();
        basicGrid.addColumn(DetalleOperacionEmpacadora::getHora).setHeader("Horas");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getNombreEmpacadora).setHeader("Empacadora");
        basicGrid.addColumn(DetalleOperacionEmpacadora::getNombreProducto).setHeader("Producto");
        basicGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

//        basicGrid.getColumns().forEach(col -> {
//            col.setAutoWidth(true);
//            col.setSortable(true);
//        });
    }

    private void configurarGridDeProdcto() {

        Binder<DetalleMovimientoProducto> binder = new Binder<>(DetalleMovimientoProducto.class);
        Editor<DetalleMovimientoProducto> editor = movimientoProductoGrid.getEditor();
        editor.setBinder(binder);

        movimientoProductoGrid.setHeight("270");
        movimientoProductoGrid.setWidthFull();
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getNombreProducto).setHeader("Producto");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getInventarioInicial).setHeader("Inv.Inicial").setKey("inventarioInicial");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getInventarioFinal).setHeader("Inv.Final");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getDespacho).setHeader("Despacho");
        movimientoProductoGrid.addColumn(DetalleMovimientoProducto::getPorduccionPorInventario).setHeader("Produccion");

        TextField txtInventarioInicial = new TextField();

        txtInventarioInicial.setWidthFull();

        movimientoProductoGrid.getColumnByKey("inventarioInicial").setEditorComponent(txtInventarioInicial);

        Grid.Column<DetalleMovimientoProducto> editColumn = movimientoProductoGrid.addComponentColumn(det -> {

            Button editButton = new Button("Registrar");

            editButton.addClassName("sinprocesar");

            editButton.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }

                movimientoProductoGrid.getEditor().editItem(det);

            });

            System.out.println("det.getEditable()  : " + det.getInventarioInicial());

            txtInventarioInicial.setValue(Integer.toString(det.getInventarioInicial()));
            txtInventarioInicial.focus();

            return editButton;
        }).setWidth("150px").setFlexGrow(1).setKey("editar");

        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                e -> editor.cancel());

        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_WARNING);

        Button saveButton = new Button("Guradar", e -> {

            try {

                if (txtInventarioInicial.getValue().isEmpty()) {

                    Notification.show("La cantidad de funda  esta vacia ", 3000, Notification.Position.MIDDLE);
                    return;
                }

                String valor = txtInventarioInicial.getValue().isEmpty() ? "0.0" : txtInventarioInicial.getValue();
                Integer inventarioInicial = Integer.valueOf(valor);

                if (inventarioInicial < 0) {

                    Notification.show("El inventario no puede ser menor que cero ", 3000, Notification.Position.MIDDLE);
                    return;
                }

                System.out.println("inventarioInicial actual : " + inventarioInicial);
                editor.getItem().setInventarioInicial(inventarioInicial);

                movimientoProductoGrid.getListDataView().getItems()
                        .forEach(det -> {

                            det.setOperacionEmpacadora(opem);

                        });

                opem.setDetalleMovimientoProductoCollection(listaDetMovProdc);
                this.operacionEmpacadoraService.guardar(opem);

                dataProviderMovProdc.refreshAll();

                txtInventarioInicial.clear();

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

    private List<DetalleMovimientoProducto> crearMovimietoProducto(OperacionEmpacadora opem) {

        Date fecha;//= Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        fecha = ClaseUtil.asDate(datePicker.getValue());
        OperacionEmpacadora opTurno1;
        List<Producto> listaProducto = this.productoService.getLista();
        List<DetalleMovimientoProducto> listaMovi = new ArrayList<>();

        DetalleMovimientoProducto detMovProd;

        opTurno1 = this.operacionEmpacadoraService.getOperacionEmpacadora(1, fecha);
        System.out.println("opTurno1 : " + opTurno1);

        if (opem.getTurno().getCodigo() == 2 && opTurno1!=null) {

            System.out.println("Segundo turno");

            for (DetalleMovimientoProducto dm : this.operacionEmpacadoraService.getDetalleMovimientoProducto(opTurno1)) {

                detMovProd = new DetalleMovimientoProducto();

                detMovProd.setProducto(dm.getProducto());
                detMovProd.setInventarioFinal(dm.getInventarioFinal());
                detMovProd.setInventarioInicial(dm.getInventarioInicial());

                detMovProd.setDspachoAcumulado(dm.getDespacho());
                detMovProd.setProduccionAcumulada(dm.getPorduccionPorEmpacadora());

                detMovProd.setNombreProducto(dm.getProducto().getDescripcion());

                detMovProd.setOperacionEmpacadora(opem);
                listaMovi.add(detMovProd);

            }

        } else {

            System.out.println("Primer turno");

            for (Producto p : listaProducto) {

                detMovProd = new DetalleMovimientoProducto();

                detMovProd.setProducto(p);
                detMovProd.setDespacho(0);
                detMovProd.setInventarioFinal(0);
                detMovProd.setInventarioInicial(0);
                detMovProd.setDespacho(0);
                detMovProd.setNombreProducto(p.getDescripcion());
                detMovProd.setOperacionEmpacadora(opem);
                listaMovi.add(detMovProd);

            }
        }

        return listaMovi;

    }

}
