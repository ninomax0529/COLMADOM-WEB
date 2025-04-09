/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.view.inventario;

/**
 *
 * @author maximilianoalmonte
 */


import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Route(value = "inventario/gestionArticulo")

@UIScope
@Component
public class GestionArticulosView extends VerticalLayout {
    
    private List<Articulo> articulos = new ArrayList<>();
    private Grid<Articulo> gridInventario;
    private Grid<Articulo> gridVentas;
    private Grid<Articulo> gridCompras;
    private Binder<Articulo> binder;
    private Articulo articuloActual;
    
    public GestionArticulosView() {
        // Crear Tabs
        Tab tabInventario = new Tab("Datos de Inventario");
        Tab tabVentas = new Tab("Datos de Ventas");
        Tab tabCompras = new Tab("Datos de Compras");
        
        Tabs tabs = new Tabs(tabInventario, tabVentas, tabCompras);
        
        // Datos de Inventario
        gridInventario = new Grid<>(Articulo.class);
        gridInventario.setItems(articulos);
        
        // Datos de Ventas
        gridVentas = new Grid<>(Articulo.class);
        gridVentas.setItems(articulos);
        
        // Datos de Compras
        gridCompras = new Grid<>(Articulo.class);
        gridCompras.setItems(articulos);
        
        // Formulario de edición
        TextField nombreField = new TextField("Nombre");
        TextField cantidadField = new TextField("Cantidad");
        TextField precioField = new TextField("Precio");
        Button guardarButton = new Button("Guardar", event -> guardarArticulo());
        
        binder = new Binder<>(Articulo.class);
        binder.bind(nombreField, Articulo::getNombre, Articulo::setNombre);
        binder.bind(cantidadField, articulo -> String.valueOf(articulo.getCantidad()), (articulo, value) -> articulo.setCantidad(Integer.parseInt(value)));
        binder.bind(precioField, articulo -> String.valueOf(articulo.getPrecio()), (articulo, value) -> articulo.setPrecio(Double.parseDouble(value)));
        
        VerticalLayout formLayout = new VerticalLayout(nombreField, cantidadField, precioField, guardarButton);
        
        // Contenidos de cada Tab
        VerticalLayout inventarioLayout = new VerticalLayout(gridInventario, formLayout);
        VerticalLayout ventasLayout = new VerticalLayout(gridVentas);
        VerticalLayout comprasLayout = new VerticalLayout(gridCompras);
        
        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Datos de Inventario", inventarioLayout);
        tabSheet.add("Datos de Ventas", ventasLayout);
        tabSheet.add("Datos de Compras", comprasLayout);
        
        add(tabs, tabSheet);
    }
    
    private void guardarArticulo() {
        if (articuloActual == null) {
            articuloActual = new Articulo();
        }
        try {
            binder.writeBean(articuloActual);
            articulos.add(articuloActual);
            gridInventario.setItems(articulos);
            gridVentas.setItems(articulos);
            gridCompras.setItems(articulos);
            articuloActual = new Articulo();
            binder.readBean(articuloActual);
            Notification.show("Artículo guardado correctamente");
        } catch (ValidationException e) {
            Notification.show("Error al guardar el artículo");
        }
    }
    
    public static class Articulo {
        private String nombre;
        private int cantidad;
        private double precio;
        
        public Articulo() {}
        
        public Articulo(String nombre, int cantidad, double precio) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
        }
        
        public String getNombre() {
            return nombre;
        }
        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
        public int getCantidad() {
            return cantidad;
        }
        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }
        public double getPrecio() {
            return precio;
        }
        public void setPrecio(double precio) {
            this.precio = precio;
        }
    }
}
