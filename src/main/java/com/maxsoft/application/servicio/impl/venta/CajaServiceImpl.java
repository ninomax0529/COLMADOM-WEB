/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.dto.ResumenCajaDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DesgloseCaja;
import com.maxsoft.application.modelo.Empresa;
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

    @Autowired
    private com.maxsoft.application.repo.EmpresaRepo empresaRepo;

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
                    + " y en la gaveta hay RD$ " + String.format("%,.2f", disponible.max(BigDecimal.ZERO)) + ".");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MovimientosCaja registrarFondoExtra(BigDecimal monto, String motivo, String usuario) {

        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("Ingrese un monto mayor que cero.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Indique el motivo o quién entrega el efectivo.");
        }
        CajaTurno turno = obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja para agregarle efectivo."));

        return registrarMovimientoPos(turno.getId().intValue(), TIPO_FONDO_EXTRA,
                monto.setScale(2, java.math.RoundingMode.HALF_UP), // entra a la gaveta
                "Fondo extra: " + motivo.trim(),
                usuario != null && !usuario.isBlank() ? usuario : "Admin");
    }

    @Override
    @Transactional(readOnly = true)
    public String generarComprobanteFondoExtraHtml(Long movimientoId) {

        MovimientosCaja mov = movimientoCajaRepository.findOneById(movimientoId)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento de caja #" + movimientoId + " no encontrado."));
        if (!("POS_" + TIPO_FONDO_EXTRA).equals(mov.getTipo())) {
            throw new IllegalArgumentException("El movimiento #" + movimientoId + " no es un fondo extra.");
        }

        Empresa empresa = empresaRepo.findFirstByOrderByCodigoAsc().orElse(null);
        String fecha = mov.getFechaHora() != null
                ? mov.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"))
                : "";
        String motivo = mov.getDescripcion() != null ? mov.getDescripcion().replaceFirst("^Fondo extra: ", "") : "";
        Integer turno = mov.getCajaTurno() != null && mov.getCajaTurno().getId() != null
                ? mov.getCajaTurno().getId().intValue() : null;

        StringBuilder h = new StringBuilder();
        h.append("<!doctype html><html><head><meta charset='utf-8'><title>Fondo extra #").append(mov.getId()).append("</title>")
                .append("<style>@page{size:80mm auto;margin:3mm}body{font-family:'Courier New',monospace;font-size:12px;width:72mm;margin:0}")
                .append(".c{text-align:center}.b{font-weight:bold}.g{font-size:16px}hr{border:0;border-top:1px dashed #000;margin:6px 0}")
                .append("table{width:100%;border-collapse:collapse}td{padding:1px 0;vertical-align:top}td.r{text-align:right}")
                .append(".f{margin-top:36px;border-top:1px solid #000;text-align:center;padding-top:2px}</style></head><body>");
        if (empresa != null) {
            h.append("<div class='c b'>").append(esc(empresa.getNombre())).append("</div>")
                    .append("<div class='c'>").append(esc(empresa.getDiereccion())).append("</div>")
                    .append("<div class='c'>Tel: ").append(esc(empresa.getTelefono()))
                    .append(" &nbsp;RNC: ").append(esc(empresa.getRnc())).append("</div>");
        }
        h.append("<hr><div class='c b'>COMPROBANTE DE ENTRADA DE EFECTIVO</div><div class='c'>(FONDO EXTRA DE CAJA)</div><hr>")
                .append("<table>")
                .append("<tr><td>No.:</td><td class='r b'>").append(mov.getId()).append("</td></tr>")
                .append("<tr><td>Fecha:</td><td class='r'>").append(fecha).append("</td></tr>")
                .append("<tr><td>Turno de caja:</td><td class='r'>").append(turno != null ? turno : "").append("</td></tr>")
                .append("<tr><td>Registrado por:</td><td class='r'>").append(esc(mov.getUsuario())).append("</td></tr>")
                .append("</table><hr>")
                .append("<div>Motivo / Entregado por:</div><div class='b'>").append(esc(motivo)).append("</div><hr>")
                .append("<table><tr><td class='b g'>MONTO:</td><td class='r b g'>RD$ ")
                .append(String.format("%,.2f", mov.getMonto())).append("</td></tr></table><hr>")
                .append("<div class='f'>Entregado por</div><div class='f'>Recibido por (cajero)</div>")
                .append("</body></html>");
        return h.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaTurno> getTurnos(java.time.LocalDate desde, java.time.LocalDate hasta) {
        java.time.LocalDate d = desde != null ? desde : java.time.LocalDate.now();
        java.time.LocalDate h = hasta != null ? hasta : d;
        if (h.isBefore(d)) {
            java.time.LocalDate t = d;
            d = h;
            h = t;
        }
        return cajaTurnoRepo.findByFechaAperturaBetweenOrderByFechaAperturaDesc(
                d.atStartOfDay(), h.plusDays(1).atStartOfDay().minusNanos(1));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientosCaja> getMovimientos(Long cajaTurnoId) {
        CajaTurno turno = cajaTurnoRepo.findById(cajaTurnoId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado."));
        return movimientoCajaRepository.findByCajaTurnoOrderByFechaHoraAscIdAsc(turno);
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
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
        BigDecimal fondoExtra = BigDecimal.ZERO;
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
                case "POS_" + TIPO_ANULACION_DELIVERY ->
                    cobrosDelivery = cobrosDelivery.subtract(monto.abs());
                case "POS_" + TIPO_ABONO_MOTORISTA ->
                    cobrosDelivery = cobrosDelivery.add(monto.abs());
                case "POS_" + TIPO_COBRO_CXC ->
                    cobrosCxc = cobrosCxc.add(monto);
                case "POS_" + TIPO_ANULACION_COBRO_CXC ->
                    cobrosCxc = cobrosCxc.subtract(monto.abs());
                case "POS_" + TIPO_FONDO_EXTRA ->
                    fondoExtra = fondoExtra.add(monto.abs());
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
        return new ResumenCajaDTO(fondo, fondoExtra, ventasEfectivo, cobrosDelivery, cobrosCxc, pagosProveedores, otros, avances, tarjeta);
    }
}
