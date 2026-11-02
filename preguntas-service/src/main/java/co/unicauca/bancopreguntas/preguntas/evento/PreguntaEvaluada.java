package co.unicauca.bancopreguntas.preguntas.evento;

/**
 * Evento que publica revision-service cuando un revisor emite su dictamen.
 * {@code revisionCompleta} indica si ya evaluaron todos los revisores asignados y
 * {@code resultadoPregunta} el dictamen final (APROBADA o RECHAZADA) en ese caso.
 */
public record PreguntaEvaluada(Long preguntaId, boolean revisionCompleta, String resultadoPregunta) {
}
