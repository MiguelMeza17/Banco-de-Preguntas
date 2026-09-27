package co.edu.unicauca.presentation;

import co.edu.unicauca.domain.Question;

import java.awt.Color;

/**
 * Colores estándar para representar visualmente el estado de una pregunta (HU02).
 */
public final class QuestionStateColors {

    private QuestionStateColors() {
    }

    public static Color colorFor(String estado) {
        if (Question.STATE_BORRADOR.equals(estado)) {
            return new Color(37, 99, 235); // Azul
        }
        if (Question.STATE_PENDIENTE_REVISION.equals(estado)) {
            return new Color(217, 119, 6); // Ámbar
        }
        if (Question.STATE_ELIMINADA.equals(estado)) {
            return new Color(220, 38, 38); // Rojo
        }
        return Color.DARK_GRAY;
    }
}
