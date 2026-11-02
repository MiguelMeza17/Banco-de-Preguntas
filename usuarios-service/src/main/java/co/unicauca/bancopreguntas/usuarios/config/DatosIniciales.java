package co.unicauca.bancopreguntas.usuarios.config;

import co.unicauca.bancopreguntas.usuarios.domain.Rol;
import co.unicauca.bancopreguntas.usuarios.repository.UsuarioRepository;
import co.unicauca.bancopreguntas.usuarios.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Crea usuarios de demostracion la primera vez que arranca el servicio (base vacia),
 * uno por cada rol que participa en el flujo de preguntas y revision.
 * Las credenciales estan documentadas en el README.
 */
@Component
@Profile("!test")
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);
    private static final String PASSWORD_DEMO = "Demo#2026";

    private final UsuarioRepository repository;
    private final UsuarioService service;

    public DatosIniciales(UsuarioRepository repository, UsuarioService service) {
        this.repository = repository;
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        service.registrar("admin", "Administrador del banco", "admin@unicauca.edu.co", Rol.ADMINISTRADOR, PASSWORD_DEMO);
        service.registrar("autor", "Autora de preguntas", "autor@unicauca.edu.co", Rol.AUTOR, PASSWORD_DEMO);
        service.registrar("revisor1", "Revisor Uno", "revisor1@unicauca.edu.co", Rol.REVISOR, PASSWORD_DEMO);
        service.registrar("revisor2", "Revisora Dos", "revisor2@unicauca.edu.co", Rol.REVISOR, PASSWORD_DEMO);
        log.info("Usuarios de demostracion creados (admin, autor, revisor1, revisor2).");
    }
}
