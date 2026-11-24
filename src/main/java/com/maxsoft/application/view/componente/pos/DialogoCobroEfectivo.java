package com.maxsoft.application.view.componente.pos;

import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import com.maxsoft.application.servicio.interfaces.delivery.DeliveryService;
import com.maxsoft.application.servicio.interfaces.venta.EstadoFacturaService;
import com.maxsoft.application.servicio.interfaces.venta.TipoVentaService;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DialogoCobroEfectivo extends Dialog {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");

    /**
     * Ya se mandó a guardar: un segundo Enter o clic (lector de códigos,
     * doble toque, sistema lento) no debe crear otra factura.
     */
    private boolean procesando = false;
    // Para los botones de billetes: "1,000" en vez de "1,000.00"
    private static final DecimalFormat MONEDA_ENTERA = new DecimalFormat("#,##0");

    @FunctionalInterface
    public interface AccionGuardarVenta {
        void ejecutar(TicketVenta ticket);
    }

    public DialogoCobroEfectivo(
            TicketVenta ticket,
            DeliveryService deliveryService,
            EstadoFacturaService estadoFacturaService,
            ClienteService clienteService,
            TipoVentaService tipoVentaService,
            AccionGuardarVenta onGuardar,
            Runnable onCerrarTicketActual,
            Runnable onEnfocarBuscador
    ) {
        setHeaderTitle("Cobrar y Facturar - " + ticket.getId());
        setWidth("470px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);

        Double subtotalProductos = ticket.getTotalAmount().doubleValue();

        // Contenedor para selección rápida de tipo de entrega
        HorizontalLayout tipoEntregaLayout = new HorizontalLayout();
        tipoEntregaLayout.setWidthFull();
        tipoEntregaLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button btnLocal = new Button("1. Local [Alt+L]", VaadinIcon.SHOP.create());
        Button btnDelivery = new Button("2. Delivery [Alt+D]", VaadinIcon.TRUCK.create());

        btnLocal.setWidth("48%");
        btnDelivery.setWidth("48%");

        // Contenedor para campos dinámicos de Delivery
        VerticalLayout deliveryLayout = new VerticalLayout();
        deliveryLayout.setPadding(false);
        deliveryLayout.setSpacing(true);
        deliveryLayout.setVisible(false);

        TextField direccionField = new TextField("Dirección de entrega");
        direccionField.setPlaceholder("Ej: 29-5, C/ Principal #5");
        direccionField.setWidthFull();
        direccionField.setMaxLength(150);
        // Valor al servidor mientras se escribe (Enter llegaba antes que el texto)
        direccionField.setValueChangeMode(ValueChangeMode.EAGER);
        // Ticket renombrado (ej. "29-5"): ese nombre es la dirección
        if (ticket.getNombrePersonalizado() != null) {
            direccionField.setValue(ticket.getNombrePersonalizado());
        }

        TextField telefonoField = new TextField("Teléfono de Contacto");
        telefonoField.setPlaceholder("809-000-0000");
        telefonoField.setWidthFull();

        ComboBox<Delivery> cbDelivery = new ComboBox<>("Asignar Motorista (Delivery)");
        cbDelivery.setItems(deliveryService.getHabilitados());
        cbDelivery.setWidthFull();

        BigDecimalField costoEnvioField = new BigDecimalField("Costo de Delivery (RD$)");
        costoEnvioField.setValue(BigDecimal.ZERO);
        costoEnvioField.setWidthFull();

        deliveryLayout.add(direccionField, cbDelivery);

        // Campos de pago
        NumberField cashGivenField = new NumberField("Efectivo Recibido (RD$)");
        cashGivenField.setWidthFull();
        cashGivenField.setValueChangeMode(ValueChangeMode.EAGER);

        final boolean[] esDeliveryState = {false};

        btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnDelivery.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // Métodos de selección que mantienen el foco estratégico
        Runnable seleccionarLocal = () -> {
            esDeliveryState[0] = false;
            deliveryLayout.setVisible(false);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            cashGivenField.focus(); // Retorna el foco al campo de monto a cobrar
        };

        Runnable seleccionarDelivery = () -> {
            esDeliveryState[0] = true;
            deliveryLayout.setVisible(true);
            btnDelivery.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            btnDelivery.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnLocal.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            direccionField.focus(); // Pasa el foco directamente a la dirección
        };

        btnLocal.addClickListener(e -> seleccionarLocal.run());
        btnDelivery.addClickListener(e -> seleccionarDelivery.run());

        // Atajos globales a nivel de Diálogo para trabajar sin importar dónde esté el foco
        Shortcuts.addShortcutListener(this, seleccionarLocal::run, Key.KEY_L, KeyModifier.ALT);
        Shortcuts.addShortcutListener(this, seleccionarDelivery::run, Key.KEY_D, KeyModifier.ALT);

        tipoEntregaLayout.add(btnLocal, btnDelivery);

        Span totalToPayLabel = new Span("Total a Pagar: RD$ " + MONEDA_FORMAT.format(subtotalProductos));
        totalToPayLabel.getStyle().set("font-size", "1.2rem").set("font-weight", "bold");

        Span changeDueLabel = new Span("Devuelta: RD$ 0.00");
        changeDueLabel.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-success-text-color)");

        // Lógica de cálculo de total con envío
        costoEnvioField.addValueChangeListener(e -> {
            BigDecimal envio = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            double totalFinal = subtotalProductos + envio.doubleValue();
            totalToPayLabel.setText("Total a Pagar (Incl. Envío): RD$ " + MONEDA_FORMAT.format(totalFinal));

            Double cashGiven = cashGivenField.getValue();
            if (cashGiven != null && cashGiven >= totalFinal) {
                Double change = cashGiven - totalFinal;
                changeDueLabel.setText("Devuelta: RD$ " + MONEDA_FORMAT.format(change));
            } else {
                changeDueLabel.setText("Devuelta: Insuficiente");
            }
        });

        // Lógica de cálculo de devuelta
        cashGivenField.addValueChangeListener(event -> {
            Double cashGiven = event.getValue();
            BigDecimal envio = costoEnvioField.getValue() != null ? costoEnvioField.getValue() : BigDecimal.ZERO;
            double totalFinal = subtotalProductos + envio.doubleValue();

            if (cashGiven != null && cashGiven >= totalFinal) {
                Double change = cashGiven - totalFinal;
                changeDueLabel.setText("Devuelta: RD$ " + MONEDA_FORMAT.format(change));
            } else {
                changeDueLabel.setText("Devuelta: Insuficiente");
            }
        });

        // Billetes rápidos: un toque pone con cuánto paga el cliente
        Div billetesRapidos = new Div();
        billetesRapidos.setWidthFull();
        billetesRapidos.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "var(--lumo-space-xs)");

        java.util.List<BigDecimal> montos = montosRapidos(ticket.getTotalAmount());
        for (int i = 0; i < montos.size(); i++) {
            BigDecimal monto = montos.get(i);
            // El primero siempre es el total exacto
            boolean exacto = i == 0;
            Button b = new Button(exacto ? "Exacto" : MONEDA_ENTERA.format(monto));
            b.addThemeVariants(exacto ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_CONTRAST);
            b.getStyle().set("flex", "1 1 0").set("min-width", "4.5em").set("margin", "0").set("font-weight", "600");
            b.setTooltipText("El cliente paga con RD$ " + MONEDA_FORMAT.format(monto));
            b.addClickListener(e -> {
                cashGivenField.setValue(monto.doubleValue());
                // El foco vuelve al campo: Enter completa la venta
                cashGivenField.focus();
            });
            billetesRapidos.add(b);
        }

        dialogLayout.add(tipoEntregaLayout, deliveryLayout, totalToPayLabel, cashGivenField, billetesRapidos, changeDueLabel);
        add(dialogLayout);

        Button cancelButton = new Button("Cancelar [Esc]", e -> close());
        Button confirmButton = new Button("Completar Venta [Enter]", VaadinIcon.CHECK.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        // Atajo global para procesar venta con Enter
        Shortcuts.addShortcutListener(this, () -> confirmButton.click(), Key.ENTER);

        confirmButton.addClickListener(e -> {

            if (procesando) {
                return;
            }

            // Venta en el local: si escribió con cuánto paga, no puede ser
            // menos que el total (la caja quedaría descuadrada). Vacío = exacto.
            // En delivery no aplica: el motorista cobra al entregar.
            if (!esDeliveryState[0] && efectivoInsuficiente(cashGivenField.getValue(), ticket.getTotalAmount())) {
                Notification.show("El efectivo recibido es menor que el total (RD$ "
                        + MONEDA_FORMAT.format(ticket.getTotalAmount()) + ")",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                cashGivenField.focus();
                return;
            }

            // Cliente de contado: PUBLICO GENERAL (por nombre, no por posición)
            final com.maxsoft.application.modelo.Cliente clienteGenerico;
            try {
                clienteGenerico = clienteService.getClienteGenerico();
            } catch (IllegalStateException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (esDeliveryState[0]) {

                // Obligatoria, salvo que el ticket tenga nombre propio (ej. "29-5")
                if (ticket.direccionEntrega(direccionField.getValue()) == null) {
                    Notification.show("Escriba la dirección de entrega (o póngale nombre al ticket)",
                            3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                    direccionField.focus();
                    return;
                }

                if (cbDelivery.getValue() == null) {
                    Notification.show("Debe asignar un motorista para el pedido", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                    cbDelivery.focus();
                    return;
                }

                ticket.setDelivery(cbDelivery.getValue());
                ticket.setEstadoFactura(estadoFacturaService.getEstadoFactura(1)); // Pendiente (delivery)

                // Ya validada arriba: la escrita o el nombre del ticket
                String dir = ticket.direccionEntrega(direccionField.getValue());
                ticket.setCliente(clienteGenerico);
                ticket.setNombreCliente(dir);
                ticket.setDireccion(dir);

            } else {

                ticket.setEstadoFactura(estadoFacturaService.getEstadoFactura(2)); // Pagada
                ticket.setCliente(clienteGenerico);

                ticket.setNombreCliente(ticket.getCliente().getNombre());
                ticket.setDireccion(ticket.getCliente().getDireccion());
            }

            ticket.setTipoVenta(tipoVentaService.getTipoVenta(1)); // Contado

            // Validado: desde aquí solo se factura una vez
            if (procesando) {
                return;
            }
            procesando = true;
            confirmButton.setEnabled(false);

            // Para mostrar la devuelta en el POS después de cerrar este diálogo
            // (en delivery no se cobra ahora: lo cobra el motorista)
            Double recibido = cashGivenField.getValue();
            ticket.setEfectivoRecibido(!esDeliveryState[0] && recibido != null
                    ? BigDecimal.valueOf(recibido) : null);

            if (onGuardar != null) {
                onGuardar.ejecutar(ticket);
            }
            if (onCerrarTicketActual != null) {
                onCerrarTicketActual.run();
            }
            close();

            if (onEnfocarBuscador != null) {
                onEnfocarBuscador.run();
            }
        });

        getFooter().add(cancelButton, confirmButton);

        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                cashGivenField.focus();
            }
        });
    }

    /**
     * true si el cajero escribió un efectivo menor que el total. Vacío (null)
     * se toma como pago exacto. Se compara a 2 decimales para que el double
     * del campo no rechace un pago exacto por centésimas sueltas.
     */
    public static boolean efectivoInsuficiente(Double efectivo, BigDecimal total) {
        if (efectivo == null || total == null) {
            return false;
        }
        BigDecimal recibido = BigDecimal.valueOf(efectivo).setScale(2, java.math.RoundingMode.HALF_UP);
        return recibido.compareTo(total.setScale(2, java.math.RoundingMode.HALF_UP)) < 0;
    }

    /** Múltiplos a los que se redondea el total (ej. 435 → 450, 500, 1000). */
    private static final int[] REDONDEOS = {50, 100, 500, 1000};
    /** Billetes dominicanos sueltos con que se suele pagar. */
    private static final int[] BILLETES = {200, 500, 1000, 2000};

    /**
     * "Exacto" + los montos con que el cliente probablemente pague: el total
     * redondeado hacia arriba (a 50, 100, 500 y 1000) y cada billete que
     * alcance. Ej.: total 435 → 435, 450, 500, 1000, 2000. Máximo 6 botones.
     */
    public static java.util.List<BigDecimal> montosRapidos(BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return java.util.List.of();
        }
        java.util.TreeSet<BigDecimal> montos = new java.util.TreeSet<>();
        BigDecimal exacto = total.setScale(2, java.math.RoundingMode.HALF_UP);
        montos.add(exacto);
        for (int r : REDONDEOS) {
            BigDecimal m = BigDecimal.valueOf(r);
            BigDecimal redondeado = exacto.divide(m, 0, java.math.RoundingMode.CEILING).multiply(m);
            if (redondeado.compareTo(exacto) > 0) {
                montos.add(redondeado.setScale(2));
            }
        }
        for (int billete : BILLETES) {
            BigDecimal b = BigDecimal.valueOf(billete).setScale(2);
            if (b.compareTo(exacto) > 0) {
                montos.add(b);
            }
        }
        return montos.stream().limit(6).toList();
    }
}