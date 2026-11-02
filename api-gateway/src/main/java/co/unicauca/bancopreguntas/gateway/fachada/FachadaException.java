package co.unicauca.bancopreguntas.gateway.fachada;

import org.springframework.http.HttpStatus;

/** Regla de la operacion compuesta que no se cumple; indica el estado HTTP a responder. */
public class FachadaException extends RuntimeException {

    private final HttpStatus estado;

    public FachadaException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
