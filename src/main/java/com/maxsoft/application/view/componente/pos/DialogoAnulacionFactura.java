package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.util.ClaseUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import java.util.Objects;

/**
 * Diálogo del POS para anular una factura por número, con motivo obligatorio.
 * Solo se cierra si la anulación fue exitosa, para que el cajero pueda corregir
 * el número sin volver a escribir el motivo.
 */
public class DialogoAnulacionFactura extends Dialog {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DialogoAnulacionFactura.class);

    private final FacturaDeVentaService facturaService;
    private final String usuario;

    private final IntegerField txtCodigoFactura = new IntegerField("Número de Factura");
    private final TextArea txtMotivo = new TextArea("Motivo de Anulación");

    /**
     * @param alCerrar se ejecuta al cerrar el diálogo (p. ej. devolver el foco al buscador)
     */
    public DialogoAnulacionFactura(FacturaDeVentaService facturaService, String usuario, Runnable alCerrar) {
        this.facturaService = facturaService;
        this.usuario = usuario;

        setHeaderTitle("Anular Factura / Venta");
        setWidth("420px");

        Span aviso = new Span("La anulación no se puede deshacer. Verifique el número de factura.");
        aviso.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-error-text-color)");

        txtCodigoFactura.setPlaceholder("Ej. 1045");
        txtCodigoFactura.setMin(1);
        txtCodigoFactura.setWidthFull();
        txtCodigoFactura.setAutofocus(true);

        txtMotivo.setPlaceholder("Ingrese la razón de la anulación...");
        txtMotivo.setMaxLength(250);
        txtMotivo.setWidthFull();

        VerticalLayout layout = new VerticalLayout(aviso, txtCodigoFactura, txtMotivo);
        layout.setPadding(false);
        layout.setSpacing(true);

        Button btnAnular = new Button("Confirmar Anulación", VaadinIcon.CLOSE_CIRCLE.create(), e -> confirmar());
        btnAnular.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        // Evita doble anulación por doble clic mientras procesa
        btnAnular.setDisableOnClick(true);
        btnAnular.addClickListener(e -> btnAnular.setEnabled(true));

        Button btnCancelar = new Button("Cancelar [Esc]", e -> close());
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        if (alCerrar != null) {
            addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    alCerrar.run();
                }
            });
        }

        getFooter().add(btnCancelar, btnAnular);
        add(layout);
    }

    private void confirmar() {
        Integer codigoFactura = txtCodigoFactura.getValue();
        String motivo = txtMotivo.getValue();

        if (codigoFactura == null || codigoFactura <= 0) {
            ClaseUtil.mostrarNotificacion("Debe ingresar un número de factura válido.",
                    NotificationVariant.LUMO_WARNING);
            txtCodigoFactura.focus();
            return;
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            ClaseUtil.mostrarNotificacion("Debe especificar el motivo de la anulación.",
                    NotificationVariant.LUMO_WARNING);
            txtMotivo.focus();
            return;
        }

        if (anular(codigoFactura, motivo.trim())) {
            close();
        }
    }

    /**
     * @return true si la factura quedó anulada; false si no existe o el servicio
     * la rechazó (ya anulada, cerrada, etc.).
     */
    private boolean anular(Integer codigoFactura, String motivo) {
        try {
            FacturaDeVenta anulada = facturaService.anularVenta(codigoFactura, motivo, usuario);

            if (anulada == null) {
                ClaseUtil.mostrarNotificacion("No se encontró la factura N° " + codigoFactura,
                        NotificationVariant.LUMO_ERROR);
                return false;
            }

            ClaseUtil.mostrarNotificacion("Factura #" + codigoFactura + " anulada correctamente.",
                    NotificationVariant.LUMO_SUCCESS);
            return true;

        } catch (Exception ex) {
            log.error("Error al anular la factura #{}", codigoFactura, ex);
            // Factura ya anulada, cerrada o cualquier error de negocio
            Notification.show("Error al anular: "
                    + Objects.requireNonNullElse(ex.getMessage(), "error inesperado."),
                    4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
    }
}
