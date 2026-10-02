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

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArticuloEmpaqueServiceImpl implements ArticuloEmpaqueService {

    private final ArticuloEmpaqueRepo repository;

    @Autowired
    public ArticuloEmpaqueServiceImpl(ArticuloEmpaqueRepo repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloEmpaque> getPorArticulo(Integer codArticulo) {
        return repository.findByArticuloCodigo(codArticulo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArticuloEmpaque> getPorCodigoBarra(String codigoBarra) {
        return repository.findByCodigoBarra(codigoBarra);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArticuloEmpaque> getEmpaqueBase(Integer codArticulo) {
        return repository.findByArticuloCodigoAndEsEmpaqueBaseTrue(codArticulo);
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
        return repository.save(empaque);
    }

    @Override
    public void guardarLista(List<ArticuloEmpaque> empaques, String usuario) {
        for (ArticuloEmpaque emp : empaques) {
            guardar(emp);
        }
    }

    @Override
    public void eliminar(Integer codigo) {
        repository.deleteById(codigo);
    }

 
}