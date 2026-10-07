package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.*;
import com.maxsoft.application.repo.ArticuloEmpaqueRepo;
import com.maxsoft.application.repo.EntradaDeInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class EntradaDeInventarioServiceImpl implements EntradaDeInventarioService {

    private final EntradaDeInventarioRepo entradaRepo;
    private final MovimientoInventarioService movimientoService;
    private final TipoMovimientoService tipoMovimientoService;
    private final TipoDocumentoService tipoDocumentoService;
    private final ArticuloAlmacenService articuloAlmacenService;
    ArticuloEmpaqueService articuloEmpaqueService;
    ArticuloEmpaqueRepo articuloEmpaqueRepo;

    @Autowired
    public EntradaDeInventarioServiceImpl(
            
            EntradaDeInventarioRepo entradaRepo,
            MovimientoInventarioService movimientoService,
            TipoMovimientoService tipoMovimientoService,
            TipoDocumentoService tipoDocumentoService,
            ArticuloAlmacenService articuloAlmacenService,
            ArticuloEmpaqueService articuloEmpaqueService,
             ArticuloEmpaqueRepo articuloEmpaqueRepo
    ) {
        this.entradaRepo = entradaRepo;
        this.movimientoService = movimientoService;
        this.tipoMovimientoService = tipoMovimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.articuloAlmacenService = articuloAlmacenService;
        this.articuloEmpaqueService=articuloEmpaqueService;
        this.articuloEmpaqueRepo=articuloEmpaqueRepo;
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
                
                 ArticuloEmpaque artEmpaque =this.articuloEmpaqueService
                        .getEmpaqueBase(articulo.getCodigo()).get();
//
//                ArticuloEmpaque artEmpaque = articulo.getUnidadBase() != null
//                        ? articulo.getUnidadBase()
//                        : null;

                BigDecimal cantidadEntrante = detalle.getCantidadRecibida() != null
                        ? detalle.getCantidadRecibida()
                        : BigDecimal.ZERO;

                BigDecimal cantidadFisicaBase = detalle.getCantidadFisicaBase() != null
                        ? detalle.getCantidadFisicaBase()
                        : BigDecimal.ZERO;

//                BigDecimal factorConversion = detalle.getFactorConversion() != null
//                        ? detalle.getFactorConversion()
//                        : BigDecimal.ONE;

                if (cantidadEntrante.compareTo(BigDecimal.ZERO) > 0) {

                    // Cálculo/extracción de totales para la firma completa
                    BigDecimal costoUnitario = detalle.getCostoUnitario() != null
                            ? detalle.getCostoUnitario()
                            : BigDecimal.ZERO;

                    BigDecimal subTotal = costoUnitario.multiply(cantidadEntrante);
                    BigDecimal itbis = BigDecimal.ZERO; // Ajustar si calculas ITBIS en el detalle
                    BigDecimal total = subTotal.add(itbis);

                    // LLAMADA CORREGIDA CON LOS 13 PARÁMETROS DE LA INTERFAZ:
                    movimientoService.registrarMovimiento(
                            articulo, // 1. Articulo
                            alm, // 2. Almacen
                            artEmpaque, // 3. ArticuloEmpaque (null si es unidad suelta)
                            BigDecimal.ONE, // 4. factorConversion (1 por defecto)
                            cantidadFisicaBase, // 5. cantidadEmpaque
                            subTotal, // 6. subTotal
                            itbis, // 7. itbis
                            total, // 8. total
                            tm, // 9. TipoMovimiento
                            tp, // 10. TipoDocumento
                            numDocumento, // 11. numeroDoc
                            cantidadEntrante, // 12. cantidad
                            usuario, // 13. usuario
                            obj.getComentario() // 14. observacion
                    );
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
                        .filter(d -> d.getCodigo() != null && d.getCodigo().equals(itemDev.getIdDetalleFactura()))
                        .findFirst()
                        .orElse(null);

                // 3. Validar que exista el detalle y el artículo sea inventariable
                if (det != null && det.getArticulo() != null
                        && Boolean.TRUE.equals(det.getArticulo().getInventariable())) {

                    // Convertir cantidad de forma segura según su tipo
                    BigDecimal cantidad = itemDev.getCantidadADevolver() != null
                            ? new BigDecimal(itemDev.getCantidadADevolver().toString())
                            : BigDecimal.ZERO;

                    DetalleEntradaInventario detEntrada = buildDetalleEntradaBase(
                            entrada,
                            det.getArticulo(),
                            det.getArticuloEmpaque(),
                            det.getFactorConversion(),
                            det.getCantidadFisicaBase(),
                            det.getItbis(),
                            det.getSubTotal(),
                            det.getTotal(),
                            cantidad,
                            det.getArticulo().getExistencia(),
                            new Almacen(1),
                            "General",
                            "Unidad"
                    );

                    // Asignar costos y precios de venta con fallbacks seguros
                    BigDecimal costo = det.getPrecioCompra() != null ? det.getPrecioCompra() : BigDecimal.ZERO;
                    BigDecimal precio = det.getPrecioVenta() != null ? det.getPrecioVenta() : costo;

                    detEntrada.setCostoUnitario(costo);
                    detEntrada.setPrecioVenta(precio);

                    detallesList.add(detEntrada);
                }
            }
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
                        det.getCantidadFisicaBase(),
                        det.getItbis(),
                        det.getSubTotal(),
                        det.getTotal(),
                        det.getCantidad(),
                        det.getArticulo().getExistencia(),
                        new Almacen(1),
                        "General",
                        "Unidad"
                );
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
            if (det.getArticulo() != null && det.getCantidad() != null && det.getCantidad().compareTo(BigDecimal.ZERO) > 0) {

                DetalleEntradaInventario detEntrada = buildDetalleEntradaBase(
                        entrada,
                        det.getArticulo(),
                        det.getArticuloEmpaque(),
                        det.getFactorConversion(),
                        det.getCantidadFisicaBase(),
                        det.getItbis(),
                        det.getSubTotal(),
                        det.getTotal(),
                        det.getCantidad(),
                        det.getArticulo().getExistencia(),
                        det.getAlmacen(),
                        det.getAlmacen() != null ? det.getAlmacen().getNombre() : "General",
                        det.getNombreUnidad()
                );

                detallesEntrada.add(detEntrada);
            }
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

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();

        for (DetalleRecepcionMercancia det : detalles) {

            if (det.getArticulo() != null && det.getCantidadRecibida() != null && det.getCantidadRecibida().compareTo(BigDecimal.ZERO) > 0) {

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
                detallesEntrada.add(detEntrada);
            }
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

                detallesEntrada.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);
        return guardar(entrada, entrada.getNombreUsuario());
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

    private DetalleEntradaInventario buildDetalleEntradaBase(
            EntradaInventario entrada,
            Articulo articulo,
            ArticuloEmpaque articuloEmpaque,
            BigDecimal factorConversion,
            BigDecimal cantidadEmpaque,
            BigDecimal subTotal,
            BigDecimal itbis,
            BigDecimal total,
            BigDecimal cantidadRecibida,
            BigDecimal stockActual,
            Almacen almacen,
            String nombreAlmacen,
            String nombreUnidad
    ) {
        BigDecimal actual = stockActual != null ? stockActual : BigDecimal.ZERO;
        BigDecimal costo = articulo.getPrecioCompra() != null ? articulo.getPrecioCompra() : BigDecimal.ZERO;

        DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();
        detEntrada.setEntradaInventario(entrada);
        detEntrada.setArticulo(articulo);
        detEntrada.setCantidadRecibida(cantidadRecibida);
        detEntrada.setDescripcionArticulo(articulo.getDescripcion());
        detEntrada.setCostoUnitario(costo);
        detEntrada.setExistenciaActual(actual);
        detEntrada.setCantidadPedida(BigDecimal.ZERO);
        detEntrada.setCantidadPendiente(BigDecimal.ZERO);
        detEntrada.setNuevaExistencia(actual.add(cantidadRecibida));
        detEntrada.setNombreAlmacen(nombreAlmacen);
        detEntrada.setNombreUnidad(nombreUnidad != null ? nombreUnidad : "Unidad");
        detEntrada.setUnidad(articulo.getUnidadBase());
        detEntrada.setArticuloEmpaque(articuloEmpaque);
        detEntrada.setFactorConversion(factorConversion);
        detEntrada.setCantidadFisicaBase(cantidadRecibida);
        detEntrada.setPrecioCompra(costo);
        detEntrada.setAlmacen(almacen);

        return detEntrada;
    }
}
