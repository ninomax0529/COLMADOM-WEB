/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.ControlDeFundaVacia;
import com.example.application.modelo.DetalleControlDeFundaVacia;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface ControlFundaVaciaService {
    
     public ControlDeFundaVacia guardar(ControlDeFundaVacia control);

    public List<ControlDeFundaVacia> getLista();

    public ControlDeFundaVacia getControlDeFundaVacia(int codigo);

    public ControlDeFundaVacia getControlDeFundaVacia(Date fecha);

    public List<DetalleControlDeFundaVacia> getDetalle(int control);
}
