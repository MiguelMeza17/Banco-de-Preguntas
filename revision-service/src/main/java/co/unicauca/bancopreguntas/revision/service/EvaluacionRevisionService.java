package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;
import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.evento.PreguntaEvaluada;
import co.unicauca.bancopreguntas.revision.evento.PublicadorEventos;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso HU-5: el revisor aprueba o rechaza una pregunta asignada y deja observaciones.
 * Las reglas viven en {@link Revision#evaluar}; el servicio carga, delega, guarda y publica
 * el evento {@link PreguntaEvaluada} para que se notifique al autor y al administrador.
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

        publicadorEventos.publicar(PreguntaEvaluada.de(guardada));
        return guardada;
    }
}
