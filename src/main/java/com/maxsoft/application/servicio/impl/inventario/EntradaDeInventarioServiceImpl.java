package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.EntradaDeInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import java.util.Collection;
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

    @Autowired
    public EntradaDeInventarioServiceImpl(EntradaDeInventarioRepo entradaRepo,
            MovimientoInventarioService movimientoService,
            TipoMovimientoService tipoMovimientoService,
            TipoDocumentoService tipoDocumentoService
    ) {
        this.entradaRepo = entradaRepo;
        this.movimientoService = movimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.tipoMovimientoService = tipoMovimientoService;
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

                      Articulo articulo =detalle.getArticulo();
                    // Cargar el artículo fresco desde el repositorio para evitar Lazy Proxy / inventariable null
//                    Articulo articulo = articuloRepo.findById(articuloProxy.getCodigo())
//                            .orElse(articuloProxy);
//
//                    boolean esInventariable = articulo.getInventariable() == null || Boolean.TRUE.equals(articulo.getInventariable());
//
//                    if (esInventariable) {
                        double cantidadEntrante = detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0.0;

                        if (cantidadEntrante > 0) {
                            
                            movimientoService.registrarMovimiento(
                                    articulo,
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
}
