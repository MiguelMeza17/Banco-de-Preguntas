package co.unicauca.bancopreguntas.revision.domain;

/**
 * Se lanza cuando una operacion sobre una revision incumple una regla de negocio.
 */
public class ReglaRevisionException extends RuntimeException {

    public ReglaRevisionException(String mensaje) {
        super(mensaje);
    }
}
