package co.unicauca.bancopreguntas.revision.infraestructura;

import co.unicauca.bancopreguntas.revision.evento.EventoIntegracion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reenvia al bus de eventos los eventos de integracion despues del commit. El servicio de
 * notificaciones y preguntas-service los consumen (RevisoresAsignados, PreguntaEvaluada).
 */
@Component
public class ReenvioEventosAlBus {

    private static final Logger log = LoggerFactory.getLogger(ReenvioEventosAlBus.class);

    private final JmsTemplate jmsTemplate;

    public ReenvioEventosAlBus(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void reenviar(EventoIntegracion evento) {
        try {
            jmsTemplate.convertAndSend(evento.topico(), evento);
            log.info("Evento publicado en {}: {}", evento.topico(), evento);
        } catch (JmsException e) {
            log.error("No se pudo publicar {} en el bus: {}", evento, e.getMessage());
        }
    }
}
