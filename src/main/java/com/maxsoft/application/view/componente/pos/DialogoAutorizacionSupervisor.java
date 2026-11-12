package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.servicio.interfaces.venta.AutorizacionService;
import com.maxsoft.application.view.venta.cajaChica.DialogoCajaEstilo;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import java.util.function.Consumer;

/**
 * Pide usuario y clave de un supervisor para autorizar una operación
 * restringida en caja (p. ej. vender de un lote vencido). Si las credenciales
 * son válidas, entrega el nombre del supervisor y se cierra.
 */
public class DialogoAutorizacionSupervisor extends Dialog {

    private final TextField txtUsuario = new TextField("Usuario del supervisor");
    private final PasswordField txtClave = new PasswordField("Clave");
    private final Span error = new Span();

    /**
     * @param titulo qué se autoriza
     * @param lineas detalle de lo que se autoriza (una por línea)
     * @param alAutorizar recibe el nombre del supervisor que autorizó
     */
    public DialogoAutorizacionSupervisor(String titulo, List<String> lineas,
            AutorizacionService autorizacionService, Consumer<String> alAutorizar) {

        addClassName("dc-dialogo");
        setHeaderTitle("Autorización de supervisor");
        setCloseOnOutsideClick(false);
        setWidth("520px");
        setMaxWidth("95vw");

        Div banda = DialogoCajaEstilo.banda(VaadinIcon.LOCK, titulo,
                "Verifique el producto antes de autorizar");

        Div detalle = new Div();
        detalle.addClassName("dc-tarjeta");
        detalle.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "6px")
                .set("background", "#fdecea").set("border-color", "#c62828");
        for (String l : lineas) {
            Span s = new Span("• " + l);
            s.getStyle().set("color", "#8e1b1b").set("font-weight", "600");
            detalle.add(s);
        }
        Span aviso = new Span("Vender productos vencidos está prohibido (Ley 358-05). Autorice solo si el"
                + " producto que tiene el cliente en la mano NO está vencido (p. ej. llegó mercancía nueva"
                + " que aún no se ha registrado). La autorización queda registrada en la factura.");
        aviso.getStyle().set("font-size", "0.85rem").set("color", "var(--lumo-secondary-text-color)");

        txtUsuario.setWidthFull();
        txtClave.setWidthFull();
        error.getStyle().set("color", "#c62828").set("font-weight", "600");

        VerticalLayout cuerpo = new VerticalLayout(banda, detalle, aviso, txtUsuario, txtClave, error);
        cuerpo.addClassName("dc-cuerpo");
        cuerpo.setPadding(false);
        add(cuerpo);

        Button cancelar = new Button("Cancelar", e -> close());
        cancelar.addClassName("dc-btn-secundario");
        Button autorizar = new Button("Autorizar", VaadinIcon.CHECK.create(), e -> {
            try {
                String supervisor = autorizacionService.autorizarSupervisor(txtUsuario.getValue(), txtClave.getValue());
                close();
                alAutorizar.accept(supervisor);
            } catch (IllegalArgumentException ex) {
                error.setText(ex.getMessage());
                txtClave.clear();
                txtClave.focus();
            }
        });
        autorizar.addClassName("dc-btn-abrir");
        autorizar.addClickShortcut(Key.ENTER);
        getFooter().add(cancelar, autorizar);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                txtUsuario.focus();
            }
        });
    }
}
