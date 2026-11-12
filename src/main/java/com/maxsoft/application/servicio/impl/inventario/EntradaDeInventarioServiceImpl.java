package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.*;
import com.maxsoft.application.repo.ArticuloEmpaqueRepo;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.EntradaDeInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EntradaDeInventarioServiceImpl implements EntradaDeInventarioService {

    private final EntradaDeInventarioRepo entradaRepo;
    private final MovimientoInventarioService movimientoService;
    private final TipoMovimientoService tipoMovimientoService;
    private final TipoDocumentoService tipoDocumentoService;
    private final ArticuloAlmacenService articuloAlmacenService;
    ArticuloEmpaqueService articuloEmpaqueService;
    ArticuloEmpaqueRepo articuloEmpaqueRepo;
    private final ArticuloRepo articuloRepo;

    @Autowired
    private LoteService loteService;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntradaDeInventarioServiceImpl.class);

    @Autowired
    public EntradaDeInventarioServiceImpl(
            EntradaDeInventarioRepo entradaRepo,
            MovimientoInventarioService movimientoService,
            TipoMovimientoService tipoMovimientoService,
            TipoDocumentoService tipoDocumentoService,
            ArticuloAlmacenService articuloAlmacenService,
            ArticuloEmpaqueService articuloEmpaqueService,
            ArticuloEmpaqueRepo articuloEmpaqueRepo,
            ArticuloRepo articuloRepo
    ) {
        this.articuloRepo = articuloRepo;
        this.entradaRepo = entradaRepo;
        this.movimientoService = movimientoService;
        this.tipoMovimientoService = tipoMovimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.articuloAlmacenService = articuloAlmacenService;
        this.articuloEmpaqueService = articuloEmpaqueService;
        this.articuloEmpaqueRepo = articuloEmpaqueRepo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntradaInventario guardar(EntradaInventario obj, String usuario) {

        // 1. Asignar usuario de respaldo si no viene en el objeto
        if (obj.getNombreUsuario() == null) {
            obj.setNombreUsuario(usuario);
        }

        // 2. Persistir cabecera para obtener ID
        EntradaInventario entradaGuardada = entradaRepo.save(obj);

        String numDocumento = "ENT-" + (entradaGuardada.getCodigo() != null
                ? entradaGuardada.getCodigo()
                : System.currentTimeMillis());

        // 3. Obtener Tipos de Documento y Movimiento (con getTipoMovimiento corregido)
        TipoDocumento tp = this.tipoDocumentoService.getTipoDocumento(1);
        TipoMovimiento tm = this.tipoMovimientoService.getTipoMovimientoa(1);

        Collection<DetalleEntradaInventario> detalles = obj.getDetalleEntradaInventarioCollection();

        if (detalles != null && !detalles.isEmpty()) {

            for (DetalleEntradaInventario detalle : detalles) {

                detalle.setEntradaInventario(entradaGuardada);

                Articulo articulo = detalle.getArticulo();

                if (articulo == null) {
                    continue; // Evitar NullPointerException si un ítem no tiene artículo
                }

                Almacen alm = detalle.getAlmacen() != null
                        ? detalle.getAlmacen()
                        : new Almacen(1);

                ArticuloEmpaque artEmpaque = this.articuloEmpaqueService
                        .getEmpaqueBase(articulo.getCodigo()).orElse(null);

                BigDecimal cantidadEntrante = detalle.getCantidadRecibida() != null
                        ? detalle.getCantidadRecibida()
                        : BigDecimal.ZERO;

                boolean hayCantidad = cantidadEntrante.signum() > 0
                        || (detalle.isUsarCantidadFisicaBase() && detalle.getCantidadFisicaBase() != null
                        && detalle.getCantidadFisicaBase().signum() > 0);
                if (hayCantidad) {

                    // Cálculo/extracción de totales para la firma completa
                    BigDecimal costoUnitario = detalle.getCostoUnitario() != null
                            ? detalle.getCostoUnitario()
                            : BigDecimal.ZERO;

                    BigDecimal subTotal = costoUnitario.multiply(cantidadEntrante);
                    BigDecimal itbis = BigDecimal.ZERO; // Ajustar si calculas ITBIS en el detalle
                    BigDecimal total = subTotal.add(itbis);

                    BigDecimal factor = detalle.getFactorConversion() != null
                            && detalle.getFactorConversion().signum() > 0
                            ? detalle.getFactorConversion() : BigDecimal.ONE;
                    // UNIDAD BASE. Las líneas repartidas por lote traen la cantidad base exacta.
                    BigDecimal cantidadFisicaBase = detalle.isUsarCantidadFisicaBase()
                            && detalle.getCantidadFisicaBase() != null
                            ? detalle.getCantidadFisicaBase()
                            : cantidadEntrante.multiply(factor);

                    // Lote: el de la línea; si no hay pero se digitó número o vencimiento, se crea;
                    // si no hay nada, el movimiento usa el lote automático del día.
                    Lote lote = detalle.getLote();
                    Articulo actual = articuloRepo.findById(articulo.getCodigo()).orElse(articulo);
                    if (lote == null && actual.isControlaLote()
                            && ((detalle.getNumeroLote() != null && !detalle.getNumeroLote().isBlank())
                            || detalle.getFechaVencimientoLote() != null)) {
                        BigDecimal costoBase = costoUnitario.signum() > 0
                                ? costoUnitario.divide(factor, 4, RoundingMode.HALF_UP) : null;
                        lote = loteService.obtenerOCrearLote(actual, detalle.getNumeroLote(),
                                detalle.getFechaVencimientoLote(), costoBase, null, numDocumento, usuario);
                    }

                    List<MovimientoInventario> movs = movimientoService.registrarMovimientoConLote(
                            articulo, alm, artEmpaque, BigDecimal.ONE, cantidadFisicaBase,
                            subTotal, itbis, total, tm, tp, numDocumento, cantidadEntrante,
                            usuario, obj.getComentario(), lote);

                    if (movs.get(0).getLote() != null) {
                        detalle.setLote(movs.get(0).getLote());
                    }
                }
            }
        }

        return entradaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntradaInventario> getLista() {
        return entradaRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleEntradaInventario> getDetalle(int obj) {
        return entradaRepo.getDetalle(obj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntradaInventario crearEntradaPorDevolucion(FacturaDeVenta factura, SolicitudDevolucionDto solicitud) {

        // 1. Inicializar la cabecera
        EntradaInventario entrada = inicializarCabeceraBase(
                7,
                "FACT-" + factura.getCodigo(),
                solicitud.getUsuario(),
                (Boolean.TRUE.equals(factura.getAnulada()) ? "Devolución Total" : "Devolución Parcial")
                + " de Factura #" + factura.getCodigo() + ". Motivo: " + solicitud.getMotivo()
        );

        List<DetalleEntradaInventario> detallesList = new ArrayList<>();

        if (solicitud.getItems() != null) {

            for (SolicitudDevolucionDto.ItemDevolucionDto itemDev : solicitud.getItems()) {

                // Linea estática errónea eliminada
                // 2. Buscar el detalle de la factura correspondiente al ítem
                DetalleFacturaDeVenta det = factura.getDetalleFacturaDeVentaCollection().stream()
                        .filter(d -> d.getCodigo() != null && d.getCodigo()
                        .equals(itemDev.getIdDetalleFactura()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                        "El detalle " + itemDev.getIdDetalleFactura() + " no pertenece a la factura #" + factura.getCodigo()));

                if (det.getArticulo() == null) {
                    throw new IllegalStateException("La línea " + det.getCodigo() + " de la factura no tiene artículo.");
                }
                if (det.getAlmacen() == null) {
                    throw new IllegalStateException("La línea de " + det.getArticulo().getDescripcion()
                            + " no tiene almacén; no se puede reingresar al inventario.");
                }

                // Cantidad devuelta en la unidad vendida (empaque) y su equivalente en unidad base
                BigDecimal cantidad = itemDev.getCantidadADevolver() != null
                        ? new BigDecimal(itemDev.getCantidadADevolver().toString())
                        : BigDecimal.ZERO;
                BigDecimal factor = det.getFactorConversion() != null && det.getFactorConversion().signum() > 0
                        ? det.getFactorConversion() : BigDecimal.ONE;
                BigDecimal cantidadBase = cantidad.multiply(factor);

                // Importes proporcionales a lo devuelto (no los de la línea completa)
                BigDecimal proporcion = det.getCantidad() != null && det.getCantidad().signum() > 0
                        ? cantidad.divide(det.getCantidad(), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ONE;
                BigDecimal itbis = escalar(det.getTotalItbis(), proporcion);
                BigDecimal subTotal = escalar(det.getSubTotal(), proporcion);
                BigDecimal total = escalar(det.getTotal(), proporcion);

                // Asignar costos y precios de venta con fallbacks seguros
                BigDecimal costo = det.getPrecioCompra() != null ? det.getPrecioCompra() : BigDecimal.ZERO;
                BigDecimal precio = det.getPrecioVenta() != null ? det.getPrecioVenta() : costo;

                // Una línea por lote del que había salido (vuelve al mismo lote)
                agregarLineasDeReposicion(detallesList, entrada, det, factor, cantidad, cantidadBase,
                        itbis, subTotal, total, costo, precio);
            }
        }
        if (detallesList.isEmpty()) {
            throw new IllegalArgumentException("La devolución no tiene artículos.");
        }

        // 4. Asignar lista de detalles a la cabecera y guardar
        entrada.setDetalleEntradaInventarioCollection(detallesList);

        return guardar(entrada, solicitud.getUsuario());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntradaInventario crearEntradaPorAjuste(AjusteInventario ajuste, List<DetalleAjusteInventario> detalleAju) {

        String usuario = ajuste.getUsuario() != null ? ajuste.getUsuario().getNombre() : "SISTEMA";
        String observacion = "Entrada por Ajuste de Inventario #" + ajuste.getCodigo() + ". "
                + (ajuste.getObservacion() != null ? ajuste.getObservacion() : "");

        EntradaInventario entrada = inicializarCabeceraBase(3, "AJ-" + ajuste.getCodigo(), usuario, observacion);

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();

        for (DetalleAjusteInventario det : detalleAju) {

            if (det.getArticulo() != null && det.getCantidad() != null && det.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
                DetalleEntradaInventario detEntrada = buildDetalleEntradaBase(
                        entrada,
                        det.getArticulo(),
                        det.getArticuloEmpaque(),
                        det.getFactorConversion(),
                        det.getFactorConversion().multiply(det.getCantidad()),
                        det.getItbis(),
                        det.getSubTotal(),
                        det.getTotal(),
                        det.getCantidad(),
                        det.getArticulo().getExistencia(),
                        ajuste.getAlmacen(),
                        ajuste.getAlmacen().getNombre(),
                        det.getArticuloEmpaque().getNombreEmpaque()
                );
                // Ajuste positivo: lote elegido, o número/vencimiento digitados (se crea), o lote del día
                detEntrada.setLote(det.getLote());
                detEntrada.setNumeroLote(det.getNumeroLote());
                detEntrada.setFechaVencimientoLote(det.getFechaVencimientoLote());
                detallesEntrada.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);
        return guardar(entrada, entrada.getNombreUsuario());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntradaInventario crearEntradaPorAnulacionVenta(FacturaDeVenta factura, List<DetalleFacturaDeVenta> listaDetFact) {
        String usuario = factura.getNombreUsuario() != null ? factura.getNombreUsuario() : "SISTEMA";
        String comentario = "Entrada por anulacion de venta #" + factura.getCodigo() + ". "
                + (factura.getComentario() != null ? factura.getComentario() : "");

        EntradaInventario entrada = inicializarCabeceraBase(6, "AN-" + factura.getCodigo(), usuario, comentario);

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();

        for (DetalleFacturaDeVenta det : listaDetFact) {

            // Repone lo que se vendió (menos lo ya devuelto) en los mismos lotes de los que salió
            BigDecimal factor = det.getFactorConversion() != null && det.getFactorConversion().signum() > 0
                    ? det.getFactorConversion() : BigDecimal.ONE;
            BigDecimal cantidadBase = det.getCantidadFisicaBase() != null
                    ? det.getCantidadFisicaBase()
                    : (det.getCantidad() != null ? det.getCantidad().multiply(factor) : BigDecimal.ZERO);
            if (cantidadBase.signum() <= 0) {
                continue;
            }
            agregarLineasDeReposicion(detallesEntrada, entrada, det, factor, det.getCantidad(), cantidadBase,
                    det.getTotalItbis(), det.getSubTotal(), det.getTotal(), null, null);
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);
        return guardar(entrada, entrada.getNombreUsuario());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntradaInventario crearEntradaPorRecepcion(RecepcionMercancia recepcion, List<DetalleRecepcionMercancia> detalles) {

        String usuario = recepcion.getUsuario() != null ? recepcion.getUsuario().getNombre() : "SISTEMA";
        String comentario = "Entrada por Recepcion de Mercancia #" + recepcion.getCodigo() + ". "
                + (recepcion.getComentario() != null ? recepcion.getComentario() : "");

        EntradaInventario entrada = inicializarCabeceraBase(9, "REC-" + recepcion.getCodigo(), usuario, comentario);

        // El costo se recalcula ANTES de mover el stock: el promedio necesita la
        // existencia que había antes de esta compra.
        actualizarCostoPromedio(detalles);

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();

        for (DetalleRecepcionMercancia det : detalles) {

//            if (det.getArticulo() != null && det.getCantidadRecibida() != null && det.getCantidadRecibida().compareTo(BigDecimal.ZERO) > 0) {
            det.setCantidadFisicaBase(det.getCantidadRecibida().multiply(det.getFactorConversion()));
//                System.out.println("getCantidadFisicaBase " + det.getCantidadFisicaBase()
//                        .multiply(det.getFactorConversion()));

            DetalleEntradaInventario detEntrada = buildDetalleEntradaBase(
                    entrada,
                    det.getArticulo(),
                    det.getArticuloEmpaque(),
                    det.getFactorConversion(),
                    det.getCantidadFisicaBase(),
                    det.getItbis(),
                    det.getSubTotal(),
                    det.getTotal(),
                    det.getCantidadRecibida(),
                    det.getArticulo().getExistencia(),
                    det.getAlmacen(),
                    det.getAlmacen().getNombre(),
                    det.getNombreUnidad()
            );

            // La entrada se valora al precio pagado en esta compra (por unidad del documento)
            if (det.getPrecioUnitario() != null) {
                detEntrada.setCostoUnitario(det.getPrecioUnitario());
                detEntrada.setPrecioCompra(det.getPrecioUnitario());
            }

            // Lote de la compra: número y vencimiento digitados (vacío = lote del día con
            // vencimiento por vida útil). Queda en el detalle de la recepción para que su
            // anulación saque del mismo lote.
            Articulo actual = articuloRepo.findById(det.getArticulo().getCodigo()).orElse(det.getArticulo());
            if (actual.isControlaLote()) {
                Lote lote = det.getLote();
                if (lote == null) {
                    BigDecimal factor = det.getFactorConversion() != null && det.getFactorConversion().signum() > 0
                            ? det.getFactorConversion() : BigDecimal.ONE;
                    BigDecimal costoBase = det.getPrecioUnitario() != null
                            ? det.getPrecioUnitario().divide(factor, 4, RoundingMode.HALF_UP) : null;
                    lote = loteService.obtenerOCrearLote(actual, det.getNumeroLote(), det.getFechaVencimientoLote(),
                            costoBase, recepcion.getProveedor(), "REC-" + recepcion.getCodigo(), usuario);
                    det.setLote(lote);
                }
                detEntrada.setLote(lote);
            }

            detallesEntrada.add(detEntrada);
//            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);
        return guardar(entrada, entrada.getNombreUsuario());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntradaInventario crearEntradaPorTraslado(TrasladoInventario traslado, List<DetalleTrasladoInventario> detalles) {

        String usuario = traslado.getUsuarioEnvia() != null ? traslado.getUsuarioEnvia().getNombre() : "SISTEMA";
        String comentario = "Entrada por Traslado de Mercancia #" + traslado.getCodigo() + ". "
                + (traslado.getObservacion() != null ? traslado.getObservacion() : "");

        EntradaInventario entrada = inicializarCabeceraBase(3, "AJ-" + traslado.getCodigo(), usuario, comentario);
        Almacen almacenDestino = traslado.getAlmacenDestino();

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();

        for (DetalleTrasladoInventario det : detalles) {

            if (det.getArticulo() != null && det.getCantidadRecibida() != null && det.getCantidadRecibida().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal stockActual = articuloAlmacenService
                        .buscarPorArticuloYAlmacen(det.getArticulo().getCodigo(), almacenDestino.getCodigo())
                        .map(ArticuloAlmacen::getExistencia)
                        .orElse(BigDecimal.ZERO);

                DetalleEntradaInventario detEntrada = buildDetalleEntradaBase(
                        entrada,
                        det.getArticulo(),
                        det.getArticuloEmpaque(),
                        det.getFactorConversion(),
                        det.getCantidadFisicaBase(),
                        det.getItbis(),
                        det.getSubTotal(),
                        det.getTotal(),
                        det.getCantidadRecibida(),
                        stockActual,
                        almacenDestino,
                        almacenDestino != null ? almacenDestino.getNombre() : "General",
                        det.getNombreUnidad()
                );

                detEntrada.setCantidadPedida(det.getCantidadEnviada());
                detEntrada.setUnidad(det.getUnidad());

                // Lotes: entra al destino con los mismos lotes (y cantidades) que salieron del origen
                List<MovimientoInventario> salidos = det.getMovimientosSalida();
                if (salidos != null && salidos.stream().anyMatch(m -> m.getLote() != null)) {
                    BigDecimal factor = det.getFactorConversion() != null && det.getFactorConversion().signum() > 0
                            ? det.getFactorConversion() : BigDecimal.ONE;
                    BigDecimal baseTotal = salidos.stream().map(MovimientoInventario::getCantidad)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    for (MovimientoInventario m : salidos) {
                        BigDecimal proporcion = baseTotal.signum() > 0
                                ? m.getCantidad().divide(baseTotal, 8, RoundingMode.HALF_UP) : BigDecimal.ONE;
                        DetalleEntradaInventario parte = buildDetalleEntradaBase(
                                entrada, det.getArticulo(), det.getArticuloEmpaque(), factor,
                                m.getCantidad(),
                                escalar(det.getItbis(), proporcion),
                                escalar(det.getSubTotal(), proporcion),
                                escalar(det.getTotal(), proporcion),
                                m.getCantidad().divide(factor, 2, RoundingMode.HALF_UP),
                                stockActual, almacenDestino,
                                almacenDestino != null ? almacenDestino.getNombre() : "General",
                                det.getNombreUnidad());
                        parte.setCantidadPedida(det.getCantidadEnviada());
                        parte.setUnidad(det.getUnidad());
                        parte.setLote(m.getLote());
                        parte.setUsarCantidadFisicaBase(true);
                        detallesEntrada.add(parte);
                    }
                    continue;
                }

                detallesEntrada.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);
        return guardar(entrada, entrada.getNombreUsuario());
    }

    /**
     * Costo promedio ponderado por unidad base:
     * (existencia × costo actual + cantidad comprada × costo de compra) / (existencia + cantidad comprada).
     * Si la existencia es cero o negativa, o el artículo no tenía costo, queda el costo de la compra.
     * Se agrupa por artículo por si la misma recepción trae varias líneas (o empaques) del mismo.
     * Luego se actualiza el costo de cada empaque = costo base × factor.
     */
    private void actualizarCostoPromedio(List<DetalleRecepcionMercancia> detalles) {

        // artículo -> [cantidad base comprada, costo total de la compra]
        Map<Integer, BigDecimal[]> compraPorArticulo = new LinkedHashMap<>();

        for (DetalleRecepcionMercancia det : detalles) {
            if (det.getArticulo() == null || det.getPrecioUnitario() == null
                    || det.getCantidadRecibida() == null || det.getCantidadRecibida().signum() <= 0) {
                continue;
            }
            BigDecimal factor = det.getFactorConversion() != null && det.getFactorConversion().signum() > 0
                    ? det.getFactorConversion() : BigDecimal.ONE;
            BigDecimal cantidadBase = det.getCantidadRecibida().multiply(factor);
            BigDecimal costoTotal = det.getCantidadRecibida().multiply(det.getPrecioUnitario());

            compraPorArticulo.merge(det.getArticulo().getCodigo(),
                    new BigDecimal[]{cantidadBase, costoTotal},
                    (a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(b[1])});
        }

        for (Map.Entry<Integer, BigDecimal[]> e : compraPorArticulo.entrySet()) {

            Articulo articulo = articuloRepo.findById(e.getKey()).orElse(null);
            if (articulo == null) {
                continue;
            }
            BigDecimal cantidadComprada = e.getValue()[0];
            BigDecimal costoCompraTotal = e.getValue()[1];
            BigDecimal costoCompraBase = costoCompraTotal.divide(cantidadComprada, 4, RoundingMode.HALF_UP);

            BigDecimal existencia = articulo.getExistencia() != null
                    ? articulo.getExistencia().max(BigDecimal.ZERO) : BigDecimal.ZERO;
            BigDecimal costoActual = articulo.getPrecioCompra();

            BigDecimal nuevoCosto;
            if (existencia.signum() == 0 || costoActual == null || costoActual.signum() <= 0) {
                nuevoCosto = costoCompraBase;
            } else {
                nuevoCosto = existencia.multiply(costoActual).add(costoCompraTotal)
                        .divide(existencia.add(cantidadComprada), 4, RoundingMode.HALF_UP);
            }

            log.info("Costo promedio artículo {} ({}): existencia {} a {} + compra {} a {} => {}",
                    articulo.getCodigo(), articulo.getDescripcion(), existencia, costoActual,
                    cantidadComprada, costoCompraBase, nuevoCosto);

            articulo.setPrecioCompra(nuevoCosto.setScale(2, RoundingMode.HALF_UP));
            articuloRepo.save(articulo);

            for (ArticuloEmpaque emp : articuloEmpaqueRepo.findByArticuloCodigo(articulo.getCodigo())) {
                BigDecimal factor = emp.getFactorConversion() != null && emp.getFactorConversion().signum() > 0
                        ? emp.getFactorConversion() : BigDecimal.ONE;
                emp.setPrecioCompra(nuevoCosto.multiply(factor).setScale(2, RoundingMode.HALF_UP));
                articuloEmpaqueRepo.save(emp);
            }
        }
    }

    // --- Métodos Helper ---
    private EntradaInventario inicializarCabeceraBase(Integer tipoDocumento, String numDocumento, String usuario, String comentario) {
        EntradaInventario entrada = new EntradaInventario();
        Date fechaActual = new Date();

        entrada.setFecha(fechaActual);
        entrada.setFechaCreacion(fechaActual);
        entrada.setFechaActualizacion(fechaActual);
        entrada.setFechaContabilizacion(fechaActual);
        entrada.setTipoDocumento(tipoDocumento);
        entrada.setNumeroDocumento(numDocumento);
        entrada.setMoneda(1);
        entrada.setNombreMoneda("DOP");
        entrada.setNombreUsuario(usuario);
        entrada.setAnulada(false);
        entrada.setComentario(comentario);

        return entrada;
    }

    /**
     * Agrega las líneas de entrada que reponen (devolución o anulación) una
     * línea de venta: una por cada lote del que había salido, con la cantidad
     * y los montos repartidos en proporción (la última lleva el resto).
     *
     * @param costo/precio null = los que pone buildDetalleEntradaBase
     */
    private void agregarLineasDeReposicion(List<DetalleEntradaInventario> destino, EntradaInventario entrada,
            DetalleFacturaDeVenta det, BigDecimal factor, BigDecimal cantidadDoc, BigDecimal cantidadBase,
            BigDecimal itbis, BigDecimal subTotal, BigDecimal total, BigDecimal costo, BigDecimal precio) {

        List<LoteService.Tramo> tramos = loteService.repartirReposicionVenta(det, cantidadBase);
        BigDecimal restoDoc = cantidadDoc != null ? cantidadDoc : BigDecimal.ZERO;
        BigDecimal restoItbis = itbis != null ? itbis : BigDecimal.ZERO;
        BigDecimal restoSub = subTotal != null ? subTotal : BigDecimal.ZERO;
        BigDecimal restoTotal = total != null ? total : BigDecimal.ZERO;

        for (int i = 0; i < tramos.size(); i++) {
            LoteService.Tramo tramo = tramos.get(i);
            boolean ultimo = i == tramos.size() - 1;
            BigDecimal proporcion = tramo.cantidad().divide(cantidadBase, 8, RoundingMode.HALF_UP);

            BigDecimal tDoc = ultimo ? restoDoc : escalar(cantidadDoc, proporcion);
            BigDecimal tItbis = ultimo ? restoItbis : escalar(itbis, proporcion);
            BigDecimal tSub = ultimo ? restoSub : escalar(subTotal, proporcion);
            BigDecimal tTotal = ultimo ? restoTotal : escalar(total, proporcion);
            restoDoc = restoDoc.subtract(tDoc);
            restoItbis = restoItbis.subtract(tItbis);
            restoSub = restoSub.subtract(tSub);
            restoTotal = restoTotal.subtract(tTotal);

            DetalleEntradaInventario detEntrada = buildDetalleEntradaBase(
                    entrada,
                    det.getArticulo(),
                    det.getArticuloEmpaque(),
                    factor,
                    tramo.cantidad(),
                    tItbis,
                    tSub,
                    tTotal,
                    tDoc,
                    det.getArticulo().getExistencia(),
                    det.getAlmacen(),
                    det.getAlmacen() != null ? det.getAlmacen().getNombre() : "General",
                    det.getNombreUnidad()
            );
            detEntrada.setLote(tramo.lote());
            detEntrada.setUsarCantidadFisicaBase(true);
            if (costo != null) {
                detEntrada.setCostoUnitario(costo);
            }
            if (precio != null) {
                detEntrada.setPrecioVenta(precio);
            }
            destino.add(detEntrada);
        }
    }

    private static BigDecimal escalar(BigDecimal valor, BigDecimal proporcion) {
        return valor == null ? BigDecimal.ZERO : valor.multiply(proporcion).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private DetalleEntradaInventario buildDetalleEntradaBase(
            EntradaInventario entrada,
            Articulo articulo,
            ArticuloEmpaque articuloEmpaque,
            BigDecimal factorConversion,
            BigDecimal cantidadEmpaque, // cantidad en UNIDAD BASE
            BigDecimal itbis, // todos los llamadores pasan (itbis, subTotal, total)
            BigDecimal subTotal,
            BigDecimal total,
            BigDecimal cantidadRecibida, // cantidad en la unidad del documento
            BigDecimal stockActual,
            Almacen almacen,
            String nombreAlmacen,
            String nombreUnidad
    ) {
        BigDecimal actual = stockActual != null ? stockActual : BigDecimal.ZERO;
        BigDecimal costo = articulo.getPrecioCompra() != null ? articulo.getPrecioCompra() : BigDecimal.ZERO;

        DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();

//        cantidadEmpaque.add(factorConversion.multiply(cantidadRecibida));
        detEntrada.setEntradaInventario(entrada);
        detEntrada.setArticulo(articulo);
        detEntrada.setCantidadRecibida(cantidadRecibida);
        detEntrada.setCantidadFisicaBase(cantidadEmpaque);
        detEntrada.setDescripcionArticulo(articulo.getDescripcion());
        detEntrada.setCostoUnitario(costo);
        detEntrada.setExistenciaActual(actual);
        detEntrada.setCantidadPedida(BigDecimal.ZERO);
        detEntrada.setCantidadPendiente(BigDecimal.ZERO);
        // La existencia está en unidad base: se suma la cantidad base, no la del empaque
        BigDecimal base = cantidadEmpaque != null ? cantidadEmpaque
                : (cantidadRecibida != null && factorConversion != null
                        ? cantidadRecibida.multiply(factorConversion) : BigDecimal.ZERO);
        detEntrada.setNuevaExistencia(actual.add(base));
        detEntrada.setNombreAlmacen(nombreAlmacen);
        detEntrada.setNombreUnidad(nombreUnidad);
        detEntrada.setUnidad(articuloEmpaque.getUnidadEmpaque());
        detEntrada.setArticuloEmpaque(articuloEmpaque);
        detEntrada.setFactorConversion(factorConversion);
        detEntrada.setPrecioCompra(costo);
        detEntrada.setAlmacen(almacen);
        detEntrada.setItbis(itbis);
        detEntrada.setSubTotal(subTotal);
        detEntrada.setTotal(total);

        return detEntrada;
    }
}
