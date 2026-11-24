/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.dto.ArticuloRegistroDTO;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.Unidad;
import com.maxsoft.application.repo.ArticuloEmpaqueRepo;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.servicio.interfaces.inventario.AlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticuloServiceImpl implements ArticuloService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArticuloServiceImpl.class);


    @Autowired
    ArticuloRepo repo;
    @Autowired
    ArticuloEmpaqueRepo articuloEmpaqueRepo;

    @Autowired
    com.maxsoft.application.repo.ArticuloAlmacenRepo articuloAlmacenRepo;
    @Autowired
    AlmacenService almacenService;
    @Autowired
    com.maxsoft.application.servicio.interfaces.inventario.LoteService loteService;

    @Override
    public Articulo guardar(Articulo art) {

        if (art != null) {
            art.setDescripcion(normalizarNombre(art.getDescripcion()));
        }
        return repo.save(art);
    }

    /**
     * Nombre de producto uniforme: en MAYÚSCULAS, sin espacios al inicio ni al
     * final y sin espacios repetidos ("leche rica  lista " → "LECHE RICA
     * LISTA"). Así se ve igual en el POS y la búsqueda no depende de cómo se
     * escribió.
     */
    public static String normalizarNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        return nombre.trim().replaceAll("\\s+", " ").toUpperCase(new java.util.Locale("es", "DO"));
    }

    @Override
    public List<Articulo> getLista() {

        List<Articulo> lista = null;
        lista = repo.findAll();
//        
        return lista;

    }

    @Override
    public void eliminarArticulo(int codigo) {

        repo.deleteById(codigo);
    }

    @Override
    public Articulo buscarPorCodigo(int codigo) {
        return repo.findById(codigo).get();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Articulo guardarArticuloCompleto(ArticuloRegistroDTO dto) {

        // 1. Validaciones
        if (dto == null || dto.getArticulo() == null) {
            throw new IllegalArgumentException("El objeto Artículo no puede ser nulo.");
        }
        if (dto.getEmpaques() == null || dto.getEmpaques().isEmpty()) {
            throw new IllegalArgumentException("Debe incluir al menos un empaque/presentación para el artículo.");
        }

        // 2. Extraer el Empaque Base
        ArticuloEmpaque empaqueBase = dto.getEmpaques().stream()
                .filter(emp -> Boolean.TRUE.equals(emp.getEsEmpaqueBase())
                || (emp.getFactorConversion() != null && emp.getFactorConversion().compareTo(BigDecimal.ONE) == 0))
                .findFirst()
                .orElse(dto.getEmpaques().get(0));

        Unidad unidadBase = empaqueBase.getUnidadEmpaque();
        if (unidadBase == null) {
            throw new IllegalArgumentException("El empaque base debe tener asignada una unidad de medida válida.");
        }

        Articulo articulo = dto.getArticulo();
        articulo.setDescripcion(normalizarNombre(articulo.getDescripcion()));
        // Las presentaciones guardan una copia del nombre (es la que muestra el
        // POS): se mantiene igual al nombre del artículo
        for (ArticuloEmpaque emp : dto.getEmpaques()) {
            emp.setNombreArticulo(articulo.getDescripcion());
        }
        articulo.setUnidadBase(unidadBase);
        articulo.setNombreUnidadBase(unidadBase.getDescripcion());

        // 3. Preparar y asignar la colección de Almacenes (ArticuloAlmacen)
        List<ArticuloAlmacen> listaAlmacenes = dto.getAlmacenes() != null ? dto.getAlmacenes() : new ArrayList<>();

        if (listaAlmacenes.size() <= 0) {

            log.debug("listaAlmacenes.size() " + listaAlmacenes.size());
            log.debug("this.almacenService." + this.almacenService);
            ArticuloAlmacen itemAlmacen = new ArticuloAlmacen();

            Almacen alm = this.almacenService.getAlmacen(1);//almacen para la  compra,almacen general
            itemAlmacen.setAlmacen(alm);
            itemAlmacen.setNombreAlmacen(alm.getNombre());

            itemAlmacen.setArticulo(articulo); // Sincronizar relación bidireccional
            itemAlmacen.setUnidad(unidadBase); // Extrapolar la unidad base
            itemAlmacen.setNombreUnidad(unidadBase.getDescripcion());
            itemAlmacen.setDescripcionArticulo(articulo.getDescripcion());
            itemAlmacen.setUbicacionPasillo("A012");
            itemAlmacen.setFechaCreacion(new Date());
            itemAlmacen.setFechaActualizacion(new Date());
            itemAlmacen.setCreadoPor("admin");

            itemAlmacen.setExistencia(BigDecimal.ZERO);
            itemAlmacen.setMinimo(BigDecimal.ZERO);
            itemAlmacen.setMaximo(new BigDecimal("999999"));

            listaAlmacenes.add(itemAlmacen);

            itemAlmacen = new ArticuloAlmacen();

            alm = this.almacenService.getAlmacen(2);//almacen de venta
            itemAlmacen.setAlmacen(alm);
            itemAlmacen.setNombreAlmacen(alm.getNombre());

            itemAlmacen.setArticulo(articulo); // Sincronizar relación bidireccional
            itemAlmacen.setUnidad(unidadBase); // Extrapolar la unidad base
            itemAlmacen.setNombreUnidad(unidadBase.getDescripcion());
            itemAlmacen.setDescripcionArticulo(articulo.getDescripcion());
            itemAlmacen.setUbicacionPasillo("A012");
            itemAlmacen.setFechaCreacion(new Date());
            itemAlmacen.setFechaActualizacion(new Date());
            itemAlmacen.setCreadoPor("admin");

            itemAlmacen.setExistencia(BigDecimal.ZERO);
            itemAlmacen.setMinimo(BigDecimal.ZERO);
            itemAlmacen.setMaximo(new BigDecimal("999999"));

            listaAlmacenes.add(itemAlmacen);

        } else {

            for (ArticuloAlmacen itemAlmacen : listaAlmacenes) {

                itemAlmacen.setArticulo(articulo); // Sincronizar relación bidireccional
                itemAlmacen.setUnidad(unidadBase); // Extrapolar la unidad base
                itemAlmacen.setNombreUnidad(unidadBase.getDescripcion());
                itemAlmacen.setDescripcionArticulo(articulo.getDescripcion());
                itemAlmacen.setUbicacionPasillo("A012");
                itemAlmacen.setFechaCreacion(new Date());
                itemAlmacen.setFechaActualizacion(new Date());
                itemAlmacen.setCreadoPor("admin");

            }
        }

        // La existencia solo la cambian los movimientos de inventario. Al editar la
        // ficha se conserva la de la BD (la pantalla puede traer un valor viejo si
        // hubo ventas o compras mientras estaba abierta), y la existencia general
        // es siempre la suma de los almacenes.
        BigDecimal existenciaTotal = BigDecimal.ZERO;
        for (ArticuloAlmacen itemAlmacen : listaAlmacenes) {
            if (itemAlmacen.getCodigo() != null) {
                articuloAlmacenRepo.findById(itemAlmacen.getCodigo())
                        .ifPresent(bd -> itemAlmacen.setExistencia(bd.getExistencia()));
            }
            if (itemAlmacen.getExistencia() != null) {
                existenciaTotal = existenciaTotal.add(itemAlmacen.getExistencia());
            }
        }
        articulo.setExistencia(existenciaTotal);

        // 🔥 Asignación directa mediante la colección del Artículo
        articulo.setArticuloAlmacenCollection(listaAlmacenes);

        // 4. Preparar y asignar la colección de Empaques (ArticuloEmpaque)
        List<ArticuloEmpaque> listaEmpaques = dto.getEmpaques();
        for (ArticuloEmpaque emp : listaEmpaques) {

//            emp.setCodigo(null);
            emp.setArticulo(articulo); // Sincronizar relación bidireccional
            if (emp.getCodigo() == null) {
                emp.setCreadoPor("admin");
            }
        }

        // 🔥 Asignación directa mediante la colección del Artículo
        articulo.setArticuloEmpaqueCollection(listaEmpaques);

        // 5. UN SOLO SAVE: JPA/Hibernate persistirá el artículo y en cascada guardará ambas colecciones
        Articulo guardado = repo.save(articulo);

        // 6. Control de lotes: lo que haya en existencia y no esté en ningún lote
        //    (al activar el control, o por un descuadre) queda en el lote INICIAL.
        if (guardado.isControlaLote()) {
            loteService.sincronizarLotes(guardado, guardado.getCreadoPor());
        }
        return guardado;
    }
}
