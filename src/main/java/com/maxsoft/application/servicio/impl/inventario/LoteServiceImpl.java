package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.DetalleFacturaVentaLote;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.repo.DetalleFacturaVentaLoteRepo;
import com.maxsoft.application.modelo.Lote;
import com.maxsoft.application.modelo.LoteAlmacen;
import com.maxsoft.application.modelo.Proveedor;
import com.maxsoft.application.repo.ArticuloAlmacenRepo;
import com.maxsoft.application.repo.LoteAlmacenRepo;
import com.maxsoft.application.repo.LoteRepo;
import com.maxsoft.application.servicio.interfaces.inventario.LoteService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoteServiceImpl implements LoteService {

    private static final DateTimeFormatter FORMATO_LOTE_AUTOMATICO = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LoteRepo loteRepo;
    private final LoteAlmacenRepo loteAlmacenRepo;
    private final ArticuloAlmacenRepo articuloAlmacenRepo;
    private final DetalleFacturaVentaLoteRepo detalleFacturaVentaLoteRepo;

    public LoteServiceImpl(LoteRepo loteRepo, LoteAlmacenRepo loteAlmacenRepo, ArticuloAlmacenRepo articuloAlmacenRepo,
            DetalleFacturaVentaLoteRepo detalleFacturaVentaLoteRepo) {
        this.loteRepo = loteRepo;
        this.loteAlmacenRepo = loteAlmacenRepo;
        this.articuloAlmacenRepo = articuloAlmacenRepo;
        this.detalleFacturaVentaLoteRepo = detalleFacturaVentaLoteRepo;
    }

    // ============================================================
    // CREACIÓN
    // ============================================================
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Lote obtenerOCrearLote(Articulo articulo, String numeroLote, LocalDate fechaVencimiento,
            BigDecimal costoUnitario, Proveedor proveedor, String documentoOrigen, String usuario) {

        if (articulo == null || articulo.getCodigo() == null) {
            throw new IllegalArgumentException("El artículo del lote no es válido.");
        }

        LocalDate hoy = LocalDate.now();
        String numero = numeroLote == null ? "" : numeroLote.trim().toUpperCase();
        if (numero.isEmpty()) {
            numero = "L" + hoy.format(FORMATO_LOTE_AUTOMATICO);
        }
        if (numero.length() > 50) {
            throw new IllegalArgumentException("El número de lote no puede pasar de 50 caracteres.");
        }

        // Vencimiento sugerido por la vida útil del artículo (solo lotes normales)
        LocalDate vence = fechaVencimiento;
        boolean loteEspecial = Lote.NUMERO_INICIAL.equals(numero) || NUMERO_SIN_LOTE.equals(numero);
        if (vence == null && !loteEspecial
                && articulo.getDiasVencimiento() != null && articulo.getDiasVencimiento() > 0) {
            vence = hoy.plusDays(articulo.getDiasVencimiento());
        }

        Lote existente = loteRepo.findByArticuloCodigoAndNumeroLote(articulo.getCodigo(), numero).orElse(null);
        if (existente != null) {
            if (fechaVencimiento != null && existente.getFechaVencimiento() != null
                    && !fechaVencimiento.equals(existente.getFechaVencimiento())) {
                throw new IllegalArgumentException("El lote " + numero + " de '" + articulo.getDescripcion()
                        + "' ya existe con vencimiento " + existente.getFechaVencimiento().format(FORMATO_FECHA)
                        + ". Use otro número de lote o el mismo vencimiento.");
            }
            if (existente.getFechaVencimiento() == null && fechaVencimiento != null) {
                existente.setFechaVencimiento(fechaVencimiento);
                existente = loteRepo.save(existente);
            }
            return existente;
        }

        Lote lote = new Lote();
        lote.setArticulo(articulo);
        lote.setNumeroLote(numero);
        lote.setFechaVencimiento(vence);
        lote.setFechaEntrada(LocalDateTime.now());
        lote.setCostoUnitario(costoUnitario != null ? costoUnitario : articulo.getPrecioCompra());
        lote.setProveedor(proveedor);
        lote.setDocumentoOrigen(documentoOrigen);
        lote.setActivo(true);
        lote.setCreadoPor(usuario != null && !usuario.isBlank() ? usuario : "SISTEMA");
        return loteRepo.save(lote);
    }

    // ============================================================
    // REPARTO DE SALIDAS (FEFO)
    // ============================================================
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<Tramo> repartirSalida(Articulo articulo, Almacen almacen, BigDecimal cantidad, Lote loteIndicado) {

        if (cantidad == null || cantidad.signum() <= 0) {
            throw new IllegalArgumentException("La cantidad de la salida debe ser mayor a cero.");
        }
        LocalDate hoy = LocalDate.now();
        List<Tramo> tramos = new ArrayList<>();

        // 1. Lote indicado por el usuario: todo sale de ahí
        if (loteIndicado != null && loteIndicado.getCodigo() != null) {
            Lote lote = loteRepo.findById(loteIndicado.getCodigo())
                    .orElseThrow(() -> new IllegalArgumentException("El lote indicado no existe."));
            if (!Objects.equals(lote.getArticulo().getCodigo(), articulo.getCodigo())) {
                throw new IllegalArgumentException("El lote " + lote.getNumeroLote()
                        + " no pertenece al artículo '" + articulo.getDescripcion() + "'.");
            }
            BigDecimal disponible = loteAlmacenRepo.findParaActualizar(lote.getCodigo(), almacen.getCodigo())
                    .map(LoteAlmacen::getExistencia).orElse(BigDecimal.ZERO);
            if (disponible.compareTo(cantidad) < 0) {
                throw new IllegalStateException("El lote " + lote.getNumeroLote() + " de '" + articulo.getDescripcion()
                        + "' solo tiene " + disponible.stripTrailingZeros().toPlainString()
                        + " (unidad base) en el almacén " + almacen.getNombre()
                        + "; se requieren " + cantidad.stripTrailingZeros().toPlainString() + ".");
            }
            tramos.add(new Tramo(lote, cantidad, lote.isVencido(hoy)));
            return tramos;
        }

        // 2. FEFO: primero los lotes vigentes, después los vencidos
        List<LoteAlmacen> filas = loteAlmacenRepo.findFefoParaActualizar(articulo.getCodigo(), almacen.getCodigo());
        BigDecimal pendiente = cantidad;

        for (boolean pasadaVencidos : new boolean[]{false, true}) {
            for (LoteAlmacen fila : filas) {
                if (pendiente.signum() <= 0) {
                    break;
                }
                boolean vencido = fila.getLote().isVencido(hoy);
                if (vencido != pasadaVencidos) {
                    continue;
                }
                BigDecimal toma = fila.getExistencia().min(pendiente);
                if (toma.signum() > 0) {
                    tramos.add(new Tramo(fila.getLote(), toma, vencido));
                    pendiente = pendiente.subtract(toma);
                }
            }
        }

        // 3. Lo que no alcanzó (venta sin existencia): lote SIN-LOTE en negativo
        if (pendiente.signum() > 0) {
            Lote sinLote = obtenerOCrearLote(articulo, NUMERO_SIN_LOTE, null, null, null, NUMERO_SIN_LOTE, "SISTEMA");
            tramos.add(new Tramo(sinLote, pendiente, false));
        }
        return tramos;
    }

    // ============================================================
    // EXISTENCIAS
    // ============================================================
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public BigDecimal[] moverExistencia(Lote lote, Almacen almacen, BigDecimal delta) {

        LoteAlmacen fila = loteAlmacenRepo.findParaActualizar(lote.getCodigo(), almacen.getCodigo())
                .orElseGet(() -> new LoteAlmacen(lote, almacen));
        BigDecimal anterior = fila.getExistencia();
        BigDecimal nueva = anterior.add(delta);
        fila.setExistencia(nueva);
        loteAlmacenRepo.save(fila);
        return new BigDecimal[]{anterior, nueva};
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sincronizarLotes(Articulo articulo, String usuario) {

        if (articulo == null || articulo.getCodigo() == null || !articulo.isControlaLote()) {
            return 0;
        }
        List<ArticuloAlmacen> almacenes = articuloAlmacenRepo.buscarPorArticulo(articulo.getCodigo())
                .orElse(List.of());

        int ajustados = 0;
        for (ArticuloAlmacen aa : almacenes) {
            if (cuadrarAlmacen(articulo, aa.getAlmacen(), aa.getExistencia(), usuario)) {
                ajustados++;
            }
        }
        return ajustados;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean cuadrarAlmacen(Articulo articulo, Almacen almacen, BigDecimal existenciaAlmacen, String usuario) {

        if (articulo == null || !articulo.isControlaLote()) {
            return false;
        }
        BigDecimal existencia = existenciaAlmacen != null ? existenciaAlmacen : BigDecimal.ZERO;
        BigDecimal sumaLotes = loteAlmacenRepo.sumarExistencia(articulo.getCodigo(), almacen.getCodigo());
        BigDecimal diferencia = existencia.subtract(sumaLotes != null ? sumaLotes : BigDecimal.ZERO);
        if (diferencia.signum() == 0) {
            return false;
        }
        Lote inicial = obtenerOCrearLote(articulo, Lote.NUMERO_INICIAL, null, articulo.getPrecioCompra(),
                null, Lote.NUMERO_INICIAL, usuario);
        moverExistencia(inicial, almacen, diferencia);
        return true;
    }

    // ============================================================
    // VENTAS: vencidos, consumo y reposición
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public String describirVentaDeVencidos(Articulo articulo, Almacen almacen, BigDecimal cantidadBase) {

        if (articulo == null || !articulo.isControlaLote() || cantidadBase == null || cantidadBase.signum() <= 0) {
            return null;
        }
        LocalDate hoy = LocalDate.now();
        BigDecimal vigente = BigDecimal.ZERO;
        BigDecimal vencido = BigDecimal.ZERO;
        List<Lote> lotesVencidos = new ArrayList<>();
        for (LoteAlmacen la : loteAlmacenRepo.findDisponibles(articulo.getCodigo(), almacen.getCodigo())) {
            if (la.getLote().isVencido(hoy)) {
                vencido = vencido.add(la.getExistencia());
                lotesVencidos.add(la.getLote());
            } else {
                vigente = vigente.add(la.getExistencia());
            }
        }
        BigDecimal deVencidos = cantidadBase.subtract(vigente).max(BigDecimal.ZERO).min(vencido);
        if (deVencidos.signum() <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(articulo.getDescripcion()).append(": ")
                .append(deVencidos.stripTrailingZeros().toPlainString())
                .append(" de ").append(cantidadBase.stripTrailingZeros().toPlainString())
                .append(" (unidad base) saldrían de lote vencido (");
        for (int i = 0; i < lotesVencidos.size(); i++) {
            Lote l = lotesVencidos.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(l.getNumeroLote()).append(" venció ").append(l.getFechaVencimiento().format(FORMATO_FECHA));
        }
        return sb.append(")").toString();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void registrarConsumoVenta(DetalleFacturaDeVenta detalle, List<MovimientoInventario> movimientos,
            String autorizadoPor) {

        if (detalle == null || detalle.getCodigo() == null || movimientos == null) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        for (MovimientoInventario mov : movimientos) {
            if (mov.getLote() == null) {
                continue;
            }
            boolean vencido = mov.getLote().isVencido(hoy);
            DetalleFacturaVentaLote fila = new DetalleFacturaVentaLote();
            fila.setDetalleFacturaDeVenta(detalle);
            fila.setLote(mov.getLote());
            fila.setAlmacen(mov.getAlmacen());
            fila.setCantidad(mov.getCantidad());
            fila.setCantidadDevuelta(BigDecimal.ZERO);
            fila.setLoteVencido(vencido);
            fila.setAutorizadoPor(vencido ? autorizadoPor : null);
            fila.setFecha(LocalDateTime.now());
            detalleFacturaVentaLoteRepo.save(fila);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<Tramo> repartirReposicionVenta(DetalleFacturaDeVenta detalle, BigDecimal cantidadBase) {

        List<Tramo> tramos = new ArrayList<>();
        if (cantidadBase == null || cantidadBase.signum() <= 0) {
            return tramos;
        }
        BigDecimal pendiente = cantidadBase;
        if (detalle != null && detalle.getCodigo() != null) {
            for (DetalleFacturaVentaLote fila : detalleFacturaVentaLoteRepo.findPorDetalle(detalle.getCodigo())) {
                if (pendiente.signum() <= 0) {
                    break;
                }
                BigDecimal toma = fila.getPendientePorDevolver().min(pendiente);
                if (toma.signum() <= 0) {
                    continue;
                }
                fila.setCantidadDevuelta(fila.getCantidadDevuelta().add(toma));
                detalleFacturaVentaLoteRepo.save(fila);
                tramos.add(new Tramo(fila.getLote(), toma, false));
                pendiente = pendiente.subtract(toma);
            }
        }
        if (pendiente.signum() > 0) {
            tramos.add(new Tramo(null, pendiente, false));
        }
        return tramos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteAlmacen> getLotesDisponibles(Integer codigoArticulo, Integer codigoAlmacen) {
        return loteAlmacenRepo.findDisponibles(codigoArticulo, codigoAlmacen);
    }

    // ============================================================
    // CONSULTAS
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<LoteAlmacen> getExistenciasPorArticulo(Integer codigoArticulo) {
        return loteAlmacenRepo.findPorArticulo(codigoArticulo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteAlmacen> getLotesConExistencia() {
        return loteAlmacenRepo.findConExistencia();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDescuadres() {
        return loteAlmacenRepo.findDescuadres();
    }
}
