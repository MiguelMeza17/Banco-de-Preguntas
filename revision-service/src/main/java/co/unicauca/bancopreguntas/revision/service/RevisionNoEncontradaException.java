package co.unicauca.bancopreguntas.revision.service;

public class RevisionNoEncontradaException extends RuntimeException {

    public RevisionNoEncontradaException(Long id) {
        super("No existe la revision " + id + ".");
    }
}
