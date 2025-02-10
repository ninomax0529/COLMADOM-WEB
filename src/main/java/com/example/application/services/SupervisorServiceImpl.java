/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.SupervisorRepo;
import com.example.application.modelo.Supervisor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupervisorServiceImpl implements SupervisorService {

    @Autowired
    SupervisorRepo supervisorRepo;
    @Override
    public List<Supervisor> getLista() {

        List<Supervisor> lista = null;

        try {
            
            lista=supervisorRepo.findAll();

        } catch (Exception e) {
            System.out.println("Erro : "+e.getMessage());
        }

        return lista;
    }

    @Override
    public Supervisor getSupervisor(int codigo) {
      
        
        Supervisor obj = null;
        try {

            obj = this.supervisorRepo.findById(codigo).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }
    
}
