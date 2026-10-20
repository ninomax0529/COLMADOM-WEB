package com.maxsoft.application.view.cxp.factura;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.CondicionPago;
import com.maxsoft.application.modelo.DetalleFacturaDeCompra;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.modelo.TipoCompra;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.servicio.interfaces.compra.ProveedorService;
import com.maxsoft.application.servicio.interfaces.cxp.FacturaDeCompraService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.componente.EmpaqueSeleccionDialogo;
import com.maxsoft.application.view.componente.ToolBarBotonera;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@PermitAll
@PageTitle("Registro Factura de Compra")
@Route(value = "compra/registroFactura")
public class RegistroFacturaDeCompraView extends VerticalLayout {

    private static final String USUARIO_ACTUAL = "ADMIN";

    private static final DecimalFormat FORMATO_NUMERO = new DecimalFormat("#,##0.00");

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private static final int ESCALA_CALCULO = 4;

    private static final int ESCALA_MONETARIA = 2;

    private final Grid<DetalleFacturaDeCompra> grid = new Grid<>(DetalleFacturaDeCompra.class, false);
    private Editor<DetalleFacturaDeCompra> editor;

    private final ComboBox<Proveedor> cbProveedor = new ComboBox<>("Suplidor / Proveedor");
    private final TextField txtNumeroFactura = new TextField("N° Factura del Suplidor");
    private final TextField txtNcf = new TextField("NCF / Comprobante");
    private final DatePicker dpFecha = new DatePicker("Fecha");
    private final ComboBox<Almacen> cbAlmacen = new ComboBox<>("Almacén Destino");
    private final TextField txtObservacion = new TextField("Observación");
    private final TextField txtBuscar = new TextField();
    private final ToolBarBotonera botonera = new ToolBarBotonera(false, true, true);
    private Button btnNuevo;

    private final FacturaDeCompraService facturaService;
    private final ProveedorService proveedorService;
    private final AlmacenService almacenService;
    private final ArticuloService articuloService;
    private final ArticuloEmpaqueService articuloEmpaqueService;

    private final List<DetalleFacturaDeCompra> listDet = new ArrayList<>();

    // Columnas con pie (totales)
    private Grid.Column<DetalleFacturaDeCompra> colDescripcion;
    private Grid.Column<DetalleFacturaDeCompra> colSubTotal;
    private Grid.Column<DetalleFacturaDeCompra> colDescuento;
    private Grid.Column<DetalleFacturaDeCompra> colItbis;
    private Grid.Column<DetalleFacturaDeCompra> colTotal;

    @Autowired
    public RegistroFacturaDeCompraView(FacturaDeCompraService facturaService,
            ProveedorService proveedorService,
            AlmacenService almacenService,
            ArticuloService articuloService,
            ArticuloEmpaqueService articuloEmpaqueService) {

        this.facturaService = facturaService;
        this.proveedorService = proveedorService;
        this.almacenService = almacenService;
        this.articuloService = articuloService;
        this.articuloEmpaqueService = articuloEmpaqueService;

        setSizeFull();
        configurarBotonera();
        configurarControlesSuperiores();
        configurarGridDetalle();

        HorizontalLayout hlDatos = new HorizontalLayout(
                cbProveedor, txtNumeroFactura, txtNcf, dpFecha, cbAlmacen, txtObservacion, botonera);
        hlDatos.setAlignItems(Alignment.BASELINE);
        hlDatos.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout hlArticulo = new HorizontalLayout(txtBuscar, btnNuevo);
        hlArticulo.setWidthFull();

        add(hlDatos, hlArticulo, grid);
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void configurarBotonera() {
        botonera.getGuardar().addClickListener(e -> procesarGuardado());
        botonera.getCancelar().addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
    }

    private void configurarControlesSuperiores() {

        cbProveedor.setItems(this.proveedorService.getLista());
        cbProveedor.setItemLabelGenerator(p -> p.getRnc() != null ? p.getNombre() + " (" + p.getRnc() + ")" : p.getNombre());
        cbProveedor.setAllowCustomValue(false);
        cbProveedor.setRequired(true);
        cbProveedor.setWidth("320px");
        cbProveedor.setPlaceholder("Seleccione el suplidor...");

        txtNumeroFactura.setRequired(true);
        txtNumeroFactura.setWidth("180px");

        txtNcf.setWidth("180px");
        txtNcf.setPlaceholder("Ej. B0100000001");

        dpFecha.setValue(LocalDate.now());

        cbAlmacen.setItems(this.almacenService.getLista());
        cbAlmacen.setItemLabelGenerator(Almacen::getNombre);
        cbAlmacen.setAllowCustomValue(false);
        cbAlmacen.setRequired(true);

        txtObservacion.setWidth("240px");

        btnNuevo = new Button("Artículo (F2)", VaadinIcon.PLUS.create(), event -> abrirDialogoSeleccionArticulo());
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickShortcut(Key.F2);

        txtBuscar.setWidth("50%");
        txtBuscar.setPlaceholder("Filtrar por código o descripción...");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
    }

    // ============================================================
    // ARTÍCULOS
    // ============================================================
    private void abrirDialogoSeleccionArticulo() {

        if (cbProveedor.getValue() == null) {
            ClaseUtil.mostrarNotificacion("Debe seleccionar el suplidor antes de agregar artículos", NotificationVariant.LUMO_WARNING);
            cbProveedor.focus();
            return;
        }

        try {
            ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloService, articulo -> {
                if (articulo != null) {
                    new EmpaqueSeleccionDialogo(
                            articulo,
                            articuloEmpaqueService,
                            empaque -> {
                                if (empaque != null) {
                                    agregarOActualizar(articulo, empaque);
                                }
                            }
                    ).open();
                }
            });
            dialog.open();
        } catch (Exception e) {
            Notification.show("Error abriendo diálogo de artículos: " + e.getMessage(), 3000, Position.TOP_CENTER);
        }
    }

    private void agregarOActualizar(Articulo articulo, ArticuloEmpaque empaque) {

        if (articulo == null || empaque == null) {
            Notification.show("Seleccione un artículo y empaque válidos", 3000, Position.TOP_CENTER);
            return;
        }

        DetalleFacturaDeCompra existente = listDet.stream()
                .filter(d -> d.getArticulo() != null
                && Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())
                && d.getArticuloEmpaque() != null
                && Objects.equals(d.getArticuloEmpaque().getCodigo(), empaque.getCodigo()))
                .findFirst()
                .orElse(null);

        // Mismo artículo y presentación: se suma 1 a la cantidad
        if (existente != null) {

            BigDecimal cantidad = valor(existente.getCantidad()).add(BigDecimal.ONE);
            if (actualizarLinea(existente, cantidad, empaque,
                    existente.getPrecioUnitario(), existente.getPorcientoDescuento())) {
                grid.getDataProvider().refreshItem(existente);
                actualizarTotales();
            }
            return;
        }

        DetalleFacturaDeCompra det = new DetalleFacturaDeCompra();
        det.setCodigo(empaque.getCodigo());
        det.setArticulo(articulo);
        det.setDescripcionArticulo(articulo.getDescripcion());
        det.setPorcientoItbis(itbisDe(articulo));

        if (!actualizarLinea(det, BigDecimal.ONE, empaque, costoDe(articulo, empaque), BigDecimal.ZERO)) {
            return;
        }

        listDet.add(det);
        grid.getDataProvider().refreshAll();
        actualizarTotales();
    }

    /**
     * Único lugar donde se aplican los cambios de una línea: valida, asigna
     * cantidad, presentación, factor, costo y descuento, y recalcula los
     * importes.
     */
    private boolean actualizarLinea(DetalleFacturaDeCompra det, BigDecimal cantidad,
            ArticuloEmpaque empaque, BigDecimal precio, BigDecimal descuento) {

        if (cantidad == null || cantidad.signum() <= 0) {
            Notification.show("La cantidad debe ser mayor a cero.", 3000, Position.MIDDLE);
            return false;
        }

        if (precio == null || precio.signum() < 0) {
            Notification.show("El precio de compra no puede ser negativo.", 3000, Position.MIDDLE);
            return false;
        }

        BigDecimal desc = descuento != null ? descuento : BigDecimal.ZERO;
        if (desc.signum() < 0 || desc.compareTo(CIEN) > 0) {
            Notification.show("El descuento debe estar entre 0 y 100.", 3000, Position.MIDDLE);
            return false;
        }

        det.setCantidad(cantidad);
        det.setArticuloEmpaque(empaque);
        det.setFactorConversion(factorDe(empaque));

        if (empaque != null && empaque.getUnidadEmpaque() != null) {
            det.setUnidad(empaque.getUnidadEmpaque().getCodigo());
        }

        det.setPrecioUnitario(precio);
        det.setPorcientoDescuento(desc);

        calcularImportes(det);

        return true;
    }

    private void calcularImportes(DetalleFacturaDeCompra det) {

        BigDecimal subTotal = valor(det.getCantidad()).multiply(valor(det.getPrecioUnitario()));

        BigDecimal descuento = subTotal
                .multiply(valor(det.getPorcientoDescuento()))
                .divide(CIEN, ESCALA_CALCULO, RoundingMode.HALF_UP);

        BigDecimal baseImponible = subTotal.subtract(descuento);

        BigDecimal itbis = baseImponible
                .multiply(valor(det.getPorcientoItbis()))
                .divide(CIEN, ESCALA_CALCULO, RoundingMode.HALF_UP);

        det.setSubTotal(dinero(subTotal));
        det.setTotalDescuento(dinero(descuento));
        det.setTotalItbis(dinero(itbis));
        det.setTotal(dinero(baseImponible.add(itbis)));
    }

    /**
     * Costo de la presentación: el costo propio del empaque; si no lo tiene,
     * costo base del artículo x factor.
     */
    private BigDecimal costoDe(Articulo articulo, ArticuloEmpaque empaque) {

        if (empaque != null && empaque.getPrecioCompra() != null) {
            return empaque.getPrecioCompra();
        }

        BigDecimal base = (articulo != null && articulo.getPrecioCompra() != null)
                ? articulo.getPrecioCompra() : BigDecimal.ZERO;

        return base.multiply(factorDe(empaque));
    }

    /**
     * Porcentaje de ITBIS del artículo (0 si no está gravado).
     */
    private BigDecimal itbisDe(Articulo articulo) {

        return (articulo != null && articulo.getItbisGravado() != null)
                ? BigDecimal.valueOf(articulo.getItbisGravado())
                : BigDecimal.ZERO;
    }

    private BigDecimal factorDe(ArticuloEmpaque empaque) {

        if (empaque == null) {
            return BigDecimal.ONE;
        }
        BigDecimal f = empaque.getFactorConversion();
        return (f == null || f.signum() <= 0) ? BigDecimal.ONE : f;
    }

    // ============================================================
    // GRID
    // ============================================================
    private void configurarGridDetalle() {

        grid.setHeightFull();
        grid.setWidthFull();

        GridListDataView<DetalleFacturaDeCompra> dataView = grid.setItems(listDet);

        colDescripcion = grid.addColumn(DetalleFacturaDeCompra::getDescripcionArticulo)
                .setHeader("Artículo")
                .setAutoWidth(true)
                .setFooter("TOTAL: 0 artículos");

        grid.addColumn(d -> d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion()
                : d.getUnidad())
                .setHeader("Empaque / Presentación")
                .setKey("empaque")
                .setAutoWidth(true);

        grid.addColumn(d -> numero(d.getCantidad()))
                .setHeader("Cantidad")
                .setKey("cantidad")
                .setAutoWidth(true);

        grid.addColumn(d -> "x" + numero(d.getFactorConversion()))
                .setHeader("Factor")
                .setAutoWidth(true);

        grid.addColumn(d -> numero(d.getPrecioUnitario()))
                .setHeader("Precio Compra")
                .setKey("precio")
                .setAutoWidth(true);

        grid.addColumn(d -> numero(d.getPorcientoDescuento()) + " %")
                .setHeader("Desc. %")
                .setKey("descuento")
                .setAutoWidth(true);

        colSubTotal = grid.addColumn(d -> numero(d.getSubTotal()))
                .setHeader("SubTotal")
                .setAutoWidth(true)
                .setFooter("0.00");

        colDescuento = grid.addColumn(d -> numero(d.getTotalDescuento()))
                .setHeader("Descuento")
                .setAutoWidth(true)
                .setFooter("0.00");

        colItbis = grid.addColumn(d -> numero(d.getTotalItbis()))
                .setHeader("ITBIS")
                .setAutoWidth(true)
                .setFooter("0.00");

        colTotal = grid.addColumn(d -> numero(d.getTotal()))
                .setHeader("Total")
                .setAutoWidth(true)
                .setFooter("0.00");

        grid.addColumn(new ComponentRenderer<>(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que desea remover '" + item.getDescripcionArticulo() + "' de la factura?",
                        () -> {
                            listDet.remove(item);
                            grid.getDataProvider().refreshAll();
                            actualizarTotales();
                        },
                        () -> {
                        }
                );
                dialog.open();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return deleteButton;
        })).setHeader("Acciones").setAutoWidth(true);

        txtBuscar.addValueChangeListener(e -> dataView.setFilter(det -> {
            String term = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            if (term.isEmpty()) {
                return true;
            }
            boolean matchDesc = det.getDescripcionArticulo() != null
                    && det.getDescripcionArticulo().toLowerCase().contains(term);
            boolean matchCod = det.getArticulo() != null && det.getArticulo().getCodigo() != null
                    && det.getArticulo().getCodigo().toString().contains(term);
            return matchDesc || matchCod;
        }));

        configurarEditorInLine();
    }

    /**
     * Doble clic en una línea: se edita presentación, cantidad, precio y
     * descuento. Cualquier cambio pasa por actualizarLinea().
     */
    private void configurarEditorInLine() {

        editor = grid.getEditor();
        editor.setBuffered(false);

        BigDecimalField cantidadField = new BigDecimalField();
        cantidadField.setWidthFull();

        BigDecimalField precioField = new BigDecimalField();
        precioField.setWidthFull();

        BigDecimalField descuentoField = new BigDecimalField();
        descuentoField.setWidthFull();

        ComboBox<ArticuloEmpaque> cbEmpaqueEditor = new ComboBox<>();
        cbEmpaqueEditor.setItemLabelGenerator(ae -> ae.getUnidadEmpaque() != null ? ae.getUnidadEmpaque().getDescripcion() : "");
        cbEmpaqueEditor.setWidthFull();

        grid.getColumnByKey("empaque").setEditorComponent(cbEmpaqueEditor);
        grid.getColumnByKey("cantidad").setEditorComponent(cantidadField);
        grid.getColumnByKey("precio").setEditorComponent(precioField);
        grid.getColumnByKey("descuento").setEditorComponent(descuentoField);

        grid.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel();
            }

            DetalleFacturaDeCompra seleccionado = event.getItem();

            if (seleccionado != null && seleccionado.getArticulo() != null) {
                List<ArticuloEmpaque> opciones = articuloEmpaqueService.getPorArticulo(seleccionado.getArticulo().getCodigo());
                cbEmpaqueEditor.setItems(opciones != null ? opciones : new ArrayList<>());
                cbEmpaqueEditor.setValue(seleccionado.getArticuloEmpaque());
                cantidadField.setValue(seleccionado.getCantidad());
                precioField.setValue(seleccionado.getPrecioUnitario());
                descuentoField.setValue(seleccionado.getPorcientoDescuento());
            }

            editor.editItem(seleccionado);
        });

        // Cambiar la presentación recalcula el costo con el de la nueva presentación
        cbEmpaqueEditor.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null || e.getValue() == null) {
                return;
            }

            DetalleFacturaDeCompra actual = editor.getItem();
            ArticuloEmpaque nuevo = e.getValue();

            if (Objects.equals(nuevo, actual.getArticuloEmpaque())) {
                return;
            }

            BigDecimal nuevoCosto = costoDe(actual.getArticulo(), nuevo);

            if (actualizarLinea(actual, actual.getCantidad(), nuevo, nuevoCosto, actual.getPorcientoDescuento())) {
                precioField.setValue(nuevoCosto);
                refrescar(actual);
            } else {
                cbEmpaqueEditor.setValue(actual.getArticuloEmpaque());
            }
        });

        cantidadField.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleFacturaDeCompra actual = editor.getItem();

            if (actualizarLinea(actual, e.getValue(), actual.getArticuloEmpaque(),
                    actual.getPrecioUnitario(), actual.getPorcientoDescuento())) {
                refrescar(actual);
            } else {
                cantidadField.setValue(actual.getCantidad());
            }
        });

        precioField.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleFacturaDeCompra actual = editor.getItem();

            if (actualizarLinea(actual, actual.getCantidad(), actual.getArticuloEmpaque(), e.getValue(), actual.getPorcientoDescuento())) {
                refrescar(actual);
            } else {
                precioField.setValue(actual.getPrecioUnitario());
            }
        });

        descuentoField.addValueChangeListener(e -> {
            if (!editor.isOpen() || editor.getItem() == null) {
                return;
            }

            DetalleFacturaDeCompra actual = editor.getItem();

            if (actualizarLinea(actual, actual.getCantidad(), actual.getArticuloEmpaque(), actual.getPrecioUnitario(), e.getValue())) {
                refrescar(actual);
            } else {
                descuentoField.setValue(actual.getPorcientoDescuento());
            }
        });
    }

    private void refrescar(DetalleFacturaDeCompra det) {

        grid.getDataProvider().refreshItem(det);
        actualizarTotales();
    }

    private void actualizarTotales() {

        BigDecimal subTotal = listDet.stream().map(d -> valor(d.getSubTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuento = listDet.stream().map(d -> valor(d.getTotalDescuento())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal itbis = listDet.stream().map(d -> valor(d.getTotalItbis())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = listDet.stream().map(d -> valor(d.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);

        colDescripcion.setFooter("TOTAL: " + listDet.size() + (listDet.size() == 1 ? " artículo" : " artículos"));
        colSubTotal.setFooter(numero(subTotal));
        colDescuento.setFooter(numero(descuento));
        colItbis.setFooter(numero(itbis));
        colTotal.setFooter(numero(total));
    }

    // ============================================================
    // GUARDADO
    // ============================================================
    private void procesarGuardado() {

        if (cbProveedor.getValue() == null) {
            Notification.show("Debe seleccionar el suplidor", 3000, Position.TOP_CENTER);
            cbProveedor.focus();
            return;
        }

        if (txtNumeroFactura.getValue() == null || txtNumeroFactura.getValue().isBlank()) {
            Notification.show("Debe indicar el número de factura del suplidor", 3000, Position.TOP_CENTER);
            txtNumeroFactura.focus();
            return;
        }

        if (dpFecha.getValue() == null) {
            Notification.show("Debe indicar la fecha de la factura", 3000, Position.TOP_CENTER);
            dpFecha.focus();
            return;
        }

        if (cbAlmacen.getValue() == null) {
            Notification.show("Debe seleccionar el almacén destino", 3000, Position.TOP_CENTER);
            cbAlmacen.focus();
            return;
        }

        if (listDet.isEmpty()) {
            Notification.show("La factura no tiene artículos registrados", 3000, Position.TOP_CENTER);
            return;
        }

        for (DetalleFacturaDeCompra det : listDet) {
            if (det.getCantidad() == null || det.getCantidad().signum() <= 0) {
                Notification.show("El artículo '" + det.getDescripcionArticulo() + "' tiene una cantidad inválida.", 4000, Position.TOP_CENTER);
                return;
            }
            if (det.getArticuloEmpaque() == null) {
                Notification.show("El artículo '" + det.getDescripcionArticulo() + "' debe tener un empaque asignado.", 4000, Position.TOP_CENTER);
                return;
            }
        }

        try {
            
            FacturaDeCompra factura = new FacturaDeCompra();
            
            factura.setProveedor(cbProveedor.getValue());
            factura.setTipoCompra(new TipoCompra(1));
            factura.setNombreProveedor(cbProveedor.getValue().getNombre());
            factura.setAnulada(false);            
            factura.setNombreEstado("PENDIENTE");
            factura.setUsuario(new Usuario(1));
            factura.setNombreUsaurio("Admin");          
            factura.setTotalIsr(BigDecimal.ZERO);        
            factura.setTotalPagado(BigDecimal.ZERO);
          
            factura.setNumeroFactura(txtNumeroFactura.getValue().trim());
            factura.setNcf(txtNcf.getValue() != null ? txtNcf.getValue().trim() : null);
            factura.setFecha(ClaseUtil.asDate(dpFecha.getValue()));

            factura.setRecepcionMercancia(null); // <--- Aquí aseguraramos la trazabilidad

            factura.setCondicionPago(new CondicionPago(1));

//            factura.setAlmacen(cbAlmacen.getValue());
            factura.setConcepto(txtObservacion.getValue());

            factura.setSubTotal(listDet.stream().map(d -> valor(d.getSubTotal())).reduce(BigDecimal.ZERO, BigDecimal::add));
            factura.setTotalItbis(listDet.stream().map(
                    d -> valor(d.getTotalItbis())).reduce(BigDecimal.ZERO, BigDecimal::add));

            factura.setTotal(listDet.stream().map(d -> valor(d.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add));
              factura.setTotalPendiente(listDet.stream().map(d -> valor(d.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add));

            listDet.forEach(d -> {
                d.setFacturaDeCompra(factura);
                d.setCodigo(null);
            });

            factura.setDetalleFacturaDeCompraCollection(listDet);

            FacturaDeCompra guardada = this.facturaService.guardar(factura);

            ClaseUtil.mostrarNotificacion("Factura de compra #" + guardada.getCodigo() + " guardada correctamente.",
                    NotificationVariant.LUMO_PRIMARY);

            limpiarFormulario();

        } catch (IllegalStateException | IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 5000, Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error al guardar la factura de compra: " + e.getMessage(), 4000, Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void limpiarFormulario() {

        listDet.clear();
        cbProveedor.clear();
        txtNumeroFactura.clear();
        txtNcf.clear();
        txtObservacion.clear();
        dpFecha.setValue(LocalDate.now());
        grid.getDataProvider().refreshAll();
        actualizarTotales();
        cbProveedor.focus();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private BigDecimal valor(BigDecimal v) {

        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal dinero(BigDecimal v) {

        return v.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    private String numero(BigDecimal v) {

        return FORMATO_NUMERO.format(valor(v));
    }
}
