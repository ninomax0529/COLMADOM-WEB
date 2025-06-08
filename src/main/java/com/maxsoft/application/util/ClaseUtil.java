/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxsoft.application.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author maximilianoa-te
 */
public class ClaseUtil {

    private static SimpleDateFormat sdfH = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    private static DecimalFormat df = new DecimalFormat("###,###,###.00");

    public ClaseUtil() {
    }

    public static Date asDate(LocalDate localDate) {

        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String getFormatoHora(Date fecha) {

        return sdfH.format(fecha);

    }

    public static String formatoFecha(Date fecha) {

        return sdf2.format(fecha);

    }

    public static LocalDate convertToLocalDateViaMilisecond(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Long diasEntreLocalDate(LocalDate fechaIni, LocalDate fechaFin) {

        long dias = DAYS.between(fechaIni, fechaFin);

        System.out.println("Numero de dias: " + dias); // 365 dias
        return dias;
    }

    public static Long diferenciaHoras(LocalDateTime ldt1, LocalDateTime ldt2) {

        long days = ldt1.until(ldt2, ChronoUnit.DAYS);
        long hours = ldt1.until(ldt2, ChronoUnit.HOURS);
        System.out.println((hours % 24) + " horas.");

        return hours;
    }

    public static Long diferenciaDias(LocalDateTime ldt1, LocalDateTime ldt2) {

        long days = ldt1.until(ldt2, ChronoUnit.DAYS);
        long hours = ldt1.until(ldt2, ChronoUnit.HOURS);
        System.out.println(days + " dias ");

        return days;
    }

    public static Double formatoNumero(double i) {

        return Double.parseDouble(df.format(i));
    }

    public static double FormatearDouble(double i, int posicion) {
        BigDecimal bd = new BigDecimal(i);
        bd = bd.setScale(posicion, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Configura las credenciales para autenticación básica.
     *
     * @param username
     * @param password
     * @return
     */
    public static HttpHeaders configurarEncabezado(String username, String password) {
        String autorizacion = username + ":" + password;
        String encodeAutorizacion = Base64.getEncoder().encodeToString(autorizacion.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.add("Authorization", "Basic " + encodeAutorizacion);
        return headers;
    }

    public static Date fechaAyer(Date fecha) {

        int diferenciaEnDias = 1;
//            Date fechaActual = Calendar.getInstance().getTime();
        long tiempoActual = fecha.getTime();
        long unDia = diferenciaEnDias * 24 * 60 * 60 * 1000;
        Date fechaAyer = new Date(tiempoActual - unDia);

        return fechaAyer;
    }

    public static String getNombreDia(Date date) {
        String nombreDia = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = 0;

        try {

            month = calendar.get(Calendar.DAY_OF_WEEK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        switch (month) {

            case 1: {

                nombreDia = "Domingo";
                break;
            }

            case 2: {
                nombreDia = "Lunes";
                break;
            }
            case 3: {
                nombreDia = "Martes";
                break;
            }
            case 4: {
                nombreDia = "Miercoles";
                break;
            }
            case 5: {
                nombreDia = "Jueves";
                break;
            }
            case 6: {
                nombreDia = "Viernes";
                break;
            }

            case 7: {
                nombreDia = "Sabado";
                break;
            }

        }
        return nombreDia;
    }

    public static void main(String[] args) {

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println("password : " + bCryptPasswordEncoder.encode("kelvin321."));
//        LocalDateTime ltdThen = LocalDateTime.parse("2021-04-03T06:00:00");
//        LocalDateTime ltdNow = LocalDateTime.parse("2021-05-05T11:00:00");

    

////        LocalDateTime ltdNow = LocalDateTime.now();
//        diferenciaHoras(ltdThen, ltdNow);
    }
}
