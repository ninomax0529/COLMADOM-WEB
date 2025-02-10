/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.Operador;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface OperadorService {

    public List<Operador> getLista();

    public Operador getOperador(int codigo);
}
