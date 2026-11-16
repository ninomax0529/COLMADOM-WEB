package com.maxsoft.application.servicio.interfaces.compra;

import com.maxsoft.application.modelo.AplicacionCreditoProveedor;
import com.maxsoft.application.modelo.DetalleDevolucionProveedor;
import com.maxsoft.application.modelo.DevolucionProveedor;
import com.maxsoft.application.modelo.FacturaDeCompra;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Devolución de mercancía a proveedor (por lote). Ver
 * {@link DevolucionProveedor} para las tres formas de liquidarla.
 */
public interface DevolucionProveedorService {

    /**
     * Registra la devolución en una sola transacción: saca la mercancía de
     * cada lote y liquida según el tipo:
     * <ul>
     * <li>NOTA_CREDITO: queda el total como crédito; si trae factura de
     * compra, se aplica a ella lo que permita su pendiente.</li>
     * <li>CAMBIO: entra la misma cantidad en el lote nuevo de cada
     * línea.</li>
     * <li>EFECTIVO: entra el total a la caja abierta.</li>
     * </ul>
     * Para artículos con control de lote cada línea debe indicar su lote.
     *
     * @throws IllegalArgumentException datos incompletos
     * @throws IllegalStateException no hay existencia o no hay caja abierta
     */
    DevolucionProveedor registrar(DevolucionProveedor devolucion, List<DetalleDevolucionProveedor> detalles,
            String usuario);

    /**
     * Aplica (parte de) el crédito disponible de una nota de crédito a una
     * factura de compra pendiente del mismo proveedor. Genera un recibo de
     * egreso sin efectivo.
     */
    AplicacionCreditoProveedor aplicarCredito(Integer devolucion, Integer facturaCompra, BigDecimal monto,
            String usuario);

    /**
     * Anula la devolución: la mercancía vuelve a sus lotes, se anulan los
     * recibos de las aplicaciones de crédito, en un cambio se retira la
     * reposición (si ya se vendió no se puede) y en un reembolso sale el
     * efectivo de la caja abierta.
     */
    DevolucionProveedor anular(Integer devolucion, String motivo, String usuario);

    List<DevolucionProveedor> getLista();

    DevolucionProveedor getDevolucion(Integer codigo);

    List<DetalleDevolucionProveedor> getDetalle(Integer devolucion);

    List<AplicacionCreditoProveedor> getAplicaciones(Integer devolucion);

    /** Facturas de compra del proveedor con saldo pendiente. */
    List<FacturaDeCompra> getFacturasPendientes(Integer proveedor);

    /** Pendiente actual de una factura de compra (total - pagos vigentes). */
    BigDecimal getPendienteFactura(Integer facturaCompra);

    /** Notas de crédito del proveedor con saldo por aplicar. */
    List<DevolucionProveedor> getConCreditoDisponible(Integer proveedor);

    /** Calcula cantidad base, subtotal, ITBIS y total de la línea. */
    static void calcularLinea(DetalleDevolucionProveedor d) {
        BigDecimal cant = d.getCantidad();
        d.setCantidadBase(cant.multiply(d.getFactorConversion()));
        BigDecimal sub = cant.multiply(d.getCostoUnitario()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal itbis = sub.multiply(d.getPorcentajeItbis()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        d.setSubTotal(sub);
        d.setItbis(itbis);
        d.setTotal(sub.add(itbis));
    }
}
