package co.unicauca.bancopreguntas.preguntas.service;

public class PreguntaNoEncontradaException extends RuntimeException {

    public PreguntaNoEncontradaException(Long id) {
        super("No existe la pregunta " + id + ".");
    }
}
