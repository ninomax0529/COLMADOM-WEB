/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.interfaces.venta;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DesgloseCaja;
import com.maxsoft.application.modelo.MovimientosCaja;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CajaService {

    CajaTurno guardar(CajaTurno obj);

    Optional<CajaTurno> obtenerCajaAbierta();

    CajaTurno abrirCaja(BigDecimal montoApertura, String cajero, String observaciones);

    MovimientosCaja registrarMovimiento(Integer cajaTurnoId, String tipo, BigDecimal monto, String descripcion, String usuario);

    CajaTurno cerrarCaja(Integer cajaTurnoId, String cajeroCierre, List<DesgloseCaja> desglosesEntrada,
            BigDecimal ventasEfectivoColmado, BigDecimal ventasTarjeta, BigDecimal ventasFiado);
    
      CajaTurno cerrarCaja(CajaTurno caja);


    // ... tus otros métodos anteriores ...
    MovimientosCaja registrarMovimientoPos(Integer cajaTurnoId, String tipoPos, BigDecimal monto, String descripcion, String usuario);
}
