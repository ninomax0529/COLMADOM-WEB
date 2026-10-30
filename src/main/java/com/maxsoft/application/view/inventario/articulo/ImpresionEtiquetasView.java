
package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Vista Vaadin para seleccionar artículos de empaque y solicitar
 * la generación del reporte Jasper etiqueta.jasper.
 *
 * IMPORTANTE:
 * 1. Ajusta el import de ArticuloEmpaqueService al paquete real del proyecto.
 * 2. Completa cargarArticulos() con el método real de tu servicio.
 * 3. Completa obtenerCodigo(), obtenerDescripcion() y obtenerPrecio()
 *    según los getters reales de ArticuloEmpaque.
 * 4. Completa generarReportePdf() con los parámetros y campos reales
 *    que espera etiqueta.jasper.
 */
//@Route("impresion-etiquetas")
@PageTitle("Impresión de etiquetas")
@Route(value = "inventario/impresion-etiquetas")
public class ImpresionEtiquetasView extends VerticalLayout {

    private final ArticuloEmpaqueService articuloEmpaqueService;

    private final TextField filtro = new TextField("Buscar artículo");
    private final ComboBox<String> criterio = new ComboBox<>("Buscar por");
    private final IntegerField cantidadEtiquetas =
            new IntegerField("Cantidad de etiquetas");
    private final Grid<ArticuloEmpaqueFila> grid =
            new Grid<>(ArticuloEmpaqueFila.class, false);

    private final List<ArticuloEmpaqueFila> articulos = new ArrayList<>();
    private ListDataProvider<ArticuloEmpaqueFila> dataProvider;

    public ImpresionEtiquetasView(
            ArticuloEmpaqueService articuloEmpaqueService) {
        this.articuloEmpaqueService = articuloEmpaqueService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        construirVista();
        cargarArticulos();
    }

    private void construirVista() {
        H3 titulo = new H3("Impresión de etiquetas de artículos");

        criterio.setItems("Código", "Descripción");
        criterio.setValue("Descripción");
        criterio.setWidth("180px");

        filtro.setPlaceholder("Escriba para buscar...");
        filtro.setClearButtonVisible(true);
        filtro.setWidthFull();

        filtro.addValueChangeListener(event -> filtrar());

        grid.addColumn(ArticuloEmpaqueFila::getCodigo)
                .setHeader("Código")
                .setAutoWidth(true);

        grid.addColumn(ArticuloEmpaqueFila::getDescripcion)
                .setHeader("Descripción")
                .setFlexGrow(1);

        grid.addColumn(ArticuloEmpaqueFila::getPrecioTexto)
                .setHeader("Precio")
                .setAutoWidth(true);

        grid.addComponentColumn(fila -> {
            IntegerField cantidad = new IntegerField();
            cantidad.setMin(1);
            cantidad.setMax(500);
            cantidad.setStepButtonsVisible(true);
            cantidad.setWidth("130px");
            cantidad.setValue(fila.getCantidad());

            cantidad.addValueChangeListener(event -> {
                Integer valor = event.getValue();
                if (valor != null && valor >= 1 && valor <= 500) {
                    fila.setCantidad(valor);
                }
            });
            return cantidad;
        }).setHeader("Etiquetas a imprimir").setAutoWidth(true);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setWidthFull();
        grid.setHeight("520px");

        criterio.addValueChangeListener(event -> filtrar());

        Button seleccionarTodos = new Button("Seleccionar todos", event ->
                grid.deselectAll());

        Button limpiarSeleccion = new Button("Limpiar selección", event ->
                grid.deselectAll());

        Button recargar = new Button("Recargar artículos", event ->
                cargarArticulos());

        Button imprimir = new Button("Generar etiquetas (Jasper)", event ->
                generarReportePdf());
        imprimir.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cantidadEtiquetas.setMin(1);
        cantidadEtiquetas.setMax(500);
        cantidadEtiquetas.setValue(1);
        cantidadEtiquetas.setStepButtonsVisible(true);
        cantidadEtiquetas.setHelperText(
                "Se usa como cantidad predeterminada para los artículos seleccionados.");

        Button aplicarCantidad = new Button("Aplicar cantidad", event -> {
            Integer cantidad = cantidadEtiquetas.getValue();
            if (cantidad == null || cantidad < 1 || cantidad > 500) {
                Notification.show("Indique una cantidad entre 1 y 500.");
                return;
            }

            for (ArticuloEmpaqueFila fila :
                    grid.asMultiSelect().getSelectedItems()) {
                fila.setCantidad(cantidad);
            }
            grid.getDataProvider().refreshAll();
            Notification.show("Cantidad aplicada a la selección.");
        });

        HorizontalLayout busqueda = new HorizontalLayout(criterio, filtro);
        busqueda.setWidthFull();
        busqueda.setAlignItems(Alignment.END);
        busqueda.setFlexGrow(1, filtro);

        HorizontalLayout cantidadLayout =
                new HorizontalLayout(cantidadEtiquetas, aplicarCantidad);
        cantidadLayout.setAlignItems(Alignment.END);

        HorizontalLayout acciones = new HorizontalLayout(
                seleccionarTodos, limpiarSeleccion, recargar, imprimir);
        acciones.setWidthFull();
//        acciones.setFlexWrap(FlexWrap.WRAP);

        Span ayuda = new Span(
                "Seleccione los artículos, indique cuántas etiquetas desea "
                + "por artículo y genere el reporte Jasper.");
        ayuda.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(titulo, busqueda, cantidadLayout, acciones, grid, ayuda);
        setFlexGrow(1, grid);
    }

    /**
     * Carga artículos desde el servicio.
     *
     * Sustituye el bloque señalado por el método real que exponga
     * ArticuloEmpaqueService. Se deja aislado porque no se ha proporcionado
     * la interfaz real y no sería correcto inventar el nombre de su método.
     */
    private void cargarArticulos() {
        try {
            articulos.clear();

            // TODO: Invocar aquí el método real de ArticuloEmpaqueService.
            // Ejemplo ilustrativo, NO compilable hasta adaptar el método:
            // Collection<ArticuloEmpaque> resultado =
            //         articuloEmpaqueService.listarTodos();
            // resultado.forEach(entidad -> articulos.add(
            //         new ArticuloEmpaqueFila(
            //             obtenerCodigo(entidad),
            //             obtenerDescripcion(entidad),
            //             obtenerPrecio(entidad))));

            dataProvider = new ListDataProvider<>(articulos);
            grid.setDataProvider(dataProvider);

            Notification.show(
                    "Conecte cargarArticulos() al método real de "
                    + "ArticuloEmpaqueService para mostrar los registros.");
        } catch (Exception e) {
            Notification.show(
                    "No fue posible cargar los artículos: " + e.getMessage(),
                    5000,
                    Notification.Position.MIDDLE);
        }
    }

    private void filtrar() {
        if (dataProvider == null) {
            return;
        }

        String texto = filtro.getValue() == null
                ? ""
                : filtro.getValue().trim().toLowerCase();

        String campo = criterio.getValue();

        dataProvider.setFilter(fila -> {
            if (texto.isEmpty()) {
                return true;
            }

            String valor = "Código".equals(campo)
                    ? fila.getCodigo()
                    : fila.getDescripcion();

            return valor != null
                    && valor.toLowerCase().contains(texto);
        });
    }

    /**
     * Aquí se integra JasperReports.
     *
     * Para implementarlo de forma exacta hay que conocer:
     * - el formato de los parámetros de etiqueta.jasper;
     * - si el reporte recibe una colección como JRBeanCollectionDataSource,
     *   una consulta SQL interna o parámetros de un solo artículo;
     * - los campos de la entidad ArticuloEmpaque.
     *
     * La opción habitual es cargar /reports/etiqueta.jasper,
     * llenar el reporte con JasperFillManager.fillReport(...),
     * exportarlo con JasperExportManager.exportReportToPdf(...) y abrir
     * el PDF desde Vaadin. No se inventan parámetros ni columnas del reporte.
     */
    private void generarReportePdf() {
        Collection<ArticuloEmpaqueFila> seleccionados =
                grid.asMultiSelect().getSelectedItems();

        if (seleccionados.isEmpty()) {
            Notification.show("Seleccione al menos un artículo.");
            return;
        }

        Notification.show(
                "La selección está lista. Falta conectar el llenado y "
                + "exportación de etiqueta.jasper con sus parámetros reales.",
                5000,
                Notification.Position.MIDDLE);
    }

    /**
     * DTO de presentación. No reemplaza la entidad de persistencia.
     * Los datos deben mapearse desde ArticuloEmpaque.
     */
    public static class ArticuloEmpaqueFila {
        private final String codigo;
        private final String descripcion;
        private final String precioTexto;
        private int cantidad = 1;

        public ArticuloEmpaqueFila(
                String codigo, String descripcion, String precioTexto) {
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.precioTexto = precioTexto;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getPrecioTexto() {
            return precioTexto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }
    }
}
