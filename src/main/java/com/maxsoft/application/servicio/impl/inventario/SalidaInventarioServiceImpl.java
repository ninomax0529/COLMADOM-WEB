package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.SalidaInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import java.util.Collection;
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
        SalidaInventario salidaGuardada = salidaRepo.save(obj);

        String numDocumento = "SAL-" + (salidaGuardada.getCodigo() != null
                ? salidaGuardada.getCodigo()
                : System.currentTimeMillis());

        // Obtener catálogos para tipo de documento y movimiento (Ajustar ID según corresponda a Salida)
        TipoDocumento tp = this.tipoDocumentoService.getTipoDocumento(2); // Ej: 2 = Salida de Inventario
        TipoMovimiento tm = this.tipoMovimientoService.getTipoMovimientoa(2); // Ej: 2 = Salida / Decremento

        // 2. Usar directamente la colección recibida en 'obj' desde la interfaz de Vaadin
        Collection<DetalleSalidaInventario> detalles = obj.getDetalleSalidaInventarioCollection();

        if (detalles != null && !detalles.isEmpty()) {

            for (DetalleSalidaInventario detalle : detalles) {

                // Vincular explícitamente la cabecera guardada con cada renglón
                detalle.setSalidaInventario(salidaGuardada);

                Articulo articuloProxy = detalle.getArticulo();

                if (articuloProxy != null && articuloProxy.getCodigo() != null) {

                    // Cargar el artículo fresco desde el repositorio para evitar Lazy Proxy / campos nulos
                    Articulo articulo = articuloRepo.findById(articuloProxy.getCodigo())
                            .orElse(articuloProxy);

//                    boolean esInventariable = articulo.getInventariable() == null || Boolean.TRUE.equals(articulo.getInventariable());
//
//                    if (esInventariable) {
                        
                        double cantidadSalida = detalle.getCantidad() != null ? detalle.getCantidad() : 0.0;

                        if (cantidadSalida > 0) {
                            // Registra el movimiento en el Kardex y descuenta el stock real.
                            // Si el stock es insuficiente, lanza IllegalStateException y hace Rollback automático.
                            movimientoService.registrarMovimiento(
                                    articulo,
                                    tm,
                                    tp,
                                    numDocumento,
                                    cantidadSalida,
                                    usuario,
                                    obj.getObservacion()
                            );
                        }
                    }
                }
//            }
        }

        return salidaGuardada;
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
    public List<SalidaInventario> getLista(boolean estado) {

        return salidaRepo.getLista(estado);
    }


}