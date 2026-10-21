package co.unicauca.bancopreguntas.revision.web.dto;

import co.unicauca.bancopreguntas.revision.domain.Revision;

import java.time.Instant;

public record RevisionResponse(
        Long id,
        Long preguntaId,
        Long revisorId,
        String estado,
        Instant fechaAsignacion
) {
    public static RevisionResponse from(Revision revision) {
        return new RevisionResponse(
                revision.getId(),
                revision.getPreguntaId(),
                revision.getRevisorId(),
                revision.getEstado().name(),
                revision.getFechaAsignacion()
        );
    }
}
