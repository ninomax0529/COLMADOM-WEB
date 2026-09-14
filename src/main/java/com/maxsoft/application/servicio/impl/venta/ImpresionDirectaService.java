/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

@Service
public class ImpresionDirectaService {

    public void imprimirJasperDirecto(JasperPrint jasperPrint, String nombreImpresora) throws Exception {
        PrintService printService = buscarImpresora(nombreImpresora);
        
        if (printService == null) {
            printService = PrintServiceLookup.lookupDefaultPrintService();
        }

        if (printService == null) {
            throw new RuntimeException("No se encontró ninguna impresora disponible.");
        }

        // Exporta directamente al driver de impresión sin mostrar pantalla emergente
        JasperPrintManager.printReport(jasperPrint, false);
    }

    private PrintService buscarImpresora(String nombreImpresora) {
        if (nombreImpresora == null || nombreImpresora.isEmpty()) return null;
        
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : printServices) {
            if (service.getName().equalsIgnoreCase(nombreImpresora)) {
                return service;
            }
        }
        return null;
    }
}