/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.dto.ArticuloRegistroDTO;
import com.maxsoft.application.enums.MetodoCosteo;
import com.maxsoft.application.modelo.*;
import com.maxsoft.application.servicio.ArticuloDaoService;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.UnidadService;
import com.maxsoft.application.servicio.interfaces.venta.UnidadDeVentaService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.maxsoft.application.view.inventario.articulo.ArticuloView;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
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

@PageTitle("Registrar de Artículos")
@Route(value = "inventario/registrarArticulov4")
public class RegistrarArticuloViewV4 extends VerticalLayout implements HasUrlParameter<String> {

    private static final List<MetodoCosteo> METODOS_COSTEO = new ArrayList<>();

    private final ArticuloService articuloService;
    private final UnidadDeVentaService unidaVentaService;
    private final AlmacenService almacenService;
    private final ArticuloAlmacenService articuloAlmacenService;
    private final ArticuloEmpaqueService articuloEmpaqueService;
    private final ArticuloDaoService articuloDaoService;
    private final UnidadService unidadService;
    private final ProveedorService proveedorService;

    private final Binder<Articulo> binder = new Binder<>(Articulo.class);

    // Campos generales del artículo
    private final TextField txtDescripcion = new TextField("Descripción");
    private final BigDecimalField txtPrecioCompra = new BigDecimalField("Precio Compra Base");
    private final BigDecimalField txtPrecioVenta = new BigDecimalField("Precio Venta Base");
    private final BigDecimalField txtExistencia = new BigDecimalField("Existencia General Base");
    private final IntegerField txtCodigo = new IntegerField("Código");
    private final RadioButtonGroup<UnidadDeVenta> rbbGrupo = new RadioButtonGroup<>();

    // TAB 4: Datos exclusivos de inventario
    private final Checkbox chkInventariable = new Checkbox("Artículo inventariable (controla existencia)");
    private final Checkbox chkExistenciaNegativa = new Checkbox("Permitir vender sin existencia (existencia negativa)");
    private final Checkbox chkPerecedero = new Checkbox("Perecedero (maneja fecha de vencimiento)");
    private final IntegerField txtDiasVencimiento = new IntegerField("Días de vida útil");
    private final Checkbox chkControlaLote = new Checkbox("Controla lotes (existencia por lote y vencimiento; sale primero lo que vence primero)");
    private final BigDecimalField txtPuntoReorden = new BigDecimalField("Punto de reorden (unidad base)");
    private final ComboBox<MetodoCosteo> cbMetodoCosteo = new ComboBox<>("Método de costeo");
    private final TextField txtCodigoBarraPrincipal = new TextField("Código de barras principal");

    // TAB 5: Datos de compra
    private final Checkbox chkSeCompra = new Checkbox("Artículo de compra (se adquiere a suplidores)", true);
    private final ComboBox<Proveedor> cbProveedorPrincipal = new ComboBox<>("Suplidor principal");
    private final TextField txtCodigoProveedor = new TextField("Código del artículo en el suplidor");
    private final IntegerField txtDiasEntrega = new IntegerField("Tiempo de entrega (días)");
    private final BigDecimalField txtCantidadMinimaCompra = new BigDecimalField("Cantidad mínima de compra (unidad base)");
    private final BigDecimalField txtDescuentoCompra = new BigDecimalField("Descuento habitual del suplidor (%)");

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
    private Tab tabGeneral;
    private Tab tabAlmacenes;
    private Tab tabEmpaques;
    private Tab tabInventario;
    private Tab tabCompra;
    private Tab tabLotes;
    private final VerticalLayout containerTabGeneral = new VerticalLayout();
    private final VerticalLayout containerTabAlmacenes = new VerticalLayout();
    private final VerticalLayout containerTabEmpaques = new VerticalLayout();
    private final VerticalLayout containerTabInventario = new VerticalLayout();
    private final VerticalLayout containerTabCompra = new VerticalLayout();
    private final VerticalLayout containerTabLotes = new VerticalLayout();
    private final Grid<com.maxsoft.application.modelo.LoteAlmacen> gridLotes = new Grid<>(com.maxsoft.application.modelo.LoteAlmacen.class, false);
    private final com.maxsoft.application.servicio.interfaces.inventario.LoteService loteService;
    ListDataProvider<ArticuloEmpaque> dataProvider;
    ListDataProvider<ArticuloAlmacen> artalmDataProvider;

    private Articulo articuloActual;

    @Autowired
    public RegistrarArticuloViewV4(
            
            @Lazy ArticuloService articuloServiceArg,
            UnidadDeVentaService unidadDeVentaServiceArg,
            AlmacenService almacenServiceArg,
            ArticuloAlmacenService articuloAlmacenServiceArg,
            @Lazy ArticuloEmpaqueService articuloEmpaqueServiceArg,
            @Lazy ArticuloDaoService articuloDaoServiceArg,
            UnidadService unidadService,
            ProveedorService proveedorServiceArg,
            com.maxsoft.application.servicio.interfaces.inventario.LoteService loteServiceArg
    ) {
        this.loteService = loteServiceArg;
        this.articuloService = articuloServiceArg;
        this.unidaVentaService = unidadDeVentaServiceArg;
        this.almacenService = almacenServiceArg;
        this.articuloAlmacenService = articuloAlmacenServiceArg;
        this.articuloEmpaqueService = articuloEmpaqueServiceArg;
        this.articuloDaoService = articuloDaoServiceArg;
        this.unidadService = unidadService;
        this.proveedorService = proveedorServiceArg;

        setSizeFull();
        setSpacing(false);

        configurarBotonera();
        configurarFormularioGeneral();
        configurarFormularioInventario();
        configurarFormularioCompra();
        configurarGridAlmacenes();
        configurarGridEmpaques();
        configurarGridLotes();
        configurarTabs();

        add(botonera, tabs, containerTabGeneral, containerTabAlmacenes, containerTabEmpaques,
                containerTabInventario, containerTabCompra, containerTabLotes);
    }

    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> {

            try {

                if (txtDescripcion.getValue().isBlank()) {
                    ClaseUtil.mostrarNotificacion("La descripcion esta vacia", NotificationVariant.LUMO_PRIMARY);
                    txtDescripcion.focus();
                    return;
                }

                if (rbbGrupo.getValue() == null) {
                    ClaseUtil.mostrarNotificacion("Tiene que seleccionar una unidad de venta", NotificationVariant.LUMO_PRIMARY);
                    return;
                }

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
        tabGeneral = new Tab("1. Datos Generales");
        tabAlmacenes = new Tab("2. Existencia por Almacén");
        tabEmpaques = new Tab("3. Multi-Empaques / Presentaciones");
        tabInventario = new Tab("4. Datos de Inventario");
        tabCompra = new Tab("5. Datos de Compra");
        tabLotes = new Tab("6. Lotes");

        tabs.add(tabGeneral, tabAlmacenes, tabEmpaques, tabInventario, tabCompra, tabLotes);
        tabs.setWidthFull();

        containerTabAlmacenes.setVisible(false);
        containerTabEmpaques.setVisible(false);
        containerTabInventario.setVisible(false);
        containerTabCompra.setVisible(false);
        containerTabLotes.setVisible(false);

        tabs.addSelectedChangeListener(event -> {
            Tab sel = event.getSelectedTab();
            containerTabGeneral.setVisible(sel.equals(tabGeneral));
            containerTabAlmacenes.setVisible(sel.equals(tabAlmacenes));
            containerTabEmpaques.setVisible(sel.equals(tabEmpaques));
            containerTabInventario.setVisible(sel.equals(tabInventario));
            containerTabCompra.setVisible(sel.equals(tabCompra));
            containerTabLotes.setVisible(sel.equals(tabLotes));
        });
    }

    private void configurarFormularioGeneral() {
        txtCodigo.setEnabled(false);
        txtCodigo.setWidth("100px");

        rbbGrupo.setLabel("Unidad Venta Base:");
        rbbGrupo.setItems(unidaVentaService.getLista());

        HorizontalLayout hbPrecio = new HorizontalLayout(txtPrecioCompra, txtPrecioVenta, txtExistencia);
        hbPrecio.setWidthFull();

        HorizontalLayout hlArt = new HorizontalLayout(txtCodigo, txtDescripcion);
        hlArt.addAndExpand(txtDescripcion);

        FormLayout formLayout = new FormLayout(hlArt, hbPrecio, rbbGrupo);

        binder.bind(txtCodigo, Articulo::getCodigo, Articulo::setCodigo);
        binder.bind(txtDescripcion, Articulo::getDescripcion, Articulo::setDescripcion);
        binder.bind(txtPrecioCompra, Articulo::getPrecioCompra, Articulo::setPrecioCompra);
        binder.bind(txtPrecioVenta, Articulo::getPrecioVenta, Articulo::setPrecioVenta);
        binder.bind(txtExistencia, Articulo::getExistencia, Articulo::setExistencia);
        binder.bind(rbbGrupo, Articulo::getUnidadDeVenta, Articulo::setUnidadDeVenta);

        containerTabGeneral.add(formLayout);
    }

    // ==========================================
    // TAB 4: DATOS EXCLUSIVOS DE INVENTARIO
    // ==========================================
    /**
     * Propiedades de Articulo que usa esta pestaña: inventariable,
     * permitirVentaSinExistencia, perecedero, diasVencimiento, puntoReorden,
     * metodoCosteo y codigoDeBarra.
     */
    private void configurarFormularioInventario() {

        cbMetodoCosteo.setItems(MetodoCosteo.values());
        cbMetodoCosteo.setAllowCustomValue(false);
        cbMetodoCosteo.setRequiredIndicatorVisible(true);

        txtDiasVencimiento.setMin(1);
        txtDiasVencimiento.setStepButtonsVisible(true);
        txtDiasVencimiento.setHelperText("Días desde la entrada hasta el vencimiento");

        txtPuntoReorden.setHelperText("Cuando la existencia total baje de este valor conviene reponer");

        txtCodigoBarraPrincipal.setHelperText("Código del artículo; cada presentación tiene el suyo en la pestaña 3");

        chkPerecedero.addValueChangeListener(e -> {
            // Un perecedero se controla por lote (vencimiento) salvo que el usuario lo quite
            if (e.isFromClient() && Boolean.TRUE.equals(e.getValue())) {
                chkControlaLote.setValue(true);
            }
            actualizarEstadoCamposInventario();
        });
        chkInventariable.addValueChangeListener(e -> actualizarEstadoCamposInventario());
        chkControlaLote.setTooltipText("Al guardar, la existencia actual que no esté en ningún lote queda en el lote INICIAL.");

        FormLayout form = new FormLayout();
        form.add(chkInventariable, chkExistenciaNegativa, cbMetodoCosteo, txtPuntoReorden,
                chkPerecedero, txtDiasVencimiento, chkControlaLote, txtCodigoBarraPrincipal);

        form.setColspan(chkInventariable, 2);
        form.setColspan(chkExistenciaNegativa, 2);
        form.setColspan(chkPerecedero, 2);
        form.setColspan(chkControlaLote, 2);
        form.setColspan(txtCodigoBarraPrincipal, 2);

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        binder.bind(chkInventariable,
                a -> Boolean.TRUE.equals(a.getInventariable()), Articulo::setInventariable);

        // La propiedad real de Articulo es "permitirVentaSinExistencia"
        binder.bind(chkExistenciaNegativa,
                a -> Boolean.TRUE.equals(a.getPermitirVentaSinExistencia()), Articulo::setPermitirVentaSinExistencia);

        binder.bind(chkPerecedero,
                a -> Boolean.TRUE.equals(a.getPerecedero()), Articulo::setPerecedero);

        binder.bind(txtDiasVencimiento, Articulo::getDiasVencimiento, Articulo::setDiasVencimiento);
        binder.bind(chkControlaLote, Articulo::isControlaLote, Articulo::setControlaLote);
//        binder.bind(txtPuntoReorden, Articulo::getPuntoReorden, Articulo::setPuntoReorden);
//        binder.bind(cbMetodoCosteo, Articulo::getMetodoCosteo, Articulo::setMetodoCosteo);
        binder.bind(txtCodigoBarraPrincipal,
                a -> a.getCodigoDeBarra() == null ? "" : a.getCodigoDeBarra(), Articulo::setCodigoDeBarra);

        containerTabInventario.add(form);
    }

    /**
     * Un artículo que no controla inventario no necesita existencia negativa,
     * método de costeo, punto de reorden ni vencimiento; los días de vida útil
     * solo aplican a los perecederos.
     */
    private void actualizarEstadoCamposInventario() {

        boolean inventariable = chkInventariable.getValue();

        chkExistenciaNegativa.setEnabled(inventariable);
        cbMetodoCosteo.setEnabled(inventariable);
        txtPuntoReorden.setEnabled(inventariable);
        chkPerecedero.setEnabled(inventariable);
        txtDiasVencimiento.setEnabled(inventariable && chkPerecedero.getValue());
        chkControlaLote.setEnabled(inventariable);
    }

    // ==========================================
    // TAB 5: DATOS DE COMPRA
    // ==========================================
    /**
     * Propiedades de Articulo que usa esta pestaña: seCompra,
     * proveedorPrincipal, codigoProveedor, diasEntrega, cantidadMinimaCompra y
     * descuentoCompra.
     */
    private void configurarFormularioCompra() {

        cbProveedorPrincipal.setItems(proveedorService.getLista());
        cbProveedorPrincipal.setItemLabelGenerator(p -> p.getRnc() != null
                ? p.getNombre() + " (" + p.getRnc() + ")" : p.getNombre());
        cbProveedorPrincipal.setClearButtonVisible(true);
        cbProveedorPrincipal.setAllowCustomValue(false);
        cbProveedorPrincipal.setPlaceholder("Sin suplidor principal");
        cbProveedorPrincipal.setHelperText("Suplidor al que normalmente se le compra este artículo");

        txtCodigoProveedor.setHelperText("Referencia con la que el suplidor identifica el artículo");
        txtCodigoProveedor.setMaxLength(50);

        txtDiasEntrega.setMin(0);
        txtDiasEntrega.setStepButtonsVisible(true);
        txtDiasEntrega.setHelperText("Días que tarda el suplidor en entregar");

        txtCantidadMinimaCompra.setHelperText("Mínimo que el suplidor vende por pedido");

        txtDescuentoCompra.setHelperText("Entre 0 y 100; se sugiere al registrar facturas de compra");

        chkSeCompra.addValueChangeListener(e -> actualizarEstadoCamposCompra());

        FormLayout form = new FormLayout();
        form.add(chkSeCompra, cbProveedorPrincipal, txtCodigoProveedor, txtDiasEntrega,
                txtCantidadMinimaCompra, txtDescuentoCompra);

        form.setColspan(chkSeCompra, 2);
        form.setColspan(cbProveedorPrincipal, 2);

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

//        binder.bind(chkSeCompra,
//                a -> !Boolean.FALSE.equals(a.get()), Articulo::setSeCompra);
        
//        binder.bind(cbProveedorPrincipal, Articulo::getProveedorPrincipal, Articulo::setProveedorPrincipal);
//        
//        binder.bind(txtCodigoProveedor,
//                a -> a.getCodigoProveedor() == null ? "" : a.getCodigoProveedor(), Articulo::setCodigoProveedor);
//        binder.bind(txtDiasEntrega, Articulo::getDiasEntrega, Articulo::setDiasEntrega);
//        binder.bind(txtCantidadMinimaCompra, Articulo::getCantidadMinimaCompra, Articulo::setCantidadMinimaCompra);
//        binder.bind(txtDescuentoCompra, Articulo::getDescuentoCompra, Articulo::setDescuentoCompra);

        containerTabCompra.add(form);
    }

    /**
     * Si el artículo no se compra (por ejemplo, un producto elaborado en el
     * negocio), el resto de los datos de compra no aplican.
     */
    private void actualizarEstadoCamposCompra() {

        boolean seCompra = chkSeCompra.getValue();

        cbProveedorPrincipal.setEnabled(seCompra);
        txtCodigoProveedor.setEnabled(seCompra);
        txtDiasEntrega.setEnabled(seCompra);
        txtCantidadMinimaCompra.setEnabled(seCompra);
        txtDescuentoCompra.setEnabled(seCompra);
    }

    /**
     * Valores por defecto de un artículo nuevo, para los datos de inventario.
     */
    private void aplicarValoresPorDefectoInventario(Articulo articulo) {

        if (articulo.getInventariable() == false) {
            articulo.setInventariable(true);
        }
        if (articulo.getPermitirVentaSinExistencia() == true) {
            articulo.setPermitirVentaSinExistencia(true);
        }
        if (articulo.getPerecedero() == true) {
            articulo.setPerecedero(false);
        }

//        if (articulo.getMetodoCosteo() == null) {
//            articulo.setMetodoCosteo("PROMEDIO");
//        }
    }

    /**
     * Valores por defecto de un artículo nuevo, para los datos de compra: se
     * asume que es un artículo de compra.
     */
    private void aplicarValoresPorDefectoCompra(Articulo articulo) {
//
//        if (articulo.getSeCompra() == null) {
//            articulo.setSeCompra(true);
//        }
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

            Button btnEditar = new Button(new Icon(VaadinIcon.EDIT), e -> {

                abrirModalAlmacen(item);

            });

            btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button btnBorrar = new Button(new Icon(VaadinIcon.TRASH), e -> {
                listaArticuloAlmacen.remove(item);
//                gridAlmacenes.setItems(listaArticuloAlmacen);
            });
            btnBorrar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            return new HorizontalLayout(btnEditar);
        })).setHeader("Acciones");

        btnAgregarAlmacen.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarAlmacen.addClickListener(e -> abrirModalAlmacen(null));

        // 3. Configurar el DataProvider para romper la duplicidad del ID null
        artalmDataProvider = new ListDataProvider<>(listaArticuloAlmacen) {
            @Override
            public Object getId(ArticuloAlmacen item) {
                // Si el artículo ya está guardado en la BD, usa su ID real.
                // Si es nuevo y su ID es null, usa el código de identidad único del objeto en Java.
                return item.getCodigo() != null ? item.getCodigo() : System.identityHashCode(item);
            }
        };

        gridAlmacenes.setDataProvider(artalmDataProvider);

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
            if (itemEditar.getCodigo() != null) {
                // Ya guardado: la existencia solo cambia con movimientos (ajuste, compra,
                // venta...) para que el kardex y el stock no se descuadren.
                txtExistenciaModal.setLabel("Existencia");
                txtExistenciaModal.setReadOnly(true);
                txtExistenciaModal.setHelperText("Para corregirla use un Ajuste de Inventario.");
            }
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
            if (!(esEdicion && itemEditar.getCodigo() != null)) {
                aa.setExistencia(txtExistenciaModal.getValue() != null ? txtExistenciaModal.getValue() : BigDecimal.ZERO);
            }
            aa.setMinimo(txtMinimoModal.getValue() != null ? txtMinimoModal.getValue() : BigDecimal.ZERO);
            aa.setMaximo(txtMaximoModal.getValue() != null ? txtMaximoModal.getValue() : new BigDecimal("999999"));
            aa.setUbicacionPasillo(txtUbicacionModal.getValue());

            if (!esEdicion) {
                listaArticuloAlmacen.add(aa);
            }

            artalmDataProvider.refreshAll();
//            gridAlmacenes.setItems(listaArticuloAlmacen);
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

//                gridEmpaques.setItems(listaEmpaques);
            });
            btnBorrar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            return new HorizontalLayout(btnEditar, btnBorrar);
        })).setHeader("Acciones");

        btnAgregarEmpaque.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregarEmpaque.addClickListener(e -> abrirModalEmpaque(null));

        containerTabEmpaques.add(btnAgregarEmpaque, gridEmpaques);

        // 3. Configurar el DataProvider para romper la duplicidad del ID null
        dataProvider = new ListDataProvider<>(listaEmpaques) {
            @Override
            public Object getId(ArticuloEmpaque item) {
                // Si el artículo ya está guardado en la BD, usa su ID real.
                // Si es nuevo y su ID es null, usa el código de identidad único del objeto en Java.
                return item.getCodigo() != null ? item.getCodigo() : System.identityHashCode(item);
            }
        };

        gridEmpaques.setDataProvider(dataProvider);
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

        TextField txtDescEmpaque = new TextField("Descripcion Empaque");
        TextField txtBarcode = new TextField("Código de Barras");
        BigDecimalField txtPrecioCompraEmp = new BigDecimalField("Precio Compra Presentación");
        BigDecimalField txtPrecioVentaEmp = new BigDecimalField("Precio Venta Presentación");

        Checkbox chkBase = new Checkbox("Es Empaque Base");
        Checkbox chkCompra = new Checkbox("Empaque para Compras", true);
        Checkbox chkVenta = new Checkbox("Empaque para Ventas", true);

        if (esEdicion) {

            cbUnidad.setValue(empaqueEditar.getUnidadEmpaque());
            txtFactor.setValue(empaqueEditar.getFactorConversion());
            // Sin código de barras guardado el campo queda vacío (antes se rellenaba con "1",
            // que luego se guardaba como código de barras y chocaba con los demás empaques)
            txtBarcode.setValue(empaqueEditar.getCodigoBarra() == null ? "" : empaqueEditar.getCodigoBarra());

            txtDescEmpaque.setValue(empaqueEditar.getDescripcionEmpaque() == null ? "" : empaqueEditar.getDescripcionEmpaque());

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

        FormLayout form = new FormLayout(
                txtDescEmpaque,
                cbUnidad, txtFactor, txtBarcode, txtPrecioCompraEmp, txtPrecioVentaEmp, chkBase, chkCompra, chkVenta);

        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("300px", 2));

        Button btnGuardarModal = new Button(esEdicion ? "Guardar" : "Agregar", e -> {
            Unidad unidadSeleccionada = cbUnidad.getValue();
            BigDecimal factor = txtFactor.getValue();
            String barcode = txtBarcode.getValue() != null ? txtBarcode.getValue().trim() : "";

            if (txtDescEmpaque.getValue().isEmpty()) {

                Notification.show("La descripcion del empaque esta vacia", 3000, Notification.Position.MIDDLE);
                return;
            }

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

//            if (esEdicion == false) {
//                emp.setCodigo(unidadSeleccionada.getCodigo());
//            }
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
            emp.setDescripcionEmpaque(txtDescEmpaque.getValue());

            if (!esEdicion) {

                listaEmpaques.add(emp);
            }

//            gridEmpaques.setItems(listaEmpaques);
//            gridEmpaques.getDataProvider().refreshAll();
            // 2. Le avisas al dataProvider que la lista cambió
            dataProvider.refreshAll();
            dialog.close();
        });
        btnGuardarModal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelarModal = new Button("Cancelar", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(btnCancelarModal, btnGuardarModal);
        dialog.open();
    }

    // ==========================================
    // TAB 6: LOTES (solo lectura)
    // ==========================================
    private void configurarGridLotes() {
        java.time.format.DateTimeFormatter fecha = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        java.text.DecimalFormat cantidad = new java.text.DecimalFormat("#,##0.##");

        com.maxsoft.application.view.componente.EstiloConsulta.aplicarGrid(gridLotes);
        gridLotes.setWidthFull();
        gridLotes.setHeight("380px");
        gridLotes.addComponentColumn(la -> {
            com.maxsoft.application.view.inventario.lote.LotesVencimientosView.Estado e
                    = com.maxsoft.application.view.inventario.lote.LotesVencimientosView.estado(la, java.time.LocalDate.now());
            return com.maxsoft.application.view.componente.EstiloConsulta.chip(e.getEtiqueta(), e.getClase());
        }).setHeader("Estado").setAutoWidth(true).setFlexGrow(0);
        gridLotes.addColumn(la -> la.getLote().getNumeroLote()).setHeader("Lote").setAutoWidth(true);
        gridLotes.addColumn(la -> la.getLote().getFechaVencimiento() != null
                ? la.getLote().getFechaVencimiento().format(fecha) : "—").setHeader("Vence").setAutoWidth(true);
        gridLotes.addColumn(la -> la.getAlmacen().getNombre()).setHeader("Almacén").setAutoWidth(true);
        gridLotes.addColumn(la -> cantidad.format(la.getExistencia())).setHeader("Existencia (unidad base)")
                .setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        gridLotes.addColumn(la -> la.getLote().getCostoUnitario() != null
                ? com.maxsoft.application.view.componente.EstiloConsulta.moneda(la.getLote().getCostoUnitario()) : "—")
                .setHeader("Costo unit.").setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        gridLotes.addColumn(la -> la.getLote().getProveedor() != null ? la.getLote().getProveedor().getNombre() : "")
                .setHeader("Proveedor").setAutoWidth(true);
        gridLotes.addColumn(la -> la.getLote().getDocumentoOrigen() != null ? la.getLote().getDocumentoOrigen() : "")
                .setHeader("Documento").setAutoWidth(true).setFlexGrow(1);

        com.vaadin.flow.component.html.Span ayuda = new com.vaadin.flow.component.html.Span("Lotes del artículo con su existencia por almacén (los que vencen primero arriba)."
                + " Para el kardex de un lote use Inventario > Lotes y vencimientos.");
        ayuda.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");
        containerTabLotes.add(ayuda, gridLotes);
        containerTabLotes.setWidthFull();
    }

    private void cargarLotes() {
        if (articuloActual == null || articuloActual.getCodigo() == null) {
            gridLotes.setItems(new ArrayList<>());
            return;
        }
        gridLotes.setItems(loteService.getExistenciasPorArticulo(articuloActual.getCodigo()).stream()
                .filter(la -> la.getExistencia().signum() != 0).toList());
    }

    // ==========================================
    // LÓGICA DE EDICIÓN Y GUARDADO ATÓMICO
    // ==========================================
    private void editarArticulo(Articulo articulo) {

        this.articuloActual = articulo;
        this.articuloDaoService.setArticulo(articulo);

        // Un artículo nuevo arranca con los valores por defecto de inventario y compra
        if (articulo.getCodigo() == null) {
            aplicarValoresPorDefectoInventario(articulo);
            aplicarValoresPorDefectoCompra(articulo);
        }

        binder.setBean(articulo);
        actualizarEstadoCamposInventario();
        actualizarEstadoCamposCompra();

        listaEmpaques.clear();
        listaArticuloAlmacen.clear();

        if (articulo.getCodigo() != null) {

            listaEmpaques.addAll(articuloEmpaqueService.getPorArticulo(articuloActual.getCodigo()));

            listaArticuloAlmacen.addAll(articuloAlmacenService.buscarPorArticulo(articuloActual.getCodigo()).get());
        }

        cargarLotes();

//        gridEmpaques.setItems(listaEmpaques);
//        gridAlmacenes.setItems(listaArticuloAlmacen);
    }

    private void guardarArticulo() {

        if (articuloActual == null) {
            articuloActual = new Articulo();
        }

        articuloActual.setInventariable(chkInventariable.getValue());
//        articuloActual.setMetodoDeCosteo(cbMetodoCosteo.getValue();

        if (binder.writeBeanIfValid(articuloActual)) {

            // Validaciones de la pestaña de inventario
            if (Boolean.TRUE.equals(articuloActual.getInventariable())
                    && cbMetodoCosteo.getValue() == null) {

                tabs.setSelectedTab(tabInventario);
                cbMetodoCosteo.focus();
                Notification.show("Seleccione el método de costeo del artículo", 3500, Notification.Position.MIDDLE);
                return;
            }

            if (Boolean.TRUE.equals(articuloActual.getPerecedero())
                    && (articuloActual.getDiasVencimiento() == null || articuloActual.getDiasVencimiento() <= 0)) {

                tabs.setSelectedTab(tabInventario);
                txtDiasVencimiento.focus();
                Notification.show("Indique los días de vida útil del artículo perecedero", 3500, Notification.Position.MIDDLE);
                return;
            }

            // Validaciones de la pestaña de compra
            if (!validarDatosCompra()) {
                return;
            }

            if (listaEmpaques.isEmpty()) {

                ArticuloEmpaque emp = new ArticuloEmpaque();

                Unidad uni = null;

                if (null != articuloActual.getUnidadDeVenta()) {

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
            } else {
                System.out.println("listaEmpaques" + listaEmpaques.size());
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

    /**
     * Valida los datos de compra. Si algo no es válido, abre la pestaña de
     * compra, enfoca el campo y avisa.
     */
    private boolean validarDatosCompra() {

//        if (!Boolean.TRUE.equals(articuloActual.getSeCompra())) {
//            return true; // no se compra: los demás datos no aplican
//        }
//
//        BigDecimal descuento = articuloActual.getDescuentoCompra();
//        if (descuento != null && (descuento.signum() < 0 || descuento.compareTo(BigDecimal.valueOf(100)) > 0)) {
//            tabs.setSelectedTab(tabCompra);
//            txtDescuentoCompra.focus();
//            Notification.show("El descuento del suplidor debe estar entre 0 y 100", 3500, Notification.Position.MIDDLE);
//            return false;
//        }
//
//        BigDecimal minima = articuloActual.getCantidadMinimaCompra();
//        if (minima != null && minima.signum() < 0) {
//            tabs.setSelectedTab(tabCompra);
//            txtCantidadMinimaCompra.focus();
//            Notification.show("La cantidad mínima de compra no puede ser negativa", 3500, Notification.Position.MIDDLE);
//            return false;
//        }
//
//        Integer dias = articuloActual.getDiasEntrega();
//        if (dias != null && dias < 0) {
//            tabs.setSelectedTab(tabCompra);
//            txtDiasEntrega.focus();
//            Notification.show("El tiempo de entrega no puede ser negativo", 3500, Notification.Position.MIDDLE);
//            return false;
//        }

        return true;
    }

    private void limpiarFormulario() {
        editarArticulo(new Articulo());
        tabs.setSelectedTab(tabGeneral);
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