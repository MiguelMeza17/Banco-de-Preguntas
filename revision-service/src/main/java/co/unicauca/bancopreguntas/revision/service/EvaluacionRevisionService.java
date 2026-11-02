package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;
import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.evento.PreguntaEvaluada;
import co.unicauca.bancopreguntas.revision.evento.PublicadorEventos;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso HU-5: el revisor aprueba o rechaza una pregunta asignada y deja observaciones.
 * Las reglas viven en {@link Revision#evaluar}; el servicio carga, delega, guarda y publica
 * el evento {@link PreguntaEvaluada} para que se notifique al autor y al administrador.
 * <p>
 * Cuando todos los revisores de la pregunta ya evaluaron, el evento lleva el dictamen final:
 * basta un rechazo para que la pregunta quede rechazada.
 */
@Service
public class EvaluacionRevisionService {

    private final RevisionRepository revisionRepository;
    private final PublicadorEventos publicadorEventos;

    public EvaluacionRevisionService(RevisionRepository revisionRepository,
                                     PublicadorEventos publicadorEventos) {
        this.revisionRepository = revisionRepository;
        this.publicadorEventos = publicadorEventos;
    }

    @Transactional
    public Revision evaluar(Long revisionId, Long revisorId, EstadoRevision resultado, String observaciones) {
        Revision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new RevisionNoEncontradaException(revisionId));
        revision.evaluar(revisorId, resultado, observaciones);
        Revision guardada = revisionRepository.save(revision);

        List<Revision> todas = revisionRepository.findByPreguntaId(guardada.getPreguntaId());
        boolean completa = todas.stream().allMatch(Revision::tieneDictamen);
        String dictamenFinal = completa ? dictamenFinal(todas) : null;

        publicadorEventos.publicar(PreguntaEvaluada.de(guardada, completa, dictamenFinal));
        return guardada;
    }

    static String dictamenFinal(List<Revision> revisiones) {
        boolean algunRechazo = revisiones.stream().anyMatch(r -> r.getEstado() == EstadoRevision.RECHAZADA);
        return (algunRechazo ? EstadoRevision.RECHAZADA : EstadoRevision.APROBADA).name();
    }
}
