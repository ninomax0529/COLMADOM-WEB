package com.maxsoft.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Cuenta de un motorista para la consulta "Cuenta de motoristas".
 *
 * @param cargos total de faltantes cargados
 * @param descargos total de abonos y reversos (en positivo)
 * @param saldo lo que debe (negativo = saldo a su favor)
 * @param ultimoMovimiento fecha del último movimiento, o null si no tiene
 */
public record CuentaMotoristaResumenDTO(
        Integer codigo,
        String nombre,
        String celular,
        BigDecimal cargos,
        BigDecimal descargos,
        BigDecimal saldo,
        Date ultimoMovimiento) implements Serializable {
}
