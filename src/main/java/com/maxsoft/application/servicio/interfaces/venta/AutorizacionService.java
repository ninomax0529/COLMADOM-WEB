package com.maxsoft.application.servicio.interfaces.venta;

/**
 * Autorizaciones de supervisor en caja (p. ej. vender de un lote vencido).
 */
public interface AutorizacionService {

    /** tipo_usuario de los usuarios que pueden autorizar (administrador). */
    String TIPO_SUPERVISOR = "1";

    /**
     * Valida usuario y clave de un supervisor (usuario habilitado y de tipo
     * administrador).
     *
     * @return el nombre del supervisor, para dejarlo registrado
     * @throws IllegalArgumentException usuario o clave incorrectos, o el
     * usuario no puede autorizar
     */
    String autorizarSupervisor(String usuario, String clave);
}
