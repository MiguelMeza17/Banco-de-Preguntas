package co.unicauca.bancopreguntas.gateway.fachada;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la fachada simulando las respuestas HTTP de los tres microservicios, sin levantarlos.
 */
class BancoPreguntasFacadeTest {

    private final Map<String, String> respuestas = new HashMap<>();
    private final List<String> llamadas = new ArrayList<>();
    private BancoPreguntasFacade fachada;

    @BeforeEach
    void setUp() {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
            String clave = request.method() + " " + request.url().getHost() + ":" + request.url().getPort()
                    + request.url().getPath() + (request.url().getQuery() == null ? "" : "?" + request.url().getQuery());
            llamadas.add(clave);
            String cuerpo = respuestas.get(clave);
            if (cuerpo == null) {
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            }
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).body(cuerpo).build());
        });
        fachada = new BancoPreguntasFacade(new ClientesMicroservicios(builder,
                "http://usuarios:1", "http://preguntas:2", "http://revision:3"));

        respuestas.put("GET preguntas:2/preguntas/10",
                "{\"id\":10,\"titulo\":\"Graficas\",\"estado\":\"PENDIENTE_REVISION\",\"estadoNombre\":\"Pendiente de revision\"}");
        respuestas.put("GET preguntas:2/preguntas/11",
                "{\"id\":11,\"titulo\":\"Algebra\",\"estado\":\"BORRADOR\",\"estadoNombre\":\"Borrador\"}");
        respuestas.put("GET usuarios:1/usuarios/3",
                "{\"id\":3,\"login\":\"revisor1\",\"nombreCompleto\":\"Revisor Uno\",\"rol\":\"REVISOR\",\"estado\":\"ACTIVO\"}");
        respuestas.put("GET usuarios:1/usuarios/2",
                "{\"id\":2,\"login\":\"autor\",\"nombreCompleto\":\"Autora\",\"rol\":\"AUTOR\",\"estado\":\"ACTIVO\"}");
        respuestas.put("POST revision:3/revisiones/asignar", "[{\"id\":100,\"preguntaId\":10,\"revisorId\":3}]");
        respuestas.put("GET revision:3/revisiones/mis-asignadas?revisorId=3",
                "[{\"id\":100,\"preguntaId\":10,\"revisorId\":3,\"estado\":\"ASIGNADA\"}]");
        respuestas.put("GET revision:3/revisiones?preguntaId=10",
                "[{\"id\":100,\"preguntaId\":10,\"revisorId\":3,\"estado\":\"APROBADA\"}]");
    }

    @Test
    void asignaCuandoLaPreguntaEstaPendienteYLosUsuariosSonRevisores() {
        StepVerifier.create(fachada.asignarRevisores(10L, List.of(3L, 3L)))
                .assertNext(r -> assertThat(r.path("id").asLong()).isEqualTo(100))
                .verifyComplete();
        assertThat(llamadas).contains("POST revision:3/revisiones/asignar");
    }

    @Test
    void noAsignaSiLaPreguntaNoEstaPendiente() {
        StepVerifier.create(fachada.asignarRevisores(11L, List.of(3L)))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(FachadaException.class);
                    assertThat(((FachadaException) e).getEstado()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
        assertThat(llamadas).doesNotContain("POST revision:3/revisiones/asignar");
    }

    @Test
    void noAsignaAUnUsuarioQueNoEsRevisor() {
        StepVerifier.create(fachada.asignarRevisores(10L, List.of(2L)))
                .expectErrorMatches(e -> e.getMessage().contains("no es un revisor"))
                .verify();
        assertThat(llamadas).doesNotContain("POST revision:3/revisiones/asignar");
    }

    @Test
    void preguntaInexistenteResponde404() {
        StepVerifier.create(fachada.asignarRevisores(99L, List.of(3L)))
                .expectErrorSatisfies(e -> assertThat(((FachadaException) e).getEstado()).isEqualTo(HttpStatus.NOT_FOUND))
                .verify();
    }

    @Test
    void laBandejaDelRevisorCombinaRevisionYPregunta() {
        StepVerifier.create(fachada.asignacionesDeRevisor(3L))
                .assertNext(item -> {
                    assertThat(item.path("revision").path("estado").asText()).isEqualTo("ASIGNADA");
                    assertThat(item.path("pregunta").path("titulo").asText()).isEqualTo("Graficas");
                })
                .verifyComplete();
    }

    @Test
    void elDetalleIncluyeElHistorialConElNombreDelRevisor() {
        StepVerifier.create(fachada.detallePregunta(10L))
                .assertNext(d -> {
                    assertThat(d.path("pregunta").path("id").asLong()).isEqualTo(10);
                    JsonNode revision = d.path("revisiones").get(0);
                    assertThat(revision.path("revisorNombre").asText()).isEqualTo("Revisor Uno");
                })
                .verifyComplete();
    }
}
