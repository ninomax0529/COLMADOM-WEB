/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.Equipo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.application.data.EquipoRepo;
import java.util.Optional;

@Service
public class EquipoServiceImpl implements EquipoService {

    @Autowired
    EquipoRepo empacadoraRepo;

    @Override
    public List<Equipo> getLista() {

        List<Equipo> lista = null;

        try {

            lista = empacadoraRepo.findAll();

        } catch (Exception e) {
            System.out.println("Erro : " + e.getMessage());
        }

        return lista;
    }

    @Override
    public List<Equipo> getLista(int tipoEquipo) {
      
        
        List<Equipo> lista = null;

        try {

            lista = empacadoraRepo.getLista(tipoEquipo);

        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Equipo getEquipo(int codigoEquipo) {
        
        Optional<Equipo> equipo=this.empacadoraRepo.findById(codigoEquipo);
        
        return equipo.get();
       
    }
 

}
