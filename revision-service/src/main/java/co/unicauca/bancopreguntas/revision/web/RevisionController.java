package co.unicauca.bancopreguntas.revision.web;

import co.unicauca.bancopreguntas.revision.domain.Revision;
import co.unicauca.bancopreguntas.revision.service.AsignacionRevisionService;
import co.unicauca.bancopreguntas.revision.web.dto.AsignarRevisionRequest;
import co.unicauca.bancopreguntas.revision.web.dto.RevisionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/revisiones")
public class RevisionController {

    private final AsignacionRevisionService asignacionRevisionService;

    public RevisionController(AsignacionRevisionService asignacionRevisionService) {
        this.asignacionRevisionService = asignacionRevisionService;
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
}
