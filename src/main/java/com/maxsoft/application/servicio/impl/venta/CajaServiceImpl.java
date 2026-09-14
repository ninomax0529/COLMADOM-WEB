/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DesgloseCaja;
import com.maxsoft.application.modelo.MovimientoCaja;
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
    public MovimientoCaja registrarMovimiento(Integer cajaTurnoId, String tipo, BigDecimal monto, String descripcion, String usuario) {
        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));

        if (!"ABIERTA".equals(turno.getEstado())) {
            throw new IllegalStateException("No se pueden registrar movimientos en una caja cerrada.");
        }

        MovimientoCaja movimiento = new MovimientoCaja();
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

        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));

        if (!"ABIERTA".equals(turno.getEstado())) {
            throw new IllegalStateException("La caja ya se encuentra cerrada.");
        }

        BigDecimal montoCierreReal = BigDecimal.ZERO;
        for (DesgloseCaja d : desglosesEntrada) {
            d.setCajaTurno(turno);
            BigDecimal subtotal = d.getDenominacion().multiply(BigDecimal.valueOf(d.getCantidad()));
            d.setSubtotal(subtotal);
            montoCierreReal = montoCierreReal.add(subtotal);
        }

        List<MovimientoCaja> movimientos = movimientoCajaRepository.findByCajaTurnoId(cajaTurnoId);
        BigDecimal totalMovimientos = BigDecimal.ZERO;
        for (MovimientoCaja m : movimientos) {
            if ("RETIRO".equals(m.getTipo()) || "GASTO_MENOR".equals(m.getTipo())) {
                totalMovimientos = totalMovimientos.subtract(m.getMonto());
            } else if ("INGRESO_EXTRA".equals(m.getTipo())) {
                totalMovimientos = totalMovimientos.add(m.getMonto());
            }
        }

        BigDecimal efectivoEsperado = turno.getMontoApertura()
                .add(ventasEfectivoColmado)
                .add(totalMovimientos);

        BigDecimal diferencia = montoCierreReal.subtract(efectivoEsperado);

        turno.setFechaCierre(LocalDateTime.now());
        turno.setMontoCierreReal(montoCierreReal);
        turno.setVentasEfectivoEsperadas(efectivoEsperado);
        turno.setVentasTarjeta(ventasTarjeta);
        turno.setVentasFiado(ventasFiado);
        turno.setDiferencia(diferencia);
        turno.setCajeroCierre(cajeroCierre);
        turno.setEstado("CERRADA");

        CajaTurno cajaGuardada = cajaTurnoRepo.save(turno);

        for (DesgloseCaja d : desglosesEntrada) {
            desgloseCajaRepository.save(d);
        }

        return cajaGuardada;
    }

    @Override
    @Transactional
    public MovimientoCaja registrarMovimientoPos(Integer cajaTurnoId, String tipoPos, BigDecimal monto, String descripcion, String usuario) {
        
        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));

        if (!"ABIERTA".toLowerCase().equals(turno.getEstado().toLowerCase())) {
            throw new IllegalStateException("No se pueden registrar movimientos en una caja cerrada.");
        }

        MovimientoCaja movimiento = new MovimientoCaja();
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
}
