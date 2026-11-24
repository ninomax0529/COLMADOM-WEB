package com.maxsoft.application.view.componente.pos;

import com.vaadin.flow.dom.Element;

/**
 * JavaScript del POS que se resuelve en el navegador: atajos Alt+tecla, foco del
 * buscador y "clic en zona vacía devuelve el foco al buscador".
 *
 * <p>Los atajos llaman a métodos {@code @ClientCallable} de la vista:
 * atajoNuevaVenta, atajoRenombrarTicket, atajoCancelarVenta, atajoAnularFactura,
 * atajoReimprimir y atajoSeleccionarTicket(int).</p>
 */
public final class ScriptsNavegadorPos {

    /**
     * Selector de los diálogos abiertos en el navegador. Con uno abierto, el
     * foco y los atajos le pertenecen al diálogo.
     */
    public static final String SELECTOR_DIALOGO_ABIERTO
            = "vaadin-dialog-overlay[opened], vaadin-confirm-dialog-overlay[opened]";

    private ScriptsNavegadorPos() {
    }

    /**
     * Atajos Alt+tecla de la pantalla de venta:
     * <ul>
     * <li>Alt+N: nueva venta</li>
     * <li>Alt+R: cambiar nombre de la venta activa</li>
     * <li>Alt+C: cancelar la venta activa</li>
     * <li>Alt+X: anular factura</li>
     * <li>Alt+P: reimprimir la última factura</li>
     * <li>Alt+1 ... Alt+9: cambiar de venta</li>
     * </ul>
     *
     * También cancela la acción del navegador para F2, F3, F4 y F6 (F3 abría
     * el "Buscar" del navegador), sin detener el evento: los atajos de Vaadin
     * de esas teclas se siguen disparando.
     *
     * Se detectan por la tecla FÍSICA (event.code) y en fase de captura, antes
     * que cualquier componente. Con el atajo de Vaadin, si el foco estaba en el
     * buscador, Alt+letra podía llegar como otro carácter (según el teclado o
     * el sistema) y entonces el atajo no se disparaba y la letra se escribía en
     * el buscador. Aquí se cancela el evento, así que la letra nunca se
     * escribe. Se instala una sola vez por vista y se limpia al salir.
     */
    public static void instalarAtajosAlt(Element vista) {
        vista.executeJs(
                """
                const vista = $0;
                if (vista.__atajosAltInstalados) { return; }
                vista.__atajosAltInstalados = true;

                // Mientras se mantiene Alt después de un atajo (y un momento
                // después de soltarlo) se bloquea cualquier carácter: en el
                // teclado español AltGr+2 escribe "@", y Windows puede insertar
                // un carácter por código Alt al soltar la tecla.
                let bloquearTexto = false;
                let temporizador = null;

                const limpiar = () => {
                    document.removeEventListener('keydown', manejador, true);
                    document.removeEventListener('keyup', alSoltar, true);
                    document.removeEventListener('keypress', bloquear, true);
                    document.removeEventListener('beforeinput', bloquear, true);
                    vista.__atajosAltInstalados = false;
                };

                const bloquear = e => {
                    if (bloquearTexto) {
                        e.preventDefault();
                        e.stopPropagation();
                    }
                };

                const alSoltar = e => {
                    if (bloquearTexto && (e.key === 'Alt' || e.key === 'AltGraph')) {
                        clearTimeout(temporizador);
                        temporizador = setTimeout(() => { bloquearTexto = false; }, 300);
                    }
                };

                const manejador = e => {
                    // Si la vista ya no está en pantalla, se limpia el atajo
                    if (!vista.isConnected) { limpiar(); return; }

                    // F2 (cobrar), F3 (fiar), F4 (buscador) y F6 (delivery) son
                    // del POS: se cancela la acción del navegador (F3 abre
                    // "Buscar" en la página, F6 va a la barra de direcciones)
                    // pero el evento sigue su camino, así que los atajos de
                    // Vaadin se disparan igual.
                    if (['F2', 'F3', 'F4', 'F6'].includes(e.key) && !e.altKey && !e.ctrlKey && !e.metaKey) {
                        e.preventDefault();
                        return;
                    }

                    // AltGr cuenta como Alt (en algunos navegadores llega sin altKey)
                    const altGr = e.getModifierState && e.getModifierState('AltGraph');
                    if (!(e.altKey || altGr)) {
                        // Una tecla normal (sin Alt) vuelve a permitir escribir, por si
                        // nunca llegó el keyup de Alt (p. ej. la ventana perdió el foco)
                        if (!['Alt', 'AltGraph', 'Control'].includes(e.key)) { bloquearTexto = false; }
                        return;
                    }
                    if (e.metaKey || e.shiftKey) { return; }
                    // Ctrl+Alt es AltGr en Windows: se acepta solo si es AltGr
                    if (e.ctrlKey && !altGr) { return; }

                    let accion = null;
                    switch (e.code) {
                        case 'KeyN': accion = () => vista.$server.atajoNuevaVenta(); break;
                        case 'KeyR': accion = () => vista.$server.atajoRenombrarTicket(); break;
                        case 'KeyC': accion = () => vista.$server.atajoCancelarVenta(); break;
                        case 'KeyX': accion = () => vista.$server.atajoAnularFactura(); break;
                        case 'KeyP': accion = () => vista.$server.atajoReimprimir(); break;
                        default: {
                            const m = /^(Digit|Numpad)([1-9])$/.exec(e.code);
                            if (m) {
                                const posicion = parseInt(m[2], 10) - 1;
                                accion = () => vista.$server.atajoSeleccionarTicket(posicion);
                            }
                        }
                    }
                    if (!accion) { return; }

                    // Con un diálogo abierto no se disparan los atajos de la venta
                    if (document.querySelector($1)) { return; }

                    e.preventDefault();
                    e.stopPropagation();
                    clearTimeout(temporizador);
                    bloquearTexto = true;
                    accion();
                };

                document.addEventListener('keydown', manejador, true);
                document.addEventListener('keyup', alSoltar, true);
                document.addEventListener('keypress', bloquear, true);
                document.addEventListener('beforeinput', bloquear, true);
                """,
                vista,
                SELECTOR_DIALOGO_ABIERTO
        );
    }

    /**
     * Evita perder ventas sin cobrar al refrescar la página. Mientras haya
     * productos en alguna venta (ver {@link #marcarVentaConProductos}):
     * <ul>
     * <li>F5, Ctrl+R y Ctrl+F5 se cancelan y se llama a
     * {@code avisoRecargaBloqueada()} de la vista para mostrar el aviso;</li>
     * <li>el botón de recargar del navegador o cerrar la pestaña muestran la
     * confirmación del navegador ("¿Quieres volver a cargar el sitio?"). Eso
     * no se puede bloquear del todo ni cambiar su texto.</li>
     * </ul>
     * Se instala una sola vez por vista y se limpia cuando la vista ya no
     * está en pantalla.
     */
    public static void instalarBloqueoRecarga(Element vista) {
        vista.executeJs(
                """
                const vista = $0;
                if (vista.__bloqueoRecargaInstalado) { return; }
                vista.__bloqueoRecargaInstalado = true;

                const limpiar = () => {
                    document.removeEventListener('keydown', alPresionar, true);
                    window.removeEventListener('beforeunload', alSalir);
                    vista.__bloqueoRecargaInstalado = false;
                };

                const alPresionar = e => {
                    if (!vista.isConnected) { limpiar(); return; }
                    if (!vista.__ventaConProductos) { return; }

                    const recarga = e.key === 'F5'
                            || ((e.ctrlKey || e.metaKey) && e.code === 'KeyR');
                    if (!recarga) { return; }

                    e.preventDefault();
                    e.stopPropagation();
                    vista.$server.avisoRecargaBloqueada();
                };

                const alSalir = e => {
                    if (!vista.isConnected) { limpiar(); return; }
                    if (!vista.__ventaConProductos) { return; }

                    e.preventDefault();
                    e.returnValue = '';
                };

                document.addEventListener('keydown', alPresionar, true);
                window.addEventListener('beforeunload', alSalir);
                """,
                vista
        );
    }

    /**
     * Detecta el POS abierto en otra pestaña del mismo navegador. Las ventas
     * abiertas viven en cada pestaña por separado, así que trabajar en dos
     * hace que se "pierdan" ventas o se cobren en la pestaña equivocada.
     *
     * <p>Cada pestaña se anuncia por un BroadcastChannel ("hola") y las que ya
     * estaban responden ("aqui"). Se llama a {@code avisoOtraPestana(boolean)}
     * de la vista: {@code true} en la pestaña nueva, {@code false} en la que ya
     * estaba abierta. Solo avisa, no bloquea. Si la vista ya no está en
     * pantalla, el canal se cierra y no responde.</p>
     */
    public static void instalarAvisoOtraPestana(Element vista) {
        vista.executeJs(
                """
                const vista = $0;
                if (!('BroadcastChannel' in window)) { return; }
                if (vista.__canalPos) { vista.__canalPos.close(); }

                const id = Math.random().toString(36).slice(2);
                const canal = new BroadcastChannel('colmadom-pos');
                vista.__canalPos = canal;
                // Con 3 o más pestañas, un solo aviso por apertura
                let avisado = false;

                canal.onmessage = e => {
                    if (!vista.isConnected) {
                        canal.close();
                        if (vista.__canalPos === canal) { vista.__canalPos = null; }
                        return;
                    }
                    const m = e.data || {};
                    if (m.id === id) { return; }
                    if (m.tipo === 'hola') {
                        canal.postMessage({ tipo: 'aqui', id });
                        vista.$server.avisoOtraPestana(false);
                    } else if (m.tipo === 'aqui' && !avisado) {
                        avisado = true;
                        vista.$server.avisoOtraPestana(true);
                    }
                };

                canal.postMessage({ tipo: 'hola', id });
                """,
                vista
        );
    }

    /**
     * Cantidad antes del producto: en el buscador se escribe "50" y luego "*"
     * (teclado numérico o Shift+8); se llama a
     * {@code fijarCantidadPendiente(String)} de la vista y el buscador queda
     * vacío para escanear o buscar el producto, que entra con esa cantidad.
     * Esc en el buscador con una cantidad pendiente llama a
     * {@code cancelarCantidadPendiente()}.
     *
     * <p>Se atiende en fase de captura y se detiene el evento: así el "*" no
     * llega al atajo de la vista que abre "Editar cantidad". Si el buscador no
     * tiene solo un número, "*" sigue funcionando como antes.</p>
     */
    public static void instalarCantidadConAsterisco(Element vista, Element buscador) {
        vista.executeJs(
                """
                const vista = $0;
                const buscador = $1;
                if (vista.__cantidadAsteriscoInstalada) { return; }
                vista.__cantidadAsteriscoInstalada = true;

                const manejador = e => {
                    if (!vista.isConnected) {
                        document.removeEventListener('keydown', manejador, true);
                        vista.__cantidadAsteriscoInstalada = false;
                        return;
                    }
                    if (!e.composedPath().includes(buscador)) { return; }

                    const input = buscador.inputElement;
                    const texto = input ? input.value.trim() : '';

                    if (e.key === 'Escape' && vista.__cantidadPendiente && texto === '') {
                        e.preventDefault();
                        e.stopPropagation();
                        vista.$server.cancelarCantidadPendiente();
                        return;
                    }

                    if (e.key !== '*' || e.altKey || e.ctrlKey || e.metaKey) { return; }
                    if (!/^\\d{1,5}([.,]\\d{1,3})?$/.test(texto)) { return; }

                    e.preventDefault();
                    e.stopPropagation();
                    buscador.opened = false;
                    buscador.filter = '';
                    input.value = '';
                    vista.$server.fijarCantidadPendiente(texto.replace(',', '.'));
                };

                document.addEventListener('keydown', manejador, true);
                """,
                vista,
                buscador
        );
    }

    /** Indica al navegador si hay una cantidad esperando producto (para Esc). */
    public static void marcarCantidadPendiente(Element vista, boolean pendiente) {
        vista.executeJs("$0.__cantidadPendiente = $1;", vista, pendiente);
    }

    /** Indica al navegador si hay ventas con productos (activa el bloqueo de recarga). */
    public static void marcarVentaConProductos(Element vista, boolean hayProductos) {
        vista.executeJs("$0.__ventaConProductos = $1;", vista, hayProductos);
    }

    /**
     * Pone el foco en el buscador y selecciona su texto, salvo que haya un
     * diálogo abierto o el buscador ya tenga el foco.
     */
    public static void enfocarBuscador(Element buscador) {
        buscador.executeJs(
                """
                if (document.querySelector($0)) { return; }
                if (document.activeElement === this) { return; }
                this.focus();
                if (this.inputElement) { this.inputElement.select(); }
                """,
                SELECTOR_DIALOGO_ABIERTO
        );
    }

    /**
     * Un clic en una zona "vacía" de la vista devuelve el foco al buscador,
     * pero un clic sobre un control (botón, grid, campo, pestaña...) lo
     * respeta. Antes cualquier clic que subiera hasta la vista (incluidos los
     * "clics" sintéticos de los atajos F2, F3, Alt+C y Alt+N) mandaba el foco
     * al buscador: el diálogo recién abierto lo perdía y el grid dejaba de
     * recibir Supr y *.
     */
    public static void instalarFocoPorClick(Element vista, Element buscador) {
        vista.executeJs(
                """
                const vista = $0;
                const buscador = $1;
                if (vista.__focoBuscadorInstalado) { return; }
                vista.__focoBuscadorInstalado = true;

                const interactivo = /^(VAADIN-(BUTTON|GRID|GRID-CELL-CONTENT|GRID-SORTER|TEXT-FIELD|TEXT-AREA|INTEGER-FIELD|NUMBER-FIELD|BIG-DECIMAL-FIELD|PASSWORD-FIELD|COMBO-BOX|SELECT|DATE-PICKER|CHECKBOX|RADIO-GROUP|TAB|TABS|ICON)|BUTTON|INPUT|TEXTAREA|SELECT|A)$/;

                vista.addEventListener('click', e => {
                    if (e.composedPath().some(n => n.tagName && interactivo.test(n.tagName))) { return; }
                    if (document.querySelector($2)) { return; }
                    buscador.focus();
                });
                """,
                vista,
                buscador,
                SELECTOR_DIALOGO_ABIERTO
        );
    }
}
