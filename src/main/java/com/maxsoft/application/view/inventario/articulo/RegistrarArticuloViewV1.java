/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.modelo.*;
import com.maxsoft.application.servicio.ArticuloDaoService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.venta.UnidadDeVentaService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Lazy;

@PageTitle("Registrar Artículos con Multi-Empaque")
@Route(value = "inventario/registrarArticulov1")
public class RegistrarArticuloViewV1 extends VerticalLayout implements HasUrlParameter<String> {

    private final ArticuloService articuloService;
    private final UnidadDeVentaService unidaService;
    private final AlmacenService almacenService;
    private final ArticuloAlmacenService articuloAlmacenService;
    private final ArticuloEmpaqueService articuloEmpaqueService;
    private final ArticuloDaoService articuloDaoService;

    private final Binder<Articulo> binder = new Binder<>(Articulo.class);

    // Campos generales del artículo
    private final TextField txtDescripcion = new TextField("Descripción");
    private final BigDecimalField txtPrecioCompra = new BigDecimalField("Precio Compra Base");
    private final BigDecimalField txtPrecioVenta = new BigDecimalField("Precio Venta Base");
    private final BigDecimalField txtExistencia = new BigDecimalField("Existencia Inicial (Unidades)");
    private final IntegerField txtCodigo = new IntegerField("Código");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén por Defecto");
    private final RadioButtonGroup<UnidadDeVenta> rdbGrupo = new RadioButtonGroup<>();

    // Multi-Empaques
    private final Grid<ArticuloEmpaque> gridEmpaques = new Grid<>(ArticuloEmpaque.class, false);
    private final List<ArticuloEmpaque> listaEmpaques = new ArrayList<>();
    private final Button btnAgregarEmpaque = new Button("Agregar Presentación/Empaque", new Icon(VaadinIcon.PLUS));

    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private final Tabs tabs = new Tabs();
    private final VerticalLayout containerTabGeneral = new VerticalLayout();
    private final VerticalLayout containerTabEmpaques = new VerticalLayout();

    private Articulo articuloActual;

//    @Autowired
//    public RegistrarArticuloViewV1(ArticuloService articuloServiceArg,
//            UnidadDeVentaService unidadDeVentaServiceArg,
//            AlmacenService almacenServiceArg,
//            ArticuloAlmacenService articuloAlmacenServiceArg,
//            ArticuloEmpaqueService articuloEmpaqueServiceArg,
//            ArticuloDaoService articuloDaoServiceArg) {
//
//        this.articuloService = articuloServiceArg;
//        this.unidaService = unidadDeVentaServiceArg;
//        this.almacenService = almacenServiceArg;
//        this.articuloAlmacenService = articuloAlmacenServiceArg;
//        this.articuloEmpaqueService = articuloEmpaqueServiceArg;
//        this.articuloDaoService = articuloDaoServiceArg;
//
//        setSizeFull();
//        setSpacing(false);
//
//        configurarBotonera();
//        configurarAlmacenes();
//        configurarFormularioGeneral();
//        configurarGridEmpaques();
//        configurarTabs();
//
//        add(botonera, tabs, containerTabGeneral, containerTabEmpaques);
//    }



// ... (resto del código)

    @Autowired
    public RegistrarArticuloViewV1(
            @Lazy ArticuloService articuloServiceArg,             // 🔥 Agregado @Lazy
            UnidadDeVentaService unidadDeVentaServiceArg,
            AlmacenService almacenServiceArg,
            ArticuloAlmacenService articuloAlmacenServiceArg,
            @Lazy ArticuloEmpaqueService articuloEmpaqueServiceArg, // 🔥 Agregado @Lazy
            @Lazy ArticuloDaoService articuloDaoServiceArg) {       // 🔥 Agregado @Lazy

        this.articuloService = articuloServiceArg;
        this.unidaService = unidadDeVentaServiceArg;
        this.almacenService = almacenServiceArg;
        this.articuloAlmacenService = articuloAlmacenServiceArg;
        this.articuloEmpaqueService = articuloEmpaqueServiceArg;
        this.articuloDaoService = articuloDaoServiceArg;

        setSizeFull();
        setSpacing(false);

        configurarBotonera();
        configurarAlmacenes();
        configurarFormularioGeneral();
        configurarGridEmpaques();
        configurarTabs();

        add(botonera, tabs, containerTabGeneral, containerTabEmpaques);
    }

    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> guardarArticulo());
        botonera.getCancelar().addClickListener(e -> UI.getCurrent().navigate(ArticuloView.class));
    }

    private void configurarAlmacenes() {
        List<Almacen> almacenes = almacenService.getLista();
        cbAlmacen.setItems(almacenes);
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        if (!almacenes.isEmpty()) {
            cbAlmacen.setValue(almacenes.get(0));
        }
    }

    private void configurarTabs() {
        Tab tabGeneral = new Tab("Datos Generales");
        Tab tabEmpaques = new Tab("Multi-Empaques / Presentaciones");

        tabs.add(tabGeneral, tabEmpaques);
        tabs.setWidthFull();

        containerTabEmpaques.setVisible(false);

        tabs.addSelectedChangeListener(event -> {
            boolean isEmpaques = event.getSelectedTab().equals(tabEmpaques);
            containerTabGeneral.setVisible(!isEmpaques);
            containerTabEmpaques.setVisible(isEmpaques);
        });
    }

    private void configurarFormularioGeneral() {
        txtCodigo.setEnabled(false);
        txtCodigo.setWidth("100px");

        rdbGrupo.setLabel("Unidad Venta Base:");
        rdbGrupo.setItems(unidaService.getLista());

        HorizontalLayout hbPrecio = new HorizontalLayout(txtPrecioCompra, txtPrecioVenta, txtExistencia, cbAlmacen);
        hbPrecio.setWidthFull();

        HorizontalLayout hlArt = new HorizontalLayout(txtCodigo, txtDescripcion);
        hlArt.addAndExpand(txtDescripcion);

        FormLayout formLayout = new FormLayout(hlArt, hbPrecio, rdbGrupo);

        binder.bind(txtCodigo, Articulo::getCodigo, Articulo::setCodigo);
        binder.bind(txtDescripcion, Articulo::getDescripcion, Articulo::setDescripcion);
        binder.bind(txtPrecioCompra, Articulo::getPrecioCompra, Articulo::setPrecioCompra);
        binder.bind(txtPrecioVenta, Articulo::getPrecioVenta, Articulo::setPrecioVenta);
        binder.bind(txtExistencia, Articulo::getExistencia, Articulo::setExistencia);
        binder.bind(rdbGrupo, Articulo::getUnidadDeVenta, Articulo::setUnidadDeVenta);

        containerTabGeneral.add(formLayout);
    }

    private void configurarGridEmpaques() {
        gridEmpaques.setWidthFull();
        gridEmpaques.setHeight("300px");

        gridEmpaques.addColumn(e -> e.getUnidadEmpaque() != null ? e.getUnidadEmpaque().getNombre() : "")
                .setHeader("Empaque / Presentación");
        gridEmpaques.addColumn(ArticuloEmpaque::getFactorConversion).setHeader("Factor Conv. (Unidades)");
        gridEmpaques.addColumn(ArticuloEmpaque::getCodigoBarra).setHeader("Código de Barra");
        gridEmpaques.addColumn(ArticuloEmpaque::getPrecioVenta).setHeader("Precio Venta");
        gridEmpaques.addColumn(e -> Boolean.TRUE.equals(e.getEsEmpaqueBase()) ? "Sí" : "No").setHeader("Es Base");

        gridEmpaques.addColumn(new ComponentRenderer<>(empaque -> {
            Button btnBorrar = new Button(new Icon(VaadinIcon.TRASH), e -> {
                listaEmpaques.remove(empaque);
                gridEmpaques.setItems(listaEmpaques);
            });
            btnBorrar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return btnBorrar;
        })).setHeader("Acciones");

        btnAgregarEmpaque.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarEmpaque.addClickListener(e -> abrirModalEmpaque());

        containerTabEmpaques.add(btnAgregarEmpaque, gridEmpaques);
    }

    private void abrirModalEmpaque() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Agregar Nuevo Empaque / Presentación");
        dialog.setWidth("500px");

        ComboBox<UnidadDeVenta> cbUnidad = new ComboBox<>("Unidad Empaque");
        cbUnidad.setItems(unidaService.getLista());
        cbUnidad.setItemLabelGenerator(UnidadDeVenta::getNombre);
        cbUnidad.setRequired(true);

        BigDecimalField txtFactor = new BigDecimalField("Factor Conversión (Equivalencia en Uds)");
        txtFactor.setValue(BigDecimal.ONE);

        TextField txtBarcode = new TextField("Código de Barras");
        BigDecimalField txtPrecioCompraEmp = new BigDecimalField("Precio Compra Presentación");
        BigDecimalField txtPrecioVentaEmp = new BigDecimalField("Precio Venta Presentación");

        Checkbox chkBase = new Checkbox("Es Empaque Base");
        Checkbox chkCompra = new Checkbox("Empaque para Compras", true);
        Checkbox chkVenta = new Checkbox("Empaque para Ventas", true);

        // Al cambiar factor, pre-calcular precios orientativos
        txtFactor.addValueChangeListener(e -> {
            if (e.getValue() != null && txtPrecioVenta.getValue() != null) {
                txtPrecioVentaEmp.setValue(txtPrecioVenta.getValue().multiply(e.getValue()));
            }
            if (e.getValue() != null && txtPrecioCompra.getValue() != null) {
                txtPrecioCompraEmp.setValue(txtPrecioCompra.getValue().multiply(e.getValue()));
            }
        });

        FormLayout form = new FormLayout(cbUnidad, txtFactor, txtBarcode, txtPrecioCompraEmp, txtPrecioVentaEmp, chkBase, chkCompra, chkVenta);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("300px", 2));

        Button btnGuardarModal = new Button("Agregar", e -> {
            if (cbUnidad.getValue() == null || txtFactor.getValue() == null) {
                Notification.show("Ingrese los datos requeridos", 3000, Notification.Position.MIDDLE);
                return;
            }

            ArticuloEmpaque emp = new ArticuloEmpaque();
            emp.setUnidadEmpaque(cbUnidad.getValue());
            emp.setFactorConversion(txtFactor.getValue());
            emp.setCodigoBarra(txtBarcode.getValue());
            emp.setPrecioCompra(txtPrecioCompraEmp.getValue());
            emp.setPrecioVenta(txtPrecioVentaEmp.getValue());
            emp.setEsEmpaqueBase(chkBase.getValue());
            emp.setEsEmpaqueCompra(chkCompra.getValue());
            emp.setEsEmpaqueVenta(chkVenta.getValue());

            listaEmpaques.add(emp);
            gridEmpaques.setItems(listaEmpaques);
            dialog.close();
        });
        btnGuardarModal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelarModal = new Button("Cancelar", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(btnCancelarModal, btnGuardarModal);
        dialog.open();
    }

    private void editarArticulo(Articulo articulo) {
        this.articuloActual = articulo;
        this.articuloDaoService.setArticulo(articulo);
        binder.setBean(articulo);

        listaEmpaques.clear();
        if (articulo.getCodigo() != null) {
            listaEmpaques.addAll(articuloEmpaqueService.getPorArticulo(articulo.getCodigo()));
        }
        gridEmpaques.setItems(listaEmpaques);
    }

    private void guardarArticulo() {
        if (articuloActual == null) {
            articuloActual = new Articulo();
        }

        if (binder.writeBeanIfValid(articuloActual)) {
            boolean esNuevo = (articuloActual.getCodigo() == null);

            // 1. Guardar Artículo
            Articulo articuloGuardado = articuloService.guardar(articuloActual);

            // 2. Guardar Asignación de Almacén si es nuevo
            if (esNuevo && cbAlmacen.getValue() != null) {
                ArticuloAlmacen articuloAlmacen = new ArticuloAlmacen();
                articuloAlmacen.setArticulo(articuloGuardado);
                articuloAlmacen.setAlmacen(cbAlmacen.getValue());

                BigDecimal existencia = articuloGuardado.getExistencia() != null ? articuloGuardado.getExistencia() : BigDecimal.ZERO;
                articuloAlmacen.setExistencia(existencia);
                articuloAlmacen.setMinimo(BigDecimal.ZERO);
                articuloAlmacen.setMaximo(new BigDecimal("999999"));

                articuloAlmacenService.guardar(articuloAlmacen);
            }

            // 3. Guardar Lista de Multi-Empaques
            for (ArticuloEmpaque emp : listaEmpaques) {
                emp.setArticulo(articuloGuardado);
            }
            articuloEmpaqueService.guardarLista(listaEmpaques, "ADMIN");

            Notification.show("Artículo, Almacén y Presentaciones guardados correctamente", 2500, Notification.Position.TOP_CENTER);
            limpiarFormulario();
        } else {
            Notification.show("Complete los campos obligatorios del formulario", 2500, Notification.Position.MIDDLE);
        }
    }

    private void limpiarFormulario() {
        editarArticulo(new Articulo());
        configurarAlmacenes();
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {

        Articulo entidad = NavigationContext.retrieve(parameter, Articulo.class);
        if (entidad != null) {
            editarArticulo(entidad);
        } else {
            limpiarFormulario();
        }
//        
//        Articulo entidad = NavigationContext.retrieve(parameter, Articulo.class);
//        if (entidad != null) {
//            editarArticulo(entidad);
//        } else {
//            limpiarFormulario();
//        }
    }
}
