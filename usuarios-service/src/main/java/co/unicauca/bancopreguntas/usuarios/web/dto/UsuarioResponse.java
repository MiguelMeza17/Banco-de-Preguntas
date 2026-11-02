package co.unicauca.bancopreguntas.usuarios.web.dto;

import co.unicauca.bancopreguntas.usuarios.domain.Usuario;

/** Vista publica del usuario: nunca incluye el hash de la contrasena. */
public record UsuarioResponse(Long id, String login, String nombreCompleto, String email, String rol, String estado) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getLogin(), u.getNombreCompleto(), u.getEmail(),
                u.getRol().name(), u.getEstado().name());
    }
}
