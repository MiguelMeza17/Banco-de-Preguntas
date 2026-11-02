package co.unicauca.bancopreguntas.notificaciones.infraestructura;

import co.unicauca.bancopreguntas.notificaciones.evento.Eventos;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
 * Mensajes JSON con el nombre logico del evento en {@code _tipo} y suscripciones durables
 * compartidas: si el servicio esta caido los avisos esperan en el bus, y varias instancias se
 * reparten la carga.
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
                "PreguntaCreada", Eventos.PreguntaCreada.class,
                "PreguntaEnviadaARevision", Eventos.PreguntaEnviadaARevision.class,
                "RevisoresAsignados", Eventos.RevisoresAsignados.class,
                "PreguntaEvaluada", Eventos.PreguntaEvaluada.class));
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

    @Bean
    public OpenAPI apiNotificaciones() {
        return new OpenAPI().info(new Info()
                .title("notificaciones-service")
                .version("0.1.0")
                .description("Bandeja de avisos generados a partir de los eventos del sistema."));
    }
}
