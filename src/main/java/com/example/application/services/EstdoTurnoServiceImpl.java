/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.EstadoTurnoRepo;
import com.example.application.modelo.EstadoTurno;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EstdoTurnoServiceImpl implements EstdoTurnoService {

    @Autowired
    EstadoTurnoRepo estadoTurno;

    @Override
    public List<EstadoTurno> getLista() {

        List<EstadoTurno> lista = null;

        try {

            lista = estadoTurno.findAll();

        } catch (Exception e) {
            System.out.println("Erro : " + e.getMessage());
        }

        return lista;
    }

    @Override
    public EstadoTurno getEstadoTurno(int codigo) {

        Optional<EstadoTurno> equipo = this.estadoTurno.findById(codigo);

        return equipo.get();

    }

}
