/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.reporte;

import com.maxsoft.application.reporte.venta.RptFaturaVenta;
import com.maxsoft.application.servicio.interfaces.reporte.ReporteService;
import com.vaadin.flow.server.StreamResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final DataSource dataSource;

    public ReporteServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public StreamResource generarReporteFacturaVenta(int facturaCodigo) {
        try (Connection conn = dataSource.getConnection()) {
            RptFaturaVenta rptP = new RptFaturaVenta();
            return rptP.rptFacturaVenta(facturaCodigo, conn);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte de la factura: " + facturaCodigo, e);
        }
    }

    @Override
    public JasperPrint generarJasperPrintFacturaVenta(int facturaCodigo) {
        try (Connection conn = dataSource.getConnection()) {
            RptFaturaVenta rptP = new RptFaturaVenta();
            return rptP.getJasperPrintFacturaVenta(facturaCodigo, conn);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener JasperPrint: " + facturaCodigo, e);
        }
    }
}
