/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.ProyeccionDeVentaRepo;
import com.example.application.modelo.DetalleProyeccionDeVenta;
import com.example.application.modelo.ProyecconDeVenta;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProyeccionDeVentaServiceImpl implements ProyeccionDeVentaService {

    @Autowired
    ProyeccionDeVentaRepo proyeccionDeVentaRepo;

    @Override
    public ProyecconDeVenta guardar(ProyecconDeVenta obj) {

        ProyecconDeVenta controlFdVacia = obj;
        try {

            controlFdVacia = proyeccionDeVentaRepo.save(obj);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return controlFdVacia;
    }

    @Override
    public List<ProyecconDeVenta> getLista() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ProyecconDeVenta getControlDeFundaVacia(int codigo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ProyecconDeVenta getProyecconDeVenta(Date fecha) {

        ProyecconDeVenta controlFda;

        controlFda = this.proyeccionDeVentaRepo.getProyecconDeVenta(fecha);

        return controlFda;
    }

    @Override
    public List<DetalleProyeccionDeVenta> getDetalle(int obj) {

        List<DetalleProyeccionDeVenta> lista = null;

        try {

            lista = this.proyeccionDeVentaRepo.getDetalle(obj);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;

    }

}
