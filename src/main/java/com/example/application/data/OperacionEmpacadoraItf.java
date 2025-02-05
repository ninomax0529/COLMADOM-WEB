/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

import com.example.application.modelo.DetalleOperacionEmpacadora;
import com.example.application.modelo.OperacionEmpacadora;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Maximiliano
 */
@Repository
public interface OperacionEmpacadoraItf {
    
      public List<DetalleOperacionEmpacadora> getDetalleOperacion(OperacionEmpacadora op);
}
