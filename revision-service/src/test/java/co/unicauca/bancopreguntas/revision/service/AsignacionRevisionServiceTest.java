package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;
import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.evento.PublicadorEventos;
import co.unicauca.bancopreguntas.revision.evento.RevisoresAsignados;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignacionRevisionServiceTest {

    @Mock
    private RevisionRepository revisionRepository;

    @Mock
    private PublicadorEventos publicadorEventos;

    @Captor
    private ArgumentCaptor<List<Revision>> revisionesCaptor;

    @Captor
    private ArgumentCaptor<Object> eventoCaptor;

    private AsignacionRevisionService service;

    @BeforeEach
    void setUp() {
        service = new AsignacionRevisionService(revisionRepository, publicadorEventos);
        when(revisionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void creaUnaRevisionPorCadaRevisor() {
        List<Revision> resultado = service.asignarRevisores(10L, List.of(1L, 2L, 3L));

        assertThat(resultado).hasSize(3);
        assertThat(resultado).extracting(Revision::getRevisorId).containsExactly(1L, 2L, 3L);
        assertThat(resultado).allSatisfy(r -> assertThat(r.getPreguntaId()).isEqualTo(10L));
    }

    @Test
    void lasRevisionesQuedanEnEstadoAsignadaConFecha() {
        List<Revision> resultado = service.asignarRevisores(10L, List.of(1L, 2L));

        assertThat(resultado).allSatisfy(r -> {
            assertThat(r.getEstado()).isEqualTo(EstadoRevision.ASIGNADA);
            assertThat(r.getFechaAsignacion()).isNotNull();
        });
    }

    @Test
    void persisteTodasLasAsignaciones() {
        service.asignarRevisores(10L, List.of(1L, 2L));

        verify(revisionRepository).saveAll(revisionesCaptor.capture());
        assertThat(revisionesCaptor.getValue()).hasSize(2);
    }

    @Test
    void publicaElEventoRevisoresAsignadosDespuesDeGuardar() {
        service.asignarRevisores(10L, List.of(1L, 2L));

        InOrder orden = inOrder(revisionRepository, publicadorEventos);
        orden.verify(revisionRepository).saveAll(anyList());
        orden.verify(publicadorEventos).publicar(eventoCaptor.capture());

        assertThat(eventoCaptor.getValue()).isInstanceOf(RevisoresAsignados.class);
        RevisoresAsignados evento = (RevisoresAsignados) eventoCaptor.getValue();
        assertThat(evento.preguntaId()).isEqualTo(10L);
        assertThat(evento.revisorIds()).containsExactly(1L, 2L);
        assertThat(evento.revisionIds()).hasSize(2);
        assertThat(evento.ocurridoEn()).isNotNull();
    }
}
