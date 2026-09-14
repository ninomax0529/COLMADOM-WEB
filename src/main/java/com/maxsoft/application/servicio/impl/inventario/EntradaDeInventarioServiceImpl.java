package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.EntradaDeInventarioRepo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntradaDeInventarioServiceImpl implements EntradaDeInventarioService {

    @Autowired
    private EntradaDeInventarioRepo entradaRepo;

    @Autowired
    private ArticuloRepo articuloRepo; // Inyección para actualizar la tabla Artículo directamente

    @Override
    @Transactional // Garantiza que si falla la actualización del stock, la entrada no se guarda
    public EntradaInventario guardar(EntradaInventario obj) {

        // 1. Guardar el registro de la entrada y sus detalles
        EntradaInventario entradaGuardada = entradaRepo.save(obj);

        // 2. Actualizar el stock en la tabla de artículos
        if (entradaGuardada.getDetalleEntradaInventarioCollection() != null) {
            for (DetalleEntradaInventario detalle : entradaGuardada.getDetalleEntradaInventarioCollection()) {
                
                // Obtener el artículo original desde el detalle
                if (detalle.getArticulo() != null && detalle.getArticulo().getCodigo() != null) {
                    
                    Articulo articulo = articuloRepo.findById(detalle.getArticulo().getCodigo())
                            .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + detalle.getArticulo().getCodigo()));

                    // Solo actualizar si el artículo está configurado como inventariable
                    if (Boolean.TRUE.equals(articulo.getInventariable())) {
                        double stockActual = articulo.getExistencia() != null ? articulo.getExistencia() : 0.0;
                        double cantidadEntrante = detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0.0;

                        // Suma algebraica: Regulariza automáticamente existencias en negativo (-5 + 12 = 7)
                        articulo.setExistencia(stockActual + cantidadEntrante);
                        articuloRepo.save(articulo);
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
}