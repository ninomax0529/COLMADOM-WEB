/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.DetalleProyeccionDeVenta;
import com.example.application.modelo.ProyecconDeVenta;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface ProyeccionDeVentaService {

    public ProyecconDeVenta guardar(ProyecconDeVenta control);

    public List<ProyecconDeVenta> getLista();

    public ProyecconDeVenta getControlDeFundaVacia(int codigo);

    public ProyecconDeVenta getProyecconDeVenta(Date fecha);

    public List<DetalleProyeccionDeVenta> getDetalle(int obj);
}
