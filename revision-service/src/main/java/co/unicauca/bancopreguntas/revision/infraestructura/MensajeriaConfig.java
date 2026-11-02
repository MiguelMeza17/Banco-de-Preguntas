package co.unicauca.bancopreguntas.revision.infraestructura;

import co.unicauca.bancopreguntas.revision.evento.PreguntaEvaluada;
import co.unicauca.bancopreguntas.revision.evento.RevisoresAsignados;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

/** Mensajes JSON con el nombre logico del evento en la cabecera {@code _tipo}. */
@Configuration
public class MensajeriaConfig {

    @Bean
    public MessageConverter convertidorJson(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter convertidor = new MappingJackson2MessageConverter();
        convertidor.setTargetType(MessageType.TEXT);
        convertidor.setTypeIdPropertyName("_tipo");
        convertidor.setTypeIdMappings(Map.of(
                "RevisoresAsignados", RevisoresAsignados.class,
                "PreguntaEvaluada", PreguntaEvaluada.class));
        convertidor.setObjectMapper(objectMapper);
        return convertidor;
    }

    @Bean
    public OpenAPI apiRevisiones() {
        return new OpenAPI().info(new Info()
                .title("revision-service")
                .version("0.1.0")
                .description("Asignacion de revisores y evaluacion por pares de las preguntas."));
    }
}
