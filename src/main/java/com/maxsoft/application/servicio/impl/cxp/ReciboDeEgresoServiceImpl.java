/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxp;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DetalleReciboDeEgreso;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ReciboDeEgresoRepo;
import com.maxsoft.application.servicio.interfaces.cxp.FacturaDeCompraService;
import com.maxsoft.application.servicio.interfaces.cxp.ReciboDeEgresoService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReciboDeEgresoServiceImpl implements ReciboDeEgresoService {

    @Autowired
    ReciboDeEgresoRepo repo;
    @Autowired
    FacturaDeCompraService FacturaDeCompraService;
            
    @Autowired
    private CajaService cajaService;


    @Override
    public ReciboDeEgreso guardar(ReciboDeEgreso obj) {
        return repo.save(obj);
    }

    @Override
    public ReciboDeEgreso getReciboDeEgreso(int codigo) {
        return repo.getReferenceById(codigo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeEgreso procesarReciboDeEgreso(ReciboDeEgreso obj, List<DetalleReciboDeEgreso> listaDet) {

        if (listaDet == null || listaDet.isEmpty()) {
            throw new IllegalArgumentException("El recibo no tiene facturas a pagar.");
        }

        // 1. Calcular y validar todas las líneas ANTES de guardar (antes se guardaba
        // dentro del ciclo y, con varias facturas, el pago se contaba dos veces).
        java.util.Map<Integer, BigDecimal> pagadoPorFactura = new java.util.HashMap<>();
        java.util.Map<Integer, FacturaDeCompra> facturas = new java.util.LinkedHashMap<>();
        BigDecimal totalRecibo = BigDecimal.ZERO;

        for (DetalleReciboDeEgreso det : listaDet) {

            Integer codigo = det.getFactura().getCodigo();
            FacturaDeCompra factCompra = facturas.computeIfAbsent(codigo, c -> this.FacturaDeCompraService.getFactura(c));

            if (Boolean.TRUE.equals(factCompra.getAnulada())) {
                throw new IllegalStateException("La factura de compra #" + codigo + " está anulada.");
            }
            if (det.getTotal() == null || det.getTotal().signum() <= 0) {
                throw new IllegalArgumentException("El monto a pagar a la factura #" + codigo + " debe ser mayor que cero.");
            }

            BigDecimal totalPago = pagadoPorFactura.computeIfAbsent(codigo, c -> {
                Double pagado = this.repo.getPagoFacturaCompra(c);
                return pagado != null ? BigDecimal.valueOf(pagado) : BigDecimal.ZERO;
            });
            BigDecimal balanceAntrior = factCompra.getTotal().subtract(totalPago);

            if (det.getTotal().compareTo(balanceAntrior) > 0) {
                throw new IllegalArgumentException("La factura de compra #" + codigo + " solo tiene pendiente RD$ "
                        + String.format("%,.2f", balanceAntrior) + "; no se puede pagar RD$ "
                        + String.format("%,.2f", det.getTotal()) + ".");
            }

            BigDecimal nuevoPagado = totalPago.add(det.getTotal());
            BigDecimal balanceActual = factCompra.getTotal().subtract(nuevoPagado);
            pagadoPorFactura.put(codigo, nuevoPagado);

            det.setBalancePendienteAntes(balanceAntrior);
            det.setBalancePendienteDespues(balanceActual);
            det.setConcepto(balanceActual.signum() == 0
                    ? "Pago a factura total en este recibo "
                    : "Abono a factura en este recibo ");
            det.setReciboEgreso(obj);
            det.setFactura(factCompra);

            factCompra.setTotalPagado(nuevoPagado);
            factCompra.setTotalPendiente(balanceActual);
            factCompra.setFechaActualizacion(LocalDateTime.now());
            if (balanceActual.signum() == 0) {
                factCompra.setEstado(new EstadoFactura(2));
            }
            totalRecibo = totalRecibo.add(det.getTotal());
        }

        // 2. Guardar una sola vez
        obj.setTotalPagado(totalRecibo);
        obj.setDetalleReciboDeEgresoCollection(listaDet);
        obj = this.repo.saveAndFlush(obj);

        for (FacturaDeCompra f : facturas.values()) {
            this.FacturaDeCompraService.guardar(f);
        }

        return obj;
    }

    /**
     * Pago manual a proveedor (pantalla de Recibo de Egreso). Si se paga en
     * efectivo, el dinero sale de la gaveta: exige caja abierta, liga el recibo
     * al turno (así su anulación devuelve el efectivo) y registra la salida.
     * Cheque/transferencia no tocan la caja. Todo en una sola transacción.
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeEgreso registrarPagoProveedor(ReciboDeEgreso recibo, List<DetalleReciboDeEgreso> detalles,
            boolean enEfectivo, String usuario) {

        String user = usuario != null && !usuario.isBlank() ? usuario : "Admin";

        CajaTurno turno = null;
        if (enEfectivo) {
            turno = cajaService.obtenerCajaAbierta()
                    .orElseThrow(() -> new IllegalStateException(
                    "Debe abrir una caja: el pago en efectivo sale de la gaveta."));
            recibo.setCajaChica(turno.getId().intValue());

            BigDecimal aPagar = BigDecimal.ZERO;
            if (detalles != null) {
                for (DetalleReciboDeEgreso d : detalles) {
                    if (d.getTotal() != null) {
                        aPagar = aPagar.add(d.getTotal());
                    }
                }
            }
            cajaService.validarEfectivoDisponible(turno.getId().intValue(), aPagar, "pagar al proveedor");
        } else {
            recibo.setCajaChica(null);
        }
        if (recibo.getProveedor() != null && recibo.getNombreProveedor() == null) {
            recibo.setNombreProveedor(recibo.getProveedor().getNombre());
        }

        ReciboDeEgreso guardado = procesarReciboDeEgreso(recibo, detalles);

        if (turno != null) {
            cajaService.registrarMovimientoPos(
                    turno.getId().intValue(),
                    CajaService.TIPO_PAGO_PROVEEDOR,
                    guardado.getTotalPagado().negate(), // sale efectivo de la gaveta
                    "Pago a proveedor " + (guardado.getNombreProveedor() != null ? guardado.getNombreProveedor() : "")
                    + " Recibo de Egreso #" + guardado.getCodigo(),
                    user);
        }
        return guardado;
    }

    @Override
    public List<ReciboDeEgreso> getLista() {
        return repo.findAll();
    }

    @Override
    public List<DetalleReciboDeEgreso> getDetalle(int obj) {
        return repo.getDetalle(obj);
    }

    @Override
    public ReciboDeEgreso anularReciboDeEgreso(Integer id) {
        return anularReciboDeEgreso(id, null, "Admin");
    }

    /**
     * Anula el recibo: devuelve a cada factura lo que este recibo le había
     * abonado y, si el pago salió de la gaveta (recibo ligado a un turno de
     * caja), registra el reingreso de ese efectivo en la caja abierta.
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeEgreso anularReciboDeEgreso(Integer id, String motivo, String usuario) {

        ReciboDeEgreso recibo = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El recibo de egreso #" + id + " no existe."));
        if (Boolean.TRUE.equals(recibo.getAnulado())) {
            throw new IllegalStateException("El recibo de egreso #" + id + " ya está anulado.");
        }

        CajaTurno turno = null;
        if (recibo.getCajaChica() != null) {
            turno = cajaService.obtenerCajaAbierta()
                    .orElseThrow(() -> new IllegalStateException(
                    "Debe abrir una caja: el recibo #" + id + " se pagó en efectivo y el dinero vuelve a la gaveta."));
        }

        for (DetalleReciboDeEgreso det : repo.getDetalle(id)) {
            FacturaDeCompra fact = this.FacturaDeCompraService.getFactura(det.getFactura().getCodigo());
            BigDecimal monto = det.getTotal() != null ? det.getTotal() : BigDecimal.ZERO;
            BigDecimal pagado = (fact.getTotalPagado() != null ? fact.getTotalPagado() : BigDecimal.ZERO).subtract(monto);
            if (pagado.signum() < 0) {
                pagado = BigDecimal.ZERO;
            }
            fact.setTotalPagado(pagado);
            fact.setTotalPendiente(fact.getTotal().subtract(pagado));
            fact.setFechaActualizacion(LocalDateTime.now());
            if (fact.getTotalPendiente().signum() > 0) {
                fact.setEstado(new EstadoFactura(1));
            }
            this.FacturaDeCompraService.guardar(fact);
        }

        recibo.setAnulado(true);
        String nota = "ANULADO" + (motivo != null && !motivo.isBlank() ? ": " + motivo.trim() : "");
        recibo.setConcepto(recibo.getConcepto() != null ? recibo.getConcepto() + " | " + nota : nota);
        ReciboDeEgreso anulado = repo.save(recibo);

        if (turno != null && recibo.getTotalPagado() != null && recibo.getTotalPagado().signum() > 0) {
            cajaService.registrarMovimientoPos(
                    turno.getId().intValue(),
                    CajaService.TIPO_ANULACION_PAGO_PROVEEDOR,
                    recibo.getTotalPagado(), // el efectivo vuelve a la gaveta
                    "Anulación Recibo de Egreso #" + id
                    + (motivo != null && !motivo.isBlank() ? ". Motivo: " + motivo.trim() : ""),
                    usuario != null ? usuario : "Admin");
        }
        return anulado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReciboDeEgreso> getRecibosVigentesDeFactura(Integer factura) {
        return repo.findVigentesPorFactura(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean pagaOtrasFacturas(Integer recibo, Integer factura) {
        return repo.contarOtrasFacturas(recibo, factura) > 0;
    }

    @Override
    public Double getPagoFacturaCompra(int fact) {
        return repo.getPagoFacturaCompra(fact);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeEgreso procesarReciboDeEgreso(FacturaDeCompra factCompra) {

                // 1. Validación de Caja
        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja antes de registrar movimientos de POS"));
        // La compra de contado se paga de la gaveta: debe haber con qué pagarla
        cajaService.validarEfectivoDisponible(turnoActual.getId().intValue(), factCompra.getTotal(),
                "pagar la compra de contado");

        ReciboDeEgreso recibo = new ReciboDeEgreso();
        recibo.setNombreProveedor(factCompra.getNombreProveedor());
        recibo.setProveedor(factCompra.getProveedor());
        recibo.setFecha(factCompra.getFecha());
        recibo.setCajaChica(turnoActual.getId().intValue());
       
        recibo.setConcepto(factCompra.getConcepto());
        recibo.setTotalPagado(factCompra.getTotal());
        recibo.setFechaCreacion(new Date());
        recibo.setUsuario(new Usuario(1));
        recibo.setNombreUsuario("admin");
        recibo.setAnulado(false);

        List<DetalleReciboDeEgreso> detalles = new ArrayList<>();

        DetalleReciboDeEgreso d = new DetalleReciboDeEgreso();

        d.setReciboEgreso(recibo);
        d.setFactura(factCompra);
        d.setBalancePendienteAntes(factCompra.getTotal());
        d.setTotal(factCompra.getTotal());
        d.setBalancePendienteDespues(BigDecimal.ZERO);      
        detalles.add(d);
        recibo.setDetalleReciboDeEgresoCollection(detalles);

        ReciboDeEgreso reciBd = this.repo.save(recibo);

        // La compra de contado queda saldada: si no se actualiza la factura, sigue
        // apareciendo como pendiente en CxP y se podría pagar dos veces.
        factCompra.setTotalPagado(factCompra.getTotal());
        factCompra.setTotalPendiente(BigDecimal.ZERO);
        factCompra.setEstado(new EstadoFactura(2));
        factCompra.setFechaActualizacion(LocalDateTime.now());
        this.FacturaDeCompraService.guardar(factCompra);

//                6. Registrar Movimiento de Caja
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    CajaService.TIPO_PAGO_PROVEEDOR,
                    reciBd.getTotalPagado().negate(), // sale efectivo de la gaveta
                    "Egreso por compra #" + factCompra.getCodigo() + " Recibo de Egreso #" + reciBd.getCodigo(),
                    reciBd.getNombreUsuario()
            );

        return reciBd;
    }

}
