package co.unicauca.bancopreguntas.revision.evento;

/**
 * Puerto de salida para publicar eventos de dominio (patron Observer).
 * <p>
 * Los servicios dependen de esta interfaz y no de la tecnologia de mensajeria: cuando el bus
 * de eventos del sistema este disponible basta con agregar otra implementacion.
 */
public interface PublicadorEventos {

    void publicar(Object evento);
}
