/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import java.util.Date;

/**
 *
 * @author Maximiliano
 */
public interface DespachoService {

    public Double getDespacho(String producto, Date fecha, int turno);

    public Double getDespacho(String producto, Date fecha);

    public Double getDespacho(String producto, Date fi, Date ff);

    public Double getDespacho(String producto, Date fi, Date ff, int turno);

    public Double getDespacho(String producto);

    public Double getDespacho(String turno, String producto, Date fecha);
}
