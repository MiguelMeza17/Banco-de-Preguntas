package co.unicauca.bancopreguntas.usuarios.repository;

import co.unicauca.bancopreguntas.usuarios.domain.Rol;
import co.unicauca.bancopreguntas.usuarios.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLoginIgnoreCase(String login);

    boolean existsByLoginIgnoreCase(String login);

    List<Usuario> findByRolOrderByNombreCompleto(Rol rol);
}
