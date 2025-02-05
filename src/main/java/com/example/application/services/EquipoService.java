/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.Equipo;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface EquipoService {

    public List<Equipo> getLista();

    public List<Equipo> getLista(int tipoEquipo);

    public Equipo getEquipo(int equipo);
}
