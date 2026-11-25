package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloClasificacionAbc;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.Configuracion;
import com.maxsoft.application.modelo.ConteoInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.DetalleConteoInventario;
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.AlmacenRepo;
import com.maxsoft.application.repo.ArticuloAlmacenRepo;
import com.maxsoft.application.repo.ArticuloClasificacionAbcRepo;
import com.maxsoft.application.repo.ArticuloEmpaqueRepo;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.repo.ConfiguracionRepo;
import com.maxsoft.application.repo.ConteoInventarioRepo;
import com.maxsoft.application.repo.TipoAjusteRepo;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.ConteoCiclicoService;
import com.maxsoft.application.util.ConteoCiclicoUtil;
import com.maxsoft.application.util.ConteoCiclicoUtil.Frecuencia;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConteoCiclicoServiceImpl implements ConteoCiclicoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConteoCiclicoServiceImpl.class);

    private static final ZoneId ZONA = ZoneId.of("America/Santo_Domingo");
    private static final int ID_CONFIGURACION = 1;
    private static final int POR_DIA_POR_DEFECTO = 10;
    private static final int TIPO_INCREMENTO = 1;
    private static final int TIPO_DISMINUCION = 2;
    private static final int DIAS_EXACTITUD = 30;

    private final ConteoInventarioRepo conteoRepo;
    private final ConfiguracionRepo configuracionRepo;
    private final ArticuloRepo articuloRepo;
    private final ArticuloAlmacenRepo articuloAlmacenRepo;
    private final ArticuloEmpaqueRepo empaqueRepo;
    private final ArticuloClasificacionAbcRepo abcRepo;
    private final AlmacenRepo almacenRepo;
    private final TipoAjusteRepo tipoAjusteRepo;
    private final AjusteInventarioService ajusteService;

    public ConteoCiclicoServiceImpl(ConteoInventarioRepo conteoRepo, ConfiguracionRepo configuracionRepo,
            ArticuloRepo articuloRepo, ArticuloAlmacenRepo articuloAlmacenRepo, ArticuloEmpaqueRepo empaqueRepo,
            ArticuloClasificacionAbcRepo abcRepo, AlmacenRepo almacenRepo, TipoAjusteRepo tipoAjusteRepo,
            AjusteInventarioService ajusteService) {
        this.conteoRepo = conteoRepo;
        this.configuracionRepo = configuracionRepo;
        this.articuloRepo = articuloRepo;
        this.articuloAlmacenRepo = articuloAlmacenRepo;
        this.empaqueRepo = empaqueRepo;
        this.abcRepo = abcRepo;
        this.almacenRepo = almacenRepo;
        this.tipoAjusteRepo = tipoAjusteRepo;
        this.ajusteService = ajusteService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloAContar> paraHoy(Integer almacen, boolean contarMas) {
        if (almacen == null) {
            return List.of();
        }
        LocalDate hoy = hoy();
        Configuracion config = configuracion();
        int porDia = porDia(config);
        int cuantos = contarMas ? porDia : porDia - (int) conteoRepo.contadosEnFecha(almacen, hoy);

        Map<Integer, ArticuloAContar> datos = new LinkedHashMap<>();
        List<ConteoCiclicoUtil.Candidato> candidatos = new ArrayList<>();
        for (Object[] f : conteoRepo.candidatos(almacen)) {
            ArticuloAContar a = new ArticuloAContar(((Number) f[0]).intValue(), aTexto(f[1]), aTexto(f[2]),
                    aTexto(f[3]), aTexto(f[4]), aFecha(f[5]), aDecimal(f[6]));
            datos.put(a.articulo(), a);
            candidatos.add(new ConteoCiclicoUtil.Candidato(a.articulo(), a.clase(), a.ultimoConteo()));
        }
        return ConteoCiclicoUtil.paraContar(candidatos, hoy, frecuencia(config), cuantos).stream()
                .map(c -> datos.get(c.articulo()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenConteo resumen(Integer almacen) {
        Configuracion config = configuracion();
        int porDia = porDia(config);
        if (almacen == null) {
            return new ResumenConteo(0, 0, porDia, 0, null);
        }
        LocalDate hoy = hoy();
        List<ConteoCiclicoUtil.Candidato> candidatos = conteoRepo.candidatos(almacen).stream()
                .map(f -> new ConteoCiclicoUtil.Candidato(((Number) f[0]).intValue(), aTexto(f[2]), aFecha(f[5])))
                .toList();
        long tocan = ConteoCiclicoUtil.cuantosTocan(candidatos, hoy, frecuencia(config));

        List<LineaHistorial> recientes = historial(almacen, DIAS_EXACTITUD);
        long sinDiferencia = recientes.stream().filter(l -> l.diferencia().signum() == 0).count();
        return new ResumenConteo(tocan, conteoRepo.contadosEnFecha(almacen, hoy), porDia, recientes.size(),
                ConteoCiclicoUtil.exactitud(recientes.size(), sinDiferencia));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultadoConteo guardar(Integer codigoAlmacen, List<LineaConteo> lineas, String usuario) {
        if (codigoAlmacen == null) {
            throw new IllegalArgumentException("Seleccione el almacén que contó.");
        }
        List<LineaConteo> contadas = lineas == null ? List.of()
                : lineas.stream().filter(l -> l.articulo() != null && l.contada() != null).toList();
        if (contadas.isEmpty()) {
            throw new IllegalArgumentException("No hay cantidades contadas para guardar.");
        }
        if (contadas.stream().anyMatch(l -> l.contada().signum() < 0)) {
            throw new IllegalArgumentException("Lo contado no puede ser negativo.");
        }
        Almacen almacen = almacenRepo.findById(codigoAlmacen)
                .orElseThrow(() -> new IllegalArgumentException("El almacén no existe."));
        LocalDate hoy = hoy();

        ConteoInventario conteo = new ConteoInventario();
        conteo.setFecha(hoy);
        conteo.setFechaRegistro(LocalDateTime.now(ZONA).withNano(0));
        conteo.setAlmacen(almacen.getCodigo());
        conteo.setNombreAlmacen(almacen.getNombre());
        conteo.setNombreUsuario(usuario);
        conteo.setObservacion("Conteo cíclico");

        List<DetalleAjusteInventario> sobrantes = new ArrayList<>();
        List<DetalleAjusteInventario> faltantes = new ArrayList<>();
        int conDiferencia = 0;
        for (LineaConteo l : contadas) {
            Articulo articulo = articuloRepo.findById(l.articulo())
                    .orElseThrow(() -> new IllegalArgumentException("El artículo " + l.articulo() + " no existe."));
            // Existencia al momento de guardar (no la que había al abrir la pantalla)
            BigDecimal sistema = articuloAlmacenRepo.findByArticuloCodigoAndAlmacenCodigo(l.articulo(), codigoAlmacen)
                    .map(aa -> aa.getExistencia() != null ? aa.getExistencia() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);
            BigDecimal contada = l.contada().setScale(2, RoundingMode.HALF_UP);
            BigDecimal diferencia = contada.subtract(sistema).setScale(2, RoundingMode.HALF_UP);

            DetalleConteoInventario det = new DetalleConteoInventario();
            det.setConteo(conteo);
            det.setArticulo(articulo.getCodigo());
            det.setDescripcionArticulo(articulo.getDescripcion());
            det.setClase(abcRepo.findById(articulo.getCodigo()).map(ArticuloClasificacionAbc::getClase).orElse(null));
            det.setExistenciaSistema(sistema);
            det.setCantidadContada(contada);
            det.setDiferencia(diferencia);
            conteo.getDetalles().add(det);

            if (diferencia.signum() != 0) {
                conDiferencia++;
                DetalleAjusteInventario aj = lineaAjuste(articulo, almacen, sistema, contada, diferencia.abs());
                (diferencia.signum() > 0 ? sobrantes : faltantes).add(aj);
            }
        }
        conteo.setArticulos(contadas.size());
        conteo.setConDiferencia(conDiferencia);
        conteo = conteoRepo.saveAndFlush(conteo);

        String observacion = "Conteo cíclico #" + conteo.getCodigo();
        if (!sobrantes.isEmpty()) {
            conteo.setAjusteIncremento(ajustar(almacen, TIPO_INCREMENTO, sobrantes, observacion + " (sobrante)",
                    usuario));
        }
        if (!faltantes.isEmpty()) {
            conteo.setAjusteDisminucion(ajustar(almacen, TIPO_DISMINUCION, faltantes, observacion + " (faltante)",
                    usuario));
        }
        conteo = conteoRepo.save(conteo);

        log.info("Conteo cíclico #{} en {}: {} artículos, {} con diferencia (ajustes {} / {})",
                conteo.getCodigo(), almacen.getNombre(), contadas.size(), conDiferencia,
                conteo.getAjusteIncremento(), conteo.getAjusteDisminucion());
        return new ResultadoConteo(conteo.getCodigo(), contadas.size(), conDiferencia,
                conteo.getAjusteIncremento(), conteo.getAjusteDisminucion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LineaHistorial> historial(Integer almacen, int dias) {
        if (almacen == null) {
            return List.of();
        }
        LocalDate desde = hoy().minusDays(Math.max(dias, 1) - 1L);
        return conteoRepo.historial(almacen, desde).stream()
                .map(f -> new LineaHistorial(aFecha(f[0]), aEntero(f[1]), aTexto(f[2]), aTexto(f[3]),
                aDecimal(f[4]), aDecimal(f[5]), aDecimal(f[6]), aEntero(f[7]), aEntero(f[8])))
                .toList();
    }

    /** Ajuste de inventario por las diferencias de un sentido; devuelve su código. */
    private Integer ajustar(Almacen almacen, int tipo, List<DetalleAjusteInventario> detalles, String observacion,
            String usuario) {
        AjusteInventario ajuste = new AjusteInventario();
        ajuste.setFecha(Date.from(hoy().atStartOfDay(ZONA).toInstant()));
        ajuste.setAlmacen(almacen);
        ajuste.setNombreAlmacen(almacen.getNombre());
        ajuste.setTipoAjuste(tipoAjusteRepo.findById(tipo)
                .orElseThrow(() -> new IllegalStateException("No existe el tipo de ajuste " + tipo + ".")));
        ajuste.setObservacion(observacion);
        // Igual que el registro de ajustes: todavía no hay usuario de sesión
        ajuste.setUsuario(new Usuario(1));
        ajuste.setNombreUsuario(usuario);
        ajuste.setAnulado(false);
        return ajusteService.procesarAjusteTransaccional(ajuste, detalles, usuario).getCodigo();
    }

    /** Línea de ajuste en la presentación base (factor 1), con el costo como en el registro de ajustes. */
    private DetalleAjusteInventario lineaAjuste(Articulo articulo, Almacen almacen, BigDecimal sistema,
            BigDecimal contada, BigDecimal cantidad) {
        ArticuloEmpaque base = empaqueRepo.findByArticuloCodigoAndEsEmpaqueBaseTrue(articulo.getCodigo())
                .orElseThrow(() -> new IllegalStateException(articulo.getDescripcion()
                + " no tiene presentación base: no se puede ajustar."));

        DetalleAjusteInventario d = new DetalleAjusteInventario();
        d.setArticulo(articulo);
        d.setDescripcionArticulo(articulo.getDescripcion());
        d.setExistencia(sistema);
        d.setNuevaExistencia(contada);
        d.setCantidad(cantidad);
        d.setAlmacen(almacen);
        d.setNombreAlmacen(almacen.getNombre());
        d.setArticuloEmpaque(base);
        d.setFactorConversion(base.getFactorConversion() != null && base.getFactorConversion().signum() > 0
                ? base.getFactorConversion() : BigDecimal.ONE);
        d.setUnidad(articulo.getUnidadBase());
        d.setNombreUnidad(base.getUnidadEmpaque() != null ? base.getUnidadEmpaque().getDescripcion()
                : articulo.getUnidadBase() != null ? articulo.getUnidadBase().getDescripcion() : "Unidad");

        BigDecimal precio = base.getPrecioCompra() != null ? base.getPrecioCompra()
                : articulo.getPrecioCompra() != null ? articulo.getPrecioCompra() : BigDecimal.ZERO;
        BigDecimal subTotal = cantidad.multiply(precio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tasa = articulo.getItbisGravado() != null ? articulo.getItbisGravado() : BigDecimal.ZERO;
        BigDecimal itbis = subTotal.multiply(tasa.movePointLeft(2)).setScale(2, RoundingMode.HALF_UP);
        d.setPrecioCompra(precio);
        d.setCostoUnitario(precio);
        d.setSubTotal(subTotal);
        d.setItbis(itbis);
        d.setTotal(subTotal.add(itbis));
        return d;
    }

    private Configuracion configuracion() {
        return configuracionRepo.findById(ID_CONFIGURACION).orElse(null);
    }

    private static int porDia(Configuracion c) {
        return positivo(c != null ? c.getAbcConteoPorDia() : null, POR_DIA_POR_DEFECTO);
    }

    private static Frecuencia frecuencia(Configuracion c) {
        Frecuencia d = Frecuencia.POR_DEFECTO;
        if (c == null) {
            return d;
        }
        return new Frecuencia(positivo(c.getAbcConteoDiasA(), d.diasA()), positivo(c.getAbcConteoDiasB(), d.diasB()),
                positivo(c.getAbcConteoDiasC(), d.diasC()));
    }

    private static int positivo(Integer valor, int porDefecto) {
        return valor != null && valor > 0 ? valor : porDefecto;
    }

    private static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    /** Texto de una consulta nativa: las columnas CHAR(1) (la clase) llegan como Character. */
    private static String aTexto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static Integer aEntero(Object valor) {
        return valor == null ? null : ((Number) valor).intValue();
    }

    private static BigDecimal aDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor instanceof BigDecimal bd ? bd : new BigDecimal(valor.toString());
    }

    /** DATE de una consulta nativa (java.sql.Date: su toInstant() lanza). */
    private static LocalDate aFecha(Object valor) {
        if (valor instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        if (valor instanceof LocalDate ld) {
            return ld;
        }
        if (valor instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (valor instanceof LocalDateTime ldt) {
            return ldt.toLocalDate();
        }
        return null;
    }
}
