package co.unicauca.bancopreguntas.preguntas.infraestructura.mensajeria;

import co.unicauca.bancopreguntas.preguntas.evento.PreguntaCreada;
import co.unicauca.bancopreguntas.preguntas.evento.PreguntaEnviadaARevision;
import co.unicauca.bancopreguntas.preguntas.evento.PreguntaEvaluada;
import co.unicauca.bancopreguntas.preguntas.evento.RevisoresAsignados;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

/**
 * Configuracion del bus de eventos: mensajes JSON con el nombre logico del evento en la cabecera
 * {@code _tipo}, y suscripciones durables compartidas para que varias instancias del servicio se
 * repartan los mensajes y ninguno se pierda si el servicio esta caido.
 */
@Configuration
@EnableJms
public class MensajeriaConfig {

    public static final String FABRICA_SUSCRIPCIONES = "fabricaSuscripciones";

    @Bean
    public MessageConverter convertidorJson(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter convertidor = new MappingJackson2MessageConverter();
        convertidor.setTargetType(MessageType.TEXT);
        convertidor.setTypeIdPropertyName("_tipo");
        convertidor.setTypeIdMappings(Map.of(
                "PreguntaCreada", PreguntaCreada.class,
                "PreguntaEnviadaARevision", PreguntaEnviadaARevision.class,
                "RevisoresAsignados", RevisoresAsignados.class,
                "PreguntaEvaluada", PreguntaEvaluada.class));
        convertidor.setObjectMapper(objectMapper);
        return convertidor;
    }

    @Bean(name = FABRICA_SUSCRIPCIONES)
    public DefaultJmsListenerContainerFactory fabricaSuscripciones(ConnectionFactory connectionFactory,
                                                                   DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory fabrica = new DefaultJmsListenerContainerFactory();
        configurer.configure(fabrica, connectionFactory);
        fabrica.setPubSubDomain(true);
        fabrica.setSubscriptionDurable(true);
        fabrica.setSubscriptionShared(true);
        fabrica.setSessionTransacted(true);
        return fabrica;
    }
}
