/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.InventarioDeProductoRepo;
import com.example.application.modelo.CementoDejadoEnPiso;
import com.example.application.modelo.DetalleCementoDejadoEnPiso;
import com.example.application.util.ClaseUtil;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventarioDeProductoServiceImpl implements InventarioDeProductoService {

    @Autowired
    InventarioDeProductoRepo inventarioDeProductoRepo;

    @Override
    public CementoDejadoEnPiso guardar(CementoDejadoEnPiso obj) {

        CementoDejadoEnPiso cementoEmcado = obj;
        try {

            cementoEmcado = inventarioDeProductoRepo.save(cementoEmcado);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cementoEmcado;

    }

    @Override
    public List<CementoDejadoEnPiso> getLista() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CementoDejadoEnPiso getCementoDejadoEnPiso(int codigo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CementoDejadoEnPiso getCementoDejadoEnPiso(Date fecha) {

        CementoDejadoEnPiso obj;

        obj = this.inventarioDeProductoRepo.getCementoDejadoEnPiso(fecha);

        return obj;

    }

    @Override
    public List<DetalleCementoDejadoEnPiso> getDetalle(int obj) {

        List<DetalleCementoDejadoEnPiso> lista = null;

        try {

            lista = inventarioDeProductoRepo.getDetalle(obj);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    @Override
    public Integer getInvAyer(Date fecha, Integer produc) {

        Date fechaAyer = ClaseUtil.fechaAyer(fecha);
        Integer inventario = this.inventarioDeProductoRepo.getInvAyer(fechaAyer, produc);

        return inventario==null ? 0 : inventario;
    }

}
