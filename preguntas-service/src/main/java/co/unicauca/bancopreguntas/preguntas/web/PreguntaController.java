package co.unicauca.bancopreguntas.preguntas.web;

import co.unicauca.bancopreguntas.preguntas.domain.Dificultad;
import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;
import co.unicauca.bancopreguntas.preguntas.filtro.CriteriosBusqueda;
import co.unicauca.bancopreguntas.preguntas.service.PreguntaService;
import co.unicauca.bancopreguntas.preguntas.web.adaptador.PreguntaRequestAdapter;
import co.unicauca.bancopreguntas.preguntas.web.dto.CambiarEstadoRequest;
import co.unicauca.bancopreguntas.preguntas.web.dto.PaginaResponse;
import co.unicauca.bancopreguntas.preguntas.web.dto.PreguntaRequest;
import co.unicauca.bancopreguntas.preguntas.web.dto.PreguntaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/preguntas")
@Tag(name = "Preguntas", description = "Autoria, ciclo de vida y consulta de preguntas")
public class PreguntaController {

    private final PreguntaService service;

    public PreguntaController(PreguntaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una pregunta (HU-1)",
            description = "Aplica la validacion estructural (RF-08 a RF-13). La pregunta nace en Borrador y se "
                    + "publica el evento PreguntaCreada.")
    @ApiResponse(responseCode = "201", description = "Pregunta creada")
    @ApiResponse(responseCode = "400", description = "La pregunta no cumple la validacion estructural")
    public PreguntaResponse crear(@RequestBody PreguntaRequest request) {
        return PreguntaResponse.from(service.crear(new PreguntaRequestAdapter(request), request.usuarioId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar una pregunta en Borrador (RF-06)")
    @ApiResponse(responseCode = "409", description = "La pregunta no esta en Borrador o el usuario no es el autor")
    public PreguntaResponse actualizar(@PathVariable Long id, @RequestBody PreguntaRequest request) {
        return PreguntaResponse.from(service.actualizar(id, new PreguntaRequestAdapter(request), request.usuarioId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una pregunta")
    @ApiResponse(responseCode = "404", description = "No existe")
    public PreguntaResponse buscar(@PathVariable Long id) {
        return PreguntaResponse.from(service.buscar(id));
    }

    @GetMapping
    @Operation(summary = "Listar preguntas con filtros y paginacion (HU-3, RF-07)",
            description = "Todos los filtros son opcionales y se combinan con AND. La pagina empieza en 0.")
    public PaginaResponse<PreguntaResponse> listar(
            @RequestParam(required = false) Long autorId,
            @RequestParam(required = false) CodigoEstado estado,
            @RequestParam(required = false) String tema,
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) Dificultad dificultad,
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        CriteriosBusqueda criterios = new CriteriosBusqueda(autorId, estado, tema, competencia, dificultad, texto);
        return PaginaResponse.from(service.buscar(criterios, pagina, tamano), PreguntaResponse::from);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de una pregunta (HU-2)",
            description = "PENDIENTE_REVISION (el autor envia a revision; publica PreguntaEnviadaARevision), "
                    + "BORRADOR (el autor retoma una rechazada), PUBLICADA o ARCHIVADA.")
    @ApiResponse(responseCode = "409", description = "Transicion no permitida desde el estado actual")
    public PreguntaResponse cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoRequest request) {
        return PreguntaResponse.from(service.cambiarEstado(id, request.estado(), request.usuarioId()));
    }
}
