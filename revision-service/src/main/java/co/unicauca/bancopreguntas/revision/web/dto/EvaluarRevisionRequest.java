package co.unicauca.bancopreguntas.revision.web.dto;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;

/**
 * Cuerpo de {@code POST /revisiones/{id}/evaluar}.
 *
 * @param revisorId     revisor que emite el dictamen
 * @param resultado     APROBADA o RECHAZADA
 * @param observaciones comentarios para el autor (obligatorios al rechazar)
 */
public record EvaluarRevisionRequest(
        Long revisorId,
        EstadoRevision resultado,
        String observaciones
) {
}
