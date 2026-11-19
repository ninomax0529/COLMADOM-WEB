package com.maxsoft.application.view.componente.pos;

import com.vaadin.flow.dom.Element;

/**
 * JavaScript del POS que se resuelve en el navegador: atajos Alt+tecla, foco del
 * buscador y "clic en zona vacía devuelve el foco al buscador".
 *
 * <p>Los atajos llaman a métodos {@code @ClientCallable} de la vista:
 * atajoNuevaVenta, atajoRenombrarTicket, atajoCancelarVenta, atajoAnularFactura,
 * atajoAbonoLibreta y atajoSeleccionarTicket(int).</p>
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
     * <li>Alt+A: abono de libreta</li>
     * <li>Alt+1 ... Alt+9: cambiar de venta</li>
     * </ul>
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
                        case 'KeyA': accion = () => vista.$server.atajoAbonoLibreta(); break;
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
