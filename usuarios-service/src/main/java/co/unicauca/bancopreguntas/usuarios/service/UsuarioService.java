package co.unicauca.bancopreguntas.usuarios.service;

import co.unicauca.bancopreguntas.usuarios.domain.HasherPassword;
import co.unicauca.bancopreguntas.usuarios.domain.Rol;
import co.unicauca.bancopreguntas.usuarios.domain.Usuario;
import co.unicauca.bancopreguntas.usuarios.domain.ValidadorPassword;
import co.unicauca.bancopreguntas.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final HasherPassword hasher;
    private final ValidadorPassword validador = new ValidadorPassword();

    public UsuarioService(UsuarioRepository repository, HasherPassword hasher) {
        this.repository = repository;
        this.hasher = hasher;
    }

    @Transactional
    public Usuario registrar(String login, String nombreCompleto, String email, Rol rol, String password) {
        if (login == null || login.isBlank() || nombreCompleto == null || nombreCompleto.isBlank() || rol == null) {
            throw new UsuarioException(400, "Login, nombre completo y rol son obligatorios.");
        }
        if (repository.existsByLoginIgnoreCase(login.trim())) {
            throw new UsuarioException(409, "El login '" + login.trim() + "' ya existe.");
        }
        List<String> errores = validador.errores(password);
        if (!errores.isEmpty()) {
            throw new UsuarioException(400, String.join(" ", errores));
        }
        return repository.save(new Usuario(login.trim(), nombreCompleto.trim(),
                email == null || email.isBlank() ? null : email.trim(), rol, hasher.hash(password)));
    }

    /**
     * Autentica al usuario. Devuelve el mismo error si el login no existe o la contrasena
     * es incorrecta, para no revelar cuales usuarios existen.
     */
    @Transactional(readOnly = true)
    public Usuario autenticar(String login, String password) {
        Optional<Usuario> usuario = login == null ? Optional.empty() : repository.findByLoginIgnoreCase(login.trim());
        if (usuario.isEmpty() || !hasher.verificar(usuario.get().getPasswordHash(), password)) {
            throw new UsuarioException(401, "Usuario o contrasena incorrectos.");
        }
        if (!usuario.get().estaActivo()) {
            throw new UsuarioException(403, "El usuario esta inactivo.");
        }
        return usuario.get();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioException(404, "No existe el usuario " + id + "."));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar(Rol rol) {
        return rol == null ? repository.findAll() : repository.findByRolOrderByNombreCompleto(rol);
    }
}
