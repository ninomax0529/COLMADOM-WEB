package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.SalidaInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

    @Autowired
    public SalidaInventarioServiceImpl(SalidaInventarioRepo salidaRepo,
            ArticuloRepo articuloRepo,
            MovimientoInventarioService movimientoService,
            TipoDocumentoService tipoDocumentoService,
            TipoMovimientoService tipoMovimientoService) {
        
        this.salidaRepo = salidaRepo;
        this.articuloRepo = articuloRepo;
        this.movimientoService = movimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.tipoMovimientoService = tipoMovimientoService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalidaInventario guardar(SalidaInventario obj, String usuario) {

        // 1. Guardar la cabecera del documento de salida
        SalidaInventario salidaGuardada = salidaRepo.saveAndFlush(obj);

        String numDocumento = "SAL-" + (salidaGuardada.getNumeroDocumento() != null
                ? salidaGuardada.getNumeroDocumento()
                : System.currentTimeMillis());

        // Obtener catálogos para tipo de documento y movimiento (Ej: 2 = Salida de Inventario)
        TipoDocumento tp = this.tipoDocumentoService.getTipoDocumento(2);
        TipoMovimiento tm = this.tipoMovimientoService.getTipoMovimientoa(2);

        // 2. Procesar detalles para impacto en el Kardex
        Collection<DetalleSalidaInventario> detalles = salidaGuardada.getDetalleSalidaInventarioCollection();

        if (detalles != null && !detalles.isEmpty()) {

            for (DetalleSalidaInventario detalle : detalles) {

                detalle.setSalidaInventario(salidaGuardada);
                Articulo articuloProxy = detalle.getArticulo();

                if (articuloProxy != null && articuloProxy.getCodigo() != null) {

                    Articulo articulo = articuloRepo.findById(articuloProxy.getCodigo())
                            .orElse(articuloProxy);

                    double cantidadSalida = detalle.getCantidad() != null ? detalle.getCantidad().doubleValue() : 0.0;

                    if (cantidadSalida > 0) {
                        movimientoService.registrarMovimiento(
                                articulo,
                                tm,
                                tp,
                                numDocumento,
                                cantidadSalida,
                                usuario != null ? usuario : "SISTEMA",
                                obj.getObservacion()
                        );
                    }
                }
            }
        }

        return salidaGuardada;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalidaInventario crearSalidaPorVenta(FacturaDeVenta factura) {

        SalidaInventario salida = new SalidaInventario();
        Date fechaActual = new Date();

        salida.setFecha(factura.getFecha());
        salida.setFechaRegistro(new Date());
        salida.setFechaContabilizacion(factura.getFecha());

        salida.setTipoDocumento(this.tipoDocumentoService.getTipoDocumento(5)); // ID 5: Venta POS
        salida.setNumeroDocumento(factura.getCodigo().toString());
        salida.setUsuario(new Usuario(1));
        salida.setNombreUsuario(factura.getNombreUsuario() != null ? factura.getNombreUsuario() : "Admin");
        salida.setAnulada(false);
        salida.setObservacion("Salida automática por Factura POS #" + factura.getCodigo());

        List<DetalleSalidaInventario> detalles = new ArrayList<>();

        for (DetalleFacturaDeVenta detFactura : factura.getDetalleFacturaDeVentaCollection()) {

            if (Boolean.TRUE.equals(detFactura.getArticulo().getInventariable())) {

                DetalleSalidaInventario detSalida = new DetalleSalidaInventario();
                detSalida.setSalidaInventario(salida);
                detSalida.setArticulo(detFactura.getArticulo());
                detSalida.setDescripcionArticulo(detFactura.getArticulo().getDescripcion());
                detSalida.setCantidad(detFactura.getCantidad());

                double stockActual = detFactura.getArticulo().getExistencia() != null 
                        ? detFactura.getArticulo().getExistencia().doubleValue() : 0.0;
                
                detSalida.setExistenciaAnterior(BigDecimal.valueOf(stockActual));
                detSalida.setExistencia(BigDecimal.valueOf(stockActual));
                detSalida.setUnidad(detFactura.getArticulo().getUnidadSalida());
                detSalida.setCostoUnitario(detFactura.getPrecioCompra());
                
                detSalida.setPrecioCompra(detFactura.getPrecioCompra());
                detSalida.setPrecioVenta(detFactura.getPrecioVenta() != null ? detFactura.getPrecioVenta() : BigDecimal.ZERO );

                detalles.add(detSalida);
            }
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
        salida.setNumeroDocumento(ajuste.getCodigo().toString());
        salida.setUsuario(ajuste.getUsuario() != null ? ajuste.getUsuario() : new Usuario(1));
        
        String nombreUsuario = (ajuste.getUsuario() != null && ajuste.getUsuario().getNombre() != null) 
                ? ajuste.getUsuario().getNombre() : "SISTEMA";
        
        salida.setNombreUsuario(nombreUsuario);
        salida.setObservacion("Salida por Ajuste de Inventario #" + ajuste.getCodigo() + ". " 
                + (ajuste.getObservacion() != null ? ajuste.getObservacion() : ""));

        List<DetalleSalidaInventario> detallesSalida = new ArrayList<>();

        for (DetalleAjusteInventario det : detalles) {

            if (det.getArticulo() != null && det.getCantidad() != null && det.getCantidad().doubleValue() > 0) {

                DetalleSalidaInventario detSalida = new DetalleSalidaInventario();
                detSalida.setSalidaInventario(salida);
                detSalida.setArticulo(det.getArticulo());
                detSalida.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detSalida.setCantidad(det.getCantidad());

                double stockActual = det.getArticulo().getExistencia() != null ? det.getArticulo().getExistencia().doubleValue() : 0.0;
                detSalida.setExistenciaAnterior(BigDecimal.valueOf(stockActual));
                detSalida.setExistencia(BigDecimal.valueOf(stockActual));
                detSalida.setUnidad(det.getArticulo().getUnidadSalida());
                detSalida.setCostoUnitario(det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO  );
                
                detSalida.setPrecioCompra(det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : BigDecimal.ZERO );
                
                detSalida.setPrecioVenta(det.getArticulo().getPrecioVenta() != null ? det.getArticulo().getPrecioVenta() : BigDecimal.ZERO);

                detallesSalida.add(detSalida);
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
}