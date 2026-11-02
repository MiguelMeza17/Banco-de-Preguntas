package co.unicauca.bancopreguntas.notificaciones.web;

import co.unicauca.bancopreguntas.notificaciones.domain.Notificacion;
import co.unicauca.bancopreguntas.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    public record NotificacionResponse(Long id, String tipoEvento, Long preguntaId, String titulo, String mensaje,
                                       boolean leida, Instant fecha) {
        static NotificacionResponse from(Notificacion n) {
            return new NotificacionResponse(n.getId(), n.getTipoEvento(), n.getPreguntaId(), n.getTitulo(),
                    n.getMensaje(), n.isLeida(), n.getFecha());
        }
    }

    @GetMapping
    @Operation(summary = "Bandeja de un usuario: sus avisos y los dirigidos a su rol")
    public List<NotificacionResponse> bandeja(@RequestParam Long usuarioId,
                                              @RequestParam(required = false) String rol) {
        return service.bandeja(usuarioId, rol).stream().map(NotificacionResponse::from).toList();
    }

    @PatchMapping("/{id}/leida")
    @Operation(summary = "Marcar un aviso como leido")
    public NotificacionResponse marcarLeida(@PathVariable Long id) {
        return NotificacionResponse.from(service.marcarLeida(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail noEncontrada(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
