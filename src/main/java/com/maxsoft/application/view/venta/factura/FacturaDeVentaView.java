/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.factura;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.FacturaDeVentaService;
import com.maxsoft.application.view.componente.FiltroAvanzado;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.tabs.TabSheet;
import java.util.List;

@PageTitle("Factura de Venta")
@Route(value = "venta/FacturaVenta")

public class FacturaDeVentaView extends VerticalLayout {

    private final FacturaDeVentaService facturaService;
    private final Grid<FacturaDeVenta> grid = new Grid<>(FacturaDeVenta.class, false);
    private Grid<DetalleFacturaDeVenta> gridDetalle = new Grid<>(DetalleFacturaDeVenta.class, false);
    
//    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    List<DetalleFacturaDeVenta> listDet;

    List<FacturaDeVenta> listaFactura;

    @Autowired
    public FacturaDeVentaView(FacturaDeVentaService entradaServiceArg) {

        setSizeFull();
        setSpacing(false);

        this.facturaService = entradaServiceArg;
        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        
//         GridListDataView<FacturaDeVenta> dataView = grid.setItems(facturaService.getLista());
//// Crear el filtro avanzado
//        FiltroAvanzado<FacturaDeVenta> filtro = new FiltroAvanzado<>(dataView);
                
// Agregar filtros por nombre y cédula
//        filtro.addFiltro("Codigo", FacturaDeVenta::getCodigo);
//        filtro.addFiltro("Fecha", FacturaDeVenta::getFecha);

        tabs.add("Factura", grid);
        tabs.add("Detalle", gridDetalle);
//
//        botonera.getNuevo().addClickListener(e -> {
//            // lógica de nuevo
//
//          UI.getCurrent().navigate(RegistroEntradaDeIventarioView.class);
//
//        });


        grid.addItemDoubleClickListener(c -> {

            listDet = this.facturaService.getDetalle(c.getItem().getCodigo());
            gridDetalle.setItems(listDet);
            tabs.setSelectedIndex(1);
            tabs.getTabAt(1).setLabel("Detalle( Factura -> " + c.getItem().getCodigo() + " )");

        });

        configurarGrid();
        configurarGridDetalle();

        add(tabs);

    }

    private void configurarGrid() {

        listaFactura = this.facturaService.getLista();

        grid.addColumn(FacturaDeVenta::getCodigo).setHeader("Codigo")
                .setFooter("TOTAL ENTRADA:");

        grid.addColumn(FacturaDeVenta::getFecha)
                .setHeader("Fecha")
                .setKey("fecha");

        grid.addColumn(FacturaDeVenta::getFechaCreacion)
                .setHeader("Fecha Creacion")
                .setKey("fechaCreacion");

        grid.addColumn(FacturaDeVenta::getNombreUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        grid.setItems(listaFactura);
        grid.setSizeFull();

    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();

//        gridDetalle.addColumn(DetalleFacturaDeVenta::getCodigo).setHeader("Codigo")
//                .setFooter("TOTAL ARTICULOS:");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo)
                .setHeader("Descripcion")
                .setKey("descripcion")
                 .setFooter("TOTAL:");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getNuevaExistencia)
                .setHeader("Existencia")
                .setKey("existencia");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getExistenciaActual)
                .setHeader("Nueva Existencia")
                .setKey("nuevaexistencia");

        gridDetalle.addColumn(DetalleFacturaDeVenta::getNombreUnidad)
                .setHeader("Unidad")
                .setKey("unidad");

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

    }

}
