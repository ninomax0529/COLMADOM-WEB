/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario.salida;

/**
 *
 * @author maximilianoalmonte
 */

import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.servicio.interfaces.SalidaInventarioService;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import jakarta.annotation.security.PermitAll;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;

@PermitAll
@PageTitle("Salida de Inventario")
@Route(value = "inventario/salida")

public class SalidaDeIventarioView extends VerticalLayout {

    
    @Autowired
    DataSource dataSource;
    private final SalidaInventarioService salidaService;
    private final Grid<SalidaInventario> grid = new Grid<>(SalidaInventario.class, false);
    private Grid<DetalleSalidaInventario> gridDetalle = new Grid<>(DetalleSalidaInventario.class, false);
    ToolBarBotonera botonera = new ToolBarBotonera(true, false, false);

    List<DetalleSalidaInventario> listDet;

    private TextField nombre = new TextField("Nombre");
    private NumberField precio = new NumberField("Precio");
    private NumberField stock = new NumberField("Stock");

    private Button guardar = new Button("Guardar");
    private Button cancelar = new Button("Cancelar");
    private Button nuevo = new Button("Nuevo");

    List<SalidaInventario> listaEntInv;

    @Autowired
    public SalidaDeIventarioView(SalidaInventarioService salidaServiceArg) {

        setSizeFull();
        setSpacing(false);

        this.salidaService = salidaServiceArg;
        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();

        tabs.add("Salida de Inventario", grid);
        tabs.add("Detalle", gridDetalle);

        botonera.getNuevo().addClickListener(e -> {
            // lógica de nuevo

            UI.getCurrent().navigate(RegistroSalidaDeIventarioView.class);

        });

        grid.addItemDoubleClickListener(c -> {

            listDet = this.salidaService.getDetalle(c.getItem().getCodigo());
            gridDetalle.setItems(listDet);
            tabs.setSelectedIndex(1);
            tabs.getTabAt(1).setLabel("Detalle( Salida -> " + c.getItem().getCodigo() + " )");

        });

        configurarGrid();
        configurarGridDetalle();

        add(botonera, tabs);

    }

    private void configurarGrid() {

        listaEntInv = this.salidaService.getLista(false);

        grid.addColumn(SalidaInventario::getCodigo).setHeader("Codigo")
                .setFooter("TOTAL SALIDA:");

        grid.addColumn(SalidaInventario::getFecha)
                .setHeader("Fecha")
                .setKey("fecha");

        grid.addColumn(SalidaInventario::getFechaRegistro)
                .setHeader("Fecha Registro")
                .setKey("fechaRegistro");

        grid.addColumn(SalidaInventario::getUsuario)
                .setHeader("Usuario")
                .setKey("usuario");

        grid.setItems(listaEntInv);
        grid.setSizeFull();
        
        grid.addComponentColumn(documento -> {

            HorizontalLayout acciones = new HorizontalLayout();

            Button verBtn = new Button(VaadinIcon.EYE.create(), event -> {

//                 Aquí puedes abrir un diálogo o navegar
                try (Connection conn = dataSource.getConnection()) {

//                    RptPlantilla rptP = new RptPlantilla();
//                    if (rptP != null) {
//
////                        StreamResource pdfResource = rptP.verProgramacion(documento.getCodigo(), conn);
//
//                        Anchor anchor = new Anchor(pdfResource, "");
//                        anchor.getElement().setAttribute("download", false);
//                        anchor.getElement().setAttribute("target", "_blank");
//
//                        anchor.getElement().callJsFunction("click"); // dispara la apertura automática
//
//                        add(anchor);
//                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Notification.show("Error al generar el reporte", 3000, Notification.Position.TOP_CENTER);
                }

            });

            Button btnAnular = new Button("Anular ", event -> {

//                ConfirmDialog dialog = new ConfirmDialog(
//                        "¿Seguro que quiere anular el regsitro -> "+documento.getCodigo(),
//                        () -> {
//
//                            documento.setAnulada(true);
//                            salidaService.guardar(documento);
//                            grid.setItems(salidaService.getLista(false));
//                            grid.getDataProvider().refreshAll();
//
//                            Notification.show("Registro anulado exitosamente", 3000, Notification.Position.TOP_CENTER);
//
//                        },
//                        () -> {
//
//                        }
//                );

//                dialog.open();

            });

            verBtn.getElement().setAttribute("title", "Ver");

      

            acciones.setSizeFull();
//            acciones.setSpacing("2px");

            acciones.add(btnAnular);

            return acciones;
        }).setHeader("Acciones").setAutoWidth(true);

    }

    private void configurarGridDetalle() {

        gridDetalle.setHeight("620px");
        gridDetalle.setWidthFull();
//
//        gridDetalle.addColumn(
//                DetalleEntradaInventario::getArticulo).setHeader("Articulo")
//                .setFooter("TOTAL ARTICULOS:");

        gridDetalle.addColumn(DetalleSalidaInventario::getDescripcionArticulo)
                .setHeader("Descripcion")
                .setKey("descripcion")
                .setFooter("TOTAL:");

        gridDetalle.addColumn(DetalleSalidaInventario::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad");

//        gridDetalle.addColumn(DetalleEntradaInventario::getNuev)
//                .setHeader("Existencia")
//                .setKey("existencia");
//        gridDetalle.addColumn(DetalleEntradaInventario::getExistenciaActual)
//                .setHeader("Nueva Existencia")
//                .setKey("nuevaexistencia");
        gridDetalle.addColumn(DetalleSalidaInventario::getUnidad)
                .setHeader("Unidad")
                .setKey("unidad");

        gridDetalle.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

    }

}
