/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario;

import com.maxsoft.application.view.inventario.articulo.ArticuloView;
import com.maxsoft.application.view.ModuloPrincipal;
import com.maxsoft.application.view.inventario.ajuste.ConsultaAjusteInventarioView;
import com.maxsoft.application.view.inventario.articulo.ArticuloAlmacenView;
import com.maxsoft.application.view.inventario.articulo.GestionEtiquetasView;
import com.maxsoft.application.view.inventario.articulo.ImpresionCodigoBarraView;
import com.maxsoft.application.view.inventario.entrada.EntradaDeIventarioView;
import com.maxsoft.application.view.inventario.merma.MermaView;
import com.maxsoft.application.view.inventario.movimiento.MovimientosInventarioView;
import com.maxsoft.application.view.inventario.salida.SalidaDeIventarioView;
import com.maxsoft.application.view.inventario.traslado.ConsultaTrasladoInventarioView;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.view.inventario.lote.LotesVencimientosView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinSession;
import java.time.LocalDate;
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
@Menu(order = 3, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)

public class ModuloInventario extends AppLayout implements AfterNavigationObserver {

    /** Atributo de sesión con la fecha del último aviso de vencimientos mostrado. */
    private static final String SESION_AVISO_LOTES = "aviso-lotes-fecha";

    private final LoteService loteService;
    private final Button btnAlertaLotes = new Button();

    public ModuloInventario(LoteService loteService) {

        this.loteService = loteService;
        createHeader();
        createDrawer();
    }

    // ============================================================
    // ALERTA DE VENCIMIENTOS
    // ============================================================
    /** Refresca el aviso del encabezado en cada navegación dentro del módulo. */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        actualizarAlertaLotes();
    }

    /**
     * Cuenta los lotes con existencia vencidos y los que vencen en los
     * próximos días. Pinta el aviso del encabezado y, la primera vez del día
     * en la sesión, muestra una notificación.
     */
    private void actualizarAlertaLotes() {
        LocalDate hoy = LocalDate.now();
        long vencidos = 0;
        long porVencer = 0;
        try {
            for (LoteAlmacen la : loteService.getLotesConExistencia()) {
                LotesVencimientosView.Estado e = LotesVencimientosView.estado(la, hoy);
                if (e == LotesVencimientosView.Estado.VENCIDO) {
                    vencidos++;
                } else if (e == LotesVencimientosView.Estado.CRITICO) {
                    porVencer++;
                }
            }
        } catch (RuntimeException ex) {
            btnAlertaLotes.setVisible(false);
            return;
        }

        if (vencidos == 0 && porVencer == 0) {
            btnAlertaLotes.setVisible(false);
            return;
        }
        StringBuilder texto = new StringBuilder();
        if (vencidos > 0) {
            texto.append(vencidos).append(vencidos == 1 ? " vencido" : " vencidos");
        }
        if (porVencer > 0) {
            texto.append(texto.length() > 0 ? " · " : "").append(porVencer).append(" vencen en ")
                    .append(LotesVencimientosView.DIAS_CRITICO).append(" días");
        }
        btnAlertaLotes.setText(texto.toString());
        btnAlertaLotes.setVisible(true);
        btnAlertaLotes.removeThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_CONTRAST);
        btnAlertaLotes.addThemeVariants(vencidos > 0 ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_CONTRAST);

        VaadinSession sesion = VaadinSession.getCurrent();
        if (sesion != null && !hoy.equals(sesion.getAttribute(SESION_AVISO_LOTES))) {
            sesion.setAttribute(SESION_AVISO_LOTES, hoy);
            Notification n = Notification.show("Lotes: " + texto
                    + ". Revise \"Lotes y vencimientos\" (no se pueden vender vencidos sin autorización).",
                    7000, Notification.Position.TOP_END);
            n.addThemeVariants(vencidos > 0 ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_WARNING);
        }
    }

    private void createDrawer() {

        RouterLink linkArticulo = createLink(VaadinIcon.DASHBOARD, "Articulo", ArticuloView.class);
 
        RouterLink linkEntrada = createLink(VaadinIcon.FILE_TABLE, "Entrada ", EntradaDeIventarioView.class);
        RouterLink linkSalida = createLink(VaadinIcon.FILE_TABLE, "Salida ", SalidaDeIventarioView.class);
//        RouterLink linkAjuste = createLink(VaadinIcon.FILE_TABLE, "Ajuste", RegistroAjusteDeInventarioView.class);
        RouterLink linkMovimiento = createLink(VaadinIcon.FILE_TABLE, "Movimiento", MovimientosInventarioView.class);
        RouterLink linkLotes = createLink(VaadinIcon.CALENDAR_CLOCK, "Lotes y vencimientos", LotesVencimientosView.class);
        RouterLink linkTrasladoInventario = createLink(VaadinIcon.FILE_TABLE, "Traslado", ConsultaTrasladoInventarioView.class);
        RouterLink linkTrasladoArticalmacen = createLink(VaadinIcon.FILE_TABLE, "Articulo Almacen", ArticuloAlmacenView.class);
        RouterLink linkGestionEtiquetasView = createLink(VaadinIcon.FILE_TABLE, "Gestion de Etiqueta", GestionEtiquetasView.class);
        RouterLink linkConsultaAjusteInventarioView = createLink(VaadinIcon.FILE_TABLE, "Ajuste", ConsultaAjusteInventarioView.class);
        RouterLink linkMermaView = createLink(VaadinIcon.FILE_TABLE, "Merma", MermaView.class);
        RouterLink linkImpresionCodigoBarraView = createLink(VaadinIcon.FILE_TABLE, "Etiqueta", ImpresionCodigoBarraView.class);

        RouterLink linkModulo = createLink(VaadinIcon.EXIT, "Salir", ModuloPrincipal.class);
        ////
//        // Layout vertical que contiene los enlaces del menú
        VerticalLayout menuLayout = new VerticalLayout(
                linkArticulo,

                linkEntrada,
                //                linkConsultaEntradaInventarioView,
                linkSalida,
                //                linkConsultaSalidaInventarioView,
                //                linkAjuste,
                linkConsultaAjusteInventarioView,
                linkTrasladoInventario,
                linkMermaView,
                linkMovimiento,
                linkLotes,
                linkTrasladoArticalmacen,
                linkGestionEtiquetasView,
                linkImpresionCodigoBarraView,
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
        H3 logo = new H3("Inventario");
        logo.getStyle().set("margin", "1");

        // Espaciador flexible para empujar elementos a los extremos
        Span spacer = new Span();
        spacer.getStyle().set("flex", "1");

        // Icono de usuario (por ejemplo, para perfil o logout)
        Icon userIcon = VaadinIcon.USER.create();
        userIcon.getStyle().set("cursor", "pointer");

        // Layout horizontal que agrupa los componentes del header
        // Aviso de lotes vencidos / por vencer (se llena en afterNavigation)
        btnAlertaLotes.setIcon(VaadinIcon.CALENDAR_CLOCK.create());
        btnAlertaLotes.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        btnAlertaLotes.setTooltipText("Ver lotes vencidos y por vencer");
        btnAlertaLotes.setVisible(false);
        btnAlertaLotes.addClickListener(e -> UI.getCurrent().navigate(LotesVencimientosView.class,
                QueryParameters.simple(java.util.Map.of("estado", "POR_ATENDER"))));

        HorizontalLayout header = new HorizontalLayout(menuIcon, logo, spacer, btnAlertaLotes, userIcon);
        header.setWidth("100%");
        header.setAlignItems(Alignment.CENTER);
        header.addClassName("header");

        addToNavbar(header);

    }

    private void toggleDrawer() {
        setDrawerOpened(!isDrawerOpened());
    }

}
