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

    @Autowired
    ArticuloRepo repo;
    @Autowired
    ArticuloEmpaqueRepo articuloEmpaqueRepo;
    @Autowired
    AlmacenService almacenService;

    @Override
    public Articulo guardar(Articulo art) {

        return repo.save(art);
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
        articulo.setUnidadBase(unidadBase);
        articulo.setNombreUnidadBase(unidadBase.getDescripcion());

        // 3. Preparar y asignar la colección de Almacenes (ArticuloAlmacen)
        List<ArticuloAlmacen> listaAlmacenes = dto.getAlmacenes() != null ? dto.getAlmacenes() : new ArrayList<>();

        if (listaAlmacenes.size() <= 0) {

            System.out.println("listaAlmacenes.size() " + listaAlmacenes.size());
            System.out.println("this.almacenService." + this.almacenService);
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

        // 🔥 Asignación directa mediante la colección del Artículo
        articulo.setArticuloAlmacenCollection(listaAlmacenes);

        // 4. Preparar y asignar la colección de Empaques (ArticuloEmpaque)
        List<ArticuloEmpaque> listaEmpaques = dto.getEmpaques();
        for (ArticuloEmpaque emp : listaEmpaques) {

            emp.setCodigo(null);
            emp.setArticulo(articulo); // Sincronizar relación bidireccional
            if (emp.getCodigo() == null) {
                emp.setCreadoPor("admin");
            }
        }

        // 🔥 Asignación directa mediante la colección del Artículo
        articulo.setArticuloEmpaqueCollection(listaEmpaques);

        // 5. UN SOLO SAVE: JPA/Hibernate persistirá el artículo y en cascada guardará ambas colecciones
        return repo.save(articulo);
    }
}
