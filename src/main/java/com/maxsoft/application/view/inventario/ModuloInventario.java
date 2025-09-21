/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario;

import com.maxsoft.application.view.inventario.articulo.ArticuloView;
import com.maxsoft.application.view.ModuloPrincipal;
import com.maxsoft.application.view.inventario.entrada.EntradaDeIventarioView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 *
 * @author maximilianoalmonte
 */
@PageTitle("Inventario")
@Route("mdlInv")
//@JsModule("./gestion-colmado/themes/views/inventario-view.css")
@Layout(value = "inventario")
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)

public class ModuloInventario extends AppLayout {

    public ModuloInventario() {
    
        createHeader();
        createDrawer();
    }

    private void createDrawer() {

        RouterLink linkArticulo = createLink(VaadinIcon.DASHBOARD, "Articulo", ArticuloView.class);
        RouterLink linkEntrada = createLink(VaadinIcon.FILE_TABLE, "Entrada Inventario ", EntradaDeIventarioView.class);
        
        
//          RouterLink linkConsArtInv1 = createLink(VaadinIcon.FILE_TABLE, "Filtro Inventario ", GridColumnFiltering.class);

        RouterLink linkModulo = createLink(VaadinIcon.EXIT, "Salir", ModuloPrincipal.class);
////
//        // Layout vertical que contiene los enlaces del menú
        VerticalLayout menuLayout = new VerticalLayout(linkArticulo, linkEntrada,linkModulo);

        menuLayout.setPadding(false);
        menuLayout.setSpacing(false);
        menuLayout.setSizeFull();
        menuLayout.addClassName("nav-menu");

        addToDrawer(menuLayout);

    }

    // Método auxiliar para crear un RouterLink con icono y texto
    private RouterLink createLink(VaadinIcon icon, String text, Class<? extends Component> navigationTarget) {
        Icon linkIcon = icon.create();
        linkIcon.getStyle().set("marginRight", "10px");
        RouterLink link = new RouterLink();
        link.add(linkIcon, new Span(text));
        link.setRoute(navigationTarget);
//        link.setHighlightCondition(HighlightCondition.sameLocation());
        return link;
    }

    private void createHeader() {

        // Botón para mostrar/ocultar el menú lateral
        Icon menuIcon = VaadinIcon.MENU.create();
        menuIcon.getStyle().set("cursor", "pointer");
        menuIcon.addClickListener(e -> toggleDrawer());

        // Título o logo de la aplicación
        H3 logo = new H3("Inventario");
        logo.getStyle().set("margin", "1");

        // Espaciador flexible para empujar elementos a los extremos
        Span spacer = new Span();
        spacer.getStyle().set("flex", "1");

        // Icono de usuario (por ejemplo, para perfil o logout)
        Icon userIcon = VaadinIcon.USER.create();
        userIcon.getStyle().set("cursor", "pointer");

        // Layout horizontal que agrupa los componentes del header
        HorizontalLayout header = new HorizontalLayout(menuIcon, logo, spacer, userIcon);
        header.setWidth("100%");
        header.setAlignItems(Alignment.CENTER);
        header.addClassName("header");

        addToNavbar(header);

    }

    private void toggleDrawer() {
        setDrawerOpened(!isDrawerOpened());
    }

}
