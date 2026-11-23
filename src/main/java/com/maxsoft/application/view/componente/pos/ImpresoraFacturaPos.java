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

                    // Para imprimir, el iframe toma el foco. Al terminar se
                    // devuelve al buscador ($1) para seguir escaneando, salvo
                    // que haya un diálogo abierto.
                    const devolverFoco = () => {
                        const buscador = $1;
                        if (!buscador || !buscador.isConnected) { return; }
                        if (document.querySelector($2)) { return; }
                        window.focus();
                        buscador.focus();
                        if (buscador.inputElement) { buscador.inputElement.select(); }
                    };

                    iframe.onload = function() {
                        setTimeout(() => {
                            iframe.contentWindow.addEventListener('afterprint', devolverFoco, { once: true });
                            iframe.contentWindow.focus();
                            iframe.contentWindow.print();
                            URL.revokeObjectURL(blobUrl);
                            // En modo kiosk print() vuelve enseguida y a veces
                            // no llega "afterprint": se devuelve el foco igual.
                            setTimeout(devolverFoco, 150);
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
        imprimir(facturaCodigo, null);
    }

    /**
     * Imprime la factura y, al terminar la impresión en el navegador, devuelve
     * el foco al {@code buscador} (puede ser null).
     */
    public void imprimir(int facturaCodigo, com.vaadin.flow.dom.Element buscador) {
        try {
            StreamResource pdf = reporteService.generarReporteFacturaVenta(facturaCodigo);
            pdf.setContentType("application/pdf");

            liberar();
            ultimoPdf = VaadinSession.getCurrent().getResourceRegistry().registerResource(pdf);

            // La URL va como parámetro ($0), no concatenada en el script
            UI.getCurrent().getPage().executeJs(SCRIPT_IMPRIMIR, ultimoPdf.getResourceUri().toString(), buscador,
                    ScriptsNavegadorPos.SELECTOR_DIALOGO_ABIERTO);

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
