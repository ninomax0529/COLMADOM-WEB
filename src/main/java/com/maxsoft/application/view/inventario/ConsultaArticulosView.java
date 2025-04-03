/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario;

/**
 *
 * @author Maximiliano
 */
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;


@Route("InventarioDeProductos")
@Menu(order = 7, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)

public class ConsultaArticulosView extends VerticalLayout {

    public ConsultaArticulosView(ArticuloService articuloService) {
        Button abrirDialogo = new Button("Consultar Artículos", event -> {
//            ConsultaArticulosDialog dialog = new ConsultaArticulosDialog();
//            dialog.open();
        });

        add(abrirDialogo);
    }
}
