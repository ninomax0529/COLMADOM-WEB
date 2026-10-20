/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.TipoMerma;
import com.maxsoft.application.repo.TipoMermaRepo;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMermaService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TipoMermaServiceImpl implements TipoMermaService {

    private final TipoMermaRepo tipoMermaRepo;

    public TipoMermaServiceImpl(TipoMermaRepo tipoMermaRepo) {
        this.tipoMermaRepo = tipoMermaRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMerma> getLista() {
        return tipoMermaRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMerma> getListaActivos() {
        return tipoMermaRepo.findByActivoTrueOrderByDescripcionAsc();
    }

    @Override
    @Transactional
    public TipoMerma guardar(TipoMerma tipoMerma) {
        if (tipoMerma == null || tipoMerma.getDescripcion() == null || tipoMerma.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción del tipo de merma es obligatoria.");
        }
        tipoMerma.setDescripcion(tipoMerma.getDescripcion().trim().toUpperCase());
        return tipoMermaRepo.save(tipoMerma);
    }
}
