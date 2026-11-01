package co.unicauca.bancopreguntas.revision.infraestructura;

import co.unicauca.bancopreguntas.revision.evento.PublicadorEventos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementacion del {@link PublicadorEventos} con los eventos de aplicacion de Spring.
 * <p>
 * Permite que los oyentes ({@code @EventListener}) reaccionen dentro del mismo proceso
 * mientras se integra el bus de eventos entre servicios.
 */
@Component
public class PublicadorEventosSpring implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorEventosSpring.class);

    private final ApplicationEventPublisher publisher;

    public PublicadorEventosSpring(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(Object evento) {
        log.info("Evento publicado: {}", evento);
        publisher.publishEvent(evento);
    }
}
