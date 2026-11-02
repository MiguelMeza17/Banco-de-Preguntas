package co.unicauca.bancopreguntas.revision.evento;

/**
 * Puerto de salida para publicar eventos de dominio (patron Observer).
 * <p>
 * Los servicios dependen de esta interfaz y no de la tecnologia de mensajeria.
 */
public interface PublicadorEventos {

    void publicar(EventoIntegracion evento);
}
