/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.dto.ArticuloRegistroDTO;
import com.maxsoft.application.modelo.*;
import com.maxsoft.application.servicio.ArticuloDaoService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.UnidadService;
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
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Registrar Artículos con Multi-Empaque y Multialmacén")
@Route(value = "inventario/registrarArticulov2")
public class RegistrarArticuloViewV2 extends VerticalLayout implements HasUrlParameter<String> {

    private final ArticuloService articuloService;
    private final UnidadDeVentaService unidaVentaService;
    private final AlmacenService almacenService;
    private final ArticuloAlmacenService articuloAlmacenService;
    private final ArticuloEmpaqueService articuloEmpaqueService;
    private final ArticuloDaoService articuloDaoService;
    private final UnidadService unidadService;

    private final Binder<Articulo> binder = new Binder<>(Articulo.class);

    // Campos generales del artículo
    private final TextField txtDescripcion = new TextField("Descripción");
    private final BigDecimalField txtPrecioCompra = new BigDecimalField("Precio Compra Base");
    private final BigDecimalField txtPrecioVenta = new BigDecimalField("Precio Venta Base");
    private final BigDecimalField txtExistencia = new BigDecimalField("Existencia General Base");
    private final IntegerField txtCodigo = new IntegerField("Código");
    private final RadioButtonGroup<UnidadDeVenta> rdbGrupo = new RadioButtonGroup<>();

    // TAB 2: Grid de Stock por Almacén (ArticuloAlmacen)
    private final Grid<ArticuloAlmacen> gridAlmacenes = new Grid<>(ArticuloAlmacen.class, false);
    private final List<ArticuloAlmacen> listaArticuloAlmacen = new ArrayList<>();
    private final Button btnAgregarAlmacen = new Button("Asignar Almacén", new Icon(VaadinIcon.PLUS));

    // TAB 3: Grid de Multi-Empaques (ArticuloEmpaque)
    private final Grid<ArticuloEmpaque> gridEmpaques = new Grid<>(ArticuloEmpaque.class, false);
    private final List<ArticuloEmpaque> listaEmpaques = new ArrayList<>();
    private final Button btnAgregarEmpaque = new Button("Agregar Presentación/Empaque", new Icon(VaadinIcon.PLUS));

    // Layout principal y navegación por pestañas
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private final Tabs tabs = new Tabs();
    private final VerticalLayout containerTabGeneral = new VerticalLayout();
    private final VerticalLayout containerTabAlmacenes = new VerticalLayout();
    private final VerticalLayout containerTabEmpaques = new VerticalLayout();

    private Articulo articuloActual;

    @Autowired
    public RegistrarArticuloViewV2(
            @Lazy ArticuloService articuloServiceArg,
            UnidadDeVentaService unidadDeVentaServiceArg,
            AlmacenService almacenServiceArg,
            ArticuloAlmacenService articuloAlmacenServiceArg,
            @Lazy ArticuloEmpaqueService articuloEmpaqueServiceArg,
            @Lazy ArticuloDaoService articuloDaoServiceArg,
            UnidadService unidadService
    ) {
        this.articuloService = articuloServiceArg;
        this.unidaVentaService = unidadDeVentaServiceArg;
        this.almacenService = almacenServiceArg;
        this.articuloAlmacenService = articuloAlmacenServiceArg;
        this.articuloEmpaqueService = articuloEmpaqueServiceArg;
        this.articuloDaoService = articuloDaoServiceArg;
        this.unidadService = unidadService;

        setSizeFull();
        setSpacing(false);

        configurarBotonera();
        configurarFormularioGeneral();
        configurarGridAlmacenes();
        configurarGridEmpaques();
        configurarTabs();

        add(botonera, tabs, containerTabGeneral, containerTabAlmacenes, containerTabEmpaques);
    }

    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> {

            try {
                guardarArticulo();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        botonera.getCancelar().addClickListener(e -> {

            UI.getCurrent().navigate(ArticuloView.class);

        });
    }

    private void configurarTabs() {
        Tab tabGeneral = new Tab("1. Datos Generales");
        Tab tabAlmacenes = new Tab("2. Stock por Almacén");
        Tab tabEmpaques = new Tab("3. Multi-Empaques / Presentaciones");

        tabs.add(tabGeneral, tabAlmacenes, tabEmpaques);
        tabs.setWidthFull();

        containerTabAlmacenes.setVisible(false);
        containerTabEmpaques.setVisible(false);

        tabs.addSelectedChangeListener(event -> {
            Tab sel = event.getSelectedTab();
            containerTabGeneral.setVisible(sel.equals(tabGeneral));
            containerTabAlmacenes.setVisible(sel.equals(tabAlmacenes));
            containerTabEmpaques.setVisible(sel.equals(tabEmpaques));
        });
    }

    private void configurarFormularioGeneral() {
        txtCodigo.setEnabled(false);
        txtCodigo.setWidth("100px");

        rdbGrupo.setLabel("Unidad Venta Base:");
        rdbGrupo.setItems(unidaVentaService.getLista());

        HorizontalLayout hbPrecio = new HorizontalLayout(txtPrecioCompra, txtPrecioVenta, txtExistencia);
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

    // ==========================================
    // TAB 2: CONFIGURACIÓN GRID STOCK POR ALMACÉN
    // ==========================================
    private void configurarGridAlmacenes() {
        gridAlmacenes.setWidthFull();
        gridAlmacenes.setHeight("300px");

        gridAlmacenes.addColumn(a -> a.getAlmacen() != null ? a.getAlmacen().getNombre() : "")
                .setHeader("Almacén").setAutoWidth(true);
        gridAlmacenes.addColumn(ArticuloAlmacen::getExistencia).setHeader("Existencia");
        gridAlmacenes.addColumn(ArticuloAlmacen::getNombreUnidad).setHeader("Unidad").setAutoWidth(true);
        gridAlmacenes.addColumn(ArticuloAlmacen::getMinimo).setHeader("Mínimo").setAutoWidth(true);
        gridAlmacenes.addColumn(ArticuloAlmacen::getMaximo).setHeader("Máximo").setAutoWidth(true);

        gridAlmacenes.addColumn(ArticuloAlmacen::getUbicacionPasillo).setHeader("Ubicación / Pasillo");

        gridAlmacenes.addColumn(new ComponentRenderer<>(item -> {
            Button btnEditar = new Button(new Icon(VaadinIcon.EDIT), e -> abrirModalAlmacen(item));
            btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button btnBorrar = new Button(new Icon(VaadinIcon.TRASH), e -> {
                listaArticuloAlmacen.remove(item);
                gridAlmacenes.setItems(listaArticuloAlmacen);
            });
            btnBorrar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            return new HorizontalLayout(btnEditar, btnBorrar);
        })).setHeader("Acciones");

        btnAgregarAlmacen.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarAlmacen.addClickListener(e -> abrirModalAlmacen(null));

        containerTabAlmacenes.add(btnAgregarAlmacen, gridAlmacenes);
    }

    private void abrirModalAlmacen(ArticuloAlmacen itemEditar) {
        Dialog dialog = new Dialog();
        boolean esEdicion = itemEditar != null;
        dialog.setHeaderTitle(esEdicion ? "Editar Stock en Almacén" : "Asignar Almacén");
        dialog.setWidth("500px");

        ComboBox<Almacen> cbAlmacenModal = new ComboBox<>("Almacén");
        cbAlmacenModal.setItems(almacenService.getLista());
        cbAlmacenModal.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenModal.setRequired(true);

        BigDecimalField txtExistenciaModal = new BigDecimalField("Existencia Inicial");

        // 🔥 CONTROL DE EXISTENCIA INICIAL:
        if (esEdicion) {
            txtExistenciaModal.setValue(itemEditar.getExistencia());
        } else {
            // Si es un almacén nuevo y es el primero en asignarse, toma la existencia del formulario general
            if (listaArticuloAlmacen.isEmpty() && txtExistencia.getValue() != null) {
                txtExistenciaModal.setValue(txtExistencia.getValue());
            } else {
                txtExistenciaModal.setValue(BigDecimal.ZERO);
            }
        }

        BigDecimalField txtMinimoModal = new BigDecimalField("Stock Mínimo");
        txtMinimoModal.setValue(esEdicion && itemEditar.getMinimo() != null ? itemEditar.getMinimo() : BigDecimal.ZERO);

        BigDecimalField txtMaximoModal = new BigDecimalField("Stock Máximo");
        txtMaximoModal.setValue(esEdicion && itemEditar.getMaximo() != null ? itemEditar.getMaximo() : new BigDecimal("999999"));

        TextField txtUbicacionModal = new TextField("Ubicación / Pasillo");

        if (esEdicion) {
            cbAlmacenModal.setValue(itemEditar.getAlmacen());
            cbAlmacenModal.setEnabled(false); // No cambiar el almacén en edición
            txtUbicacionModal.setValue(itemEditar.getUbicacionPasillo());
        }

        FormLayout form = new FormLayout(cbAlmacenModal, txtExistenciaModal, txtMinimoModal, txtMaximoModal, txtUbicacionModal);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("300px", 2));

        Button btnGuardarModal = new Button(esEdicion ? "Guardar" : "Agregar", e -> {
            Almacen almacenSel = cbAlmacenModal.getValue();
            if (almacenSel == null) {
                Notification.show("Seleccione un almacén válido", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Validar Duplicados de Almacén
            for (ArticuloAlmacen aa : listaArticuloAlmacen) {
                if (!esEdicion && aa.getAlmacen() != null && aa.getAlmacen().equals(almacenSel)) {
                    Notification.show("El almacén '" + almacenSel.getNombre() + "' ya fue asignado", 3000, Notification.Position.MIDDLE);
                    return;
                }
            }

            ArticuloAlmacen aa = esEdicion ? itemEditar : new ArticuloAlmacen();
            aa.setAlmacen(almacenSel);
            aa.setNombreAlmacen(almacenSel.getNombre());
            aa.setExistencia(txtExistenciaModal.getValue() != null ? txtExistenciaModal.getValue() : BigDecimal.ZERO);
            aa.setMinimo(txtMinimoModal.getValue() != null ? txtMinimoModal.getValue() : BigDecimal.ZERO);
            aa.setMaximo(txtMaximoModal.getValue() != null ? txtMaximoModal.getValue() : new BigDecimal("999999"));
            aa.setUbicacionPasillo(txtUbicacionModal.getValue());

            if (!esEdicion) {
                listaArticuloAlmacen.add(aa);
            }

            gridAlmacenes.setItems(listaArticuloAlmacen);
            dialog.close();
        });
        btnGuardarModal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelarModal = new Button("Cancelar", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(btnCancelarModal, btnGuardarModal);
        dialog.open();
    }

    // ==========================================
    // TAB 3: CONFIGURACIÓN GRID MULTI-EMPAQUES
    // ==========================================
    private void configurarGridEmpaques() {

        gridEmpaques.setWidthFull();
        gridEmpaques.setHeight("300px");

        gridEmpaques.addColumn(e -> e.getUnidadEmpaque() != null ? e.getUnidadEmpaque().getDescripcion() : "")
                .setHeader("Empaque / Presentación");

        gridEmpaques.addColumn(ArticuloEmpaque::getFactorConversion).setHeader("Factor Conv. (Unidades)").setAutoWidth(true);;
        gridEmpaques.addColumn(ArticuloEmpaque::getCodigoBarra).setHeader("Código de Barra");
        gridEmpaques.addColumn(ArticuloEmpaque::getPrecioCompra).setHeader("Precio Compra");
        gridEmpaques.addColumn(ArticuloEmpaque::getPrecioVenta).setHeader("Precio Venta");
        gridEmpaques.addColumn(e -> Boolean.TRUE.equals(e.getSeCompraEn()) ? "Sí" : "No").setHeader("Se Compra En");
        gridEmpaques.addColumn(e -> Boolean.TRUE.equals(e.getSeVendeEn()) ? "Sí" : "No").setHeader("Se Vende En");
        gridEmpaques.addColumn(e -> Boolean.TRUE.equals(e.getEsEmpaqueBase()) ? "Sí" : "No").setHeader("Es Base");

        gridEmpaques.addColumn(new ComponentRenderer<>(empaque -> {

            Button btnEditar = new Button(new Icon(VaadinIcon.EDIT), e -> {

                abrirModalEmpaque(empaque);
            });

            btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button btnBorrar = new Button(new Icon(VaadinIcon.TRASH), e -> {
                listaEmpaques.remove(empaque);

                gridEmpaques.setItems(listaEmpaques);
            });
            btnBorrar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            return new HorizontalLayout(btnEditar, btnBorrar);
        })).setHeader("Acciones");

        btnAgregarEmpaque.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarEmpaque.addClickListener(e -> abrirModalEmpaque(null));

        containerTabEmpaques.add(btnAgregarEmpaque, gridEmpaques);
    }

    private void abrirModalEmpaque(ArticuloEmpaque empaqueEditar) {
        Dialog dialog = new Dialog();
        boolean esEdicion = empaqueEditar != null;
        dialog.setHeaderTitle(esEdicion ? "Editar Empaque / Presentación" : "Agregar Nuevo Empaque / Presentación");
        dialog.setWidth("500px");

        ComboBox<Unidad> cbUnidad = new ComboBox<>("Unidad Empaque");
        cbUnidad.setItems(unidadService.getLista());
        cbUnidad.setItemLabelGenerator(Unidad::getDescripcion);
        cbUnidad.setRequired(true);

        BigDecimalField txtFactor = new BigDecimalField("Factor Conversión (Equivalencia en Uds)");
        txtFactor.setValue(BigDecimal.ONE);

        TextField txtBarcode = new TextField("Código de Barras");
        BigDecimalField txtPrecioCompraEmp = new BigDecimalField("Precio Compra Presentación");
        BigDecimalField txtPrecioVentaEmp = new BigDecimalField("Precio Venta Presentación");

        Checkbox chkBase = new Checkbox("Es Empaque Base");
        Checkbox chkCompra = new Checkbox("Empaque para Compras", true);
        Checkbox chkVenta = new Checkbox("Empaque para Ventas", true);

        if (esEdicion) {

            cbUnidad.setValue(empaqueEditar.getUnidadEmpaque());
            txtFactor.setValue(empaqueEditar.getFactorConversion());
            txtBarcode.setValue(empaqueEditar.getCodigoBarra() == null ? "1" : empaqueEditar.getCodigoBarra());
            txtPrecioCompraEmp.setValue(empaqueEditar.getPrecioCompra());
            txtPrecioVentaEmp.setValue(empaqueEditar.getPrecioVenta());
            chkBase.setValue(Boolean.TRUE.equals(empaqueEditar.getEsEmpaqueBase()));
            chkCompra.setValue(Boolean.TRUE.equals(empaqueEditar.getSeCompraEn()));
            chkVenta.setValue(Boolean.TRUE.equals(empaqueEditar.getSeVendeEn()));
        }

        // CONTROL EMPAQUE BASE: Si factor es 1, forzar Checkbox Base
        Runnable evaluarEmpaqueBase = () -> {
            BigDecimal factor = txtFactor.getValue();
            if (factor != null && factor.compareTo(BigDecimal.ONE) == 0) {
                chkBase.setValue(true);
                chkBase.setEnabled(false);
            } else {
                chkBase.setEnabled(true);
            }
        };

        evaluarEmpaqueBase.run();

        txtFactor.addValueChangeListener(e -> {
            evaluarEmpaqueBase.run();
            if (!esEdicion && e.getValue() != null) {
                if (txtPrecioVenta.getValue() != null) {
                    txtPrecioVentaEmp.setValue(txtPrecioVenta.getValue().multiply(e.getValue()));
                }
                if (txtPrecioCompra.getValue() != null) {
                    txtPrecioCompraEmp.setValue(txtPrecioCompra.getValue().multiply(e.getValue()));
                }
            }
        });

        FormLayout form = new FormLayout(cbUnidad, txtFactor, txtBarcode, txtPrecioCompraEmp, txtPrecioVentaEmp, chkBase, chkCompra, chkVenta);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("300px", 2));

        Button btnGuardarModal = new Button(esEdicion ? "Guardar" : "Agregar", e -> {
            Unidad unidadSeleccionada = cbUnidad.getValue();
            BigDecimal factor = txtFactor.getValue();
            String barcode = txtBarcode.getValue() != null ? txtBarcode.getValue().trim() : "";

            if (unidadSeleccionada == null || factor == null) {
                Notification.show("Ingrese los datos requeridos", 3000, Notification.Position.MIDDLE);
                return;
            }

            // CONTROL DUPLICADOS DE EMPAQUE
            for (ArticuloEmpaque empExistente : listaEmpaques) {
                if (esEdicion && empExistente.equals(empaqueEditar)) {
                    continue;
                }

                if (empExistente.getUnidadEmpaque() != null && empExistente.getUnidadEmpaque().equals(unidadSeleccionada)) {
                    Notification.show("Ya existe un empaque registrado para la unidad '" + unidadSeleccionada.getDescripcion() + "'", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (!barcode.isEmpty() && empExistente.getCodigoBarra() != null && empExistente.getCodigoBarra().equalsIgnoreCase(barcode)) {
                    Notification.show("El código de barras '" + barcode + "' ya está asignado a otro empaque", 3000, Notification.Position.MIDDLE);
                    return;
                }
            }

            boolean esBase = factor.compareTo(BigDecimal.ONE) == 0 || chkBase.getValue();

            // Desmarcar empaques base anteriores de la lista si este pasa a ser base
            if (esBase) {
                for (ArticuloEmpaque empExistente : listaEmpaques) {
                    if (!empExistente.equals(empaqueEditar)) {
                        empExistente.setEsEmpaqueBase(false);
                    }
                }
            }

            ArticuloEmpaque emp = esEdicion ? empaqueEditar : new ArticuloEmpaque();
            emp.setCodigo(unidadSeleccionada.getCodigo());
            emp.setArticulo(articuloActual);
            emp.setNombreEmpaque(unidadSeleccionada.getDescripcion());
            emp.setNombreArticulo(articuloActual.getDescripcion());
            emp.setUnidadEmpaque(unidadSeleccionada);
            emp.setFactorConversion(factor);
            emp.setCodigoBarra(barcode);
            emp.setPrecioCompra(txtPrecioCompraEmp.getValue());
            emp.setPrecioVenta(txtPrecioVentaEmp.getValue());
            emp.setEsEmpaqueBase(esBase);
            emp.setSeCompraEn(chkCompra.getValue());
            emp.setSeVendeEn(chkVenta.getValue());

            if (!esEdicion) {
                listaEmpaques.add(emp);
            }

            gridEmpaques.setItems(listaEmpaques);
            dialog.close();
        });
        btnGuardarModal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelarModal = new Button("Cancelar", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(btnCancelarModal, btnGuardarModal);
        dialog.open();
    }

    // ==========================================
    // LÓGICA DE EDICIÓN Y GUARDADO ATÓMICO
    // ==========================================
    private void editarArticulo(Articulo articulo) {

        this.articuloActual = articulo;
        this.articuloDaoService.setArticulo(articulo);
        binder.setBean(articulo);

        listaEmpaques.clear();
        listaArticuloAlmacen.clear();

        if (articulo.getCodigo() != null) {

            listaEmpaques.addAll(articuloEmpaqueService.getPorArticulo(articuloActual.getCodigo()));

            listaArticuloAlmacen.addAll(articuloAlmacenService.buscarPorArticulo(articuloActual.getCodigo()).get());
        }

        gridEmpaques.setItems(listaEmpaques);
        gridAlmacenes.setItems(listaArticuloAlmacen);
    }

    private void guardarArticulo() {

        if (articuloActual == null) {
            articuloActual = new Articulo();
        }

        if (binder.writeBeanIfValid(articuloActual)) {

            if (listaEmpaques.isEmpty()) {

                ArticuloEmpaque emp = new ArticuloEmpaque();

                Unidad uni = null;

                if (null != articuloActual.getUnidadDeVenta().getCodigo()) {

                    switch (articuloActual.getUnidadDeVenta().getCodigo()) {
                        case 1 -> {
                            uni = this.unidadService.getUnidad(1);

                        }
                        case 2 ->
                            uni = this.unidadService.getUnidad(4);
                        case 3 ->
                            uni = this.unidadService.getUnidad(2);
                        default -> {
                        }
                    }
                }

                emp.setArticulo(articuloActual);

                emp.setUnidadEmpaque(uni);
                emp.setNombreEmpaque(uni.getDescripcion());
                emp.setNombreArticulo(articuloActual.getDescripcion());

                emp.setFactorConversion(new BigDecimal("1.00"));
                emp.setCodigoBarra(articuloActual.getCodigoDeBarra());
                emp.setPrecioCompra(articuloActual.getPrecioCompra());
                emp.setPrecioVenta(articuloActual.getPrecioVenta());
                emp.setEsEmpaqueBase(true);
                emp.setSeCompraEn(true);
                emp.setSeVendeEn(true);

                listaEmpaques.add(emp);

//                Notification.show("Debe agregar al menos un empaque/presentación para el artículo", 3000, Notification.Position.MIDDLE);
//                return;
            }

            try {
                // Instanciar DTO con los 3 bloques de datos
                ArticuloRegistroDTO dto = new ArticuloRegistroDTO(
                        articuloActual,
                        listaArticuloAlmacen,
                        listaEmpaques
                );

                // Ejecución transaccional atómica desde la capa de servicios
                Articulo guardado = articuloService.guardarArticuloCompleto(dto);

                Notification.show("Artículo #" + guardado.getCodigo() + " guardado de forma atómica y sincronizado correctamente.", 3000, Notification.Position.TOP_CENTER);
                limpiarFormulario();

            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Error al procesar la transacción: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }

        } else {
            Notification.show("Complete los campos obligatorios del formulario", 2500, Notification.Position.MIDDLE);
        }
    }

    private void limpiarFormulario() {
        editarArticulo(new Articulo());
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        Articulo entidad = NavigationContext.retrieve(parameter, Articulo.class);
        if (entidad != null) {
            editarArticulo(entidad);
        } else {
            limpiarFormulario();
        }
    }
}
