package co.unicauca.bancopreguntas.preguntas.domain.estado;

import co.unicauca.bancopreguntas.preguntas.domain.ReglaNegocioException;

/** La accion solicitada no es valida en el estado actual de la pregunta (RF-15). */
public class TransicionInvalidaException extends ReglaNegocioException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
