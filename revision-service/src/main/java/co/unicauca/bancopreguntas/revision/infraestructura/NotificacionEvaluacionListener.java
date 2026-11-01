package co.unicauca.bancopreguntas.revision.infraestructura;

import co.unicauca.bancopreguntas.revision.evento.PreguntaEvaluada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Observador de {@link PreguntaEvaluada}: genera el aviso para el autor y el administrador.
 * <p>
 * Se ejecuta solo si la transaccion de la evaluacion se confirma, asi nunca se notifica una
 * evaluacion que no quedo guardada. Por ahora registra el aviso en el log; cuando el bus de
 * eventos (#33) este disponible, el servicio de notificaciones consumira el mismo evento.
 */
@Component
public class NotificacionEvaluacionListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEvaluacionListener.class);

    @TransactionalEventListener
    public void alEvaluarPregunta(PreguntaEvaluada evento) {
        evento.destinatarios().forEach(destinatario -> log.info(
                "Notificar a {} de la pregunta {}: {} por el revisor {}{}",
                destinatario,
                evento.preguntaId(),
                evento.resultado(),
                evento.revisorId(),
                evento.observaciones() == null ? "" : " - Observaciones: " + evento.observaciones()));
    }
}
