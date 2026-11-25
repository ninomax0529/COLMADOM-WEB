package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.ArticuloClasificacionAbc;
import com.maxsoft.application.modelo.Configuracion;
import com.maxsoft.application.repo.ArticuloClasificacionAbcRepo;
import com.maxsoft.application.repo.ConfiguracionRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ClasificacionAbcService;
import com.maxsoft.application.util.ClasificacionAbcUtil;
import com.maxsoft.application.util.ClasificacionAbcUtil.ResultadoAbc;
import com.maxsoft.application.util.ClasificacionAbcUtil.VentaArticulo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ClasificacionAbcServiceImpl implements ClasificacionAbcService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClasificacionAbcServiceImpl.class);

    private static final ZoneId ZONA = ZoneId.of("America/Santo_Domingo");
    private static final int ID_CONFIGURACION = 1;
    private static final int DIAS_PERIODO_POR_DEFECTO = 90;
    private static final int DIAS_NUEVO_POR_DEFECTO = 30;
    private static final int HORAS_VIGENCIA = 24;

    private final ArticuloClasificacionAbcRepo abcRepo;
    private final ConfiguracionRepo configuracionRepo;
    // Transacción explícita: recalcular() también se llama desde la tarea
    // nocturna y el arranque (misma clase), donde @Transactional no aplicaría
    private final TransactionTemplate transaccion;

    public ClasificacionAbcServiceImpl(ArticuloClasificacionAbcRepo abcRepo,
            ConfiguracionRepo configuracionRepo, PlatformTransactionManager transactionManager) {
        this.abcRepo = abcRepo;
        this.configuracionRepo = configuracionRepo;
        this.transaccion = new TransactionTemplate(transactionManager);
    }

    /** Todos los días a las 2:00 a.m. (hora de RD). */
    @Scheduled(cron = "0 0 2 * * *", zone = "America/Santo_Domingo")
    public void tareaNocturna() {
        try {
            recalcular();
        } catch (RuntimeException e) {
            log.error("Clasificación ABC nocturna: no se pudo calcular", e);
        }
    }

    /** Al arrancar: si la PC estuvo apagada a las 2:00 a.m., se calcula ahora. */
    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        try {
            recalcularSiEstaVencida();
        } catch (RuntimeException e) {
            // Nunca debe impedir que la app arranque
            log.error("Clasificación ABC al arrancar: no se pudo calcular", e);
        }
    }

    @Override
    public synchronized ResumenAbc recalcular() {
        return transaccion.execute(estado -> calcularYGuardar());
    }

    @Override
    public boolean recalcularSiEstaVencida() {
        Optional<LocalDateTime> ultimo = ultimoCalculo();
        LocalDateTime limite = LocalDateTime.now(ZONA).minusHours(HORAS_VIGENCIA);
        // Sin filas guardadas también se recalcula (un cálculo vacío no vale),
        // y si quedan filas de antes de guardar la demanda o los sugeridos
        if (ultimo.isPresent() && ultimo.get().isAfter(limite) && abcRepo.count() > 0
                && !abcRepo.existsByDiasConDatos(0)
                && !abcRepo.existsByDemandaDiariaGreaterThanAndMinimoSugeridoIsNull(BigDecimal.ZERO)) {
            log.debug("Clasificación ABC vigente (último cálculo {})", ultimo.get());
            return false;
        }
        recalcular();
        return true;
    }

    @Override
    public List<ArticuloClasificacionAbc> obtenerTodas() {
        return abcRepo.findAll();
    }

    @Override
    public Optional<ArticuloClasificacionAbc> obtenerPorArticulo(Integer codigoArticulo) {
        return codigoArticulo == null ? Optional.empty() : abcRepo.findById(codigoArticulo);
    }

    @Override
    public int aplicarSugeridos(java.util.Collection<Integer> codigosArticulo) {
        if (codigosArticulo == null || codigosArticulo.isEmpty()) {
            return 0;
        }
        Integer actualizados = transaccion.execute(estado -> abcRepo.aplicarSugeridos(codigosArticulo));
        log.info("Mínimo y máximo sugeridos aplicados a {} artículos", actualizados);
        return actualizados != null ? actualizados : 0;
    }

    @Override
    public List<ArticuloPorReponer> porReponer(boolean soloClaseA) {
        List<ArticuloPorReponer> lista = new ArrayList<>();
        for (Object[] f : abcRepo.articulosQueVenden()) {
            // clase es CHAR(1): la consulta nativa la devuelve como Character
            String clase = f[2] != null ? f[2].toString() : null;
            if (soloClaseA && !ClasificacionAbcUtil.CLASE_A.equals(clase)) {
                continue;
            }
            BigDecimal existencia = aDecimal(f[3]);
            BigDecimal demanda = aDecimal(f[4]);
            BigDecimal minimo = f[5] != null ? aDecimal(f[5]) : null;
            BigDecimal maximo = f[6] != null ? aDecimal(f[6]) : null;
            if (!ClasificacionAbcUtil.porReponer(existencia, demanda, minimo)) {
                continue;
            }
            lista.add(new ArticuloPorReponer(((Number) f[0]).intValue(), f[1] != null ? f[1].toString() : null, clase, existencia,
                    ClasificacionAbcUtil.diasCobertura(existencia, demanda), minimo, maximo,
                    ClasificacionAbcUtil.cantidadAPedir(existencia, maximo)));
        }
        lista.sort(java.util.Comparator.comparing(ArticuloPorReponer::diasCobertura,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        return lista;
    }

    @Override
    public Optional<LocalDateTime> ultimoCalculo() {
        return configuracionRepo.findById(ID_CONFIGURACION).map(Configuracion::getAbcUltimoCalculo);
    }

    private ResumenAbc calcularYGuardar() {

        Configuracion config = configuracionRepo.findById(ID_CONFIGURACION).orElse(null);
        int diasPeriodo = positivo(config != null ? config.getAbcDiasPeriodo() : null, DIAS_PERIODO_POR_DEFECTO);
        int diasNuevo = positivo(config != null ? config.getAbcDiasNuevo() : null, DIAS_NUEVO_POR_DEFECTO);
        BigDecimal limiteA = config != null ? config.getAbcLimiteA() : null;
        BigDecimal limiteB = config != null ? config.getAbcLimiteB() : null;
        ClasificacionAbcUtil.PoliticaStock politica = politica(config);

        LocalDateTime ahora = LocalDateTime.now(ZONA).withNano(0);
        // Solo días completos (hasta ayer): a las 2:00 a.m. el día de hoy casi
        // no tiene ventas y contarlo baja la demanda diaria
        LocalDate hasta = ahora.toLocalDate().minusDays(1);
        LocalDate desde = hasta.minusDays(diasPeriodo - 1L);
        LocalDate nuevoDesde = hasta.minusDays(diasNuevo);

        // Antes de la primera factura el sistema no vendía: esos días no
        // cuentan para la demanda diaria
        LocalDate primeraVenta = aFecha(abcRepo.primeraFechaDeVenta());
        LocalDate inicioDatos = primeraVenta != null && primeraVenta.isAfter(desde) ? primeraVenta : desde;

        List<VentaArticulo> ventas = new ArrayList<>();
        Map<Integer, Integer> diasConDatos = new HashMap<>();
        for (Object[] fila : abcRepo.ventasPorArticulo(desde, hasta)) {
            LocalDate creado = aFecha(fila[1]);
            boolean nuevo = creado != null && creado.isAfter(nuevoDesde);
            // Un artículo creado dentro del período solo cuenta sus días
            LocalDate inicio = creado != null && creado.isAfter(inicioDatos) ? creado : inicioDatos;
            diasConDatos.put(((Number) fila[0]).intValue(),
                    (int) Math.max(1, ChronoUnit.DAYS.between(inicio, hasta) + 1));
            ventas.add(new VentaArticulo(
                    ((Number) fila[0]).intValue(),
                    aDecimal(fila[2]),
                    ((Number) fila[3]).longValue(),
                    aDecimal(fila[4]),
                    nuevo));
        }

        List<ResultadoAbc> resultados = ClasificacionAbcUtil.clasificar(ventas, limiteA, limiteB);

        // Se reemplaza el cálculo anterior completo (artículos deshabilitados
        // o que ya no son de venta desaparecen)
        abcRepo.deleteAllInBatch();
        List<ArticuloClasificacionAbc> filas = new ArrayList<>(resultados.size());
        int a = 0, b = 0, c = 0, n = 0, parados = 0;
        for (ResultadoAbc r : resultados) {
            ArticuloClasificacionAbc fila = aEntidad(r, desde, hasta, ahora);
            int dias = diasConDatos.getOrDefault(r.articulo(), 1);
            fila.setDiasConDatos(dias);
            fila.setDemandaDiaria(ClasificacionAbcUtil.demandaDiaria(r.unidades(), dias));
            fila.setMinimoSugerido(ClasificacionAbcUtil.minimoSugerido(fila.getDemandaDiaria(), r.clase(), politica));
            fila.setMaximoSugerido(ClasificacionAbcUtil.maximoSugerido(fila.getDemandaDiaria(), r.clase(), politica));
            filas.add(fila);
            switch (r.clase()) {
                case ClasificacionAbcUtil.CLASE_A -> a++;
                case ClasificacionAbcUtil.CLASE_B -> b++;
                case ClasificacionAbcUtil.CLASE_NUEVO -> n++;
                default -> c++;
            }
            if (r.sinMovimiento()) {
                parados++;
            }
        }
        abcRepo.saveAll(filas);

        if (config != null) {
            config.setAbcUltimoCalculo(ahora);
            configuracionRepo.save(config);
        }

        ResumenAbc resumen = new ResumenAbc(filas.size(), a, b, c, n, parados, desde, hasta, ahora);
        log.info("Clasificación ABC {} a {}: {} artículos -> A {}, B {}, C {}, nuevos {}, sin movimiento {}",
                desde, hasta, resumen.articulos(), a, b, c, n, parados);
        return resumen;
    }

    private static ArticuloClasificacionAbc aEntidad(ResultadoAbc r, LocalDate desde,
            LocalDate hasta, LocalDateTime ahora) {
        ArticuloClasificacionAbc e = new ArticuloClasificacionAbc();
        e.setArticulo(r.articulo());
        e.setClase(r.clase());
        e.setClaseValor(r.claseValor());
        e.setClaseRotacion(r.claseRotacion());
        e.setSinMovimiento(r.sinMovimiento());
        e.setVentas(r.ventas().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setFacturas((int) r.facturas());
        e.setUnidades(r.unidades().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setPorcientoValor(r.porcientoValor());
        e.setPorcientoRotacion(r.porcientoRotacion());
        e.setFechaDesde(desde);
        e.setFechaHasta(hasta);
        e.setFechaCalculo(ahora);
        return e;
    }

    /** Días de la configuración; los vacíos o en cero usan los de siempre. */
    private static ClasificacionAbcUtil.PoliticaStock politica(Configuracion c) {
        ClasificacionAbcUtil.PoliticaStock d = ClasificacionAbcUtil.PoliticaStock.POR_DEFECTO;
        if (c == null) {
            return d;
        }
        return new ClasificacionAbcUtil.PoliticaStock(
                positivo(c.getAbcDiasReposicion(), d.diasReposicion()),
                noNegativo(c.getAbcSeguridadA(), d.seguridadA()),
                noNegativo(c.getAbcSeguridadB(), d.seguridadB()),
                noNegativo(c.getAbcSeguridadC(), d.seguridadC()),
                positivo(c.getAbcCompraA(), d.compraA()),
                positivo(c.getAbcCompraB(), d.compraB()),
                positivo(c.getAbcCompraC(), d.compraC()));
    }

    private static int noNegativo(Integer valor, int porDefecto) {
        return valor != null && valor >= 0 ? valor : porDefecto;
    }

    private static int positivo(Integer valor, int porDefecto) {
        return valor != null && valor > 0 ? valor : porDefecto;
    }

    private static BigDecimal aDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor instanceof BigDecimal bd ? bd : new BigDecimal(valor.toString());
    }

    /** DATETIME de una consulta nativa: según el driver llega como Timestamp o LocalDateTime. */
    private static LocalDate aFecha(Object valor) {
        if (valor instanceof LocalDateTime ldt) {
            return ldt.toLocalDate();
        }
        if (valor instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        // DATE llega como java.sql.Date, cuyo toInstant() siempre lanza
        // UnsupportedOperationException: va antes que java.util.Date
        if (valor instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        if (valor instanceof java.util.Date d) {
            return d.toInstant().atZone(ZONA).toLocalDate();
        }
        if (valor instanceof LocalDate ld) {
            return ld;
        }
        return null;
    }
}
