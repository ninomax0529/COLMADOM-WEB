//
//package com.maxsoft.application.view.componente.pos;
//
//import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
//import com.vaadin.flow.component.Key;
//import com.vaadin.flow.component.KeyModifier;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.html.H3;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.orderedlayout.FlexComponent;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.tabs.TabSheet;
//
//import java.util.List;
//
//public class PanelCarritos extends VerticalLayout {
//
//    @FunctionalInterface
//    public interface AccionCambioSeleccionTicket {
//        void ejecutar(TicketVenta ticketSeleccionado);
//    }
//
//    public PanelCarritos(
//            TabSheet ticketTabSheet,
//            List<TicketVenta> ticketsAbiertos,
//            Runnable onCrearNuevoTicket,
//            Runnable onCambiarNombre,
//            AccionCambioSeleccionTicket onSeleccionarTicket
//    ) {
//
//        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
//        getStyle().set("padding", "1rem");
//        getStyle().set("border-radius", "8px");
//
//        setHeightFull();
//
//        // =========================================================
//        // ENCABEZADO
//        // =========================================================
//
//        HorizontalLayout headerLayout = new HorizontalLayout();
//
//        headerLayout.setWidthFull();
//        headerLayout.setJustifyContentMode(
//                FlexComponent.JustifyContentMode.BETWEEN
//        );
//        headerLayout.setAlignItems(
//                FlexComponent.Alignment.CENTER
//        );
//
//        H3 cartTitle = new H3("Ventas Abiertas");
//
//        // =========================================================
//        // BOTÓN CAMBIAR NOMBRE
//        // =========================================================
//
//        Button renameTicketBtn = new Button(
//                "Cambiar Nombre [Alt+R]",
//                VaadinIcon.EDIT.create()
//        );
//
//        renameTicketBtn.addThemeVariants(
//                ButtonVariant.LUMO_TERTIARY,
//                ButtonVariant.LUMO_SMALL
//        );
//
//        renameTicketBtn.addClickListener(e -> {
//
//            if (onCambiarNombre != null) {
//                onCambiarNombre.run();
//            }
//
//        });
//
//        // =========================================================
//        // BOTÓN NUEVA VENTA
//        // =========================================================
//
//        Button addTicketBtn = new Button(
//                "Nueva Venta (+) [Alt+N]",
//                VaadinIcon.PLUS.create()
//        );
//
//        addTicketBtn.addThemeVariants(
//                ButtonVariant.LUMO_PRIMARY,
//                ButtonVariant.LUMO_SMALL
//        );
//
//        addTicketBtn.addClickListener(e -> {
//
//            if (onCrearNuevoTicket != null) {
//                onCrearNuevoTicket.run();
//            }
//
//        });
//
//        addTicketBtn.addClickShortcut(
//                Key.KEY_N,
//                KeyModifier.ALT
//        );
//
//        // =========================================================
//        // AGRUPAR BOTONES
//        // =========================================================
//
//        HorizontalLayout acciones = new HorizontalLayout(
//                renameTicketBtn,
//                addTicketBtn
//        );
//
//        acciones.setSpacing(true);
//        acciones.setPadding(false);
//        acciones.setAlignItems(
//                FlexComponent.Alignment.CENTER
//        );
//
//        // =========================================================
//        // HEADER
//        // =========================================================
//
//        headerLayout.add(
//                cartTitle,
//                acciones
//        );
//
//        // =========================================================
//        // TABS
//        // =========================================================
//
//        ticketTabSheet.setWidthFull();
//        ticketTabSheet.setHeightFull();
//
//        ticketTabSheet.getStyle().set(
//                "display",
//                "flex"
//        );
//
//        ticketTabSheet.getStyle().set(
//                "flex-direction",
//                "column"
//        );
//
//        ticketTabSheet.addSelectedChangeListener(event -> {
//
//            int index = ticketTabSheet.getSelectedIndex();
//
//            if (index >= 0 && index < ticketsAbiertos.size()) {
//
//                TicketVenta ticketSeleccionado =
//                        ticketsAbiertos.get(index);
//
//                if (onSeleccionarTicket != null) {
//
//                    onSeleccionarTicket.ejecutar(
//                            ticketSeleccionado
//                    );
//                }
//            }
//        });
//
//        add(
//                headerLayout,
//                ticketTabSheet
//        );
//
//        setFlexGrow(
//                1,
//                ticketTabSheet
//        );
//    }
//}


package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import java.util.List;

public class PanelCarrito extends VerticalLayout {

    /**
     * El título "Ventas Abiertas" repite lo que ya dicen las pestañas
     * ("Venta 1", "Venta 2"...). Ocultarlo libera una fila para el ticket.
     * Poner en true para volver a mostrarlo.
     */
    private static final boolean MOSTRAR_TITULO = false;

    private final HorizontalLayout acciones;

    @FunctionalInterface
    public interface AccionCambioSeleccionTicket {
        void ejecutar(TicketVenta ticketSeleccionado);
    }

    public PanelCarrito(
            TabSheet ticketTabSheet,
            List<TicketVenta> ticketsAbiertos,
            Runnable onCrearNuevoTicket,
            Runnable onCambiarNombre,
            AccionCambioSeleccionTicket onSeleccionarTicket
    ) {

        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        getStyle().set("padding", "1rem");
        getStyle().set("border-radius", "8px");

        setHeightFull();

        // Permite que el contenido interno (grid del ticket) haga scroll en
        // lugar de desbordar la pantalla.
        getStyle().set("min-height", "0");

        // =========================================================
        // ENCABEZADO
        // =========================================================

        HorizontalLayout headerLayout = new HorizontalLayout();

        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(
                MOSTRAR_TITULO
                        ? FlexComponent.JustifyContentMode.BETWEEN
                        : FlexComponent.JustifyContentMode.END
        );
        headerLayout.setAlignItems(
                FlexComponent.Alignment.CENTER
        );

        // =========================================================
        // BOTÓN CAMBIAR NOMBRE
        // =========================================================

        Button renameTicketBtn = new Button(
                "Cambiar Nombre [Alt+R]",
                VaadinIcon.EDIT.create()
        );

        renameTicketBtn.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL
        );

        renameTicketBtn.setTooltipText("Renombrar la venta activa");

        renameTicketBtn.addClickListener(e -> {

            if (onCambiarNombre != null) {
                onCambiarNombre.run();
            }

        });

        // =========================================================
        // BOTÓN NUEVA VENTA
        // =========================================================

        Button addTicketBtn = new Button(
                "Nueva Venta (+) [Alt+N]",
                VaadinIcon.PLUS.create()
        );

        addTicketBtn.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_SMALL
        );

        addTicketBtn.setTooltipText("Abrir otra venta en paralelo");

        addTicketBtn.addClickListener(e -> {

            if (onCrearNuevoTicket != null) {
                onCrearNuevoTicket.run();
            }

        });

        addTicketBtn.addClickShortcut(
                Key.KEY_N,
                KeyModifier.ALT
        );

        // =========================================================
        // AGRUPAR BOTONES
        // =========================================================

        acciones = new HorizontalLayout(
                renameTicketBtn,
                addTicketBtn
        );

        acciones.setSpacing(true);
        acciones.setPadding(false);
        acciones.setAlignItems(
                FlexComponent.Alignment.CENTER
        );

        // =========================================================
        // HEADER
        // =========================================================

        if (MOSTRAR_TITULO) {
            headerLayout.add(new H3("Ventas Abiertas"));
        }

        headerLayout.add(acciones);

        // =========================================================
        // TABS
        // =========================================================

        ticketTabSheet.setWidthFull();
        ticketTabSheet.setHeightFull();

        ticketTabSheet.getStyle().set(
                "display",
                "flex"
        );

        ticketTabSheet.getStyle().set(
                "flex-direction",
                "column"
        );

        ticketTabSheet.getStyle().set(
                "min-height",
                "0"
        );

        ticketTabSheet.addSelectedChangeListener(event -> {

            int index = ticketTabSheet.getSelectedIndex();

            if (index >= 0 && index < ticketsAbiertos.size()) {

                TicketVenta ticketSeleccionado =
                        ticketsAbiertos.get(index);

                if (onSeleccionarTicket != null) {

                    onSeleccionarTicket.ejecutar(
                            ticketSeleccionado
                    );
                }
            }
        });

        add(
                headerLayout,
                ticketTabSheet
        );

        setFlexGrow(
                1,
                ticketTabSheet
        );
    }

    /**
     * Punto de extensión: agrega un componente (por ejemplo el botón "Anular
     * factura") al inicio de la barra de acciones del encabezado, junto a
     * "Cambiar Nombre" y "Nueva Venta". Evita una fila aparte encima del panel.
     */
    public void agregarAccion(Component accion) {

        if (accion != null) {
            acciones.addComponentAsFirst(accion);
        }
    }
}
