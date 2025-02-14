/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.CementoDejadoEnPiso;
import com.example.application.modelo.DetalleCementoDejadoEnPiso;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface InventarioDeProductoService {

    public CementoDejadoEnPiso guardar(CementoDejadoEnPiso obj);

    public List<CementoDejadoEnPiso> getLista();

    public CementoDejadoEnPiso getCementoDejadoEnPiso(int codigo);

    public CementoDejadoEnPiso getCementoDejadoEnPiso(Date fecha);

    public List<DetalleCementoDejadoEnPiso> getDetalle(int obj);

    public Integer getInvAyer(Date fecha, Integer produc);
}
