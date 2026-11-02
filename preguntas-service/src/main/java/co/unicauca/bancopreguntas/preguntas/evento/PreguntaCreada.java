package co.unicauca.bancopreguntas.preguntas.evento;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;

import java.time.Instant;

/** Un autor creo una pregunta (en Borrador). */
public record PreguntaCreada(
        Long preguntaId,
        Long autorId,
        String titulo,
        String competencia,
        String tema,
        Instant ocurridoEn
) implements EventoIntegracion {

    public static PreguntaCreada de(Pregunta p) {
        return new PreguntaCreada(p.getId(), p.getAutorId(), p.getTitulo(), p.getCompetencia(), p.getTema(),
                p.getFechaCreacion());
    }

    @Override
    public String topico() {
        return Topicos.PREGUNTA_CREADA;
    }
}
