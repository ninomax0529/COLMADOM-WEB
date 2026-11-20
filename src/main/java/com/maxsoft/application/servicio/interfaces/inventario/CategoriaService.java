package com.maxsoft.application.servicio.interfaces.inventario;

import com.maxsoft.application.modelo.Categoria;
import java.util.List;

/** Categorías de artículos (filtran el catálogo del POS). */
public interface CategoriaService {

    /** Categorías habilitadas, en orden de creación. */
    List<Categoria> listarHabilitadas();

    /**
     * Devuelve la categoría con ese nombre (sin distinguir mayúsculas); si no
     * existe, la crea habilitada.
     */
    Categoria obtenerOCrear(String nombre, String usuario);
}
