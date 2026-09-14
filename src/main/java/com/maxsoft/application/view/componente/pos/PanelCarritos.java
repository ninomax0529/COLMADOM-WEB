
package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
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

public class PanelCarritos extends VerticalLayout {

    @FunctionalInterface
    public interface AccionCambioSeleccionTicket {
        void ejecutar(TicketVenta ticketSeleccionado);
    }

    public PanelCarritos(
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

        // =========================================================
        // ENCABEZADO
        // =========================================================

        HorizontalLayout headerLayout = new HorizontalLayout();

        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(
                FlexComponent.JustifyContentMode.BETWEEN
        );
        headerLayout.setAlignItems(
                FlexComponent.Alignment.CENTER
        );

        H3 cartTitle = new H3("Ventas Abiertas");

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

        HorizontalLayout acciones = new HorizontalLayout(
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

        headerLayout.add(
                cartTitle,
                acciones
        );

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
}