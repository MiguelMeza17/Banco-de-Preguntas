package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignacionRevisionService {

    private final RevisionRepository revisionRepository;

    public AsignacionRevisionService(RevisionRepository revisionRepository) {
        this.revisionRepository = revisionRepository;
    }

    /**
     * Crea una asignacion por cada revisor para la pregunta indicada y las persiste
     * en estado ASIGNADA.
     */
    public List<Revision> asignarRevisores(Long preguntaId, List<Long> revisorIds) {
        List<Revision> asignaciones = revisorIds.stream()
                .map(revisorId -> new Revision(preguntaId, revisorId))
                .toList();
        return revisionRepository.saveAll(asignaciones);
    }
}
