package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.Merma;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.modelo.TrasladoInventario;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.SalidaInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalidaInventarioServiceImpl implements SalidaInventarioService {

    private final SalidaInventarioRepo salidaRepo;
    private final ArticuloRepo articuloRepo;
    private final MovimientoInventarioService movimientoService;
    private final TipoDocumentoService tipoDocumentoService;
    private final TipoMovimientoService tipoMovimientoService;
    private final ArticuloAlmacenService articuloAlmacenService;
    private final ArticuloEmpaqueService articuloEmpaqueService;

    @Autowired
    public SalidaInventarioServiceImpl(SalidaInventarioRepo salidaRepo,
            ArticuloRepo articuloRepo,
            MovimientoInventarioService movimientoService,
            TipoDocumentoService tipoDocumentoService,
            TipoMovimientoService tipoMovimientoService,
            ArticuloAlmacenService articuloAlmacenService,
            ArticuloEmpaqueService articuloEmpaqueService
    ) {
        this.salidaRepo = salidaRepo;
        this.articuloRepo = articuloRepo;
        this.movimientoService = movimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.tipoMovimientoService = tipoMovimientoService;
        this.articuloAlmacenService = articuloAlmacenService;
        this.articuloEmpaqueService = articuloEmpaqueService;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalidaInventario guardar(SalidaInventario obj, String usuario) {

        // 1. Guardar la cabecera del documento de salida
        SalidaInventario salidaGuardada = salidaRepo.save(obj);

        String numDocumento = "SAL-" + (salidaGuardada.getNumeroDocumento() != null
                ? salidaGuardada.getNumeroDocumento()
                : System.currentTimeMillis());

        // Corregido: getTipoMovimiento (se eliminó la 'a' extra)
        TipoDocumento tp = this.tipoDocumentoService.getTipoDocumento(2);
        TipoMovimiento tm = this.tipoMovimientoService.getTipoMovimientoa(2);

        // 2. Procesar detalles para impacto en el Kardex
        Collection<DetalleSalidaInventario> detalles = salidaGuardada.getDetalleSalidaInventarioCollection();

        if (detalles != null && !detalles.isEmpty()) {

            for (DetalleSalidaInventario detalle : detalles) {

                detalle.setSalidaInventario(salidaGuardada);
                Articulo articuloProxy = detalle.getArticulo();

                if (articuloProxy != null && articuloProxy.getCodigo() != null) {

                    System.out.println("Factor " + detalle.getFactorConversion());
                    System.out.println("CantidaBase " + detalle.getCantidadFisicaBase());

                    Articulo articulo = articuloRepo.findById(articuloProxy.getCodigo())
                            .orElse(articuloProxy);

                    BigDecimal cantidadSalida = detalle.getCantidad() != null
                            ? detalle.getCantidad()
                            : BigDecimal.ZERO;

                    Almacen alm = detalle.getAlmacen() != null
                            ? detalle.getAlmacen()
                            : new Almacen(1);

                    if (cantidadSalida.compareTo(BigDecimal.ZERO) > 0) {

                        BigDecimal costoUnitario = detalle.getCostoUnitario() != null
                                ? detalle.getCostoUnitario()
                                : BigDecimal.ZERO;
                        BigDecimal subTotal = costoUnitario.multiply(cantidadSalida);
                        BigDecimal itbis = BigDecimal.ZERO;
                        BigDecimal total = subTotal.add(itbis);
                        BigDecimal cantidadFisicaBase = detalle.getFactorConversion().multiply(cantidadSalida);

                        ArticuloEmpaque artEmpaq = this.articuloEmpaqueService.getEmpaqueBase(articulo.getCodigo()).get();

                        // LLAMADA CORREGIDA: 13 parámetros según la interfaz MovimientoInventarioService
                        movimientoService.registrarMovimiento(
                                articulo, // 1. Articulo
                                alm, // 2. Almacen
                                artEmpaq, // 3. ArticuloEmpaque
                                BigDecimal.ONE, // 4. factorConversion
                                cantidadFisicaBase, // 5. cantidadEmpaque
                                subTotal, // 6. subTotal
                                itbis, // 7. itbis
                                total, // 8. total
                                tm, // 9. TipoMovimiento
                                tp, // 10. TipoDocumento
                                numDocumento, // 11. numeroDoc
                                cantidadSalida, // 12. cantidad
                                usuario != null ? usuario : "SISTEMA", // 13. usuario
                                obj.getObservacion() // 14. observacion
                        );
                    }
                }
            }
        }

        return salidaGuardada;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalidaInventario crearSalidaPorVenta(FacturaDeVenta factura, List<DetalleFacturaDeVenta> listaDEtFactura) {

        SalidaInventario salida = new SalidaInventario();
        Date fechaActual = new Date();

        salida.setFecha(factura.getFecha() != null ? factura.getFecha() : fechaActual);
        salida.setFechaRegistro(fechaActual);
        salida.setFechaContabilizacion(factura.getFecha() != null ? factura.getFecha() : fechaActual);

        salida.setTipoDocumento(this.tipoDocumentoService.getTipoDocumento(5)); // ID 5: Venta POS
        salida.setNumeroDocumento(factura.getCodigo() != null ? factura.getCodigo().toString() : "");
        salida.setUsuario(new Usuario(1));
        salida.setNombreUsuario(factura.getNombreUsuario() != null ? factura.getNombreUsuario() : "Admin");
        salida.setAnulada(false);
        salida.setObservacion("Salida automática por Factura POS #" + factura.getCodigo());

        List<DetalleSalidaInventario> detalles = new ArrayList<>();

        if (!listaDEtFactura.isEmpty()) {

            System.out.println("listaDEtFactura " + listaDEtFactura.size());

            for (DetalleFacturaDeVenta detFactura : listaDEtFactura) {

//                if (detFactura.getArticulo() != null && Boolean.TRUE.equals(detFactura.getArticulo().getInventariable())) {
                DetalleSalidaInventario detSalida = new DetalleSalidaInventario();

                detSalida.setSalidaInventario(salida);
                detSalida.setArticulo(detFactura.getArticulo());
                detSalida.setDescripcionArticulo(detFactura.getArticulo().getDescripcion());
                detSalida.setCantidad(detFactura.getCantidad());
                detSalida.setAlmacen(detFactura.getAlmacen());
                detSalida.setNombreAlmacen(detFactura.getNombreAlmacen());
                detSalida.setUnidad(detFactura.getUnidad());
                detSalida.setFactorConversion(detFactura.getFactorConversion());
                detSalida.setCantidadFisicaBase(detFactura.getCantidadFisicaBase());
                detSalida.setArticuloEmpaque(detFactura.getArticuloEmpaque());
                detSalida.setItbis(detFactura.getTotalItbis());
                detSalida.setSubTotal(detFactura.getSubTotal());
                detSalida.setTotal(detFactura.getTotal());

                BigDecimal stockActual = detFactura.getArticulo().getExistencia() != null
                        ? detFactura.getArticulo().getExistencia()
                        : BigDecimal.ZERO;

                detSalida.setExistenciaAnterior(stockActual);
                detSalida.setExistencia(stockActual);
//                    detSalida.setUnidad(detFactura.getArticulo().getUnidadSalida());
                detSalida.setCostoUnitario(detFactura.getPrecioCompra());

                detSalida.setPrecioCompra(detFactura.getPrecioCompra());
                detSalida.setPrecioVenta(detFactura.getPrecioVenta() != null ? detFactura.getPrecioVenta() : BigDecimal.ZERO);

                detalles.add(detSalida);
//                }
            }

        } else {
            throw new IllegalArgumentException("Detalle de la factura vacio '");
        }

        salida.setDetalleSalidaInventarioCollection(detalles);

        return guardar(salida, salida.getNombreUsuario());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalidaInventario crearSalidaPorAjuste(AjusteInventario ajuste, List<DetalleAjusteInventario> detalles) {

        SalidaInventario salida = new SalidaInventario();

        salida.setFechaRegistro(new Date());
        salida.setFecha(ajuste.getFecha());
        salida.setTipoDocumento(this.tipoDocumentoService.getTipoDocumento(3)); // Asignar Tipo Ajuste
        salida.setNumeroDocumento(ajuste.getCodigo() != null ? ajuste.getCodigo().toString() : "");
        salida.setUsuario(ajuste.getUsuario() != null ? ajuste.getUsuario() : new Usuario(1));

        String nombreUsuario = (ajuste.getUsuario() != null && ajuste.getUsuario().getNombre() != null)
                ? ajuste.getUsuario().getNombre() : "SISTEMA";

        salida.setNombreUsuario(nombreUsuario);
        salida.setObservacion("Salida por Ajuste de Inventario #" + ajuste.getCodigo() + ". "
                + (ajuste.getObservacion() != null ? ajuste.getObservacion() : ""));

        List<DetalleSalidaInventario> detallesSalida = new ArrayList<>();

        if (detalles != null) {

            for (DetalleAjusteInventario det : detalles) {

//                if (det.getArticulo() != null && det.getCantidad() != null && det.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
                DetalleSalidaInventario detSalida = new DetalleSalidaInventario();

                detSalida.setSalidaInventario(salida);
                detSalida.setArticulo(det.getArticulo());
                detSalida.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detSalida.setCantidad(det.getCantidad());
                detSalida.setAlmacen(det.getAlmacen());

                BigDecimal stockActual = det.getArticulo().getExistencia() != null
                        ? det.getArticulo().getExistencia()
                        : BigDecimal.ZERO;

                detSalida.setExistenciaAnterior(stockActual);
                detSalida.setExistencia(stockActual);
                detSalida.setUnidad(det.getArticuloEmpaque().getUnidadEmpaque());
                detSalida.setFactorConversion(det.getFactorConversion());
                detSalida.setCantidadFisicaBase(det.getFactorConversion().multiply(det.getCantidad()));
                detSalida.setArticuloEmpaque(det.getArticuloEmpaque());

                BigDecimal costo = det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO;
                BigDecimal precio = det.getArticulo().getPrecioVenta() != null ? det.getArticulo().getPrecioVenta() : BigDecimal.ZERO;

                detSalida.setCostoUnitario(costo);
                detSalida.setPrecioCompra(costo);
                detSalida.setPrecioVenta(precio);

                detallesSalida.add(detSalida);
//                }
            }
        }

        salida.setDetalleSalidaInventarioCollection(detallesSalida);

        return guardar(salida, nombreUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalidaInventario> getLista() {
        return salidaRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleSalidaInventario> getDetalle(int codigoSalida) {
        return salidaRepo.getDetalle(codigoSalida);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalidaInventario> getLista(boolean estado) {
        return salidaRepo.getLista(estado);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalidaInventario crearSalidaPorTraslado(TrasladoInventario traslado, List<DetalleTrasladoInventario> detalles) {

        SalidaInventario salida = new SalidaInventario();

        salida.setFechaRegistro(new Date());
        salida.setFecha(traslado.getFechaEmision());
        salida.setTipoDocumento(this.tipoDocumentoService.getTipoDocumento(2));
        salida.setNumeroDocumento(traslado.getCodigo() != null ? traslado.getCodigo().toString() : "");
        salida.setUsuario(traslado.getUsuarioEnvia() != null ? traslado.getUsuarioEnvia() : new Usuario(1));

        String nombreUsuario = (traslado.getUsuarioEnvia() != null && traslado.getUsuarioEnvia().getNombre() != null)
                ? traslado.getUsuarioEnvia().getNombre() : "SISTEMA";

        salida.setNombreUsuario(nombreUsuario);
        salida.setObservacion("Salida por Traslado de Inventario #" + traslado.getCodigo() + ". "
                + (traslado.getObservacion() != null ? traslado.getObservacion() : ""));

        Almacen almacenOrigen = traslado.getAlmacenOrigen();

        List<DetalleSalidaInventario> detallesSalida = new ArrayList<>();

        if (detalles != null) {

            for (DetalleTrasladoInventario det : detalles) {

//                if (det.getArticulo() != null && det.getCantidadEnviada() != null && det.getCantidadEnviada().compareTo(BigDecimal.ZERO) > 0) {
                DetalleSalidaInventario detSalida = new DetalleSalidaInventario();

                // 1. Obtener el factor de conversión con fallback seguro a BigDecimal.ONE
                BigDecimal factorConversion = BigDecimal.ONE;

                if (det.getArticuloEmpaque() != null && det.getArticuloEmpaque().getFactorConversion() != null) {
                    factorConversion = det.getArticuloEmpaque().getFactorConversion();
                } else if (det.getFactorConversion() != null && det.getFactorConversion().compareTo(BigDecimal.ZERO) > 0) {
                    factorConversion = det.getFactorConversion();
                }

// 2. Asignar explícitamente el factor al detalle de salida antes de operar
                detSalida.setFactorConversion(factorConversion);

// 3. Obtener la cantidad enviada de manera segura
                BigDecimal cantidad = det.getCantidadEnviada() != null
                        ? det.getCantidadEnviada()
                        : BigDecimal.ZERO;

                detSalida.setCantidad(cantidad);

// 4. Multiplicación segura evitando NullPointerException
                BigDecimal cantidadFisicaBase = cantidad.multiply(factorConversion);
                detSalida.setCantidadFisicaBase(cantidadFisicaBase);

                detSalida.setArticuloEmpaque(det.getArticuloEmpaque());

                detSalida.setSalidaInventario(salida);
                detSalida.setArticulo(det.getArticulo());
                detSalida.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detSalida.setCantidad(det.getCantidadEnviada());

                // Protección Optional al buscar la existencia por almacén
                Optional<ArticuloAlmacen> optArtiAlm = (almacenOrigen != null && det.getArticulo().getCodigo() != null)
                        ? this.articuloAlmacenService.buscarPorArticuloYAlmacen(det.getArticulo().getCodigo(), almacenOrigen.getCodigo())
                        : Optional.empty();

                BigDecimal stockActual = BigDecimal.ZERO;
                if (optArtiAlm.isPresent()) {
                    detSalida.setAlmacen(optArtiAlm.get().getAlmacen());
                    stockActual = optArtiAlm.get().getExistencia() != null ? optArtiAlm.get().getExistencia() : BigDecimal.ZERO;
                } else {
                    detSalida.setAlmacen(almacenOrigen);
                }

                detSalida.setExistenciaAnterior(stockActual);
                detSalida.setExistencia(stockActual);
                detSalida.setUnidad(det.getUnidad());

                BigDecimal costo = det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO;
                BigDecimal precio = det.getArticulo().getPrecioVenta() != null ? det.getArticulo().getPrecioVenta() : BigDecimal.ZERO;

                detSalida.setCostoUnitario(costo);
                detSalida.setPrecioCompra(costo);
                detSalida.setPrecioVenta(precio);

                BigDecimal cantidadFisicaBase1 = detSalida.getCantidad()
                        .multiply(detSalida.getFactorConversion());

                detSalida.setCantidadFisicaBase(cantidadFisicaBase1);

                detallesSalida.add(detSalida);
//                }
            }
        }

        salida.setDetalleSalidaInventarioCollection(detallesSalida);

        return guardar(salida, nombreUsuario);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalidaInventario crearSalidaPorMerma(Merma merma, List<DetalleMerma> detalles) {

        SalidaInventario salida = new SalidaInventario();

        salida.setFechaRegistro(new Date());
        salida.setFecha(merma.getFecha());
        salida.setTipoDocumento(this.tipoDocumentoService.getTipoDocumento(2));
        salida.setNumeroDocumento(merma.getCodigo() != null ? merma.getCodigo().toString() : "");
        salida.setUsuario(merma.getUsuario());

        String nombreUsuario = (merma.getUsuario() != null && merma.getUsuario().getNombre() != null)
                ? merma.getUsuario().getNombre() : "SISTEMA";

        salida.setNombreUsuario(nombreUsuario);
        salida.setObservacion("Salida por Merma de Inventario #" + merma.getCodigo() + ". "
                + (merma.getObservacion() != null ? merma.getObservacion() : ""));

        List<DetalleSalidaInventario> detallesSalida = new ArrayList<>();

        if (detalles != null) {

            for (DetalleMerma det : detalles) {

//                if (det.getArticulo() != null && det.getCantidadEnviada() != null && det.getCantidadEnviada().compareTo(BigDecimal.ZERO) > 0) {
                DetalleSalidaInventario detSalida = new DetalleSalidaInventario();

                // 1. Obtener el factor de conversión con fallback seguro a BigDecimal.ONE
                BigDecimal factorConversion = BigDecimal.ONE;

                if (det.getArticuloEmpaque() != null && det.getArticuloEmpaque().getFactorConversion() != null) {
                    factorConversion = det.getArticuloEmpaque().getFactorConversion();
                } else if (det.getFactorConversion() != null && det.getFactorConversion().compareTo(BigDecimal.ZERO) > 0) {
                    factorConversion = det.getFactorConversion();
                }

// 2. Asignar explícitamente el factor al detalle de salida antes de operar
                detSalida.setFactorConversion(factorConversion);

// 3. Obtener la cantidad enviada de manera segura
                BigDecimal cantidad = det.getCantidad() != null
                        ? det.getCantidad()
                        : BigDecimal.ZERO;

                detSalida.setCantidad(cantidad);

// 4. Multiplicación segura evitando NullPointerException
                BigDecimal cantidadFisicaBase = cantidad.multiply(factorConversion);
                detSalida.setCantidadFisicaBase(cantidadFisicaBase);

                detSalida.setArticuloEmpaque(det.getArticuloEmpaque());

                detSalida.setSalidaInventario(salida);
                detSalida.setArticulo(det.getArticulo());
                detSalida.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detSalida.setCantidad(det.getCantidad());

                // Protección Optional al buscar la existencia por almacén
                Optional<ArticuloAlmacen> optArtiAlm = (merma.getAlmacen() != null && det.getArticulo().getCodigo() != null)
                        ? this.articuloAlmacenService.buscarPorArticuloYAlmacen(det.getArticulo().getCodigo(), merma.getAlmacen().getCodigo())
                        : Optional.empty();
                BigDecimal stockActual = BigDecimal.ZERO;
//                    
                if (optArtiAlm.isPresent()) {

                    detSalida.setAlmacen(optArtiAlm.get().getAlmacen());
                    stockActual = optArtiAlm.get().getExistencia() != null ? optArtiAlm.get().getExistencia() : BigDecimal.ZERO;
                } else {
                    detSalida.setAlmacen(merma.getAlmacen());
                }

                detSalida.setExistenciaAnterior(stockActual);
                detSalida.setExistencia(stockActual);
                detSalida.setUnidad(det.getArticuloEmpaque().getUnidadEmpaque());

                BigDecimal costo = det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO;
                BigDecimal precio = det.getArticulo().getPrecioVenta() != null ? det.getArticulo().getPrecioVenta() : BigDecimal.ZERO;

                detSalida.setCostoUnitario(costo);
                detSalida.setPrecioCompra(costo);
                detSalida.setPrecioVenta(precio);

                BigDecimal cantidadFisicaBase1 = detSalida.getCantidad()
                        .multiply(detSalida.getFactorConversion());

                detSalida.setCantidadFisicaBase(cantidadFisicaBase1);

                detallesSalida.add(detSalida);
//                }
            }
        }

        salida.setDetalleSalidaInventarioCollection(detallesSalida);

        return guardar(salida, nombreUsuario);

    }
}
