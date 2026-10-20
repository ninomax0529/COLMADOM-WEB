/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Merma;
import java.util.List;

/**
 * Gestión de mermas (pérdidas de inventario: vencimiento, daño, robo, consumo
 * interno...). Cada merma registrada genera una SALIDA DE INVENTARIO que
 * descuenta las existencias.
 */
public interface MermaService {

    List<Merma> getLista();

    List<DetalleMerma> getDetalle(Integer codigoMerma);

    /**
     * Registra la merma y genera la salida de inventario correspondiente, todo
     * en una sola transacción: si la salida falla (por ejemplo, stock
     * insuficiente) la merma tampoco se guarda.
     *
     * @param merma
     * @param detalles
     * @param usuario
     * @return 
     * @throws IllegalArgumentException datos incompletos o inválidos
     * @throws IllegalStateException la salida de inventario no se pudo procesar
     */
    Merma procesarMerma(Merma merma, List<DetalleMerma> detalles, String usuario);
}
