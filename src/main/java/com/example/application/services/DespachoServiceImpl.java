/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.sap.DataXSJS;
import com.example.application.util.ClaseUtil;
import static com.vaadin.uitest.parser.Parser.objectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DespachoServiceImpl implements DespachoService {

    String username = "SYSTEM";
    String password = "S@hana2017.#";

    @Autowired
    private final RestTemplate restTemplate = null;

    @Override
    public Double getDespacho(String producto, Date fecha, int turno) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Double getDespacho(String producto, Date fecha) {

        String url = "http://172.20.1.18:8000/ccibao/v1/tracking/despacho_de_cemento.xsjs?item=" + producto + "";

        String existencia;

        try {
            // Crear encabezados con autenticación
            HttpHeaders headers = ClaseUtil.configurarEncabezado(username, password);

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

            existencia = dataXSJS.getData().getQuantity() == null ? "0.0" : dataXSJS.getData().getQuantity();

            return Double.valueOf(existencia);

        } catch (Exception e) {

            System.out.println("Error al consumir el servicio XSJS: " + e.getMessage());
            return null;
        }

//        
//        String result = restTemplate.getForObject(url, String.class); // Realiza la llamada a la API
//        return Double.valueOf(result); // Convierte el array en una lista
    }

    @Override
    public Double getDespacho(String producto) {

        String url = "http://172.20.1.18:8000/ccibao/v1/tracking/cementoenbodega_prueba.xsjs?item=" + producto + "";
        String result = restTemplate.getForObject(url, String.class); // Realiza la llamada a la API
        System.out.println("json " + result);

        return Double.valueOf("200"); // Convierte el array en una lista
    }

    @Override
    public Double getDespacho(String producto, Date fi, Date ff) {

        String fiStr = new SimpleDateFormat("yyyy-MM-dd").format(fi);
        String ffStr = new SimpleDateFormat("yyyy-MM-dd").format(ff);

        System.out.println("fiStr " + fiStr + " ffStr : " + ffStr);

        String url = "http://172.20.1.18:8000/ccibao/v1/tracking/despacho_de_cemento.xsjs?fi=" + fiStr
                + "&ff=" + ffStr + "&item=" + producto;

        String existencia;

        try {
            // Crear encabezados con autenticación
            HttpHeaders headers = ClaseUtil.configurarEncabezado(username, password);

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

            existencia = dataXSJS.getData().getQuantity() == null ? "0.0" : dataXSJS.getData().getQuantity();

            return Double.valueOf(existencia);

        } catch (Exception e) {

            System.out.println("Error al consumir el servicio XSJS: " + e.getMessage());
            return null;
        }

    }

    @Override
    public Double getDespacho(String producto, Date fi, Date ff, int turno) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Double getDespacho(String turno, String producto, Date fecha) {

        String fiStr = new SimpleDateFormat("yyyy-MM-dd").format(fecha);
//        String ffStr = new SimpleDateFormat("yyyy-MM-dd").format(ff);

//        System.out.println("fiStr " + fiStr + " ffStr : " + ffStr);
        String url = "http://172.20.1.18:8000/ccibao/v1/tracking/despachoPorTurno.xsjs?hf=" + turno
                + "&item=" + producto
                + "&ff=" + fiStr;

        String despacho;

        try {
            // Crear encabezados con autenticación
            HttpHeaders headers = ClaseUtil.configurarEncabezado(username, password);

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

            despacho = dataXSJS.getData().getQuantity() == null ? "0.0" : dataXSJS.getData().getQuantity();

            return Double.valueOf(despacho);

        } catch (Exception e) {
            e.printStackTrace();

            System.out.println("Error al consumir el servicio XSJS: " + e.getMessage());
            return null;
        }

    }

}
