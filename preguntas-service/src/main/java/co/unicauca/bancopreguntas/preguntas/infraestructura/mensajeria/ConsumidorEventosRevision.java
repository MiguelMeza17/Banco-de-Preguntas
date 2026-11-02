package co.unicauca.bancopreguntas.preguntas.infraestructura.mensajeria;

import co.unicauca.bancopreguntas.preguntas.domain.ReglaNegocioException;
import co.unicauca.bancopreguntas.preguntas.evento.PreguntaEvaluada;
import co.unicauca.bancopreguntas.preguntas.evento.RevisoresAsignados;
import co.unicauca.bancopreguntas.preguntas.evento.Topicos;
import co.unicauca.bancopreguntas.preguntas.service.PreguntaNoEncontradaException;
import co.unicauca.bancopreguntas.preguntas.service.PreguntaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Observador de los eventos del contexto de Revision: mantiene el estado de la pregunta
 * sincronizado con el proceso de revision por pares, sin llamadas directas entre servicios.
 */
@Component
public class ConsumidorEventosRevision {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorEventosRevision.class);

    private final PreguntaService service;

    public ConsumidorEventosRevision(PreguntaService service) {
        this.service = service;
    }

    @JmsListener(destination = Topicos.REVISORES_ASIGNADOS,
            subscription = "preguntas-service.revisores-asignados",
            containerFactory = MensajeriaConfig.FABRICA_SUSCRIPCIONES)
    public void alAsignarRevisores(RevisoresAsignados evento) {
        log.info("Recibido RevisoresAsignados: {}", evento);
        ejecutar(() -> service.registrarInicioRevision(evento.preguntaId()));
    }

    @JmsListener(destination = Topicos.PREGUNTA_EVALUADA,
            subscription = "preguntas-service.pregunta-evaluada",
            containerFactory = MensajeriaConfig.FABRICA_SUSCRIPCIONES)
    public void alEvaluarPregunta(PreguntaEvaluada evento) {
        log.info("Recibido PreguntaEvaluada: {}", evento);
        if (evento.revisionCompleta()) {
            ejecutar(() -> service.registrarDictamen(evento.preguntaId(), evento.resultadoPregunta()));
        }
    }

    /** Los errores de negocio no se reintentan: el mensaje no se volveria valido. */
    private void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (PreguntaNoEncontradaException | ReglaNegocioException e) {
            log.warn("Evento ignorado: {}", e.getMessage());
        }
    }
}
