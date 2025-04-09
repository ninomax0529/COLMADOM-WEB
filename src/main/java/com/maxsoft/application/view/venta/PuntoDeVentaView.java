/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.servicio.interfaces.FacturaDeVentaService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.ModuloPrincipal;
import com.maxsoft.application.view.inventario.articulo.ConsultaArticuloDgView;
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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Punto de Venta")
@Route(value = "puntoDeVenta")
//@Layout(value = "venta")
@Menu(order = 3, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)

public class PuntoDeVentaView extends VerticalLayout {

    private Grid<DetalleFacturaDeVenta> grid = new Grid<>(DetalleFacturaDeVenta.class, false);
    private ListDataProvider<DetalleFacturaDeVenta> dataProvider = null;
    private ListDataProvider<Articulo> dataProviderArt;
    private Binder<DetalleFacturaDeVenta> binder = new Binder<>(DetalleFacturaDeVenta.class);
    Editor<DetalleFacturaDeVenta> editor = grid.getEditor();
    private final Dialog consultaArticuloDialog = new Dialog();
    private Grid<Articulo> gridArt = new Grid<>(Articulo.class, false);
    private Articulo articulo;

    DetalleEntradaInventario det;

    private TextField txtNumDoc = new TextField("Numero Entrada");
    private DatePicker dpFecha = new DatePicker("Fecha");
    TextField txtCantidad = new TextField();

    private Button btnGuardar = new Button("Guardar");
    private Button btnSalir;

    ArticuloService articuloServicel;

    List<DetalleFacturaDeVenta> listDet = new ArrayList<>();

    HorizontalLayout botones = new HorizontalLayout();

    private FacturaDeVenta factura;
    FacturaDeVentaService factService;
    int i = 0;

    @Autowired
    public PuntoDeVentaView(FacturaDeVentaService factServiceArg,
            ArticuloService articuloServiceArg) {

        this.factService = factServiceArg;
        this.articuloServicel = articuloServiceArg;

        btnSalir = new Button("Salir", e -> UI.getCurrent().navigate(ModuloPrincipal.class) );

        btnGuardar = new Button("Guardar",
                event -> {

                    try {

                        guardar();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

        Button btnNuevo = new Button("Agragar Ariculo",
                event -> {

                    try {

                        ConsultaArticuloDgView dialog = new ConsultaArticuloDgView(articuloServicel, entrada -> {

                            if (!(entrada == null)) {

                                DetalleFacturaDeVenta det1 = new DetalleFacturaDeVenta();

                                det1.setCodigo(entrada.getCodigo());//Colocarlo anull cuando le asignemo el encabezada

                                det1.setArticulo(entrada);

                                det1.setDescripcionArticulo(entrada.getDescripcion());

                                det1.setCantidad(1.00);

                                det1.setPrecioVenta(entrada.getPrecioVenta());

                                det1.setSubTotal(subTotal(1.00, det1.getPrecioVenta()));
                                det1.setPorcientoDescuento(10.00);
                                det1.setPorcientoItbis(18.00);

                                det1.setTotalDescuento(totalDescuento(det1.getSubTotal(), det1.getPorcientoDescuento()));

                                det1.setTotalItbis(totalItbis(det1.getSubTotal(), det1.getTotalDescuento(), det1.getPorcientoItbis()));

                                det1.setTotal(total(det1.getSubTotal(), det1.getTotalDescuento(), det1.getTotalItbis()));
                             
                                det1.setNombreAlmacen("General");
                                det1.setNombreUnidad("Unidad");
                                listDet.add(det1);

                                System.out.println("Articulo ingresó: " + entrada.getDescripcion());
//            
                                grid.getDataProvider().refreshAll(); // Refrescar el Grid sin perder la lista
                            }

                        });

                        dialog.open();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

        grid.setItems(listDet);

        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

        configurarGridDetalle();
        editarDetalle();
        configurarFormulario();

        add(btnNuevo, grid);

    }

    private void configurarFormulario() {

        HorizontalLayout hlDatos = new HorizontalLayout(txtNumDoc, dpFecha, btnGuardar, btnSalir);
        hlDatos.setAlignItems(Alignment.BASELINE);

        FormLayout formLayout = new FormLayout(hlDatos);

        add(formLayout, botones);

    }

    private void guardar() {

        LocalDate localFecha = dpFecha.getValue();
        Date fecha = ClaseUtil.asDate(localFecha);

        try {

            factura = new FacturaDeVenta();
            factura.setFecha(fecha);
            factura.setFechaCreacion(new Date());
            factura.setFechaActualizacion(new Date());
            factura.setNombreUsuario("Administrador");

//            entradaInventario.setTipoDocumento(1);
//            entradaInventario.setSecuenciaDocumento(new SecuenciaDocumento(i));
            listDet.stream().forEach(e -> {

                e.setFactura(factura);
                e.setCodigo(null);
            });

            factura.setDetalleFacturaDeVentaCollection(listDet);
            this.factService.guardar(factura);
            Notification.show("Factura guardada exitosamente", 3000, Notification.Position.TOP_CENTER);

        } catch (Exception e) {
            Notification.show("Error guardando la factura ", 3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }

    }

    private void configurarGridDetalle() {

        grid.setHeight("620px");
        grid.setWidthFull();

        grid.setItems(listDet);

        grid.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo).setHeader("Articulo")
                .setFooter("TOTAL ARTICULOS:");

//        grid.addColumn(DetalleEntradaInventario::getDescripcionArticulo)
//                .setHeader("Descripcion")
//                .setKey("descripcion");
        grid.addColumn(DetalleFacturaDeVenta::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad");

        grid.addColumn(DetalleFacturaDeVenta::getPrecioVenta)
                .setHeader("Precio")
                .setKey("precio");

        grid.addColumn(DetalleFacturaDeVenta::getSubTotal)
                .setHeader("SubTotal")
                .setKey("subTotal");

        grid.addColumn(DetalleFacturaDeVenta::getTotal)
                .setHeader("Total")
                .setKey("total");

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        grid.getColumnByKey("cantidad").setEditorComponent(txtCantidad);

        grid.addColumn(new ComponentRenderer<>(item -> {

            Button deleteButton = new Button("🗑️ Eliminar", click -> {
                listDet.remove(item);
                grid.getDataProvider().refreshAll();
                Notification.show("Articulo eliminado");
            });

            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return deleteButton;
        })).setHeader("Acciones").setAutoWidth(true);

        editarDetalle();
    }

    private void editarDetalle() {

        editor = grid.getEditor();
        editor.setBuffered(true); // sin botón de guardar

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

                    Notification.show("La cantidad no puede ser menor que cero ", 3000, Notification.Position.MIDDLE);
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
                }

            } catch (NumberFormatException ex) {
                Notification.show(" Ingrese un número válido ", 2000, Notification.Position.MIDDLE);

                cantidadField.clear();
            }
        });

    }

    private Double subTotal(Double cant, Double precio) {

        return cant * precio;
    }

    private Double total(Double subTotal, Double totalDesc, Double totalItbis) {

        Double total = (subTotal - totalDesc) + totalItbis;

        System.out.println("Total "+subTotal+" "+totalDesc+ " "+totalItbis);
        return total;
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

}
