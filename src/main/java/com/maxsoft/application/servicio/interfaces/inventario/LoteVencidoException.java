package com.maxsoft.application.servicio.interfaces.inventario;

import java.util.List;

/**
 * La venta toma mercancía de lotes vencidos y no trae autorización de un
 * supervisor. El POS la captura, pide la autorización y vuelve a intentar.
 */
public class LoteVencidoException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final List<String> lineas;

    public LoteVencidoException(List<String> lineas) {
        super("Hay artículos que solo tienen existencia en lotes vencidos:\n" + String.join("\n", lineas)
                + "\nSe requiere la autorización de un supervisor.");
        this.lineas = List.copyOf(lineas);
    }

    /** Una línea por artículo afectado, lista para mostrar. */
    public List<String> getLineas() {
        return lineas;
    }
}
