package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.modelo.TrasladoInventario;
import com.maxsoft.application.repo.EntradaDeInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EntradaDeInventarioServiceImpl implements EntradaDeInventarioService {

    private final EntradaDeInventarioRepo entradaRepo;
    private final MovimientoInventarioService movimientoService;
    TipoMovimientoService tipoMovimientoService;
    TipoDocumentoService tipoDocumentoService;
    ArticuloAlmacenService articuloAlmacenService;

    @Autowired
    public EntradaDeInventarioServiceImpl(EntradaDeInventarioRepo entradaRepo,
            MovimientoInventarioService movimientoService,
            TipoMovimientoService tipoMovimientoService,
            TipoDocumentoService tipoDocumentoService,
            ArticuloAlmacenService articuloAlmacenService
    ) {
        this.entradaRepo = entradaRepo;
        this.movimientoService = movimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.tipoMovimientoService = tipoMovimientoService;
        this.articuloAlmacenService = articuloAlmacenService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntradaInventario guardar(EntradaInventario obj, String usuario) {

        // 1. Guardar la cabecera
        EntradaInventario entradaGuardada = entradaRepo.save(obj);

        String numDocumento = "ENT-" + (entradaGuardada.getCodigo() != null
                ? entradaGuardada.getCodigo()
                : System.currentTimeMillis());

        TipoDocumento tp = this.tipoDocumentoService.getTipoDocumento(1);
        TipoMovimiento tm = this.tipoMovimientoService.getTipoMovimientoa(1);

        // 2. Usar la colección del parámetro 'obj' recibido en lugar de 'entradaGuardada'
        Collection<DetalleEntradaInventario> detalles = obj.getDetalleEntradaInventarioCollection();

        if (detalles != null && !detalles.isEmpty()) {

            for (DetalleEntradaInventario detalle : detalles) {

                // Asignar manualmente la cabecera ya persistida a cada detalle
                detalle.setEntradaInventario(entradaGuardada);

//                Articulo articuloProxy = detalle.getArticulo();
//                if (articuloProxy != null && articuloProxy.getCodigo() != null) {
                Articulo articulo = detalle.getArticulo();
                Almacen alm = detalle.getAlmacen();
                // Cargar el artículo fresco desde el repositorio para evitar Lazy Proxy / inventariable null
//                    Articulo articulo = articuloRepo.findById(articuloProxy.getCodigo())
//                            .orElse(articuloProxy);
//
//                    boolean esInventariable = articulo.getInventariable() == null || Boolean.TRUE.equals(articulo.getInventariable());
//
//                    if (esInventariable) {
                double cantidadEntrante = detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida().doubleValue() : 0.0;

                if (cantidadEntrante > 0) {

                    movimientoService.registrarMovimiento(
                            articulo,
                            alm,
                            tm,
                            tp,
                            numDocumento,
                            cantidadEntrante,
                            usuario,
                            obj.getComentario()
                    );
                }
//                    }
//                }
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

    @Transactional
    @Override
    public EntradaInventario crearEntradaPorDevolucion(FacturaDeVenta factura, SolicitudDevolucionDto solicitud) {

        EntradaInventario entrada = new EntradaInventario();
        Date fechaActual = new Date();

        // 1. Asignar Fechas
        entrada.setFecha(fechaActual);
        entrada.setFechaCreacion(fechaActual);
        entrada.setFechaActualizacion(fechaActual);
        entrada.setFechaContabilizacion(fechaActual);

        // 2. Tipos de Documento y Movimiento (Tipos Integer según tu modelo)
        entrada.setTipoDocumento(7); // ID del Tipo de Documento 'Devolucion de Venta'
        entrada.setNumeroDocumento("FACT-" + factura.getCodigo()); // Guardamos el número de factura como documento de origen

        // 3. Moneda (Valores por defecto si no están configurados)
        entrada.setMoneda(1);
        entrada.setNombreMoneda("DOP"); // O la moneda correspondiente en tu sistema

        // 4. Usuario y Observaciones
        entrada.setNombreUsuario(solicitud.getUsuario());
        entrada.setAnulada(false);

        String tipoDevStr = Boolean.TRUE.equals(factura.getAnulada()) ? "Devolución Total" : "Devolución Parcial";
        entrada.setComentario(tipoDevStr + " de Factura #" + factura.getCodigo() + ". Motivo: " + solicitud.getMotivo());

        // 5. Construir Colección de Detalles
        List<DetalleEntradaInventario> detallesList = new ArrayList<>();

        for (SolicitudDevolucionDto.ItemDevolucionDto itemDev : solicitud.getItems()) {

            DetalleFacturaDeVenta detFactura = factura.getDetalleFacturaDeVentaCollection().stream()
                    .filter(d -> d.getCodigo().equals(itemDev.getIdDetalleFactura()))
                    .findFirst()
                    .orElse(null);

            if (detFactura != null && Boolean.TRUE.equals(detFactura.getArticulo().getInventariable())) {

                DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();

                // Vincular relación bidireccional
                detEntrada.setEntradaInventario(entrada);
                detEntrada.setArticulo(detFactura.getArticulo());
                detEntrada.setCantidadRecibida(BigDecimal.valueOf(itemDev.getCantidadADevolver()));
                detEntrada.setArticulo(detFactura.getArticulo());

                double stockActual = detEntrada.getArticulo().getExistencia()
                        != null ? detEntrada.getArticulo().getExistencia().doubleValue() : 0.0;

                detEntrada.setExistenciaActual(BigDecimal.valueOf(stockActual));

                detEntrada.setCantidadPedida(BigDecimal.ZERO);
                detEntrada.setCantidadPendiente(BigDecimal.ZERO);
                detEntrada.setNuevaExistencia(BigDecimal.valueOf(stockActual + itemDev.getCantidadADevolver()));
                detEntrada.setNombreAlmacen("General");
                detEntrada.setNombreUnidad("Unidad");
                detEntrada.setUnidad(detFactura.getArticulo().getUnidadEntrada());
                detEntrada.setPrecioCompra(detFactura.getArticulo().getPrecioCompra());
                detEntrada.setAlmacen(new Almacen(1));

                // CORRECCIÓN: Asignar la descripción requerida por Bean Validation (@NotNull)
                detEntrada.setDescripcionArticulo(detFactura.getArticulo().getDescripcion());

                // Si tu entidad DetalleEntradaInventario maneja costos/precios:
                BigDecimal costo = detFactura.getPrecioCompra() != null ? detFactura.getPrecioCompra() : BigDecimal.ZERO;
                double precio = detFactura.getPrecioVenta() != null ? detFactura.getPrecioVenta().doubleValue()
                        : detFactura.getPrecioCompra().doubleValue();

                detEntrada.setCostoUnitario(costo);
                detEntrada.setPrecioVenta(BigDecimal.valueOf(precio));
                detEntrada.setPrecioCompra(detFactura.getArticulo().getPrecioCompra());

                detallesList.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesList);

        // Guardar (CascadeType.ALL se encargará de insertar los detalles en la BD)
        return entradaRepo.save(entrada);
    }

    @Override
    public EntradaInventario crearEntradaPorAjuste(AjusteInventario ajuste, List<DetalleAjusteInventario> detalleAju) {

        EntradaInventario entrada = new EntradaInventario();

        Date fechaActual = new Date();
        entrada.setFecha(fechaActual);
        entrada.setFechaCreacion(fechaActual);
        entrada.setFechaActualizacion(fechaActual);
        entrada.setFechaContabilizacion(fechaActual);

        // Tipo de Documento e Identificación (Ajuste Positivo)
        entrada.setTipoDocumento(3); // ID del Tipo de Documento 'Ajuste de Inventario'
        entrada.setNumeroDocumento("AJ-" + ajuste.getCodigo());

        // Moneda
        entrada.setMoneda(1);
        entrada.setNombreMoneda("DOP");

        // Usuario y Observaciones
        entrada.setNombreUsuario(ajuste.getUsuario() != null ? ajuste.getUsuario().getNombre() : "SISTEMA");
        entrada.setAnulada(false);
        entrada.setComentario("Entrada por Ajuste de Inventario #" + ajuste.getCodigo() + ". "
                + (ajuste.getObservacion() != null ? ajuste.getObservacion() : ""));

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();
        for (DetalleAjusteInventario det : detalleAju) {

            if (det.getArticulo() != null && det.getCantidad() != null && det.getCantidad().doubleValue() > 0) {

                DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();
                detEntrada.setEntradaInventario(entrada); // Vinculación bidireccional
                detEntrada.setArticulo(det.getArticulo());
                detEntrada.setCantidadRecibida(det.getCantidad());

                // Toma la descripción desde la relación del artículo
                detEntrada.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detEntrada.setCostoUnitario(det.getArticulo().getPrecioCompra() != null
                        ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO);

                double stockActual = det.getArticulo().getExistencia() != null ? det.getArticulo().getExistencia().doubleValue() : 0.0;
                detEntrada.setExistenciaActual(BigDecimal.valueOf(stockActual));

                detEntrada.setCantidadPedida(BigDecimal.ZERO);
                detEntrada.setCantidadPendiente(BigDecimal.ZERO);
                detEntrada.setNuevaExistencia(BigDecimal.valueOf(stockActual + det.getCantidad().doubleValue()));
                detEntrada.setNombreAlmacen("General");
                detEntrada.setNombreUnidad("Unidad");
                detEntrada.setUnidad(det.getArticulo().getUnidadEntrada());
                detEntrada.setPrecioCompra(det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO);
                detEntrada.setAlmacen(new Almacen(1));

                detallesEntrada.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);

        // Al guardar se dispara EntradaInventarioCreadaEvent hacia el Kardex
        guardar(entrada, entrada.getNombreUsuario());

        return entrada;
    }

    @Override
    public EntradaInventario crearEntradaPorAnulacionVenta(FacturaDeVenta factura, List<DetalleFacturaDeVenta> listaDetFact) {

        EntradaInventario entrada = new EntradaInventario();

        Date fechaActual = new Date();
        entrada.setFecha(fechaActual);
        entrada.setFechaCreacion(fechaActual);
        entrada.setFechaActualizacion(fechaActual);
        entrada.setFechaContabilizacion(fechaActual);

        // Tipo de Documento e Identificación (Ajuste Positivo)
        entrada.setTipoDocumento(6); // ID del Tipo de Documento 'Anulacion de Factura'
        entrada.setNumeroDocumento("AN-" + factura.getCodigo());

        // Moneda
        entrada.setMoneda(1);
        entrada.setNombreMoneda("DOP");

        // Usuario y Observaciones
        entrada.setNombreUsuario(factura.getNombreUsuario() != null ? factura.getNombreUsuario() : "SISTEMA");
        entrada.setAnulada(false);
        entrada.setComentario("Entrada por anulacion de venta #" + factura.getCodigo() + ". "
                + (factura.getComentario() != null ? factura.getComentario() : ""));

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();

        for (DetalleFacturaDeVenta det : listaDetFact) {

            if (det.getArticulo() != null && det.getCantidad() != null && det.getCantidad().doubleValue() > 0) {

                DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();
                detEntrada.setEntradaInventario(entrada); // Vinculación bidireccional
                detEntrada.setArticulo(det.getArticulo());
                detEntrada.setCantidadRecibida(det.getCantidad());

                // Toma la descripción desde la relación del artículo
                detEntrada.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detEntrada.setCostoUnitario(det.getArticulo().getPrecioCompra() != null
                        ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO);

                BigDecimal existenciaActual = det.getArticulo().getExistencia() != null
                        ? det.getArticulo().getExistencia() : BigDecimal.ZERO;
                detEntrada.setExistenciaActual(existenciaActual);

                detEntrada.setCantidadPedida(BigDecimal.ZERO);
                detEntrada.setCantidadPendiente(BigDecimal.ZERO);
                detEntrada.setNuevaExistencia(existenciaActual.add(det.getCantidad()));
                detEntrada.setNombreAlmacen(det.getAlmacen().getNombre());
                detEntrada.setNombreUnidad(det.getNombreUnidad());
                detEntrada.setUnidad(det.getArticulo().getUnidadEntrada());
                detEntrada.setPrecioCompra(det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO);
                detEntrada.setAlmacen(det.getAlmacen());

                detallesEntrada.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);

        // Al guardar se dispara EntradaInventarioCreadaEvent hacia el Kardex
        guardar(entrada, entrada.getNombreUsuario());

        return entrada;

    }

    @Override
    public EntradaInventario crearEntradaPorRecepcion(RecepcionMercancia recepcion, List<DetalleRecepcionMercancia> detalles) {

        EntradaInventario entrada = new EntradaInventario();

        Date fechaActual = new Date();
        entrada.setFecha(fechaActual);
        entrada.setFechaCreacion(fechaActual);
        entrada.setFechaActualizacion(fechaActual);
        entrada.setFechaContabilizacion(fechaActual);

        // Tipo de Documento e Identificación (Ajuste Positivo)
        entrada.setTipoDocumento(9); // ID del Tipo de Documento 'Recepcion de mercancia'
        entrada.setNumeroDocumento("REC-" + recepcion.getCodigo());

        // Moneda
        entrada.setMoneda(1);
        entrada.setNombreMoneda("DOP");

        // Usuario y Observaciones
        entrada.setNombreUsuario(recepcion.getUsuario() != null ? recepcion.getUsuario().getNombre() : "SISTEMA");
        entrada.setAnulada(false);
        entrada.setComentario("Entrada por Recepcion de Mercancia #" + recepcion.getCodigo() + ". "
                + (recepcion.getComentario() != null ? recepcion.getComentario() : ""));

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();
        for (DetalleRecepcionMercancia det : detalles) {

            if (det.getArticulo() != null && det.getCantidadRecibida() != null && det.getCantidadRecibida().doubleValue() > 0) {

                DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();
                detEntrada.setEntradaInventario(entrada); // Vinculación bidireccional
                detEntrada.setArticulo(det.getArticulo());
                detEntrada.setCantidadRecibida(det.getCantidadRecibida());

                // Toma la descripción desde la relación del artículo
                detEntrada.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detEntrada.setCostoUnitario(det.getArticulo().getPrecioCompra() != null
                        ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO);

                double stockActual = det.getArticulo().getExistencia() != null ? det.getArticulo().getExistencia().doubleValue() : 0.0;
                detEntrada.setExistenciaActual(BigDecimal.valueOf(stockActual));

                detEntrada.setCantidadPedida(BigDecimal.ZERO);
                detEntrada.setCantidadPendiente(BigDecimal.ZERO);
                detEntrada.setNuevaExistencia(BigDecimal.valueOf(stockActual + det.getCantidadRecibida().doubleValue()));
                detEntrada.setNombreAlmacen("General");
                detEntrada.setNombreUnidad("Unidad");
                detEntrada.setUnidad(det.getArticulo().getUnidadEntrada());
                detEntrada.setPrecioCompra(det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO);
                detEntrada.setAlmacen(new Almacen(1));

                detallesEntrada.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);

        // Al guardar se dispara EntradaInventarioCreadaEvent hacia el Kardex
        guardar(entrada, entrada.getNombreUsuario());

        return entrada;
    }

    @Override
    public EntradaInventario crearEntradaPorTraslado(TrasladoInventario traslado, List<DetalleTrasladoInventario> detalles) {

        EntradaInventario entrada = new EntradaInventario();

        Date fechaActual = new Date();
        entrada.setFecha(fechaActual);
        entrada.setFechaCreacion(fechaActual);
        entrada.setFechaActualizacion(fechaActual);
        entrada.setFechaContabilizacion(fechaActual);

        // Tipo de Documento e Identificación (Ajuste Positivo)
        entrada.setTipoDocumento(3); // ID del Tipo de Documento 'Ajuste de Inventario'
        entrada.setNumeroDocumento("AJ-" + traslado.getCodigo());

        // Moneda
        entrada.setMoneda(1);
        entrada.setNombreMoneda("DOP");

        // Usuario y Observaciones
        entrada.setNombreUsuario(traslado.getUsuarioEnvia() != null ? traslado.getUsuarioEnvia().getNombre() : "SISTEMA");
        entrada.setAnulada(false);
        entrada.setComentario("Entrada por Traslado de Mercancia #" + traslado.getCodigo() + ". "
                + (traslado.getObservacion() != null ? traslado.getObservacion() : ""));

        Almacen almacen = traslado.getAlmacenDestino();

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();

        for (DetalleTrasladoInventario det : detalles) {

            if (det.getArticulo() != null && det.getCantidadRecibida() != null && det.getCantidadRecibida().doubleValue() > 0) {

                DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();
                detEntrada.setEntradaInventario(entrada); // Vinculación bidireccional
                detEntrada.setArticulo(det.getArticulo());
                detEntrada.setCantidadRecibida(det.getCantidadRecibida());

                // Toma la descripción desde la relación del artículo
                detEntrada.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detEntrada.setCostoUnitario(det.getArticulo().getPrecioCompra() != null
                        ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO);

                ArticuloAlmacen artiAlm = this.articuloAlmacenService
                        .buscarPorArticuloYAlmacen(det.getArticulo().getCodigo(), almacen.getCodigo()).get();

                double stockActual =artiAlm.getExistencia() != null ? artiAlm.getExistencia().doubleValue() : 0.0;
                
                detEntrada.setExistenciaActual(BigDecimal.valueOf(stockActual));

                detEntrada.setCantidadPedida(det.getCantidadEnviada());
                detEntrada.setCantidadPendiente(BigDecimal.ZERO);
                detEntrada.setNuevaExistencia(BigDecimal.valueOf(stockActual + det.getCantidadRecibida().doubleValue()));
                detEntrada.setNombreAlmacen(almacen.getNombre());
                detEntrada.setNombreUnidad(det.getNombreUnidad());
                detEntrada.setUnidad(det.getUnidad());
                detEntrada.setPrecioCompra(det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO);
                detEntrada.setAlmacen(almacen);

                detallesEntrada.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);

        // Al guardar se dispara EntradaInventarioCreadaEvent hacia el Kardex
        guardar(entrada, entrada.getNombreUsuario());

        return entrada;
    }

}
