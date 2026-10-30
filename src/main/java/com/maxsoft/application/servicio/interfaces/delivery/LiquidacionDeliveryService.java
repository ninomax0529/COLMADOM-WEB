package com.maxsoft.application.servicio.interfaces.delivery;

import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.modelo.Delivery;
import com.maxsoft.application.modelo.LiquidacionDelivery;

import java.math.BigDecimal;
import java.util.List;

public interface LiquidacionDeliveryService {

    /**
     * Facturas de delivery del motorista pendientes de liquidar.
     * @param codigoDelivery
     * @return 
     */
    List<PedidoDeliveryDTO> getPedidosPendientes(Integer codigoDelivery);

    /**
     * Liquida las facturas indicadas del motorista: registra el cuadre
     * (efectivo esperado, recibido y diferencia) y deja las facturas fuera de
     * los pendientes.
     *
     * @param motorista
     * @param codigosFactura
     * @param efectivoRecibido efectivo que entrega el motorista por las
     * facturas de contado
     * @param observacion
     * @param usuario
     * @return 
     * @throws IllegalArgumentException datos inválidos, o diferencia sin
     * observación
     * @throws IllegalStateException alguna factura ya fue liquidada o no está
     * pendiente para ese motorista
     */
    LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            BigDecimal efectivoRecibido, String observacion, String usuario);
}
