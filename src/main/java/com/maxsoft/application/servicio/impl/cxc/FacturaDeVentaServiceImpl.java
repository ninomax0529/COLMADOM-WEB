/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.cxc;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.evento.VentaDevueltaEvent;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.servicio.interfaces.cxc.CuentaPorCobrarService;
import com.maxsoft.application.servicio.interfaces.cxc.ReciboDeIngresoService;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteVencidoException;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.servicio.interfaces.cxc.FacturaDeVentaService;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.maxsoft.application.repo.FacturaDeventaRepo;
import java.time.LocalDateTime;

@Service
public class FacturaDeVentaServiceImpl implements FacturaDeVentaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaDeVentaServiceImpl.class);

    @Autowired
    private FacturaDeventaRepo facttRepo;

    @Autowired
    private CajaService cajaService;

    @Autowired
    private InventarioService inventarioService;
    @Autowired
    private EntradaDeInventarioService entradaInventarioService;
    @Autowired
    SalidaInventarioService salidaInventarioService;
    @Autowired
    ReciboDeIngresoService reciboDeIngresoService;
    @Autowired
    CuentaPorCobrarService cuentaPorCobrarService;

    @Autowired
    ArticuloService articuloService;
    @Autowired
    AlmacenService almacenService;
    @Autowired
    LoteService loteService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FacturaDeVenta procesarVenta(TicketVenta ticketVenta, String nombreUsuario) {

        // 1. Validación de Caja
        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja antes de registrar movimientos de POS"));

        // 2. Validaciones de Negocio e Inventario
        if (ticketVenta.getItems() == null || ticketVenta.getItems().isEmpty()) {
            throw new IllegalArgumentException("La factura no tiene detalle.");
        }

        Almacen alm = this.almacenService.getAlmacen(2);
        java.util.Map<Integer, BigDecimal> requeridoBasePorArticulo = new java.util.LinkedHashMap<>();

        for (DetalleFacturaDeVenta det : ticketVenta.getItems()) {

            BigDecimal cantidad = det.getCantidad() != null ? det.getCantidad() : BigDecimal.ZERO;

            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException("El artículo '"
                        + (det.getDescripcionArticulo() != null ? det.getDescripcionArticulo() : "Desconocido")
                        + "' tiene la cantidad en cero o no válida.");
            }

            // Asignación de almacén predeterminado
            det.setAlmacen(alm);
            det.setNombreAlmacen(alm.getNombre());

            // Acumular en UNIDAD BASE por artículo (un mismo artículo puede
            // venir en varias presentaciones: unidad, caja, fardo...)
            if (det.getArticulo() != null && det.getArticulo().getCodigo() != null
                    && Boolean.TRUE.equals(det.getArticulo().getInventariable())) {

                BigDecimal factor = (det.getArticuloEmpaque() != null
                        && det.getArticuloEmpaque().getFactorConversion() != null
                        && det.getArticuloEmpaque().getFactorConversion().signum() > 0)
                        ? det.getArticuloEmpaque().getFactorConversion()
                        : BigDecimal.ONE;
                requeridoBasePorArticulo.merge(det.getArticulo().getCodigo(),
                        cantidad.multiply(factor), BigDecimal::add);
            }
        }

        // Validar stock antes de guardar nada (respeta "permitir venta sin existencia")
        for (java.util.Map.Entry<Integer, BigDecimal> req : requeridoBasePorArticulo.entrySet()) {
            inventarioService.validarStockDisponible(req.getKey(), req.getValue().doubleValue());
        }

        // Lotes vencidos: si lo vigente no alcanza y habría que vender de un lote vencido,
        // se bloquea salvo que un supervisor lo haya autorizado (el POS lo pide y reintenta).
        java.util.List<String> ventaDeVencidos = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Integer, BigDecimal> req : requeridoBasePorArticulo.entrySet()) {
            Articulo art = articuloService.buscarPorCodigo(req.getKey());
            String aviso = loteService.describirVentaDeVencidos(art, alm, req.getValue());
            if (aviso != null) {
                ventaDeVencidos.add(aviso);
            }
        }
        String autorizadoVencido = ticketVenta.getAutorizadoLoteVencidoPor();
        if (!ventaDeVencidos.isEmpty() && (autorizadoVencido == null || autorizadoVencido.isBlank())) {
            throw new LoteVencidoException(ventaDeVencidos);
        }

        // 3. Mapeo y Construcción de la Factura
        FacturaDeVenta factura = new FacturaDeVenta();
        factura.setUsuario(1);
        factura.setEstadoFactura(ticketVenta.getEstadoFactura());
        factura.setTipoVenta(ticketVenta.getTipoVenta());
        factura.setCliente(ticketVenta.getCliente());
        factura.setNombreCliente(ticketVenta.getNombreCliente());
        factura.setDireccion(ticketVenta.getDireccion());
        factura.setAnulada(Boolean.FALSE);

        if (ticketVenta.getDelivery() != null) {
            factura.setDelivery(ticketVenta.getDelivery());
            factura.setNombreDelivery(ticketVenta.getDelivery().getNombre());
        } else {
            factura.setDelivery(null);
            factura.setNombreDelivery("N/A");
        }

        factura.setFecha(new Date());
        factura.setFechaCreacion(new Date());
        factura.setFechaActualizacion(new Date());
        factura.setNombreUsuario(nombreUsuario);

        factura.setSubTotal(ticketVenta.getTotalAmount());
        factura.setTotal(ticketVenta.getTotalAmount());
        factura.setTotalItbis(BigDecimal.ZERO);
        factura.setPorcientoDescuento(BigDecimal.ZERO);
        factura.setPorcientoItbis(BigDecimal.ZERO);

        if (factura.getTipoVenta().getCodigo() == 1 && factura.getDelivery() == null) {

            factura.setTotalDescuento(BigDecimal.ZERO);
            factura.setTotalPendiente(BigDecimal.ZERO);
            factura.setTotalPagado(ticketVenta.getTotalAmount());

        } else if (factura.getTipoVenta().getCodigo() == 1 && factura.getDelivery() != null) {

            factura.setTotalDescuento(BigDecimal.ZERO);
            factura.setTotalPendiente(ticketVenta.getTotalAmount());
            factura.setTotalPagado(BigDecimal.ZERO);

        } else {

            factura.setTotalDescuento(BigDecimal.ZERO);
            factura.setTotalPendiente(ticketVenta.getTotalAmount());
            factura.setTotalPagado(BigDecimal.ZERO);
        }

        // Asignar bidireccionalidad en los ítems
        ticketVenta.getItems().forEach(item -> {

            item.setFactura(factura);
            item.setCantidadDevuelta(BigDecimal.ZERO);
//            item.setNombreUnidad(item.getArticulo().getUnidadBase().getDescripcion());
//            item.setUnidad(item.getArticulo().getUnidadBase());

            item.setFactorConversion(item.getArticuloEmpaque().getFactorConversion());
            item.setCantidadFisicaBase(item.getFactorConversion().multiply(item.getCantidad()));

            // Costo de la línea en la unidad vendida: costo base actual (leído de la BD,
            // no el que tenía el POS en memoria) × factor del empaque.
            BigDecimal costoBase = BigDecimal.ZERO;
            if (item.getArticulo() != null && item.getArticulo().getCodigo() != null) {
                Articulo actual = articuloService.buscarPorCodigo(item.getArticulo().getCodigo());
                if (actual != null && actual.getPrecioCompra() != null) {
                    costoBase = actual.getPrecioCompra();
                }
            }
            BigDecimal factorCosto = item.getFactorConversion() != null && item.getFactorConversion().signum() > 0
                    ? item.getFactorConversion() : BigDecimal.ONE;
            BigDecimal precioCompra = costoBase.multiply(factorCosto).setScale(2, java.math.RoundingMode.HALF_UP);

            item.setPrecioCompra(precioCompra);
            item.setCodigo(null); // Asegura que Hibernate lo interprete como inserción
        });

        factura.setDetalleFacturaDeVentaCollection(ticketVenta.getItems());
        factura.setAutorizadoLoteVencidoPor(ventaDeVencidos.isEmpty() ? null : autorizadoVencido);

        // 4. Persistencia de la factura
        FacturaDeVenta facturaGuardada = facttRepo.saveAndFlush(factura);

        // 4.1 Venta a crédito (libreta, mostrador o delivery): débito en el estado de
        // cuenta del cliente. Valida el límite de crédito; si se excede, se revierte todo.
        if (facturaGuardada.getTipoVenta().getCodigo() == CuentaPorCobrarService.TIPO_VENTA_CREDITO) {
            cuentaPorCobrarService.registrarFacturaCredito(facturaGuardada, nombreUsuario);
        }

        // 5. Generar la Salida de Inventario (Nombre de variable en minúscula)
        List<DetalleFacturaDeVenta> listaDet = this.facttRepo.getDetalle(factura.getCodigo());

        this.salidaInventarioService.crearSalidaPorVenta(facturaGuardada, listaDet);

        if (facturaGuardada.getTipoVenta().getCodigo() == 1 && facturaGuardada.getDelivery() == null) {// de contado y en el mostrador

            ReciboDeIngreso reciGuardado = this.reciboDeIngresoService.procesarReciboDeIngresoPos(facturaGuardada);

            // 6. Registrar Movimiento de Caja
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    CajaService.TIPO_VENTA,
                    reciGuardado.getTotalPagado(),
                    "Ingreso por venta #" + facturaGuardada.getCodigo() + " Recibo de Ingreso #" + reciGuardado.getCodigo(),
                    nombreUsuario
            );
        }

        // 7. Preparar DTOs para Eventos de Dominio
//        List<VentaRealizadaEvent.ItemVentaDto> itemsDto = facturaGuardada.getDetalleFacturaDeVentaCollection().stream()
//                .filter(item -> item.getArticulo() != null && item.getArticulo().getCodigo() != null)
//                .map(item -> {
//                    Integer idArticulo = item.getArticulo().getCodigo();
//                    Integer idAlmacen = (item.getAlmacen() != null) ? item.getAlmacen().getCodigo() : 2;
//
//                    return new VentaRealizadaEvent.ItemVentaDto(idArticulo, item.getCantidad(), idAlmacen);
//                })
//                .collect(Collectors.toList());
        // Publicación del evento
        // eventPublisher.publishEvent(new VentaRealizadaEvent(facturaGuardada.getCodigo(), itemsDto));
        return facturaGuardada;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FacturaDeVenta anularVenta(Integer idFactura, String motivoAnulacion,
            String nombreUsuario
    ) {

        // 1. Validar existencia y estado de la factura
        FacturaDeVenta factura = facttRepo.findById(idFactura)
                .orElseThrow(() -> new IllegalArgumentException("La factura ID " + idFactura + " no existe."));

        if (Boolean.TRUE.equals(factura.getAnulada())) {
            throw new IllegalStateException("La factura #" + idFactura + " ya se encuentra anulada.");
        }

        // Con devoluciones parciales, anular devolvería otra vez al inventario y a la
        // caja lo ya devuelto. Se debe devolver el resto de los artículos.
        if (factura.getTotalDevuelto().signum() > 0) {
            throw new IllegalStateException("La factura #" + idFactura + " tiene devoluciones registradas (RD$ "
                    + String.format("%,.2f", factura.getTotalDevuelto())
                    + "). En lugar de anularla, devuelva los artículos restantes.");
        }

        // 2. Verificar que la caja actual esté abierta para registrar la salida de dinero
        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja para procesar la anulación."));

        // 2.1 Si era a crédito, el cliente deja de deber lo que quedaba pendiente
        if (cuentaPorCobrarService.facturaEnEstadoDeCuenta(factura)) {
            cuentaPorCobrarService.registrarAnulacionFactura(factura, motivoAnulacion, nombreUsuario);
        }

        // 3. Marcar factura como ANULADA
        factura.setAnulada(true);
        factura.setFechaActualizacion(new Date());
        factura.setFechaAnulada(new Date());

        FacturaDeVenta facturaAnulada = facttRepo.save(factura);
        List<DetalleFacturaDeVenta> listaDetFactura = facttRepo.getDetalle(facturaAnulada.getCodigo());

        // 5. Mapear ítems inventariables para el evento de devolución
//        List<DetalleFacturaDeVenta> itemsDto = listaDetFactura
//                .stream()
//                .filter(item -> Boolean.TRUE.equals(item.getArticulo().getInventariable()))
//                .map(item -> item)
//                .collect(Collectors.toList());
        this.entradaInventarioService.crearEntradaPorAnulacionVenta(factura, listaDetFactura);

        // 4. Revertir de caja solo el efectivo que realmente se cobró. Una venta
        // fiada o un delivery sin liquidar no entraron a la gaveta.
        BigDecimal cobrado = facturaAnulada.getTotalPagado() != null ? facturaAnulada.getTotalPagado() : BigDecimal.ZERO;
        if (cobrado.signum() > 0) {
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    CajaService.TIPO_ANULACION_VENTA,
                    cobrado.negate(),
                    "Anulación de Venta #" + facturaAnulada.getCodigo() + ". Motivo: " + motivoAnulacion,
                    nombreUsuario
            );
        }

        return facturaAnulada;
    }

    /**
     * Devolución (parcial o total) de artículos de una factura.
     * <p>
     * El monto devuelto (a precio cobrado) se aplica primero a lo que la
     * factura tenga pendiente y solo el resto se reembolsa en efectivo:
     * <ul>
     * <li>Contado en mostrador (ya pagada): todo se reembolsa de la caja.</li>
     * <li>Fiado: rebaja la deuda del cliente (estado de cuenta); si había
     * abonado más de lo que queda, el excedente se le devuelve en efectivo.</li>
     * <li>Delivery sin liquidar: rebaja lo que el motorista debe entregar;
     * no sale dinero de la caja.</li>
     * </ul>
     * Todo en una sola transacción: si algo falla, no se aplica nada.
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public FacturaDeVenta procesarDevolucion(SolicitudDevolucionDto solicitud) {

        if (solicitud == null || solicitud.getIdFactura() == null) {
            throw new IllegalArgumentException("Indique la factura a devolver.");
        }
        if (solicitud.getItems() == null || solicitud.getItems().isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un artículo para devolver.");
        }
        String usuario = solicitud.getUsuario() != null ? solicitud.getUsuario() : "Admin";

        // 1. Factura
        FacturaDeVenta factura = facttRepo.findById(solicitud.getIdFactura())
                .orElseThrow(() -> new IllegalArgumentException("La factura ID " + solicitud.getIdFactura() + " no existe."));

        if (Boolean.TRUE.equals(factura.getAnulada())) {
            throw new IllegalStateException("No se pueden realizar devoluciones sobre una factura anulada.");
        }

        // 2. Validar ítems y calcular el monto devuelto (precio realmente cobrado por unidad)
        BigDecimal montoDevuelto = BigDecimal.ZERO;
        List<DetalleFacturaDeVenta> listaDetFactura = facttRepo.getDetalle(factura.getCodigo());

        for (SolicitudDevolucionDto.ItemDevolucionDto itemDev : solicitud.getItems()) {

            DetalleFacturaDeVenta detalle = listaDetFactura.stream()
                    .filter(d -> d.getCodigo().equals(itemDev.getIdDetalleFactura()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El detalle enviado no pertenece a la factura #" + factura.getCodigo()));

            BigDecimal cantidadDev = itemDev.getCantidadADevolver() != null
                    ? new BigDecimal(itemDev.getCantidadADevolver().toString()) : BigDecimal.ZERO;
            BigDecimal devueltoPrevio = detalle.getCantidadDevuelta() != null ? detalle.getCantidadDevuelta() : BigDecimal.ZERO;
            BigDecimal disponible = detalle.getCantidad().subtract(devueltoPrevio);

            if (cantidadDev.signum() <= 0 || cantidadDev.compareTo(disponible) > 0) {
                throw new IllegalArgumentException("Cantidad inválida para " + detalle.getArticulo().getDescripcion()
                        + ". Máximo disponible para devolver: " + disponible.stripTrailingZeros().toPlainString());
            }

            detalle.setCantidadDevuelta(devueltoPrevio.add(cantidadDev));

            montoDevuelto = montoDevuelto.add(precioUnitarioCobrado(detalle).multiply(cantidadDev));
        }
        montoDevuelto = montoDevuelto.setScale(2, java.math.RoundingMode.HALF_UP);

        // 3. Repartir: primero rebaja lo pendiente, el resto se reembolsa en efectivo
        BigDecimal total = factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO;
        BigDecimal pagado = factura.getTotalPagado() != null ? factura.getTotalPagado() : BigDecimal.ZERO;
        BigDecimal pendienteActual = total.subtract(factura.getTotalDevuelto()).subtract(pagado).max(BigDecimal.ZERO);

        BigDecimal aplicadoAPendiente = montoDevuelto.min(pendienteActual);
        BigDecimal reembolsoEfectivo = montoDevuelto.subtract(aplicadoAPendiente);

        CajaTurno turnoActual = null;
        if (reembolsoEfectivo.signum() > 0) {
            turnoActual = cajaService.obtenerCajaAbierta()
                    .orElseThrow(() -> new IllegalStateException("Debe abrir una caja: se reembolsarán RD$ "
                    + String.format("%,.2f", reembolsoEfectivo) + " en efectivo."));
            // No se puede devolver más efectivo del que hay en la gaveta
            cajaService.validarEfectivoDisponible(turnoActual.getId().intValue(), reembolsoEfectivo,
                    "reembolsar la devolución de la factura #" + factura.getCodigo());
        }

        boolean esDevolucionTotal = listaDetFactura.stream()
                .allMatch(d -> {
                    BigDecimal dev = d.getCantidadDevuelta() != null ? d.getCantidadDevuelta() : BigDecimal.ZERO;
                    return dev.compareTo(d.getCantidad()) >= 0;
                });

        // 4. Estado de cuenta del cliente (fiado): el crédito por lo devuelto
        if (aplicadoAPendiente.signum() > 0 && cuentaPorCobrarService.facturaEnEstadoDeCuenta(factura)) {
            cuentaPorCobrarService.registrarDevolucion(factura, aplicadoAPendiente, solicitud.getMotivo(), usuario);
        }

        // 5. Factura
        factura.setTotalDevuelto(factura.getTotalDevuelto().add(montoDevuelto));
        factura.setTotalPagado(pagado.subtract(reembolsoEfectivo).max(BigDecimal.ZERO));
        factura.setTotalPendiente(pendienteActual.subtract(aplicadoAPendiente));
        if (esDevolucionTotal) {
            factura.setAnulada(true);
            factura.setFechaAnulada(new Date());
        }
        factura.setFechaActualizacion(new Date());
        FacturaDeVenta facturaGuardada = facttRepo.save(factura);

        // 6. Inventario: entra lo devuelto
        EntradaInventario entradaCreada = entradaInventarioService.crearEntradaPorDevolucion(facturaGuardada, solicitud);
        log.info("Devolución factura #{}: entrada {} , devuelto RD$ {}, a pendiente RD$ {}, efectivo RD$ {}",
                facturaGuardada.getCodigo(), entradaCreada != null ? entradaCreada.getCodigo() : null,
                montoDevuelto, aplicadoAPendiente, reembolsoEfectivo);

        // 7. Caja: solo el efectivo que realmente se devuelve
        if (turnoActual != null) {
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    CajaService.TIPO_DEVOLUCION,
                    reembolsoEfectivo.negate(),
                    (esDevolucionTotal ? "Devolución Total" : "Devolución Parcial")
                    + " Factura #" + facturaGuardada.getCodigo() + ". Motivo: " + solicitud.getMotivo(),
                    usuario
            );
        }

        return facturaGuardada;
    }

    /** Precio por unidad realmente cobrado en la línea (incluye descuentos si los hubo). */
    private BigDecimal precioUnitarioCobrado(DetalleFacturaDeVenta detalle) {

        BigDecimal cantidad = detalle.getCantidad();
        if (detalle.getTotal() != null && cantidad != null && cantidad.signum() > 0) {
            return detalle.getTotal().divide(cantidad, 4, java.math.RoundingMode.HALF_UP);
        }
        if (detalle.getPrecioVenta() != null) {
            return detalle.getPrecioVenta();
        }
        throw new IllegalStateException("La línea de " + detalle.getDescripcionArticulo() + " no tiene precio de venta.");
    }

    @Override
    public FacturaDeVenta guardar(FacturaDeVenta obj
    ) {
        return facttRepo.save(obj);
    }

    @Override
    public List<FacturaDeVenta> getLista() {
        return facttRepo.findAll();
    }

    @Override
    public List<DetalleFacturaDeVenta> getDetalle(int obj
    ) {
        return facttRepo.getDetalle(obj);
    }

    @Override
    public FacturaDeVenta getFactura(int codigo
    ) {

        return facttRepo.getFactura(codigo);

    }

    @Override
    public List<FacturaDeVenta> getListaFactPendiente(int cliente
    ) {

        return facttRepo.getListaFactPendiente(cliente);
    }

}
