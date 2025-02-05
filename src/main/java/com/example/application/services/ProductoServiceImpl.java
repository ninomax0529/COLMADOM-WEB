/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.ProductoRepo;
import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.Producto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    ProductoRepo productoRepo;

    @Override
    public List<Producto> getLista() {

        List<Producto> lista = null;

        try {

            lista = productoRepo.findAll();

        } catch (Exception e) {
            System.out.println("Erro : " + e.getMessage());
        }

        return lista;
    }

    @Override
    public List<DetalleMovimientoProducto> getListaMov() {

        List<DetalleMovimientoProducto> listaMov = new ArrayList<>();

        DetalleMovimientoProducto detaMov;

        for (Producto p : this.getLista()) {

            detaMov = new DetalleMovimientoProducto();

            detaMov.setProducto(p);
            detaMov.setNombreProducto(p.getDescripcion());
            detaMov.setDespacho(0);
            detaMov.setInventarioFinal(0);
            detaMov.setInventarioFinalSap(0);
            detaMov.setInventarioInicial(0);
            detaMov.setInventarioInicialSap(0);
            detaMov.setProducto(p);
            listaMov.add(detaMov);

        }

        return listaMov;

    }

}
