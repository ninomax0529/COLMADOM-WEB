package com.maxsoft.application.reporte.inventario;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.barcodes.Barcode1D;
import com.itextpdf.barcodes.BarcodeEAN;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BoxSizingPropertyValue;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.OverflowPropertyValue;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.maxsoft.application.util.CodigoBarraUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Etiquetas de código de barras en PDF, al tamaño exacto en milímetros:
 * en rollo (una etiqueta por página del tamaño de la etiqueta) o en hoja
 * carta (en filas). Las barras salen en EAN-13/EAN-8/UPC-A si el código es
 * uno válido del fabricante, si no en Code128.
 */
public final class EtiquetasPdf {

    /** Una presentación y cuántas etiquetas imprimir de ella. */
    public record Etiqueta(String nombre, String presentacion, String codigo, BigDecimal precio, int cantidad) {
    }

    /** Tamaño, distribución y qué textos llevan las etiquetas. */
    public record Formato(int anchoMm, int altoMm, int porFila, boolean rollo, boolean nombre,
            boolean presentacion, boolean codigo, boolean precio) {
    }

    private static final float PT_POR_MM = 72f / 25.4f;
    /** Margen de la hoja carta y separación entre etiquetas. */
    private static final float MARGEN_HOJA_MM = 8;
    private static final float SEPARACION_MM = 2;
    private static final float RELLENO_MM = 1.2f;
    /** Ancho máximo de la barra más fina: más grueso el código corto se ve tosco. */
    private static final float MODULO_MAXIMO_MM = 0.45f;

    private EtiquetasPdf() {
    }

    /** Cuántas etiquetas de {@code anchoMm} caben por fila en una hoja carta. */
    public static int maximoPorFila(int anchoMm) {
        float util = PageSize.LETTER.getWidth() / PT_POR_MM - 2 * MARGEN_HOJA_MM;
        return Math.max(1, (int) ((util + SEPARACION_MM) / (anchoMm + SEPARACION_MM)));
    }

    /**
     * @param vistaPrevia dibuja el borde de cada etiqueta para ver sus límites
     */
    public static byte[] generar(List<Etiqueta> etiquetas, Formato f, boolean vistaPrevia) {

        float ancho = f.anchoMm() * PT_POR_MM;
        float alto = f.altoMm() * PT_POR_MM;

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(salida))) {

            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont negrita = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Estilo estilo = new Estilo(f, normal, negrita, vistaPrevia);

            if (f.rollo()) {
                // Una página por etiqueta, del tamaño de la etiqueta
                PageSize pagina = new PageSize(ancho, alto);
                try (Document doc = new Document(pdf, pagina)) {
                    doc.setMargins(0, 0, 0, 0);
                    boolean primera = true;
                    for (Etiqueta e : etiquetas) {
                        for (int i = 0; i < e.cantidad(); i++) {
                            if (!primera) {
                                doc.add(new AreaBreak(pagina));
                            }
                            primera = false;
                            // Un punto menos de alto: si llena la página exacta, el
                            // redondeo la empuja a una página en blanco
                            doc.add(etiqueta(pdf, e, estilo, ancho, alto - 1));
                        }
                    }
                }
            } else {
                int columnas = Math.min(Math.max(1, f.porFila()), maximoPorFila(f.anchoMm()));
                float separacion = SEPARACION_MM * PT_POR_MM;
                try (Document doc = new Document(pdf, PageSize.LETTER)) {
                    float margen = MARGEN_HOJA_MM * PT_POR_MM;
                    doc.setMargins(margen, margen, margen, margen);

                    float[] anchos = new float[columnas];
                    java.util.Arrays.fill(anchos, ancho + separacion);
                    Table hoja = new Table(UnitValue.createPointArray(anchos));
                    int celdas = 0;
                    for (Etiqueta e : etiquetas) {
                        for (int i = 0; i < e.cantidad(); i++) {
                            hoja.addCell(new Cell().add(etiqueta(pdf, e, estilo, ancho, alto))
                                    .setBorder(Border.NO_BORDER).setPadding(separacion / 2)
                                    .setKeepTogether(true));
                            celdas++;
                        }
                    }
                    // Completa la última fila para que la tabla cierre bien
                    while (celdas % columnas != 0) {
                        hoja.addCell(new Cell().setBorder(Border.NO_BORDER));
                        celdas++;
                    }
                    doc.add(hoja);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudieron generar las etiquetas", e);
        }
        return salida.toByteArray();
    }

    /** Tamaños de letra y de barras según el tamaño de la etiqueta. */
    private record Estilo(Formato f, PdfFont normal, PdfFont negrita, boolean vistaPrevia) {

        float fuenteNombre() {
            return limitar(f.altoMm() * 0.28f, 6, 12);
        }

        float fuenteTexto() {
            return limitar(f.altoMm() * 0.22f, 5, 9);
        }

        float altoBarrasMm() {
            // Lo que deja el texto: menos barras cuantas más líneas lleve
            float resto = 1f;
            if (f.nombre()) {
                resto -= 0.30f;
            }
            if (f.presentacion()) {
                resto -= 0.10f;
            }
            if (f.codigo()) {
                resto -= 0.10f;
            }
            if (f.precio()) {
                resto -= 0.15f;
            }
            return Math.max(f.altoMm() * 0.25f, f.altoMm() * resto - 2 * RELLENO_MM);
        }
    }

    private static Div etiqueta(PdfDocument pdf, Etiqueta e, Estilo s, float ancho, float alto) {

        float relleno = RELLENO_MM * PT_POR_MM;
        float anchoUtil = ancho - 2 * relleno;

        Div d = new Div().setWidth(ancho).setHeight(alto).setPadding(relleno)
                .setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER)
                .setKeepTogether(true);
        d.setProperty(Property.BOX_SIZING, BoxSizingPropertyValue.BORDER_BOX);
        d.setProperty(Property.OVERFLOW_X, OverflowPropertyValue.HIDDEN);
        d.setProperty(Property.OVERFLOW_Y, OverflowPropertyValue.HIDDEN);
        if (s.vistaPrevia()) {
            d.setBorder(new DashedBorder(ColorConstants.GRAY, 0.5f));
        }

        Formato f = s.f();
        if (f.nombre() && e.nombre() != null) {
            // Una línea en etiquetas bajas, dos en las demás: lo que no cabe se corta
            // (si no, el nombre empuja las barras o el precio fuera de la etiqueta)
            int lineas = f.altoMm() < 30 ? 1 : 2;
            d.add(linea(cortar(e.nombre().trim(), s.negrita(), s.fuenteNombre(), anchoUtil, lineas),
                    s.negrita(), s.fuenteNombre()));
        }
        if (f.presentacion() && e.presentacion() != null) {
            d.add(linea(e.presentacion(), s.normal(), s.fuenteTexto()));
        }

        Barcode1D codigo = barras(pdf, e.codigo());
        if (codigo != null) {
            // Centradas, con blanco a los lados (zona de silencio) y sin estirar el
            // módulo más de 0.45 mm: un código corto a todo lo ancho queda tosco
            float natural = codigo.getBarcodeSize().getWidth();
            float anchoBarras = Math.min(anchoUtil * 0.92f, natural * MODULO_MAXIMO_MM * PT_POR_MM / codigo.getX());
            Image barras = new Image(codigo.createFormXObject(ColorConstants.BLACK, null, pdf));
            barras.scaleAbsolute(anchoBarras, s.altoBarrasMm() * PT_POR_MM);
            barras.setHorizontalAlignment(HorizontalAlignment.CENTER);
            barras.setMarginTop(0.5f * PT_POR_MM);
            d.add(barras);
        } else {
            d.add(linea("Código no válido", s.normal(), s.fuenteTexto()).setFontColor(ColorConstants.RED));
        }

        if (f.codigo() && e.codigo() != null) {
            d.add(linea(e.codigo().trim(), s.normal(), s.fuenteTexto()).setCharacterSpacing(0.6f));
        }
        if (f.precio() && e.precio() != null) {
            d.add(linea("RD$ " + new DecimalFormat("#,##0.00").format(e.precio()), s.negrita(),
                    s.fuenteNombre() + 2));
        }
        return d;
    }

    /** Barras del código en la simbología que le toca, sin el texto debajo (va aparte); null si no es válido. */
    private static Barcode1D barras(PdfDocument pdf, String codigo) {

        String c = codigo != null ? codigo.trim() : "";
        Barcode1D barcode;
        switch (CodigoBarraUtil.tipo(c)) {
            case EAN13 -> {
                BarcodeEAN ean = new BarcodeEAN(pdf);
                ean.setCodeType(BarcodeEAN.EAN13);
                barcode = ean;
            }
            case EAN8 -> {
                BarcodeEAN ean = new BarcodeEAN(pdf);
                ean.setCodeType(BarcodeEAN.EAN8);
                barcode = ean;
            }
            case UPCA -> {
                BarcodeEAN ean = new BarcodeEAN(pdf);
                ean.setCodeType(BarcodeEAN.UPCA);
                barcode = ean;
            }
            case CODE128 -> barcode = new Barcode128(pdf);
            default -> {
                return null;
            }
        }
        barcode.setCode(c);
        barcode.setFont(null);
        return barcode;
    }

    private static Paragraph linea(String texto, PdfFont fuente, float tamano) {
        return new Paragraph(texto).setFont(fuente).setFontSize(tamano).setMargin(0)
                .setMultipliedLeading(1.05f).setTextAlignment(TextAlignment.CENTER);
    }

    /**
     * Corta el texto (con "…") para que quepa en {@code lineas} líneas del
     * ancho dado, midiéndolo con la fuente. Con dos líneas se deja margen
     * porque el salto por palabras desperdicia espacio.
     */
    private static String cortar(String texto, PdfFont fuente, float tamano, float ancho, int lineas) {
        float disponible = ancho * (lineas == 1 ? 0.97f : lineas * 0.82f);
        if (fuente.getWidth(texto, tamano) <= disponible) {
            return texto;
        }
        String t = texto;
        while (t.length() > 1 && fuente.getWidth(t + "…", tamano) > disponible) {
            t = t.substring(0, t.length() - 1);
        }
        return t.stripTrailing() + "…";
    }

    private static float limitar(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
