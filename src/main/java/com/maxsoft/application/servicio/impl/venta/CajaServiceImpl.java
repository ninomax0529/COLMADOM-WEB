/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.dto.ResumenCajaDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DesgloseCaja;
import com.maxsoft.application.modelo.MovimientosCaja;
import com.maxsoft.application.repo.CajaTurnoRepo;
import com.maxsoft.application.repo.DesgloseCajaRepo;
import com.maxsoft.application.repo.MovimientoCajaRepo;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CajaServiceImpl implements CajaService {

    @Autowired
    private CajaTurnoRepo cajaTurnoRepo;

    @Autowired
    private MovimientoCajaRepo movimientoCajaRepository;

    @Autowired
    private DesgloseCajaRepo desgloseCajaRepository;

    @Override
    public Optional<CajaTurno> obtenerCajaAbierta() {
        return cajaTurnoRepo.findByEstado("ABIERTA");
    }

    @Override
    @Transactional
    public CajaTurno abrirCaja(BigDecimal montoApertura, String cajero, String observaciones) {
        if (obtenerCajaAbierta().isPresent()) {
            throw new IllegalStateException("Ya existe un turno de caja abierto actualmente.");
        }

        CajaTurno nuevoTurno = new CajaTurno();
        nuevoTurno.setFechaApertura(LocalDateTime.now());
        nuevoTurno.setMontoApertura(montoApertura);
        nuevoTurno.setCajeroApertura(cajero);
        nuevoTurno.setEstado("ABIERTA");
        nuevoTurno.setObservaciones(observaciones);

        nuevoTurno.setVentasEfectivoEsperadas(montoApertura);
        nuevoTurno.setVentasTarjeta(BigDecimal.ZERO);
        nuevoTurno.setVentasFiado(BigDecimal.ZERO);

        return cajaTurnoRepo.save(nuevoTurno);
    }

    @Override
    @Transactional
    public MovimientosCaja registrarMovimiento(Integer cajaTurnoId, String tipo, BigDecimal monto, String descripcion, String usuario) {
        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));

        if (!"ABIERTA".equals(turno.getEstado())) {
            throw new IllegalStateException("No se pueden registrar movimientos en una caja cerrada.");
        }

        MovimientosCaja movimiento = new MovimientosCaja();
        movimiento.setCajaTurno(turno);
        movimiento.setTipo(tipo); // "RETIRO", "GASTO_MENOR", "INGRESO_EXTRA"
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setUsuario(usuario);

        return movimientoCajaRepository.save(movimiento);
    }

    @Override
    @Transactional
    public CajaTurno cerrarCaja(Integer cajaTurnoId, String cajeroCierre, List<DesgloseCaja> desglosesEntrada,
            BigDecimal ventasEfectivoColmado, BigDecimal ventasTarjeta, BigDecimal ventasFiado) {

        CajaTurno cajaTurnoDb = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));

        if (!"ABIERTA".equals(cajaTurnoDb.getEstado())) {
            throw new IllegalStateException("La caja ya se encuentra cerrada.");
        }

        BigDecimal montoCierreReal = BigDecimal.ZERO;
        for (DesgloseCaja d : desglosesEntrada) {
            d.setCajaTurno(cajaTurnoDb);
            BigDecimal subtotal = d.getDenominacion().multiply(BigDecimal.valueOf(d.getCantidad()));
            d.setSubtotal(subtotal);
            montoCierreReal = montoCierreReal.add(subtotal);
        }

        // El efectivo esperado sale de los movimientos reales del turno; los
        // parámetros ventasEfectivoColmado/ventasTarjeta se ignoran para no
        // contar dos veces las ventas que ya están registradas como movimientos.
        ResumenCajaDTO resumen = calcularResumen(cajaTurnoDb);
        BigDecimal efectivoEsperado = resumen.getEfectivoEsperado();
        ventasTarjeta = resumen.getVentasTarjeta();

        BigDecimal diferencia = montoCierreReal.subtract(efectivoEsperado);

        cajaTurnoDb.setFechaCierre(LocalDateTime.now());
        cajaTurnoDb.setMontoCierreReal(montoCierreReal);
        cajaTurnoDb.setVentasEfectivoEsperadas(efectivoEsperado);
        cajaTurnoDb.setVentasTarjeta(ventasTarjeta);
        cajaTurnoDb.setVentasFiado(ventasFiado);
        cajaTurnoDb.setDiferencia(diferencia);
        cajaTurnoDb.setCajeroCierre(cajeroCierre);
        cajaTurnoDb.setEstado("CERRADA");

        CajaTurno cajaGuardada = cajaTurnoRepo.save(cajaTurnoDb);

        for (DesgloseCaja d : desglosesEntrada) {
            desgloseCajaRepository.save(d);
        }

        return cajaGuardada;
    }

    @Override
    @Transactional
    public MovimientosCaja registrarMovimientoPos(Integer cajaTurnoId, String tipoPos, BigDecimal monto, String descripcion, String usuario) {
        
        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));

        if (!"ABIERTA".toLowerCase().equals(turno.getEstado().toLowerCase())) {
            throw new IllegalStateException("No se pueden registrar movimientos en una caja cerrada.");
        }

        MovimientosCaja movimiento = new MovimientosCaja();
        movimiento.setCajaTurno(turno);
        movimiento.setTipo("POS_" + tipoPos);
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setUsuario(usuario);

        return movimientoCajaRepository.save(movimiento);
    }

    @Override
    public CajaTurno guardar(CajaTurno obj) {
       return  cajaTurnoRepo.save(obj);
    }

    @Override
    public CajaTurno cerrarCaja(CajaTurno cajaTurnoId) {

         return cajaTurnoRepo.save(cajaTurnoId);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCajaDTO obtenerResumenCaja(Integer cajaTurnoId) {
        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));
        return calcularResumen(turno);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarEfectivoDisponible(Integer cajaTurnoId, BigDecimal monto, String operacion) {

        if (monto == null || monto.signum() <= 0) {
            return;
        }
        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));
        BigDecimal disponible = calcularResumen(turno).getEfectivoEsperado();
        if (monto.compareTo(disponible) > 0) {
            throw new IllegalStateException("Efectivo insuficiente en caja para " + operacion
                    + ": se necesitan RD$ " + String.format("%,.2f", monto)
                    + " y en la gaveta hay RD$ " + String.format("%,.2f", disponible.max(BigDecimal.ZERO))
                    + ". Pague con cheque o transferencia, o registre primero la entrada de efectivo.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CajaTurno cerrarCaja(Integer cajaTurnoId, String cajeroCierre, BigDecimal montoContado) {
        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));

        if (!"ABIERTA".equalsIgnoreCase(turno.getEstado())) {
            throw new IllegalStateException("La caja ya se encuentra cerrada.");
        }
        if (montoContado == null || montoContado.signum() < 0) {
            throw new IllegalArgumentException("Ingrese el efectivo contado en la caja.");
        }

        ResumenCajaDTO resumen = calcularResumen(turno);
        BigDecimal efectivoEsperado = resumen.getEfectivoEsperado();

        turno.setFechaCierre(LocalDateTime.now());
        turno.setMontoCierreReal(montoContado);
        turno.setVentasEfectivoEsperadas(efectivoEsperado);
        turno.setVentasTarjeta(resumen.getVentasTarjeta());
        turno.setDiferencia(montoContado.subtract(efectivoEsperado));
        turno.setCajeroCierre(cajeroCierre != null && !cajeroCierre.isBlank() ? cajeroCierre : "SISTEMA");
        turno.setEstado("CERRADA");

        return cajaTurnoRepo.save(turno);
    }

    /**
     * Clasifica los movimientos del turno. Convención: monto positivo = entra
     * efectivo a la gaveta, negativo = sale. Las operaciones con tarjeta se
     * guardan siempre en positivo y se interpretan según su tipo.
     */
    private ResumenCajaDTO calcularResumen(CajaTurno turno) {
        BigDecimal ventasEfectivo = BigDecimal.ZERO;
        BigDecimal cobrosDelivery = BigDecimal.ZERO;
        BigDecimal cobrosCxc = BigDecimal.ZERO;
        BigDecimal pagosProveedores = BigDecimal.ZERO;
        BigDecimal otros = BigDecimal.ZERO;
        BigDecimal avances = BigDecimal.ZERO;
        BigDecimal tarjeta = BigDecimal.ZERO;

        for (MovimientosCaja m : movimientoCajaRepository.findByCajaTurno(turno)) {
            BigDecimal monto = m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO;
            String tipo = m.getTipo() != null ? m.getTipo() : "";
            String desc = m.getDescripcion() != null ? m.getDescripcion() : "";

            switch (tipo) {
                // --- Tarjeta / datáfono ---
                case "POS_" + TARJETA_CONSUMO ->
                    tarjeta = tarjeta.add(monto.abs());
                case "POS_" + TARJETA_ANULACION ->
                    tarjeta = tarjeta.subtract(monto.abs());
                case "POS_" + TARJETA_AVANCE -> {
                    // Se cobra a la tarjeta y se entrega efectivo de la gaveta
                    tarjeta = tarjeta.add(monto.abs());
                    avances = avances.subtract(monto.abs());
                }
                // --- Efectivo (monto con signo) ---
                case "POS_" + TIPO_VENTA, "POS_" + TIPO_ANULACION_VENTA, "POS_" + TIPO_DEVOLUCION ->
                    ventasEfectivo = ventasEfectivo.add(monto);
                case "POS_" + TIPO_DELIVERY ->
                    cobrosDelivery = cobrosDelivery.add(monto);
                case "POS_" + TIPO_COBRO_CXC ->
                    cobrosCxc = cobrosCxc.add(monto);
                case "POS_" + TIPO_PAGO_PROVEEDOR ->
                    pagosProveedores = pagosProveedores.subtract(monto.abs());
                case "POS_" + TIPO_ANULACION_PAGO_PROVEEDOR ->
                    pagosProveedores = pagosProveedores.add(monto.abs());
                // --- Movimientos manuales de caja chica ---
                case "RETIRO", "GASTO_MENOR" ->
                    otros = otros.subtract(monto.abs());
                case "INGRESO_EXTRA" ->
                    otros = otros.add(monto.abs());
                default -> {
                    // Registros anteriores a esta convención (tipo = nombre del
                    // tipo de venta o condición de pago). Los egresos a
                    // proveedores se guardaban en positivo.
                    if (desc.startsWith("Egreso por compra") || "POS_SALIDA DE DINERO".equals(tipo)) {
                        pagosProveedores = pagosProveedores.subtract(monto.abs());
                    } else if (desc.startsWith("Liquidación delivery")) {
                        cobrosDelivery = cobrosDelivery.add(monto);
                    } else if (tipo.startsWith("POS_")) {
                        ventasEfectivo = ventasEfectivo.add(monto);
                    } else {
                        otros = otros.add(monto);
                    }
                }
            }
        }

        BigDecimal fondo = turno.getMontoApertura() != null ? turno.getMontoApertura() : BigDecimal.ZERO;
        return new ResumenCajaDTO(fondo, ventasEfectivo, cobrosDelivery, cobrosCxc, pagosProveedores, otros, avances, tarjeta);
    }
}
