package co.unicauca.bancopreguntas.usuarios.service;

import co.unicauca.bancopreguntas.usuarios.domain.HasherPassword;
import co.unicauca.bancopreguntas.usuarios.domain.Rol;
import co.unicauca.bancopreguntas.usuarios.domain.Usuario;
import co.unicauca.bancopreguntas.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private HasherPassword hasher;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, hasher);
        lenient().when(hasher.hash(anyString())).thenAnswer(inv -> "hash:" + inv.getArgument(0));
        lenient().when(hasher.verificar(anyString(), anyString()))
                .thenAnswer(inv -> inv.getArgument(0).equals("hash:" + inv.getArgument(1)));
        lenient().when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void registraUnUsuarioConLaContrasenaCifrada() {
        Usuario usuario = service.registrar("ana", "Ana Ruiz", "ana@u.co", Rol.AUTOR, "Clave#1");

        assertThat(usuario.getLogin()).isEqualTo("ana");
        assertThat(usuario.getPasswordHash()).isEqualTo("hash:Clave#1");
        assertThat(usuario.estaActivo()).isTrue();
    }

    @Test
    void noRegistraUnLoginRepetido() {
        when(repository.existsByLoginIgnoreCase("ana")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar("ana", "Ana", null, Rol.AUTOR, "Clave#1"))
                .isInstanceOf(UsuarioException.class)
                .extracting("estadoHttp").isEqualTo(409);
        verify(repository, never()).save(any());
    }

    @Test
    void rechazaContrasenasDebiles() {
        assertThatThrownBy(() -> service.registrar("ana", "Ana", null, Rol.AUTOR, "clave"))
                .isInstanceOf(UsuarioException.class)
                .hasMessageContaining("mayuscula")
                .hasMessageContaining("digito");
    }

    @Test
    void autenticaConCredencialesCorrectas() {
        Usuario ana = new Usuario("ana", "Ana", null, Rol.AUTOR, "hash:Clave#1");
        when(repository.findByLoginIgnoreCase("ana")).thenReturn(Optional.of(ana));

        assertThat(service.autenticar("ana", "Clave#1")).isSameAs(ana);
    }

    @Test
    void noRevelaSiElUsuarioExiste() {
        when(repository.findByLoginIgnoreCase("ana"))
                .thenReturn(Optional.of(new Usuario("ana", "Ana", null, Rol.AUTOR, "hash:Clave#1")));
        when(repository.findByLoginIgnoreCase("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autenticar("ana", "otra"))
                .hasMessage("Usuario o contrasena incorrectos.");
        assertThatThrownBy(() -> service.autenticar("nadie", "Clave#1"))
                .hasMessage("Usuario o contrasena incorrectos.");
    }
}
