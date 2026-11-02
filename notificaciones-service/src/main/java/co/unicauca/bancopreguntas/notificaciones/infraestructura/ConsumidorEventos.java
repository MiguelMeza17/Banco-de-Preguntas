package co.unicauca.bancopreguntas.notificaciones.infraestructura;

import co.unicauca.bancopreguntas.notificaciones.evento.Eventos;
import co.unicauca.bancopreguntas.notificaciones.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/** Observador de los eventos del bus: traduce cada evento en avisos. */
@Component
public class ConsumidorEventos {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorEventos.class);

    private final NotificacionService service;

    public ConsumidorEventos(NotificacionService service) {
        this.service = service;
    }

    @JmsListener(destination = Eventos.PREGUNTA_CREADA, subscription = "notificaciones-service.pregunta-creada",
            containerFactory = MensajeriaConfig.FABRICA_SUSCRIPCIONES)
    public void preguntaCreada(Eventos.PreguntaCreada e) {
        log.info("Recibido PreguntaCreada: {}", e);
        service.alCrearPregunta(e);
    }

    @JmsListener(destination = Eventos.PREGUNTA_ENVIADA_A_REVISION,
            subscription = "notificaciones-service.enviada-revision",
            containerFactory = MensajeriaConfig.FABRICA_SUSCRIPCIONES)
    public void preguntaEnviada(Eventos.PreguntaEnviadaARevision e) {
        log.info("Recibido PreguntaEnviadaARevision: {}", e);
        service.alEnviarARevision(e);
    }

    @JmsListener(destination = Eventos.REVISORES_ASIGNADOS,
            subscription = "notificaciones-service.revisores-asignados",
            containerFactory = MensajeriaConfig.FABRICA_SUSCRIPCIONES)
    public void revisoresAsignados(Eventos.RevisoresAsignados e) {
        log.info("Recibido RevisoresAsignados: {}", e);
        service.alAsignarRevisores(e);
    }

    @JmsListener(destination = Eventos.PREGUNTA_EVALUADA,
            subscription = "notificaciones-service.pregunta-evaluada",
            containerFactory = MensajeriaConfig.FABRICA_SUSCRIPCIONES)
    public void preguntaEvaluada(Eventos.PreguntaEvaluada e) {
        log.info("Recibido PreguntaEvaluada: {}", e);
        service.alEvaluarPregunta(e);
    }
}
