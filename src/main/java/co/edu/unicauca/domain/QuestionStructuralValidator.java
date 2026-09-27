package co.edu.unicauca.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuestionStructuralValidator implements IQuestionValidator {

    @Override
    public List<String> validate(Question question) {
        List<String> errors = new ArrayList<>();

        if (question == null) {
            errors.add("La pregunta no puede ser nula.");
            return errors;
        }

        if (isBlank(question.getNombre())) {
            errors.add("El título de la pregunta es obligatorio.");
        }
        if (isBlank(question.getContexto())) {
            errors.add("El contexto es obligatorio.");
        }
        if (isBlank(question.getPregunta())) {
            errors.add("La pregunta directa es obligatoria.");
        }
        if (isBlank(question.getJustificacion())) {
            errors.add("La justificación de la respuesta es obligatoria.");
        }
        if (isBlank(question.getBibliografia())) {
            errors.add("La bibliografía es obligatoria.");
        }
        if (isBlank(question.getCompetencia())) {
            errors.add("La competencia es obligatoria.");
        }
        if (isBlank(question.getTema())) {
            errors.add("El tema es obligatorio.");
        }
        if (isBlank(question.getSubtema())) {
            errors.add("El subtema es obligatorio.");
        }
        if (isBlank(question.getDificultad())) {
            errors.add("El nivel de dificultad es obligatorio.");
        }

        validateDistractors(question, errors);

        return errors;
    }

    private void validateDistractors(Question question, List<String> errors) {
        QuestionDistractors d = question.getDistractors();
        if (d == null) {
            errors.add("Debe definir las 4 opciones de respuesta (distractores).");
            return;
        }

        List<String> options = List.of(
                nullToEmpty(d.getOptionA()),
                nullToEmpty(d.getOptionB()),
                nullToEmpty(d.getOptionC()),
                nullToEmpty(d.getOptionD())
        );

        for (int i = 0; i < options.size(); i++) {
            if (isBlank(options.get(i))) {
                errors.add("La opción " + (char) ('A' + i) + " no puede estar vacía.");
            }
        }

        Set<String> unique = new HashSet<>();
        boolean hasDuplicates = false;
        for (String option : options) {
            if (!isBlank(option) && !unique.add(option.trim().toLowerCase())) {
                hasDuplicates = true;
            }
        }
        if (hasDuplicates) {
            errors.add("Las 4 opciones no pueden repetirse.");
        }

        if (isBlank(question.getRespuestaCorrecta())) {
            errors.add("Debe indicar cuál es la respuesta correcta.");
        } else {
            boolean matches = options.stream()
                    .anyMatch(o -> !isBlank(o) && o.trim().equalsIgnoreCase(question.getRespuestaCorrecta().trim()));
            if (!matches) {
                errors.add("La respuesta correcta debe coincidir exactamente con una de las 4 opciones.");
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
