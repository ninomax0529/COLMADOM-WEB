/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

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
        movimiento.setCajaTurnoId(turno);
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

        List<MovimientosCaja> movimientos = movimientoCajaRepository.findByCajaTurno(cajaTurnoDb);
        BigDecimal totalMovimientos = BigDecimal.ZERO;
        for (MovimientosCaja m : movimientos) {
            if ("RETIRO".equals(m.getTipo()) || "GASTO_MENOR".equals(m.getTipo())) {
                totalMovimientos = totalMovimientos.subtract(m.getMonto());
            } else if ("INGRESO_EXTRA".equals(m.getTipo())) {
                totalMovimientos = totalMovimientos.add(m.getMonto());
            }
        }

        BigDecimal efectivoEsperado = cajaTurnoDb.getMontoApertura()
                .add(ventasEfectivoColmado)
                .add(totalMovimientos);

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
        movimiento.setCajaTurnoId(turno);
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
