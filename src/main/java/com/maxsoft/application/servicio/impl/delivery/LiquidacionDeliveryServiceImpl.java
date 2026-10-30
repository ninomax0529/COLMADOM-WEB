package com.maxsoft.application.servicio.impl.delivery;

import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.DetalleLiquidacionDelivery;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.LiquidacionDelivery;
import com.maxsoft.application.repo.LiquidacionDeliveryRepo;
import com.maxsoft.application.servicio.interfaces.delivery.LiquidacionDeliveryService;
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

    public LiquidacionDeliveryServiceImpl(LiquidacionDeliveryRepo liquidacionRepo) {
        this.liquidacionRepo = liquidacionRepo;
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

    private PedidoDeliveryDTO aDto(FacturaDeVenta f) {

        return new PedidoDeliveryDTO(
                f.getCodigo(),
                String.valueOf(f.getCodigo()),
                f.getNombreCliente(),
                esFiado(f) ? PedidoDeliveryDTO.FIADO : PedidoDeliveryDTO.CONTADO,
                f.getTotal()
        );
    }

    /**
     * Una factura es fiada cuando se vendió a libreta; el resto se cobra de
     * contado.
     */
    private boolean esFiado(FacturaDeVenta f) {

        return f.getTipoVenta() != null
                && Objects.equals(f.getTipoVenta().getCodigo(), TIPO_VENTA_LIBRETA);
    }

    // ============================================================
    // LIQUIDACIÓN
    // ============================================================
    @Override
    @Transactional
    public LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            BigDecimal efectivoRecibido, String observacion, String usuario) {

        if (motorista == null) {
            throw new IllegalArgumentException("Debe seleccionar el motorista.");
        }
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

        BigDecimal esperado = BigDecimal.ZERO;
        BigDecimal fiado = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        LiquidacionDelivery liquidacion = new LiquidacionDelivery();
        List<DetalleLiquidacionDelivery> detalles = new ArrayList<>();

        for (FacturaDeVenta f : aLiquidar) {
            BigDecimal monto = f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO;
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

        return liquidacionRepo.save(liquidacion);
    }
}
