/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.DetalleOperacionEmpacadora;
import com.example.application.modelo.OperacionEmpacadora;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface OperacionEmpacadoraService {

    public OperacionEmpacadora guardar(OperacionEmpacadora opem);

    public List<OperacionEmpacadora> getLista();

    public List<DetalleOperacionEmpacadora> getDetalleOperacion(OperacionEmpacadora op);

    public List<DetalleMovimientoProducto> getDetalleMovimientoProducto(OperacionEmpacadora op);

    public List<DetalleOperacionEmpacadora> getDetalleOperacion(OperacionEmpacadora op, int empacadora, int producto);

    public OperacionEmpacadora getOperacionEmpacadora(int turno, Date fecha);

    public OperacionEmpacadora getOperacionEmpacadora(int turno, int estado, Date fecha);

    public Double getEmpacado(int turno, Date fecha, int product);

    public Double getEmpacado(Date fecha, int product);

}
