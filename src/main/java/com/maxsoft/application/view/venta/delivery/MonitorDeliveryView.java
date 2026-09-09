/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.delivery;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Monitor de Deliveries")
@Route(value = "monitorDelivery")
@Menu(order = 6, icon = LineAwesomeIconUrl.MAP_MARKED_ALT_SOLID)
public class MonitorDeliveryView extends VerticalLayout {

    private static final DecimalFormat MONEDA_FORMAT = new DecimalFormat("#,##0.00");
    private final Grid<PedidoMonitoreoDTO> gridMonitor = new Grid<>(PedidoMonitoreoDTO.class, false);

    public MonitorDeliveryView() {
        setSizeFull();
        setSpacing(true);

        H2 title = new H2("Monitor de Deliveries en Tiempo Real");

        // Barra superior de filtros opcionales (por motorista)
        ComboBox<String> filtroMotorista = new ComboBox<>("Filtrar por Motorista");
        filtroMotorista.setItems("Todos", "José (Motor 1)", "Luis (Motor 2)", "Junior (Motor 3)");
        filtroMotorista.setValue("Todos");
        filtroMotorista.setWidth("250px");
        filtroMotorista.addValueChangeListener(e -> actualizarFiltro(e.getValue()));

        // Configuración de la tabla de monitoreo
        gridMonitor.addColumn(PedidoMonitoreoDTO::getNumeroFactura).setHeader("No. Factura").setAutoWidth(true);
        gridMonitor.addColumn(PedidoMonitoreoDTO::getCliente).setHeader("Cliente").setAutoWidth(true);
        gridMonitor.addColumn(PedidoMonitoreoDTO::getDireccion).setHeader("Dirección / Sector").setAutoWidth(true);
        gridMonitor.addColumn(PedidoMonitoreoDTO::getMotorista).setHeader("Motorista").setAutoWidth(true);
        gridMonitor.addColumn(item -> "RD$ " + MONEDA_FORMAT.format(item.getMontoTotal())).setHeader("Monto Total");
        
        // Columna visual para el Estado del Delivery con etiquetas estilizadas
        gridMonitor.addComponentColumn(this::crearBadgeEstado).setHeader("Estatus Actual").setAutoWidth(true);

        // Columna de acciones rápidas (Ej: Marcar como Entregado o Reasignar)
        gridMonitor.addComponentColumn(pedido -> {
            Button btnAccion = new Button("Cambiar Estado", VaadinIcon.EXCHANGE.create());
            btnAccion.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnAccion.addClickListener(e -> cambiarEstadoPedido(pedido));
            return btnAccion;
        }).setHeader("Acciones").setAutoWidth(true);

        // Cargar datos de prueba iniciales
        gridMonitor.setItems(obtenerMockData());

        add(title, filtroMotorista, gridMonitor);
    }

    private Span crearBadgeEstado(PedidoMonitoreoDTO pedido) {
        Span badge = new Span(pedido.getEstado());
        badge.getElement().getStyle().set("padding", "4px 10px");
        badge.getElement().getStyle().set("border-radius", "12px");
        badge.getElement().getStyle().set("font-size", "0.85rem");
        badge.getElement().getStyle().set("font-weight", "bold");

        switch (pedido.getEstado()) {
            case "EN RUTA":
                badge.getStyle().set("background-color", "var(--lumo-warning-color-10x)");
                badge.getStyle().set("color", "var(--lumo-warning-text-color)");
                break;
            case "ENTREGADO":
                badge.getStyle().set("background-color", "var(--lumo-success-color-10x)");
                badge.getStyle().set("color", "var(--lumo-success-text-color)");
                break;
            case "DEVUELTO":
                badge.getStyle().set("background-color", "var(--lumo-error-color-10x)");
                badge.getStyle().set("color", "var(--lumo-error-text-color)");
                break;
        }
        return badge;
    }

    private void cambiarEstadoPedido(PedidoMonitoreoDTO pedido) {
        // Lógica simple para rotar estados de ejemplo
        if ("EN RUTA".equals(pedido.getEstado())) {
            pedido.setEstado("ENTREGADO");
        } else if ("ENTREGADO".equals(pedido.getEstado())) {
            pedido.setEstado("DEVUELTO");
        } else {
            pedido.setEstado("EN RUTA");
        }
        gridMonitor.getDataProvider().refreshItem(pedido);
        Notification.show("Estado actualizado para el pedido " + pedido.getNumeroFactura(), 2000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void actualizarFiltro(String motorista) {
        // Aquí filtrarías la lista desde tu base de datos o servicio
        List<PedidoMonitoreoDTO> data = obtenerMockData();
        if (!"Todos".equals(motorista)) {
            data.removeIf(p -> !p.getMotorista().equals(motorista));
        }
        gridMonitor.setItems(data);
    }

    private List<PedidoMonitoreoDTO> obtenerMockData() {
        List<PedidoMonitoreoDTO> lista = new ArrayList<>();
        lista.add(new PedidoMonitoreoDTO("FAC-001", "Juan Pérez", "Ensanche Altagracia, C/ 4 #12", "José (Motor 1)", 500.00, "EN RUTA"));
        lista.add(new PedidoMonitoreoDTO("FAC-002", "María Gómez", "Los Jardines, C/ Principal #8", "Luis (Motor 2)", 1250.00, "EN RUTA"));
        lista.add(new PedidoMonitoreoDTO("FAC-003", "Carlos Juan", "Cerros de Gurabo, C/ 1 #5", "Junior (Motor 3)", 850.00, "ENTREGADO"));
        return lista;
    }

    // DTO auxiliar para el monitoreo
    public static class PedidoMonitoreoDTO {
        private String numeroFactura;
        private String cliente;
        private String direccion;
        private String motorista;
        private double montoTotal;
        private String estado; // EN RUTA, ENTREGADO, DEVUELTO

        public PedidoMonitoreoDTO(String numeroFactura, String cliente, String direccion, String motorista, double montoTotal, String estado) {
            this.numeroFactura = numeroFactura;
            this.cliente = cliente;
            this.direccion = direccion;
            this.motorista = motorista;
            this.montoTotal = montoTotal;
            this.estado = estado;
        }

        public String getNumeroFactura() { return numeroFactura; }
        public String getCliente() { return cliente; }
        public String getDireccion() { return direccion; }
        public String getMotorista() { return motorista; }
        public double getMontoTotal() { return montoTotal; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}
