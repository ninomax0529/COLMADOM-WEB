/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.AlmacenRepo;
import com.example.application.modelo.Almacen;
import com.example.application.modelo.Equipo;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlmacenServiceImpl implements AlmacenService {

    @Autowired
    AlmacenRepo almacenRepo;

    @Override
    public List<Almacen> getLista() {

        List<Almacen> lista = null;

        try {

            lista = almacenRepo.findAll();

        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }

        return lista;
    }

    @Override
    public List<Almacen> getLista(int tipo) {

        List<Almacen> lista = null;

        try {

            lista = almacenRepo.getLista(tipo);

        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Almacen getAlmacen(int codigoalm) {

        Optional<Almacen> almacen = this.almacenRepo.findById(codigoalm);

        return almacen.get();
    }

}
