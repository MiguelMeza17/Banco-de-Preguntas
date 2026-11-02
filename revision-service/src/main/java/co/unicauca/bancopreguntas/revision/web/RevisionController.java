package co.unicauca.bancopreguntas.revision.web;

import co.unicauca.bancopreguntas.revision.domain.EstadoRevision;
import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.service.AsignacionRevisionService;
import co.unicauca.bancopreguntas.revision.service.ConsultaRevisionService;
import co.unicauca.bancopreguntas.revision.service.EvaluacionRevisionService;
import co.unicauca.bancopreguntas.revision.web.dto.AsignarRevisionRequest;
import co.unicauca.bancopreguntas.revision.web.dto.EvaluarRevisionRequest;
import co.unicauca.bancopreguntas.revision.web.dto.IniciarRevisionRequest;
import co.unicauca.bancopreguntas.revision.web.dto.RevisionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/revisiones")
@Tag(name = "Revisiones", description = "Asignacion de revisores y evaluacion por pares")
public class RevisionController {

    private final AsignacionRevisionService asignacionRevisionService;
    private final EvaluacionRevisionService evaluacionRevisionService;
    private final ConsultaRevisionService consultaRevisionService;

    public RevisionController(AsignacionRevisionService asignacionRevisionService,
                              EvaluacionRevisionService evaluacionRevisionService,
                              ConsultaRevisionService consultaRevisionService) {
        this.asignacionRevisionService = asignacionRevisionService;
        this.evaluacionRevisionService = evaluacionRevisionService;
        this.consultaRevisionService = consultaRevisionService;
    }

    @PostMapping("/asignar")
    @Operation(summary = "Asignar revisores a una pregunta (HU-4)", description = "Publica RevisoresAsignados.")
    public ResponseEntity<List<RevisionResponse>> asignar(@RequestBody AsignarRevisionRequest request) {
        List<Revision> asignaciones = asignacionRevisionService.asignarRevisores(
                request.preguntaId(), request.revisorIds());
        List<RevisionResponse> response = asignaciones.stream()
                .map(RevisionResponse::from)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mis-asignadas")
    @Operation(summary = "Preguntas asignadas a un revisor (T5.2)",
            description = "Sin estado devuelve primero las pendientes de evaluar.")
    public List<RevisionResponse> misAsignadas(@RequestParam Long revisorId,
                                               @RequestParam(required = false) EstadoRevision estado) {
        return consultaRevisionService.asignadasA(revisorId, estado).stream().map(RevisionResponse::from).toList();
    }

    @GetMapping
    @Operation(summary = "Historial de revisiones de una pregunta (RF-19)")
    public List<RevisionResponse> historial(@RequestParam Long preguntaId) {
        return consultaRevisionService.historialDePregunta(preguntaId).stream().map(RevisionResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una revision")
    public RevisionResponse buscar(@PathVariable Long id) {
        return RevisionResponse.from(consultaRevisionService.buscar(id));
    }

    @PostMapping("/{id}/iniciar")
    @Operation(summary = "El revisor abre la pregunta: ASIGNADA pasa a EN_REVISION")
    public RevisionResponse iniciar(@PathVariable Long id, @RequestBody IniciarRevisionRequest request) {
        return RevisionResponse.from(consultaRevisionService.iniciar(id, request.revisorId()));
    }

    /**
     * El revisor aprueba o rechaza la pregunta de la revision indicada.
     * Responde 200 con la revision evaluada, 404 si no existe y 409 si se incumple una regla.
     */
    @PostMapping("/{id}/evaluar")
    @Operation(summary = "Aprobar o rechazar con observaciones (T5.3)", description = "Publica PreguntaEvaluada.")
    public ResponseEntity<RevisionResponse> evaluar(@PathVariable Long id,
                                                    @RequestBody EvaluarRevisionRequest request) {
        Revision evaluada = evaluacionRevisionService.evaluar(
                id, request.revisorId(), request.resultado(), request.observaciones());
        return ResponseEntity.ok(RevisionResponse.from(evaluada));
    }
}
