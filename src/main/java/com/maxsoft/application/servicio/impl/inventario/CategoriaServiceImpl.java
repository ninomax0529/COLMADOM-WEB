package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.Categoria;
import com.maxsoft.application.repo.CategoriaRepo;
import com.maxsoft.application.servicio.interfaces.inventario.CategoriaService;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepo categoriaRepo;

    public CategoriaServiceImpl(CategoriaRepo categoriaRepo) {
        this.categoriaRepo = categoriaRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarHabilitadas() {
        return categoriaRepo.findAll().stream()
                .filter(Categoria::getHabilitada)
                .sorted(Comparator.comparing(Categoria::getCodigo))
                .toList();
    }

    @Override
    @Transactional
    public Categoria obtenerOCrear(String nombre, String usuario) {
        String limpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría está vacío.");
        }
        if (limpio.length() > 45) {
            throw new IllegalArgumentException("El nombre de la categoría admite hasta 45 caracteres.");
        }

        return categoriaRepo.findAll().stream()
                .filter(c -> c.getNombre() != null && c.getNombre().equalsIgnoreCase(limpio))
                .findFirst()
                .orElseGet(() -> {
                    Categoria nueva = new Categoria();
                    nueva.setNombre(limpio);
                    nueva.setFechaCreacion(new Date());
                    nueva.setCreadaPor(usuario);
                    nueva.setHabilitada(true);
                    return categoriaRepo.save(nueva);
                });
    }
}
