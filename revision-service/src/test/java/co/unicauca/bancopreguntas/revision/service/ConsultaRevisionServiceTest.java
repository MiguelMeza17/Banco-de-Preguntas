package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;
import co.unicauca.bancopreguntas.revision.domain.ReglaRevisionException;
import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/** T5.2: listado de preguntas asignadas a un revisor y apertura de la revision. */
@ExtendWith(MockitoExtension.class)
class ConsultaRevisionServiceTest {

    private static final Long REVISOR = 7L;

    @Mock
    private RevisionRepository repository;

    private ConsultaRevisionService service;

    @BeforeEach
    void setUp() {
        service = new ConsultaRevisionService(repository);
        lenient().when(repository.save(any(Revision.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void lasPendientesDeEvaluarAparecenPrimero() {
        Revision evaluada = new Revision(10L, REVISOR);
        evaluada.evaluar(REVISOR, EstadoRevision.APROBADA, null);
        Revision pendiente = new Revision(11L, REVISOR);
        when(repository.findByRevisorIdOrderByFechaAsignacionDesc(REVISOR)).thenReturn(List.of(evaluada, pendiente));

        assertThat(service.asignadasA(REVISOR, null)).extracting(Revision::getPreguntaId).containsExactly(11L, 10L);
    }

    @Test
    void filtraPorEstadoCuandoSeIndica() {
        Revision pendiente = new Revision(11L, REVISOR);
        when(repository.findByRevisorIdAndEstadoOrderByFechaAsignacionDesc(REVISOR, EstadoRevision.ASIGNADA))
                .thenReturn(List.of(pendiente));

        assertThat(service.asignadasA(REVISOR, EstadoRevision.ASIGNADA)).containsExactly(pendiente);
    }

    @Test
    void abrirLaRevisionLaPasaAEnRevisionYEsIdempotente() {
        Revision revision = new Revision(10L, REVISOR);
        when(repository.findById(1L)).thenReturn(Optional.of(revision));

        service.iniciar(1L, REVISOR);
        service.iniciar(1L, REVISOR);

        assertThat(revision.getEstado()).isEqualTo(EstadoRevision.EN_REVISION);
    }

    @Test
    void otroRevisorNoPuedeAbrirla() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Revision(10L, REVISOR)));

        assertThatThrownBy(() -> service.iniciar(1L, 99L)).isInstanceOf(ReglaRevisionException.class);
    }

    @Test
    void unaRevisionEvaluadaNoSeReabre() {
        Revision revision = new Revision(10L, REVISOR);
        revision.evaluar(REVISOR, EstadoRevision.RECHAZADA, "Falta bibliografia");
        when(repository.findById(1L)).thenReturn(Optional.of(revision));

        assertThatThrownBy(() -> service.iniciar(1L, REVISOR)).isInstanceOf(ReglaRevisionException.class);
    }
}
