/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.venta;

import com.maxsoft.application.dto.ResumenCajaDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DesgloseCaja;
import com.maxsoft.application.modelo.MovimientosCaja;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CajaService {

    /*
     * Tipos de movimiento para registrarMovimientoPos (se guardan con prefijo "POS_").
     * Convención de montos: positivo = entra efectivo a la gaveta, negativo = sale.
     */
    String TIPO_VENTA = "VENTA";
    String TIPO_ANULACION_VENTA = "ANULACION_VENTA";
    String TIPO_DEVOLUCION = "DEVOLUCION";
    String TIPO_DELIVERY = "DELIVERY";
    String TIPO_PAGO_PROVEEDOR = "PAGO_PROVEEDOR";
    /** Reverso de un pago a proveedor (anulación de recibo de egreso): el efectivo vuelve. */
    String TIPO_ANULACION_PAGO_PROVEEDOR = "ANULACION_PAGO_PROVEEDOR";
    /** Abonos / pagos en efectivo de clientes a su libreta (CxC). */
    String TIPO_COBRO_CXC = "COBRO_CXC";
    /** Reverso de un cobro CxC en efectivo (anulación de recibo de ingreso): el efectivo sale. */
    String TIPO_ANULACION_COBRO_CXC = "ANULACION_COBRO_CXC";
    /** Efectivo que se agrega a la gaveta con el turno abierto (fondo extra / cambio). */
    String TIPO_FONDO_EXTRA = "FONDO_EXTRA";
    /** Texto de la opción en el diálogo de movimientos de caja. */
    String OPCION_FONDO_EXTRA = "Entrada de Efectivo (Fondo Extra)";

    // Operaciones con tarjeta registradas desde el diálogo de movimientos POS
    String TARJETA_CONSUMO = "Consumo con Tarjeta / Datáfono";
    String TARJETA_AVANCE = "Avance de Efectivo con Tarjeta";
    String TARJETA_ANULACION = "Anulación de Voucher";

    CajaTurno guardar(CajaTurno obj);

    /**
     * Calcula el resumen del turno (efectivo esperado, tarjeta, etc.) a partir
     * de sus movimientos de caja.
     */
    ResumenCajaDTO obtenerResumenCaja(Integer cajaTurnoId);

    /**
     * Verifica que en la gaveta haya efectivo suficiente (según los movimientos
     * del turno) para sacar el monto indicado; si no, lanza IllegalStateException.
     */
    void validarEfectivoDisponible(Integer cajaTurnoId, BigDecimal monto, String operacion);

    /**
     * Registra una entrada de efectivo a la caja abierta (fondo extra), con su
     * motivo y quién lo entrega. El id del movimiento es el número del comprobante.
     */
    MovimientosCaja registrarFondoExtra(BigDecimal monto, String motivo, String usuario);

    /** Ticket (HTML de 80 mm) del comprobante de un fondo extra, listo para imprimir. */
    String generarComprobanteFondoExtraHtml(Long movimientoId);

    /** Turnos de caja abiertos entre las dos fechas (inclusive), del más reciente al más antiguo. */
    List<CajaTurno> getTurnos(java.time.LocalDate desde, java.time.LocalDate hasta);

    /** Movimientos de un turno en orden cronológico. */
    List<MovimientosCaja> getMovimientos(Long cajaTurnoId);

    /** Nombre legible de un tipo de movimiento (p. ej. "POS_VENTA" -> "Venta"). */
    static String nombreTipo(String tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case "POS_" + TIPO_VENTA -> "Venta";
            case "POS_" + TIPO_ANULACION_VENTA -> "Anulación de venta";
            case "POS_" + TIPO_DEVOLUCION -> "Devolución";
            case "POS_" + TIPO_DELIVERY -> "Liquidación delivery";
            case "POS_" + TIPO_PAGO_PROVEEDOR -> "Pago a proveedor";
            case "POS_" + TIPO_ANULACION_PAGO_PROVEEDOR -> "Anulación pago proveedor";
            case "POS_" + TIPO_COBRO_CXC -> "Abono de cliente (CxC)";
            case "POS_" + TIPO_ANULACION_COBRO_CXC -> "Anulación abono (CxC)";
            case "POS_" + TIPO_FONDO_EXTRA -> "Fondo extra";
            case "POS_" + TARJETA_CONSUMO -> "Tarjeta: consumo";
            case "POS_" + TARJETA_AVANCE -> "Tarjeta: avance de efectivo";
            case "POS_" + TARJETA_ANULACION -> "Tarjeta: anulación voucher";
            case "RETIRO" -> "Retiro";
            case "GASTO_MENOR" -> "Gasto menor";
            case "INGRESO_EXTRA" -> "Ingreso extra";
            default -> tipo.startsWith("POS_") ? tipo.substring(4) : tipo;
        };
    }

    /**
     * Cuánto cambia el efectivo de la gaveta con este movimiento (positivo
     * entra, negativo sale, cero si no toca efectivo). Misma regla que el
     * resumen/cierre de caja.
     */
    static BigDecimal efectoEnGaveta(MovimientosCaja m) {
        BigDecimal monto = m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO;
        String tipo = m.getTipo() != null ? m.getTipo() : "";
        String desc = m.getDescripcion() != null ? m.getDescripcion() : "";
        return switch (tipo) {
            case "POS_" + TARJETA_CONSUMO, "POS_" + TARJETA_ANULACION -> BigDecimal.ZERO;
            case "POS_" + TARJETA_AVANCE, "POS_" + TIPO_PAGO_PROVEEDOR, "RETIRO", "GASTO_MENOR" -> monto.abs().negate();
            case "POS_" + TIPO_ANULACION_PAGO_PROVEEDOR, "POS_" + TIPO_FONDO_EXTRA, "INGRESO_EXTRA" -> monto.abs();
            default -> (desc.startsWith("Egreso por compra") || "POS_SALIDA DE DINERO".equals(tipo))
                    ? monto.abs().negate() : monto;
        };
    }

    /** true si el movimiento es de tarjeta (no mueve efectivo de la gaveta, salvo el avance). */
    static boolean esTarjeta(String tipo) {
        return ("POS_" + TARJETA_CONSUMO).equals(tipo) || ("POS_" + TARJETA_ANULACION).equals(tipo)
                || ("POS_" + TARJETA_AVANCE).equals(tipo);
    }

    /**
     * Cierra el turno: recalcula el efectivo esperado con los movimientos
     * reales y guarda el conteo y la diferencia (sobrante/faltante).
     */
    CajaTurno cerrarCaja(Integer cajaTurnoId, String cajeroCierre, BigDecimal montoContado);

    Optional<CajaTurno> obtenerCajaAbierta();

    CajaTurno abrirCaja(BigDecimal montoApertura, String cajero, String observaciones);

    MovimientosCaja registrarMovimiento(Integer cajaTurnoId, String tipo, BigDecimal monto, String descripcion, String usuario);

    CajaTurno cerrarCaja(Integer cajaTurnoId, String cajeroCierre, List<DesgloseCaja> desglosesEntrada,
            BigDecimal ventasEfectivoColmado, BigDecimal ventasTarjeta, BigDecimal ventasFiado);
    
      CajaTurno cerrarCaja(CajaTurno caja);


    // ... tus otros métodos anteriores ...
    MovimientosCaja registrarMovimientoPos(Integer cajaTurnoId, String tipoPos, BigDecimal monto, String descripcion, String usuario);
}
