package com.maxsoft.application.reporte.inventario;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Empresa;
import com.maxsoft.application.modelo.Merma;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Comprobante de merma en PDF (carta) para la firma del encargado: datos de
 * la merma, artículos con su valor de pérdida y espacio para firmas. Si está
 * anulada lo dice arriba con el motivo.
 */
public final class ComprobanteMermaPdf {

    private ComprobanteMermaPdf() {
    }

    public static byte[] generar(Merma merma, List<DetalleMerma> detalles, Empresa empresa) {

        DecimalFormat numero = new DecimalFormat("#,##0.00");
        SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat fechaHora = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(salida));
                Document doc = new Document(pdf, PageSize.LETTER)) {

            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont negrita = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            doc.setFont(normal).setFontSize(10);
            doc.setMargins(36, 36, 36, 36);

            // Encabezado del negocio
            if (empresa != null) {
                doc.add(new Paragraph(texto(empresa.getNombre())).setFont(negrita).setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
                String linea = texto(empresa.getDiereccion())
                        + (empresa.getTelefono() != null ? "  ·  Tel. " + empresa.getTelefono() : "");
                doc.add(new Paragraph(linea).setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(0));
            }

            doc.add(new Paragraph("COMPROBANTE DE MERMA #" + merma.getCodigo()).setFont(negrita).setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(8));

            if (merma.isAnulada()) {
                String anulada = "ANULADA"
                        + (merma.getFechaAnulacion() != null ? " el " + fechaHora.format(merma.getFechaAnulacion()) : "")
                        + (merma.getUsuarioAnulacion() != null ? " por " + merma.getUsuarioAnulacion() : "")
                        + (merma.getMotivoAnulacion() != null ? ". Motivo: " + merma.getMotivoAnulacion() : "");
                doc.add(new Paragraph(anulada).setFont(negrita).setFontColor(ColorConstants.RED)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // Datos de la merma
            Table datos = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2})).useAllAvailableWidth()
                    .setMarginTop(6);
            dato(datos, "Fecha:", merma.getFecha() != null ? fecha.format(merma.getFecha()) : "", negrita);
            dato(datos, "Registrada:", merma.getFechaRegistro() != null ? fechaHora.format(merma.getFechaRegistro()) : "", negrita);
            dato(datos, "Almacén:", merma.getAlmacen() != null ? merma.getAlmacen().getNombre() : "", negrita);
            dato(datos, "Tipo:", merma.getTipoMerma() != null ? merma.getTipoMerma().getDescripcion() : "", negrita);
            dato(datos, "Usuario:", texto(merma.getNombreUsuario()), negrita);
            dato(datos, "Salida N°:", merma.getSalidaInventario() != null
                    ? String.valueOf(merma.getSalidaInventario().getCodigo()) : "-", negrita);
            doc.add(datos);
            if (merma.getObservacion() != null && !merma.getObservacion().isBlank()) {
                doc.add(new Paragraph().add(new com.itextpdf.layout.element.Text("Observación: ").setFont(negrita))
                        .add(merma.getObservacion()).setMarginTop(4));
            }

            // Artículos
            Table tabla = new Table(UnitValue.createPercentArray(new float[]{5, 2.2f, 1.4f, 1.6f, 1.6f, 1.8f}))
                    .useAllAvailableWidth().setMarginTop(10);
            for (String titulo : new String[]{"Artículo", "Presentación", "Cantidad", "Unid. base", "Costo", "Valor pérdida"}) {
                tabla.addHeaderCell(new Cell().add(new Paragraph(titulo).setFont(negrita))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(titulo.equals("Artículo") || titulo.equals("Presentación")
                                ? TextAlignment.LEFT : TextAlignment.RIGHT));
            }
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal totalBase = BigDecimal.ZERO;
            for (DetalleMerma d : detalles) {
                BigDecimal cantidad = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
                BigDecimal factor = d.getFactorConversion() != null && d.getFactorConversion().signum() > 0
                        ? d.getFactorConversion() : BigDecimal.ONE;
                BigDecimal costo = d.getCostoUnitario() != null ? d.getCostoUnitario() : BigDecimal.ZERO;
                BigDecimal valor = cantidad.multiply(costo);
                BigDecimal base = cantidad.multiply(factor);
                total = total.add(valor);
                totalBase = totalBase.add(base);

                String presentacion = d.getArticuloEmpaque() != null && d.getArticuloEmpaque().getUnidadEmpaque() != null
                        ? d.getArticuloEmpaque().getUnidadEmpaque().getDescripcion() : texto(d.getNombreUnidad());
                String articulo = texto(d.getDescripcionArticulo())
                        + (d.getLote() != null ? "  (lote " + d.getLote().getNumeroLote() + ")" : "");
                tabla.addCell(celda(articulo, TextAlignment.LEFT));
                tabla.addCell(celda(presentacion, TextAlignment.LEFT));
                tabla.addCell(celda(numero.format(cantidad), TextAlignment.RIGHT));
                tabla.addCell(celda(numero.format(base), TextAlignment.RIGHT));
                tabla.addCell(celda(numero.format(costo), TextAlignment.RIGHT));
                tabla.addCell(celda(numero.format(valor), TextAlignment.RIGHT));
            }
            tabla.addCell(new Cell(1, 3).add(new Paragraph(detalles.size()
                    + (detalles.size() == 1 ? " artículo" : " artículos")).setFont(negrita)));
            tabla.addCell(new Cell().add(new Paragraph(numero.format(totalBase)).setFont(negrita))
                    .setTextAlignment(TextAlignment.RIGHT));
            tabla.addCell(new Cell().add(new Paragraph("TOTAL").setFont(negrita)).setTextAlignment(TextAlignment.RIGHT));
            tabla.addCell(new Cell().add(new Paragraph("RD$ " + numero.format(total)).setFont(negrita))
                    .setTextAlignment(TextAlignment.RIGHT));
            doc.add(tabla);

            // Firmas
            Table firmas = new Table(UnitValue.createPercentArray(new float[]{1, 0.3f, 1})).useAllAvailableWidth()
                    .setMarginTop(60);
            firmas.addCell(firma("Reportado por"));
            firmas.addCell(new Cell().setBorder(Border.NO_BORDER));
            firmas.addCell(firma("Autorizado por (encargado)"));
            doc.add(firmas);

            doc.add(new Paragraph("Impreso el " + fechaHora.format(new java.util.Date())).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.RIGHT).setMarginTop(20));

        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el comprobante de la merma", e);
        }
        return salida.toByteArray();
    }

    private static void dato(Table tabla, String etiqueta, String valor, PdfFont negrita) {
        tabla.addCell(new Cell().add(new Paragraph(etiqueta).setFont(negrita)).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph(valor)).setBorder(Border.NO_BORDER));
    }

    private static Cell celda(String valor, TextAlignment alineacion) {
        return new Cell().add(new Paragraph(valor)).setTextAlignment(alineacion);
    }

    private static Cell firma(String titulo) {
        return new Cell().add(new Paragraph(titulo).setTextAlignment(TextAlignment.CENTER).setFontSize(9))
                .setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1));
    }

    private static String texto(String valor) {
        return valor != null ? valor : "";
    }
}
