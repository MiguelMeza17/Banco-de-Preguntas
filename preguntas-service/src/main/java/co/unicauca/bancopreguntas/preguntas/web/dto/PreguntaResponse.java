package co.unicauca.bancopreguntas.preguntas.web.dto;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;

import java.time.Instant;

public record PreguntaResponse(
        Long id,
        String tipo,
        String titulo,
        String contexto,
        String enunciado,
        String opcionA,
        String opcionB,
        String opcionC,
        String opcionD,
        String respuestaCorrecta,
        String justificacion,
        String bibliografia,
        String competencia,
        String tema,
        String subtema,
        String dificultad,
        String estado,
        String estadoNombre,
        boolean editable,
        Long autorId,
        Instant fechaCreacion,
        Instant fechaActualizacion
) {
    public static PreguntaResponse from(Pregunta p) {
        return new PreguntaResponse(
                p.getId(),
                p.getTipo().name(),
                p.getTitulo(),
                p.getContexto(),
                p.getEnunciado(),
                p.getOpciones().getOpcionA(),
                p.getOpciones().getOpcionB(),
                p.getOpciones().getOpcionC(),
                p.getOpciones().getOpcionD(),
                p.getRespuestaCorrecta(),
                p.getJustificacion(),
                p.getBibliografia(),
                p.getCompetencia(),
                p.getTema(),
                p.getSubtema(),
                p.getDificultad().name(),
                p.getEstado().name(),
                p.getEstado().getNombre(),
                p.comportamiento().permiteEdicion(),
                p.getAutorId(),
                p.getFechaCreacion(),
                p.getFechaActualizacion());
    }
}
