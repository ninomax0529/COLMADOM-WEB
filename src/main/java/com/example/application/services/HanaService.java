/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

/**
 *
 * @author Maximiliano
 */
import com.example.application.data.sap.DataXSJS;
import static com.vaadin.uitest.parser.Parser.objectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import org.springframework.http.HttpMethod;

@Service
public class HanaService {

    private final RestTemplate restTemplate;

    String username = "SYSTEM";
    String password = "S@hana2017.#";

    public HanaService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Configura las credenciales para autenticación básica.
     */
    private HttpHeaders configurarEncabezado() {
        String autorizacion = username + ":" + password;
        String encodeAutorizacion = Base64.getEncoder().encodeToString(autorizacion.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.add("Authorization", "Basic " + encodeAutorizacion);
        return headers;
    }

    /**
     * Consume el servicio XSJS con autenticación básica.
     *
     * @param almacen
     * @param articulo
     * @return
     */
    public DataXSJS getExistenciaArticulos(String almacen, String articulo) {

        String url = "http://172.20.1.18:8000/ccibao/v1/tracking/existenciaArticulos.xsjs?alm=" + almacen + "&item=" + articulo;

        try {
            // Crear encabezados con autenticación
            HttpHeaders headers = configurarEncabezado();

            // Crear la entidad HTTP con los encabezados
            HttpEntity<DataXSJS> entity = new HttpEntity<>(headers);

            // Realizar la solicitud GET con autenticación
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            System.out.println("getStatusCode : " + response.getBody());

            DataXSJS dataXSJS = objectMapper.readValue(response.getBody(), DataXSJS.class);

            System.out.println("resData : " + dataXSJS.getData().getQuantity());

            return dataXSJS;

        } catch (Exception e) {
            System.out.println("Error al consumir el servicio XSJS: " + e.getMessage());
            return null;
        }
    }

    public Double getExistenciaArticulo(String almacen, String articulo) {

        String url = "http://172.20.1.18:8000/ccibao/v1/tracking/existenciaArticulos.xsjs?alm=" + almacen + "&item=" + articulo;

        String existencia;

        try {
            // Crear encabezados con autenticación
            HttpHeaders headers = configurarEncabezado();

            // Crear la entidad HTTP con los encabezados
            HttpEntity<DataXSJS> entity = new HttpEntity<>(headers);

            // Realizar la solicitud GET con autenticación
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            DataXSJS dataXSJS = objectMapper.readValue(response.getBody(), DataXSJS.class);

            existencia = dataXSJS.getData().getQuantity() == null? "0.0" :dataXSJS.getData().getQuantity() ;

            return Double.valueOf(existencia);

        } catch (Exception e) {

            System.out.println("Error al consumir el servicio XSJS: " + e.getMessage());
            return null;
        }
    }
}
