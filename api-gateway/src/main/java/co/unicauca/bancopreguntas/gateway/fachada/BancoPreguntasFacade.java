package co.unicauca.bancopreguntas.gateway.fachada;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Patron Facade (GoF, estructural). Ofrece al frontend operaciones de alto nivel que por dentro
 * necesitan coordinar varios microservicios (usuarios, preguntas y revision). El cliente hace
 * una sola llamada y no conoce cuantos servicios intervienen ni en que orden.
 */
@Service
public class BancoPreguntasFacade {

    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private final ClientesMicroservicios clientes;

    public BancoPreguntasFacade(ClientesMicroservicios clientes) {
        this.clientes = clientes;
    }

    /**
     * Bandeja del revisor (T5.4): sus revisiones de revision-service junto con el contenido de cada
     * pregunta de preguntas-service, en una sola respuesta.
     */
    public Flux<JsonNode> asignacionesDeRevisor(Long revisorId) {
        return clientes.revisionesDeRevisor(revisorId)
                .concatMap(revision -> clientes.pregunta(revision.path("preguntaId").asLong())
                        .map(pregunta -> (JsonNode) combinar(revision, pregunta))
                        .onErrorResume(WebClientResponseException.NotFound.class,
                                e -> Mono.just(combinar(revision, JSON.nullNode()))));
    }

    /** Detalle de una pregunta con su historial de revisiones y el nombre de cada revisor (RF-19). */
    public Mono<JsonNode> detallePregunta(Long preguntaId) {
        Mono<JsonNode> pregunta = clientes.pregunta(preguntaId);
        Mono<List<JsonNode>> historial = clientes.historialDePregunta(preguntaId)
                .concatMap(this::conNombreDeRevisor)
                .collectList();
        return Mono.zip(pregunta, historial).map(t -> {
            ObjectNode detalle = JSON.objectNode();
            detalle.set("pregunta", t.getT1());
            detalle.set("revisiones", JSON.arrayNode().addAll(t.getT2()));
            return detalle;
        });
    }

    /**
     * Asignar revisores (HU-4) validando antes, contra los otros servicios, que la pregunta este
     * pendiente de revision y que cada usuario exista, este activo y tenga rol REVISOR.
     */
    public Flux<JsonNode> asignarRevisores(Long preguntaId, List<Long> revisorIds) {
        if (revisorIds == null || revisorIds.isEmpty()) {
            return Flux.error(new FachadaException(HttpStatus.BAD_REQUEST, "Debe elegir al menos un revisor."));
        }
        List<Long> unicos = List.copyOf(new LinkedHashSet<>(revisorIds));
        Mono<Void> preguntaPendiente = clientes.pregunta(preguntaId)
                .onErrorMap(WebClientResponseException.NotFound.class,
                        e -> new FachadaException(HttpStatus.NOT_FOUND, "No existe la pregunta " + preguntaId + "."))
                .flatMap(p -> "PENDIENTE_REVISION".equals(p.path("estado").asText())
                        ? Mono.<Void>empty()
                        : Mono.error(new FachadaException(HttpStatus.CONFLICT,
                        "Solo se asignan revisores a preguntas pendientes de revision; esta esta en "
                                + p.path("estadoNombre").asText() + ".")));
        Mono<Void> todosSonRevisores = Flux.fromIterable(unicos)
                .concatMap(id -> clientes.usuario(id)
                        .onErrorMap(WebClientResponseException.NotFound.class,
                                e -> new FachadaException(HttpStatus.BAD_REQUEST, "No existe el usuario " + id + "."))
                        .flatMap(u -> esRevisorActivo(u)
                                ? Mono.<Void>empty()
                                : Mono.error(new FachadaException(HttpStatus.BAD_REQUEST,
                                "El usuario " + u.path("login").asText() + " no es un revisor activo."))))
                .then();
        return preguntaPendiente.then(todosSonRevisores).thenMany(clientes.asignar(preguntaId, unicos));
    }

    private static boolean esRevisorActivo(JsonNode usuario) {
        return "REVISOR".equals(usuario.path("rol").asText()) && "ACTIVO".equals(usuario.path("estado").asText());
    }

    private Mono<JsonNode> conNombreDeRevisor(JsonNode revision) {
        ObjectNode copia = revision.deepCopy();
        return clientes.usuario(revision.path("revisorId").asLong())
                .map(u -> (JsonNode) copia.put("revisorNombre", u.path("nombreCompleto").asText()))
                .onErrorResume(e -> Mono.just(copia.put("revisorNombre", "Revisor " + revision.path("revisorId").asText())));
    }

    private static ObjectNode combinar(JsonNode revision, JsonNode pregunta) {
        ObjectNode combinado = JSON.objectNode();
        combinado.set("revision", revision);
        combinado.set("pregunta", pregunta);
        return combinado;
    }
}
