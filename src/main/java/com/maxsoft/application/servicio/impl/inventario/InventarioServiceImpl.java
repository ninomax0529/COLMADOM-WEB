package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.repo.ArticuloAlmacenRepo;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.maxsoft.application.util.TrasladoMostradorUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final ArticuloAlmacenRepo articuloAlmacenRepo;

    public InventarioServiceImpl(ArticuloRepo articuloRepo, ArticuloAlmacenRepo articuloAlmacenRepo) {
        this.articuloRepo = articuloRepo;
        this.articuloAlmacenRepo = articuloAlmacenRepo;
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

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<Integer, BigDecimal> existenciasPorArticulo() {
        java.util.Map<Integer, BigDecimal> mapa = new java.util.HashMap<>();
        for (Object[] fila : articuloAlmacenRepo.existenciasDelAlmacen(ALMACEN_VENTA)) {
            mapa.put((Integer) fila[0], fila[1] != null ? (BigDecimal) fila[1] : BigDecimal.ZERO);
        }
        return mapa;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoMostrador estadoMostrador(Integer idArticulo) {
        List<Object[]> filas = articuloAlmacenRepo.estadosMostrador(ALMACEN_VENTA, idArticulo);
        if (filas.isEmpty()) {
            return new EstadoMostrador(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "");
        }
        String dondeHay = articuloAlmacenRepo.existenciaEnOtrosAlmacenes(idArticulo, ALMACEN_VENTA).stream()
                .map(f -> (f[0] != null ? f[0].toString() : "otro almacén") + ": " + texto(aDecimal(f[1])))
                .collect(Collectors.joining(", "));
        return estado(filas.get(0), dondeHay);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, EstadoMostrador> estadosMostrador() {
        Map<Integer, EstadoMostrador> mapa = new HashMap<>();
        for (Object[] fila : articuloAlmacenRepo.estadosMostrador(ALMACEN_VENTA, null)) {
            mapa.put(((Number) fila[0]).intValue(), estado(fila, null));
        }
        return mapa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloPorTrasladar> porTrasladar() {
        // Filas por artículo y almacén, de más a menos existencia: la primera
        // de cada artículo es el origen
        Map<Integer, List<Object[]>> porArticulo = new java.util.LinkedHashMap<>();
        for (Object[] f : articuloAlmacenRepo.existenciasFueraDelMostrador(ALMACEN_VENTA)) {
            porArticulo.computeIfAbsent(((Number) f[0]).intValue(), k -> new java.util.ArrayList<>()).add(f);
        }

        List<ArticuloPorTrasladar> lista = new java.util.ArrayList<>();
        for (List<Object[]> filas : porArticulo.values()) {
            Object[] f = filas.get(0);
            BigDecimal mostrador = aDecimal(f[2]);
            BigDecimal minimo = aDecimal(f[3]);
            BigDecimal maximo = aDecimal(f[4]);
            BigDecimal enOrigen = aDecimal(f[7]);
            BigDecimal enOtros = filas.stream().map(x -> aDecimal(x[7])).reduce(BigDecimal.ZERO, BigDecimal::add);

            if (!TrasladoMostradorUtil.tocaTrasladar(mostrador, minimo, enOtros)) {
                continue;
            }
            lista.add(new ArticuloPorTrasladar(((Number) f[0]).intValue(), f[1] != null ? f[1].toString() : "",
                    mostrador, minimo, maximo, ((Number) f[5]).intValue(), f[6] != null ? f[6].toString() : "",
                    enOrigen, enOtros, TrasladoMostradorUtil.cantidadSugerida(mostrador, minimo, maximo, enOrigen)));
        }
        return lista;
    }

    private static EstadoMostrador estado(Object[] fila, String dondeHay) {
        return new EstadoMostrador(aDecimal(fila[1]), aDecimal(fila[2]), aDecimal(fila[3]), dondeHay);
    }

    private static BigDecimal aDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor instanceof BigDecimal b ? b : new BigDecimal(valor.toString());
    }

    private static String texto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    /**
     * Existencia en el almacén de venta (no la total del artículo, que suma
     * también el almacén principal y no se puede vender desde el POS).
     */
    private BigDecimal existenciaDe(Articulo articulo) {
        return articuloAlmacenRepo.existenciaEnAlmacen(articulo.getCodigo(), ALMACEN_VENTA)
                .orElse(BigDecimal.ZERO);
    }
}
