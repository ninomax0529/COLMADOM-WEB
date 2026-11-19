package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validaciones de inventario previas a una venta. Los movimientos de existencia
 * (Kardex) los registra MovimientoInventarioService, llamado desde las salidas y
 * entradas de inventario.
 */
@Service
public class InventarioServiceImpl implements InventarioService {

    private final ArticuloRepo articuloRepo;

    public InventarioServiceImpl(ArticuloRepo articuloRepo) {
        this.articuloRepo = articuloRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public void validarStockDisponible(Integer idArticulo, BigDecimal cantidad) {
        Articulo articulo = articuloRepo.findById(idArticulo)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado ID: " + idArticulo));

        if (Boolean.FALSE.equals(articulo.getInventariable())) {
            return;
        }

        if (Boolean.TRUE.equals(articulo.getPermitirVentaSinExistencia())) {
            return;
        }

        BigDecimal existencia = existenciaDe(articulo);
        BigDecimal req = cantidad != null ? cantidad : BigDecimal.ZERO;

        // cantidad viene en UNIDAD BASE (cantidad del empaque x factor de conversión)
        if (existencia.compareTo(req) < 0) {
            throw new IllegalStateException("Existencia insuficiente para '" + articulo.getDescripcion()
                    + "'. Disponible: " + existencia.stripTrailingZeros().toPlainString()
                    + ", solicitado: " + req.stripTrailingZeros().toPlainString() + " (unidad base).");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal existenciaDisponible(Integer idArticulo) {
        return articuloRepo.findById(idArticulo).map(this::existenciaDe).orElse(BigDecimal.ZERO);
    }

    private BigDecimal existenciaDe(Articulo articulo) {
        return articulo.getExistencia() != null ? articulo.getExistencia() : BigDecimal.ZERO;
    }
}
