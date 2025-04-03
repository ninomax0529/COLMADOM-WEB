/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.util;

/**
 *
 * @author Maximiliano
 */
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.core.io.ClassPathResource;

public class OCRExample {

    public static void main(String[] args) {

        String imagePath = "/imagen/img1.jpg"; // Cambia esto por la ruta real
        try {
            // Ruta de la imagen de entrada

            String subreportPath = new ClassPathResource(imagePath).getFile().getAbsolutePath();

            if (subreportPath == null) {
                throw new RuntimeException("El archivo de reporte no se encontró en la ruta ." + subreportPath);
//                return pdfResource;

            }

            System.out.println("Imagen encontrada, iniciando OCR...");

            // Configuración de Tesseract OCR
            Tesseract tesseract = new Tesseract();
//                    tesseract.setDatapath("/tessdata/eng.traineddata"); // Ruta donde está el archivo de entrenamiento
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata"); // Ruta donde está el archivo de entrenamiento
//                tesseract.setLanguage("eng"); // Idioma: inglés (puedes usar "spa" para español)

            try {
                // Realiza el OCR en la imagen
                String extractedText = tesseract.doOCR(new File(subreportPath));
                System.out.println("Texto extraído:");
                System.out.println(extractedText);
            } catch (TesseractException e) {
                System.err.println("Error en el OCR: " + e.getMessage());
            }

        } catch (IOException ex) {
            Logger.getLogger(OCRExample.class.getName()).log(Level.SEVERE, null, ex);
        }

//        File imageFile = new File(imagePath);
    }
}
