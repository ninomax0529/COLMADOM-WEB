package com.maxsoft.application.util;

import com.vaadin.flow.component.UI;

/**
 * Imprime un documento HTML (tickets de 80 mm) con la impresora del navegador,
 * mediante un iframe oculto, igual que los tickets de venta.
 */
public final class ImpresionHtmlUtil {

    private ImpresionHtmlUtil() {
    }

    private static final String SCRIPT = """
            let f = document.getElementById('comprobante-caja-frame');
            if (!f) {
                f = document.createElement('iframe');
                f.id = 'comprobante-caja-frame';
                f.style.position = 'fixed'; f.style.right = '0'; f.style.bottom = '0';
                f.style.width = '0'; f.style.height = '0'; f.style.border = '0';
                document.body.appendChild(f);
            }
            f.onload = function() {
                setTimeout(() => { f.contentWindow.focus(); f.contentWindow.print(); }, 200);
            };
            f.srcdoc = $0;
            """;

    public static void imprimir(UI ui, String html) {
        UI destino = ui != null ? ui : UI.getCurrent();
        if (destino == null) {
            throw new IllegalStateException("No hay una pantalla activa para imprimir.");
        }
        destino.getPage().executeJs(SCRIPT, html);
    }
}
