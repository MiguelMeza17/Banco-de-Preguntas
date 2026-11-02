package co.unicauca.bancopreguntas.revision.evento;

import co.unicauca.bancopreguntas.revision.domain.Revision;

import java.time.Instant;
import java.util.Set;

/**
 * Evento de dominio: un revisor aprobo o rechazo una pregunta.
 * <p>
 * El servicio de revision no conoce al autor ni al administrador (son datos de otros servicios),
 * por eso el evento indica los <b>roles</b> a notificar y el servicio de notificaciones resuelve
 * quienes son a partir de {@code preguntaId}.
 *
 * @param revisionId        revision evaluada
 * @param preguntaId        pregunta evaluada
 * @param revisorId         revisor que emitio el dictamen
 * @param resultado         dictamen de este revisor: APROBADA o RECHAZADA
 * @param observaciones     comentarios del revisor (puede ser nulo si se aprobo sin comentarios)
 * @param destinatarios     roles que deben recibir la notificacion
 * @param revisionCompleta  true si ya evaluaron todos los revisores asignados a la pregunta
 * @param resultadoPregunta dictamen final de la pregunta cuando la revision esta completa
 *                          (RECHAZADA si algun revisor rechazo); nulo si aun faltan revisores
 * @param ocurridoEn        momento de la evaluacion
 */
public record PreguntaEvaluada(
        Long revisionId,
        Long preguntaId,
        Long revisorId,
        String resultado,
        String observaciones,
        Set<Destinatario> destinatarios,
        boolean revisionCompleta,
        String resultadoPregunta,
        Instant ocurridoEn
) implements EventoIntegracion {

    /** Roles que se notifican cuando se evalua una pregunta. */
    public enum Destinatario {
        AUTOR,
        ADMINISTRADOR
    }

    public PreguntaEvaluada {
        destinatarios = Set.copyOf(destinatarios);
    }

    public static PreguntaEvaluada de(Revision revision, boolean revisionCompleta, String resultadoPregunta) {
        return new PreguntaEvaluada(
                revision.getId(),
                revision.getPreguntaId(),
                revision.getRevisorId(),
                revision.getEstado().name(),
                revision.getObservaciones(),
                Set.of(Destinatario.AUTOR, Destinatario.ADMINISTRADOR),
                revisionCompleta,
                resultadoPregunta,
                revision.getFechaEvaluacion());
    }

    @Override
    public String topico() {
        return Topicos.PREGUNTA_EVALUADA;
    }
}
