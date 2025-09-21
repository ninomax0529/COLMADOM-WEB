/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.servicio.interfaces.FacturaDeVentaService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.ModuloPrincipal;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.maxsoft.application.view.venta.CobroView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Punto de Venta")
@Route(value = "puntoDeVenta")
//@Layout(value = "venta")
//https://github.com/ninomax0529/vaadin-railway.git
@Menu(order = 3, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)

public class PuntoDeVentaView extends VerticalLayout {

    private final Grid<DetalleFacturaDeVenta> grid = new Grid<>(DetalleFacturaDeVenta.class, false);
    private Binder<DetalleFacturaDeVenta> binder = new Binder<>(DetalleFacturaDeVenta.class);
    Editor<DetalleFacturaDeVenta> editor = grid.getEditor();
    private final TextField txtNumDoc = new TextField();
    TextField txtBuscar = new TextField();
    private final DatePicker dpFecha = new DatePicker();
    TextField txtCantidad = new TextField();
    TextField txtSubTotal = new TextField("Sub Total");
    TextField txtTotal = new TextField("Total a Cobrar");
    TextField txtDecuento = new TextField("Descuento");
    TextField txtItbis = new TextField("Itbis");
//    TextField txtTotalArt = new TextField("Total Articulo");

    private Button btnSalir;
    private Button btnArticulo = null;
    private Button btnAyuda = null;
    private Button btnCobrar = null;
    private Button btnCliente = null;
    private Button btnImprimir = null;
    private Button btnCancelar = null;
    private Button btnDescuento = null;
    Button deleteButton = null;
    Button btnEditar = null;
    TextField filtroNombre = new TextField();
    H3 lbNumDoc = new H3("Factura");

    FormLayout formLayout = new FormLayout();
    GridListDataView<DetalleFacturaDeVenta> dataView = null;

    ArticuloService articuloServicel;

    List<DetalleFacturaDeVenta> listDet = new ArrayList<>();

    VerticalLayout vlBotones = new VerticalLayout();
    HorizontalLayout hlContenniido = new HorizontalLayout();

    private FacturaDeVenta factura;
    FacturaDeVentaService factService;
    int i = 0;

    @Autowired
    public PuntoDeVentaView(FacturaDeVentaService factServiceArg,
            ArticuloService articuloServiceArg) {

        setSizeFull();
        ////        setSpacing(true);

        this.factService = factServiceArg;
        this.articuloServicel = articuloServiceArg;

        configurarFormulario();
        configurarGridDetalle();
        editarDetalle();

        hlContenniido.setWidth("90%");
        hlContenniido.setHeightFull();
        hlContenniido.setAlignItems(Alignment.START);
        hlContenniido.setAlignSelf(Alignment.AUTO, grid);

        hlContenniido.setSpacing(false);
        vlBotones.setWidth("20px");
        vlBotones.setPadding(true);
        vlBotones.setMargin(false);

        hlContenniido.add(grid, vlBotones);
        add(formLayout, hlContenniido);

    }

    private void configurarFormulario() {

        btnArticulo = new Button("(F2)", e -> buscarArticulo());

        btnImprimir = new Button("Imprimir(F6)", e -> guardar());

        btnSalir = new Button("SalirF12()", e -> UI.getCurrent().navigate(ModuloPrincipal.class));

        btnAyuda = new Button("Ayuda(F1)", e -> UI.getCurrent().navigate(ModuloPrincipal.class));

        btnCobrar = new Button("Cobrar(F10)", e -> cobrar());

        btnCliente = new Button("Cliente(F7)", e -> UI.getCurrent().navigate(ModuloPrincipal.class));

        btnDescuento = new Button("Descuento(F5)", e -> UI.getCurrent().navigate(ModuloPrincipal.class));

        btnCancelar = new Button("Cancelar(F9)", e -> UI.getCurrent().navigate(ModuloPrincipal.class));

        txtNumDoc.setWidth("120px");

        btnArticulo.setWidth("10px");
        btnImprimir.setWidth("150px");
        btnSalir.setWidth("150px");
        btnCliente.setWidth("150px");
        btnCobrar.setWidth("150px");
        btnDescuento.setWidth("150px");
        btnCancelar.setWidth("150px");

        txtSubTotal.setWidth("150px");
        txtDecuento.setWidth("150px");
        txtItbis.setWidth("150px");
        txtTotal.setWidth("150px");

//        txtSubTotal.setEnabled(false);
        txtDecuento.setEnabled(false);
        txtItbis.setEnabled(false);
        txtTotal.setEnabled(false);

        HorizontalLayout hlArt = new HorizontalLayout();

        VerticalLayout vllTotal = new VerticalLayout();

        vllTotal.add(txtSubTotal, txtDecuento, txtItbis, txtTotal);

        vllTotal.setSpacing(false);

        hlArt.add(dpFecha, lbNumDoc, txtNumDoc, txtBuscar, btnArticulo);
        hlArt.setAlignItems(Alignment.START);

        vlBotones.add(new H3("Resumen: "));

        Hr separador = new Hr();

        TextField campoAzul = new TextField("Usuario");
        campoAzul.getStyle()
                .set("background-color", "#1565c0") // azul fuerte
                .set("color", "red") // texto blanco
                .set("border-radius", "8px")
                .set("padding", "8px")
                .set("font-weight", "bold");

        txtSubTotal.getStyle()
                .set("background-color", "1565c0") // gris muy claro azulado
                .set("border-radius", "8px")
                .set("color", "red")
                .set("padding", "8px");

        vlBotones.add(txtSubTotal, txtDecuento, txtItbis, txtTotal, separador, btnCobrar, btnImprimir, btnDescuento, btnCliente, btnCancelar);

        vlBotones.setSpacing(false);
        vlBotones.setAlignItems(Alignment.START);

        btnAyuda.addClickShortcut(Key.F1);
        btnArticulo.addClickShortcut(Key.F2);
        btnCliente.addClickShortcut(Key.F7);
        btnCancelar.addClickShortcut(Key.F9);
        btnCobrar.addClickShortcut(Key.F10);
        btnImprimir.addClickShortcut(Key.F6);
        btnDescuento.addClickShortcut(Key.F5);
        btnSalir.addClickShortcut(Key.F12);
        txtBuscar.focus();
        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

        txtBuscar.setWidth("150px");
        txtBuscar.setPlaceholder("Articulo");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
        txtBuscar.addValueChangeListener(e -> dataView.refreshAll());
        txtBuscar.setClearButtonVisible(true);

        filtroNombre.setPlaceholder("Filtrar por nombre");
        filtroNombre.setClearButtonVisible(true);
        filtroNombre.setPrefixComponent(new Icon(VaadinIcon.SEARCH));

        formLayout.add(hlArt);

    }

    private void guardar() {

        try {

            if (listDet.size() <= 0) {

                Notification.show("La factura no tiene detalle ",
                        3000, Notification.Position.TOP_CENTER);
                return;
            }

            for (DetalleFacturaDeVenta det2 : listDet) {

                if (det2.getCantidad() <= 0) {

                    Notification.show("El articulo  " + det2.getDescripcionArticulo() + " tiene la cantidad en cero ",
                            4000, Notification.Position.TOP_CENTER);
                    return;
                }

            }

            LocalDate localFecha = dpFecha.getValue();
            Date fecha = ClaseUtil.asDate(localFecha);

            factura = new FacturaDeVenta();
            factura.setFecha(fecha);
            factura.setFechaCreacion(new Date());
            factura.setFechaActualizacion(new Date());
            factura.setNombreUsuario("Administrador");

            listDet.stream().forEach(e -> {

                e.setFactura(factura);
                e.setCodigo(null);
            });

            factura.setDetalleFacturaDeVentaCollection(listDet);
            this.factService.guardar(factura);
            Notification.show("Factura guardada exitosamente", 3000, Notification.Position.TOP_CENTER);
            listDet.clear();
            grid.getDataProvider().refreshAll();
            txtBuscar.focus();

        } catch (Exception e) {
            Notification.show("Error guardando la factura ", 3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }

    }

    private void configurarGridDetalle() {

        grid.setHeight("90%");
        grid.setWidth("90%");

        grid.setItems(listDet);

        dataView = grid.setItems(listDet);

        grid.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo).setHeader("Articulo")
                .setKey("articulo")
//                .setFooter("TOTAL:")
                ;

        grid.addColumn(DetalleFacturaDeVenta::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad");

        grid.addColumn(DetalleFacturaDeVenta::getPrecioVenta)
                .setHeader("Precio")
                .setKey("precio");

        grid.addColumn(DetalleFacturaDeVenta::getSubTotal)
                .setHeader("SubTotal")
                .setKey("subTotal");

        grid.addColumn(DetalleFacturaDeVenta::getExistenciaActual)
                .setHeader("Existencia")
                .setKey("existencia");

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        grid.getColumnByKey("cantidad").setEditorComponent(txtCantidad);

        grid.addColumn(new ComponentRenderer<>(item -> {

            HorizontalLayout actions = new HorizontalLayout();

            btnEditar = new Button(new Button("(F4)", click -> {

                editarDetalle();
            }));

            btnEditar.addClickListener(e -> {

                if (editor.isOpen()) {
                    editor.cancel();
                }
                grid.getEditor().editItem(item);

            });

            txtCantidad.setValue(Double.toString(item.getCantidad()));

            deleteButton = new Button("🗑️ (F3)", click -> {

                if (!txtBuscar.isEmpty()) {

                    listDet.remove(item);
                    grid.getDataProvider().refreshAll();
                    deleteButton.addClickShortcut(Key.F3);
                    txtBuscar.clear();
                    filtroNombre.clear();
                }

//                Notification.show("Articulo eliminado");
            });

            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            deleteButton.addClickShortcut(Key.F3);
            btnEditar.addClickShortcut(Key.DIGIT_1);

            actions.add(btnEditar, deleteButton);

            return actions;

        })).setHeader("Acciones").setAutoWidth(true);

        dataView.addFilter(selectArticulo -> {

            String searchTerm = txtBuscar.getValue().trim();

            if (searchTerm.isEmpty()) {
                return true;
            }

            boolean matchesDescripcion = matchesTerm(selectArticulo.getArticulo().getDescripcion(),
                    searchTerm);
            boolean matchesCodigo = matchesTerm(selectArticulo.getCodigo().toString(), searchTerm);
            grid.select(selectArticulo);

            return matchesDescripcion || matchesCodigo;
        });

    }

    private void editarDetalle() {

        editor = grid.getEditor();
        editor.setBuffered(true); // sin botón de guardar
        binder = new Binder<>(DetalleFacturaDeVenta.class);
        editor.setBinder(binder);

        TextField cantidadField = new TextField();
        cantidadField.setWidthFull();

        grid.getColumnByKey("cantidad").setEditorComponent(cantidadField);

        grid.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel(); // cerrar el editor anterior
                return;
            }

            editor.editItem(event.getItem());

        });

        editor.addOpenListener(e -> {

            DetalleFacturaDeVenta item = e.getItem();
            cantidadField.setValue(String.valueOf(item.getCantidad()));

        });

        cantidadField.addValueChangeListener(e -> {

            Double cantidad;

            DetalleFacturaDeVenta item = editor.getItem();

            try {

                cantidad = Double.valueOf(e.getValue());

                if (cantidad <= 0) {

                    Notification.show("La cantidad no puede ser menor O igual a cero ", 3000, Notification.Position.MIDDLE);
                    cantidad = 0.00;
                    return;
                }

                if (item != null) {

                    item.setCantidad(cantidad);

                    item.setSubTotal(subTotal(cantidad, item.getPrecioVenta()));

                    item.setTotalDescuento(totalDescuento(item.getSubTotal(), item.getPorcientoDescuento()));

                    item.setTotalItbis(totalItbis(item.getSubTotal(), item.getTotalDescuento(), 18.00));

                    item.setTotal(total(item.getSubTotal(), item.getTotalDescuento(), item.getTotalItbis()));

                    grid.getListDataView().refreshAll();
                    txtBuscar.clear();
                    filtroNombre.clear();
                }

            } catch (NumberFormatException ex) {
                Notification.show(" Ingrese un número válido ", 2000, Notification.Position.MIDDLE);

                cantidadField.clear();
            }
        });

    }

    private Double subTotal(Double cant, Double precio) {

        Double sutTotal = cant * precio;

        return ClaseUtil.FormatearDouble(sutTotal, 2);

    }

    private Double total(Double subTotal, Double totalDesc, Double totalItbis) {

        Double total = (subTotal - totalDesc) + totalItbis;

        System.out.println("Total " + subTotal + " " + totalDesc + " " + totalItbis);
        return ClaseUtil.FormatearDouble(total, 2);
    }

    private Double totalItbis(Double subTotal, Double descuento, Double itbis) {

        itbis = itbis / 100.00;//Converftir el itbis en tasa

        Double totalItbis = (subTotal - descuento) * itbis;

        return ClaseUtil.FormatearDouble(totalItbis, 2);
    }

    private Double totalDescuento(Double subTotal, Double desc) {

        desc = desc / 100.00;//Converftir el itbis en tasa

        Double totalDesc = subTotal * desc;

        return ClaseUtil.FormatearDouble(totalDesc, 2);
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    private void buscarArticulo() {

        try {

            ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloServicel, articulo -> {

                if (!(articulo == null)) {

                   boolean existe=existeArticulo(listDet, articulo.getCodigo());
                   
                   if(existe){
                       
                       listDet.forEach((var d)->{
                           
                         if(Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())){
                             d.setCantidad(d.getCantidad()+1);
                             grid.getDataProvider().refreshAll();
                            
                         }
                           
                       });
                       return;
                   }
                    
                    DetalleFacturaDeVenta det1 = new DetalleFacturaDeVenta();

                    det1.setCodigo(articulo.getCodigo());//Colocarlo anull cuando le asignemo el encabezada

                    det1.setArticulo(articulo);

                    det1.setDescripcionArticulo(articulo.getDescripcion());

                    det1.setCantidad(1.00);
                    det1.setExistenciaActual(articulo.getExistencia());

                    det1.setPrecioVenta(articulo.getPrecioVenta());

                    det1.setSubTotal(subTotal(1.00, det1.getPrecioVenta()));
                    det1.setPorcientoDescuento(10.00);
                    det1.setPorcientoItbis(18.00);

                    det1.setTotalDescuento(totalDescuento(det1.getSubTotal(), det1.getPorcientoDescuento()));

                    det1.setTotalItbis(totalItbis(det1.getSubTotal(), det1.getTotalDescuento(), det1.getPorcientoItbis()));

                    det1.setTotal(total(det1.getSubTotal(), det1.getTotalDescuento(), det1.getTotalItbis()));

                    det1.setNombreAlmacen("General");
                    det1.setNombreUnidad("Unidad");

                    listDet.add(det1);

                    System.out.println("Articulo ingresó: " + articulo.getDescripcion());
//            
                    grid.getDataProvider().refreshAll(); // Refrescar el Grid sin perder la lista
                }

            });

            dialog.open();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void cobrar() {

        CobroView dialog = new CobroView(12.99); // total simulado
        dialog.open();

    }
    

    public static boolean existeArticulo(List<DetalleFacturaDeVenta> lista, int art) {
        return lista.stream()
                .anyMatch(n -> n.getArticulo().getCodigo() == art);
    }

    
}



