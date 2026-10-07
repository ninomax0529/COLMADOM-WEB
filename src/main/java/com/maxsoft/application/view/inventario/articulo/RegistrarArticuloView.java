package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.Unidad;
import com.maxsoft.application.modelo.UnidadDeVenta;
import com.maxsoft.application.servicio.ArticuloDaoService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.UnidadService;
import com.maxsoft.application.servicio.interfaces.venta.UnidadDeVentaService;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@PageTitle("Registrar Artículos")
@Route(value = "inventario/registrarArticulo")
public class RegistrarArticuloView extends VerticalLayout implements HasUrlParameter<String> {

    final ArticuloService articuloService;
    final UnidadDeVentaService unidaVentaService;
    final AlmacenService almacenService;
    final ArticuloAlmacenService articuloAlmacenService;
    UnidadService unidadService;

    Binder<Articulo> binder = new Binder<>(Articulo.class);

    TextField txtDescripcion = new TextField("Descripción");
    BigDecimalField txtPrecioCompra = new BigDecimalField("Precio de Compra Unitario");
    BigDecimalField txtPrecioVenta = new BigDecimalField("Precio de Venta Unitario");
    BigDecimalField txtExistencia = new BigDecimalField("Existencia Inicial");
    IntegerField txtCodigo = new IntegerField("Código");
    ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén por Defecto");
    RadioButtonGroup<UnidadDeVenta> rdbGrupo = new RadioButtonGroup<>();

    ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    FormLayout formLayout = new FormLayout();

    ArticuloDaoService articuloDaoService;
    private Articulo articuloActual;

    @Autowired
    public RegistrarArticuloView(
            ArticuloService articuloServiceArg,
            UnidadDeVentaService unidadDeVentaServiceArg,
            AlmacenService almacenServiceArg,
            ArticuloAlmacenService articuloAlmacenServiceArg,
            ArticuloDaoService articuloDaoServiceArg,
            UnidadService unidadService
    ) {

        this.articuloDaoService = articuloDaoServiceArg;
        this.articuloService = articuloServiceArg;
        this.unidaVentaService = unidadDeVentaServiceArg;
        this.almacenService = almacenServiceArg;
        this.articuloAlmacenService = articuloAlmacenServiceArg;
        this.unidadService=unidadService ;

        setSizeFull();
        setSpacing(false);

        add(botonera);

        botonera.getGuardar().addClickListener(e -> {

            try {

                guardarArticulo();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        botonera.getCancelar().addClickListener(e -> UI.getCurrent().navigate(ArticuloView.class));

        rdbGrupo.setLabel("Se Vende por :");
        rdbGrupo.setItems(unidaVentaService.getLista());

        configurarAlmacenes();
        configurarFormulario();
    }

    private void configurarAlmacenes() {
        List<Almacen> listaAlmacenes = almacenService.getLista();
        cbAlmacen.setItems(listaAlmacenes);
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);

        // Seleccionar el primer almacén disponible por defecto
        if (!listaAlmacenes.isEmpty()) {
            cbAlmacen.setValue(listaAlmacenes.get(0));
        }
    }

    private void configurarFormulario() {
        txtCodigo.setEnabled(false);
        txtCodigo.setWidth("100px");

        HorizontalLayout hbPrecio = new HorizontalLayout(txtPrecioCompra, txtPrecioVenta, txtExistencia, cbAlmacen);
        hbPrecio.setWidthFull();

        HorizontalLayout hlArt = new HorizontalLayout(txtCodigo, txtDescripcion);
        hlArt.addAndExpand(txtDescripcion);

        formLayout = new FormLayout(hlArt, hbPrecio, rdbGrupo);

        binder.bind(txtCodigo, Articulo::getCodigo, Articulo::setCodigo);
        binder.bind(txtDescripcion, Articulo::getDescripcion, Articulo::setDescripcion);
        binder.bind(txtPrecioCompra, Articulo::getPrecioCompra, Articulo::setPrecioCompra);
        binder.bind(txtPrecioVenta, Articulo::getPrecioVenta, Articulo::setPrecioVenta);
        binder.bind(txtExistencia, Articulo::getExistencia, Articulo::setExistencia);
        binder.bind(rdbGrupo, Articulo::getUnidadDeVenta, Articulo::setUnidadDeVenta);

        add(formLayout);
    }

    private void editarArticulo(Articulo articulo) {
        this.articuloActual = articulo;
        this.articuloDaoService.setArticulo(articulo);
        binder.setBean(articulo);
    }

    private void guardarArticulo() {

        if (articuloActual == null) {
            articuloActual = new Articulo();
        }

        if (binder.writeBeanIfValid(articuloActual)) {

            boolean esNuevo = (articuloActual.getCodigo() == null);

            articuloActual.setUnidadBase(new Unidad(1));
            // 1. Guardar el artículo
            Articulo articuloGuardado = articuloService.guardar(articuloActual);

            // 2. Si es un artículo nuevo y se seleccionó un almacén, crear la relación ArticuloAlmacen
            if (esNuevo && cbAlmacen.getValue() != null) {

                Almacen almacenSeleccionado = cbAlmacen.getValue();

                ArticuloAlmacen articuloAlmacen = new ArticuloAlmacen();

                articuloAlmacen.setFechaActualizacion(new Date());
                articuloAlmacen.setFechaCreacion(new Date());
                articuloAlmacen.setNombreAlmacen(almacenSeleccionado.getNombre());
                articuloAlmacen.setDescripcionArticulo(articuloGuardado.getDescripcion());
                articuloAlmacen.setNombreUnidad(articuloGuardado.getUnidadDeVenta().getNombre());

                articuloAlmacen.setArticulo(articuloGuardado);
                articuloAlmacen.setAlmacen(almacenSeleccionado);

                // Asignar existencia inicial y valores por defecto para límites
                BigDecimal existencia = articuloGuardado.getExistencia() != null ? articuloGuardado.getExistencia() : BigDecimal.ZERO;
                articuloAlmacen.setExistencia(existencia);
                articuloAlmacen.setMinimo(BigDecimal.ZERO);
                articuloAlmacen.setMaximo(new BigDecimal("999999"));

                articuloAlmacenService.guardar(articuloAlmacen);
            }

            Notification.show("Artículo registrado y asignado al almacén correctamente", 2000, Notification.Position.TOP_CENTER);
            limpiarFormulario();
        } else {

            Notification.show("Por favor complete los campos requeridos", 2000, Notification.Position.MIDDLE);
        }
    }

    private void limpiarFormulario() {
        editarArticulo(new Articulo());
        configurarAlmacenes(); // Reestablecer almacén por defecto
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
