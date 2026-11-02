package co.unicauca.bancopreguntas.notificaciones.evento;

import java.util.List;
import java.util.Set;

/**
 * Version local de los eventos que consume este servicio: solo los campos que necesita.
 * Los productores pueden agregar campos sin romper a este consumidor.
 */
public final class Eventos {

    private Eventos() {
    }

    public record PreguntaCreada(Long preguntaId, Long autorId, String titulo) {
    }

    public record PreguntaEnviadaARevision(Long preguntaId, Long autorId, String titulo) {
    }

    public record RevisoresAsignados(Long preguntaId, List<Long> revisionIds, List<Long> revisorIds) {
    }

    public record PreguntaEvaluada(Long revisionId, Long preguntaId, Long revisorId, String resultado,
                                   String observaciones, Set<String> destinatarios, boolean revisionCompleta,
                                   String resultadoPregunta) {
    }

    /** Topicos del bus (ver docs/corte2/EVENTOS.md). */
    public static final String PREGUNTA_CREADA = "banco.preguntas.creada";
    public static final String PREGUNTA_ENVIADA_A_REVISION = "banco.preguntas.enviada-revision";
    public static final String REVISORES_ASIGNADOS = "banco.revisiones.revisores-asignados";
    public static final String PREGUNTA_EVALUADA = "banco.revisiones.pregunta-evaluada";
}
