package co.edu.unicauca.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionStructuralValidatorTest {

    private final QuestionStructuralValidator validator = new QuestionStructuralValidator();

    private Question.Builder validQuestionBuilder() {
        return new Question.Builder()
                .id("Q-1")
                .nombre("Título")
                .contexto("Un contexto suficientemente descriptivo.")
                .pregunta("¿Cuál es la respuesta correcta?")
                .distractors(new QuestionDistractors("A", "B", "C", "D"))
                .respuestaCorrecta("A")
                .justificacion("Porque sí, con evidencia.")
                .bibliografia("Autor (2024). Libro.")
                .competencia("Interpretativa")
                .tema("Tema 1")
                .subtema("Subtema 1")
                .dificultad("Media");
    }

    @Test
    @DisplayName("Debe aceptar una pregunta con todos los campos DCE diligenciados")
    void testValidQuestionHasNoErrors() {
        Question question = validQuestionBuilder().build();
        assertTrue(validator.validate(question).isEmpty());
    }

    @Test
    @DisplayName("Debe reportar errores por campos DCE faltantes (contexto, justificación, etc.)")
    void testMissingMandatoryFields() {
        Question question = new Question.Builder()
                .id("Q-2")
                .nombre("Título")
                .distractors(new QuestionDistractors("A", "B", "C", "D"))
                .respuestaCorrecta("A")
                .build();

        List<String> errors = validator.validate(question);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("contexto")));
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("justificación")));
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("bibliografía")));
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("tema")));
    }

    @Test
    @DisplayName("Debe rechazar distractores duplicados")
    void testDuplicatedDistractors() {
        Question question = validQuestionBuilder()
                .distractors(new QuestionDistractors("A", "A", "C", "D"))
                .build();

        List<String> errors = validator.validate(question);
        assertTrue(errors.stream().anyMatch(e -> e.contains("repetirse")));
    }

    @Test
    @DisplayName("Debe rechazar cuando la respuesta correcta no coincide con ninguna opción")
    void testCorrectAnswerNotInOptions() {
        Question question = validQuestionBuilder().respuestaCorrecta("Z").build();

        List<String> errors = validator.validate(question);
        assertTrue(errors.stream().anyMatch(e -> e.contains("coincidir")));
    }

    @Test
    @DisplayName("Debe rechazar cuando faltan las 4 opciones")
    void testMissingDistractors() {
        Question question = validQuestionBuilder().distractors(null).build();

        List<String> errors = validator.validate(question);
        assertTrue(errors.stream().anyMatch(e -> e.contains("distractores")));
    }
}
