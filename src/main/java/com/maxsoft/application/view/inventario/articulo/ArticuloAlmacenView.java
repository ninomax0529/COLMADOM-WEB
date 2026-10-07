/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PermitAll
@PageTitle("Asignación de Artículos por Almacén")
@Route(value = "inventario/articuloalmacen")
public class ArticuloAlmacenView extends VerticalLayout {

    private final Grid<ArticuloAlmacen> grid = new Grid<>(ArticuloAlmacen.class, false);
    private final TextField txtBuscar = new TextField();
    private final ComboBox<Almacen> cbFiltroAlmacen = new ComboBox<>("Filtrar por Almacén");
    private final Button btnNuevo = new Button("Nueva Asignación", new Icon(VaadinIcon.PLUS));

    private final ArticuloAlmacenService articuloAlmacenService;
    private final ArticuloService articuloService;
    private final AlmacenService almacenService;

    private List<ArticuloAlmacen> listaArticulosAlmacen = new ArrayList<>();
    private GridListDataView<ArticuloAlmacen> dataView;

    @Autowired
    public ArticuloAlmacenView(ArticuloAlmacenService articuloAlmacenService,
            ArticuloService articuloService,
            AlmacenService almacenService) {

        this.articuloAlmacenService = articuloAlmacenService;
        this.articuloService = articuloService;
        this.almacenService = almacenService;

        setSizeFull();
        configurarControlesSuperiores();
        configurarGrid();

        HorizontalLayout hlFiltros = new HorizontalLayout(txtBuscar, cbFiltroAlmacen, btnNuevo);
        hlFiltros.setWidthFull();
        hlFiltros.setAlignItems(Alignment.BASELINE);

        add(hlFiltros, grid);
        actualizarGrid();
    }

    private void configurarControlesSuperiores() {
        txtBuscar.setPlaceholder("Buscar por artículo o código...");
        txtBuscar.setWidth("40%");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
        txtBuscar.addValueChangeListener(e -> aplicarFiltros());

        cbFiltroAlmacen.setItems(almacenService.getLista());
        cbFiltroAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbFiltroAlmacen.setClearButtonVisible(true);
        cbFiltroAlmacen.addValueChangeListener(e -> aplicarFiltros());

        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> abrirFormularioModal(new ArticuloAlmacen()));
    }

    private void configurarGrid() {

        grid.setSizeFull();

        grid.addColumn(ArticuloAlmacen::getDescripcionArticulo).setHeader("Artículo").setAutoWidth(true);
        grid.addColumn(ArticuloAlmacen::getNombreAlmacen).setHeader("Almacén").setAutoWidth(true);
        grid.addColumn(ArticuloAlmacen::getExistencia).setHeader("Existencia").setAutoWidth(true);
        grid.addColumn(ArticuloAlmacen::getNombreUnidad).setHeader("Unidad").setAutoWidth(true);
        grid.addColumn(ArticuloAlmacen::getMinimo).setHeader("Mínimo").setAutoWidth(true);
        grid.addColumn(ArticuloAlmacen::getMaximo).setHeader("Máximo").setAutoWidth(true);
        grid.addColumn(ArticuloAlmacen::getUbicacionPasillo).setHeader("Pasillo/Ubicación").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(item -> {

            Button btnEditar = new Button(new Icon(VaadinIcon.EDIT), e -> abrirFormularioModal(item));
            btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button btnBorrar = new Button(new Icon(VaadinIcon.TRASH), e -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Desea eliminar la configuración de '" + item.getDescripcionArticulo() + "' en '" + item.getNombreAlmacen() + "'?",
                        () -> {
//                            articuloAlmacenService.eliminar(item.getCodigo());
                            actualizarGrid();
                            ClaseUtil.mostrarNotificacion("Registro eliminado exitosamente", NotificationVariant.LUMO_SUCCESS);
                        },
                        () -> {
                        }
                );
                dialog.open();
            });
            btnBorrar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            return new HorizontalLayout(btnEditar, btnBorrar);
        })).setHeader("Acciones").setAutoWidth(true);
    }

    private void actualizarGrid() {
        listaArticulosAlmacen = articuloAlmacenService.getLista();
        dataView = grid.setItems(listaArticulosAlmacen);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (dataView == null) {
            return;
        }

        String term = txtBuscar.getValue() != null ? txtBuscar.getValue().trim().toLowerCase() : "";
        Almacen almacenSel = cbFiltroAlmacen.getValue();

        dataView.addFilter(item -> {
            boolean matchTexto = term.isEmpty()
                    || (item.getDescripcionArticulo() != null && item.getDescripcionArticulo().toLowerCase().contains(term))
                    || (item.getCodigo() != null && item.getCodigo().toString().contains(term));

            boolean matchAlmacen = almacenSel == null
                    || (item.getAlmacen() != null && item.getAlmacen().getCodigo().equals(almacenSel.getCodigo()));

            return matchTexto && matchAlmacen;
        });
    }

    private void abrirFormularioModal(ArticuloAlmacen entidad) {
        
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(entidad.getCodigo() == null ? "Nueva Configuración Artículo-Almacén" : "Editar Asignación");
        dialog.setWidth("550px");

        Binder<ArticuloAlmacen> binder = new BeanValidationBinder<>(ArticuloAlmacen.class);

        ComboBox<Articulo> cbArticulo = new ComboBox<>("Artículo");
        cbArticulo.setItems(articuloService.getLista());
        cbArticulo.setItemLabelGenerator(Articulo::getDescripcion);
        cbArticulo.setRequired(true);

        ComboBox<Almacen> cbAlmacenModal = new ComboBox<>("Almacén");
        cbAlmacenModal.setItems(almacenService.getLista());
        cbAlmacenModal.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacenModal.setRequired(true);

        BigDecimalField txtExistencia = new BigDecimalField("Existencia Inicial");
        BigDecimalField txtMinimo = new BigDecimalField("Stock Mínimo");
        BigDecimalField txtMaximo = new BigDecimalField("Stock Máximo");
        TextField txtUbicacion = new TextField("Ubicación / Pasillo");

//        if (entidad.getCodigo() != null) {
//            cbArticulo.setEnabled(false);
//            cbAlmacenModal.setEnabled(false);
//        }

        cbArticulo.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue().getUnidadBase()!= null) {
                entidad.setUnidad(e.getValue().getUnidadBase());
            }
        });

        binder.forField(cbArticulo).asRequired("Seleccione un artículo").bind(ArticuloAlmacen::getArticulo, ArticuloAlmacen::setArticulo);
        binder.forField(cbAlmacenModal).asRequired("Seleccione un almacén").bind(ArticuloAlmacen::getAlmacen, ArticuloAlmacen::setAlmacen);
        binder.forField(txtExistencia).asRequired("Ingrese la existencia").bind(ArticuloAlmacen::getExistencia, ArticuloAlmacen::setExistencia);
        binder.forField(txtMinimo).asRequired("Ingrese stock mínimo").bind(ArticuloAlmacen::getMinimo, ArticuloAlmacen::setMinimo);
        binder.forField(txtMaximo).asRequired("Ingrese stock máximo").bind(ArticuloAlmacen::getMaximo, ArticuloAlmacen::setMaximo);
        binder.forField(txtUbicacion).bind(ArticuloAlmacen::getUbicacionPasillo, ArticuloAlmacen::setUbicacionPasillo);

        binder.readBean(entidad);

        FormLayout formLayout = new FormLayout(cbArticulo, cbAlmacenModal, txtExistencia, txtMinimo, txtMaximo, txtUbicacion);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("300px", 2));

        Button btnGuardarModal = new Button("Guardar", e -> {
            try {

                entidad.setFechaActualizacion(new Date());
                entidad.setFechaCreacion(new Date());
                entidad.setNombreAlmacen(cbAlmacenModal.getValue().getNombre());
                entidad.setDescripcionArticulo(cbArticulo.getValue().getDescripcion());
                entidad.setNombreUnidad(cbArticulo.getValue().getUnidadDeVenta().getNombre());

                binder.writeBean(entidad);

                if (entidad.getMinimo().compareTo(entidad.getMaximo()) > 0) {
                    Notification.show("El stock mínimo no puede ser mayor al máximo", 3000, Notification.Position.MIDDLE);
                    return;
                }

                articuloAlmacenService.guardar(entidad);
                actualizarGrid();
                dialog.close();
                ClaseUtil.mostrarNotificacion("Configuración guardada correctamente", NotificationVariant.LUMO_SUCCESS);
            } catch (ValidationException ex) {
                ex.printStackTrace();
                Notification.show("Complete todos los campos requeridos correctamente", 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Error al guardar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        btnGuardarModal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelarModal = new Button("Cancelar", e -> dialog.close());

        dialog.add(formLayout);
        dialog.getFooter().add(btnCancelarModal, btnGuardarModal);
        dialog.open();
    }
}
