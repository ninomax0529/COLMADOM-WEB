/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.CementoEmpacadoPorsiloRepo;
import com.example.application.modelo.CementoEmpacadoPorSilo;
import com.example.application.modelo.DetalleCementoEmpacadoPorSilo;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CementoEmcadoPorSiloServiceImpl implements CementoEmcadoPorSiloService {

    @Autowired
    CementoEmpacadoPorsiloRepo cementoEmcadoRepo;

    @Override
    public List<CementoEmpacadoPorSilo> getLista() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public List<DetalleCementoEmpacadoPorSilo> getDetalle(int cementoEmcado) {
       
     
        List<DetalleCementoEmpacadoPorSilo> lista = null;

        try {

            lista = cementoEmcadoRepo.getDetalle(cementoEmcado);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
        
    }

    @Override
    public CementoEmpacadoPorSilo guardar(CementoEmpacadoPorSilo cementoEmpacadoArg) {
       
        
        CementoEmpacadoPorSilo cementoEmcado = cementoEmpacadoArg;
        try {

            cementoEmcado = cementoEmcadoRepo.save(cementoEmpacadoArg);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cementoEmcado;
    }

    @Override
    public CementoEmpacadoPorSilo getCementoEmcadoPorSilo(int codigo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CementoEmpacadoPorSilo getCementoEmcadoPorSilo(Date fecha) {
     
        CementoEmpacadoPorSilo cementoEmp;
     

        cementoEmp = this.cementoEmcadoRepo.getCementoEmcadoPorSilo(fecha);


        return cementoEmp;
    }
    
}
