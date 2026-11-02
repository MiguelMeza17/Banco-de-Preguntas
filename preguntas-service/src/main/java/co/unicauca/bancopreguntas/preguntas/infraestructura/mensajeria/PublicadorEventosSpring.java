package co.unicauca.bancopreguntas.preguntas.infraestructura.mensajeria;

import co.unicauca.bancopreguntas.preguntas.evento.EventoIntegracion;
import co.unicauca.bancopreguntas.preguntas.evento.PublicadorEventos;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publica el evento dentro del proceso; {@link ReenvioEventosAlBus} lo reenvia al bus cuando la
 * transaccion se confirma, para no anunciar cambios que luego se deshacen.
 */
@Component
public class PublicadorEventosSpring implements PublicadorEventos {

    private final ApplicationEventPublisher publisher;

    public PublicadorEventosSpring(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(EventoIntegracion evento) {
        publisher.publishEvent(evento);
    }
}
