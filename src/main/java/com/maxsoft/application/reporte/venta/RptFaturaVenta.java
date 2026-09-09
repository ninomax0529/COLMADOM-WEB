/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.reporte.venta;

import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperRunManager;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Maximiliano
 */
public class RptFaturaVenta {

    public RptFaturaVenta() {
    }

    public StreamResource rptFacturaVenta(int factura, Connection dataSource) {

        StreamResource pdfResource = null;

        try {

            InputStream reportStream = getClass()
                    .getResourceAsStream("/reporte/venta/FacturaColmadoContinua.jasper");

            String subreportPath = new ClassPathResource("/reporte/venta/").getFile().getAbsolutePath();

            if (reportStream == null) {
                throw new RuntimeException("El archivo de reporte no se encontró en la ruta .");
//                return pdfResource;

            }

//            String subreportPath = new ClassPathResource("/reporte/EntradaMateriales/").getFile().getAbsolutePath();
            System.out.println("reportStream : " + reportStream);
            // Parámetros para el informe
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("factura", factura);
            parameters.put("SUBREPORT_DIR", subreportPath + "/");

            System.out.println("jasperPrint : ");

//             Exportar el informe a PDF
            System.out.println(" dataSource.getConnection() : " + dataSource);
            byte[] pdfContent = JasperRunManager.runReportToPdf(reportStream, parameters, dataSource);

            pdfResource = new StreamResource("informe.pdf", () -> new ByteArrayInputStream(pdfContent));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pdfResource;
    }

    public JasperPrint getJasperPrintFacturaVenta(int factura, Connection dataSource) {
        try {
            InputStream reportStream = getClass()
                    .getResourceAsStream("/reporte/venta/FacturaColmadoContinua.jasper");

            if (reportStream == null) {
                throw new RuntimeException("El archivo de reporte no se encontró en la ruta especificada.");
            }

            String subreportPath = new ClassPathResource("/reporte/venta/").getFile().getAbsolutePath();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("factura", factura);
            parameters.put("SUBREPORT_DIR", subreportPath + "/");

            // Genera directamente el objeto JasperPrint listo para la impresora
            return JasperFillManager.fillReport(reportStream, parameters, dataSource);

        } catch (Exception e) {
            throw new RuntimeException("Error al llenar el reporte JasperPrint para la factura: " + factura, e);
        }
    }

}
