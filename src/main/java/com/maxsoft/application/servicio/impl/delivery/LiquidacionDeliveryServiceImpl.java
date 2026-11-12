package com.maxsoft.application.servicio.impl.delivery;

import com.maxsoft.application.dto.DeliveryRealizadoDTO;
import com.maxsoft.application.dto.PedidoDeliveryDTO;
import com.maxsoft.application.dto.PendientesMotoristaDTO;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.Empresa;
import com.maxsoft.application.repo.EmpresaRepo;
import java.text.SimpleDateFormat;
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
    private final EmpresaRepo empresaRepo;

    public LiquidacionDeliveryServiceImpl(LiquidacionDeliveryRepo liquidacionRepo,
            TipoVentaService tipoVentaService,
            ReciboDeIngresoService reciboDeIngresoService,
            CajaService cajaService,
            CuentaPorCobrarService cuentaPorCobrarService,
            EmpresaRepo empresaRepo
    ) {
        this.liquidacionRepo = liquidacionRepo;
        this.tipoVentaService = tipoVentaService;
        this.reciboDeIngresoService = reciboDeIngresoService;
        this.cajaService = cajaService;
        this.cuentaPorCobrarService = cuentaPorCobrarService;
        this.empresaRepo = empresaRepo;
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
                f.getCliente(),
                f.getFechaCreacion() // con hora; "fecha" es solo el día
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, PendientesMotoristaDTO> getPendientesPorMotorista() {

        Map<Integer, PendientesMotoristaDTO> resumen = new LinkedHashMap<>();
        for (FacturaDeVenta f : liquidacionRepo.findPendientesTodos(ESTADO_FACTURA_DELIVERY)) {
            Integer motorista = f.getDelivery().getCodigo();
            PendientesMotoristaDTO actual = resumen.getOrDefault(motorista, PendientesMotoristaDTO.VACIO);
            resumen.put(motorista, new PendientesMotoristaDTO(actual.pedidos() + 1,
                    actual.monto().add(netoFactura(f))));
        }
        return resumen;
    }

    @Override
    @Transactional(readOnly = true)
    public String generarComprobanteHtml(Integer codigoLiquidacion) {

        LiquidacionDelivery l = liquidacionRepo.findById(codigoLiquidacion)
                .orElseThrow(() -> new IllegalArgumentException("Liquidación #" + codigoLiquidacion + " no encontrada."));
        Empresa empresa = empresaRepo.findFirstByOrderByCodigoAsc().orElse(null);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

        StringBuilder h = new StringBuilder();
        h.append("<!doctype html><html><head><meta charset='utf-8'><title>Liquidación delivery #").append(l.getCodigo())
                .append("</title><style>@page{size:80mm auto;margin:3mm}body{font-family:'Courier New',monospace;font-size:12px;width:72mm;margin:0}")
                .append(".c{text-align:center}.b{font-weight:bold}.g{font-size:15px}hr{border:0;border-top:1px dashed #000;margin:6px 0}")
                .append("table{width:100%;border-collapse:collapse}td{padding:1px 0;vertical-align:top}td.r{text-align:right}")
                .append(".f{margin-top:36px;border-top:1px solid #000;text-align:center;padding-top:2px}</style></head><body>");
        if (empresa != null) {
            h.append("<div class='c b'>").append(esc(empresa.getNombre())).append("</div>")
                    .append("<div class='c'>").append(esc(empresa.getDiereccion())).append("</div>")
                    .append("<div class='c'>Tel: ").append(esc(empresa.getTelefono()))
                    .append(" &nbsp;RNC: ").append(esc(empresa.getRnc())).append("</div>");
        }
        h.append("<hr><div class='c b'>LIQUIDACIÓN DE DELIVERY</div><hr><table>")
                .append("<tr><td>No.:</td><td class='r b'>").append(l.getCodigo()).append("</td></tr>")
                .append("<tr><td>Fecha:</td><td class='r'>").append(l.getFecha() != null ? df.format(l.getFecha()) : "").append("</td></tr>")
                .append("<tr><td>Motorista:</td><td class='r b'>").append(esc(l.getDelivery() != null ? l.getDelivery().getNombre() : "")).append("</td></tr>")
                .append("<tr><td>Cajero:</td><td class='r'>").append(esc(l.getNombreUsuario())).append("</td></tr>")
                .append("</table><hr><table>")
                .append("<tr><td class='b'>Factura</td><td class='b'>Pago</td><td class='r b'>Monto</td></tr>");

        List<DetalleLiquidacionDelivery> detalles = l.getDetalleLiquidacionDeliveryCollection() != null
                ? new ArrayList<>(l.getDetalleLiquidacionDeliveryCollection()) : new ArrayList<>();
        for (DetalleLiquidacionDelivery d : detalles) {
            boolean aCredito = d.getCliente() != null;
            h.append("<tr><td>#").append(d.getFactura() != null ? d.getFactura().getCodigo() : "").append("</td><td>")
                    .append(aCredito ? "CRÉDITO" : esc(d.getTipoPago())).append("</td><td class='r'>")
                    .append(String.format("%,.2f", valor(d.getMonto()))).append("</td></tr>");
            if (aCredito) {
                h.append("<tr><td colspan='3'>&nbsp;&nbsp;a nombre de ").append(esc(d.getCliente().getNombre())).append("</td></tr>");
            }
        }

        BigDecimal diferencia = valor(l.getDiferencia());
        h.append("</table><hr><table>")
                .append(fila("Pedidos:", String.valueOf(detalles.size()), false))
                .append(fila("Total liquidado:", "RD$ " + String.format("%,.2f", valor(l.getTotalLiquidado())), false))
                .append(fila("Fiado / crédito:", "RD$ " + String.format("%,.2f", valor(l.getTotalFiado())), false))
                .append(fila("Efectivo esperado:", "RD$ " + String.format("%,.2f", valor(l.getEfectivoEsperado())), true))
                .append(fila("Efectivo recibido:", "RD$ " + String.format("%,.2f", valor(l.getEfectivoRecibido())), true))
                .append(fila(diferencia.signum() == 0 ? "Diferencia:" : diferencia.signum() > 0 ? "SOBRANTE:" : "FALTANTE:",
                        "RD$ " + String.format("%,.2f", diferencia.abs()), true))
                .append("</table>");
        if (l.getObservacion() != null && !l.getObservacion().isBlank()) {
            h.append("<hr><div>Observación:</div><div>").append(esc(l.getObservacion())).append("</div>");
        }
        h.append("<hr><div class='f'>Firma del motorista</div><div class='f'>Recibido por (cajero)</div></body></html>");
        return h.toString();
    }

    private static String fila(String etiqueta, String valor, boolean resaltar) {
        String c = resaltar ? " class='b'" : "";
        return "<tr><td" + c + ">" + etiqueta + "</td><td class='r" + (resaltar ? " b" : "") + "'>" + valor + "</td></tr>";
    }

    private static BigDecimal valor(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
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
