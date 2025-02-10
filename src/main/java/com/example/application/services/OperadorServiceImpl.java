/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.OperadorRepo;
import com.example.application.modelo.Operador;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperadorServiceImpl implements OperadorService {

    @Autowired
    OperadorRepo OperadorRepo;

    @Override
    public List<Operador> getLista() {

        List<Operador> lista = null;

        try {

            lista = OperadorRepo.findAll();

        } catch (Exception e) {
            System.out.println("Erro : " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Operador getOperador(int codigo) {

        Operador obj = null;
        try {

            obj = this.OperadorRepo.findById(codigo).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

}
