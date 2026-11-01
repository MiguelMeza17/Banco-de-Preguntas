package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.evento.PublicadorEventos;
import co.unicauca.bancopreguntas.revision.evento.RevisoresAsignados;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AsignacionRevisionService {

    private final RevisionRepository revisionRepository;
    private final PublicadorEventos publicadorEventos;

    public AsignacionRevisionService(RevisionRepository revisionRepository,
                                     PublicadorEventos publicadorEventos) {
        this.revisionRepository = revisionRepository;
        this.publicadorEventos = publicadorEventos;
    }

    /**
     * Crea una asignacion por cada revisor para la pregunta indicada, las persiste
     * en estado ASIGNADA y publica el evento {@link RevisoresAsignados}.
     * El servicio ya no notifica directamente a los revisores.
     */
    @Transactional
    public List<Revision> asignarRevisores(Long preguntaId, List<Long> revisorIds) {
        List<Revision> asignaciones = revisorIds.stream()
                .map(revisorId -> new Revision(preguntaId, revisorId))
                .toList();
        List<Revision> guardadas = revisionRepository.saveAll(asignaciones);

        publicadorEventos.publicar(new RevisoresAsignados(
                preguntaId,
                guardadas.stream().map(Revision::getId).toList(),
                guardadas.stream().map(Revision::getRevisorId).toList(),
                Instant.now()));
        return guardadas;
    }
}
