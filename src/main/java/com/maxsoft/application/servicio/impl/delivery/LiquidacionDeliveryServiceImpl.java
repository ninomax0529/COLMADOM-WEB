package com.maxsoft.application.servicio.impl.delivery;

import com.maxsoft.application.dto.DeliveryRealizadoDTO;
import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.DetalleLiquidacionDelivery;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.LiquidacionDelivery;
import com.maxsoft.application.repo.LiquidacionDeliveryRepo;
import com.maxsoft.application.servicio.interfaces.cxc.CuentaPorCobrarService;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import com.maxsoft.application.servicio.interfaces.delivery.LiquidacionDeliveryService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.servicio.interfaces.venta.TipoVentaService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiquidacionDeliveryServiceImpl implements LiquidacionDeliveryService {

    // Mismos códigos que usa el punto de venta
    private static final int ESTADO_FACTURA_DELIVERY = 1;
    private static final int TIPO_VENTA_LIBRETA = 2;

    private final LiquidacionDeliveryRepo liquidacionRepo;
    private final TipoVentaService tipoVentaService;
    private final ReciboDeIngresoService reciboDeIngresoService;
    private final CajaService cajaService;
    private final CuentaPorCobrarService cuentaPorCobrarService;

    public LiquidacionDeliveryServiceImpl(LiquidacionDeliveryRepo liquidacionRepo,
            TipoVentaService tipoVentaService,
            ReciboDeIngresoService reciboDeIngresoService,
            CajaService cajaService,
            CuentaPorCobrarService cuentaPorCobrarService
    ) {
        this.liquidacionRepo = liquidacionRepo;
        this.tipoVentaService = tipoVentaService;
        this.reciboDeIngresoService = reciboDeIngresoService;
        this.cajaService = cajaService;
        this.cuentaPorCobrarService = cuentaPorCobrarService;
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<PedidoDeliveryDTO> getPedidosPendientes(Integer codigoDelivery) {

        if (codigoDelivery == null) {
            return new ArrayList<>();
        }

        return liquidacionRepo.findPendientes(codigoDelivery, ESTADO_FACTURA_DELIVERY)
                .stream()
                .map(this::aDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryRealizadoDTO> getDeliveriesRealizados(Date desde, Date hastaExclusivo, Integer codigoDelivery) {

        return liquidacionRepo.findDeliveriesConLiquidacion(desde, hastaExclusivo)
                .stream()
                .map(fila -> aDtoRealizado((FacturaDeVenta) fila[0], (DetalleLiquidacionDelivery) fila[1]))
                .filter(dto -> codigoDelivery == null || Objects.equals(dto.getCodigoMotorista(), codigoDelivery))
                .toList();
    }

    private DeliveryRealizadoDTO aDtoRealizado(FacturaDeVenta f, DetalleLiquidacionDelivery liquidada) {

        String estado;
        if (Boolean.TRUE.equals(f.getAnulada())) {
            estado = DeliveryRealizadoDTO.ANULADO;
        } else if (liquidada != null) {
            estado = DeliveryRealizadoDTO.LIQUIDADO;
        } else {
            estado = DeliveryRealizadoDTO.PENDIENTE;
        }

        Delivery motorista = f.getDelivery();

        return new DeliveryRealizadoDTO(
                f.getCodigo(),
                f.getFecha(),
                f.getNombreCliente(),
                motorista != null ? motorista.getCodigo() : null,
                motorista != null ? motorista.getNombre() : "",
                esFiado(f) ? PedidoDeliveryDTO.FIADO : PedidoDeliveryDTO.CONTADO,
                netoFactura(f),
                estado,
                liquidada != null ? liquidada.getLiquidacionDelivery().getCodigo() : null,
                liquidada != null ? liquidada.getLiquidacionDelivery().getFecha() : null,
                liquidada != null && liquidada.getCliente() != null
                ? liquidada.getCliente().getNombre() : null
        );
    }

    private PedidoDeliveryDTO aDto(FacturaDeVenta f) {

        return new PedidoDeliveryDTO(
                f.getCodigo(),
                String.valueOf(f.getCodigo()),
                f.getNombreCliente(),
                esFiado(f) ? PedidoDeliveryDTO.FIADO : PedidoDeliveryDTO.CONTADO,
                netoFactura(f),
                f.getCliente()
        );
    }

    /** Total menos lo devuelto antes de liquidar: es lo que el motorista debe entregar. */
    private static BigDecimal netoFactura(FacturaDeVenta f) {
        BigDecimal total = f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO;
        return total.subtract(f.getTotalDevuelto()).max(BigDecimal.ZERO);
    }

    /**
     * Una factura es fiada cuando se vendió a libreta; el resto se cobra de
     * contado.
     */
    private boolean esFiado(FacturaDeVenta f) {

        return f.getTipoVenta() != null
                && Objects.equals(f.getTipoVenta().getCodigo(), TIPO_VENTA_LIBRETA);
    }

    /**
     * Convierte la factura a libreta (cuentas por cobrar) a nombre del cliente.
     * Replica lo que hace el punto de venta al fiar: cliente, nombre y tipo de
     * venta libreta. La factura es una entidad gestionada dentro de la
     * transacción, así que el cambio se guarda al confirmar.
     */
    private void pasarACredito(FacturaDeVenta f, Cliente cliente) {

        f.setCliente(cliente);
        f.setNombreCliente(cliente.getNombre());
        f.setTipoVenta(tipoVentaService.getTipoVenta(TIPO_VENTA_LIBRETA));
    }

    // ============================================================
    // LIQUIDACIÓN
    // ============================================================
    @Override
    public LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            BigDecimal efectivoRecibido, String observacion, String usuario) {

        return liquidar(motorista, codigosFactura, Map.of(), efectivoRecibido, observacion, usuario);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            Map<Integer, Cliente> clientesCredito, BigDecimal efectivoRecibido,
            String observacion, String usuario) {

        Map<Integer, Cliente> credito = clientesCredito != null ? clientesCredito : Map.of();

        if (motorista == null) {
            throw new IllegalArgumentException("Debe seleccionar el motorista.");
        }
        // El efectivo que entrega el motorista entra a la gaveta: requiere caja abierta.
        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException(
                "Debe abrir una caja antes de liquidar el delivery."));
        if (codigosFactura == null || codigosFactura.isEmpty()) {
            throw new IllegalArgumentException("No hay facturas para liquidar.");
        }
        BigDecimal recibido = efectivoRecibido != null ? efectivoRecibido : BigDecimal.ZERO;
        if (recibido.signum() < 0) {
            throw new IllegalArgumentException("El efectivo recibido no puede ser negativo.");
        }

        // Solo se liquidan facturas que siguen pendientes de este motorista
        Map<Integer, FacturaDeVenta> pendientes = new LinkedHashMap<>();
        for (FacturaDeVenta f : liquidacionRepo.findPendientes(motorista.getCodigo(), ESTADO_FACTURA_DELIVERY)) {
            pendientes.put(f.getCodigo(), f);
        }

        List<FacturaDeVenta> aLiquidar = new ArrayList<>();
        for (Integer codigo : codigosFactura) {
            FacturaDeVenta f = pendientes.get(codigo);
            if (f == null) {
                throw new IllegalStateException("La factura " + codigo
                        + " ya fue liquidada o no está pendiente para este motorista.");
            }
            aLiquidar.add(f);
        }

        // Cada factura a crédito debe estar en esta liquidación, tener cliente y salir de contado
        for (Map.Entry<Integer, Cliente> e : credito.entrySet()) {
            if (!codigosFactura.contains(e.getKey())) {
                throw new IllegalArgumentException("La factura " + e.getKey()
                        + " no forma parte de esta liquidación.");
            }
            if (e.getValue() == null) {
                throw new IllegalArgumentException("Seleccione el cliente para la factura "
                        + e.getKey() + " que se queda a crédito.");
            }
            if (esFiado(pendientes.get(e.getKey()))) {
                throw new IllegalArgumentException("La factura " + e.getKey() + " ya es a crédito.");
            }
        }

        BigDecimal esperado = BigDecimal.ZERO;
        BigDecimal fiado = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        LiquidacionDelivery liquidacion = new LiquidacionDelivery();
        List<DetalleLiquidacionDelivery> detalles = new ArrayList<>();

        for (FacturaDeVenta f : aLiquidar) {
            
            BigDecimal monto = netoFactura(f);

            // De contado que se queda a crédito: pasa a cuentas por cobrar y no se cobra al motorista
            Cliente clienteCredito = credito.get(f.getCodigo());
            if (clienteCredito != null) {
                pasarACredito(f, clienteCredito);
                // Ahora la debe el cliente: entra a su estado de cuenta (valida su límite)
                cuentaPorCobrarService.registrarFacturaCredito(f, usuario);
            }

            boolean esFiado = esFiado(f);

            if (esFiado) {
                fiado = fiado.add(monto);
            } else {
                esperado = esperado.add(monto);
            }
            total = total.add(monto);

            DetalleLiquidacionDelivery d = new DetalleLiquidacionDelivery();
            d.setLiquidacionDelivery(liquidacion);
            d.setFactura(f);
            d.setTipoPago(esFiado ? PedidoDeliveryDTO.FIADO : PedidoDeliveryDTO.CONTADO);
            d.setMonto(monto);
            d.setCliente(clienteCredito);
            detalles.add(d);
        }

        BigDecimal diferencia = recibido.subtract(esperado);

        // Un sobrante o faltante siempre debe quedar explicado
        if (diferencia.signum() != 0 && (observacion == null || observacion.isBlank())) {
            throw new IllegalArgumentException("Debe indicar el motivo del "
                    + (diferencia.signum() > 0 ? "sobrante" : "faltante") + ".");
        }

        liquidacion.setFecha(new Date());
        liquidacion.setDelivery(motorista);
        liquidacion.setNombreUsuario(usuario);
        liquidacion.setEfectivoEsperado(esperado);
        liquidacion.setEfectivoRecibido(recibido);
        liquidacion.setDiferencia(diferencia);
        liquidacion.setTotalFiado(fiado);
        liquidacion.setTotalLiquidado(total);
        liquidacion.setObservacion(observacion != null ? observacion.trim() : null);
        liquidacion.setDetalleLiquidacionDeliveryCollection(detalles);

        LiquidacionDelivery liqDb=liquidacionRepo.save(liquidacion);
        this.reciboDeIngresoService.procesarReciboDeIngresoLiquidacionDelivery(detalles);

        // Se registra lo que realmente entregó el motorista (incluye sobrante/faltante).
        if (recibido.signum() > 0) {
            String descripcion = "Liquidación delivery #" + liqDb.getCodigo()
                    + " - Motorista: " + motorista.getNombre()
                    + ". Esperado RD$ " + esperado.toPlainString()
                    + ", recibido RD$ " + recibido.toPlainString()
                    + (diferencia.signum() != 0
                            ? (diferencia.signum() > 0 ? ", sobrante RD$ " : ", faltante RD$ ")
                            + diferencia.abs().toPlainString()
                            : "");
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    CajaService.TIPO_DELIVERY,
                    recibido,
                    descripcion,
                    usuario != null && !usuario.isBlank() ? usuario : "SISTEMA"
            );
        }

        return liqDb;
    }
}
