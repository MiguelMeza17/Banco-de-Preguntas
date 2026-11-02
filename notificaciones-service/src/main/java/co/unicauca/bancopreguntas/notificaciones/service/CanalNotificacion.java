package co.unicauca.bancopreguntas.notificaciones.service;

import co.unicauca.bancopreguntas.notificaciones.domain.Notificacion;

/**
 * Canal externo por el que se entrega un aviso (correo, SMS...). El proyecto permite simular las
 * integraciones externas, asi que la implementacion por defecto solo registra el envio.
 */
public interface CanalNotificacion {

    void enviar(Notificacion notificacion);
}
