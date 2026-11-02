package co.unicauca.bancopreguntas.gateway.fachada;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/** Endpoints de la fachada; toda la logica de coordinacion vive en {@link BancoPreguntasFacade}. */
@RestController
@RequestMapping("/api/fachada")
public class FachadaController {

    private final BancoPreguntasFacade fachada;

    public FachadaController(BancoPreguntasFacade fachada) {
        this.fachada = fachada;
    }

    public record AsignarRequest(List<Long> revisorIds) {
    }

    @GetMapping("/revisores/{revisorId}/asignaciones")
    public Flux<JsonNode> asignaciones(@PathVariable Long revisorId) {
        return fachada.asignacionesDeRevisor(revisorId);
    }

    @GetMapping("/preguntas/{id}/detalle")
    public Mono<JsonNode> detalle(@PathVariable Long id) {
        return fachada.detallePregunta(id);
    }

    @PostMapping("/preguntas/{id}/revisores")
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<JsonNode> asignar(@PathVariable Long id, @RequestBody AsignarRequest request) {
        return fachada.asignarRevisores(id, request.revisorIds());
    }

    @ExceptionHandler(FachadaException.class)
    public ResponseEntity<ProblemDetail> regla(FachadaException e) {
        return ResponseEntity.status(e.getEstado()).body(ProblemDetail.forStatusAndDetail(e.getEstado(), e.getMessage()));
    }

    /** Si un microservicio responde con error, se reenvia su estado y su mensaje. */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> errorDeServicio(WebClientResponseException e) {
        return ResponseEntity.status(e.getStatusCode())
                .header("Content-Type", "application/problem+json")
                .body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ProblemDetail> servicioCaido(WebClientRequestException e) {
        HttpStatus estado = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(estado).body(ProblemDetail.forStatusAndDetail(estado,
                "Un microservicio no esta disponible: " + e.getUri()));
    }
}
