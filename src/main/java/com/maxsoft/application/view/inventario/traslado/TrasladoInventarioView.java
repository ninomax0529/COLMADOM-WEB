/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.traslado;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.TrasladoInventario;
import com.maxsoft.application.repo.AlmacenRepo;
import com.maxsoft.application.repo.ArticuloAlmacenRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.TrasladoInventarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



@PermitAll
@PageTitle("Traslado de Inventario | MaxSoft ERP")
@Route(value = "inventario/registroTraslado")
public class TrasladoInventarioView extends VerticalLayout {

    // Servicios / Repositorios
    private final TrasladoInventarioService trasladoService;
    private final AlmacenRepo almacenRepo;
    private final ArticuloAlmacenService articuloAlmacenService;

    // Componentes de la interfaz
    private ComboBox<Almacen> cbAlmacenOrigen;
    private ComboBox<Almacen> cbAlmacenDestino;
    private ComboBox<ArticuloAlmacen> cbArticulo;
    private NumberField nfCantidad;
    private NumberField nfStockDisponible;
    private TextArea txtObservacion;
    private Button btnAgregar;
    private Button btnProcesar;
    private Button btnLimpiar;

    // Grid y Colección de Datos
    private Grid<DetalleTrasladoInventario> gridDetalle;
    private List<DetalleTrasladoInventario> listaDetalles = new ArrayList<>();

    @Autowired
    public TrasladoInventarioView(TrasladoInventarioService trasladoService,
                                  AlmacenRepo almacenRepo,
                                  ArticuloAlmacenService articuloAlmacenService) {
        this.trasladoService = trasladoService;
        this.almacenRepo = almacenRepo;
        this.articuloAlmacenService = articuloAlmacenService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Construir la interfaz
        add(new H2("Transferencia / Traslado de Inventario"));
        add(crearFormularioCabecera());
        add(crearFormularioDetalle());
        add(crearGridDetalle());
        add(crearBotonesAccion());

        // Cargar datos iniciales
        cargarAlmacenes();
    }

    private FormLayout crearFormularioCabecera() {
        FormLayout formCabecera = new FormLayout();

        cbAlmacenOrigen = new ComboBox<>("Almacén Origen");
        cbAlmacenOrigen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenOrigen.setRequired(true);

        cbAlmacenDestino = new ComboBox<>("Almacén Destino");
        cbAlmacenDestino.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenDestino.setRequired(true);

        // Listener: Validar que origen y destino no sean iguales
        cbAlmacenOrigen.addValueChangeListener(e -> {
            validarAlmacenes();
            actualizarComboArticulos();
        });

        cbAlmacenDestino.addValueChangeListener(e -> validarAlmacenes());

        txtObservacion = new TextArea("Observación / Motivo");
        txtObservacion.setPlaceholder("Ingrese detalles adicionales del traslado...");

        formCabecera.add(cbAlmacenOrigen, cbAlmacenDestino, txtObservacion);
        formCabecera.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3)
        );

        return formCabecera;
    }

    private HorizontalLayout crearFormularioDetalle() {
        HorizontalLayout layoutDetalle = new HorizontalLayout();
        layoutDetalle.setWidthFull();
        layoutDetalle.setAlignItems(Alignment.BASELINE);

        cbArticulo = new ComboBox<>("Seleccionar Artículo");
        cbArticulo.setItemLabelGenerator(aa -> aa.getArticulo().getDescripcion());
        cbArticulo.setWidth("40%");
        cbArticulo.setEnabled(false);

        nfStockDisponible = new NumberField("Stock Disponible");
        nfStockDisponible.setReadOnly(true);
        nfStockDisponible.setWidth("15%");

        cbArticulo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                nfStockDisponible.setValue(e.getValue().getExistencia().doubleValue());
            } else {
                nfStockDisponible.setValue(0.0);
            }
        });

        nfCantidad = new NumberField("Cantidad a Transferir");
        nfCantidad.setMin(1);
        nfCantidad.setWidth("20%");

        btnAgregar = new Button("Agregar", VaadinIcon.PLUS.create());
        btnAgregar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregar.addClickListener(e -> agregarDetalleGrid());

        layoutDetalle.add(cbArticulo, nfStockDisponible, nfCantidad, btnAgregar);
        return layoutDetalle;
    }

    private Grid<DetalleTrasladoInventario> crearGridDetalle() {
        gridDetalle = new Grid<>(DetalleTrasladoInventario.class, false);
        gridDetalle.setWidthFull();
        gridDetalle.setHeight("300px");

        gridDetalle.addColumn(d -> d.getArticulo().getCodigo()).setHeader("Código").setFlexGrow(1);
        gridDetalle.addColumn(d -> d.getArticulo().getDescripcion()).setHeader("Descripción").setFlexGrow(3);
        gridDetalle.addColumn(d -> d.getCantidadEnviada().toString()).setHeader("Cantidad Enviada").setFlexGrow(1);

        // Columna para eliminar un renglón
        gridDetalle.addComponentColumn(detalle -> {
            Button btnEliminar = new Button(VaadinIcon.TRASH.create());
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            btnEliminar.addClickListener(e -> {
                listaDetalles.remove(detalle);
                gridDetalle.setItems(listaDetalles);
            });
            return btnEliminar;
        }).setHeader("Acciones").setFlexGrow(1);

        return gridDetalle;
    }

    private HorizontalLayout crearBotonesAccion() {
        HorizontalLayout layoutAcciones = new HorizontalLayout();

        btnProcesar = new Button("Procesar Traslado", VaadinIcon.CHECK.create());
        btnProcesar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnProcesar.addClickListener(e -> procesarTraslado());

        btnLimpiar = new Button("Limpiar Formulario", VaadinIcon.REFRESH.create());
        btnLimpiar.addClickListener(e -> limpiarFormulario());

        layoutAcciones.add(btnProcesar, btnLimpiar);
        return layoutAcciones;
    }

    // -----------------------------------------------------------------
    // LÓGICA DE CONTROL Y EVENTOS
    // -----------------------------------------------------------------

    private void cargarAlmacenes() {
        List<Almacen> almacenes = almacenRepo.findAll();
        cbAlmacenOrigen.setItems(almacenes);
        cbAlmacenDestino.setItems(almacenes);
    }

    private void validarAlmacenes() {
        if (cbAlmacenOrigen.getValue() != null && cbAlmacenDestino.getValue() != null) {
            if (cbAlmacenOrigen.getValue().equals(cbAlmacenDestino.getValue())) {
                mostrarNotificacion("El almacén de origen y destino no pueden ser el mismo.", NotificationVariant.LUMO_ERROR);
                cbAlmacenDestino.clear();
            }
        }
    }

    private void actualizarComboArticulos() {
        
        if (cbAlmacenOrigen.getValue() != null) {
            
            // Cargar artículos que estén asignados al almacén origen
            List<ArticuloAlmacen> articulosOrigen = articuloAlmacenService.buscarPorAlmacen(
                    cbAlmacenOrigen.getValue().getCodigo() ).get();
            
                    
            cbArticulo.setItems(articulosOrigen);
            cbArticulo.setEnabled(true);
        } else {
            cbArticulo.clear();
            cbArticulo.setEnabled(false);
        }
    }

    private void agregarDetalleGrid() {
        ArticuloAlmacen stockSeleccionado = cbArticulo.getValue();
        Double cantidad = nfCantidad.getValue();

        if (stockSeleccionado == null) {
            mostrarNotificacion("Seleccione un artículo.", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (cantidad == null || cantidad <= 0) {
            mostrarNotificacion("Ingrese una cantidad válida mayor a 0.", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (cantidad > stockSeleccionado.getExistencia().doubleValue()) {
            mostrarNotificacion("La cantidad supera el stock disponible (" + stockSeleccionado.getExistencia() + ")", NotificationVariant.LUMO_ERROR);
            return;
        }

        // Crear detalle
        DetalleTrasladoInventario detalle = new DetalleTrasladoInventario();
        detalle.setArticulo(stockSeleccionado.getArticulo());
        detalle.setCantidadEnviada(BigDecimal.valueOf(cantidad));
        detalle.setCostoUnitario(stockSeleccionado.getArticulo().getPrecioCompra());

        listaDetalles.add(detalle);
        gridDetalle.setItems(listaDetalles);

        // Limpiar controles de detalle
        cbArticulo.clear();
        nfCantidad.clear();
        nfStockDisponible.clear();
    }

    private void procesarTraslado() {
        if (cbAlmacenOrigen.getValue() == null || cbAlmacenDestino.getValue() == null) {
            mostrarNotificacion("Debe seleccionar origen y destino.", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (listaDetalles.isEmpty()) {
            mostrarNotificacion("Debe agregar al menos un ítem al traslado.", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            
            TrasladoInventario traslado = new TrasladoInventario();
            traslado.setAlmacenOrigen(cbAlmacenOrigen.getValue());
            traslado.setAlmacenDestino(cbAlmacenDestino.getValue());
            traslado.setObservacion(txtObservacion.getValue());
            traslado.setNumeroDocumento("TR-" + System.currentTimeMillis());
            traslado.setDetalleTrasladoInventarioCollection(listaDetalles);

            trasladoService.procesarTraslado(traslado);

            mostrarNotificacion("¡Traslado procesado exitosamente!", NotificationVariant.LUMO_SUCCESS);
            limpiarFormulario();

        } catch (Exception ex) {
            mostrarNotificacion("Error al procesar traslado: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarFormulario() {
        cbAlmacenOrigen.clear();
        cbAlmacenDestino.clear();
        cbArticulo.clear();
        txtObservacion.clear();
        nfCantidad.clear();
        nfStockDisponible.clear();
        listaDetalles.clear();
        gridDetalle.setItems(listaDetalles);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification notif = Notification.show(mensaje, 4000, Notification.Position.TOP_END);
        notif.addThemeVariants(variante);
    }
}