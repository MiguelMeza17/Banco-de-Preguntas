package co.unicauca.bancopreguntas.gateway.fachada;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Subsistemas que oculta la fachada: un cliente HTTP por microservicio. Trabaja con
 * {@link JsonNode} para no duplicar en el gateway las clases de cada servicio.
 */
@Component
public class ClientesMicroservicios {

    private final WebClient usuarios;
    private final WebClient preguntas;
    private final WebClient revision;

    public ClientesMicroservicios(WebClient.Builder builder,
                                  @Value("${servicios.usuarios}") String usuarios,
                                  @Value("${servicios.preguntas}") String preguntas,
                                  @Value("${servicios.revision}") String revision) {
        this.usuarios = builder.clone().baseUrl(usuarios).build();
        this.preguntas = builder.clone().baseUrl(preguntas).build();
        this.revision = builder.clone().baseUrl(revision).build();
    }

    public Mono<JsonNode> pregunta(Long id) {
        return preguntas.get().uri("/preguntas/{id}", id).retrieve().bodyToMono(JsonNode.class);
    }

    public Mono<JsonNode> usuario(Long id) {
        return usuarios.get().uri("/usuarios/{id}", id).retrieve().bodyToMono(JsonNode.class);
    }

    public Flux<JsonNode> revisionesDeRevisor(Long revisorId) {
        return revision.get().uri(u -> u.path("/revisiones/mis-asignadas").queryParam("revisorId", revisorId).build())
                .retrieve().bodyToFlux(JsonNode.class);
    }

    public Flux<JsonNode> historialDePregunta(Long preguntaId) {
        return revision.get().uri(u -> u.path("/revisiones").queryParam("preguntaId", preguntaId).build())
                .retrieve().bodyToFlux(JsonNode.class);
    }

    public Flux<JsonNode> asignar(Long preguntaId, List<Long> revisorIds) {
        return revision.post().uri("/revisiones/asignar")
                .bodyValue(Map.of("preguntaId", preguntaId, "revisorIds", revisorIds))
                .retrieve().bodyToFlux(JsonNode.class);
    }
}
