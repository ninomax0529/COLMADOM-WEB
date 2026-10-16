/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.repo.ArticuloEmpaqueRepo;

import com.maxsoft.application.servicio.interfaces.inventario.ArticuloEmpaqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArticuloEmpaqueServiceImpl implements ArticuloEmpaqueService {

    private final ArticuloEmpaqueRepo repo;

    @Autowired
    public ArticuloEmpaqueServiceImpl(ArticuloEmpaqueRepo repository) {
        this.repo = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloEmpaque> getPorArticulo(Integer codArticulo) {
        return repo.findByArticuloCodigo(codArticulo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArticuloEmpaque> getPorCodigoBarra(String codigoBarra) {
        return repo.findByCodigoBarra(codigoBarra);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArticuloEmpaque> getEmpaqueBase(Integer codArticulo) {
        return repo.findByArticuloCodigoAndEsEmpaqueBaseTrue(codArticulo);
    }

    @Override
    public ArticuloEmpaque guardar(ArticuloEmpaque empaque) {

        if (empaque.getArticulo() == null || empaque.getUnidadEmpaque() == null) {
            throw new IllegalArgumentException("El artículo y la unidad de empaque son obligatorios.");
        }
        if (empaque.getCodigo() == null) {
            empaque.setFechaCreacion(new Date());
            empaque.setCreadoPor("admin");
        }
        return repo.save(empaque);
    }

    @Override
    public void guardarLista(List<ArticuloEmpaque> empaques, String usuario) {
        for (ArticuloEmpaque emp : empaques) {
            guardar(emp);
        }
    }

    @Override
    public void eliminar(Integer codigo) {
        repo.deleteById(codigo);
    }

    @Override
    public List<ArticuloEmpaque> getArticuloEmpaqueParaVenta() {
        return repo.getArticuloEmpaqueParaVenta();
    }

    @Override
    public List<ArticuloEmpaque> buscarPorDescripcionEfectivo(String filtro) {

        // Si el filtro viene vacío (el usuario no ha escrito nada), limpiamos espacios
        String textoBuscar = (filtro == null) ? "" : filtro.trim();

        // Ejecuta la búsqueda indexada en la base de datos SQL
        return repo.buscarPorDescripcionEfectivo(textoBuscar);
    }

// ... dentro de tu clase ArticuloEmpaqueService
    @Transactional
    @Override
    public void actualizarCodigoBarra(Integer empaqueId, String nuevoCodigo) {
        if (empaqueId == null || nuevoCodigo == null || nuevoCodigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del empaque o el nuevo código no pueden estar vacíos");
        }

        // Ejecuta el UPDATE en SQL
        int filasAfectadas = repo.modificarCodigoBarraDirecto(empaqueId, nuevoCodigo.trim());

        if (filasAfectadas == 0) {
            throw new RuntimeException("No se encontró el empaque con el ID especificado para actualizar el código");
        }
    }

    @Override
    public Optional<ArticuloEmpaque> buscarPorCodigoBarra(String codigoBarra) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<ArticuloEmpaque> buscarPorDescripcionEfectivoPaginado(String filtro, int offset, int limit) {
        // Vaadin maneja filas (offset), Spring maneja número de páginas. 
        // Dividimos el offset entre el límite por página para saber el número de página actual.
        int numeroPagina = offset / limit;

        Pageable pageable = PageRequest.of(numeroPagina, limit);

        // Llamamos al repositorio pasándole el filtro y la paginación
        return repo.buscarPorFiltro(filtro, pageable).getContent();

    }
}