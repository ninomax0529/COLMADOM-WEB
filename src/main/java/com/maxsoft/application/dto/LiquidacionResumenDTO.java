package com.maxsoft.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Una liquidación de delivery para la consulta.
 *
 * @param diferencia (efectivo + transferencia) - esperado: positivo sobrante, negativo faltante
 * @param faltanteCargado parte del faltante que quedó como deuda del motorista
 */
public record LiquidacionResumenDTO(
        Integer codigo,
        Date fecha,
        Integer codigoMotorista,
        String motorista,
        String usuario,
        int pedidos,
        int aCredito,
        BigDecimal totalLiquidado,
        BigDecimal totalFiado,
        BigDecimal efectivoEsperado,
        BigDecimal efectivoRecibido,
        BigDecimal transferenciaRecibida,
        String referenciaTransferencia,
        BigDecimal diferencia,
        BigDecimal faltanteCargado,
        String observacion,
        boolean anulada,
        Date fechaAnulacion,
        String motivoAnulacion,
        String usuarioAnulacion) implements Serializable {

    public BigDecimal totalRecibido() {
        return efectivoRecibido.add(transferenciaRecibida);
    }
}
