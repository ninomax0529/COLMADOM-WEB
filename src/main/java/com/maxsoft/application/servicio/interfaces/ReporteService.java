/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces;

import com.vaadin.flow.server.StreamResource;
import net.sf.jasperreports.engine.JasperPrint;

public interface ReporteService {
    
    StreamResource generarReporteFacturaVenta(int facturaCodigo);

    // Nuevo método para obtener el JasperPrint necesario para la impresión directa en servidor
    JasperPrint generarJasperPrintFacturaVenta(int facturaCodigo);
}