package com.maxsoft.application.view.inventario.traslado;

import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService.ArticuloPorTrasladar;
import com.maxsoft.application.util.NavigationContext;
import com.maxsoft.application.util.TrasladoMostradorUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Artículos del mostrador que llegaron a su mínimo (o se agotaron) y tienen
 * existencia en otro almacén, con la cantidad sugerida. "Crear traslado" abre
 * la pantalla de traslado ya llena con los seleccionados.
 */
@PageTitle("Por trasladar al mostrador")
@Route(value = "inventario/porTrasladar")
public class PorTrasladarView extends VerticalLayout {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PorTrasladarView.class);

    private final InventarioService inventarioService;

    private final Grid<ArticuloPorTrasladar> grid = new Grid<>();
    private final Button btnCrear = new Button("Crear traslado", VaadinIcon.EXCHANGE.create());
    private final Span resumen = new Span();

    public PorTrasladarView(InventarioService inventarioService) {
        this.inventarioService = inventarioService;

        setSizeFull();

        H2 titulo = new H2("Por trasladar al mostrador");
        titulo.getStyle().set("margin", "0");

        Paragraph ayuda = new Paragraph("Artículos cuyo mostrador está en su mínimo o agotado y que tienen "
                + "existencia en otro almacén. Cantidades en unidad base. La cantidad a trasladar llega "
                + "hasta el máximo del mostrador (o el doble del mínimo si no tiene máximo), sin pasar de lo "
                + "que hay en el almacén de origen.");
        ayuda.getStyle().set("margin", "0").set("color", "var(--lumo-secondary-text-color)");

        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCrear.addClickListener(e -> crearTraslado());

        Button btnActualizar = new Button("Actualizar", VaadinIcon.REFRESH.create(), e -> cargar());

        HorizontalLayout acciones = new HorizontalLayout(btnCrear, btnActualizar, resumen);
        acciones.setAlignItems(Alignment.CENTER);

        configurarGrid();

        add(titulo, ayuda, acciones, grid);
        cargar();
    }

    private void configurarGrid() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        grid.addColumn(ArticuloPorTrasladar::descripcion).setHeader("Artículo")
                .setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(ArticuloPorTrasladar::descripcion));
        grid.addColumn(a -> numero(a.mostrador())).setHeader("Mostrador")
                .setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(ArticuloPorTrasladar::mostrador));
        grid.addColumn(a -> a.minimo().signum() > 0 ? numero(a.minimo()) : "—").setHeader("Mínimo")
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(a -> a.maximo().signum() > 0 && a.maximo().compareTo(TrasladoMostradorUtil.SIN_MAXIMO) < 0
                ? numero(a.maximo()) : "—").setHeader("Máximo")
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(ArticuloPorTrasladar::nombreOrigen).setHeader("Almacén origen")
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(a -> numero(a.enOrigen())).setHeader("En origen")
                .setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(ArticuloPorTrasladar::enOrigen));
        var colSugerido = grid.addComponentColumn(a -> {
            Span s = new Span(numero(a.sugerido()));
            s.getStyle().set("font-weight", "600");
            return s;
        }).setHeader("A trasladar").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(ArticuloPorTrasladar::sugerido));

        grid.addSelectionListener(e -> actualizarBoton());
        grid.sort(List.of(new com.vaadin.flow.component.grid.GridSortOrder<>(colSugerido, SortDirection.DESCENDING)));
    }

    private void cargar() {
        List<ArticuloPorTrasladar> lista;
        try {
            lista = inventarioService.porTrasladar();
        } catch (RuntimeException ex) {
            log.error("No se pudo calcular los artículos por trasladar", ex);
            Notification.show("No se pudo calcular la lista: " + ex.getMessage(), 4000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            lista = List.of();
        }
        grid.setItems(lista);
        // Se proponen todos los que tienen algo que trasladar
        lista.stream().filter(a -> a.sugerido().signum() > 0).forEach(grid::select);
        resumen.setText(lista.isEmpty()
                ? "No hay artículos por trasladar: el mostrador está surtido."
                : lista.size() + (lista.size() == 1 ? " artículo" : " artículos") + " en su mínimo o agotados.");
        actualizarBoton();
    }

    private void actualizarBoton() {
        long n = grid.getSelectedItems().stream().filter(a -> a.sugerido().signum() > 0).count();
        btnCrear.setText(n > 0 ? "Crear traslado (" + n + ")" : "Crear traslado");
        btnCrear.setEnabled(n > 0);
    }

    private void crearTraslado() {
        List<ArticuloPorTrasladar> seleccion = grid.getSelectedItems().stream()
                .filter(a -> a.sugerido().signum() > 0)
                .sorted(Comparator.comparing(ArticuloPorTrasladar::descripcion))
                .toList();
        if (seleccion.isEmpty()) {
            return;
        }

        // Un traslado tiene un solo origen
        Set<Integer> origenes = seleccion.stream().map(ArticuloPorTrasladar::almacenOrigen)
                .collect(Collectors.toSet());
        if (origenes.size() > 1) {
            Notification.show("Los seleccionados salen de almacenes distintos. Seleccione los de un solo "
                    + "almacén de origen y haga un traslado por cada uno.", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        LinkedHashMap<Integer, BigDecimal> cantidades = new LinkedHashMap<>();
        seleccion.forEach(a -> cantidades.put(a.articulo(), a.sugerido()));

        String clave = NavigationContext.store(new InventarioService.PropuestaTraslado(
                origenes.iterator().next(), InventarioService.ALMACEN_VENTA,
                "Reposición del mostrador", cantidades));
        UI.getCurrent().navigate(TrasladoInventarioView.class, clave);
    }

    private static String numero(BigDecimal valor) {
        return valor == null ? "0" : valor.stripTrailingZeros().toPlainString();
    }
}
