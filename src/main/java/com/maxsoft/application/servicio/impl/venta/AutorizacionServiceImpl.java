package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.UsuarioRepo;
import com.maxsoft.application.servicio.interfaces.venta.AutorizacionService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutorizacionServiceImpl implements AutorizacionService {

    private final UsuarioRepo usuarioRepo;

    public AutorizacionServiceImpl(UsuarioRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public String autorizarSupervisor(String usuario, String clave) {

        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("Indique el usuario y la clave del supervisor.");
        }
        Usuario u = usuarioRepo.findFirstByUsuario(usuario.trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o clave incorrectos."));

        if (!claveCorrecta(clave, u.getContrasena())) {
            throw new IllegalArgumentException("Usuario o clave incorrectos.");
        }
        if (!u.getHabilitado()) {
            throw new IllegalArgumentException("El usuario " + u.getUsuario() + " está deshabilitado.");
        }
        if (!TIPO_SUPERVISOR.equals(u.getTipoUsuario() != null ? u.getTipoUsuario().trim() : null)) {
            throw new IllegalArgumentException("El usuario " + u.getUsuario() + " no puede autorizar (no es supervisor).");
        }
        return u.getNombre() != null && !u.getNombre().isBlank() ? u.getNombre() : u.getUsuario();
    }

    /** Acepta clave cifrada con BCrypt o, mientras no se migren, en texto plano. */
    private static boolean claveCorrecta(String clave, String guardada) {
        if (guardada == null) {
            return false;
        }
        if (guardada.startsWith("$2a$") || guardada.startsWith("$2b$") || guardada.startsWith("$2y$")) {
            return new BCryptPasswordEncoder().matches(clave, guardada);
        }
        return guardada.equals(clave);
    }
}
