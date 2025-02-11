/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.ControlFundaVaciaRepo;
import com.example.application.modelo.ControlDeFundaVacia;
import com.example.application.modelo.DetalleControlDeFundaVacia;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ControlFundaVaciaServiceImpl implements ControlFundaVaciaService {

    @Autowired
    ControlFundaVaciaRepo controlRepo;
    
    @Override
    public ControlDeFundaVacia guardar(ControlDeFundaVacia controlFunda) {
      
        
        
        ControlDeFundaVacia controlFdVacia = controlFunda;
        try {

            controlFdVacia = controlRepo.save(controlFunda);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return controlFdVacia;
    }

    @Override
    public List<ControlDeFundaVacia> getLista() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ControlDeFundaVacia getControlDeFundaVacia(int codigo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ControlDeFundaVacia getControlDeFundaVacia(Date fecha) {
      
        
        ControlDeFundaVacia controlFda;
     

        controlFda = this.controlRepo.getControlDeFundaVacia(fecha);


        return controlFda;
    }

    @Override
    public List<DetalleControlDeFundaVacia> getDetalle(int control) {
        
             
        List<DetalleControlDeFundaVacia> lista = null;

        try {

            lista =this.controlRepo.getDetalle(control);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
        
    }
    
}
