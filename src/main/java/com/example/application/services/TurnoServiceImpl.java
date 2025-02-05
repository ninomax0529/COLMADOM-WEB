/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.TurnoRepo;
import com.example.application.modelo.Turno;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TurnoServiceImpl implements TurnoService {

    @Autowired
    TurnoRepo turnoRepo;
    @Override
    public List<Turno>  getLista() {

        List<Turno> lista = null;

        try {
            
            lista=turnoRepo.findAll();

        } catch (Exception e) {
            System.out.println("Erro : "+e.getMessage());
        }

        return lista;
    }
}
