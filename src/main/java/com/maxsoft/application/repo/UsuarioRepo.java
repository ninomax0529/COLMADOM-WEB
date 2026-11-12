package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepo extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findFirstByUsuario(String usuario);
}
