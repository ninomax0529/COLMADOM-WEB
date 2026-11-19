package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.servicio.interfaces.reporte.ReporteService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

/**
 * Imprime la factura del POS en la impresora de caja: genera el PDF, lo registra
 * en la sesión y lo manda a imprimir desde un iframe oculto (modo kiosk).
 * Una instancia por vista: guarda el último PDF para liberarlo al imprimir el
 * siguiente o al salir del POS.
 */
public class ImpresoraFacturaPos {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImpresoraFacturaPos.class);

    private static final String SCRIPT_IMPRIMIR = """
            fetch($0)
                .then(response => {
                    if (!response.ok) { throw new Error('HTTP ' + response.status); }
                    return response.blob();
                })
                .then(blob => {
                    const blobUrl = URL.createObjectURL(blob);

                    let iframe = document.getElementById('kiosk-print-frame');

                    if (!iframe) {
                        iframe = document.createElement('iframe');
                        iframe.id = 'kiosk-print-frame';
                        iframe.style.position = 'fixed';
                        iframe.style.right = '0';
                        iframe.style.bottom = '0';
                        iframe.style.width = '0';
                        iframe.style.height = '0';
                        iframe.style.border = '0';

                        document.body.appendChild(iframe);
                    }

                    iframe.src = blobUrl;

                    iframe.onload = function() {
                        setTimeout(() => {
                            iframe.contentWindow.focus();
                            iframe.contentWindow.print();
                            URL.revokeObjectURL(blobUrl);
                        }, 300);
                    };
                })
                .catch(err => console.error('Error al imprimir en modo kiosk:', err));
            """;

    private final ReporteService reporteService;

    /** PDF de la última factura impresa; se libera al imprimir la siguiente. */
    private StreamRegistration ultimoPdf;

    public ImpresoraFacturaPos(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /**
     * Imprime la factura. Si falla, avisa que la factura sí quedó guardada para
     * reimprimirla desde la consulta de facturas.
     */
    public void imprimir(int facturaCodigo) {
        try {
            StreamResource pdf = reporteService.generarReporteFacturaVenta(facturaCodigo);
            pdf.setContentType("application/pdf");

            liberar();
            ultimoPdf = VaadinSession.getCurrent().getResourceRegistry().registerResource(pdf);

            // La URL va como parámetro ($0), no concatenada en el script
            UI.getCurrent().getPage().executeJs(SCRIPT_IMPRIMIR, ultimoPdf.getResourceUri().toString());

            Notification.show("Enviando a la impresora de caja...", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception ex) {
            log.error("Error al imprimir la factura #{}", facturaCodigo, ex);
            Notification.show("La factura #" + facturaCodigo
                    + " se guardó, pero hubo un error al generar el documento.",
                    5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /** Libera el PDF registrado en la sesión (llamar al salir del POS). */
    public void liberar() {
        if (ultimoPdf != null) {
            ultimoPdf.unregister();
            ultimoPdf = null;
        }
    }
}
