package co.unicauca.bancopreguntas.preguntas.evento;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;

import java.time.Instant;

/** El autor envio la pregunta a revision: el administrador debe asignar revisores. */
public record PreguntaEnviadaARevision(
        Long preguntaId,
        Long autorId,
        String titulo,
        Instant ocurridoEn
) implements EventoIntegracion {

    public static PreguntaEnviadaARevision de(Pregunta p) {
        return new PreguntaEnviadaARevision(p.getId(), p.getAutorId(), p.getTitulo(), p.getFechaActualizacion());
    }

    @Override
    public String topico() {
        return Topicos.PREGUNTA_ENVIADA_A_REVISION;
    }
}
