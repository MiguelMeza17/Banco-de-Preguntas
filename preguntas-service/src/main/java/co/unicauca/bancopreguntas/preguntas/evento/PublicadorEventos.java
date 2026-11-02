package co.unicauca.bancopreguntas.preguntas.evento;

/**
 * Puerto de salida para publicar eventos de dominio (patron Observer). El servicio no conoce
 * la tecnologia del bus.
 */
public interface PublicadorEventos {

    void publicar(EventoIntegracion evento);
}
