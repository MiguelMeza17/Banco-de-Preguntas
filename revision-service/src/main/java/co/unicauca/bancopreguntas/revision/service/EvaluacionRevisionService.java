package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;
import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso HU-5: el revisor aprueba o rechaza una pregunta asignada y deja observaciones.
 * Las reglas viven en {@link Revision#evaluar}; el servicio solo carga, delega y guarda.
 */
@Service
public class EvaluacionRevisionService {

    private final RevisionRepository revisionRepository;

    public EvaluacionRevisionService(RevisionRepository revisionRepository) {
        this.revisionRepository = revisionRepository;
    }

    @Transactional
    public Revision evaluar(Long revisionId, Long revisorId, EstadoRevision resultado, String observaciones) {
        Revision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new RevisionNoEncontradaException(revisionId));
        revision.evaluar(revisorId, resultado, observaciones);
        return revisionRepository.save(revision);
    }
}
