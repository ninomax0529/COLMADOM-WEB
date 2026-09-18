/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxsoft.application.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
// Formateadores estáticos reutilizables para mejor rendimiento
    private static final DecimalFormat SIMBOLO_RD_FORMAT;
    private static final DecimalFormat NUMERICO_FORMAT;

    private static final DateTimeFormatter FECHA_TICKET_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_TICKET_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter FECHA_HORA_TICKET_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");

    static {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        SIMBOLO_RD_FORMAT = new DecimalFormat("RD$ #,##0.00", symbols);
        NUMERICO_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    // ==========================================
    // MÉTODOS DE CÁLCULO MONETARIO
    // ==========================================
    public static Double subTotal(Double cantidad, Double precio) {
        if (cantidad == null || precio == null) {
            return 0.0;
        }
        BigDecimal cant = BigDecimal.valueOf(cantidad);
        BigDecimal prec = BigDecimal.valueOf(precio);
        return redondear(cant.multiply(prec));
    }

    public static Double totalDescuento(Double subTotal, Double porcientoDescuento) {
        if (subTotal == null || porcientoDescuento == null || porcientoDescuento <= 0) {
            return 0.0;
        }
        BigDecimal st = BigDecimal.valueOf(subTotal);
        BigDecimal porc = BigDecimal.valueOf(porcientoDescuento).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return redondear(st.multiply(porc));
    }

    public static Double totalItbis(Double subTotal, Double totalDescuento, Double porcientoItbis) {
        if (subTotal == null || porcientoItbis == null || porcientoItbis <= 0) {
            return 0.0;
        }
        double desc = (totalDescuento != null) ? totalDescuento : 0.0;

        BigDecimal baseImponible = BigDecimal.valueOf(subTotal - desc);
        if (baseImponible.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }

        BigDecimal porc = BigDecimal.valueOf(porcientoItbis).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return redondear(baseImponible.multiply(porc));
    }

    public static Double total(Double subTotal, Double totalDescuento, Double totalItbis) {
        double st = (subTotal != null) ? subTotal : 0.0;
        double desc = (totalDescuento != null) ? totalDescuento : 0.0;
        double itbis = (totalItbis != null) ? totalItbis : 0.0;

        BigDecimal resultado = BigDecimal.valueOf(st)
                .subtract(BigDecimal.valueOf(desc))
                .add(BigDecimal.valueOf(itbis));
        return redondear(resultado);
    }

    public static Double redondear(BigDecimal valor) {
        if (valor == null) {
            return 0.0;
        }
        return valor.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static Double redondear(Double valor) {
        if (valor == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // ==========================================
    // FORMATEADORES MONETARIOS
    // ==========================================
    /**
     * Formatea un valor double a "RD$ 1,250.00"
     */
    public static String formatearMoneda(Double valor) {
        if (valor == null) {
            return "RD$ 0.00";
        }
        return SIMBOLO_RD_FORMAT.format(valor);
    }

    /**
     * Formatea un valor con prefijo personalizado ej: "RD$ 1,250.00" o "DÓLARES
     * 1,250.00"
     */
    public static String formatearMoneda(Double valor, String prefijo) {
        if (valor == null) {
            return (prefijo != null ? prefijo : "") + " 0.00";
        }
        return (prefijo != null ? prefijo + " " : "") + NUMERICO_FORMAT.format(valor);
    }

    /**
     * Formatea solo el número sin el prefijo RD$, ej: "1,250.00" (Ideal para
     * columnas de Grid/Tickets)
     */
    public static String formatearNumero(Double valor) {
        if (valor == null) {
            return "0.00";
        }
        return NUMERICO_FORMAT.format(valor);
    }

    // ==========================================
    // FORMATEADORES DE FECHA Y HORA (TICKETS)
    // ==========================================
    /**
     * Retorna la fecha actual en formato "dd/MM/yyyy"
     */
    public static String fechaActualTicket() {
        return LocalDateTime.now().format(FECHA_TICKET_FORMAT);
    }

    /**
     * Retorna la hora actual en formato "hh:mm:ss a" (ej: 02:30:15 PM)
     */
    public static String horaActualTicket() {
        return LocalDateTime.now().format(HORA_TICKET_FORMAT);
    }

    /**
     * Retorna la fecha y hora actual en formato "dd/MM/yyyy hh:mm:ss a"
     */
    public static String fechaHoraActualTicket() {
        return LocalDateTime.now().format(FECHA_HORA_TICKET_FORMAT);
    }

    /**
     * Formatea un LocalDateTime a "dd/MM/yyyy hh:mm:ss a"
     */
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return "";
        }
        return fechaHora.format(FECHA_HORA_TICKET_FORMAT);
    }

    /**
     * Formatea un LocalDate a "dd/MM/yyyy"
     */
    public static String formatearFecha(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FECHA_TICKET_FORMAT);
    }

    /**
     * Sobrecarga para compatibilidad con java.util.Date
     */
    public static String formatearFechaHora(Date fecha) {
        if (fecha == null) {
            return "";
        }
        LocalDateTime ldt = new java.sql.Timestamp(fecha.getTime()).toLocalDateTime();
        return ldt.format(FECHA_HORA_TICKET_FORMAT);
    }

    public static void mostrarNotificacion(String mensaje, NotificationVariant variante) {

        // 1. Crear la instancia de la notificación
        Notification notification = new Notification();

// 2. Configurar la duración (4000 ms) y la posición
        notification.setDuration(4000);
        notification.setPosition(Notification.Position.TOP_CENTER);

// 3. Añadir la variante de tema (ej. NotificationVariant.LUMO_SUCCESS)
        notification.addThemeVariants(variante);
// 1. Crear el contenedor con las dimensiones deseadas
        Div content = new Div();
        content.setText(mensaje);
        content.setWidth("500px");   // Mantiene el ancho fijo que definiste
        content.setHeight("150px");  // Mantiene la altura fija que definiste

// 2. Aplicar estilos de tamaño, fuente y centrado absoluto (Flexbox)
        content.getStyle().set("font-size", "34px");
        content.getStyle().set("font-weight", "bold");

// Activa Flexbox para centrar en ambos ejes (horizontal y vertical)
        content.getStyle().set("display", "flex");
        content.getStyle().set("align-items", "center");     // Centrado vertical
        content.getStyle().set("justify-content", "center"); // Centrado horizontal

// Aquí puedes añadir más componentes al contenedor:
// content.add(new Button("Cerrar", e -> notification.close()));
        notification.add(content);

        // 2. Ejecutar JS para reproducir un sonido web estándar (beep) al abrir
        notification.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                UI.getCurrent().getPage().executeJs(
                        "new Audio('https://google.com').play();"
                );
            }
        });

// 5. Mostrar la notificación
        notification.open();

//        Notification notification = Notification.show(mensaje, 4000, Notification.Position.TOP_CENTER);
//        notification.addThemeVariants(variante);
    }

    public static void main(String[] args) {

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println("password : " + bCryptPasswordEncoder.encode("wilson321"));
//        LocalDateTime ltdThen = LocalDateTime.parse("2021-04-03T06:00:00");
//        LocalDateTime ltdNow = LocalDateTime.parse("2021-05-05T11:00:00");

    

////        LocalDateTime ltdNow = LocalDateTime.now();
//        diferenciaHoras(ltdThen, ltdNow);
    }
}
