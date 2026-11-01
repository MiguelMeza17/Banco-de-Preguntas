package co.unicauca.bancopreguntas.revision.web;

import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.service.AsignacionRevisionService;
import co.unicauca.bancopreguntas.revision.service.EvaluacionRevisionService;
import co.unicauca.bancopreguntas.revision.web.dto.AsignarRevisionRequest;
import co.unicauca.bancopreguntas.revision.web.dto.EvaluarRevisionRequest;
import co.unicauca.bancopreguntas.revision.web.dto.RevisionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/revisiones")
public class RevisionController {

    private final AsignacionRevisionService asignacionRevisionService;
    private final EvaluacionRevisionService evaluacionRevisionService;

    public RevisionController(AsignacionRevisionService asignacionRevisionService,
                              EvaluacionRevisionService evaluacionRevisionService) {
        this.asignacionRevisionService = asignacionRevisionService;
        this.evaluacionRevisionService = evaluacionRevisionService;
    }

    @PostMapping("/asignar")
    public ResponseEntity<List<RevisionResponse>> asignar(@RequestBody AsignarRevisionRequest request) {
        List<Revision> asignaciones = asignacionRevisionService.asignarRevisores(
                request.preguntaId(), request.revisorIds());
        List<RevisionResponse> response = asignaciones.stream()
                .map(RevisionResponse::from)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * El revisor aprueba o rechaza la pregunta de la revision indicada.
     * Responde 200 con la revision evaluada, 404 si no existe y 409 si se incumple una regla.
     */
    @PostMapping("/{id}/evaluar")
    public ResponseEntity<RevisionResponse> evaluar(@PathVariable Long id,
                                                    @RequestBody EvaluarRevisionRequest request) {
        Revision evaluada = evaluacionRevisionService.evaluar(
                id, request.revisorId(), request.resultado(), request.observaciones());
        return ResponseEntity.ok(RevisionResponse.from(evaluada));
    }
}
