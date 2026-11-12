/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.delivery;

import com.maxsoft.application.view.delivery.liquidarfactura.MonitorDeliveryView;
import com.maxsoft.application.view.ModuloPrincipal;
import com.maxsoft.application.view.delivery.gestiondelivey.DeliveryView;
import com.maxsoft.application.view.delivery.liquidarfactura.ConsultaDeliveryRealizadoView;
import com.maxsoft.application.view.delivery.liquidarfactura.ConsultaLiquidacionDeliveryView;
import com.maxsoft.application.view.delivery.liquidarfactura.CuentaMotoristasView;
import com.maxsoft.application.view.delivery.liquidarfactura.LiquidacionDeliveryView;
import com.maxsoft.application.view.delivery.vehiculo.VehiculoView;
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
@PageTitle("Delivery")
@Route("mdlDelivery")
@Layout(value = "delivery")
@Menu(order = 8, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class ModuloDelivery extends AppLayout {

    public ModuloDelivery() {
        createHeader();
        createDrawer();
    }

    private void createDrawer() {

        RouterLink linkLiquidar = createLink(VaadinIcon.DASHBOARD, "Liquidación de Delivery", LiquidacionDeliveryView.class);
        RouterLink linkConsultaLiquidaciones = createLink(VaadinIcon.WALLET, "Consulta de liquidaciones", ConsultaLiquidacionDeliveryView.class);
        RouterLink linkCuentaMotoristas = createLink(VaadinIcon.USER_CARD, "Cuenta de motoristas", CuentaMotoristasView.class);
        RouterLink linkConsultaRealizadoView = createLink(VaadinIcon.FILE_TABLE, "Consulta factura delivery", ConsultaDeliveryRealizadoView.class);
        RouterLink linkMonitorDeliveryView = createLink(VaadinIcon.FILE_TABLE, "Monitor de Deliveries", MonitorDeliveryView.class);
        RouterLink linkDeliveryView = createLink(VaadinIcon.FILE_TABLE, "Gestion Delivery", DeliveryView.class);
        RouterLink linkVehiculoView = createLink(VaadinIcon.FILE_TABLE, "Gestion Vehiculo", VehiculoView.class);

        RouterLink linkModulo = createLink(VaadinIcon.EXIT, "Salir", ModuloPrincipal.class);
        ////
//        // Layout vertical que contiene los enlaces del menú
        VerticalLayout menuLayout = new VerticalLayout(
                linkLiquidar,
                linkConsultaLiquidaciones,
                linkCuentaMotoristas,
                linkConsultaRealizadoView,
                linkMonitorDeliveryView,
                linkDeliveryView,
                linkVehiculoView,
                linkModulo
        );

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
        H3 logo = new H3("Venta");
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
