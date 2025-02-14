/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.DetalleResumenDespacho;
import com.example.application.modelo.ResumenDespacho;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface ResumenDespachoService {
    
    
    public ResumenDespacho guardar(ResumenDespacho obj);

    public List<ResumenDespacho> getLista();

    public ResumenDespacho getResumenDespacho(int codigo);

    public ResumenDespacho getResumenDespacho(Date fecha);

    public List<DetalleResumenDespacho> getDetalle(int obj);

    public Integer getInvAyer(Date fecha, Integer produc);
}
