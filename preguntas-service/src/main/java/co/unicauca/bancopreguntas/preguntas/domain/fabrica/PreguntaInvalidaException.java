package co.unicauca.bancopreguntas.preguntas.domain.fabrica;

import co.unicauca.bancopreguntas.preguntas.domain.ReglaNegocioException;

import java.util.List;

/** La pregunta no supera la validacion estructural; trae todos los errores encontrados. */
public class PreguntaInvalidaException extends ReglaNegocioException {

    private final List<String> errores;

    public PreguntaInvalidaException(List<String> errores) {
        super("La pregunta no cumple la validacion estructural: " + String.join(" ", errores));
        this.errores = List.copyOf(errores);
    }

    public List<String> getErrores() {
        return errores;
    }
}
