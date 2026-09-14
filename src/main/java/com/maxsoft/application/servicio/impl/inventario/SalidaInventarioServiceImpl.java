package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleSalidaInventario;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.SalidaInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalidaInventarioServiceImpl implements SalidaInventarioService {

    @Autowired
    private SalidaInventarioRepo repo;

    @Autowired
    private ArticuloRepo articuloRepo; // Inyección para actualizar existencias

    @Override
    @Transactional // Garantiza atomicidad: si algo falla, no se guarda la salida ni se altera el stock
    public SalidaInventario guardar(SalidaInventario obj) {

        // 1. Guardar la salida y sus detalles
        SalidaInventario salidaGuardada = repo.save(obj);

        // 2. Descontar el stock en la tabla de artículos
        if (salidaGuardada.getDetalleSalidaInventarioCollection() != null) {
            for (DetalleSalidaInventario detalle : salidaGuardada.getDetalleSalidaInventarioCollection()) {

                if (detalle.getArticulo() != null && detalle.getArticulo().getCodigo() != null) {

                    Articulo articulo = articuloRepo.findById(detalle.getArticulo().getCodigo())
                            .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + detalle.getArticulo().getCodigo()));

                    // Solo se descuenta si el producto es inventariable
                    if (Boolean.TRUE.equals(articulo.getInventariable())) {
                        double stockActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;
                        double cantidadSalida = detalle.getCantidad() != null ? detalle.getCantidad() : 0.0;

                        // Resta directa en la existencia
                        articulo.setExistencia(stockActual - cantidadSalida);
                        articuloRepo.save(articulo);
                    }
                }
            }
        }

        return salidaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalidaInventario> getLista() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleSalidaInventario> getDetalle(int obj) {
        return repo.getDetalle(obj);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalidaInventario> getLista(boolean estado) {
        return repo.getLista(estado);
    }
}