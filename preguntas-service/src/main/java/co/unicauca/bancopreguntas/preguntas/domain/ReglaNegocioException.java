package co.unicauca.bancopreguntas.preguntas.domain;

/** Se lanza cuando una operacion incumple una regla de negocio del banco de preguntas. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
