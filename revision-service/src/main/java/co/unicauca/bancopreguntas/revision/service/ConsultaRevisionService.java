package co.unicauca.bancopreguntas.revision.service;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;
import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.repository.RevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Consultas del contexto de revision (HU-5 y RF-19: historial de revisiones). */
@Service
public class ConsultaRevisionService {

    private final RevisionRepository repository;

    public ConsultaRevisionService(RevisionRepository repository) {
        this.repository = repository;
    }

    /**
     * Preguntas asignadas a un revisor (T5.2). Sin estado devuelve primero las pendientes de
     * evaluar (ASIGNADA y EN_REVISION) y luego las ya evaluadas.
     */
    @Transactional(readOnly = true)
    public List<Revision> asignadasA(Long revisorId, EstadoRevision estado) {
        if (estado != null) {
            return repository.findByRevisorIdAndEstadoOrderByFechaAsignacionDesc(revisorId, estado);
        }
        return repository.findByRevisorIdOrderByFechaAsignacionDesc(revisorId).stream()
                .sorted((a, b) -> Boolean.compare(a.tieneDictamen(), b.tieneDictamen()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Revision> historialDePregunta(Long preguntaId) {
        return repository.findByPreguntaIdOrderByFechaAsignacionAsc(preguntaId);
    }

    @Transactional(readOnly = true)
    public Revision buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RevisionNoEncontradaException(id));
    }

    /** El revisor abre la pregunta para leerla: ASIGNADA pasa a EN_REVISION. */
    @Transactional
    public Revision iniciar(Long id, Long revisorId) {
        Revision revision = buscar(id);
        revision.iniciar(revisorId);
        return repository.save(revision);
    }
}
