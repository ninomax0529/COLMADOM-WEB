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
