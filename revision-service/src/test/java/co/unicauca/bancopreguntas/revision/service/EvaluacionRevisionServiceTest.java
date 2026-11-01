package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;
import co.unicauca.bancopreguntas.revision.domain.ReglaRevisionException;
import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.evento.PreguntaEvaluada;
import co.unicauca.bancopreguntas.revision.evento.PublicadorEventos;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluacionRevisionServiceTest {

    private static final Long REVISION = 1L;
    private static final Long PREGUNTA = 10L;
    private static final Long REVISOR = 7L;

    @Mock
    private RevisionRepository revisionRepository;

    @Mock
    private PublicadorEventos publicadorEventos;

    @Captor
    private ArgumentCaptor<Object> eventoCaptor;

    private EvaluacionRevisionService service;

    @BeforeEach
    void setUp() {
        service = new EvaluacionRevisionService(revisionRepository, publicadorEventos);
        lenient().when(revisionRepository.save(any(Revision.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void existeRevisionAsignada() {
        when(revisionRepository.findById(REVISION)).thenReturn(Optional.of(new Revision(PREGUNTA, REVISOR)));
    }

    @Test
    void aprobarGuardaLaRevisionYPublicaElEvento() {
        existeRevisionAsignada();

        Revision resultado = service.evaluar(REVISION, REVISOR, EstadoRevision.APROBADA, "Correcta");

        assertThat(resultado.getEstado()).isEqualTo(EstadoRevision.APROBADA);
        InOrder orden = inOrder(revisionRepository, publicadorEventos);
        orden.verify(revisionRepository).save(resultado);
        orden.verify(publicadorEventos).publicar(eventoCaptor.capture());

        PreguntaEvaluada evento = (PreguntaEvaluada) eventoCaptor.getValue();
        assertThat(evento.preguntaId()).isEqualTo(PREGUNTA);
        assertThat(evento.revisorId()).isEqualTo(REVISOR);
        assertThat(evento.resultado()).isEqualTo("APROBADA");
        assertThat(evento.observaciones()).isEqualTo("Correcta");
        assertThat(evento.ocurridoEn()).isNotNull();
    }

    @Test
    void elEventoNotificaAlAutorYAlAdministrador() {
        existeRevisionAsignada();

        service.evaluar(REVISION, REVISOR, EstadoRevision.RECHAZADA, "Falta la justificacion");

        verify(publicadorEventos).publicar(eventoCaptor.capture());
        PreguntaEvaluada evento = (PreguntaEvaluada) eventoCaptor.getValue();
        assertThat(evento.resultado()).isEqualTo("RECHAZADA");
        assertThat(evento.destinatarios()).containsExactlyInAnyOrder(
                PreguntaEvaluada.Destinatario.AUTOR, PreguntaEvaluada.Destinatario.ADMINISTRADOR);
    }

    @Test
    void siLaRevisionNoExisteLanzaExcepcionYNoPublica() {
        when(revisionRepository.findById(REVISION)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluar(REVISION, REVISOR, EstadoRevision.APROBADA, null))
                .isInstanceOf(RevisionNoEncontradaException.class);
        verify(revisionRepository, never()).save(any());
        verify(publicadorEventos, never()).publicar(any());
    }

    @Test
    void siSeIncumpleUnaReglaNoGuardaNiPublica() {
        existeRevisionAsignada();

        assertThatThrownBy(() -> service.evaluar(REVISION, REVISOR, EstadoRevision.RECHAZADA, ""))
                .isInstanceOf(ReglaRevisionException.class);
        verify(revisionRepository, never()).save(any());
        verify(publicadorEventos, never()).publicar(any());
    }

    @Test
    void otroRevisorNoPuedeEvaluar() {
        existeRevisionAsignada();

        assertThatThrownBy(() -> service.evaluar(REVISION, 99L, EstadoRevision.APROBADA, null))
                .isInstanceOf(ReglaRevisionException.class);
        verify(publicadorEventos, never()).publicar(any());
    }
}
