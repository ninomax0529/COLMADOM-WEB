package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Venta por delivery (F6): el motorista se lleva el pedido, cobra al
 * entregar y después el administrador liquida la factura en el colmado
 * (Delivery → Liquidación). La factura queda PENDIENTE hasta esa liquidación.
 *
 * El diálogo solo recoge los datos; la vista arma el ticket y lo guarda.
 */
public class DialogoDelivery extends Dialog {

    private static final DecimalFormat MONEDA = new DecimalFormat("#,##0.00");
    private static final DecimalFormat MONEDA_ENTERA = new DecimalFormat("#,##0");

    /** Largo de la columna factura_de_venta.direccion (VARCHAR 150). */
    private static final int MAX_DIRECCION = 150;

    @FunctionalInterface
    public interface AccionConfirmarDelivery {
        /** cliente puede ser null (cliente de paso: solo dirección). */
        void ejecutar(Delivery motorista, Cliente cliente, String direccion);
    }

    private Delivery motoristaElegido;

    /** Ya se envió: un segundo Enter o clic no debe crear otra factura. */
    private boolean procesando = false;
    private final List<Button> botonesMotorista = new ArrayList<>();

    public DialogoDelivery(
            TicketVenta ticket,
            List<Delivery> motoristas,
            List<Cliente> clientes,
            AccionConfirmarDelivery onConfirmar
    ) {
        setHeaderTitle("Enviar por Delivery - " + ticket.getId());
        setWidth("480px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        BigDecimal total = ticket.getTotalAmount() != null ? ticket.getTotalAmount() : BigDecimal.ZERO;

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        // =========================================================
        // MOTORISTA: botones grandes, son pocos y se elige con un toque
        // =========================================================
        Span lblMotorista = new Span("Motorista");
        lblMotorista.getStyle().set("font-weight", "600").set("font-size", "var(--lumo-font-size-s)");

        Div gridMotoristas = new Div();
        gridMotoristas.setWidthFull();
        gridMotoristas.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(130px, 1fr))")
                .set("gap", "var(--lumo-space-xs)");

        for (Delivery d : motoristas) {
            Button b = new Button(d.getNombre() != null ? d.getNombre() : "Motorista " + d.getCodigo(),
                    VaadinIcon.TRUCK.create());
            b.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            b.getStyle().set("margin", "0");
            b.addClickListener(e -> elegirMotorista(d, b));
            botonesMotorista.add(b);
            gridMotoristas.add(b);
        }
        if (motoristas.size() == 1) {
            elegirMotorista(motoristas.get(0), botonesMotorista.get(0));
        }

        // =========================================================
        // CLIENTE (opcional) Y DIRECCIÓN
        // =========================================================
        TextField direccion = new TextField("Dirección de entrega");
        direccion.setPlaceholder("Ej: 29-5, C/ Principal #5");
        direccion.setWidthFull();
        direccion.setMaxLength(MAX_DIRECCION);
        // El valor se envía mientras se escribe: con el modo por defecto solo
        // llegaba al perder el foco, y al escribir "29-5" + Enter el atajo del
        // diálogo veía el campo vacío y pedía la dirección.
        direccion.setValueChangeMode(ValueChangeMode.EAGER);
        // Si el ticket se renombró (ej. "29-5"), ese nombre es la dirección
        if (ticket.getNombrePersonalizado() != null) {
            direccion.setValue(recortar(ticket.getNombrePersonalizado()));
        }

        ComboBox<Cliente> cbCliente = new ComboBox<>("Cliente (opcional)");
        cbCliente.setPlaceholder("Cliente de paso");
        cbCliente.setItems((c, texto) -> coincide(c, texto), clientes);
        cbCliente.setItemLabelGenerator(c -> c.getNombre() != null ? c.getNombre() : "");
        cbCliente.setClearButtonVisible(true);
        cbCliente.setWidthFull();
        // Al elegir un cliente se trae su dirección (se puede corregir)
        cbCliente.addValueChangeListener(e -> {
            Cliente c = e.getValue();
            if (c != null && c.getDireccion() != null && !c.getDireccion().isBlank()) {
                direccion.setValue(recortar(c.getDireccion().trim()));
            }
        });

        // =========================================================
        // TOTAL Y DEVUELTA QUE LLEVA EL MOTORISTA
        // =========================================================
        Span lblTotal = new Span("Total a cobrar al entregar: RD$ " + MONEDA.format(total));
        lblTotal.getStyle().set("font-size", "1.2rem").set("font-weight", "bold");

        Span lblDevuelta = new Span();
        lblDevuelta.getStyle().set("font-weight", "600").set("color", "var(--lumo-success-text-color)");

        Span lblPagaCon = new Span("¿Con cuánto paga? (para mandar la devuelta)");
        lblPagaCon.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        Div billetes = new Div();
        billetes.setWidthFull();
        billetes.getStyle().set("display", "flex").set("flex-wrap", "wrap").set("gap", "var(--lumo-space-xs)");
        List<Button> botonesBillete = new ArrayList<>();
        // Sin billete elegido todavía (no arrastrar el de un intento anterior)
        ticket.setEfectivoRecibido(null);
        List<BigDecimal> montos = DialogoCobroEfectivo.montosRapidos(total);
        for (int i = 0; i < montos.size(); i++) {
            BigDecimal monto = montos.get(i);
            Button b = new Button(i == 0 ? "Exacto" : MONEDA_ENTERA.format(monto));
            b.addThemeVariants(ButtonVariant.LUMO_SMALL);
            b.getStyle().set("flex", "1 1 0").set("min-width", "4em").set("margin", "0");
            b.addClickListener(e -> {
                botonesBillete.forEach(x -> x.removeThemeVariants(ButtonVariant.LUMO_PRIMARY));
                b.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                // Se muestra en el POS después de enviar ("Llevar devuelta")
                ticket.setEfectivoRecibido(monto);
                BigDecimal devuelta = monto.subtract(total);
                lblDevuelta.setText(devuelta.signum() > 0
                        ? "Mandar devuelta: RD$ " + MONEDA.format(devuelta)
                        : "Paga exacto: no lleva devuelta");
            });
            botonesBillete.add(b);
            billetes.add(b);
        }

        layout.add(lblMotorista, gridMotoristas, cbCliente, direccion, lblTotal, lblPagaCon, billetes, lblDevuelta);
        add(layout);

        // =========================================================
        // BOTONES
        // =========================================================
        Button cancelar = new Button("Cancelar [Esc]", e -> close());
        Button confirmar = new Button("Enviar Delivery [Enter]", VaadinIcon.TRUCK.create());
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        confirmar.addClickListener(e -> {
            if (procesando) {
                return;
            }
            if (motoristaElegido == null) {
                aviso("Elija el motorista que lleva el pedido");
                return;
            }
            // Obligatoria, salvo que el ticket tenga nombre propio (ej. "29-5")
            String dir = ticket.direccionEntrega(direccion.getValue());
            if (dir == null) {
                aviso("Escriba la dirección de entrega (o póngale nombre al ticket)");
                direccion.focus();
                return;
            }
            // Validado: desde aquí solo se factura una vez
            procesando = true;
            confirmar.setEnabled(false);
            close();
            if (onConfirmar != null) {
                onConfirmar.ejecutar(motoristaElegido, cbCliente.getValue(), recortar(dir));
            }
        });
        Shortcuts.addShortcutListener(this, confirmar::click, Key.ENTER);

        getFooter().add(cancelar, confirmar);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                if (motoristaElegido == null && !botonesMotorista.isEmpty()) {
                    botonesMotorista.get(0).focus();
                } else {
                    cbCliente.focus();
                }
            }
        });
    }

    private void elegirMotorista(Delivery d, Button boton) {
        motoristaElegido = d;
        botonesMotorista.forEach(b -> {
            b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            b.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        });
        boton.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
        boton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    private static String recortar(String s) {
        return s.length() > MAX_DIRECCION ? s.substring(0, MAX_DIRECCION) : s;
    }

    private static void aviso(String texto) {
        Notification.show(texto, 2500, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    private static boolean coincide(Cliente c, String texto) {
        if (texto == null || texto.isBlank()) {
            return true;
        }
        String pajar = String.join(" ",
                c.getNombre() != null ? c.getNombre() : "",
                c.getApodo() != null ? c.getApodo() : "",
                c.getCelular() != null ? c.getCelular() : "",
                c.getDireccion() != null ? c.getDireccion() : "").toLowerCase();
        for (String palabra : texto.toLowerCase().trim().split("\\s+")) {
            if (!pajar.contains(palabra)) {
                return false;
            }
        }
        return true;
    }
}
