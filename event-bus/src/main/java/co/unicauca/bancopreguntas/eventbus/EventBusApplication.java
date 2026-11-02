package co.unicauca.bancopreguntas.eventbus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bus de eventos del sistema: levanta un broker ActiveMQ Artemis embebido que escucha por TCP
 * para que los microservicios publiquen y consuman eventos de dominio de forma asincrona.
 * <p>
 * Se usa un broker embebido para que el sistema completo se pueda ejecutar solo con Java,
 * sin instalar RabbitMQ, Kafka ni Docker. En produccion se reemplaza por un broker Artemis
 * independiente cambiando unicamente la URL de conexion de cada servicio.
 */
@SpringBootApplication
public class EventBusApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventBusApplication.class, args);
    }
}
