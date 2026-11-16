package com.maxsoft.application.servicio.impl.compra;

import com.maxsoft.application.modelo.AplicacionCreditoProveedor;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DetalleDevolucionProveedor;
import com.maxsoft.application.modelo.DetalleReciboDeEgreso;
import com.maxsoft.application.modelo.DevolucionProveedor;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.AplicacionCreditoProveedorRepo;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.DetalleDevolucionProveedorRepo;
import com.maxsoft.application.repo.DevolucionProveedorRepo;
import com.maxsoft.application.repo.LoteAlmacenRepo;
import com.maxsoft.application.servicio.interfaces.compra.DevolucionProveedorService;
import com.maxsoft.application.servicio.interfaces.cxp.FacturaDeCompraService;
import com.maxsoft.application.servicio.interfaces.cxp.ReciboDeEgresoService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevolucionProveedorServiceImpl implements DevolucionProveedorService {

    private final DevolucionProveedorRepo repo;
    private final DetalleDevolucionProveedorRepo detalleRepo;
    private final AplicacionCreditoProveedorRepo aplicacionRepo;
    private final ArticuloRepo articuloRepo;
    private final LoteAlmacenRepo loteAlmacenRepo;
    private final LoteService loteService;
    private final ArticuloAlmacenService articuloAlmacenService;
    private final SalidaInventarioService salidaService;
    private final EntradaDeInventarioService entradaService;
    private final FacturaDeCompraService facturaCompraService;
    private final ReciboDeEgresoService reciboEgresoService;
    private final CajaService cajaService;

    public DevolucionProveedorServiceImpl(DevolucionProveedorRepo repo, DetalleDevolucionProveedorRepo detalleRepo,
            AplicacionCreditoProveedorRepo aplicacionRepo, ArticuloRepo articuloRepo, LoteAlmacenRepo loteAlmacenRepo,
            LoteService loteService, ArticuloAlmacenService articuloAlmacenService,
            SalidaInventarioService salidaService, EntradaDeInventarioService entradaService,
            FacturaDeCompraService facturaCompraService, ReciboDeEgresoService reciboEgresoService,
            CajaService cajaService) {
        this.repo = repo;
        this.detalleRepo = detalleRepo;
        this.aplicacionRepo = aplicacionRepo;
        this.articuloRepo = articuloRepo;
        this.loteAlmacenRepo = loteAlmacenRepo;
        this.loteService = loteService;
        this.articuloAlmacenService = articuloAlmacenService;
        this.salidaService = salidaService;
        this.entradaService = entradaService;
        this.facturaCompraService = facturaCompraService;
        this.reciboEgresoService = reciboEgresoService;
        this.cajaService = cajaService;
    }

    // ============================================================
    // REGISTRAR
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevolucionProveedor registrar(DevolucionProveedor dev, List<DetalleDevolucionProveedor> detalles,
            String usuario) {

        String user = usuario(usuario);
        validarCabecera(dev);
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La devolución no tiene artículos.");
        }

        // 1. Validar y calcular líneas
        BigDecimal sub = BigDecimal.ZERO;
        BigDecimal itbis = BigDecimal.ZERO;
        for (DetalleDevolucionProveedor d : detalles) {
            if (d.getArticulo() == null || d.getArticuloEmpaque() == null) {
                throw new IllegalArgumentException("Hay una línea sin artículo o sin empaque.");
            }
            if (d.getCantidad().signum() <= 0) {
                throw new IllegalArgumentException("La cantidad de " + d.getArticulo().getDescripcion()
                        + " debe ser mayor que cero.");
            }
            if (d.getCostoUnitario().signum() < 0) {
                throw new IllegalArgumentException("El costo de " + d.getArticulo().getDescripcion()
                        + " no puede ser negativo.");
            }
            Articulo art = articuloRepo.findById(d.getArticulo().getCodigo()).orElse(d.getArticulo());
            d.setArticulo(art);
            if (d.getDescripcionArticulo() == null) {
                d.setDescripcionArticulo(art.getDescripcion());
            }
            d.setFactorConversion(d.getArticuloEmpaque().getFactorConversion());
            if (art.isControlaLote()) {
                if (d.getLote() == null) {
                    throw new IllegalArgumentException("Indique el lote que se devuelve de " + art.getDescripcion() + ".");
                }
                if (!Objects.equals(d.getLote().getArticulo().getCodigo(), art.getCodigo())) {
                    throw new IllegalArgumentException("El lote " + d.getLote().getNumeroLote()
                            + " no es de " + art.getDescripcion() + ".");
                }
            } else {
                d.setLote(null);
            }
            DevolucionProveedorService.calcularLinea(d);
            sub = sub.add(d.getSubTotal());
            itbis = itbis.add(d.getItbis());
        }
        validarExistencia(dev, detalles);

        // 2. Cabecera y líneas
        LocalDateTime ahora = LocalDateTime.now();
        if (dev.getFecha() == null) {
            dev.setFecha(ahora);
        }
        dev.setFechaCreacion(ahora);
        dev.setUsuario(user);
        dev.setSubTotal(sub);
        dev.setItbis(itbis);
        dev.setTotal(sub.add(itbis));
        dev.setCreditoAplicado(BigDecimal.ZERO);
        dev.setCreditoDisponible(BigDecimal.ZERO);
        dev.setAnulada(false);
        FacturaDeCompra facturaElegida = dev.getFacturaCompra();
        if (facturaElegida != null && !DevolucionProveedor.NOTA_CREDITO.equals(dev.getTipoLiquidacion())) {
            // en cambio/efectivo solo queda como referencia
            facturaElegida = null;
        }
        dev = repo.saveAndFlush(dev);
        for (DetalleDevolucionProveedor d : detalles) {
            d.setCodigo(null);
            d.setDevolucion(dev);
        }

        // 3. Lotes nuevos de la reposición (antes de guardar las líneas)
        if (DevolucionProveedor.CAMBIO.equals(dev.getTipoLiquidacion())) {
            for (DetalleDevolucionProveedor d : detalles) {
                if (d.getArticulo().isControlaLote()) {
                    BigDecimal costoBase = d.getCostoUnitario().signum() > 0
                            ? d.getCostoUnitario().divide(d.getFactorConversion(), 4, RoundingMode.HALF_UP) : null;
                    Lote nuevo = loteService.obtenerOCrearLote(d.getArticulo(), d.getNumeroLoteNuevo(),
                            d.getFechaVencimientoNueva(), costoBase, dev.getProveedor(), dev.getNumero(), user);
                    if (d.getLote() != null && Objects.equals(nuevo.getCodigo(), d.getLote().getCodigo())) {
                        throw new IllegalArgumentException("La reposición de " + d.getArticulo().getDescripcion()
                                + " debe entrar en un lote distinto al devuelto.");
                    }
                    d.setLoteNuevo(nuevo);
                    d.setNumeroLoteNuevo(nuevo.getNumeroLote());
                    d.setFechaVencimientoNueva(nuevo.getFechaVencimiento());
                }
            }
        }
        detalleRepo.saveAll(detalles);

        // 4. Sale la mercancía de sus lotes
        SalidaInventario salida = salidaService.crearSalidaPorDevolucionProveedor(dev, detalles, false, user);
        dev.setSalidaInventario(salida.getCodigo());

        // 5. Liquidación
        switch (dev.getTipoLiquidacion()) {
            case DevolucionProveedor.NOTA_CREDITO -> {
                dev.setCreditoDisponible(dev.getTotal());
                dev = repo.saveAndFlush(dev);
                if (facturaElegida != null && dev.getTotal().signum() > 0) {
                    BigDecimal pendiente = getPendienteFactura(facturaElegida.getCodigo());
                    BigDecimal monto = pendiente.min(dev.getTotal());
                    if (monto.signum() > 0) {
                        aplicar(dev, facturaElegida.getCodigo(), monto, user);
                    }
                }
            }
            case DevolucionProveedor.CAMBIO -> {
                EntradaInventario entrada = entradaService.crearEntradaPorDevolucionProveedor(dev, detalles, true, user);
                dev.setEntradaInventario(entrada.getCodigo());
            }
            case DevolucionProveedor.EFECTIVO -> {
                CajaTurno turno = cajaService.obtenerCajaAbierta().orElseThrow(() -> new IllegalStateException(
                        "Debe abrir una caja: el reembolso en efectivo entra a la gaveta."));
                dev.setCajaTurno(turno.getId().intValue());
                if (dev.getTotal().signum() > 0) {
                    cajaService.registrarMovimientoPos(turno.getId().intValue(), CajaService.TIPO_REEMBOLSO_PROVEEDOR,
                            dev.getTotal(), "Reembolso de " + dev.getProveedor().getNombre()
                            + " por devolución " + dev.getNumero(), user);
                }
            }
            default -> throw new IllegalArgumentException("Tipo de liquidación desconocido: " + dev.getTipoLiquidacion());
        }
        return repo.saveAndFlush(dev);
    }

    private void validarCabecera(DevolucionProveedor dev) {
        if (dev.getProveedor() == null) {
            throw new IllegalArgumentException("Seleccione el proveedor.");
        }
        if (dev.getAlmacen() == null) {
            throw new IllegalArgumentException("Seleccione el almacén de donde sale la mercancía.");
        }
        if (dev.getTipoLiquidacion() == null) {
            throw new IllegalArgumentException("Indique cómo liquida el proveedor la devolución.");
        }
        if (dev.getMotivo() == null) {
            dev.setMotivo(DevolucionProveedor.MOTIVO_OTRO);
        }
        if (dev.getFacturaCompra() != null) {
            FacturaDeCompra f = facturaCompraService.getFactura(dev.getFacturaCompra().getCodigo());
            if (f.getProveedor() == null || !Objects.equals(f.getProveedor().getCodigo(), dev.getProveedor().getCodigo())) {
                throw new IllegalArgumentException("La factura de compra no es de ese proveedor.");
            }
            if (Boolean.TRUE.equals(f.getAnulada())) {
                throw new IllegalArgumentException("La factura de compra está anulada.");
            }
            dev.setFacturaCompra(f);
        }
    }

    /** Hay existencia suficiente en cada lote (o en el almacén, para artículos sin lote). */
    private void validarExistencia(DevolucionProveedor dev, List<DetalleDevolucionProveedor> detalles) {
        Integer almacen = dev.getAlmacen().getCodigo();
        Map<String, BigDecimal> pedido = new LinkedHashMap<>();
        Map<String, String> nombre = new LinkedHashMap<>();
        for (DetalleDevolucionProveedor d : detalles) {
            String clave = d.getLote() != null ? "L" + d.getLote().getCodigo() : "A" + d.getArticulo().getCodigo();
            pedido.merge(clave, d.getCantidadBase(), BigDecimal::add);
            nombre.put(clave, d.getArticulo().getDescripcion()
                    + (d.getLote() != null ? " (lote " + d.getLote().getNumeroLote() + ")" : ""));
        }
        for (Map.Entry<String, BigDecimal> e : pedido.entrySet()) {
            String clave = e.getKey();
            Integer codigo = Integer.valueOf(clave.substring(1));
            BigDecimal hay = clave.startsWith("L")
                    ? loteAlmacenRepo.findByLoteCodigoAndAlmacenCodigo(codigo, almacen)
                            .map(la -> la.getExistencia()).orElse(BigDecimal.ZERO)
                    : articuloAlmacenService.buscarPorArticuloYAlmacen(codigo, almacen)
                            .map(aa -> aa.getExistencia() != null ? aa.getExistencia() : BigDecimal.ZERO)
                            .orElse(BigDecimal.ZERO);
            if (e.getValue().compareTo(hay) > 0) {
                throw new IllegalStateException("No hay suficiente " + nombre.get(clave) + " en "
                        + dev.getAlmacen().getNombre() + ": hay " + hay.stripTrailingZeros().toPlainString()
                        + " y se quieren devolver " + e.getValue().stripTrailingZeros().toPlainString()
                        + " (unidad base).");
            }
        }
    }

    // ============================================================
    // NOTA DE CRÉDITO: APLICAR
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AplicacionCreditoProveedor aplicarCredito(Integer devolucion, Integer facturaCompra, BigDecimal monto,
            String usuario) {
        DevolucionProveedor dev = repo.findById(devolucion)
                .orElseThrow(() -> new IllegalArgumentException("La devolución #" + devolucion + " no existe."));
        return aplicar(dev, facturaCompra, monto, usuario(usuario));
    }

    private AplicacionCreditoProveedor aplicar(DevolucionProveedor dev, Integer facturaCompra, BigDecimal monto,
            String user) {

        if (dev.isAnulada()) {
            throw new IllegalStateException("La devolución " + dev.getNumero() + " está anulada.");
        }
        if (!DevolucionProveedor.NOTA_CREDITO.equals(dev.getTipoLiquidacion())) {
            throw new IllegalStateException("La devolución " + dev.getNumero() + " no es una nota de crédito.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto a aplicar debe ser mayor que cero.");
        }
        monto = monto.setScale(2, RoundingMode.HALF_UP);
        if (monto.compareTo(dev.getCreditoDisponible()) > 0) {
            throw new IllegalArgumentException("La nota " + dev.getNumero() + " solo tiene disponible RD$ "
                    + String.format("%,.2f", dev.getCreditoDisponible()) + ".");
        }
        FacturaDeCompra factura = facturaCompraService.getFactura(facturaCompra);
        if (factura.getProveedor() == null
                || !Objects.equals(factura.getProveedor().getCodigo(), dev.getProveedor().getCodigo())) {
            throw new IllegalArgumentException("La factura de compra no es de " + dev.getProveedor().getNombre() + ".");
        }

        // Recibo de egreso sin efectivo: rebaja el pendiente de la factura como cualquier pago
        ReciboDeEgreso recibo = new ReciboDeEgreso();
        recibo.setProveedor(dev.getProveedor());
        recibo.setNombreProveedor(dev.getProveedor().getNombre());
        recibo.setFecha(new Date());
        recibo.setFechaCreacion(new Date());
        recibo.setConcepto("NOTA DE CRÉDITO " + dev.getNumero() + " (devolución de mercancía)");
        recibo.setTotalPagado(monto);
        recibo.setUsuario(new Usuario(1));
        recibo.setNombreUsuario(user);
        recibo.setAnulado(false);
        recibo.setCajaChica(null);

        DetalleReciboDeEgreso det = new DetalleReciboDeEgreso();
        det.setReciboEgreso(recibo);
        det.setFactura(factura);
        det.setTotal(monto);
        List<DetalleReciboDeEgreso> lineas = new ArrayList<>(List.of(det));
        recibo.setDetalleReciboDeEgresoCollection(lineas);
        recibo = reciboEgresoService.procesarReciboDeEgreso(recibo, lineas);

        AplicacionCreditoProveedor ap = new AplicacionCreditoProveedor();
        ap.setDevolucion(dev);
        ap.setFacturaCompra(factura);
        ap.setReciboEgreso(recibo);
        ap.setMonto(monto);
        ap.setFecha(LocalDateTime.now());
        ap.setUsuario(user);
        ap.setAnulada(false);
        ap = aplicacionRepo.save(ap);

        dev.setCreditoAplicado(dev.getCreditoAplicado().add(monto));
        dev.setCreditoDisponible(dev.getCreditoDisponible().subtract(monto));
        if (dev.getFacturaCompra() == null) {
            dev.setFacturaCompra(factura);
        }
        repo.saveAndFlush(dev);
        return ap;
    }

    // ============================================================
    // ANULAR
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevolucionProveedor anular(Integer devolucion, String motivo, String usuario) {

        String user = usuario(usuario);
        DevolucionProveedor dev = repo.findById(devolucion)
                .orElseThrow(() -> new IllegalArgumentException("La devolución #" + devolucion + " no existe."));
        if (dev.isAnulada()) {
            throw new IllegalStateException("La devolución " + dev.getNumero() + " ya está anulada.");
        }
        List<DetalleDevolucionProveedor> detalles = detalleRepo.findPorDevolucion(devolucion);
        String nota = "Anulación de la devolución " + dev.getNumero()
                + (motivo != null && !motivo.isBlank() ? ": " + motivo.trim() : "");

        switch (dev.getTipoLiquidacion()) {
            case DevolucionProveedor.NOTA_CREDITO -> {
                for (AplicacionCreditoProveedor ap : aplicacionRepo.findPorDevolucion(devolucion)) {
                    if (!ap.isAnulada()) {
                        reciboEgresoService.anularReciboDeEgreso(ap.getReciboEgreso().getCodigo(), nota, user);
                        ap.setAnulada(true);
                        aplicacionRepo.save(ap);
                    }
                }
                dev.setCreditoAplicado(BigDecimal.ZERO);
                dev.setCreditoDisponible(BigDecimal.ZERO);
            }
            case DevolucionProveedor.CAMBIO -> {
                // La reposición tiene que estar todavía: si ya se vendió, no se puede anular
                for (DetalleDevolucionProveedor d : detalles) {
                    BigDecimal hay = d.getLoteNuevo() != null
                            ? loteAlmacenRepo.findByLoteCodigoAndAlmacenCodigo(d.getLoteNuevo().getCodigo(),
                                    dev.getAlmacen().getCodigo()).map(la -> la.getExistencia()).orElse(BigDecimal.ZERO)
                            : articuloAlmacenService.buscarPorArticuloYAlmacen(d.getArticulo().getCodigo(),
                                    dev.getAlmacen().getCodigo()).map(aa -> aa.getExistencia() != null
                                    ? aa.getExistencia() : BigDecimal.ZERO).orElse(BigDecimal.ZERO);
                    if (d.getCantidadBase().compareTo(hay) > 0) {
                        throw new IllegalStateException("No se puede anular: de la reposición de "
                                + d.getArticulo().getDescripcion()
                                + (d.getLoteNuevo() != null ? " (lote " + d.getLoteNuevo().getNumeroLote() + ")" : "")
                                + " solo quedan " + hay.stripTrailingZeros().toPlainString() + ".");
                    }
                }
                salidaService.crearSalidaPorDevolucionProveedor(dev, detalles, true, user);
            }
            case DevolucionProveedor.EFECTIVO -> {
                if (dev.getTotal().signum() > 0) {
                    CajaTurno turno = cajaService.obtenerCajaAbierta().orElseThrow(() -> new IllegalStateException(
                            "Debe abrir una caja: el reembolso que entró a la gaveta tiene que salir."));
                    cajaService.validarEfectivoDisponible(turno.getId().intValue(), dev.getTotal(),
                            "reversar el reembolso del proveedor");
                    cajaService.registrarMovimientoPos(turno.getId().intValue(),
                            CajaService.TIPO_ANULACION_REEMBOLSO_PROVEEDOR, dev.getTotal().negate(), nota, user);
                }
            }
            default -> {
            }
        }

        // La mercancía devuelta vuelve a sus lotes
        entradaService.crearEntradaPorDevolucionProveedor(dev, detalles, false, user);

        dev.setAnulada(true);
        dev.setFechaAnulada(LocalDateTime.now());
        dev.setMotivoAnulacion(motivo != null && !motivo.isBlank() ? motivo.trim() : null);
        return repo.saveAndFlush(dev);
    }

    // ============================================================
    // CONSULTAS
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<DevolucionProveedor> getLista() {
        return repo.findTodas();
    }

    @Override
    @Transactional(readOnly = true)
    public DevolucionProveedor getDevolucion(Integer codigo) {
        return repo.findById(codigo).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleDevolucionProveedor> getDetalle(Integer devolucion) {
        return detalleRepo.findPorDevolucion(devolucion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AplicacionCreditoProveedor> getAplicaciones(Integer devolucion) {
        return aplicacionRepo.findPorDevolucion(devolucion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaDeCompra> getFacturasPendientes(Integer proveedor) {
        List<FacturaDeCompra> lista = new ArrayList<>();
        for (FacturaDeCompra f : facturaCompraService.getListaFactPendiente(proveedor)) {
            if (!Boolean.TRUE.equals(f.getAnulada()) && getPendienteFactura(f.getCodigo()).signum() > 0) {
                lista.add(f);
            }
        }
        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPendienteFactura(Integer facturaCompra) {
        FacturaDeCompra f = facturaCompraService.getFactura(facturaCompra);
        Double pagado = reciboEgresoService.getPagoFacturaCompra(facturaCompra);
        BigDecimal total = f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO;
        BigDecimal pendiente = total.subtract(pagado != null ? BigDecimal.valueOf(pagado) : BigDecimal.ZERO);
        return pendiente.signum() > 0 ? pendiente.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevolucionProveedor> getConCreditoDisponible(Integer proveedor) {
        return repo.findConCreditoDisponible(proveedor);
    }

    private static String usuario(String usuario) {
        return usuario != null && !usuario.isBlank() ? usuario : "ADMIN";
    }
}
