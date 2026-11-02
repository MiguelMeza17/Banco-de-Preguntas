package co.unicauca.bancopreguntas.preguntas.evento;

/** Evento de dominio que se reenvia al bus para que lo consuman otros servicios. */
public interface EventoIntegracion {

    /** Topico del bus donde se publica. */
    String topico();
}
