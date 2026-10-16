///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.maxsoft.application.view.inventario.articulo;
//
//import com.maxsoft.application.modelo.ArticuloEmpaque;
//import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
//import com.vaadin.flow.component.Key;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.formlayout.FormLayout;
//import com.vaadin.flow.component.html.H2;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.textfield.IntegerField;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import jakarta.annotation.security.PermitAll;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.Optional;
//
//@PermitAll
//@PageTitle("Gestion de Etiqueta")
//@Route(value = "inventario/gestion-etiquetas")
//public class GestionEtiquetasView extends VerticalLayout {
//
//    // Componentes de Búsqueda
//    private ComboBox<ArticuloEmpaque> buscadorArticuloCombo = new ComboBox<>("🔍 Buscar Artículo / Presentación");
//    private TextField escanerFiel = new TextField("🖨️ Probar Escáner de Barra");
//
//    // Componentes del Formulario de Gestión
//    private TextField descripcionFiel = new TextField("Descripción Completa");
//    private TextField codigoBarraFiel = new TextField("Código de Barra Actual");
//    private TextField precioVentaFiel = new TextField("Precio de Venta (RD$)");
//    private IntegerField cantidadCopias = new IntegerField("Cantidad de Etiquetas a Imprimir");
//
//    // Botones de Acción
//    private Button btnGenerarCodigo = new Button("Generar Código Interno Auto");
//    private Button btnImprimir = new Button("Imprimir Etiqueta [F4]", VaadinIcon.PRINT.create());
//    private Button btnLimpiar = new Button("Limpiar Formulario");
//
//    @Autowired
//    private ArticuloEmpaqueService articuloEmpaqueService;
//
//    // Guardará la entidad seleccionada en la sesión de la pantalla
//    private ArticuloEmpaque empaqueSeleccionado;
//
//    public GestionEtiquetasView() {
//        setSizeFull();
//        setPadding(true);
//        setSpacing(true);
//
//        // --- ENCABEZADO ---
//        H2 titulo = new H2("🏷️ Gestión de Códigos de Barra e Impresión de Etiquetas");
//        Span subtitulo = new Span("Cree códigos internos para los artículos sueltos e imprima los precios para las perchas.");
//        subtitulo.getStyle().set("color", "gray");
//
//        // --- CONFIGURACIÓN DE BUSCADORES ---
//        // Plan B/Buscador principal: Escribe y busca directo en la tabla articulo_empaque
//        buscadorArticuloCombo.setPlaceholder("Ej: Platano, Sopita, Salsa...");
//        buscadorArticuloCombo.setWidthFull();
//        buscadorArticuloCombo.setClearButtonVisible(true);
//
//        // REEMPLAZA EL COMPORTAMIENTO DEL COMBOBOX POR ESTO:
//        
////        buscadorArticuloCombo.setItemsWithFilterProvider(
////                // 1. Aquí Vaadin te pasa el texto que el colmadero va escribiendo
////                filterText -> articuloEmpaqueService.buscarPorDescripcionEfectivo(filterText),
////                // 2. Aquí Vaadin calcula la paginación de forma interna y automática
////                query -> query
////        );
//
//        buscadorArticuloCombo.setItems(query -> articuloEmpaqueService.buscarPorDescripcionEfectivo(query.getFilter().orElse("")).stream());
//
//        buscadorArticuloCombo.setItemLabelGenerator(ArticuloEmpaque::getDescripcionEmpaque); // Usa el campo descripción directo
//        buscadorArticuloCombo.addValueChangeListener(e -> cargarDatosEmpaque(e.getValue()));
//
//        // Campo auxiliar para probar si el escáner lee bien un código ya pegado
//        escanerFiel.setPlaceholder("Coloque el cursor aquí y escanee...");
//        escanerFiel.setWidthFull();
//        escanerFiel.addKeyPressListener(Key.ENTER, e -> procesarEscaneoFiel());
//
//        HorizontalLayout filaBusqueda = new HorizontalLayout(buscadorArticuloCombo, escanerFiel);
//        filaBusqueda.setWidthFull();
//
//        // --- CONFIGURACIÓN DEL FORMULARIO DE DETALLES ---
//        descripcionFiel.setReadOnly(true);
//        codigoBarraFiel.setPlaceholder("Falta código de barra");
//        precioVentaFiel.setReadOnly(true);
//
//        cantidadCopias.setValue(1);
//        cantidadCopias.setMin(1);
//        cantidadCopias.setMax(100);
////        cantidadCopias.setHasControls(true);
//
//        FormLayout formLayout = new FormLayout();
//        formLayout.add(descripcionFiel, codigoBarraFiel, precioVentaFiel, cantidadCopias);
//        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2)); // Dos columnas limpias
//
//        // --- CONFIGURACIÓN DE BOTONES ---
//        btnGenerarCodigo.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
//        btnGenerarCodigo.setEnabled(false);
//        btnGenerarCodigo.addClickListener(e -> generarCodigoInternoAutomatico());
//
//        btnImprimir.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
//        btnImprimir.setEnabled(false);
//        btnImprimir.addClickShortcut(Key.F4); // Atajo F4 instantáneo
//        btnImprimir.addClickListener(e -> mandarAImprimir());
//
//        btnLimpiar.addClickListener(e -> limpiarFormulario());
//
//        HorizontalLayout filaAcciones = new HorizontalLayout(btnGenerarCodigo, btnImprimir, btnLimpiar);
//        filaAcciones.setSpacing(true);
//
//        // Ensamblar todo en la vista
//        add(titulo, subtitulo, filaBusqueda, formLayout, filaAcciones);
//    }
//
//    // Carga la información de la base de datos a los campos de texto
//    private void cargarDatosEmpaque(ArticuloEmpaque empaque) {
//        this.empaqueSeleccionado = empaque;
//
//        if (empaque != null) {
//            descripcionFiel.setValue(empaque.getNombreEmpaque());
//            precioVentaFiel.setValue("RD$ " + empaque.getPrecioVenta());
//
//            if (empaque.getCodigoBarra() != null && !empaque.getCodigoBarra().trim().isEmpty()) {
//                codigoBarraFiel.setValue(empaque.getCodigoBarra());
//                codigoBarraFiel.setReadOnly(true);
//                btnGenerarCodigo.setEnabled(false);
//                btnImprimir.setEnabled(true);
//            } else {
//                codigoBarraFiel.setValue("");
//                codigoBarraFiel.setReadOnly(false); // Deja que el usuario escriba uno manualmente si quiere
//                codigoBarraFiel.setPlaceholder("⚠️ Sin código. Digite uno o genere uno automático abajo.");
//                btnGenerarCodigo.setEnabled(true);
//                btnImprimir.setEnabled(false);
//            }
//        } else {
//            limpiarFormulario();
//        }
//    }
//
//    // Si pasan el escáner en el campo de prueba, el sistema busca el artículo al instante
//    private void procesarEscaneoFiel() {
//
//        String codigo = escanerFiel.getValue().trim();
//        if (!codigo.isEmpty()) {
//            Optional<ArticuloEmpaque> empaqueOpt = articuloEmpaqueService.buscarPorCodigoBarra(codigo);
//            if (empaqueOpt.isPresent()) {
//                buscadorArticuloCombo.setValue(empaqueOpt.get()); // Lo selecciona en el combo y dispara la carga
//                escanerFiel.clear();
//            } else {
//                Notification n = Notification.show("❌ Código '" + codigo + "' no está registrado en el sistema.");
//                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
//                escanerFiel.clear();
//            }
//        }
//    }
//
//    // Lógica del PLAN B: Generar código de barra automático si el artículo vino suelto
//    private void generarCodigoInternoAutomatico() {
//        if (empaqueSeleccionado != null) {
//            // Genera un código estándar de 12 dígitos usando el prefijo interno '20' y el ID del artículo
//            String codigoGenerado = String.format("200000000%03d", empaqueSeleccionado.getArticulo());
//
//            // Guardamos directamente en la base de datos SQL
//            articuloEmpaqueService.actualizarCodigoBarra(empaqueSeleccionado.getCodigo(), codigoGenerado);
//
//            Notification n = Notification.show("✅ Código de barras interno generado con éxito para: "
//                    + empaqueSeleccionado.getNombreArticulo());
//
//            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//
//            // Recargamos el combo para refrescar los datos en pantalla
//            buscadorArticuloCombo.clear();
//        }
//    }
//
//    // Envía la orden de impresión al backend
//    private void mandarAImprimir() {
//        if (empaqueSeleccionado != null && cantidadCopias.getValue() != null) {
//            // Aquí el servicio invoca tus librerías de impresión (ej: JasperReports o comandos ESC/POS directos)
////            articuloEmpaqueService.imprimirEtiquetaTermica(empaqueSeleccionado, cantidadCopias.getValue());
//
//            Notification n = Notification.show("🖨️ Enviando " + cantidadCopias.getValue()
//                    + " etiqueta(s) de '" + empaqueSeleccionado.getNombreEmpaque() + "' a la ticketera.");
//
//            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//            limpiarFormulario();
//        }
//    }
//
//    private void limpiarFormulario() {
//        empaqueSeleccionado = null;
//        buscadorArticuloCombo.clear();
//        escanerFiel.clear();
//        descripcionFiel.clear();
//        codigoBarraFiel.clear();
//        precioVentaFiel.clear();
//        cantidadCopias.setValue(1);
//        btnGenerarCodigo.setEnabled(false);
//        btnImprimir.setEnabled(false);
//    }
//}


package com.maxsoft.application.view.inventario.articulo;

import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@PermitAll
@PageTitle("Gestion de Etiqueta")
@Route(value = "inventario/gestion-etiquetas")
public class GestionEtiquetasView extends VerticalLayout {

    // Componentes de Búsqueda
    private ComboBox<ArticuloEmpaque> buscadorArticuloCombo = new ComboBox<>("🔍 Buscar Artículo / Presentación");
    private TextField escanerFiel = new TextField("🖨️ Probar Escáner de Barra");

    // Componentes del Formulario de Gestión
    private TextField descripcionFiel = new TextField("Descripción Completa");
    private TextField codigoBarraFiel = new TextField("Código de Barra Actual");
    private TextField precioVentaFiel = new TextField("Precio de Venta (RD$)");
    private IntegerField cantidadCopias = new IntegerField("Cantidad de Etiquetas a Imprimir");

    // Botones de Acción
    private Button btnGenerarCodigo = new Button("Generar Código Interno Auto");
    private Button btnImprimir = new Button("Imprimir Etiqueta [F4]", VaadinIcon.PRINT.create());
    private Button btnLimpiar = new Button("Limpiar Formulario");

    @Autowired
    private ArticuloEmpaqueService articuloEmpaqueService;

    // Guardará la entidad seleccionada en la sesión de la pantalla
    private ArticuloEmpaque empaqueSeleccionado;

    public GestionEtiquetasView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // --- ENCABEZADO ---
        H2 titulo = new H2("🏷️ Gestión de Códigos de Barra e Impresión de Etiquetas");
        Span subtitulo = new Span("Cree códigos internos para los artículos sueltos e imprima los precios para las perchas.");
        subtitulo.getStyle().set("color", "gray");

        // --- CONFIGURACIÓN DE BUSCADORES ---
        buscadorArticuloCombo.setPlaceholder("Ej: Platano, Sopita, Salsa...");
        buscadorArticuloCombo.setWidthFull();
        buscadorArticuloCombo.setClearButtonVisible(true);
        
        // CORRECCIÓN: Uso de Callback con paginación integrada (offset y limit)
        buscadorArticuloCombo.setItemsWithFilterConverter(
            query -> {
                int offset = query.getOffset();
                int limit = query.getLimit();
                String filtro = query.getFilter().orElse("");
                
                // Llama al nuevo método paginado de tu servicio
                return articuloEmpaqueService.buscarPorDescripcionEfectivoPaginado(filtro, offset, limit).stream();
            },
            filter -> filter
        );
        
        buscadorArticuloCombo.setItemLabelGenerator(ArticuloEmpaque::getNombreArticulo); 
        buscadorArticuloCombo.addValueChangeListener(e -> cargarDatosEmpaque(e.getValue()));

        // Campo auxiliar para probar si el escáner lee bien un código ya pegado
        escanerFiel.setPlaceholder("Coloque el cursor aquí y escanee...");
        escanerFiel.setWidthFull();
        escanerFiel.addKeyPressListener(Key.ENTER, e -> procesarEscaneoFiel());

        HorizontalLayout filaBusqueda = new HorizontalLayout(buscadorArticuloCombo, escanerFiel);
        filaBusqueda.setWidthFull();

        // --- CONFIGURACIÓN DEL FORMULARIO DE DETALLES ---
        descripcionFiel.setReadOnly(true);
        codigoBarraFiel.setPlaceholder("Falta código de barra");
        precioVentaFiel.setReadOnly(true);
        
        cantidadCopias.setValue(1);
        cantidadCopias.setMin(1);
        cantidadCopias.setMax(100);

        FormLayout formLayout = new FormLayout();
        formLayout.add(descripcionFiel, codigoBarraFiel, precioVentaFiel, cantidadCopias);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2)); 

        // --- CONFIGURACIÓN DE BOTONES ---
        btnGenerarCodigo.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnGenerarCodigo.setEnabled(false);
        btnGenerarCodigo.addClickListener(e -> generarCodigoInternoAutomatico());

        btnImprimir.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnImprimir.setEnabled(false);
        btnImprimir.addClickShortcut(Key.F4); 
        btnImprimir.addClickListener(e -> mandarAImprimir());

        btnLimpiar.addClickListener(e -> limpiarFormulario());

        HorizontalLayout filaAcciones = new HorizontalLayout(btnGenerarCodigo, btnImprimir, btnLimpiar);
        filaAcciones.setSpacing(true);

        // Ensamblar todo en la vista
        add(titulo, subtitulo, filaBusqueda, formLayout, filaAcciones);
    }

    // Carga la información de la base de datos a los campos de texto
    private void cargarDatosEmpaque(ArticuloEmpaque empaque) {
        
        this.empaqueSeleccionado = empaque;

        if (empaque != null) {
            // NOTA: Asegúrate de si es getNombreEmpaque() o getDescripcionEmpaque() según tu entidad
            descripcionFiel.setValue(empaque.getDescripcionEmpaque() != null ? empaque.getDescripcionEmpaque() : "");
            precioVentaFiel.setValue("RD$ " + empaque.getPrecioVenta());
            
            if (empaque.getCodigoBarra() != null && !empaque.getCodigoBarra().trim().isEmpty()) {
                codigoBarraFiel.setValue(empaque.getCodigoBarra());
                codigoBarraFiel.setReadOnly(true);
                btnGenerarCodigo.setEnabled(false);
                btnImprimir.setEnabled(true);
            } else {
                codigoBarraFiel.setValue("");
                codigoBarraFiel.setReadOnly(false); 
                codigoBarraFiel.setPlaceholder("⚠️ Sin código. Digite uno o genere uno automático abajo.");
                btnGenerarCodigo.setEnabled(true);
                btnImprimir.setEnabled(false);
            }
        } else {
            limpiarFormulario();
        }
    }

    // Si pasan el escáner en el campo de prueba, el sistema busca el artículo al instante
    private void procesarEscaneoFiel() {
        String codigo = escanerFiel.getValue().trim();
        if (!codigo.isEmpty()) {
            Optional<ArticuloEmpaque> empaqueOpt = articuloEmpaqueService.buscarPorCodigoBarra(codigo);
            if (empaqueOpt.isPresent()) {
                buscadorArticuloCombo.setValue(empaqueOpt.get()); 
                escanerFiel.clear();
            } else {
                Notification n = Notification.show("❌ Código '" + codigo + "' no está registrado en el sistema.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                escanerFiel.clear();
            }
        }
    }

    // Lógica del PLAN B: Generar código de barra automático si el artículo vino suelto
    private void generarCodigoInternoAutomatico() {
        if (empaqueSeleccionado != null) {
            String codigoGenerado = String.format("200000000%03d", empaqueSeleccionado.getArticulo());
            articuloEmpaqueService.actualizarCodigoBarra(empaqueSeleccionado.getCodigo(), codigoGenerado);
            
            Notification n = Notification.show("✅ Código de barras interno generado con éxito: " + codigoGenerado);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Recargar datos actualizados
            empaqueSeleccionado.setCodigoBarra(codigoGenerado);
            cargarDatosEmpaque(empaqueSeleccionado);
        }
    }

    private void mandarAImprimir() {
        Notification.show("🖨️ Mandando a imprimir " + cantidadCopias.getValue() + " copia(s)...");
    }

    private void limpiarFormulario() {
        this.empaqueSeleccionado = null;
        buscadorArticuloCombo.clear();
        escanerFiel.clear();
        descripcionFiel.clear();
        codigoBarraFiel.clear();
        codigoBarraFiel.setPlaceholder("");
        precioVentaFiel.clear();
        cantidadCopias.setValue(1);
        btnGenerarCodigo.setEnabled(false);
        btnImprimir.setEnabled(false);
    }
}
