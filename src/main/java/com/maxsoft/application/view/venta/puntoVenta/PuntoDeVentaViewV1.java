/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.venta.puntoVenta;

/**
 *
 * @author maximilianoalmonte
 */
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.servicio.interfaces.ArticuloService;
import com.maxsoft.application.servicio.interfaces.FacturaDeVentaService;
import com.maxsoft.application.util.ClaseUtil;
import com.maxsoft.application.view.ModuloPrincipal;
import com.maxsoft.application.view.componente.CobrarComponent;
import com.maxsoft.application.view.componente.ComponenetePos;
import com.maxsoft.application.view.componente.RelojDigitalComponent;
import com.maxsoft.application.view.componente.VistaCobrar;
import com.maxsoft.application.view.dialogo.ConfirmDialog;
import com.maxsoft.application.view.inventario.articulo.ArticuloDialogoFilteringView;
import com.maxsoft.application.view.venta.CobroView;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.Menu;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Punto de Venta V1")
@Route(value = "puntoDeVentav1")
//@Layout(value = "venta")
//https://github.com/ninomax0529/vaadin-railway.git
@Menu(order = 4, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)

public class PuntoDeVentaViewV1 extends VerticalLayout implements BeforeLeaveObserver {

    private final Grid<DetalleFacturaDeVenta> grid = new Grid<>(DetalleFacturaDeVenta.class, false);
    private Binder<DetalleFacturaDeVenta> binder = new Binder<>(DetalleFacturaDeVenta.class);
    Editor<DetalleFacturaDeVenta> editor = grid.getEditor();
    private final TextField txtNumDoc = new TextField();
    TextField txtBuscar = new TextField();
    private final DatePicker dpFecha = new DatePicker();
    TextField txtCantidad = new TextField();
    TextField txtSubTotal = new TextField("Recibido:");
    TextField txtTotal = new TextField("Total");
    TextField txtDecuento = new TextField("Descuento");
    TextField txtItbis = new TextField("Itbis");
    private Button btnSalir;
    private Button btnArticulo = null;
    private Button btnAyuda = null;
    private Button btnCobrar = null;
    private Button btnEliminar = null;
    private Button btnCliente = null;
    private Button btnImprimir = null;
    private Button btnCancelar = null;
    private Button btnDescuento = null;

    Button deleteButton = null;
    Button btnEditar = null;
    TextField filtroNombre = new TextField();
    H3 lbNumDoc = new H3("Factura");
    Boolean cambiosSinGuardar = false;

    FormLayout formLayout = new FormLayout();
    GridListDataView<DetalleFacturaDeVenta> dataView = null;
    CobrarComponent cobrarComponent = new CobrarComponent();
    ComponenetePos componente = new ComponenetePos();

    ArticuloService articuloServicel;

    List<DetalleFacturaDeVenta> listDet = new ArrayList<>();

    VerticalLayout vlBotones = new VerticalLayout();
    HorizontalLayout hlContenniido = new HorizontalLayout();
    HorizontalLayout actions = null;

    RelojDigitalComponent reloj = new RelojDigitalComponent();

    private FacturaDeVenta factura;
    FacturaDeVentaService factService;
    int i = 0;

    @Autowired
    public PuntoDeVentaViewV1(FacturaDeVentaService factServiceArg,
            ArticuloService articuloServiceArg) {

        setSizeFull();
//
//        UI.getCurrent().getPage().executeJs("""
//            window.addEventListener('beforeunload', function (e) {
//                e.preventDefault();
//                e.returnValue = '';
//            });
//        """);
//
//        UI.getCurrent().getPage().executeJs("""
//        window.onbeforeunload = null;
//    """);

        i = 1;

        H1 tituloPrincipal = new H1("PUNTO DE VENTA");
        tituloPrincipal.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", "white");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        header.getStyle()
                .set("background", "#111827")
                .set("padding", "15px 23px")
                .set("border-radius", "20px")
                .set("box-shadow", "0 10px 25px rgba(0,0,0,0.5)");

        header.add(tituloPrincipal, reloj);

        ////        setSpacing(true);
        H2 tituloGrid = new H2("Detalle de la Factura ");
        tituloGrid.setWidthFull();
//        
        tituloGrid.addClassName("pos-cambio-label");

        tituloGrid.getStyle()
                .set("margin", "3px")
                .set("padding", "0px 0px 0px 2px")
                .set("font-size", "20px")
                .set("background", "linear-gradient(70deg,#22c55e,#10a34a)")
                .set("border-radius", "0px");

        Div linea = new Div();
        linea.getStyle()
                .set("height", "4px")
                .set("width", "60px")
                .set("background", "linear-gradient(90deg,#22c55e,#16a34a)")
                .set("border-radius", "10px");
        linea.setWidth("100%");

        header.setSpacing(false);
        header.setPadding(false);

        VerticalLayout gridContainer = new VerticalLayout(tituloGrid, grid);
        gridContainer.setSizeFull();
        gridContainer.setSpacing(false);
        gridContainer.setPadding(false);

        this.factService = factServiceArg;
        this.articuloServicel = articuloServiceArg;

        configurarFormulario();
        configurarGridDetalle();
        editarDetalle();

        hlContenniido.setAlignItems(Alignment.START);
        hlContenniido.setWidth("90%");
        hlContenniido.setHeightFull();
        hlContenniido.setAlignItems(Alignment.START);
        hlContenniido.setAlignSelf(Alignment.AUTO, gridContainer);

        hlContenniido.setSpacing(false);
        vlBotones.setWidth("40px");
        vlBotones.setPadding(true);
        vlBotones.setMargin(false);
        vlBotones.setAlignItems(Alignment.START);

        hlContenniido.add(gridContainer, vlBotones);
        add(header, linea, hlContenniido);

        btnCobrar.setEnabled(false);

        btnCobrar.addClassName("btn-cobrar");
        btnEliminar.addClassName("btn-cobrar");
        btnCancelar.addClassName("btn-cancelar");
        btnSalir.addClassNames("btn-salir");
        btnArticulo.addClassNames("btn-salir");

        Button cobrar = new Button("Cobrar");

        cobrar.addClickListener(e -> {

            UI.getCurrent().navigate(CobrarComponent.class);
//            BigDecimal total = calcularTotal();
//
//            VistaCobrar dialog = new VistaCobrar(total.doubleValue(), () -> {
//
//               guardar();
//
//            });
//
//            dialog.open();

        });

        txtBuscar.addBlurListener(e -> {

            txtBuscar.focus();

        });

        txtBuscar.addKeyPressListener(Key.ENTER, e -> {

//            buscarArticulo(txtBuscar.getValue());
        });

        cobrarComponent.setEstadoValidezListener(btnCobrar::setEnabled);

        btnArticulo.getStyle()
                .set("background", "#0f172a");

    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        if (cambiosSinGuardar) {
            event.postpone();
        }
    }

    private void salir() {

        ConfirmDialog dialog = new ConfirmDialog(
                "¿Seguro que quiere salir del punto de venta-> ",
                () -> {

                    cambiosSinGuardar = false;
                    UI.getCurrent().navigate(ModuloPrincipal.class);

                },
                () -> {

                }
        );

        dialog.open();

    }

    private void configurarFormulario() {

        btnArticulo = new Button("Buscar(F2)", e -> buscarArticulo());

        btnImprimir = new Button("Imprimir(F6)", e -> guardar());

        btnSalir = new Button("Salir(F12)", e -> salir());

        btnEliminar = new Button("Eliminar(F3)", e -> {

            grid.asSingleSelect().addValueChangeListener(event -> {

                DetalleFacturaDeVenta seleccionado = event.getValue();

                if (seleccionado != null) {
                    System.out.println("Seleccionado: " + seleccionado.getDescripcionArticulo());
                } else {
                    Notification.show("Tiene que selecionar un articulo", 3000, Notification.Position.MIDDLE);
                    return;
                }
            });
//            if(grid.asSingleSelect().getValue()==null){
//                
//                Notification.show("Tiene que selecionar un articulo", 3000, Notification.Position.MIDDLE);
//                return;
//            }
//            
//            
//            DetalleFacturaDeVenta item = listDet.get(0);
//
//            eliminarArticulo(item);

        });

        btnAyuda = new Button("Ayuda(F1)", e -> UI.getCurrent().navigate(ModuloPrincipal.class));

        btnCobrar = new Button("Cobrar(F10)", e -> guardar());

        btnCliente = new Button("Cliente(F7)", e -> UI.getCurrent().navigate(ModuloPrincipal.class));

        btnDescuento = new Button("Descuento(F5)", e -> UI.getCurrent().navigate(ModuloPrincipal.class));

        btnCancelar = new Button("Cancelar(F9)", e -> cancelar());

        txtNumDoc.setWidth("120px");

        btnArticulo.setWidth("10px");
        btnImprimir.setWidth("150px");
        btnSalir.setWidth("150px");
        btnEliminar.setWidth("150px");
        btnCliente.setWidth("150px");
        btnCobrar.setWidth("150px");
        btnDescuento.setWidth("150px");
        btnCancelar.setWidth("150px");
        btnArticulo.setWidth("150px");

        txtSubTotal.setWidth("150px");
        txtDecuento.setWidth("150px");
        txtItbis.setWidth("150px");
        txtTotal.setWidth("150px");
        btnCobrar.setEnabled(false);
//        txtSubTotal.setEnabled(false);
        txtDecuento.setEnabled(false);
        txtItbis.setEnabled(false);
        txtTotal.setEnabled(false);

        HorizontalLayout hlArt = new HorizontalLayout();

        VerticalLayout vllTotal = new VerticalLayout();
        VerticalLayout vlArticulo = new VerticalLayout();

        vllTotal.add(txtSubTotal, txtDecuento, txtItbis, txtTotal);

        vllTotal.setSpacing(false);
        vlArticulo.setSizeFull();
        vlArticulo.setSpacing(false);
        vlArticulo.setPadding(false);

        vlArticulo.add(txtBuscar, btnArticulo, btnEliminar);

//           btnArticulo.setVisible(false);
        hlArt.setSpacing(false);
//        hlArt.add(dpFecha, lbNumDoc, txtNumDoc, txtBuscar, btnArticulo);
        hlArt.setAlignItems(Alignment.START);

        vlBotones.add(new H3("Resumen Venta:"));

        Hr separador = new Hr();
        separador.setSizeFull();

        TextField campoAzul = new TextField("Usuario");
        separador.addClassNames("btn-cancelar");;

        txtSubTotal.getStyle()
                .set("background-color", "1565c0") // gris muy claro azulado
                .set("border-radius", "8px")
                .set("color", "red")
                .set("padding", "8px");

//        vlBotones.add(txtSubTotal, txtDecuento, txtItbis, txtTotal, separador, btnCobrar, btnImprimir, btnDescuento, btnCliente, btnCancelar);
        vlBotones.add(cobrarComponent, separador, vlArticulo, separador, btnCobrar, btnCancelar, btnSalir);

        vlBotones.setSpacing(false);
        vlBotones.setAlignItems(Alignment.STRETCH);

        btnAyuda.addClickShortcut(Key.F1);
        btnArticulo.addClickShortcut(Key.F2);
        btnCliente.addClickShortcut(Key.F7);
        btnCancelar.addClickShortcut(Key.F9);
        btnCobrar.addClickShortcut(Key.F10);
        btnImprimir.addClickShortcut(Key.F6);
        btnDescuento.addClickShortcut(Key.F5);
        btnSalir.addClickShortcut(Key.F12);
        btnEliminar.addClickShortcut(Key.F3);
//        txtBuscar.focus();
        txtNumDoc.setEnabled(false);
        dpFecha.setValue(LocalDate.now());

        txtBuscar.setWidth("150px");
        txtBuscar.setPlaceholder("Articulo");
        txtBuscar.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        txtBuscar.setValueChangeMode(ValueChangeMode.EAGER);
        txtBuscar.addValueChangeListener(e -> dataView.refreshAll());
        txtBuscar.setClearButtonVisible(true);

        filtroNombre.setPlaceholder("Filtrar por nombre");
        filtroNombre.setClearButtonVisible(true);
        filtroNombre.setPrefixComponent(new Icon(VaadinIcon.SEARCH));

        formLayout.add(hlArt);

    }

    private void guardar() {

        try {

            if (listDet.size() <= 0) {

                Notification.show("La factura no tiene detalle ",
                        3000, Notification.Position.MIDDLE);
                return;
            }

            for (DetalleFacturaDeVenta det2 : listDet) {

                if (det2.getCantidad() <= 0) {

                    Notification.show("El articulo  " + det2.getDescripcionArticulo() + " tiene la cantidad en cero ",
                            4000, Notification.Position.MIDDLE);
                    return;
                }

            }

            LocalDate localFecha = dpFecha.getValue();
            Date fecha = ClaseUtil.asDate(localFecha);

            factura = new FacturaDeVenta();
            factura.setFecha(fecha);
            factura.setFechaCreacion(new Date());
            factura.setFechaActualizacion(new Date());
            factura.setNombreUsuario("Administrador");

            listDet.stream().forEach(e -> {

                e.setFactura(factura);
                e.setCodigo(null);
            });

            ConfirmDialog dialog = new ConfirmDialog(
                    "¿Seguro que quiere guardar la venta-> ",
                    () -> {

                        factura.setDetalleFacturaDeVentaCollection(listDet);
                        this.factService.guardar(factura);
                        Notification.show("Factura guardada exitosamente", 3000, Notification.Position.MIDDLE);
                        listDet.clear();
                        i = 1;
                        cambiosSinGuardar = false;
                        grid.getDataProvider().refreshAll();
                        txtBuscar.focus();

                        // 🔔 Sonido POS
//                        SonidoUtil.reproducir("sound.mp3");
                        cobrarComponent.limpiarMonto();

                    },
                    () -> {

                    }
            );

            dialog.open();

        } catch (Exception e) {
            Notification.show("Error guardando la factura ", 3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }

    }

    private void configurarGridDetalle() {

        grid.setHeight("95%");
        grid.setWidthFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//        grid.addThemeVariants( GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setItems(listDet);

        dataView = grid.setItems(listDet);

        grid.addColumn(DetalleFacturaDeVenta::getNumeroDeLinea)
                .setHeader("Numero")
                .setKey("codigo").setWidth("15px");

        grid.addColumn(DetalleFacturaDeVenta::getCantidad)
                .setHeader("Cantidad")
                .setKey("cantidad").setWidth("30px");

        grid.addColumn(DetalleFacturaDeVenta::getDescripcionArticulo)
                .setHeader("Articulo")
                .setKey("articulo").setAutoWidth(true);

        grid.addColumn(DetalleFacturaDeVenta::getPrecioVenta)
                .setHeader("Precio")
                .setKey("precio").setWidth("30px");

        grid.addColumn(DetalleFacturaDeVenta::getSubTotal)
                .setHeader("SubTotal")
                .setKey("subTotal").setWidth("30px");

        grid.getColumnByKey("cantidad").setEditorComponent(txtCantidad);
//
//        Grid.Column columna = grid.addComponentColumn(item -> {
//
//            actions = new HorizontalLayout();
//
//            actions.setAlignItems(Alignment.END);
//            actions.setSpacing(false);
//            actions.setPadding(false);
//            actions.setJustifyContentMode(JustifyContentMode.END);
//
//            btnEditar = new Button(new Button("(F4)", click -> {
//
//                editarDetalle();
//            }));
//
//            btnEditar.addClickListener(e -> {
//
//                if (editor.isOpen()) {
//                    editor.cancel();
//                }
//                grid.getEditor().editItem(item);
//
//            });
//
//            txtCantidad.setValue(Double.toString(item.getCantidad()));
//
//            deleteButton = new Button("🗑️ (F3)", click -> {
//
//                ConfirmDialog dialog = new ConfirmDialog(
//                        "¿Seguro que quiere eliminar el articulo -> " + item.getDescripcionArticulo().toUpperCase(),
//                        () -> {
//
//                            listDet.remove(item);
//                            grid.getDataProvider().refreshAll();
//                            if (!txtBuscar.isEmpty()) {
//
//                                listDet.remove(item);
//                                grid.getDataProvider().refreshAll();
//                                txtBuscar.clear();
//
//                            }
//
//                            cobrarComponent.setMontoACobrar(calcularTotal());
//                        },
//                        () -> {
//                            txtBuscar.clear();
//                        }
//                );
//
//                dialog.open();
//
//            });
//
//            deleteButton.addClickShortcut(Key.F3);
//            btnEditar.addClickShortcut(Key.F4);
//            deleteButton.setWidth("25px");
//            actions.add(deleteButton);
//
        ////            actions.add(new CantidadStepper(item, grid), deleteButton);
//            actions.setWidthFull();
//
//            return actions;
//
//        }).setHeader("Acciones").setAutoWidth(true);
//
//        columna.addClassName("centralizar-header");

        dataView.addFilter(selectArticulo -> {

            String searchTerm = txtBuscar.getValue().trim();

            if (searchTerm.isEmpty()) {
                return true;
            }

            boolean matchesDescripcion = matchesTerm(selectArticulo.getArticulo().getDescripcion(),
                    searchTerm);
            boolean matchesCodigo = matchesTerm(selectArticulo.getCodigo().toString(), searchTerm);
            grid.select(selectArticulo);

            return matchesDescripcion || matchesCodigo;
        });

        Shortcuts.addShortcutListener(grid, () -> {

            DetalleFacturaDeVenta seleccionado = grid.asSingleSelect().getValue();

            if (seleccionado != null) {

                ConfirmDialog dialog = new ConfirmDialog(
                        "¿Seguro que quiere eliminar el articulo -> " + seleccionado.getDescripcionArticulo().toUpperCase(),
                        () -> {

                            listDet.remove(seleccionado);
                            grid.getDataProvider().refreshAll();
                            txtBuscar.clear();

                            cobrarComponent.setMontoACobrar(calcularTotal());
                        },
                        () -> {
                            txtBuscar.clear();
                        }
                );

                dialog.open();

            }

        }, Key.F3);

//        Editor<DetalleFacturaDeVenta> editor = grid.getEditor();
//        Binder<DetalleFacturaDeVenta> binder = new Binder<>(DetalleFacturaDeVenta.class);
//        editor.setBinder(binder);
//
//        TextField cantidadField1 = new TextField();
//        binder.forField(cantidadField1)
//                .withConverter(new StringToDoubleConverter("Número inválido"))
//                .bind(DetalleFacturaDeVenta::getCantidad, DetalleFacturaDeVenta::setCantidad);
//
//        grid.addColumn(DetalleFacturaDeVenta::getCantidad).setEditorComponent(cantidadField1);
        Shortcuts.addShortcutListener(grid, () -> {

            DetalleFacturaDeVenta seleccionado = grid.asSingleSelect().getValue();

            if (seleccionado != null) {

                NumberField cantidadField = new NumberField("Cantidad");
                cantidadField.setValue(seleccionado.getCantidad());
                cantidadField.setMin(1);
                cantidadField.setStep(1);

                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Editar cantidad de: " + seleccionado.getDescripcionArticulo());
                dialog.addOpenedChangeListener(e -> {
                    if (e.isOpened()) {
                        cantidadField.focus();
                    }
                });

                Button guardar = new Button("Actualizar", e -> {

                    seleccionado.setCantidad(cantidadField.getValue().intValue());
                    seleccionado.setSubTotal(subTotal(seleccionado.getCantidad(), seleccionado.getPrecioVenta()));

                    seleccionado.setTotalDescuento(totalDescuento(seleccionado.getSubTotal(), seleccionado.getPorcientoDescuento()));

                    seleccionado.setTotalItbis(totalItbis(seleccionado.getSubTotal(), seleccionado.getTotalDescuento(), 18.00));

                    seleccionado.setTotal(total(seleccionado.getSubTotal(), seleccionado.getTotalDescuento(), seleccionado.getTotalItbis()));

                    cobrarComponent.setMontoACobrar(calcularTotal());
                    txtBuscar.clear();

                    grid.getDataProvider().refreshItem(seleccionado);

                    dialog.close();
                });

//                cantidadField.addKeyDownListener(Key.ENTER, e -> {
//                    seleccionado.setCantidad(cantidadField.getValue().intValue());
//                    grid.getDataProvider().refreshItem(seleccionado);
//                    dialog.close();
//                });
                guardar.addClickShortcut(Key.ENTER);
                Button cancelar = new Button("Cancelar", e -> dialog.close());

                HorizontalLayout botones = new HorizontalLayout(guardar, cancelar);

                dialog.add(cantidadField, botones);
                dialog.open();
            }

        }, Key.F4);

    }

    private void editarDetalle() {

        editor = grid.getEditor();
        editor.setBuffered(true); // sin botón de guardar
        binder = new Binder<>(DetalleFacturaDeVenta.class);
        editor.setBinder(binder);

        TextField cantidadField = new TextField();
        cantidadField.setWidthFull();
        cantidadField.focus();

        grid.getColumnByKey("cantidad").setEditorComponent(cantidadField);

        grid.addItemDoubleClickListener(event -> {
            if (editor.isOpen()) {
                editor.cancel(); // cerrar el editor anterior
                return;
            }

            editor.editItem(event.getItem());

        });

        editor.addOpenListener(e -> {

            DetalleFacturaDeVenta item = e.getItem();
            cantidadField.setValue(String.valueOf(item.getCantidad()));

        });

        cantidadField.addValueChangeListener(e -> {

            Double cantidad;

            DetalleFacturaDeVenta item = editor.getItem();

            try {

                cantidad = Double.valueOf(e.getValue());

                if (cantidad <= 0) {

                    Notification.show("La cantidad no puede ser menor O igual a cero ", 3000, Notification.Position.MIDDLE);
                    cantidad = 0.00;
                    return;
                }
//                if (!txtBuscar.isEmpty()) {

                if (item != null) {

                    item.setCantidad(cantidad);

                    item.setSubTotal(subTotal(cantidad, item.getPrecioVenta()));

                    item.setTotalDescuento(totalDescuento(item.getSubTotal(), item.getPorcientoDescuento()));

                    item.setTotalItbis(totalItbis(item.getSubTotal(), item.getTotalDescuento(), 18.00));

                    item.setTotal(total(item.getSubTotal(), item.getTotalDescuento(), item.getTotalItbis()));

                    grid.getListDataView().refreshAll();
                    txtBuscar.clear();
                    filtroNombre.clear();
                    cobrarComponent.setMontoACobrar(calcularTotal());
                }

//                }
            } catch (NumberFormatException ex) {
                Notification.show(" Ingrese un número válido ", 2000, Notification.Position.MIDDLE);

                cantidadField.clear();
            }
        });

    }

    private Double subTotal(Double cant, Double precio) {

        Double sutTotal = cant * precio;

        return ClaseUtil.FormatearDouble(sutTotal, 2);

    }

    private Double total(Double subTotal, Double totalDesc, Double totalItbis) {

        Double total = (subTotal - totalDesc) + totalItbis;

        System.out.println("Total " + subTotal + " " + totalDesc + " " + totalItbis);
        return ClaseUtil.FormatearDouble(total, 2);
    }

    private Double totalItbis(Double subTotal, Double descuento, Double itbis) {

        itbis = itbis / 100.00;//Converftir el itbis en tasa

        Double totalItbis = (subTotal - descuento) * itbis;

        return ClaseUtil.FormatearDouble(totalItbis, 2);
    }

    private Double totalDescuento(Double subTotal, Double desc) {

        desc = desc / 100.00;//Converftir el itbis en tasa

        Double totalDesc = subTotal * desc;

        return ClaseUtil.FormatearDouble(totalDesc, 2);
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    private void buscarArticulo() {

        try {

            ArticuloDialogoFilteringView dialog = new ArticuloDialogoFilteringView(articuloServicel, articulo -> {

                if (!(articulo == null)) {

                    boolean existe = existeArticulo(listDet, articulo.getCodigo());

                    if (existe) {

                        listDet.forEach((var d) -> {

                            if (Objects.equals(d.getArticulo().getCodigo(), articulo.getCodigo())) {

                                d.setCantidad(d.getCantidad() + 1);

                                d.setSubTotal(subTotal(d.getCantidad(), d.getPrecioVenta()));

                                d.setTotalDescuento(totalDescuento(d.getSubTotal(), d.getPorcientoDescuento()));

                                d.setTotalItbis(totalItbis(d.getSubTotal(), d.getTotalDescuento(), 18.00));

                                d.setTotal(total(d.getSubTotal(), d.getTotalDescuento(), d.getTotalItbis()));

                                grid.getListDataView().refreshAll();

                                cobrarComponent.setMontoACobrar(calcularTotal());

                            }

                        });
                        return;
                    }

                    DetalleFacturaDeVenta det1 = new DetalleFacturaDeVenta();

                    det1.setCodigo(articulo.getCodigo());//Colocarlo anull cuando le asignemo el encabezada

                    det1.setArticulo(articulo);
                    det1.setNumeroDeLinea(i++);

                    det1.setDescripcionArticulo(articulo.getDescripcion());

                    det1.setCantidad(1.00);
                    det1.setExistenciaActual(articulo.getExistencia());

                    det1.setPrecioVenta(articulo.getPrecioVenta());

                    det1.setSubTotal(subTotal(1.00, det1.getPrecioVenta()));
                    det1.setPorcientoDescuento(10.00);
                    det1.setPorcientoItbis(18.00);

                    det1.setTotalDescuento(totalDescuento(det1.getSubTotal(), det1.getPorcientoDescuento()));

                    det1.setTotalItbis(totalItbis(det1.getSubTotal(), det1.getTotalDescuento(), det1.getPorcientoItbis()));

                    det1.setTotal(total(det1.getSubTotal(), det1.getTotalDescuento(), det1.getTotalItbis()));

                    det1.setNombreAlmacen("General");
                    det1.setNombreUnidad("Unidad");

                    listDet.add(det1);
                    cambiosSinGuardar = true;

                    System.out.println("Articulo ingresó: " + articulo.getDescripcion());
//            
                    grid.getDataProvider().refreshAll(); // Refrescar el Grid sin perder la lista

                    cobrarComponent.setMontoACobrar(calcularTotal());

                }

                cobrarComponent.getBigMontoRecibido().focus();
            });

            dialog.open();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void cobrar() {

        CobroView dialog = new CobroView(12.99); // total simulado
        dialog.open();

    }

    public static boolean existeArticulo(List<DetalleFacturaDeVenta> lista, int art) {
        return lista.stream()
                .anyMatch(n -> n.getArticulo().getCodigo() == art);
    }

    // Componente Stepper personalizado
    public class CantidadStepper extends HorizontalLayout {

        private final DetalleFacturaDeVenta detFactLocal;
        private final Grid<DetalleFacturaDeVenta> grid;
        private final Span lblCantidad;

        public CantidadStepper(DetalleFacturaDeVenta detFact, Grid<DetalleFacturaDeVenta> grid) {
            this.detFactLocal = detFact;
            this.grid = grid;
            this.lblCantidad = new Span(String.valueOf(detFact.getCantidad()));
            setWidth("25");

            TextField txtCantidad = new TextField();
            Button btnMenos = new Button("➖", e -> disminuir());
            Button btnMas = new Button("➕", e -> aumentar());

            btnMas.setWidth("8px");
            btnMenos.setWidth("8px");
//            txtCantidad.setWidth("60px");
            HorizontalLayout hl = new HorizontalLayout();

            hl.setAlignItems(Alignment.END);
            hl.add(btnMenos, btnMas);
//            hl.setWidth("50%");
            hl.setSpacing(false);

            add(hl);
            setAlignItems(Alignment.CENTER);
            setPadding(false);

        }

        private void aumentar() {

            detFactLocal.setCantidad(detFactLocal.getCantidad() + 1);
            lblCantidad.setText(String.valueOf(detFactLocal.getCantidad()));

            detFactLocal.setSubTotal(subTotal(detFactLocal.getCantidad(), detFactLocal.getPrecioVenta()));

            detFactLocal.setTotalDescuento(totalDescuento(detFactLocal.getSubTotal(), detFactLocal.getPorcientoDescuento()));

            detFactLocal.setTotalItbis(totalItbis(detFactLocal.getSubTotal(), detFactLocal.getTotalDescuento(), 18.00));

            detFactLocal.setTotal(total(detFactLocal.getSubTotal(), detFactLocal.getTotalDescuento(), detFactLocal.getTotalItbis()));

            grid.getListDataView().refreshAll();
            txtBuscar.clear();
            filtroNombre.clear();

            grid.getDataProvider().refreshItem(detFactLocal);

            cobrarComponent.setMontoACobrar(calcularTotal());
        }

        private void disminuir() {

            if (detFactLocal.getCantidad() > 0) {

                detFactLocal.setCantidad(detFactLocal.getCantidad() - 1);
                lblCantidad.setText(String.valueOf(detFactLocal.getCantidad()));

                detFactLocal.setSubTotal(subTotal(detFactLocal.getCantidad(), detFactLocal.getPrecioVenta()));

                detFactLocal.setTotalDescuento(totalDescuento(detFactLocal.getSubTotal(), detFactLocal.getPorcientoDescuento()));

                detFactLocal.setTotalItbis(totalItbis(detFactLocal.getSubTotal(), detFactLocal.getTotalDescuento(), 18.00));

                detFactLocal.setTotal(total(detFactLocal.getSubTotal(), detFactLocal.getTotalDescuento(), detFactLocal.getTotalItbis()));

                grid.getListDataView().refreshAll();
                txtBuscar.clear();
                filtroNombre.clear();

                grid.getDataProvider().refreshItem(detFactLocal);

                cobrarComponent.setMontoACobrar(calcularTotal());
            }
        }
    }

    private BigDecimal calcularTotal() {

        Double totalVenta = 0.00;
        for (DetalleFacturaDeVenta det : listDet) {

            totalVenta += det.getSubTotal();
//            totalVenta += det.getTotal();
        }

        return new BigDecimal(totalVenta);
    }

    private void cancelar() {

        if (!listDet.isEmpty()) {

            ConfirmDialog dialog = new ConfirmDialog(
                    "¿Seguro que quiere cancelar la venta-> ",
                    () -> {

                        listDet.clear();
                        grid.getDataProvider().refreshAll();
                        txtBuscar.clear();
                        txtBuscar.focus();
                        cobrarComponent.limpiarMonto();

                    },
                    () -> {
                        txtBuscar.clear();
                    }
            );

            dialog.open();
        }

    }

    private void eliminarArticulo(DetalleFacturaDeVenta item) {

        ConfirmDialog dialog = new ConfirmDialog(
                "¿Seguro que quiere eliminar el articulo -> " + item.getDescripcionArticulo().toUpperCase(),
                () -> {

                    listDet.remove(item);
                    grid.getDataProvider().refreshAll();
                    if (!txtBuscar.isEmpty()) {

                        listDet.remove(item);
                        grid.getDataProvider().refreshAll();
                        txtBuscar.clear();

                    }

                    cobrarComponent.setMontoACobrar(calcularTotal());
                },
                () -> {
                    txtBuscar.clear();
                }
        );

        dialog.open();

    }
//
//    private void agregarProductoPorPeso(Producto producto, double peso) {
//
//        double total = producto.getPrecio() * peso;
//
//        Producto item = new Producto();
//        item.setId(producto.getId());
//        item.setNombre(producto.getNombre() + " (" + peso + " kg)");
//        item.setCantidad(peso); // aquí cantidad = peso
//        item.setPrecio(producto.getPrecio());
//        item.setTotal(total);
//
//        listaProductos.add(item);
//        grid.getDataProvider().refreshAll();
//
//        actualizarTotal();
//    }

}
