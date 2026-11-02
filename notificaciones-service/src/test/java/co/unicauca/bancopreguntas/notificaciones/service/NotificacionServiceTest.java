package co.unicauca.bancopreguntas.notificaciones.service;

import co.unicauca.bancopreguntas.notificaciones.domain.Notificacion;
import co.unicauca.bancopreguntas.notificaciones.evento.Eventos;
import co.unicauca.bancopreguntas.notificaciones.repository.NotificacionRepository;
import co.unicauca.bancopreguntas.notificaciones.repository.PreguntaConocidaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Reglas de "a quien avisar" con una base H2 real. */
@DataJpaTest
@Import({NotificacionService.class, CanalCorreoSimulado.class})
class NotificacionServiceTest {

    private static final Long AUTOR = 2L;
    private static final Long ADMIN = 1L;

    @Autowired
    private NotificacionService service;

    @Autowired
    private NotificacionRepository notificaciones;

    @Autowired
    private PreguntaConocidaRepository preguntas;

    private void crearPregunta() {
        service.alCrearPregunta(new Eventos.PreguntaCreada(10L, AUTOR, "Lectura de graficas"));
    }

    @Test
    void crearUnaPreguntaSoloRegistraSuAutor() {
        crearPregunta();

        assertThat(preguntas.findById(10L)).get().extracting("autorId").isEqualTo(AUTOR);
        assertThat(notificaciones.count()).isZero();
    }

    @Test
    void alEnviarARevisionSeAvisaALosAdministradores() {
        service.alEnviarARevision(new Eventos.PreguntaEnviadaARevision(10L, AUTOR, "Lectura de graficas"));

        List<Notificacion> bandejaAdmin = service.bandeja(ADMIN, "ADMINISTRADOR");
        assertThat(bandejaAdmin).hasSize(1);
        assertThat(bandejaAdmin.get(0).getMensaje()).contains("Lectura de graficas");
        assertThat(service.bandeja(AUTOR, "AUTOR")).isEmpty();
    }

    @Test
    void cadaRevisorAsignadoRecibeSuAviso() {
        crearPregunta();

        service.alAsignarRevisores(new Eventos.RevisoresAsignados(10L, List.of(100L, 101L), List.of(3L, 4L)));

        assertThat(service.bandeja(3L, "REVISOR")).hasSize(1);
        assertThat(service.bandeja(4L, "REVISOR")).hasSize(1);
        assertThat(service.bandeja(5L, "REVISOR")).isEmpty();
    }

    @Test
    void laEvaluacionSeNotificaAlAutorYAlAdministrador() {
        crearPregunta();

        service.alEvaluarPregunta(new Eventos.PreguntaEvaluada(100L, 10L, 3L, "RECHAZADA",
                "La opcion C tambien es valida", Set.of("AUTOR", "ADMINISTRADOR"), true, "RECHAZADA"));

        List<Notificacion> delAutor = service.bandeja(AUTOR, "AUTOR");
        assertThat(delAutor).hasSize(1);
        assertThat(delAutor.get(0).getMensaje())
                .contains("rechazada")
                .contains("La opcion C tambien es valida")
                .contains("Revision completa");
        assertThat(service.bandeja(ADMIN, "ADMINISTRADOR")).hasSize(1);
    }

    @Test
    void siFaltanRevisoresElAvisoLoIndica() {
        crearPregunta();

        service.alEvaluarPregunta(new Eventos.PreguntaEvaluada(100L, 10L, 3L, "APROBADA", null,
                Set.of("AUTOR", "ADMINISTRADOR"), false, null));

        assertThat(service.bandeja(AUTOR, null).get(0).getMensaje()).contains("Aun faltan revisores");
    }

    @Test
    void marcarComoLeida() {
        crearPregunta();
        List<Notificacion> nuevas = service.alAsignarRevisores(
                new Eventos.RevisoresAsignados(10L, List.of(100L), List.of(3L)));

        service.marcarLeida(nuevas.get(0).getId());

        assertThat(service.bandeja(3L, null).get(0).isLeida()).isTrue();
    }
}
