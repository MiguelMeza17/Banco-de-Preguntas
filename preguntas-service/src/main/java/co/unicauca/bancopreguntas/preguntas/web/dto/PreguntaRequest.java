package co.unicauca.bancopreguntas.preguntas.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo REST para crear o editar una pregunta. Es un DTO plano pensado para el cliente web;
 * {@code CrearPreguntaRequestAdapter} lo adapta a lo que espera el dominio.
 */
@Schema(description = "Datos de una pregunta de seleccion multiple con unica respuesta")
public record PreguntaRequest(
        @Schema(description = "Usuario que crea o edita la pregunta", example = "2") Long usuarioId,
        @Schema(example = "SELECCION_MULTIPLE_UNICA_RESPUESTA") String tipo,
        @Schema(example = "Lectura de graficas") String titulo,
        String contexto,
        @Schema(example = "Segun la grafica, ¿en que ano se produjo el mayor crecimiento?") String enunciado,
        String opcionA,
        String opcionB,
        String opcionC,
        String opcionD,
        @Schema(description = "Letra de la opcion correcta", example = "B") String respuestaCorrecta,
        String justificacion,
        String bibliografia,
        @Schema(example = "Razonamiento cuantitativo") String competencia,
        @Schema(example = "Estadistica") String tema,
        @Schema(example = "Interpretacion de graficas") String subtema,
        @Schema(example = "MEDIA", allowableValues = {"BAJA", "MEDIA", "ALTA"}) String dificultad
) {
}
