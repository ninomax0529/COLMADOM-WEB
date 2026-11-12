package com.maxsoft.application.servicio.impl.delivery;

import com.maxsoft.application.dto.DeliveryRealizadoDTO;
import com.maxsoft.application.dto.DetalleLiquidacionDTO;
import com.maxsoft.application.dto.LiquidacionResumenDTO;
import com.maxsoft.application.modelo.CuentaMotorista;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.repo.CuentaMotoristaRepo;
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
    private static final int TIPO_VENTA_CONTADO = 1;
    private static final int TIPO_VENTA_LIBRETA = 2;

    private final LiquidacionDeliveryRepo liquidacionRepo;
    private final TipoVentaService tipoVentaService;
    private final ReciboDeIngresoService reciboDeIngresoService;
    private final CajaService cajaService;
    private final CuentaPorCobrarService cuentaPorCobrarService;
    private final EmpresaRepo empresaRepo;
    private final CuentaMotoristaRepo cuentaMotoristaRepo;
    private final com.maxsoft.application.repo.DeliveryRepo deliveryRepo;

    public LiquidacionDeliveryServiceImpl(LiquidacionDeliveryRepo liquidacionRepo,
            TipoVentaService tipoVentaService,
            ReciboDeIngresoService reciboDeIngresoService,
            CajaService cajaService,
            CuentaPorCobrarService cuentaPorCobrarService,
            EmpresaRepo empresaRepo,
            CuentaMotoristaRepo cuentaMotoristaRepo,
            com.maxsoft.application.repo.DeliveryRepo deliveryRepo
    ) {
        this.deliveryRepo = deliveryRepo;
        this.liquidacionRepo = liquidacionRepo;
        this.tipoVentaService = tipoVentaService;
        this.reciboDeIngresoService = reciboDeIngresoService;
        this.cajaService = cajaService;
        this.cuentaPorCobrarService = cuentaPorCobrarService;
        this.empresaRepo = empresaRepo;
        this.cuentaMotoristaRepo = cuentaMotoristaRepo;
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
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

        StringBuilder h = encabezadoTicket("Liquidación delivery #" + l.getCodigo());
        h.append("<hr><div class='c b'>LIQUIDACIÓN DE DELIVERY</div>");
        if (l.isAnulada()) {
            h.append("<div class='anulada'>*** ANULADA ***</div>")
                    .append("<div>Anulada el ").append(l.getFechaAnulacion() != null ? df.format(l.getFechaAnulacion()) : "")
                    .append(" por ").append(esc(l.getUsuarioAnulacion())).append("</div>")
                    .append("<div>Motivo: ").append(esc(l.getMotivoAnulacion())).append("</div>");
        }
        h.append("<hr><table>")
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
                .append(fila("A cobrar (contado):", "RD$ " + String.format("%,.2f", valor(l.getEfectivoEsperado())), true))
                .append(fila("Efectivo recibido:", "RD$ " + String.format("%,.2f", valor(l.getEfectivoRecibido())), true));
        if (l.getTransferenciaRecibida().signum() > 0) {
            h.append(fila("Transferencia:", "RD$ " + String.format("%,.2f", l.getTransferenciaRecibida()), true));
            if (l.getReferenciaTransferencia() != null) {
                h.append(fila("&nbsp;&nbsp;Ref.:", esc(l.getReferenciaTransferencia()), false));
            }
        }
        h.append(fila(diferencia.signum() == 0 ? "Diferencia:" : diferencia.signum() > 0 ? "SOBRANTE:" : "FALTANTE:",
                "RD$ " + String.format("%,.2f", diferencia.abs()), true));
        if (l.getFaltanteCargado().signum() > 0) {
            h.append(fila("Cargado al motorista:", "RD$ " + String.format("%,.2f", l.getFaltanteCargado()), true));
        }
        BigDecimal deuda = getDeudaMotorista(l.getDelivery().getCodigo());
        if (deuda.signum() != 0) {
            h.append(fila(deuda.signum() > 0 ? "Deuda total motorista:" : "Saldo a favor motorista:",
                    "RD$ " + String.format("%,.2f", deuda.abs()), true));
        }
        h.append("</table>");
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

        return liquidar(motorista, codigosFactura, Map.of(), efectivoRecibido, BigDecimal.ZERO, null,
                false, observacion, usuario);
    }

    @Override
    public LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            Map<Integer, Cliente> clientesCredito, BigDecimal efectivoRecibido,
            String observacion, String usuario) {

        return liquidar(motorista, codigosFactura, clientesCredito, efectivoRecibido, BigDecimal.ZERO, null,
                false, observacion, usuario);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiquidacionDelivery liquidar(Delivery motorista, List<Integer> codigosFactura,
            Map<Integer, Cliente> clientesCredito, BigDecimal efectivoRecibido,
            BigDecimal transferenciaRecibida, String referenciaTransferencia,
            boolean cargarFaltante, String observacion, String usuario) {

        Map<Integer, Cliente> credito = clientesCredito != null ? clientesCredito : Map.of();
        BigDecimal transferencia = transferenciaRecibida != null ? transferenciaRecibida : BigDecimal.ZERO;
        if (transferencia.signum() < 0) {
            throw new IllegalArgumentException("La transferencia no puede ser negativa.");
        }

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

        // Pago mixto: lo que se compara con lo esperado es efectivo + transferencia
        BigDecimal diferencia = recibido.add(transferencia).subtract(esperado);

        // Un sobrante o faltante siempre debe quedar explicado
        if (diferencia.signum() != 0 && (observacion == null || observacion.isBlank())) {
            throw new IllegalArgumentException("Debe indicar el motivo del "
                    + (diferencia.signum() > 0 ? "sobrante" : "faltante") + ".");
        }
        BigDecimal faltanteCargado = cargarFaltante && diferencia.signum() < 0
                ? diferencia.abs() : BigDecimal.ZERO;
        String user = usuario != null && !usuario.isBlank() ? usuario : "SISTEMA";

        liquidacion.setFecha(new Date());
        liquidacion.setDelivery(motorista);
        liquidacion.setNombreUsuario(usuario);
        liquidacion.setEfectivoEsperado(esperado);
        liquidacion.setEfectivoRecibido(recibido);
        liquidacion.setTransferenciaRecibida(transferencia);
        liquidacion.setReferenciaTransferencia(transferencia.signum() > 0 && referenciaTransferencia != null
                && !referenciaTransferencia.isBlank() ? referenciaTransferencia.trim() : null);
        liquidacion.setDiferencia(diferencia);
        liquidacion.setFaltanteCargado(faltanteCargado);
        liquidacion.setAnulada(false);
        liquidacion.setTotalFiado(fiado);
        liquidacion.setTotalLiquidado(total);
        liquidacion.setObservacion(observacion != null ? observacion.trim() : null);
        liquidacion.setDetalleLiquidacionDeliveryCollection(detalles);

        LiquidacionDelivery liqDb=liquidacionRepo.save(liquidacion);
        this.reciboDeIngresoService.procesarReciboDeIngresoLiquidacionDelivery(detalles);

        // El faltante que no se perdona queda como deuda del motorista
        if (faltanteCargado.signum() > 0) {
            registrarMovimientoCuenta(motorista, CuentaMotorista.FALTANTE, faltanteCargado, liqDb,
                    "Faltante liquidación #" + liqDb.getCodigo(), user);
        }

        // A la gaveta entra solo el efectivo; la transferencia va al banco.
        if (recibido.signum() > 0) {
            String descripcion = "Liquidación delivery #" + liqDb.getCodigo()
                    + " - Motorista: " + motorista.getNombre()
                    + ". Esperado RD$ " + esperado.toPlainString()
                    + ", efectivo RD$ " + recibido.toPlainString()
                    + (transferencia.signum() > 0 ? ", transferencia RD$ " + transferencia.toPlainString() : "")
                    + (diferencia.signum() != 0
                            ? (diferencia.signum() > 0 ? ", sobrante RD$ " : ", faltante RD$ ")
                            + diferencia.abs().toPlainString()
                            : "")
                    + (faltanteCargado.signum() > 0 ? " (cargado al motorista)" : "");
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    CajaService.TIPO_DELIVERY,
                    recibido,
                    descripcion,
                    user
            );
        }

        return liqDb;
    }

    // ============================================================
    // CONSULTA DE LIQUIDACIONES
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<LiquidacionResumenDTO> getLiquidaciones(Date desde, Date hastaExclusivo, Integer codigoDelivery) {

        return liquidacionRepo.findEntreFechas(desde, hastaExclusivo).stream()
                .filter(l -> codigoDelivery == null || Objects.equals(l.getDelivery().getCodigo(), codigoDelivery))
                .map(this::aResumen)
                .toList();
    }

    private LiquidacionResumenDTO aResumen(LiquidacionDelivery l) {

        int pedidos = 0;
        int aCredito = 0;
        if (l.getDetalleLiquidacionDeliveryCollection() != null) {
            for (DetalleLiquidacionDelivery d : l.getDetalleLiquidacionDeliveryCollection()) {
                pedidos++;
                if (d.getCliente() != null) {
                    aCredito++;
                }
            }
        }
        return new LiquidacionResumenDTO(
                l.getCodigo(), l.getFecha(),
                l.getDelivery().getCodigo(), l.getDelivery().getNombre(),
                l.getNombreUsuario(), pedidos, aCredito,
                valor(l.getTotalLiquidado()), valor(l.getTotalFiado()),
                valor(l.getEfectivoEsperado()), valor(l.getEfectivoRecibido()),
                l.getTransferenciaRecibida(), l.getReferenciaTransferencia(),
                valor(l.getDiferencia()), l.getFaltanteCargado(), l.getObservacion(),
                l.isAnulada(), l.getFechaAnulacion(), l.getMotivoAnulacion(), l.getUsuarioAnulacion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleLiquidacionDTO> getDetalleLiquidacion(Integer codigoLiquidacion) {

        return liquidacionRepo.findDetalles(codigoLiquidacion).stream()
                .map(d -> new DetalleLiquidacionDTO(
                d.getFactura().getCodigo(),
                d.getFactura().getFechaCreacion() != null ? d.getFactura().getFechaCreacion() : d.getFactura().getFecha(),
                d.getFactura().getNombreCliente(),
                d.getTipoPago(),
                valor(d.getMonto()),
                d.getCliente() != null ? d.getCliente().getNombre() : null))
                .toList();
    }

    // ============================================================
    // ANULACIÓN
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiquidacionDelivery anularLiquidacion(Integer codigoLiquidacion, String motivo, String usuario) {

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Indique el motivo de la anulación.");
        }
        String user = usuario != null && !usuario.isBlank() ? usuario : "SISTEMA";
        LiquidacionDelivery l = liquidacionRepo.findById(codigoLiquidacion)
                .orElseThrow(() -> new IllegalArgumentException("Liquidación #" + codigoLiquidacion + " no encontrada."));
        if (l.isAnulada()) {
            throw new IllegalStateException("La liquidación #" + codigoLiquidacion + " ya está anulada.");
        }
        List<DetalleLiquidacionDelivery> detalles = liquidacionRepo.findDetalles(codigoLiquidacion);
        String nota = "Anulación liquidación delivery #" + codigoLiquidacion + ": " + motivo.trim();

        // 1. Validar todo antes de tocar nada
        BigDecimal efectivo = valor(l.getEfectivoRecibido());
        CajaTurno turno = null;
        if (efectivo.signum() > 0) {
            turno = cajaService.obtenerCajaAbierta()
                    .orElseThrow(() -> new IllegalStateException(
                    "Debe abrir una caja: el efectivo de la liquidación (RD$ "
                    + String.format("%,.2f", efectivo) + ") sale de la gaveta."));
            cajaService.validarEfectivoDisponible(turno.getId().intValue(), efectivo,
                    "anular la liquidación #" + codigoLiquidacion);
        }
        for (DetalleLiquidacionDelivery d : detalles) {
            FacturaDeVenta f = d.getFactura();
            if (Boolean.TRUE.equals(f.getAnulada())) {
                throw new IllegalStateException("La factura #" + f.getCodigo()
                        + " se anuló después de liquidarse; esta liquidación ya no se puede anular.");
            }
            if (d.getCliente() != null) {
                List<ReciboDeIngreso> abonos = reciboDeIngresoService.getRecibosVigentesDeFactura(f.getCodigo());
                if (!abonos.isEmpty()) {
                    throw new IllegalStateException("El cliente " + d.getCliente().getNombre()
                            + " ya abonó a la factura #" + f.getCodigo() + " (recibo #" + abonos.get(0).getCodigo()
                            + "). Anule primero ese recibo.");
                }
            }
        }

        // 2. Facturas: vuelven a quedar como estaban antes de liquidar
        for (DetalleLiquidacionDelivery d : detalles) {
            FacturaDeVenta f = d.getFactura();
            if (d.getCliente() != null) {
                // Se había pasado de contado a crédito: sale del estado de cuenta y vuelve a contado
                cuentaPorCobrarService.registrarReversoCredito(f, nota, user);
                f.setTipoVenta(tipoVentaService.getTipoVenta(TIPO_VENTA_CONTADO));
                f.setEstadoFactura(new com.maxsoft.application.modelo.EstadoFactura(ESTADO_FACTURA_DELIVERY));
                f.setFechaActualizacion(new Date());
            } else if (PedidoDeliveryDTO.CONTADO.equals(d.getTipoPago())) {
                // Su recibo de la liquidación se anula: la factura vuelve a deber
                reciboDeIngresoService.anularRecibosLiquidacionDelivery(f.getCodigo(), nota, user);
                f.setEstadoFactura(new com.maxsoft.application.modelo.EstadoFactura(ESTADO_FACTURA_DELIVERY));
                f.setFechaActualizacion(new Date());
            }
            // Las fiadas de origen no cambian: siguen en la libreta del cliente con sus abonos
            d.setAnulada(true);
        }

        // 3. El faltante cargado al motorista se reversa
        if (l.getFaltanteCargado().signum() > 0) {
            registrarMovimientoCuenta(l.getDelivery(), CuentaMotorista.ANULACION_FALTANTE,
                    l.getFaltanteCargado().negate(), l, "Reverso faltante: " + nota, user);
        }

        // 4. Liquidación anulada
        l.setAnulada(true);
        l.setFechaAnulacion(new Date());
        l.setMotivoAnulacion(motivo.trim().length() > 300 ? motivo.trim().substring(0, 300) : motivo.trim());
        l.setUsuarioAnulacion(user);
        LiquidacionDelivery anulada = liquidacionRepo.save(l);

        // 5. Caja: sale el efectivo que había entrado
        if (turno != null) {
            cajaService.registrarMovimientoPos(turno.getId().intValue(), CajaService.TIPO_ANULACION_DELIVERY,
                    efectivo.negate(), nota + " - Motorista: " + l.getDelivery().getNombre(), user);
        }
        return anulada;
    }

    // ============================================================
    // CUENTA DEL MOTORISTA
    // ============================================================
    private CuentaMotorista registrarMovimientoCuenta(Delivery motorista, String tipo, BigDecimal monto,
            LiquidacionDelivery liquidacion, String descripcion, String usuario) {

        CuentaMotorista m = new CuentaMotorista();
        m.setDelivery(motorista);
        m.setFecha(new Date());
        m.setTipo(tipo);
        m.setMonto(monto);
        m.setLiquidacionDelivery(liquidacion);
        m.setDescripcion(descripcion != null && descripcion.length() > 300 ? descripcion.substring(0, 300) : descripcion);
        m.setNombreUsuario(usuario);
        return cuentaMotoristaRepo.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getDeudaMotorista(Integer codigoDelivery) {
        return codigoDelivery == null ? BigDecimal.ZERO : valor(cuentaMotoristaRepo.saldo(codigoDelivery));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, BigDecimal> getDeudasMotoristas() {
        Map<Integer, BigDecimal> deudas = new LinkedHashMap<>();
        for (Object[] fila : cuentaMotoristaRepo.saldos()) {
            deudas.put((Integer) fila[0], valor((BigDecimal) fila[1]));
        }
        return deudas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.maxsoft.application.dto.CuentaMotoristaResumenDTO> getResumenCuentasMotoristas() {

        Map<Integer, Object[]> porMotorista = new LinkedHashMap<>();
        for (Object[] fila : cuentaMotoristaRepo.resumenPorMotorista()) {
            porMotorista.put((Integer) fila[0], fila);
        }
        List<com.maxsoft.application.dto.CuentaMotoristaResumenDTO> resultado = new ArrayList<>();
        for (Delivery d : deliveryRepo.findAll()) {
            Object[] f = porMotorista.get(d.getCodigo());
            BigDecimal cargos = f != null ? valor((BigDecimal) f[1]) : BigDecimal.ZERO;
            BigDecimal descargos = f != null ? valor((BigDecimal) f[2]).abs() : BigDecimal.ZERO;
            resultado.add(new com.maxsoft.application.dto.CuentaMotoristaResumenDTO(
                    d.getCodigo(), d.getNombre(), d.getCelular(),
                    cargos, descargos, cargos.subtract(descargos),
                    f != null ? (Date) f[3] : null));
        }
        // Los que más deben primero
        resultado.sort(java.util.Comparator
                .comparing(com.maxsoft.application.dto.CuentaMotoristaResumenDTO::saldo).reversed()
                .thenComparing(r -> r.nombre() != null ? r.nombre() : ""));
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaMotorista> getCuentaMotorista(Integer codigoDelivery) {
        return codigoDelivery == null ? List.of() : cuentaMotoristaRepo.findMovimientos(codigoDelivery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CuentaMotorista registrarAbonoMotorista(Delivery motorista, BigDecimal monto, String nota, String usuario) {

        if (motorista == null) {
            throw new IllegalArgumentException("Seleccione el motorista.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("Ingrese un monto mayor que cero.");
        }
        CajaTurno turno = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja: el abono entra a la gaveta."));
        BigDecimal deuda = getDeudaMotorista(motorista.getCodigo());
        if (deuda.signum() <= 0) {
            throw new IllegalStateException(motorista.getNombre() + " no tiene deuda pendiente.");
        }
        if (monto.compareTo(deuda) > 0) {
            throw new IllegalArgumentException("El abono (RD$ " + String.format("%,.2f", monto)
                    + ") es mayor que la deuda (RD$ " + String.format("%,.2f", deuda) + ").");
        }
        String user = usuario != null && !usuario.isBlank() ? usuario : "SISTEMA";
        String descripcion = "Abono a faltantes" + (nota != null && !nota.isBlank() ? ": " + nota.trim() : "");

        CuentaMotorista mov = registrarMovimientoCuenta(motorista, CuentaMotorista.ABONO, monto.negate(), null,
                descripcion, user);
        cajaService.registrarMovimientoPos(turno.getId().intValue(), CajaService.TIPO_ABONO_MOTORISTA, monto,
                "Abono motorista " + motorista.getNombre() + " #" + mov.getCodigo() + " - " + descripcion, user);
        return mov;
    }

    @Override
    @Transactional(readOnly = true)
    public String generarComprobanteAbonoHtml(Integer codigoMovimiento) {

        CuentaMotorista m = cuentaMotoristaRepo.findById(codigoMovimiento)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento #" + codigoMovimiento + " no encontrado."));
        if (!CuentaMotorista.ABONO.equals(m.getTipo())) {
            throw new IllegalArgumentException("El movimiento #" + codigoMovimiento + " no es un abono.");
        }
        BigDecimal saldo = getDeudaMotorista(m.getDelivery().getCodigo());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

        StringBuilder h = encabezadoTicket("Abono motorista #" + m.getCodigo());
        h.append("<hr><div class='c b'>RECIBO DE ABONO DEL MOTORISTA</div><div class='c'>(FALTANTES DE DELIVERY)</div><hr><table>")
                .append(fila("No.:", String.valueOf(m.getCodigo()), true))
                .append(fila("Fecha:", m.getFecha() != null ? df.format(m.getFecha()) : "", false))
                .append(fila("Motorista:", esc(m.getDelivery().getNombre()), true))
                .append(fila("Cajero:", esc(m.getNombreUsuario()), false))
                .append("</table><hr><div>").append(esc(m.getDescripcion())).append("</div><hr><table>")
                .append("<tr><td class='b g'>ABONO:</td><td class='r b g'>RD$ ")
                .append(String.format("%,.2f", valor(m.getMonto()).abs())).append("</td></tr>")
                .append(fila("Deuda actual:", "RD$ " + String.format("%,.2f", saldo), true))
                .append("</table><hr><div class='f'>Firma del motorista</div><div class='f'>Recibido por (cajero)</div></body></html>");
        return h.toString();
    }

    /** Inicio del ticket de 80 mm con los datos de la empresa. */
    private StringBuilder encabezadoTicket(String titulo) {

        Empresa empresa = empresaRepo.findFirstByOrderByCodigoAsc().orElse(null);
        StringBuilder h = new StringBuilder();
        h.append("<!doctype html><html><head><meta charset='utf-8'><title>").append(esc(titulo))
                .append("</title><style>@page{size:80mm auto;margin:3mm}body{font-family:'Courier New',monospace;font-size:12px;width:72mm;margin:0}")
                .append(".c{text-align:center}.b{font-weight:bold}.g{font-size:15px}hr{border:0;border-top:1px dashed #000;margin:6px 0}")
                .append("table{width:100%;border-collapse:collapse}td{padding:1px 0;vertical-align:top}td.r{text-align:right}")
                .append(".f{margin-top:36px;border-top:1px solid #000;text-align:center;padding-top:2px}")
                .append(".anulada{border:2px solid #000;text-align:center;font-weight:bold;font-size:16px;padding:4px;margin:6px 0}</style></head><body>");
        if (empresa != null) {
            h.append("<div class='c b'>").append(esc(empresa.getNombre())).append("</div>")
                    .append("<div class='c'>").append(esc(empresa.getDiereccion())).append("</div>")
                    .append("<div class='c'>Tel: ").append(esc(empresa.getTelefono()))
                    .append(" &nbsp;RNC: ").append(esc(empresa.getRnc())).append("</div>");
        }
        return h;
    }
}
