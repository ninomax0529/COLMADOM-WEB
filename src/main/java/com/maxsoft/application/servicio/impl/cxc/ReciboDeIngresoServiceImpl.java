/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxc;

import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.DetalleLiquidacionDelivery;
import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.EstadoFactura;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ReciboDeIngresoRepo;
import com.maxsoft.application.servicio.interfaces.cxc.CuentaPorCobrarService;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import java.math.BigDecimal;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReciboDeIngresoServiceImpl implements ReciboDeIngresoService {

    @Autowired
    ReciboDeIngresoRepo repo;

    @Autowired
    @Lazy
    FacturaDeVentaService facturaDeVentaService;

    @Autowired
    CuentaPorCobrarService cuentaPorCobrarService;

    @Autowired
    @Lazy
    CajaService cajaService;

    @Override
    public ReciboDeIngreso guardar(ReciboDeIngreso obj) {
        return repo.save(obj);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso getReciboDeIngreso(int codigo) {
        return repo.getReferenceById(codigo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso procesarReciboDeIngresoPos(FacturaDeVenta factura) {

        ReciboDeIngreso recibo = new ReciboDeIngreso();
        Date fechaActual = new Date();

        recibo.setTotalPagado(factura.getTotal());
        recibo.setBalancePendienteAntes(factura.getTotal());
        recibo.setBalancePendienteDespues(BigDecimal.ZERO);

        recibo.setFecha(factura.getFecha() != null ? factura.getFecha() : fechaActual);
        recibo.setFechaCreacion(fechaActual);
        recibo.setFecha(factura.getFecha() != null ? factura.getFecha() : fechaActual);
        recibo.setCliente(factura.getCliente());
        recibo.setNombreCliente(factura.getCliente().getNombre());

        recibo.setUsuario(new Usuario(1));
        recibo.setNombreUsuario(factura.getNombreUsuario() != null ? factura.getNombreUsuario() : "Admin");
        recibo.setAnulado(false);
        recibo.setConcepto("Recibo automática por Factura de venta #" + factura.getCodigo());

        List<DetalleReciboDeIngreso> detalles = new ArrayList<>();

        DetalleReciboDeIngreso detSalida = new DetalleReciboDeIngreso();

        detSalida.setReciboIngreso(recibo);
        detSalida.setFactura(factura);
        detSalida.setConcepto("PAgo Factura de venta #" + factura.getCodigo());

        detSalida.setTotal(factura.getTotal());
        detSalida.setBalancePendienteAntes(factura.getTotal());
        detSalida.setBalancePendienteDespues(BigDecimal.ZERO);

        detalles.add(detSalida);

        recibo.setDetalleReciboDeIngresoCollection(detalles);

        ReciboDeIngreso reciGurad = this.repo.saveAndFlush(recibo);
        
        factura.setTotalPagado(reciGurad.getTotalPagado());
        // Venta de contado en mostrador: queda saldada en el mismo momento
        factura.setTotalPendiente(BigDecimal.ZERO);
        this.facturaDeVentaService.guardar(factura);

        return reciGurad;
    }

    @Override
    public List<ReciboDeIngreso> getLista() {
        return repo.findAll();
    }

    @Override
    public List<DetalleReciboDeIngreso> getDetalle(int obj) {
        return repo.getDetalle(obj);
    }

    /** Conceptos de recibos que se crean solos y no se anulan desde aquí. */
    private static final String CONCEPTO_RECIBO_POS = "Recibo automática por Factura de venta";
    private static final String CONCEPTO_RECIBO_LIQUIDACION = "Pago con liquidacion de factura";

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso anularReciboDeIngreso(Integer id, String motivo, String usuario) {

        String user = usuario != null && !usuario.isBlank() ? usuario : "Admin";
        ReciboDeIngreso recibo = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El recibo de ingreso #" + id + " no existe."));
        if (Boolean.TRUE.equals(recibo.getAnulado())) {
            throw new IllegalStateException("El recibo de ingreso #" + id + " ya está anulado.");
        }
        String concepto = recibo.getConcepto() != null ? recibo.getConcepto() : "";
        if (concepto.startsWith(CONCEPTO_RECIBO_POS)) {
            throw new IllegalStateException("El recibo #" + id + " es el cobro automático de una venta del POS. "
                    + "Para revertirlo, anule o devuelva la factura de venta.");
        }
        if (concepto.startsWith(CONCEPTO_RECIBO_LIQUIDACION)) {
            throw new IllegalStateException("El recibo #" + id + " se generó en una liquidación de delivery "
                    + "y no se puede anular por separado.");
        }

        BigDecimal total = recibo.getTotalPagado() != null ? recibo.getTotalPagado() : BigDecimal.ZERO;

        // Si el cobro entró a la gaveta, el efectivo sale: caja abierta y con fondos
        CajaTurno turno = null;
        if (recibo.getCajaChica() != null && total.signum() > 0) {
            turno = cajaService.obtenerCajaAbierta()
                    .orElseThrow(() -> new IllegalStateException(
                    "Debe abrir una caja: el recibo #" + id + " se cobró en efectivo y ese dinero sale de la gaveta."));
            cajaService.validarEfectivoDisponible(turno.getId().intValue(), total,
                    "devolver el cobro del recibo #" + id);
        }

        ReciboDeIngreso anulado = revertirRecibo(recibo, motivo, user);

        // 4. Caja: sale el efectivo que había entrado con el cobro
        if (turno != null) {
            cajaService.registrarMovimientoPos(
                    turno.getId().intValue(),
                    CajaService.TIPO_ANULACION_COBRO_CXC,
                    total.negate(),
                    "Anulación Recibo de Ingreso #" + id
                    + (motivo != null && !motivo.isBlank() ? ". Motivo: " + motivo.trim() : ""),
                    user);
        }
        return anulado;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int anularRecibosLiquidacionDelivery(Integer codigoFactura, String motivo, String usuario) {

        String user = usuario != null && !usuario.isBlank() ? usuario : "Admin";
        List<ReciboDeIngreso> recibos = repo.findVigentesPorFacturaYConcepto(codigoFactura, CONCEPTO_RECIBO_LIQUIDACION);
        for (ReciboDeIngreso r : recibos) {
            // Sin movimiento de caja: quien anula la liquidación saca el efectivo completo
            revertirRecibo(r, motivo, user);
        }
        return recibos.size();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReciboDeIngreso> getRecibosVigentesDeFactura(Integer codigoFactura) {
        return repo.findVigentesPorFactura(codigoFactura);
    }

    /**
     * Pasos comunes de una anulación: las facturas vuelven a deber lo pagado,
     * el estado de cuenta se reversa y el recibo queda anulado. No toca caja.
     */
    private ReciboDeIngreso revertirRecibo(ReciboDeIngreso recibo, String motivo, String user) {

        Integer id = recibo.getCodigo();
        String concepto = recibo.getConcepto() != null ? recibo.getConcepto() : "";

        // 1. Facturas: vuelven a deber lo que este recibo les pagó
        for (DetalleReciboDeIngreso det : repo.getDetalle(id)) {
            if (det.getFactura() == null) {
                continue;
            }
            FacturaDeVenta factura = facturaDeVentaService.getFactura(det.getFactura().getCodigo());
            BigDecimal monto = det.getTotal() != null ? det.getTotal() : BigDecimal.ZERO;

            BigDecimal pagado = (factura.getTotalPagado() != null ? factura.getTotalPagado() : BigDecimal.ZERO)
                    .subtract(monto).max(BigDecimal.ZERO);
            factura.setTotalPagado(pagado);
            if (!Boolean.TRUE.equals(factura.getAnulada())) {
                BigDecimal pendiente = netoFactura(factura).subtract(pagado).max(BigDecimal.ZERO);
                factura.setTotalPendiente(pendiente);
                if (pendiente.signum() > 0) {
                    factura.setEstadoFactura(new EstadoFactura(1));
                }
            }
            factura.setFechaActualizacion(new Date());
            facturaDeVentaService.guardar(factura);

            // 2. Estado de cuenta: el cliente vuelve a deber (si el pago estaba ahí)
            cuentaPorCobrarService.registrarAnulacionRecibo(recibo, factura, motivo, user);
        }

        // 3. Recibo anulado
        recibo.setAnulado(true);
        String nota = "ANULADO" + (motivo != null && !motivo.isBlank() ? ": " + motivo.trim() : "");
        recibo.setConcepto(concepto.isBlank() ? nota : concepto + " | " + nota);
        return repo.save(recibo);
    }

    @Override
    public Double getPagoFacturaVenta(int fact) {
        return repo.getPagoFacturaVenta(fact);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso procesarReciboDeIngreso(ReciboDeIngreso obj, List<DetalleReciboDeIngreso> listaDet) {

        if (listaDet == null || listaDet.isEmpty()) {
            throw new IllegalArgumentException("El recibo no tiene facturas a pagar.");
        }

        // 1. Calcular y validar TODAS las líneas antes de guardar. Los pagos previos
        // se leen una sola vez por factura; si el recibo trae varias líneas de la
        // misma factura, se acumulan en memoria.
        java.util.Map<Integer, BigDecimal> pagadoPorFactura = new java.util.HashMap<>();
        java.util.Map<Integer, FacturaDeVenta> facturas = new java.util.LinkedHashMap<>();
        BigDecimal totalRecibo = BigDecimal.ZERO;

        for (DetalleReciboDeIngreso det : listaDet) {

            Integer codigoFactura = det.getFactura().getCodigo();
            FacturaDeVenta factura = facturas.computeIfAbsent(codigoFactura,
                    c -> this.facturaDeVentaService.getFactura(c));

            BigDecimal totalPago = pagadoPorFactura.computeIfAbsent(codigoFactura, c -> {
                Double pagado = this.repo.getPagoFacturaVenta(c);
                return pagado != null ? BigDecimal.valueOf(pagado) : BigDecimal.ZERO;
            });
            BigDecimal balanceAntrior = netoFactura(factura).subtract(totalPago).max(BigDecimal.ZERO);

            if (Boolean.TRUE.equals(factura.getAnulada())) {
                throw new IllegalStateException("La factura #" + factura.getCodigo() + " está anulada.");
            }
            if (det.getTotal() == null || det.getTotal().signum() <= 0) {
                throw new IllegalArgumentException("El monto a pagar a la factura #" + factura.getCodigo()
                        + " debe ser mayor que cero.");
            }
            if (det.getTotal().compareTo(balanceAntrior) > 0) {
                throw new IllegalArgumentException("La factura #" + factura.getCodigo() + " solo tiene pendiente RD$ "
                        + String.format("%,.2f", balanceAntrior) + "; no se puede pagar RD$ "
                        + String.format("%,.2f", det.getTotal()) + ".");
            }
            BigDecimal nuevoPagado = totalPago.add(det.getTotal());
            BigDecimal balanceActual = netoFactura(factura).subtract(nuevoPagado).max(BigDecimal.ZERO);
            pagadoPorFactura.put(codigoFactura, nuevoPagado);

            det.setBalancePendienteAntes(balanceAntrior);
            det.setBalancePendienteDespues(balanceActual);
            det.setConcepto(balanceActual.signum() == 0
                    ? "Pago total a factura en este recibo "
                    : "Abono a factura en este recibo ");
            det.setReciboIngreso(obj);
            det.setFactura(factura);

            factura.setTotalPagado(nuevoPagado);
            factura.setTotalPendiente(balanceActual);
            factura.setFechaActualizacion(new Date());
            if (balanceActual.signum() == 0) {
                factura.setEstadoFactura(new EstadoFactura(2));
            }

            totalRecibo = totalRecibo.add(det.getTotal());
        }

        // 2. Guardar el recibo (con sus líneas) una sola vez y actualizar las facturas
        obj.setTotalPagado(totalRecibo);
        obj.setDetalleReciboDeIngresoCollection(listaDet);
        obj = this.repo.saveAndFlush(obj);

        for (FacturaDeVenta factura : facturas.values()) {
            this.facturaDeVentaService.guardar(factura);
        }

        // Los pagos a facturas a crédito rebajan el estado de cuenta del cliente
        for (DetalleReciboDeIngreso det : listaDet) {
            if (det.getFactura().getTipoVenta() != null
                    && det.getFactura().getTipoVenta().getCodigo() == CuentaPorCobrarService.TIPO_VENTA_CREDITO) {
                this.cuentaPorCobrarService.registrarPago(obj, det, obj.getNombreUsuario());
            }
        }

        return obj;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso registrarCobroCliente(ReciboDeIngreso recibo, List<DetalleReciboDeIngreso> detalles,
            boolean enEfectivo, String usuario) {

        // El efectivo entra a la gaveta: hace falta un turno abierto antes de tocar nada
        CajaTurno turno = null;
        if (enEfectivo) {
            turno = cajaService.obtenerCajaAbierta()
                    .orElseThrow(() -> new IllegalStateException("Debe abrir una caja antes de cobrar en efectivo."));
        }

        String nombreUsuario = usuario != null && !usuario.isBlank() ? usuario : "Admin";
        recibo.setNombreUsuario(nombreUsuario);
        // Liga el recibo al turno: así su anulación sabe que debe sacar el efectivo de la caja
        recibo.setCajaChica(turno != null ? turno.getId().intValue() : null);
        if (recibo.getCliente() != null) {
            recibo.setNombreCliente(recibo.getCliente().getNombre());
        }

        ReciboDeIngreso guardado = procesarReciboDeIngreso(recibo, detalles);

        if (turno != null && guardado.getTotalPagado() != null && guardado.getTotalPagado().signum() > 0) {
            cajaService.registrarMovimientoPos(
                    turno.getId().intValue(),
                    CajaService.TIPO_COBRO_CXC,
                    guardado.getTotalPagado(),
                    "Cobro a cliente " + guardado.getNombreCliente() + " - Recibo de Ingreso #" + guardado.getCodigo(),
                    nombreUsuario
            );
        }
        return guardado;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReciboDeIngreso abonarLibreta(Integer clienteId, BigDecimal monto, String usuario) {

        if (clienteId == null) {
            throw new IllegalArgumentException("Seleccione el cliente.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("Ingrese un monto de abono mayor que cero.");
        }

        List<FacturaDeVenta> pendientes = facturasLibretaPendientes(clienteId);
        BigDecimal totalPendiente = BigDecimal.ZERO;
        for (FacturaDeVenta f : pendientes) {
            totalPendiente = totalPendiente.add(pendienteFactura(f));
        }
        if (totalPendiente.signum() <= 0) {
            throw new IllegalStateException("El cliente no tiene facturas a crédito pendientes.");
        }
        if (monto.compareTo(totalPendiente) > 0) {
            throw new IllegalArgumentException("El abono (RD$ " + String.format("%,.2f", monto)
                    + ") es mayor que lo pendiente en la libreta (RD$ " + String.format("%,.2f", totalPendiente) + ").");
        }

        Cliente cliente = pendientes.get(0).getCliente();

        ReciboDeIngreso recibo = new ReciboDeIngreso();
        Date ahora = new Date();
        recibo.setAnulado(false);
        recibo.setFecha(ahora);
        recibo.setFechaCreacion(ahora);
        recibo.setCliente(cliente);
        recibo.setUsuario(new Usuario(1));
        recibo.setConcepto("Abono a libreta desde el POS");
        recibo.setTotalPagado(monto);
        recibo.setBalancePendienteAntes(totalPendiente);
        recibo.setBalancePendienteDespues(totalPendiente.subtract(monto));

        // Se salda primero la factura más vieja
        List<DetalleReciboDeIngreso> detalles = new ArrayList<>();
        BigDecimal restante = monto;
        for (FacturaDeVenta f : pendientes) {
            if (restante.signum() <= 0) {
                break;
            }
            BigDecimal pendiente = pendienteFactura(f);
            if (pendiente.signum() <= 0) {
                continue;
            }
            BigDecimal aplicar = restante.min(pendiente);

            DetalleReciboDeIngreso d = new DetalleReciboDeIngreso();
            d.setReciboIngreso(recibo);
            d.setFactura(f);
            d.setTotal(aplicar);
            d.setBalancePendienteAntes(pendiente);
            d.setBalancePendienteDespues(pendiente.subtract(aplicar));
            detalles.add(d);

            restante = restante.subtract(aplicar);
        }

        return registrarCobroCliente(recibo, detalles, true, usuario);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal pendienteLibreta(Integer clienteId) {

        BigDecimal total = BigDecimal.ZERO;
        for (FacturaDeVenta f : facturasLibretaPendientes(clienteId)) {
            total = total.add(pendienteFactura(f));
        }
        return total;
    }

    /** Facturas a crédito con balance pendiente del cliente, de la más vieja a la más nueva. */
    private List<FacturaDeVenta> facturasLibretaPendientes(Integer clienteId) {

        return this.facturaDeVentaService.getListaFactPendiente(clienteId).stream()
                .filter(f -> f.getTipoVenta() != null
                && f.getTipoVenta().getCodigo() == CuentaPorCobrarService.TIPO_VENTA_CREDITO)
                .filter(f -> !Boolean.TRUE.equals(f.getAnulada()))
                .sorted(Comparator.comparing(FacturaDeVenta::getFecha, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(FacturaDeVenta::getCodigo))
                .toList();
    }

    /** Total de la factura menos lo devuelto (lo que realmente se le cobra al cliente). */
    private static BigDecimal netoFactura(FacturaDeVenta f) {
        BigDecimal total = f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO;
        return total.subtract(f.getTotalDevuelto()).max(BigDecimal.ZERO);
    }

    /** Lo pendiente de una factura con el mismo cálculo que valida el recibo. */
    private BigDecimal pendienteFactura(FacturaDeVenta f) {

        Double pagado = this.repo.getPagoFacturaVenta(f.getCodigo());
        BigDecimal total = netoFactura(f);
        return total.subtract(pagado != null ? BigDecimal.valueOf(pagado) : BigDecimal.ZERO).max(BigDecimal.ZERO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void procesarReciboDeIngresoLiquidacionDelivery(List<DetalleLiquidacionDelivery> listLiquidacion) {

       
        for (DetalleLiquidacionDelivery liquidacion : listLiquidacion) {

            // Solo lo cobrado en efectivo por el motorista genera recibo.
            // Las facturas FIADO quedan pendientes en CxC a nombre del cliente.
            if (!PedidoDeliveryDTO.CONTADO.equals(liquidacion.getTipoPago())) {
                continue;
            }
            if (liquidacion.getMonto() == null || liquidacion.getMonto().signum() <= 0) {
                continue;
            }

            ReciboDeIngreso recibo = new ReciboDeIngreso();
            recibo.setAnulado(false);
            recibo.setFecha(new Date());
            recibo.setFechaCreacion(new Date());

            recibo.setConcepto("Pago con liquidacion de factura");

            recibo.setTotalPagado(liquidacion.getMonto());

            List<DetalleReciboDeIngreso> detalles = new ArrayList<>();

            DetalleReciboDeIngreso d = new DetalleReciboDeIngreso(); 
            
            d.setReciboIngreso(recibo);
            FacturaDeVenta factura = this.facturaDeVentaService.getFactura(liquidacion.getFactura().getCodigo());
            recibo.setCliente(liquidacion.getCliente() != null ? liquidacion.getCliente() : factura.getCliente());
            d.setFactura(factura);
            d.setBalancePendienteAntes(netoFactura(factura));
            d.setTotal(liquidacion.getMonto());
            d.setBalancePendienteDespues(BigDecimal.ZERO);
            detalles.add(d);
            
            recibo.setDetalleReciboDeIngresoCollection(detalles);
            
            procesarReciboDeIngreso(recibo, detalles);
            
        }

    }

}
