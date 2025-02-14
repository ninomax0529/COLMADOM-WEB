/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.ResumenDespachoRepo;
import com.example.application.modelo.DetalleResumenDespacho;
import com.example.application.modelo.ResumenDespacho;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumenDespachoServiceImpl implements ResumenDespachoService {

    @Autowired
    ResumenDespachoRepo resumenRepo;

    @Override
    public ResumenDespacho guardar(ResumenDespacho obj) {

        ResumenDespacho resumenDesp = obj;
        try {

            resumenDesp = resumenRepo.save(resumenDesp);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resumenDesp;
    }

    @Override
    public List<ResumenDespacho> getLista() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ResumenDespacho getResumenDespacho(int codigo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ResumenDespacho getResumenDespacho(Date fecha) {
       
        
        ResumenDespacho cementoEmp;
     

        cementoEmp = this.resumenRepo.getResumenDespacho(fecha);


        return cementoEmp;
    }

    @Override
    public List<DetalleResumenDespacho> getDetalle(int obj) {

        List<DetalleResumenDespacho> lista = null;

        try {

            lista = this.resumenRepo.getDetalle(obj);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;

    }

    @Override
    public Integer getInvAyer(Date fecha, Integer produc) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
