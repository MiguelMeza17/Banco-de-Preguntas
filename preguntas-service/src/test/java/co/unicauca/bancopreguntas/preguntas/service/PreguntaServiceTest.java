package co.unicauca.bancopreguntas.preguntas.service;

import co.unicauca.bancopreguntas.preguntas.PreguntasDePrueba;
import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import co.unicauca.bancopreguntas.preguntas.domain.ReglaNegocioException;
import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.CreadorPreguntaSeleccionMultiple;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.FabricaPreguntas;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.PreguntaInvalidaException;
import co.unicauca.bancopreguntas.preguntas.evento.EventoIntegracion;
import co.unicauca.bancopreguntas.preguntas.evento.PreguntaCreada;
import co.unicauca.bancopreguntas.preguntas.evento.PreguntaEnviadaARevision;
import co.unicauca.bancopreguntas.preguntas.evento.PublicadorEventos;
import co.unicauca.bancopreguntas.preguntas.filtro.CombinadorFiltros;
import co.unicauca.bancopreguntas.preguntas.repository.PreguntaRepository;
import co.unicauca.bancopreguntas.preguntas.web.adaptador.PreguntaRequestAdapter;
import co.unicauca.bancopreguntas.preguntas.web.dto.PreguntaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static co.unicauca.bancopreguntas.preguntas.PreguntasDePrueba.AUTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreguntaServiceTest {

    @Mock
    private PreguntaRepository repository;

    @Mock
    private PublicadorEventos eventos;

    @Captor
    private ArgumentCaptor<EventoIntegracion> eventoCaptor;

    private PreguntaService service;

    @BeforeEach
    void setUp() {
        service = new PreguntaService(repository,
                new FabricaPreguntas(List.of(new CreadorPreguntaSeleccionMultiple())),
                new CombinadorFiltros(List.of()), eventos);
        lenient().when(repository.save(any(Pregunta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void existe(Pregunta p) {
        when(repository.findById(1L)).thenReturn(Optional.of(p));
    }

    @Test
    void crearGuardaEnBorradorYPublicaPreguntaCreada() {
        PreguntaRequest request = PreguntasDePrueba.valida();

        Pregunta creada = service.crear(new PreguntaRequestAdapter(request), AUTOR);

        assertThat(creada.getEstado()).isEqualTo(CodigoEstado.BORRADOR);
        verify(repository).save(creada);
        verify(eventos).publicar(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue()).isInstanceOf(PreguntaCreada.class);
        assertThat(((PreguntaCreada) eventoCaptor.getValue()).autorId()).isEqualTo(AUTOR);
    }

    @Test
    void unaPreguntaInvalidaNoSeGuardaNiSeAnuncia() {
        PreguntaRequest v = PreguntasDePrueba.valida();
        PreguntaRequest sinOpciones = new PreguntaRequest(AUTOR, null, v.titulo(), v.contexto(), v.enunciado(),
                null, null, null, null, "A", v.justificacion(), v.bibliografia(), v.competencia(), v.tema(),
                v.subtema(), v.dificultad());

        assertThatThrownBy(() -> service.crear(new PreguntaRequestAdapter(sinOpciones), AUTOR))
                .isInstanceOf(PreguntaInvalidaException.class);
        verify(repository, never()).save(any());
        verify(eventos, never()).publicar(any());
    }

    @Test
    void enviarARevisionPublicaPreguntaEnviadaARevision() {
        existe(PreguntasDePrueba.pregunta());

        Pregunta p = service.cambiarEstado(1L, CodigoEstado.PENDIENTE_REVISION, AUTOR);

        assertThat(p.getEstado()).isEqualTo(CodigoEstado.PENDIENTE_REVISION);
        verify(eventos).publicar(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue()).isInstanceOf(PreguntaEnviadaARevision.class);
    }

    @Test
    void losEstadosDeLaRevisionNoSePidenPorRest() {
        existe(PreguntasDePrueba.pregunta());

        assertThatThrownBy(() -> service.cambiarEstado(1L, CodigoEstado.APROBADA, AUTOR))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("revision por pares");
        verify(eventos, never()).publicar(any());
    }

    @Test
    void preguntaInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(1L)).isInstanceOf(PreguntaNoEncontradaException.class);
    }

    @Test
    void alAsignarRevisoresPasaAEnRevisionYEsIdempotente() {
        Pregunta p = PreguntasDePrueba.pregunta();
        p.enviarARevision(AUTOR);
        existe(p);

        service.registrarInicioRevision(1L);
        service.registrarInicioRevision(1L);

        assertThat(p.getEstado()).isEqualTo(CodigoEstado.EN_REVISION);
    }

    @Test
    void elDictamenFinalApruebaORechaza() {
        Pregunta p = PreguntasDePrueba.pregunta();
        p.enviarARevision(AUTOR);
        p.iniciarRevision();
        existe(p);

        service.registrarDictamen(1L, "RECHAZADA");

        assertThat(p.getEstado()).isEqualTo(CodigoEstado.RECHAZADA);
    }

    @Test
    void unDictamenRepetidoSeIgnora() {
        Pregunta p = PreguntasDePrueba.pregunta();
        p.enviarARevision(AUTOR);
        p.iniciarRevision();
        p.aprobar();
        existe(p);

        service.registrarDictamen(1L, "RECHAZADA");

        assertThat(p.getEstado()).isEqualTo(CodigoEstado.APROBADA);
    }
}
