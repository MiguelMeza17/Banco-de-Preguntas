package co.unicauca.bancopreguntas.notificaciones.service;

import co.unicauca.bancopreguntas.notificaciones.domain.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Correo simulado: deja el envio en el log en lugar de usar un servidor SMTP real. */
@Component
public class CanalCorreoSimulado implements CanalNotificacion {

    private static final Logger log = LoggerFactory.getLogger(CanalCorreoSimulado.class);

    @Override
    public void enviar(Notificacion n) {
        String destino = n.getUsuarioId() != null ? "usuario " + n.getUsuarioId() : "rol " + n.getRol();
        log.info("[correo simulado] Para {}: {} - {}", destino, n.getTitulo(), n.getMensaje());
    }
}
